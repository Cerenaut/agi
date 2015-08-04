package io.agi.ef.demos.helloworld;

import io.agi.ef.core.actuators.LightActuator;
import io.agi.ef.core.CommsMode;
import io.agi.ef.core.network.entities.World;

/**
 *
 * Simple test World that contains a single light source.
 *
 * Created by gideon on 1/08/15.
 */
public class HelloWorld extends World {

    public HelloWorld() {
        super();
        setup();
    }

    public HelloWorld( String contextPath, int port ) throws Exception {
        super( contextPath, port );
        setup();
    }

    private void setup() {
        LightActuator lightActuator = new LightActuator();
        addActuator( lightActuator );
    }
}
