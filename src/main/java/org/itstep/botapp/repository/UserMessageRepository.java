package org.itstep.botapp.repository;

import org.itstep.botapp.entity.UserMessage;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserMessageRepository extends CrudRepository<UserMessage, Long> {
        UserMessage findUserMessageByChatChatId(long chatId);
}
