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
public class GrowingNeuralGasEntityConfig extends EntityConfig {

    float learningRate = 0.1f;
    int widthCells = 8;
    int heightCells = 8;

    float learningRateNeighbours = 0.05f;
    float noiseMagnitude = 0.005f;
    int edgeMaxAge = 200;
    float stressLearningRate = 0.01f;
    float stressSplitLearningRate = 0.5f;
    float stressThreshold = 0.01f;
    public float utilityLearningRate = 0;
    public float utilityThreshold = -1f;
    int growthInterval = 2;

}
