package ru.noteandmark.bot.service;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.noteandmark.bot.dao.impl.ItemDaoImpl;
import ru.noteandmark.bot.domain.Item;
import ru.noteandmark.bot.domain.Product;
import ru.noteandmark.bot.domain.Store;
import ru.noteandmark.bot.readers.FileOperator;
import ru.noteandmark.bot.readers.PropertyReader;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static java.lang.Integer.*;

public class BotService extends TelegramLongPollingBot {

    private String botUserName;
    private String botToken;
    private String botPassword;
    private String codeToDelete = "-1";
    private int deleteFromWarehouse = -1;
    private String nameToDelete = "";
    private String codeToUpdate = "-1";
    private int updateFromWarehouse = -1;
    private float balance = 0.0F;
    private String nameToUpdate = "";
    private LinkedList<String> list;
    private Product product;
    private Item item;
    private Store store;
    private List<Item> items;
    private List<String> users;
    private ItemDaoImpl dao;
    private TypeButton typeButton;

    public static final String betFile = "betStore.txt";
    public static final String betLogging = "betLogging.txt";

    public BotService() {

        PropertyReader loader = new PropertyReader();
        try {
            Properties properties = loader.readProperties();
            botUserName = properties.getProperty("bot.username");
            botToken = properties.getProperty("bot.token");
            botPassword = properties.getProperty("bot.password");
        } catch (IOException e) {
            e.printStackTrace();
        }

        list = new LinkedList<>();
        list.add("init");
        item = new Item();
        store = new Store();
        items = new ArrayList<>();
        dao = new ItemDaoImpl();
        typeButton = new TypeButton();

        startupLoadDB();
    }

    @Override
    public String getBotUsername() {
        return botUserName;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {

        String userInput = "";
        boolean delete = false;
        boolean updated = false;

        if (update.hasMessage()) {
            Message message = update.getMessage();
            if (message.hasText()) {

                list.add(message.getText());

                if (!message.getText().equals("/password")) {
                    if (!users.contains(update.getMessage().getChatId().toString())) {
                        sendMsg(message, "Введите пароль от бота!", typeButton);
                        return;
                    }
                }
                userInput = list.get(list.size() - 2);

                switch (message.getText()) {
                    case "/add_book":
                        typeButton.setFirstButton("Добавить код товара");
                        typeButton.setSecondButton("Добавить название товара");
                        typeButton.setThirdButton("Добавить номер склада");
                        typeButton.setFourthButton("Указать количество");
                        typeButton.setCreateKeyboard(true);
                        sendMsg(message, "Пришлите код товара и нажмите кнопку \"Добавить код товара\"", typeButton);
                        product = new Product();
                        break;
                    case "Добавить код товара":
                        try {
                            product.setCodeId(userInput);
                        } catch (NumberFormatException exception) {
                            sendMsg(message, "Ошибка. Не добавили код товара. Введите код в виде цифр.", typeButton);
                            break;
                        }
                        sendMsg(message, "Пришлите название товара и нажмите кнопку \"Добавить название товара\"", typeButton);
                        break;
                    case "Добавить название товара":
                        product.setName(userInput);
                        sendMsg(message, "Пришлите номер склада и нажмите кнопку \"Добавить номер склада\"", typeButton);
                        break;
                    case "Добавить номер склада":
                        product.setWarehouseId(parseInt(userInput));
                        item.setProduct(product);
                        sendMsg(message, "Укажите количество этого товара на этом складе и нажмите кнопку \"Указать количество\"", typeButton);
                        break;
                    case "Указать количество":
                        float sumAmount;
                        try {
                            sumAmount = Float.parseFloat(userInput);
                        } catch (NumberFormatException exception) {
                            exception.printStackTrace();
                            sendMsg(message, "Введите число правильно (к примеру: 1.5 или 1 или 1.0)", typeButton);
                            break;
                        }
                        Optional<Item> byCodeId = dao.getByCodeIdAndName(product.getCodeId(), product.getName());
                        if (byCodeId.isPresent()) {
                            sendMsg(message, "Такой товар есть на этом складе. Суммируем.", typeButton);
                            dao.delete(product.getCodeId(), product.getWarehouseId());
                            sumAmount = byCodeId.get().getAmount() + sumAmount;
                        }
                        item.setAmount(sumAmount);
                        try {
                            dao.save(item);
                            typeButton.setCreateKeyboard(false);
                            sendMsg(message, "Товар добавлен в базу с такими данными: \n" +
                                    "код = " + product.getCodeId() + "\n" +
                                    "название = " + product.getName() + "\n" +
                                    "склад, где хранится = " + product.getWarehouseId() + "\n" +
                                    "количество = " + item.getAmount(), typeButton);
                        } catch (IOException e) {
                            e.printStackTrace();
                            sendMsg(message, "Что-то пошло не так. Попробуйте еще раз.", typeButton);
                        }
                        break;
                    case "/view_all_books":
                        typeButton.setCreateKeyboard(false);
                        items = dao.findAll();
                        sendMsg(message, "Хранение книг на всех складах:\n", typeButton);
                        for (Item itemOne : items) {
                            sendMsg(message, itemOne.toString(), typeButton);
                        }
                        sendMsg(message, "Все товары выведены",typeButton);
                        break;
                    case "/delete_book":
                        typeButton.setFirstButton("Введите код товара");
                        typeButton.setSecondButton("С какого склада убираем");
                        typeButton.setThirdButton("(доп.) Название");
                        typeButton.setFourthButton("-");
                        typeButton.setCreateKeyboard(true);
                        sendMsg(message, "Пришлите код товара, который нужно удалить, и нажмите кнопку \"Введите код товара\"", typeButton);
                        break;
                    case "Введите код товара":
                        codeToDelete = userInput;
                        sendMsg(message, "Введите, с какого склада удаляем, и нажмите кнопку \"С какого склада убираем\"", typeButton);
                        break;
                    case "(доп.) Название":
                        nameToDelete = userInput;
                        delete = dao.delete(codeToDelete, deleteFromWarehouse, nameToDelete);
                        checkedDelete(delete, message);
                        break;
                    case "С какого склада убираем":
                        deleteFromWarehouse = parseInt(userInput);
                        if (!checkIsOnlyOneProductOnWarehouse(codeToDelete, deleteFromWarehouse)) {
                            if (nameToDelete.equals("")) {
                                sendMsg(message, "Товар с таким кодом не один на этом складе. Введите полное название и нажмите кнопку (доп.) Название", typeButton);
                                break;
                            } else {
                                delete = dao.delete(codeToDelete, deleteFromWarehouse, nameToDelete);
                            }
                        } else {
                            delete = dao.delete(codeToDelete, deleteFromWarehouse);
                        }
                        checkedDelete(delete, message);
                        break;
                    case "/find_one_code":
                        typeButton.setCreateKeyboard(false);
                        String codeId;
                        try {
                            codeId = userInput;
                        } catch (NumberFormatException exception) {
                            exception.printStackTrace();
                            sendMsg(message, "Введите код товара, который будем искать", typeButton);
                            break;
                        }
                        Optional<List<Item>> byCodeId1 = dao.getByCodeId(codeId);
                        List<Item> foundItem = byCodeId1.get();
                        sendMsg(message, !foundItem.isEmpty() ? foundItem.toString() : "Товар с таким кодом не найден", typeButton);
                        break;
                    case "/find_many_codes":
                        typeButton.setCreateKeyboard(false);
                        if (!userInput.contains(",") || (userInput.contains(" "))) {
                            sendMsg(message, "Введите коды через запятую (без пробелов)", typeButton);
                            break;
                        }
                        String[] codes = userInput.split(",");
                        for (String code : codes) {
                            byCodeId1 = dao.getByCodeId(code);
                            List<Item> foundItems = byCodeId1.get();
                            sendMsg(message, !foundItems.isEmpty() ? foundItems.toString() : "Товар с кодoм " + code + " не найден", typeButton);
                        }
                        break;
                    case "/find_name":
                        typeButton.setCreateKeyboard(false);
                        Optional<List<Item>> byName = dao.getByName(userInput);
                        List<Item> foundItems = new ArrayList<>();
                        foundItems = byName.get();
                        sendMsg(message, !foundItems.isEmpty() ? foundItems.toString() : "Товар с таким названием не найден", typeButton);
                        break;
                    case "/find_warehouse":
                        typeButton.setCreateKeyboard(false);
                        String warehouseId;
                        try {
                            warehouseId = userInput;
                        } catch (NumberFormatException exception) {
                            exception.printStackTrace();
                            sendMsg(message, "Введите код склада, по которому будем искать", typeButton);
                            break;
                        }
                        Optional<List<Item>> byWarehouse = dao.getByWarehouse(warehouseId);
                        List<Item> itemInWarehouse;
                        itemInWarehouse = byWarehouse.get();
                        for (Item item1 : itemInWarehouse) {
                            sendMsg(message, item1.toString(), typeButton);
                        }
                        break;
                    case "/update":
                        typeButton.setCreateKeyboard(false);
                        typeButton.setFirstButton("код");
                        typeButton.setSecondButton("склад");
                        typeButton.setThirdButton("остаток");
                        typeButton.setFourthButton("(доп.) название");
                        typeButton.setCreateKeyboard(true);
                        sendMsg(message, "Пришлите код товара, для которого будем менять остаток, и нажмите кнопку \"код\"", typeButton);
                        break;
                    case "код":
                        codeToUpdate = userInput;
                        sendMsg(message, "Введите, с какого склада изменяем остаток, и нажмите кнопку \"склад\"", typeButton);
                        break;
                    case "склад":
                        updateFromWarehouse = parseInt(userInput);
                        sendMsg(message, "Введите, сколько осталось этого товара, и нажмите кнопку \"остаток\"", typeButton);
                        break;
                    case "(доп.) название":
                        nameToUpdate = userInput;
                        updated = dao.update(codeToUpdate, updateFromWarehouse, nameToUpdate, balance);
                        checkedUpdate(updated, message);
                        break;
                    case "остаток":
                        balance = Float.parseFloat(userInput);
                        if (!checkIsOnlyOneProductOnWarehouse(codeToUpdate, updateFromWarehouse)) {
                            if (nameToUpdate.equals("")) {
                                sendMsg(message, "Товар с таким кодом не один на этом складе. Введите полное название и нажмите кнопку (доп.) название", typeButton);
                                break;
                            } else {
                                updated = dao.update(codeToUpdate, updateFromWarehouse, nameToUpdate, balance);
                            }
                        } else {
                            updated = dao.update(codeToUpdate, updateFromWarehouse, balance);
                        }
                        checkedUpdate(updated, message);
                        break;
                    case "/password":
                        typeButton.setCreateKeyboard(false);
                        if (userInput.equals(botPassword)) {
                            sendMsg(message, "Пароль верный. Ваше устройство запомнено.\n" +
                                    "Добро пожаловать в телеграм-бот.\n" +
                                    "С помощью него вы можете быстро найти, на каком складе лежит тот или другой товар.\n" +
                                    "Выберите команду /help для помощи", typeButton);
                            try {
                                Optional<String> s = FileOperator.writeLogging(update.getMessage().getChatId());
                                if (s.isPresent()) {
                                    users.add(s.get());
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                                sendMsg(message, "Не получилось записать в файл. Обратитесь к администратору", typeButton);
                            }
                        } else {
                            sendMsg(message, "Неправильный пароль. Уточните у администратора", typeButton);
                        }
                        break;
                    case "/help":
                        typeButton.setCreateKeyboard(false);
                        sendMsg(message, "HELP MENU", typeButton);
                        sendMsg(message, "Бот поддерживает следующие команды:\n" +
                                "/find_one_code - находит одну книгу по коду\n" +
                                "/find_many_code - находит много книг (вводите коды через запятую без пробелов)\n" +
                                "/update - изменяет остаток товара\n" +
                                "/find_name - находит книгу по любому слову в названии без учета регистра\n" +
                                "/find_warehouse - выводит все книги на выбранном складе\n" +
                                "/view_all_books - выводит полный список всех книг по всем складам (без сортировки)\n" +
                                "/add_book - книгу добавляет так: 1) выбираем эту команду, 2) набираем код товара и нажимаем " +
                                "отправить сообщение, 3) точно так же повторяем с названием, кодом склада и количеством\n" +
                                "/delete_book - удаляет книгу\n" +
                                "/password - для аутентификации в боте\n" +
                                "Запросы не выполнять одновременно с нескольких устройств.\n" +
                                "Автор - Андрей Марков.\nВерсия программы 1.1\n" +
                                "Для изменения функционала сообщать на почту:\n" +
                                "noteandmark@gmail.com", typeButton);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private void checkedUpdate(boolean updated, Message message) {
        if (!updated) {
            sendMsg(message, "Не было изменено. Проверьте правильность ввода.", typeButton);
        } else {
            typeButton.setCreateKeyboard(false);
            sendMsg(message, "Изменено", typeButton);
            codeToUpdate = "-1";
            updateFromWarehouse = -1;
            nameToUpdate = "";
            balance = 0.0F;
            items = dao.findAll();
            store.setItems(items);
        }
    }

    private void checkedDelete(boolean delete, Message message) {
        if (!delete) {
            sendMsg(message, "Не было удалено. Проверьте правильность ввода.", typeButton);
        } else {
            typeButton.setCreateKeyboard(false);
            sendMsg(message, "Удалено", typeButton);
            codeToDelete = "-1";
            deleteFromWarehouse = -1;
            nameToDelete = "";
            items = dao.findAll();
            store.setItems(items);
        }
    }

    private boolean checkIsOnlyOneProductOnWarehouse(String codeToDelete, int deleteFromWarehouse) {
        boolean onlyOne = true;
        int count = 0;
        List<Item> itemsCheck = dao.findAll();
        for (Item checkedItem : itemsCheck) {
            if (checkedItem.getProduct().getCodeId() == codeToDelete && checkedItem.getProduct().getWarehouseId() == deleteFromWarehouse) {
                count++;
            }
        }
        if (count > 1) {
            onlyOne = false;
        }
        return onlyOne;
    }

    // send message to the user with created custom keyboard
    private void sendMsg(Message message, String text, TypeButton typeButton) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        if (typeButton.isCreateKeyboard()) {
            // Создаем клавиуатуру
            ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
            sendMessage.setReplyMarkup(replyKeyboardMarkup);
            replyKeyboardMarkup.setSelective(true);
            replyKeyboardMarkup.setResizeKeyboard(true);
            replyKeyboardMarkup.setOneTimeKeyboard(true);

            // Создаем список строк клавиатуры
            List<KeyboardRow> keyboard = new ArrayList<>();

            // Первая строчка клавиатуры
            KeyboardRow keyboardFirstRow = new KeyboardRow();
            // Добавляем кнопки в первую строчку клавиатуры
            keyboardFirstRow.add(typeButton.getFirstButton());
            keyboardFirstRow.add(typeButton.getSecondButton());

            // Вторая строчка клавиатуры
            KeyboardRow keyboardSecondRow = new KeyboardRow();
            // Добавляем кнопки во вторую строчку клавиатуры
            keyboardSecondRow.add(typeButton.getThirdButton());
            keyboardSecondRow.add(typeButton.getFourthButton());

            // Добавляем все строчки клавиатуры в список
            keyboard.add(keyboardFirstRow);
            keyboard.add(keyboardSecondRow);
            // и устанваливаем этот список нашей клавиатуре
            replyKeyboardMarkup.setKeyboard(keyboard);
        } else removeKeyboard(sendMessage);

        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setReplyToMessageId(message.getMessageId());
        sendMessage.setText(text);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    // delete created custom keyboard
    private void removeKeyboard(SendMessage sendMessage) {
        ReplyKeyboardRemove replyKeyboardRemove = new ReplyKeyboardRemove();
        replyKeyboardRemove.setSelective(true);
        replyKeyboardRemove.setRemoveKeyboard(true);
        sendMessage.setReplyMarkup(replyKeyboardRemove);
    }

    // load all data from files on first load telegram bot
    public void startupLoadDB() {
        File f = new File(betFile);
        if (f.exists() && f.isFile()) {
            items = dao.findAll();
            store.setItems(items);
        } else {
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        File logging = new File(betLogging);
        if (logging.exists() && logging.isFile()) {
            users = FileOperator.readLinesFromFile(betLogging);
        } else {
            try {
                logging.createNewFile();
                users = new ArrayList<>();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}