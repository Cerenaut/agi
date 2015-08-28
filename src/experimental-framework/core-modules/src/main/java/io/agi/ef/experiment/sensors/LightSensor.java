package io.agi.ef.experiment.sensors;


import io.agi.core.data.Data;
import io.agi.ef.experiment.actuators.LightActuator;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by gideon on 1/08/15.
 */
public class LightSensor extends Sensor {

    private static final Logger _logger = Logger.getLogger( LightSensor.class.getName() );
    private Data _outData = null;
    private float _gain = 1.0f;
    private float _brightness;

    public LightSensor( float gain ) {
        super( 1, 1 );

        _gain = gain;
        _outData = new Data( 1 );
    }

    @Override
    public void update( ) {

        /*
            get state for relevant key in the world
         */

        String val = "5";
        Float fVal = Float.valueOf( val );
        _brightness = fVal * _gain;
        _outData.set( _brightness );
    }

    @Override
    public Data getOutput() {
        return _outData;
    }

    public float getBrightness() {
        return _brightness;
    }
}
