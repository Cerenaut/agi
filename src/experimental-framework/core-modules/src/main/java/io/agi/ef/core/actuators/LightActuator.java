package io.agi.ef.core.actuators;

import io.agi.core.data.Data;

/**
 * Triangle wave of light brightness
 * Created by gideon on 1/08/15.
 */
public class LightActuator extends Actuator {

    private int _brightness = 0;
    private int _state = 0;     // 0 = increasing brightness, 1 = decreasing brightness
    private Data _outData = null;
    private int _min = 0;
    private int _max = 0;

    public LightActuator( int min, int max ) {
        super( 0, 1 );

        _min = min;
        _max = max;

        _outData = new Data( 1 );
    }

    @Override
    public void setInput( Data input ) {
        // does not depend on input
    }

    @Override
    public void step() {
        if ( _state == 0 ) {
            _brightness++;

            if ( _brightness >= _max ) {
                _state = 1;
            }
        }
        else {
            _brightness--;

            if ( _brightness <= _min ) {
                _state = 0;
            }
        }

        _outData.set( _brightness );
    }

    public float getBrightness() {
        return _brightness;
    }

    @Override
    public Data getOutput() {
        return _outData;
    }
}
