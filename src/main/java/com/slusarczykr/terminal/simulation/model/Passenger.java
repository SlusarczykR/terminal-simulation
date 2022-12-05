package com.slusarczykr.terminal.simulation.model;

import java.util.UUID;

public class Passenger {

    private final String uid;
    private final int flightId;
    private final double generationTime;

    public Passenger(double generationTime, int flightId) {
        this.uid = UUID.randomUUID().toString();
        this.flightId = flightId;
        this.generationTime = generationTime;
    }

    public int getFlightId() {
        return flightId;
    }

    public String getUid() {
        return uid;
    }

    public double getGenerationTime() {
        return generationTime;
    }

    @Override
    public String toString() {
        return "Passenger{" +
                "uid='" + uid + '\'' +
                ", flightId=" + flightId +
                ", generationTime=" + generationTime +
                '}';
    }
}
