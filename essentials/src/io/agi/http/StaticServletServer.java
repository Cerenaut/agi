/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package io.agi.http;

import io.agi.properties.AgiProperties;
import java.util.ArrayList;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;

/**
 * A simple Web server based on Jetty.
 * 
 * Configured by .properties file[s].
 * 
 * @author dave
 */
public class StaticServletServer {

    public static final String SERVLET_CONTEXT_PATHSPEC = "/"; // xLabs Web Services
    public static final String STATIC_CONTEXT_PATHSPEC = "/"; // xLabs Web Services
    
    public static final String PROPERTY_PORT = "port"; 
    public static final int DEFAULT_PORT = 80;
    
    public static final String PROPERTY_SERVLET_CONTEXT = "servletContext";
    public static final String PROPERTY_STATIC_CONTEXT = "staticContext";
    public static final String PROPERTY_STATIC_DIRECTORY = "staticDirectory";

    public static final String DEFAULT_SERVLET_CONTEXT = "/";
    public static final String DEFAULT_STATIC_CONTEXT = "/";
    public static final String DEFAULT_STATIC_DIRECTORY = "./www";

    org.eclipse.jetty.server.Server _s;
    int _port;
    String _servletContext;
    String _staticContext;
    String _staticDirectory;

    public void setProperties( ArrayList< String > propertiesFiles ) {
        _port = DEFAULT_PORT;
        _servletContext  = DEFAULT_SERVLET_CONTEXT;
        _staticContext   = DEFAULT_STATIC_CONTEXT;
        _staticDirectory = DEFAULT_STATIC_DIRECTORY;
        
        for( String file : propertiesFiles ) {
            _port = Integer.valueOf( AgiProperties.get( file, PROPERTY_PORT, String.valueOf( _port ) ) );
            _servletContext  = AgiProperties.get( file, PROPERTY_SERVLET_CONTEXT, _servletContext );
            _staticContext   = AgiProperties.get( file, PROPERTY_STATIC_CONTEXT, _staticContext );
            _staticDirectory = AgiProperties.get( file, PROPERTY_STATIC_DIRECTORY, _staticDirectory );
        }
    }
        
    public StaticServletServer() {
        
    }

    public void run() {
        try {
            start();
        }
        catch( Exception e ){
            e.printStackTrace();
        }
    }

    public void start() throws Exception {
        _s.start();
        _s.join();
    }
    
    public void setup() {
        
        try {
            _s = new org.eclipse.jetty.server.Server( _port );
            ContextHandlerCollection contexts = new ContextHandlerCollection();
            _s.setHandler( contexts );

            // add file handling context:
            ResourceHandler rh = new ResourceHandler();
            rh.setDirectoriesListed( true );
            rh.setWelcomeFiles( new String[]{ "index.html" } );
            rh.setResourceBase( _staticDirectory );

            ContextHandler ch = new ContextHandler();
            ch.setContextPath( _staticContext );
            ch.setClassLoader( Thread.currentThread().getContextClassLoader() );
            ch.setHandler( rh );
            contexts.addHandler( ch );

            // add servlet contexts:
            ServletContextHandler shc = new ServletContextHandler( contexts, _servletContext );
        }
        catch( Exception e ) {
            e.printStackTrace();
            System.exit( -1 );
        }
        
        addServlets();
    }

    public void addServlets() {
    }
    
}
