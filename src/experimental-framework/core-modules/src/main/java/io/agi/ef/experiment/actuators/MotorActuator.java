package io.agi.ef.experiment.actuators;

import io.agi.core.data.Data;

/**
 * Simple motor. The input equals the output with a gain factor.
 *
 * Created by gideon on 1/08/15.
 */
public class MotorActuator extends Actuator {

    private float _input = 0;
    private float _output = 0;
    private float _gain = 1.0f;
    private Data _outData = new Data( 1 );

    public MotorActuator( float gain ) {
        super( 1, 1 );
        _gain = gain;
    }

    public void setInput( float in ) {
        Data d = new Data( 1 );
        d.set( in );
        setInput( d );
    }

    @Override
    public void setInput( Data input ) {
        _input = input.max();   // todo the only way i know how to get the single dimension value!
    }

    @Override
    public void step() {
        _output = _input * _gain;
        _outData.set( _output );
    }

    @Override
    public Data getOutput() {
        return _outData;
    }

    public float getPower() {
        return _output;
    }
}
