package org.itstep.botapp.config;

import lombok.Getter;
import lombok.Setter;
import org.itstep.botapp.bot.TelegramBot;
import org.itstep.botapp.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "telegrambot")
public class BotConfig {

    private String botUserName;
    private String botToken;


    public BotConfig() {

    }

    @Bean
    public TelegramBot telegramBot(){
        TelegramBot telegramBot = new TelegramBot();
        telegramBot.setBotUserName(botUserName);
        telegramBot.setBotToken(botToken);
        return telegramBot;
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
