package io.agi.ef.coordinator.services;

import io.swagger.api.*;

import io.swagger.model.TStamp;

import java.math.BigDecimal;
import java.util.ArrayList;

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
        // do some magic!
        return Response.ok().entity( new ApiResponseMessage( ApiResponseMessage.OK, "c magic!" ) ).build();
    }

    @Override
    public Response controlStopGet()
            throws NotFoundException {
        // do some magic!
        return Response.ok().entity( new ApiResponseMessage( ApiResponseMessage.OK, "c magic!" ) ).build();
    }

}
