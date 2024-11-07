package com.quadrofleet.service;

import java.util.Locale;
import java.util.ResourceBundle;

public class ConfigService {

    private static ConfigService instance;

    private boolean runSDLEventLoop = true;

    private ConfigService() {
        //
    }

    public static synchronized ConfigService getInstance() {
        if (instance == null) {
            instance = new ConfigService();
        }
        return instance;
    }

    public boolean isRunSDLEventLoop() {
        return runSDLEventLoop;
    }

    public void setRunSDLEventLoop(boolean runSDLEventLoop) {
        this.runSDLEventLoop = runSDLEventLoop;
    }

    public String getBundleString(String key) {
        return ResourceBundle.getBundle("messages", Locale.getDefault()).getString(key);
    }

}
