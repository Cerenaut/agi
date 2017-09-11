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

package io.agi.core.alg;

import io.agi.core.ann.NetworkConfig;
import io.agi.core.ann.unsupervised.OnlineKSparseAutoencoderConfig;
import io.agi.core.orm.ObjectMap;

import java.awt.*;
import java.util.Random;

/**
 * Created by dave on 4/07/16.
 */
public class PredictiveCodingConfig extends NetworkConfig {

    public static final String INPUT_C_WIDTH = "input-c-width";
    public static final String INPUT_C_HEIGHT = "input-c-height";
    public static final String INPUT_P_SIZE = "input-p-size";

    public static final String OUTPUT_SPIKE_AGE_MAX = "output-spike-age-max";
    public static final String OUTPUT_DECAY_RATE = "output-decay-rate";

//    public static final String SUFFIX_CLASSIFIER = "classifier";
//    public static final String SUFFIX_PREDICTOR = "predictor";

    public PredictiveCodingConfig() {
    }

    public void setup(
            ObjectMap om,
            String name,
            Random r,
            int inputCWidth,
            int inputCHeight,
//            int inputPSize,
            int outputSpikeAgeMax,
            float outputDecayRate ) {

        super.setup( om, name, r );

        setInputCSize( inputCWidth, inputCHeight );
//        setInputPSize( inputPSize );

        setOutputSpikeAgeMax( outputSpikeAgeMax );
        setOutputDecayRate( outputDecayRate );
    }

    public Random getRandom() {
        return _r;
    }

    public float getOutputDecayRate() {
        float r = _om.getFloat( getKey( OUTPUT_DECAY_RATE ) );
        return r;
    }

    public void setOutputDecayRate( float r ) {
        _om.put( getKey( OUTPUT_DECAY_RATE ), r );
    }

    public int getOutputSpikeAgeMax() {
        int r = _om.getInteger( getKey( OUTPUT_SPIKE_AGE_MAX ) );
        return r;
    }

    public void setOutputSpikeAgeMax( int r ) {
        _om.put( getKey( OUTPUT_SPIKE_AGE_MAX ), r );
    }

    public int getInputCArea() {
        Point p = getInputCSize();
        int area = p.x * p.y;
        return area;
    }

    public Point getInputCSize() {
        int inputWidth = _om.getInteger( getKey( INPUT_C_WIDTH ) );
        int inputHeight = _om.getInteger( getKey( INPUT_C_HEIGHT ) );
        return new Point( inputWidth, inputHeight );
    }

    public void setInputCSize( int ffInputWidth, int ffInputHeight ) {
        _om.put( getKey( INPUT_C_WIDTH ), ffInputWidth );
        _om.put( getKey( INPUT_C_HEIGHT ), ffInputHeight );
    }

//    public int getInputPSize() {
//        int size = _om.getInteger( getKey( INPUT_P_SIZE ) );
//        return size;
//    }
//
//    public void setInputPSize( int size ) {
//        _om.put( getKey( INPUT_P_SIZE ), size );
//    }

}
