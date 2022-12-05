package com.slusarczykr.terminal.simulation.action;

import com.slusarczykr.terminal.simulation.coordinator.SimulationCoordinator;
import com.slusarczykr.terminal.simulation.model.Passenger;
import deskit.SimActivity;
import deskit.random.SimGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.slusarczykr.terminal.simulation.coordinator.PassengerQueue.QueueType.SECURITY_CHECK;

public class SecurityCheckPassengerActivity extends SimActivity {

    private static final Logger log = LogManager.getLogger(SecurityCheckPassengerActivity.class);

    private final SimGenerator generator;

    public SecurityCheckPassengerActivity() {
        this.generator = new SimGenerator();
    }

    @Override
    public void action() {
        log.debug("Starting security check passenger activity...");
        SimulationCoordinator simulationCoordinator = (SimulationCoordinator) getParentSimObject();

        while (simulationCoordinator.getQueueLength(SECURITY_CHECK) > 0) {
            simulationCoordinator.blockQueue(SECURITY_CHECK);
            Passenger passenger = simulationCoordinator.nextPassenger(SECURITY_CHECK);
            log.debug("Performing passenger security check: '{}'", passenger.getUid());

            double delay = generator.chisquare(7);
            simulationCoordinator.setServiceTime(delay);
            simulationCoordinator.setWaitingTime(simTime() - passenger.getGenerationTime());

            if (await(delay)) {
                break;
            }
            simulationCoordinator.getFlight(passenger.getFlightId())
                    .ifPresent(it -> it.addPassenger(passenger));
            simulationCoordinator.freeQueue(SECURITY_CHECK);
            log.debug("Passenger: '{}' security check procedure finished", passenger.getUid());
        }
    }

    private boolean await(double delay) {
        waitDuration(delay);
        return isStopped() || isInterrupted();
    }
}
