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
import java.util.Random;

/**
 * Created by dave on 7/07/16.
 */
public class AutoRegionLayerEntity extends Entity {

    public static final String ENTITY_TYPE = "auto-region-layer";

    public static final String INPUT_1 = "input-1";
    public static final String INPUT_2 = "input-2";

    public static final String OUTPUT_INPUT_1 = "output-input-1";
    public static final String OUTPUT_INPUT_2 = "output-input-2";

    public static final String CONTEXT_FREE_ACTIVITY     = "context-free-activity";
    public static final String CONTEXT_FREE_ACTIVITY_OLD = "context-free-activity-old";
    public static final String CONTEXT_FREE_ACTIVITY_NEW = "context-free-activity-new";

//    public static final String CONTEXTUAL_ACTIVITY     = "contextual-activity";
//    public static final String CONTEXTUAL_ACTIVITY_OLD = "contextual-activity-old";
//    public static final String CONTEXTUAL_ACTIVITY_NEW = "contextual-activity-new";
    public static final String PREDICTION_FP  = "prediction-fp";
    public static final String PREDICTION_FN  = "prediction-fn";
    public static final String PREDICTION_OLD = "prediction-old";
    public static final String PREDICTION_NEW = "prediction-new";
    public static final String PREDICTION_NEW_REAL = "prediction-new-real";
    public static final String PREDICTION_INHIBITION = "prediction-inhibition";

    public static final String OUTPUT = "output";
    public static final String OUTPUT_AGE = "output-age";

    public static final String PREDICTOR_WEIGHTS = "predictor-weights";

    public static final String CONTEXT_FREE_WEIGHTS = "context-free-weights";
    public static final String CONTEXT_FREE_BIASES_1 = "context-free-biases-1";
    public static final String CONTEXT_FREE_BIASES_2 = "context-free-biases-2";

//    public static final String CONTEXT_FREE_WEIGHTS_VELOCITY = "context-free-weights-velocity";
//    public static final String CONTEXT_FREE_BIASES_1_VELOCITY = "context-free-biases-1-velocity";
//    public static final String CONTEXT_FREE_BIASES_2_VELOCITY = "context-free-biases-2-velocity";

    public static final String CONTEXT_FREE_ERRORS = "context-free-errors";
    public static final String CONTEXT_FREE_WEIGHTED_SUM = "context-free-weighted-sum";
    public static final String CONTEXT_FREE_TRANSFER = "context-free-transfer";
    public static final String CONTEXT_FREE_RESPONSE = "context-free-response";
    public static final String CONTEXT_FREE_RECONSTRUCTION = "context-free-reconstruction";
    public static final String CONTEXT_FREE_AGES = "context-free-ages";
    public static final String CONTEXT_FREE_RATES = "context-free-rates";
    public static final String CONTEXT_FREE_PROMOTION = "context-free-promotion";
    public static final String CONTEXT_FREE_INHIBITION = "context-free-inhibition";

//    public static final String CONTEXTUAL_WEIGHTS = "contextual-weights";
//    public static final String CONTEXTUAL_BIASES_1 = "contextual-biases-1";
//    public static final String CONTEXTUAL_BIASES_2 = "contextual-biases-2";
//    public static final String CONTEXTUAL_ERRORS = "contextual-errors";
//    public static final String CONTEXTUAL_RESPONSE = "contextual-response";
//    public static final String CONTEXTUAL_RECONSTRUCTION = "contextual-reconstruction";

    public AutoRegionLayerEntity( ObjectMap om, Node n, ModelEntity model ) {
        super( om, n, model );
    }

    public void getInputAttributes( Collection< String > attributes ) {
        attributes.add( INPUT_1 );
        attributes.add( INPUT_2 );
    }

    public void getOutputAttributes( Collection< String > attributes, DataFlags flags ) {

        attributes.add( OUTPUT_INPUT_1 );
        attributes.add( OUTPUT_INPUT_2 );

        flags.putFlag( OUTPUT_INPUT_1, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( OUTPUT_INPUT_2, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( OUTPUT_INPUT_1, DataFlags.FLAG_PERSIST_ONLY );
        flags.putFlag( OUTPUT_INPUT_2, DataFlags.FLAG_PERSIST_ONLY );

        attributes.add( CONTEXT_FREE_ACTIVITY );
        attributes.add( CONTEXT_FREE_ACTIVITY_OLD );
        attributes.add( CONTEXT_FREE_ACTIVITY_NEW );

        flags.putFlag( CONTEXT_FREE_ACTIVITY, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( CONTEXT_FREE_ACTIVITY_OLD, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( CONTEXT_FREE_ACTIVITY_NEW, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( CONTEXT_FREE_ACTIVITY, DataFlags.FLAG_SPARSE_BINARY );
        flags.putFlag( CONTEXT_FREE_ACTIVITY_OLD, DataFlags.FLAG_SPARSE_BINARY );
        flags.putFlag( CONTEXT_FREE_ACTIVITY_NEW, DataFlags.FLAG_SPARSE_BINARY );

//        attributes.add( CONTEXTUAL_ACTIVITY );
//        attributes.add( CONTEXTUAL_ACTIVITY_OLD );
//        attributes.add( CONTEXTUAL_ACTIVITY_NEW );

        attributes.add( PREDICTION_FP );
        attributes.add( PREDICTION_FN );
        attributes.add( PREDICTION_OLD );
        attributes.add( PREDICTION_NEW );
        attributes.add( PREDICTION_NEW_REAL );
        attributes.add( PREDICTION_INHIBITION );

        flags.putFlag( PREDICTION_FP, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( PREDICTION_FN, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( PREDICTION_OLD, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( PREDICTION_NEW, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( PREDICTION_NEW_REAL, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( PREDICTION_FP, DataFlags.FLAG_SPARSE_BINARY );
        flags.putFlag( PREDICTION_FN, DataFlags.FLAG_SPARSE_BINARY );
        flags.putFlag( PREDICTION_OLD, DataFlags.FLAG_SPARSE_BINARY );
        flags.putFlag( PREDICTION_NEW, DataFlags.FLAG_SPARSE_BINARY );

        attributes.add( OUTPUT );
        attributes.add( OUTPUT_AGE );

        flags.putFlag( OUTPUT, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( OUTPUT, DataFlags.FLAG_SPARSE_BINARY );

        attributes.add( PREDICTOR_WEIGHTS );

        flags.putFlag( PREDICTOR_WEIGHTS, DataFlags.FLAG_NODE_CACHE );

        attributes.add( CONTEXT_FREE_WEIGHTS );
        attributes.add( CONTEXT_FREE_BIASES_1 );
        attributes.add( CONTEXT_FREE_BIASES_2 );

//        attributes.add( CONTEXT_FREE_WEIGHTS_VELOCITY );
//        attributes.add( CONTEXT_FREE_BIASES_1_VELOCITY );
//        attributes.add( CONTEXT_FREE_BIASES_2_VELOCITY );

        attributes.add( CONTEXT_FREE_ERRORS );
        attributes.add( CONTEXT_FREE_TRANSFER );
        attributes.add( CONTEXT_FREE_RESPONSE );
        attributes.add( CONTEXT_FREE_WEIGHTED_SUM );
        attributes.add( CONTEXT_FREE_RECONSTRUCTION );
        attributes.add( CONTEXT_FREE_AGES );
        attributes.add( CONTEXT_FREE_RATES );
        attributes.add( CONTEXT_FREE_PROMOTION );
        attributes.add( CONTEXT_FREE_INHIBITION );

        flags.putFlag( CONTEXT_FREE_WEIGHTS, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( CONTEXT_FREE_BIASES_1, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( CONTEXT_FREE_BIASES_2, DataFlags.FLAG_NODE_CACHE );

//        flags.putFlag( CONTEXT_FREE_WEIGHTS_VELOCITY, DataFlags.FLAG_NODE_CACHE );
//        flags.putFlag( CONTEXT_FREE_BIASES_1_VELOCITY, DataFlags.FLAG_NODE_CACHE );
//        flags.putFlag( CONTEXT_FREE_BIASES_2_VELOCITY, DataFlags.FLAG_NODE_CACHE );

        flags.putFlag( CONTEXT_FREE_ERRORS, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( CONTEXT_FREE_TRANSFER, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( CONTEXT_FREE_RESPONSE, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( CONTEXT_FREE_RECONSTRUCTION, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( CONTEXT_FREE_AGES, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( CONTEXT_FREE_RATES, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( CONTEXT_FREE_PROMOTION, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( CONTEXT_FREE_INHIBITION, DataFlags.FLAG_NODE_CACHE );

//        attributes.add( CONTEXTUAL_WEIGHTS );
//        attributes.add( CONTEXTUAL_BIASES_1 );
//        attributes.add( CONTEXTUAL_BIASES_2 );
//        attributes.add( CONTEXTUAL_ERRORS );
//        attributes.add( CONTEXTUAL_RESPONSE );
//        attributes.add( CONTEXTUAL_RECONSTRUCTION );
    }

    @Override
    public Class getConfigClass() {
        return AutoRegionLayerEntityConfig.class;
    }

    protected void doUpdateSelf() {

        // Do nothing unless the input is defined
        Data input1 = getData( INPUT_1 );
        Data input2 = getData( INPUT_2 );

        if( ( input2 == null ) || ( input1 == null ) ) {
            return; // can't update yet.
        }

        // Get all the parameters:
        String regionLayerName = getName();

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Test parameters
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Feedforward size
        Point input1Size = Data2d.getSize( input1 );
        Point input2Size = Data2d.getSize( input2 );

        int input1Width  = input1Size.x;
        int input1Height = input1Size.y;
        int input2Width  = input2Size.x;
        int input2Height = input2Size.y;

        int inputArea = input1Width * input1Height + input2Width * input2Height;

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Algorithm specific parameters
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Region size
        AutoRegionLayerEntityConfig config = ( AutoRegionLayerEntityConfig ) _config;

        // Build the algorithm
        //RandomInstance.setSeed(randomSeed); // make the tests repeatable
        ObjectMap om = ObjectMap.GetInstance();

        KSparseAutoencoderConfig contextFreeConfig = new KSparseAutoencoderConfig();
        KSparseAutoencoderConfig  contextualConfig = new KSparseAutoencoderConfig();

        String contextFreeName = getKey( AutoRegionLayerConfig.SUFFIX_CONTEXT_FREE );
  //      String contextualName  = getKey( AutoRegionLayerConfig.SUFFIX_CONTEXTUAL );

        contextFreeConfig.setup(
                om, contextFreeName, _r,
                inputArea, config.contextFreeWidthCells, config.contextFreeHeightCells, config.contextFreeLearningRate,
                config.contextFreeBinaryOutput,
                config.contextFreeSparsityOutput, config.contextFreeSparsity, config.contextFreeSparsityMin, config.contextFreeSparsityMax,
                config.contextFreeAgeMin, config.contextFreeAgeMax, config.contextFreeAge, config.contextFreeAgeScale,
                config.rateScale, config.rateMax, config.rateLearningRate );

//        int contextFreeCellArea = config.contextFreeWidthCells * config.contextFreeHeightCells;
//        int contextualInputArea = contextFreeCellArea * 2;
//        int contextualCellArea = config.contextualWidthCells * config.contextualHeightCells;

//        contextualConfig.setup(
//                om, contextualName, _r,
//                contextualInputArea, config.contextualWidthCells, config.contextualHeightCells, config.contextualLearningRate,
//                config.contextualSparsityOutput, config.contextualSparsity, config.contextualSparsityMin, config.contextualSparsityMax, config.contextualAgeMin, config.contextualAgeMax, config.contextualAge );

        AutoRegionLayerConfig rlc = new AutoRegionLayerConfig();
        rlc.setup(
            om, regionLayerName, _r,
            contextFreeConfig, contextualConfig,
            input1Width, input1Height,
            input2Width, input2Height,
            config.outputSparsity, config.predictorLearningRate, config.defaultPredictionInhibition );

        AutoRegionLayer rl = new AutoRegionLayer( regionLayerName, om );
        rl.setup( rlc );

        // Load data, overwriting the default setup.
        copyDataFromPersistence( rl );

        // Update the region-layer, including optional reset and learning on/off switch
        if( config.reset ) {
            rl.reset();
        }

        rl._rc.setLearn( config.learn );
//        for( int i = 0; i < 100; ++i ) {
            rl.update(); // 120-150ms. The rest of doUpdateSelf() is maybe 50ms.
//        }

        // Save data
        config.contextFreeSparsity = contextFreeConfig.getSparsity();
//        config.contextualSparsity = contextualConfig.getSparsity();

        config.contextFreeAge = contextFreeConfig.getAge();
//        config.contextualAge = contextualConfig.getAge();
        copyDataToPersistence( rl );
    }

    protected void copyDataFromPersistence( AutoRegionLayer rl ) {

        rl._input1 = getData( INPUT_1 );
        rl._input2 = getData( INPUT_2 );

        rl._contextFreeActivity = getDataLazyResize( CONTEXT_FREE_ACTIVITY, rl._contextFreeActivity._dataSize );
        rl._contextFreeActivityOld = getDataLazyResize( CONTEXT_FREE_ACTIVITY_OLD, rl._contextFreeActivityOld._dataSize );
        rl._contextFreeActivityNew = getDataLazyResize( CONTEXT_FREE_ACTIVITY_NEW, rl._contextFreeActivityNew._dataSize );

//        rl._contextualActivity = getDataLazyResize( CONTEXTUAL_ACTIVITY, rl._contextualActivity._dataSize );
//        rl._contextualActivityOld = getDataLazyResize( CONTEXTUAL_ACTIVITY_OLD, rl._contextualActivityOld._dataSize );
//        rl._contextualActivityNew = getDataLazyResize( CONTEXTUAL_ACTIVITY_NEW, rl._contextualActivityNew._dataSize );
        rl._predictionFP = getDataLazyResize( PREDICTION_FP, rl._predictionFP._dataSize );
        rl._predictionFN = getDataLazyResize( PREDICTION_FN, rl._predictionFN._dataSize );
        rl._predictionOld = getDataLazyResize( PREDICTION_OLD, rl._predictionOld._dataSize );
        rl._predictionNew = getDataLazyResize( PREDICTION_NEW, rl._predictionNew._dataSize );
        rl._predictionNewReal = getDataLazyResize( PREDICTION_NEW_REAL, rl._predictionNewReal._dataSize );
        rl._predictionInhibition = getDataLazyResize( PREDICTION_INHIBITION, rl._predictionInhibition._dataSize );

        rl._output = getDataLazyResize( OUTPUT, rl._output._dataSize );
        rl._outputAge = getDataLazyResize( OUTPUT_AGE, rl._outputAge._dataSize );

//        rl._predictor._weights = getDataLazyResize( PREDICTOR_WEIGHTS, rl._predictor._weights._dataSize );

        rl._contextFreeClassifier._cellWeights = getDataLazyResize( CONTEXT_FREE_WEIGHTS, rl._contextFreeClassifier._cellWeights._dataSize );
        rl._contextFreeClassifier._cellBiases1 = getDataLazyResize( CONTEXT_FREE_BIASES_1, rl._contextFreeClassifier._cellBiases1._dataSize );
        rl._contextFreeClassifier._cellBiases2 = getDataLazyResize( CONTEXT_FREE_BIASES_2, rl._contextFreeClassifier._cellBiases2._dataSize );

//        rl._contextFreeClassifier._cellWeightsVelocity = getDataLazyResize( CONTEXT_FREE_WEIGHTS_VELOCITY, rl._contextFreeClassifier._cellWeightsVelocity._dataSize );
//        rl._contextFreeClassifier._cellBiases1Velocity = getDataLazyResize( CONTEXT_FREE_BIASES_1_VELOCITY, rl._contextFreeClassifier._cellBiases1Velocity._dataSize );
//        rl._contextFreeClassifier._cellBiases2Velocity = getDataLazyResize( CONTEXT_FREE_BIASES_2_VELOCITY, rl._contextFreeClassifier._cellBiases2Velocity._dataSize );

        rl._contextFreeClassifier._cellErrors = getDataLazyResize( CONTEXT_FREE_ERRORS, rl._contextFreeClassifier._cellErrors._dataSize );
        rl._contextFreeClassifier._cellWeightedSum = getDataLazyResize( CONTEXT_FREE_WEIGHTED_SUM, rl._contextFreeClassifier._cellWeightedSum._dataSize );
        rl._contextFreeClassifier._cellTransfer = getDataLazyResize( CONTEXT_FREE_TRANSFER, rl._contextFreeClassifier._cellTransfer._dataSize );
        rl._contextFreeClassifier._cellTransferTopK = getDataLazyResize( CONTEXT_FREE_RESPONSE, rl._contextFreeClassifier._cellTransferTopK._dataSize );
        rl._contextFreeClassifier._inputReconstruction = getDataLazyResize( CONTEXT_FREE_RECONSTRUCTION, rl._contextFreeClassifier._inputReconstruction._dataSize );
        rl._contextFreeClassifier._cellAges = getDataLazyResize( CONTEXT_FREE_AGES, rl._contextFreeClassifier._cellAges._dataSize );
        rl._contextFreeClassifier._cellRates = getDataLazyResize( CONTEXT_FREE_RATES, rl._contextFreeClassifier._cellRates._dataSize );
//        rl._contextFreeClassifier._cellPromotion = getDataLazyResize( CONTEXT_FREE_PROMOTION, rl._contextFreeClassifier._cellPromotion._dataSize );

//        rl._contextualClassifier._cellWeights = getDataLazyResize( CONTEXTUAL_WEIGHTS, rl._contextualClassifier._cellWeights._dataSize );
//        rl._contextualClassifier._cellBiases1 = getDataLazyResize( CONTEXTUAL_BIASES_1, rl._contextualClassifier._cellBiases1._dataSize );
//        rl._contextualClassifier._cellBiases2 = getDataLazyResize( CONTEXTUAL_BIASES_2, rl._contextualClassifier._cellBiases2._dataSize );
//        rl._contextualClassifier._cellErrors = getDataLazyResize( CONTEXTUAL_ERRORS, rl._contextualClassifier._cellErrors._dataSize );
//        rl._contextualClassifier._cellResponse = getDataLazyResize( CONTEXTUAL_RESPONSE, rl._contextualClassifier._cellResponse._dataSize );
//        rl._contextualClassifier._inputReconstruction = getDataLazyResize( CONTEXTUAL_RECONSTRUCTION, rl._contextualClassifier._inputReconstruction._dataSize );
    }

    protected void copyDataToPersistence( AutoRegionLayer rl ) {

        setData( OUTPUT_INPUT_1, rl._outputInput1 );
        setData( OUTPUT_INPUT_2, rl._outputInput2 );

        setData( CONTEXT_FREE_ACTIVITY, rl._contextFreeActivity );
        setData( CONTEXT_FREE_ACTIVITY_OLD, rl._contextFreeActivityOld );
        setData( CONTEXT_FREE_ACTIVITY_NEW, rl._contextFreeActivityNew );

//        setData( CONTEXTUAL_ACTIVITY, rl._contextualActivity );
//        setData( CONTEXTUAL_ACTIVITY_OLD, rl._contextualActivityOld );
//        setData( CONTEXTUAL_ACTIVITY_NEW, rl._contextualActivityNew );
        setData( PREDICTION_FP, rl._predictionFP );
        setData( PREDICTION_FN, rl._predictionFN );
        setData( PREDICTION_OLD, rl._predictionOld );
        setData( PREDICTION_NEW, rl._predictionNew );
        setData( PREDICTION_NEW_REAL, rl._predictionNewReal );
        setData( PREDICTION_INHIBITION, rl._predictionInhibition );

        setData( OUTPUT, rl._output );
        setData( OUTPUT_AGE, rl._outputAge );

//        setData( PREDICTOR_WEIGHTS, rl._predictor._weights );

        setData( CONTEXT_FREE_WEIGHTS, rl._contextFreeClassifier._cellWeights );
        setData( CONTEXT_FREE_BIASES_1, rl._contextFreeClassifier._cellBiases1 );
        setData( CONTEXT_FREE_BIASES_2, rl._contextFreeClassifier._cellBiases2 );

//        setData( CONTEXT_FREE_WEIGHTS_VELOCITY, rl._contextFreeClassifier._cellWeightsVelocity );
//        setData( CONTEXT_FREE_BIASES_1_VELOCITY, rl._contextFreeClassifier._cellBiases1Velocity );
//        setData( CONTEXT_FREE_BIASES_2_VELOCITY, rl._contextFreeClassifier._cellBiases2Velocity );

        setData( CONTEXT_FREE_ERRORS, rl._contextFreeClassifier._cellErrors );
        setData( CONTEXT_FREE_WEIGHTED_SUM, rl._contextFreeClassifier._cellWeightedSum );
        setData( CONTEXT_FREE_TRANSFER, rl._contextFreeClassifier._cellTransfer );
        setData( CONTEXT_FREE_RESPONSE, rl._contextFreeClassifier._cellTransferTopK );
        setData( CONTEXT_FREE_RECONSTRUCTION, rl._contextFreeClassifier._inputReconstruction );
        setData( CONTEXT_FREE_AGES, rl._contextFreeClassifier._cellAges );
        setData( CONTEXT_FREE_RATES, rl._contextFreeClassifier._cellRates );
        setData( CONTEXT_FREE_PROMOTION, rl._contextFreeClassifier._cellPromotion );
        setData( CONTEXT_FREE_INHIBITION, rl._contextFreeClassifier._cellInhibition );

//        setData( CONTEXTUAL_WEIGHTS, rl._contextualClassifier._cellWeights );
//        setData( CONTEXTUAL_BIASES_1, rl._contextualClassifier._cellBiases1 );
//        setData( CONTEXTUAL_BIASES_2, rl._contextualClassifier._cellBiases2 );
//        setData( CONTEXTUAL_ERRORS, rl._contextualClassifier._cellErrors );
//        setData( CONTEXTUAL_RESPONSE, rl._contextualClassifier._cellResponse );
//        setData( CONTEXTUAL_RECONSTRUCTION, rl._contextualClassifier._inputReconstruction );

    }

}
