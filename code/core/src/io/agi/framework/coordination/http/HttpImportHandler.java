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

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by dave on 2/04/16.
 */
public class HttpImportHandler implements HttpHandler {

    public static final String CONTEXT = "/import";


    public HttpImportHandler() {
    }

    @Override
    public void handle( HttpExchange t ) throws IOException {
        int status = 400;
        String response = "";

        try {
            InputStream inputStream = t.getRequestBody();
            java.util.Scanner s = new java.util.Scanner( inputStream ).useDelimiter( "\\A" );
            String subtree = s.hasNext() ? s.next() : "";
            boolean b = Framework.ImportSubtree( subtree );
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