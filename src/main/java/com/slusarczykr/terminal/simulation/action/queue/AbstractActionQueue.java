package com.slusarczykr.terminal.simulation.action.queue;

import com.slusarczykr.terminal.simulation.action.ActionKey;
import deskit.monitors.MonitoredVar;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class AbstractActionQueue<T> implements ActionQueue<T> {

    private final ActionKey actionKey;
    private final Queue<T> elements;
    private final MonitoredVar queueLength;
    private final AtomicBoolean occupied;

    public AbstractActionQueue(ActionKey actionKey, MonitoredVar queueLength) {
        this.actionKey = actionKey;
        this.elements = new ConcurrentLinkedQueue<>();
        this.queueLength = queueLength;
        this.occupied = new AtomicBoolean(false);
    }

    private void updateQueueSize() {
        this.queueLength.setValue(elements.size());
    }

    @Override
    public ActionKey getActionKey() {
        return actionKey;
    }

    @Override
    public Queue<T> getQueue() {
        return elements;
    }

    @Override
    public T poll() {
        T element = elements.poll();
        updateQueueSize();

        return element;
    }

    @Override
    public void add(T element) {
        this.elements.add(element);
        updateQueueSize();
    }

    @Override
    public int getLength() {
        return (int) queueLength.getValue();
    }

    @Override
    public AtomicBoolean getOccupied() {
        return occupied;
    }
}
