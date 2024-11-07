package com.quadrofleet.service;

import com.quadrofleet.ConfigLoader;
import systems.beep.crossfire.frame.AttitudeFrame;
import systems.beep.crossfire.frame.BatteryFrame;
import systems.beep.crossfire.frame.CRSFFrame;
import systems.beep.crossfire.frame.FlightModeFrame;
import systems.beep.crossfire.frame.GPSFrame;
import systems.beep.processor.FrameProcessor;
import systems.beep.processor.IFrameProcessor;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.logging.Logger;

public class FrameReceiverService {

    private final Logger logger = Logger.getLogger(FrameReceiverService.class.getName());

    private final IFrameProcessor frameProcessor = new FrameProcessor();

    private final Thread THREAD;

    public FrameReceiverService() {
        THREAD = new Thread(this::frameReceivingProcessing);
    }

    public void start() {
        THREAD.start();
    }

    public void stop() {
        THREAD.interrupt();
    }

    private void frameReceivingProcessing() {
        int udpLocalPort = ConfigLoader.getInstance().getPropertyAsInteger("telemetry.stream.receiver.port");
        int interval = ConfigLoader.getInstance().getPropertyAsInteger("telemetry.stream.receiver.interval");

        try (DatagramSocket socket = new DatagramSocket(udpLocalPort, InetAddress.getByName("0.0.0.0"))) {
            logger.info("UDP receiver started on port " + udpLocalPort);

            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            while (true) {
                socket.receive(packet);

                frameProcessor.processData(packet.getData(), frame -> {
                    processFrame(frame);

                    if (frame.isTelemetry()) {
                        FlightConfigService.getInstance().getFlightStatus().setNowLastPacket();
                    }
                });

                Thread.sleep(interval);
            }
        } catch (Exception e) {
            logger.severe(e.getMessage());
        }
    }

    private void processFrame(CRSFFrame frame) {
        if (frame instanceof AttitudeFrame) {
            // AttitudeFrame processing
            AttitudeFrame attitudeFrame = (AttitudeFrame) frame;

            FlightConfigService.getInstance().getFlightStatus().setPitch(attitudeFrame.getPitch());
            FlightConfigService.getInstance().getFlightStatus().setRoll(attitudeFrame.getRoll());
            FlightConfigService.getInstance().getFlightStatus().setYaw(attitudeFrame.getYaw());

            FlightConfigService.getInstance().getFlightStatus().setHeading(attitudeFrame.getYaw());
        } else if (frame instanceof BatteryFrame) {
            // BatteryFrame processing
            BatteryFrame batteryFrame = (BatteryFrame) frame;

            FlightConfigService.getInstance().getFlightStatus().setVoltage(batteryFrame.getVoltage());
            FlightConfigService.getInstance().getFlightStatus().setCurrent(batteryFrame.getCurrent());
            FlightConfigService.getInstance().getFlightStatus().setFuel(batteryFrame.getFuel());
            FlightConfigService.getInstance().getFlightStatus().setRemaining(batteryFrame.getRemaining());
        } else if (frame instanceof FlightModeFrame) {
            // FlightModeFrame processing
            FlightModeFrame flightModeFrame = (FlightModeFrame) frame;

            FlightConfigService.getInstance().getFlightStatus().setFlightMode(flightModeFrame.getMode());
        } else if (frame instanceof GPSFrame) {
            // GPSFrame processing
            GPSFrame gpsFrame = (GPSFrame) frame;

            FlightConfigService.getInstance().getFlightStatus().setLatitude(gpsFrame.getLatitude());
            FlightConfigService.getInstance().getFlightStatus().setLongitude(gpsFrame.getLongitude());
            FlightConfigService.getInstance().getFlightStatus().setGroundSpeed(gpsFrame.getGroundSpeed());
            FlightConfigService.getInstance().getFlightStatus().setAltitude(gpsFrame.getAltitude());
            FlightConfigService.getInstance().getFlightStatus().setSatellites(gpsFrame.getSatellites());
        }
    }

}
