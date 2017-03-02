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

package io.agi.framework.entities;

import io.agi.framework.EntityConfig;

/**
 *
 * Created by dave on 7/07/16.
 */
public class PyramidRegionLayerEntityConfig extends EntityConfig {

    // parameters you can adjust
    int widthCells = 20;
    int heightCells = 20;
    float classifierLearningRate = 0.01f;
    float classifierMomentum = 0;
    float classifierSparsityOutput = 1.5f; // a factor determining the output sparsity
    int classifierSparsity = 15; // k, the number of active cells each step
    int classifierAgeMin = 500; // age of disuse where we start to promote cells
    int classifierAgeMax = 1000; // age of disuse where we maximally promote cells
    float classifierAgeTruncationFactor = 0.5f;
    float classifierAgeScale = 17f; // promotion nonlinearity
    float classifierRateScale = 5f; // inhibition nonlinearity
    float classifierRateMax = 0.25f; // i.e. never more than 1 in 4.
    float classifierRateLearningRate = 0.001f; // how fast the measurement of rate of cell use changes.

    float classifierWeightsStdDev = 0f;
    int classifierBatchCount = 0;
    int classifierBatchSize = 0;

    float predictorLearningRate = 0.01f; // how fast the prediction weights learn
    int predictorHiddenCells = 0;
    float predictorLeakiness = 0.0f;
    float predictorRegularization = 0.0f;
    int predictorBatchSize = 0;

    float outputDecayRate = 0.0f;

//    float outputCodingSparsityFactor = 2.0f; // density of output including history

    // stats calculated during operation
    boolean resetDelayed = false;

    float sumClassifierError = 0;
    float sumClassifierResponse = 0;
    float sumOutputSpikes = 0;
    float sumPredictionErrorFP = 0;
    float sumPredictionErrorFN = 0;
//    float sumIntegration = 0;

}
