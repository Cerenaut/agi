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

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.agi.framework.Framework;
import io.agi.framework.persistence.Persistence;
import io.agi.framework.persistence.PersistenceUtil;
import io.agi.framework.persistence.models.ModelEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Map;

/**
 * Created by dave on 17/03/16.
 */
public class HttpConfigHandler implements HttpHandler {

    protected static final Logger logger = LogManager.getLogger();

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
        String response = "generic response";
        String request = "";

        try {
            String query = t.getRequestURI().getQuery();
            String method = t.getRequestMethod();

            request = "Request (" + method + "): " + HttpCoordinationHandler.CONTEXT + " " + query;

            Map< String, String > m = HttpUtil.GetQueryParams( query );

            if ( !m.containsKey( PARAMETER_ENTITY ) ) {
                String message = "Unable to handle config request: " + request + "\n";
                message += "No entity was specified.";

                HttpError httpError = new HttpError();
                httpError.status = status;
                httpError.message = message;

                response = new Gson().toJson( httpError );
            }
            else {
                String entityName = m.get( PARAMETER_ENTITY ).trim(); // essential

                String configPath = null; // optional
                String configValue = null; // optional
                String config = null; // optional

                if( m.containsKey( PARAMETER_PATH ) ) {
                    configPath = m.get( PARAMETER_PATH ).trim();
                }

                if( m.containsKey( PARAMETER_VALUE ) ) {
                    configValue = m.get( PARAMETER_VALUE ).trim();
                }

                if( m.containsKey( PARAMETER_CONFIG ) ) {
                    config = m.get( PARAMETER_CONFIG ).trim();
                }

                ModelEntity modelEntity = _p.getEntity( entityName );

                if( modelEntity == null ) {
                    response = "Entity Name is incorrect - there is no entity by the name: " + entityName;
                }
                else {
                    if( method.equalsIgnoreCase( "GET" ) ) {
                        configValue = PersistenceUtil.GetConfig( entityName );
                        if( configValue == null ) {
                            configValue = "null";
                        }
                        if( configValue.length() == 0 ) {
                            configValue = "{}";
                        }
                        response = "{ \"" + PARAMETER_ENTITY + "\" : \"" + entityName + "\", \"" + PARAMETER_VALUE + "\" : " + configValue + " }";
                        status = 200;
                    }
                    else if( method.equalsIgnoreCase( "POST" ) || method.equalsIgnoreCase( "PUT" ) ) {
                        if( config != null ) {
                            modelEntity.config = config; // replace entire config
                            _p.persistEntity( modelEntity );
                            response = "{ \"" + PARAMETER_ENTITY + "\" : \"" + entityName + "\", \"" + PARAMETER_VALUE + "\" : " + config + " }";
                            status = 200;
                        }
                        else if( ( configPath != null ) && ( configValue != null ) ) {
                            PersistenceUtil.SetConfig( entityName, configPath, configValue );
                            response = "{ \"" + PARAMETER_ENTITY + "\" : \"" + entityName + "\", \"" + PARAMETER_PATH + "\" : \"" + configPath + "\", \"" + PARAMETER_VALUE + "\" : \"" + configValue + "\" }";
                            status = 200;
                        }
                        else {
                            response = "You must specify parameters 'config' or both 'value' and 'path'";
                            status = 400;
                        }
                    }
                }
            }
        }
        catch( Exception e ) {

            String message = "Unable to handle config request: " + request;

            HttpError httpError = new HttpError();
            httpError.status = status;
            httpError.message = message;

            response = new Gson().toJson( httpError );

            logger.error( message );
            logger.error( "Response: " + response );
            logger.error( e.toString(), e );
        }

        HttpUtil.SendResponse( t, status, response );
    }
}
