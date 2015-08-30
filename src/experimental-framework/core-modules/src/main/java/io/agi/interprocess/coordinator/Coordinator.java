package io.agi.interprocess.coordinator;

import io.agi.interprocess.ConnectionManager;
import io.agi.interprocess.apiInterfaces.ControlInterface;
import org.eclipse.jetty.server.Server;

/**
 *
 * Abstract base class of Master and Slave Coordinator.
 * Collects the common interface to any part of the system.
 *
 * Created by gideon on 29/08/15.
 */
public abstract class Coordinator implements ControlInterface {

    protected ConnectionManager _cm = null;

    /**
     * Run the Master-Coordinator server.
     */
    private class RunServer implements Runnable {
        public void run() {
            Server server = _cm.setupServer( contextPath(), listenerPort() );
            try {
                server.start();
                server.join();
            }
            catch ( Exception e ) {
                e.printStackTrace();
            }
        }
    }

    Coordinator() {
        _cm = new ConnectionManager();
    }

    protected void startServer() throws Exception {
        setServiceImplementation();

        Thread serverThread = new Thread( new RunServer() );
        serverThread.start();
    }

    abstract protected void setServiceImplementation();
    abstract protected String contextPath();
    abstract protected int listenerPort();
}
