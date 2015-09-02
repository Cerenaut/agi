package io.agi.ef.experiment.entities;


import io.agi.ef.interprocess.coordinator.CoordinatorSlave;
import io.agi.ef.interprocess.apiInterfaces.ControlInterface;
import io.agi.ef.interprocess.coordinator.CoordinatorSlaveDelegate;

import javax.ws.rs.core.Response;
import java.util.logging.Logger;

/**
 * Base class for Agent and World (referred to generically as 'entities').
 *
 * Created by gideon on 3/08/15.
 */
public abstract class AbstractEntity implements ControlInterface, CoordinatorSlaveDelegate {

    protected Logger _logger = null;
    private String _name = null;
    private int _time = 0;                      // time step
    CoordinatorSlave _slave = null;

    public AbstractEntity( String name ) throws Exception {
        _logger = Logger.getLogger( this.getClass().getPackage().getName() );

        _name = name;
        _slave = CoordinatorSlave.getInstance();

        _slave.set_delegate( this );
    }

    public int getTime() {
        return _time;
    }

    protected void incTime() {
        ++_time;
    }

    public String name() {
        return _name;
    }



    @Override
    public Response command( String command ) {
        return null;
    }

    @Override
    public Response status( String state ) {
        return null;
    }
}
