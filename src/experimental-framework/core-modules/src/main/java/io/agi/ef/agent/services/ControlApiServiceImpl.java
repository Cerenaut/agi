package io.agi.ef.agent.services;


import io.agi.ef.agent.Agent;
import io.agi.ef.core.Utils;
import io.agi.ef.serverapi.api.*;

import io.agi.ef.serverapi.model.TStamp;

import java.util.List;

import io.agi.ef.serverapi.api.NotFoundException;

import javax.ws.rs.core.Response;


public class ControlApiServiceImpl extends ControlApiService {

    @Override
    public Response controlRunGet()
            throws NotFoundException {

        List< TStamp > tsl = Utils.currentServerTimeStamp( Agent._sTime );
        return Response.ok().entity( tsl ).build();
    }

    @Override
    public Response controlStepGet()
            throws NotFoundException {

        ++Agent._sTime;

        List< TStamp > tsl = Utils.currentServerTimeStamp( Agent._sTime );

        System.out.println( "Agent received step.\nResponse is " + tsl );
        return Response.ok().entity( tsl ).build();
    }

    @Override
    public Response controlStopGet()
            throws NotFoundException {

        List< TStamp > tsl = Utils.currentServerTimeStamp( Agent._sTime );
        return Response.ok().entity( tsl ).build();
    }

}
