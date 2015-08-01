package io.agi.ef.helloworld;

import io.agi.ef.agent.Agent;
import io.agi.ef.agent.actuators.MotorActuator;
import io.agi.ef.agent.sensors.LightSensor;
import io.agi.ef.core.CommsMode;

import javax.ws.rs.core.Response;

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

    public HelloAgent( CommsMode commsMode, String agentContextPath ) {
        super( commsMode, agentContextPath );

        LightSensor sensor = new LightSensor();
        addSensor( sensor );

        MotorActuator motorActuator = new MotorActuator();
        addActuator( motorActuator );
    }

    public HelloAgent( CommsMode commsMode ) {
        super( commsMode );
    }

    @Override
    public Response step() {
        Response response = super.step();

        System.out.println( "Hello world, I'm at step " + getTime() );

        return response;
    }

    @Override
    public void state() {
    }
}
