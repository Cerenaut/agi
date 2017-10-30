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
import io.agi.framework.Framework;
import io.agi.framework.persistence.PersistenceUtil;

/**
 * Created by dave on 16/10/17.
 */
public class BatchSparseNetworkEntityConfig extends EntityConfig {

    float learningRate = 0f;
    float momentum = 0f;
    int widthCells = 0;
    int heightCells = 0;
    int outputs = 0;
    int sparsity = 0;
    int sparsityLifetime = 0;
    int sparsityOutput = 0;
    float weightsStdDev = 0f; // used at reset

    int batchCount = 0;
    int batchSize = 0;

    public static void Set(
            String entityName,
            boolean cache,
            int widthCells,
            int heightCells,
            int outputs,
            int sparsity,
            int sparsityLifetime,
            int sparsityOutput,
            int batchSize,
            float learningRate,
            float momentum,
            float weightsStdDev ) {

        BatchSparseNetworkEntityConfig config = new BatchSparseNetworkEntityConfig();

        config.cache = cache;
        config.widthCells = widthCells;
        config.heightCells = heightCells;
        config.outputs = outputs;
        config.sparsity = sparsity;
        config.sparsityLifetime = sparsityLifetime;
        config.sparsityOutput = sparsityOutput;
        config.batchSize = batchSize;
        config.learningRate = learningRate;
        config.momentum = momentum;
        config.weightsStdDev = weightsStdDev;

        PersistenceUtil.SetConfig( entityName, config );
    }

}

