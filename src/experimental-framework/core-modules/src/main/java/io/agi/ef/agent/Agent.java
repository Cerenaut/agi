package io.agi.ef.agent;


import io.agi.ef.agent.services.ControlApiServiceImpl;
import io.agi.ef.agent.services.DataApiServiceImpl;
import io.agi.ef.clientapi.ApiException;
import io.agi.ef.core.CommsMode;
import io.agi.ef.core.network.ConnectionManager;
import io.agi.ef.core.apiInterfaces.ControlInterface;
import io.agi.ef.core.apiInterfaces.DataInterface;
import io.agi.ef.core.network.ConnectionManagerListener;
import io.agi.ef.core.network.EndpointUtils;
import io.agi.ef.core.network.ServerConnection;
import io.agi.ef.serverapi.api.factories.ControlApiServiceFactory;
import io.agi.ef.serverapi.api.factories.DataApiServiceFactory;
import org.eclipse.jetty.server.Server;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * When CommsMode == NETWORK, all communications between entities occurs over the network
 *      CommsMode == NON_NETWORK, the Agent does not start a Server, and does not request the Coordinator connect to it
 *
 * Created by gideon on 26/07/15.
 */
public class Agent implements ConnectionManagerListener, ControlInterface, DataInterface {

    private static final Logger _logger = Logger.getLogger( Agent.class.getName() );
    private final CommsMode _Comms_mode;
    private ConnectionManager _cm = new ConnectionManager();
    private ServerConnection _coordinatorConnection = null;
    private String _agentContextPath = null;

    private int _time = 0;

    public Agent( String agentContextPath, CommsMode commsMode ) {
        _agentContextPath = agentContextPath;
        _Comms_mode = commsMode;
    }

    private class RunServer implements Runnable {
        public void run() {
            Server server = _cm.setupServer(
                    EndpointUtils.agentListenPort(),
                    _agentContextPath );
            try {
                server.start();
                server.join();
            }
            catch ( Exception e ) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void connectionAccepted( ServerConnection sc ) throws ApiException {
        if ( sc.getId() == _coordinatorConnection.getId() ) {

            _logger.log( Level.FINE, "Agent. Connection to server accepted, now send request to connect to this (Agent) running Server.." );

            io.agi.ef.clientapi.api.ConnectApi capi = new io.agi.ef.clientapi.api.ConnectApi( sc.getClientApi() );
            capi.connectAgentBaseurlGet( _agentContextPath );
        }
    }

    public void setupProperties( ArrayList<String> properties ) { }

    public void start() throws Exception {

        // if Network mode, run server, and request Coordinator to connect
        if ( _Comms_mode == CommsMode.NETWORK) {

            // inject service implementations to be used by the server lib
            DataApiServiceFactory.setService( new DataApiServiceImpl() );

            ControlApiServiceImpl controlApiService = new ControlApiServiceImpl();
            controlApiService._agent = this;
            ControlApiServiceFactory.setService( controlApiService );


            // start server
            Thread th = new Thread( new RunServer() );
            th.start();

            _coordinatorConnection = _cm.registerServer(
                    ServerConnection.ServerType.Coordinator,
                    EndpointUtils.coordinatorListenPort(),
                    EndpointUtils.coordinatorContextPath() );

            _cm.addListener( this );
        }
    }

    @Override
    public final Response run() {
        // To Discuss:
        // I don't think this should be an option in AGENT - only in Coordinator for synchronisation
        // Dave will disagree
        return null;
    }

    @Override
    public Response step() {
        _time++;
        _logger.log( Level.INFO, "Agent received step at time: {0}", _time );
        return null;
    }

    @Override
    public final Response stop() {
        // this shouldn't be implemented in derived classes
        // for reasons given for 'run()' above

        return null;
    }

    @Override
    public void state() {

    }

    public int getTime() {
        return _time;
    }
}
