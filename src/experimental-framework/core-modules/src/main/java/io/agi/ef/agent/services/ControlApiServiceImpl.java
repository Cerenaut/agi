package io.agi.ef.agent.services;


import io.agi.ef.agent.Agent;
import io.agi.ef.core.Utils;
import io.agi.ef.serverapi.api.*;

import io.agi.ef.serverapi.model.TStamp;

import java.util.List;

import io.agi.ef.serverapi.api.NotFoundException;

import javax.ws.rs.core.Response;


public class ControlApiServiceImpl extends ControlApiService {

    public Agent _agent = null;

    @Override
    public Response controlRunGet()
            throws NotFoundException {

        _agent.run();

        List< TStamp > tsl = Utils.currentServerTimeStamp( _agent.getTime() );
        return Response.ok().entity( tsl ).build();
    }

    @Override
    public Response controlStepGet()
            throws NotFoundException {

        _agent.step();

        List< TStamp > tsl = Utils.currentServerTimeStamp( _agent.getTime() );

        System.out.println( "Agent received step.\nResponse is " + tsl );
        return Response.ok().entity( tsl ).build();
    }

    @Override
    public Response controlStopGet()
            throws NotFoundException {

        _agent.stop();

        List< TStamp > tsl = Utils.currentServerTimeStamp( _agent.getTime() );
        return Response.ok().entity( tsl ).build();
    }

}
