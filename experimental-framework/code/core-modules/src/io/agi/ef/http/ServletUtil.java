package io.agi.ef.http;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Convenience functions for using Jetty Servlets.
 *
 * Created by dave on 11/09/15.
 */
public class ServletUtil {

    /**
     * Returns true if all the "required" parameters are present.
     * @param parameters
     * @param required
     * @return
     */
    public static boolean hasParameters( HashMap< String, String > parameters, HashSet< String > required ) {
        Set< String > provided = parameters.keySet();

        for( String parameter : required ) {
            if( !provided.contains( parameter ) ) {
                return false;
            }
        }

        return true;
    }

    /**
     * Produces a parameter-list as a String from the HashMap of key-value pairs provided.
     * @param parameters
     * @return
     */
    public static String setParameters( HashMap< String, String > parameters ) {
        String s = "";

        boolean first = true;

        Set<Map.Entry< String, String >> es = parameters.entrySet();

        for( Map.Entry< String, String > e : es ) {
            if( first ) {
                s = s + "?";
            }
            else {
                s = s + "&";
            }
            String key = e.getKey();
            String value = e.getValue();
            s = s + key + "=" + value;

            first = false;
        }

        return s;
    }

    /**
     * Retrieves all parameters from a request as a HashMap.
     * @param request
     * @return
     */
    public static HashMap< String, String > getParameters( HttpServletRequest request ) {

        HashMap< String, String > hm = new HashMap< String, String >();

        Map< String, String[] > m = request.getParameterMap();

        Set<Map.Entry< String, String[] >> es = m.entrySet();

        for( Map.Entry< String, String[] > e : es ) {
            String key = e.getKey();
            String[] value = e.getValue();

            hm.put( key, value[ 0 ] );
        }

        return hm;
    }

    /**
     * Create a response with data type of JSON.
     * @param response
     * @param status
     * @param body
     * @throws ServletException
     * @throws IOException
     */
    public static void createResponseJson( HttpServletResponse response, int status, String body ) throws ServletException, IOException {
        createResponse( response, status, "application/json;charset=utf-8", body );
    }

    /**
     * Create a text/html response.
     * @param response
     * @param status
     * @param body
     * @throws ServletException
     * @throws IOException
     */
    public static void createResponseText( HttpServletResponse response, int status, String body ) throws ServletException, IOException {
        createResponse( response, status, "text/html;charset=utf-8", body );
    }

    /**
     * Creates a text/html response without any body data. Conveniently short arg list.
     * @param response
     * @param status
     * @throws ServletException
     * @throws IOException
     */
    public static void createResponse( HttpServletResponse response, int status ) throws ServletException, IOException {
        createResponseText( response, status, null );
    }

    /**
     * Create a response specifying contentType, and body data, and status.
     * The "Access-Control-Allow-Origin" flag is set.
     *
     * @param response
     * @param status
     * @param contentType
     * @param body
     * @throws ServletException
     * @throws IOException
     */
    public static void createResponse( HttpServletResponse response, int status, String contentType, String body ) throws ServletException, IOException {
        response.setContentType( contentType );
        response.setStatus( status );
        response.addHeader( "Access-Control-Allow-Origin", "*" ); // dangerous, allows all URLs to call these

        if( body == null ) {
            return;
        }

        try {
            PrintWriter out = response.getWriter();
            out.println( body );
            out.close();
        }
        catch( IOException ioe ) {}
    }
}
