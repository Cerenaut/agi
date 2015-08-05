package io.agi.ef.core.network.entities;

import io.agi.ef.core.CommsMode;
import io.agi.ef.core.apiInterfaces.ControlInterface;
import io.agi.ef.core.apiInterfaces.DataInterface;

import java.util.logging.Logger;

/**
 * Base class for Agent and World (referred to generically as 'entities'.
 *
 * Created by gideon on 3/08/15.
 */
public abstract class AbstractEntity implements ControlInterface, DataInterface {

    private int _time = 0;                      // time step
    private final CommsMode _commsMode;
    NetworkEntity _networkEntity = null;

    /**
     * Constructor with no parameters sets the CommsMode to non-network.
     */
    public AbstractEntity() {
        super();
        _commsMode = CommsMode.NON_NETWORK;    // Default context path. This will conflict with other agents if not customised.
    }

    /**
     * Constructor with contextPath parameter sets the CommsMode to Network.
     */
    public AbstractEntity( String contextPath, int port ) throws Exception {
        super();
        _commsMode = CommsMode.NETWORK;

        _networkEntity = new NetworkEntity( contextPath, port );
        _networkEntity.start( this );
    }

    protected abstract Logger getLogger();

    public int getTime() {
        return _time;
    }

    protected void incTime() {
        ++_time;
    }
}
