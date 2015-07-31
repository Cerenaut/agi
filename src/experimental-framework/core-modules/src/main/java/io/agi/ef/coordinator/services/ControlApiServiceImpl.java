package io.agi.ef.coordinator.services;

import io.agi.ef.coordinator.Coordinator;
import io.agi.ef.core.ConnectionManager;
import io.agi.ef.core.ControlInterface;
import io.agi.ef.core.Utils;
import io.agi.ef.serverapi.api.*;

import io.agi.ef.serverapi.model.TStamp;

import java.util.HashMap;
import java.util.List;

import io.agi.ef.serverapi.api.NotFoundException;

import javax.ws.rs.core.Response;

public class ControlApiServiceImpl extends ControlApiService {

    public Coordinator _coordinator = null;

    @Override
    public Response controlRunGet()
            throws NotFoundException {

        _coordinator.run();

        /*
            NOT IMPLEMENTED YET
         */


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

        _coordinator.step();

        /*
            THIS SHOULD PROBABLY JUST CALL BACK TO COORDINATOR, THEN THE FUNCTIONALITY SHOULD BE
            TRIGGERED VIA A NETWORK LAYER
         */

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

        System.out.println( "Stepped all servers, the response is: " + response );

        return response;
    }

    @Override
    public Response controlStopGet()
            throws NotFoundException {

        _coordinator.stop();

        /*
            NOT IMPLEMENTED YET, I want to use Reactive Java
         */

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
