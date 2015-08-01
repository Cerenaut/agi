package io.agi.ef.coordinator;

import io.agi.ef.agent.services.ControlApiServiceImpl;
import io.agi.ef.agent.services.DataApiServiceImpl;
import io.agi.ef.clientapi.ApiException;
import io.agi.ef.core.CommsMode;
import io.agi.ef.core.apiInterfaces.ControlInterface;
import io.agi.ef.core.apiInterfaces.DataInterface;
import io.agi.ef.core.network.ConnectionManager;
import io.agi.ef.core.network.ConnectionManagerListener;
import io.agi.ef.core.network.EndpointUtils;
import io.agi.ef.core.network.ServerConnection;
import io.agi.ef.serverapi.api.factories.ControlApiServiceFactory;
import io.agi.ef.serverapi.api.factories.DataApiServiceFactory;
import org.eclipse.jetty.server.Server;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * Any of the other major modules inherit CoordinatorClientServer.
 * It contains the core functionality for being a client of the Coordinator, as well as launching a server of its own
 * and establishing a connection with the Coordinator by requesting for the Coordinator to be a client of it.
 *
 * Created by gideon on 1/08/15.
 */
public abstract class CoordinatorClientServer implements ConnectionManagerListener, ControlInterface, DataInterface {

    private int _time = 0;          // time step

    // Communications
    private final CommsMode _Comms_mode;                        // the mode of communications (network or via objects within project)
    private final String _contextPath;
    private ConnectionManager _cm = new ConnectionManager();    // manages connections required for network comms
    private ServerConnection _coordinatorConnection = null;     // special connection for Coordinator


    public CoordinatorClientServer( CommsMode commsMode ) {
        _Comms_mode = commsMode;
        _contextPath = "default-context-path"; // todo: append a GUID for uniqueness
    }

    public CoordinatorClientServer( CommsMode commsMode, String contextPath ) {
        _Comms_mode = commsMode;
        _contextPath = contextPath;
    }

    protected abstract int listenerPort();

    protected abstract Logger getLogger();

    private class RunServer implements Runnable {
        public void run() {
            Server server = _cm.setupServer(
                    listenerPort(),
                    _contextPath );
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

            getLogger().log( Level.FINE, "Connection to server accepted, now send request to connect to this (Agent/World) running Server.." );

            io.agi.ef.clientapi.api.ConnectApi capi = new io.agi.ef.clientapi.api.ConnectApi( sc.getClientApi() );
            capi.connectAgentBaseurlGet( _contextPath );
        }
    }

    public void setupProperties( ArrayList<String> properties ) { }

    public void start() throws Exception {

        // if Network mode, run server, and request Coordinator to connect
        if ( _Comms_mode == CommsMode.NETWORK) {

            // inject service implementations to be used by the server lib
            DataApiServiceFactory.setService( new DataApiServiceImpl() );

            ControlApiServiceImpl controlApiService = new ControlApiServiceImpl();
            controlApiService._coordClientServer = this;
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

    public int getTime() {
        return _time;
    }

    protected void incTime() {
        ++_time;
    }
}
