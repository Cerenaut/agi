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
import io.agi.framework.persistence.models.ModelData;

/**
 * Created by dave on 2/04/16.
 */
public class ValueSeriesEntityConfig extends EntityConfig {

    public int flushPeriod = -1; // number of samples before it flushes and clears -1 for infinite
    public int period = 100; // number of samples before it wraps. -1 for infinite
    public String entityName;
    public String configPath;

    // for writing to disk:
    public String writeFileEncoding = ModelData.ENCODING_DENSE;
    public String writeFilePath = "";
    public String writeFilePrefix = "";
    public String writeFileExtension = "json";

}
