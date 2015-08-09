package io.agi.ef.core.network.coordinator;

import io.agi.ef.core.network.entities.AbstractEntity;
import io.agi.ef.core.network.entities.Agent;
import io.agi.ef.clientapi.ApiException;
import io.agi.ef.core.network.entities.NetworkProxyEntity;
import io.agi.ef.core.network.coordinator.services.ControlApiServiceImpl;
import io.agi.ef.core.network.coordinator.services.DataApiServiceImpl;
import io.agi.ef.core.network.coordinator.services.ConnectApiServiceImpl;
import io.agi.ef.core.CommsMode;
import io.agi.ef.core.UniversalState;
import io.agi.ef.core.apiInterfaces.ConnectInterface;
import io.agi.ef.core.network.ConnectionManager;
import io.agi.ef.core.apiInterfaces.ControlInterface;
import io.agi.ef.core.network.ConnectionManagerListener;
import io.agi.ef.core.network.EndpointUtils;
import io.agi.ef.core.network.ServerConnection;
import io.agi.ef.serverapi.api.ApiResponseMessage;
import io.agi.ef.serverapi.api.factories.ConnectApiServiceFactory;
import io.agi.ef.serverapi.api.factories.ControlApiServiceFactory;
import io.agi.ef.serverapi.api.factories.DataApiServiceFactory;
import io.agi.ef.core.network.entities.World;
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
public class Coordinator implements ConnectionManagerListener, ControlInterface, ConnectInterface {

    private static final Logger _logger = Logger.getLogger( Coordinator.class.getName() );
    private final CommsMode _Comms_mode;
    private ConnectionManager _cm = null;
    private HashSet<AbstractEntity> _agents = new HashSet<>( );
    private AbstractEntity _world = null;

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

    /**
     * Constructor with no parameters sets the CommsMode to Network
     * @throws Exception
     */
    public Coordinator() throws Exception {
        _Comms_mode = CommsMode.NETWORK;
        setup();
    }

    /**
     * Constructor with no ability to set CommsMode explicitly
     * @param commsMode
     * @throws Exception
     */
    public Coordinator( CommsMode commsMode ) throws Exception {
        _Comms_mode = commsMode;
        setup();
    }

    private void setup() throws Exception {
        _cm = new ConnectionManager();
        _cm.addListener( this );

        start();
    }

    public void setupProperties( ArrayList<String> properties ) {}

    public void removeAgent( Agent agent ) {
        _agents.remove( agent );
    }

    private void start() throws Exception {

        // inject service implementations to be used by the server lib
        DataApiServiceFactory.setService( new DataApiServiceImpl() );

        ControlApiServiceImpl controlApiService = new ControlApiServiceImpl();
        controlApiService._serviceDelegate = this;
        ControlApiServiceFactory.setService( controlApiService );

        ConnectApiServiceImpl connectApiService = new ConnectApiServiceImpl();
        connectApiService._serviceDelegate = this;
        ConnectApiServiceFactory.setService( connectApiService );

        Thread serverThread = new Thread( new RunServer() );
        serverThread.start();

        Thread expThread = new Thread( new RunExperiment() );
        expThread.start();
    }


    @Override
    public void connectionAccepted( ServerConnection sc ) throws ApiException {

        if ( sc.isAgent() ) {
            addAgent( sc );
        }
        else if ( sc.isWorld() ) {
            addWorld( sc );
        }
        else {
            _logger.log( Level.WARNING, "You are trying to add a non Agent or World to the Coordinator" );
        }
    }

    private void addWorld( ServerConnection sc ) {
        NetworkProxyEntity networkEntity = new NetworkProxyEntity( sc );
        _world = networkEntity;
    }

    private void addAgent( ServerConnection sc ) {
        NetworkProxyEntity networkEntity = new NetworkProxyEntity( sc );
        _agents.add( networkEntity );
    }

    public void addAgent( Agent agent ) {
        _agents.add( agent );
    }

    public void addWorld( World world ) {
        _world = world;
    }

    @Override
    public Response run() {
        _running = true;
        Response response = Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "Run Run Run")).build();
        return response;
    }

    @Override
    public Response step() {

        Response response = null;

        if ( _world == null && _agents.size() == 0 ) {
            response = Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "Step() error: No Agent or World.")).build();
            return response;
        }

        UniversalState worldState = getWorldState();                        // get updated world state

        response = stepAllAgents( worldState );                             // progress agents (use world sensor inputs)

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

        // todo: need to implement for World to respond to Agents

        return null;
    }

    /**
     * - Send the combined Agent states to the World
     * - Step the world
     * @param combinedStates
     */
    private void updateAndStepWorld( Collection<UniversalState> combinedStates ) {

        if ( _world == null ) {
            _logger.log( Level.WARNING, "updateAndStepWorld(): _world is null" );
            return;
        }

        // send combined Agent states to the World
        _world.setAgentStates( combinedStates );

        // step the world
        if ( _world != null ) {
            _world.step();
        }
    }

    /**
     * Step all Agents, and provide the combined state.
     * @param worldState
     * @return
     */
    private Response stepAllAgents( UniversalState worldState ) {

        Response response = null;

        if ( _agents.size() != 0 ) {
            for ( AbstractEntity agent : _agents ) {
                agent.setWorldState( worldState );
                agent.step();
            }
            response = Response.ok().entity( new ApiResponseMessage( ApiResponseMessage.OK, "All agents stepped." ) ).build();
        }
        else {
            response = Response.ok().entity( new ApiResponseMessage( ApiResponseMessage.OK, "There are no connected Agents to step." ) ).build();
        }

        return response;
    }

    /**
     * Get the World state
     * @return
     */
    private UniversalState getWorldState() {

        if ( _world == null ) {
            _logger.log( Level.WARNING, "getWorldState(): _world is null" );
            return null;
        }

        UniversalState state = null;
        state = _world.getState();
        return state;
    }


    @Override
    public Response stop() {
        // stop the clients
        _running = false;
        Response response = Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "Stop stop stop")).build();
        return response;
    }

    public Response connectWorld( String contextPath ) {
        return connectServer( contextPath, ServerConnection.ServerType.World, EndpointUtils.worldListenPort() );
    }

    public Response connectAgent( String contextPath ) {

        return connectServer( contextPath, ServerConnection.ServerType.Agent, EndpointUtils.agentListenPort() );
    }

    public Response connectServer( String contextPath, ServerConnection.ServerType type, int port ) {
        Response response = null;

        // todo: Port number - this should come from the request, but wishing to remove need for it completely.
        if ( _Comms_mode == CommsMode.NETWORK ) {
            _cm.registerServer(
                    type,
                    port,
                    contextPath );

            response = Response.ok().entity( new ApiResponseMessage( ApiResponseMessage.OK, "connect to agent: " + contextPath + "." ) ).build();
        }
        else {
            response = Response.ok().entity( new ApiResponseMessage( ApiResponseMessage.OK, "Not in network mode") ).build();
        }

        return response;
    }

}