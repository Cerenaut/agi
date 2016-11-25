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
import io.agi.core.alg.ConsensusRegionLayer;
import io.agi.core.alg.ConsensusRegionLayerConfig;
import io.agi.core.ann.unsupervised.ConsensusAutoencoderConfig;
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
public class ConsensusRegionLayerEntity extends Entity {

    public static final String ENTITY_TYPE = "consensus-region-layer";

    public static final String INPUT_1 = "input-1";
    public static final String INPUT_2 = "input-2";

    public static final String CONTEXT_FREE_ACTIVITY     = "context-free-activity";
    public static final String CONTEXT_FREE_ACTIVITY_OLD = "context-free-activity-old";
    public static final String CONTEXT_FREE_ACTIVITY_NEW = "context-free-activity-new";

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

    public static final String CONTEXT_FREE_ERRORS = "context-free-errors";
    public static final String CONTEXT_FREE_WEIGHTED_SUM = "context-free-weighted-sum";
    public static final String CONTEXT_FREE_TRANSFER = "context-free-transfer";
    public static final String CONTEXT_FREE_RESPONSE = "context-free-response";
    public static final String CONTEXT_FREE_RECONSTRUCTION = "context-free-reconstruction";
    public static final String CONTEXT_FREE_AGES = "context-free-ages";
    public static final String CONTEXT_FREE_PROMOTION = "context-free-promotion";

    public static final String CONTEXT_FREE_INHIBITION_WEIGHTS = "context-free-inhibition-weights";
    public static final String CONTEXT_FREE_INHIBITION_HISTORY = "context-free-inhibition-history";
    public static final String CONTEXT_FREE_CONSENSUS_HISTORY = "context-free-consensus-history";

    public ConsensusRegionLayerEntity( ObjectMap om, Node n, ModelEntity model ) {
        super( om, n, model );
    }

    public void getInputAttributes( Collection< String > attributes ) {
        attributes.add( INPUT_1 );
        attributes.add( INPUT_2 );
    }

    public void getOutputAttributes( Collection< String > attributes, DataFlags flags ) {

        attributes.add( CONTEXT_FREE_ACTIVITY );
        attributes.add( CONTEXT_FREE_ACTIVITY_OLD );
        attributes.add( CONTEXT_FREE_ACTIVITY_NEW );

        flags.putFlag( CONTEXT_FREE_ACTIVITY, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( CONTEXT_FREE_ACTIVITY_OLD, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( CONTEXT_FREE_ACTIVITY_NEW, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( CONTEXT_FREE_ACTIVITY, DataFlags.FLAG_SPARSE_BINARY );
        flags.putFlag( CONTEXT_FREE_ACTIVITY_OLD, DataFlags.FLAG_SPARSE_BINARY );
        flags.putFlag( CONTEXT_FREE_ACTIVITY_NEW, DataFlags.FLAG_SPARSE_BINARY );

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

        attributes.add( CONTEXT_FREE_ERRORS );
        attributes.add( CONTEXT_FREE_TRANSFER );
        attributes.add( CONTEXT_FREE_RESPONSE );
        attributes.add( CONTEXT_FREE_WEIGHTED_SUM );
        attributes.add( CONTEXT_FREE_RECONSTRUCTION );
        attributes.add( CONTEXT_FREE_AGES );
        attributes.add( CONTEXT_FREE_PROMOTION );

        flags.putFlag( CONTEXT_FREE_WEIGHTS, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( CONTEXT_FREE_BIASES_1, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( CONTEXT_FREE_BIASES_2, DataFlags.FLAG_NODE_CACHE );

        flags.putFlag( CONTEXT_FREE_ERRORS, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( CONTEXT_FREE_TRANSFER, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( CONTEXT_FREE_RESPONSE, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( CONTEXT_FREE_RECONSTRUCTION, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( CONTEXT_FREE_AGES, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( CONTEXT_FREE_PROMOTION, DataFlags.FLAG_NODE_CACHE );

        attributes.add( CONTEXT_FREE_INHIBITION_WEIGHTS );
        attributes.add( CONTEXT_FREE_INHIBITION_HISTORY );
        attributes.add( CONTEXT_FREE_CONSENSUS_HISTORY );

        flags.putFlag( CONTEXT_FREE_INHIBITION_WEIGHTS, DataFlags.FLAG_NODE_CACHE );
    }

    @Override
    public Class getConfigClass() {
        return ConsensusRegionLayerEntityConfig.class;
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
        ConsensusRegionLayerEntityConfig config = ( ConsensusRegionLayerEntityConfig ) _config;

        // Build the algorithm
        //RandomInstance.setSeed(randomSeed); // make the tests repeatable
        ObjectMap om = ObjectMap.GetInstance();

        ConsensusAutoencoderConfig contextFreeConfig = new ConsensusAutoencoderConfig();

        String contextFreeName = getKey( ConsensusRegionLayerConfig.SUFFIX_CONTEXT_FREE );

        contextFreeConfig.setup(
                om, contextFreeName, _r,
                inputArea, config.contextFreeWidthCells, config.contextFreeHeightCells, config.contextFreeLearningRate,
                config.contextFreeBinaryOutput,
                config.contextFreeSparsityOutput, config.contextFreeSparsity, config.contextFreeSparsityMin, config.contextFreeSparsityMax,
                config.contextFreeAgeMin, config.contextFreeAgeMax, config.contextFreeAge, config.contextFreeAgeScale,
                config.consensusLearningRate, config.consensusDecayRate, config.consensusStrength, config.consensusSteps );

        ConsensusRegionLayerConfig rlc = new ConsensusRegionLayerConfig();
        rlc.setup(
            om, regionLayerName, _r,
            contextFreeConfig,
            input1Width, input1Height,
            input2Width, input2Height,
            config.outputSparsity, config.predictorLearningRate, config.defaultPredictionInhibition );

        ConsensusRegionLayer rl = new ConsensusRegionLayer( regionLayerName, om );
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
        config.contextFreeAge = contextFreeConfig.getAge();
        copyDataToPersistence( rl );
    }

    protected void copyDataFromPersistence( ConsensusRegionLayer rl ) {

        rl._input1 = getData( INPUT_1 );
        rl._input2 = getData( INPUT_2 );

        rl._contextFreeActivity = getDataLazyResize( CONTEXT_FREE_ACTIVITY, rl._contextFreeActivity._dataSize );
        rl._contextFreeActivityOld = getDataLazyResize( CONTEXT_FREE_ACTIVITY_OLD, rl._contextFreeActivityOld._dataSize );
        rl._contextFreeActivityNew = getDataLazyResize( CONTEXT_FREE_ACTIVITY_NEW, rl._contextFreeActivityNew._dataSize );

        rl._predictionFP = getDataLazyResize( PREDICTION_FP, rl._predictionFP._dataSize );
        rl._predictionFN = getDataLazyResize( PREDICTION_FN, rl._predictionFN._dataSize );
        rl._predictionOld = getDataLazyResize( PREDICTION_OLD, rl._predictionOld._dataSize );
        rl._predictionNew = getDataLazyResize( PREDICTION_NEW, rl._predictionNew._dataSize );
        rl._predictionNewReal = getDataLazyResize( PREDICTION_NEW_REAL, rl._predictionNewReal._dataSize );
        rl._predictionInhibition = getDataLazyResize( PREDICTION_INHIBITION, rl._predictionInhibition._dataSize );

        rl._output = getDataLazyResize( OUTPUT, rl._output._dataSize );
        rl._outputAge = getDataLazyResize( OUTPUT_AGE, rl._outputAge._dataSize );

        rl._contextFreeClassifier._cellWeights = getDataLazyResize( CONTEXT_FREE_WEIGHTS, rl._contextFreeClassifier._cellWeights._dataSize );
        rl._contextFreeClassifier._cellBiases1 = getDataLazyResize( CONTEXT_FREE_BIASES_1, rl._contextFreeClassifier._cellBiases1._dataSize );
        rl._contextFreeClassifier._cellBiases2 = getDataLazyResize( CONTEXT_FREE_BIASES_2, rl._contextFreeClassifier._cellBiases2._dataSize );

        rl._contextFreeClassifier._cellErrors = getDataLazyResize( CONTEXT_FREE_ERRORS, rl._contextFreeClassifier._cellErrors._dataSize );
        rl._contextFreeClassifier._cellWeightedSum = getDataLazyResize( CONTEXT_FREE_WEIGHTED_SUM, rl._contextFreeClassifier._cellWeightedSum._dataSize );
        rl._contextFreeClassifier._cellTransfer = getDataLazyResize( CONTEXT_FREE_TRANSFER, rl._contextFreeClassifier._cellTransfer._dataSize );
        rl._contextFreeClassifier._cellTransferTopK = getDataLazyResize( CONTEXT_FREE_RESPONSE, rl._contextFreeClassifier._cellTransferTopK._dataSize );
        rl._contextFreeClassifier._inputReconstruction = getDataLazyResize( CONTEXT_FREE_RECONSTRUCTION, rl._contextFreeClassifier._inputReconstruction._dataSize );
        rl._contextFreeClassifier._cellAges = getDataLazyResize( CONTEXT_FREE_AGES, rl._contextFreeClassifier._cellAges._dataSize );
        rl._contextFreeClassifier._cellPromotion = getDataLazyResize( CONTEXT_FREE_PROMOTION, rl._contextFreeClassifier._cellPromotion._dataSize );

        rl._contextFreeClassifier._cellInhibitionWeights = getDataLazyResize( CONTEXT_FREE_INHIBITION_WEIGHTS, rl._contextFreeClassifier._cellInhibitionWeights._dataSize );
        rl._contextFreeClassifier._inhibitionHistory = getDataLazyResize( CONTEXT_FREE_INHIBITION_HISTORY, rl._contextFreeClassifier._inhibitionHistory._dataSize );
        rl._contextFreeClassifier._concensusHistory = getDataLazyResize( CONTEXT_FREE_CONSENSUS_HISTORY, rl._contextFreeClassifier._concensusHistory._dataSize );
    }

    protected void copyDataToPersistence( ConsensusRegionLayer rl ) {

        setData( CONTEXT_FREE_ACTIVITY, rl._contextFreeActivity );
        setData( CONTEXT_FREE_ACTIVITY_OLD, rl._contextFreeActivityOld );
        setData( CONTEXT_FREE_ACTIVITY_NEW, rl._contextFreeActivityNew );

        setData( PREDICTION_FP, rl._predictionFP );
        setData( PREDICTION_FN, rl._predictionFN );
        setData( PREDICTION_OLD, rl._predictionOld );
        setData( PREDICTION_NEW, rl._predictionNew );
        setData( PREDICTION_NEW_REAL, rl._predictionNewReal );
        setData( PREDICTION_INHIBITION, rl._predictionInhibition );

        setData( OUTPUT, rl._output );
        setData( OUTPUT_AGE, rl._outputAge );

        setData( CONTEXT_FREE_WEIGHTS, rl._contextFreeClassifier._cellWeights );
        setData( CONTEXT_FREE_BIASES_1, rl._contextFreeClassifier._cellBiases1 );
        setData( CONTEXT_FREE_BIASES_2, rl._contextFreeClassifier._cellBiases2 );

        setData( CONTEXT_FREE_ERRORS, rl._contextFreeClassifier._cellErrors );
        setData( CONTEXT_FREE_WEIGHTED_SUM, rl._contextFreeClassifier._cellWeightedSum );
        setData( CONTEXT_FREE_TRANSFER, rl._contextFreeClassifier._cellTransfer );
        setData( CONTEXT_FREE_RESPONSE, rl._contextFreeClassifier._cellTransferTopK );
        setData( CONTEXT_FREE_RECONSTRUCTION, rl._contextFreeClassifier._inputReconstruction );
        setData( CONTEXT_FREE_AGES, rl._contextFreeClassifier._cellAges );
        setData( CONTEXT_FREE_PROMOTION, rl._contextFreeClassifier._cellPromotion );

        setData( CONTEXT_FREE_INHIBITION_WEIGHTS, rl._contextFreeClassifier._cellInhibitionWeights );
        setData( CONTEXT_FREE_INHIBITION_HISTORY, rl._contextFreeClassifier._inhibitionHistory );
        setData( CONTEXT_FREE_CONSENSUS_HISTORY, rl._contextFreeClassifier._concensusHistory );

    }

}
