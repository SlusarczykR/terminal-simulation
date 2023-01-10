package com.slusarczykr.terminal.simulation.action.random;

import com.slusarczykr.terminal.simulation.action.AbstractAction;
import com.slusarczykr.terminal.simulation.action.ActionKey;
import com.slusarczykr.terminal.simulation.coordinator.SimulationCoordinator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RandomEventAction<T> extends AbstractAction<T> {

    private static final Logger log = LogManager.getLogger(RandomEventAction.class);

    private static int indexOffset = 0;

    private boolean executed;
    private final ActionKey actionKey;
    private final ActionKey nextActionKey;
    private final T element;

    public RandomEventAction(SimulationCoordinator<T> simulationCoordinator,
                             ActionKey randomActionKey,
                             ActionKey nextActionKey,
                             T element
    ) {
        super(simulationCoordinator, indexOffset++);
        this.actionKey = randomActionKey;
        this.nextActionKey = nextActionKey;
        this.element = element;
    }

    @Override
    public ActionKey getKey() {
        return actionKey;
    }

    @Override
    public boolean randomEventEnabled() {
        return false;
    }

    @Override
    public ActionKey getNextActionKey() {
        return nextActionKey;
    }

    @Override
    public void action() {
        log.debug("Starting random event: '{}' for: '{}'", actionKey, element);

        double delay = simulationGenerator.chisquare(1);
        simulationCoordinator.setRandomEventActionTime(delay);

        await(delay);
        log.debug("Random event: '{}' finished for: '{}'", actionKey, element);
        callNextAction(element);
        markExecuted();
    }

    public boolean isExecuted() {
        return executed;
    }

    public void markExecuted() {
        this.executed = true;
    }

    @Override
    public void terminateAction() {
        try {
            terminate();
        } catch (Exception e) {
            getLogger().error("Exception thrown during activity termination", e);
        }
    }

    @Override
    protected Logger getLogger() {
        return log;
    }
}
