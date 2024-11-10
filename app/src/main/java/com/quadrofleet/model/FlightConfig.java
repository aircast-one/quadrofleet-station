package com.quadrofleet.model;

public class FlightConfig {

    private ThrottleMode throttleMode = ThrottleMode.ACRO;

    /**
     * The border limit for movement adjustments.
     */
    private double movementBorders = 1;

    /**
     * The minimum activation zone threshold.
     */
    private double activationZone = 0.1;

    /**
     * The pitch of the flight control.
     */
    private double pitch = 0;

    /**
     * Whether the pitch control is inverted.
     */
    private boolean invertedPitch = false;

    /**
     * The roll of the flight control.
     */
    private double roll = 0;

    /**
     * The yaw of the flight control.
     */
    private double yaw = 0;

    /**
     * The throttle of the flight control.
     */
    private double throttle = 0;

    /**
     * The sensitivity of the pitch adjustments.
     */
    private int pitchSensibility = 100;

    /**
     * The sensitivity of the roll adjustments.
     */
    private int rollSensibility = 100;

    /**
     * The sensitivity of the yaw adjustments.
     */
    private int yawSensibility = 100;

    /**
     * The sensitivity of the throttle adjustments.
     */
    private int throttleSensibility = 100;

    /**
     * Whether the flight control is armed.
     */
    private boolean armed = false;

    /**
     * Whether the drone in angle.
     */
    private boolean angleMode;

    /**
     * Whether the drone in ALTHOLD.
     */
    private boolean altHoldMode;

    /**
     * CHANNEL 8
     */
    private int channel8 = 0;

    /**
     * CHANNEL 9
     */
    private int channel9 = 0;

    /**
     * CHANNEL 10
     */
    private int channel10 = 0;

    /**
     * CHANNEL 11
     */
    private int channel11 = 0;

    /**
     * CHANNEL 12
     */
    private int channel12 = 0;

    /**
     * CHANNEL 13
     */
    private int channel13 = 0;

    /**
     * CHANNEL 14
     */
    private int channel14 = 0;

    /**
     * CHANNEL 15
     */
    private int channel15 = 0;

    /**
     * CHANNEL 16
     */
    private int channel16 = 0;

    private double homeLatitude = 0;

    private double homeLongitude;

    private double targetLatitude = 0;

    private double targetLongitude = 0;

    /**
     * Resets all the flight config data to their default values.
     * Sets numeric fields to zero and string fields to null.
     */
    public void flush() {
        setPitch(0);
        setRoll(0);
        setYaw(0);
        resetThrottle();
        setArmed(false);
        setMovementBorders(1);
        setThrottleMode(ThrottleMode.ACRO);
    }

    public double getPitch() {
        return pitch;
    }

    /**
     * Sets the pitch value for the flight control.
     * The pitch value is adjusted by the activation zone and movement borders.
     * If the absolute pitch is below the activation zone, the pitch is set to 0.
     *
     * @param pitch the desired pitch value
     * @return the current instance of FlightConfig for chaining
     */
    public FlightConfig setPitch(double pitch) {
        this.pitch = (Math.abs(pitch) >= activationZone)
                ? pitch * (invertedPitch ? 1 : -1) * movementBorders
                : 0;
        this.pitch = processSensibility(this.pitch, this.pitchSensibility);

        return this;
    }

    public double getRoll() {
        return roll;
    }

    /**
     * Sets the roll value for the flight control.
     * The roll value is adjusted by the activation zone and movement borders.
     * If the absolute roll is below the activation zone, the roll is set to 0.
     *
     * @param roll the desired roll value
     * @return the current instance of FlightConfig for chaining
     */
    public FlightConfig setRoll(double roll) {
        this.roll = (Math.abs(roll) >= activationZone) ? roll * movementBorders : 0;
        this.roll = processSensibility(this.roll, this.rollSensibility);

        return this;
    }

    public double getYaw() {
        return yaw;
    }

    /**
     * Sets the yaw value for the flight control.
     * The yaw value is adjusted by the activation zone and movement borders.
     * If the absolute yaw is below the activation zone, the yaw is set to 0.
     *
     * @param yaw the desired yaw value
     * @return the current instance of FlightConfig for chaining
     */
    public FlightConfig setYaw(double yaw) {
        this.yaw = (Math.abs(yaw) >= activationZone) ? yaw * movementBorders : 0;
        this.yaw = processSensibility(this.yaw, this.yawSensibility);

        return this;
    }

    public double getThrottle() {
        return throttle;
    }

    /**
     * Sets the throttle value for the flight control.
     * The throttle value is adjusted by the activation zone and throttle sensitivity.
     * The throttle is clamped between 0 and 1.
     * 0 = 0%, 1 = 100%
     *
     * @param throttle the desired throttle value
     * @return the current instance of FlightConfig for chaining
     */
    public FlightConfig setThrottle(double throttle) {
        this.throttle = (Math.abs(throttle) >= activationZone) ? throttle * -1 * movementBorders : 0;
        this.throttle = processSensibility(this.throttle, this.throttleSensibility);

        return this;
    }

    public FlightConfig resetThrottle() {
        this.throttle = 0;

        return this;
    }

    private double processSensibility(double value, int sensibility) {
        return value / 100 * sensibility;
    }

    public boolean isArmed() {
        return armed;
    }

    public FlightConfig setArmed(boolean armed) {
        this.armed = armed;

        return this;
    }

    public double getMovementBorders() {
        return movementBorders;
    }

    public FlightConfig setMovementBorders(double movementBorders) {
        this.movementBorders = movementBorders;

        return this;
    }

    public ThrottleMode getThrottleMode() {
        return throttleMode;
    }

    public FlightConfig setThrottleMode(ThrottleMode throttleMode) {
        this.throttleMode = throttleMode;
        return this;
    }

    public boolean isAngleMode() {
        return angleMode;
    }

    public FlightConfig setAngleMode(boolean angleMode) {
        this.angleMode = angleMode;
        return this;
    }

    public boolean isAltHoldMode() {
        return altHoldMode;
    }

    public FlightConfig setAltHoldMode(boolean altHoldMode) {
        this.altHoldMode = altHoldMode;
        return this;
    }

    public double getTargetLongitude() {
        return targetLongitude;
    }

    public FlightConfig setTargetLongitude(double targetLongitude) {
        this.targetLongitude = targetLongitude;
        return this;
    }

    public double getTargetLatitude() {
        return targetLatitude;
    }

    public FlightConfig setTargetLatitude(double targetLatitude) {
        this.targetLatitude = targetLatitude;
        return this;
    }

    public double getHomeLongitude() {
        return homeLongitude;
    }

    public FlightConfig setHomeLongitude(double homeLongitude) {
        this.homeLongitude = homeLongitude;
        return this;
    }

    public double getHomeLatitude() {
        return homeLatitude;
    }

    public FlightConfig setHomeLatitude(double homeLatitude) {
        this.homeLatitude = homeLatitude;
        return this;
    }

    public enum ThrottleMode {
        ACRO, ANGLE
    }
}
