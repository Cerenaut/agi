package io.agi.ef.agent.services;


import io.agi.ef.coordinator.CoordinatorClientServer;
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
    public CoordinatorClientServer _coordClientServer = null;

    @Override
    public Response controlRunGet()
            throws NotFoundException {

        _coordClientServer.run();

        List< TStamp > tsl = Utils.currentServerTimeStamp( _coordClientServer.getTime() );
        return Response.ok().entity( tsl ).build();
    }

    @Override
    public Response controlStepGet()
            throws NotFoundException {

        _coordClientServer.step();

        List< TStamp > tsl = Utils.currentServerTimeStamp( _coordClientServer.getTime() );

        _logger.log( Level.INFO, "Agent received network step.\nResponse is {0}", tsl );
        return Response.ok().entity( tsl ).build();
    }

    @Override
    public Response controlStopGet()
            throws NotFoundException {

        _coordClientServer.stop();

        List< TStamp > tsl = Utils.currentServerTimeStamp( _coordClientServer.getTime() );
        return Response.ok().entity( tsl ).build();
    }

}
