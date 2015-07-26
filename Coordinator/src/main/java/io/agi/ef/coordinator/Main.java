package io.agi.ef.coordinator;

import io.agi.ef.ConnectionManager;
import io.agi.ef.EndpointUtils;
import io.agi.ef.coordinator.services.ConnectApiServiceImpl;
import io.agi.ef.coordinator.services.ControlApiServiceImpl;
import io.agi.ef.coordinator.services.DataApiServiceImpl;
import io.agi.ef.serverapi.api.factories.ConnectApiServiceFactory;
import io.agi.ef.serverapi.api.factories.ControlApiServiceFactory;
import io.agi.ef.serverapi.api.factories.DataApiServiceFactory;
import org.eclipse.jetty.server.Server;



public class Main {


    public static void main(String[] args) throws Exception {

        // inject service implementations to be used by the server lib
        DataApiServiceFactory.setService( new DataApiServiceImpl() );
        ControlApiServiceFactory.setService( new ControlApiServiceImpl() );
        ConnectApiServiceFactory.setService( new ConnectApiServiceImpl() );

        // start server
        Server server = ConnectionManager.getInstance().setupServer(    EndpointUtils.coordinatorListenPort(),
                                                                        EndpointUtils.coordinatorContextPath() );
        server.start();
        server.join();
    }


}


