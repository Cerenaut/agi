package io.agi.ef.agent;

import io.agi.ef.ConnectionManager;
import io.agi.ef.EndpointUtils;
import io.agi.ef.agent.services.ControlApiServiceImpl;
import io.agi.ef.agent.services.DataApiServiceImpl;
import io.agi.ef.clientapi.ApiException;
import io.agi.ef.serverapi.api.factories.ControlApiServiceFactory;
import io.agi.ef.serverapi.api.factories.DataApiServiceFactory;
import org.eclipse.jetty.server.Server;



/**
 * Created by gideon on 26/07/15.
 */
public class Agent implements ConnectionManager.ConnectionManagerListener {

    ConnectionManager.ServerConnection _coordinatorConnection = null;
    private String _agentContextPath = null;

    /**
     *
     * @param agentContextPath is the word used in the context path i.e. no '/'
     * @throws Exception
     */
    public Agent( String agentContextPath ) throws Exception {

        _agentContextPath = agentContextPath;


        // connect to coordinator
        // ------------------------------------------------------------

        _coordinatorConnection = ConnectionManager.getInstance().registerServer(
                ConnectionManager.ServerConnection.ServerType.Coordinator,
                EndpointUtils.coordinatorListenPort(),
                EndpointUtils.coordinatorContextPath() );

        ConnectionManager.getInstance().addListener( this );



        // start server
        // ------------------------------------------------------------

        // inject service implementations to be used by the server lib
        DataApiServiceFactory.setService( new DataApiServiceImpl() );
        ControlApiServiceFactory.setService( new ControlApiServiceImpl() );
        // ConnectApiServiceFactory.setService( new ConnectApiServiceImpl() );


        // run agent server
        // todo: this should first, so need to move that server initiation into its own thread
        Server server = ConnectionManager.getInstance().setupServer(    EndpointUtils.agentListenPort(),
                                                                        _agentContextPath );
        server.start();
        server.join();
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
}
