package io.agi.ef.experiment.sensors;

import io.agi.core.data.Data;


/**
 *
 * The means by which an Agent can sense the World.
 *
 * Created by gideon on 1/08/15.
 */
public abstract class Sensor {

    public Sensor( int dimInput, int dimOutput ) {}

    public abstract void update( );

    /**
     * Get current output of Sensor. It can change after an updateAndStep() cycle.
     * @return
     */
    public abstract Data getOutput();
}
