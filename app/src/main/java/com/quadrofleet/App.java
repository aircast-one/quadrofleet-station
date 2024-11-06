package com.quadrofleet;

import com.quadrofleet.service.FrameReceiverService;
import com.quadrofleet.service.FrameSenderService;
import com.quadrofleet.service.GamepadService;
import com.quadrofleet.service.TrayIconService;
import com.quadrofleet.web.WebServerService;

public class App {

    public static void main(String[] args) {
        TrayIconService.initTrayIcon();

        new WebServerService().start();
        new GamepadService().start();

        new FrameSenderService().start();
        new FrameReceiverService().start();
    }

}
