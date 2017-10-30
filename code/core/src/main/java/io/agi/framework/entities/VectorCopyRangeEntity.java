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

import io.agi.core.data.Data;
import io.agi.core.data.Data2d;
import io.agi.core.orm.ObjectMap;
import io.agi.core.util.FileUtil;
import io.agi.framework.DataFlags;
import io.agi.framework.Entity;
import io.agi.framework.Node;
import io.agi.framework.persistence.DataJsonSerializer;
import io.agi.framework.persistence.models.ModelData;
import io.agi.framework.persistence.models.ModelEntity;

import java.io.File;
import java.util.Collection;

/**
 * Creates a rolling window of values from an Input Data, captured over time. The window is updated every update() with a
 * new value from the Input. The oldest value is discarded.
 * <p/>
 * Created by dave on 2/04/16.
 */
public class VectorCopyRangeEntity extends Entity {

    public static final String ENTITY_TYPE = "vector-copy-range";

    public static final String INPUT = "input";
    public static final String OUTPUT = "output";

    public VectorCopyRangeEntity( ObjectMap om, Node n, ModelEntity model ) {
        super( om, n, model );
    }

    public void getInputAttributes( Collection< String > attributes ) {
        attributes.add( INPUT );
    }

    public void getOutputAttributes( Collection< String > attributes, DataFlags flags ) {

        VectorCopyRangeEntityConfig config = ( VectorCopyRangeEntityConfig ) _config;

        attributes.add( OUTPUT );

        if( config.encoding.equals( DataJsonSerializer.ENCODING_SPARSE_BINARY ) ) {
            flags.putFlag( OUTPUT, DataFlags.FLAG_SPARSE_BINARY );
        }
        else if( config.encoding.equals( DataJsonSerializer.ENCODING_SPARSE_REAL ) ) {
            flags.putFlag( OUTPUT, DataFlags.FLAG_SPARSE_REAL );
        }
        else { // remove existing flag, if any
            flags.removeFlag( OUTPUT, DataFlags.FLAG_SPARSE_BINARY );
            flags.removeFlag( OUTPUT, DataFlags.FLAG_SPARSE_REAL );
        }
    }

    public Class getConfigClass() {
        return VectorCopyRangeEntityConfig.class;
    }

    protected void doUpdateSelf() {

        // Get all the parameters:
        VectorCopyRangeEntityConfig config = ( VectorCopyRangeEntityConfig ) _config;

        Data input = getData( INPUT );

        if( input == null ) {
            return; // nothing to log yet
        }

        Data output = new Data( config.range );

        int offsetInput = config.offsetInput;
        int offsetOutput = config.offsetOutput;
        int range = config.range;

        output.copyRange( input, offsetOutput, offsetInput, range );

        setData( OUTPUT, output );
    }

}
