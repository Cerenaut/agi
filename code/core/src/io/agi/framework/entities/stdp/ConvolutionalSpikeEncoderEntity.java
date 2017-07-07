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

import io.agi.core.data.ConvolutionData3d;
import io.agi.core.data.Data;
import io.agi.core.data.Data2d;
import io.agi.core.data.DataSize;
import io.agi.core.orm.ObjectMap;
import io.agi.framework.DataFlags;
import io.agi.framework.Entity;
import io.agi.framework.Framework;
import io.agi.framework.Node;
import io.agi.framework.persistence.models.ModelEntity;

import java.awt.*;
import java.util.Collection;

/**
 * Transforms 2x 2D input matrices into a 3D output (w, h, depth) encoded as spike trains. Intensity of input determines
 * output spike rate.
 *
 * Created by dave on 5/05/17.
 */
public class ConvolutionalSpikeEncoderEntity extends Entity {

    public static final String ENTITY_TYPE = "convolutional-spike-encoder";

    // data
    public static final String DATA_INPUT_POS = "input-pos";
    public static final String DATA_INPUT_NEG = "input-neg";
    public static final String DATA_INTEGRATED = "integrated";
    public static final String DATA_OUTPUT = "output";

    public ConvolutionalSpikeEncoderEntity( ObjectMap om, Node n, ModelEntity model ) {
        super( om, n, model );
    }

    @Override
    public void getInputAttributes( Collection< String > attributes ) {
        attributes.add( DATA_INPUT_POS );
        attributes.add( DATA_INPUT_NEG );
    }

    @Override
    public void getOutputAttributes( Collection< String > attributes, DataFlags flags ) {
        attributes.add( DATA_INTEGRATED );
        attributes.add( DATA_OUTPUT );
    }

    @Override
    public Class getConfigClass() {
        return ConvolutionalSpikeEncoderEntityConfig.class;
    }

    public void doUpdateSelf() {

        ConvolutionalSpikeEncoderEntityConfig config = ( ConvolutionalSpikeEncoderEntityConfig ) _config;

        Data inputPos = getData( DATA_INPUT_POS );
        Data inputNeg = getData( DATA_INPUT_NEG );

        Point sizePos = Data2d.getSize( inputPos );
        Point sizeNeg = Data2d.getSize( inputNeg );

        if( !sizePos.equals( sizeNeg ) ) {
            return; // can't run
        }

        // 1. +/- DoGs of input. Abs.
        // 2. SpikeRate encoding
        // 3. Interleave output as 3d.

        int depth = 2;
        DataSize convolutionalSize = ConvolutionData3d.getDataSize( sizePos.x, sizePos.y, depth );

        Data integrated = getDataLazyResize( DATA_INTEGRATED, convolutionalSize );
        Data output = new Data( convolutionalSize );

        boolean clear = false;

        try {
            String stringValue = Framework.GetConfig( config.clearFlagEntityName, config.clearFlagConfigPath );
            clear = Boolean.valueOf( stringValue );
        }
        catch( Exception e ) {
        }

        clear |= config.clear;

        if( clear ) {
            //System.err.println(" Clearing " + getName() );
            integrated.set( 0f );
            config.clear = false;
        }
        else {
            //System.err.println(" Not clearing " );
        }

        for( int y = 0; y < sizePos.y; ++y ) {
            for( int x = 0; x < sizePos.x; ++x ) {
                int offset2d = Data2d.getOffset( sizePos.x, x, y );

                float valuePos = Math.max( 0f, inputPos._values[ offset2d ] );
                float valueNeg = Math.max( 0f, inputNeg._values[ offset2d ] );

                int z = 0;
                int offset3d = ConvolutionData3d.getOffset( x, y, z, convolutionalSize );

                float valuePosOld = integrated._values[ offset3d + 0 ];
                float valueNegOld = integrated._values[ offset3d + 1 ];

                float valuePosNew = valuePosOld + valuePos;
                float valueNegNew = valueNegOld + valueNeg;

                float spikePos = 0;
                float spikeNeg = 0;

                if( valuePosNew >= config.spikeThreshold ) {
                    spikePos = 1f;
                    valuePosNew = 0f;
                }

                if( valueNegNew >= config.spikeThreshold ) {
                    spikeNeg = 1f;
                    valueNegNew = 0f;
                }

                integrated._values[ offset3d + 0 ] = valuePosNew;
                integrated._values[ offset3d + 1 ] = valueNegNew;

                output._values[ offset3d + 0 ] = spikePos;
                output._values[ offset3d + 1 ] = spikeNeg;
            }
        }

        setData( DATA_INTEGRATED, integrated );
        setData( DATA_OUTPUT, output );
    }
}
