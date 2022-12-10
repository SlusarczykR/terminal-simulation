package com.slusarczykr.terminal.simulation.coordinator;

import com.slusarczykr.terminal.simulation.action.Action;
import com.slusarczykr.terminal.simulation.action.ActionKey;
import com.slusarczykr.terminal.simulation.model.Flight;
import com.slusarczykr.terminal.simulation.model.Passenger;
import deskit.SimObject;
import deskit.monitors.MonitoredVar;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static java.util.stream.IntStream.rangeClosed;

public class SimulationCoordinator<T> extends SimObject {

    private static final Logger log = LogManager.getLogger(SimulationCoordinator.class);

    public static final int DEFAULT_FLIGHTS_NUMBER = 10;

    private final Map<ActionKey, Action<T>> actions;
    private final Map<Integer, Flight> flights;

    private final AtomicInteger departedFlightsNumber;

    public SimulationCoordinator(Function<SimulationCoordinator<T>, Map<ActionKey, Action<T>>> actionsSupplier) {
        this.actions = actionsSupplier.apply(this);
        this.flights = generateFlights();
        this.departedFlightsNumber = new AtomicInteger(0);
    }

    private Map<Integer, Flight> generateFlights() {
        Map<Integer, Flight> generatedFlights = new ConcurrentHashMap<>();
        rangeClosed(1, DEFAULT_FLIGHTS_NUMBER).forEach(idx -> {
            Flight flight = generateFlight();
            generatedFlights.put(flight.getId(), flight);
        });

        return generatedFlights;
    }

    private Flight generateFlight() {
        Flight flight = new Flight((SimulationCoordinator<Passenger>) this);
        log.debug("Generated flight with id: '{}'", flight.getId());

        return flight;
    }

    public List<Integer> getFlightsIds() {
        return new ArrayList<>(flights.keySet());
    }

    public Optional<Flight> getFlight(int id) {
        return Optional.ofNullable(flights.get(id));
    }

    public void removeFlightIfPresent(int id) {
        if (flights.containsKey(id)) {
            removeFlight(id);

            Flight flight = generateFlight();
            flights.put(flight.getId(), flight);
        }
    }

    private void removeFlight(int id) {
        flights.remove(id);
        departedFlightsNumber.incrementAndGet();
        log.info("Flight: '{}' removed", id);
    }

    public int getDepartedFlightsNumber() {
        return departedFlightsNumber.get();
    }

    public Action<T> getAction(ActionKey actionKey) {
        return actions.get(actionKey);
    }

    public MonitoredVar getActionTime(ActionKey actionKey) {
        Action<T> action = actions.get(actionKey);
        return action.getActionTime();
    }
}
