package io.agi.ef.core.network.entities;

import io.agi.ef.core.network.entities.services.ControlApiServiceImpl;
import io.agi.ef.core.network.entities.services.DataApiServiceImpl;
import io.agi.ef.clientapi.ApiException;
import io.agi.ef.core.network.ConnectionManager;
import io.agi.ef.core.network.ConnectionManagerListener;
import io.agi.ef.core.network.EndpointUtils;
import io.agi.ef.core.network.ServerConnection;
import io.agi.ef.serverapi.api.factories.ControlApiServiceFactory;
import io.agi.ef.serverapi.api.factories.DataApiServiceFactory;
import org.eclipse.jetty.server.Server;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * NetworkEntity provides the functionality for an Entity to communicate with other modules via the network.
 * It provides for being a client of the Coordinator, as well as launching a server of its own
 * and establishing a connection with the Coordinator by requesting for the Coordinator to be a client of it.
 *
 * Created by gideon on 1/08/15.
 */
public class NetworkEntity implements ConnectionManagerListener {

    private static final Logger _logger = Logger.getLogger( NetworkEntity.class.getName() );

    // Communications
    private final String _contextPath;
    private ConnectionManager _cm = new ConnectionManager();    // manages connections required for network comms
    private ServerConnection _coordinatorConnection = null;     // special connection for Coordinator
    private int _listenerPort;

    public NetworkEntity( String contextPath, int port ) {
        _contextPath = contextPath;
        _listenerPort = port;
    }

    private class RunServer implements Runnable {
        public void run() {
            Server server = _cm.setupServer(
                    _listenerPort,
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

            _logger.log( Level.FINE, "Connection to server accepted, now send request to connect to this (Agent/World) running Server.." );

            io.agi.ef.clientapi.api.ConnectApi capi = new io.agi.ef.clientapi.api.ConnectApi( sc.getClientApi() );
            capi.connectAgentBaseurlGet( _contextPath );
        }
    }


    public void start( AbstractEntity entity ) throws Exception {

        // inject service implementations to be used by the server lib
        DataApiServiceFactory.setService( new DataApiServiceImpl() );

        ControlApiServiceImpl controlApiService = new ControlApiServiceImpl();
        controlApiService._entity = entity;
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
