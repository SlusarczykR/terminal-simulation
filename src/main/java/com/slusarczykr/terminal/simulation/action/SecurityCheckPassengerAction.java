package com.slusarczykr.terminal.simulation.action;

import com.slusarczykr.terminal.simulation.action.queue.ActionQueue;
import com.slusarczykr.terminal.simulation.action.queue.PassengerQueue;
import com.slusarczykr.terminal.simulation.coordinator.TerminalSimulationCoordinator;
import com.slusarczykr.terminal.simulation.model.Flight;
import com.slusarczykr.terminal.simulation.model.Passenger;
import deskit.monitors.MonitoredVar;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

import static com.slusarczykr.terminal.simulation.action.ActionKey.SECURITY_CHECK;

public class SecurityCheckPassengerAction extends AbstractAction<Passenger> {

    private static final Logger log = LogManager.getLogger(SecurityCheckPassengerAction.class);

    public SecurityCheckPassengerAction(TerminalSimulationCoordinator simulationCoordinator, int index) {
        super(simulationCoordinator, index);
    }

    @Override
    protected Logger getLogger() {
        return log;
    }

    @Override
    public ActionKey getKey() {
        return SECURITY_CHECK;
    }

    @Override
    public ActionQueue<Passenger> createActionQueue() {
        return new PassengerQueue(getKey(), new MonitoredVar(simulationCoordinator));
    }

    @Override
    public void action() {
        log.debug("['{}'] Starting security check passenger activity", getIndex());
        TerminalSimulationCoordinator simulationCoordinator = (TerminalSimulationCoordinator) getParentSimObject();
        ActionQueue<Passenger> actionQueue = getQueue();

        while (actionQueue.getLength() > 0) {
            actionQueue.block();
            Passenger passenger = actionQueue.poll();
            log.debug("['{}'] Performing passenger security check: '{}'", getIndex(), passenger.getUid());

            double delay = simulationGenerator.chisquare(7);
            setActionTime(delay);

            if (await(delay)) {
                break;
            }
            actionQueue.release();
            log.debug("['{}'] Passenger: '{}' security check procedure finished", getIndex(), passenger.getUid());
            addPassengerToFlightIfAvailable(simulationCoordinator, passenger);
        }
    }

    private void addPassengerToFlightIfAvailable(TerminalSimulationCoordinator simulationCoordinator, Passenger passenger) {
        log.debug("['{}'] Searching for flight with id: {}", getIndex(), passenger.getFlightId());
        Optional<Flight> maybeFlight = simulationCoordinator.getFlight(passenger.getFlightId(), false);

        if (maybeFlight.isPresent()) {
            Flight flight = maybeFlight.get();
            log.debug("['{}'] Adding passenger: '{}' to flight: '{}'", getIndex(), passenger.getUid(), flight.getId());
            flight.addPassenger(passenger, false);
        } else {
            log.debug("['{}'] Passenger: '{}' missed the flight: '{}'", getIndex(), passenger.getUid(), passenger.getFlightId());
            simulationCoordinator.addMissedPassenger(passenger);
        }
    }
}
