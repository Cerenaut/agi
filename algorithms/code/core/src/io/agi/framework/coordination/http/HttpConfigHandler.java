package io.agi.framework.coordination.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.agi.framework.Framework;
import io.agi.framework.persistence.Persistence;
import io.agi.framework.persistence.models.ModelEntity;

import java.io.IOException;
import java.util.Map;

/**
 * Created by dave on 17/03/16.
 */
public class HttpConfigHandler implements HttpHandler {

    public static final String CONTEXT = "/config";

    public static final String PARAMETER_ENTITY = "entity";
    public static final String PARAMETER_PATH = "path";
    public static final String PARAMETER_VALUE = "value";

    public Persistence _p;

    public HttpConfigHandler( Persistence p ) {
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

            Map< String, String > m = HttpUtil.GetQueryParams( query );

            String entityName = m.get( PARAMETER_ENTITY ).trim(); // essential
            String configPath = m.get( PARAMETER_PATH ).trim(); // essential
            String configValue = null; // optional

            if ( m.containsKey( PARAMETER_VALUE ) ) {
                configValue = m.get( PARAMETER_VALUE ).trim();
            }

            ModelEntity me = _p.getEntity( entityName );

            if ( method.equalsIgnoreCase( "GET" ) ) {
                configValue = Framework.GetConfig( _p, entityName, configPath );
            }
            else if ( method.equalsIgnoreCase( "POST" ) || method.equalsIgnoreCase( "PUT" ) ) {
                Framework.SetConfig( _p, entityName, configPath, configValue );
                _p.setEntity( me );
            }

            response = "{ \"" + PARAMETER_ENTITY + "\" : \"" + entityName + "\", \"" + PARAMETER_PATH + "\" : \"" + configPath + "\", \"" + PARAMETER_VALUE + "\" : \"" + configValue + "\" }";

            status = 200;
        }
        catch ( Exception e ) {
            e.printStackTrace();
        }

        HttpUtil.SendResponse( t, status, response );
    }
}
