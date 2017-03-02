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
import io.agi.core.orm.ObjectMap;
import io.agi.core.sdr.SparseDistributedEncoder;
import io.agi.framework.DataFlags;
import io.agi.framework.Entity;
import io.agi.framework.Node;
import io.agi.framework.persistence.models.ModelEntity;

import java.util.Collection;

/**
 * Creates a circular queue of Data items copied from some other Data source.
 *
 * Created by Dave on 26/03/2016.
 */
public class DataQueueEntity extends Entity {

    public static final String ENTITY_TYPE = "data-queue";

    // data
    public static final String DATA_INPUT = "input";
    public static final String DATA_QUEUE_ = "queue-";

    public DataQueueEntity( ObjectMap om, Node n, ModelEntity model ) {
        super( om, n, model );
    }

    @Override
    public void getInputAttributes( Collection< String > attributes ) {
        attributes.add( DATA_INPUT );
    }

    @Override
    public void getOutputAttributes( Collection< String > attributes, DataFlags flags ) {

        // add one output for each
        DataQueueEntityConfig config = (DataQueueEntityConfig)_config;

        int length = config.queueLength;
        for( int i = 0; i < length; ++i ) {
            String suffix = getDataQueueSuffix( i );
            attributes.add( suffix );
        }
    }

    @Override
    public Class getConfigClass() {
        return DataQueueEntityConfig.class;
    }

    public void doUpdateSelf() {

        DataQueueEntityConfig config = (DataQueueEntityConfig)_config;

        Data input = getData( DATA_INPUT );

        if( input == null ) {
            return; // nothing to enqueue
        }

        Data copy = new Data( input ); // create deep copy

        String suffix = getDataQueueSuffix( config.queueHead );

        setData( suffix, copy );

        config.queueHead += 1;

        if( config.queueHead >= config.queueLength ) {
            config.queueHead = 0; // wrap
        }
    }

    public String getDataQueueSuffix( int i ) {
        return DATA_QUEUE_ + i;
    }
}