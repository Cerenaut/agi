package io.agi.ef.demo.helloworld;

import io.agi.interprocess.coordinator.Coordinator;
import io.agi.ef.demo.MainDemo;
import io.agi.interprocess.coordinator.CoordinatorProxy;

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
        String host = "http://localhost";
        int port = 8080;
        CoordinatorProxy coordinatorProxy = CoordinatorProxy.createInstance( host, port );
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
    }

}