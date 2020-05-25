package org.itstep.botapp.repository;

import org.itstep.botapp.entity.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRepository extends CrudRepository<Chat, Long> {
    Chat findByChatId(long chatId);

    @Modifying
    @Query("update Chat c set c.login = :login where c.userId = :userId")
    void updateLogin(@Param("login") String login, @Param("userId") long userId);
    @Modifying
    @Query("update Chat c set c.password = :password where c.chatId = :chatId")
    void updatePassword(@Param("password") String password, @Param("chatId") long chatId);
}
