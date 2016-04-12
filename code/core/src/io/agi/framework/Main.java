package io.agi.framework;

import io.agi.core.orm.ObjectMap;
import io.agi.core.util.PropertiesUtil;
import io.agi.framework.coordination.Coordination;
import io.agi.framework.coordination.http.HttpCoordination;
import io.agi.framework.coordination.monolithic.SingleProcessCoordination;
import io.agi.framework.persistence.Persistence;
import io.agi.framework.persistence.couchbase.CouchbasePersistence;
import io.agi.framework.persistence.jdbc.JdbcPersistence;
import io.agi.framework.persistence.models.ModelNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * NOTE: Run with the following virtual machine (VM) arguments:
 *
 * -Xms2000m  -Dlog4j.configurationFile=file:./log4j2.xml
 *
 * Created by dave on 6/03/16.
 */
public class Main {

    private static final Logger logger = LogManager.getLogger();

    public static final String PROPERTY_NODE_NAME = "node-name";
    public static final String PROPERTY_NODE_HOST = "node-host";
    public static final String PROPERTY_NODE_PORT = "node-port";

    public static final String PROPERTY_PERSISTENCE_TYPE = "persistence-type";
    public static final String PROPERTY_COORDINATION_TYPE = "coordination-type";

    public ModelNode _modelNode;


    public EntityFactory _ef;
    public ObjectMap _om;
    public Coordination _c;
    public Persistence _p;
    public Node _n;

    public Main() {

    }

    public void setup( String propertiesFile, ObjectMap om, EntityFactory ef ) {
        _ef = ef;

        if ( om == null ) {
            om = ObjectMap.GetInstance();
        }

        _om = om;

        // Create persistence & Node now so you can Create entities in code that are hosted and persisted on the Node.
        _p = createPersistence( propertiesFile );
        _c = createCoordination( propertiesFile );

        String nodeName = PropertiesUtil.get( propertiesFile, PROPERTY_NODE_NAME, "node-1" );
        String nodeHost = PropertiesUtil.get( propertiesFile, PROPERTY_NODE_HOST, "localhost" );
        int nodePort = Integer.valueOf( PropertiesUtil.get( propertiesFile, PROPERTY_NODE_PORT, "8491" ) );
        _modelNode = new ModelNode( nodeName, nodeHost, nodePort );

        // The persistent description of this Node
        Node node = Node.NodeInstance();
        node.setup( _om, _modelNode._name, _modelNode._host, _modelNode._port, ef, _c, _p );
        _n = node;

        ef.setNode( node );
    }

    public Coordination createCoordination( String propertiesFile ) {
        String type = PropertiesUtil.get( propertiesFile, PROPERTY_COORDINATION_TYPE, "http" );
        Coordination c = null;
        if ( type.equals( "http" ) ) {
            logger.info( "Distributed coordination." );
            c = new HttpCoordination();
        }
        else {
            logger.info( "Monolithic coordination." );
            c = new SingleProcessCoordination();
        }
        return c;
    }

    public Persistence createPersistence( String propertiesFile ) {
        String type = PropertiesUtil.get( propertiesFile, PROPERTY_PERSISTENCE_TYPE, "couchbase" );
        Persistence p = null;
        if ( type.equals( "couchbase" ) ) {
            logger.info( "Using Couchbase for persistence." );
            p = CouchbasePersistence.Create( propertiesFile );
        }
        else {
            logger.info( "Using JDBC (SQL) for persistence." );
            p = JdbcPersistence.Create( propertiesFile );
        }
        return p;
    }

    public void run() {
        try {
            if ( _c instanceof HttpCoordination ) {
                HttpCoordination c = ( HttpCoordination ) _c;
                c.setNode( _n );
                c.start();
            }
        }
        catch ( Exception e ) {
            logger.error( e.getStackTrace() );
            System.exit( -1 );
        }
    }
}