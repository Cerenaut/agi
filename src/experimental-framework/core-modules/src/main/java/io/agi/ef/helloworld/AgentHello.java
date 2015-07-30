package io.agi.ef.helloworld;

import io.agi.ef.agent.Agent;

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
    public void step() {
        System.out.println( "hello world, i'm at step " + _sTime );
    }

    @Override
    public void state() {

    }
}
