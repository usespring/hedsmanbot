# hedsmanbot
A project run to recognize and remove annoying bots and messages in a telegram group.

### Before you start, You have to follow these instructions:
1. Set the allow write permission on the *expression.xml* file located in root directory.
2. Set your username or whoever you want to have accesses to use bot command at `TelegramLongPollingBot.hasPermissionForUser()` method.
3. Set your target groupId, where you are going to add the bot, at `TelegramLongPollingBot.hasPermissionGroup()` method.
4. Set your bot's username at `TelegramLongPollingBot.getBotUsername()` method and its token at `TelegramLongPollingBot.getBotToken()`
5. Then you can add your bot as an admin member to your target supergroup.
6. Please notice that, The application has not any api or a user interface like home.html, you can only see the application's status by tracing log file or trying `/start` command to know whether it works correctly or not. 
7. You can see the bot command instruction by typing `/start` at the bot's PV. 

**Please feel free to contact me at telegram [@abbasghahreman](https://web.telegram.org/#/im?p=@abbasghahreman)**
