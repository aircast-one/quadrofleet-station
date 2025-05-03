package com.quadrofleet.service;

import com.quadrofleet.helper.GamepadHelper;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Locale;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class OSDService {

    private final Logger logger = Logger.getLogger(OSDService.class.getName());

    private final Thread THREAD;

    private ImageView view;

    private BorderPane pane;

    // DateTime label
    private Label dateTimeLabel;

    private Label statusIcon;

    // Pitch/Roll label
    private Label pitchLabel;

    // GroundSpeed label
    private Label groundSpeedLabel;

    // Altitude label
    private Label altitudeLabel;

    // GPS label
    private Label gpsLabel;

    // Distance label
    private Label distanceLabel;

    private Label distanceIcon;

    // Compass label
    private Label flightModeLabel;

    private Label compassDirectionLabel;

    private Label compassTargetIcon;

    // Battery label
    private Label batteryLabel;

    //

    public OSDService() {
        THREAD = new Thread(this::refreshOSD);
    }

    private static String getYesNoIcon(boolean value) {
        return (value) ? "\uE5CA" : "\uE15B";
    }

    private static String generateStatusIcons() {
        return "\n\n" +
                "            " + getYesNoIcon(FlightConfigService.getInstance().getFlightStatus().isArmed()) + "\n" +
                "            " + getYesNoIcon(FlightConfigService.getInstance().getFlightStatus().isAngleMode()) + "\n" +
                "            " + getYesNoIcon(FlightConfigService.getInstance().getFlightStatus().isAltHoldMode());
    }

    private static String generateBatteryGSMInfo() {
        long remainingInt = Math.round(FlightConfigService.getInstance().getFlightStatus().getRemaining());

        String voltage = String.format(Locale.US, "%,.2fV", FlightConfigService.getInstance().getFlightStatus().getVoltage());
        String current = String.format(Locale.US, "%,.2fA", FlightConfigService.getInstance().getFlightStatus().getCurrent());
        String remaining = remainingInt + "%";
        String fuel = Math.round(FlightConfigService.getInstance().getFlightStatus().getFuel()) + "mAh";

        String boardTemperature = "Temp: " +
                String.format(Locale.US, "%d°C", FlightConfigService.getInstance().getFlightStatus().getBoardTemperature());
        String rx = "Rx: " + FlightConfigService.getInstance().getFlightStatus().getRxSpeed() + " KB/s";
        String tx = "Tx: " + FlightConfigService.getInstance().getFlightStatus().getTxSpeed() + " KB/s";
        String rssi = "RSSI: " + FlightConfigService.getInstance().getFlightStatus().getRssi();
        String snr = "SNR: " + FlightConfigService.getInstance().getFlightStatus().getSnr();

        return "Bat: " + remaining + " " + fuel + " " + "\n" +
                voltage + " " + current + "\n\n" +
                boardTemperature + "\n" +
                rx + "\n" +
                tx + "\n" +
                rssi + "\n" +
                snr + "\n";
    }

    private static String generateCompassTargetIcons() {
        return "\n\n" + GamepadHelper.drawTarget(
                (int) FlightConfigService.getInstance().getFlightStatus().getYaw(), "H", "˄");
    }

    private static String generateCompassDirectionLabel() {
        return "\n" + GamepadHelper.generateCompassDirections(FlightConfigService.getInstance().getFlightStatus().getYaw());
    }

    private static String generateFlightModeLabel() {
        return (FlightConfigService.getInstance().getFlightStatus().getFlightMode() != null) ?
                FlightConfigService.getInstance().getFlightStatus().getFlightMode().replace("!", "") :
                "----";
    }

    private static String generateGPSLabel() {
        return "    " + FlightConfigService.getInstance().getFlightStatus().getSatellites() + "\n" +
                "Lat " + String.format(Locale.US, "%,.8f", FlightConfigService.getInstance().getFlightStatus().getLatitude()) + "\n" +
                "Lon " + String.format(Locale.US, "%,.8f", FlightConfigService.getInstance().getFlightStatus().getLongitude());
    }

    private static String generateAltitudeLabel() {
        return "Alt: " + FlightConfigService.getInstance().getFlightStatus().getAltitude() + "m";
    }

    private static String generateGroundSpeedLabel() {
        return "Speed: " + String.format(Locale.US, "%,.2fm/s", FlightConfigService.getInstance().getFlightStatus().getGroundSpeed());
    }

    private static String generatePitchRollLabel() {
        return String.format(Locale.US, "Pitch %,.2f°", FlightConfigService.getInstance().getFlightStatus().getPitch()) + "\n" +
                String.format(Locale.US, "Roll  %,.2f°", FlightConfigService.getInstance().getFlightStatus().getRoll()) + "\n" +
                String.format(Locale.US, "Yaw   %,.2f°", FlightConfigService.getInstance().getFlightStatus().getYaw());
    }

    private static String generateDateTimeLabel() {
        return LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("d MMM yyyy HH:mm:ss.SS")) + "\n\nArmed\nAngle\nAltHold";
    }

    private static String generateDistanceIcons() {
        return FlightConfigService.getInstance()
                .getFlightConfig()
                .getMapPoints()
                .stream()
                .sorted(Comparator.comparing(GamepadHelper::getPointDistance))
                .map(point -> (point.isHome()) ? "\uE88A\n" : "\uE55D\n")
                .collect(Collectors.joining());
    }

    private static String generateDistanceLabel() {
        return FlightConfigService.getInstance()
                .getFlightConfig()
                .getMapPoints()
                .stream()
                .sorted(Comparator.comparing(GamepadHelper::getPointDistance))
                .map(point -> String.format(Locale.US, "   %dm\n", GamepadHelper.getPointDistance(point)))
                .collect(Collectors.joining());
    }

    private void init(ImageView view, BorderPane pane) {
        // Left - Top - Date/Time
        dateTimeLabel = new Label("Datetime");
        dateTimeLabel.getStyleClass().add("label");

        statusIcon = new Label("Status");
        statusIcon.getStyleClass().add("icon");

        // Left - Bottom - Pitch/Roll
        pitchLabel = new Label("Pitch/Roll");
        pitchLabel.getStyleClass().add("label");

        // Center - Bottom - GroundSpeed
        groundSpeedLabel = new Label("GroundSpeed");
        groundSpeedLabel.getStyleClass().add("label");

        // Right - Center - Altitude
        altitudeLabel = new Label("Altitude");
        altitudeLabel.getStyleClass().add("label");

        // Right - Bottom - GPS
        Label gpsIcon = new Label("\uEB3A                 \n\n\n\n\n");
        gpsIcon.getStyleClass().add("icon");

        gpsLabel = new Label("GPS");
        gpsLabel.getStyleClass().add("label");

        // Left - Center - Distance
        distanceIcon = new Label("");
        distanceIcon.getStyleClass().add("icon");

        distanceLabel = new Label("GPS");
        distanceLabel.getStyleClass().add("label");

        // Center - Top - Compass
        flightModeLabel = new Label("OK");
        flightModeLabel.getStyleClass().add("label");

        compassDirectionLabel = new Label("OK");
        compassDirectionLabel.getStyleClass().add("label");

        compassTargetIcon = new Label("OK");
        compassTargetIcon.getStyleClass().add("label");

        // Right - Top - Battery
        batteryLabel = new Label("Battery");
        batteryLabel.getStyleClass().add("label");

        StackPane stackPane = new StackPane();

        stackPane.getChildren().addAll(view,
                dateTimeLabel,
                statusIcon,
                pitchLabel,
                groundSpeedLabel,
                altitudeLabel,
                gpsLabel,
                gpsIcon,
                distanceLabel,
                distanceIcon,
                flightModeLabel,
                compassDirectionLabel,
                compassTargetIcon,
                batteryLabel
        );

        // Left - Top - Date/Time
        StackPane.setAlignment(dateTimeLabel, Pos.TOP_LEFT);
        StackPane.setAlignment(statusIcon, Pos.TOP_LEFT);

        // Left - Bottom - Pitch/Roll
        StackPane.setAlignment(pitchLabel, Pos.BOTTOM_LEFT);

        // Center - Bottom - GroundSpeed
        StackPane.setAlignment(groundSpeedLabel, Pos.BOTTOM_CENTER);

        // Right - Center - Altitude
        StackPane.setAlignment(altitudeLabel, Pos.CENTER_RIGHT);

        // Right - Bottom - GPS
        StackPane.setAlignment(gpsLabel, Pos.BOTTOM_RIGHT);
        StackPane.setAlignment(gpsIcon, Pos.BOTTOM_RIGHT);

        // Left - Center - Distance
        StackPane.setAlignment(distanceLabel, Pos.CENTER_LEFT);
        StackPane.setAlignment(distanceIcon, Pos.CENTER_LEFT);

        // Center - Top - Compass
        StackPane.setAlignment(flightModeLabel, Pos.TOP_CENTER);
        StackPane.setAlignment(compassDirectionLabel, Pos.TOP_CENTER);

        // Right - Top - Battery
        StackPane.setAlignment(compassTargetIcon, Pos.TOP_CENTER);
        StackPane.setAlignment(batteryLabel, Pos.TOP_RIGHT);

        pane.setCenter(stackPane);
    }

    private void refreshOSD() {
        while (true) {
            Platform.runLater(() -> {
                        // DateTime label
                        dateTimeLabel.setText(generateDateTimeLabel());
                        statusIcon.setText(generateStatusIcons());

                        // Pitch/Roll label
                        pitchLabel.setText(generatePitchRollLabel());

                        // GroundSpeed label
                        groundSpeedLabel.setText(generateGroundSpeedLabel());

                        // Altitude label
                        altitudeLabel.setText(generateAltitudeLabel());

                        // GPS label
                        gpsLabel.setText(generateGPSLabel());

                        // Distance label
                        distanceLabel.setText(generateDistanceLabel());
                        distanceIcon.setText(generateDistanceIcons());

                        // Compass label
                        flightModeLabel.setText(generateFlightModeLabel());
                        compassDirectionLabel.setText(generateCompassDirectionLabel());
                        compassTargetIcon.setText(generateCompassTargetIcons());

                        // Battery label
                        batteryLabel.setText(generateBatteryGSMInfo());
                    }
            );

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void start() {
        fontInit();
        init(view, pane);
        THREAD.start();
    }

    public void stop() {
        THREAD.interrupt();
    }

    public ImageView getView() {
        return view;
    }

    public OSDService setView(ImageView view) {
        this.view = view;
        return this;
    }

    public BorderPane getPane() {
        return pane;
    }

    public OSDService setPane(BorderPane pane) {
        this.pane = pane;
        return this;
    }

    private void fontInit() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("MaterialIcons-Regular.ttf")) {
            if (inputStream != null) {
                Font font = Font.loadFont(inputStream, 20);
                if (font != null) {
                    logger.info("Loaded font: " + font.getName());
                }
            }
        } catch (IOException e) {
            logger.warning("Error loading Material Icons font.");
        }
    }

}
