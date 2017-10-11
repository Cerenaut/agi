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

import io.agi.core.ann.unsupervised.BiasedSparseAutoencoder;
import io.agi.core.ann.unsupervised.BiasedSparseAutoencoderConfig;
import io.agi.core.ann.unsupervised.LifetimeSparseAutoencoder;
import io.agi.core.ann.unsupervised.LifetimeSparseAutoencoderConfig;
import io.agi.core.data.Data;
import io.agi.core.data.Data2d;
import io.agi.core.orm.ObjectMap;
import io.agi.framework.DataFlags;
import io.agi.framework.Entity;
import io.agi.framework.Node;
import io.agi.framework.persistence.models.ModelEntity;

import java.awt.*;
import java.util.Collection;

/**
 * Created by dave on 7/07/16.
 */
public class BiasedSparseAutoencoderEntity extends LifetimeSparseAutoencoderEntity {

    public static final String ENTITY_TYPE = "biased-sparse-autoencoder";


    public static final String INPUT_LEARNING_RATE = "input-learning-rate";
    public static final String BATCH_LEARNING_RATES = "batch-learning-rates";
    public static final String INPUT_OLD = "input-old";

    public BiasedSparseAutoencoderEntity( ObjectMap om, Node n, ModelEntity model ) {
        super( om, n, model );
    }

    public void getInputAttributes( Collection< String > attributes ) {
        super.getInputAttributes( attributes );
        attributes.add( INPUT_LEARNING_RATE );
    }

    public void getOutputAttributes( Collection< String > attributes, DataFlags flags ) {

        super.getOutputAttributes( attributes, flags );

        attributes.add( BATCH_LEARNING_RATES );
        attributes.add( INPUT_OLD );

        flags.putFlag( BATCH_LEARNING_RATES, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( INPUT_OLD, DataFlags.FLAG_NODE_CACHE );
    }

    @Override
    public Class getConfigClass() {
        return BiasedSparseAutoencoderEntityConfig.class;
    }

    protected void doUpdateSelf() {

        // Do nothing unless the input is defined
        Data input = getData( INPUT );
        if( input == null ) {
            return; // can't update yet.
        }

        // Get all the parameters:
        String name = getName();

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Test parameters
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Feedforward size
        Point inputSize = Data2d.getSize( input );

        int inputWidth = inputSize.x;
        int inputHeight = inputSize.y;
        int inputArea = inputWidth * inputHeight;

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Algorithm specific parameters
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Region size
        BiasedSparseAutoencoderEntityConfig config = ( BiasedSparseAutoencoderEntityConfig ) _config;

        // Build the algorithm
        ObjectMap om = ObjectMap.GetInstance();

        BiasedSparseAutoencoderConfig autoencoderConfig = new BiasedSparseAutoencoderConfig();

        autoencoderConfig.setup(
                om, name, _r,
                inputArea, config.widthCells, config.heightCells,
                config.learningRate,
                config.momentum,
                config.sparsity,
                config.sparsityLifetime,
                config.weightsStdDev,
                config.batchCount, config.batchSize );

        BiasedSparseAutoencoder ksa = new BiasedSparseAutoencoder( name, om );

        ksa.setup( autoencoderConfig );

        // Load data, overwriting the default setup.
        copyDataFromPersistence( ksa );

        // Update the region-layer, including optional reset and learning on/off switch
        if( config.reset ) {
            ksa.reset();
        }

        ksa._c.setLearn( config.learn );
        ksa.update();

        // Save computed properties
        config.batchCount = autoencoderConfig.getBatchCount();
//        config.age = autoencoderConfig.getAge(); no, let this be advanced by the entity

        // Save data
        copyDataToPersistence( ksa );
    }

    protected void copyDataFromPersistence( BiasedSparseAutoencoder ksa ) {

        super.copyDataFromPersistence( ksa );

        Data inputLearningRate = getData( INPUT_LEARNING_RATE );
        if( inputLearningRate == null ) {
            inputLearningRate = new Data( 1 );
            inputLearningRate._values[ 0 ] = 1f; // default: learn as normal
        }

        ksa._inputLearningRate.copy( inputLearningRate );
        ksa._inputValuesOld = getDataLazyResize( INPUT_OLD, ksa._inputValuesOld._dataSize );
        ksa._batchLearningRates = getDataLazyResize( BATCH_LEARNING_RATES, ksa._batchLearningRates._dataSize );
    }

    protected void copyDataToPersistence( BiasedSparseAutoencoder ksa ) {

        super.copyDataToPersistence( ksa );

        setData( INPUT_OLD, ksa._inputValuesOld );
        setData( BATCH_LEARNING_RATES, ksa._batchLearningRates );
    }

}
