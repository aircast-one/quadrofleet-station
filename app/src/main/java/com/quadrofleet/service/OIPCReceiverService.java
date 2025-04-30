package com.quadrofleet.service;

import com.quadrofleet.ConfigLoader;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.logging.Logger;

public class OIPCReceiverService {

    private final Logger logger = Logger.getLogger(OIPCReceiverService.class.getName());

    private final Thread THREAD;

    private final int udpLocalPort;

    private final int interval;

    public OIPCReceiverService() {
        udpLocalPort = ConfigLoader.getInstance().getPropertyAsInteger("oipc.stream.receiver.port");
        interval = ConfigLoader.getInstance().getPropertyAsInteger("oipc.stream.receiver.interval");

        THREAD = new Thread(this::telemetryReceivingProcessing);
    }

    public void start() {
        THREAD.start();
    }

    public void stop() {
        THREAD.interrupt();
    }

    private void telemetryReceivingProcessing() {
        try (DatagramSocket socket = new DatagramSocket(udpLocalPort, InetAddress.getByName("0.0.0.0"))) {
            logger.info("OpenIPC UDP receiver started on port " + udpLocalPort + " with interval " + interval + "ms");

            byte[] buffer = new byte[256];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            while (true) {
                socket.receive(packet);

                processTelemetry(Arrays.copyOf(packet.getData(), packet.getLength()));

                Thread.sleep(interval);
            }
        } catch (Exception e) {
            logger.severe(e.getMessage());
        }
    }

    private void processTelemetry(byte[] bytes) {
        String telemetry = new String(bytes); // "Temp: 43 C, R: 11 KB/s, T: 358 KB/s, RSSI: 0, SNR: 0"

        if (telemetry.contains("RSSI")) {
            String[] parts = telemetry.split(", ");
            if (parts.length >= 5) {
                FlightConfigService.getInstance().getFlightStatus().setBoardTemperature(Integer.parseInt(parts[0].split(": ")[1]));
                FlightConfigService.getInstance().getFlightStatus().setRxSpeed(parts[1].split(": ")[1]);
                FlightConfigService.getInstance().getFlightStatus().setTxSpeed(parts[2].split(": ")[1]);
                FlightConfigService.getInstance().getFlightStatus().setRssi(parts[3].split(": ")[1]);
                FlightConfigService.getInstance().getFlightStatus().setSnr(parts[4].split(": ")[1]);
            }
        } else {
            FlightConfigService.getInstance().getFlightStatus().setGsmStatus(telemetry);
        }
    }

}
