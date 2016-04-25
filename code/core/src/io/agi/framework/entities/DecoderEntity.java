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
import io.agi.core.sdr.EncoderFactory;
import io.agi.core.sdr.SparseDistributedEncoder;
import io.agi.framework.DataFlags;
import io.agi.framework.Entity;
import io.agi.framework.Node;
import io.agi.framework.persistence.models.ModelEntity;

import java.util.Collection;

/**
 * Created by gideon on 26/03/2016.
 */
public class DecoderEntity extends Entity {

    public static final String ENTITY_TYPE = "decoder";

    // data
    public static final String DATA_INPUT_ENCODED = "input-encoded";
    public static final String DATA_OUTPUT_DECODED = "output-decoded";

    public DecoderEntity( ObjectMap om, Node n, ModelEntity model ) {
        super( om, n, model );
    }

    @Override
    public void getInputAttributes( Collection< String > attributes ) {
        attributes.add( DATA_INPUT_ENCODED );
    }

    @Override
    public void getOutputAttributes( Collection< String > attributes, DataFlags flags ) {
        attributes.add( DATA_OUTPUT_DECODED );
    }

    @Override
    public Class getConfigClass() {
        return EncoderEntityConfig.class;
    }

    public void doUpdateSelf() {

        EncoderEntityConfig config = ( EncoderEntityConfig ) _config;

        SparseDistributedEncoder encoder = EncoderFactory.create( config.encoderType );

        if( encoder == null ) {
            _logger.error( getName() + ": Could not create Encoder object" );
            return;
        }

        encoder.setup( config.bits, config.density, config.encodeZero );

        Data input = getData( DATA_INPUT_ENCODED );

        if( input == null ) {
            getData( DATA_INPUT_ENCODED );
            _logger.info( getName() + ": Could not update because input missing." );
            return;
        }

        Data output = encoder.createDecodingOutput( input );
        encoder.decode( input, output );

        setData( DATA_OUTPUT_DECODED, output );
    }

}