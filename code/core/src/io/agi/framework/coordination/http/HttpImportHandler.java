package io.agi.framework.coordination.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.agi.framework.Framework;
import io.agi.framework.persistence.Persistence;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by dave on 2/04/16.
 */
public class HttpImportHandler implements HttpHandler {

    public static final String CONTEXT = "/import";


    public HttpImportHandler( ) {
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
            if ( b ) {
                status = 200;
            }
        }
        catch ( Exception e ) {
            e.printStackTrace();
        }

        HttpUtil.SendResponse( t, status, response );
    }
}