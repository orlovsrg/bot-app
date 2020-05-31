package org.itstep.botapp.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.telegram.telegrambots.meta.api.objects.Message;

import javax.persistence.*;
import java.util.Objects;

@Getter
@Setter
@Entity
public class UserMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(columnDefinition = "TEXT")
    private String message;

    @ManyToOne
    @JoinColumn(name = "chat_id", referencedColumnName = "id")
    private Chat chat;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserMessage that = (UserMessage) o;
        return id == that.id &&
                Objects.equals(message, that.message) &&
                Objects.equals(chat, that.chat);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, message, chat);
    }
}
