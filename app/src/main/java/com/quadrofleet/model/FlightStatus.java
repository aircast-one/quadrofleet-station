package com.quadrofleet.model;

import java.time.LocalDateTime;

public class FlightStatus {

    // Telemetry

    private boolean armed;

    private boolean angleMode;

    private boolean altHoldMode;

    private String flightMode;

    private double pitch;

    private double roll;

    private double yaw;

    private double throttle;

    private LocalDateTime lastPacket;

    // OpenIPC Telemetry

    private int boardTemperature;

    private String rxSpeed;

    private String txSpeed;

    private String rssi;

    private String snr;

    private String gsmStatus;

    // Battery

    private double voltage;

    private double current;

    private double fuel;

    private double remaining;

    // GPS

    private double latitude;

    private double longitude;

    private double groundSpeed;

    private double heading;

    private int altitude;

    private int satellites;

    /**
     * Resets all the flight status data to their default values.
     * Sets numeric fields to zero and string fields to null.
     */
    public void flush() {
        setArmed(false);
        setAngleMode(false);
        setAltHoldMode(false);
        setFlightMode(null);
        setPitch(0);
        setRoll(0);
        setYaw(0);
        setThrottle(0);

        setLastPacket(null);

        setBoardTemperature(0);
        setRxSpeed("");
        setTxSpeed("");
        setRssi("");
        setSnr("");
        setGsmStatus("");

        setVoltage(0);
        setCurrent(0);
        setFuel(0);
        setRemaining(0);

        setLatitude(0);
        setLongitude(0);
        setGroundSpeed(0);
        setHeading(0);
        setAltitude(0);
        setSatellites(0);
    }

    /**
     * Updates the last packet timestamp to the current date and time.
     */
    public void setNowLastPacket() {
        lastPacket = LocalDateTime.now();
    }

    /**
     * Checks if the last packet timestamp is older than 10 seconds from the current date and time.
     *
     * @return true if the last packet is older than 10 seconds, false otherwise.
     */
    public boolean isLastPacketExpired() {
        return lastPacket != null && LocalDateTime.now().minusSeconds(10).isAfter(lastPacket);
    }

    public LocalDateTime getLastPacket() {
        return lastPacket;
    }

    public FlightStatus setLastPacket(LocalDateTime lastPacket) {
        this.lastPacket = lastPacket;
        return this;
    }

    public int getBoardTemperature() {
        return boardTemperature;
    }

    public FlightStatus setBoardTemperature(int boardTemperature) {
        this.boardTemperature = boardTemperature;
        return this;
    }

    public String getRxSpeed() {
        return rxSpeed;
    }

    public FlightStatus setRxSpeed(String rxSpeed) {
        this.rxSpeed = rxSpeed;
        return this;
    }

    public String getTxSpeed() {
        return txSpeed;
    }

    public FlightStatus setTxSpeed(String txSpeed) {
        this.txSpeed = txSpeed;
        return this;
    }

    public String getRssi() {
        return rssi;
    }

    public FlightStatus setRssi(String rssi) {
        this.rssi = rssi;
        return this;
    }

    public String getSnr() {
        return snr;
    }

    public FlightStatus setSnr(String snr) {
        this.snr = snr;
        return this;
    }

    public String getGsmStatus() {
        return gsmStatus;
    }

    public FlightStatus setGsmStatus(String gsmStatus) {
        this.gsmStatus = gsmStatus;
        return this;
    }

    public double getPitch() {
        return pitch;
    }

    public FlightStatus setPitch(double pitch) {
        this.pitch = pitch;
        return this;
    }

    public double getRoll() {
        return roll;
    }

    public FlightStatus setRoll(double roll) {
        this.roll = roll;
        return this;
    }

    public double getYaw() {
        return yaw;
    }

    public FlightStatus setYaw(double yaw) {
        this.yaw = yaw;
        return this;
    }

    public double getThrottle() {
        return throttle;
    }

    public FlightStatus setThrottle(double throttle) {
        this.throttle = throttle;
        return this;
    }

    public String getFlightMode() {
        return flightMode;
    }

    public FlightStatus setFlightMode(String flightMode) {
        this.flightMode = flightMode;
        return this;
    }

    public boolean isArmed() {
        return armed;
    }

    public FlightStatus setArmed(boolean armed) {
        this.armed = armed;
        return this;
    }

    public boolean isAngleMode() {
        return angleMode;
    }

    public FlightStatus setAngleMode(boolean angleMode) {
        this.angleMode = angleMode;
        return this;
    }

    public boolean isAltHoldMode() {
        return altHoldMode;
    }

    public FlightStatus setAltHoldMode(boolean altHoldMode) {
        this.altHoldMode = altHoldMode;
        return this;
    }

    public double getVoltage() {
        return voltage;
    }

    public FlightStatus setVoltage(double voltage) {
        this.voltage = voltage;
        return this;
    }

    public double getCurrent() {
        return current;
    }

    public FlightStatus setCurrent(double current) {
        this.current = current;
        return this;
    }

    public double getFuel() {
        return fuel;
    }

    public FlightStatus setFuel(double fuel) {
        this.fuel = fuel;
        return this;
    }

    public double getRemaining() {
        return remaining;
    }

    public FlightStatus setRemaining(double remaining) {
        this.remaining = remaining;
        return this;
    }

    public double getLatitude() {
        return latitude;
    }

    public FlightStatus setLatitude(double latitude) {
        this.latitude = latitude;
        return this;
    }

    public double getLongitude() {
        return longitude;
    }

    public FlightStatus setLongitude(double longitude) {
        this.longitude = longitude;
        return this;
    }

    public double getGroundSpeed() {
        return groundSpeed;
    }

    public FlightStatus setGroundSpeed(double groundSpeed) {
        this.groundSpeed = groundSpeed;
        return this;
    }

    public double getHeading() {
        return heading;
    }

    public FlightStatus setHeading(double heading) {
        this.heading = heading;
        return this;
    }

    public int getAltitude() {
        return altitude;
    }

    public FlightStatus setAltitude(int altitude) {
        this.altitude = altitude;
        return this;
    }

    public int getSatellites() {
        return satellites;
    }

    public FlightStatus setSatellites(int satellites) {
        this.satellites = satellites;
        return this;
    }

}
