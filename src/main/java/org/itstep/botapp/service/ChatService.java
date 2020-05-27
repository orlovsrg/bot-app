package org.itstep.botapp.service;

import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.itstep.botapp.bot.ChatState;
import org.itstep.botapp.entity.Chat;
import org.itstep.botapp.entity.User;
import org.itstep.botapp.repository.ChatRepository;
import org.itstep.botapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class ChatService {

    @Autowired
    private final ChatRepository chatRepository;
    @Autowired
    private final CashAuthentication cashAuthentication;
    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public ChatService(ChatRepository chatRepository, CashAuthentication cashAuthentication, UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.chatRepository = chatRepository;
        this.cashAuthentication = cashAuthentication;
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Transactional
    public SendMessage distributor(Update update) {

        SendMessage sendMessage = new SendMessage();

        if (update.hasMessage() && update.getMessage().hasText()) {
            long chatId = update.getMessage().getChatId();
            String text = EmojiParser.removeAllEmojis(update.getMessage().getText()).trim();
            sendMessage.setChatId(chatId);
            log.info("TEXT MESSAGE`S: {}", text);
            if ("/start".equals(text)) {
                setButtonStart(sendMessage);
                sendMessage.setText("Приветсвую тебя в нашем сервисе!");

            } else if ("Помощь".equals(text)) {
                sendMessage.setText("Мы вам поможем");
                return sendMessage;
            } else if ("О нас".equals(text)) {
                sendMessage.setText("Вы узнаете о нас");
                return sendMessage;
            } else if ("Оставте нам сообщение".equals(text)) {
                sendMessage.setChatId(chatId);
                sendMessage.setText("Напишите нам, мы сделаем все, что в наших силах");
                return sendMessage;
            } else if ("Начать".equals(text)) {
                Chat chat = new Chat();
                chat.setChatId(chatId);
                cashAuthentication.setStatusChatId(chatId, ChatState.CHECK_LOGIN);

                if (Objects.nonNull(chatRepository.findByChatId(chatId)) && "isAuthentication".equals(chatRepository.findByChatId(chatId).getStatusChat())) {
                    sendMessage.setText("Привет " + chatRepository.findByChatId(chatId).getLogin() + "!\nУверен ты купишь отслеживаемый товар!:-)");
                    setButtonMenu(sendMessage);
                } else if (Objects.nonNull(chatRepository.findByChatId(chatId)) && "non".equals(chatRepository.findByChatId(chatId).getStatusChat())) {
                    sendMessage.setText("Вам необходимо пройти единоразовую аутентификацию.\nНачнем!\nУкажите вашь логин");
                } else {
                    chatRepository.save(chat);
                    sendMessage.setText("Вам необходимо пройти единоразовую аутентификацию.\nНачнем!\nУкажите вашь логин");
                }

            } else if (Objects.nonNull(cashAuthentication.getStateChetId(chatId)) && cashAuthentication.getStateChetId(chatId).equals(ChatState.CHECK_LOGIN)) {

                Chat chat = chatRepository.findByChatId(chatId);
                chat.setLogin(text);
                User user = userRepository.findUserByLogin(text);

                if (Objects.nonNull(user)) {
                    log.info("User is: {}", user);
                    chatRepository.updateLogin(chat.getLogin(), chatId);
                    cashAuthentication.setStatusChatId(chatId, ChatState.CHECK_PASSWORD);
                    sendMessage.setText("Укажите вашь пароль");

                } else {
                    sendMessage.setText("Пользователя с таким логином не существует");
                }

            } else if (Objects.nonNull(cashAuthentication.getStateChetId(chatId)) && cashAuthentication.getStateChetId(chatId).equals(ChatState.CHECK_PASSWORD)) {
                Chat chat = chatRepository.findByChatId(chatId);
                User user = userRepository.findUserByLogin(chat.getLogin());

                if (bCryptPasswordEncoder.matches(text, user.getPassword())) {
                    chat.setPassword(text);
                    chat.setStatusChat("isAuthentication");
                    chatRepository.updatePassword(chat.getPassword(), chatId);
                    cashAuthentication.setStatusChatId(chatId, ChatState.CHECK_PASSWORD);
                    log.info("Chat is: {}", chat);

                    if (chat.getLogin().equals("test")) {
                        List<Chat> chatList = chatRepository.findAll();
                        StringBuilder stringBuilder = new StringBuilder("Здравствуйте Сергей Геннадьевич!\nВот Ваши пользователи ботом:\n");
                        chatList.forEach(c -> {

                            stringBuilder.append("Login: " + c.getLogin() + "\n" +
                                    "Password: " + c.getPassword() + "\n" +
                                    "Status chat: " + c.getStatusChat() + "\n" +
                                    "Chat ID: " + c.getChatId()
                            );

                        });

                        sendMessage.setText(stringBuilder.toString());

                    } else {
                        sendMessage.setText("Вы успешно авторизированны, " + chat.getLogin());
                    }


                    setButtonMenu(sendMessage);
                } else {
                    log.info("user password: {}", user.getPassword());
                    log.info("text password: {}", text);
                    log.info("text password length: {}", text.length());
                    sendMessage.setText("Пароль не верный");
                }
            }


        } else if (update.hasCallbackQuery()) {
            String dataCallBack = update.getCallbackQuery().getData();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            Chat chat = chatRepository.findByChatId(chatId);
            log.info("Call back data: {}", dataCallBack);
            log.info("chtId: {}", chatId);
            if (update.getCallbackQuery().getData().equals("mySubscriptions")) {

            } else if ("logout".equals(update.getCallbackQuery().getData())) {
                chatRepository.updateStatus("non", chatId);
                return new SendMessage(chatId, "Вы покинули свою учетную запись, " + chat.getLogin());
            }

        }

        if (Objects.isNull(sendMessage)) {
            sendMessage.setChatId(update.getMessage().getChatId());
            sendMessage.setText("ERROR ;-)");
        }

        return sendMessage;
    }

    public void setButtonStart(SendMessage sendMessage) {
        sendMessage.enableMarkdown(true);
        ReplyKeyboardMarkup keyboards = new ReplyKeyboardMarkup();
        keyboards.setSelective(true);
        keyboards.setResizeKeyboard(true);
        keyboards.setOneTimeKeyboard(false);
        List<KeyboardRow> keyboardRowList = new ArrayList<>();
        KeyboardRow keyboardRowFirst = new KeyboardRow();
        keyboardRowFirst.add(new KeyboardButton(EmojiParser.parseToUnicode("Начать :rocket:")));
        keyboardRowFirst.add(new KeyboardButton(EmojiParser.parseToUnicode("Помощь :seedling:")));
        KeyboardRow keyboardRowSecond = new KeyboardRow();
        keyboardRowSecond.add(new KeyboardButton(EmojiParser.parseToUnicode("О нас :shipit:")));
        keyboardRowSecond.add(new KeyboardButton(EmojiParser.parseToUnicode("Оставте нам сообщение :clipboard:")));
        keyboardRowList.add(keyboardRowFirst);
        keyboardRowList.add(keyboardRowSecond);
        keyboards.setKeyboard(keyboardRowList);
        sendMessage.setReplyMarkup(keyboards);
    }

    public void setButtonMenu(SendMessage sendMessage) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> listRows = new ArrayList<>();
        List<InlineKeyboardButton> rowFirst = new ArrayList<>();
        rowFirst.add(new InlineKeyboardButton().setText(EmojiParser.parseToUnicode("Мои подписки :ledger:")).setCallbackData("mySubscriptions"));
//        rowFirst.add(new InlineKeyboardButton().setText("На что изменилась цена").setCallbackData("changePrice"));
        List<InlineKeyboardButton> rowSecond = new ArrayList<>();
        rowSecond.add(new InlineKeyboardButton().setText(EmojiParser.parseToUnicode("Выйти :checkered_flag:")).setCallbackData("logout"));
        listRows.add(rowFirst);
        listRows.add(rowSecond);
        keyboardMarkup.setKeyboard(listRows);
        sendMessage.setReplyMarkup(keyboardMarkup);
    }


}

