package com.slusarczykr.terminal.simulation.action;

import com.slusarczykr.terminal.simulation.coordinator.SimulationCoordinator;
import com.slusarczykr.terminal.simulation.model.Passenger;
import deskit.SimActivity;
import deskit.random.SimGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.slusarczykr.terminal.simulation.coordinator.PassengerQueue.QueueType.CHECK_IN;
import static com.slusarczykr.terminal.simulation.coordinator.PassengerQueue.QueueType.SECURITY_CHECK;

public class CheckInPassengerActivity extends SimActivity {

    private static final Logger log = LogManager.getLogger(CheckInPassengerActivity.class);

    private final SimGenerator generator;

    public CheckInPassengerActivity() {
        this.generator = new SimGenerator();
    }

    @Override
    public void action() {
        log.debug("Starting check in passenger activity...");
        SimulationCoordinator simulationCoordinator = (SimulationCoordinator) getParentSimObject();

        while (simulationCoordinator.getQueueLength(CHECK_IN) > 0) {
            simulationCoordinator.blockQueue(CHECK_IN);
            Passenger passenger = simulationCoordinator.nextPassenger(CHECK_IN);
            log.debug("Checking in passenger: '{}'", passenger.getUid());

            double delay = generator.chisquare(7);
            simulationCoordinator.setServiceTime(delay);
            simulationCoordinator.setWaitingTime(simTime() - passenger.getGenerationTime());

            if (await(delay)) {
                break;
            }
            simulationCoordinator.freeQueue(CHECK_IN);
            log.debug("Passenger: '{}' check in procedure finished", passenger.getUid());
            callActivity(simulationCoordinator, simulationCoordinator.getSecurityCheckPassengerActivity());
            simulationCoordinator.addPassenger(SECURITY_CHECK, passenger);
        }
    }

    private boolean await(double delay) {
        waitDuration(delay);
        return isStopped() || isInterrupted();
    }
}
