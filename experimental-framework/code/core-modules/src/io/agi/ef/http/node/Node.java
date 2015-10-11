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
 * <p>
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

        Persistence.getInstance().addNode( name, host, port );

        ObjectMap.Put( KEY_NODE, this );
    }

    public static Node getInstance() {
        return ( Node ) ObjectMap.Get( KEY_NODE );
    }

    public void stop() {
        NodeServer.stop();
    }

    public synchronized void delete() {
        Persistence.removeNode( _name );
    }

    /**
     * Discovers its own IP address.
     *
     * @return
     */
    public static String getLocalHostAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        }
        catch ( Exception e ) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * The immutable unique name of the Node.
     *
     * @return
     */
    public synchronized String getName() {
        return _name;
    }

    /**
     * The Node name should not be changed after instantiation.
     *
     * @param name
     */
    public synchronized void setName( String name ) {
        _name = name;
    }

    /**
     * Returns true if the specified name matches the name of this Node.
     *
     * @param name
     * @return
     */
    public synchronized boolean isSelf( String name ) {
        if ( _name == null ) {
            return false;
        }
        return _name.equals( name );
    }

    /**
     * Convenience method.
     *
     * @param jo
     * @return
     */
    public static String getBaseUrl( JSONObject jo ) {
        return EndpointUtil.getBasePath( getHost( jo ), getPort( jo ) );
    }

    /**
     * Interpreting the JSON notation of this Node.
     *
     * @param jo
     * @return
     */
    public static String getHost( JSONObject jo ) {
        try {
            String host = jo.getString( HOST );
            return host;
        }
        catch ( Exception e ) {
            return null;
        }
    }

    /**
     * Interpreting the JSON notation of this Node.
     *
     * @param jo
     * @return
     */
    public static int getPort( JSONObject jo ) {
        try {
            int port = jo.getInt( PORT );
            return port;
        }
        catch ( Exception e ) {
            return -1;
        }
    }
}
