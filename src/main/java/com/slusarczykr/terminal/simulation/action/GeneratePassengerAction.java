package com.slusarczykr.terminal.simulation.action;

import com.slusarczykr.terminal.simulation.coordinator.TerminalSimulationCoordinator;
import com.slusarczykr.terminal.simulation.model.Passenger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

import static com.slusarczykr.terminal.simulation.action.ActionKey.CHECK_IN;
import static com.slusarczykr.terminal.simulation.action.ActionKey.GENERATE_PASSENGER;

public class GeneratePassengerAction extends AbstractAction<Passenger> {

    private static final Logger log = LogManager.getLogger(GeneratePassengerAction.class);

    public GeneratePassengerAction(TerminalSimulationCoordinator simulationCoordinator, int index) {
        super(simulationCoordinator, index);
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
    public ActionKey getNextActionKey() {
        return CHECK_IN;
    }

    @Override
    public void action() {
        log.debug("['{}'] Starting generate passenger activity", getIndex());
        TerminalSimulationCoordinator simCoordinator = (TerminalSimulationCoordinator) simulationCoordinator;

        while (simCoordinator.anyFlightAvailable()) {
            Passenger passenger = generatePassenger(simCoordinator);
            log.debug("['{}'] Passenger: '{}' generated", getIndex(), passenger.getUid());
            callNextAction(passenger);

            double delay = simulationGenerator.chisquare(8);

            if (await(delay)) {
                break;
            }
            setActionTime(delay);
        }
    }

    private Passenger generatePassenger(TerminalSimulationCoordinator simulationCoordinator) {
        log.debug("['{}'] Generating new passenger", getIndex());
        List<Integer> flightsIds = simulationCoordinator.getFlightsIds();
        int flightIdx = random.nextInt(flightsIds.size());

        return new Passenger(simTime(), flightsIds.get(flightIdx));
    }
}
