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

import io.agi.core.orm.ObjectMap;

import java.util.Random;

/**
 * Created by dave on 25/03/17.
 */
public class HebbianQuiltPredictorConfig extends QuiltPredictorConfig {

    public static final String PREDICTOR_LEARNING_RATE = "predictor-learning-rate";

    public HebbianQuiltPredictorConfig() {
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
            float predictorLearningRate ) {

        super.setup( om, name, r, inputCWidth, inputCHeight, inputCColumnWidth, inputCColumnHeight, inputPSize );

        setPredictorLearningRate( predictorLearningRate );
    }

    public float getPredictorLearningRate() {
        float r = _om.getFloat( getKey( PREDICTOR_LEARNING_RATE ) );
        return r;
    }

   public void setPredictorLearningRate( float r ) {
        _om.put( getKey( PREDICTOR_LEARNING_RATE ), r );
    }

}