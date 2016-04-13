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

import javax.servlet.*;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.agi.framework.Framework;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

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

        for(Map.Entry<String, List<String> > header : t.getRequestHeaders().entrySet()) {
            System.out.println(header.getKey() + ": " + header.getValue().get(0));
        }

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

//            t.getResponseHeaders().add( "Content-type", "text/plain" );
//            t.sendResponseHeaders( 200, 0 );
//
//            OutputStream os = t.getResponseBody();
            for( FileItem fi : result ) {

//                os.write(fi.getName().getBytes());
//                os.write("\r\n".getBytes());
                System.out.println("File-Item: " + fi.getFieldName() + " = " + fi.getName());
            }
//            os.close();

        }
        catch( Exception e ) {
            logger.error( e.getStackTrace() );
        }

        try {
            InputStream inputStream = t.getRequestBody();
            java.util.Scanner s = new java.util.Scanner( inputStream ).useDelimiter( "\\A" );
            String subtree = s.hasNext() ? s.next() : "";
            String jsonEntities = null;
            String jsonData = null;
            boolean b = Framework.ImportSubtree( jsonEntities, jsonData );
            if( b ) {
                status = 200;
            }
        }
        catch( Exception e ) {
            e.printStackTrace();
        }

        HttpUtil.SendResponse( t, status, response );
    }
}