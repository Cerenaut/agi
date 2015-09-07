package io.agi.ef.demo.helloworld;

import io.agi.ef.experiment.Experiment;
import io.agi.ef.interprocess.coordinator.CoordinatorMaster;
import io.agi.ef.demo.MainDemo;
import io.agi.ef.interprocess.coordinator.CoordinatorSlave;
import rx.Observable;
import rx.functions.Action1;

import java.util.ArrayList;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by gideon on 31/07/15.
 */
public class Main extends MainDemo {

    String masterHost = "localhost";
    int masterPort = 8080;

    public static void main( String[] args ) throws Exception {
        Main m = new Main();

        ArrayList< String > packageLogging = new ArrayList();
        packageLogging.add( "io.agi.ef.interprocess.coordinator" );
        packageLogging.add( "io.agi.ef.interprocess" );

        Observable.from( packageLogging ).subscribe( new Action1<String>() {

            @Override
            public void call( String s ) {
                Logger logger = Logger.getLogger( s );
                // LOG this level to the log
                logger.setLevel( Level.FINER );

                ConsoleHandler handler = new ConsoleHandler();
                // PUBLISH this level
                handler.setLevel( Level.FINER );
                logger.addHandler( handler );
            }

        } );

        m.run( args );
    }

    @Override
    public void runAgentOnly() throws Exception {

        CoordinatorSlave.setup( "localhost", 8081, "slave1", masterHost, masterPort );
        HelloAgent agent = new HelloAgent( "agent99" );

        ArrayList<String> agents = new ArrayList();
        agents.add( agent.name() );

        Experiment exp = new Experiment( "exp1", "world1", agents );
    }

    @Override
    public void runWorldOnly() throws Exception {

        CoordinatorSlave.setup( "localhost", 8082, "slave1", masterHost, masterPort );
        new HelloWorld( "world1" );
    }

    @Override
    public void runCoordinatorOnly() throws Exception {
        new CoordinatorMaster( masterPort );
    }

    @Override
    public void runAll() throws Exception {
        runCoordinatorOnly();
        runWorldOnly();
        runAgentOnly();
    }

}