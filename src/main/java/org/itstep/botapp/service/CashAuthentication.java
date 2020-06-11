package org.itstep.botapp.service;

import org.itstep.botapp.bot.ChatState;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentSkipListMap;

@Service
public class CashAuthentication {
    Map<Long, ChatState> chatMap = new ConcurrentSkipListMap<>();

   public synchronized ChatState getStateChetId(long chatId){
       return chatMap.get(chatId);
   }

   public synchronized void setStatusChatId(long chatId, ChatState chatState){
       chatMap.put(chatId, chatState);
   }

   public void clearCache(long charId){
       chatMap.remove(charId);
   }

}
