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

import org.apache.commons.fileupload.FileItem;

import org.apache.commons.fileupload.RequestContext;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.agi.framework.Framework;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;


/**
 * Created by dave on 2/04/16.
 */
public class HttpImportHandler implements HttpHandler {

    protected static final Logger logger = LogManager.getLogger();

    public static final String CONTEXT = "/import";

    public HttpImportHandler() {
    }

    @Override
    public void handle( HttpExchange t ) throws IOException {

        int status = 400;
        String response = "";

        //for(Map.Entry<String, List<String> > header : t.getRequestHeaders().entrySet()) {
        //    System.out.println(header.getKey() + ": " + header.getValue().get(0));
        //}

        // Based on accepted answer: http://stackoverflow.com/questions/33732110/file-upload-using-httphandler
        DiskFileItemFactory d = new DiskFileItemFactory();

        try {
            ServletFileUpload up = new ServletFileUpload( d );
            List< FileItem > result = up.parseRequest( new RequestContext() {

                @Override
                public String getCharacterEncoding() {
                    return "UTF-8";
                }

                @Override
                public int getContentLength() {
                    return 0; //tested to work with 0 as return
                }

                @Override
                public String getContentType() {
                    return t.getRequestHeaders().getFirst("Content-type");
                }

                @Override
                public InputStream getInputStream() throws IOException {
                    return t.getRequestBody();
                }

            } );

            for( FileItem fi : result ) {

                //System.out.println("File-Item: " + fi.getFieldName() + " = " + fi.getName());

                String value = fi.getString();

                if( value == null ) {
                    continue;
                }

                //System.out.print( value );

                String fieldName = fi.getFieldName();

                if( fieldName.equalsIgnoreCase( "entity-file" ) ) {
                    Framework.ImportEntities( value );
                    status = 200;
                    response = response + "Imported Entities from: " + fi.getName() + "\n";

                    logger.info( "Import: entities file: " + fi.getName() );
                }
                else if( fieldName.equalsIgnoreCase( "data-file" ) ) {
                    Framework.ImportData( value );
                    status = 200;
                    response = response + "Imported Data from: " + fi.getName() + "\n";

                    logger.info( "Import: data file: " + fi.getName() );
                }
            }

        }
        catch( Exception e ) {
            logger.error( "Unable to import entities/data.");
            logger.error( e.toString(), e );
        }

        HttpUtil.SendResponse( t, status, response );
    }
}