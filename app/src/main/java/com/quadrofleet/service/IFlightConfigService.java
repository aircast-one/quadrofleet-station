package com.quadrofleet.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quadrofleet.model.FlightConfig;
import com.quadrofleet.model.FlightStatus;

public interface IFlightConfigService {

    void flush();

    FlightStatus getFlightStatus();

    FlightConfig getFlightConfig();

    ObjectMapper getObjectMapper();

    String getJSONFlightStatus() throws JsonProcessingException;

}
