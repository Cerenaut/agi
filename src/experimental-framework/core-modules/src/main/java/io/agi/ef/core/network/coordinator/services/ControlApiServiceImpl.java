package io.agi.ef.core.network.coordinator.services;

import io.agi.ef.core.apiInterfaces.ControlInterface;
import io.agi.ef.core.network.coordinator.Coordinator;
import io.agi.ef.serverapi.api.*;

import io.agi.ef.serverapi.api.NotFoundException;

import javax.ws.rs.core.Response;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ControlApiServiceImpl extends ControlApiService {

    public ControlInterface _serviceDelegate = null;

    @Override
    public Response controlRunGet()
            throws NotFoundException {

        return _serviceDelegate.run();
    }

    @Override
    public Response controlStepGet()
            throws NotFoundException {

        return _serviceDelegate.step();
    }

    @Override
    public Response controlStopGet()
            throws NotFoundException {

        return _serviceDelegate.stop();
    }

}
