package io.github.headsmanbot;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;

@SpringBootApplication
public class ApplicationInitializer {
    private static final Logger logger = LogManager.getLogger("ApplicationInitializer");

    public static void main(String[] args) {
        SpringApplication.run(ApplicationInitializer.class, args);

        //step1. Initialize Api Context
        ApiContextInitializer.init();
        logger.info("Api Context has initialized");
        //step2. Instantiate Telegram Bots API
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        logger.info("Telegram Bots API has instantiated");
        //step3. Register our bot
        try {
            telegramBotsApi.registerBot(new HeadsmanBot());
            logger.info("HeadsmanBot has registered!");
            //other example
//            telegramBotsApi.registerBot(new ChannelHandlers());
//            telegramBotsApi.registerBot(new DirectionsHandlers());
//            telegramBotsApi.registerBot(new RaeHandlers());
//            telegramBotsApi.registerBot(new WeatherHandlers());
//            telegramBotsApi.registerBot(new TransifexHandlers());
//            telegramBotsApi.registerBot(new FilesHandlers());
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        }
    }
}

