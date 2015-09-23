package io.agi.ef.interprocess.coordinator;

import io.agi.ef.clientapi.model.TStamp;
import io.agi.ef.interprocess.ServerConnection;
import io.agi.ef.serverapi.api.ApiResponseMessage;
import io.agi.ef.interprocess.apiInterfaces.ControlInterface;

import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * This is used by the Master as a proxy to the CoordinatorSlave, also referred to as CoordinatorProxy.
 * In other words, it is a local representation of the Slave.
 * e.g. The Master tells it to step, and it passes the step on to the slave node.
 *
 * It allows for future implementation of other types of proxies that may communicate over other channels.
 * It will just need to comply with the ControlInterface interface.
 *
 * Created by gideon on 3/08/15.
 */
public abstract class CoordinatorSlaveProxy implements ControlInterface {

    private Logger _logger = null;
    ServerConnection _sc = null;

    public CoordinatorSlaveProxy( ServerConnection sc ) {
        _logger = Logger.getLogger( this.getClass().getPackage().getName() );
        _sc = sc;
    }

// renamed to entity event, and implemented in Entity class or Coordinator base class.
    public Response command( String entityName, String command ) {

        Response response = null;
        TStamp tstamp = null;

        if ( _sc.getClientApi() == null ) {
            response = Response.ok().entity( new ApiResponseMessage( ApiResponseMessage.OK, "The server connection is invalid." ) ).build();
        }
        else {
            io.agi.ef.clientapi.api.ControlApi capi = new io.agi.ef.clientapi.api.ControlApi( _sc.getClientApi() );
            try {
                tstamp = capi.controlEntityEntityNameCommandCommandGet( entityName, command );
            }
            catch ( io.agi.ef.clientapi.ApiException e ) {
                e.printStackTrace();
            }
            // todo: this catches a connection refused exception, but should be tidied up
            catch ( Exception e ) {
                e.printStackTrace();
            }
            response = Response.ok().entity( tstamp ).build();
        }

        return response;
    }

// Deprecated API call
//    @Override
//    public Response status( String entityName, String state ) {
//
//        Response response = null;
//        boolean ret = false;
//
//        if ( _sc.getClientApi() == null ) {
//            response = Response.ok().entity( new ApiResponseMessage( ApiResponseMessage.OK, "The server connection is invalid." ) ).build();
//        }
//        else {
//            io.agi.ef.clientapi.api.ControlApi capi = new io.agi.ef.clientapi.api.ControlApi( _sc.getClientApi() );
//            try {
//                ret = capi.controlEntityEntityNameStatusStateGet( entityName, state );
//            }
//            catch ( io.agi.ef.clientapi.ApiException e ) {
//                e.printStackTrace();
//            }
//            // todo: this catches a connection refused exception, but should be tidied up
//            catch ( Exception e ) {
//                e.printStackTrace();
//            }
//            response = Response.ok().entity( ret ).build();
//        }
//
//        return response;
//    }
}
