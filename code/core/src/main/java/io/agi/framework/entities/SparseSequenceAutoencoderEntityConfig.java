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
import io.agi.framework.persistence.PersistenceUtil;

/**
 * Created by dave on 10/11/17.
 */
public class SparseSequenceAutoencoderEntityConfig extends EntityConfig {

    // Computed:
    boolean resetDelayed = false;
//    float predictionError = 0f;

    int uniqueRows = 0;

    float compressionErrorF = 0f;
    float compressionErrorB = 0f;

    // Parameters:
    int cellMappingDensity = 0;

    int sparsityTrainingF = 0;
    int sparsityOutputF = 0;
    int sparsityBatchF = 0;

    int sparsityTrainingB = 0;
    int sparsityOutputB = 0;
    int sparsityBatchB = 0;

    float learningRate = 0f;
    float momentum = 0f;
    int widthCellsF = 0;
    int heightCellsF = 0;
    int widthCellsB = 0;
    int heightCellsB = 0;
    float weightsStdDev = 0f; // used at reset

    int batchCountF = 0;
    int batchCountB = 0;
    int batchSizeF = 0;
    int batchSizeB = 0;

    public static void Set(
            String entityName,
            boolean cache,
            int cellMappingDensity,
            int sparsityTrainingF,
            int sparsityOutputF,
            int sparsityBatchF,
            int sparsityTrainingB,
            int sparsityOutputB,
            int sparsityBatchB,
            int widthCellsF,
            int heightCellsF,
            int widthCellsB,
            int heightCellsB,
            int batchSizeF,
            int batchSizeB,
            float learningRate,
            float momentum,
            float weightsStdDev ) {

        SparseSequenceAutoencoderEntityConfig config = new SparseSequenceAutoencoderEntityConfig();

        config.cellMappingDensity = cellMappingDensity;
        config.sparsityTrainingF = sparsityTrainingF;
        config.sparsityOutputF = sparsityOutputF;
        config.sparsityBatchF = sparsityBatchF;

        config.sparsityTrainingB = sparsityTrainingB;
        config.sparsityOutputB = sparsityOutputB;
        config.sparsityBatchB = sparsityBatchB;

        config.cache = cache;
        config.widthCellsF = widthCellsF;
        config.heightCellsF = heightCellsF;
        config.widthCellsB = widthCellsB;
        config.heightCellsB = heightCellsB;
        config.batchSizeF = batchSizeF;
        config.batchSizeB = batchSizeB;
        config.learningRate = learningRate;
        config.momentum = momentum;
        config.weightsStdDev = weightsStdDev;

        PersistenceUtil.SetConfig( entityName, config );
    }

}
