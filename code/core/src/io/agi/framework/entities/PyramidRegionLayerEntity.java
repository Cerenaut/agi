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

import io.agi.core.alg.PyramidRegionLayer;
import io.agi.core.alg.PyramidRegionLayerConfig;
import io.agi.core.ann.supervised.NetworkLayer;
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

//    public static final String INPUT_C1 = "input-c1";
//    public static final String INPUT_C2 = "input-c2";
    public static final String INPUT_C = "input-c";
//    public static final String INPUT_C_PREDICTED = "input-c-predicted";
//    public static final String INPUT_C1_PREDICTED = "input-c1-predicted";
//    public static final String INPUT_C2_PREDICTED = "input-c2-predicted";
//    public static final String INPUT_P1 = "input-p1";
//    public static final String INPUT_P2 = "input-p2";
    public static final String INPUT_P = "input-p";

    public static final String INPUT_P_OLD = "input-p-old"; // combined
    public static final String INPUT_P_NEW = "input-p-new";

    public static final String PREDICTION_ERROR_FP  = "prediction-error-fp";
    public static final String PREDICTION_ERROR_FN  = "prediction-error-fn";
    public static final String PREDICTION_OLD = "prediction-old";
    public static final String PREDICTION_NEW = "prediction-new";
    public static final String PREDICTION_NEW_UNIT = "prediction-new-unit";

    public static final String PREDICTOR_IDEALS = "predictor-ideals";
    public static final String PREDICTOR_WEIGHTS_1 = "predictor-weights-1";
    public static final String PREDICTOR_WEIGHTS_2 = "predictor-weights-2";
    public static final String PREDICTOR_BIASES_1 = "predictor-biases-1";
    public static final String PREDICTOR_BIASES_2 = "predictor-biases-2";
    public static final String ERROR_GRADIENTS_1 = "error-gradients-1";
    public static final String ERROR_GRADIENTS_2 = "error-gradients-2";

//    public static final String PREDICTOR_WEIGHTS = "predictor-weights";

//    public static final String CLASSIFIER_SPIKES_OLD = "classifier-spikes-old";
//    public static final String CLASSIFIER_SPIKES_NEW = "classifier-spikes-new";
//    public static final String CLASSIFIER_SPIKES_INTEGRATED = "classifier-spikes-integrated";

    public static final String OUTPUT_SPIKES_OLD = "output-spikes-old";
    public static final String OUTPUT_SPIKES_NEW = "output-spikes-new";
    public static final String OUTPUT = "output";
    public static final String OUTPUT_SPIKES_AGE = "output-spikes-age";

//    public static final String CLASSIFIER_WEIGHTS = "classifier-weights";
//    public static final String CLASSIFIER_BIASES_1 = "classifier-biases-1";
//    public static final String CLASSIFIER_BIASES_2 = "classifier-biases-2";
//
//    public static final String CLASSIFIER_ERRORS = "classifier-errors";
//    public static final String CLASSIFIER_WEIGHTED_SUM = "classifier-weighted-sum";
//    public static final String CLASSIFIER_TRANSFER = "classifier-transfer";
//    public static final String CLASSIFIER_RESPONSE = "classifier-response";
//    public static final String CLASSIFIER_AGES = "classifier-ages";
//    public static final String CLASSIFIER_RATES = "classifier-rates";
//    public static final String CLASSIFIER_PROMOTION = "classifier-promotion";
//    public static final String CLASSIFIER_INHIBITION = "classifier-inhibition";
//
//    public static final String CLASSIFIER_WEIGHTS_VELOCITY = "classifier-weights-velocity";
//    public static final String CLASSIFIER_BIASES_1_VELOCITY = "classifier-biases-1-velocity";
//    public static final String CLASSIFIER_BIASES_2_VELOCITY = "classifier-biases-2-velocity";
//
//    public static final String CLASSIFIER_TRANSFER_PROMOTED = "classifier-transfer-promoted";
//    public static final String CLASSIFIER_TRANSFER_TOP_K = "classifier-transfer-top-k";
//    public static final String CLASSIFIER_TRANSFER_TOP_KA = "classifier-transfer-top-ka";
//
//    public static final String CLASSIFIER_SPIKES_TOP_K = "classifier-spikes-top-k";
//    public static final String CLASSIFIER_SPIKES_TOP_KA = "classifier-spikes-top-ka";
//
//    public static final String CLASSIFIER_RECONSTRUCTION_WEIGHTED_SUM = "classifier-reconstruction-weighted-sum";
//    public static final String CLASSIFIER_RECONSTRUCTION_TRANSFER = "classifier-reconstruction-transfer";
//
//    public static final String CLASSIFIER_OUTPUT_GRADIENTS = "classifier-output-gradients";
//    public static final String CLASSIFIER_HIDDEN_GRADIENTS = "classifier-hidden-gradients";

    public PyramidRegionLayerEntity( ObjectMap om, Node n, ModelEntity model ) {
        super( om, n, model );
    }

    public void getInputAttributes( Collection< String > attributes ) {
//        attributes.add( INPUT_C1 );
//        attributes.add( INPUT_C2 );
        attributes.add( INPUT_C );
//        attributes.add( INPUT_P1 );
//        attributes.add( INPUT_P2 );
        attributes.add( INPUT_P );
    }

    public void getOutputAttributes( Collection< String > attributes, DataFlags flags ) {

//        attributes.add( INPUT_C1_PREDICTED );
//        attributes.add( INPUT_C2_PREDICTED );
//        attributes.add( INPUT_C_PREDICTED );

//        flags.putFlag( INPUT_C1_PREDICTED, DataFlags.FLAG_NODE_CACHE );
//        flags.putFlag( INPUT_C2_PREDICTED, DataFlags.FLAG_NODE_CACHE );
//        flags.putFlag( INPUT_C_PREDICTED, DataFlags.FLAG_NODE_CACHE );

        attributes.add( INPUT_P_OLD );
        attributes.add( INPUT_P_NEW );

        flags.putFlag( INPUT_P_OLD, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( INPUT_P_NEW, DataFlags.FLAG_NODE_CACHE );

//        flags.putFlag( INPUT_P_OLD, DataFlags.FLAG_SPARSE_BINARY );
//        flags.putFlag( INPUT_NEW, DataFlags.FLAG_SPARSE_BINARY );

        // cell state
//        attributes.add( CLASSIFIER_SPIKES_OLD );
//        attributes.add( CLASSIFIER_SPIKES_NEW );
//        attributes.add( CLASSIFIER_SPIKES_INTEGRATED );
        attributes.add( OUTPUT_SPIKES_AGE );

//        flags.putFlag( CLASSIFIER_SPIKES_OLD, DataFlags.FLAG_NODE_CACHE );
//        flags.putFlag( CLASSIFIER_SPIKES_NEW, DataFlags.FLAG_NODE_CACHE );
////        flags.putFlag( CLASSIFIER_SPIKES_INTEGRATED, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( OUTPUT_SPIKES_AGE, DataFlags.FLAG_NODE_CACHE );
//
//        flags.putFlag( CLASSIFIER_SPIKES_OLD, DataFlags.FLAG_SPARSE_BINARY );
//        flags.putFlag( CLASSIFIER_SPIKES_NEW, DataFlags.FLAG_SPARSE_BINARY );

        attributes.add( OUTPUT_SPIKES_OLD );
        attributes.add( OUTPUT_SPIKES_NEW );
        attributes.add( OUTPUT );

        flags.putFlag( OUTPUT_SPIKES_OLD, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( OUTPUT_SPIKES_NEW, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( OUTPUT, DataFlags.FLAG_NODE_CACHE );

//        flags.putFlag( OUTPUT_SPIKES_OLD, DataFlags.FLAG_SPARSE_BINARY );
//        flags.putFlag( OUTPUT_SPIKES_NEW, DataFlags.FLAG_SPARSE_BINARY );

        // predictor
        attributes.add( PREDICTION_ERROR_FP );
        attributes.add( PREDICTION_ERROR_FN );
        attributes.add(PREDICTION_OLD);
        attributes.add(PREDICTION_NEW);
        attributes.add( PREDICTION_NEW_UNIT );

        flags.putFlag( PREDICTION_ERROR_FP, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( PREDICTION_ERROR_FN, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( PREDICTION_OLD, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( PREDICTION_NEW, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( PREDICTION_NEW_UNIT, DataFlags.FLAG_NODE_CACHE );

//        flags.putFlag( PREDICTION_OLD, DataFlags.FLAG_SPARSE_BINARY );
//        flags.putFlag(PREDICTION_NEW, DataFlags.FLAG_SPARSE_BINARY);

//        attributes.add( PREDICTOR_WEIGHTS );
//        attributes.add( PREDICTOR_OUTPUT_TRACES );
//        attributes.add( PREDICTOR_INPUT_OUTPUT_TRACES );

//        flags.putFlag( PREDICTOR_WEIGHTS, DataFlags.FLAG_NODE_CACHE );
//        flags.putFlag( PREDICTOR_OUTPUT_TRACES, DataFlags.FLAG_NODE_CACHE );
//        flags.putFlag( PREDICTOR_INPUT_OUTPUT_TRACES, DataFlags.FLAG_NODE_CACHE );

//        flags.putFlag( PREDICTOR_WEIGHTS, DataFlags.FLAG_PERSIST_ON_FLUSH );
////        flags.putFlag( PREDICTOR_OUTPUT_TRACES, DataFlags.FLAG_PERSIST_ON_FLUSH );
//        flags.putFlag( PREDICTOR_INPUT_OUTPUT_TRACES, DataFlags.FLAG_PERSIST_ON_FLUSH );

        attributes.add( PREDICTOR_IDEALS );
        attributes.add( PREDICTOR_WEIGHTS_1 );
        attributes.add( PREDICTOR_WEIGHTS_2 );
        attributes.add( PREDICTOR_BIASES_1 );
        attributes.add( PREDICTOR_BIASES_2 );
        attributes.add( ERROR_GRADIENTS_1 );
        attributes.add( ERROR_GRADIENTS_2 );

        // K sparse autoencoder data
//        attributes.add( CLASSIFIER_WEIGHTS );
//        attributes.add( CLASSIFIER_BIASES_1 );
//        attributes.add( CLASSIFIER_BIASES_2 );
//
//        attributes.add( CLASSIFIER_ERRORS );
//        attributes.add( CLASSIFIER_TRANSFER );
//        attributes.add( CLASSIFIER_RESPONSE );
//        attributes.add( CLASSIFIER_WEIGHTED_SUM );
//        attributes.add( CLASSIFIER_AGES );
//        attributes.add( CLASSIFIER_RATES );
//        attributes.add( CLASSIFIER_PROMOTION );
//        attributes.add( CLASSIFIER_INHIBITION );

//        flags.putFlag( CLASSIFIER_WEIGHTS, DataFlags.FLAG_NODE_CACHE );
//        flags.putFlag( CLASSIFIER_BIASES_1, DataFlags.FLAG_NODE_CACHE );
//        flags.putFlag( CLASSIFIER_BIASES_2, DataFlags.FLAG_NODE_CACHE );
//
//        flags.putFlag( CLASSIFIER_ERRORS, DataFlags.FLAG_NODE_CACHE );
//        flags.putFlag( CLASSIFIER_TRANSFER, DataFlags.FLAG_NODE_CACHE );
//        flags.putFlag( CLASSIFIER_RESPONSE, DataFlags.FLAG_NODE_CACHE );
//        flags.putFlag( CLASSIFIER_AGES, DataFlags.FLAG_NODE_CACHE );
//        flags.putFlag( CLASSIFIER_RATES, DataFlags.FLAG_NODE_CACHE );
//        flags.putFlag( CLASSIFIER_PROMOTION, DataFlags.FLAG_NODE_CACHE );
//        flags.putFlag( CLASSIFIER_INHIBITION, DataFlags.FLAG_NODE_CACHE );
//
//        attributes.add( CLASSIFIER_WEIGHTS_VELOCITY ); flags.putFlag( CLASSIFIER_WEIGHTS_VELOCITY, DataFlags.FLAG_NODE_CACHE );
//        attributes.add( CLASSIFIER_BIASES_1_VELOCITY ); flags.putFlag( CLASSIFIER_BIASES_1_VELOCITY, DataFlags.FLAG_NODE_CACHE );
//        attributes.add( CLASSIFIER_BIASES_2_VELOCITY ); flags.putFlag( CLASSIFIER_BIASES_2_VELOCITY, DataFlags.FLAG_NODE_CACHE );
//
//        attributes.add( CLASSIFIER_TRANSFER_PROMOTED ); flags.putFlag( CLASSIFIER_TRANSFER_PROMOTED, DataFlags.FLAG_NODE_CACHE );
//        attributes.add( CLASSIFIER_TRANSFER_TOP_K ); flags.putFlag( CLASSIFIER_TRANSFER_TOP_K, DataFlags.FLAG_NODE_CACHE );
//        attributes.add( CLASSIFIER_TRANSFER_TOP_KA ); flags.putFlag( CLASSIFIER_TRANSFER_TOP_KA, DataFlags.FLAG_NODE_CACHE );
//
//        attributes.add( CLASSIFIER_SPIKES_TOP_K ); flags.putFlag( CLASSIFIER_SPIKES_TOP_K, DataFlags.FLAG_NODE_CACHE );
//        attributes.add( CLASSIFIER_SPIKES_TOP_KA ); flags.putFlag( CLASSIFIER_SPIKES_TOP_KA, DataFlags.FLAG_NODE_CACHE );
//
//        attributes.add( CLASSIFIER_RECONSTRUCTION_WEIGHTED_SUM ); flags.putFlag( CLASSIFIER_RECONSTRUCTION_WEIGHTED_SUM, DataFlags.FLAG_NODE_CACHE );
//        attributes.add( CLASSIFIER_RECONSTRUCTION_TRANSFER ); flags.putFlag( CLASSIFIER_RECONSTRUCTION_TRANSFER, DataFlags.FLAG_NODE_CACHE );
//
//        attributes.add( CLASSIFIER_OUTPUT_GRADIENTS ); flags.putFlag( CLASSIFIER_OUTPUT_GRADIENTS, DataFlags.FLAG_NODE_CACHE );
//        attributes.add( CLASSIFIER_HIDDEN_GRADIENTS ); flags.putFlag( CLASSIFIER_HIDDEN_GRADIENTS, DataFlags.FLAG_NODE_CACHE );
    }

    @Override
    public Class getConfigClass() {
        return PyramidRegionLayerEntityConfig.class;
    }

    protected void doUpdateSelf() {

        PyramidRegionLayerEntityConfig config = ( PyramidRegionLayerEntityConfig ) _config;

        // Do nothing unless the input is defined
//        Data inputC1 = getData( INPUT_C1 );
//        Data inputC2 = getData( INPUT_C2 );
//        Data inputP1 = getData( INPUT_P1 );
//        Data inputP2 = getData( INPUT_P2 );

        Data inputC = getData( INPUT_C );
        Data inputP = getData( INPUT_P );

//        if( ( inputC1 == null ) || ( inputC2 == null ) || ( inputP1 == null ) || ( inputP2 == null ) ) {
        if( ( inputC == null ) || ( inputP == null )  ) {
            // we need to produce our output even if we can't write to it yet, to allow circular dependencies to be formed.
//            Data classifierSpikesOld = new Data( config.widthCells, config.heightCells );
//            Data classifierSpikesNew = new Data( config.widthCells, config.heightCells );
            Data outputSpikesOld = new Data( config.widthCells, config.heightCells );
            Data outputSpikesNew = new Data( config.widthCells, config.heightCells );
            Data output          = new Data( config.widthCells, config.heightCells );
            setData( OUTPUT_SPIKES_OLD, outputSpikesOld );
            setData( OUTPUT_SPIKES_NEW, outputSpikesNew );
//            setData( CLASSIFIER_SPIKES_OLD, classifierSpikesOld );
//            setData( CLASSIFIER_SPIKES_NEW, classifierSpikesNew );
            setData( OUTPUT, output );

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
        Point inputCSize = Data2d.getSize( inputC );
        int inputPSize = inputP.getSize();

//        Point inputC1Size = Data2d.getSize( inputC1 );
//        Point inputC2Size = Data2d.getSize( inputC2 );
//        Point inputP1Size = Data2d.getSize( inputP1 );
//        Point inputP2Size = Data2d.getSize( inputP2 );

//        int inputC1Width  = inputC1Size.x;
//        int inputC1Height = inputC1Size.y;
//        int inputC2Width  = inputC2Size.x;
//        int inputC2Height = inputC2Size.y;
//
//        int inputP1Width  = inputP1Size.x;
//        int inputP1Height = inputP1Size.y;
//        int inputP2Width  = inputP2Size.x;
//        int inputP2Height = inputP2Size.y;

        int inputCWidth  = inputCSize.x;
        int inputCHeight = inputCSize.y;

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Algorithm specific parameters
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Build the algorithm
        ObjectMap om = ObjectMap.GetInstance();

        PyramidRegionLayerConfig rlc = new PyramidRegionLayerConfig();
        rlc.setup(
            om, regionLayerName, _r,
            inputCWidth,
            inputCHeight,
            config.columnWidthCells,
            config.columnHeightCells,
            inputPSize,
//            inputC1Width, inputC1Height,
//            inputC2Width, inputC2Height,
//            inputP1Width, inputP1Height,
//            inputP2Width, inputP2Height,
//            config.widthCells, config.heightCells,
//            config.classifierLearningRate,
//            config.classifierMomentum,
//            config.classifierSparsityOutput, // a factor determining the output sparsity
//            config.classifierSparsity,// k, the number of active cells each step
//            config.classifierAgeMin,
//            config.classifierAgeMax,
//            config.age,
//            config.classifierAgeTruncationFactor,
//            config.classifierAgeScale,
//            config.classifierRateScale,
//            config.classifierRateMax,
//            config.classifierRateLearningRate,
//            config.classifierWeightsStdDev,
//            config.classifierBatchCount,
//            config.classifierBatchSize,
            config.predictorLearningRate,
            config.predictorHiddenCells,
            config.predictorLeakiness,
            config.predictorRegularization,
            config.predictorBatchSize,
            config.outputSpikeAgeMax,
            config.outputDecayRate );

        PyramidRegionLayer rl = new PyramidRegionLayer( regionLayerName, om );
        rl.setup( rlc );

        copyDataFromPersistence( rl );

        if( config.reset || config.resetDelayed ) {
            rl.reset();
            config.resetDelayed = false;
        }

        copyConfigChangesFromPersistence( rl, config );

        rl.update();

        // Save config changes caused by the algorithm
        copyConfigChangesToPersistence( rl, config );

        // Save data changes
        copyDataToPersistence( rl );
    }

    protected void copyConfigChangesFromPersistence( PyramidRegionLayer rl, PyramidRegionLayerEntityConfig config ) {
        rl._predictor._ffn._c.setBatchCount( config.predictorBatchCount );
        rl._rc.setLearn( config.learn );
    }

    protected void copyConfigChangesToPersistence( PyramidRegionLayer rl, PyramidRegionLayerEntityConfig config ) {

        config.predictorBatchCount = rl._predictor._ffn._c.getBatchCount();
//        config.sumClassifierError = rl._sumClassifierError;
//        config.sumClassifierResponse = rl._sumClassifierResponse;
        config.sumOutputSpikes = rl._sumOutputSpikes;
        config.sumPredictionErrorFP = rl._sumPredictionErrorFP;
        config.sumPredictionErrorFN = rl._sumPredictionErrorFN;
//        config.sumIntegration = rl._sumIntegration;
    }

    protected void copyDataFromPersistence( PyramidRegionLayer rl ) {

        rl._inputC = getData( INPUT_C );
        rl._inputP = getData( INPUT_P );

//        rl._inputC1 = getData(INPUT_C1);
//        rl._inputC2 = getData(INPUT_C2);
//        rl._inputP1 = getData(INPUT_P1);
//        rl._inputP2 = getData(INPUT_P2);

        rl._inputPOld = getDataLazyResize( INPUT_P_OLD, rl._inputPOld._dataSize );
        rl._inputPNew = getDataLazyResize( INPUT_P_NEW, rl._inputPNew._dataSize );

//        rl._classifierSpikesOld = getDataLazyResize( CLASSIFIER_SPIKES_OLD, rl._classifierSpikesOld._dataSize );
//        rl._classifierSpikesNew = getDataLazyResize( CLASSIFIER_SPIKES_NEW, rl._classifierSpikesNew._dataSize );
//        rl._spikesIntegrated = getDataLazyResize( CLASSIFIER_SPIKES_INTEGRATED, rl._spikesIntegrated._dataSize );

        rl._outputSpikesOld = getDataLazyResize( OUTPUT_SPIKES_OLD, rl._outputSpikesOld._dataSize );
        rl._outputSpikesNew = getDataLazyResize( OUTPUT_SPIKES_NEW, rl._outputSpikesNew._dataSize );
        rl._outputSpikesAge = getDataLazyResize( OUTPUT_SPIKES_AGE, rl._outputSpikesAge._dataSize );
        rl._output = getDataLazyResize( OUTPUT, rl._output._dataSize );

        rl._predictionErrorFP = getDataLazyResize( PREDICTION_ERROR_FP, rl._predictionErrorFP._dataSize );
        rl._predictionErrorFN = getDataLazyResize( PREDICTION_ERROR_FN, rl._predictionErrorFN._dataSize );
        rl._predictionOld = getDataLazyResize( PREDICTION_OLD, rl._predictionOld._dataSize );
        rl._predictionNew = getDataLazyResize( PREDICTION_NEW, rl._predictionNew._dataSize );
//        rl._predictionNewReal = getDataLazyResize( PREDICTION_NEW_REAL, rl._predictionNewReal._dataSize );

        NetworkLayer layer1 = rl._predictor._ffn._layers.get( 0 );
        NetworkLayer layer2 = rl._predictor._ffn._layers.get( 1 );
        rl._predictor._ffn._ideals = getDataLazyResize( PREDICTOR_IDEALS, rl._predictor._ffn._ideals._dataSize );
        layer1._weights = getDataLazyResize( PREDICTOR_WEIGHTS_1, layer1._weights._dataSize );
        layer2._weights = getDataLazyResize( PREDICTOR_WEIGHTS_2, layer2._weights._dataSize );
        layer1._biases = getDataLazyResize( PREDICTOR_BIASES_1, layer1._biases._dataSize );
        layer2._biases = getDataLazyResize( PREDICTOR_BIASES_2, layer2._biases._dataSize );
        layer1._costGradients = getDataLazyResize( ERROR_GRADIENTS_1, layer1._costGradients._dataSize );
        layer2._costGradients = getDataLazyResize( ERROR_GRADIENTS_2, layer2._costGradients._dataSize );
//        rl._predictor._outputTraces = getDataLazyResize( PREDICTOR_OUTPUT_TRACES, rl._predictor._outputTraces._dataSize );
//        rl._predictor._inputOutputTraces = getDataLazyResize( PREDICTOR_INPUT_OUTPUT_TRACES, rl._predictor._inputOutputTraces._dataSize );

//        rl._classifier._cellWeights = getDataLazyResize( CLASSIFIER_WEIGHTS, rl._classifier._cellWeights._dataSize );
//        rl._classifier._cellBiases1 = getDataLazyResize( CLASSIFIER_BIASES_1, rl._classifier._cellBiases1._dataSize );
//        rl._classifier._cellBiases2 = getDataLazyResize( CLASSIFIER_BIASES_2, rl._classifier._cellBiases2._dataSize );
//
//        rl._classifier._cellWeightsVelocity = getDataLazyResize( CLASSIFIER_WEIGHTS_VELOCITY, rl._classifier._cellWeightsVelocity._dataSize );
//        rl._classifier._cellBiases1Velocity = getDataLazyResize( CLASSIFIER_BIASES_1_VELOCITY, rl._classifier._cellBiases1Velocity._dataSize );
//        rl._classifier._cellBiases2Velocity = getDataLazyResize( CLASSIFIER_BIASES_2_VELOCITY, rl._classifier._cellBiases2Velocity._dataSize );
//
//        rl._classifier._cellErrors = getDataLazyResize( CLASSIFIER_ERRORS, rl._classifier._cellErrors._dataSize );
//        rl._classifier._cellWeightedSum = getDataLazyResize( CLASSIFIER_WEIGHTED_SUM, rl._classifier._cellWeightedSum._dataSize );
//        rl._classifier._cellTransfer = getDataLazyResize( CLASSIFIER_TRANSFER, rl._classifier._cellTransfer._dataSize );
//        rl._classifier._cellTransferPromoted = getDataLazyResize( CLASSIFIER_TRANSFER_PROMOTED, rl._classifier._cellTransferPromoted._dataSize );
//        rl._classifier._cellTransferTopK = getDataLazyResize( CLASSIFIER_TRANSFER_TOP_K, rl._classifier._cellTransferTopK._dataSize );
//        rl._classifier._cellTransferTopKA = getDataLazyResize( CLASSIFIER_TRANSFER_TOP_KA, rl._classifier._cellTransferTopKA._dataSize );
//
//        rl._classifier._cellSpikesTopK = getDataLazyResize( CLASSIFIER_SPIKES_TOP_K, rl._classifier._cellSpikesTopK._dataSize );
//        rl._classifier._cellSpikesTopKA = getDataLazyResize( CLASSIFIER_SPIKES_TOP_KA, rl._classifier._cellSpikesTopKA._dataSize );
//
//        rl._classifier._inputReconstructionWeightedSum = getDataLazyResize( CLASSIFIER_RECONSTRUCTION_WEIGHTED_SUM, rl._classifier._inputReconstructionWeightedSum._dataSize );
//        rl._classifier._inputReconstructionTransfer = getDataLazyResize( CLASSIFIER_RECONSTRUCTION_TRANSFER, rl._classifier._inputReconstructionTransfer._dataSize );
//
//        rl._classifier._cellAges = getDataLazyResize( CLASSIFIER_AGES, rl._classifier._cellAges._dataSize );
//        rl._classifier._cellRates = getDataLazyResize( CLASSIFIER_RATES, rl._classifier._cellRates._dataSize );
//
//        rl._classifier._inputGradients = getDataLazyResize( CLASSIFIER_OUTPUT_GRADIENTS, rl._classifier._inputGradients._dataSize );
//        rl._classifier._cellGradients = getDataLazyResize( CLASSIFIER_HIDDEN_GRADIENTS, rl._classifier._cellGradients._dataSize );
    }

    protected void copyDataToPersistence( PyramidRegionLayer rl ) {

        setData( INPUT_P_OLD, rl._inputPOld );
        setData( INPUT_P_NEW, rl._inputPNew );

//        setData( INPUT_C1_PREDICTED, rl._inputC1Predicted );
//        setData( INPUT_C2_PREDICTED, rl._inputC2Predicted );

//        setData( INPUT_C_PREDICTED, rl._inputCPredicted );

//        setData( CLASSIFIER_SPIKES_OLD, rl._classifierSpikesOld );
//        setData( CLASSIFIER_SPIKES_NEW, rl._classifierSpikesNew );
//        setData( CLASSIFIER_SPIKES_INTEGRATED, rl._spikesIntegrated );

        setData( OUTPUT_SPIKES_OLD, rl._outputSpikesOld );
        setData( OUTPUT_SPIKES_NEW, rl._outputSpikesNew );
        setData( OUTPUT_SPIKES_AGE, rl._outputSpikesAge );
        setData( OUTPUT, rl._output );

        setData( PREDICTION_ERROR_FP, rl._predictionErrorFP );
        setData( PREDICTION_ERROR_FN, rl._predictionErrorFN );
        setData( PREDICTION_OLD, rl._predictionOld );
        setData( PREDICTION_NEW, rl._predictionNew );
        setData( PREDICTION_NEW_UNIT, rl._predictionNewUnit );

        NetworkLayer layer1 = rl._predictor._ffn._layers.get( 0 );
        NetworkLayer layer2 = rl._predictor._ffn._layers.get( 1 );

        setData( PREDICTOR_IDEALS, rl._predictor._ffn._ideals );
        setData( PREDICTOR_WEIGHTS_1, layer1._weights );
        setData( PREDICTOR_WEIGHTS_2, layer2._weights );
        setData( PREDICTOR_BIASES_1, layer1._biases );
        setData( PREDICTOR_BIASES_2, layer2._biases );
        setData( ERROR_GRADIENTS_1, layer1._costGradients);
        setData( ERROR_GRADIENTS_2, layer2._costGradients);

//        setData( CLASSIFIER_WEIGHTS, rl._classifier._cellWeights );
//        setData( CLASSIFIER_BIASES_1, rl._classifier._cellBiases1 );
//        setData( CLASSIFIER_BIASES_2, rl._classifier._cellBiases2 );
//
//        setData( CLASSIFIER_WEIGHTS_VELOCITY, rl._classifier._cellWeightsVelocity );
//        setData( CLASSIFIER_BIASES_1_VELOCITY, rl._classifier._cellBiases1Velocity );
//        setData( CLASSIFIER_BIASES_2_VELOCITY, rl._classifier._cellBiases2Velocity );
//
//        setData( CLASSIFIER_ERRORS, rl._classifier._cellErrors );
//        setData( CLASSIFIER_WEIGHTED_SUM, rl._classifier._cellWeightedSum );
//        setData( CLASSIFIER_TRANSFER, rl._classifier._cellTransfer );
//        setData( CLASSIFIER_TRANSFER_PROMOTED, rl._classifier._cellTransferPromoted );
//        setData( CLASSIFIER_TRANSFER_TOP_K, rl._classifier._cellTransferTopK );
//        setData( CLASSIFIER_TRANSFER_TOP_KA, rl._classifier._cellTransferTopKA );
//
//        setData( CLASSIFIER_SPIKES_TOP_K, rl._classifier._cellSpikesTopK );
//        setData( CLASSIFIER_SPIKES_TOP_KA, rl._classifier._cellSpikesTopKA );
//
//        setData( CLASSIFIER_RECONSTRUCTION_WEIGHTED_SUM, rl._classifier._inputReconstructionWeightedSum );
//        setData( CLASSIFIER_RECONSTRUCTION_TRANSFER, rl._classifier._inputReconstructionTransfer );
//
//        setData( CLASSIFIER_AGES, rl._classifier._cellAges );
//        setData( CLASSIFIER_RATES, rl._classifier._cellRates );
//        setData( CLASSIFIER_PROMOTION, rl._classifier._cellPromotion );
//        setData( CLASSIFIER_INHIBITION, rl._classifier._cellInhibition );
//
//        setData( CLASSIFIER_OUTPUT_GRADIENTS, rl._classifier._inputGradients );
//        setData( CLASSIFIER_HIDDEN_GRADIENTS, rl._classifier._cellGradients );
    }

}
