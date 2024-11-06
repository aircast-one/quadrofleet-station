package com.quadrofleet.service;

import com.quadrofleet.model.FlightConfig;
import systems.beep.crossfire.ChannelBuilder;
import systems.beep.crossfire.frame.ChannelsFrame;
import systems.beep.crossfire.frame.sub.Address;
import systems.beep.helper.TelemetryHelper;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.logging.Logger;

public class FrameSenderService {

    private final Logger logger = Logger.getLogger(FrameSenderService.class.getName());

    private int interval = 20;

    private String udpTargetUrl = "100.96.1.3";

    private String udpTargetPort = "10800";

    private DatagramSocket socket;

    private final Thread THREAD;

    public FrameSenderService() {
        initSocket();

        THREAD = new Thread(this::sendChannelsFrame);
    }

    private void sendChannelsFrame() {
        while (true) {
            byte[] channelsFrame = ChannelsFrame.builder()
                    .setAddress(Address.FLIGHT_CONTROLLER)
                    .setChannels(generateChannelsByFlightConfig(FlightConfigService.getInstance().getFlightConfig()))
                    .build();

            // Build by config

            sendUdpPacket(channelsFrame);

            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void start() {
        THREAD.start();
    }

    public void stop() {
        THREAD.interrupt();
    }

    private void initSocket() {
        try {
            socket = new DatagramSocket();

            logger.info("UDP sender initialized");
        } catch (SocketException e) {
            logger.severe(e.getMessage());
        }
    }

    private boolean isInitialized() {
        return socket != null && !socket.isClosed();
    }

    private void sendUdpPacket(byte[] data) {
        if (!isInitialized()) {
            logger.warning("Socket is not initialized");
            return;
        }

        try {
            socket.send(new DatagramPacket(
                    data,
                    data.length,
                    InetAddress.getByName(udpTargetUrl),
                    Integer.parseInt(udpTargetPort)
            ));
        } catch (Exception e) {
            logger.severe(e.getMessage());
        }
    }

    private static int[] generateChannelsByFlightConfig(FlightConfig flightConfig) {
        ChannelBuilder builder = ChannelBuilder.builder()
                .setRoll(flightConfig.getRoll())
                .setPitch(flightConfig.getPitch())
                .setYaw(flightConfig.getYaw())

                .setArmed(flightConfig.isArmed())
                .setChannel6(flightConfig.isAngleMode())
                .setChannel7(flightConfig.isAltHoldMode())

                .setChannel8(TelemetryHelper.FAILSAFE_MICROSECONDS_VALUE)
                .setChannel9(TelemetryHelper.FAILSAFE_MICROSECONDS_VALUE)
                .setChannel10(TelemetryHelper.FAILSAFE_MICROSECONDS_VALUE)
                .setChannel11(TelemetryHelper.FAILSAFE_MICROSECONDS_VALUE)
                .setChannel12(TelemetryHelper.FAILSAFE_MICROSECONDS_VALUE)
                .setChannel13(TelemetryHelper.FAILSAFE_MICROSECONDS_VALUE)
                .setChannel14(TelemetryHelper.FAILSAFE_MICROSECONDS_VALUE)
                .setChannel15(TelemetryHelper.FAILSAFE_MICROSECONDS_VALUE)
                .setChannel16(TelemetryHelper.FAILSAFE_MICROSECONDS_VALUE);

        if (flightConfig.isAltHoldMode()) {
            builder.setThrottle(flightConfig.getThrottle());
        } else {
            builder.setChannel3(generateThrottleInPositiveMode(flightConfig));
        }

        return builder.build();
    }

    private static int generateThrottleInPositiveMode(FlightConfig flightConfig) {
        return (int) (TelemetryHelper.FAILSAFE_CRSF_VALUE
                + Math.max(0, Math.min(1, flightConfig.getThrottle()))
                * (TelemetryHelper.MAX_MICROSECONDS_VALUE - TelemetryHelper.FAILSAFE_CRSF_VALUE));
    }

}
