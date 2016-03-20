package io.agi.framework.coordination.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.agi.framework.persistence.Persistence;
import io.agi.framework.serialization.ModelEntity;
import io.agi.framework.serialization.ModelNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * Created by dave on 17/03/16.
 */
public class HttpEntitiesHandler implements HttpHandler {

    public static final String CONTEXT = "/entities";

    public static final String PARAMETER_NAME = "name";

    public Persistence _p;

    public HttpEntitiesHandler(Persistence p) {
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

            Map<String, String> parameters = HttpUtil.GetQueryParams(query);

            Collection<ModelEntity> results = null;

            for( String key : parameters.keySet() ) {
                if( key.equalsIgnoreCase( PARAMETER_NAME ) ) {
                    String value = parameters.get(key);
                    ModelEntity m = _p.getEntity( value );

                    if( results == null ) {
                        results = new ArrayList< ModelEntity >();
                    }

                    results.add( m );
                }
            }

            if( results == null ) {
                results = _p.getEntities();
            }

            boolean first = true;

            if( method.equalsIgnoreCase( "GET" ) ) {

                response += "[ ";

                for( ModelEntity m : results ) {
                    if (first) {
                        first = false;
                    } else {
                        response += ", ";
                    }

                    response += "{ ";

                    response += " \"key\": \"" + m._key + "\"" + ",";
                    response += " \"node\": \"" + m._node + "\"" + ",";
                    response += " \"parent\": \"" + m._parent + "\"" + ",";
                    response += " \"type\": \"" + m._type + "\"";

                    response += " }";
                }

                response += " ]";

                status = 200;
            }
        }
        catch( Exception e ) {
            e.printStackTrace();
        }

        HttpUtil.SendResponse(t, status, response);
    }
}
