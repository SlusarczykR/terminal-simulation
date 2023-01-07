package com.slusarczykr.terminal.simulation.action;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum ActionKey {

    GENERATE_PASSENGER,
    CHECK_IN,
    SECURITY_CHECK,
    DEPARTURE_FLIGHT,
    RANDOM,
    TOILET,
    DINNER,
    SHOPPING;

    private static final List<ActionKey> RANDOM_EVENT_ACTION_KEYS = Arrays.asList(TOILET, DINNER, SHOPPING);

    public static List<ActionKey> getRandomEventActionKeys() {
        return Collections.unmodifiableList(RANDOM_EVENT_ACTION_KEYS);
    }

    public static boolean isRandomEvent(ActionKey actionKey) {
        return getRandomEventActionKeys().contains(actionKey);
    }
}
