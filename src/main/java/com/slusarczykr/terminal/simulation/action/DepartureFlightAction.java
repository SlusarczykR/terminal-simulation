package com.slusarczykr.terminal.simulation.action;

import com.slusarczykr.terminal.simulation.coordinator.SimulationCoordinator;
import com.slusarczykr.terminal.simulation.model.Flight;
import com.slusarczykr.terminal.simulation.model.Passenger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.slusarczykr.terminal.simulation.action.ActionKey.DEPARTURE_FLIGHT;

public class DepartureFlightAction extends AbstractAction<Passenger> {

    private static final Logger log = LogManager.getLogger(DepartureFlightAction.class);

    public DepartureFlightAction(SimulationCoordinator<Passenger> simulationCoordinator) {
        super(simulationCoordinator);
    }

    @Override
    public ActionKey getKey() {
        return DEPARTURE_FLIGHT;
    }

    @Override
    public void action() {
        log.debug("Starting departure flight activity...");
        Flight flight = (Flight) getParentSimObject();

        log.debug("Departing flight: '{}'", flight.getId());
        double delay = flight.getDepartureTime();
        waitDuration(delay);

        log.debug("Flight: '{}' departed from the airport", flight.getId());
    }
}
