package io.agi.ef.interprocess.coordinator;

import io.agi.ef.experiment.entities.AbstractEntity;
import io.agi.ef.interprocess.ConnectionManager;
import io.agi.ef.interprocess.apiInterfaces.ControlInterface;
import org.eclipse.jetty.server.Server;

import java.util.logging.Logger;

/**
 *
 * Abstract base class of Master and Slave Coordinator.
 * Collects the common interface to any part of the system.
 *
 * Created by gideon on 29/08/15.
 */
public abstract class Coordinator implements ControlInterface {

    protected Logger _logger = null;
    protected ConnectionManager _cm = null;

    public Coordinator () {
        _logger = Logger.getLogger( this.getClass().getPackage().getName() );
        _cm = new ConnectionManager();
    }

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

    protected void startServer() throws Exception {
        setServiceImplementation();

        Thread serverThread = new Thread( new RunServer() );
        serverThread.start();
    }

    abstract protected void setServiceImplementation();
    abstract protected String contextPath();
    abstract protected int listenerPort();
}
