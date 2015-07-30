package io.agi.ef.coordinator;

import io.agi.ef.coordinator.services.*;
import io.agi.ef.core.ConnectionManager;
import io.agi.ef.core.EndpointUtils;
import io.agi.ef.serverapi.api.factories.ConnectApiServiceFactory;
import io.agi.ef.serverapi.api.factories.ControlApiServiceFactory;
import io.agi.ef.serverapi.api.factories.DataApiServiceFactory;
import org.eclipse.jetty.server.Server;

import java.util.ArrayList;

/**
 * Created by gideon on 30/07/15.
 */
public class Coordinator {

    public Coordinator() throws Exception {

    }

    public void setupProperties( ArrayList<String> properties ) {
//        _var = DEFAULT_VAL;
//        for( String file : properties ) {
//            _var = Integer.valueOf( io.agi.properties.Properties.get( file, PROPERTY_VAL, String.valueOf( _var ) ) );
//        }
    }

    public void start() throws Exception {

        // inject service implementations to be used by the server lib
        DataApiServiceFactory.setService( new DataApiServiceImpl() );
        ControlApiServiceFactory.setService( new ControlApiServiceImpl() );
        ConnectApiServiceFactory.setService( new ConnectApiServiceImpl() );

        // instantiate and run server
        Server server = ConnectionManager.getInstance().setupServer(
                EndpointUtils.coordinatorListenPort(),
                EndpointUtils.coordinatorContextPath() );
        server.start();
        server.join();
    }
}