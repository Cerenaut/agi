package io.agi.ef.demo.helloworld;

import io.agi.ef.experiment.Experiment;
import io.agi.interprocess.coordinator.CoordinatorMaster;
import io.agi.ef.demo.MainDemo;
import io.agi.interprocess.coordinator.CoordinatorSlave;
import io.agi.interprocess.coordinator.CoordinatorSlaveProxy;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.LogManager;

/**
 * Created by gideon on 31/07/15.
 */
public class Main extends MainDemo {

    String masterHost = "localhost";
    int masterPort = 8080;

    public static void main( String[] args ) throws Exception {
        Main m = new Main();
        m.run( args );
    }

    @Override
    public void runAgentOnly() throws Exception {

        CoordinatorSlave.setup( "slave1", 8081, masterHost, masterPort );
        HelloAgent agent = new HelloAgent( "agent99" );

        ArrayList<String> agents = new ArrayList();
        agents.add( agent.name() );

        Experiment exp = new Experiment( "world1", agents );
    }

    @Override
    public void runWorldOnly() throws Exception {

        CoordinatorSlave.setup( "slave2", 8082, masterHost, masterPort );
        new HelloWorld( "world1" );
    }

    @Override
    public void runCoordinatorOnly() throws Exception {
        new CoordinatorMaster( masterPort );
    }

    @Override
    public void runAllLocally() throws Exception {
        runCoordinatorOnly();
        runWorldOnly();
        runAgentOnly();
    }

}