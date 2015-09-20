package io.agi.ef.demo.helloworld;

import io.agi.core.data.Data;
import io.agi.ef.Entity;
import io.agi.ef.entities.experiment.Motor;

import java.util.HashSet;

/**
 * Triangle wave of light brightness.
 * This class is neither a sensor nor a motor.
 *
 * Created by gideon on 1/08/15.
 */
public class LightSource extends Entity {

    public static final String ENTITY_TYPE = "light-source";

    public static final String PROPERTY_MIN = "min";
    public static final String PROPERTY_MAX = "max";
    public static final String PROPERTY_STATE = "state";

    public static final String DATA_OUTPUT = "output";

    public LightSource() {
    }

    public void configure(String config) {

        float min = configureProperty( config, PROPERTY_MIN, 0.f );
        addProperty( PROPERTY_MIN, min );

        float max = configureProperty( config, PROPERTY_MAX, 1.f );
        addProperty( PROPERTY_MAX, max );

        int state = configureProperty( config, PROPERTY_STATE, 0 );
        addProperty( PROPERTY_STATE, state );

        Data d2 = new Data( 1 );
        addData(DATA_OUTPUT, d2);
    }

    @Override
    public void doStep( HashSet< String > dirtyData ) {
        super.doStep(dirtyData);

        float min = getPropertyAsFloat( PROPERTY_MIN );
        float max = getPropertyAsFloat(PROPERTY_MAX);
        int state = getPropertyAsInt(PROPERTY_STATE);

        Data d = getData(DATA_OUTPUT);
        float brightness = d._values[ 0 ];

        if( state == 0 ) {
            brightness++;

            if ( brightness >= max ) {
                state = 1;
                setProperty( PROPERTY_STATE, state );
            }
        }
        else {
            brightness--;

            if ( brightness <= min ) {
                state = 0;
                setProperty( PROPERTY_STATE, state );
            }
        }

        Data d2 = new Data( 1 );
        d2._values[ 0 ] = brightness;
        setData( DATA_OUTPUT, d2 );
        //_outData.set( _brightness );
    }

}
