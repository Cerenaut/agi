package io.agi.ef.agent;

import io.agi.ef.agent.services.ControlApiServiceImpl;
import io.agi.ef.agent.services.DataApiServiceImpl;
import io.agi.ef.clientapi.*;

import java.util.List;

import io.agi.ef.clientapi.api.ControlApi;
import io.agi.ef.clientapi.model.TStamp;
import io.agi.ef.serverapi.api.factories.ControlApiServiceFactory;
import io.agi.ef.serverapi.api.factories.DataApiServiceFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.sun.jersey.spi.container.servlet.ServletContainer;

/**
 * Created by gideon on 25/06/15.
 */
public class Main {

    // public port to listen on (hardcoded for poc)
    public static int PORT = 6666;

    public static void main(String[] args) throws Exception {

        // connect to Coordinator (port 9999)
        ApiClient apiClient = Configuration.getDefaultApiClient();
        apiClient.setBasePath( "http://localhost:9999" );
        apiClient.setUserAgent( "Agent" );

        // test a call
        ControlApi capi = new ControlApi(  );
        List<TStamp> tsl = capi.controlRunGet();
        for ( TStamp ts : tsl ) {
            System.out.println( ts );
        }
        System.out.println( "finished" );


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
        sh.setInitParameter( "com.sun.jersey.config.property.packages", "io.agi.ef.serverapi" ); //Set the package where the services reside
        sh.setInitParameter( "com.sun.jersey.api.json.POJOMappingFeature", "true" );

        Server server = new Server( port );
        ServletContextHandler context = new ServletContextHandler(server, "/", ServletContextHandler.SESSIONS);
        context.addServlet( sh, "/*" );

        return server;
    }

}