package ru.noteandmark.bot.dao.impl;

import ru.noteandmark.bot.dao.ItemDao;
import ru.noteandmark.bot.domain.Item;
import ru.noteandmark.bot.domain.Product;
import ru.noteandmark.bot.domain.Store;
import ru.noteandmark.bot.readers.FileOperator;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static ru.noteandmark.bot.service.BotService.betFile;

public class ItemDaoImpl implements ItemDao {

    private FileOperator fileOperator;

    public ItemDaoImpl() {
        fileOperator = new FileOperator();
    }

    @Override
    public Item save(Item item) throws IOException {
        fileOperator.writeItemToFile(item);
        return item;
    }

    @Override
    public Optional<List<Item>> getByCodeId(String codeId) {
        List<Item> items = findAll();
        List<Item> itemList = new ArrayList<>();
        for (Item item : items) {
            if (item.getProduct().getCodeId().equals(codeId)) {
                itemList.add(item);
            }
        }
        return Optional.ofNullable(itemList);
    }

    public Optional<List<Item>> getByName(String name) {
        List<Item> items = findAll();
        List<Item> foundItems = new ArrayList<>();
        for (Item item : items) {
            if (item.getProduct().getName().toLowerCase().contains(name.toLowerCase())) {
                foundItems.add(item);
            }
        }
        return Optional.ofNullable(foundItems);
    }

    public Optional<List<Item>> getByWarehouse(String id) {
        List<Item> items = findAll();
        List<Item> foundItems = new ArrayList<>();
        int warehouseId = 0;
        try {
            warehouseId = Integer.parseInt(id);
        } catch (NumberFormatException exception) {
            exception.printStackTrace();
        }
        for (Item item : items) {
            if (item.getProduct().getWarehouseId() == warehouseId) {
                foundItems.add(item);
            }
        }
        return Optional.ofNullable(foundItems);
    }

    public Optional<Item> getByCodeIdAndName(String codeId, String name) {
        List<Item> items = findAll();
        if (!items.isEmpty()) {
            for (Item item : items) {
                if (item.getProduct().getCodeId().equals(codeId) && item.getProduct().getName().equals(name)) {
                    Optional<Item> optionalItem = Optional.of(item);
                    return optionalItem;
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Item> findAll() {
        List<String> lines = fileOperator.readLinesFromFile(betFile);
        List<Item> items = new ArrayList<>();
        if (lines.isEmpty()) {
            return new ArrayList<>();
        }
        System.out.println("lines = " + lines.toString());
        return lines.stream()
                .map(l -> l.split("_"))
                .map(this::buildItemFromLine)
                .collect(Collectors.toList());
    }

    @Override
    public boolean update(String productId, int warehouseId, float balance) {
        boolean isUpdated = false;
        List<Item> items = findAll();
        for (Item item : items) {
            if (item.getProduct().getCodeId().equals(productId) && item.getProduct().getWarehouseId() == warehouseId) {
                item.setAmount(balance);
                Store store = new Store();
                store.setItems(items);
                try {
                    fileOperator.writeStoreToFile(store);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                isUpdated = true;
            }
        }
        return isUpdated;
    }

    public boolean update(String productId, int warehouseId, String nameToUpdate, float balance) {
        boolean isUpdated = false;
        List<Item> items = findAll();
        for (Item item : items) {
            if (item.getProduct().getCodeId().equals(productId) && item.getProduct().getWarehouseId() == warehouseId && item.getProduct().getName().equals(nameToUpdate)) {
                item.setAmount(balance);
                Store store = new Store();
                store.setItems(items);
                try {
                    fileOperator.writeStoreToFile(store);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                isUpdated = true;
            }
        }
        return isUpdated;
    }

    @Override
    public boolean delete(String productId, int warehouseId) {
        boolean isDeleted = false;
        List<Item> items = findAll();
        Store store = new Store();
        Iterator<Item> itemIterator = items.iterator();
        while (itemIterator.hasNext()) {
            Item nextItem = itemIterator.next();
            if (nextItem.getProduct().getCodeId().equals(productId) && nextItem.getProduct().getWarehouseId() == warehouseId) {
                itemIterator.remove();
                try {
                    store.setItems(items);
                    fileOperator.writeStoreToFile(store);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                isDeleted = true;
            }
        }
        return isDeleted;
    }

    public boolean delete(String productId, int warehouseId, String nameToDelete) {
        boolean isDeleted = false;
        List<Item> items = findAll();
        Store store = new Store();
        Iterator<Item> itemIterator = items.iterator();
        while (itemIterator.hasNext()) {
            Item nextItem = itemIterator.next();
            if (nextItem.getProduct().getCodeId().equals(productId) && nextItem.getProduct().getWarehouseId() == warehouseId && nextItem.getProduct().getName().equals(nameToDelete)) {
                itemIterator.remove();
                try {
                    store.setItems(items);
                    fileOperator.writeStoreToFile(store);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                isDeleted = true;
            }
        }
        return isDeleted;
    }

    private Item buildItemFromLine(String[] strings) {
        Item item = new Item();
        Product product = new Product();
        String code = strings[0];
        String name = strings[1];
        int warehouseId = Integer.parseInt(strings[2]);
        float amount = Float.parseFloat(strings[3]);
        product.setCodeId(code);
        product.setName(name);
        product.setWarehouseId(warehouseId);
        item.setProduct(product);
        item.setAmount(amount);
        return item;
    }
}
