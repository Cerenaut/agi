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
import io.agi.core.data.Data2d;
import io.agi.core.data.DataSize;
import io.agi.core.orm.ObjectMap;
import io.agi.framework.DataFlags;
import io.agi.framework.Entity;
import io.agi.framework.Framework;
import io.agi.framework.Node;
import io.agi.framework.persistence.models.ModelEntity;

import java.util.Collection;

/**
 * Creates a rolling window of values from an Input Data, captured over time. The window is updated every update() with a
 * new value from the Input. The oldest value is discarded.
 * <p/>
 * Created by dave on 2/04/16.
 */
public class VectorSeriesEntity extends Entity {

    public static final String ENTITY_TYPE = "vector-series";

    public static final String INPUT = "input";
    public static final String OUTPUT = "output";

    public VectorSeriesEntity( ObjectMap om, Node n, ModelEntity model ) {
        super( om, n, model );
    }

    public void getInputAttributes( Collection< String > attributes ) {
        attributes.add( INPUT );
    }

    public void getOutputAttributes( Collection< String > attributes, DataFlags flags ) {
        attributes.add( OUTPUT );

        flags.putFlag( OUTPUT, DataFlags.FLAG_SPARSE_BINARY );
    }

    public Class getConfigClass() {
        return VectorSeriesEntityConfig.class;
    }

    protected void doUpdateSelf() {

        // Get all the parameters:
        VectorSeriesEntityConfig config = ( VectorSeriesEntityConfig ) _config;

        Data input = getData( INPUT );

        if( input == null ) {
            return; // nothing to log yet
        }

        if( !_config.learn ) {
             return; // don't append or update the output data except when "learning" ie accumulating
        }

        Data oldOutput = getData( OUTPUT );
        Data newOutput = Data2d.accumulateVectors( input, config.period, oldOutput );
        setData( OUTPUT, newOutput );

/*        int elements = input.getSize();

        if( config.period < 0 ) {
            // keep appending
            Data oldOutput = getData( OUTPUT ); // error in classification (0,1)

            int oldHistoryLength = 0;
            int newHistoryLength = 0;

            if( oldOutput == null ) {
                newHistoryLength = 1;
            }
            else {
                // infinite length
                oldHistoryLength = oldOutput._dataSize.getSize( DataSize.DIMENSION_Y );
                newHistoryLength = oldHistoryLength + 1;
            }

            newOutput = new Data( DataSize.create( DataSize.DIMENSION_X, elements, DataSize.DIMENSION_Y, newHistoryLength ) );

            // copy old vectors
            int offsetThis = 0;
            int offsetThat = 0;

            if( oldOutput != null ) {
                offsetThis = oldHistoryLength * elements;
                newOutput.copyRange( oldOutput, 0, 0, oldHistoryLength * elements );
            }

            // append new vector
            newOutput.copyRange( input, offsetThis, offsetThat, elements );
        }
        else {
            // rolling window
            DataSize ds = DataSize.create( DataSize.DIMENSION_X, elements, DataSize.DIMENSION_Y, config.period );
            newOutput = getDataLazyResize( OUTPUT, ds );

            // it's slow, but ideally the picture makes sense when viewed. So to make this happen we need to shift all the data.
            // shift all the old values 1 place
            for( int i1 = config.period -2; i1 >= 0; --i1 ) {

                int i2 = i1 + 1;
                if( i2 >= config.period ) {
                    continue;
                }

                for( int j = 0; j < elements; ++j ) {
                    int offset1 = i1 * elements +j;
                    int offset2 = i2 * elements +j;
                    float x1 = newOutput._values[ offset1 ];
                    newOutput._values[ offset2 ] = x1;
                }
            }

            for( int j = 0; j < elements; ++j ) {
                int offset2 = 0 * elements +j;
                float x1 = input._values[ j ];
                newOutput._values[ offset2 ] = x1;
            }
        }

        setData( OUTPUT, newOutput );*/
    }

}
