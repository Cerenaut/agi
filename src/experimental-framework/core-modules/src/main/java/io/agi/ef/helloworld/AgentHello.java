package io.agi.ef.helloworld;

import io.agi.ef.agent.Agent;

import javax.ws.rs.core.Response;

/**
 * Created by gideon on 30/07/15.
 */
public class AgentHello extends Agent {

    /**
     * @param agentContextPath is the word used in the context path i.e. no '/'
     * @throws Exception
     */
    public AgentHello( String agentContextPath ) {
        super( agentContextPath );
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
