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
import io.agi.framework.Node;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Map;

/**
 * Created by dave on 18/07/16.
 */
public class HttpStopHandler implements HttpHandler {

    protected static final Logger logger = LogManager.getLogger();

    public static final String CONTEXT = "/stop";

    public Node _n;

    public HttpStopHandler( Node n ) {
        _n = n;
    }

    @Override
    public void handle( HttpExchange t ) throws IOException {
        String response = "Bad request.";
        int status = 400;

        try {
            _n.stop();

            status = 200;
            response = "Stopping node...";
        }
        catch( Exception e ) {
            logger.error( e.getStackTrace() );
        }

        HttpUtil.SendResponse( t, status, response );
    }
}