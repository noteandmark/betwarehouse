package ru.noteandmark.bot.domain;

import lombok.Data;

import java.util.List;

@Data
public class Store {
    List<Item> items;
}
