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
 * Randomly produces points on a discrete grid.
 * Makes for a good test of unsupervised learning methods.
 * <p/>
 * Created by dave on 13/03/16.
 */
public class DiscreteRandomEntity extends Entity {

    public static final String ENTITY_TYPE = "discrete-random";

    public static final String OUTPUT = "output";

    public DiscreteRandomEntity( ObjectMap om, Node n, ModelEntity model ) {
        super( om, n, model );
    }

    public void getInputAttributes( Collection< String > attributes ) {
    }

    public void getOutputAttributes( Collection< String > attributes, DataFlags flags ) {
        attributes.add( OUTPUT );
    }

    @Override
    public Class getConfigClass() {
        return DiscreteRandomEntityConfig.class;
    }

    protected void doUpdateSelf() {

        DiscreteRandomEntityConfig config = ( DiscreteRandomEntityConfig ) _config;

        Data output = getDataLazyResize( OUTPUT, DataSize.create( config.elements ) );
        Random r = getRandom();

        // range = 1
        // intervals = 5
        // _1_2_3_4_5_   = 6 gaps.
        // _|_|_|_|_|_   = 6 gaps.
        // min        max

        float perLevel = 1.f / ( float ) ( config.levels + 1 );
        float range = config.max - config.min;

        for( int i = 0; i < config.elements; ++i ) {

            int n = r.nextInt( config.levels ) + 1; // so will be 0 -> n-1
            float x = ( float ) n * perLevel;
            x = x * range;
            x += config.min;
            //System.out.println(  "random: " + x );
            output._values[ i ] = x;
        }

        setData( OUTPUT, output );
    }
}