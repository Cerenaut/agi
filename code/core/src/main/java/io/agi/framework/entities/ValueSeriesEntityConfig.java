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
import io.agi.framework.Framework;
import io.agi.framework.persistence.DataJsonSerializer;
import io.agi.framework.persistence.PersistenceUtil;
import io.agi.framework.persistence.models.ModelData;

/**
 * This should properly be called ScalarSeries, as it logs a single scalar value.
 * Created by dave on 2/04/16.
 */
public class ValueSeriesEntityConfig extends EntityConfig {

    // Period for logging (if positive, rolling window, else infinite) and flushing content
    public int periodAccumulate = 1; // option to log every N steps the sum of the interval
    public int flushPeriod = -1; // number of samples before it flushes and clears -1 for infinite, i.e. no flush
    public int period = 100; // number of samples before it wraps. -1 for infinite

    public float value = 0; // the value of the entity, which is available as scalar and Data
    public float valueAccumulate = 0; // This allows us to accumulate
    public float factorAccumulate = 1; // This allows us to scale the accumulated value
    public int countAccumulate = 0; // how many samples have been accumulated

    // Option 1: Get scalar from entity config property
    public String entityName; // name of entity providing input scalar
    public String configPath; // config property of entity providing input scalar

    // Option 2: Get scalar from an element of a Data
    public String dataName; // alternatively, specify a data to source the value from
    public int dataOffset = 0; // default, pick first value in input data

    // For periodically flushing to disk:
    public String writeFileEncoding = DataJsonSerializer.ENCODING_DENSE;
    public String writeFilePath = "";
    public String writeFilePrefix = "";
    public String writeFileExtension = "json";

    public static void Set(
            String entityName,
            int accumulatePeriod,
            float accumulateFactor,
            int flushPeriod,
            int period,
            String inputEntityName,
            String inputConfigPath,
            String inputDataName,
            int inputDataOffset ) {
        ValueSeriesEntityConfig entityConfig = new ValueSeriesEntityConfig();

        entityConfig.cache = true;
        entityConfig.value = 0;
        entityConfig.periodAccumulate = accumulatePeriod;
        entityConfig.factorAccumulate = accumulateFactor;
        entityConfig.valueAccumulate = 0;
        entityConfig.countAccumulate = 0;
        entityConfig.flushPeriod = flushPeriod;
        entityConfig.period = period;
        entityConfig.learn = true;
        entityConfig.writeFilePath = "";
        entityConfig.writeFilePrefix = "";
        entityConfig.writeFileEncoding = DataJsonSerializer.ENCODING_DENSE;

        entityConfig.entityName = inputEntityName;
        entityConfig.configPath = inputConfigPath;
        entityConfig.dataName = inputDataName;
        entityConfig.dataOffset = inputDataOffset;

        PersistenceUtil.SetConfig( entityName, entityConfig );
    }


}
