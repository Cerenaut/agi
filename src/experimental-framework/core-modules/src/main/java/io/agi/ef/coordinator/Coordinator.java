package io.agi.ef.coordinator;

import io.agi.ef.agent.Agent;
import io.agi.ef.clientapi.model.TStamp;
import io.agi.ef.coordinator.services.ControlApiServiceImpl;
import io.agi.ef.coordinator.services.DataApiServiceImpl;
import io.agi.ef.coordinator.services.ConnectApiServiceImpl;
import io.agi.ef.core.CommsMode;
import io.agi.ef.core.apiInterfaces.ConnectInterface;
import io.agi.ef.core.network.ConnectionManager;
import io.agi.ef.core.apiInterfaces.ControlInterface;
import io.agi.ef.core.network.EndpointUtils;
import io.agi.ef.core.network.ServerConnection;
import io.agi.ef.serverapi.api.ApiResponseMessage;
import io.agi.ef.serverapi.api.factories.ConnectApiServiceFactory;
import io.agi.ef.serverapi.api.factories.ControlApiServiceFactory;
import io.agi.ef.serverapi.api.factories.DataApiServiceFactory;
import org.eclipse.jetty.server.Server;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 **
 * When CommsMode == NETWORK, all communications between entities occurs over the network
 *      CommsMode == NON_NETWORK, to reject all requests from Agents/Worlds for a connection
 *
 * Created by gideon on 30/07/15.
 */
public class Coordinator implements ControlInterface, ConnectInterface {

    private static final Logger _logger = Logger.getLogger( Coordinator.class.getName() );
    private final CommsMode _Comms_mode;
    private ConnectionManager _cm = new ConnectionManager();
    private HashSet< Agent > _agents = new HashSet<>( ); // todo: consider moving to ConnectionManager

    private class RunServer implements Runnable {
        public void run() {
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

    public Coordinator( CommsMode commsMode ) {
        _Comms_mode = commsMode;
    }

    public void setupProperties( ArrayList<String> properties ) {}

    public void addAgent( Agent agent ) {
        _agents.add( agent );
    }

    public void removeAgent( Agent agent ) {
        _agents.remove( agent );
    }

    public void start() throws Exception {

        // inject service implementations to be used by the server lib
        DataApiServiceFactory.setService( new DataApiServiceImpl() );

        ControlApiServiceImpl controlApiService = new ControlApiServiceImpl();
        controlApiService._coordinator = this;
        ControlApiServiceFactory.setService( controlApiService );

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

        _logger.log( Level.FINE, "Coordinator received step.");

        HashMap< String, List< TStamp >> serverTimeStamps = new HashMap<>();
        List<io.agi.ef.clientapi.model.TStamp> tStamps;

        // 1) step the Agents/Worlds connected directly
        int agentCount = 0;
        for ( Agent agent : _agents ) {
            agentCount++;
            agent.step();
        }

        // 2) step the Agents/Worlds connected via the network
        Response response = null;
        for ( ServerConnection sc : _cm.getServers() ) {

            if ( sc.getClientApi() == null ) {
                continue;
            }

            agentCount++;

            io.agi.ef.clientapi.api.ControlApi capi = new io.agi.ef.clientapi.api.ControlApi( sc.getClientApi() );
            try {

                // todo: known bug: doesn't seem to be able to deserialise tStamps, but i want to change their format anyway
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

        if ( agentCount == 0 ) {
            response = Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "There are no connected servers to step.")).build();
        }
        else {
            response = Response.ok().entity( serverTimeStamps ).build();
        }

        _logger.log( Level.FINE, "Stepped all servers, the response is: {0}", response );

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
            response = Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "To do... stop all servers")).build();
        }

        return response;
    }

    public void connectAgentBaseurl( String contextPath ) {

        if ( _Comms_mode == CommsMode.NETWORK ) {
            _cm.registerServer(
                    ServerConnection.ServerType.Agent,
                    EndpointUtils.agentListenPort(),     // todo: ugly hardcoding. This should come from the request, but wishing to remove need for it completely.
                    contextPath );
        }
    }

}