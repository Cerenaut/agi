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
public abstract class QuiltPredictorEntity extends Entity {

    public static final String INPUT_C = "input-c";
    public static final String INPUT_P = "input-p";

    public static final String INPUT_P_OLD = "input-p-old"; // combined
    public static final String INPUT_P_NEW = "input-p-new";

    public static final String PREDICTION_OLD = "prediction-old";
    public static final String PREDICTION_NEW = "prediction-new";
    public static final String PREDICTION_NEW_UNIT = "prediction-new-unit";

    public QuiltPredictorEntity( ObjectMap om, Node n, ModelEntity model ) {
        super( om, n, model );
    }

    public void getInputAttributes( Collection< String > attributes ) {
        attributes.add( INPUT_C );
        attributes.add( INPUT_P );
    }

    public void getOutputAttributes( Collection< String > attributes, DataFlags flags ) {

        attributes.add( INPUT_P_OLD );
        attributes.add( INPUT_P_NEW );

        attributes.add( PREDICTION_OLD );
        attributes.add( PREDICTION_NEW );
        attributes.add( PREDICTION_NEW_UNIT );
    }

    @Override
    public Class getConfigClass() {
        return QuiltPredictorEntityConfig.class;
    }

    protected void doUpdateSelf() {

        QuiltPredictorEntityConfig config = ( QuiltPredictorEntityConfig ) _config;

        Data inputC = getData( INPUT_C );
        Data inputP = getData( INPUT_P );

        if( ( inputC == null ) || ( inputP == null )  ) {
            // we need to produce our output even if we can't write to it yet, to allow circular dependencies to be formed.
            Data predictionOld = new Data( config.widthCells, config.heightCells );
            Data predictionNew = new Data( config.widthCells, config.heightCells );
            setData( PREDICTION_OLD, predictionOld );
            setData( PREDICTION_OLD, predictionNew );

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
//        Point inputCSize = Data2d.getSize( inputC );
//        int inputPSize = inputP.getSize();
//        int inputCWidth  = inputCSize.x;
//        int inputCHeight = inputCSize.y;

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Algorithm specific parameters
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Build the algorithm
        ObjectMap om = ObjectMap.GetInstance();

        QuiltPredictorConfig qpc = createQuiltPredictorConfig();
        QuiltPredictorAlgorithm predictorAlgorithm = createQuiltPredictorAlgorithm();

        QuiltPredictor rl = new QuiltPredictor( regionLayerName, om );
        rl.setup( qpc, predictorAlgorithm );

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

    protected abstract QuiltPredictorConfig createQuiltPredictorConfig();
    protected abstract QuiltPredictorAlgorithm createQuiltPredictorAlgorithm();

    protected void copyConfigChangesFromPersistence( QuiltPredictor rl, QuiltPredictorEntityConfig config ) {
        rl._rc.setLearn( config.learn );
    }

    protected void copyConfigChangesToPersistence( QuiltPredictor rl, QuiltPredictorEntityConfig config ) {
    }

    protected void copyDataFromPersistence( QuiltPredictor rl ) {
        rl._inputC = getData( INPUT_C );
        rl._inputP = getData( INPUT_P );

        rl._inputPOld = getDataLazyResize( INPUT_P_OLD, rl._inputPOld._dataSize );
        rl._inputPNew = getDataLazyResize( INPUT_P_NEW, rl._inputPNew._dataSize );

        rl._predictionOld = getDataLazyResize( PREDICTION_OLD, rl._predictionOld._dataSize );
        rl._predictionNew = getDataLazyResize( PREDICTION_NEW, rl._predictionNew._dataSize );
//        rl._predictionNewUnit = getDataLazyResize( PREDICTION_NEW_UNIT, rl._predictionNewUnit._dataSize );
    }

    protected void copyDataToPersistence( QuiltPredictor rl ) {
        setData( INPUT_P_OLD, rl._inputPOld );
        setData( INPUT_P_NEW, rl._inputPNew );

        setData( PREDICTION_OLD, rl._predictionOld );
        setData( PREDICTION_NEW, rl._predictionNew );
        setData( PREDICTION_NEW_UNIT, rl._predictionNewUnit );
    }

}
