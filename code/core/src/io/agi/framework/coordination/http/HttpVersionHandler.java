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
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.agi.framework.Main;
import io.agi.framework.persistence.Persistence;
import io.agi.framework.persistence.models.ModelNode;
import io.agi.framework.persistence.models.ModelVersion;

import java.io.IOException;
import java.util.Collection;

/**
 * Created by dave on 17/03/16.
 */
public class HttpVersionHandler implements HttpHandler {

    public static final String CONTEXT = "/version";


    public HttpVersionHandler() {
    }

    @Override
    public void handle( HttpExchange t ) throws IOException {
        int status = 400;

        ModelVersion modelVersion = new ModelVersion();

        String version = Main.getPackageVersion();

        if ( version != null ) {
            modelVersion.version = version;
            status = 200;
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String response = gson.toJson( modelVersion );

        HttpUtil.SendResponse( t, status, response );
    }
}
