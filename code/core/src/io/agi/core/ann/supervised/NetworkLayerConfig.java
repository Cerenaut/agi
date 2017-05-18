/*
 * Copyright (c) 2016.
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

package io.agi.core.ann.supervised;

import io.agi.core.ann.NetworkConfig;
import io.agi.core.orm.ObjectMap;

import java.util.Random;

/**
 * Created by dave on 3/01/16.
 */
public class NetworkLayerConfig extends NetworkConfig {

//    public ObjectMap _om;

    public static final String ACTIVATION_FUNCTION = "activation-function";
    public static final String LEARNING_RATE = "learning-rate";
    public static final String REGULARIZATION = "regularization";
    public static final String INPUTS = "i";
    public static final String CELLS = "w";
    public static final String BATCH_SIZE = "b";

    public NetworkLayerConfig() {
    }

    public void setup( ObjectMap om, String name, Random r, int inputs, int cells, int batchSize, float learningRate, float regularization, String activationFunction ) {
        super.setup( om, name, r );

        setInputs( inputs );
        setCells( cells );
        setBatchSize( batchSize );
        setLearningRate( learningRate );
        setRegularization( regularization );
        setActivationFunction( activationFunction );
    }

    public void copyFrom( NetworkConfig nc, String name ) {
        super.copyFrom( nc, name );

        NetworkLayerConfig c = ( NetworkLayerConfig ) nc;

        setInputs( c.getInputs() );
        setCells( c.getCells() );
        setBatchSize( c.getBatchSize() );
        setLearningRate( c.getLearningRate() );
        setRegularization( c.getRegularization() );
        setActivationFunction( c.getActivationFunction() );
    }

    public void setRegularization( float r ) {
        _om.put( getKey( REGULARIZATION ), r );
    }

    public Float getRegularization() {
        return ( Float ) _om.get( getKey( REGULARIZATION ) );
    }

    public int getInputs() {
        Integer i = _om.getInteger( getKey( INPUTS ) );
        return i.intValue();
    }

    public void setInputs( int inputs ) {
        _om.put( getKey( INPUTS ), inputs );
    }

    public void setCells( int cells ) {
        _om.put( getKey( CELLS ), cells );
    }

    public int getBatchSize() {
        Integer b = _om.getInteger( getKey( BATCH_SIZE ) );
        return b.intValue();
    }

    public void setBatchSize( int b ) {
        _om.put( getKey( BATCH_SIZE ), b );
    }

    public int getCells() {
        Integer w = _om.getInteger( getKey( CELLS ) );
        return w.intValue();
    }

    public void setLearningRate( float learningRate ) {
        _om.put( getKey( LEARNING_RATE ), learningRate );
    }

    public float getLearningRate() {
        Float r = _om.getFloat( getKey( LEARNING_RATE ) );
        return r.floatValue();
    }

    public void setActivationFunction( String costFunction ) {
        _om.put( getKey( ACTIVATION_FUNCTION ), costFunction );
    }

    public String getActivationFunction() {
        return ( String ) _om.get( getKey( ACTIVATION_FUNCTION ) );
    }
}
