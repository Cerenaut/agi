package io.agi.ef.http.servlets;

import io.agi.ef.EntityFactory;
import io.agi.ef.http.ServletUtil;
import io.agi.ef.interprocess.coordinator.Coordinator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Creates an entity of the specified type, using the given config parameter as an input to the factory.
 * This allows runtime specification of entity configuration!
 *
 * Usage: /create?name=xxx&type=yyy&parent=zzz&config={ threshold:1, something:"bob" }
 *
 * The config parameter is passed straight to the Entity setup() method.
 *
 * Created by dave on 14/09/15.
 */
public class CreateServlet extends HttpServlet {

    public static final String PATHSPEC = "/create";

    public static final String PARAMETER_NAME = "name";
    public static final String PARAMETER_TYPE = "type";
    public static final String PARAMETER_PARENT = "parent";
    public static final String PARAMETER_CONFIG = "config";

    public HashSet<String> _requiredParameters = new HashSet<>();

    public CreateServlet() {
        _requiredParameters.add(PARAMETER_NAME);
        _requiredParameters.add(PARAMETER_TYPE);
        _requiredParameters.add(PARAMETER_PARENT);
        _requiredParameters.add(PARAMETER_CONFIG);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response); // make GET behave as POST for convenience e.g. calling from browser
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // 1. Check parameters all provided.
        HashMap<String, String> parameters = ServletUtil.getParameters(request);

        if (!ServletUtil.hasParameters(parameters, _requiredParameters)) {
            ServletUtil.createResponse(response, HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // handle any old_entities waiting on this event.
        String entityName = parameters.get(CreateServlet.PARAMETER_NAME);
        String entityType = parameters.get(CreateServlet.PARAMETER_TYPE);
        String parentEntityName = parameters.get(CreateServlet.PARAMETER_PARENT);
        String entityConfig = parameters.get(CreateServlet.PARAMETER_CONFIG);

        Coordinator c = Coordinator.getInstance();
        c.onCreateEvent(entityName, entityType, parentEntityName, entityConfig);

        ServletUtil.createResponse(response, HttpServletResponse.SC_OK);
    }
}
