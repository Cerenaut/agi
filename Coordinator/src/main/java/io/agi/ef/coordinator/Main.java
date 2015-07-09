package io.agi.ef.coordinator;

import com.sun.jersey.spi.container.servlet.ServletContainer;
import io.agi.ef.clientapi.ApiClient;
import io.agi.ef.coordinator.services.ControlApiServiceImpl;
import io.agi.ef.coordinator.services.DataApiServiceImpl;
import io.agi.ef.serverapi.api.factories.ControlApiServiceFactory;
import io.agi.ef.serverapi.api.factories.DataApiServiceFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;


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



        ServletContextHandler context = new ServletContextHandler(server, "/", ServletContextHandler.SESSIONS);
        context.addServlet( sh, "/*" );

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
