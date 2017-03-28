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

import io.agi.core.alg.*;
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
public class FeedForwardNetworkQuiltPredictorEntity extends QuiltPredictorEntity {

    public static final String ENTITY_TYPE = "feed-forward-network-quilt-predictor";

//    public static final String INPUT_C = "input-c";
//    public static final String INPUT_P = "input-p";
//
//    public static final String INPUT_P_OLD = "input-p-old"; // combined
//    public static final String INPUT_P_NEW = "input-p-new";
//
//    public static final String PREDICTION_OLD = "prediction-old";
//    public static final String PREDICTION_NEW = "prediction-new";
//    public static final String PREDICTION_NEW_UNIT = "prediction-new-unit";

    public static final String PREDICTOR_IDEALS = "predictor-ideals";
    public static final String PREDICTOR_WEIGHTS_1 = "predictor-weights-1";
    public static final String PREDICTOR_WEIGHTS_2 = "predictor-weights-2";
    public static final String PREDICTOR_BIASES_1 = "predictor-biases-1";
    public static final String PREDICTOR_BIASES_2 = "predictor-biases-2";
    public static final String ERROR_GRADIENTS_1 = "error-gradients-1";
    public static final String ERROR_GRADIENTS_2 = "error-gradients-2";

    public FeedForwardNetworkQuiltPredictorEntity( ObjectMap om, Node n, ModelEntity model ) {
        super( om, n, model );
    }

    public void getInputAttributes( Collection< String > attributes ) {
        super.getInputAttributes( attributes );
//        attributes.add( INPUT_C );
//        attributes.add( INPUT_P );
    }

    public void getOutputAttributes( Collection< String > attributes, DataFlags flags ) {
        super.getOutputAttributes( attributes, flags );

//        attributes.add( INPUT_P_OLD );
//        attributes.add( INPUT_P_NEW );
//
//        flags.putFlag( INPUT_P_OLD, DataFlags.FLAG_NODE_CACHE );
//        flags.putFlag( INPUT_P_NEW, DataFlags.FLAG_NODE_CACHE );
//
//        attributes.add( PREDICTION_OLD );
//        attributes.add( PREDICTION_NEW );
//        attributes.add( PREDICTION_NEW_UNIT );
//
//        flags.putFlag( PREDICTION_OLD, DataFlags.FLAG_NODE_CACHE );
//        flags.putFlag( PREDICTION_NEW, DataFlags.FLAG_NODE_CACHE );
//        flags.putFlag( PREDICTION_NEW_UNIT, DataFlags.FLAG_NODE_CACHE );
//
        attributes.add( PREDICTOR_IDEALS );
        attributes.add( PREDICTOR_WEIGHTS_1 );
        attributes.add( PREDICTOR_WEIGHTS_2 );
        attributes.add( PREDICTOR_BIASES_1 );
        attributes.add( PREDICTOR_BIASES_2 );
        attributes.add( ERROR_GRADIENTS_1 );
        attributes.add( ERROR_GRADIENTS_2 );
    }

    @Override
    public Class getConfigClass() {
        return FeedForwardNetworkQuiltPredictorEntityConfig.class;
    }

//    protected void doUpdateSelf() {
//
//        FeedForwardNetworkQuiltPredictorEntityConfig config = ( FeedForwardNetworkQuiltPredictorEntityConfig ) _config;
//
//        Data inputC = getData( INPUT_C );
//        Data inputP = getData( INPUT_P );
//
//        if( ( inputC == null ) || ( inputP == null )  ) {
//            // we need to produce our output even if we can't write to it yet, to allow circular dependencies to be formed.
//            Data predictionOld = new Data( config.widthCells, config.heightCells );
//            Data predictionNew = new Data( config.widthCells, config.heightCells );
//            setData( PREDICTION_OLD, predictionOld );
//            setData( PREDICTION_OLD, predictionNew );
//
//            if( config.reset ) {
//                config.resetDelayed = true;
//            }
//
//            return; // can't update yet.
//        }
//
//        // Get all the parameters:
//        String regionLayerName = getName();
//
//        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//        // Test parameters
//        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//        // Feedforward size
//        Point inputCSize = Data2d.getSize( inputC );
//        int inputPSize = inputP.getSize();
//        int inputCWidth  = inputCSize.x;
//        int inputCHeight = inputCSize.y;
//
//        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//        // Algorithm specific parameters
//        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//        // Build the algorithm
//        ObjectMap om = ObjectMap.GetInstance();
//
////        DataSize dataSizeInputC = DataSize.create( inputCSize.x, inputCSize.y );
////        DataSize dataSizeInputP = DataSize.create( inputPSize );
////
////        int cells = dataSizeInputC.getVolume(); //_rc._classifierConfig.getNbrCells();
////        int predictorInputs = dataSizeInputP.getVolume() + cells;
////        int predictorOutputs = cells;
////        float predictorLearningRate = rlc.getPredictorLearningRate();
//////        float predictorDecayRate = rc.getPredictorTraceDecayRate();
////        int predictorHiddenCells = rlc.getPredictorHiddenCells();
////        float predictorLeakiness = rlc.getPredictorLeakiness();
////        float predictorRegularization = rlc.getPredictorRegularization();
////        int predictorBatchSize = rlc.getPredictorBatchSize();
//        FeedForwardNetworkQuiltPredictor predictorAlgorithm = new FeedForwardNetworkQuiltPredictor();
////        predictorAlgorithm.setup( //, om, _r, predictorInputs, predictorHiddenCells, predictorOutputs, predictorLearningRate, predictorLeakiness, predictorRegularization, predictorBatchSize );//, predictorDecayRate );
//
//        QuiltPredictor rl = new QuiltPredictor( regionLayerName, om );
//        rl.setup( rlc, predictorAlgorithm );
//
//        copyDataFromPersistence( rl );
//
//        if( config.reset || config.resetDelayed ) {
//            rl.reset();
//            config.resetDelayed = false;
//        }
//
//        copyConfigChangesFromPersistence( rl, config );
//
//        rl.update();
//
//        // Save config changes caused by the algorithm
//        copyConfigChangesToPersistence( rl, config );
//
//        // Save data changes
//        copyDataToPersistence( rl );
//    }

    protected QuiltPredictorConfig createQuiltPredictorConfig() {
        ObjectMap om = ObjectMap.GetInstance();
        String name = getName();
        Data inputC = getData( INPUT_C );
        Data inputP = getData( INPUT_P );
        Point inputCSize = Data2d.getSize( inputC );
        int inputPSize = inputP.getSize();
        int inputCWidth  = inputCSize.x;
        int inputCHeight = inputCSize.y;

        FeedForwardNetworkQuiltPredictorEntityConfig config = (FeedForwardNetworkQuiltPredictorEntityConfig)_config;
        FeedForwardNetworkQuiltPredictorConfig rlc = new FeedForwardNetworkQuiltPredictorConfig();
        rlc.setup(
                om, name, _r,
                inputCWidth,
                inputCHeight,
                config.columnWidthCells,
                config.columnHeightCells,
                inputPSize,
                config.predictorLearningRate,
                config.predictorHiddenCells,
                config.predictorLeakiness,
                config.predictorRegularization,
                config.predictorBatchSize );


        return rlc;
    }

    protected QuiltPredictorAlgorithm createQuiltPredictorAlgorithm() {
        FeedForwardNetworkQuiltPredictor predictorAlgorithm = new FeedForwardNetworkQuiltPredictor();
        return predictorAlgorithm;
    }

    protected void copyConfigChangesFromPersistence( QuiltPredictor rl, QuiltPredictorEntityConfig config ) {
        super.copyConfigChangesFromPersistence( rl, config );
        FeedForwardNetworkQuiltPredictor predictorAlgorithm = (FeedForwardNetworkQuiltPredictor)rl._predictor;
        FeedForwardNetworkQuiltPredictorEntityConfig predictorEntityConfig = (FeedForwardNetworkQuiltPredictorEntityConfig)config;
        predictorAlgorithm._ffn._c.setBatchCount( predictorEntityConfig.predictorBatchCount );
    }

    protected void copyConfigChangesToPersistence( QuiltPredictor rl, QuiltPredictorEntityConfig config ) {
        super.copyConfigChangesToPersistence( rl, config );
        FeedForwardNetworkQuiltPredictor predictorAlgorithm = (FeedForwardNetworkQuiltPredictor)rl._predictor;
        FeedForwardNetworkQuiltPredictorEntityConfig predictorEntityConfig = (FeedForwardNetworkQuiltPredictorEntityConfig)config;
        predictorEntityConfig.predictorBatchCount = predictorAlgorithm._ffn._c.getBatchCount();
    }

    protected void copyDataFromPersistence( QuiltPredictor rl ) {

        super.copyDataFromPersistence( rl );

        FeedForwardNetworkQuiltPredictor predictorAlgorithm = (FeedForwardNetworkQuiltPredictor)rl._predictor;

//        rl._inputC = getData( INPUT_C );
//        rl._inputP = getData( INPUT_P );
//
//        rl._inputPOld = getDataLazyResize( INPUT_P_OLD, rl._inputPOld._dataSize );
//        rl._inputPNew = getDataLazyResize( INPUT_P_NEW, rl._inputPNew._dataSize );
//
//        rl._predictionOld = getDataLazyResize( PREDICTION_OLD, rl._predictionOld._dataSize );
//        rl._predictionNew = getDataLazyResize( PREDICTION_NEW, rl._predictionNew._dataSize );
////        rl._predictionNewReal = getDataLazyResize( PREDICTION_NEW_REAL, rl._predictionNewReal._dataSize );

        NetworkLayer layer1 = predictorAlgorithm._ffn._layers.get( 0 );
        NetworkLayer layer2 = predictorAlgorithm._ffn._layers.get( 1 );
        predictorAlgorithm._ffn._ideals = getDataLazyResize( PREDICTOR_IDEALS, predictorAlgorithm._ffn._ideals._dataSize );
        layer1._weights = getDataLazyResize( PREDICTOR_WEIGHTS_1, layer1._weights._dataSize );
        layer2._weights = getDataLazyResize( PREDICTOR_WEIGHTS_2, layer2._weights._dataSize );
        layer1._biases = getDataLazyResize( PREDICTOR_BIASES_1, layer1._biases._dataSize );
        layer2._biases = getDataLazyResize( PREDICTOR_BIASES_2, layer2._biases._dataSize );
        layer1._costGradients = getDataLazyResize( ERROR_GRADIENTS_1, layer1._costGradients._dataSize );
        layer2._costGradients = getDataLazyResize( ERROR_GRADIENTS_2, layer2._costGradients._dataSize );
    }

    protected void copyDataToPersistence( QuiltPredictor rl ) {

        super.copyDataToPersistence( rl );

        FeedForwardNetworkQuiltPredictor predictorAlgorithm = (FeedForwardNetworkQuiltPredictor)rl._predictor;

//        setData( INPUT_P_OLD, rl._inputPOld );
//        setData( INPUT_P_NEW, rl._inputPNew );
//
//        setData( PREDICTION_OLD, rl._predictionOld );
//        setData( PREDICTION_NEW, rl._predictionNew );
//        setData( PREDICTION_NEW_UNIT, rl._predictionNewUnit );

        NetworkLayer layer1 = predictorAlgorithm._ffn._layers.get( 0 );
        NetworkLayer layer2 = predictorAlgorithm._ffn._layers.get( 1 );

        setData( PREDICTOR_IDEALS, predictorAlgorithm._ffn._ideals );
        setData( PREDICTOR_WEIGHTS_1, layer1._weights );
        setData( PREDICTOR_WEIGHTS_2, layer2._weights );
        setData( PREDICTOR_BIASES_1, layer1._biases );
        setData( PREDICTOR_BIASES_2, layer2._biases );
        setData( ERROR_GRADIENTS_1, layer1._costGradients);
        setData( ERROR_GRADIENTS_2, layer2._costGradients);
    }

}
