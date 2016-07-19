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
public class AutoRegionLayerEntityConfig extends EntityConfig {

    float contextFreeLearningRate = 0.1f;
    int contextFreeWidthCells = 15;
    int contextFreeHeightCells = 15;
    boolean contextFreeBinaryOutput = true;
    int contextFreeSparsity = 0; // current value, computed
    int contextFreeSparsityOutput = 0;
    int contextFreeSparsityMin = 0;
    int contextFreeSparsityMax = 0;
    int contextFreeAgeMin = 0;
    int contextFreeAgeMax = 0;
    int contextFreeAge = 0; // current value, computed

//    float contextualLearningRate = 0.1f;
//    int contextualWidthCells = 20;
//    int contextualHeightCells = 20;
//    int contextualSparsity = 0; // current value, computed
//    int contextualSparsityOutput = 0;
//    int contextualSparsityMin = 0;
//    int contextualSparsityMax = 0;
//    int contextualAgeMin = 0;
//    int contextualAgeMax = 0;
//    int contextualAge = 0; // current value, computed

    int outputSparsity = 0;

    float defaultPredictionInhibition = 0.f; // don't inhibit

    float predictorLearningRate = 100.0f;

}
