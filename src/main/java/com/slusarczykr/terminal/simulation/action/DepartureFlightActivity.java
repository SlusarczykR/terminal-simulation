package com.slusarczykr.terminal.simulation.action;

import com.slusarczykr.terminal.simulation.model.Flight;
import deskit.SimActivity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DepartureFlightActivity extends SimActivity {

    private static final Logger log = LogManager.getLogger(DepartureFlightActivity.class);

    @Override
    public void action() {
        log.debug("Starting departure flight activity...");
        Flight flight = (Flight) getParentSimObject();

        log.debug("Departing flight: '{}'", flight.getId());
        double delay = flight.getDepartureTime();
        waitDuration(delay);

        log.debug("Flight: '{}' departed from the airport", flight.getId());
    }
}
