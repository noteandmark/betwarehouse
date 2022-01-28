package ru.noteandmark.bot.readers;

import ru.noteandmark.bot.domain.Item;
import ru.noteandmark.bot.domain.Product;
import ru.noteandmark.bot.domain.Store;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ru.noteandmark.bot.service.BotService.betFile;
import static ru.noteandmark.bot.service.BotService.betLogging;

public class FileOperator {

    public static List<String> readLinesFromFile(String fileName) {
        List<String> lines;
        try (Stream<String> streamFromFiles = Files.lines(Paths.get(fileName))) {
            lines = streamFromFiles.collect(Collectors.toList());
        } catch (IOException e) {
            throw new IllegalArgumentException("Check file name. This file doesn't exist");
        }
        return lines;
    }

    public void writeStoreToFile(Store list) throws IOException {
//        try (FileWriter writer = new FileWriter(betFile)) {
        try {
            File file = new File(betFile);
            file.createNewFile(); // если файл существует - команда игнорируется
            FileOutputStream fileOutputStream = new FileOutputStream(file, false);
// false -> true, если надо продолжать писать в файл при его наличии, с false файл будет очищен
            Writer writer = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8);
            List<Item> items = list.getItems();
            for (Item item : items) {
                writeItem(item, writer);
            }
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Optional<String> writeLogging(Long chatId) throws IOException {
        String userChatId = null;
//        try (FileWriter writer = new FileWriter(betLogging, true)) {
        try {
            File file = new File(betLogging);
            file.createNewFile();
            FileOutputStream fileOutputStream = new FileOutputStream(file, true);
            Writer writer = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8);
            userChatId = String.valueOf(chatId);
            writer.write(userChatId + System.getProperty("line.separator"));
            writer.flush();
            writer.close();
        } catch (FileNotFoundException e) {
            throw e; // catch and re-throw
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Optional.of(userChatId);
    }

    public void writeItemToFile(Item item) throws IOException {
//        try (FileWriter writer = new FileWriter(betFile, true)) {
        try {
            File file = new File(betFile);
            file.createNewFile();
            FileOutputStream fileOutputStream = new FileOutputStream(file, true);
            Writer writer = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8);
            writeItem(item, writer);
            writer.flush();
            writer.close();
        } catch (FileNotFoundException e) {
            throw e; // catch and re-throw
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeItem(Item item, Writer writer) throws IOException {
        Product product = item.getProduct();
        float amount = item.getAmount();
        String codeId = product.getCodeId();
        int warehouseId = product.getWarehouseId();
        String name = product.getName();
        writer.write(codeId + "_" + name + "_" + warehouseId + "_" + amount + System.getProperty("line.separator"));
    }

}