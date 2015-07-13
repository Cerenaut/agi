package io.agi.ef.coordinator;

import com.sun.jersey.spi.container.servlet.ServletContainer;
import io.agi.ef.clientapi.ApiClient;
import io.agi.ef.coordinator.services.ControlApiServiceImpl;
import io.agi.ef.coordinator.services.DataApiServiceImpl;
import io.agi.ef.serverapi.api.factories.ControlApiServiceFactory;
import io.agi.ef.serverapi.api.factories.DataApiServiceFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlets.CrossOriginFilter;

import javax.servlet.DispatcherType;
import java.util.EnumSet;


public class Main {

    public static int PORT = 9999;

    public static void main(String[] args) throws Exception {

        setupClients();

        Server server = setupServer( PORT );
        server.start();
        server.join();
    }

    private static Server setupServer( int port ) {
        // inject service implementations to be used by the server lib
        DataApiServiceFactory.setService( new DataApiServiceImpl() );
        ControlApiServiceFactory.setService( new ControlApiServiceImpl() );

        // setup server with jetty and jersey
        ServletHolder sh = new ServletHolder(ServletContainer.class);
        sh.setInitParameter("com.sun.jersey.config.property.resourceConfigClass", "com.sun.jersey.api.core.PackagesResourceConfig");
        sh.setInitParameter( "com.sun.jersey.config.property.packages", "io.agi.ef.serverapi" );//Set the package where the services reside
        sh.setInitParameter( "com.sun.jersey.api.json.POJOMappingFeature", "true" );

        Server server = new Server( port );

        ServletContextHandler context = new ServletContextHandler( server, "/", ServletContextHandler.SESSIONS );
        context.addServlet( sh, "/*" );

        // Add the filter, and then use the provided FilterHolder to configure it
        FilterHolder cors = context.addFilter( CrossOriginFilter.class,"/*", EnumSet.of( DispatcherType.REQUEST ) );
        cors.setInitParameter( CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "*" );
        cors.setInitParameter( CrossOriginFilter.ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, "*" );
        cors.setInitParameter( CrossOriginFilter.ALLOWED_METHODS_PARAM, "GET,POST,HEAD" );
        cors.setInitParameter( CrossOriginFilter.ALLOWED_HEADERS_PARAM, "X-Requested-With,Content-Type,Accept,Origin" );

//        sh.getServletHandler().addFilter( cors );
//
//        // Use a DefaultServlet to serve static files. Alternate Holder technique, prepare then add.
//        ServletHolder def = new ServletHolder( "default", DefaultServlet.class );     // DefaultServlet should be named 'default'
//        def.setInitParameter( "resourceBase","./http/" );
//        def.setInitParameter( "dirAllowed", "false" );
//        context.addServlet( def, "/" );

        return server;
    }

    private static void setupClients() {

        Coord coord = Coord.getInstance();

        // todo : create a new ApiClient for each connection, rather than one for world and agent
        ApiClient world = new ApiClient();
        world.setBasePath( "http://localhost:9988" );
        world.setUserAgent( "Coord" );

        ApiClient agent = new ApiClient();
        agent.setBasePath( "http://localhost:6666" );
        agent.setUserAgent( "Coord" );

        coord.addWorldClient( world );
        coord.addAgentClient( agent );
    }
}
