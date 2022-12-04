package com.slusarczykr.terminal.simulation.coordinator;

import com.slusarczykr.terminal.simulation.action.GeneratePassengerActivity;
import com.slusarczykr.terminal.simulation.action.ServicePassengerActivity;
import com.slusarczykr.terminal.simulation.model.Passenger;
import deskit.SimActivity;
import deskit.SimObject;
import deskit.monitors.MonitoredVar;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class SimulationCoordinator extends SimObject {

    private static final Logger log = LogManager.getLogger(SimulationCoordinator.class);

    public final MonitoredVar serviceTime;
    public final MonitoredVar waitingTime;
    public final MonitoredVar queueLength;

    private final Queue<Passenger> queue;
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

    public void addPassenger(Passenger passenger) {
        log.info("Add passenger to queue: {}", passenger);
        queue.add(passenger);
        updateQueueSize();
    }

    public Passenger nextPassenger() {
        log.info("Poll passenger from the queue with size: {}", queue.size());
        Passenger passenger = queue.poll();
        updateQueueSize();

        return passenger;
    }

    public void updateQueueSize() {
        this.queueLength.setValue(queue.size());
    }

    public boolean isOccupied() {
        return occupied.get();
    }

    public void blockQueue() {
        log.info("Blocking the queue...");
        this.occupied.set(true);
    }

    public void freeQueue() {
        log.info("Releasing the queue...");
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
        log.info("Set service time: {}", delay);
        serviceTime.setValue(delay);
    }

    public MonitoredVar getWaitingTime() {
        return waitingTime;
    }

    public void setWaitingTime(double delay) {
        log.info("Set waiting time: {}", delay);
        waitingTime.setValue(delay);
    }

    public int getQueueLength() {
        return (int) queueLength.getValue();
    }
}
