package com.slusarczykr.terminal.simulation.model;

import com.slusarczykr.terminal.simulation.action.DepartureFlightAction;
import com.slusarczykr.terminal.simulation.coordinator.TerminalSimulationCoordinator;
import deskit.SimManager;
import deskit.SimObject;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Flight extends SimObject {

    private static int flightIdOffset = 0;

    private final TerminalSimulationCoordinator simulationCoordinator;
    private final int id;
    private final Set<Passenger> passengers;
    private final Set<Passenger> missedPassengers;
    private final DepartureFlightAction action;
    private final boolean executed;

    public Flight(TerminalSimulationCoordinator simulationCoordinator) {
        this.simulationCoordinator = simulationCoordinator;
        this.id = flightIdOffset++;
        this.passengers = ConcurrentHashMap.newKeySet();
        this.missedPassengers = ConcurrentHashMap.newKeySet();
        this.action = new DepartureFlightAction(simulationCoordinator, this);
        this.executed = callDepartureFlightAction();
    }

    private boolean callDepartureFlightAction() {
        boolean shouldStartAction = shouldStartDepartureFlightAction(action);

        if (shouldStartAction) {
            action.call();
        }
        return shouldStartAction;
    }

    private boolean shouldStartDepartureFlightAction(DepartureFlightAction action) {
        SimManager simulationManager = simulationCoordinator.simManager;
        double timeLeft = simulationManager.getStopTime() - simulationManager.getSimTime();
        double flightDepartureTime = action.getFlightPreparationTime() + action.getDepartureTime();

        return timeLeft > flightDepartureTime;
    }

    public int getId() {
        return id;
    }

    public void addPassenger(Passenger passenger, boolean missed) {
        if (missed) {
            this.missedPassengers.add(passenger);
        } else {
            this.passengers.add(passenger);
        }
    }

    public Set<Passenger> getPassengers() {
        return passengers;
    }

    public Set<Passenger> getMissedPassengers() {
        return missedPassengers;
    }

    public DepartureFlightAction getAction() {
        return action;
    }

    public boolean isExecuted() {
        return executed;
    }

    @Override
    public String toString() {
        return "Flight{" +
                "id=" + id +
                '}';
    }
}
