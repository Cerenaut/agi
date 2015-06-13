package io.agi.coordinator;

import io.agi.http.StaticServletServer;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;

import java.util.ArrayList;

/**
 *
 * This is the central controller for the AGI Experimental Framework (AGI-EF).
 * It is a RESTful Server that interacts with
 *  - AGI-EF UI front ends, as well as
 *  - AGI-EF Agents
 *  - AGI-EF Worlds
 *
 * Created by gideon on 13/06/15.
 */
public class CoordinatorServer extends StaticServletServer {

    // todo: this can be refactored so that it is not duplicated in each inheriting StaticServletServer
    public static void run(  ArrayList< String > files ) {
        CoordinatorServer s = new CoordinatorServer();
        s.setProperties( files );
        s.setup();
        s.run();
    }

    public void addServletContextHandlers( ContextHandlerCollection chc ) {
        ServletContextHandlerControl schc = new ServletContextHandlerControl( chc );
        ServletContextHandlerData schd = new ServletContextHandlerData( chc );
    }
}
