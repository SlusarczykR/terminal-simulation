package com.slusarczykr.terminal.simulation.action;

import com.slusarczykr.terminal.simulation.coordinator.SimulationCoordinator;
import com.slusarczykr.terminal.simulation.model.Flight;
import com.slusarczykr.terminal.simulation.model.Passenger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.slusarczykr.terminal.simulation.action.ActionKey.DEPARTURE_FLIGHT;

public class DepartureFlightAction extends AbstractAction<Passenger> {

    private static final Logger log = LogManager.getLogger(DepartureFlightAction.class);

    private final Flight flight;
    private final double flightPreparationTime;
    private final double departureTime;

    public DepartureFlightAction(SimulationCoordinator<Passenger> simulationCoordinator, Flight flight) {
        super(simulationCoordinator);
        this.flight = flight;
        this.flightPreparationTime = simulationGenerator.chisquare(20000);
        this.departureTime = simulationGenerator.chisquare(10000);
    }

    @Override
    protected Logger getLogger() {
        return log;
    }

    @Override
    public ActionKey getKey() {
        return DEPARTURE_FLIGHT;
    }

    @Override
    public void action() {
        log.info("Flight will be ready for passengers boarding after {}ms", format(flightPreparationTime));
        await(flightPreparationTime);

        log.info("Flight '{}' will depart for {}ms", flight.getId(), format(departureTime));
        await(departureTime);

        log.info("Flight: '{}' departed from the airport with {} passengers on the board",
                flight.getId(), flight.getPassengers().size());
        simulationCoordinator.removeFlightIfPresent(flight.getId());
    }
}
