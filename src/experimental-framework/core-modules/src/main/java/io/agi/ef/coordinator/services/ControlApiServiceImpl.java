package io.agi.ef.coordinator.services;

import io.agi.ef.coordinator.Coordinator;
import io.agi.ef.core.ConnectionManager;
import io.agi.ef.serverapi.api.*;

import java.util.HashMap;
import java.util.List;

import io.agi.ef.serverapi.api.NotFoundException;

import javax.ws.rs.core.Response;

public class ControlApiServiceImpl extends ControlApiService {

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

        System.out.println( "------- step -------" );

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
