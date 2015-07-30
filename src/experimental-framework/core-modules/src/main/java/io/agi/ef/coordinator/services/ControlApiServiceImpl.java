package io.agi.ef.coordinator.services;

import io.agi.ef.coordinator.Coordinator;
import io.agi.ef.core.ConnectionManager;
import io.agi.ef.core.Utils;
import io.agi.ef.serverapi.api.*;

import io.agi.ef.serverapi.model.TStamp;

import java.util.HashMap;
import java.util.List;

import io.agi.ef.serverapi.api.NotFoundException;

import javax.ws.rs.core.Response;

public class ControlApiServiceImpl extends ControlApiService {

    @Override
    public Response controlRunGet()
            throws NotFoundException {

        // run the clients
        ConnectionManager cm = ConnectionManager.getInstance();

        Response response = null;
        if ( cm.getServers().size() == 0 ) {
            response = Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "There are no connected servers to step.")).build();
        }
        else {
            // to implement
            response = Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "to do... run all servers")).build();
        }

        return response;
    }

    @Override
    public Response controlStepGet()
            throws NotFoundException {

        HashMap<String, List<io.agi.ef.clientapi.model.TStamp>> serverTimeStamps = new HashMap<String, List<io.agi.ef.clientapi.model.TStamp>>();

        // step the clients
        ConnectionManager cm = ConnectionManager.getInstance();

        Response response = null;
        if ( cm.getServers().size() == 0 ) {
            response = Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "There are no connected servers to step.")).build();
        }
        else {
            for ( ConnectionManager.ServerConnection sc : cm.getServers() ) {

                if ( sc.getClientApi() == null ) {
                    continue;
                }

                List<io.agi.ef.clientapi.model.TStamp> tStamps;
                io.agi.ef.clientapi.api.ControlApi capi = new io.agi.ef.clientapi.api.ControlApi( sc.getClientApi() );
                try {

                    // known bug: doesn't seem to be able to deserialise tStamps, but i want to change their format anyway
                    tStamps = capi.controlStepGet();
                    serverTimeStamps.put( sc.basePath(), tStamps );
                }
                catch ( io.agi.ef.clientapi.ApiException e ) {
                    e.printStackTrace();
                }
                // todo: this catches a connection refused exception, but should be tidied up
                catch ( Exception e ) {
                    e.printStackTrace();
                }

            }
            response = Response.ok().entity( serverTimeStamps ).build();
        }
        return response;
    }

    @Override
    public Response controlStopGet()
            throws NotFoundException {

        // stop the clients
        ConnectionManager cm = ConnectionManager.getInstance();

        Response response = null;
        if ( cm.getServers().size() == 0 ) {
            response = Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "There are no connected servers to step.")).build();
        }
        else {
            // to implement
            response = Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "to do... stop all servers")).build();
        }

        return response;

    }

}
