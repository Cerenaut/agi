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

import io.agi.core.data.Coordinate;
import io.agi.core.data.Data;
import io.agi.core.data.DataSize;
import io.agi.core.orm.ObjectMap;
import io.agi.framework.DataFlags;
import io.agi.framework.Entity;
import io.agi.framework.Node;
import io.agi.framework.persistence.models.ModelEntity;

import java.util.Collection;

/**
 * Creates an ever increasing log of new Data vectors, concatenated into a matrix.
 * Size is history length x input vector elements
 *
 * Created by gideon on 23/07/16.
 */
public class DataSeriesEntity extends Entity {

    public static final String ENTITY_TYPE = "data-series";

    public static final String INPUT = "input";
    public static final String OUTPUT = "output";

    public DataSeriesEntity( ObjectMap om, Node n, ModelEntity model ) {
        super( om, n, model );
    }

    public void getInputAttributes( Collection< String > attributes ) {
        attributes.add( INPUT );
    }

    public void getOutputAttributes( Collection< String > attributes, DataFlags flags ) {
        attributes.add( OUTPUT );
    }

    public Class getConfigClass() {
        return DataSeriesEntityConfig.class;
    }

    /**
     * Get the new copy of the input Data and append it to the Data series
     * dataSeries = dataSeries + data
     */
    protected void doUpdateSelf() {

        DataSeriesEntityConfig config = ( DataSeriesEntityConfig ) _config;

        Data dataInput = getData( INPUT );

        int inputs = dataInput.getSize();

        Data dataSeriesOld = getData( OUTPUT );

        // work out the size of the existing series output
        int seriesLength = 0;

        if( config.reset ) {
            dataSeriesOld = null; // reset by forgetting all old data
        }

        if( dataSeriesOld != null ) {
            int volume = dataSeriesOld.getSize();
            seriesLength = volume / inputs;
        }

        // create a new buffer which is +inputs larger than the last one
        // input is stored contiguously
        Data dataSeriesNew = new Data( DataSize.create( inputs, seriesLength +1 ) );

        // copy old values:
        int offsetThis = 0;
        int offsetThat = 0;

        if( seriesLength > 0 ) {
            int range = inputs * seriesLength;
            dataSeriesNew.copyRange( dataSeriesOld, offsetThis, offsetThat, range );
            offsetThis += range;
        }

        // copy new values
        dataSeriesNew.copyRange( dataInput, offsetThis, offsetThat, inputs );

        setData( OUTPUT, dataSeriesNew );
    }

}
