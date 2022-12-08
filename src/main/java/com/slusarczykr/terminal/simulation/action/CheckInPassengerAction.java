package com.slusarczykr.terminal.simulation.action;

import com.slusarczykr.terminal.simulation.action.queue.ActionQueue;
import com.slusarczykr.terminal.simulation.action.queue.PassengerQueue;
import com.slusarczykr.terminal.simulation.coordinator.SimulationCoordinator;
import com.slusarczykr.terminal.simulation.model.Passenger;
import deskit.monitors.MonitoredVar;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.slusarczykr.terminal.simulation.action.ActionKey.CHECK_IN;
import static com.slusarczykr.terminal.simulation.action.ActionKey.SECURITY_CHECK;

public class CheckInPassengerAction extends AbstractAction<Passenger> {

    private static final Logger log = LogManager.getLogger(CheckInPassengerAction.class);

    public CheckInPassengerAction(SimulationCoordinator<Passenger> simulationCoordinator) {
        super(simulationCoordinator);
    }

    @Override
    public ActionKey getKey() {
        return CHECK_IN;
    }

    @Override
    public ActionQueue<Passenger> createActionQueue() {
        return new PassengerQueue(getKey(), new MonitoredVar(simulationCoordinator));
    }

    @Override
    public ActionKey getNextActionKey() {
        return SECURITY_CHECK;
    }

    @Override
    public void action() {
        log.debug("Starting check in passenger activity...");
        ActionQueue<Passenger> actionQueue = getQueue();

        while (actionQueue.getLength() > 0) {
            actionQueue.block();
            Passenger passenger = actionQueue.poll();
            log.debug("Checking in passenger: '{}'", passenger.getUid());

            double delay = simulationGenerator.chisquare(7);
            setActionTime(delay);

            if (await(delay)) {
                break;
            }
            actionQueue.release();
            log.debug("Passenger: '{}' check in procedure finished", passenger.getUid());
            callNextAction(passenger);
        }
    }
}
