package io.agi.framework.coordination.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.agi.framework.persistence.Persistence;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by dave on 17/03/16.
 */
public class HttpPropertiesHandler implements HttpHandler {

    public static final String CONTEXT = "/properties";

    public static final String PARAMETER_SEARCH = "search";

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

            Map<String, String> results = new HashMap< String, String >();

            boolean first = true;

            for( String key : m.keySet() ) {

                String value = m.get(key);

                if (key.equalsIgnoreCase(PARAMETER_SEARCH)) {
                    results = _p.getProperties(value); // must be GET.
                    break;
                }

                if (method.equalsIgnoreCase("GET")) {
                    String newValue = _p.getPropertyString(key, "");
                    results.put( key, newValue );
                } else if (method.equalsIgnoreCase("POST")) {
                    _p.setPropertyString(key, value);
                    results.put( key, value );
                } else if (method.equalsIgnoreCase("PUT")) {
                    _p.setPropertyString(key, value);
                    results.put( key, value );
                }
            }

            response = "[ ";

            for( String key : results.keySet() ) {
                if (first) {
                    first = false;
                } else {
                    response += ", ";
                }

                String value = results.get( key );
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
