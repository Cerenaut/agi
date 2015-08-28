package io.agi.interprocess.coordinator;

import io.agi.ef.experiment.entities.AbstractEntity;
import io.agi.interprocess.entity.services.ControlApiServiceImpl;
import io.agi.interprocess.entity.services.DataApiServiceImpl;
import io.agi.ef.clientapi.ApiException;
import io.agi.interprocess.ConnectionManager;
import io.agi.interprocess.ConnectionManagerListener;
import io.agi.interprocess.EndpointUtils;
import io.agi.interprocess.ServerConnection;
import io.agi.ef.serverapi.api.factories.ControlApiServiceFactory;
import io.agi.ef.serverapi.api.factories.DataApiServiceFactory;
import org.eclipse.jetty.server.Server;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * NetworkEntity provides the functionality for an Entity to communicate with other modules via the Coordinator.
 * It provides for being a client of the Coordinator, as well as launching a server of its own
 * and establishing a connection with the Coordinator by requesting for the Coordinator to be a client of it.
 *
 * Created by gideon on 1/08/15.
 */
public class CoordinatorProxy implements ConnectionManagerListener {

    private static final Logger _logger = Logger.getLogger( CoordinatorProxy.class.getName() );

    // Communications
    private final String _contextPath;
    private ConnectionManager _cm = new ConnectionManager();    // manages connections required for Coordinator comms
    private ServerConnection _coordinatorConnection = null;     // special connection for Coordinator
    private int _listenerPort;
    private ServerConnection.ServerType _type;


    private CoordinatorProxy( String contextPath, int port, ServerConnection.ServerType type ) {
        _contextPath = contextPath;
        _listenerPort = port;
        _type = type;
    }

    /**
     * Host and port of the actual coordinator.
     * @param host
     * @param port
     * @return
     */
    public static CoordinatorProxy createInstance( String host, int port,  ) {

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

            if ( _type == ServerConnection.ServerType.Agent ) {
                capi.connectAgentContextPathGet( _contextPath );
            }
            else if ( _type == ServerConnection.ServerType.World ) {
                capi.connectWorldContextPathGet( _contextPath );
            }
        }
    }


    public void start( AbstractEntity entity ) throws Exception {

        // inject service implementations to be used by the server lib
        DataApiServiceFactory.setService( new DataApiServiceImpl() );

        ControlApiServiceImpl controlApiService = new ControlApiServiceImpl();
        controlApiService._serviceDelegate = entity;
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
