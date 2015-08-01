package io.agi.ef.helloworld;

import io.agi.ef.agent.Agent;
import io.agi.ef.core.CommsMode;

import javax.ws.rs.core.Response;

/**
 * Created by gideon on 30/07/15.
 */
public class AgentHello extends Agent {

    public AgentHello( String agentContextPath, CommsMode commsMode ) {
        super( agentContextPath, commsMode );
    }

    @Override
    public Response step() {
        Response response = super.step();

        System.out.println( "Hello world, i'm at step " + getTime() );

        return response;
    }

    @Override
    public void state() {
    }
}
