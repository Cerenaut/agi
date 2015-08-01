package io.agi.ef.helloworld;

import io.agi.ef.agent.actuators.LightActuator;
import io.agi.ef.core.CommsMode;
import io.agi.ef.world.World;

/**
 *
 * Simple test World that contains a single light source.
 *
 * Created by gideon on 1/08/15.
 */
public class HelloWorld extends World {

    public HelloWorld( CommsMode commsMode ) {
        super( commsMode );
        setup();
    }

    public HelloWorld( CommsMode commsMode, String contextPath ) {
        super( commsMode, contextPath );
        setup();
    }

    private void setup() {
        LightActuator lightActuator = new LightActuator();
        addActuator( lightActuator );
    }
}
