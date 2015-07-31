package io.agi.ef.agent;


import io.agi.ef.agent.services.ControlApiServiceImpl;
import io.agi.ef.agent.services.DataApiServiceImpl;
import io.agi.ef.clientapi.ApiException;
import io.agi.ef.core.ConnectionManager;
import io.agi.ef.core.ControlInterface;
import io.agi.ef.core.DataInterface;
import io.agi.ef.core.EndpointUtils;
import io.agi.ef.serverapi.api.factories.ControlApiServiceFactory;
import io.agi.ef.serverapi.api.factories.DataApiServiceFactory;
import org.eclipse.jetty.server.Server;

import java.util.ArrayList;


/**
 * Created by gideon on 26/07/15.
 */
public class Agent implements ConnectionManager.ConnectionManagerListener, ControlInterface, DataInterface {

    ConnectionManager.ServerConnection _coordinatorConnection = null;
    private String _agentContextPath = null;

    private int _time = 0;

    /**
     *
     * @param agentContextPath is the word used in the context path i.e. no '/'
     * @throws Exception
     */
    public Agent( String agentContextPath ) {
        _agentContextPath = agentContextPath;
    }

    private class RunServer implements Runnable {
        public void run() {
            // run agent server
            // todo: this should first, so need to move that server initiation into its own thread
            Server server = ConnectionManager.getInstance().setupServer(
                    EndpointUtils.agentListenPort(),
                    _agentContextPath );
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
    public void connectionAccepted( ConnectionManager.ServerConnection sc ) throws ApiException {
        if ( sc.getId() == _coordinatorConnection.getId() ) {

            System.out.println( "Sending request to coordinator" );

            io.agi.ef.clientapi.api.ConnectApi capi = new io.agi.ef.clientapi.api.ConnectApi( sc.getClientApi() );
            // TODO: Use contextPath for consistency, that is the string that was sent as the name of the agent
            capi.connectAgentBaseurlGet( _agentContextPath );

            System.out.println( "Sent request to coordinator" );
        }
    }

    public void setupProperties( ArrayList<String> properties ) { }

    public void start() throws Exception {

        // inject service implementations to be used by the server lib
        DataApiServiceFactory.setService( new DataApiServiceImpl() );
        ControlApiServiceImpl controlApiService = new ControlApiServiceImpl();
        controlApiService._agent = this;
        ControlApiServiceFactory.setService( controlApiService );
        // ConnectApiServiceFactory.setService( new ConnectApiServiceImpl() );

        // start server
        // ------------------------------------------------------------
        Thread th = new Thread( new RunServer() );
        th.start();

        // connect to coordinator
        // ------------------------------------------------------------
        _coordinatorConnection = ConnectionManager.getInstance().registerServer(
                ConnectionManager.ServerConnection.ServerType.Coordinator,
                EndpointUtils.coordinatorListenPort(),
                EndpointUtils.coordinatorContextPath() );

        ConnectionManager.getInstance().addListener( this );
    }

    @Override
    public final void run() {
        // To Discuss:
        // I don't think this should be an option in AGENT - only in Coordinator for synchronisation
        // Dave will disagree
    }

    @Override
    public void step() {
        _time++;
    }

    @Override
    public final void stop() {
        // this shouldn't be implemented in derived classes
        // for reasons given for 'run()' above
    }

    @Override
    public void state() {

    }

    public int getTime() {
        return _time;
    }
}
