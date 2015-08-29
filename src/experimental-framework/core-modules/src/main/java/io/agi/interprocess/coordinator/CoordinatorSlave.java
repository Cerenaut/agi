package io.agi.interprocess.coordinator;

import io.agi.ef.experiment.entities.AbstractEntity;
import io.agi.interprocess.coordinator.slave.services.ControlApiServiceImpl;
import io.agi.interprocess.coordinator.slave.services.DataApiServiceImpl;
import io.agi.ef.clientapi.ApiException;
import io.agi.interprocess.ConnectionManager;
import io.agi.interprocess.ConnectionManagerListener;
import io.agi.interprocess.EndpointUtils;
import io.agi.interprocess.ServerConnection;
import io.agi.ef.serverapi.api.factories.ControlApiServiceFactory;
import io.agi.ef.serverapi.api.factories.DataApiServiceFactory;
import org.eclipse.jetty.server.Server;

import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * CoordinatorSlave provides an interface to the CoordinatorMaster.
 * It provides for being a client of the Coordinator, as well as launching a server of its own
 * and establishing a connection with the Coordinator by requesting for the Coordinator to be a client of it.
 *
 * Created by gideon on 1/08/15.
 */
public class CoordinatorSlave extends CoordinatorInterface implements ConnectionManagerListener {

    private static final Logger _logger = Logger.getLogger( CoordinatorSlave.class.getName() );

    // Communications
    private static String _contextPath;      // CoordinatorSlave server's context path
    private static int _listenerPort;        // CoordinatorSlave server's listener port
    private static String _masterHost;
    private static int _masterPort;
    private ConnectionManager _cm = new ConnectionManager();    // manages connections required for Coordinator comms
    private ServerConnection _coordinatorConnection = null;     // special connection for Coordinator

    Collection< AbstractEntity > _entities;

    public static boolean _setup = false;

    public static void setup( String contextPath, int port, String masterHost, int masterPort ) {
        _contextPath = contextPath;
        _listenerPort = port;
        _masterHost = masterHost;
        _masterPort = masterPort;

        _setup = true;
    }

    private static CoordinatorSlave _slave = null;

    public static CoordinatorSlave getInstance( ) throws Exception {

        if ( _slave != null && _setup == true) {
            _slave = new CoordinatorSlave();
            _slave.startServer();
        }
        else {
            // todo: throw an exception
        }

        return _slave;
    }

    @Override
    public Response run() {

        // iterate _entities, and call run()

        return null;
    }

    @Override
    public Response step() {

        // iterate _entities, and call step()

        return null;
    }

    @Override
    public Response stop() {

        // iterate _entities, and call stop()

        return null;
    }

    @Override
    public Response connectNode( String contextPath ) {
        return null;
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

    public void startServer() throws Exception {

        // inject service implementations to be used by the server lib
        DataApiServiceFactory.setService( new DataApiServiceImpl() );

        ControlApiServiceImpl controlApiService = new ControlApiServiceImpl();
        controlApiService._serviceDelegate = this;
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


    @Override
    public void connectionAccepted( ServerConnection sc ) throws ApiException {
        if ( sc.getId() == _coordinatorConnection.getId() ) {

            _logger.log( Level.FINE, "Connection to server accepted, now send request to connect to this (Agent/World) running Server.." );

            io.agi.ef.clientapi.api.ConnectApi capi = new io.agi.ef.clientapi.api.ConnectApi( sc.getClientApi() );

            capi.connectAgentContextPathGet( _contextPath );
        }
    }
}
