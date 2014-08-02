package com.alerttown.app;

import java.util.HashMap;

import android.location.Location;

/**
 * @author zkhan
 * Class to event callbacks from service to activity
 *
 */
public interface EventInterface {

    void getCallback(HashMap<Long, Event> event);
    void locationCallback(Location location);
}
