package io.agi.ef.http.servlets;

import io.agi.ef.http.ServletUtil;
import io.agi.ef.http.node.Node;
import io.agi.ef.interprocess.coordinator.Coordinator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Receives events concerning Data and passes to the Node.
 *
 * Usage: /data?name=xxx&action=yyy
 *
 * Created by dave on 17/09/15.
 */
public class DataEventServlet extends HttpServlet {

    public static final String PATHSPEC = "/data";

    public static final String PARAMETER_NAME = "name";
    public static final String PARAMETER_ACTION = "action";

    public HashSet< String > _requiredParameters = new HashSet<>();

    public DataEventServlet() {
        _requiredParameters.add( PARAMETER_NAME );
        _requiredParameters.add( PARAMETER_ACTION );
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost( request, response ); // make GET behave as POST for convenience e.g. calling from browser
    }

    @Override
    protected void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException {

        // 1. Check parameters all provided.
        HashMap<String, String> parameters = ServletUtil.getParameters(request);

        if( !ServletUtil.hasParameters(parameters, _requiredParameters) ) {
            ServletUtil.createResponse( response, HttpServletResponse.SC_BAD_REQUEST );
            return;
        }

        // handle any old_entities waiting on this event.
        String entity = parameters.get(DataEventServlet.PARAMETER_NAME);
        String action = parameters.get(DataEventServlet.PARAMETER_ACTION);

        Coordinator c = Coordinator.getInstance();
        c.onDataEvent(entity, action);

        ServletUtil.createResponse( response, HttpServletResponse.SC_OK );
    }
}

