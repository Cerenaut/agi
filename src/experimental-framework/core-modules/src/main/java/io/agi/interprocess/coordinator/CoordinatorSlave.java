package io.agi.interprocess.coordinator;

import io.agi.ef.clientapi.ApiException;
import io.agi.ef.experiment.entities.AbstractEntity;
import io.agi.ef.serverapi.api.factories.ControlApiServiceFactory;
import io.agi.ef.serverapi.api.factories.DataApiServiceFactory;
import io.agi.interprocess.ConnectionManager;
import io.agi.interprocess.ConnectionManagerListener;
import io.agi.interprocess.ServerConnection;
import io.agi.interprocess.coordinator.services.*;
import io.agi.interprocess.apiInterfaces.ConnectInterface;

import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * CoordinatorSlave provides an interface to the CoordinatorMaster.
 * It provides for being a client of the Master, as well as launching a server of its own
 * and establishing a connection with the Master by requesting for the Master to be a client of it.
 *
 * Created by gideon on 1/08/15.
 */
public class CoordinatorSlave extends Coordinator implements ConnectionManagerListener, ConnectInterface {

    private static final Logger _logger = Logger.getLogger( CoordinatorSlave.class.getName() );

    // Communications
    private static String _contextPath;      // CoordinatorSlave server's context path
    private static int _listenerPort;        // CoordinatorSlave server's listener listenerPort
    private static String _masterHost;
    private static int _masterPort;
    private ConnectionManager _cm = new ConnectionManager();    // manages connections required for Master comms
    private ServerConnection _coordinatorConnection = null;     // special connection for Master

    Collection< AbstractEntity > _entities;

    public static boolean _setup = false;
    private CoordinatorSlaveDelegate delegate;

    public static void setup( String contextPath, int listenerPort, String masterHost, int masterPort ) {
        _contextPath = contextPath;
        _listenerPort = listenerPort;
        _masterHost = masterHost;
        _masterPort = masterPort;

        _setup = true;
    }

    private static CoordinatorSlave _slave = null;

    public static CoordinatorSlave getInstance( ) throws Exception {

        if ( _slave == null && _setup == true) {
            _slave = new CoordinatorSlave();
        }
        else {
            // todo: throw an exception
        }

        return _slave;
    }

    private CoordinatorSlave() throws Exception {
        super();

        _logger.setLevel( Level.FINE );

        startServer();

        _cm.addListener( this );
        _coordinatorConnection = _cm.registerServer(
                _masterHost,
                CoordinatorMaster.sContextPath,
                _masterPort );
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

    /**
     * We are in the Slave, so this has the meaning, connectMaster
     * @param contextPath
     * @return
     */
    @Override
    public Response connectCoordinator( String contextPath ) {
        return null;
    }


    @Override
    protected void setServiceImplementation() {
        // inject service implementations to be used by the server lib
        DataApiServiceFactory.setService( new DataApiServiceImpl() );

        ControlApiServiceImpl controlApiService = new ControlApiServiceImpl();
        controlApiService._serviceDelegate = this;
        ControlApiServiceFactory.setService( controlApiService );
    }

    @Override
    protected String contextPath() {
        return _contextPath;
    }

    @Override
    protected int listenerPort() {
        return _listenerPort;
    }


    @Override
    public void connectionAccepted( ServerConnection sc ) throws ApiException {
        if ( sc.getId() == _coordinatorConnection.getId() ) {
            _logger.log( Level.FINE, "Connection to server accepted, now send request to connect to this (Agent/World) running Server.." );
            io.agi.ef.clientapi.api.ConnectApi capi = new io.agi.ef.clientapi.api.ConnectApi( sc.getClientApi() );
            capi.connectAgentContextPathGet( _contextPath );
        }
    }

    public void setDelegate( CoordinatorSlaveDelegate delegate ) {
        this.delegate = delegate;
    }
}
