package com.slusarczykr.terminal.simulation.model;

import deskit.SimActivity;
import deskit.SimObject;
import deskit.random.SimGenerator;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Flight extends SimObject {

    private final int id;
    private final Queue<Passenger> passengers;
    private final double departureTime;
    private final SimActivity departureFlightAction;

    public Flight(int id, SimActivity departureFlightAction) {
        this.id = id;
        this.passengers = new ConcurrentLinkedQueue<>();
        this.departureTime = new SimGenerator().chisquare(50);
        this.departureFlightAction = departureFlightAction;
    }

    public int getId() {
        return id;
    }

    public void addPassenger(Passenger passenger) {
        this.passengers.add(passenger);
    }

    public Queue<Passenger> getPassengers() {
        return passengers;
    }

    public double getDepartureTime() {
        return departureTime;
    }

    public SimActivity getDepartureFlightActivity() {
        return departureFlightAction;
    }

    @Override
    public String toString() {
        return "Flight{" +
                "id=" + id +
                ", departureTime=" + departureTime +
                '}';
    }
}
