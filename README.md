# hedsmanbot(telegram bot)
A java project run to recognize and remove annoying bots and messages in telegram.

### Before you start:
### You have to have thoso ids:
1. Your supergroup chatId, For obtain it, First, you have to add your bot as an admin to your soupergroup. After sending first message, by https://api.telegram.org/bot<your bot token>/getUpdates you can find group chat id in the json reponse.
2. Ids of those admins that you want to access to the bot command. For this, You want an admin to send a message to your bot PM and then you can find her id by https://api.telegram.org/bot<your bot token>/getUpdates.
3. Ids of the channel that contains all your soupergroup admins, your bot will send to the channel all her activities include what messages he has deleted and what bots he has kicked.
  
### And set those ids at:
1. Set an id of your soupergroups at the `TelegramLongPollingBot.existGroup()` ,also at `TelegramLongPollingBot.findAllGroupIdsByAdminChatId()`.
2. Set all ids of your supergroup admins at `TelegramLongPollingBot.isAsAdmin()`.
3. Set your channels id at `TelegramLongPollingBot.getChannelIdBygroupId()`.

### About deploying and running application:
4. The application has not any api or a user interface like home.html, you can only see the application's status by tracing log file or seeing every respone to every command. 
5. Set the allow write permission on the *expression.xml* file located in root directory.
6. Set enviroment variable with name `bot.token` and value `your bot token`. 

### How dose the bot work:
. Acording to above, in order to access to the bot command, be sure your id is exist at `TelegramLongPollingBot.isAsAdmin()`.
. You can start by sending `/start` at the bot's PM. The bot recieves you expressions and save them into the expression.xml file. 

**Please feel free to contact me at telegram [@abbasghahreman](https://web.telegram.org/#/im?p=@abbasghahreman)**
