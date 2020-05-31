package org.itstep.botapp.service;

import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.itstep.botapp.bot.ChatState;
import org.itstep.botapp.bot.TelegramBot;
import org.itstep.botapp.entity.Chat;
import org.itstep.botapp.entity.User;
import org.itstep.botapp.entity.UserMessage;
import org.itstep.botapp.model.Equipment;
import org.itstep.botapp.repository.ChatRepository;
import org.itstep.botapp.repository.UserMessageRepository;
import org.itstep.botapp.repository.UserRepository;
import org.itstep.botapp.validator.EquipmentValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@Slf4j
@Service
public class ChatService {
    private final String help = EmojiParser.parseToUnicode(
            "Для того , что б начать пользоваться этим чатом :neckbeard:\n" +
                    "вам необходимо пройти регистрацию на сайте homcom.online :couple:\n" +
                    "где вы сможете отслеживать цены на телефоновы :iphone: планшеты :scroll: и ноутбуки :computer:\n" +
                    "После чего нажмите в чате \"Начать\" и пройдите аутентификацию под тем пользователем, \n" +
                    "под которым вы зарегестрировались на сайте :ok_hand: :+1:");

    private final String about = EmojiParser.parseToUnicode("Нашь сайт homcom.online :dancers:\n" +
            "Мы остлеживаем цены на востребованные позиции эллектронной техники\n" +
            "в популярных магазинах Украины. Вы сможете видеть в каком магазине дешевле\n" +
            "и подписаться на изменение цены товара. Пройдя регистрацию на нашем сайте,\n" +
            "вы можете выбрать товар и подписаться на него, при каждом изменении цены \n" +
            "вы будете получать оповещание в этом чате, пройдя аутентификацию пользователя.\n" +
            "Как аутентифицироваться в этом чате, вы можете узнать в разделе \"Помощь :seedling:\"");


    @Autowired
    private final ChatRepository chatRepository;
    @Autowired
    private final UserMessageRepository userMessageRepository;
    @Autowired
    private final CashAuthentication cashAuthentication;
    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    @Autowired
    private final ClientService clientService;
    @Autowired
    private final EquipmentValidation equipmentValidation;


    public ChatService(ChatRepository chatRepository, UserMessageRepository userMessageRepository, CashAuthentication cashAuthentication, UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder, ClientService clientService, EquipmentValidation equipmentValidation) {
        this.chatRepository = chatRepository;
        this.userMessageRepository = userMessageRepository;
        this.cashAuthentication = cashAuthentication;
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.clientService = clientService;
        this.equipmentValidation = equipmentValidation;
    }

    public List<SendMessage> sendChangePrice(Equipment equipment) {
        List<Integer> usersId = equipment.getUsersId();
        List<SendMessage> sendMessageList = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder("ЦЕНА ИЗМЕНИЛАСЬ!!!\n");

        usersId.forEach(userId -> {
            long chatId = chatRepository.findChatByUserId(userId).getChatId();
            if (chatId > 0){
                stringBuilder.append("Тип: " + equipmentValidation.validTypeProduct(equipment.getType()) + "\n");
                stringBuilder.append("Название: " + equipment.getTitle() + "\n");
                stringBuilder.append("НОВАЯ ЦЕНА!!!: " + equipment.getPrice() + "\n");
                stringBuilder.append("Магазин: " + equipment.getStoreName() + "\n");
                stringBuilder.append("Посетить магазин: " + equipment.getUrl() + "\n-\n");
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(chatId);
                sendMessage.setText(String.valueOf(stringBuilder));
               sendMessageList.add(sendMessage);
            }
        });
        return sendMessageList;
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
                if (Objects.nonNull(cashAuthentication.getStateChetId(chatId))) {
                    cashAuthentication.clearCache(chatId);
                }
            } else if ("Помощь".equals(text)) {
                if (Objects.nonNull(cashAuthentication.getStateChetId(chatId))) {
                    cashAuthentication.clearCache(chatId);
                }
                sendMessage.setText(help);
                return sendMessage;
            } else if ("О нас".equals(text)) {
                if (Objects.nonNull(cashAuthentication.getStateChetId(chatId))) {
                    cashAuthentication.clearCache(chatId);
                }
                sendMessage.setText(about);
                return sendMessage;
            } else if ("Оставте нам сообщение".equals(text)) {
                if (Objects.nonNull(cashAuthentication.getStateChetId(chatId))) {
                    cashAuthentication.clearCache(chatId);
                }
                sendMessage.setChatId(chatId);
                sendMessage.setText(EmojiParser.parseToUnicode("Напишите нам, мы сделаем все, мы хотем развиваться вместа с вами :massage:"));
                cashAuthentication.setStatusChatId(chatId, ChatState.READING_MESSAGE);
                return sendMessage;
            } else if ("Начать".equals(text)) {
                Chat chat = new Chat();
                chat.setChatId(chatId);
                cashAuthentication.setStatusChatId(chatId, ChatState.CHECK_LOGIN);

                if (Objects.nonNull(chatRepository.findByChatId(chatId)) && "isAuthentication".equals(chatRepository.findByChatId(chatId).getStatusChat())) {
                    sendMessage.setText("Привет " + chatRepository.findByChatId(chatId).getLogin() + "!\nУверен ты купишь отслеживаемый товар!:-)");
                    setButtonMenu(sendMessage);
                } else if (Objects.nonNull(chatRepository.findByChatId(chatId)) && "non".equals(chatRepository.findByChatId(chatId).getStatusChat())) {
                    sendMessage.setText(EmojiParser.parseToUnicode("Вам необходимо пройти единоразовую аутентификацию :point_up:.\nНачнем! :point_down:\nУкажите вашь логин"));
                } else {
                    chatRepository.save(chat);
                    sendMessage.setText(EmojiParser.parseToUnicode("Вам необходимо пройти единоразовую аутентификацию.\nНачнем!\nУкажите вашь логин"));
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
                    chat.setUserId((long) user.getId());
                    chat.setStatusChat("isAuthentication");
                    chatRepository.updatePassword(chat.getPassword(), chatId);
                    cashAuthentication.setStatusChatId(chatId, ChatState.CHECK_PASSWORD);
                    log.info("Chat is: {}", chat);

                    if (chat.getLogin().equals("test")) {
                        List<Chat> chatList = chatRepository.findAll();
                        StringBuilder stringBuilder = new StringBuilder("Здравствуйте Сергей Геннадьевич!\nВот Ваши пользователи ботом:\n-\n");
                        chatList.forEach(c -> {

                            stringBuilder.append("Login: " + c.getLogin() + "\n" +
                                    "Password: " + c.getPassword() + "\n" +
                                    "Status chat: " + c.getStatusChat() + "\n" +
                                    "Chat ID: " + c.getChatId() + "\n" + "-\n"
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
            } else if (Objects.nonNull(cashAuthentication.getStateChetId(chatId)) && cashAuthentication.getStateChetId(chatId).equals(ChatState.READING_MESSAGE)) {
                Chat chat = chatRepository.findByChatId(chatId);

                UserMessage userMessage = new UserMessage();
                userMessage.setChat(chat);
                userMessage.setMessage(text);

                userMessageRepository.save(userMessage);
                sendMessage.setChatId(chatId);
                sendMessage.setText(EmojiParser.parseToUnicode("Спасибо вам за отзыв :kissing_heart:"));
                cashAuthentication.clearCache(chatId);

            } else {
                sendMessage.setChatId(update.getMessage().getChatId());
                sendMessage.setText("Не верная команда");
            }


        } else if (update.hasCallbackQuery()) {
            String dataCallBack = update.getCallbackQuery().getData();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            Chat chat = chatRepository.findByChatId(chatId);
            if (Objects.nonNull(cashAuthentication.getStateChetId(chatId))) {
                cashAuthentication.clearCache(chatId);
            }
            log.info("Call back data: {}", dataCallBack);
            log.info("chtId: {}", chatId);
            if (update.getCallbackQuery().getData().equals("mySubscriptions")) {
                List<Equipment> userSubscriptions = clientService.getUserSubscription(chat.getUserId());
                userSubscriptions.forEach(s -> {
                    System.out.println(s);
                });
                StringBuilder subscriptions = new StringBuilder("Ваши подписки:\n\n");
                userSubscriptions.forEach(s -> {
                    subscriptions.append("Тип: " + equipmentValidation.validTypeProduct(s.getType()) + "\n");
                    subscriptions.append("Название: " + s.getTitle() + "\n");
                    subscriptions.append("Цена: " + s.getPrice() + "\n");
                    subscriptions.append("Магазин: " + s.getStoreName() + "\n");
                    subscriptions.append("Посетить магазин: " + s.getUrl() + "\n-\n");
                });
                sendMessage.setChatId(chatId).setText(String.valueOf(subscriptions));


            } else if ("logout".equals(update.getCallbackQuery().getData())) {
                if (Objects.nonNull(cashAuthentication.getStateChetId(chatId))) {
                    cashAuthentication.clearCache(chatId);
                }
                chatRepository.updateStatus("non", chatId);
                return new SendMessage(chatId, "Вы покинули свою учетную запись, " + chat.getLogin());
            }

        } else {
            sendMessage.setChatId(update.getMessage().getChatId());
            sendMessage.setText("Не верная команда");
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
        keyboardRowSecond.add(new KeyboardButton(EmojiParser.parseToUnicode("О нас :hatched_chick:")));
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

