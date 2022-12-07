package com.slusarczykr.terminal.simulation.action;

import com.slusarczykr.terminal.simulation.action.queue.ActionQueue;
import com.slusarczykr.terminal.simulation.coordinator.SimulationCoordinator;
import deskit.SimActivity;
import deskit.monitors.MonitoredVar;
import deskit.random.SimGenerator;

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
        SimActivity.callActivity(simulationCoordinator, (SimActivity) nextAction);
    }

    @Override
    public void callNextAction(T element) {
        Action<T> nextAction = simulationCoordinator.getAction(getNextActionKey());
        SimActivity.callActivity(simulationCoordinator, (SimActivity) nextAction);
        ActionQueue<T> nextActionQueue = nextAction.getQueue();
        nextActionQueue.add(element);
    }

    @Override
    public MonitoredVar getActionTime() {
        return actionTime;
    }

    protected boolean await(double delay) {
        waitDuration(delay);
        return isStopped() || isInterrupted();
    }
}
