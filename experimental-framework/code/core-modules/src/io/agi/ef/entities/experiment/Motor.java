package io.agi.ef.entities.experiment;

import io.agi.core.data.Data;
import io.agi.ef.Entity;

/**
 *
 * The Agent acts in the the World via an Actuator.
 *
 * Created by gideon on 1/08/15.
 */
public class Motor extends Entity {

    public static final String ENTITY_TYPE = "motor";
    public static final String DATA_INPUT = "input";
    public static final String DATA_OUTPUT = "output";

    public Motor() {
    }

    public void configure(String config) {
        Data d1 = configureData( config, DATA_INPUT );
        if( d1 == null ) {
            d1 = new Data( 1 );
        }
        addData(DATA_INPUT, d1);

        Data d2 = configureData( config, DATA_OUTPUT );
        if( d2 == null ) {
            d2 = new Data( 1 );
        }
        addData(DATA_OUTPUT, d2);
    }

    public static boolean SetInput( String motorName, Data d ) {
        return SetData(motorName, DATA_INPUT, d);
    }

    public static Data GetOutput( String motorName ) {
        return GetData( motorName, DATA_OUTPUT );
    }

//    /**
//     * Update the input of the Actuator.
//     * @param input
//     */
//    public abstract void setInput( Data input );
//
//    /**
//     * Get current output of Actuator. It can change after an update() cycle.
//     * @return
//     */
//    public abstract Data getOutput();
}
