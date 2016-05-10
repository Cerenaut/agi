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

package io.agi.framework.demo.mnist;

import io.agi.core.orm.ObjectMap;
import io.agi.framework.Entity;
import io.agi.framework.factories.CommonEntityFactory;
import io.agi.framework.persistence.models.ModelEntity;

/**
 * Created by dave on 5/05/16.
 */
public class MnistEntityFactory extends CommonEntityFactory {

    public MnistEntityFactory() {

    }

    public Entity create( ObjectMap objectMap, ModelEntity modelEntity ) {

        Entity e = super.create( objectMap, modelEntity );

        if( e != null ) {
            return e;
        }

        String entityType = modelEntity.type;

        if( entityType.equals( MnistEntity.ENTITY_TYPE ) ) {
            return new MnistEntity( objectMap, _n, modelEntity );
        }

        return null;
    }
}