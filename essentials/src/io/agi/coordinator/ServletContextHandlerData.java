package io.agi.coordinator;

import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 *
 * The servlet context for communications pertaining to Data.
 * This includes requests for data to visualise by the UI, and receiving state from the Agent and World.
 *
 * Creates the ServletContextHandler and add the necessary Servlets.
 *
 * Created by gideon on 13/06/15.
 */
public class ServletContextHandlerData {

    ServletContextHandler _sch = null;

    public ServletContextHandlerData( ContextHandlerCollection contextHandlers ) {

        _sch = new ServletContextHandler(  contextHandlers,
                "/data",
                ServletContextHandler.SESSIONS);

        _sch.addServlet( new ServletHolder( new ServletData( "sent get state" ) ), "/state/*" );
    }
}
