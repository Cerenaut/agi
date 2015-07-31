package io.agi.ef.core;

import com.sun.jersey.spi.container.servlet.ServletContainer;
import io.agi.ef.clientapi.*;
import org.eclipse.jetty.server.Server;
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
 * This class enables the creation and running of the AGIEF core server.
 * It also enables connecting to other core servers - in the form of Agents and Worlds.
 *
 * Created by gideon on 1/07/15.
 */
public class ConnectionManager {

    public interface ConnectionManagerListener {
        void connectionAccepted( ServerConnection sc ) throws ApiException;
    }

    private static final Logger _logger = Logger.getLogger( ConnectionManager.class.getName() );
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
     * Data structure to encapsulate information about a connection, as a client, to an AGIEF core server.
     * It may not yet be established.
     */
    public static class ServerConnection {

        private ApiClient client;

        public enum ServerType {
            Agent,
            World,
            Coordinator
        }

        static public int _sCount = 0;

        private int _id = ++_sCount;             // unique id
        private ServerType _type = null;         // which 'type' of server to connect to (Agents and Worlds are types of AGIEF core servers)
        private int _port = -1;
        private String _contextPath = null;      // the context that appears after the base basic url and port name. Together with _contextPath you have the 'basepath'. DO NOT include preceding or trailing slashes.
        private ApiClient _client = null;        // data structure that enables making requests


        public ServerConnection( ServerType type, int port, String contextPath ) {
            _type = type;
            _port = port;
            _contextPath = contextPath;
        }

        public String basePath() {
            return EndpointUtils.basePath( _port, _contextPath );
        }

        public void setClient( ApiClient client ) {
            _client = client;
        }

        public ApiClient getClientApi() {
            return _client;
        }

        public Object getId() {
            return _id;
        }

        @Override
        public String toString() {
            String str = "[id:" + _id + "], type:(" + _type + "), port:(" + _port + "), contextPath:(" + _contextPath + "), clientApi:(" + _client + ")";
            return str;
        }
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


            // todo: this ISN'T actually connected yet, just sets it up need to test before calling back to listener

            for ( ConnectionManagerListener cml : _cm._listeners ) {
                cml.connectionAccepted( sc );
            }
        }
    }


    public ConnectionManager() {
        _connectToServers = new ConnectToServers( this );
        new Thread( _connectToServers ).start();
    }


    /**
     * Register Server to try to connect to it.
     *
     * @param type
     * @param contextPath
     */
    public ServerConnection registerServer( ServerConnection.ServerType type, int port, String contextPath ) {

        _logger.log( FINE, "Register server type: {0}, at contextPath: {1}", new Object[]{ type , contextPath } );

        ServerConnection sc = new ServerConnection( type, port, contextPath );
        _servers.add( sc ) ;

        return sc;
    }

    /**
     * Get the list of ApiClient agents
     * @return
     */
    public HashSet< ServerConnection > getServers() {
        return _servers;
    }

    /**
     * Set up AGIEF server. It accepts clients, and receives requests to become a client of other servers (Agents and Worlds).
     * @param port
     * @param contextPath does not include slashes
     * @return
     */
    public Server setupServer( int port, String contextPath ) {

        _logger.log( Level.FINE, "Setup server on port: {0}, at contextpath: {1}. Basepath = {2}",
                new Object[]{   port,
                                contextPath,
                                EndpointUtils.basePath( port, contextPath )} );

        // setup server with jetty and jersey
        ServletHolder sh = new ServletHolder( ServletContainer.class );
        sh.setInitParameter( "com.sun.jersey.config.property.resourceConfigClass", "com.sun.jersey.api.core.PackagesResourceConfig" );
        sh.setInitParameter( "com.sun.jersey.config.property.packages", "io.agi.ef.serverapi" );//Set the package where the services reside
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
            // Use a DefaultServlet to serve static files. Alternate Holder technique, prepare then add.
            ServletHolder def = new ServletHolder( "default", DefaultServlet.class );     // DefaultServlet should be named 'default'
            def.setInitParameter( "resourceBase", "./http/" );
            def.setInitParameter( "dirAllowed", "false" );
            context.addServlet( def, "/" );
        }

        return server;
    }

}
