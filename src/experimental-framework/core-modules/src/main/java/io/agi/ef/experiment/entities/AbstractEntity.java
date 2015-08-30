package io.agi.ef.experiment.entities;


import io.agi.interprocess.coordinator.CoordinatorSlave;
import io.agi.interprocess.apiInterfaces.ControlInterface;
import io.agi.interprocess.coordinator.CoordinatorSlaveDelegate;

import java.util.logging.Logger;

/**
 * Base class for Agent and World (referred to generically as 'entities').
 *
 * Created by gideon on 3/08/15.
 */
public abstract class AbstractEntity implements ControlInterface, CoordinatorSlaveDelegate {

    // todo: move _logger to here, and get class name dynamically, instead of hardcoding to THIS Class name.
    // String className = this.getClass().getSimpleName();

    private String _name = null;
    private int _time = 0;                      // time step
    CoordinatorSlave _slave = null;

    public AbstractEntity( String name ) throws Exception {
        _name = name;
        _slave = CoordinatorSlave.getInstance();

        _slave.setDelegate( this );
    }

    protected abstract Logger getLogger();

    public int getTime() {
        return _time;
    }

    protected void incTime() {
        ++_time;
    }

    public String name() {
        return _name;
    }
}
