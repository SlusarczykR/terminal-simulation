package com.slusarczykr.terminal.simulation.action;

import com.slusarczykr.terminal.simulation.coordinator.SimulationCoordinator;
import com.slusarczykr.terminal.simulation.model.Passenger;
import deskit.SimActivity;
import deskit.random.SimGenerator;

public class GeneratePassengerActivity extends SimActivity {

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
            callActivity(simulationCoordinator, simulationCoordinator.getServicePassengerActivity());
        }
    }
}
