package io.agi.ef.core.sensors;

import io.agi.core.data.Data;
import io.agi.ef.core.UniversalState;

/**
 *
 * The means by which an Agent can sense the World.
 *
 * Created by gideon on 1/08/15.
 */
public abstract class Sensor {

    public Sensor( int dimInput, int dimOutput ) {}

    /**
     * Provide worldState and step sensor.
     * @param worldState
     */
    public abstract void update( UniversalState worldState );

    /**
     * Get current output of Sensor. It can change after an updateAndStep() cycle.
     * @return
     */
    public abstract Data getOutput();
}
