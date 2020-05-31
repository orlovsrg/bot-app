package org.itstep.botapp.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.itstep.botapp.bot.ChatState;
import org.telegram.telegrambots.meta.api.objects.User;

import javax.persistence.*;
import java.util.Set;

@ToString
@Getter
@Setter
@Entity
public class Chat implements Comparable<Chat>{

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Long chatId;
    private String login;
    private String password;
    private Long userId;
    private String statusChat = "non";

    @OneToMany(mappedBy = "chat")
    private Set<UserMessage> userMessageSet;

    @Override
    public int compareTo(Chat o) {
        return this.chatId.compareTo(o.chatId);
    }
}
