package io.agi.ef.demo.helloworld;

import io.agi.core.ef.Experiment;
import io.agi.ef.experiment.Exp;
import io.agi.interprocess.coordinator.CoordinatorMaster;
import io.agi.ef.demo.MainDemo;
import io.agi.interprocess.coordinator.CoordinatorSlave;
import sun.management.resources.agent;

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

        CoordinatorSlave.setup( "slave1", 8080, host, port );
        CoordinatorSlave coordinator = CoordinatorSlave.getInstance();

        HelloAgent agent = new HelloAgent();

        Exp experiment = new Exp( coordinator, agent.getName() );
    }

    @Override
    public void runWorldOnly() throws Exception {
        HelloWorld w = new HelloWorld( "world" );
    }

    @Override
    public void runCoordinatorOnly() throws Exception {
        CoordinatorMaster c = new CoordinatorMaster();
    }

    @Override
    public void runAllLocally() throws Exception {
        CoordinatorMaster c = new CoordinatorMaster();

        HelloWorld w = new HelloWorld();

        // create and run a 'hello world' Agent
        HelloAgent a = new HelloAgent();
    }

}