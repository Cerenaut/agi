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

import io.agi.core.alg.AutoRegionLayer;
import io.agi.core.alg.AutoRegionLayerConfig;
import io.agi.core.ann.unsupervised.KSparseAutoencoder;
import io.agi.core.ann.unsupervised.KSparseAutoencoderConfig;
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
public class KSparseAutoencoderEntity extends Entity {

    public static final String ENTITY_TYPE = "k-sparse-autoencoder";

    public static final String INPUT = "input";

    public static final String WEIGHTS = "weights";
    public static final String BIASES_1 = "biases-1";
    public static final String BIASES_2 = "biases-2";

    public static final String WEIGHTS_VELOCITY = "weights-velocity";
    public static final String BIASES_1_VELOCITY = "biases-1-velocity";
    public static final String BIASES_2_VELOCITY = "biases-2-velocity";

    public static final String ERRORS = "errors";
    public static final String WEIGHTED_SUM = "weighted-sum";
    public static final String SPIKES_TOP_KA = "spikes-top-ka";
    public static final String SPIKES_TOP_K = "spikes-top-k";

    public static final String RECONSTRUCTION_KA = "reconstruction-ka";
    public static final String RECONSTRUCTION_K = "reconstruction-k";
    public static final String AGES = "ages";

    public static final String OUTPUT_GRADIENTS = "output-gradients";
    public static final String HIDDEN_GRADIENTS = "hidden-gradients";

    public KSparseAutoencoderEntity( ObjectMap om, Node n, ModelEntity model ) {
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
        attributes.add( SPIKES_TOP_KA );
        attributes.add( SPIKES_TOP_K );
        attributes.add( WEIGHTED_SUM );
        attributes.add( RECONSTRUCTION_KA );
        attributes.add( RECONSTRUCTION_K );
        attributes.add( AGES );

        attributes.add( OUTPUT_GRADIENTS );
        attributes.add( HIDDEN_GRADIENTS );

        flags.putFlag(WEIGHTS, DataFlags.FLAG_NODE_CACHE);
        flags.putFlag(BIASES_1, DataFlags.FLAG_NODE_CACHE);
        flags.putFlag( BIASES_2, DataFlags.FLAG_NODE_CACHE );

        flags.putFlag( WEIGHTS_VELOCITY, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( BIASES_1_VELOCITY, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( BIASES_2_VELOCITY, DataFlags.FLAG_NODE_CACHE );

        flags.putFlag( ERRORS, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( SPIKES_TOP_KA, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( SPIKES_TOP_K, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( RECONSTRUCTION_KA, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( RECONSTRUCTION_K, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( AGES, DataFlags.FLAG_NODE_CACHE );

        flags.putFlag( OUTPUT_GRADIENTS, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( HIDDEN_GRADIENTS, DataFlags.FLAG_NODE_CACHE );
    }

    @Override
    public Class getConfigClass() {
        return KSparseAutoencoderEntityConfig.class;
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

        int inputWidth  = inputSize.x;
        int inputHeight = inputSize.y;
        int inputArea = inputWidth * inputHeight;

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Algorithm specific parameters
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Region size
        KSparseAutoencoderEntityConfig config = ( KSparseAutoencoderEntityConfig ) _config;

        // Build the algorithm
        ObjectMap om = ObjectMap.GetInstance();

        KSparseAutoencoderConfig autoencoderConfig = new KSparseAutoencoderConfig();

        autoencoderConfig.setup(
                om, name, _r,
                inputArea, config.widthCells, config.heightCells,
                config.learningRate, config.momentum,
                config.sparsityOutput, config.sparsity, config.sparsityMin, config.sparsityMax,
                config.ageMin, config.ageMax, config.age,
                config.weightsStdDev,
                config.batchCount, config.batchSize );

        KSparseAutoencoder ksa = new KSparseAutoencoder( name, om );

        ksa.setup( autoencoderConfig );

        // Load data, overwriting the default setup.
        copyDataFromPersistence( ksa );

        // Update the region-layer, including optional reset and learning on/off switch
        if( config.reset ) {
            ksa.reset();
        }

        ksa._c.setLearn(config.learn);
        ksa.update();

        // Save computed properties
        config.sparsity = autoencoderConfig.getSparsity();
        config.batchCount = autoencoderConfig.getBatchCount();
//        config.age = autoencoderConfig.getAge(); no, let this be advanced by the entity

        // Save data
        copyDataToPersistence( ksa );
    }

    protected void copyDataFromPersistence( KSparseAutoencoder ksa ) {

        ksa._inputValues = getData( INPUT );

        ksa._cellWeights = getDataLazyResize(WEIGHTS, ksa._cellWeights._dataSize);
        ksa._cellBiases1 = getDataLazyResize( BIASES_1, ksa._cellBiases1._dataSize );
        ksa._cellBiases2 = getDataLazyResize( BIASES_2, ksa._cellBiases2._dataSize );

        ksa._cellWeightsVelocity = getDataLazyResize( WEIGHTS_VELOCITY, ksa._cellWeightsVelocity._dataSize );
        ksa._cellBiases1Velocity = getDataLazyResize( BIASES_1_VELOCITY, ksa._cellBiases1Velocity._dataSize );
        ksa._cellBiases2Velocity = getDataLazyResize( BIASES_2_VELOCITY, ksa._cellBiases2Velocity._dataSize );

        ksa._cellErrors = getDataLazyResize( ERRORS, ksa._cellErrors._dataSize );
        ksa._cellWeightedSum = getDataLazyResize( WEIGHTED_SUM, ksa._cellWeightedSum._dataSize );
        ksa._cellSpikesTopKA = getDataLazyResize( SPIKES_TOP_KA, ksa._cellSpikesTopKA._dataSize );
        ksa._cellSpikesTopK = getDataLazyResize( SPIKES_TOP_K, ksa._cellSpikesTopK._dataSize );
        ksa._inputReconstructionKA = getDataLazyResize( RECONSTRUCTION_KA, ksa._inputReconstructionKA._dataSize );
        ksa._inputReconstructionK = getDataLazyResize( RECONSTRUCTION_K, ksa._inputReconstructionK._dataSize );
        ksa._cellAges = getDataLazyResize( AGES, ksa._cellAges._dataSize );

        ksa._inputGradients = getDataLazyResize( OUTPUT_GRADIENTS, ksa._inputGradients._dataSize );
        ksa._cellGradients = getDataLazyResize( HIDDEN_GRADIENTS, ksa._cellGradients._dataSize );
    }

    protected void copyDataToPersistence( KSparseAutoencoder ksa ) {

        setData( WEIGHTS, ksa._cellWeights );
        setData( BIASES_1, ksa._cellBiases1 );
        setData( BIASES_2, ksa._cellBiases2 );

        setData( WEIGHTS_VELOCITY, ksa._cellWeightsVelocity );
        setData( BIASES_1_VELOCITY, ksa._cellBiases1Velocity );
        setData( BIASES_2_VELOCITY, ksa._cellBiases2Velocity );

        setData( ERRORS, ksa._cellErrors );
        setData( WEIGHTED_SUM, ksa._cellWeightedSum );
        setData( SPIKES_TOP_KA, ksa._cellSpikesTopKA );
        setData( SPIKES_TOP_K, ksa._cellSpikesTopK );
        setData( RECONSTRUCTION_KA, ksa._inputReconstructionKA );
        setData( RECONSTRUCTION_K, ksa._inputReconstructionK );
        setData( AGES, ksa._cellAges );

        setData( OUTPUT_GRADIENTS, ksa._inputGradients );
        setData( HIDDEN_GRADIENTS, ksa._cellGradients );
    }

}
