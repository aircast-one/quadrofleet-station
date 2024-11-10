package com.quadrofleet.service;

import com.quadrofleet.helper.GamepadHelper;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import io.github.libsdl4j.api.event.SDL_Event;
import io.github.libsdl4j.api.event.events.SDL_JoyAxisEvent;
import io.github.libsdl4j.api.gamecontroller.SDL_GameController;
import io.github.libsdl4j.api.joystick.SDL_JoystickID;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static io.github.libsdl4j.api.Sdl.SDL_Init;
import static io.github.libsdl4j.api.Sdl.SDL_Quit;
import static io.github.libsdl4j.api.SdlSubSystemConst.SDL_INIT_EVERYTHING;
import static io.github.libsdl4j.api.error.SdlError.SDL_GetError;
import static io.github.libsdl4j.api.event.SDL_EventType.SDL_CONTROLLERDEVICEADDED;
import static io.github.libsdl4j.api.event.SDL_EventType.SDL_JOYAXISMOTION;
import static io.github.libsdl4j.api.event.SdlEvents.SDL_PollEvent;
import static io.github.libsdl4j.api.gamecontroller.SdlGamecontroller.SDL_GameControllerOpen;
import static io.github.libsdl4j.api.hints.SdlHints.SDL_SetHint;

public class GamepadService {

    private static final String[] DIRECTIONS = {
            "E----SE----S----SW----W",
            "----SE----S----SW----W-",
            "---SE----S----SW----W--",
            "--SE----S----SW----W---",
            "-SE----S----SW----W----",
            "SE----S----SW----W----NW",
            "----S----SW----W----NW-",
            "---S----SW----W----NW--",
            "--S----SW----W----NW---",
            "-S----SW----W----NW----",
            "S----SW----W----NW----N",
            "----SW----W----NW----N-",
            "---SW----W----NW----N--",
            "--SW----W----NW----N---",
            "-SW----W----NW----N----",
            "SW----W----NW----N----NE",
            "----W----NW----N----NE-",
            "---W----NW----N----NE--",
            "--W----NW----N----NE---",
            "-W----NW----N----NE----",
            "W----NW----N----NE----E",
            "----NW----N----NE----E-",
            "---NW----N----NE----E--",
            "--NW----N----NE----E---",
            "-NW----N----NE----E----",
            "NW----N----NE----E----SE",
            "----N----NE----E----SE-",
            "---N----NE----E----SE--",
            "--N----NE----E----SE---",
            "-N----NE----E----SE----",
            "N----NE----E----SE----S",
            "----NE----E----SE----S-",
            "---NE----E----SE----S--",
            "--NE----E----SE----S---",
            "-NE----E----SE----S----",
            "NE----E----SE----S----SW",
            "----E----SE----S----SW-",
            "---E----SE----S----SW--",
            "--E----SE----S----SW---",
            "-E----SE----S----SW----"
    };

    private final Logger logger = Logger.getLogger(GamepadService.class.getName());

    private final Thread THREAD;

    private JFrame overlay;

    private double windowHeightParam;

    private double windowWidthParam;

    public GamepadService() {
        THREAD = new Thread(this::SDLInitialization);
    }

    public static String generateCompassDirections(double degrees) {
        int normalizedDegrees = (int) ((degrees + 180) % 360);

        if (normalizedDegrees < 0) {
            normalizedDegrees += 360;
        }

        return DIRECTIONS[normalizedDegrees / 9];
    }

    public void start() {
        THREAD.start();
    }

    private void SDLInitialization() {
        int result = SDL_Init(SDL_INIT_EVERYTHING);

        if (result != 0) {
            JOptionPane.showMessageDialog(
                    null,
                    "Unable to initialize SDL library (Error code " + result + "): " + SDL_GetError(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );

            return;
        }

        // Allow background joystick actions
        SDL_SetHint("SDL_JOYSTICK_ALLOW_BACKGROUND_EVENTS", "1");

        SDL_GameController sdlGameController = null;
        SDL_Event evt = new SDL_Event();

        fontInit();

        // Overlay
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

        executor.scheduleAtFixedRate(() -> {
            WinDef.HWND hwnd = User32.INSTANCE.FindWindow(null, "GStreamer D3D video sink (internal window)");

            if (hwnd != null) {
                if (overlay == null) {
                    setupOverlay();
                }

                WinDef.RECT rect = new WinDef.RECT();
                User32.INSTANCE.GetWindowRect(hwnd, rect);

                SwingUtilities.invokeLater(() -> {
                    if (!overlay.isVisible()) {
                        overlay.setVisible(true);
                    }

                    windowHeightParam = (double) (rect.bottom - rect.top) / 480;
                    windowWidthParam = (double) (rect.right - rect.left) / 640;

                    overlay.setSize(rect.right - rect.left, rect.bottom - rect.top);
                    overlay.setLocation(rect.left, rect.top);
                });
            }

            if (overlay != null && (!overlay.isVisible() || hwnd == null)) {
                SwingUtilities.invokeLater(() -> overlay.setVisible(false));
            }
        }, 1000, 20, TimeUnit.MILLISECONDS);

        while (ConfigService.getInstance().isRunSDLEventLoop()) {

            if (overlay != null && overlay.isVisible()) {
                overlay.repaint();
            }

            if (sdlGameController == null) {
                sdlGameController = SDL_GameControllerOpen(0);
            }

            while (SDL_PollEvent(evt) != 0) {

                updateGamepadButtons(sdlGameController, evt.jaxis);

                switch (evt.type) {
                    case SDL_CONTROLLERDEVICEADDED:
                        System.out.println("Controller added: " + evt.cdevice);
                        break;
                    case SDL_JOYAXISMOTION:
                        if (evt.jaxis.which.equals(new SDL_JoystickID(0))) {
                            updateGamepadSticks(evt.jaxis);
                        }
                    default:
                        break;
                }
            }

            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        SDL_Quit();
    }

    private void fontInit() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("MaterialIcons-Regular.ttf")) {
            if (inputStream != null) {
                GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(Font.createFont(Font.TRUETYPE_FONT, inputStream));
            }

        } catch (IOException | FontFormatException e) {
            logger.warning("Error by loading FontAwesome");
        }
    }

    private void updateGamepadButtons(SDL_GameController sdlGameController, SDL_JoyAxisEvent event) {
        if (GamepadHelper.isPressedUp(sdlGameController)) {
            FlightConfigService.getInstance()
                    .getFlightConfig()
                    .resetThrottle();
            FlightConfigService.getInstance()
                    .getFlightConfig()
                    .setArmed(true);
        }

        if (GamepadHelper.isPressedDown(sdlGameController)) {
            FlightConfigService.getInstance()
                    .getFlightConfig()
                    .setArmed(false);
        }

        if (GamepadHelper.isPressedRight(sdlGameController)) {
            FlightConfigService.getInstance()
                    .getFlightConfig()
                    .setAngleMode(true);
        }

        if (GamepadHelper.isPressedLeft(sdlGameController)) {
            FlightConfigService.getInstance()
                    .getFlightConfig()
                    .setAngleMode(false);
        }

        if (GamepadHelper.isPressedRB(sdlGameController) && GamepadHelper.isPressedA(sdlGameController)) {
            FlightConfigService.getInstance()
                    .getFlightConfig()
                    .setAltHoldMode(true);
        }

        if (GamepadHelper.isPressedRB(sdlGameController) && GamepadHelper.isPressedB(sdlGameController)) {
            FlightConfigService.getInstance()
                    .getFlightConfig()
                    .setAltHoldMode(false);
        }

        FlightConfigService
                .getInstance()
                .getFlightStatus()
                .setArmed(FlightConfigService.getInstance()
                        .getFlightConfig()
                        .isArmed());

        FlightConfigService
                .getInstance()
                .getFlightStatus()
                .setAngleMode(FlightConfigService.getInstance()
                        .getFlightConfig()
                        .isAngleMode());

        FlightConfigService
                .getInstance()
                .getFlightStatus()
                .setAltHoldMode(FlightConfigService.getInstance()
                        .getFlightConfig()
                        .isAltHoldMode());
    }

    private void updateGamepadSticks(SDL_JoyAxisEvent event) {
        if (event.axis == 0) {
            // Left Stick X Axis
            FlightConfigService.getInstance().getFlightConfig().setRoll(GamepadHelper.convertShortToDouble(event.value));
        } else if (event.axis == 1) {
            // Left Stick Y Axis
            FlightConfigService.getInstance().getFlightConfig().setPitch(GamepadHelper.convertShortToDouble(event.value));
        } else if (event.axis == 2) {
            // Right Stick X Axis
            FlightConfigService.getInstance().getFlightConfig().setYaw(GamepadHelper.convertShortToDouble(event.value));
        } else if (event.axis == 3) {
            // Right Stick Y Axis
            FlightConfigService.getInstance().getFlightConfig().setThrottle(GamepadHelper.convertShortToDouble(event.value));
        }
    }

    private void setupOverlay() {
        overlay = new JFrame();
        overlay.setType(javax.swing.JFrame.Type.UTILITY);
        overlay.setUndecorated(true);
        overlay.setBackground(new Color(0, 0, 0, 0)); // Transparent background
        overlay.setAlwaysOnTop(true);

        overlay.add(generateTelemetryPanel());

        overlay.setVisible(true);
    }

    private JPanel generateTelemetryPanel() {
        JLabel dateTimeLabel = new JLabel("NW");
        dateTimeLabel.setForeground(Color.WHITE);

        JLabel batteryLabel = new JLabel("NE");
        batteryLabel.setForeground(Color.WHITE);
        batteryLabel.setVerticalTextPosition(SwingConstants.CENTER);
        batteryLabel.setHorizontalTextPosition(SwingConstants.CENTER);

        JLabel pitchRollLabel = new JLabel("SW");
        pitchRollLabel.setForeground(Color.WHITE);

        JLabel gpsLabel = new JLabel("SE");
        gpsLabel.setForeground(Color.WHITE);

        JLabel compassLabel = new JLabel("Compass");
        compassLabel.setForeground(Color.WHITE);

        JLabel groundSpeedLabel = new JLabel("Ground speed");
        groundSpeedLabel.setForeground(Color.WHITE);

        JLabel altitudeLabel = new JLabel("Altitude");
        altitudeLabel.setForeground(Color.WHITE);

        JLabel targetDistanceLabel = new JLabel("Target distance");
        targetDistanceLabel.setForeground(Color.WHITE);

        Font fontConsole = new Font("Consolas", Font.PLAIN, 14);

        //

        JPanel result = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                dateTimeLabel.setFont(fontConsole.deriveFont(Font.PLAIN, (int) (windowHeightParam * 14)));
                dateTimeLabel.setText(generateDateTimeInfo());

                batteryLabel.setFont(fontConsole.deriveFont(Font.PLAIN, (int) (windowHeightParam * 14)));
                batteryLabel.setText(generateBatteryInfo());

                pitchRollLabel.setFont(fontConsole.deriveFont(Font.PLAIN, (int) (windowHeightParam * 14)));
                pitchRollLabel.setText(generatePitchRollInfo());

                gpsLabel.setFont(fontConsole.deriveFont(Font.PLAIN, (int) (windowHeightParam * 14)));
                gpsLabel.setText(generateGPSInfo());

                compassLabel.setFont(fontConsole.deriveFont(Font.PLAIN, (int) (windowHeightParam * 14)));
                compassLabel.setText(generateCompassInfo());

                groundSpeedLabel.setFont(fontConsole.deriveFont(Font.PLAIN, (int) (windowHeightParam * 14)));
                groundSpeedLabel.setText(generateGroundSpeedInfo());

                altitudeLabel.setFont(fontConsole.deriveFont(Font.PLAIN, (int) (windowHeightParam * 14)));
                altitudeLabel.setText(generateAltitudeInfo());

                targetDistanceLabel.setFont(fontConsole.deriveFont(Font.PLAIN, (int) (windowHeightParam * 14)));
                targetDistanceLabel.setText(generateTargetDistanceInfo());
            }
        };

        SpringLayout springLayout = new SpringLayout();
        result.setLayout(springLayout);

        springLayout.putConstraint(SpringLayout.WEST,
                dateTimeLabel,
                30,
                SpringLayout.WEST,
                result);
        springLayout.putConstraint(SpringLayout.NORTH,
                dateTimeLabel,
                50,
                SpringLayout.NORTH,
                result);

        springLayout.putConstraint(SpringLayout.EAST,
                batteryLabel,
                -30,
                SpringLayout.EAST,
                result);
        springLayout.putConstraint(SpringLayout.NORTH,
                batteryLabel,
                50,
                SpringLayout.NORTH,
                result);

        springLayout.putConstraint(SpringLayout.WEST,
                pitchRollLabel,
                30,
                SpringLayout.WEST,
                result);
        springLayout.putConstraint(SpringLayout.SOUTH,
                pitchRollLabel,
                -20,
                SpringLayout.SOUTH,
                result);

        springLayout.putConstraint(SpringLayout.EAST,
                gpsLabel,
                -30,
                SpringLayout.EAST,
                result);
        springLayout.putConstraint(SpringLayout.SOUTH,
                gpsLabel,
                -20,
                SpringLayout.SOUTH,
                result);

        springLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER,
                compassLabel,
                (int) windowHeightParam * -25,
                SpringLayout.HORIZONTAL_CENTER,
                result);
        springLayout.putConstraint(SpringLayout.NORTH,
                compassLabel,
                50,
                SpringLayout.NORTH,
                result);

        springLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER,
                groundSpeedLabel,
                (int) windowHeightParam * -25,
                SpringLayout.HORIZONTAL_CENTER,
                result);
        springLayout.putConstraint(SpringLayout.SOUTH,
                groundSpeedLabel,
                -20,
                SpringLayout.SOUTH,
                result);

        springLayout.putConstraint(SpringLayout.EAST,
                altitudeLabel,
                -30,
                SpringLayout.EAST,
                result);
        springLayout.putConstraint(SpringLayout.VERTICAL_CENTER,
                altitudeLabel,
                0,
                SpringLayout.VERTICAL_CENTER,
                result);

        springLayout.putConstraint(SpringLayout.WEST,
                targetDistanceLabel,
                30,
                SpringLayout.WEST,
                result);
        springLayout.putConstraint(SpringLayout.VERTICAL_CENTER,
                targetDistanceLabel,
                0,
                SpringLayout.VERTICAL_CENTER,
                result);

        result.add(dateTimeLabel);
        result.add(batteryLabel);
        result.add(pitchRollLabel);
        result.add(gpsLabel);
        result.add(compassLabel);
        result.add(groundSpeedLabel);
        result.add(altitudeLabel);
        result.add(targetDistanceLabel);

        result.setOpaque(false);

        return result;
    }

    private String generateDateTimeInfo() {
        return "<html>" +
                "<p>" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("d MMM yyyy HH:mm:ss.SS")) + "</p>" +
                "<p>&nbsp;</p>" +
                "<p>Armed&nbsp;&nbsp;&nbsp;" + getYesNoIcon(FlightConfigService.getInstance().getFlightStatus().isArmed()) + "</p>" +
                "<p>Angle&nbsp;&nbsp;&nbsp;" + getYesNoIcon(FlightConfigService.getInstance().getFlightStatus().isAngleMode()) + "</p>" +
                "<p>AltHold&nbsp;" + getYesNoIcon(FlightConfigService.getInstance().getFlightStatus().isAltHoldMode()) + "</p>" +
                "</html>";
    }

    private String getYesNoIcon(boolean value) {
        return (value) ?
                GamepadHelper.getIcon("e86c", (int) (windowHeightParam * 8)) :
                GamepadHelper.getIcon("ef4a", (int) (windowHeightParam * 8));
    }

    private String generateCompassInfo() {
        String flightMode = (FlightConfigService.getInstance().getFlightStatus().getFlightMode() != null) ?
                FlightConfigService.getInstance().getFlightStatus().getFlightMode().replace("!", "") :
                "----";

        return "<html>" +
                "<p style='text-align:center;'>" + flightMode + "</p>" +
                "<p>" + generateCompassDirections(FlightConfigService.getInstance().getFlightStatus().getYaw()) + "</p>" +
                "<p>" + GamepadHelper.drawTarget(
                (int) FlightConfigService.getInstance().getFlightStatus().getYaw(),
                GamepadHelper.getIcon("e88a", (int) (windowHeightParam * 8)),
                GamepadHelper.getIcon("e55d", (int) (windowHeightParam * 8))) + "</p>" +
                "</html>";
    }

    private String generateBatteryInfo() {
        String batteryIcon = GamepadHelper.getIcon("e1a4", (int) (windowHeightParam * 8));

        long remainingInt = Math.round(FlightConfigService.getInstance().getFlightStatus().getRemaining());

        String voltage = String.format(Locale.US, "%,.2fV", FlightConfigService.getInstance().getFlightStatus().getVoltage());
        String current = String.format(Locale.US, "%,.2fA", FlightConfigService.getInstance().getFlightStatus().getCurrent());
        String remaining = remainingInt + "%";
        String fuel = Math.round(FlightConfigService.getInstance().getFlightStatus().getFuel()) + "mAh";

        if (remainingInt > 50 && remainingInt <= 75) {
            batteryIcon = GamepadHelper.getIcon("ebd4", (int) (windowHeightParam * 8));
        } else if (remainingInt >= 25 && remainingInt < 50) {
            batteryIcon = GamepadHelper.getIcon("ebe2", (int) (windowHeightParam * 8));
        } else if (remainingInt < 25) {
            batteryIcon = GamepadHelper.getIcon("e19c", (int) (windowHeightParam * 8));
        }

        return "<html>" +
                "<p>" + batteryIcon + " " + remaining + " " + fuel + "</p>" +
                "<p>" + voltage + " " + current + "</p>" +
                "</html>";
    }

    private String generateAltitudeInfo() {
        return "<html>" +
                "<p>" + GamepadHelper.getIcon("ea16",
                (int) (windowHeightParam * 8)) + " " + FlightConfigService.getInstance().getFlightStatus().getAltitude() + "m</p>" +
                "</html>";
    }

    private String generateGPSInfo() {
        return "<html>" +
                "<p>Lat " + String.format(Locale.US, "%,.8f", FlightConfigService.getInstance().getFlightStatus().getLatitude()) + "</p>" +
                "<p>Lon " + String.format(Locale.US, "%,.8f", FlightConfigService.getInstance().getFlightStatus().getLongitude()) + "</p>" +
                "<p>" + GamepadHelper.getIcon("eb3a",
                (int) (windowHeightParam * 8)) + " " + FlightConfigService.getInstance().getFlightStatus().getSatellites() + "</p>" +
                "</html>";
    }

    private String generateGroundSpeedInfo() {
        return "<html>" +
                "<p>" +
                GamepadHelper.getIcon("e539", (int) (windowHeightParam * 8)) +
                " " +
                String.format(Locale.US, "%,.2fm/s", FlightConfigService.getInstance().getFlightStatus().getGroundSpeed()) +
                "</p>" +
                "</html>";
    }

    private String generatePitchRollInfo() {
        return "<html>" +
                "<p>" + String.format(Locale.US, "P %,.2f°", FlightConfigService.getInstance().getFlightStatus().getPitch()) + "</p>" +
                "<p>" + String.format(Locale.US, "R %,.2f°", FlightConfigService.getInstance().getFlightStatus().getRoll()) + "</p>" +
                "<p>" + String.format(Locale.US, "Y %,.2f°", FlightConfigService.getInstance().getFlightStatus().getYaw()) + "</p>" +
                "</html>";
    }

    private String generateTargetDistanceInfo() {
        return "<html>" + GamepadHelper.generateTargetDistanceList(windowHeightParam) + "</html>";
    }

}
