package io.agi.ef.experiment.actuators;

import io.agi.core.data.Data;

/**
 *
 * The Agent acts in the the World via an Actuator.
 *
 * Created by gideon on 1/08/15.
 */
public abstract class Actuator {

    public Actuator( int dimInput, int dimOutput ) {}

    /**
     * Update the input of the Actuator.
     * @param input
     */
    public abstract void setInput( Data input );

    /**
     * Step Actuator.
     */
    public abstract void step();

    /**
     * Get current output of Actuator. It can change after an update() cycle.
     * @return
     */
    public abstract Data getOutput();
}
