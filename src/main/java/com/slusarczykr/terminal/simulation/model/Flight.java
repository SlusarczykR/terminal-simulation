package com.slusarczykr.terminal.simulation.model;

import com.slusarczykr.terminal.simulation.action.Action;
import com.slusarczykr.terminal.simulation.action.DepartureFlightAction;
import com.slusarczykr.terminal.simulation.coordinator.SimulationCoordinator;
import deskit.SimObject;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Flight extends SimObject {

    private static int flightIdOffset = 0;

    private final int id;
    private final Queue<Passenger> passengers;
    private final Action<Passenger> departureFlightAction;

    public Flight(SimulationCoordinator<Passenger> simulationCoordinator) {
        this.id = flightIdOffset++;
        this.passengers = new ConcurrentLinkedQueue<>();
        this.departureFlightAction = callDepartureFlightAction(simulationCoordinator);
    }

    private Action<Passenger> callDepartureFlightAction(SimulationCoordinator<Passenger> simulationCoordinator) {
        Action<Passenger> action = new DepartureFlightAction(simulationCoordinator, this);
        action.call();

        return action;
    }

    public int getId() {
        return id;
    }

    public Action<Passenger> getDepartureFlightAction() {
        return departureFlightAction;
    }

    public void addPassenger(Passenger passenger) {
        this.passengers.add(passenger);
    }

    public Queue<Passenger> getPassengers() {
        return passengers;
    }

    @Override
    public String toString() {
        return "Flight{" +
                "id=" + id +
                '}';
    }
}
