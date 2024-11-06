package com.quadrofleet;

import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {

    private static final ConfigLoader INSTANCE = new ConfigLoader();

    private final Properties properties = new Properties();

    private ConfigLoader() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (input != null) {
                properties.load(input);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ConfigLoader getInstance() {
        return INSTANCE;
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public Integer getPropertyAsInteger(String key) {
        return Integer.valueOf(properties.getProperty(key));
    }

}
