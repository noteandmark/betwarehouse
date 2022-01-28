package ru.noteandmark.bot.readers;

import ru.noteandmark.bot.BetWarehouse;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyReader {

    public Properties readProperties() throws IOException {

        Properties properties = new Properties();

        InputStream stream = BetWarehouse.class.getClassLoader().getResourceAsStream("application.properties");
        properties.load(stream);

        return properties;
    }

}
