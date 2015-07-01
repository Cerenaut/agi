package io.agi.ef.coordinator.services;

import io.agi.ef.coordinator.Coord;
import io.swagger.api.*;

import io.swagger.api.ControlApi;
import io.swagger.client.*;
import io.swagger.client.ApiException;
import io.swagger.client.api.*;
import io.swagger.model.TStamp;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import io.swagger.api.NotFoundException;

import javax.ws.rs.core.Response;

public class ControlApiServiceImpl extends ControlApiService {

    @Override
    public Response controlRunGet()
            throws NotFoundException {

        TStamp tstamp = new TStamp();
        tstamp.setTimeId( new BigDecimal( "2" ) );

        ArrayList<TStamp> tsl = new ArrayList<TStamp>();
        tsl.add(tstamp);
        tstamp.setTimeId( new BigDecimal( "3" ) );
        tsl.add( tstamp );

        return Response.ok().entity( tsl ).build();
    }

    @Override
    public Response controlStepGet()
            throws NotFoundException {

        Coord coord = Coord.getInstance();

        // step the world
        ApiClient agent = coord.getWorld();
        io.swagger.client.api.ControlApi capi = new io.swagger.client.api.ControlApi( agent );
        try {
            capi.controlStepGet();
        }
        catch ( ApiException e ) {
            e.printStackTrace();
        }

        // step the agents
        for ( ApiClient client : coord.getAgents() ) {
            capi = new io.swagger.client.api.ControlApi( client );
            try {
                capi.controlStepGet();
            }
            catch ( io.swagger.client.ApiException e ) {
                e.printStackTrace();
            }
        }

        return Response.ok().entity( new ApiResponseMessage( ApiResponseMessage.OK, "sent request to step the world and agents!" ) ).build();
    }

    @Override
    public Response controlStopGet()
            throws NotFoundException {
        // do some magic!
        return Response.ok().entity( new ApiResponseMessage( ApiResponseMessage.OK, "c magic!" ) ).build();
    }

}
