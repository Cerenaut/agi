package io.agi.ef.coordinator.services;

import io.agi.ef.coordinator.Coordinator;
import io.agi.ef.serverapi.api.*;

import io.agi.ef.serverapi.api.NotFoundException;

import javax.ws.rs.core.Response;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ControlApiServiceImpl extends ControlApiService {

    private static final Logger _logger = Logger.getLogger( ControlApiServiceImpl.class.getName() + "-coord" );
    public Coordinator _coordinator = null;

    @Override
    public Response controlRunGet()
            throws NotFoundException {

        Response response = _coordinator.run();
        return response;
    }

    @Override
    public Response controlStepGet()
            throws NotFoundException {

        _logger.log( Level.INFO, "Coordinator received network step." );

        Response response = _coordinator.step();
        return response;
    }

    @Override
    public Response controlStopGet()
            throws NotFoundException {

        Response response = _coordinator.stop();
        return response;
    }

}
