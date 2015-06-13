package io.agi.world;

import io.agi.coordinator.ServletContextHandlerControl;
import io.agi.coordinator.ServletContextHandlerData;
import io.agi.http.StaticServletServer;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;

import java.util.ArrayList;

/**
 *
 * This server serves the content for the Web UI client.
 * It also receives input from the Web UI client to interact with the CoordiantorServer.
 *
 * Created by gideon on 13/06/15.
 */
public class WebUIServer extends StaticServletServer {

    // todo: this can be refactored so that it is not duplicated in each inheriting StaticServletServer
    public static void run(  ArrayList< String > files ) {
        WebUIServer s = new WebUIServer();
        s.setProperties( files );
        s.setup();
        s.run();
    }

    public void addServletContextHandlers( ContextHandlerCollection chc ) {

    }
}
