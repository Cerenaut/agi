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
public class QuiltedCompetitiveLearningEntityConfig extends EntityConfig {

    public boolean resetDelayed = false;

    public boolean emit2ndBest = false;
    public boolean useSharedWeights = true;

    public int quiltWidth = 0;
    public int quiltHeight = 0;

    public int classifierWidth = 0;
    public int classifierHeight = 0;

    public int field1OffsetX = 0;
    public int field1OffsetY = 0;
    public int field2OffsetX = 0;
    public int field2OffsetY = 0;

    public int field1StrideX = 0;
    public int field1StrideY = 0;
    public int field2StrideX = 0;
    public int field2StrideY = 0;

    public int field1SizeX = 0;
    public int field1SizeY = 0;
    public int field2SizeX = 0;
    public int field2SizeY = 0;

    public float classifierLearningRate = 0;
    public float classifierLearningRateNeighbours = 0;
    public float classifierNoiseMagnitude = 0;
    public int classifierEdgeMaxAge = 0;
    public float classifierStressLearningRate = 0;
    public float classifierStressSplitLearningRate = 0;
    public float classifierStressThreshold = 0;
    public float classifierUtilityLearningRate = 0;
    public float classifierUtilityThreshold = -1f;
    public int classifierGrowthInterval = 0;
    public float classifierDenoisePercentage = 0;
}
