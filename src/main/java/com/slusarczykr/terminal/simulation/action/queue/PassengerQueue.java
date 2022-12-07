package com.slusarczykr.terminal.simulation.action.queue;

import com.slusarczykr.terminal.simulation.action.ActionKey;
import com.slusarczykr.terminal.simulation.model.Passenger;
import deskit.monitors.MonitoredVar;

public class PassengerQueue extends AbstractActionQueue<Passenger> {

    public PassengerQueue(ActionKey actionKey, MonitoredVar queueLength) {
        super(actionKey, queueLength);
    }
}
