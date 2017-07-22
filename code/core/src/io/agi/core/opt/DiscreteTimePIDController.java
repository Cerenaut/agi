/*
 * Copyright (c) 2017.
 *
 * This file is part of Project AGI. <http://agi.io>
 *
 * Project AGI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Project AGI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Project AGI.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.agi.core.opt;

import io.agi.core.data.Data;
import io.agi.core.data.DataSize;
import io.agi.core.data.FloatArray;

/**
 * Derived from: http://controlsystemslab.com/discrete-time-pid-controller-implementation/
 *
 * Although a vector of variables can be controlled, they are all independent. Controls variables to approach a target
 * value by controlling an output.
 *
 * u = Control outputs
 * e = Observed errors in the dependent variables
 *
 * Created by dave on 16/07/17.
 */
public class DiscreteTimePIDController {

    public float _ku1; // Calculated during setup
    public float _ku2; // Calculated during setup
    public float _ke0; // Calculated during setup
    public float _ke1; // Calculated during setup
    public float _ke2; // Calculated during setup
    public float _e2; // Error at t-2
    public float _e1; // Error at t-1
    public float _e0; // Error at t-0
    public float _u2; // Output at t-2
    public float _u1; // Output at t-1
    public float _u0; // Output at t-0
    public float _uMin; // Limits to be applied to output control
    public float _uMax; // Limits to be applied to output control
    public float _input; // A vector of observations that should be optimized
    public float _target; // The ideal value for each observation, ie. target or set value

    /**
     * Resets the internal state of the controller
     *
     */
    public void reset( float input, float target ) {
        _e2 = 0f;
        _e1 = 0f;
        _e0 = 0f;
        _u2 = 0f;
        _u1 = 0f;
        _u0 = 0f;

        _input = input;
        _target = target;
    }

    /**
     * Sets up the controller with given parameters
     * 
     * @param Kp Proportional weights (P)
     * @param Ki Integral weights (I)
     * @param Kd Derivative weights (D)
     * @param N filterCoefficients 
     * @param Ts samplingTime
     */
    public void setup(
            float Kp, // proportional
            float Ki, // integral term
            float Kd, // derivative term
            float N, // filter coefficients
            float Ts,
            float uMin,
            float uMax ) { // Sampling time

        _uMin = uMin;
        _uMax = uMax;

        // a0 = (1+N*Ts);
        float a0 = 1f + N * Ts;

        // a1 = -( 2 + N * Ts );
        float a1 = -( 2f + N * Ts );

        // a2 = 1;
        float a2 = 1f;

        // b0 = Kp * ( 1 + N * Ts ) + Ki * Ts * ( 1 + N * Ts ) + Kd * N;
        float b0 = Kp * ( 1 + N * Ts )
                 + Ki * Ts * ( 1 + N * Ts )
                 + Kd * N;

        // b1 = -( Kp * ( 2 + N * Ts ) + Ki * Ts + 2 * Kd * N );
        float b1 = -( Kp * ( 2 + N * Ts )
                + Ki * Ts
                + 2 * Kd * N );

        // b2 = Kp + Kd * N;
        float b2 = Kp + Kd * N;

        // ku1 = a1/a0;
        float ku1 = a1 / a0;
        _ku1 = ku1;

        // ku2 = a2/a0;
        float ku2 = a2 / a0;
        _ku2 = ku2;

        // ke0 = b0/a0;
        float ke0 = b0 / a0;
        _ke0 = ke0;

        // ke1 = b1/a0;
        float ke1 = b1 / a0;
        _ke1 = ke1;

        // ke2 = b2/a0;
        float ke2 = b2 / a0;
        _ke2 = ke2;
    }

    /**
     * Update controller output with latest input.
     * @param input New observation
     * @param target Ideal value, if changed
     */
    public void update(
            float input,
            float target ) {

        // update variables
        _e2 = _e1;
        _e1 = _e0;
        _u2 = _u1;
        _u1 = _u0;

        // calculate new step
        //y = //read();  // read plant output
        _input = input;
        _target = target;

        //        e0 = r–y;  // compute new error
        float errorValue = target - input;
        _e0 = errorValue;

        //u0 = -ku1 * u1–ku2 * u2 + ke0 * e0 + ke1 * e1 + ke2 * e2; // eq (12)
        float output = - _ku1 * _u1
                       - _ku2 * _u2
                       + _ke0 * _e0
                       + _ke1 * _e1
                       + _ke2 * _e2; // eq (12)
        _u0 = output;

        //if (u0 > UMAX) u0 = UMAX;  // limit to DAC or PWM range
        //if (U0 < UMIN) u0 = UMIN;
        _u0 = Math.max( _uMin, _u0 );
        _u0 = Math.min( _uMax, _u0 );

        //write(u0);   // sent to output
    }

    public void setInput( float input ) {
        _input = input;
    }
    public float getInput() {
        return _input;
    }

    public float getTarget() {
        return _target;
    }

    public float getOutput() {
        return _u0;
    }

    public void setOutput( float output ) {
        _u0 = output;
    }

    public float getOutputMax() {
        return _uMax;
    }

    public float getOutputMin() {
        return _uMin;
    }

    public static void main( String[] args ) {
        test();
    }

    /**
     * This is a pretty hacky test; basically play with the parameters on this trivial square wave matching problem.
     * It can be solved pretty well with either integral or derivative (both +proportional) but I didn't spend time
     * finding a very good combined (PID) solution.
     */
    public static void test() {
        float Kp = 0.01f; // proportional
        float Ki = 0.0005f; // integral term
//        float Ki = 0.f; // integral term
        float Kd = 0.001f; // derivative term
        float N = 100f; // filter coefficients
        float Ts  = 0.01f; // Sampling time

        DiscreteTimePIDController pid = new DiscreteTimePIDController();
        pid.setup( Kp, Ki, Kd, N, Ts, -1f, 1f );

        float target = 0;
        float value = 1f;

        int epoch = 0;
        int epochs = 10;
        int samplesPerEpoch = 1000;

        while( epoch < epochs ) {

            target = 1f - target; // 1-1=0, 1-0=1

            for( int s = 0; s < samplesPerEpoch; ++s ) {

                pid.update( value, target );

                System.out.println( s + ", " + target + ", " + value + ", " + pid._e0 );

                value += pid._u0;
            }

            ++epoch;
        }

    }

    /**
     * Serializes a controller as a FloatArray object.
     * @param c
     * @return
     */
    public static FloatArray Controller2Data( DiscreteTimePIDController c ) {
        FloatArray d = new FloatArray( 15 );
        d._values[ 0 ] = c._ku1;
        d._values[ 1 ] = c._ku2;
        d._values[ 2 ] = c._ke0;
        d._values[ 3 ] = c._ke1;
        d._values[ 4 ] = c._ke2;
        d._values[ 5 ] = c._e2;
        d._values[ 6 ] = c._e1;
        d._values[ 7 ] = c._e0;
        d._values[ 8 ] = c._u2;
        d._values[ 9 ] = c._u1;
        d._values[ 10 ] = c._u0;
        d._values[ 11 ] = c._uMin;
        d._values[ 12 ] = c._uMax;
        d._values[ 13 ] = c._input;
        d._values[ 14 ] = c._target;
        return d;
    }

    /**
     * Deserializes a controller from a FloatArray.
     * @param d
     * @return
     */
    public static DiscreteTimePIDController Data2Controller( FloatArray d ) {
        assert( d.getSize() == 15 );

        DiscreteTimePIDController c = new DiscreteTimePIDController();

        c._ku1 = d._values[ 0 ];
        c._ku2 = d._values[ 1 ];
        c._ke0 = d._values[ 2 ];
        c._ke1 = d._values[ 3 ];
        c._ke2 = d._values[ 4 ];
        c._e2 = d._values[ 5 ];
        c._e1 = d._values[ 6 ];
        c._e0 = d._values[ 7 ];
        c._u2 = d._values[ 8 ];
        c._u1 = d._values[ 9 ];
        c._u0 = d._values[ 10 ];
        c._uMin = d._values[ 11 ];
        c._uMax = d._values[ 12 ];
        c._input = d._values[ 13 ];
        c._target = d._values[ 14 ];

        return c;
    }

}
