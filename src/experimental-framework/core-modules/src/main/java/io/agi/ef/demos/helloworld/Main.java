package io.agi.ef.demos.helloworld;

import io.agi.ef.core.network.EndpointUtils;
import io.agi.ef.core.network.coordinator.Coordinator;
import io.agi.ef.core.CommsMode;
import io.agi.ef.demos.MainDemo;

/**
 * Created by gideon on 31/07/15.
 */
public class Main extends MainDemo {

    public static void main( String[] args ) throws Exception {
        Main m = new Main();
        m.run( args );
    }

    @Override
    public void runAgentOnly() throws Exception {
        // Create and run a 'hello world' Agent
        HelloAgent a = new HelloAgent( "agent" );
    }

    @Override
    public void runWorldOnly() throws Exception {
        HelloWorld w = new HelloWorld( "world" );
    }

    @Override
    public void runCoordinatorOnly() throws Exception {
        Coordinator c = new Coordinator();
    }

    @Override
    public void runAllLocally() throws Exception {
        Coordinator c = new Coordinator();

        HelloWorld w = new HelloWorld();

        // create and run a 'hello world' Agent
        HelloAgent a = new HelloAgent();

        c.addWorld( w );
        c.addAgent( a );
    }

}