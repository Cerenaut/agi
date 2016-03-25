package io.agi.framework;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.agi.core.orm.Keys;
import io.agi.core.orm.ObjectMap;
import io.agi.core.util.FileUtil;
import io.agi.core.util.PropertiesUtil;
import io.agi.framework.Persistence.Persistence;
import io.agi.framework.http.HttpCoordination;
import io.agi.framework.monolithic.SingleProcessCoordination;
import io.agi.framework.serialization.ModelDataReference;
import io.agi.framework.serialization.ModelEntity;
import io.agi.framework.Persistence.sql.JdbcPersistence;
import io.agi.framework.serialization.ModelPropertySet;

import java.lang.reflect.Type;
import java.util.List;

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

        if ( om == null ) {
            om = ObjectMap.GetInstance();
        }

        _om = om;

        // Create persistence & Node now so you can Create entities in code that are hosted and persisted on the Node.
        _p = createPersistence(propertiesFile);
        _c = createCoordination(propertiesFile);

        _nodeName = PropertiesUtil.get( propertiesFile, PROPERTY_NODE_NAME, "node-1" );
        _nodeHost = PropertiesUtil.get( propertiesFile, PROPERTY_NODE_HOST, "localhost" );
        _nodePort = Integer.valueOf( PropertiesUtil.get( propertiesFile, PROPERTY_NODE_PORT, "8491" ) );

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
        Gson gson = new Gson();

        try {
            String jsonEntity = FileUtil.readFile( file );

            Type listType = new TypeToken<List<ModelEntity>>() {
            }.getType();
            List<ModelEntity> entities = gson.fromJson( jsonEntity, listType );

            for ( ModelEntity modelEntity : entities ) {
                System.out.println( "Creating Entity of type: " + modelEntity.type + ", that is hosted at Node: " + modelEntity.node );
                _p.setEntity( modelEntity );
            }
        }
        catch ( Exception e ) {
            e.printStackTrace();
            System.exit( -1 );
        }
    }

    public void loadReferences( String file ) {
        Gson gson = new Gson();
        try {
            String jsonEntity = FileUtil.readFile( file );

            Type listType = new TypeToken<List<ModelDataReference>>() {
            }.getType();
            List<ModelDataReference> references = gson.fromJson( jsonEntity, listType );
            for ( ModelDataReference modelDataReference : references ) {
                System.out.println( "Creating data input reference for data: " + modelDataReference.dataKey + " with input data keys: " + modelDataReference.refKey );
                Entity.SetDataReference( _p, modelDataReference.dataKey, modelDataReference.refKey );
            }
        }
        catch ( Exception e ) {
            e.printStackTrace();
            System.exit( -1 );
        }
    }

    public void loadProperties( String file ) {
        Gson gson = new Gson();
        try {
            String jsonEntity = FileUtil.readFile( file );

            Type listType = new TypeToken<List<ModelPropertySet>>() {
            }.getType();
            List<ModelPropertySet> modelProperties = gson.fromJson( jsonEntity, listType );

            for ( ModelPropertySet modelPropertySet : modelProperties ) {

                System.out.println( "Creating property for entity: " + modelPropertySet.entity );

                for ( String keySuffix : modelPropertySet.properties.keySet() ) {
                    String value = modelPropertySet.properties.get( keySuffix );

                    System.out.println( "\tKeySuffix: " + keySuffix + ", Value: " + value);

                    String key = Keys.concatenate( modelPropertySet.entity, keySuffix );
                    _p.setPropertyString( key, value );
                }

            }
        }
        catch ( Exception e ) {
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
        catch ( Exception e ) {
            e.printStackTrace();
            System.exit( -1 );
        }
    }
}
