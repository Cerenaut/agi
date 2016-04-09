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

    public Node _n;

    public HttpExportHandler( Node n ) {
        _n = n;
    }

    @Override
    public void handle( HttpExchange t ) throws IOException {
        int status = 400;
        String response = "";

        try {
            String query = t.getRequestURI().getQuery();
            Map< String, String > m = HttpUtil.GetQueryParams( query );
            String entityName = m.get( PARAMETER_ENTITY ).trim(); // essential
            response = Framework.ExportSubtree( _n, entityName );
            status = 200;
        }
        catch ( Exception e ) {
            e.printStackTrace();
        }

        HttpUtil.SendResponse( t, status, response );
    }
}

