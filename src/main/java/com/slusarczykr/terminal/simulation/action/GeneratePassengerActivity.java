package com.slusarczykr.terminal.simulation.action;

import com.slusarczykr.terminal.simulation.coordinator.SimulationCoordinator;
import com.slusarczykr.terminal.simulation.model.Passenger;
import deskit.SimActivity;
import deskit.random.SimGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;

import static com.slusarczykr.terminal.simulation.coordinator.PassengerQueue.QueueType.CHECK_IN;

public class GeneratePassengerActivity extends SimActivity {

    private static final Logger log = LogManager.getLogger(GeneratePassengerActivity.class);

    private final SimGenerator generator;

    private final Random random;

    public GeneratePassengerActivity() {
        this.generator = new SimGenerator();
        this.random = new Random();
    }

    @Override
    public void action() {
        log.debug("Starting generate passenger activity...");
        SimulationCoordinator simulationCoordinator = (SimulationCoordinator) getParentSimObject();

        while (true) {
            generatePassenger(simulationCoordinator);
            checkInPassengerIfQueueNotOccupied(simulationCoordinator);

            double delay = generator.chisquare(8);

            if (await(delay)) {
                break;
            }
            simulationCoordinator.setWaitingTime(delay);
        }
    }

    private void generatePassenger(SimulationCoordinator simulationCoordinator) {
        log.debug("Generating new passenger...");
        int flightNumber = random.nextInt() * (simulationCoordinator.getFlightsSize() + 1);
        Passenger passenger = new Passenger(simTime(), flightNumber);
        log.debug("Passenger: '{}' generated", passenger.getUid());
        simulationCoordinator.addPassenger(CHECK_IN, passenger);
    }

    private boolean await(double delay) {
        waitDuration(delay);
        return isStopped() || isInterrupted();
    }

    private void checkInPassengerIfQueueNotOccupied(SimulationCoordinator simulationCoordinator) {
        if (simulationCoordinator.getQueueLength(CHECK_IN) == 1 && !simulationCoordinator.isOccupied(CHECK_IN)) {
            callActivity(simulationCoordinator, simulationCoordinator.getCheckInPassengerActivity());
        } else {
            log.debug("Service passenger activity could not be started - queue length: {}", simulationCoordinator.getQueueLength(CHECK_IN));
        }
    }
}
