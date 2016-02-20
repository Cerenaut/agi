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
     * This is an externally generated request for an update to an entity somewhere in the distributed system
     * @param entityName
     * @param origin
     */
    public void externalRequestUpdate( String entityName, String origin ) {
        if( origin != null ) {
            if( origin.equals( _n.getName() ) ) {
                return; // ignore self events
            }
        }

        _n.doUpdate(entityName);
    }

    /**
     * This is an externally generated notification of an update to an entity somewhere in the distributed system
     * @param entityName
     * @param origin
     */
    public void externalNotifyUpdated( String entityName, String origin ) {
        if( origin != null ) {
            if( origin.equals( _n.getName() ) ) {
                return; // ignore self events
            }
        }

        _n.onUpdated(entityName);
    }

    /**
     * This is an internally generated request for a distributed update.
     * @param entityName
     */
    public void requestUpdate(String entityName) {
        _n.doUpdate(entityName);
        broadcastUpdate(entityName);
    }

    /**
     * This is an internally generated request for a distributed updated notification.
     * @param entityName
     */
    public void notifyUpdated(String entityName) {
        _n.onUpdated(entityName);
        broadcastUpdated(entityName);
    }

    public void broadcastUpdate( String entityName ) {
        String query = getQuery( entityName, HttpCoordinationHandler.VALUE_UPDATE, _n.getName() );
        broadcast( query );
    }

    public void broadcastUpdated( String entityName ) {
        String query = getQuery( entityName, HttpCoordinationHandler.VALUE_UPDATED, _n.getName() );
        broadcast( query );
    }

    public String getQuery( String entityName, String event, String origin ) {
        String query = HttpCoordinationHandler.CONTEXT
                + "?" + HttpCoordinationHandler.PARAMETER_ENTITY + "=" + entityName
                + "&" + HttpCoordinationHandler.PARAMETER_EVENT + "=" + event
                + "&" + HttpCoordinationHandler.PARAMETER_ORIGIN + "=" + origin;
        return query;
    }

    public void broadcast( String query ) {
        Collection< JsonNode > nodes = _n.getPersistence().getNodes();

        for( JsonNode jn : nodes ) {
            if( jn._key.equals( _n.getName() ) ) {
//                continue; // don't send to self
            }

            send( query, jn );
        }
    }

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
