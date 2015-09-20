package io.agi.ef.http.node;

import io.agi.core.ObjectMap;
import io.agi.ef.Entity;
import io.agi.ef.Persistence;
import io.agi.ef.entities.Relay;
import io.agi.ef.http.EndpointUtil;
import io.agi.ef.http.RequestUtil;
import io.agi.ef.http.servlets.CreateServlet;
import io.agi.ef.http.servlets.DataEventServlet;
import io.agi.ef.http.servlets.EntityEventServlet;
import org.json.JSONObject;

import java.net.InetAddress;
import java.util.HashSet;

/**
 * The wrapper object for all the parts of the distributed system running within the current OS process.
 * Each process is a Node. The system comprises many Nodes.
 *
 * Created by dave on 11/09/15.
 */
public class Node {

    public static final String KEY_NODE = "node";

    public static final String NAME = "name";
    public static final String HOST = "host";
    public static final String PORT = "port";

    public static final String PROPERTY_COORDINATOR_NODE = "coordinator-node";

    protected String _name = null;

    /**
     * Instantiate the Node at the given host, port.
     * Each Node has a unique name.
     *
     * @param name
     * @param host
     * @param port
     */
    public Node( String name, String host, int port ) {

        _name = name;

        Persistence.addNode(name, host, port);

        ObjectMap.Put( KEY_NODE, this );
    }

    public static Node getInstance() {
        return (Node)ObjectMap.Get( KEY_NODE );
    }

    public synchronized void delete() {
        Persistence.removeNode(_name);
    }

    /**
     * Discovers its own IP address.
     * @return
     */
    public static String getLocalHostAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        }
        catch( Exception e ) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * The immutable unique name of the Node.
     * @return
     */
    public synchronized String getName() {
        return _name;
    }

    /**
     * The Node name should not be changed after instantiation.
     * @param name
     */
    public synchronized void setName( String name ) {
        _name = name;
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
        String nodeName = getName();
        HashSet<String> entityNames = Persistence.getEntitiesAtNode( nodeName );

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
        String nodeName = getName();
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

        JSONObject jo = Persistence.getNode( nodeName );
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

        JSONObject jo = Persistence.getNode( nodeName );
        String baseUrl = Node.getBaseUrl( jo );

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
    public void postCreateEvent( String nodeName, String entityName, String entityType, String parentEntityName, String entityConfig ) {

        JSONObject jo = Persistence.getNode( nodeName );
        String baseUrl = Node.getBaseUrl( jo );

        String request = baseUrl + NodeServer.SERVLET_CONTEXT_PATHSPEC + "/" + CreateServlet.PATHSPEC
                       + "?"+CreateServlet.PARAMETER_NAME+"=" + entityName
                       + "&"+CreateServlet.PARAMETER_TYPE+"=" +entityType
                       + "&"+CreateServlet.PARAMETER_PARENT+"=" +parentEntityName
                       + "&"+CreateServlet.PARAMETER_CONFIG+"=" +entityConfig;

        System.out.println( "RelayEntity/Request: " + request );
        String response = RequestUtil.postSync( request );
        System.out.println( "RelayEntity/Response: " + response );
    }

    /**
     * Returns true if the specified name matches the name of this Node.
     * @param name
     * @return
     */
    public synchronized boolean isSelf( String name ) {
        if( _name == null ) {
            return false;
        }
        return _name.equals(name);
    }

    /**
     * Convenience method.
     * @param jo
     * @return
     */
    public static String getBaseUrl( JSONObject jo ) {
        return EndpointUtil.getBasePath(getHost(jo), getPort(jo));
    }

    /**
     * Interpreting the JSON notation of this Node.
     * @param jo
     * @return
     */
    public static String getHost( JSONObject jo ) {
        try {
            String host = jo.getString(HOST);
            return host;
        }
        catch( Exception e ) {
            return null;
        }
    }

    /**
     * Interpreting the JSON notation of this Node.
     * @param jo
     * @return
     */
    public static int getPort( JSONObject jo ) {
        try {
            int port = jo.getInt( PORT );
            return port;
        }
        catch( Exception e ) {
            return -1;
        }
    }
}
