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

    public static final String CONTEXT_FREE_ACTIVITY     = "context-free-activity";
    public static final String CONTEXT_FREE_ACTIVITY_OLD = "context-free-activity-old";
    public static final String CONTEXT_FREE_ACTIVITY_NEW = "context-free-activity-new";

    public static final String CONTEXTUAL_ACTIVITY     = "contextual-activity";
    public static final String CONTEXTUAL_ACTIVITY_OLD = "contextual-activity-old";
    public static final String CONTEXTUAL_ACTIVITY_NEW = "contextual-activity-new";
    public static final String CONTEXTUAL_ACTIVITY_FP  = "contextual-activity-fp";
    public static final String CONTEXTUAL_ACTIVITY_FN  = "contextual-activity-fn";
    public static final String CONTEXTUAL_PREDICTION_INHIBITION = "contextual-prediction-inhibition";
    public static final String CONTEXTUAL_OUTPUT = "contextual-output";
    public static final String CONTEXTUAL_OUTPUT_AGE = "contextual-output-age";

    public static final String PREDICTOR_WEIGHTS = "predictor-weights";

    public static final String CONTEXT_FREE_WEIGHTS = "context-free-weights";
    public static final String CONTEXT_FREE_BIASES_1 = "context-free-biases-1";
    public static final String CONTEXT_FREE_BIASES_2 = "context-free-biases-2";
    public static final String CONTEXT_FREE_ERRORS = "context-free-errors";
    public static final String CONTEXT_FREE_RESPONSE = "context-free-response";
    public static final String CONTEXT_FREE_RECONSTRUCTION = "context-free-reconstruction";

    public static final String CONTEXTUAL_WEIGHTS = "contextual-weights";
    public static final String CONTEXTUAL_BIASES_1 = "contextual-biases-1";
    public static final String CONTEXTUAL_BIASES_2 = "contextual-biases-2";
    public static final String CONTEXTUAL_ERRORS = "contextual-errors";
    public static final String CONTEXTUAL_RESPONSE = "contextual-response";
    public static final String CONTEXTUAL_RECONSTRUCTION = "contextual-reconstruction";

    public AutoRegionLayerEntity( ObjectMap om, Node n, ModelEntity model ) {
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

        attributes.add( CONTEXTUAL_ACTIVITY );
        attributes.add( CONTEXTUAL_ACTIVITY_OLD );
        attributes.add( CONTEXTUAL_ACTIVITY_NEW );

        attributes.add( CONTEXTUAL_ACTIVITY_FP );
        attributes.add( CONTEXTUAL_ACTIVITY_FN );
        attributes.add( CONTEXTUAL_PREDICTION_INHIBITION );
        attributes.add( CONTEXTUAL_OUTPUT );
        attributes.add( CONTEXTUAL_OUTPUT_AGE );

        attributes.add( PREDICTOR_WEIGHTS );

        attributes.add( CONTEXT_FREE_WEIGHTS );
        attributes.add( CONTEXT_FREE_BIASES_1 );
        attributes.add( CONTEXT_FREE_BIASES_2 );
        attributes.add( CONTEXT_FREE_ERRORS );
        attributes.add( CONTEXT_FREE_RESPONSE );
        attributes.add( CONTEXT_FREE_RECONSTRUCTION );

        attributes.add( CONTEXTUAL_WEIGHTS );
        attributes.add( CONTEXTUAL_BIASES_1 );
        attributes.add( CONTEXTUAL_BIASES_2 );
        attributes.add( CONTEXTUAL_ERRORS );
        attributes.add( CONTEXTUAL_RESPONSE );
        attributes.add( CONTEXTUAL_RECONSTRUCTION );
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
        String contextualName  = getKey( AutoRegionLayerConfig.SUFFIX_CONTEXTUAL );

        contextFreeConfig.setup(
                om, contextFreeName, _r,
                inputArea, config.contextFreeWidthCells, config.contextFreeHeightCells, config.contextFreeLearningRate,
                config.contextFreeSparsityOutput, config.contextFreeSparsity, config.contextFreeSparsityMin, config.contextFreeSparsityMax, config.contextFreeAgeMin, config.contextFreeAgeMax, config.contextFreeAge );

        int contextFreeCellArea = config.contextFreeWidthCells * config.contextFreeHeightCells;
        int contextualInputArea = contextFreeCellArea * 2;
//        int contextualCellArea = config.contextualWidthCells * config.contextualHeightCells;

        contextualConfig.setup(
                om, contextualName, _r,
                contextualInputArea, config.contextualWidthCells, config.contextualHeightCells, config.contextualLearningRate,
                config.contextualSparsityOutput, config.contextualSparsity, config.contextualSparsityMin, config.contextualSparsityMax, config.contextualAgeMin, config.contextualAgeMax, config.contextualAge );

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
        rl.update(); // 120-150ms. The rest of doUpdateSelf() is maybe 50ms.

        // Save data
        copyDataToPersistence( rl );
    }

    protected void copyDataFromPersistence( AutoRegionLayer rl ) {

        rl._input1 = getData( INPUT_1 );
        rl._input2 = getData( INPUT_2 );

        rl._contextFreeActivity = getDataLazyResize( CONTEXT_FREE_ACTIVITY, rl._contextFreeActivity._dataSize );
        rl._contextFreeActivityOld = getDataLazyResize( CONTEXT_FREE_ACTIVITY_OLD, rl._contextFreeActivityOld._dataSize );
        rl._contextFreeActivityNew = getDataLazyResize( CONTEXT_FREE_ACTIVITY_NEW, rl._contextFreeActivityNew._dataSize );

        rl._contextualActivity = getDataLazyResize( CONTEXTUAL_ACTIVITY, rl._contextualActivity._dataSize );
        rl._contextualActivityOld = getDataLazyResize( CONTEXTUAL_ACTIVITY_OLD, rl._contextualActivityOld._dataSize );
        rl._contextualActivityNew = getDataLazyResize( CONTEXTUAL_ACTIVITY_NEW, rl._contextualActivityNew._dataSize );
        rl._contextualActivityFP = getDataLazyResize( CONTEXTUAL_ACTIVITY_FP, rl._contextualActivityFP._dataSize );
        rl._contextualActivityFN = getDataLazyResize( CONTEXTUAL_ACTIVITY_FN, rl._contextualActivityFN._dataSize );
        rl._contextualPredictionInhibition = getDataLazyResize( CONTEXTUAL_PREDICTION_INHIBITION, rl._contextualPredictionInhibition._dataSize );
        rl._contextualOutput = getDataLazyResize( CONTEXTUAL_OUTPUT, rl._contextualOutput._dataSize );
        rl._contextualOutputAge = getDataLazyResize( CONTEXTUAL_OUTPUT_AGE, rl._contextualOutputAge._dataSize );

        rl._predictor._weights = getDataLazyResize( PREDICTOR_WEIGHTS, rl._predictor._weights._dataSize );

        rl._contextFreeClassifier._cellWeights = getDataLazyResize( CONTEXT_FREE_WEIGHTS, rl._contextFreeClassifier._cellWeights._dataSize );
        rl._contextFreeClassifier._cellBiases1 = getDataLazyResize( CONTEXT_FREE_BIASES_1, rl._contextFreeClassifier._cellBiases1._dataSize );
        rl._contextFreeClassifier._cellBiases2 = getDataLazyResize( CONTEXT_FREE_BIASES_2, rl._contextFreeClassifier._cellBiases2._dataSize );
        rl._contextFreeClassifier._cellErrors = getDataLazyResize( CONTEXT_FREE_ERRORS, rl._contextFreeClassifier._cellErrors._dataSize );
        rl._contextFreeClassifier._cellResponse = getDataLazyResize( CONTEXT_FREE_RESPONSE, rl._contextFreeClassifier._cellResponse._dataSize );
        rl._contextFreeClassifier._inputReconstruction = getDataLazyResize( CONTEXT_FREE_RECONSTRUCTION, rl._contextFreeClassifier._inputReconstruction._dataSize );

        rl._contextualClassifier._cellWeights = getDataLazyResize( CONTEXTUAL_WEIGHTS, rl._contextualClassifier._cellWeights._dataSize );
        rl._contextualClassifier._cellBiases1 = getDataLazyResize( CONTEXTUAL_BIASES_1, rl._contextualClassifier._cellBiases1._dataSize );
        rl._contextualClassifier._cellBiases2 = getDataLazyResize( CONTEXTUAL_BIASES_2, rl._contextualClassifier._cellBiases2._dataSize );
        rl._contextualClassifier._cellErrors = getDataLazyResize( CONTEXTUAL_ERRORS, rl._contextualClassifier._cellErrors._dataSize );
        rl._contextualClassifier._cellResponse = getDataLazyResize( CONTEXTUAL_RESPONSE, rl._contextualClassifier._cellResponse._dataSize );
        rl._contextualClassifier._inputReconstruction = getDataLazyResize( CONTEXTUAL_RECONSTRUCTION, rl._contextualClassifier._inputReconstruction._dataSize );
    }

    protected void copyDataToPersistence( AutoRegionLayer rl ) {

        setData( CONTEXT_FREE_ACTIVITY, rl._contextFreeActivity );
        setData( CONTEXT_FREE_ACTIVITY_OLD, rl._contextFreeActivityOld );
        setData( CONTEXT_FREE_ACTIVITY_NEW, rl._contextFreeActivityNew );

        setData( CONTEXTUAL_ACTIVITY, rl._contextualActivity );
        setData( CONTEXTUAL_ACTIVITY_OLD, rl._contextualActivityOld );
        setData( CONTEXTUAL_ACTIVITY_NEW, rl._contextualActivityNew );
        setData( CONTEXTUAL_ACTIVITY_FP, rl._contextualActivityFP );
        setData( CONTEXTUAL_ACTIVITY_FN, rl._contextualActivityFN );
        setData( CONTEXTUAL_PREDICTION_INHIBITION, rl._contextualPredictionInhibition );
        setData( CONTEXTUAL_OUTPUT, rl._contextualOutput );
        setData( CONTEXTUAL_OUTPUT_AGE, rl._contextualOutputAge );

        setData( PREDICTOR_WEIGHTS, rl._predictor._weights );

        setData( CONTEXT_FREE_WEIGHTS, rl._contextFreeClassifier._cellWeights );
        setData( CONTEXT_FREE_BIASES_1, rl._contextFreeClassifier._cellBiases1 );
        setData( CONTEXT_FREE_BIASES_2, rl._contextFreeClassifier._cellBiases2 );
        setData( CONTEXT_FREE_ERRORS, rl._contextFreeClassifier._cellErrors );
        setData( CONTEXT_FREE_RESPONSE, rl._contextFreeClassifier._cellResponse );
        setData( CONTEXT_FREE_RECONSTRUCTION, rl._contextFreeClassifier._inputReconstruction );

        setData( CONTEXTUAL_WEIGHTS, rl._contextualClassifier._cellWeights );
        setData( CONTEXTUAL_BIASES_1, rl._contextualClassifier._cellBiases1 );
        setData( CONTEXTUAL_BIASES_2, rl._contextualClassifier._cellBiases2 );
        setData( CONTEXTUAL_ERRORS, rl._contextualClassifier._cellErrors );
        setData( CONTEXTUAL_RESPONSE, rl._contextualClassifier._cellResponse );
        setData( CONTEXTUAL_RECONSTRUCTION, rl._contextualClassifier._inputReconstruction );

    }

}
