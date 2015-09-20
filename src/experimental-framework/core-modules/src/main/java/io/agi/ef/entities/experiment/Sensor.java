package io.agi.ef.entities.experiment;

import io.agi.core.data.Data;
import io.agi.ef.Entity;


/**
 *
 * The means by which an Agent can sense the World.
 * This helper class creates a single sensor matrix that is populated and sized on configure().
 *
 * Created by gideon on 1/08/15.
 */
public class Sensor extends Entity {

    public static final String ENTITY_TYPE = "sensor";
    public static final String DATA_SENSED = "sensed";

    public Sensor() {

    }

    public void configure(String config) {
        Data d = configureData( config, DATA_SENSED );
        if( d == null ) {
            d = new Data( 1 );
        }
        addData(DATA_SENSED, d);
    }

    public static Data GetSensed( String sensorName ) {
        return GetData( sensorName, DATA_SENSED );
    }
//    /**
//     * Get current output of Sensor. It can change after an updateAndStep() cycle.
//     * @return
//     */
//    public abstract Data getOutput();
}
