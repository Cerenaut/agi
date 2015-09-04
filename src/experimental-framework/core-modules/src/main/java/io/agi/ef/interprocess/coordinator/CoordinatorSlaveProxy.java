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
 * This is used by the Master as a proxy to the Master slave, also referred to as CoordinatorProxy.
 * The class used by remote nodes to access the Master functionality.
 *
 * It is essentially used as a local representation of the remote CoordinatorProxy.
 * e.g. The Master tells it to step, and it passes the step on to the remote Master
 *
 * Created by gideon on 3/08/15.
 */
public class CoordinatorSlaveProxy implements ControlInterface {

    private Logger _logger = null;
    ServerConnection _sc = null;

    public CoordinatorSlaveProxy( ServerConnection sc ) {
        _logger = Logger.getLogger( this.getClass().getPackage().getName() );
        _sc = sc;
    }

    @Override
    public Response command( String entityName, String command ) {

        Response response = null;

        if ( _sc.getClientApi() == null ) {
            response = Response.ok().entity( new ApiResponseMessage( ApiResponseMessage.OK, "The server connection is invalid." ) ).build();
        }
        else {
            HashMap<String, List<TStamp>> serverTimeStamps = new HashMap<>();
            List<io.agi.ef.clientapi.model.TStamp> tStamps;

            io.agi.ef.clientapi.api.ControlApi capi = new io.agi.ef.clientapi.api.ControlApi( _sc.getClientApi() );
            try {

                // todo: known bug: doesn't seem to be able to deserialise tStamps, but i want to change their format anyway
                tStamps = capi.controlEntityEntityNameCommandCommandGet( entityName, command );
                serverTimeStamps.put( _sc.basePath(), tStamps );
            }
            catch ( io.agi.ef.clientapi.ApiException e ) {
                e.printStackTrace();
            }
            // todo: this catches a connection refused exception, but should be tidied up
            catch ( Exception e ) {
                e.printStackTrace();
            }
            response = Response.ok().entity( serverTimeStamps ).build();
        }

        return response;
    }

    @Override
    public Response status( String entityName, String state ) {
        return null;
    }
}
