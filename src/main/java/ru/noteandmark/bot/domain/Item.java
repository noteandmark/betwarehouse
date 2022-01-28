package ru.noteandmark.bot.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@EqualsAndHashCode
@Getter
@Setter
public class Item {
    Product product;
    float amount;

    public Item() {
    }

    public String toString() {
        return "\nТовар:" +
                "\nкод = " + this.getProduct().getCodeId() + "\n" +
                "название = " + this.getProduct().getName() + "\n" +
                "на складе " + this.getProduct().getWarehouseId() + "\n" +
                "количество = " + this.getAmount() + "\n";
    }
}
