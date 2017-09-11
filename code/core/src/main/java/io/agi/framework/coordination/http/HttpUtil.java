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
import com.sun.net.httpserver.HttpServer;
import io.agi.core.orm.AbstractPair;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by dave on 17/03/16.
 */
public class HttpUtil {

    public static void AddHandler( HttpServer server, String handlerContext, HttpHandler h ) {
        server.createContext( handlerContext, h );
    }

    public static HttpServer Create( final HttpCoordination c, int port, String handlerContext, HttpCoordinationHandler h ) {

        try {
            HttpServer server = HttpServer.create( new InetSocketAddress( port ), 0 );
            AddHandler( server, handlerContext, h );
            server.setExecutor( null ); // creates a default executor
            return server;
        }
        catch( Exception e ) {
            e.printStackTrace();
            return null;
        }
    }

    public static Map< String, String > GetQueryParams( String query ) {

        Map< String, String > result = new HashMap< String, String >();

        if( query == null ) {
            return result;
        }

        for( String param : query.split( "&" ) ) {

            String pair[] = param.split( "=" );

            if( pair.length > 1 ) {
                result.put( pair[ 0 ], pair[ 1 ] );
            } else {
                result.put( pair[ 0 ], "" );
            }
        }
        return result;
    }

    public static ArrayList< AbstractPair< String, String > > GetDuplicateQueryParams( String query ) {

        ArrayList< AbstractPair< String, String > > result = new ArrayList< AbstractPair< String, String > >();

        if( query == null ) {
            return result;
        }

        for( String param : query.split( "&" ) ) {

            String pair[] = param.split( "=" );

            if( pair.length > 1 ) {
                result.add( new AbstractPair< String, String >( pair[ 0 ], pair[ 1 ] ) );
            } else {
                result.add( new AbstractPair< String, String >( pair[ 0 ], "" ) );
            }
        }
        return result;
    }

    public static void SendResponse( HttpExchange t, int status, String response ) throws IOException {
        ArrayList< String > list = new ArrayList< String >();
        list.add( "*" );
        t.getResponseHeaders().put( "Access-Control-Allow-Origin", list );
        t.sendResponseHeaders( status, response.length() );
        OutputStream os = t.getResponseBody();
        os.write( response.getBytes() );
        os.close();
    }
}
