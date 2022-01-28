package ru.noteandmark.bot.domain;

import lombok.Data;

@Data
public class Product {
  String codeId;
  int warehouseId;
  String name;
}
