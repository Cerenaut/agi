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
 * Created by dave on 2/04/16.
 */
public class RegionEntityConfig extends EntityConfig {

    int organizerWidthCells = 10;
    int organizerHeightCells = 10;

    // Column Sizing
    int classifierWidthCells = 6;
    int classifierHeightCells = 6;

    // Organizer training
    float receptiveFieldsTrainingSamples = 0.1f;//12;
    int receptiveFieldSize = 8;
    float organizerLearningRate = 0.02f;
    float organizerLearningRateNeighbours = 0.01f;
    float organizerNoiseMagnitude = 0.0f;
    int organizerEdgeMaxAge = 200;
    float organizerStressLearningRate = 0.01f;
    float organizerStressThreshold = 0.1f;
    int organizerGrowthInterval = 100;

    // Classifier training
    float classifierLearningRate = 0.02f;
    float classifierLearningRateNeighbours = 0.01f;
    float classifierNoiseMagnitude = 0.0f;
    int classifierEdgeMaxAge = 200;
    float classifierStressLearningRate = 0.01f;
    float classifierStressThreshold = 0.1f;
    int classifierGrowthInterval = 100;

    // Predictor
//    float predictorHiddenLayerScaleFactor = 0.1f; // Note, this assumes that the input data can be easily modelled by a few causes
    float predictorLearningRate = 0.1f;
//    float predictorRegularization = 0.0f;

}
