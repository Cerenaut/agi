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
public class GatedRecurrentMemory {

    public Data _input;
    public Data _stored;
    public Data _output;

//    public Data _gateRead;
    public Data _gateWrite;
    public Data _gateClear;

    public float _threshold = 0.5f;

    public GatedRecurrentMemory() {

    }

    public void setup( int size ) {
        _input = new Data( size );
        _stored = new Data( size );
        _output = new Data( size );

//        _gateRead = new Data( size );
        _gateWrite = new Data( size );
        _gateClear = new Data( size );
    }

    public int getNbrGates() {
        int inputSize = _input.getSize();
        int storedSize = _stored.getSize();
//        return _input.getSize() * 3;
        int gates = inputSize + storedSize;
        return gates;
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
        int inputSize = _input.getSize();
        int storedSize = _stored.getSize();
        int gatesSize = inputSize * 2 + storedSize;
        Data gates = new Data( gatesSize );

        int offsetThis = 0;
//        gates.copyRange( _gateRead , offsetThis, 0, inputSize );
//        offsetThis += inputSize;
        gates.copyRange( _gateWrite, offsetThis, 0, inputSize );
        offsetThis += inputSize;
        gates.copyRange( _gateClear, offsetThis, 0, storedSize );

        return gates;
    }

    public void setGates( Data gates ) {
        int inputSize = _input.getSize();
        int storedSize = _stored.getSize();

        int offsetThat = 0;
//        _gateRead .copyRange( gates, 0, offsetThat, inputSize );
//        offsetThat += inputSize;
        _gateWrite.copyRange( gates, 0, offsetThat, inputSize );
        offsetThat += inputSize;
        _gateClear.copyRange( gates, 0, offsetThat, storedSize );
    }

//    public void setGateRead( Collection< Integer > bits ) {
//        setGates( _gateRead, bits );
//    }

    public void setGateWrite( Collection< Integer > bits ) {
        setGates( _gateWrite, bits );
    }

    public void setGateClear( Collection< Integer > bits ) {
        setGates( _gateClear, bits );
    }

    public void setGates( Data gates, Collection< Integer > bits ) {
        gates.set( 0f );
        for( Integer i : bits ) {
            gates._values[ i ] = 1;
        }
    }

    public void reset() {
        _stored.set( 0 );
//        _gateRead.set( 0 );
        _gateWrite.set( 0 );
        _gateClear.set( 0 );
    }

    public void update() {

        // erase as per gates (forget)
        // since we dont have flexible addressing we need the ability to forget explicitly
        HashSet< Integer > clearBits = _gateClear.indicesMoreThan( _threshold );
        for( Integer i : clearBits ) {
            _stored._values[ i ] = 0;
        }

        // selectively overwrite
        HashSet< Integer > writeBits = _gateWrite.indicesMoreThan( _threshold );
        for( Integer i : writeBits ) {
            _stored._values[ i ] = _input._values[ i ];
        }

        // TODO: Compress stored here. Only recurrently store compressed form.
        // Decompress when reading.
        // Are gates compressed? Yes, the gates would be compressed size. We compress and expand the data only.

        // selectively read
//        HashSet< Integer > readBits = _gateRead.indicesMoreThan( _threshold );
        _output.set( 0 );
//        for( Integer i : readBits ) {
        // ungated reads
        int inputSize = _input.getSize();
        for( int i = 0; i < inputSize; ++i ) {
            _output._values[ i ] = _stored._values[ i ];
        }

    }
}
