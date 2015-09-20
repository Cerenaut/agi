package io.agi.ef.demo.helloworld;


import io.agi.core.data.Data;
import io.agi.ef.entities.experiment.Sensor;
import org.codehaus.jettison.json.JSONObject;

import java.util.HashSet;
import java.util.logging.Logger;

/**
 * A sensor controlled by two properties - an input and a gain.
 * Created by gideon on 1/08/15.
 */
public class LightSensor extends Sensor {

    public static final String ENTITY_TYPE = "light-sensor";

//    public static final String PROPERTY_INPUT = "input";
    public static final String PROPERTY_GAIN = "gain";

    public LightSensor() {
    }

    public void configure( String config ) {

//        float input = configureProperty( config, PROPERTY_INPUT, 0.f );
//        addProperty( PROPERTY_INPUT, input );

        float gain = configureProperty( config, PROPERTY_GAIN, 1.f );
        addProperty( PROPERTY_GAIN, gain );

        Data d = new Data( 1 );
        addData(DATA_SENSED, d); // fixed size
    }

    @Override
    public void doStep( HashSet< String > dirtyData ) {
        super.doStep(dirtyData);

        Data d1 = GetData( HelloWorld.LIGHT_SOURCE_NAME, LightSource.DATA_OUTPUT );
        float input = d1._values[ 0 ];
        float gain = getPropertyAsFloat( PROPERTY_GAIN );
        float luma = input * gain;

        Data d = new Data( 1 );
        d._values[ 0 ] = luma;
        setData( DATA_SENSED, d );
    }

}
