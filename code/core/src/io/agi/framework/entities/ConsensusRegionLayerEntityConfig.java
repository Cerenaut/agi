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
public class ConsensusRegionLayerEntityConfig extends EntityConfig {

    float contextFreeLearningRate = 0.1f;
    int contextFreeWidthCells = 15;
    int contextFreeHeightCells = 15;
    boolean contextFreeBinaryOutput = true;
    int contextFreeSparsity = 0; // current value, computed
    float contextFreeSparsityOutput = 0;
    int contextFreeSparsityMin = 0;
    int contextFreeSparsityMax = 0;
    int contextFreeAgeMin = 0;
    int contextFreeAgeMax = 0;
    int contextFreeAge = 0; // current value, computed
    float contextFreeAgeScale = 17f; // current value, computed

    float outputSparsity = 0;

    float defaultPredictionInhibition = 0.f; // don't inhibit

    float predictorLearningRate = 100.0f;

    float consensusLearningRate = 0.01f;
    float consensusDecayRate = 0.99f;
    float consensusStrength = 0.1f; // 10% influence
    int consensusSteps = 20;

}
