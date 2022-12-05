package com.slusarczykr.terminal.simulation.coordinator;

import com.slusarczykr.terminal.simulation.model.Passenger;
import deskit.monitors.MonitoredVar;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class PassengerQueue {

    private final QueueType queueType;
    private final Queue<Passenger> passengers;
    private final MonitoredVar queueLength;
    private final AtomicBoolean occupied = new AtomicBoolean(false);

    public PassengerQueue(QueueType queueType, MonitoredVar queueLength) {
        this.queueType = queueType;
        this.passengers = new ConcurrentLinkedQueue<>();
        this.queueLength = queueLength;
    }

    public void add(Passenger passenger) {
        this.passengers.add(passenger);
        updateQueueSize();
    }

    public Passenger poll() {
        Passenger passenger = passengers.poll();
        updateQueueSize();

        return passenger;
    }

    public int getLength() {
        return (int) queueLength.getValue();
    }

    public void updateQueueSize() {
        this.queueLength.setValue(passengers.size());
    }

    public void blockQueue() {
        this.occupied.set(true);
    }

    public void freeQueue() {
        this.occupied.set(false);
    }

    public boolean isOccupied() {
        return occupied.get();
    }

    public enum QueueType {
        CHECK_IN, SECURITY_CHECK;
    }
}
