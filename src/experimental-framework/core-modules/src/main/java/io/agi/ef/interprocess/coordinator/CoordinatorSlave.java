package io.agi.ef.interprocess.coordinator;

import io.agi.ef.clientapi.ApiException;
import io.agi.ef.experiment.entities.AbstractEntity;
import io.agi.ef.serverapi.api.ApiResponseMessage;
import io.agi.ef.serverapi.api.factories.ControlApiServiceFactory;
import io.agi.ef.interprocess.ConnectionManager;
import io.agi.ef.interprocess.ConnectionManagerListener;
import io.agi.ef.interprocess.ServerConnection;
import io.agi.ef.interprocess.coordinator.services.*;
import io.agi.ef.interprocess.apiInterfaces.ConnectInterface;
import rx.functions.Action1;

import javax.ws.rs.core.Response;
import java.util.HashSet;
import java.util.logging.Level;

/**
 * CoordinatorSlave provides an interface to the CoordinatorMaster.
 * It provides for being a client of the Master, as well as launching a server of its own
 * and establishing a connection with the Master by requesting for the Master to be a client of it.
 *
 * Created by gideon on 1/08/15.
 */
public class CoordinatorSlave extends Coordinator implements ConnectionManagerListener, ConnectInterface {

    // Communications
    private static String _host;             // CoordinatorSlave server's host
    private static int _listenerPort;        // CoordinatorSlave server's listener listenerPort
    private static String _contextPath;      // CoordinatorSlave server's context path
    private static String _masterHost;
    private static int _masterPort;
    private ConnectionManager _cm = null;    // manages connections required for Master comms
    private ServerConnection _coordinatorConnection = null;         // special connection for Master
    private HashSet< AbstractEntity > _entities = new HashSet();

    private static boolean _setup = false;

    public static void setup( String slaveHost, int slaveListenerPort, String slaveContextPath, String masterHost, int masterPort ) {
        _contextPath = slaveContextPath;
        _listenerPort = slaveListenerPort;
        _host = slaveHost;

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

        _cm = new ConnectionManager();

        startServer();

        _cm.addListener( this );
        _coordinatorConnection = _cm.registerServer(
                _masterHost,
                _masterPort,
                CoordinatorMaster.sContextPath );
    }

    public void addEntity( AbstractEntity abstractEntity ) {
        _entities.add( abstractEntity );
    }

    @Override
    protected void setServiceImplementation() {

        // inject service implementations to be used by the server lib
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
            _logger.log( Level.FINE, "Connection to server accepted, now send request to connect to this (node) running Server.." );
            io.agi.ef.clientapi.api.ConnectApi capi = new io.agi.ef.clientapi.api.ConnectApi( sc.getClientApi() );

            String port = "" + _listenerPort;
            capi.connectHostHostPortPortContextPathContextPathGet( _host, port, _contextPath );
        }
    }

    /**
     * We are in the Slave, so this has the meaning, connectMaster
     */
    @Override
    public Response connect( String host, String port, String contextPath ) {
        return  Response.ok().entity( new ApiResponseMessage( ApiResponseMessage.ERROR, "'Connect' can only be used with Master (not Slave) Coordinator(s)" ) ).build();
    }

    // TODO: In command() and status() add RXJava Filter by entityName()

    @Override
    public Response command( final String entityName, final String command ) {

        rx.Observable.from( _entities ).subscribe( new Action1<AbstractEntity>() {
            @Override
            public void call( AbstractEntity entity ) {
                try {
                    entity.command( command );
                }
                catch ( Exception e ) {
                    e.printStackTrace();
                }
            }
        } );

        return  Response.ok().entity( new ApiResponseMessage( ApiResponseMessage.OK, "Slave sent to entity: " + entityName + ", the command: " + command ) ).build();
    }

    @Override
    public Response status( final String entityName, final String state ) {

        rx.Observable.from( _entities ).subscribe( new Action1< AbstractEntity >() {
            @Override
            public void call( AbstractEntity entity ) {
                entity.status( state );
            }
        } );

        return  Response.ok().entity( new ApiResponseMessage( ApiResponseMessage.OK, "Slave sent sent request to entity: " + entityName +  ", the status for state: " + state )).build();
    }

}
