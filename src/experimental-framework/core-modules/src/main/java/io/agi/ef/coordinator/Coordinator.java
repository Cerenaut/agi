package io.agi.ef.coordinator;

import io.agi.ef.clientapi.model.TStamp;
import io.agi.ef.coordinator.services.*;
import io.agi.ef.core.ApiInterfaces.ConnectInterface;
import io.agi.ef.core.ConnectionManager;
import io.agi.ef.core.ApiInterfaces.ControlInterface;
import io.agi.ef.core.EndpointUtils;
import io.agi.ef.serverapi.api.ApiResponseMessage;
import io.agi.ef.serverapi.api.factories.ConnectApiServiceFactory;
import io.agi.ef.serverapi.api.factories.ControlApiServiceFactory;
import io.agi.ef.serverapi.api.factories.DataApiServiceFactory;
import org.eclipse.jetty.server.Server;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by gideon on 30/07/15.
 */
public class Coordinator implements ControlInterface, ConnectInterface {

    private ConnectionManager _cm = new ConnectionManager();

    public void connectAgentBaseurl( String contextPath ) {
        _cm.registerServer(
                ConnectionManager.ServerConnection.ServerType.Agent,
                EndpointUtils.agentListenPort(),        // todo: ugly hardcoding. This should come from the request, but wishing to remove need for it completely.
                contextPath );

    }

    private class RunServer implements Runnable {
        public void run() {
            // instantiate and run server
            Server server = _cm.setupServer(
                    EndpointUtils.coordinatorListenPort(),
                    EndpointUtils.coordinatorContextPath() );
            try {
                server.start();
                server.join();
            }
            catch ( Exception e ) {
                e.printStackTrace();
            }
        }
    }

    public Coordinator() { }


    public void setupProperties( ArrayList<String> properties ) {}

    public void start() throws Exception {

        // inject service implementations to be used by the server lib
        DataApiServiceFactory.setService( new DataApiServiceImpl() );

        ControlApiServiceImpl controlApiServiceImpl = new ControlApiServiceImpl();
        controlApiServiceImpl._coordinator = this;
        ControlApiServiceFactory.setService( controlApiServiceImpl );

        ConnectApiServiceImpl connectApiService = new ConnectApiServiceImpl();
        connectApiService._coordinator = this;
        ConnectApiServiceFactory.setService( connectApiService );


        Thread th = new Thread( new RunServer() );
        th.start();
    }


    @Override
    public Response run() {
        /*
            NOT IMPLEMENTED YET
         */

        // run the clients
        Response response = null;
        if ( _cm.getServers().size() == 0 ) {
            response = Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "There are no connected servers to step.")).build();
        }
        else {
            // to implement
            response = Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "to do... run all servers")).build();
        }

        return response;
    }

    @Override
    public Response step() {

        HashMap<String, List<TStamp>> serverTimeStamps = new HashMap<String, List<io.agi.ef.clientapi.model.TStamp>>();

        // step the clients
        Response response = null;
        if ( _cm.getServers().size() == 0 ) {
            response = Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "There are no connected servers to step.")).build();
        }
        else {
            for ( ConnectionManager.ServerConnection sc : _cm.getServers() ) {

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
    public Response stop() {
        // stop the clients

        /*
            NOT IMPLEMENTED YET, I want to use Reactive Java
         */

        Response response = null;
        if ( _cm.getServers().size() == 0 ) {
            response = Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "There are no connected servers to step.")).build();
        }
        else {
            // to implement
            response = Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "to do... stop all servers")).build();
        }

        return response;
    }


}