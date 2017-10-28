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
 *
 * Created by dave on 7/07/16.
 */
public class PredictiveCodingEntityConfig extends EntityConfig {

    // parameters you can adjust
    public int widthCells = 0;
    public int heightCells = 0;

    public float outputDecayRate = 0.0f;
    public int outputSpikeAgeMax = 0;

    // stats calculated during operation
    public boolean resetDelayed = false;

    public float sumPredictionErrorFP = 0;
    public float sumPredictionErrorFN = 0;

    public static void Set(
            String entityName,
            boolean cache,
            int widthCells,
            int heightCells,
            int outputSpikeAgeMax,
            float outputDecayRate ) {

        PredictiveCodingEntityConfig config = new PredictiveCodingEntityConfig();

        config.widthCells = widthCells;
        config.heightCells = heightCells;
        config.cache = cache;
        config.outputSpikeAgeMax = outputSpikeAgeMax;
        config.outputDecayRate = outputDecayRate;

        PersistenceUtil.SetConfig( entityName, config );
    }
}
