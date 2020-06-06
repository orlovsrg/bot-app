package org.itstep.botapp.api;

import org.itstep.botapp.bot.TelegramBot;
import org.itstep.botapp.model.Equipment;
import org.itstep.botapp.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/user")
public class UserApi {
    @Autowired
    private final ChatService chatService;
    @Autowired
    private final TelegramBot telegramBot;

    public UserApi(ChatService chatService, TelegramBot telegramBot) {
        this.chatService = chatService;
        this.telegramBot = telegramBot;
    }


    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public void sendMessage(@RequestBody Equipment equipment){
        System.out.println(equipment);
        telegramBot.sendChangePrice(equipment);
    }

}
