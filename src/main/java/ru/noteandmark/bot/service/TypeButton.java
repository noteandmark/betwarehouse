package ru.noteandmark.bot.service;

import lombok.Data;

@Data
public class TypeButton {
    private String firstButton;
    private String secondButton;
    private String thirdButton;
    private String fourthButton;
    public boolean createKeyboard;
}
