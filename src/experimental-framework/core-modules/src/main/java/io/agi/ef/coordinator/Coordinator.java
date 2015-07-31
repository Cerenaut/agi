package io.agi.ef.coordinator;

import io.agi.ef.coordinator.services.*;
import io.agi.ef.core.ConnectionManager;
import io.agi.ef.core.ControlInterface;
import io.agi.ef.core.EndpointUtils;
import io.agi.ef.serverapi.api.factories.ConnectApiServiceFactory;
import io.agi.ef.serverapi.api.factories.ControlApiServiceFactory;
import io.agi.ef.serverapi.api.factories.DataApiServiceFactory;
import org.eclipse.jetty.server.Server;

import java.util.ArrayList;

/**
 * Created by gideon on 30/07/15.
 */
public class Coordinator implements ControlInterface {

    private class RunServer implements Runnable {
        public void run() {
            // instantiate and run server
            Server server = ConnectionManager.getInstance().setupServer(
                    EndpointUtils.coordinatorListenPort(),
                    EndpointUtils.coordinatorContextPath() );
            try {
                server.start();
                server.join();
            }
            catch ( Exception e ) {
                e.printStackTrace();
            }
        }
    }

    public Coordinator() { }


    public void setupProperties( ArrayList<String> properties ) {}

    public void start() throws Exception {

        // inject service implementations to be used by the server lib
        DataApiServiceFactory.setService( new DataApiServiceImpl() );

        ControlApiServiceImpl controlApiServiceImpl = new ControlApiServiceImpl();
        controlApiServiceImpl._coordinator = this;
        ControlApiServiceFactory.setService( controlApiServiceImpl );

        ConnectApiServiceFactory.setService( new ConnectApiServiceImpl() );


        Thread th = new Thread( new RunServer() );
        th.start();
    }


    @Override
    public void run() {

    }

    @Override
    public void step() {
        /*
            THIS SHOULD PROBABLY JUST BE CALLED BACK TO FROM ControlApiServiceImpl,
            THEN WE COULD TRIGGER THE NEXT ACTION VIA A NETWORK LAYER (currently all in ControlApiServiceImpl)

            e.g. ConnectionManager.forAllServers.step();
         */

    }

    @Override
    public void stop() {

    }


}