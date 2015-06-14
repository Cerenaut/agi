/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package io.agi.http;

import io.agi.properties.AGIProperties;

import java.awt.*;
import java.util.ArrayList;

import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;

import javax.activation.MimeType;

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
    public static final String PROPERTY_APIDOCS_CONTEXT = "apiDocs";
    public static final String PROPERTY_STATIC_DIRECTORY = "staticDirectory";

    public static final String DEFAULT_SERVLET_CONTEXT = "/";
    public static final String DEFAULT_STATIC_CONTEXT = "/";
    public static final String DEFAULT_APIDOCS_CONTEXT = "/api";
    public static final String DEFAULT_STATIC_DIRECTORY = "./www";

    org.eclipse.jetty.server.Server _s;
    int _port;
    String _servletContext;
    String _staticContext;
    String _apiDocsContext;
    String _staticDirectory;


    private ContextHandlerCollection _contextHandlers = null;

    public void setProperties( ArrayList< String > propertiesFiles ) {
        _port = DEFAULT_PORT;
        _servletContext  = DEFAULT_SERVLET_CONTEXT;
        _staticContext   = DEFAULT_STATIC_CONTEXT;
        _apiDocsContext  = DEFAULT_APIDOCS_CONTEXT;
        _staticDirectory = DEFAULT_STATIC_DIRECTORY;
        
        for( String file : propertiesFiles ) {
            _port = Integer.valueOf( AGIProperties.get( file, PROPERTY_PORT, String.valueOf( _port ) ) );
            _servletContext  = AGIProperties.get( file, PROPERTY_SERVLET_CONTEXT, _servletContext );
            _staticContext   = AGIProperties.get( file, PROPERTY_STATIC_CONTEXT, _staticContext );
            _apiDocsContext  = AGIProperties.get( file, PROPERTY_APIDOCS_CONTEXT, _apiDocsContext );
            _staticDirectory = AGIProperties.get( file, PROPERTY_STATIC_DIRECTORY, _staticDirectory );
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
            _contextHandlers = new ContextHandlerCollection();
            _s.setHandler( _contextHandlers );

            // add file handling context:
            ResourceHandler rh = new ResourceHandler();
            rh.setDirectoriesListed( true );
            rh.setWelcomeFiles( new String[]{ "index.html" } );
            rh.setResourceBase( _staticDirectory );

            ContextHandler ch = new ContextHandler();
            ch.setContextPath( _staticContext );
            ch.setClassLoader( Thread.currentThread().getContextClassLoader() );
            ch.setHandler( rh );
            _contextHandlers.addHandler( ch );

            // add file describing Swagger API Spec
            ResourceHandler rh1 = new ResourceHandler();
            rh1.setDirectoriesListed( true );
            //rh1.setWelcomeFiles( new String[]{ "swagger.json" } );
            rh1.setResourceBase( _staticDirectory );

            MimeTypes mimeTypes=new MimeTypes();
            mimeTypes.addMimeMapping("json","application/json");
            rh1.setMimeTypes( mimeTypes );

            ContextHandler ch1 = new ContextHandler();
            ch1.setContextPath( _apiDocsContext );
            ch1.setClassLoader( Thread.currentThread().getContextClassLoader() );
            ch1.setHandler( rh1 );
            _contextHandlers.addHandler( ch1 );

            // add servlet contexts:
            ServletContextHandler shc = new ServletContextHandler( _contextHandlers, _servletContext );
        }
        catch( Exception e ) {
            e.printStackTrace();
            System.exit( -1 );
        }
        
        addServlets();
        addServletContextHandlers( _contextHandlers );
    }

    public void addServlets() {
    }

    public void addServletContextHandlers( ContextHandlerCollection chc ) {
    }
}
