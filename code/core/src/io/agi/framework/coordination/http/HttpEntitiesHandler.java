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
import io.agi.core.orm.AbstractPair;
import io.agi.framework.persistence.Persistence;
import io.agi.framework.persistence.models.ModelEntity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by dave on 17/03/16.
 */
public class HttpEntitiesHandler implements HttpHandler {

    public static final String CONTEXT = "/entities";

    public static final String PARAMETER_NAME = "name";

    public Persistence _p;

    public HttpEntitiesHandler( Persistence p ) {
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

            Collection< ModelEntity > results = null;

            for( AbstractPair< String, String > ap : parameters ) {
                String key = ap._first;
                String value = ap._second;
                if( key.equalsIgnoreCase( PARAMETER_NAME ) ) {
                    ModelEntity m = _p.fetchEntity( value );

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

                    // sanitize the config
                    String config = m.config;

                    if( config == null ) {
                        config = "null";
                    }

                    if( config.isEmpty() ) {
                        config = "\"\""; // empty double quotes.
                    }

                    // write the entity
                    if( first ) {
                        first = false;
                    } else {
                        response += ", ";
                    }

                    response += "{ ";

                    response += " \"key\": \"" + m.name + "\"" + ",";
                    response += " \"node\": \"" + m.node + "\"" + ",";
                    response += " \"parent\": \"" + m.parent + "\"" + ",";
                    response += " \"type\": \"" + m.type + "\"";
                    response += " \"config\": " + config; // this must be JSON

                    response += " }";
                }

                response += " ]";

                status = 200;
            }
        }
        catch( Exception e ) {
            e.printStackTrace();
        }

        HttpUtil.SendResponse( t, status, response );
    }
}
