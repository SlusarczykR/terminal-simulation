package com.slusarczykr.terminal.simulation;

import com.slusarczykr.terminal.simulation.action.Action;
import com.slusarczykr.terminal.simulation.action.ActionKey;
import com.slusarczykr.terminal.simulation.action.CheckInPassengerAction;
import com.slusarczykr.terminal.simulation.action.GeneratePassengerAction;
import com.slusarczykr.terminal.simulation.action.SecurityCheckPassengerAction;
import com.slusarczykr.terminal.simulation.coordinator.SimulationCoordinator;
import com.slusarczykr.terminal.simulation.model.Passenger;
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
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.slusarczykr.terminal.simulation.action.ActionKey.CHECK_IN;
import static com.slusarczykr.terminal.simulation.action.ActionKey.GENERATE_PASSENGER;
import static com.slusarczykr.terminal.simulation.action.ActionKey.SECURITY_CHECK;
import static java.awt.Color.GREEN;
import static java.math.RoundingMode.HALF_UP;

public class Simulation {

    private static final Logger log = LogManager.getLogger(Simulation.class);

    public static void main(String[] args) {
        setLoggerLevel(args);
        double stopTime = getStopTime(args);

        SimulationCoordinator<Passenger> simulationCoordinator = new SimulationCoordinator<>(Simulation::initPassengerActions);
        SimActivity.callActivity(simulationCoordinator, (SimActivity) simulationCoordinator.getAction(GENERATE_PASSENGER));
        SimManager.getSimManager().setStopTime(stopTime);

        Instant start = Instant.now();
        log.info("Starting simulation with duration: {}ms...", stopTime);
        SimManager.getSimManager().startSimulation();
        log.info("Simulation duration: {}ms", Duration.between(start, Instant.now()).toMillis());
//        generateStatistics(simulationCoordinator);
//        generateServiceTimeHistogram(simulationCoordinator);
    }

    private static void setLoggerLevel(String[] args) {
        if (args.length > 1) {
            Logger logger = LogManager.getRootLogger();
            Level level = Level.getLevel(args[1].toUpperCase());
            Configurator.setAllLevels(logger.getName(), level);
        }
    }

    private static double getStopTime(String[] args) {
        double stopTime = 2000;

        if (args.length > 1) {
            stopTime = Double.parseDouble(args[0]);
        }
        return stopTime;
    }

    private static Map<ActionKey, Action<Passenger>> initPassengerActions(SimulationCoordinator<Passenger> simulationCoordinator) {
        return Stream.of(
                        new GeneratePassengerAction(simulationCoordinator),
                        new CheckInPassengerAction(simulationCoordinator),
                        new SecurityCheckPassengerAction(simulationCoordinator)
                )
                .collect(Collectors.toConcurrentMap(Action::getKey, Function.identity()));
    }

    private static void generateStatistics(SimulationCoordinator<Passenger> simulationCoordinator) {
        logProcessedPassengers(simulationCoordinator, GENERATE_PASSENGER);
        logProcessedPassengers(simulationCoordinator, CHECK_IN);
        logProcessedPassengers(simulationCoordinator, SECURITY_CHECK);

        generateAverageStatistics(simulationCoordinator);
    }

    private static void logProcessedPassengers(SimulationCoordinator<Passenger> simulationCoordinator, ActionKey actionKey) {
        MonitoredVar actionTime = simulationCoordinator.getActionTime(actionKey);
        log.info("Total number of passengers processed in: '{}' action - {}", actionKey, actionTime.getChanges().size());
    }

    private static void generateAverageStatistics(SimulationCoordinator<Passenger> simulationCoordinator) {
        double averageCheckInTime = calculateAverageTime(simulationCoordinator.getActionTime(CHECK_IN));
        double averageSecurityCheckTime = calculateAverageTime(simulationCoordinator.getActionTime(SECURITY_CHECK));
        log.info("Average passenger waiting time: {}ms, service time: {}ms", averageCheckInTime, averageSecurityCheckTime);
    }

    private static double calculateAverageTime(MonitoredVar monitoredVar) {
        double arithmeticMean = Statistics.arithmeticMean(monitoredVar);
        return BigDecimal.valueOf(arithmeticMean).setScale(2, HALF_UP).doubleValue();
    }

    private static void generateServiceTimeHistogram(SimulationCoordinator<Passenger> simulationCoordinator) {
        log.info("Generating service time histogram");
        Diagram serviceTimeHistogram = new Diagram("Histogram", "Service Time");
        serviceTimeHistogram.add(simulationCoordinator.getActionTime(CHECK_IN), GREEN);
        serviceTimeHistogram.show();
    }
}
