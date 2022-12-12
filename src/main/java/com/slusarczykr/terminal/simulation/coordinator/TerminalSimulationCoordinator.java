package com.slusarczykr.terminal.simulation.coordinator;

import com.slusarczykr.terminal.simulation.action.Action;
import com.slusarczykr.terminal.simulation.action.ActionKey;
import com.slusarczykr.terminal.simulation.action.CheckInPassengerAction;
import com.slusarczykr.terminal.simulation.action.GeneratePassengerAction;
import com.slusarczykr.terminal.simulation.action.SecurityCheckPassengerAction;
import com.slusarczykr.terminal.simulation.config.SimulationConfiguration;
import com.slusarczykr.terminal.simulation.model.Flight;
import com.slusarczykr.terminal.simulation.model.Passenger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

import static com.slusarczykr.terminal.simulation.action.ActionKey.CHECK_IN;
import static com.slusarczykr.terminal.simulation.action.ActionKey.GENERATE_PASSENGER;
import static com.slusarczykr.terminal.simulation.action.ActionKey.SECURITY_CHECK;
import static java.util.stream.IntStream.rangeClosed;

public class TerminalSimulationCoordinator extends SimulationCoordinator<Passenger> {

    private static final Logger log = LogManager.getLogger(TerminalSimulationCoordinator.class);

    private final Map<Integer, Flight> flights;
    private final Set<Flight> departedFlights;

    public TerminalSimulationCoordinator(SimulationConfiguration simulationConfig) {
        super();
        this.actions.putAll(createSimulationActions(simulationConfig));
        this.flights = generateFlights();
        this.departedFlights = ConcurrentHashMap.newKeySet();
    }

    private Map<ActionKey, List<Action<Passenger>>> createSimulationActions(SimulationConfiguration simulationConfig) {
        Map<ActionKey, List<Action<Passenger>>> actionKeyToActionInstances = new ConcurrentHashMap<>();
        actionKeyToActionInstances.put(
                GENERATE_PASSENGER,
                createActionInstances(
                        simulationConfig.getActionInstances(GENERATE_PASSENGER),
                        idx -> new GeneratePassengerAction(this, idx)
                ));
        actionKeyToActionInstances.put(
                CHECK_IN,
                createActionInstances(
                        simulationConfig.getActionInstances(CHECK_IN),
                        idx -> new CheckInPassengerAction(this, idx)
                ));
        actionKeyToActionInstances.put(
                SECURITY_CHECK,
                createActionInstances(
                        simulationConfig.getActionInstances(SECURITY_CHECK),
                        idx -> new SecurityCheckPassengerAction(this, idx)
                ));

        return actionKeyToActionInstances;
    }

    private List<Action<Passenger>> createActionInstances(int instancesNumber, IntFunction<Action<Passenger>> actionCreator) {
        if (instancesNumber > 0 && actionCreator != null) {
            return rangeClosed(1, instancesNumber)
                    .mapToObj(actionCreator)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
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
        Flight flight = new Flight(this);
        log.debug("Generated flight with id: '{}'", flight.getId());

        return flight;
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
}