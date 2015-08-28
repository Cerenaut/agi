package io.agi.interprocess.coordinator;

import io.agi.ef.experiment.entities.AbstractEntity;
import io.agi.ef.clientapi.ApiException;
import io.agi.interprocess.coordinator.services.ControlApiServiceImpl;
import io.agi.interprocess.coordinator.services.DataApiServiceImpl;
import io.agi.interprocess.coordinator.services.ConnectApiServiceImpl;
import io.agi.interprocess.apiInterfaces.ConnectInterface;
import io.agi.interprocess.ConnectionManager;
import io.agi.interprocess.apiInterfaces.ControlInterface;
import io.agi.interprocess.ConnectionManagerListener;
import io.agi.interprocess.EndpointUtils;
import io.agi.interprocess.ServerConnection;
import io.agi.interprocess.entity.EntityProxy;
import io.agi.ef.serverapi.api.ApiResponseMessage;
import io.agi.ef.serverapi.api.factories.ConnectApiServiceFactory;
import io.agi.ef.serverapi.api.factories.ControlApiServiceFactory;
import io.agi.ef.serverapi.api.factories.DataApiServiceFactory;
import org.eclipse.jetty.server.Server;

import javax.ws.rs.core.Response;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * This is one of the main modules of the AGIEF.
 * //todo fill in from documentation
 *
 * Created by gideon on 30/07/15.
 */
public class Coordinator implements ConnectionManagerListener, ControlInterface, ConnectInterface {

    private static final Logger _logger = Logger.getLogger( Coordinator.class.getName() );
    private ConnectionManager _cm = null;

    private boolean _running = false;
    private Collection< EntityProxy > _entities;

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
        setup();
    }


    private void setup() throws Exception {
        _cm = new ConnectionManager();
        _cm.addListener( this );

        start();
    }

    public void setupProperties( ArrayList<String> properties ) {}


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
            addEntityProxy( sc );
        }
        else {
            _logger.log( Level.WARNING, "You are trying to add a non Agent or World to the Coordinator" );
        }
    }

    private void addEntityProxy( ServerConnection sc ) {
        EntityProxy entityProxy = new EntityProxy( sc );
        _entities.add( entityProxy );
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


        response = stepAllNodes();                             // progress agents (use world sensor inputs)

        _logger.log( Level.FINE, "Stepped all servers, the response is: {0}", response );

        return response;
    }

    /**
     * Step all Agents, and provide the combined state.
     * @return
     */
    private Response stepAllNodes( ) {

        Response response = null;

        if ( _entities.size() != 0 ) {
            for ( AbstractEntity agent : _entities ) {
                agent.step();
            }
            response = Response.ok().entity( new ApiResponseMessage( ApiResponseMessage.OK, "All agents stepped." ) ).build();
        }
        else {
            response = Response.ok().entity( new ApiResponseMessage( ApiResponseMessage.OK, "There are no connected Agents to step." ) ).build();
        }

        return response;
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

        _cm.registerServer(
                type,
                port,
                contextPath );

        response = Response.ok().entity( new ApiResponseMessage( ApiResponseMessage.OK, "connect to agent: " + contextPath + "." ) ).build();

        return response;
    }

}