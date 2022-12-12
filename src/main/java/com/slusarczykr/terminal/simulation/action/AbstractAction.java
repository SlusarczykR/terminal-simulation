package com.slusarczykr.terminal.simulation.action;

import com.slusarczykr.terminal.simulation.action.queue.ActionQueue;
import com.slusarczykr.terminal.simulation.action.random.RandomEventAction;
import com.slusarczykr.terminal.simulation.coordinator.SimulationCoordinator;
import deskit.SimActivity;
import deskit.monitors.MonitoredVar;
import deskit.random.SimGenerator;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;
import java.util.Random;

public abstract class AbstractAction<T> extends SimActivity implements Action<T> {

    protected final int index;
    protected final SimulationCoordinator<T> simulationCoordinator;
    protected final SimGenerator simulationGenerator;
    protected final MonitoredVar actionTime;
    protected final Random random;
    protected final ActionQueue<T> actionQueue;

    protected AbstractAction(SimulationCoordinator<T> simulationCoordinator, int index) {
        this.index = index;
        this.simulationCoordinator = simulationCoordinator;
        this.simulationGenerator = new SimGenerator();
        this.actionTime = new MonitoredVar(simulationCoordinator);
        this.random = new Random();
        this.actionQueue = createActionQueue();
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public void call() {
        if (isSimulationRunning()) {
            try {
                callActivity(simulationCoordinator, this);
                simulationCoordinator.addActivity(this);
            } catch (Exception e) {
                getLogger().error("Exception thrown during action execution", e);
            }
        } else {
            getLogger().debug("Simulation is no longer running. Action: '{}' will not be called", getKey());
        }
    }

    protected boolean isSimulationRunning() {
        return simulationCoordinator.isSimulationRunning();
    }

    @Override
    public void callNextAction() {
        Action<T> nextAction = simulationCoordinator.getAction(getNextActionKey());
        nextAction.call();
    }

    @Override
    public void callNextAction(T element) {
        ActionKey nextActionKey = getNextActionKey();
        Optional<ActionKey> randomNextActionKey = getRandomNextActionKey();

        if (randomNextActionKey.isPresent()) {
            Action<T> nextAction = new RandomEventAction<>(simulationCoordinator, randomNextActionKey.get(), nextActionKey, element);
            nextAction.call();
        } else {
            Action<T> nextAction = simulationCoordinator.getAction(nextActionKey);

            ActionQueue<T> nextActionQueue = nextAction.getQueue();
            nextActionQueue.add(element);

            if (nextActionQueue.getLength() == 1 && !nextActionQueue.isOccupied()) {
                nextAction.call();
            }
        }
    }

    protected abstract Logger getLogger();

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
    public ActionQueue<T> getQueue() {
        return actionQueue;
    }

    protected ActionQueue<T> createActionQueue() {
        return null;
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

    protected double format(double number) {
        return Double.parseDouble(String.format("%.2f", number));
    }
}
