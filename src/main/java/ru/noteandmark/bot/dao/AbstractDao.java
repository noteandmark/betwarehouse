package ru.noteandmark.bot.dao;

import ru.noteandmark.bot.domain.Item;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface AbstractDao <T> {
    T save (T t) throws IOException;
    Optional<List<Item>> getByCodeId(String codeId);
    List<T> findAll();
    boolean update(String productId, int warehouseIdt, float balance);
    boolean delete(String productId, int warehouseId);
}
