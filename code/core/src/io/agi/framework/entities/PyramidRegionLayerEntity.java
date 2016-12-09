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
import io.agi.core.alg.PyramidRegionLayer;
import io.agi.core.alg.PyramidRegionLayerConfig;
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
public class PyramidRegionLayerEntity extends Entity {

    public static final String ENTITY_TYPE = "pyramid-region-layer";

    public static final String INPUT_C1 = "input-c1";
    public static final String INPUT_C2 = "input-c2";
    public static final String INPUT_P1 = "input-p1";
    public static final String INPUT_P2 = "input-p2";

    public static final String INPUT_OLD = "input-old";
    public static final String INPUT_NEW = "input-new";

    public static final String PREDICTION_ERROR_FP  = "prediction-error-fp";
    public static final String PREDICTION_ERROR_FN  = "prediction-error-fn";
    public static final String PREDICTION_OLD = "prediction-old";
    public static final String PREDICTION_NEW = "prediction-new";
    public static final String PREDICTION_NEW_REAL = "prediction-new-real";

    public static final String PREDICTOR_WEIGHTS = "predictor-weights";
//    public static final String PREDICTOR_OUTPUT_TRACES = "predictor-output-traces";
//    public static final String PREDICTOR_INPUT_OUTPUT_TRACES = "predictor-input-output-traces";

    public static final String CLASSIFIER_SPIKES_OLD = "classifier-spikes-old";
    public static final String CLASSIFIER_SPIKES_NEW = "classifier-spikes-new";
//    public static final String CLASSIFIER_SPIKES_INTEGRATED = "classifier-spikes-integrated";

    public static final String OUTPUT_SPIKES_OLD = "output-spikes-old";
    public static final String OUTPUT_SPIKES_NEW = "output-spikes-new";
    public static final String OUTPUT_SPIKES_AGE = "output-spikes-age";

    public static final String CLASSIFIER_WEIGHTS = "classifier-weights";
    public static final String CLASSIFIER_BIASES_1 = "classifier-biases-1";
    public static final String CLASSIFIER_BIASES_2 = "classifier-biases-2";
    public static final String CLASSIFIER_MASK = "classifier-mask";
    public static final String CLASSIFIER_ERRORS = "classifier-errors";
    public static final String CLASSIFIER_WEIGHTED_SUM = "classifier-weighted-sum";
    public static final String CLASSIFIER_TRANSFER = "classifier-transfer";
    public static final String CLASSIFIER_RESPONSE = "classifier-response";
    public static final String CLASSIFIER_RECONSTRUCTION = "classifier-reconstruction";
    public static final String CLASSIFIER_AGES = "classifier-ages";
    public static final String CLASSIFIER_RATES = "classifier-rates";
    public static final String CLASSIFIER_PROMOTION = "classifier-promotion";
    public static final String CLASSIFIER_INHIBITION = "classifier-inhibition";

    public PyramidRegionLayerEntity( ObjectMap om, Node n, ModelEntity model ) {
        super( om, n, model );
    }

    public void getInputAttributes( Collection< String > attributes ) {
        attributes.add( INPUT_C1 );
        attributes.add( INPUT_C2 );
        attributes.add( INPUT_P1 );
        attributes.add( INPUT_P2 );
    }

    public void getOutputAttributes( Collection< String > attributes, DataFlags flags ) {

        attributes.add( INPUT_OLD );
        attributes.add( INPUT_NEW );

        flags.putFlag( INPUT_OLD, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( INPUT_NEW, DataFlags.FLAG_NODE_CACHE );

        flags.putFlag( INPUT_OLD, DataFlags.FLAG_SPARSE_BINARY );
        flags.putFlag( INPUT_NEW, DataFlags.FLAG_SPARSE_BINARY );

        // cell state
        attributes.add( CLASSIFIER_SPIKES_OLD );
        attributes.add( CLASSIFIER_SPIKES_NEW );
//        attributes.add( CLASSIFIER_SPIKES_INTEGRATED );
        attributes.add( OUTPUT_SPIKES_AGE );

        flags.putFlag( CLASSIFIER_SPIKES_OLD, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( CLASSIFIER_SPIKES_NEW, DataFlags.FLAG_NODE_CACHE );
//        flags.putFlag( CLASSIFIER_SPIKES_INTEGRATED, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( OUTPUT_SPIKES_AGE, DataFlags.FLAG_NODE_CACHE );

        flags.putFlag( CLASSIFIER_SPIKES_OLD, DataFlags.FLAG_SPARSE_BINARY );
        flags.putFlag( CLASSIFIER_SPIKES_NEW, DataFlags.FLAG_SPARSE_BINARY );

        attributes.add( OUTPUT_SPIKES_OLD );
        attributes.add( OUTPUT_SPIKES_NEW );

        flags.putFlag( OUTPUT_SPIKES_OLD, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( OUTPUT_SPIKES_NEW, DataFlags.FLAG_NODE_CACHE );

        flags.putFlag( OUTPUT_SPIKES_OLD, DataFlags.FLAG_SPARSE_BINARY );
        flags.putFlag( OUTPUT_SPIKES_NEW, DataFlags.FLAG_SPARSE_BINARY );

        // predictor
        attributes.add( PREDICTION_ERROR_FP );
        attributes.add( PREDICTION_ERROR_FN );
        attributes.add( PREDICTION_OLD );
        attributes.add( PREDICTION_NEW );
        attributes.add( PREDICTION_NEW_REAL );

        flags.putFlag( PREDICTION_ERROR_FP, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( PREDICTION_ERROR_FN, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( PREDICTION_OLD, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( PREDICTION_NEW, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( PREDICTION_NEW_REAL, DataFlags.FLAG_NODE_CACHE );

        flags.putFlag( PREDICTION_OLD, DataFlags.FLAG_SPARSE_BINARY );
        flags.putFlag( PREDICTION_NEW, DataFlags.FLAG_SPARSE_BINARY );

        attributes.add( PREDICTOR_WEIGHTS );
//        attributes.add( PREDICTOR_OUTPUT_TRACES );
//        attributes.add( PREDICTOR_INPUT_OUTPUT_TRACES );

        flags.putFlag( PREDICTOR_WEIGHTS, DataFlags.FLAG_NODE_CACHE );
//        flags.putFlag( PREDICTOR_OUTPUT_TRACES, DataFlags.FLAG_NODE_CACHE );
//        flags.putFlag( PREDICTOR_INPUT_OUTPUT_TRACES, DataFlags.FLAG_NODE_CACHE );

        flags.putFlag( PREDICTOR_WEIGHTS, DataFlags.FLAG_PERSIST_ON_FLUSH );
////        flags.putFlag( PREDICTOR_OUTPUT_TRACES, DataFlags.FLAG_PERSIST_ON_FLUSH );
//        flags.putFlag( PREDICTOR_INPUT_OUTPUT_TRACES, DataFlags.FLAG_PERSIST_ON_FLUSH );

        // K sparse autoencoder data
        attributes.add( CLASSIFIER_WEIGHTS );
        attributes.add( CLASSIFIER_BIASES_1 );
        attributes.add( CLASSIFIER_BIASES_2 );
        attributes.add( CLASSIFIER_MASK );
        attributes.add( CLASSIFIER_ERRORS );
        attributes.add( CLASSIFIER_TRANSFER );
        attributes.add( CLASSIFIER_RESPONSE );
        attributes.add( CLASSIFIER_WEIGHTED_SUM );
        attributes.add( CLASSIFIER_RECONSTRUCTION );
        attributes.add( CLASSIFIER_AGES );
        attributes.add( CLASSIFIER_RATES );
        attributes.add( CLASSIFIER_PROMOTION );
        attributes.add( CLASSIFIER_INHIBITION );

        flags.putFlag( CLASSIFIER_WEIGHTS, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( CLASSIFIER_BIASES_1, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( CLASSIFIER_BIASES_2, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( CLASSIFIER_MASK, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( CLASSIFIER_ERRORS, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( CLASSIFIER_TRANSFER, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( CLASSIFIER_RESPONSE, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( CLASSIFIER_RECONSTRUCTION, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( CLASSIFIER_AGES, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( CLASSIFIER_RATES, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( CLASSIFIER_PROMOTION, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( CLASSIFIER_INHIBITION, DataFlags.FLAG_NODE_CACHE );
    }

    @Override
    public Class getConfigClass() {
        return PyramidRegionLayerEntityConfig.class;
    }

    protected void doUpdateSelf() {

        PyramidRegionLayerEntityConfig config = ( PyramidRegionLayerEntityConfig ) _config;

        // Do nothing unless the input is defined
        Data inputC1 = getData( INPUT_C1 );
        Data inputC2 = getData( INPUT_C2 );
        Data inputP1 = getData( INPUT_P1 );
        Data inputP2 = getData( INPUT_P2 );

        if( ( inputC1 == null ) || ( inputC2 == null ) || ( inputP1 == null ) || ( inputP2 == null ) ) {
            // we need to produce our output even if we can't write to it yet, to allow circular dependencies to be formed.
            Data outputSpikes = new Data( config.widthCells, config.heightCells );
            setData( OUTPUT_SPIKES_OLD, outputSpikes );
            setData( OUTPUT_SPIKES_NEW, outputSpikes );

            if( config.reset ) {
                config.resetDelayed = true;
            }
            return; // can't update yet.
        }

        // Get all the parameters:
        String regionLayerName = getName();

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Test parameters
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Feedforward size
        Point inputC1Size = Data2d.getSize( inputC1 );
        Point inputC2Size = Data2d.getSize( inputC2 );
        Point inputP1Size = Data2d.getSize( inputP1 );
        Point inputP2Size = Data2d.getSize( inputP2 );

        int inputC1Width  = inputC1Size.x;
        int inputC1Height = inputC1Size.y;
        int inputC2Width  = inputC2Size.x;
        int inputC2Height = inputC2Size.y;

        int inputP1Width  = inputP1Size.x;
        int inputP1Height = inputP1Size.y;
        int inputP2Width  = inputP2Size.x;
        int inputP2Height = inputP2Size.y;

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Algorithm specific parameters
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Build the algorithm
        ObjectMap om = ObjectMap.GetInstance();

        PyramidRegionLayerConfig rlc = new PyramidRegionLayerConfig();
        rlc.setup(
            om, regionLayerName, _r,
            inputC1Width, inputC1Height,
            inputC2Width, inputC2Height,
            inputP1Width, inputP1Height,
            inputP2Width, inputP2Height,
            config.widthCells, config.heightCells,
            config.classifierLearningRate,
            config.classifierSparsity,// k, the number of active cells each step
            config.classifierSparsityOutput, // a factor determining the output sparsity
            config.classifierAgeMin,
            config.classifierAgeMax,
            config.classifierAgeScale,
            config.classifierRateScale,
            config.classifierRateMax,
            config.classifierRateLearningRate,
            config.spikeOutputAgeMax,
//            config.integrationDecayRate,
//            config.integrationSpikeWeight,
            config.predictorLearningRate );//,
//            config.predictorTraceDecayRate ) ;

        PyramidRegionLayer rl = new PyramidRegionLayer( regionLayerName, om );
        rl.setup( rlc );

        copyDataFromPersistence( rl );

        if( config.reset || config.resetDelayed ) {
            rl.reset();
            config.resetDelayed = false;
        }

        rl._rc.setLearn( config.learn );
        rl.update();

        // Save config changes caused by the algorithm
        copyConfigChangesToPersistence( rl, config );

        // Save data changes
        copyDataToPersistence( rl );
    }

    protected void copyConfigChangesToPersistence( PyramidRegionLayer rl, PyramidRegionLayerEntityConfig config ) {

        config.sumClassifierError = rl._sumClassifierError;
        config.sumClassifierResponse = rl._sumClassifierResponse;
        config.sumOutputSpikes = rl._sumOutputSpikes;
        config.sumPredictionErrorFP = rl._sumPredictionErrorFP;
        config.sumPredictionErrorFN = rl._sumPredictionErrorFN;
        config.sumIntegration = rl._sumIntegration;
    }

    protected void copyDataFromPersistence( PyramidRegionLayer rl ) {

        rl._inputC1 = getData( INPUT_C1 );
        rl._inputC2 = getData( INPUT_C2 );
        rl._inputP1 = getData( INPUT_P1 );
        rl._inputP2 = getData( INPUT_P2 );

        rl._inputOld = getDataLazyResize( INPUT_OLD, rl._inputOld._dataSize );
        rl._inputNew = getDataLazyResize( INPUT_NEW, rl._inputNew._dataSize );

        rl._spikesOld = getDataLazyResize( CLASSIFIER_SPIKES_OLD, rl._spikesOld._dataSize );
        rl._spikesNew = getDataLazyResize( CLASSIFIER_SPIKES_NEW, rl._spikesNew._dataSize );
//        rl._spikesIntegrated = getDataLazyResize( CLASSIFIER_SPIKES_INTEGRATED, rl._spikesIntegrated._dataSize );

        rl._outputSpikesOld = getDataLazyResize( OUTPUT_SPIKES_OLD, rl._outputSpikesOld._dataSize );
        rl._outputSpikesNew = getDataLazyResize( OUTPUT_SPIKES_NEW, rl._outputSpikesNew._dataSize );
        rl._outputSpikesAge = getDataLazyResize( OUTPUT_SPIKES_AGE, rl._outputSpikesAge._dataSize );

        rl._predictionErrorFP = getDataLazyResize( PREDICTION_ERROR_FP, rl._predictionErrorFP._dataSize );
        rl._predictionErrorFN = getDataLazyResize( PREDICTION_ERROR_FN, rl._predictionErrorFN._dataSize );
        rl._predictionOld = getDataLazyResize( PREDICTION_OLD, rl._predictionOld._dataSize );
        rl._predictionNew = getDataLazyResize( PREDICTION_NEW, rl._predictionNew._dataSize );
        rl._predictionNewReal = getDataLazyResize( PREDICTION_NEW_REAL, rl._predictionNewReal._dataSize );

        rl._predictor._weights = getDataLazyResize( PREDICTOR_WEIGHTS, rl._predictor._weights._dataSize );
//        rl._predictor._outputTraces = getDataLazyResize( PREDICTOR_OUTPUT_TRACES, rl._predictor._outputTraces._dataSize );
//        rl._predictor._inputOutputTraces = getDataLazyResize( PREDICTOR_INPUT_OUTPUT_TRACES, rl._predictor._inputOutputTraces._dataSize );

        rl._classifier._cellWeights = getDataLazyResize( CLASSIFIER_WEIGHTS, rl._classifier._cellWeights._dataSize );
        rl._classifier._cellBiases1 = getDataLazyResize( CLASSIFIER_BIASES_1, rl._classifier._cellBiases1._dataSize );
        rl._classifier._cellBiases2 = getDataLazyResize( CLASSIFIER_BIASES_2, rl._classifier._cellBiases2._dataSize );
        rl._classifier._cellMask = getDataLazyResize( CLASSIFIER_MASK, rl._classifier._cellMask._dataSize );
        rl._classifier._cellErrors = getDataLazyResize( CLASSIFIER_ERRORS, rl._classifier._cellErrors._dataSize );
        rl._classifier._cellWeightedSum = getDataLazyResize( CLASSIFIER_WEIGHTED_SUM, rl._classifier._cellWeightedSum._dataSize );
        rl._classifier._cellTransfer = getDataLazyResize( CLASSIFIER_TRANSFER, rl._classifier._cellTransfer._dataSize );
        rl._classifier._cellTransferTopK = getDataLazyResize( CLASSIFIER_RESPONSE, rl._classifier._cellTransferTopK._dataSize );
        rl._classifier._inputReconstruction = getDataLazyResize( CLASSIFIER_RECONSTRUCTION, rl._classifier._inputReconstruction._dataSize );
        rl._classifier._cellAges = getDataLazyResize( CLASSIFIER_AGES, rl._classifier._cellAges._dataSize );
        rl._classifier._cellRates = getDataLazyResize( CLASSIFIER_RATES, rl._classifier._cellRates._dataSize );
    }

    protected void copyDataToPersistence( PyramidRegionLayer rl ) {

        setData( INPUT_OLD, rl._inputOld );
        setData( INPUT_NEW, rl._inputNew );

        setData( CLASSIFIER_SPIKES_OLD, rl._spikesOld );
        setData( CLASSIFIER_SPIKES_NEW, rl._spikesNew );
//        setData( CLASSIFIER_SPIKES_INTEGRATED, rl._spikesIntegrated );

        setData( OUTPUT_SPIKES_OLD, rl._outputSpikesOld );
        setData( OUTPUT_SPIKES_NEW, rl._outputSpikesNew );
        setData( OUTPUT_SPIKES_AGE, rl._outputSpikesAge );

        setData( PREDICTION_ERROR_FP, rl._predictionErrorFP );
        setData( PREDICTION_ERROR_FN, rl._predictionErrorFN );
        setData( PREDICTION_OLD, rl._predictionOld );
        setData( PREDICTION_NEW, rl._predictionNew );
        setData( PREDICTION_NEW_REAL, rl._predictionNewReal );

        setData( PREDICTOR_WEIGHTS, rl._predictor._weights );
//        setData( PREDICTOR_OUTPUT_TRACES, rl._predictor._outputTraces );
//        setData( PREDICTOR_INPUT_OUTPUT_TRACES, rl._predictor._inputOutputTraces );

        setData( CLASSIFIER_WEIGHTS, rl._classifier._cellWeights );
        setData( CLASSIFIER_BIASES_1, rl._classifier._cellBiases1 );
        setData( CLASSIFIER_BIASES_2, rl._classifier._cellBiases2 );

        setData( CLASSIFIER_MASK, rl._classifier._cellMask );
        setData( CLASSIFIER_ERRORS, rl._classifier._cellErrors );
        setData( CLASSIFIER_WEIGHTED_SUM, rl._classifier._cellWeightedSum );
        setData( CLASSIFIER_TRANSFER, rl._classifier._cellTransfer );
        setData( CLASSIFIER_RESPONSE, rl._classifier._cellTransferTopK );
        setData( CLASSIFIER_RECONSTRUCTION, rl._classifier._inputReconstruction );
        setData( CLASSIFIER_AGES, rl._classifier._cellAges );
        setData( CLASSIFIER_RATES, rl._classifier._cellRates );
        setData( CLASSIFIER_PROMOTION, rl._classifier._cellPromotion );
        setData( CLASSIFIER_INHIBITION, rl._classifier._cellInhibition );
    }

}
