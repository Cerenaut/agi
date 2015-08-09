package io.agi.ef.core;

import io.agi.core.ef.StatefulThread;

import java.util.HashMap;

/**
 *
 * < Sensor|Actuator, val >
 *
 * Created by gideon on 2/08/15.
 */
public class UniversalState {

    public HashMap< String, String > _state = new HashMap<>(  );

    public void reset() {
        _state = new HashMap<>(  );
    }
}
