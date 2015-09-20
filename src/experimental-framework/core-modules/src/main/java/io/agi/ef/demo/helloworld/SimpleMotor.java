package io.agi.ef.demo.helloworld;

import com.sun.xml.bind.v2.TODO;
import io.agi.core.data.Data;
import io.agi.ef.entities.experiment.Motor;

import java.util.HashSet;

/**
 * Simple motor. The input equals the output with a gain factor.
 *
 * Created by gideon on 1/08/15.
 */
public class SimpleMotor extends Motor {

    public static final String ENTITY_TYPE = "simple-motor";

    public static final String PROPERTY_GAIN = "gain";

    public SimpleMotor() {
    }

    public void configure(String config) {

        float gain = configureProperty( config, PROPERTY_GAIN, 1.f );
        addProperty( PROPERTY_GAIN, gain );

        Data d1 = new Data( 1 );
        addData(DATA_INPUT, d1);

        Data d2 = new Data( 1 );
        addData(DATA_OUTPUT, d2);
    }

    @Override
    public void doStep( HashSet< String > dirtyData ) {
        super.doStep(dirtyData);

        Data d1 = getData( DATA_INPUT );

        float input = d1._values[ 0 ];
        float gain = getPropertyAsFloat( PROPERTY_GAIN );
        float output = input * gain;

        Data d2 = new Data( 1 );
        d2._values[ 0 ] = output;
        setData( DATA_OUTPUT, d2 );
    }

}
