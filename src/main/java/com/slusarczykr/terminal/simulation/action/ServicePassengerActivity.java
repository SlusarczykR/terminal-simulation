package com.slusarczykr.terminal.simulation.action;

import com.slusarczykr.terminal.simulation.coordinator.SimulationCoordinator;
import com.slusarczykr.terminal.simulation.model.Passenger;
import deskit.SimActivity;
import deskit.random.SimGenerator;

public class ServicePassengerActivity extends SimActivity {

    private final SimGenerator generator;

    public ServicePassengerActivity() {
        this.generator = new SimGenerator();
    }

    public void action() {
        SimulationCoordinator simulationCoordinator = (SimulationCoordinator) getParentSimObject();

        while (simulationCoordinator.getQueueLength() > 0) {
            simulationCoordinator.blockQueue();
            Passenger passenger = simulationCoordinator.nextPassenger();

            double delay = generator.chisquare(7);
            simulationCoordinator.setServiceTime(delay);
            simulationCoordinator.setWaitingTime(simTime() - passenger.getGenerationTime());

            if (await(delay)) {
                break;
            }
            simulationCoordinator.freeQueue();
        }
    }

    private boolean await(double delay) {
        waitDuration(delay);
        return isStopped() || isInterrupted();
    }
}
