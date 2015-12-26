package io.agi.ef.http.node;

import io.agi.core.PropertiesUtil;
import io.agi.ef.EntityFactory;
import io.agi.ef.Persistence;
import io.agi.ef.entities.Clock;
import io.agi.ef.entities.experiment.*;
import io.agi.ef.entities.Relay;
import io.agi.ef.interprocess.coordinator.Coordinator;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import static org.apache.log4j.Level.WARN;

/**
 * Created by dave on 12/09/15.
 */
public class NodeMain {

    public static void main( String[] args ) {
        NodeMain m = new NodeMain();
        m.parse( args );
        m.run();
    }

    public void addEntityFactories() {
        EntityFactory.setFactory(Clock.ENTITY_TYPE, new io.agi.ef.entities.ClockFactory());
        EntityFactory.setFactory(Relay.ENTITY_TYPE, new io.agi.ef.entities.RelayFactory());
        EntityFactory.setFactory( Experiment.ENTITY_TYPE, new io.agi.ef.entities.experiment.ExperimentFactory() );
        EntityFactory.setFactory( Agent.ENTITY_TYPE, new io.agi.ef.entities.experiment.AgentFactory() );
        EntityFactory.setFactory( World.ENTITY_TYPE, new io.agi.ef.entities.experiment.WorldFactory() );
        EntityFactory.setFactory( Motor.ENTITY_TYPE, new io.agi.ef.entities.experiment.MotorFactory() );
        EntityFactory.setFactory( Sensor.ENTITY_TYPE, new io.agi.ef.entities.experiment.SensorFactory() );
        EntityFactory.setFactory( Processor.ENTITY_TYPE, new io.agi.ef.entities.experiment.ProcessorFactory() );
    }

    public void addEntities() {
        // TEST:
        // add some old_entities to the node:
        //Experiment ex = new Experiment();
        //ex.setup( "myExp" );
    }

    public void run() {

        // Configure logging
        BasicConfigurator.configure();
        Logger l = Logger.getRootLogger();

        // Now set its level. Normally you do not need to set the
        // level of a logger programmatically. This is usually done
        // in configuration files.
        l.setLevel( WARN );

        // Create core objects
        String nodeHost = Node.getLocalHostAddress();
        new Persistence( _dataHost, _dataPort ); //  - must do this before creating coordinator or Node
        new Node( _nodeName, nodeHost, _nodePort );

        // add entity factory - must do this before creating coordinator
        new EntityFactory();
        addEntityFactories();

        // if coordinator, add relay entity
        Coordinator c = new Coordinator();
        c.setup( _master ); // allow derivation

        // add predefined entities (defined in code)
        addEntities();

        // Run the completed Node
        System.out.println( "Starting Node Server...");
        try {
            NodeServer.run(_nodePort, _staticFiles);
        }
        catch( Exception e ) {
            e.printStackTrace();
        }
    }

    // Constants
    public static final String PROPERTY_STATIC_FILES_DIR = "staticFiles";

    // Members
    String _dataHost = "localhost";
    int _dataPort = 3000;
    String _nodeName = "scotch";
    int _nodePort = 8081;
    String _staticFiles = null;//"/home/dave/workspace/agi.io/agi/run/www";
    boolean _master = false;

    public NodeMain() {
    }

    public void parse( String[] args ) {

        // check number of arguments
        if( args.length < 5 ) {
            help();
        }
        if( args.length > 6 ) {
            help();
        }

        // are we to run coordinator?
        if( args.length == 6 ) {
            if( args[ 5 ].equalsIgnoreCase( "COORDINATOR" ) ) {
                System.out.println( "Node will be coordinator." );
                _master = true;
            }
            else {
                help();
            }
        }

        // get values
        _dataHost = args[ 0 ];
        _dataPort = getPort( args, 1 );
        _nodeName = args[ 2 ];
        _nodePort = getPort( args, 3 );

        String propertiesFile = args[ 4 ];

        _staticFiles = PropertiesUtil.get( propertiesFile, PROPERTY_STATIC_FILES_DIR, null );
    }

    public int getPort( String[] args, int index ) {
        try {
            String s = args[index];
            int port = Integer.valueOf( s );
            return port;
        }
        catch( Exception e ) {
            e.printStackTrace();
            help();
            return 0;
        }
    }

    public static void help() {
        System.out.println( "Arguments: DATA_HOST DATA_PORT NODE_NAME NODE_PORT PROPERTIES [COORDINATOR] " );
        System.out.println( "... where:" );
        System.out.println( " - DATA_HOST is the host for the Data service" );
        System.out.println( " - DATA_PORT is the port for the Data service" );
        System.out.println( " - NODE_NAME is the name for this service (node)" );
        System.out.println( " - NODE_PORT is the port you want this Node to serve data on" );
        System.out.println( " - PROPERTIES is the name of the configuration file for the service" );
        System.out.println( " - COORDINATOR is an optional argument indicating whether this Node is a Master node (i.e. it runs the Coordination services)." );
        System.exit( -1 );
    }
}
