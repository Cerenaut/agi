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
 * Created by dave on 23/10/16.
 */
public class HqClRegionLayerEntityConfig extends EntityConfig {

    int columnWidthCells = 0;
    int columnHeightCells = 0;

    int regionWidthColumns = 0;
    int regionHeightColumns = 0;

    int intervalsX1 = 0;
    int intervalsY1 = 0;
    int intervalsX2 = 0;
    int intervalsY2 = 0;

    float predictionLearningRate = 0;
    float predictionDecayRate = 0;
    int errorHistoryLength = 0;
    int classifiersPerBit1 = 0;
    int classifiersPerBit2 = 0;

    float classifierLearningRate = 0;
    float classifierLearningRateNeighbours = 0;
    float classifierNoiseMagnitude = 0;
    int classifierEdgeMaxAge = 0;
    float classifierStressLearningRate = 0;
    float classifierStressSplitLearningRate = 0;
    float classifierStressThreshold = 0;
    int classifierGrowthInterval = 0;

}
