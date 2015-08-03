package io.agi.ef.demos.helloworld;

import io.agi.ef.core.network.entities.Agent;
import io.agi.ef.core.actuators.MotorActuator;
import io.agi.ef.core.sensors.LightSensor;
import io.agi.ef.core.CommsMode;

/**
 *
 * Simple test Agent that contains a light sensor and a motor actuator.
 *
 * Behaviour:
 *  - measure light source strength
 *  - modify motor speed based on light strength
 *
 * Created by gideon on 30/07/15.
 */
public class HelloAgent extends Agent {

    public HelloAgent() {
        super();
        setup();
    }

    public HelloAgent( String agentContextPath ) throws Exception {
        super( agentContextPath );
        setup();
    }

    void setup() {
        LightSensor sensor = new LightSensor();
        addSensor( sensor );

        MotorActuator motorActuator = new MotorActuator();
        addActuator( motorActuator );
    }


    @Override
    public void stepBody() {
        System.out.println( "Hello world, I'm at step " + getTime() );
    }

}
