package com.slusarczykr.terminal.simulation.action;

import com.slusarczykr.terminal.simulation.coordinator.SimulationCoordinator;
import com.slusarczykr.terminal.simulation.model.Passenger;
import deskit.SimActivity;
import deskit.random.SimGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GeneratePassengerActivity extends SimActivity {

    private static final Logger log = LogManager.getLogger(SimulationCoordinator.class);

    private final SimGenerator generator;

    public GeneratePassengerActivity() {
        this.generator = new SimGenerator();
    }

    public void action() {
        SimulationCoordinator simulationCoordinator = (SimulationCoordinator) getParentSimObject();

        while (true) {
            generatePassenger(simulationCoordinator);
            servicePassengerIfQueueIsFree(simulationCoordinator);

            double delay = generator.chisquare(8);

            if (await(delay)) {
                break;
            }
            simulationCoordinator.setWaitingTime(delay);
        }
    }

    private void generatePassenger(SimulationCoordinator simulationCoordinator) {
        simulationCoordinator.addPassenger(new Passenger(simTime()));
    }

    private boolean await(double delay) {
        waitDuration(delay);
        return isStopped() || isInterrupted();
    }

    private void servicePassengerIfQueueIsFree(SimulationCoordinator simulationCoordinator) {
        if (simulationCoordinator.getQueueLength() == 1 && !simulationCoordinator.isOccupied()) {
            log.info("Starting service passenger activity...");
            callActivity(simulationCoordinator, simulationCoordinator.getServicePassengerActivity());
        } else {
            log.info("Service passenger activity could not be started. Queue length: {}, is occupied: {}",
                    simulationCoordinator.getQueueLength(), simulationCoordinator.isOccupied());
        }
    }
}
