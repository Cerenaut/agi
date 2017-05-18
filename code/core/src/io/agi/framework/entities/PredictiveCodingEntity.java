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

import io.agi.core.alg.PredictiveCoding;
import io.agi.core.alg.PredictiveCodingConfig;
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
public class PredictiveCodingEntity extends Entity {

    public static final String ENTITY_TYPE = "predictive-coding";

    public static final String INPUT_PREDICTED = "input-predicted";
    public static final String INPUT_OBSERVED = "input-observed";

//    public static final String INPUT_P_OLD = "input-p-old"; // combined
//    public static final String INPUT_P_NEW = "input-p-new";

    public static final String PREDICTION_ERROR_FP  = "prediction-error-fp";
    public static final String PREDICTION_ERROR_FN  = "prediction-error-fn";

    public static final String OUTPUT_SPIKES_OLD = "output-spikes-old";
    public static final String OUTPUT_SPIKES_NEW = "output-spikes-new";
    public static final String OUTPUT = "output";
    public static final String OUTPUT_SPIKES_AGE = "output-spikes-age";

    public PredictiveCodingEntity( ObjectMap om, Node n, ModelEntity model ) {
        super( om, n, model );
    }

    public void getInputAttributes( Collection< String > attributes ) {
        attributes.add( INPUT_PREDICTED );
        attributes.add( INPUT_OBSERVED );
    }

    public void getOutputAttributes( Collection< String > attributes, DataFlags flags ) {

//        attributes.add( INPUT_P_OLD );
//        attributes.add( INPUT_P_NEW );
//
//        flags.putFlag( INPUT_P_OLD, DataFlags.FLAG_NODE_CACHE );
//        flags.putFlag( INPUT_P_NEW, DataFlags.FLAG_NODE_CACHE );

        attributes.add( OUTPUT_SPIKES_AGE );

        flags.putFlag( OUTPUT_SPIKES_AGE, DataFlags.FLAG_NODE_CACHE );

        attributes.add( OUTPUT_SPIKES_OLD );
        attributes.add( OUTPUT_SPIKES_NEW );
        attributes.add( OUTPUT );

        flags.putFlag( OUTPUT_SPIKES_OLD, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( OUTPUT_SPIKES_NEW, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( OUTPUT, DataFlags.FLAG_NODE_CACHE );

        // predictor
        attributes.add( PREDICTION_ERROR_FP );
        attributes.add( PREDICTION_ERROR_FN );

        flags.putFlag( PREDICTION_ERROR_FP, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( PREDICTION_ERROR_FN, DataFlags.FLAG_NODE_CACHE );
    }

    @Override
    public Class getConfigClass() {
        return PredictiveCodingEntityConfig.class;
    }

    protected void doUpdateSelf() {

        PredictiveCodingEntityConfig config = ( PredictiveCodingEntityConfig ) _config;

        // Do nothing unless the input is defined
        Data inputPredicted = getData( INPUT_PREDICTED );
        Data inputObserved  = getData( INPUT_OBSERVED );

        if( ( inputPredicted == null ) || ( inputObserved == null )  ) {
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
        Point inputPSize = Data2d.getSize( inputPredicted );
        Point inputOSize = Data2d.getSize( inputObserved );
        Point inputCSize = new Point( config.widthCells, config.heightCells );

        if(    ( !inputPSize.equals( inputOSize ) )
            || ( !inputPSize.equals( inputCSize ) ) ) {
            _logger.error( "Bad (dissimilar) Data input and/or config input size. Can't continue." );
            return;
        }

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Algorithm specific parameters
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Build the algorithm
        ObjectMap om = ObjectMap.GetInstance();

        PredictiveCodingConfig rlc = new PredictiveCodingConfig();
        rlc.setup(
            om, regionLayerName, _r,
            config.widthCells,
            config.heightCells,
            config.outputSpikeAgeMax,
            config.outputDecayRate );

        PredictiveCoding rl = new PredictiveCoding( regionLayerName, om );
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

    protected void copyConfigChangesFromPersistence( PredictiveCoding rl, PredictiveCodingEntityConfig config ) {
        rl._rc.setLearn( config.learn );
    }

    protected void copyConfigChangesToPersistence( PredictiveCoding rl, PredictiveCodingEntityConfig config ) {
        config.sumPredictionErrorFP = rl._sumPredictionErrorFP;
        config.sumPredictionErrorFN = rl._sumPredictionErrorFN;
    }

    protected void copyDataFromPersistence( PredictiveCoding rl ) {

        rl._inputPredicted = getData( INPUT_PREDICTED );
        rl._inputObserved  = getData( INPUT_OBSERVED );

//        rl._inputPOld = getDataLazyResize( INPUT_P_OLD, rl._inputPOld._dataSize );
//        rl._inputPNew = getDataLazyResize( INPUT_P_NEW, rl._inputPNew._dataSize );

        rl._outputSpikesOld = getDataLazyResize( OUTPUT_SPIKES_OLD, rl._outputSpikesOld._dataSize );
        rl._outputSpikesNew = getDataLazyResize( OUTPUT_SPIKES_NEW, rl._outputSpikesNew._dataSize );
        rl._outputSpikesAge = getDataLazyResize( OUTPUT_SPIKES_AGE, rl._outputSpikesAge._dataSize );
        rl._output = getDataLazyResize( OUTPUT, rl._output._dataSize );

        rl._predictionErrorFP = getDataLazyResize( PREDICTION_ERROR_FP, rl._predictionErrorFP._dataSize );
        rl._predictionErrorFN = getDataLazyResize( PREDICTION_ERROR_FN, rl._predictionErrorFN._dataSize );

    }

    protected void copyDataToPersistence( PredictiveCoding rl ) {

//        setData( INPUT_P_OLD, rl._inputPOld );
//        setData( INPUT_P_NEW, rl._inputPNew );

        setData( OUTPUT_SPIKES_OLD, rl._outputSpikesOld );
        setData( OUTPUT_SPIKES_NEW, rl._outputSpikesNew );
        setData( OUTPUT_SPIKES_AGE, rl._outputSpikesAge );
        setData( OUTPUT, rl._output );

        setData( PREDICTION_ERROR_FP, rl._predictionErrorFP );
        setData( PREDICTION_ERROR_FN, rl._predictionErrorFN );
    }

}
