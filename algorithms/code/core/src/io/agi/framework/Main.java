package io.agi.framework;

import io.agi.core.orm.ObjectMap;
import io.agi.core.util.FileUtil;
import io.agi.core.util.PropertiesUtil;
import io.agi.framework.coordination.Coordination;
import io.agi.framework.coordination.http.HttpCoordination;
import io.agi.framework.coordination.monolithic.SingleProcessCoordination;
import io.agi.framework.persistence.Persistence;
import io.agi.framework.persistence.couchbase.CouchbasePersistence;
import io.agi.framework.serialization.ModelEntity;
import io.agi.framework.persistence.jdbc.JdbcPersistence;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by dave on 6/03/16.
 */
public class Main {

    public static final String PROPERTY_NODE_NAME = "node-name";
    public static final String PROPERTY_NODE_HOST = "node-host";
    public static final String PROPERTY_NODE_PORT = "node-port";

    public static final String PROPERTY_PERSISTENCE_TYPE = "persistence-type";
    public static final String PROPERTY_COORDINATION_TYPE = "coordination-type";

    public String _databaseUser;
    public String _databasePassword;
    public String _databaseUrl;
    public String _databaseDriverClass;

    public String _nodeName;
    public String _nodeHost;
    public int _nodePort = 0;

    public EntityFactory _ef;
    public ObjectMap _om;
    public Coordination _c;
    public Persistence _p;
    public Node _n;

    public Main() {

    }

    public void setup( String propertiesFile, ObjectMap om, EntityFactory ef ) {
        _ef = ef;

        if( om == null ) {
            om = ObjectMap.GetInstance();
        }

        _om = om;

        // Create persistence & Node now so you can Create entities in code that are hosted and persisted on the Node.
        _p = createPersistence(propertiesFile);
        _c = createCoordination(propertiesFile);

        _nodeName = PropertiesUtil.get(propertiesFile, PROPERTY_NODE_NAME, "node-1");
        _nodeHost = PropertiesUtil.get(propertiesFile, PROPERTY_NODE_HOST, "localhost");
        _nodePort = Integer.valueOf(PropertiesUtil.get(propertiesFile, PROPERTY_NODE_PORT, "8491"));

        // The persistent description of this Node
        Node n = new Node();
        n.setup( _om, _nodeName, _nodeHost, _nodePort, ef, _c, _p );
        _n = n;

        ef.setNode(n);
    }

    public Coordination createCoordination( String propertiesFile ) {
        String type = PropertiesUtil.get(propertiesFile, PROPERTY_COORDINATION_TYPE, "http" );
        Coordination c = null;
        if( type.equals( "http" ) ) {
            System.out.println( "Distributed coordination." );
            c = new HttpCoordination();
        }
        else {
            System.out.println( "Monolithic coordination." );
            c = new SingleProcessCoordination();
        }
        return c;
    }

    public Persistence createPersistence( String propertiesFile) {
        String type = PropertiesUtil.get(propertiesFile, PROPERTY_PERSISTENCE_TYPE, "couchbase" );
        Persistence p = null;
        if( type.equals( "couchbase" ) ) {
            System.out.println( "Using Couchbase for persistence." );
            p = CouchbasePersistence.Create(propertiesFile);
        }
        else {
            System.out.println( "Using JDBC (SQL) for persistence." );
            p = JdbcPersistence.Create( propertiesFile );
        }
        return p;
    }

    public void loadEntities( String file ) {
        try {
            String contents = FileUtil.readFile( file );
            JSONArray ja = new JSONArray( contents );

            for( int i = 0; i < ja.length(); ++i ) {
                JSONObject jo = ja.getJSONObject(i);

                String entityName = jo.getString("name");
                String entityType = jo.getString("type");
                String nodeName = jo.getString( "node" );
                String parentName = null;

                if( jo.has( "parent" ) ) {
                    parentName = jo.getString( "parent" ); // it's ok if this is null.
                }

                String thisNodeName = _n.getName();

//                if( !nodeName.equals( thisNodeName ) ) {
//                    System.out.println( "Ignoring Entity "+ entityName + " that is hosted at Node "+ nodeName );
//                    continue; // only Create Entities that are assigned to this Node.
//                }

                System.out.println( "Creating Entity "+ entityName + " that is hosted at Node "+ nodeName );
                ModelEntity je = new ModelEntity( entityName, entityType, nodeName, parentName );

                _p.setEntity( je );
            }
        }
        catch( Exception e ) {
            e.printStackTrace();
            System.exit( -1 );
        }
    }

    public void loadReferences( String file ) {
        try {
            String contents = FileUtil.readFile( file );
            JSONArray ja = new JSONArray( contents );

            for( int i = 0; i < ja.length(); ++i ) {
                JSONObject jo = ja.getJSONObject(i);

                String dataKey = jo.getString("dataKey");
                String refKeys = jo.getString( "refKeys" );

                System.out.println( "Creating data input reference for data: " + dataKey + " with input data keys: " + refKeys );

                Entity.SetDataReference(_p, dataKey, refKeys);
            }
        }
        catch( Exception e ) {
            e.printStackTrace();
            System.exit( -1 );
        }
    }

    public void run() {
        try {
            if( _c instanceof HttpCoordination ) {
                HttpCoordination c = (HttpCoordination)_c;
                c.setNode(_n);
                c.start();
            }
        }
        catch( Exception e ) {
            e.printStackTrace();
            System.exit( -1 );
        }
    }
}
