package com.slusarczykr.terminal.simulation;

import com.slusarczykr.terminal.simulation.coordinator.SimulationCoordinator;
import deskit.SimActivity;
import deskit.SimManager;
import deskit.monitors.Diagram;
import deskit.monitors.MonitoredVar;
import deskit.monitors.Statistics;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;

import static java.awt.Color.GREEN;
import static java.math.RoundingMode.HALF_UP;

public class Simulation {

    private static final Logger log = LogManager.getLogger(Simulation.class);

    public static void main(String[] args) {
        setLoggerLevel(args);
        double stopTime = getStopTime(args);
        SimulationCoordinator simulationCoordinator = new SimulationCoordinator();
        SimActivity.callActivity(simulationCoordinator, simulationCoordinator.getGeneratePassengerActivity());
        SimManager.getSimManager().setStopTime(stopTime);

        Instant start = Instant.now();
        log.info("Starting simulation with duration: {}ms...", stopTime);
        SimManager.getSimManager().startSimulation();
        log.info("Simulation duration: {}ms", Duration.between(start, Instant.now()).toMillis());

        generateStatistics(simulationCoordinator);
        generateServiceTimeHistogram(simulationCoordinator);
    }

    private static void setLoggerLevel(String[] args) {
        if (args.length > 1) {
            Logger logger = LogManager.getRootLogger();
            Level level = Level.getLevel(args[1].toUpperCase());
            Configurator.setAllLevels(logger.getName(), level);
        }
    }

    private static void generateStatistics(SimulationCoordinator simulationCoordinator) {
        MonitoredVar waitingTime = simulationCoordinator.getWaitingTime();
        MonitoredVar serviceTime = simulationCoordinator.getServiceTime();
        log.info("Total number of generated passengers: {}", waitingTime.getChanges().size());
        log.info("Total number of served passengers: {}", serviceTime.getChanges().size());
        generateAverageStatistics(simulationCoordinator);
    }

    private static void generateAverageStatistics(SimulationCoordinator simulationCoordinator) {
        double averageWaitingTime = calculateAverageTime(simulationCoordinator.getWaitingTime());
        double averageServiceTime = calculateAverageTime(simulationCoordinator.getServiceTime());
        log.info("Average passenger waiting time: {}ms, service time: {}ms", averageWaitingTime, averageServiceTime);
    }

    private static double calculateAverageTime(MonitoredVar monitoredVar) {
        double arithmeticMean = Statistics.arithmeticMean(monitoredVar);
        return BigDecimal.valueOf(arithmeticMean).setScale(2, HALF_UP).doubleValue();
    }

    private static double getStopTime(String[] args) {
        double stopTime = 2000;

        if (args.length == 1) {
            stopTime = Double.parseDouble(args[0]);
        }
        return stopTime;
    }

    private static void generateServiceTimeHistogram(SimulationCoordinator simulationCoordinator) {
        log.info("Generating service time histogram");
        Diagram serviceTimeHistogram = new Diagram("Histogram", "Service Time");
        serviceTimeHistogram.add(simulationCoordinator.getServiceTime(), GREEN);
        serviceTimeHistogram.show();
    }
}
