package com.slusarczykr.terminal.simulation.action;

import com.slusarczykr.terminal.simulation.action.queue.ActionQueue;
import com.slusarczykr.terminal.simulation.action.random.RandomEventAction;
import com.slusarczykr.terminal.simulation.coordinator.SimulationCoordinator;
import deskit.SimActivity;
import deskit.monitors.MonitoredVar;
import deskit.random.SimGenerator;

import java.util.List;
import java.util.Optional;
import java.util.Random;

public abstract class AbstractAction<T> extends SimActivity implements Action<T> {

    protected final SimulationCoordinator<T> simulationCoordinator;
    protected final SimGenerator simulationGenerator;
    protected final MonitoredVar actionTime;
    protected final Random random;

    protected AbstractAction(SimulationCoordinator<T> simulationCoordinator) {
        this.simulationCoordinator = simulationCoordinator;
        this.simulationGenerator = new SimGenerator();
        this.actionTime = new MonitoredVar(simulationCoordinator);
        this.random = new Random();
    }

    @Override
    public void callNextAction() {
        Action<T> nextAction = simulationCoordinator.getAction(getNextActionKey());
        callActivity(simulationCoordinator, (SimActivity) nextAction);
    }

    @Override
    public void callNextAction(T element) {
        ActionKey nextActionKey = getNextActionKey();
        Optional<ActionKey> randomNextActionKey = getRandomNextActionKey();

        if (randomNextActionKey.isPresent()) {
            Action<T> nextAction = new RandomEventAction<>(simulationCoordinator, randomNextActionKey.get(), nextActionKey, element);
            callActivity(simulationCoordinator, (SimActivity) nextAction);

        } else {
            Action<T> nextAction = simulationCoordinator.getAction(nextActionKey);

            ActionQueue<T> nextActionQueue = nextAction.getQueue();
            nextActionQueue.add(element);

            if (nextActionQueue.getLength() == 1 && !nextActionQueue.isOccupied()) {
                callActivity(simulationCoordinator, (SimActivity) nextAction);
            }
        }
    }

    private Optional<ActionKey> getRandomNextActionKey() {
        if (randomEventEnabled() && generateProbability(0.1)) {
            return Optional.of(getRandomEventActionKey());
        }
        return Optional.empty();
    }

    private ActionKey getRandomEventActionKey() {
        List<ActionKey> randomEventActionKeys = ActionKey.getRandomEventActionKeys();
        int index = random.nextInt(randomEventActionKeys.size());

        return randomEventActionKeys.get(index);
    }

    @Override
    public MonitoredVar getActionTime() {
        return actionTime;
    }

    protected boolean generateProbability(double percentage) {
        if (percentage <= 0.0) {
            percentage = 0.5;
        }
        return random.nextDouble() < percentage;
    }

    protected boolean await(double delay) {
        waitDuration(delay);
        return isStopped() || isInterrupted();
    }
}
