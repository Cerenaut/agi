package io.agi.ef.core.network.entities.services;


import io.agi.ef.core.apiInterfaces.ControlInterface;
import io.agi.ef.core.network.entities.AbstractEntity;
import io.agi.ef.core.Utils;
import io.agi.ef.serverapi.api.*;

import io.agi.ef.serverapi.model.TStamp;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.agi.ef.serverapi.api.NotFoundException;

import javax.ws.rs.core.Response;


public class ControlApiServiceImpl extends ControlApiService {

    public ControlInterface _entity = null;

    @Override
    public Response controlRunGet()
            throws NotFoundException {

        return _entity.run();
    }

    @Override
    public Response controlStepGet()
            throws NotFoundException {

        return _entity.step();
    }

    @Override
    public Response controlStopGet()
            throws NotFoundException {

        return _entity.stop();
    }

}
