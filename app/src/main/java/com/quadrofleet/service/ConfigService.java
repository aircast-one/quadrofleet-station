package com.quadrofleet.service;

import java.util.Locale;
import java.util.ResourceBundle;

public class ConfigService {

    private static ConfigService instance;

    private boolean status;

    private boolean runSDLEventLoop = true;

    private String urlToMap = "http://localhost:8090";

    private ConfigService() {
        //
    }

    public static synchronized ConfigService getInstance() {
        if (instance == null) {
            instance = new ConfigService();
        }
        return instance;
    }

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public boolean isRunSDLEventLoop() {
        return runSDLEventLoop;
    }

    public void setRunSDLEventLoop(boolean runSDLEventLoop) {
        this.runSDLEventLoop = runSDLEventLoop;
    }

    public String getUrlToMap() {
        return urlToMap;
    }

    public void setUrlToMap(String urlToMap) {
        this.urlToMap = urlToMap;
    }

    public String getBundleString(String key) {
        return ResourceBundle.getBundle("messages", Locale.getDefault()).getString(key);
    }

}
