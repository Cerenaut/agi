package io.agi.ef.experiment.entities;


import io.agi.interprocess.coordinator.CoordinatorSlave;
import io.agi.interprocess.apiInterfaces.ControlInterface;

import java.util.logging.Logger;

/**
 * Base class for Agent and World (referred to generically as 'entities').
 *
 * Created by gideon on 3/08/15.
 */
public abstract class AbstractEntity implements ControlInterface {

    private String _name = null;
    private int _time = 0;                      // time step
    CoordinatorSlave _slave = null;

    public AbstractEntity() {
        super();
    }

    protected abstract Logger getLogger();

    public int getTime() {
        return _time;
    }

    protected void incTime() {
        ++_time;
    }

    public String getName() {
        return _name;
    }
}
