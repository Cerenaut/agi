package io.agi.framework.coordination.http;

import com.sun.net.httpserver.HttpServer;
import io.agi.framework.Node;
import io.agi.framework.coordination.Coordination;
import io.agi.framework.persistence.models.ModelNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by dave on 19/02/16.
 */
public class HttpCoordination implements Coordination {

    private static final Logger logger = LogManager.getLogger();

    public Node _n;
    public HttpServer _s;
    public ExecutorService _executor;

    public HttpCoordination() {

    }

    public void setNode( Node n ) {
        _n = n;
    }

    /**
     * Start services. This is a blocking call.
     */
    public void start() {
        // Have one (or more) threads ready to do the async tasks. Do this during startup of your app.
        _executor = Executors.newFixedThreadPool( 2 );

        int nodePort = _n.getPort();
        //String handlerClassName = HttpCoordinationHandler.class.getName();
        HttpCoordinationHandler h = new HttpCoordinationHandler( this );
        _s = HttpUtil.Create( this, nodePort, HttpCoordinationHandler.CONTEXT, h );

        addHandlers();

        _s.start();
    }

    public void addHandlers() {
        HttpConfigHandler ph = new HttpConfigHandler( _n.getPersistence() );
        HttpDataHandler dh = new HttpDataHandler( _n.getPersistence() );
        HttpNodesHandler nh = new HttpNodesHandler( _n.getPersistence() );
        HttpEntitiesHandler eh = new HttpEntitiesHandler( _n.getPersistence() );
        HttpImportHandler imh = new HttpImportHandler( _n.getPersistence() );
        HttpExportHandler exh = new HttpExportHandler( _n );
        HttpConfigHandler ch = new HttpConfigHandler( _n.getPersistence() );

        HttpUtil.AddHandler( _s, HttpConfigHandler.CONTEXT, ph );
        HttpUtil.AddHandler( _s, HttpDataHandler.CONTEXT, dh );
        HttpUtil.AddHandler( _s, HttpNodesHandler.CONTEXT, nh );
        HttpUtil.AddHandler( _s, HttpEntitiesHandler.CONTEXT, eh );
        HttpUtil.AddHandler( _s, HttpImportHandler.CONTEXT, imh );
        HttpUtil.AddHandler( _s, HttpExportHandler.CONTEXT, exh );
        HttpUtil.AddHandler( _s, HttpConfigHandler.CONTEXT, ch );
    }

    /**
     * Shutdown services
     */
    public void stop() {
        // Shutdown the threads during shutdown of your app.
        _executor.shutdown();
    }

    /**
     * This is an internally generated request for a distributed update.
     *
     * @param entityName
     */
    public void doUpdate( String entityName ) {
        logger.info( "Coordination.doUpdate( " + entityName + ")" );
        _n.doUpdate( entityName );
        doUpdateBroadcast( entityName );
    }

    /**
     * Broadcasts an update request to all Nodes.
     *
     * @param entityName
     */
    public void doUpdateBroadcast( String entityName ) {
        logger.info( "Coordination.doUpdateBroadcast( " + entityName + ")" );
        String query = getQuery( entityName, HttpCoordinationHandler.VALUE_UPDATE, _n.getName() );
        broadcast( query );
    }

    /**
     * This is an externally generated request for an update to an entity somewhere in the distributed system
     *
     * @param entityName
     * @param origin
     */
    public void doUpdateExternal( String entityName, String origin ) {
        logger.info( "Coordination.doUpdateExternal( " + entityName + ", " + origin + " )" );
        if ( origin != null ) {
            if ( origin.equals( _n.getName() ) ) {
                return; // ignore self events
            }
        }
        else { // origin is null, i.e. was generated outside the network
            // append origin=this/here and broadcast to rest of network
            doUpdateBroadcast( entityName );
        }

        logger.info( "Coordinate.doUpdateExternal(): Updating locally." );
        _n.doUpdate( entityName );
    }

    /**
     * This is an internally generated request for a distributed updated notification.
     *
     * @param entityName
     */
    public void onUpdated( String entityName ) {
        logger.info( "Coordination.onUpdated( " + entityName + ")" );
        _n.onUpdated( entityName );
        onUpdatedBroadcast( entityName );
    }

    /**
     * Sends a local update notification to all nodes.
     *
     * @param entityName
     */
    public void onUpdatedBroadcast( String entityName ) {
        logger.info( "Coordination.onUpdatedBroadcast( " + entityName + ")" );
        String query = getQuery( entityName, HttpCoordinationHandler.VALUE_UPDATED, _n.getName() );
        broadcast( query );
    }

    /**
     * This is an externally generated notification of an update to an entity somewhere in the distributed system
     *
     * @param entityName
     * @param origin
     */
    public void onUpdatedExternal( String entityName, String origin ) {
        logger.info( "Coordination.onUpdatedExternal( " + entityName + ", " + origin + " )" );
        if ( origin != null ) {
            if ( origin.equals( _n.getName() ) ) {
                return; // ignore self events
            }
        }
        else { // origin is null, i.e. was generated outside the network
            // append origin=this/here and broadcast to rest of network
            onUpdatedBroadcast( entityName );
        }

        _n.onUpdated( entityName );
    }

    /**
     * Generates the HTTP messages the HTTP layer understands.
     *
     * @param entityName
     * @param event
     * @param origin
     * @return
     */
    public String getQuery( String entityName, String event, String origin ) {
        String query = HttpCoordinationHandler.CONTEXT
                + "?" + HttpCoordinationHandler.PARAMETER_ENTITY + "=" + entityName
                + "&" + HttpCoordinationHandler.PARAMETER_EVENT + "=" + event
                + "&" + HttpCoordinationHandler.PARAMETER_ORIGIN + "=" + origin;
        return query;
    }

    /**
     * Actually broadcasts the HTTP messages to all Nodes.
     *
     * @param query
     */
    public void broadcast( String query ) {
        Collection< ModelNode > nodes = _n.getPersistence().fetchNodes();

        for ( ModelNode jn : nodes ) {
            if ( jn._name.equals( _n.getName() ) ) {
//                continue; // don't send to self
            }

            send( query, jn );
        }
    }

    /**
     * Sends a HTTP request to the specified Node.
     *
     * @param query
     * @param jn
     */
    public void send( String query, ModelNode jn ) {
        String url = "http://" + jn._host + ":" + jn._port + query;
        //System.out.println( "Sending: " + url );

        // http://stackoverflow.com/questions/3142915/how-do-you-create-an-asynchronous-http-request-in-java
        class Response {
            private InputStream body;

            public Response( InputStream body ) {
                this.body = body;
            }

            public InputStream getBody() {
                return body;
            }
        }

        class Request implements Callable< Response > {
            private URL url;

            public Request( URL url ) {
                this.url = url;
            }

            @Override
            public Response call() throws Exception {
                return new Response( url.openStream() );
            }
        }

        try {
            Future< Response > response = _executor.submit( new Request( new URL( url ) ) );

            // Do your other tasks here (will be processed immediately, current thread won't block).
            // ...

            // Get the response (here the current thread will block until response is returned).
//        try {
//            InputStream body = response.get().getBody();
//        }
//        catch( ExecutionException ee ) {
//            ee.printStackTrace();
//        }
        }
        catch ( MalformedURLException e ) {
            e.printStackTrace();
        }
    }
}
