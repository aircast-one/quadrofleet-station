package com.quadrofleet.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.quadrofleet.model.FlightConfig;
import com.quadrofleet.model.FlightStatus;

public class FlightConfigService implements IFlightConfigService {

    private static FlightConfigService instance;

    private final FlightStatus flightStatus = new FlightStatus();

    private final FlightConfig flightConfig = new FlightConfig();

    private final ObjectMapper objectMapper;

    private FlightConfigService() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
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

    @Override
    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    @Override
    public String getJSONFlightStatus() throws JsonProcessingException {
        return objectMapper.writeValueAsString(flightStatus);
    }

}
