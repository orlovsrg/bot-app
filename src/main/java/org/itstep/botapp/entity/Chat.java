package org.itstep.botapp.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.itstep.botapp.bot.ChatState;

import javax.persistence.*;

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

    @Override
    public int compareTo(Chat o) {
        return this.chatId.compareTo(o.chatId);
    }
}
