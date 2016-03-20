package io.agi.framework.coordination.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.agi.framework.persistence.Persistence;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by dave on 17/03/16.
 */
public class HttpPropertiesHandler implements HttpHandler {

        public static final String CONTEXT = "/properties";

        public Persistence _p;

        public HttpPropertiesHandler( Persistence p ) {
            _p = p;
        }

        @Override
        public void handle(HttpExchange t ) throws IOException {
        int status = 400;
        String response = "";

        try {
            String query = t.getRequestURI().getQuery();
            //System.err.println("Request: " + HttpCoordinationHandler.CONTEXT + " " + query);

            String method = t.getRequestMethod();

            Map<String, String> m = HttpUtil.GetQueryParams(query);

            boolean first = true;
            response = "[ ";

            for( String key : m.keySet() ) {
                String value = m.get(key);

                if (first) {
                    first = false;
                } else {
                    response += ", ";
                }

                if (method.equalsIgnoreCase( "GET" ) ) {
                    value = _p.getPropertyString( key, "" );
                }
                else if( method.equalsIgnoreCase( "POST" ) ) {
                    _p.setPropertyString(key, value);
                }
                else if( method.equalsIgnoreCase( "PUT" ) ) {
                    _p.setPropertyString(key, value);
                }

                response += ( "{ \"key\" : \"" + key + "\", \"value\" : \"" + value + "\" }" );
            }

            response += " ]";

            status = 200;
        }
        catch( Exception e ) {
            e.printStackTrace();
        }

        HttpUtil.SendResponse(t, status, response);
    }
}
