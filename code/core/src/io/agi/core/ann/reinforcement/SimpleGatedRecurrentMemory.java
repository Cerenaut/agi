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

package io.agi.core.ann.reinforcement;

import io.agi.core.data.Data;

import java.util.Collection;
import java.util.HashSet;

/**
 * TODO: Exploit structure in data to compress stored form.
 *
 * Created by dave on 19/05/17.
 */
public class SimpleGatedRecurrentMemory {

    public Data _input;
    public Data _stored;
    public Data _output;

//    public Data _gateRead;
    public Data _gates;

    public float _threshold = 0.5f;

    public SimpleGatedRecurrentMemory() {

    }

    public void setup( int size ) {
        _input = new Data( size );
        _stored = new Data( size );
        _output = new Data( size );

//        _gateRead = new Data( size );
        _gates = new Data( 2 ); // write, and clear
    }

    public int getNbrGates() {
        return 2;
    }

    public Data getInput() {
        return _input;
    }

    public void setInput( Data input ) {
        _input.copy( input );
    }
    public Data getStored() {
        return _stored;
    }

    public Data getOutput() {
        return _output;
    }

    public Data getGates() {
        Data gates = new Data( _gates );
        return gates;
    }

    public void setWriteGate( float gateValue ) {
        _gates._values[ 0 ] = gateValue;
    }

    public void setClearGate( float gateValue ) {
        _gates._values[ 1 ] = gateValue;
    }

    public void reset() {
        _stored.set( 0 );
        _gates.set( 0 );
    }

    public void update() {

        // erase as per gates (forget)
        // since we dont have flexible addressing we need the ability to forget explicitly
        float writeValue = _gates._values[ 0 ];
        float clearValue = _gates._values[ 1 ];

        if( clearValue >= _threshold ) {
            _stored.set( 0f );
        }

        // selectively overwrite
        if( writeValue >= _threshold ) {
            _stored.copy( _input );
        }

        _output.copy( _stored );
    }
}
