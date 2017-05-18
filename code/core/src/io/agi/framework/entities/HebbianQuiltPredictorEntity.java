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
public class HebbianQuiltPredictorEntity extends QuiltPredictorEntity {

    public static final String ENTITY_TYPE = "hebbian-quilt-predictor";

//    public static final String INPUT_C = "input-c";
//    public static final String INPUT_P = "input-p";
//
//    public static final String INPUT_P_OLD = "input-p-old"; // combined
//    public static final String INPUT_P_NEW = "input-p-new";
//
//    public static final String PREDICTION_OLD = "prediction-old";
//    public static final String PREDICTION_NEW = "prediction-new";
//    public static final String PREDICTION_NEW_UNIT = "prediction-new-unit";

    public static final String PREDICTOR_WEIGHTS = "predictor-weights";

    public HebbianQuiltPredictorEntity( ObjectMap om, Node n, ModelEntity model ) {
        super( om, n, model );
    }

    public void getInputAttributes( Collection< String > attributes ) {
        super.getInputAttributes( attributes );
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

        attributes.add( PREDICTOR_WEIGHTS );
    }

    @Override
    public Class getConfigClass() {
        return HebbianQuiltPredictorEntityConfig.class;
    }

//    protected void doUpdateSelf() {
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
//
//        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//        // Test parameters
//        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
//        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//        // Algorithm specific parameters
//        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//        // Build the algorithm
//        ObjectMap om = ObjectMap.GetInstance();
//
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
        HebbianQuiltPredictorEntityConfig config = ( HebbianQuiltPredictorEntityConfig ) _config;

        ObjectMap om = ObjectMap.GetInstance();
        String name = getName();
        Data inputC = getData( INPUT_C );
        Data inputP = getData( INPUT_P );
        Point inputCSize = Data2d.getSize( inputC );
        int inputPSize = inputP.getSize();
        int inputCWidth  = inputCSize.x;
        int inputCHeight = inputCSize.y;
        HebbianQuiltPredictorConfig rlc = new HebbianQuiltPredictorConfig();
        rlc.setup(
                om, name, _r,
                inputCWidth,
                inputCHeight,
                config.columnWidthCells,
                config.columnHeightCells,
                inputPSize,
                config.predictorLearningRate );
        return rlc;
    }

    protected QuiltPredictorAlgorithm createQuiltPredictorAlgorithm() {
        HebbianQuiltPredictor predictorAlgorithm = new HebbianQuiltPredictor();
        return predictorAlgorithm;
    }

    protected void copyConfigChangesFromPersistence( QuiltPredictor rl, QuiltPredictorEntityConfig config ) {
        super.copyConfigChangesFromPersistence( rl, config );
    }

    protected void copyConfigChangesToPersistence( QuiltPredictor rl, QuiltPredictorEntityConfig config ) {
        super.copyConfigChangesToPersistence( rl, config );
    }

    protected void copyDataFromPersistence( QuiltPredictor rl ) {

        super.copyDataFromPersistence( rl );

        HebbianQuiltPredictor predictorAlgorithm = (HebbianQuiltPredictor)rl._predictor;

        predictorAlgorithm._weights = getDataLazyResize( PREDICTOR_WEIGHTS, predictorAlgorithm._weights._dataSize );
    }

    protected void copyDataToPersistence( QuiltPredictor rl ) {

        super.copyDataToPersistence( rl );

        HebbianQuiltPredictor predictorAlgorithm = (HebbianQuiltPredictor)rl._predictor;

        setData( PREDICTOR_WEIGHTS, predictorAlgorithm._weights );
    }

}
