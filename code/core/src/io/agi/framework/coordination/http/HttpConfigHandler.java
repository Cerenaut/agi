/*
 * Copyright (c) 2016.
 *
 * This file is part of Project AGI. <http://agi.io>
 *
 * Project AGI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Project AGI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Project AGI.  If not, see <http://www.gnu.org/licenses/>.
 */

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
    public static final String PARAMETER_CONFIG = "config";
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
            String configPath = null; // optional
            String configValue = null; // optional
            String config = null; // optional

            if( m.containsKey( PARAMETER_PATH ) ) {
                configValue = m.get( PARAMETER_PATH ).trim();
            }

            if( m.containsKey( PARAMETER_VALUE ) ) {
                configValue = m.get( PARAMETER_VALUE ).trim();
            }

            if( m.containsKey( PARAMETER_CONFIG ) ) {
                config = m.get( PARAMETER_CONFIG ).trim();
            }

            ModelEntity me = _p.fetchEntity( entityName );

            if( method.equalsIgnoreCase( "GET" ) ) {
                configValue = Framework.GetConfig( entityName );
                if( configValue == null ) {
                    configValue = "null";
                }
                if( configValue.length() == 0 ) {
                    configValue = "{}";
                }
                response = "{ \"" + PARAMETER_ENTITY + "\" : \"" + entityName + "\", \"" + PARAMETER_VALUE + "\" : " + configValue + " }";
            } else if( method.equalsIgnoreCase( "POST" ) || method.equalsIgnoreCase( "PUT" ) ) {
                if( config != null ) {
                    me.config = config; // replace entire config
                    _p.persistEntity( me );
                    response = "{ \"" + PARAMETER_ENTITY + "\" : \"" + entityName + "\", \"" + PARAMETER_VALUE + "\" : " + config + " }";
                } else {
                    Framework.SetConfig( entityName, configPath, configValue );
                    response = "{ \"" + PARAMETER_ENTITY + "\" : \"" + entityName + "\", \"" + PARAMETER_PATH + "\" : \"" + configPath + "\", \"" + PARAMETER_VALUE + "\" : \"" + configValue + "\" }";
                }
            }

            status = 200;
        }
        catch( Exception e ) {
            e.printStackTrace();
        }

        HttpUtil.SendResponse( t, status, response );
    }
}
