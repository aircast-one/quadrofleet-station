package com.quadrofleet.service;

import com.quadrofleet.ConfigLoader;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
            Pattern pattern = Pattern.compile("Temp: (\\d+) C, R: (\\d+) KB/s, T: (\\d+) KB/s, RSSI: (\\d+), SNR: (\\d+)\\n");
            Matcher matcher = pattern.matcher(telemetry);

            if (matcher.matches()) {
                int temperature = Integer.parseInt(matcher.group(1));
                int rxSpeed = Integer.parseInt(matcher.group(2));
                int txSpeed = Integer.parseInt(matcher.group(3));
                int rssi = Integer.parseInt(matcher.group(4));
                int snr = Integer.parseInt(matcher.group(5));

                // Set values in FlightStatus
                FlightConfigService.getInstance().getFlightStatus().setBoardTemperature(temperature);
                FlightConfigService.getInstance().getFlightStatus().setRxSpeed(rxSpeed);
                FlightConfigService.getInstance().getFlightStatus().setTxSpeed(txSpeed);
                FlightConfigService.getInstance().getFlightStatus().setRssi(rssi);
                FlightConfigService.getInstance().getFlightStatus().setSnr(snr);
            }
        } else {
            FlightConfigService.getInstance().getFlightStatus().setGsmStatus(telemetry);
        }
    }

}
