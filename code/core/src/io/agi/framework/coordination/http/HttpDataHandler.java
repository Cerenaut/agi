package io.agi.framework.coordination.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.agi.core.orm.AbstractPair;
import io.agi.framework.persistence.Persistence;
import io.agi.framework.persistence.models.ModelData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by dave on 17/03/16.
 */
public class HttpDataHandler implements HttpHandler {

    public static final String CONTEXT = "/data";

    public static final String PARAMETER_NAME = "name";

    public Persistence _p;

    public HttpDataHandler( Persistence p ) {
        _p = p;
    }

    @Override
    public void handle( HttpExchange t ) throws IOException {
        int status = 400;
        String response = "";

        try {
            String query = t.getRequestURI().getQuery();
            //System.err.println("Request: " + HttpCoordinationHandler.CONTEXT + " " + query);

            String method = t.getRequestMethod();

            ArrayList< AbstractPair< String, String > > parameters = HttpUtil.GetDuplicateQueryParams( query );

            Collection< ModelData > results = null;

            for ( AbstractPair< String, String > ap : parameters ) {
                String key = ap._first;
                String value = ap._second;
                if ( key.equalsIgnoreCase( PARAMETER_NAME ) ) {
                    ModelData m = _p.fetchData( value );

                    if ( results == null ) {
                        results = new ArrayList< ModelData >();
                    }

                    if ( m != null ) {
                        results.add( m );
                    }
                }
            }

            boolean first = true;
            if ( method.equalsIgnoreCase( "GET" ) ) {

                response += "[ ";

                if ( results != null ) {
                    for ( ModelData m : results ) {
                        if ( first ) {
                            first = false;
                        }
                        else {
                            response += ", ";
                        }

                        response += "{ ";

                        response += " \"key\": \"" + m.name + "\"" + ",";
                        response += " \"ref_key\": \"" + m.refKeys + "\"" + ",";
                        response += " \"sizes\": " + m.sizes + ",";
                        response += " \"elements\": " + m.elements;

                        response += " }";
                    }
                }

                response += " ]";

                status = 200;
            }

            status = 200;
        }
        catch ( Exception e ) {
            e.printStackTrace();
        }

        HttpUtil.SendResponse( t, status, response );
    }
}
