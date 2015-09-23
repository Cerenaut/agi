package io.agi.ef.interprocess.coordinator;

import io.agi.ef.clientapi.ApiException;
import io.agi.ef.interprocess.apiInterfaces.ConnectInterface;
import io.agi.ef.interprocess.apiInterfaces.ControlInterface;
import io.agi.ef.interprocess.coordinator.services.ControlApiServiceImpl;
import io.agi.ef.interprocess.coordinator.services.ConnectApiServiceImpl;
import io.agi.ef.interprocess.ConnectionManagerListener;
import io.agi.ef.interprocess.ServerConnection;
import io.agi.ef.serverapi.api.ApiResponseMessage;
import io.agi.ef.serverapi.api.factories.ConnectApiServiceFactory;
import io.agi.ef.serverapi.api.factories.ControlApiServiceFactory;

import rx.Observable;
import rx.functions.Action1;

import javax.ws.rs.core.Response;
import java.util.*;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is one of the main modules of the AGIEF. It is the heart of Interprocess communication.
 * Provide the distributed interprocess communication, for state and control.
 * The CoordinatorMaster is only accessible via http, it communicates with Slaves,
 * that each have local interaction with Entities.
 *
 * Created by gideon on 30/07/15.
 */
public class CoordinatorMaster extends Coordinator implements ConnectInterface, ConnectionManagerListener {

    private Logger _logger = null;
    public static final String sContextPath = "coordinator";
//    private final int _port; // moved to NodeServer
    private HashSet< ControlInterface > _slaves = new HashSet();


    public CoordinatorMaster( int port ) throws Exception {
        super();

        _logger = Logger.getLogger( this.getClass().getPackage().getName() ); // moved to NodeServer
//        _port = port; // moved to NodeServer
//        _cm.addListener( this );
//
//        startServer();
    }

    protected void setServiceImplementation() {

        // inject service implementations to be used by the server lib
        ControlApiServiceImpl controlApiService = new ControlApiServiceImpl();
        controlApiService._serviceDelegate = this;
        ControlApiServiceFactory.setService( controlApiService );

        ConnectApiServiceImpl connectApiService = null; //new ConnectApiServiceImpl();
//        connectApiService._serviceDelegate = this;
        ConnectApiServiceFactory.setService( connectApiService );
    }

// Moved to NodeServer
//    protected String contextPath() {
//        return CoordinatorMaster.sContextPath;
//    }
//
// Moved to NodeServer
//    protected int listenerPort() {
//        return _port;
//    }

    @Override
    public void connectionAccepted( ServerConnection sc ) throws ApiException {
        CoordinatorSlaveProxy slaveProxy = null;//new CoordinatorSlaveProxy( sc );
        addSlave( slaveProxy );
    }

    private void addSlave( ControlInterface slave ) {
        _slaves.add( slave );
    }

// Connect API deprecated - Nodes persist themselves to the database
//    /**
//     * We are in the Master, so this has the meaning, connectSlave
//     */
//    public Response connect( String host, String port, String contextPath ) {
//        Response response = null;
//
//        int portInt = Integer.valueOf( port );
//
////        _cm.registerServer( host, portInt, contextPath );
//        response = Response.ok().entity( new ApiResponseMessage( ApiResponseMessage.OK, "connect to agent: " + contextPath + "." ) ).build();
//        return response;
//    }

// Renamed to entity/name,action event
// Can't have object representation of subscribers, so not sure if reactive can be used.
    public Response command( final String entityName, final String command ) {

        _logger.log( Level.FINE, "**CONTROL/COMMAND: Received command request for entity: (" + entityName + ", " + command + ")." );
        _logger.log( Level.FINE, "Distribute to all slaves.");

//        Observable.from( _slaves ).subscribe( new Action1<ControlInterface>() {
//            @Override
//            public void call( ControlInterface slave ) {
//                slave.command( entityName, command );
//            }
//        } );

        // todo: it is expecting a timestamp, so modify code above to identify one - but still thinking about design
        Response response = Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "'command' sent to all slaves")).build();
//        return response;
        return null;
    }

// Deprecated API call
//    public Response status( final String entityName, final String state ) {
//
//        _logger.log( Level.FINE, "**CONTROL/STATUS: Received status request for entity: (" + entityName + ", " + state + ")." );
//        _logger.log( Level.FINE, "Distribute to all slaves.");
//
////        Observable.from( _slaves ).subscribe( new Action1<ControlInterface>() {
////            @Override
////            public void call( ControlInterface slave ) {
////                slave.status( entityName, state );
////            }
////        } );
//
//        // todo: it is expecting one boolean, so modify code above to identify one - but still thinking about design
//        Response response = Response.ok().entity( new ApiResponseMessage( ApiResponseMessage.OK, "Status request sent to all slaves" ) ).build();
////        return response;
//        return null;
//    }

}