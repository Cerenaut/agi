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

import io.agi.core.ann.unsupervised.BatchSparseNetwork;
import io.agi.core.ann.unsupervised.BatchSparseNetworkConfig;
import io.agi.core.data.Data;
import io.agi.core.data.Data2d;
import io.agi.core.orm.ObjectMap;
import io.agi.framework.DataFlags;
import io.agi.framework.Entity;
import io.agi.framework.Node;
import io.agi.framework.persistence.models.ModelEntity;

import java.awt.*;
import java.util.Collection;
import java.util.HashSet;

/**
 * Created by dave on 16/10/17.
 */
public class BatchSparseNetworkEntity extends Entity {

    public static final String ENTITY_TYPE = "batch-sparse-predictor";

    public static final String INPUT_TRAINING_INPUT  =  "input-training-input";
    public static final String INPUT_TESTING_INPUT   =  "input-testing-input";
    public static final String INPUT_TRAINING_OUTPUT =  "input-training-output";
    public static final String OUTPUT_TESTING_OUTPUT = "output-testing-output";

    public static final String WEIGHTS_1 = "weights-1";
    public static final String WEIGHTS_2 = "weights-2";
    public static final String BIASES_1 = "biases-1";
    public static final String BIASES_2 = "biases-2";

    public static final String WEIGHTS_1_VELOCITY = "weights-1-velocity";
    public static final String WEIGHTS_2_VELOCITY = "weights-2-velocity";
    public static final String BIASES_1_VELOCITY = "biases-1-velocity";
    public static final String BIASES_2_VELOCITY = "biases-2-velocity";

//    public static final String ERRORS = "errors";
    public static final String WEIGHTED_SUM = "weighted-sum";
    public static final String SPIKES = "spikes";
//    public static final String OUTPUT_RECONSTRUCTION = "reconstruction";

    public static final String BATCH_OUTPUT_IDEAL = "batch-output-ideal";
    public static final String BATCH_OUTPUT_OUTPUT = "batch-output-output";
    public static final String BATCH_OUTPUT_INPUT = "batch-output-input";
    public static final String BATCH_OUTPUT_INPUT_LIFETIME = "batch-output-input-lifetime";
    public static final String BATCH_OUTPUT_ERRORS = "batch-output-errors";
    public static final String BATCH_HIDDEN_INPUT = "batch-hidden-input";
    public static final String BATCH_HIDDEN_WEIGHTED_SUM = "batch-hidden-weighted-sum";
    public static final String BATCH_HIDDEN_ERRORS = "batch-hidden-errors";

    public BatchSparseNetworkEntity( ObjectMap om, Node n, ModelEntity model ) {
        super( om, n, model );
    }

    public void getInputAttributes( Collection< String > attributes ) {
        attributes.add( INPUT_TRAINING_INPUT );
        attributes.add( INPUT_TESTING_INPUT );
        attributes.add( INPUT_TRAINING_OUTPUT );
    }

    public void getOutputAttributes( Collection< String > attributes, DataFlags flags ) {

        attributes.add( OUTPUT_TESTING_OUTPUT );

        attributes.add( WEIGHTS_1 );
        attributes.add( WEIGHTS_2 );
        attributes.add( BIASES_1 );
        attributes.add( BIASES_2 );

        attributes.add( WEIGHTS_1_VELOCITY );
        attributes.add( WEIGHTS_2_VELOCITY );
        attributes.add( BIASES_1_VELOCITY );
        attributes.add( BIASES_2_VELOCITY );

//        attributes.add( ERRORS );
        attributes.add( SPIKES );
        attributes.add( WEIGHTED_SUM );
//        attributes.add( OUTPUT_RECONSTRUCTION );

        attributes.add( BATCH_OUTPUT_IDEAL );
        attributes.add( BATCH_OUTPUT_OUTPUT );
        attributes.add( BATCH_OUTPUT_INPUT );
        attributes.add( BATCH_OUTPUT_INPUT_LIFETIME );
        attributes.add( BATCH_OUTPUT_ERRORS );
        attributes.add( BATCH_HIDDEN_INPUT );
        attributes.add( BATCH_HIDDEN_WEIGHTED_SUM );
        attributes.add( BATCH_HIDDEN_ERRORS );
    }

    @Override
    public Class getConfigClass() {
        return BatchSparseNetworkEntityConfig.class;
    }

    protected void doUpdateSelf() {

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Test parameters
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        // Get all the parameters:
        String name = getName();
        BatchSparseNetworkEntityConfig config = ( BatchSparseNetworkEntityConfig ) _config;

        // Do nothing unless the input is defined
        Data trainingInput  = getData( INPUT_TRAINING_INPUT );
        Data  testingInput  = getData( INPUT_TESTING_INPUT );
        Data trainingOutput = getData( INPUT_TRAINING_OUTPUT );

        if( trainingOutput == null ) {
            return; // can't size the output yet
        }

        Point outputSize = Data2d.getSize( trainingOutput ); // an output size

        int outputWidth = outputSize.x;
        int outputHeight = outputSize.y;
        int outputArea = outputWidth * outputHeight;

        Data testingOutput = new Data( outputWidth, outputHeight );

        if( ( trainingInput == null ) || ( testingInput == null ) ) {
            // produce an empty output, because we have insufficient input.
            // This is to break dependency cycles in the Entity-graph
            setData( OUTPUT_TESTING_OUTPUT, testingOutput );

            if( config.reset ) {
                config.resetDelayed = true;
            }

            return;
        }

//HashSet< Integer > from = trainingInput.indicesMoreThan( 0.1f );
//HashSet< Integer > to = trainingOutput.indicesMoreThan( 0.1f );

//        Point inputSize = Data2d.getSize( trainingInput );
//
//        int inputWidth = inputSize.x;
//        int inputHeight = inputSize.y;
//        int inputArea = inputWidth * inputHeight;
        int inputArea = trainingInput.getSize();

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Algorithm specific parameters
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Build the algorithm
        ObjectMap om = ObjectMap.GetInstance();

        BatchSparseNetworkConfig autoencoderConfig = new BatchSparseNetworkConfig();

        autoencoderConfig.setup(
                om, name, _r,
                inputArea, outputArea,
                config.widthCells, config.heightCells,
                config.learningRate,
                config.momentum,
                config.sparsity,
                config.sparsityLifetime,
                config.sparsityOutput,
                config.weightsStdDev,
                config.batchCount, config.batchSize );

        BatchSparseNetwork ksa = new BatchSparseNetwork( name, om );

        ksa.setup( autoencoderConfig );

        // Load data, overwriting the default setup.
        copyDataFromPersistence( ksa );

        // Update the region-layer, including optional reset and learning on/off switch
        if( config.reset || config.resetDelayed ) {
            ksa.reset();
            config.resetDelayed = false;
        }

        ksa._c.setLearn( config.learn );

        // predict something based on older input.
        ksa.setInput( testingInput, trainingInput, trainingOutput );
        ksa.update();

        // Save computed properties
        config.batchCount = autoencoderConfig.getBatchCount();
//        config.age = autoencoderConfig.getAge(); no, let this be advanced by the entity

        // Save data
        copyDataToPersistence( ksa );
    }

    protected void copyDataFromPersistence( BatchSparseNetwork ksa ) {

        ksa._cellWeights1 = getDataLazyResize( WEIGHTS_1, ksa._cellWeights1._dataSize );
        ksa._cellWeights2 = getDataLazyResize( WEIGHTS_2, ksa._cellWeights2._dataSize );
        ksa._cellBiases1 = getDataLazyResize( BIASES_1, ksa._cellBiases1._dataSize );
        ksa._cellBiases2 = getDataLazyResize( BIASES_2, ksa._cellBiases2._dataSize );

        ksa._cellWeights1Velocity = getDataLazyResize( WEIGHTS_1_VELOCITY, ksa._cellWeights1Velocity._dataSize );
        ksa._cellWeights2Velocity = getDataLazyResize( WEIGHTS_2_VELOCITY, ksa._cellWeights2Velocity._dataSize );
        ksa._cellBiases1Velocity = getDataLazyResize( BIASES_1_VELOCITY, ksa._cellBiases1Velocity._dataSize );
        ksa._cellBiases2Velocity = getDataLazyResize( BIASES_2_VELOCITY, ksa._cellBiases2Velocity._dataSize );

//        ksa._cellErrors = getDataLazyResize( ERRORS, ksa._cellErrors._dataSize );
        ksa._testingHiddenWeightedSum = getDataLazyResize( WEIGHTED_SUM, ksa._testingHiddenWeightedSum._dataSize );
        ksa._testingHiddenSpikes = getDataLazyResize( SPIKES, ksa._testingHiddenSpikes._dataSize );
//        ksa._inputReconstruction = getDataLazyResize( OUTPUT_RECONSTRUCTION, ksa._inputReconstruction._dataSize );

        ksa._batchOutputIdeal = getDataLazyResize( BATCH_OUTPUT_IDEAL, ksa._batchOutputIdeal._dataSize );
        ksa._batchOutputOutput = getDataLazyResize( BATCH_OUTPUT_OUTPUT, ksa._batchOutputOutput._dataSize );
        ksa._batchOutputInput = getDataLazyResize( BATCH_OUTPUT_INPUT, ksa._batchOutputInput._dataSize );
        ksa._batchOutputInputLifetime = getDataLazyResize( BATCH_OUTPUT_INPUT_LIFETIME, ksa._batchOutputInputLifetime._dataSize );
        ksa._batchOutputErrors = getDataLazyResize( BATCH_OUTPUT_ERRORS, ksa._batchOutputErrors._dataSize );
        ksa._batchHiddenInput = getDataLazyResize( BATCH_HIDDEN_INPUT, ksa._batchHiddenInput._dataSize );
        ksa._batchHiddenWeightedSum = getDataLazyResize( BATCH_HIDDEN_WEIGHTED_SUM, ksa._batchHiddenWeightedSum._dataSize );
        ksa._batchHiddenErrors = getDataLazyResize( BATCH_HIDDEN_ERRORS, ksa._batchHiddenErrors._dataSize );
    }

    protected void copyDataToPersistence( BatchSparseNetwork ksa ) {

        setData( OUTPUT_TESTING_OUTPUT, ksa._testingOutputValues );

        setData( WEIGHTS_1, ksa._cellWeights1 );
        setData( WEIGHTS_2, ksa._cellWeights2 );
        setData( BIASES_1, ksa._cellBiases1 );
        setData( BIASES_2, ksa._cellBiases2 );

        setData( WEIGHTS_1_VELOCITY, ksa._cellWeights1Velocity );
        setData( WEIGHTS_2_VELOCITY, ksa._cellWeights2Velocity );
        setData( BIASES_1_VELOCITY, ksa._cellBiases1Velocity );
        setData( BIASES_2_VELOCITY, ksa._cellBiases2Velocity );

//        serialize( ERRORS, ksa._cellErrors );
        setData( WEIGHTED_SUM, ksa._testingHiddenWeightedSum );
        setData( SPIKES, ksa._testingHiddenSpikes );
//        serialize( OUTPUT_RECONSTRUCTION, ksa._inputReconstruction );

        setData( BATCH_OUTPUT_IDEAL, ksa._batchOutputIdeal );
        setData( BATCH_OUTPUT_OUTPUT, ksa._batchOutputOutput );
        setData( BATCH_OUTPUT_INPUT, ksa._batchOutputInput );
        setData( BATCH_OUTPUT_INPUT_LIFETIME, ksa._batchOutputInputLifetime );
        setData( BATCH_OUTPUT_ERRORS, ksa._batchOutputErrors );
        setData( BATCH_HIDDEN_INPUT, ksa._batchHiddenInput );
        setData( BATCH_HIDDEN_WEIGHTED_SUM, ksa._batchHiddenWeightedSum );
        setData( BATCH_HIDDEN_ERRORS, ksa._batchHiddenErrors );
    }

}