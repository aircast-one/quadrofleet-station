package com.quadrofleet;

import com.quadrofleet.gamepad.GamepadService;
import com.quadrofleet.service.TrayIconService;
import com.quadrofleet.web.WebServerService;

public class App {

    public static void main(String[] args) {
        TrayIconService.initTrayIcon();

        new WebServerService().start();
        new GamepadService().start();
    }

}
