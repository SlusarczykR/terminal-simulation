package com.slusarczykr.terminal.simulation.coordinator;

import com.slusarczykr.terminal.simulation.action.Action;
import com.slusarczykr.terminal.simulation.action.ActionKey;
import com.slusarczykr.terminal.simulation.model.Flight;
import com.slusarczykr.terminal.simulation.model.Passenger;
import deskit.SimActivity;
import deskit.SimObject;
import deskit.monitors.MonitoredVar;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static java.util.stream.IntStream.rangeClosed;

public class SimulationCoordinator<T> extends SimObject {

    private static final Logger log = LogManager.getLogger(SimulationCoordinator.class);

    public static final int DEFAULT_FLIGHTS_NUMBER = 10;

    private final Set<SimActivity> activities;
    private final Map<ActionKey, Action<T>> actions;
    private final MonitoredVar randomEventActionTime;
    private final Map<Integer, Flight> flights;
    private final Set<Flight> departedFlights;

    public SimulationCoordinator(Function<SimulationCoordinator<T>, Map<ActionKey, Action<T>>> actionsSupplier) {
        this.activities = ConcurrentHashMap.newKeySet();
        this.actions = actionsSupplier.apply(this);
        this.randomEventActionTime = new MonitoredVar(this);
        this.flights = generateFlights();
        this.departedFlights = ConcurrentHashMap.newKeySet();
    }

    public MonitoredVar getRandomEventActionTime() {
        return randomEventActionTime;
    }

    public void setRandomEventActionTime(double delay) {
        this.randomEventActionTime.setValue(delay);
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

    public void call(ActionKey actionKey) {
        getAction(actionKey).call();
    }

    public boolean anyFlightAvailable() {
        return !flights.isEmpty();
    }

    public List<Integer> getFlightsIds() {
        return new ArrayList<>(flights.keySet());
    }

    public void addMissedPassenger(Passenger passenger) {
        Optional<Flight> maybeFlight = getFlight(passenger.getFlightId(), true);
        maybeFlight.ifPresent(it -> it.addPassenger(passenger, true));
    }

    public Optional<Flight> getFlight(int id, boolean departed) {
        if (!departed) {
            return Optional.ofNullable(flights.get(id));
        }
        return departedFlights.stream()
                .filter(it -> it.getId() == id)
                .findFirst();
    }

    public void removeFlightIfPresent(int id) {
        Optional.ofNullable(flights.get(id)).ifPresent(it -> {
            flights.remove(it.getId());
            departedFlights.add(it);
            generateFlightIfSimulationIsRunning();
        });
    }

    private void generateFlightIfSimulationIsRunning() {
        if (isSimulationRunning()) {
            Flight flight = generateFlight();

            if (flight.isExecuted()) {
                flights.put(flight.getId(), flight);
            }
        }
    }

    public boolean isSimulationRunning() {
        return simManager.getSimTime() <= simManager.getStopTime()
                && simManager.getFirstSimObjectFromPendingList() != null;
    }

    public List<Flight> getDepartedFlights() {
        return new ArrayList<>(departedFlights);
    }

    public int getDepartedFlightsNumber() {
        return departedFlights.size();
    }

    public int getDepartedPassengersNumber() {
        return (int) departedFlights.stream()
                .map(Flight::getPassengers)
                .mapToLong(Set::size)
                .sum();
    }

    public int getMissedFlightPassengersNumber() {
        return (int) departedFlights.stream()
                .map(Flight::getMissedPassengers)
                .mapToLong(Set::size)
                .sum();
    }

    public void addActivity(SimActivity activity) {
        this.activities.add(activity);
    }

    public void stop() {
        activities
                .stream()
                .filter(Thread::isAlive)
                .forEach(it -> {
                    try {
                        it.resumeActivity();
                        it.terminate();
                    } catch (Exception e) {
                        log.error("Exception thrown during activity termination", e);
                    }
                });
    }

    public Action<T> getAction(ActionKey actionKey) {
        return actions.get(actionKey);
    }

    public MonitoredVar getActionTime(ActionKey actionKey) {
        Action<T> action = actions.get(actionKey);
        return action.getActionTime();
    }
}
