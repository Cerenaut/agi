package io.agi.framework.coordination.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.Map;

/**
 * http://stackoverflow.com/questions/3732109/simple-http-server-in-java-using-only-java-se-api
 * Created by dave on 19/02/16.
 */
public class HttpCoordinationHandler implements HttpHandler {

    public static final String CONTEXT = "/update";

    public static final String PARAMETER_ORIGIN = "origin";
    public static final String PARAMETER_ENTITY = "entity";
    public static final String PARAMETER_EVENT = "event";

    public static final String VALUE_UPDATE = "update";
    public static final String VALUE_UPDATED = "updated";

    public HttpCoordination _c;

    public HttpCoordinationHandler( HttpCoordination c ) {
        _c = c;
    }

    @Override
    public void handle( HttpExchange t ) throws IOException {
        String response = "What?";
        int status = 400;

        try {
            String query = t.getRequestURI().getQuery();
            //System.err.println("Request: " + HttpCoordinationHandler.CONTEXT + " " + query);

            Map< String, String > m = HttpUtil.GetQueryParams( query );

            String entityName = m.get( PARAMETER_ENTITY ).trim();
            String eventValue = m.get( PARAMETER_EVENT ).trim();
            String originValue = m.get( PARAMETER_ORIGIN );

            if ( originValue != null ) {
                originValue.trim();
            }

            if ( entityName != null ) {
                if ( eventValue != null ) {
                    if ( eventValue.equalsIgnoreCase( VALUE_UPDATE ) ) {
                        _c.doUpdateExternal( entityName, originValue );
                        status = 200;
                        response = GetResponse( entityName, VALUE_UPDATE, originValue );
                    }
                    else if ( eventValue.equalsIgnoreCase( VALUE_UPDATED ) ) {
                        _c.onUpdatedExternal( entityName, originValue );
                        status = 200;
                        response = GetResponse( entityName, VALUE_UPDATED, originValue );
                    }
                }
            }
        }
        catch ( Exception e ) {
            e.printStackTrace();
        }

        HttpUtil.SendResponse( t, status, response );
    }

    protected static String GetResponse( String entity, String event, String origin ) {
        String response = "{ \"entity\" : \"" + entity + "\", \"event\" : \"" + event + "\", \"origin\" : \"" + origin + "\" }";
        return response;
    }
}
