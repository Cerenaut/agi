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

package io.agi.framework.entities;

import io.agi.framework.EntityConfig;

/**
 *
 * Created by dave on 7/07/16.
 */
public class CompetitiveKSparseAutoencoderEntityConfig extends EntityConfig {

    float learningRate = 0f;
    float momentum = 0f;
    float weightsStdDev = 0f; // used at reset

    int widthCells = 0;
    int heightCells = 0;

    int sparsity = 0; // current value, computed
    float sparsityOutput = 0;

        float rateLearningRate = 0f;
        float correlationLearningRate = 0f;

//    boolean unitOutput = false;

    int batchCount = 0;
    int batchSize = 0;
    int bisectionCount = 0;
    int bisectionInterval = 0;


}
