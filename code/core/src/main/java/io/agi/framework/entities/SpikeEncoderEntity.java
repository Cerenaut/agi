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
import io.agi.core.orm.ObjectMap;
import io.agi.core.sdr.SparseDistributedEncoder;
import io.agi.framework.DataFlags;
import io.agi.framework.Entity;
import io.agi.framework.Node;
import io.agi.framework.persistence.models.ModelEntity;

import java.util.Collection;

/**
 * Converts a real unit input into a Spiking input of equal dimension.
 *
 * Spike probability is a function of input unit values.
 *
 * Created by dave on 30/09/16.
 */
public class SpikeEncoderEntity extends Entity {

    public static final String ENTITY_TYPE = "spike-encoder";

    // data
    public static final String DATA_INPUT = "input";
    public static final String DATA_OUTPUT = "output";

    public SpikeEncoderEntity( ObjectMap om, Node n, ModelEntity model ) {
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
        return SpikeEncoderEntityConfig.class;
    }

    public void doUpdateSelf() {

        SpikeEncoderEntityConfig config = ( SpikeEncoderEntityConfig ) _config;

        Data input = getData( DATA_INPUT );
        Data output = new Data( input.getSize() );

        int volume = input.getSize();

        for( int i = 0; i < volume; ++i ) {
            float spike = 0f;
            float unit = input._values[ i ];
            float random = _r.nextFloat();

            random *= config.maxSpikeRate; // e.g. make it 1/5 steps average if input = 1 and factor = 0.2

            if( random < unit ) {
                spike = 1f; //
            }

            output._values[ i ] = spike;
        }

        setData( DATA_OUTPUT, output );
    }
}