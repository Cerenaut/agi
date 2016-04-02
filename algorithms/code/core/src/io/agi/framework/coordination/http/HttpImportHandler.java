package io.agi.framework.coordination.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.agi.framework.persistence.Persistence;

import java.io.IOException;

/**
 * Created by dave on 2/04/16.
 */
public class HttpImportHandler implements HttpHandler {

    public static final String CONTEXT = "/import";

    public static final String PARAMETER_ENTITY = "entity";

    public Persistence _p;

    public HttpImportHandler(Persistence p) {
        _p = p;
    }

    @Override
    public void handle( HttpExchange t ) throws IOException {
        int status = 400;
        String response = "";

        try {
            // TODO - defer to a framework method for importing an entity from JSON, including children.
        }
        catch( Exception e ) {
            e.printStackTrace();
        }

        HttpUtil.SendResponse(t, status, response);
    }
}