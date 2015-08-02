package io.agi.ef.core.sensors;

import io.agi.ef.core.UniversalState;

/**
 *
 * The Agent acts in the the World via an Actuator.
 *
 * Created by gideon on 1/08/15.
 */
public abstract class Sensor {

    public abstract void update( UniversalState worldState );
}
