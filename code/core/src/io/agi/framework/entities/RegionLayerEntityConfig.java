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
public class RegionLayerEntityConfig extends EntityConfig {

    int organizerWidthCells = 6;
    int organizerHeightCells = 4;

    int organizerIntervalsInput1X = 3; // 3 * 2 = 6
    int organizerIntervalsInput2X = 2; // 3 * 2 = 6   by 4 * 1 = 4
    int organizerIntervalsInput1Y = 4;
    int organizerIntervalsInput2Y = 1;

    // Column Sizing
    int classifierWidthCells = 6;
    int classifierHeightCells = 3;
    int classifierDepthCells = 2;

    // Organizer training
    boolean organizerTrainOnChange = false;
    boolean emitUnchangedCells = true; // default, assists temporal pooling by remembering the state of other cols
    float receptiveFieldsTrainingSamples = 0.1f;//12;
    float defaultPredictionInhibition = 0.f; // don't inhibit
    int classifiersPerBit1 = 5;
    int classifiersPerBit2 = 5;

//    float organizerNeighbourhoodRange = 2.f;//5.f;//10.f; too volatile @ 10
/*    float organizerLearningRate = 0.02f;
    float organizerElasticity = 1.f;
/*    float organizerLearningRateNeighbours = 0.01f;
    float organizerNoiseMagnitude = 0.0f;
    int organizerEdgeMaxAge = 200;
    float organizerStressLearningRate = 0.01f;
    float organizerStressThreshold = 0.1f;
    int organizerGrowthInterval = 100;*/

    // Classifier training
    float classifierLearningRate = 0.02f;
    float classifierLearningRateNeighbours = 0.01f;
    float classifierNoiseMagnitude = 0.0f;
    int classifierEdgeMaxAge = 200;
    float classifierStressLearningRate = 0.0005f;
    float classifierStressSplitLearningRate = 0.5f;
    float classifierStressThreshold = 0.1f;
    int classifierGrowthInterval = 100;
//
//    float classifierNeighbouhoodRange = 2.f;//5.f;//10.f; too volatile @ 10
//    float classifierMinDistance = 0.01f;//5.f;//10.f; too volatile @ 10

//    float classifierStressLearningRate = 0.01f;
//    float classifierRankLearningRate = 0.05f;
//    float classifierRankScale = 10.0f;
//    int classifierAgeMax = 500;
//    float classifierAgeDecay = 0.7f;
//    float classifierAgeScale = 12.0f;

    // Predictor
//    float predictorHiddenLayerScaleFactor = 0.1f; // Note, this assumes that the input data can be easily modelled by a few causes
    float predictorLearningRate = 0.01f;//100.0f;
//    float predictorRegularization = 0.0f;

}
