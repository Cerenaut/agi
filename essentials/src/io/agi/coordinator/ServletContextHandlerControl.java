package io.agi.coordinator;

import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 *
 * The servlet context for communications pertaining to Control.
 * This includes receiving control signals from UI, and then sending appropriate messages to Agent and World.
 *
 * Creates the ServletContextHandler and add the necessary Servlets.
 *
 * Created by gideon on 13/06/15.
 */
public class ServletContextHandlerControl {

    ServletContextHandler _sch = null;

    public ServletContextHandlerControl( ContextHandlerCollection contextHandlers ) {

        _sch = new ServletContextHandler(  contextHandlers,
                "/control",
                ServletContextHandler.SESSIONS);

        _sch.addServlet( new ServletHolder( new ServletControl( "sent step" ) ), "/step/*" );
        _sch.addServlet( new ServletHolder( new ServletControl( "sent start" ) ), "/start/*" );
        _sch.addServlet( new ServletHolder( new ServletControl( "sent stop" ) ), "/stop/*" );
    }
}
