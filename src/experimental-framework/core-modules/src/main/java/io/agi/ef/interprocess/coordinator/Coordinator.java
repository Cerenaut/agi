package io.agi.ef.interprocess.coordinator;

import io.agi.core.ObjectMap;
import io.agi.ef.Entity;
import io.agi.ef.EntityFactory;
import io.agi.ef.Persistence;
import io.agi.ef.entities.Relay;
import io.agi.ef.http.EndpointUtil;
import io.agi.ef.http.RequestUtil;
import io.agi.ef.http.node.Node;
import io.agi.ef.http.node.NodeServer;
import io.agi.ef.http.servlets.CreateServlet;
import io.agi.ef.http.servlets.DataEventServlet;
import io.agi.ef.http.servlets.EntityEventServlet;
import io.agi.ef.interprocess.apiInterfaces.ControlInterface;
import io.agi.ef.persistenceClientApi.ApiException;
import io.agi.ef.persistenceClientApi.model.NodeModel;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.logging.Logger;

/**
 *
 * Abstract base class of Master and Slave Coordinator.
 * Collects together a common Coordinator interface to any part of the system,
 * as well as implementing the common Server functionality.
 *
 * Created by gideon on 29/08/15.
 */
public class Coordinator implements ControlInterface {

    public static final String KEY_COORDINATOR = "coordinator";
    protected Logger _logger = null;
//    protected ConnectionManager _cm = null;

    public Coordinator () {
        _logger = Logger.getLogger( this.getClass().getPackage().getName() );
//        _cm = new ConnectionManager();
    }

    public static Coordinator getInstance() {
        return (Coordinator)ObjectMap.Get( KEY_COORDINATOR );
    }

    /**
     * Need a separate call because you're not supposed to call derived methods from the constructor
     */
    public void setup( boolean master ) {
        ObjectMap.Put(KEY_COORDINATOR, this);

        // note in the DB which node is the coordinator (ie this node)
        Persistence.addProperty( Node.PROPERTY_COORDINATOR_NODE, Node.getInstance().getName() );

        if( master ) {
            Relay r = new Relay(); // can create entities like this, or via factory.
            r.setup();
        }
    }

    /**
     * The Node has received an instruction to terminate gracefully.
     */
    public void onStopEvent() {
        Node.getInstance().stop();
    }

    /**
     * Refer to the EntityFactory object to create the entity.
     * @param entityName
     * @param entityType
     * @param parentEntityName
     * @param entityConfig
     */
    public Entity onCreateEvent( String entityName, String entityType, String parentEntityName, String entityConfig ) {
        return EntityFactory.create(entityName, entityType, parentEntityName, entityConfig);
    }

    /**
     * The specified action has happened to the given Data object.
     * For example, data has changed.
     * Broadcast the event to all Entities at this Node.
     * @param data
     * @param action
     */
    public void onDataEvent(String data, String action) {
        System.out.println( "Node::onDataEvent( "+data+", "+action+")" );

        // notify all old_entities of the event
        String nodeName = Node.getInstance().getName();
        HashSet<String> entityNames = Persistence.getEntitiesAtNode(nodeName);

        ObjectMap om = ObjectMap.GetInstance();

        for( String entityName : entityNames ) {
            try {
                Object o = om.get(entityName);
                Entity e = (Entity)o;
                e.onDataEvent(data, action);
            }
            catch( Exception e ) {
                e.printStackTrace(); // allow node to continue working
            }
        }
    }

    /**
     * The Node has received an event indicating that an action has happened to an entity.
     * Broadcast the event to all Entities at this Node.
     * @param entity
     * @param action
     */
    public void onEntityEvent(String entity, String action) {
        System.out.println( "Node::onEntityEvent( "+entity+", "+action+")" );

        // notify all old_entities of the event
        String nodeName = Node.getInstance().getName();
        HashSet<String> entityNames = Persistence.getEntitiesAtNode( nodeName );

        ObjectMap om = ObjectMap.GetInstance();

        for( String entityName : entityNames ) {
            try {
                Object o = om.get(entityName);
                Entity e = (Entity)o;
                e.onEvent(entity, action);
            }
            catch( Exception e ) {
                e.printStackTrace(); // allow node to continue working
            }
        }
    }

    /**
     * Posts an event to the Relay node. It will be Relayed to all other nodes.
     * It will be handled by this Node when it arrives as a HTTP event.
     * @param data
     * @param action
     */
    public void postDataEvent(String data, String action) {
        String relayEntityName = Relay.ENTITY_NAME;
        String relayNodeName = Persistence.getNodeOfEntity(relayEntityName);
        postDataEvent(relayNodeName, data, action);
    }

    /**
     * Post the event to the specified node. If the node has a Relay entity, it will be re-posted to all nodes except
     * the Relay entity's host node.
     * @param nodeName
     * @param data
     * @param action
     */
    public void postDataEvent(String nodeName, String data, String action) {

        JSONObject jo = Persistence.getNode_old( nodeName );
        String baseUrl = Node.getBaseUrl(jo);

        String request = baseUrl + NodeServer.SERVLET_CONTEXT_PATHSPEC + "/" + DataEventServlet.PATHSPEC + "?"+DataEventServlet.PARAMETER_NAME+"=" + data + "&"+DataEventServlet.PARAMETER_ACTION+"=" +action;

        //System.out.println( "RelayEntity/Request: " + request );
        /*String response =*/ RequestUtil.postSync(request);
        //System.out.println("RelayEntity/Response: " + response);
    }

    /**
     * Posts an event to the Relay node. It will be Relayed to all other nodes.
     * It will be handled by this Node when it arrives as a HTTP event.
     * @param entity
     * @param action
     */
    public void postEntityEvent(String entity, String action) {
        String relayEntityName = Relay.ENTITY_NAME;
        String relayNodeName = Persistence.getNodeOfEntity(relayEntityName);
        postEntityEvent(relayNodeName, entity, action);
    }

    /**
     * Post the event to the specified node. If the node has a Relay entity, it will be re-posted to all nodes except
     * the Relay entity's host node.
     * @param nodeName
     * @param entity
     * @param action
     */
    public void postEntityEvent(String nodeName, String entity, String action) {

        JSONObject jo = Persistence.getNode_old( nodeName );
        String baseUrl = Node.getBaseUrl(jo);

        String request = baseUrl + NodeServer.SERVLET_CONTEXT_PATHSPEC + "/" + EntityEventServlet.PATHSPEC + "?"+EntityEventServlet.PARAMETER_NAME+"=" + entity + "&"+EntityEventServlet.PARAMETER_ACTION+"=" +action;

        //System.out.println( "RelayEntity/Request: " + request );
        /*String response =*/ RequestUtil.postSync(request);
        //System.out.println("RelayEntity/Response: " + response);
    }

    /**
     * Creates an entity of the specified type/name etc on the specified node.
     *
     * @param nodeName
     * @param entityName
     * @param entityType
     * @param parentEntityName
     * @param entityConfig
     */
    public void postCreateEvent( String nodeName, String entityName, String entityType, String parentEntityName, String entityConfig ) throws ApiException {

//        JSONObject jo = Persistence.getNode( nodeName );
//        String baseUrl = Node.getBaseUrl( jo );

        NodeModel node = Persistence.getInstance().getNode( nodeName );
        String baseUrl = EndpointUtil.getBasePath( node.getHost(), node.getPort() );

        String request = baseUrl + NodeServer.SERVLET_CONTEXT_PATHSPEC + "/" + CreateServlet.PATHSPEC
                + "?"+CreateServlet.PARAMETER_NAME+"=" + entityName
                + "&"+CreateServlet.PARAMETER_TYPE+"=" +entityType
                + "&"+CreateServlet.PARAMETER_PARENT+"=" +parentEntityName
                + "&"+CreateServlet.PARAMETER_CONFIG+"=" +entityConfig;

        //System.out.println( "RelayEntity/Request: " + request );
        String response = RequestUtil.postSync( request );
        //System.out.println( "RelayEntity/Response: " + response );
    }

//    /**
//     * Run the Master-Coordinator server.
//     */
//    private class RunServer implements Runnable {
//        public void run() {
//            Server server = _cm.setupServer( contextPath(), listenerPort() );
//            try {
//                server.start();
//                server.join();
//            }
//            catch ( Exception e ) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    protected void startServer() throws Exception {
//        setServiceImplementation();
//
//        Thread serverThread = new Thread( new RunServer() );
//        serverThread.start();
//    }
//
//    abstract protected void setServiceImplementation();
//    abstract protected String contextPath();
//    abstract protected int listenerPort();
}
