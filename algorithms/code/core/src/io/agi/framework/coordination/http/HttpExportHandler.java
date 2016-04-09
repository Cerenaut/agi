package io.agi.framework.coordination.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.agi.framework.Framework;
import io.agi.framework.Node;
import io.agi.framework.persistence.Persistence;

import java.io.IOException;
import java.util.Map;

/**
 * Created by dave on 2/04/16.
 */
public class HttpExportHandler implements HttpHandler {

    public static final String CONTEXT = "/export";

    public static final String PARAMETER_ENTITY = "entity";
    public static final String PARAMETER_TYPE = "type";

    public static final String TYPE_DATA = "data";
    public static final String TYPE_ENTITY = "entity";

    public Node _n;

    public HttpExportHandler( Node n ) {
        _n = n;
    }

    @Override
    public void handle( HttpExchange t ) throws IOException {
        int status = 400;
        String response = "Please specify both an Entity and a Type.";

        try {
            String query = t.getRequestURI().getQuery();
            Map< String, String > m = HttpUtil.GetQueryParams( query );

            if (       ( !m.containsKey( PARAMETER_TYPE ) )
                    || ( !m.containsKey( PARAMETER_ENTITY ) ) ) {
                return;
            }

            String entityName = m.get( PARAMETER_ENTITY ).trim(); // essential
            String type = m.get( PARAMETER_TYPE ).trim(); // essential
            response = Framework.ExportSubtree( _n, entityName, type );
            status = 200;
        }
        catch ( Exception e ) {
            e.printStackTrace();
        }

        HttpUtil.SendResponse( t, status, response );
    }
}

