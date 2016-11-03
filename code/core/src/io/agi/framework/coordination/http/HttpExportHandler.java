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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Map;

/**
 * Created by dave on 2/04/16.
 */
public class HttpExportHandler implements HttpHandler {

    protected static final Logger logger = LogManager.getLogger();

    public static final String CONTEXT = "/export";

    public static final String PARAMETER_ENTITY = "entity";
    public static final String PARAMETER_TYPE = "type";

    public static final String TYPE_DATA = "data";
    public static final String TYPE_DATA_REFS = "data-refs";
    public static final String TYPE_ENTITY = "entity";

    public HttpExportHandler() {
    }

    @Override
    public void handle( HttpExchange t ) throws IOException {
        int status = 400;
        String response = "Please specify both an Entity and a Type.";

        try {
            String query = t.getRequestURI().getQuery();
            Map< String, String > m = HttpUtil.GetQueryParams( query );

            if( ( m.containsKey( PARAMETER_TYPE ) )
                    && ( m.containsKey( PARAMETER_ENTITY ) ) ) {
                String entityName = m.get( PARAMETER_ENTITY ).trim(); // essential
                String type = m.get( PARAMETER_TYPE ).trim(); // essential
                response = Framework.ExportSubtree( entityName, type );

                String filename = entityName + "-" + type + ".json";
                t.getResponseHeaders().add( "Content-type", "text/json/force-download" );
                t.getResponseHeaders().add( "Content-Disposition", "attachment; filename=" + filename );

                status = 200;
            }

            HttpUtil.SendResponse( t, status, response );
        }
        catch( Exception e ) {
            logger.error( "Unable to export entities or data.");
            logger.error( e.toString(), e );
        }

    }
}

