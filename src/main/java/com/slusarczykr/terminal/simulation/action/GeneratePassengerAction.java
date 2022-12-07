package com.slusarczykr.terminal.simulation.action;

import com.slusarczykr.terminal.simulation.action.queue.ActionQueue;
import com.slusarczykr.terminal.simulation.coordinator.SimulationCoordinator;
import com.slusarczykr.terminal.simulation.model.Passenger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.slusarczykr.terminal.simulation.action.ActionKey.CHECK_IN;
import static com.slusarczykr.terminal.simulation.action.ActionKey.GENERATE_PASSENGER;

public class GeneratePassengerAction extends AbstractAction<Passenger> {

    private static final Logger log = LogManager.getLogger(GeneratePassengerAction.class);

    public GeneratePassengerAction(SimulationCoordinator<Passenger> simulationCoordinator) {
        super(simulationCoordinator);
    }

    @Override
    public void callNextAction() {
        ActionQueue<Passenger> actionQueue = getQueue();

        if (actionQueue.getLength() == 1 && !actionQueue.isOccupied()) {
            super.callNextAction();
        } else {
            log.debug("Service passenger activity could not be started - actionQueue length: {}", actionQueue.getLength());
        }
    }

    @Override
    public ActionKey getKey() {
        return GENERATE_PASSENGER;
    }

    @Override
    public ActionQueue<Passenger> getQueue() {
        return null;
    }

    @Override
    public ActionKey getNextActionKey() {
        return CHECK_IN;
    }

    @Override
    public void action() {
        log.debug("Starting generate passenger activity...");
        SimulationCoordinator<Passenger> simulationCoordinator = (SimulationCoordinator<Passenger>) getParentSimObject();

        while (true) {
            generatePassenger(simulationCoordinator);
            simulationCoordinator.callNextAction(GENERATE_PASSENGER);

            double delay = simulationGenerator.chisquare(8);

            if (await(delay)) {
                break;
            }
            simulationCoordinator.setActionTime(CHECK_IN, delay);
        }
    }

    private void generatePassenger(SimulationCoordinator<Passenger> simulationCoordinator) {
        log.debug("Generating new passenger...");
        int flightNumber = random.nextInt() * (simulationCoordinator.getFlightsSize() + 1);
        Passenger passenger = new Passenger(simTime(), flightNumber);
        log.debug("Passenger: '{}' generated", passenger.getUid());
        simulationCoordinator.addPassenger(CHECK_IN, passenger);
    }
}
