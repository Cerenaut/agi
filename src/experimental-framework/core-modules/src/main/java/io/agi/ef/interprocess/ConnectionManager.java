package io.agi.ef.interprocess;

import com.sun.jersey.spi.container.servlet.ServletContainer;
import io.agi.ef.clientapi.*;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlets.CrossOriginFilter;

import javax.servlet.DispatcherType;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.logging.Level.*;

/**
 *
 * Manage Servers and Clients.
 * This class enables the creation and running of the AGIEF experiment server.
 * It also enables connecting to other experiment servers - in the form of Agents and Worlds.
 *
 * Created by gideon on 1/07/15.
 */
public class ConnectionManager {

    protected static Logger _logger = Logger.getLogger( ConnectionManager.class.getClass().getPackage().getName() );
    private static ConnectToServers _connectToServers = null;
    private HashSet< ServerConnection > _servers = new HashSet<>(  );
    private ArrayList< ConnectionManagerListener > _listeners = new ArrayList<>(  );
    public void addListener( ConnectionManagerListener cm ) {
        _listeners.add( cm );
    }
    public void removeListener( ConnectionManagerListener cm ) {
        _listeners.remove( cm );
    }

    /**
     * Manage connections to servers.
     * This class is created to run in a thread, to regularly attempt to connect to the registered servers.
     */
    public static class ConnectToServers implements Runnable {

        private ConnectionManager _cm = null;

        ConnectToServers ( ConnectionManager cm ) {
            _cm = cm;
        }

        @Override
        public void run() {
            while ( true ) {

                for ( ServerConnection sc : _cm._servers ) {

                    // System.out.println( "ConnectToServers:run(): process sc: " + sc );

                    if ( sc.getClientApi() == null ) {

                        _logger.log( FINE, "Try to connect to server sc: {0}", sc );

                        try {
                            connectToServer( sc );
                        }
                        catch ( ApiException e ) {
                            e.printStackTrace();
                        }

                    }
                }

                try {
                    Thread.sleep( 500 );    // every 500 milliseconds
                }
                catch ( InterruptedException e ) {
                    e.printStackTrace();
                }
            }
        }

        private void connectToServer( ServerConnection sc ) throws ApiException {

            String basePath = sc.basePath();
            _logger.log( FINE, "ConnectToServer: ApiClient basepath = {0}", basePath );

            ApiClient apiClient = new ApiClient();
            apiClient.setBasePath( basePath );

            sc.setClient( apiClient );

            // todo: This ISN'T actually connected, just sets it up for requests. Is this pattern relevant? It is extensible and flexible.

            for ( ConnectionManagerListener cml : _cm._listeners ) {
                cml.connectionAccepted( sc );
            }
        }
    }


    public ConnectionManager() {
        _logger = Logger.getLogger( this.getClass().getPackage().getName() );
        _connectToServers = new ConnectToServers( this );
        new Thread( _connectToServers ).start();
    }


    /**
     * Register Server to try to connect to it.
     */
    public ServerConnection registerServer( String host, int port, String contextPath ) {

        _logger.log( FINE, "Register at host: {0}, port: {1}, and contextPath: {2}", new Object[] { host,
                                                                                                    port,
                                                                                                    contextPath } );
        ServerConnection sc = new ServerConnection( host, contextPath, port );
        _servers.add( sc ) ;

        return sc;
    }

    /**
     * Set up AGIEF server. It accepts clients, and receives requests to become a client of other servers (Agents and Worlds).
     * @param contextPath does not include slashes
     * @param port
     * @return
     */
    public Server setupServer( String contextPath, int port ) {

        _logger.log( Level.FINE, "Setup server at contextpath: {0}, listenerPort: {1}. Basepath = {2}",
                new Object[]{   contextPath,
                                port,
                                EndpointUtils.basePath( "localhost", port, contextPath )} );

        // setup server with jetty and jersey
        ServletHolder sh = new ServletHolder( ServletContainer.class );
        sh.setInitParameter( "com.sun.jersey.config.property.resourceConfigClass", "com.sun.jersey.api.core.PackagesResourceConfig" );
        sh.setInitParameter( "com.sun.jersey.config.property.packages", "io.agi.ef.serverapi" ); //Set the package where the services reside
        sh.setInitParameter( "com.sun.jersey.api.json.POJOMappingFeature", "true" );

        Server server = new Server( port );

        ServletContextHandler context = new ServletContextHandler( server, "/" + contextPath, ServletContextHandler.SESSIONS );
        context.addServlet( sh, "/*" );

        boolean addCrossFilter = true;
        if ( addCrossFilter ) {
            // Add the filter, and then use the provided FilterHolder to configure it
            FilterHolder cors = context.addFilter( CrossOriginFilter.class, "/*", EnumSet.of( DispatcherType.REQUEST ) );
            cors.setInitParameter( CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "*" );
            cors.setInitParameter( CrossOriginFilter.ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, "*" );
            cors.setInitParameter( CrossOriginFilter.ALLOWED_METHODS_PARAM, "GET,POST,HEAD" );
            cors.setInitParameter( CrossOriginFilter.ALLOWED_HEADERS_PARAM, "X-Requested-With,Content-Type,Accept,Origin" );

            sh.getServletHandler().addFilter( cors );
        }

        boolean serveStaticFiles = true;
        if ( serveStaticFiles ) {

            String pwdPath = System.getProperty( "user.dir" );
            pwdPath = pwdPath + "/html/";

            // Use a DefaultServlet to serve static files. Alternate Holder technique, prepare then add.
            ServletHolder def = new ServletHolder( "default", DefaultServlet.class );     // DefaultServlet should be named 'default'
            def.setInitParameter( "resourceBase", pwdPath );
            def.setInitParameter( "dirAllowed", "true" );
            context.addServlet( def, "/" );
        }

        return server;
    }

}
