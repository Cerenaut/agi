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

            Collection< ModelData > results = new ArrayList< ModelData >();

            for( AbstractPair< String, String > ap : parameters ) {
                String key = ap._first;
                String value = ap._second;
                if( key.equalsIgnoreCase( PARAMETER_NAME ) ) {
                    ModelData m = _p.fetchData( value );

                    if( m != null ) {
                        results.add( m );
                    }
                }
            }

            // if no data specified, get all data names.
            if( results.isEmpty() ) {
                Collection< String > names = _p.getData();

                for( String name : names ) {
                    ModelData m = new ModelData();
                    m.name = name;
                    results.add( m );
                }
            }

            boolean first = true;
            if( method.equalsIgnoreCase( "GET" ) ) {

                response += "[ ";

                for( ModelData m : results ) {
                    if( first ) {
                        first = false;
                    } else {
                        response += ", ";
                    }

                    response += "{ ";

                    response += " \"name\": \"" + m.name + "\"" + ",";
                    response += " \"refKeys\": \"" + m.refKeys + "\"" + ",";
                    response += " \"sizes\": " + m.sizes + ",";
                    response += " \"elements\": " + m.elements;

                    response += " }";
                }

                response += " ]";

                status = 200;
            }

            status = 200;
        }
        catch( Exception e ) {
            e.printStackTrace();
        }

        HttpUtil.SendResponse( t, status, response );
    }
}
