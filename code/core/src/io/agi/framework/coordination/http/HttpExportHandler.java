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
import io.agi.core.util.MemoryUtil;
import io.agi.framework.Framework;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by dave on 2/04/16.
 */
public class HttpExportHandler implements HttpHandler {

    protected static final Logger _logger = LogManager.getLogger();

    public static final String CONTEXT = "/export";

    public static final String PARAMETER_EXPORT_LOCATION = "export-location";

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
        String response = "empty - this should not be empty";

        try {
            String query = t.getRequestURI().getQuery();
            Map< String, String > m = HttpUtil.GetQueryParams( query );

            MemoryUtil.logMemory( _logger );

            if( ( m.containsKey( PARAMETER_TYPE ) )
                    && ( m.containsKey( PARAMETER_ENTITY ) ) ) {
                String entityName = m.get( PARAMETER_ENTITY ).trim(); // essential
                String type = m.get( PARAMETER_TYPE ).trim(); // essential
                
                if ( Framework.containsEntity( entityName ) ) {

                    // There are often memory exceptions when exporting (via API or saving to disk)
                    // We want to catch those exceptions, return an error status, and still continue
                    try {

                        String filename = "saved__" + entityName + "-" + type + ".json";
                        if( m.containsKey( PARAMETER_EXPORT_LOCATION ) ) {
                            String folderPath = m.get( PARAMETER_EXPORT_LOCATION ).trim(); // essential

                            Path filepath = Paths.get( folderPath, filename );

                            // todo check that path is valid

                            boolean success = Framework.SaveSubtree( entityName, type, filepath.toString() );

                            HashMap< String, String > responseMap = new HashMap<>();
                            responseMap.put( "entity", entityName );
                            responseMap.put( "type", type );
                            responseMap.put( "folder", folderPath );
                            responseMap.put( "filepath", filepath.toString() );

                            if( success ) {
                                status = 200;
                                responseMap.put( "message", "Success: Saved subtree" );
                            }
                            else {
                                responseMap.put( "message", "Error: Could not save subtree" );
                            }
                            response = new Gson().toJson( responseMap );

                        }
                        else {
                            response = Framework.ExportSubtree( entityName, type );
                            t.getResponseHeaders().add( "Content-type", "text/json/force-download" );
                            t.getResponseHeaders().add( "Content-Disposition", "attachment; filename=" + filename );
                            status = 200;
                        }

                        _logger.warn( "Created response" );
                        MemoryUtil.logMemory( _logger );
                    }
                    catch( Exception e ) {

                        _logger.error( "Exception trying to to export entities or data.");
                        _logger.error( e.toString(), e );

                        HashMap< String, String > responseMap = new HashMap<>();
                        responseMap.put( "message", "Error: Exception trying to to export entities or data." );
                        responseMap.put( "exception", e.toString() );
                        status = 400;

                        response = new Gson().toJson( responseMap );
                    }
                }
                else {
                    HashMap< String, String > responseMap = new HashMap<>();
                    responseMap.put( "message", "Error: Please specify both an Entity and a Type." );
                    response = new Gson().toJson( responseMap );
                }
            }

            HttpUtil.SendResponse( t, status, response );
        }
        catch( Exception e ) {
            _logger.error( "Unable to handle export entities or data.");
            _logger.error( e.toString(), e );
        }

    }
}

