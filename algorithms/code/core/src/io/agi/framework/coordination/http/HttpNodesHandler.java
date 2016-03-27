package io.agi.framework.coordination.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.agi.framework.persistence.Persistence;
import io.agi.framework.persistence.models.ModelNode;

import java.io.IOException;
import java.util.Collection;

/**
 * Created by dave on 17/03/16.
 */
public class HttpNodesHandler implements HttpHandler {

    public static final String CONTEXT = "/nodes";

    public Persistence _p;

    public HttpNodesHandler( Persistence p ) {
        _p = p;
    }

    @Override
    public void handle( HttpExchange t ) throws IOException {
        int status = 400;
        String response = "";

        try {
            //String query = t.getRequestURI().getQuery();
            //System.err.println("Request: " + HttpCoordinationHandler.CONTEXT + " " + query);

            String method = t.getRequestMethod();

//            Map<String, String> m = HttpUtil.GetQueryParams(query);
            boolean first = true;

            if ( method.equalsIgnoreCase( "GET" ) ) {

                Collection< ModelNode > nodes = _p.getNodes();

                response += "[ ";

                for ( ModelNode m : nodes ) {
                    if ( first ) {
                        first = false;
                    }
                    else {
                        response += ", ";
                    }

                    response += "{ ";

                    response += " \"key\": \"" + m._key + "\"" + ",";
                    response += " \"host\": \"" + m._host + "\"" + ",";
                    response += " \"port\": " + m._port;

                    response += " }";
                }

                response += " ]";

                status = 200;
            }
        }
        catch ( Exception e ) {
            e.printStackTrace();
        }

        HttpUtil.SendResponse( t, status, response );
    }
}
