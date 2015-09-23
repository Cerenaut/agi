package io.agi.ef.interprocess.coordinator;

import io.agi.ef.clientapi.ApiException;
import io.agi.ef.entities.experiment.old_entities.AbstractEntity;
import io.agi.ef.serverapi.api.ApiResponseMessage;
import io.agi.ef.serverapi.api.factories.ControlApiServiceFactory;
import io.agi.ef.interprocess.ConnectionManager;
import io.agi.ef.interprocess.ConnectionManagerListener;
import io.agi.ef.interprocess.ServerConnection;
import io.agi.ef.interprocess.coordinator.services.*;
import io.agi.ef.interprocess.apiInterfaces.ConnectInterface;

import javax.ws.rs.core.Response;
import java.util.HashSet;
import java.util.logging.Level;

/**
 *
 * CoordinatorSlave is the Singleton local instance of the Coordinator, used by Entities.
 * It is a local Client/Server with the CoordinatorMaster, and is used to interact with the CoordinatorMaster.
 *
 * It provides for being a client of the Master, as well as launching a server of its own
 * and establishing a connection with the Master by requesting for the Master to be a client of it.
 *
 * Created by gideon on 1/08/15.
 */
public class CoordinatorSlave extends Coordinator implements ConnectionManagerListener, ConnectInterface {

    // Communications
// Moved to Persistence
//    private static String _host;             // CoordinatorSlave server's host
//    private static int _listenerPort;        // CoordinatorSlave server's listener listenerPort
//    private static String _contextPath;      // CoordinatorSlave server's context path
//    private static String _masterHost;
//    private static int _masterPort;

// Connections now persisted
//    private ConnectionManager _cm = null;    // manages connections required for Master comms
//    private ServerConnection _coordinatorConnection = null;         // special connection for Master

// Entities list now persisted
//    private HashSet< AbstractEntity > _entities = new HashSet();

    private static boolean _setup = false;

    public static void setup( String slaveHost, int slaveListenerPort, String slaveContextPath, String masterHost, int masterPort ) {
//        _contextPath = slaveContextPath;
//        _listenerPort = slaveListenerPort;
//        _host = slaveHost;

//        _masterHost = masterHost;
//        _masterPort = masterPort;

        _setup = true;
    }

    private static CoordinatorSlave _slave = null;

    public static CoordinatorSlave getInstanceSlave( ) throws Exception {

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

//        _cm = new ConnectionManager();
//
//        startServer();
//
//        _cm.addListener( this );
//        _coordinatorConnection = _cm.registerServer(
//                _masterHost,
//                _masterPort,
//                CoordinatorMaster.sContextPath );
    }

    public void addEntity( AbstractEntity abstractEntity ) {
//        _entities.add( abstractEntity ); persisted and handled in new Entity class
    }

    protected void setServiceImplementation() {

        // inject service implementations to be used by the server lib
        ControlApiServiceImpl controlApiService = new ControlApiServiceImpl();
        controlApiService._serviceDelegate = this;
        ControlApiServiceFactory.setService( controlApiService );
    }

// Moved to Node or NodeServer
//    @Override
//    protected String contextPath() {
//        return _contextPath;
//    }
//
// Moved to Node or NodeServer
//    @Override
//    protected int listenerPort() {
//        return _listenerPort;
//    }

    @Override
    public void connectionAccepted( ServerConnection sc ) throws ApiException {
// Connections no longer managed outside DB
//        if ( sc.getId() == _coordinatorConnection.getId() ) {
//            _logger.log( Level.FINE, "Connection to server accepted, now send request to connect to this (node) running Server.." );
//            io.agi.ef.clientapi.api.ConnectApi capi = new io.agi.ef.clientapi.api.ConnectApi( sc.getClientApi() );
//
//            String port = Integer.toString(  _listenerPort );
//            capi.connectHostHostPortPortContextPathContextPathGet( _host, port, _contextPath );
//        }
    }

// Deprecated API
//    /**
//     * We are in the Slave, so this has the meaning, connectMaster
//     */
//    public Response connect( String host, String port, String contextPath ) {
//        return  Response.ok().entity( new ApiResponseMessage( ApiResponseMessage.ERROR, "'Connect' can only be used with Master (not Slave) Coordinator(s)" ) ).build();
//    }

    public Response command( final String entityName, final String command ) {

        _logger.log( Level.FINE, "**CONTROL/COMMAND: Received command request for entity: ( " + entityName + ", " + command + ")." );
        _logger.log( Level.FINE, "\tDistribute to appropriate old_entities.");

// Since the entities are now potentially remote, we don't have objects to subscribe to
//        rx.Observable.from( _entities )
//                .filter( entity -> entity.name().equalsIgnoreCase( entityName ) )
//                .subscribe( entity -> entity.command( command ) );

        return  Response.ok().entity( new ApiResponseMessage( ApiResponseMessage.OK, "Slave sent to entity: " + entityName + ", the command: " + command ) ).build();
    }

// Deprecated API
//    @Override
//    public Response status( final String entityName, final String state ) {
//
//        _logger.log( Level.FINE, "**CONTROL/STATUS: Received command request for entity: ( " + entityName + ", " + state + ")." );
//        _logger.log( Level.FINE, "\tDistribute to appropriate old_entities.");
//
//        rx.Observable.from( _entities )
//                .filter( entity -> entity.name().equalsIgnoreCase( entityName ) )
//                .subscribe( entity -> entity.status( state ) );
//
//        return  Response.ok().entity( new ApiResponseMessage( ApiResponseMessage.OK, "Slave sent sent request to entity: " + entityName +  ", the status for state: " + state )).build();
//    }

    public void commandBack( final String entityName, final String command ) throws ApiException {
        _logger.log( Level.FINE, "Send command for entity back to Master: ( " + entityName + ", " + command + ")." );
// Can't compile with adapted Coordinator interface.
//        io.agi.ef.clientapi.api.ControlApi capi = new io.agi.ef.clientapi.api.ControlApi( _coordinatorConnection.getClientApi() );
//        capi.controlEntityEntityNameCommandCommandGet( entityName, command );
    }

}
