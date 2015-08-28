package io.agi.interprocess.entity.services;


import io.agi.interprocess.apiInterfaces.ControlInterface;
import io.agi.ef.serverapi.api.*;

import io.agi.ef.serverapi.api.NotFoundException;

import javax.ws.rs.core.Response;


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
