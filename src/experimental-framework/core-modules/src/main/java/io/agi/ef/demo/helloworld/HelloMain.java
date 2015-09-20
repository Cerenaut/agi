package io.agi.ef.demo.helloworld;

import io.agi.ef.entities.experiment.Experiment;
import io.agi.ef.http.node.NodeMain;

/**
 * Created by gideon on 31/07/15.
 */
public class HelloMain extends NodeMain {

//    String masterHost = "localhost";
//    int masterPort = 8080;

    /**
     * Override this function to define the set of instantiable entities within this Node.
     * Can also defer instantiation of the derived classes by adding factories here.
     */
    public void addEntityFactories() {
        super.addEntityFactories();
    }

    /**
     * Override this function to hard-code the creation of entities within this Node.
     */
    public void addEntities() {
        // In this example, entities are created either by factory or by their parents.
        String experimentName = "exp";
        Experiment e = new Experiment();
        e.setup( experimentName, Experiment.ENTITY_TYPE, null, null );

        HelloWorld w = new HelloWorld();
        w.setup( "world", HelloWorld.ENTITY_TYPE, experimentName, null );

        HelloAgent a = new HelloAgent();
        a.setup( "agent", HelloAgent.ENTITY_TYPE, experimentName, null );
    }

    public static void main( String[] args ) {
        HelloMain m = new HelloMain();
        m.parse( args );
        m.run();
    }
//        Main m = new Main();
//
//        ArrayList< String > packageLogging = new ArrayList();
//        packageLogging.add( "io.agi.ef.interprocess.coordinator" );
//        packageLogging.add( "io.agi.ef.interprocess" );
//
//        Observable.from( packageLogging ).subscribe( new Action1<String>() {
//
//            @Override
//            public void call( String s ) {
//                Logger logger = Logger.getLogger( s );
//                // LOG this level to the log
//                logger.setLevel( Level.FINER );
//
//                ConsoleHandler handler = new ConsoleHandler();
//                // PUBLISH this level
//                handler.setLevel( Level.FINER );
//                logger.addHandler( handler );
//            }
//
//        } );
//
//        m.run( args );
//    }

//    @Override
//    public void runAgentOnly() throws Exception {
//
//        CoordinatorSlave.setup( "localhost", 8081, "slave1", masterHost, masterPort );
//        HelloAgent agent = new HelloAgent( "agent99" );
//
//        ArrayList<String> agents = new ArrayList();
//        agents.add( agent.name() );
//
//        Experiment exp = new Experiment( "exp1", "world1", agents );
//    }
//
//    @Override
//    public void runWorldOnly() throws Exception {
//
//        CoordinatorSlave.setup( "localhost", 8082, "slave1", masterHost, masterPort );
//        new HelloWorld( "world1" );
//    }
//
//    @Override
//    public void runCoordinatorOnly() throws Exception {
//        new CoordinatorMaster( masterPort );
//    }
//
//    @Override
//    public void runAll() throws Exception {
//        runCoordinatorOnly();
//        runWorldOnly();
//        runAgentOnly();
//    }

}