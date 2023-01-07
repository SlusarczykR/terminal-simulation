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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.slusarczykr.terminal.simulation.action.ActionKey.CHECK_IN;
import static com.slusarczykr.terminal.simulation.action.ActionKey.DEPARTURE_FLIGHT;
import static com.slusarczykr.terminal.simulation.action.ActionKey.GENERATE_PASSENGER;
import static com.slusarczykr.terminal.simulation.action.ActionKey.RANDOM;
import static com.slusarczykr.terminal.simulation.action.ActionKey.SECURITY_CHECK;
import static java.awt.Color.GREEN;
import static java.math.RoundingMode.HALF_UP;
import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;

public class TerminalSimulation {

    private static final Logger log = LogManager.getLogger(TerminalSimulation.class);

    private static final List<String> AVAILABLE_LOGGER_LEVELS = Arrays.asList("INFO", "DEBUG", "TRACE");
    private static final String DEFAULT_LOGGER_LEVEL = "INFO";

    private static final String UNSUPPORTED_OPERATION_EXCEPTION = "Unsupported operation type";
    private static final List<ActionKey> ALLOWED_ACTIONS = Arrays.asList(GENERATE_PASSENGER, CHECK_IN, SECURITY_CHECK, DEPARTURE_FLIGHT, RANDOM);

    private static TerminalSimulationCoordinator simulationCoordinator = null;

    static {
        disableSystemLogging();
    }

    public static void main(String[] args) {
        SimulationConfiguration simulationConfig = new SimulationConfiguration();

        while (true) {
            log.info("\nq - Exit\n1 - Run simulation\n2 - Configure simulation\n3 - Change log level\n\n");
            String command = readUserCommand();
            log.info("\n");

            if (command.equalsIgnoreCase("q")) {
                break;
            } else if (command.equals("1")) {
                displaySimulationMenu(simulationConfig);
            } else if (command.equals("2")) {
                displaySimulationConfigurationMenu(simulationConfig);
            } else if (command.equals("3")) {
                setLoggerLevel(readUserInput(String.format("Logger level %s:", Arrays.toString(AVAILABLE_LOGGER_LEVELS.toArray()))));
            } else {
                log.warn(UNSUPPORTED_OPERATION_EXCEPTION);
            }
            log.info("\n");
        }
    }

    private static String readUserCommand() {
        return readUserInput("Command:");
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

    private static void displaySimulationConfigurationMenu(SimulationConfiguration simulationConfig) {
        while (true) {
            log.info("\nq - Back\n1 - Overall simulation settings\n2 - Simulation actions queues\n\n");
            String command = readUserCommand();
            log.info("\n");

            if (command.equalsIgnoreCase("q")) {
                break;
            } else if (command.equals("1")) {
                setSimulationSettings(simulationConfig);
            } else if (command.equals("2")) {
                setSimulationActionsInstances(simulationConfig);
            } else {
                log.warn(UNSUPPORTED_OPERATION_EXCEPTION);
            }
            log.info("\n");
        }
    }

    private static void setSimulationSettings(SimulationConfiguration simulationConfig) {
        try {
            getUserInputAndExecute("Simulation duration in seconds:", it -> simulationConfig.setSimulationDuration(Integer.parseInt(it)));
            getUserInputAndExecute("Maximum number of simultaneous flights:", it -> simulationConfig.setMaxFlightsNumber(Integer.parseInt(it)));
            getUserInputAndExecute("Random event probability:", it -> simulationConfig.setRandomEventProbability(Double.parseDouble(it)));
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    private static void setSimulationActionsInstances(SimulationConfiguration simulationConfig) {
        try {
            getUserInputAndExecute("Check in action queues:", it -> simulationConfig.setActionInstances(CHECK_IN, Integer.parseInt(it)));
            getUserInputAndExecute("Security check action queues", it -> simulationConfig.setActionInstances(SECURITY_CHECK, Integer.parseInt(it)));
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    private static void getUserInputAndExecute(String label, Consumer<String> setConfig) {
        String input = readUserInput(label);
        setConfig.accept(input);
        log.info("\n");
    }

    private static void displaySimulationMenu(SimulationConfiguration simulationConfig) {
        if (simulationCoordinator == null) {
            simulationCoordinator = runSimulation(simulationConfig);
        }
        while (true) {
            log.info("\nq - Back\n1 - Rerun simulation\n2 - Display simulation statistics\n3 - Configure simulation\n4 - Change log level\n\n");
            String command = readUserCommand();
            log.info("\n");

            if (command.equalsIgnoreCase("q")) {
                break;
            } else if (command.equals("1")) {
                simulationCoordinator = runSimulation(simulationConfig);
            } else if (command.equals("2")) {
                displaySimulationDetailsMenu(simulationCoordinator);
            } else if (command.equals("3")) {
                displaySimulationConfigurationMenu(simulationConfig);
            } else if (command.equals("4")) {
                setLoggerLevel(readUserInput(String.format("Logger level %s:", Arrays.toString(AVAILABLE_LOGGER_LEVELS.toArray()))));
            } else {
                log.warn(UNSUPPORTED_OPERATION_EXCEPTION);
            }
            log.info("\n");
        }
    }

    private static void displaySimulationDetailsMenu(TerminalSimulationCoordinator simulationCoordinator) {
        while (true) {
            log.info("\nq - Back\n1 - Display simulation statistics\n2 - Display simulation histogram\n\n");
            String command = readUserCommand();
            log.info("\n");

            if (command.equalsIgnoreCase("q")) {
                break;
            } else if (command.equals("1")) {
                generateStatistics(simulationCoordinator);
            } else if (command.equals("2")) {
                displayHistogram(simulationCoordinator);
            } else {
                log.warn(UNSUPPORTED_OPERATION_EXCEPTION);
            }
            log.info("\n");
        }
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
        displayGeneralSimulationStatistics(simulationCoordinator);

        return simulationCoordinator;
    }

    private static void displayGeneralSimulationStatistics(TerminalSimulationCoordinator simulationCoordinator) {
        log.info("Generated passengers: {}", simulationCoordinator.getActionInvocations(GENERATE_PASSENGER));
        log.info("Generated random events: {}", simulationCoordinator.getRandomEventActionOccurrences());
        log.info("Departed flights: {}", simulationCoordinator.getDepartedFlightsNumber());
        log.info("Departed passengers: {}", simulationCoordinator.getDepartedPassengersNumber());
        log.info("Missed flight passengers: {}", simulationCoordinator.getMissedFlightPassengersNumber());
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
        level = level.toUpperCase();

        if (!AVAILABLE_LOGGER_LEVELS.contains(level)) {
            log.warn("Invalid logger level '{}' specified. Default '{}' logger level will be applied", level, DEFAULT_LOGGER_LEVEL);
            level = DEFAULT_LOGGER_LEVEL;
        }
        log.info("Changing logger level to '{}'", level);
        Level loggerLevel = Level.getLevel(level);
        Logger logger = LogManager.getRootLogger();
        Configurator.setAllLevels(logger.getName(), loggerLevel);
    }

    private static void generateStatistics(TerminalSimulationCoordinator simulationCoordinator) {
        displayGeneralSimulationStatistics(simulationCoordinator);
        logProcessedPassengers(simulationCoordinator, GENERATE_PASSENGER);
        logProcessedPassengers(simulationCoordinator, CHECK_IN);
        logProcessedPassengers(simulationCoordinator, SECURITY_CHECK);

        generateAverageStatistics(simulationCoordinator);
    }

    private static void logProcessedPassengers(TerminalSimulationCoordinator simulationCoordinator, ActionKey actionKey) {
        int actionInvocations = simulationCoordinator.getActionInvocations(actionKey);
        log.info("Total number of passengers processed in: '{}' action - {}", actionKey, actionInvocations);
        Map<Integer, MonitoredVar> actionTimes = simulationCoordinator.getActionTimes(actionKey);
        actionTimes.forEach((index, actionTime) -> {
            int actionInstanceInvocations = actionTime.getChanges().size();
            log.info("Total number of passengers processed in: '{}' action queue index: '{}' - {}", actionKey, index, actionInstanceInvocations);
        });
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

    private static void displayHistogram(TerminalSimulationCoordinator simulationCoordinator) {
        Map<Integer, ActionKey> actionKeys = IntStream.range(0, ALLOWED_ACTIONS.size()).boxed()
                .collect(Collectors.toMap(Function.identity(), ALLOWED_ACTIONS::get));

        String input = readUserInput(String.format("Action %s", Arrays.toString(toActionOptions(actionKeys).toArray())));
        try {
            ActionKey actionKey = actionKeys.get(Integer.parseInt(input));

            if (!ALLOWED_ACTIONS.contains(actionKey)) {
                throw new IllegalArgumentException("Action key is not valid");
            }
            log.info("Generating '{}' action histogram", actionKey.name());
            MonitoredVar actionTime = getActionTime(simulationCoordinator, actionKey);
            generateHistogram(actionTime, actionKey.name());
        } catch (Exception e) {
            log.error("Invalid action key: {}", input);
        }
    }

    private static List<String> toActionOptions(Map<Integer, ActionKey> actionKeys) {
        return actionKeys.entrySet().stream()
                .map(Object::toString)
                .collect(Collectors.toList());
    }

    private static MonitoredVar getActionTime(TerminalSimulationCoordinator simulationCoordinator, ActionKey actionKey) {
        if (actionKey == RANDOM) {
            return simulationCoordinator.getRandomEventActionTime();
        } else if (actionKey == DEPARTURE_FLIGHT) {
            return simulationCoordinator.getDepartureFlightActionTime();
        }
        return simulationCoordinator.getActionTime(actionKey);
    }

    private static void generateHistogram(MonitoredVar actionTime, String title) {
        Diagram serviceTimeHistogram = new Diagram("Histogram", title);
        serviceTimeHistogram.add(actionTime, GREEN);
        serviceTimeHistogram.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        serviceTimeHistogram.show();
    }
}
