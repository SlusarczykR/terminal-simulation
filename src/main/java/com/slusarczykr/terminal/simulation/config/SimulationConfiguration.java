package com.slusarczykr.terminal.simulation.config;

import com.slusarczykr.terminal.simulation.action.ActionKey;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.slusarczykr.terminal.simulation.action.ActionKey.CHECK_IN;
import static com.slusarczykr.terminal.simulation.action.ActionKey.GENERATE_PASSENGER;
import static com.slusarczykr.terminal.simulation.action.ActionKey.SECURITY_CHECK;

public class SimulationConfiguration {

    private static final double DEFAULT_SIMULATION_DURATION = 50000.0;
    private static final double MIN_SIMULATION_DURATION = 5000.0;
    private static final double DEFAULT_MAX_FLIGHTS_NUMBER = 10.0;
    private static final double MIN_FLIGHTS_NUMBER = 3;
    private static final double DEFAULT_RANDOM_EVENT_PROBABILITY = 0.1;
    private static final double MIN_RANDOM_EVENT_PROBABILITY = 0.01;

    private static final int DEFAULT_NON_QUEUE_ACTION_INSTANCES = 1;
    private static final int DEFAULT_QUEUE_ACTION_INSTANCES = 3;

    private double simulationDuration;
    private double maxFlightsNumber;
    private double randomEventProbability;
    private Map<ActionKey, Action> actionConfigs;

    public SimulationConfiguration() {
        this.simulationDuration = DEFAULT_SIMULATION_DURATION;
        this.maxFlightsNumber = DEFAULT_MAX_FLIGHTS_NUMBER;
        this.randomEventProbability = DEFAULT_RANDOM_EVENT_PROBABILITY;
        this.actionConfigs = initActionConfigs();
    }

    private Map<ActionKey, Action> initActionConfigs() {
        Map<ActionKey, Action> actionKeyToAction = new HashMap<>();
        actionKeyToAction.put(GENERATE_PASSENGER, new Action(GENERATE_PASSENGER, DEFAULT_NON_QUEUE_ACTION_INSTANCES));
        actionKeyToAction.put(CHECK_IN, new Action(GENERATE_PASSENGER, DEFAULT_QUEUE_ACTION_INSTANCES));
        actionKeyToAction.put(SECURITY_CHECK, new Action(GENERATE_PASSENGER, DEFAULT_QUEUE_ACTION_INSTANCES));

        return actionKeyToAction;
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

    public Map<ActionKey, Action> getActionConfigs() {
        return actionConfigs;
    }

    public int getActionInstances(ActionKey actionKey) {
        return Optional.ofNullable(actionConfigs.get(actionKey))
                .map(Action::getActionInstances)
                .orElse(0);
    }

    public void setActionInstances(ActionKey actionKey, int actionInstances) {
        Optional.ofNullable(actionConfigs.get(actionKey)).ifPresent(it -> it.setActionInstances(actionInstances));
    }

    public static class Action {
        private ActionKey actionKey;
        private int actionInstances;

        public Action(ActionKey actionKey, int actionInstances) {
            this.actionKey = actionKey;
            this.actionInstances = actionInstances;
        }

        public ActionKey getActionKey() {
            return actionKey;
        }

        public void setActionKey(ActionKey actionKey) {
            this.actionKey = actionKey;
        }

        public int getActionInstances() {
            return actionInstances;
        }

        public void setActionInstances(int actionInstances) {
            this.actionInstances = actionInstances;
        }
    }
}
