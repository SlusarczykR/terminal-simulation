package com.slusarczykr.terminal.simulation.action;

import com.slusarczykr.terminal.simulation.action.queue.ActionQueue;
import com.slusarczykr.terminal.simulation.coordinator.SimulationCoordinator;
import com.slusarczykr.terminal.simulation.model.Passenger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

import static com.slusarczykr.terminal.simulation.action.ActionKey.CHECK_IN;
import static com.slusarczykr.terminal.simulation.action.ActionKey.GENERATE_PASSENGER;

public class GeneratePassengerAction extends AbstractAction<Passenger> {

    private static final Logger log = LogManager.getLogger(GeneratePassengerAction.class);

    public GeneratePassengerAction(SimulationCoordinator<Passenger> simulationCoordinator) {
        super(simulationCoordinator);
    }

    @Override
    protected Logger getLogger() {
        return log;
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
        log.debug("Starting generate passenger activity");
        SimulationCoordinator<Passenger> simulationCoordinator = (SimulationCoordinator<Passenger>) getParentSimObject();

        while (true) {
            Passenger passenger = generatePassenger(simulationCoordinator);
            log.debug("Passenger: '{}' generated", passenger.getUid());
            callNextAction(passenger);

            double delay = simulationGenerator.chisquare(8);

            if (await(delay)) {
                break;
            }
            setActionTime(delay);
        }
    }

    private Passenger generatePassenger(SimulationCoordinator<Passenger> simulationCoordinator) {
        log.debug("Generating new passenger");
        List<Integer> flightsIds = simulationCoordinator.getFlightsIds();
        int flightIdx = random.nextInt(flightsIds.size());

        return new Passenger(simTime(), flightsIds.get(flightIdx));
    }
}
