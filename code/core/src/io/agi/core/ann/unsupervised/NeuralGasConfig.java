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

package io.agi.core.ann.unsupervised;

import io.agi.core.ann.NetworkConfig;
import io.agi.core.orm.ObjectMap;

import java.util.Random;

/**
 * Note, stress threshold is compared to recent-moving-average (exponentially weighted) of sum of input error.
 * Created by dave on 1/01/16.
 */
public class NeuralGasConfig extends CompetitiveLearningConfig {

    public static final String LEARNING_RATE = "learning-rate";
    public static final String NOISE_MAGNITUDE = "noise-magnitude";
    public static final String NEIGHBOURHOOD_RANGE = "neighbourhood-range";
    public static final String MIN_DISTANCE = "min-distance";
    public static final String MAX_AGE = "max-age";

    public NeuralGasConfig() {
    }

    public void setup(
            ObjectMap om,
            String name,
            Random r,
            int inputs,
            int w,
            int h,
            float learningRate,
            float noiseMagnitude,
            float neighbourhoodRange,
            float minDistance,
            int maxAge ){

        super.setup( om, name, r, inputs, w, h );

        setLearningRate( learningRate );
        setNoiseMagnitude( noiseMagnitude );
        setNeighbourhoodRange( neighbourhoodRange );
        setMinDistance( minDistance );
        setMaxAge( maxAge );
    }

    public void copyFrom( NetworkConfig nc, String name ) {
        super.copyFrom( nc, name );

        NeuralGasConfig c = ( NeuralGasConfig ) nc;

        setLearningRate( c.getLearningRate() );
        setNoiseMagnitude( c.getNoiseMagnitude() );
        setNeighbourhoodRange( c.getNeighbourhoodRange() );
        setMinDistance( c.getMinDistance() );
        setMaxAge( c.getMaxAge() );
    }

    public void setLearningRate( float r ) {
        _om.put( getKey( LEARNING_RATE ), r );
    }

    public void setNoiseMagnitude( float r ) {
        _om.put( getKey( NOISE_MAGNITUDE ), r );
    }

    public void setNeighbourhoodRange( float r ) {
        _om.put( getKey( NEIGHBOURHOOD_RANGE ), r );
    }

    public void setMinDistance( float r ) {
        _om.put( getKey( MIN_DISTANCE ), r );
    }

    public void setMaxAge( int n ) {
        _om.put( getKey( MAX_AGE ), n );
    }

    public int getMaxAge() {
        Integer n = _om.getInteger( getKey( MAX_AGE ) );
        return n.intValue();
    }

    public float getLearningRate() {
        Float r = _om.getFloat( getKey( LEARNING_RATE ) );
        return r.floatValue();
    }

    public float getNoiseMagnitude() {
        Float r = _om.getFloat( getKey( NOISE_MAGNITUDE ) );
        return r.floatValue();
    }

    public float getNeighbourhoodRange() {
        Float r = _om.getFloat( getKey( NEIGHBOURHOOD_RANGE ) );
        return r.floatValue();
    }

    public float getMinDistance() {
        Float r = _om.getFloat( getKey( MIN_DISTANCE ) );
        return r.floatValue();
    }
}
