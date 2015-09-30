package io.agi.ef;

import io.agi.core.ObjectMap;
import io.agi.core.data.Data;
import io.agi.core.data.DataSize;
import io.agi.core.data.FloatArray2;
import io.agi.ef.http.EndpointUtil;
import io.agi.ef.http.RequestUtil;
import io.agi.ef.http.node.Node;
import io.agi.ef.interprocess.coordinator.Coordinator;
import io.agi.ef.persistenceClientApi.ApiException;
import io.agi.ef.persistenceClientApi.Configuration;
import io.agi.ef.persistenceClientApi.api.DataApi;
import io.agi.ef.persistenceClientApi.model.NodeModel;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Not necessarily a database.. the Persistence layer is intended to be an abstraction of whatever distributed filesystem
 * is in use for persistence of data. It might be a relational database or a NO-SQL database. It is expected to use a
 * HTTP interface however, and persist everything as Strings (in JSON format for structured data).
 *
 * Since it represents a remote web service, the Persistence layer is accessible as a singleton object from anywhere
 * in the program via the ObjectMap.
 *
 * TODO break up and refactor this large, unwieldy class.
 *
 * Created by dave on 11/09/15.
 */
public class Persistence {

    protected static Logger _logger = Logger.getLogger( Persistence.class.getClass().getPackage().getName() );

    public static final String KEY_PERSISTENCE = "persistence";

    // Database fields and constants
    public static final String TABLE_PROPERTIES = "properties";
    public static final String TABLE_NODES = "nodes";
    public static final String TABLE_ENTITIES = "entities";
    public static final String TABLE_ENTITIES_NODES = "entities_nodes";
    public static final String TABLE_ENTITY_TYPES = "entity_types";
    public static final String TABLE_ENTITIES_PARENTS_TYPES = "entities_parents_types";
    public static final String TABLE_DATA = "data";

    // String UIDs are used externally.
    public static final String FIELD_NAME = "name";
    public static final String FIELD_PARENT_NAME = "parent_name";
    public static final String FIELD_TYPE_NAME = "type_name";
    public static final String FIELD_NODE_NAME = "node_name";

    // Ids generally an implementation detail and not used externally.
    public static final String FIELD_ID = "id";
    public static final String FIELD_ID_NODE = "id_node";
    public static final String FIELD_ID_ENTITY = "id_entity";
    public static final String FIELD_ID_ENTITY_TYPE = "id_entity_type";
    public static final String FIELD_ID_ENTITY_PARENT = "id_entity_parent";

    // Data fields
    public static final String FIELD_SIZE = "size";
    public static final String FIELD_ELEMENTS = "elements";
    public static final String FIELD_VALUE = "value";


    // Variables
    protected String _host;
    protected int _port = -1;

    /**
     * The Persistence service is always identified by its host and port.
     * @param host
     * @param port
     */
    public Persistence( String host, int port ) {
        _host = host;
        _port = port;
        ObjectMap.Put( KEY_PERSISTENCE, this );

        setupApiClient();
    }

    void setupApiClient() {
        String basePath = getBaseUrl();
        Configuration.getDefaultApiClient().setBasePath( basePath );
    }

    /**
     * Convenience method to retrieve from ObjectMap.
     * @return
     */
    public static Persistence getInstance() {
        return (Persistence)ObjectMap.Get( KEY_PERSISTENCE );
    }

    /**
     * Get the names of all the entities at the specified Node.
     * @param nodeName
     * @return
     */
    public static HashSet< String > getEntitiesAt( String nodeName ) {
        String query = Persistence.TABLE_ENTITIES_NODES + "?name_node=eq."+nodeName;
        JSONArray ja = Persistence.getTableJson(query);
        return Persistence.getObjectValues( ja, Node.NAME );
    }

    /**
     * Add data to be owned by the specified entity
     *
     * @param entityName
     * @param dataName
     * @param data
     * @return
     */
    public static boolean addData( String entityName, String dataName, Data data ) {
        try {

            JSONObject jo = new JSONObject();

            jo.put( FIELD_NAME, dataName );

            String queryEntity = getQueryWhere(TABLE_ENTITIES, FIELD_NAME, entityName);
            JSONObject joEntity = getObject(queryEntity);
            int idEntity = joEntity.getInt(FIELD_ID);

            jo.put(FIELD_ID_ENTITY, idEntity);

            // set the data properties:
            String fieldSize = Serialization.DataSizeToString(data._d);
            String fieldElements = Serialization.FloatArrayToString(data);
            jo.put(FIELD_SIZE, fieldSize );
            jo.put(FIELD_ELEMENTS, fieldElements);

            boolean b = addTableRow( Persistence.TABLE_DATA, dataName, jo );

            if( b ) {
                Coordinator.getInstance().postDataEvent( dataName, Entity.EVENT_CHANGED ); // easier to listen for just one
            }
            return b;
        }
        catch( Exception e ) {
            return false;
        }
    }

    public static boolean setData( String name, Data data ) {
        try {
            JSONObject jo = new JSONObject();

            // set the data properties:
            String fieldSize = Serialization.DataSizeToString(data._d);
            String fieldElements = Serialization.FloatArrayToString(data);
            jo.put(FIELD_SIZE, fieldSize );
            jo.put(FIELD_ELEMENTS, fieldElements );

            updateTableRow(TABLE_DATA, name, jo);

            Coordinator.getInstance().postDataEvent( name, Entity.EVENT_CHANGED );

            return true;
        }
        catch( Exception e ) {
            return false;
        }
    }

    public static Data getData( String name ) {
        String query = getQueryWhere(TABLE_DATA, FIELD_NAME, name );

        JSONArray ja = Persistence.getTableJson(query);
        try {
            JSONObject jo = ja.getJSONObject( 0 );
            String size = jo.getString(FIELD_SIZE);
            String elements = jo.getString(FIELD_ELEMENTS);
            JSONObject joSize = new JSONObject( size );
            JSONObject joElements = new JSONObject( elements );

            FloatArray2 fa = Serialization.FloatArrayFromJson(joElements);
            DataSize ds = Serialization.DataSizeFromJson(joSize);

            Data d = new Data( 1 );
            d._d = ds;
            d._values = fa._values;

            return d;
        }
        catch( Exception e ) {
            //e.printStackTrace();
            return null;
        }
    }

    /**
     * I wanted to have just "set property" but it can't be done in less than 2 transactions.
     * In between we can't guarantee database state. So it's best if we separate and explicitly add properties when we
     * create them.
     *
     * @param name
     * @return
     */
    public static boolean addProperty( String name, String value ) {
        try {
            removeTableRow(Persistence.TABLE_PROPERTIES, name );

            JSONObject jo = new JSONObject();

            jo.put( FIELD_NAME, name);
            jo.put(FIELD_VALUE, value);

            return addTableRow( Persistence.TABLE_PROPERTIES, name, jo );
        }
        catch( Exception e ) {
            return false;
        }
    }

    public static boolean setProperty( String name, String value ) {
        try {
            JSONObject jo = new JSONObject();
            jo.put(FIELD_VALUE, value);
            updateTableRow(TABLE_PROPERTIES, name, jo);
            return true;
        }
        catch( Exception e ) {
            return false;
        }
    }

    public static String getProperty( String name ) {
        String query = getQueryWhere(TABLE_PROPERTIES, FIELD_NAME, name );
        String value = getObjectString(query, FIELD_VALUE);
        return value;
    }

    public static boolean addEntity(
            String name,
            String type,
            String parentName ) {
        try {

            JSONObject jo = new JSONObject();

            jo.put( FIELD_NAME, name);

            String nodeName = Node.getInstance().getName();
            String queryNode = getQueryWhere( TABLE_NODES, FIELD_NAME, nodeName );
            JSONObject joNode = getObject(queryNode);
            int idNode = joNode.getInt( FIELD_ID );

            jo.put( FIELD_ID_NODE, idNode );

            String queryType = getQueryWhere( TABLE_ENTITY_TYPES, FIELD_NAME, type );
            JSONObject joType = getObject(queryType);
            int idType = joType.getInt( FIELD_ID );

            jo.put( FIELD_ID_ENTITY_TYPE, idType );

            if( parentName != null ) {
                String queryParent = getQueryWhere(TABLE_ENTITIES, FIELD_NAME, parentName);
                JSONObject joParent = getObject(queryParent);
                int idParent = joParent.getInt(FIELD_ID);

                jo.put(FIELD_ID_ENTITY_PARENT, idParent);
            }

            return addTableRow( Persistence.TABLE_ENTITIES, name, jo );
        }
        catch( Exception e ) {
            return false;
        }
    }

    public static boolean addEntityType( String name ) {
        try {
            JSONObject jo = new JSONObject();
            jo.put( FIELD_NAME, name);
            return addTableRow( Persistence.TABLE_ENTITY_TYPES, name, jo );
        }
        catch( Exception e ) {
            //e.printStackTrace();
            return false;
        }
    }

    public static void removeEntity( String entityName ) {
        removeTableRow(Persistence.TABLE_ENTITIES, entityName);
    }

    public static HashSet< String > getEntitiesOfType( String entityType ) {
        String queryType = getQueryWhere(TABLE_ENTITY_TYPES, FIELD_NAME, entityType);
        try {
            JSONObject joType = getObject(queryType);
            int idType = joType.getInt( FIELD_ID );
            String query = getQueryWhere(TABLE_ENTITIES_NODES, FIELD_ID_ENTITY_TYPE, String.valueOf( idType ) );
            String s = getTable(query);
            JSONArray ja = new JSONArray(s);
            HashSet<String> hs = getObjectValues(ja, FIELD_NAME);
            return hs;
        }
        catch( JSONException e ) {
            return new HashSet<>();
        }
    }

    public static HashMap< String, String > getChildEntityTypes( String parentEntityName ) {
        // get all the children where parent-name = arg
        String query = getQueryWhere( TABLE_ENTITIES_PARENTS_TYPES, FIELD_PARENT_NAME, parentEntityName );
        JSONArray ja = Persistence.getTableJson(query);

        try {
            HashMap< String, String > childEntityTypes = new HashMap<>();

            for (int i = 0; i < ja.length(); ++i) {
                JSONObject jo = ja.getJSONObject(i);

                String childName = jo.getString(FIELD_NAME);
                String childType = jo.getString( FIELD_TYPE_NAME );

                childEntityTypes.put( childName, childType );
            }

            return childEntityTypes;
        }
        catch( JSONException e ) {
            return new HashMap<>();
        }
    }

    public static String getNodeOfEntity( String entityName ) {
        String query = getQueryWhere( TABLE_ENTITIES_NODES, FIELD_NAME, entityName);
        String s = getTable(query);
        try {
            JSONArray ja = new JSONArray(s);
            JSONObject jo = ja.getJSONObject(0); // may throw exception if no entity found
            String nodeName = jo.getString(FIELD_NODE_NAME);
            return nodeName;
        }
        catch( JSONException e ) {
            return null; // not found or not parseable
        }
    }

    public static HashSet<String> getEntitiesAtNode( String nodeName ) {
        // find all old_entities at this node
        String query = getQueryWhere(TABLE_ENTITIES_NODES, FIELD_NODE_NAME, nodeName);
        String s = getTable(query);
        try {
            JSONArray ja = new JSONArray(s);
            HashSet<String> hs = getObjectValues(ja, FIELD_NAME);
            return hs;
        }
        catch( JSONException e ) {
            return new HashSet<>();
        }
    }

    public static HashSet<String> getNodes_old() {
        JSONArray ja = Persistence.getTableJson(Persistence.TABLE_NODES);
        return Persistence.getObjectValues( ja, Node.NAME );
    }

    public static JSONObject getNode_old( String nodeName ) {
        String query = getQueryWhere(Persistence.TABLE_NODES, FIELD_NAME, nodeName);
        return getObject(query);
    }

    public static List< NodeModel > getNodes() throws ApiException {
        DataApi dataApi = new DataApi(  );
        return dataApi.nodesGet( null, null, null, null );
    }

    /**
     * Return the first node matching this nodeName
     */
    public static NodeModel getNode( String nodeName ) throws ApiException {

        List< NodeModel > nodes = new DataApi().nodesGet( null, nodeName, null, null );
        return (nodes != null) ? nodes.get( 0 ) : null;
    }


    public static boolean addNode(
            String name,
            String host,
            int port )
    {
        NodeModel node = new NodeModel();
        node.setName( name );
        node.setHost( host );
        node.setPort( port );

        DataApi dataApi = new DataApi();

        try {
            dataApi.nodesPost( node );
            return true;
        }
        catch ( ApiException e ) {
            _logger.log( Level.WARNING, e.toString() );
            return false;
        }
    }

    public static boolean addNode_old(
            String name,
            String host,
            int port ) {
        try {
            JSONObject jo = new JSONObject();

            jo.put(Node.NAME, name);
            jo.put(Node.HOST, host);
            jo.put(Node.PORT, port);

            return addTableRow( Persistence.TABLE_NODES, name, jo );
        }
        catch( JSONException e ) {
            return false;
        }
    }

    public static void removeNode( String nodeName ) {
        removeTableRow(Persistence.TABLE_NODES, nodeName);
    }

    /**
     * Add a row to a table with a unique name.
     *
     * @param table
     * @param name
     * @param jo
     * @return
     */
    public static boolean addTableRow(
            String table,
            String name,
            JSONObject jo ) {

        // enforce name uniqueness
        removeTableRow(table, name);

        String query = table;
        String request = getBaseUrl() + query;
        String body = jo.toString();
        String result = RequestUtil.postSync(request, body);
        if( ( result != null ) && ( result.trim().length() > 0 ) ) {
            System.out.println("Persistence::addTableRow() returned: " + result);
        }
        return true;
    }

    public static void updateTableRow(
            String table,
            String name,
            JSONObject values ) {
        //String query = table + "?name=eq."+name;
        String query = getQueryWhere( table, FIELD_NAME, name );
        String request = getBaseUrl() + query;
        String body = values.toString();
        String result = RequestUtil.patchSync(request,body);
        if( ( result != null ) && ( result.trim().length() > 0 ) ) {
            System.out.println("Persistence::updateTableRow() returned: " + result);
        }
    }

    public static void removeTableRow( String table, String name ) {
        //String query = table + "?name=eq."+name;
        String query = getQueryWhere(table, FIELD_NAME, name);
        String request = getBaseUrl() + query;
        String result = RequestUtil.deleteSync(request);
        if( ( result != null ) && ( result.trim().length() > 0 ) ) {
            System.out.println("Persistence::removeTableRow() returned: " + result);
        }
    }

    public static JSONObject getObject( String query ) {
        //String query = Persistence.TABLE_NODES + "?name=eq."+nodeName;
        JSONArray ja = Persistence.getTableJson(query);
        try {
            return ja.getJSONObject(0);
        }
        catch( Exception e ) {
            //e.printStackTrace();
            return null;
        }
    }

    public static String getObjectString( String query, String field ) {
        //String query = Persistence.TABLE_NODES + "?name=eq."+nodeName;
        JSONArray ja = Persistence.getTableJson(query);
        try {
            JSONObject jo = ja.getJSONObject( 0 );
            String value = jo.getString(field);
            return value;
        }
        catch( Exception e ) {
            //e.printStackTrace();
            return null;
        }
    }

    public static HashSet< String > getObjectValues( JSONArray ja, String key ) {
        HashSet<String> values = new HashSet<>();

        for (int i = 0; i < ja.length(); ++i) {
            try {
                JSONObject jo = ja.getJSONObject(i);
                String name = jo.getString( key );
                values.add( name );
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return values;
    }

    public static JSONArray getTableJson(String table) {
        try {
            String s = getTable(table);
            JSONArray ja = new JSONArray(s);
            return ja;
        }
        catch( JSONException e ) {
            e.printStackTrace();
            return new JSONArray();
        }
    }

    public static String getTable( String table ) {
        String request = getBaseUrl() + table;
        String result = RequestUtil.getSync(request);
        return result;
    }

    public static String getQueryWhere( String table, String field, String value ) {
        String query = table + "?"+field+"=eq."+value;
        return query;
    }

    public static String getBaseUrl() {
        return EndpointUtil.getBasePath( getHost(), getPort() );
    }

    public static String getHost() {
        return Persistence.getInstance()._host;
    }

    public static int getPort() {
        return Persistence.getInstance()._port;
    }
}
