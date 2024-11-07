package com.quadrofleet.service;

import com.quadrofleet.App;
import com.quadrofleet.ConfigLoader;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Logger;

public class TrayIconService {

    private final Logger logger = Logger.getLogger(TrayIconService.class.getName());

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
                Toolkit.getDefaultToolkit().getImage(App.class.getResource("/app/logo.png")),
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
                    Desktop.getDesktop().browse(new URI(ConfigLoader.getInstance().getProperty("map.url")));
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
                "gst-launch-1.0.exe",
                "udpsrc",
                "port=" + ConfigLoader.getInstance().getProperty("video.stream.receiver.port"),
                "!",
                "application/x-rtp,encoding-name=H264,payload=96",
                "!",
                "rtph264depay",
                "!",
                "avdec_h264",
                "!",
                "d3dvideosink",
                "sync=false"
        };

        try {
            WinDef.HWND hwnd = User32.INSTANCE.FindWindow(null, "GStreamer D3D video sink (internal window)");

            if (hwnd != null) {
                return;
            }

            new ProcessBuilder(command).start();
        } catch (IOException e) {
            //
        }
    }

}
