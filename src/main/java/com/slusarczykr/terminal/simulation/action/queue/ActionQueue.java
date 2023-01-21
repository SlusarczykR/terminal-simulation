package com.slusarczykr.terminal.simulation.action.queue;

import com.slusarczykr.terminal.simulation.action.ActionKey;

import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

public interface ActionQueue<T> {

    ActionKey getActionKey();

    Queue<T> getQueue();

    T poll();

    void add(T element);

    int getLength();

    AtomicBoolean getOccupied();

    default void block() {
        getOccupied().set(true);
    }

    default void release() {
        getOccupied().set(false);
    }

    default boolean isOccupied() {
        return getOccupied().get();
    }
}
