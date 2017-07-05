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
import io.agi.framework.persistence.models.ModelData;

/**
 * Created by dave on 2/04/16.
 */
public class VectorSeriesEntityConfig extends EntityConfig {

    public int periodAccumulate = 1; // option to log every N steps the sum of the interval
    public int flushPeriod = -1; // number of samples before it flushes and clears -1 for infinite
    public int period = 100; // number of samples before it wraps. -1 for infinite

    public int countAccumulate = 0; // how many samples have been accumulated

    public String encoding = ModelData.ENCODING_DENSE;

    // for writing to disk:
    public String writeFilePath = "";
    public String writeFilePrefix = "";
    public String writeFileExtension = "json";

    public static void Set( String entityName, int periodAccumulate, int period, String encoding ) {
        VectorSeriesEntityConfig entityConfig = new VectorSeriesEntityConfig();

        entityConfig.cache = true;
        entityConfig.periodAccumulate = periodAccumulate;
//        entityConfig.flushPeriod = -1;
        entityConfig.period = period;
        entityConfig.countAccumulate = 0;
        entityConfig.encoding = encoding;

        Framework.SetConfig( entityName, entityConfig );
    }

}
