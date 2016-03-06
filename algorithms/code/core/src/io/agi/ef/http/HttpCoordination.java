package io.agi.ef.http;

import com.sun.net.httpserver.HttpServer;
import io.agi.ef.Coordination;
import io.agi.ef.Node;
import io.agi.ef.serialization.JsonNode;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.concurrent.*;

/**
 * Created by dave on 19/02/16.
 */
public class HttpCoordination implements Coordination {

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
        _s = HttpCoordinationServer.create( this, nodePort, HttpCoordinationHandler.CONTEXT, h );
        _s.start();
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
     * @param entityName
     */
    public void doUpdate(String entityName) {
        _n.doUpdate(entityName);
        doUpdateBroadcast(entityName);
    }

    /**
     * Broadcasts an update request to all Nodes.
     * @param entityName
     */
    public void doUpdateBroadcast(String entityName) {
        String query = getQuery( entityName, HttpCoordinationHandler.VALUE_UPDATE, _n.getName() );
        broadcast(query);
    }

    /**
     * This is an externally generated request for an update to an entity somewhere in the distributed system
     * @param entityName
     * @param origin
     */
    public void doUpdateExternal(String entityName, String origin) {
        if( origin != null ) {
            if( origin.equals( _n.getName() ) ) {
                return; // ignore self events
            }
        }
        else { // origin is null, i.e. was generated outside the network
            // append origin=this/here and broadcast to rest of network
            doUpdateBroadcast( entityName );
        }

        _n.doUpdate(entityName);
    }

    /**
     * This is an internally generated request for a distributed updated notification.
     * @param entityName
     */
    public void onUpdated(String entityName) {
        _n.onUpdated(entityName);
        onUpdatedBroadcast(entityName);
    }

    /**
     * Sends a local update notification to all nodes.
     * @param entityName
     */
    public void onUpdatedBroadcast(String entityName) {
        String query = getQuery( entityName, HttpCoordinationHandler.VALUE_UPDATED, _n.getName() );
        broadcast(query);
    }

    /**
     * This is an externally generated notification of an update to an entity somewhere in the distributed system
     * @param entityName
     * @param origin
     */
    public void onUpdatedExternal(String entityName, String origin) {
        if( origin != null ) {
            if( origin.equals( _n.getName() ) ) {
                return; // ignore self events
            }
        }
        else { // origin is null, i.e. was generated outside the network
            // append origin=this/here and broadcast to rest of network
            onUpdatedBroadcast(entityName);
        }

        _n.onUpdated(entityName);
    }

    /**
     * Generates the HTTP messages the HTTP layer understands.
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
     * @param query
     */
    public void broadcast( String query ) {
        Collection< JsonNode > nodes = _n.getPersistence().getNodes();

        for( JsonNode jn : nodes ) {
            if( jn._key.equals( _n.getName() ) ) {
//                continue; // don't send to self
            }

            send( query, jn );
        }
    }

    /**
     * Sends a HTTP request to the specified Node.
     * @param query
     * @param jn
     */
    public void send( String query, JsonNode jn ) {
        String url = "http://" + jn._host + ":" + jn._port + query;
        System.out.println( "Sending: " + url );

        // http://stackoverflow.com/questions/3142915/how-do-you-create-an-asynchronous-http-request-in-java
        class Response {
            private InputStream body;

            public Response(InputStream body) {
                this.body = body;
            }

            public InputStream getBody() {
                return body;
            }
        }

        class Request implements Callable<Response> {
            private URL url;

            public Request(URL url) {
                this.url = url;
            }

            @Override
            public Response call() throws Exception {
                return new Response( url.openStream() );
            }
        }

        try {
            Future<Response> response = _executor.submit(new Request(new URL(url)));

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
        catch( MalformedURLException e ) {
            e.printStackTrace();
        }
    }
}
