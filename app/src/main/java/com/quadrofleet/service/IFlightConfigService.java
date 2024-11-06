package com.quadrofleet.service;

import com.quadrofleet.model.FlightConfig;
import com.quadrofleet.model.FlightStatus;

public interface IFlightConfigService {

    void flush();

    FlightStatus getFlightStatus();

    FlightConfig getFlightConfig();

}
