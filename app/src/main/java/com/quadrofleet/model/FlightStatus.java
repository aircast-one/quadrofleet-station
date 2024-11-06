package com.quadrofleet.model;

import java.time.LocalDateTime;

public class FlightStatus {

    private boolean portStatus;

    //

    private LocalDateTime lastPacket;

    private String deviceName;

    private String serialNumber;

    private String hardwareVersion;

    private String softwareVersion;

    private Integer fieldCount;

    private Integer paramVersion;

    private long syncFrameOffset;

    private long syncFrameRate;

    // Attitude

    private double pitch;

    private double roll;

    private double yaw;

    private double throttle;

    // Flight mode

    private String flightMode;

    private boolean armed;

    private boolean angleMode;

    private boolean altHoldMode;

    // Battery

    private double voltage;

    private double current;

    private double fuel;

    private double remaining;

    // Link statistics

    private int uplinkRSSI1;

    private int uplinkRSSI2;

    private int uplinkLinkQuality;

    private int uplinkSNR;

    private int activeAntenna;

    private int radioFrequencyMode;

    private int uplinkPower;

    private int downlinkRSSI;

    private int downlinkLinkQuality;

    private int downlinkSNR;

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
        setPortStatus(false);

        setLastPacket(null);

        setDeviceName(null);
        setSerialNumber(null);
        setHardwareVersion(null);
        setSoftwareVersion(null);
        setFieldCount(null);
        setParamVersion(null);

        setSyncFrameRate(0);
        setSyncFrameOffset(0);

        setPitch(0);
        setRoll(0);
        setYaw(0);
        setThrottle(0);

        setFlightMode(null);
        setArmed(false);

        setVoltage(0);
        setCurrent(0);
        setFuel(0);
        setRemaining(0);

        setUplinkRSSI1(0);
        setUplinkRSSI2(0);
        setUplinkLinkQuality(0);
        setUplinkSNR(0);
        setUplinkPower(0);
        setActiveAntenna(0);
        setRadioFrequencyMode(0);
        setDownlinkRSSI(0);
        setDownlinkSNR(0);
        setDownlinkLinkQuality(0);

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

    public boolean isPortStatus() {
        return portStatus;
    }

    public FlightStatus setPortStatus(boolean portStatus) {
        this.portStatus = portStatus;
        return this;
    }

    public LocalDateTime getLastPacket() {
        return lastPacket;
    }

    public FlightStatus setLastPacket(LocalDateTime lastPacket) {
        this.lastPacket = lastPacket;
        return this;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public FlightStatus setDeviceName(String deviceName) {
        this.deviceName = deviceName;
        return this;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public FlightStatus setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
        return this;
    }

    public String getHardwareVersion() {
        return hardwareVersion;
    }

    public FlightStatus setHardwareVersion(String hardwareVersion) {
        this.hardwareVersion = hardwareVersion;
        return this;
    }

    public String getSoftwareVersion() {
        return softwareVersion;
    }

    public FlightStatus setSoftwareVersion(String softwareVersion) {
        this.softwareVersion = softwareVersion;
        return this;
    }

    public Integer getFieldCount() {
        return fieldCount;
    }

    public FlightStatus setFieldCount(Integer fieldCount) {
        this.fieldCount = fieldCount;
        return this;
    }

    public Integer getParamVersion() {
        return paramVersion;
    }

    public FlightStatus setParamVersion(Integer paramVersion) {
        this.paramVersion = paramVersion;
        return this;
    }

    public long getSyncFrameOffset() {
        return syncFrameOffset;
    }

    public FlightStatus setSyncFrameOffset(long syncFrameOffset) {
        this.syncFrameOffset = syncFrameOffset;
        return this;
    }

    public long getSyncFrameRate() {
        return syncFrameRate;
    }

    public FlightStatus setSyncFrameRate(long syncFrameRate) {
        this.syncFrameRate = syncFrameRate;
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

    public int getUplinkRSSI1() {
        return uplinkRSSI1;
    }

    public FlightStatus setUplinkRSSI1(int uplinkRSSI1) {
        this.uplinkRSSI1 = uplinkRSSI1;
        return this;
    }

    public int getUplinkRSSI2() {
        return uplinkRSSI2;
    }

    public FlightStatus setUplinkRSSI2(int uplinkRSSI2) {
        this.uplinkRSSI2 = uplinkRSSI2;
        return this;
    }

    public int getUplinkLinkQuality() {
        return uplinkLinkQuality;
    }

    public FlightStatus setUplinkLinkQuality(int uplinkLinkQuality) {
        this.uplinkLinkQuality = uplinkLinkQuality;
        return this;
    }

    public int getUplinkSNR() {
        return uplinkSNR;
    }

    public FlightStatus setUplinkSNR(int uplinkSNR) {
        this.uplinkSNR = uplinkSNR;
        return this;
    }

    public int getActiveAntenna() {
        return activeAntenna;
    }

    public FlightStatus setActiveAntenna(int activeAntenna) {
        this.activeAntenna = activeAntenna;
        return this;
    }

    public int getRadioFrequencyMode() {
        return radioFrequencyMode;
    }

    public FlightStatus setRadioFrequencyMode(int radioFrequencyMode) {
        this.radioFrequencyMode = radioFrequencyMode;
        return this;
    }

    public int getUplinkPower() {
        return uplinkPower;
    }

    public FlightStatus setUplinkPower(int uplinkPower) {
        this.uplinkPower = uplinkPower;
        return this;
    }

    public int getDownlinkRSSI() {
        return downlinkRSSI;
    }

    public FlightStatus setDownlinkRSSI(int downlinkRSSI) {
        this.downlinkRSSI = downlinkRSSI;
        return this;
    }

    public int getDownlinkLinkQuality() {
        return downlinkLinkQuality;
    }

    public FlightStatus setDownlinkLinkQuality(int downlinkLinkQuality) {
        this.downlinkLinkQuality = downlinkLinkQuality;
        return this;
    }

    public int getDownlinkSNR() {
        return downlinkSNR;
    }

    public FlightStatus setDownlinkSNR(int downlinkSNR) {
        this.downlinkSNR = downlinkSNR;
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
