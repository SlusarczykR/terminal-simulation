package com.slusarczykr.terminal.simulation.coordinator;

import com.slusarczykr.terminal.simulation.action.Action;
import com.slusarczykr.terminal.simulation.action.ActionKey;
import com.slusarczykr.terminal.simulation.action.DepartureFlightAction;
import com.slusarczykr.terminal.simulation.action.queue.ActionQueue;
import com.slusarczykr.terminal.simulation.model.Flight;
import com.slusarczykr.terminal.simulation.model.Passenger;
import deskit.SimActivity;
import deskit.SimObject;
import deskit.monitors.MonitoredVar;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static java.util.stream.IntStream.rangeClosed;

public class SimulationCoordinator<T> extends SimObject {

    private static final Logger log = LogManager.getLogger(SimulationCoordinator.class);

    public static final int DEFAULT_FLIGHTS_NUMBER = 10;

    private final Map<ActionKey, Action<T>> actions;
    private final Map<Integer, Flight> flights;

    public SimulationCoordinator(Function<SimulationCoordinator<T>, Map<ActionKey, Action<T>>> actionsSupplier) {
        this.actions = actionsSupplier.apply(this);
        this.flights = generateFlights();
    }

    private Map<Integer, Flight> generateFlights() {
        Map<Integer, Flight> generatedFlights = new ConcurrentHashMap<>();
        rangeClosed(1, DEFAULT_FLIGHTS_NUMBER).forEach(idx -> {
            Flight flight = new Flight(idx, new DepartureFlightAction((SimulationCoordinator<Passenger>) this));
            generatedFlights.put(idx, flight);
            log.debug("Starting '{}' flight departure activity...", flight.getId());
            SimActivity.callActivity(flight, flight.getDepartureFlightActivity());
        });

        return generatedFlights;
    }

    public Optional<Flight> getFlight(int id) {
        return Optional.ofNullable(flights.get(id));
    }

    public int getFlightsSize() {
        return flights.size();
    }

    public void addPassenger(ActionKey activity, T passenger) {
        log.trace("Add passenger to queue: {}", passenger);
        ActionQueue<T> actionQueue = actions.get(activity).getQueue();
        actionQueue.add(passenger);
    }

    public T nextPassenger(ActionKey activity) {
        ActionQueue<T> actionQueue = actions.get(activity).getQueue();
        log.trace("Poll passenger from the queue with size: {}", actionQueue.getLength());

        return actionQueue.poll();
    }

    public void callNextAction(ActionKey actionKey) {
        Action<T> action = actions.get(actionKey);
        action.callNextAction();
    }

    public Action<T> getAction(ActionKey actionKey) {
        return actions.get(actionKey);
    }

    public void blockQueue(ActionKey activity) {
        ActionQueue<T> actionQueue = actions.get(activity).getQueue();
        log.trace("Blocking '{}' queue...", activity);
        actionQueue.block();
    }

    public void freeQueue(ActionKey activity) {
        ActionQueue<T> actionQueue = actions.get(activity).getQueue();
        log.trace("Releasing '{}' queue...", activity);
        actionQueue.release();
    }

    public boolean isOccupied(ActionKey activity) {
        ActionQueue<T> actionQueue = actions.get(activity).getQueue();
        return actionQueue.isOccupied();
    }

    public MonitoredVar getActionTime(ActionKey actionKey) {
        Action<T> action = actions.get(actionKey);
        return action.getActionTime();
    }

    public void setActionTime(ActionKey actionKey, double delay) {
        log.trace("Set waiting time: {}", delay);
        Action<T> action = actions.get(actionKey);
        action.setActionTime(delay);
    }

    public int getQueueLength(ActionKey activity) {
        ActionQueue<T> actionQueue = actions.get(activity).getQueue();
        return actionQueue.getLength();
    }
}
