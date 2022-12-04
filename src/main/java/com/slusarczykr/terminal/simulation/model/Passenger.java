package com.slusarczykr.terminal.simulation.model;

public class Passenger {
    private final double generationTime;

    public Passenger(double generationTime) {
        this.generationTime = generationTime;
    }

    public double getGenerationTime() {
        return generationTime;
    }
}
