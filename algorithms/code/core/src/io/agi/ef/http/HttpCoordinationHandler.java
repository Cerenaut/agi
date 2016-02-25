package io.agi.ef.http;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

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
            System.err.println("Request: " + HttpCoordinationHandler.CONTEXT + " " + query);

            Map<String, String> m = GetQueryParams(query);

            String entityName = m.get(PARAMETER_ENTITY).trim();
            String eventValue = m.get(PARAMETER_EVENT).trim();
            String originValue = m.get(PARAMETER_ORIGIN);

            if( originValue != null ) {
                originValue.trim();
            }

            if (entityName != null) {
                if (eventValue != null) {
                    if (eventValue.equalsIgnoreCase(VALUE_UPDATE)) {
                        _c.externalRequestUpdate(entityName, originValue);
                        status = 200;
                        response = "Updating entity: '" + entityName + "'";
                    } else if (eventValue.equalsIgnoreCase(VALUE_UPDATED)) {
                        _c.externalNotifyUpdated(entityName, originValue);
                        status = 200;
                        response = "Entity update noted: '" + entityName + "'";
                    }
                }
            }
        }
        catch( Exception e ) {
            e.printStackTrace();
        }

        ArrayList< String > list = new ArrayList< String >();
        list.add( "*" );
        t.getResponseHeaders().put( "Access-Control-Allow-Origin", list );
        t.sendResponseHeaders( status, response.length() );
        OutputStream os = t.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    public static Map<String, String> GetQueryParams( String query ) {

        Map<String, String> result = new HashMap<String, String>();

        for( String param : query.split( "&" ) ) {

            String pair[] = param.split( "=" );

            if( pair.length > 1 ) {
                result.put( pair[0], pair[1] );
            }
            else{
                result.put( pair[0], "" );
            }
        }
        return result;
    }
}
