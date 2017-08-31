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
import io.agi.core.orm.ObjectMap;

import java.awt.*;
import java.util.Random;

/**
 * Created by dave on 25/03/17.
 */
public class FeedForwardNetworkQuiltPredictorConfig extends QuiltPredictorConfig {

    public static final String PREDICTOR_LEARNING_RATE = "predictor-learning-rate";
    public static final String PREDICTOR_HIDDEN_CELLS = "predictor-hidden-cells";
    public static final String PREDICTOR_LEAKINESS = "predictor-leakiness";
    public static final String PREDICTOR_REGULARIZATION = "predictor-regularization";
    public static final String PREDICTOR_BATCH_SIZE = "predictor-batch-size";

    public FeedForwardNetworkQuiltPredictorConfig() {
    }

    public void setup(
            ObjectMap om,
            String name,
            Random r,
            int inputCWidth,
            int inputCHeight,
            int inputCColumnWidth,
            int inputCColumnHeight,
            int inputPSize,
            float predictorLearningRate,
            int predictorHiddenCells,
            float predictorLeakiness,
            float predictorRegularization,
            int predictorBatchSize ) {

        super.setup( om, name, r, inputCWidth, inputCHeight, inputCColumnWidth, inputCColumnHeight, inputPSize );

        setPredictorLearningRate(predictorLearningRate);
        setPredictorHiddenCells(predictorHiddenCells);
        setPredictorLeakiness(predictorLeakiness);
        setPredictorRegularization(predictorRegularization);
        setPredictorBatchSize(predictorBatchSize);
    }

    public float getPredictorLearningRate() {
        float r = _om.getFloat( getKey( PREDICTOR_LEARNING_RATE ) );
        return r;
    }

    public int getPredictorHiddenCells() {
        int r = _om.getInteger(getKey(PREDICTOR_HIDDEN_CELLS));
        return r;
    }

    public float getPredictorLeakiness() {
        float r = _om.getFloat(getKey(PREDICTOR_LEAKINESS));
        return r;
    }

    public float getPredictorRegularization() {
        float r = _om.getFloat( getKey( PREDICTOR_REGULARIZATION ) );
        return r;
    }

    public int getPredictorBatchSize() {
        int r = _om.getInteger(getKey(PREDICTOR_BATCH_SIZE));
        return r;
    }

    public void setPredictorLearningRate( float r ) {
        _om.put( getKey( PREDICTOR_LEARNING_RATE ), r );
    }

    public void setPredictorHiddenCells( int r ) {
        _om.put( getKey( PREDICTOR_HIDDEN_CELLS ), r );
    }
    public void setPredictorLeakiness( float r ) {
        _om.put( getKey( PREDICTOR_LEAKINESS ), r );
    }
    public void setPredictorRegularization( float r ) {
        _om.put( getKey( PREDICTOR_REGULARIZATION ), r );
    }

    public void setPredictorBatchSize( int r ) {
        _om.put( getKey( PREDICTOR_BATCH_SIZE ), r );
    }

}
