package io.agi.ef.coordinator.services;

import io.agi.ef.clientapi.*;

import io.agi.ef.coordinator.Coord;
import io.agi.ef.serverapi.api.*;
import io.agi.ef.serverapi.api.ApiException;
import io.agi.ef.serverapi.model.*;

import com.sun.jersey.multipart.FormDataParam;

import io.agi.ef.serverapi.model.TStamp;
import io.agi.ef.serverapi.model.Error;

import java.math.BigDecimal;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;
import io.agi.ef.serverapi.api.NotFoundException;

import java.io.InputStream;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;

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
        io.agi.ef.clientapi.api.ControlApi capi = new io.agi.ef.clientapi.api.ControlApi( agent );
        try {
            capi.controlStepGet();
        }
        catch ( io.agi.ef.clientapi.ApiException e ) {
            e.printStackTrace();
        }
        // todo: this catches a connection refused exception, but should be tidied up
        catch ( Exception e ) {
            e.printStackTrace();
        }


        // step the agents
        for ( ApiClient client : coord.getAgents() ) {
            capi = new io.agi.ef.clientapi.api.ControlApi( client );
            try {
                capi.controlStepGet();
            }
            catch ( io.agi.ef.clientapi.ApiException e ) {
                e.printStackTrace();
            }
            // todo: this catches a connection refused exception, but should be tidied up
            catch ( Exception e ) {
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
