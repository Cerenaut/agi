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

/**
 *
 * Created by dave on 7/07/16.
 */
public class LifetimeSparseAutoencoderEntityConfig extends EntityConfig {

    float learningRate = 0f;
    float momentum = 0f;
    int widthCells = 0;
    int heightCells = 0;
    int sparsity = 0; // current value, computed
    int sparsityLifetime = 0; // current value, computed
    float weightsStdDev = 0f; // used at reset

    int batchCount = 0;
    int batchSize = 0;

    public static void Set(
            String entityName,
            boolean cache,
            int widthCells,
            int heightCells,
            int sparsity,
            int sparsityLifetime,
            int batchSize,
            float learningRate,
            float momentum,
            float weightsStdDev ) {

        LifetimeSparseAutoencoderEntityConfig config = new LifetimeSparseAutoencoderEntityConfig();

        config.cache = cache;
        config.widthCells = widthCells;
        config.heightCells = heightCells;
        config.sparsity = sparsity;
        config.sparsityLifetime = sparsityLifetime;
        config.batchSize = batchSize;
        config.learningRate = learningRate;
        config.momentum = momentum;
        config.weightsStdDev = weightsStdDev;

        Framework.SetConfig( entityName, config );
    }

}
