package com.slusarczykr.terminal.simulation.action;

import com.slusarczykr.terminal.simulation.action.queue.ActionQueue;
import com.slusarczykr.terminal.simulation.action.queue.PassengerQueue;
import com.slusarczykr.terminal.simulation.coordinator.SimulationCoordinator;
import com.slusarczykr.terminal.simulation.model.Flight;
import com.slusarczykr.terminal.simulation.model.Passenger;
import deskit.monitors.MonitoredVar;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

import static com.slusarczykr.terminal.simulation.action.ActionKey.SECURITY_CHECK;

public class SecurityCheckPassengerAction extends AbstractAction<Passenger> {

    private static final Logger log = LogManager.getLogger(SecurityCheckPassengerAction.class);

    public SecurityCheckPassengerAction(SimulationCoordinator<Passenger> simulationCoordinator) {
        super(simulationCoordinator);
    }

    @Override
    public ActionKey getKey() {
        return SECURITY_CHECK;
    }

    @Override
    public ActionQueue<Passenger> getQueue() {
        return new PassengerQueue(getKey(), new MonitoredVar(simulationCoordinator));
    }

    @Override
    public void action() {
        log.debug("Starting security check passenger activity...");
        SimulationCoordinator<Passenger> simulationCoordinator = (SimulationCoordinator<Passenger>) getParentSimObject();

        while (simulationCoordinator.getQueueLength(SECURITY_CHECK) > 0) {
            ActionQueue<Passenger> actionQueue = getQueue();
            actionQueue.block();
            Passenger passenger = actionQueue.poll();
            log.debug("Performing passenger security check: '{}'", passenger.getUid());

            double delay = simulationGenerator.chisquare(7);
            setActionTime(delay);

            if (await(delay)) {
                break;
            }
            Optional<Flight> maybeFlight = simulationCoordinator.getFlight(passenger.getFlightId());
            maybeFlight.ifPresent(it -> {
                log.debug("Adding passenger: '{}' to flight: {}", passenger.getUid(), it.getId());
                it.addPassenger(passenger);
            });
            simulationCoordinator.freeQueue(SECURITY_CHECK);
            log.debug("Passenger: '{}' security check procedure finished", passenger.getUid());
        }
    }
}