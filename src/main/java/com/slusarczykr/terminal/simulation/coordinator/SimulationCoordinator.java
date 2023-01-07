package com.slusarczykr.terminal.simulation.coordinator;

import com.slusarczykr.terminal.simulation.action.Action;
import com.slusarczykr.terminal.simulation.action.ActionKey;
import com.slusarczykr.terminal.simulation.action.queue.ActionQueueState;
import deskit.SimActivity;
import deskit.SimObject;
import deskit.monitors.Change;
import deskit.monitors.ChangesList;
import deskit.monitors.MonitoredVar;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.slusarczykr.terminal.simulation.action.queue.ActionQueueState.NOT_OCCUPIED;

public abstract class SimulationCoordinator<T> extends SimObject {

    private static final Logger log = LogManager.getLogger(SimulationCoordinator.class);

    public static final int DEFAULT_FLIGHTS_NUMBER = 10;

    protected final Set<SimActivity> activities;
    protected final Map<ActionKey, List<Action<T>>> actions;
    protected final MonitoredVar randomEventActionTime;
    private final Random random;
    private final boolean actionByQueueTypeEnabled;

    protected SimulationCoordinator() {
        this.activities = ConcurrentHashMap.newKeySet();
        this.actions = new ConcurrentHashMap<>();
        this.randomEventActionTime = new MonitoredVar(this);
        this.random = new Random();
        this.actionByQueueTypeEnabled = false;
    }

    public MonitoredVar getRandomEventActionTime() {
        return randomEventActionTime;
    }

    public int getRandomEventActionOccurrences() {
        return randomEventActionTime.getChanges().size();
    }

    public void setRandomEventActionTime(double delay) {
        this.randomEventActionTime.setValue(delay);
    }

    public void call(ActionKey actionKey) {
        getAction(actionKey).call();
    }

    public boolean isSimulationRunning() {
        return simManager.getSimTime() <= simManager.getStopTime()
                && simManager.getFirstSimObjectFromPendingList() != null;
    }

    public void addActivity(SimActivity activity) {
        this.activities.add(activity);
    }

    public void stop() {
        activities.stream()
                .filter(Thread::isAlive)
                .forEach(it -> {
                    try {
                        it.resumeActivity();
                        it.terminate();
                    } catch (Exception e) {
                        log.error("Exception thrown during activity termination", e);
                    }
                });
    }

    public List<Action<T>> getActionInstances(ActionKey actionKey) {
        return actions.get(actionKey);
    }

    public Action<T> getAction(ActionKey actionKey) {
        return getAction(actionKey, NOT_OCCUPIED);
    }

    public Action<T> getAction(ActionKey actionKey, ActionQueueState state) {
        List<Action<T>> actionInstances = actions.get(actionKey);

        if (actionByQueueTypeEnabled) {
            return getActionByQueueType(actionInstances, state);
        }
        return getRandomActionInstance(actionInstances);
    }

    private Action<T> getRandomActionInstance(List<Action<T>> actionInstances) {
        int actionIndex = random.nextInt(actionInstances.size());
        return actionInstances.get(actionIndex);
    }

    private Action<T> getActionByQueueType(List<Action<T>> actionInstances, ActionQueueState state) {
        if (hasQueue(actionInstances)) {
            if (state == NOT_OCCUPIED) {
                return getActionByNotOccupiedQueue(actionInstances);
            }
            return getActionByQueueWithLowestLoad(actionInstances);
        }
        return getRandomActionInstance(actionInstances);
    }

    private boolean hasQueue(List<Action<T>> actionInstances) {
        return actionInstances.stream().anyMatch(it -> it.getQueue() != null);
    }

    private Action<T> getActionByNotOccupiedQueue(List<Action<T>> actionInstances) {
        return actionInstances.stream()
                .filter(it -> !it.getQueue().isOccupied())
                .findFirst()
                .orElseGet(() -> getActionByQueueWithLowestLoad(actionInstances));
    }

    private Action<T> getActionByQueueWithLowestLoad(List<Action<T>> actionInstances) {
        Map<Integer, List<Action<T>>> queuesByLoad = actionInstances.stream()
                .collect(Collectors.groupingBy(it -> it.getQueue().getLength(), TreeMap::new, Collectors.toList()));

        return queuesByLoad.entrySet().iterator().next().getValue().get(0);
    }

    public Map<Integer, MonitoredVar> getActionTimes(ActionKey actionKey) {
        List<Action<T>> actionInstances = getActionInstances(actionKey);
        return actionInstances.stream()
                .collect(Collectors.toMap(Action::getIndex, Action::getActionTime));
    }

    public MonitoredVar getActionTime(ActionKey actionKey) {
        List<Action<T>> actionInstances = getActionInstances(actionKey);
        MonitoredVar actionTime = new MonitoredVar(this);
        actionInstances.stream()
                .map(it -> it.getActionTime().getChanges())
                .forEach(it -> addChanges(actionTime, it));

        return actionTime;
    }

    protected void addChanges(MonitoredVar monitoredVar, ChangesList changesList) {
        for (int i = 0; i < changesList.size(); i++) {
            Change change = changesList.get(i);
            monitoredVar.getChanges().add(change);
        }
    }

    public int getActionInvocations(ActionKey actionKey) {
        MonitoredVar actionTime = getActionTime(actionKey);
        return actionTime.getChanges().size();
    }
}
