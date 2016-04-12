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

import io.agi.core.data.Data;
import io.agi.core.data.DataSize;
import io.agi.core.orm.ObjectMap;
import io.agi.framework.DataFlags;
import io.agi.framework.Entity;
import io.agi.framework.Node;
import io.agi.framework.persistence.models.ModelEntity;

import java.util.Collection;
import java.util.Random;

/**
 * Created by dave on 12/03/16.
 */
public class RandomVectorEntity extends Entity {

    public static final String ENTITY_TYPE = "random-vector";

    public static final String ELEMENTS = "elements";
    public static final String MIN = "min";
    public static final String MAX = "max";

    public static final String OUTPUT = "output";

    public RandomVectorEntity( ObjectMap om, Node n, ModelEntity model ) {
        super( om, n, model );
    }

    public void getInputAttributes( Collection< String > attributes ) {
    }

    public void getOutputAttributes( Collection< String > attributes, DataFlags flags ) {
        attributes.add( OUTPUT );
    }

    @Override
    public Class getConfigClass() {
        return RandomVectorConfig.class;
    }

    protected void doUpdateSelf() {

        // Get all the parameters:
        RandomVectorConfig config = ( RandomVectorConfig ) _config;

        Data output = getDataLazyResize( OUTPUT, DataSize.create( config.elements ) );
        Random r = getRandom();

        float range = config.max - config.min;

        for( int i = 0; i < config.elements; ++i ) {

            float x = r.nextFloat();
            x = x * range;
            x += config.min;

            output._values[ i ] = x;
        }

        //output.setRandom();
        setData( OUTPUT, output );
    }
}