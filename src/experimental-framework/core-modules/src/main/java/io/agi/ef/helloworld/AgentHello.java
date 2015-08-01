package io.agi.ef.helloworld;

import io.agi.ef.agent.Agent;
import io.agi.ef.agent.actuators.Motor;
import io.agi.ef.agent.sensors.LightSensor;
import io.agi.ef.core.CommsMode;

import javax.ws.rs.core.Response;

/**
 * Created by gideon on 30/07/15.
 */
public class AgentHello extends Agent {

    public AgentHello( CommsMode commsMode, String agentContextPath ) {
        super( commsMode, agentContextPath );

        LightSensor sensor = new LightSensor();
        addSensor( sensor );

        Motor motor = new Motor();
        addActuator( motor );
    }

    public AgentHello( CommsMode commsMode ) {
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
