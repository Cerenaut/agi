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

package io.agi.framework.demo.sequence;

import io.agi.framework.EntityConfig;

/**
 * Created by dave on 7/11/17.
 */
public class RecurrentResidualEntityConfig extends EntityConfig {

    // Problem
    public boolean print = false;
    public int problemSequence = 0;
    public int problemEpoch = 0;

    // Statistics
    public int errorBatch = 100;
    public float errorSum = 0f;
    public float errorCount = 0f;
    public float errorMean = 0f;

    // Parameters
    public int memorySize = 20;
    public int batchCount = 0;
    public int batchSize = 1;
    public float learningRate = 0.01f;
    public float weightsStdDev = 0.01f;


}
