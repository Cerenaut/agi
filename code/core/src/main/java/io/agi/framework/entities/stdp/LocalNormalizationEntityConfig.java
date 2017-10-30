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

package io.agi.framework.entities.stdp;

import io.agi.framework.EntityConfig;
import io.agi.framework.Framework;
import io.agi.framework.persistence.PersistenceUtil;

/**
 * Created by dave on 9/08/17.
 */
public class LocalNormalizationEntityConfig extends EntityConfig {

    public int radius;

    public static void Set( String entityName, int radius ) {
        LocalNormalizationEntityConfig entityConfig = new LocalNormalizationEntityConfig();
        entityConfig.cache = true;
        entityConfig.radius = radius;

        PersistenceUtil.SetConfig( entityName, entityConfig );
    }

}
