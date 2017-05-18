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

package io.agi.core.alg;

import io.agi.core.ann.NetworkConfig;
import io.agi.core.ann.unsupervised.KSparseAutoencoderConfig;
import io.agi.core.ann.unsupervised.ParameterLessSelfOrganizingMapConfig;
import io.agi.core.ann.unsupervised.PlasticNeuralGasConfig;
import io.agi.core.orm.ObjectMap;

import java.awt.*;
import java.util.Random;

/**
 * Created by dave on 4/07/16.
 */
public class AutoRegionLayerConfig extends NetworkConfig {

    public static final String INPUT_1_WIDTH = "input-1-width";
    public static final String INPUT_1_HEIGHT = "input-1-height";
    public static final String INPUT_2_WIDTH = "input-2-width";
    public static final String INPUT_2_HEIGHT = "input-2-height";

    public static final String PREDICTOR_LEARNING_RATE = "predictor-learning-rate";
    public static final String DEFAULT_PREDICTION_INHIBITION = "default-prediction-inhibition";
    public static final String OUTPUT_SPARSITY = "output-sparsity";

    public static final String SUFFIX_CONTEXT_FREE = "context-free";
    public static final String SUFFIX_CONTEXTUAL = "contextual";
    public static final String SUFFIX_PREDICTOR = "predictor";

    public KSparseAutoencoderConfig _contextFreeConfig;
    public KSparseAutoencoderConfig _contextualConfig;

    public AutoRegionLayerConfig() {
    }

    public void setup(
            ObjectMap om,
            String name,
            Random r,
            KSparseAutoencoderConfig contextFreeConfig,
            KSparseAutoencoderConfig contextualConfig,
            int input1Width,
            int input1Height,
            int input2Width,
            int input2Height,
            float outputSparsity,
            float predictorLearningRate,
            float defaultPredictionInhibition ) {
        super.setup( om, name, r );

        _contextFreeConfig = contextFreeConfig;
        _contextualConfig = contextualConfig;

        setOutputSparsity( outputSparsity );
        setPredictorLearningRate( predictorLearningRate );
        setInput1Size( input1Width, input1Height );
        setInput2Size( input2Width, input2Height );
        setDefaultPredictionInhibition( defaultPredictionInhibition );
    }

    public Random getRandom() {
        return _r;
    }

    public float getPredictorLearningRate() {
        float r = _om.getFloat( getKey( PREDICTOR_LEARNING_RATE ) );
        return r;
    }

    public void setPredictorLearningRate( float learningRate ) {
        _om.put( getKey( PREDICTOR_LEARNING_RATE ), learningRate );
    }

    public float getDefaultPredictionInhibition() {
        float r = _om.getFloat( getKey( DEFAULT_PREDICTION_INHIBITION ) );
        return r;
    }

    public void setDefaultPredictionInhibition( float r ) {
        _om.put( getKey( DEFAULT_PREDICTION_INHIBITION ), r );
    }

    public float getOutputSparsity() {
        float r = _om.getFloat( getKey( OUTPUT_SPARSITY ) );
        return r;
    }

    public void setOutputSparsity( float r ) {
        _om.put( getKey( OUTPUT_SPARSITY ), r );
    }

    public int getInputArea() {
        Point input1Size = getInput1Size();
        Point input2Size = getInput2Size();
        int inputArea = input1Size.x * input1Size.y
                      + input2Size.x * input2Size.y;
        return inputArea;
    }

    public Point getInput1Size() {
        int inputWidth = _om.getInteger( getKey( INPUT_1_WIDTH ) );
        int inputHeight = _om.getInteger( getKey( INPUT_1_HEIGHT ) );
        return new Point( inputWidth, inputHeight );
    }

    public Point getInput2Size() {
        int inputWidth = _om.getInteger( getKey( INPUT_2_WIDTH ) );
        int inputHeight = _om.getInteger( getKey( INPUT_2_HEIGHT ) );
        return new Point( inputWidth, inputHeight );
    }

    public void setInput1Size( int ffInputWidth, int ffInputHeight ) {
        _om.put( getKey( INPUT_1_WIDTH ), ffInputWidth );
        _om.put( getKey( INPUT_1_HEIGHT ), ffInputHeight );
    }

    public void setInput2Size( int ffInputWidth, int ffInputHeight ) {
        _om.put( getKey( INPUT_2_WIDTH ), ffInputWidth );
        _om.put( getKey( INPUT_2_HEIGHT ), ffInputHeight );
    }

}
