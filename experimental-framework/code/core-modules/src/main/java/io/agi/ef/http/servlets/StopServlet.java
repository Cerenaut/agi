package io.agi.ef.http.servlets;

import io.agi.ef.http.node.NodeServer;
import io.agi.ef.http.ServletUtil;
import io.agi.ef.interprocess.coordinator.Coordinator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Stops the Node with a graceful shutdown.
 *
 * The default pathspec is /stop without any arguments.
 *
 * Created by dave on 12/09/15.
 */
public class StopServlet extends HttpServlet {

    public static final String PATHSPEC = "/stop";

    public StopServlet() {
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost( request, response ); // make GET behave as POST for convenience e.g. calling from browser
    }

    @Override
    protected void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException {

        ServletUtil.createResponse( response, HttpServletResponse.SC_OK );

        Coordinator c = Coordinator.getInstance();
        c.onStopEvent();
    }
}

