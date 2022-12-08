package com.slusarczykr.terminal.simulation.action;

import com.slusarczykr.terminal.simulation.action.queue.ActionQueue;
import deskit.monitors.MonitoredVar;

public interface Action<T> {

    default void callNextAction() {
        callNextAction(null);
    }

    default void callNextAction(T element) {
    }

    ActionKey getKey();

    default boolean randomEventEnabled() {
        return true;
    }

    default ActionQueue<T> getQueue() {
        return null;
    }

    default ActionKey getNextActionKey() {
        return null;
    }

    MonitoredVar getActionTime();

    default void setActionTime(double delay) {
        getActionTime().setValue(delay);
    }
}
