package com.slusarczykr.terminal.simulation.coordinator;

import com.slusarczykr.terminal.simulation.action.GeneratePassengerActivity;
import com.slusarczykr.terminal.simulation.action.ServicePassengerActivity;
import com.slusarczykr.terminal.simulation.model.Passenger;
import deskit.SimActivity;
import deskit.SimObject;
import deskit.monitors.MonitoredVar;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class SimulationCoordinator extends SimObject {

    public final MonitoredVar serviceTime;
    public final MonitoredVar waitingTime;
    public final MonitoredVar queueLength;

    private final ConcurrentLinkedQueue<Passenger> queue;
    private final AtomicBoolean occupied = new AtomicBoolean(false);

    private final SimActivity generatePassengerActivity;
    private final SimActivity servicePassengerActivity;

    public SimulationCoordinator() {
        this.serviceTime = new MonitoredVar(this);
        this.waitingTime = new MonitoredVar(this);
        this.queueLength = new MonitoredVar(this);
        this.queue = new ConcurrentLinkedQueue<>();
        this.generatePassengerActivity = new GeneratePassengerActivity();
        this.servicePassengerActivity = new ServicePassengerActivity();
    }

    public int addPassenger(Passenger passenger) {
        queue.add(passenger);
        return queue.size();
    }

    public Passenger nextPassenger() {
        queueLength.setValue(queue.size());
        return queue.poll();
    }

    public ConcurrentLinkedQueue<Passenger> getQueue() {
        return queue;
    }

    public boolean isOccupied() {
        return occupied.get();
    }

    public void blockQueue() {
        this.occupied.set(true);
    }

    public void freeQueue() {
        this.occupied.set(false);
    }

    public SimActivity getGeneratePassengerActivity() {
        return generatePassengerActivity;
    }

    public SimActivity getServicePassengerActivity() {
        return servicePassengerActivity;
    }

    public MonitoredVar getServiceTime() {
        return serviceTime;
    }

    public void setServiceTime(double delay) {
        serviceTime.setValue(delay);
    }

    public MonitoredVar getWaitingTime() {
        return waitingTime;
    }

    public void setWaitingTime(double delay) {
        waitingTime.setValue(delay);
    }

    public int getQueueLength() {
        return (int) queueLength.getValue();
    }
}
