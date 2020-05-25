package org.itstep.botapp.service;

import lombok.extern.slf4j.Slf4j;
import org.itstep.botapp.bot.ChatState;
import org.itstep.botapp.entity.Chat;
import org.itstep.botapp.entity.User;
import org.itstep.botapp.repository.ChatRepository;
import org.itstep.botapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final PasswordEncoder passwordEncoder;

    public ChatService(ChatRepository chatRepository, CashAuthentication cashAuthentication, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.chatRepository = chatRepository;
        this.cashAuthentication = cashAuthentication;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public SendMessage distributor(Update update) {

        SendMessage sendMessage = new SendMessage();

        if (update.hasMessage() && update.getMessage().hasText()) {

            long chatId = update.getMessage().getChatId();
            String text = update.getMessage().getText();
            sendMessage.setChatId(chatId);

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
                chat.setPassword(text);
                chat.setStatusChat("isAuthentication");
                chatRepository.updatePassword(chat.getPassword(), chatId);
                cashAuthentication.setStatusChatId(chatId, ChatState.CHECK_PASSWORD);
                log.info("Chat is: {}", chat);
                sendMessage.setText("Вы успешно авторизированны");
                setButtonMenu(sendMessage);
            }


        } else if (update.hasCallbackQuery()) {

            String dataCallBack = update.getCallbackQuery().getData();

            if (update.getCallbackQuery().getData().equals("mySubscriptions")) {

            } else if ("changePrice".equals(update.getCallbackQuery().getData())) {

            } else if ("login".equals(update.getCallbackQuery().getData())) {

            } else if ("logout".equals(update.getCallbackQuery().getData())) {


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
        keyboardRowFirst.add(new KeyboardButton("Начать"));
        keyboardRowFirst.add(new KeyboardButton("Помощь"));
        KeyboardRow keyboardRowSecond = new KeyboardRow();

        keyboardRowSecond.add(new KeyboardButton("О нас"));
        keyboardRowSecond.add(new KeyboardButton("Оставте нам сообщение"));
        keyboardRowList.add(keyboardRowFirst);
        keyboardRowList.add(keyboardRowSecond);
        keyboards.setKeyboard(keyboardRowList);
        sendMessage.setReplyMarkup(keyboards);

    }

    public void setButtonMenu(SendMessage sendMessage) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> listRows = new ArrayList<>();

        List<InlineKeyboardButton> rowFirst = new ArrayList<>();
        rowFirst.add(new InlineKeyboardButton().setText("Мои подписки").setCallbackData("mySubscriptions"));
        rowFirst.add(new InlineKeyboardButton().setText("На что изменилась цена").setCallbackData("changePrice"));

        List<InlineKeyboardButton> rowSecond = new ArrayList<>();
//        rowSecond.add(new InlineKeyboardButton().setText("Войти").setCallbackData("login"));
        rowSecond.add(new InlineKeyboardButton().setText("Выйти").setCallbackData("logout"));

        listRows.add(rowFirst);
        listRows.add(rowSecond);

        keyboardMarkup.setKeyboard(listRows);
        sendMessage.setReplyMarkup(keyboardMarkup);
    }


}

