package com.quadrofleet.service;

import com.quadrofleet.model.FlightConfig;
import com.quadrofleet.model.FlightStatus;

public class FlightConfigService implements IFlightConfigService {

    private static FlightConfigService instance;

    private final FlightStatus flightStatus = new FlightStatus();

    private final FlightConfig flightConfig = new FlightConfig();

    private FlightConfigService() {
        //
    }

    public static synchronized FlightConfigService getInstance() {
        if (instance == null) {
            instance = new FlightConfigService();
        }

        return instance;
    }

    @Override
    public void flush() {
        flightStatus.flush();
    }

    @Override
    public FlightStatus getFlightStatus() {
        return flightStatus;
    }

    @Override
    public FlightConfig getFlightConfig() {
        return flightConfig;
    }

}
