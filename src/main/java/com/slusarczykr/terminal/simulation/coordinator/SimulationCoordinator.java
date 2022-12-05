package com.slusarczykr.terminal.simulation.coordinator;

import com.slusarczykr.terminal.simulation.action.CheckInPassengerActivity;
import com.slusarczykr.terminal.simulation.action.GeneratePassengerActivity;
import com.slusarczykr.terminal.simulation.action.SecurityCheckPassengerActivity;
import com.slusarczykr.terminal.simulation.coordinator.PassengerQueue.QueueType;
import com.slusarczykr.terminal.simulation.model.Flight;
import com.slusarczykr.terminal.simulation.model.Passenger;
import deskit.SimActivity;
import deskit.SimObject;
import deskit.monitors.MonitoredVar;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

public class SimulationCoordinator extends SimObject {

    private static final Logger log = LogManager.getLogger(SimulationCoordinator.class);

    public static final int DEFAULT_FLIGHTS_NUMBER = 10;

    public final MonitoredVar serviceTime;
    public final MonitoredVar waitingTime;
    public final MonitoredVar queueLength;

    private final Map<QueueType, PassengerQueue> passengerQueues;
    private final Map<Integer, Flight> flights;

    private final SimActivity generatePassengerActivity;
    private final SimActivity checkInPassengerActivity;
    private final SimActivity securityCheckPassengerActivity;

    public SimulationCoordinator() {
        this.serviceTime = new MonitoredVar(this);
        this.waitingTime = new MonitoredVar(this);
        this.queueLength = new MonitoredVar(this);
        this.passengerQueues = initPassengerQueues();
        this.flights = generateFlights();
        this.generatePassengerActivity = new GeneratePassengerActivity();
        this.checkInPassengerActivity = new CheckInPassengerActivity();
        this.securityCheckPassengerActivity = new SecurityCheckPassengerActivity();
    }

    private Map<QueueType, PassengerQueue> initPassengerQueues() {
        Map<QueueType, PassengerQueue> queueMap = new ConcurrentHashMap<>();
        Arrays.stream(QueueType.values()).forEach(it -> {
            PassengerQueue passengerQueue = new PassengerQueue(it, new MonitoredVar(this));
            queueMap.put(it, passengerQueue);
        });
        return queueMap;
    }

    private Map<Integer, Flight> generateFlights() {
        Map<Integer, Flight> generatedFlights = new ConcurrentHashMap<>();
        IntStream.rangeClosed(1, DEFAULT_FLIGHTS_NUMBER)
                .forEach(idx -> generatedFlights.put(idx, new Flight(idx)));

        return generatedFlights;
    }

    public Optional<Flight> getFlight(int id) {
        return Optional.ofNullable(flights.get(id));
    }

    public int getFlightsSize() {
        return flights.size();
    }

    public void addPassenger(QueueType queueType, Passenger passenger) {
        log.trace("Add passenger to queue: {}", passenger);
        passengerQueues.get(queueType).add(passenger);
        updateQueueSize();
    }

    public Passenger nextPassenger(QueueType queueType) {
        log.trace("Poll passenger from the queue with size: {}", passengerQueues.size());
        Passenger passenger = passengerQueues.get(queueType).poll();
        updateQueueSize();

        return passenger;
    }

    public void updateQueueSize() {
        this.queueLength.setValue(passengerQueues.size());
    }


    public void blockQueue(QueueType queueType) {
        log.trace("Blocking '{}' queue...", queueType);
        this.passengerQueues.get(queueType).blockQueue();
    }

    public void freeQueue(QueueType queueType) {
        log.trace("Releasing '{}' queue...", queueType);
        this.passengerQueues.get(queueType).freeQueue();
    }

    public boolean isOccupied(QueueType queueType) {
        return passengerQueues.get(queueType).isOccupied();
    }

    public SimActivity getGeneratePassengerActivity() {
        return generatePassengerActivity;
    }

    public SimActivity getCheckInPassengerActivity() {
        return checkInPassengerActivity;
    }

    public SimActivity getSecurityCheckPassengerActivity() {
        return securityCheckPassengerActivity;
    }

    public MonitoredVar getServiceTime() {
        return serviceTime;
    }

    public void setServiceTime(double delay) {
        log.trace("Set service time: {}", delay);
        serviceTime.setValue(delay);
    }

    public MonitoredVar getWaitingTime() {
        return waitingTime;
    }

    public void setWaitingTime(double delay) {
        log.trace("Set waiting time: {}", delay);
        waitingTime.setValue(delay);
    }

    public int getQueueLength(QueueType queueType) {
        return passengerQueues.get(queueType).getLength();
    }
}
