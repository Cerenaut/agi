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

import io.agi.core.data.*;
import io.agi.core.orm.ObjectMap;
import io.agi.framework.DataFlags;
import io.agi.framework.Entity;
import io.agi.framework.Node;
import io.agi.framework.persistence.models.ModelEntity;

import java.awt.*;
import java.util.Collection;

/**
 * Created by dave on 9/08/17.
 */
public class LocalNormalizationEntity extends Entity {

    public static final String ENTITY_TYPE = "local-normalization";

    public static final String DATA_INPUT = "input";
    public static final String DATA_OUTPUT = "output";

    public LocalNormalizationEntity( ObjectMap om, Node n, ModelEntity model ) {
        super( om, n, model );
    }

    @Override
    public void getInputAttributes( Collection< String > attributes ) {
        attributes.add( DATA_INPUT );
    }

    @Override
    public void getOutputAttributes( Collection< String > attributes, DataFlags flags ) {
        attributes.add( DATA_OUTPUT );
    }

    @Override
    public Class getConfigClass() {
        return LocalNormalizationEntityConfig.class;
    }

    public void doUpdateSelf() {

        LocalNormalizationEntityConfig config = ( LocalNormalizationEntityConfig ) _config;

        Data input = getData( DATA_INPUT );

        Point inputSize = Data2d.getSize( input );

        int w = inputSize.x;
        int h = inputSize.y;
        int radius = config.radius;
        int area = w * h;

        Data output = new Data( DataSize.create( w, h ) );


        for( int y = 0; y < h; ++y ) {
            for( int x = 0; x < w; ++x ) {

                int y2a = y-radius;
                int y2b = y+radius;
                int x2a = x-radius;
                int x2b = x+radius;

                float centreValue = input._values[ y * w + x ];
                float sum = 0f;

                for( int y2 = y2a; y2 < y2b; ++y2 ) {

                    int y3 = Math.max( 0, Math.min( h-1, y2 ) ); // duplicate edge px

                    for( int x2 = x2a; x2 < x2b; ++x2 ) {

                        int x3 = Math.max( 0, Math.min( w-1, x2 ) );

                        float value = input._values[ y3 * w + x3 ];
                        sum += value;
                    }
                }

                float normalized = 0f;
                if( sum > 0f ) {
                    float mean = sum / area;
                    normalized = centreValue / mean;
                }

                output._values[ y * w + x ] = normalized;
            }
        }

        setData( DATA_OUTPUT, output );
    }

}
