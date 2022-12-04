package com.slusarczykr.terminal.simulation;

import com.slusarczykr.terminal.simulation.coordinator.SimulationCoordinator;
import deskit.SimActivity;
import deskit.SimManager;
import deskit.monitors.Diagram;
import deskit.monitors.Statistics;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;

import static java.awt.Color.GREEN;
import static java.math.BigDecimal.ROUND_HALF_UP;

public class Simulation {

    private static final Logger log = LogManager.getLogger(Simulation.class);

    public static void main(String[] args) {
        SimulationCoordinator simulationCoordinator = new SimulationCoordinator();
        SimActivity.callActivity(simulationCoordinator, simulationCoordinator.getGeneratePassengerActivity());
        SimManager.getSimManager().setStopTime(20000);

        Instant start = Instant.now();
        SimManager.getSimManager().startSimulation();
        log.info("Simulation duration: {}", Duration.between(start, Instant.now()).toMillis());

        double result = BigDecimal.valueOf(Statistics.arithmeticMean(simulationCoordinator.getWaitingTime()))
                .setScale(2, ROUND_HALF_UP).doubleValue();
        log.info("Average passenger service time: {}", result);

        showDiagram(simulationCoordinator);
    }

    private static void showDiagram(SimulationCoordinator simulationCoordinator) {
        Diagram d1 = new Diagram("Histogram", "Service Time");
        d1.add(simulationCoordinator.getServiceTime(), GREEN);
        d1.show();
    }
}
