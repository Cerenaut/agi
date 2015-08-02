package io.agi.ef.coordinator;

import io.agi.ef.agent.Agent;
import io.agi.ef.clientapi.model.TStamp;
import io.agi.ef.coordinator.services.ControlApiServiceImpl;
import io.agi.ef.coordinator.services.DataApiServiceImpl;
import io.agi.ef.coordinator.services.ConnectApiServiceImpl;
import io.agi.ef.core.CommsMode;
import io.agi.ef.core.UniversalState;
import io.agi.ef.core.apiInterfaces.ConnectInterface;
import io.agi.ef.core.network.ConnectionManager;
import io.agi.ef.core.apiInterfaces.ControlInterface;
import io.agi.ef.core.network.EndpointUtils;
import io.agi.ef.core.network.ServerConnection;
import io.agi.ef.serverapi.api.ApiResponseMessage;
import io.agi.ef.serverapi.api.factories.ConnectApiServiceFactory;
import io.agi.ef.serverapi.api.factories.ControlApiServiceFactory;
import io.agi.ef.serverapi.api.factories.DataApiServiceFactory;
import io.agi.ef.world.World;
import org.eclipse.jetty.server.Server;

import javax.ws.rs.core.Response;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * This is one of the main modules of the AGIEF. The Coordinator receives user input (usually via web front end)
 * and coordinators the Agent(s) and World.
 *
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
    private World _world = null;

    private boolean _running = false;

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

    // todo: this is a rudimentary mechanism, first pass to provide base functionality
    private class RunExperiment implements Runnable {
        public void run() {
            while ( true ) {

                if ( _running == true ) {
                    step();
                }

                try {
                    Thread.sleep( 200 );
                }
                catch ( InterruptedException e ) {
                    e.printStackTrace();
                }

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

    public void addWorld( World w ) {
        _world = w;
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


        Thread serverThread = new Thread( new RunServer() );
        serverThread.start();

        Thread expThread = new Thread( new RunExperiment() );
        expThread.start();
    }


    @Override
    public Response run() {
        _running = true;
        Response response = Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "Run Run Run")).build();
        return response;
    }

    @Override
    public Response step() {

        _logger.log( Level.INFO, "Coordinator received step." );

        UniversalState worldState = getWorldState();                        // get updated world state

        Response response = stepAllAgents( worldState );                    // progress agents (use world sensor inputs)

        Collection< UniversalState > combinedStates = getAgentStates();     // get Actuator outputs

        updateAndStepWorld( combinedStates );                               // progress world (use Actuator outputs)

        _logger.log( Level.FINE, "Stepped all servers, the response is: {0}", response );

        return response;
    }

    /**
     * Go through all agents, get state, adn combine into collection
     * @return
     */
    private Collection<UniversalState> getAgentStates() {


        return null;
    }

    /**
     * - Send the combined Agent states to the World
     * - Step the world
     * @param combinedStates
     */
    private void updateAndStepWorld( Collection<UniversalState> combinedStates ) {

        // 1) send combined Agent states to the World
        // ------------------------------------------------------

        // local world
        _world.setAgentStates( combinedStates );

        // network world


        // 2) step the world
        // ------------------------------------------------------
        // If World is local
        if ( _world != null ) {
            _world.step();
        }

        // If the World is remote
        Response response = null;
        HashMap< String, List< TStamp >> serverTimeStamps = new HashMap<>();
        List<io.agi.ef.clientapi.model.TStamp> tStamps;
        for ( ServerConnection sc : _cm.getServers() ) {

            if ( sc.getClientApi() == null || sc.isWorld() ) {
                continue;
            }
            io.agi.ef.clientapi.api.ControlApi capi = new io.agi.ef.clientapi.api.ControlApi( sc.getClientApi() );
            try {
                tStamps = capi.controlStepGet();
                serverTimeStamps.put( sc.basePath(), tStamps );
            }
            catch ( io.agi.ef.clientapi.ApiException e ) {
                e.printStackTrace();
            }
            catch ( Exception e ) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Step all Agents, and provide the combined state.
     * @param worldState
     * @return
     */
    private Response stepAllAgents( UniversalState worldState ) {

        HashMap< String, List< TStamp >> serverTimeStamps = new HashMap<>();
        List<io.agi.ef.clientapi.model.TStamp> tStamps;
        Response response = null;
        int agentCount = 0;

        // 1) step the Agents/Worlds connected directly
        for ( Agent agent : _agents ) {
            agentCount++;
            agent.step();
        }

        // 2) step the Agents/Worlds connected via the network
        for ( ServerConnection sc : _cm.getServers() ) {

            if ( sc.getClientApi() == null || sc.isAgent() ) {
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
            response = Response.ok().entity( new ApiResponseMessage( ApiResponseMessage.OK, "There are no connected servers to step." ) ).build();
        }
        else {
            response = Response.ok().entity( serverTimeStamps ).build();
        }

        return response;
    }

    /**
     * Get the World state
     * @return
     */
    private UniversalState getWorldState() {

        // 1) local world
        UniversalState state = _world.getState();

        // 2) network world

                //
                // to implement
                //
                //

        return state;
    }


    @Override
    public Response stop() {
        // stop the clients
        _running = false;
        Response response = Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "Stop stop stop")).build();
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