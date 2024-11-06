package com.quadrofleet.service;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Logger;

public class TrayIconService {

    private final Logger logger = Logger.getLogger(TrayIconService.class.getName());

    private static final String PATH_TO_ICON = "src/images/1.gif";

    public static void initTrayIcon() {
        if (!SystemTray.isSupported()) {
            JOptionPane.showMessageDialog(
                    null,
                    "System tray is not supported!",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );

            System.exit(0);

            return;
        }

        PopupMenu trayPopupMenu = new PopupMenu();
        trayPopupMenu.add(generateVideoStreamAction());
        trayPopupMenu.add(generateMapAction());
        trayPopupMenu.add(generateExitAction());

        TrayIcon trayIcon = new TrayIcon(
                Toolkit.getDefaultToolkit().getImage(PATH_TO_ICON),
                ConfigService.getInstance().getBundleString("tray.icon.tooltip"),
                trayPopupMenu);

        trayIcon.setImageAutoSize(true);

        try {
            SystemTray.getSystemTray().add(trayIcon);
        } catch (AWTException awtException) {
            //
        }
    }

    private static MenuItem generateVideoStreamAction() {
        MenuItem result = new MenuItem(ConfigService.getInstance().getBundleString("tray.video.tooltip"));

        result.addActionListener(e -> executeGStreamer());

        return result;
    }

    private static MenuItem generateMapAction() {
        MenuItem result = new MenuItem(ConfigService.getInstance().getBundleString("tray.map.tooltip"));

        result.addActionListener(e -> {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                try {
                    Desktop.getDesktop().browse(new URI(ConfigService.getInstance().getUrlToMap()));
                } catch (IOException | URISyntaxException ex) {
                    JOptionPane.showMessageDialog(
                            null,
                            "Can not open map in browser!",
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        });

        return result;
    }

    private static MenuItem generateExitAction() {
        MenuItem result = new MenuItem(ConfigService.getInstance().getBundleString("tray.exit.tooltip"));

        result.addActionListener(e -> {
            ConfigService.getInstance().setRunSDLEventLoop(false);

            System.exit(0);
        });

        return result;
    }

    private static void executeGStreamer() {
        String[] command = {
                "gst-launch-1.0",
                "libcamerasrc",
                "!",
                "video/x-raw,width=640,height=480,framerate=60/1",
                "!",
                "videoflip",
                "method=rotate-180",
                "!",
                "videoconvert",
                "!",
                "x264enc",
                "bitrate=1000",
                "speed-preset=ultrafast",
                "tune=zerolatency",
                "!",
                "h264parse",
                "!",
                "rtph264pay",
                "config-interval=1",
                "pt=96",
                "!",
                "udpsink",
                "host=100.96.1.2",
                "port=2222"
        };

        try {
            new ProcessBuilder(command).start();
        } catch (IOException e) {
            //
        }
    }

}
