package io.agi.ef.core.network.entities.services;


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

    private static final Logger _logger = Logger.getLogger( ControlApiServiceImpl.class.getName() + "-agent" );
    public AbstractEntity _entity = null;

    @Override
    public Response controlRunGet()
            throws NotFoundException {

        _entity.run();

        List< TStamp > tsl = Utils.currentServerTimeStamp( _entity.getTime() );
        return Response.ok().entity( tsl ).build();
    }

    @Override
    public Response controlStepGet()
            throws NotFoundException {

        _entity.step();

        List< TStamp > tsl = Utils.currentServerTimeStamp( _entity.getTime() );

        _logger.log( Level.INFO, "Agent received network step.\nResponse is {0}", tsl );
        return Response.ok().entity( tsl ).build();
    }

    @Override
    public Response controlStopGet()
            throws NotFoundException {

        _entity.stop();

        List< TStamp > tsl = Utils.currentServerTimeStamp( _entity.getTime() );
        return Response.ok().entity( tsl ).build();
    }

}
