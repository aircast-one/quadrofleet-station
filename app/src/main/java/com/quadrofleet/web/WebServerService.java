package com.quadrofleet.web;

import org.eclipse.jetty.server.Server;

public class WebServerService {

    private final Server SERVER;

    private final Thread THREAD;

    public WebServerService() {
        SERVER = new Server(8090);
        THREAD = new Thread(this::jettyInitialization);
    }

    public void start() {
        THREAD.start();
    }

    private void jettyInitialization() {
        SERVER.setHandler(new MainHandler());
        try {
            SERVER.start();
            SERVER.join();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
