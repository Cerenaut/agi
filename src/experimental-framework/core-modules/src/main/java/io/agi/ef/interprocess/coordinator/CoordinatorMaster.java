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

import javax.ws.rs.core.Response;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

// ************************************************
// WARNING !!!!!! :        currently assumes that slaves are at localhost on port 8081  (hardcoded below)
// ************************************************


/**
 *
 * This is one of the main modules of the AGIEF.
 * It is the heart of Interprocess communication.
 *
 * todo: to copy material from the writeup
 *
 * Created by gideon on 30/07/15.
 */
public class CoordinatorMaster extends Coordinator implements ConnectInterface, ConnectionManagerListener {

    private Logger _logger = null;
    public static final String sContextPath = "coordinator";
    private final int _port;
    private HashSet< ControlInterface > _slave = new HashSet();

    // todo: move to the Experiment class
    private boolean _running = false;

    private class RunExperiment implements Runnable {
        public void run() {
            while ( true ) {
                if ( _running == true ) {
                    step();
                }

                try {
                    Thread.sleep( 200 );
                }
                catch ( InterruptedException e ) {
                    e.printStackTrace();
                }
            }
        }
    }

    public CoordinatorMaster( int port ) throws Exception {
        super();

        _logger = Logger.getLogger( this.getClass().getPackage().getName() );

        _port = port;
        _cm.addListener( this );

        startServer();

        // todo: move to the Experiment class
        Thread expThread = new Thread( new RunExperiment() );
        expThread.start();
    }

    protected void setServiceImplementation() {

        // inject service implementations to be used by the server lib
        ControlApiServiceImpl controlApiService = new ControlApiServiceImpl();
        controlApiService._serviceDelegate = this;
        ControlApiServiceFactory.setService( controlApiService );

        ConnectApiServiceImpl connectApiService = new ConnectApiServiceImpl();
        connectApiService._serviceDelegate = this;
        ConnectApiServiceFactory.setService( connectApiService );
    }

    protected String contextPath() {
        return CoordinatorMaster.sContextPath;
    }

    protected int listenerPort() {
        return _port;
    }

    @Override
    public void connectionAccepted( ServerConnection sc ) throws ApiException {
        CoordinatorSlaveProxy slaveProxy = new CoordinatorSlaveProxy( sc );
        addSlave( slaveProxy );
    }

    private void addSlave( ControlInterface slave ) {
        _slave.add( slave );
    }


    /**
     * We are in the Master, so this has the meaning, connectSlave
     */
    @Override
    public Response connect( String host, String port, String contextPath ) {
        Response response = null;

        int portInt = Integer.valueOf( port );

        _cm.registerServer( host, portInt, contextPath );
        response = Response.ok().entity( new ApiResponseMessage( ApiResponseMessage.OK, "connect to agent: " + contextPath + "." ) ).build();
        return response;
    }


    @Override
    public Response command( String command ) {

        Response response = null;
        if ( command.equalsIgnoreCase( ControlCommand.RUN ) ) {
            response = run();
        }
        else if ( command.equalsIgnoreCase( ControlCommand.STEP ) ) {
            response = step();
        }
        else if ( command.equalsIgnoreCase( ControlCommand.STOP ) ) {
            response = stop();
        }

        return response;
    }

    @Override
    public Response status( String state ) {
        return null;
    }

    public Response run() {
        _running = true;
        Response response = Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "Run Run Run")).build();
        return response;
    }

    public Response step() {
        Response response = null;
        response = stepAllSlaves();
        _logger.log( Level.FINE, "Stepped all servers, the response is: {0}", response );

        return response;
    }

    public Response stop() {
        // stop the clients
        _running = false;
        Response response = Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "Stop stop stop")).build();
        return response;
    }

    /**
     * Step all Agents, and provide the combined state.
     * @return
     */
    private Response stepAllSlaves() {

        Response response = null;

        if ( _slave.size() != 0 ) {
            for ( ControlInterface slave : _slave ) {
                slave.command( ControlCommand.STEP );
            }
            response = Response.ok().entity( new ApiResponseMessage( ApiResponseMessage.OK, "All agents stepped." ) ).build();
        }
        else {
            response = Response.ok().entity( new ApiResponseMessage( ApiResponseMessage.OK, "There are no connected Agents to step." ) ).build();
        }

        return response;
    }

}