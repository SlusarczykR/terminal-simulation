package com.slusarczykr.terminal.simulation;

import com.slusarczykr.terminal.simulation.action.ActionKey;
import com.slusarczykr.terminal.simulation.config.SimulationConfiguration;
import com.slusarczykr.terminal.simulation.coordinator.TerminalSimulationCoordinator;
import deskit.SimManager;
import deskit.monitors.Diagram;
import deskit.monitors.MonitoredVar;
import deskit.monitors.Statistics;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.function.Consumer;

import static com.slusarczykr.terminal.simulation.action.ActionKey.CHECK_IN;
import static com.slusarczykr.terminal.simulation.action.ActionKey.GENERATE_PASSENGER;
import static com.slusarczykr.terminal.simulation.action.ActionKey.SECURITY_CHECK;
import static java.awt.Color.GREEN;
import static java.math.RoundingMode.HALF_UP;

public class TerminalSimulation {

    private static final Logger log = LogManager.getLogger(TerminalSimulation.class);

    private static final List<String> AVAILABLE_LOGGER_LEVELS = Arrays.asList("INFO", "DEBUG", "TRACE");
    private static final String DEFAULT_LOGGER_LEVEL = "INFO";

    private static final String UNSUPPORTED_OPERATION_EXCEPTION = "Unsupported operation type";

    public static void main(String[] args) {
        disableSystemLogging();
        setLoggerLevel("DEBUG");
        SimulationConfiguration simulationConfig = new SimulationConfiguration();

        while (true) {
            log.info("\nq - Exit\n1 - Run simulation\n2 - Configure simulation\n3 - Change log level\n\n");
            String command = readUserInput("Command:");
            log.info("\n");

            if (command.equalsIgnoreCase("q")) {
                break;
            } else if (command.equals("1")) {
                displaySimulationMenu(simulationConfig);
            } else if (command.equals("2")) {
                configureSimulation(simulationConfig);
            } else if (command.equals("3")) {
                setLoggerLevel(readUserInput(String.format("Logger level %s:", Arrays.toString(AVAILABLE_LOGGER_LEVELS.toArray()))));
            } else {
                log.warn(UNSUPPORTED_OPERATION_EXCEPTION);
            }
            log.info("\n");
        }
    }

    private static void disableSystemLogging() {
        PrintStream dummyWriter = new PrintStream(new OutputStream() {
            @Override
            public void write(int arg0) throws IOException {
            }
        });
        System.setOut(dummyWriter);
        System.setErr(dummyWriter);
    }

    private static void configureSimulation(SimulationConfiguration simulationConfig) {
        getUserInputAndExecute("Simulation duration in seconds:", it -> simulationConfig.setSimulationDuration(Double.parseDouble(it)));
        getUserInputAndExecute("Maximum number of simultaneous flights:", it -> simulationConfig.setMaxFlightsNumber(Double.parseDouble(it)));
        getUserInputAndExecute("Random event probability:", it -> simulationConfig.setRandomEventProbability(Double.parseDouble(it)));
    }

    private static void getUserInputAndExecute(String label, Consumer<String> setConfig) {
        String input = readUserInput(label);
        setConfig.accept(input);
        log.info("\n");
    }

    private static void displaySimulationMenu(SimulationConfiguration simulationConfig) {
        TerminalSimulationCoordinator simulationCoordinator = runSimulation(simulationConfig);

        while (true) {
            log.info("\nq - Exit\n1 - Rerun simulation\n2 - Display simulation statistics\n3 - Configure simulation\n4 - Change log level\n\n");
            String command = readUserInput("Command:");
            log.info("\n");

            if (command.equalsIgnoreCase("q")) {
                break;
            } else if (command.equals("1")) {
                simulationCoordinator = runSimulation(simulationConfig);
            } else if (command.equals("2")) {
                displaySimulationStatistics(simulationCoordinator);
            } else if (command.equals("3")) {
                configureSimulation(simulationConfig);
            } else if (command.equals("4")) {
                setLoggerLevel(readUserInput(String.format("Logger level %s:", Arrays.toString(AVAILABLE_LOGGER_LEVELS.toArray()))));
            } else {
                log.warn(UNSUPPORTED_OPERATION_EXCEPTION);
            }
            log.info("\n");
        }
    }

    private static void displaySimulationStatistics(TerminalSimulationCoordinator simulationCoordinator) {
        Instant start = Instant.now();
        log.info("Simulation duration: {}ms", Duration.between(start, Instant.now()).toMillis());
        log.info("Departed flights: {}", simulationCoordinator.getDepartedFlightsNumber());
    }

    private static TerminalSimulationCoordinator runSimulation(SimulationConfiguration simulationConfig) {
        double simulationDuration = simulationConfig.getSimulationDuration();

        log.info("Starting simulation with duration: {}ms", simulationDuration);
        SimManager simManager = initSimManager(simulationDuration);
        TerminalSimulationCoordinator simulationCoordinator = new TerminalSimulationCoordinator(simulationConfig);
        simulationCoordinator.call(GENERATE_PASSENGER);
        simManager.startSimulation();
        simulationCoordinator.stop();
        log.info("Simulation finished");
        log.info("Generated passengers: {}", simulationCoordinator.getAction(GENERATE_PASSENGER).getActionTime().getChanges().size());
        log.info("Generated random events: {}", simulationCoordinator.getRandomEventActionTime().getChanges().size());
        log.info("Departed flights: {}", simulationCoordinator.getDepartedFlightsNumber());
        log.info("Departed passengers: {}", simulationCoordinator.getDepartedPassengersNumber());
        log.info("Missed flight passengers: {}", simulationCoordinator.getMissedFlightPassengersNumber());

        return simulationCoordinator;
    }

    private static SimManager initSimManager(double simulationDuration) {
        SimManager simManager = SimManager.getSimManager();
        simManager.setSimTime(0.0);
        simManager.setStopTime(simulationDuration);

        return simManager;
    }

    private static String readUserInput(String label) {
        log.info(label);
        Scanner sc = new Scanner(System.in);
        return sc.next();
    }

    private static void setLoggerLevel(String level) {
        if (!AVAILABLE_LOGGER_LEVELS.contains(level)) {
            level = DEFAULT_LOGGER_LEVEL;
        }
        Level loggerLevel = Level.getLevel(level.toUpperCase());
        Logger logger = LogManager.getRootLogger();
        Configurator.setAllLevels(logger.getName(), loggerLevel);
    }

    private static void generateStatistics(TerminalSimulationCoordinator simulationCoordinator) {
        logProcessedPassengers(simulationCoordinator, GENERATE_PASSENGER);
        logProcessedPassengers(simulationCoordinator, CHECK_IN);
        logProcessedPassengers(simulationCoordinator, SECURITY_CHECK);

        generateAverageStatistics(simulationCoordinator);
    }

    private static void logProcessedPassengers(TerminalSimulationCoordinator simulationCoordinator, ActionKey actionKey) {
        MonitoredVar actionTime = simulationCoordinator.getActionTime(actionKey);
        log.info("Total number of passengers processed in: '{}' action - {}", actionKey, actionTime.getChanges().size());
    }

    private static void generateAverageStatistics(TerminalSimulationCoordinator simulationCoordinator) {
        double averageCheckInTime = calculateAverageTime(simulationCoordinator.getActionTime(CHECK_IN));
        double averageSecurityCheckTime = calculateAverageTime(simulationCoordinator.getActionTime(SECURITY_CHECK));
        log.info("Average passenger waiting time: {}ms, service time: {}ms", averageCheckInTime, averageSecurityCheckTime);
    }

    private static double calculateAverageTime(MonitoredVar monitoredVar) {
        double arithmeticMean = Statistics.arithmeticMean(monitoredVar);
        return BigDecimal.valueOf(arithmeticMean).setScale(2, HALF_UP).doubleValue();
    }

    private static void generateServiceTimeHistogram(TerminalSimulationCoordinator simulationCoordinator) {
        log.info("Generating service time histogram");
        Diagram serviceTimeHistogram = new Diagram("Histogram", "Service Time");
        serviceTimeHistogram.add(simulationCoordinator.getActionTime(CHECK_IN), GREEN);
        serviceTimeHistogram.show();
    }
}
