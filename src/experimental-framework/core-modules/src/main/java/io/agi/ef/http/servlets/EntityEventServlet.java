package io.agi.ef.http.servlets;

import io.agi.ef.http.node.Node;
import io.agi.ef.http.ServletUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Receives events concerning Entities. The events are handled by the Node.
 *
 * Usage: /entity?name=xxx&action=yyy
 *
 * Created by dave on 11/09/15.
 */
public class EntityEventServlet extends HttpServlet {

    public static final String PATHSPEC = "/entity";

    public static final String PARAMETER_NAME = "name";
    public static final String PARAMETER_ACTION = "action";

    public HashSet< String > _requiredParameters = new HashSet<>();

    public EntityEventServlet() {
        _requiredParameters.add(PARAMETER_NAME);
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
        String entity = parameters.get(EntityEventServlet.PARAMETER_NAME);
        String action = parameters.get(EntityEventServlet.PARAMETER_ACTION);

        Node n = Node.getInstance();
        n.onEntityEvent(entity, action);

        ServletUtil.createResponse( response, HttpServletResponse.SC_OK );
    }
}

