package com.slusarczykr.terminal.simulation.model;

import com.slusarczykr.terminal.simulation.action.DepartureFlightAction;
import com.slusarczykr.terminal.simulation.coordinator.SimulationCoordinator;
import deskit.SimActivity;
import deskit.SimObject;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static deskit.SimActivity.callActivity;

public class Flight extends SimObject {

    private final int id;
    private final Queue<Passenger> passengers;
    private final SimActivity departureFlightAction;

    public Flight(int id, SimulationCoordinator<Passenger> simulationCoordinator) {
        this.id = id;
        this.passengers = new ConcurrentLinkedQueue<>();
        this.departureFlightAction = callDepartureFlightAction(simulationCoordinator);
    }

    private SimActivity callDepartureFlightAction(SimulationCoordinator<Passenger> simulationCoordinator) {
        SimActivity action = new DepartureFlightAction(simulationCoordinator);
        callActivity(this, action);

        return action;
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

    public SimActivity getDepartureFlightActivity() {
        return departureFlightAction;
    }

    @Override
    public String toString() {
        return "Flight{" +
                "id=" + id +
                '}';
    }
}
