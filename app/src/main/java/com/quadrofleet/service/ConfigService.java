package com.quadrofleet.service;

public class ConfigService {

    private static ConfigService instance;

    private boolean status;

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

}
