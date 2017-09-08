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
public class LifetimeSparseAutoencoderEntity extends Entity {

    public static final String ENTITY_TYPE = "lifetime-sparse-autoencoder";

    public static final String INPUT = "input";

    public static final String WEIGHTS = "weights";
    public static final String BIASES_1 = "biases-1";
    public static final String BIASES_2 = "biases-2";

    public static final String WEIGHTS_VELOCITY = "weights-velocity";
    public static final String BIASES_1_VELOCITY = "biases-1-velocity";
    public static final String BIASES_2_VELOCITY = "biases-2-velocity";

    public static final String ERRORS = "errors";
    public static final String WEIGHTED_SUM = "weighted-sum";
    public static final String SPIKES = "spikes";
    public static final String RECONSTRUCTION = "reconstruction";

    public static final String BATCH_OUTPUT_OUTPUT = "batch-output-output";
    public static final String BATCH_OUTPUT_INPUT = "batch-output-input";
    public static final String BATCH_OUTPUT_INPUT_LIFETIME = "batch-output-input-lifetime";
    public static final String BATCH_OUTPUT_ERRORS = "batch-output-errors";
    public static final String BATCH_HIDDEN_OUTPUT = "batch-hidden-output";
    public static final String BATCH_HIDDEN_WEIGHTED_SUM = "batch-hidden-weighted-sum";
    public static final String BATCH_HIDDEN_ERRORS = "batch-hidden-errors";

    public LifetimeSparseAutoencoderEntity( ObjectMap om, Node n, ModelEntity model ) {
        super( om, n, model );
    }

    public void getInputAttributes( Collection< String > attributes ) {
        attributes.add( INPUT );
    }

    public void getOutputAttributes( Collection< String > attributes, DataFlags flags ) {

        attributes.add( WEIGHTS );
        attributes.add( BIASES_1 );
        attributes.add( BIASES_2 );

        attributes.add( WEIGHTS_VELOCITY );
        attributes.add( BIASES_1_VELOCITY );
        attributes.add( BIASES_2_VELOCITY );

        attributes.add( ERRORS );
        attributes.add( SPIKES );
        attributes.add( WEIGHTED_SUM );
        attributes.add( RECONSTRUCTION );

        attributes.add( BATCH_OUTPUT_OUTPUT );
        attributes.add( BATCH_OUTPUT_INPUT );
        attributes.add( BATCH_OUTPUT_INPUT_LIFETIME );
        attributes.add( BATCH_OUTPUT_ERRORS );
        attributes.add( BATCH_HIDDEN_OUTPUT );
        attributes.add( BATCH_HIDDEN_WEIGHTED_SUM );
        attributes.add( BATCH_HIDDEN_ERRORS );

        flags.putFlag( WEIGHTS, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( BIASES_1, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( BIASES_2, DataFlags.FLAG_NODE_CACHE );

        flags.putFlag( WEIGHTS_VELOCITY, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( BIASES_1_VELOCITY, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( BIASES_2_VELOCITY, DataFlags.FLAG_NODE_CACHE );

        flags.putFlag( ERRORS, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( SPIKES, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( WEIGHTED_SUM, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( RECONSTRUCTION, DataFlags.FLAG_NODE_CACHE );

        flags.putFlag( BATCH_OUTPUT_OUTPUT, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( BATCH_OUTPUT_INPUT, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( BATCH_OUTPUT_INPUT_LIFETIME, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( BATCH_OUTPUT_ERRORS, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( BATCH_HIDDEN_OUTPUT, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( BATCH_HIDDEN_WEIGHTED_SUM, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( BATCH_HIDDEN_ERRORS, DataFlags.FLAG_NODE_CACHE );
    }

    @Override
    public Class getConfigClass() {
        return LifetimeSparseAutoencoderEntityConfig.class;
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
        LifetimeSparseAutoencoderEntityConfig config = ( LifetimeSparseAutoencoderEntityConfig ) _config;

        // Build the algorithm
        ObjectMap om = ObjectMap.GetInstance();

        LifetimeSparseAutoencoderConfig autoencoderConfig = new LifetimeSparseAutoencoderConfig();

        autoencoderConfig.setup(
                om, name, _r,
                inputArea, config.widthCells, config.heightCells,
                config.learningRate,
                config.momentum,
                config.sparsity,
                config.sparsityLifetime,
                config.weightsStdDev,
                config.batchCount, config.batchSize );

        LifetimeSparseAutoencoder ksa = new LifetimeSparseAutoencoder( name, om );

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

    protected void copyDataFromPersistence( LifetimeSparseAutoencoder ksa ) {

        ksa._inputValues = getData( INPUT );

        ksa._cellWeights = getDataLazyResize( WEIGHTS, ksa._cellWeights._dataSize );
        ksa._cellBiases1 = getDataLazyResize( BIASES_1, ksa._cellBiases1._dataSize );
        ksa._cellBiases2 = getDataLazyResize( BIASES_2, ksa._cellBiases2._dataSize );

        ksa._cellWeightsVelocity = getDataLazyResize( WEIGHTS_VELOCITY, ksa._cellWeightsVelocity._dataSize );
        ksa._cellBiases1Velocity = getDataLazyResize( BIASES_1_VELOCITY, ksa._cellBiases1Velocity._dataSize );
        ksa._cellBiases2Velocity = getDataLazyResize( BIASES_2_VELOCITY, ksa._cellBiases2Velocity._dataSize );

        ksa._cellErrors = getDataLazyResize( ERRORS, ksa._cellErrors._dataSize );
        ksa._cellWeightedSum = getDataLazyResize( WEIGHTED_SUM, ksa._cellWeightedSum._dataSize );
        ksa._cellSpikes = getDataLazyResize( SPIKES, ksa._cellSpikes._dataSize );
        ksa._inputReconstruction = getDataLazyResize( RECONSTRUCTION, ksa._inputReconstruction._dataSize );

        ksa._batchOutputOutput = getDataLazyResize( BATCH_OUTPUT_OUTPUT, ksa._batchOutputOutput._dataSize );
        ksa._batchOutputInput = getDataLazyResize( BATCH_OUTPUT_INPUT, ksa._batchOutputInput._dataSize );
        ksa._batchOutputInputLifetime = getDataLazyResize( BATCH_OUTPUT_INPUT_LIFETIME, ksa._batchOutputInputLifetime._dataSize );
        ksa._batchOutputErrors = getDataLazyResize( BATCH_OUTPUT_ERRORS, ksa._batchOutputErrors._dataSize );
        ksa._batchHiddenInput = getDataLazyResize( BATCH_HIDDEN_OUTPUT, ksa._batchHiddenInput._dataSize );
        ksa._batchHiddenWeightedSum = getDataLazyResize( BATCH_HIDDEN_WEIGHTED_SUM, ksa._batchHiddenWeightedSum._dataSize );
        ksa._batchHiddenErrors = getDataLazyResize( BATCH_HIDDEN_ERRORS, ksa._batchHiddenErrors._dataSize );
    }

    protected void copyDataToPersistence( LifetimeSparseAutoencoder ksa ) {

        setData( WEIGHTS, ksa._cellWeights );
        setData( BIASES_1, ksa._cellBiases1 );
        setData( BIASES_2, ksa._cellBiases2 );

        setData( WEIGHTS_VELOCITY, ksa._cellWeightsVelocity );
        setData( BIASES_1_VELOCITY, ksa._cellBiases1Velocity );
        setData( BIASES_2_VELOCITY, ksa._cellBiases2Velocity );

        setData( ERRORS, ksa._cellErrors );
        setData( WEIGHTED_SUM, ksa._cellWeightedSum );
        setData( SPIKES, ksa._cellSpikes );
        setData( RECONSTRUCTION, ksa._inputReconstruction );

        setData( BATCH_OUTPUT_OUTPUT, ksa._batchOutputOutput );
        setData( BATCH_OUTPUT_INPUT, ksa._batchOutputInput );
        setData( BATCH_OUTPUT_INPUT_LIFETIME, ksa._batchOutputInputLifetime );
        setData( BATCH_OUTPUT_ERRORS, ksa._batchOutputErrors );
        setData( BATCH_HIDDEN_OUTPUT, ksa._batchHiddenInput );
        setData( BATCH_HIDDEN_WEIGHTED_SUM, ksa._batchHiddenWeightedSum );
        setData( BATCH_HIDDEN_ERRORS, ksa._batchHiddenErrors );
    }

}
