package com.slusarczykr.terminal.simulation;

import com.slusarczykr.terminal.simulation.action.Action;
import com.slusarczykr.terminal.simulation.action.ActionKey;
import com.slusarczykr.terminal.simulation.action.CheckInPassengerAction;
import com.slusarczykr.terminal.simulation.action.GeneratePassengerAction;
import com.slusarczykr.terminal.simulation.action.SecurityCheckPassengerAction;
import com.slusarczykr.terminal.simulation.coordinator.SimulationCoordinator;
import com.slusarczykr.terminal.simulation.model.Passenger;
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Consumer;
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

    private static final List<String> AVAILABLE_LOGGER_LEVELS = Arrays.asList("INFO", "DEBUG", "TRACE");

    private static final String UNSUPPORTED_OPERATION_EXCEPTION = "Unsupported operation type";

    public static void main(String[] args) {
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
        SimulationCoordinator<Passenger> simulationCoordinator = runSimulation(simulationConfig);

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

    private static void displaySimulationStatistics(SimulationCoordinator<Passenger> simulationCoordinator) {
        Instant start = Instant.now();
        log.info("Simulation duration: {}ms", Duration.between(start, Instant.now()).toMillis());
        log.info("Departed flights: {}", simulationCoordinator.getDepartedFlightsNumber());
    }

    private static SimulationCoordinator<Passenger> runSimulation(SimulationConfiguration simulationConfig) {
        double simulationDuration = simulationConfig.getSimulationDuration();

        SimulationCoordinator<Passenger> simulationCoordinator = new SimulationCoordinator<>(Simulation::initPassengerActions);
        simulationCoordinator.simManager.setStopTime(simulationDuration);
        simulationCoordinator.call(GENERATE_PASSENGER);

        log.info("Starting simulation with duration: {}ms", simulationDuration);
        simulationCoordinator.simManager.startSimulation();
        simulationCoordinator.stop();
        log.info("Simulation finished");

        return simulationCoordinator;
    }

    private static String readUserInput(String label) {
        log.info(label);
        Scanner sc = new Scanner(System.in);
        return sc.next();
    }

    private static void setLoggerLevel(String level) {
        if (!AVAILABLE_LOGGER_LEVELS.contains(level)) {
            level = "INFO";
        }
        Level loggerLevel = Level.getLevel(level.toUpperCase());
        Logger logger = LogManager.getRootLogger();
        Configurator.setAllLevels(logger.getName(), loggerLevel);
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

    private static class SimulationConfiguration {

        private static final double DEFAULT_SIMULATION_DURATION = 30000.0;
        private static final double MIN_SIMULATION_DURATION = 5000.0;
        private static final double DEFAULT_MAX_FLIGHTS_NUMBER = 10.0;
        private static final double MIN_FLIGHTS_NUMBER = 3;
        private static final double DEFAULT_RANDOM_EVENT_PROBABILITY = 0.1;
        private static final double MIN_RANDOM_EVENT_PROBABILITY = 0.01;

        private double simulationDuration;
        private double maxFlightsNumber;
        private double randomEventProbability;

        public SimulationConfiguration() {
            this.simulationDuration = DEFAULT_SIMULATION_DURATION;
            this.maxFlightsNumber = DEFAULT_MAX_FLIGHTS_NUMBER;
            this.randomEventProbability = DEFAULT_RANDOM_EVENT_PROBABILITY;
        }

        public double getSimulationDuration() {
            return simulationDuration;
        }

        public void setSimulationDuration(double simulationDuration) {
            if (simulationDuration < MIN_SIMULATION_DURATION) {
                simulationDuration = MIN_SIMULATION_DURATION;
            }
            this.simulationDuration = simulationDuration;
        }

        public double getMaxFlightsNumber() {
            return maxFlightsNumber;
        }

        public void setMaxFlightsNumber(double maxFlightsNumber) {
            if (maxFlightsNumber < MIN_FLIGHTS_NUMBER) {
                maxFlightsNumber = MIN_FLIGHTS_NUMBER;
            }
            this.maxFlightsNumber = maxFlightsNumber;
        }

        public double getRandomEventProbability() {
            return randomEventProbability;
        }

        public void setRandomEventProbability(double randomEventProbability) {
            if (randomEventProbability < MIN_RANDOM_EVENT_PROBABILITY) {
                randomEventProbability = MIN_RANDOM_EVENT_PROBABILITY;
            }
            this.randomEventProbability = randomEventProbability;
        }
    }
}
