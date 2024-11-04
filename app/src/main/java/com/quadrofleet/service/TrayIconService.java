package com.quadrofleet.service;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class TrayIconService {

    public static void initTrayIcon() {
        if (!SystemTray.isSupported()) {
            System.out.println("System tray is not supported !!! ");
            return;
        }
        //get the systemTray of the system
        SystemTray systemTray = SystemTray.getSystemTray();

        //get default toolkit
        //Toolkit toolkit = Toolkit.getDefaultToolkit();
        //get image
        //Toolkit.getDefaultToolkit().getImage("src/resources/busylogo.jpg");
        Image image = Toolkit.getDefaultToolkit().getImage("src/images/1.gif");

        //popupmenu
        PopupMenu trayPopupMenu = new PopupMenu();

        //1t menuitem for popupmenu
        MenuItem videoStreamAction = new MenuItem("Video stream");
        videoStreamAction.addActionListener(e -> {
            // Run gstreamer
        });
        trayPopupMenu.add(videoStreamAction);

        //2d menuitem for popupmenu
        MenuItem Mapaction = new MenuItem("Map");
        Mapaction.addActionListener(e -> {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                try {
                    Desktop.getDesktop().browse(new URI("http://localhost:8090"));
                } catch (IOException | URISyntaxException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        trayPopupMenu.add(Mapaction);

        //Exit menuitem of popupmenu
        MenuItem closeAction = new MenuItem("Close");
        closeAction.addActionListener(e -> {
            System.exit(0);
        });
        trayPopupMenu.add(closeAction);

        //setting tray icon
        TrayIcon trayIcon = new TrayIcon(image, "QuadroFleet Station", trayPopupMenu);
        //adjust to default size as per system recommendation
        trayIcon.setImageAutoSize(true);

        try {
            systemTray.add(trayIcon);
        } catch (AWTException awtException) {
            awtException.printStackTrace();
        }
    }

}
