package io.agi.ef.coordinator.services;

import io.agi.ef.ConnectionManager;
import io.agi.ef.serverapi.api.*;

import io.agi.ef.serverapi.model.TStamp;

import java.math.BigDecimal;
import java.util.ArrayList;

import io.agi.ef.serverapi.api.NotFoundException;

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

        ConnectionManager cm = ConnectionManager.getInstance();


        // step the clients
        for ( ConnectionManager.ServerConnection sc : cm.getServers() ) {

            if ( sc.getClientApi() == null ) {
                continue;
            }

            io.agi.ef.clientapi.api.ControlApi capi = new io.agi.ef.clientapi.api.ControlApi( sc.getClientApi() );
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

        TStamp tstamp = new TStamp();
        tstamp.setTimeId( new BigDecimal( "2" ) );      // todo: temporarily hard coded time step

        ArrayList<TStamp> tsl = new ArrayList<TStamp>();
        tsl.add(tstamp);

        return Response.ok().entity( tsl ).build();

    }

    @Override
    public Response controlStopGet()
            throws NotFoundException {
        // do some magic!
        return Response.ok().entity( new ApiResponseMessage( ApiResponseMessage.OK, "c magic!" ) ).build();
    }

}
