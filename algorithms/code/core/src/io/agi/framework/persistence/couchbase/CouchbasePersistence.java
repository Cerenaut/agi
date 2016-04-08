package io.agi.framework.persistence.couchbase;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.bucket.BucketManager;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.view.*;
import io.agi.core.util.PropertiesUtil;
import io.agi.framework.persistence.Persistence;
import io.agi.framework.persistence.models.ModelData;
import io.agi.framework.persistence.models.ModelEntity;
import io.agi.framework.persistence.models.ModelNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Note: http://docs.couchbase.com/developer/java-2.0/querying-n1ql.html
 * Created by dave on 14/03/16.
 */
public class CouchbasePersistence implements Persistence { //PropertyStringAccess {

    public static final String PROPERTY_CLUSTER = "couchbase-cluster";
    public static final String PROPERTY_BUCKET = "couchbase-bucket";
    public static final String PROPERTY_PASSWORD = "couchbase-password";

    public static final String KEY_PREFIX_NODE = "N";
    public static final String KEY_PREFIX_DATA = "D";
    public static final String KEY_PREFIX_ENTITY = "E";
//    public static final String KEY_PREFIX_PROPERTY = "P";

    public static final String PROPERTY_DOCUMENT_TYPE = "document_type";

    public static final String PROPERTY_NODE_HOST = "host";
    public static final String PROPERTY_NODE_PORT = "port";

    public static final String PROPERTY_ENTITY_NODE = "node";
    public static final String PROPERTY_ENTITY_PARENT = "parent";
    public static final String PROPERTY_ENTITY_TYPE = "type";
    public static final String PROPERTY_ENTITY_CONFIG = "config";

    public static final String PROPERTY_DATA_ELEMENTS = "elements";
    public static final String PROPERTY_DATA_REF_KEY = "ref_key";
    public static final String PROPERTY_DATA_SIZES = "sizes";

    public static final String PROPERTY_KEY = "name";
//    public static final String PROPERTY_PROPERTY_VALUE = "value";

    public static final long QUERY_TIMEOUT = 999999999;

//    PropertyConverter _propertyConverter = null;

    Cluster _c;
    Bucket _b;

    public static CouchbasePersistence Create( String propertiesFile ) {
        String cluster = PropertiesUtil.get( propertiesFile, PROPERTY_CLUSTER, "localhost" );
        String bucket = PropertiesUtil.get( propertiesFile, PROPERTY_BUCKET, "default" );
        String password = PropertiesUtil.get( propertiesFile, PROPERTY_PASSWORD, "password" );

        CouchbasePersistence p = new CouchbasePersistence();

        p.setup( cluster, bucket, password );

        p.createViews();

        return p;
    }

    public CouchbasePersistence() {
    }

    public void setup( String cluster, String bucket, String password ) {
        System.setProperty( "com.couchbase.queryEnabled", "true" );

        // TODO: Figure out which of these unreasonably large timeouts are actually sensible or necessary.
        System.setProperty( "com.couchbase.maxRequestLifetime", String.valueOf( QUERY_TIMEOUT ) );
        System.setProperty( "com.couchbase.queryTimeout", String.valueOf( QUERY_TIMEOUT ) );
        System.setProperty( "com.couchbase.socketConnectTimeout", String.valueOf( QUERY_TIMEOUT ) );
        System.setProperty( "com.couchbase.keepAliveInterval", String.valueOf( QUERY_TIMEOUT ) );
        System.setProperty( "com.couchbase.autoreleaseAfter", String.valueOf( QUERY_TIMEOUT ) );
        System.setProperty( "com.couchbase.kvTimeout", String.valueOf( QUERY_TIMEOUT ) );
        System.setProperty( "com.couchbase.connectTimeout", String.valueOf( QUERY_TIMEOUT ) );
        System.setProperty( "com.couchbase.disconnectTimeout", String.valueOf( QUERY_TIMEOUT ) );

        _c = CouchbaseCluster.create( cluster );

        _b = _c.openBucket( bucket, password );

//        _propertyConverter = new PropertyConverter( this );
    }

    public void createViews() {
        try {
            // http://developer.couchbase.com/documentation/server/4.0/sdks/java-2.2/managing-views.html
            BucketManager bucketManager = _b.bucketManager();

            // Initialize design document
            DesignDocument designDoc = DesignDocument.create(
                    "persistence",
                    Arrays.asList(
                            DefaultView.create( "all_nodes",
                                    "function (doc, meta) {" +
                                            "  if( doc.document_type == 'N' ) {" +
                                            "    emit(meta.id, [doc.name, doc.host, doc.port ] );" +
                                            "  } }" ),
                            DefaultView.create( "all_entities",
                                    "function (doc, meta) {" +
                                            "  if( doc.document_type == 'E' ) {" +
                                            "    emit(meta.id, [doc.name, doc.node, doc.type, doc.parent, doc.config ] );" +
                                            "  } }" ),
                            DefaultView.create( "child_entities",
                                    "function (doc, meta) { " +
                                            "  if( doc.document_type == 'E' && doc.parent ) {" +
                                            "    emit(doc.parent, doc.name );" +
                                            "  } }" ),
//                            DefaultView.create( "all_properties",
//                                    "function (doc, meta) {" +
//                                            "  if( doc.document_type == 'P' && doc.name ) {" +
//                                            "    emit( doc.name, [doc.value ] );" +
//                                            "  } }" ),
                            DefaultView.create( "all_docs",
                                    "function (doc, meta) { " +
                                            "  emit( meta.id, doc );" +
                                            "  }" )
                    )
            );

            // Insert design document into the bucket
            bucketManager.insertDesignDocument( designDoc );

            System.out.println( "NOTE: CouchBase views created." );
        }
        catch ( com.couchbase.client.java.error.DesignDocumentAlreadyExistsException e ) {
            System.out.println( "NOTE: CouchBase views already exist." );
//            e.printStackTrace();
        }
    }

    public void stop() {
        if ( _c != null ) {
            _c.disconnect();
            _c = null;
        }
    }

    /**
     * Generates Couchbase keys - since Couchbase is a Document Database, we need to ensure keys are unique and also
     * that the type of object (document) is indicated in the key.
     *
     * @param prefix
     * @param suffix
     * @return
     */
    public static String GetKey( String prefix, String suffix ) {
        return prefix + ":" + suffix;
    }

    public Collection< ModelNode > getNodes() {
        // http://docs.couchbase.com/admin/admin/Views/views-writing.html
        // http://docs.couchbase.com/developer/java-2.1/tutorial.html
        ArrayList< ModelNode > models = new ArrayList< ModelNode >();
        ViewResult result = _b.query(
                ViewQuery
                        .from( "persistence", "all_nodes" )
                        .descending()
        );
        List< ViewRow > l = result.allRows();
        for ( ViewRow r : l ) {
            JsonObject jo = r.document().content();
            String key = jo.getString( PROPERTY_KEY );
            String host = jo.getString( PROPERTY_NODE_HOST );
            int port = jo.getInt( PROPERTY_NODE_PORT );

            ModelNode m = new ModelNode( key, host, port );

            models.add( m );
        }
        return models;
    }

    public Collection< ModelEntity > getEntities() {
        ArrayList< ModelEntity > models = new ArrayList< ModelEntity >();
        ViewResult result = _b.query(
                ViewQuery
                        .from( "persistence", "all_entities" )
                        .descending()
        );
        List< ViewRow > l = result.allRows();
        for ( ViewRow r : l ) {
            JsonObject jo = r.document().content();
            String key = jo.getString( PROPERTY_KEY );
            String type = jo.getString( PROPERTY_ENTITY_TYPE );
            String node = jo.getString( PROPERTY_ENTITY_NODE );
            String parent = jo.getString( PROPERTY_ENTITY_PARENT );
            String config = jo.getString( PROPERTY_ENTITY_PARENT );
            ModelEntity m = new ModelEntity( key, type, node, parent, config );
            models.add( m );
        }
        return models;
    }

    public Collection< String > getChildEntities( String parent ) {
        ArrayList< String > models = new ArrayList< String >();
        ViewResult result = _b.query(
                ViewQuery
                        .from( "persistence", "child_entities" )
                        .key( parent )
                        .inclusiveEnd( true )
                        .descending()
        );
        List< ViewRow > l = result.allRows();
        for ( ViewRow r : l ) {
            JsonObject jo = r.document().content();
            String key = jo.getString( PROPERTY_KEY );
            models.add( key );
        }
        return models;
    }

//    public Map< String, String > getConfig( String filter ) {
//        // https://forums.couchbase.com/t/wildcard-search-using-couchbase-views/3545
//        HashMap< String, String > hm = new HashMap< String, String >();
//        String filter1 = filter;
//        String filter2 = filter + "\\uefff";
//        ViewResult result = _b.query(
//                ViewQuery
//                        .from( "persistence", "all_properties" )
//                        .startKey( filter1 )
//                        .endKey( filter2 )
//                        .inclusiveEnd( true )
//        );
//        List< ViewRow > l = result.allRows();
//        for ( ViewRow r : l ) {
//            JsonObject jo = r.document().content();
//            String key = jo.getString( PROPERTY_KEY );
//            String value = jo.getString( PROPERTY_PROPERTY_VALUE );
//            hm.put( key, value );
//        }
//
//        return hm;
//    }

    // Nodes
    public void setNode( ModelNode m ) {
        String key = GetKey( KEY_PREFIX_NODE, m._name );
        JsonObject jo = JsonObject.empty()
                .put( PROPERTY_DOCUMENT_TYPE, KEY_PREFIX_NODE )
                .put( PROPERTY_KEY, m._name )
                .put( PROPERTY_NODE_HOST, m._host )
                .put( PROPERTY_NODE_PORT, m._port );
        JsonDocument response = upsert( key, jo );
    }

    public ModelNode getNode( String nodeKey ) {
        String key = GetKey( KEY_PREFIX_NODE, nodeKey );
        JsonDocument loaded = _b.get( key );
        if ( loaded == null ) {
            return null;
        }
        else {
            String host = loaded.content().getString( PROPERTY_NODE_HOST );
            int port = loaded.content().getInt( PROPERTY_NODE_PORT );
            ModelNode jn = new ModelNode( nodeKey, host, Integer.valueOf( port ) );
            return jn;
        }
    }

    public void removeNode( String nodeName ) {
        String key = GetKey( KEY_PREFIX_NODE, nodeName );
        remove( key );
    }

    public void remove( String key ) {
        _b.remove( key );
    }

    public JsonDocument upsert( String key, JsonObject jo ) {
        JsonDocument doc = JsonDocument.create( key, jo );
        JsonDocument response = _b.upsert( doc );
        return response;
    }


    // Entities
    public void persistEntity( ModelEntity m ) {
        String key = GetKey( KEY_PREFIX_ENTITY, m.name );
        JsonObject jo = JsonObject.empty()
                .put( PROPERTY_DOCUMENT_TYPE, KEY_PREFIX_ENTITY )
                .put( PROPERTY_KEY, m.name )
                .put( PROPERTY_ENTITY_NODE, m.node )
                .put( PROPERTY_ENTITY_PARENT, m.parent )
                .put( PROPERTY_ENTITY_TYPE, m.type )
                .put( PROPERTY_ENTITY_CONFIG, m.config );
        JsonDocument response = upsert( key, jo );
    }

    public ModelEntity fetchEntity( String entityKey ) {
        String key = GetKey( KEY_PREFIX_ENTITY, entityKey );
        JsonDocument loaded = _b.get( key );
        if ( loaded == null ) {
            return null;
        }
        else {
            String type = loaded.content().getString( PROPERTY_ENTITY_TYPE );
            String node = loaded.content().getString( PROPERTY_ENTITY_NODE );
            String parent = loaded.content().getString( PROPERTY_ENTITY_PARENT );
            String config = loaded.content().getString( PROPERTY_ENTITY_CONFIG );
            ModelEntity m = new ModelEntity( key, type, node, parent, config );
            return m;
        }
    }

    public void removeEntity( String entityKey ) {
        String key = GetKey( KEY_PREFIX_ENTITY, entityKey );
        remove( key );
    }

    // Data
    public void setData( ModelData m ) {
        String key = GetKey( KEY_PREFIX_DATA, m._name );
        JsonObject jo = JsonObject.empty()
                .put( PROPERTY_DOCUMENT_TYPE, KEY_PREFIX_DATA )
                .put( PROPERTY_KEY, m._name )
                .put( PROPERTY_DATA_ELEMENTS, m._elements )
                .put( PROPERTY_DATA_REF_KEY, m._refKeys )
                .put( PROPERTY_DATA_SIZES, m._sizes );
        JsonDocument response = upsert( key, jo );
    }

    public ModelData getData( String dataKey ) {
        String key = GetKey( KEY_PREFIX_DATA, dataKey );
        JsonDocument loaded = _b.get( key );
        if ( loaded == null ) {
            return null;
        }
        else {
            String sizes = loaded.content().getString( PROPERTY_DATA_SIZES );
            String elements = loaded.content().getString( PROPERTY_DATA_ELEMENTS );
            String refKey = loaded.content().getString( PROPERTY_DATA_REF_KEY );
            ModelData m = new ModelData( dataKey, refKey, sizes, elements );
            return m;
        }
    }

    public void removeData( String dataKey ) {
        String key = GetKey( KEY_PREFIX_DATA, dataKey );
        remove( key );
    }

//    public String getPropertyString( String propertyKey, String defaultValue ) {
//        String key = GetKey( KEY_PREFIX_PROPERTY, propertyKey );
//        JsonDocument loaded = _b.get( key );
//        if ( loaded == null ) {
//            return null;
//        }
//        else {
//            String value = loaded.content().getString( PROPERTY_PROPERTY_VALUE );
//            return value;
//        }
//    }
//
//    public void setPropertyString( String propertyKey, String value ) {
//        String key = GetKey( KEY_PREFIX_PROPERTY, propertyKey );
//        JsonObject jo = JsonObject.empty()
//                .put( PROPERTY_KEY, propertyKey )
//                .put( PROPERTY_DOCUMENT_TYPE, KEY_PREFIX_PROPERTY )
//                .put( PROPERTY_PROPERTY_VALUE, value );
//        JsonDocument response = upsert( key, jo );
//    }
}
