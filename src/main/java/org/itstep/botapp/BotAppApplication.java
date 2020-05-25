package org.itstep.botapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.telegram.telegrambots.ApiContextInitializer;

@SpringBootApplication
public class BotAppApplication {

    public static void main(String[] args) {
        ApiContextInitializer.init();
        SpringApplication.run(BotAppApplication.class, args);
    }

}
