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
 * Creates a rolling window of Data from a property, captured over time. The window is updated every update() with a
 * new value from the property. The oldest value is discarded.
 * <p/>
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
     * get the new copy of the Data and append it to the Data series
     * dataSeries = dataSeries + data
     */
    protected void doUpdateSelf() {

        // Get all the parameters:
        DataSeriesEntityConfig config = ( DataSeriesEntityConfig ) _config;

        // get the new Data object to add to the series, and the other information that will be needed
        Data data = getData( INPUT );
        DataSize dataSize = data._dataSize;
        int dimensions = dataSize.getDimensions();

        // get the output (the existing data series), to append to it
        Data dataSeries = getData( OUTPUT );

        // if Data series doesn't exist, create it
        if ( dataSeries == null ) {
            // (create a new Data object, with one more dimension, for the time series of Data)
            DataSize dataSeriesSize = new DataSize( dimensions + 1 );
            dataSeries = new Data( dataSeriesSize );
        }
        // if it did exist, we need to allocate additional memory for the appended values
        else {
            // what is the best way to allocate an additional block of memory of the size of 'data'
                // Data.setSize wipes out all data
                // it should be possible to use setSize together with Data.copy() .....
        }

        // set a coordinate to point to the beginning of the new memory allocated in 'data series'
        int seriesDimensions  = dataSeries._dataSize.getDimensions();
        int lastDimensionSize = dataSeries._dataSize.getSize( seriesDimensions );

        Coordinate dataSeriesCoord = dataSeries.begin();
        dataSeriesCoord.set( seriesDimensions, lastDimensionSize - 1 );

        // iterate over data and
        // set the value in the corresponding position in the data series
        Coordinate newDataCoord = data.begin();
        float value;
        while( newDataCoord.next() ) {
            value = data.get( newDataCoord );
            dataSeriesCoord.next();
            dataSeries.set( dataSeriesCoord, value );
        }

        setData( OUTPUT, dataSeries );
    }

}
