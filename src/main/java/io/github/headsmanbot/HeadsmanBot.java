package io.github.headsmanbot;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.groupadministration.KickChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.xml.bind.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.PatternSyntaxException;

/*
    @see https://github.com/rubenlagus/TelegramBots/wiki/Getting-Started
 */
//// TODO: 12/27/2018  the project structure must be refactored.
//// TODO: 12/27/2018 also refactor for naming, returning types, exception handlers , or etc
public class HeadsmanBot extends TelegramLongPollingBot {

    //// TODO: 12/27/2018 The place and the text of loggers must be proper
    private static final Logger logger = LogManager.getLogger("TelegramLongPollingBot");

    @Override
    //all telegram updates that relevant to the bot is received in this method
    public void onUpdateReceived(Update update) {
        try {
            logger.debug(update.toString());
            Message message = update.getMessage();
            Chat chat = message.getChat();
            Long chatId = message.getChatId();
            Boolean hasMessage = update.hasMessage(); //status 1 -a message has been sent
            Boolean hasNewChatMember = message.getNewChatMembers() != null; //status 2 -a user has been added
            Boolean messageHasText = message.hasText();
            Boolean messageHasCaption = message.getCaption() != null;
            if (messageHasText) {
                Boolean isPrivateChat = chat.isUserChat(); //status 1.1
                Boolean isSuperGroupChat = chat.isSuperGroupChat(); //status 1.2
                Boolean isGroupChat = chat.isGroupChat(); //status 1.2 also
                Boolean isChannelChat = chat.isChannelChat(); //status 1.3
                if (isPrivateChat) {
                    String privateReplyText = "";
                    if (messageHasText) {
                        Integer userChatId = message.getFrom().getId();
                        String messageText = message.getText();
                        if (hasPermission(userChatId)) {
                            privateReplyText = handleUserCommand(messageText, chatId);
                        } else {
                            privateReplyText = "Access denied!";
                        }
                    } else {
                        privateReplyText = "Please send a command like /start";
                    }
                    sendMessage(chatId, privateReplyText);
                } else if (isSuperGroupChat || isGroupChat) {
                    if (hasPermissionGroup(chatId)) {
                        String messageUsername = message.getFrom().getUserName();
                        int messageId = message.getMessageId();
                        String textOrCaption = "";
                        if (messageHasText) {
                            textOrCaption = message.getText();
                        } else if (messageHasCaption) {
                            textOrCaption = message.getCaption();
                        }
                        handleAnnoyingMessage(textOrCaption, chatId, messageId, messageUsername);
                    } else {
                        //Please notice:The bot works only a registered group
                    }
                } else if (isChannelChat) {
                    //// TODO: 12/27/2018 write handler for channels
                }
            } else if (hasNewChatMember) {
                //kick all new bots
                List<User> bots = findAllBots(message.getNewChatMembers());
                for (User bot : bots) {
                    kickChatMember(chatId,bot);
                }
            }
        } catch (TelegramApiException e) {
            e.printStackTrace();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    //// TODO: 12/27/2018 the method's return type must be change better
    private List<User> findAllBots(List<User> newChatMembers) {
        List<User> bots = new ArrayList<>();
        for (User newChatMember : newChatMembers) {
            if (newChatMember.getBot()) {
                bots.add(newChatMember);
            }
        }
        return bots;
    }

    //// TODO: 12/27/2018 the method's return type must be change better
    private Map.Entry<String, Long> hasMatchExpression(String messageText) {
        Expressions expressions = loadExpressionsFromXml();
        Map<String, Long> expressionAndAdminChatIds = expressions.expressionAndAdminChatId;
        for (Map.Entry<String, Long> expressionAndAdminChatId : expressionAndAdminChatIds.entrySet()) {
            String expression = expressionAndAdminChatId.getKey();
            try {

                boolean isMatched = messageText.contains(expression);
                if (isMatched) {
                    return expressionAndAdminChatId;
                }
            } catch (PatternSyntaxException e) {
                logger.error(e.getMessage());
            }
        }
        return null;
    }

    private void handleAnnoyingMessage(String messageText, Long chatId, int messageId, String messageUsername) throws TelegramApiException {
        Map.Entry<String, Long> expressionAndAdminChatId = hasMatchExpression(messageText);
        Long ownerExpressionChatId = expressionAndAdminChatId.getValue();
        String expression = expressionAndAdminChatId.getKey();
        if (ownerExpressionChatId != 0L) {

            //step1. delete annoying message
            deleteMessage(chatId, messageId);
            //step2. send a message to info owner expression
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Hi admin. The following message has removed with your expression <b>");
            stringBuilder.append(expression);
            stringBuilder.append("</b> sent by @");
            stringBuilder.append(messageUsername);
            stringBuilder.append(" :\n <b>");
            stringBuilder.append(messageText);
            stringBuilder.append("</b>");
            //// TODO: 12/31/2018 send message in admins channel
//            sendMessage(ownerExpressionChatId, stringBuilder.toString());

            StringBuilder stringBuilderLog = new StringBuilder();
            stringBuilderLog.append("Delete a message with id:");
            stringBuilderLog.append(messageId);
            stringBuilderLog.append(" and text:");
            stringBuilderLog.append(messageText);
            stringBuilderLog.append(" in the chatId:");
            stringBuilderLog.append(chatId);
            logger.info(stringBuilderLog.toString());
        }
    }

    //// TODO: 12/27/2018 all of the admins' group must be allowed to command automatically
    private Boolean hasPermission(Integer userChatId) {
        List<Long> groupIds = findAllGroupIdsByAdminChatId(userChatId);
        Boolean userIsAdmin=groupIds.size()>0;
        return userIsAdmin;
    }

    //// TODO: 12/30/2018 Define a repository to save and load group and users information
    private List<Long> findAllGroupIdsByAdminChatId(Integer adminChatId) {
        List<Long> groupIds = new ArrayList<>();
        // for example: my test group id
        groupIds.add(-1001213671004L);
        return groupIds;
    }

    //// TODO: 12/27/2018 add an application's interface form to register new group and save them to a repository
    private boolean hasPermissionGroup(Long chatId) {
        if (chatId == -1001140328233L || chatId == -1001213671004L) {
            return true;
        }
        return false;
    }

    //// TODO: 12/27/2018 add automation test
    private void rewriteExpressionsToXml(Expressions expressions) {
        try {
            File file = loadExpressionFile();
            JAXBContext jaxbContext = JAXBContext.newInstance(Expressions.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            jaxbMarshaller.marshal(expressions, file);
            jaxbMarshaller.marshal(expressions, System.out);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    //// TODO: 12/27/2018 add automation test
    private File loadExpressionFile() {
        File file = new File("expressions.xml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    // TODO: 12/27/2018 add automation test
    private Expressions loadExpressionsFromXml() {
        try {
            File expressionXmlFile = loadExpressionFile();
            JAXBContext jaxbContext = JAXBContext.newInstance(Expressions.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            Expressions expressions = (Expressions) jaxbUnmarshaller.unmarshal(expressionXmlFile);
            return expressions;
        } catch (UnmarshalException e) {
            e.printStackTrace();
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return new Expressions();
    }

    //// TODO: 12/27/2018 improve bot commands to be clearer and more user friendly
    //// TODO: 12/27/2018 make the replyText's text clearer
    //// TODO: 12/27/2018 add more commands like /help, /stop, /listExpressions , or etc
    private String handleUserCommand(String messageText, Long chatId) throws TelegramApiException {
        String replyText = "";
        if (messageText.startsWith("e:")) {// expression start with 'e:'
            String expressionText = messageText.replaceAll("e:", "");
            if (!expressionText.isEmpty()) {
                Expressions expressions = loadExpressionsFromXml();
                expressions.expressionAndAdminChatId.put(expressionText, chatId);
                rewriteExpressionsToXml(expressions);
                replyText = "Your expression is:" + expressionText + "\n" +
                        "This expression will be applied soon.";
            } else {
                replyText = "Expressions has not to be empty! Try again";
            }
        } else if (messageText.equals("/start")) {
            replyText = "Hi admin!\nI will search your expression in every incoming message to find annoying message.\n" +
                    "You can send your expression by starting from 'e:'. For example:\n" +
                    "e:http -Every messages that contain a web link, will be removed.";
        } else {
            replyText = "Error! You have to send a expression with prefix 'e:'. For example:\n" +
                    "e:http -Every messages that contain a web link, will be removed.";
        }
        return replyText;
    }

    //// TODO: 12/27/2018 add a response handler
    private void sendMessage(Long chatId, String replyText) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage() // Create a SendMessage object with mandatory fields
                .setChatId(chatId)
                .setText(replyText)
                .enableHtml(true);
        execute(sendMessage); // Call method to send the message
        logger.info("sendMessage executed");
    }

    //// TODO: 12/27/2018 add a response handler
    private void kickChatMember(Long chatId, User bot) throws TelegramApiException {
       int botId=bot.getId();
        String botUsername=bot.getUserName();
        String botFullName=bot.getFirstName()+bot.getLastName();
        KickChatMember kickChatMemberRequest = new KickChatMember(chatId, botId);
        execute(kickChatMemberRequest);
        logger.info("kickChatMember executed");
        //// TODO: 12/31/2018 send message in admins channel
//        sendMessage();

    }

    //// TODO: 12/27/2018 add a response handler
    private void deleteMessage(Long chatId, int messageId) throws TelegramApiException {
        DeleteMessage deleteMessage = new DeleteMessage(chatId, messageId);
        execute(deleteMessage); // Call method to send the message
        logger.info("deleteMessage executed");
    }

    @Override
    public String getBotUsername() {
        // Return bot username without @
        return "Headsman_bot";
    }

    @Override
    public String getBotToken() {
        // Return bot token from BotFather
        Map<String, String> getenv = System.getenv();
        String toke = getenv.get("bot.token");
        logger.debug(toke);
        return toke="710629994:AAHLDhFDECl94V_mJ5WT81dZbM2w82D7bzE";
    }
}