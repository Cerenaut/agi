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

import io.agi.core.alg.HierarchicalQuiltedCompetitiveLearning;
import io.agi.core.alg.HierarchicalQuiltedCompetitiveLearningConfig;
import io.agi.core.ann.unsupervised.GrowingNeuralGas;
import io.agi.core.ann.unsupervised.GrowingNeuralGasConfig;
import io.agi.core.ann.unsupervised.BinaryTreeQuiltConfig;
import io.agi.core.data.Data;
import io.agi.core.data.Data2d;
import io.agi.core.data.DataSize;
import io.agi.core.orm.ObjectMap;
import io.agi.framework.DataFlags;
import io.agi.framework.Entity;
import io.agi.framework.Node;
import io.agi.framework.persistence.models.ModelEntity;

import java.awt.*;
import java.util.Collection;

/**
 * Created by dave on 23/10/16.
 */
public class HqClRegionLayerEntity { /*extends Entity {

    public static final String ENTITY_TYPE = "hq-cl-region-layer";

    public static final String INPUT_FF_1 = "input-ff-1";
    public static final String INPUT_FF_2 = "input-ff-2";
    public static final String INPUT_FB_1 = "input-fb-1";

    public static final String INPUT_FF_1_OLD = "input-ff-1-old";
    public static final String INPUT_FF_2_OLD = "input-ff-2-old";
    public static final String INPUT_FB_1_OLD = "input-fb-1-old";

    public static final String REGION_ACTIVITY = "region-activity";
    public static final String REGION_ACTIVITY_INFERRED = "region-activity-inferred";
    public static final String REGION_PREDICTION_WEIGHTS = "region-prediction-weights";
    public static final String REGION_PREDICTION = "region-prediction";
    public static final String REGION_LIKELIHOOD = "region-likelihood";
//    public static final String REGION_ERROR_HISTORY = "region-error-history";
//    public static final String REGION_ERROR_HISTORY_INDEX = "region-error-history-index";

    public static final String ORGANIZER_CELL_MASK = "organizer-cell-mask";
    public static final String ORGANIZER_CELL_WEIGHTS = "organizer-cell-weights";

    public static final String CLASSIFIER_CELL_WEIGHTS = "classifier-cell-weights";
    public static final String CLASSIFIER_CELL_ERRORS = "classifier-cell-errors";
    public static final String CLASSIFIER_CELL_MASK = "classifier-cell-mask";

    public static final String CLASSIFIER_CELL_STRESS = "classifier-cell-stress";
    public static final String CLASSIFIER_CELL_AGES = "classifier-cell-ages";
    public static final String CLASSIFIER_EDGES = "classifier-edges";
    public static final String CLASSIFIER_EDGES_AGES = "classifier-edges-ages";
    public static final String CLASSIFIER_AGE_SINCE_GROWTH = "classifier-age-since-growth";

    protected Data _classifierCellWeights;
    protected Data _classifierCellErrors;
    protected Data _classifierCellMask;

    protected Data _classifierCellStress;
    protected Data _classifierCellAges;
    protected Data _classifierEdges;
    protected Data _classifierEdgesAges;
    protected Data _classifierAgeSinceGrowth;

    public HqClRegionLayerEntity( ObjectMap om, Node n, ModelEntity model ) {
        super( om, n, model );
    }

    public void getInputAttributes( Collection< String > attributes ) {
        attributes.add( INPUT_FF_1 );
        attributes.add( INPUT_FF_2 );
        attributes.add( INPUT_FB_1 );
    }

    public void getOutputAttributes( Collection< String > attributes, DataFlags flags ) {

        attributes.add( INPUT_FF_1_OLD );

        flags.putFlag( INPUT_FF_1_OLD, DataFlags.FLAG_SPARSE_BINARY );
        flags.putFlag( INPUT_FF_1_OLD, DataFlags.FLAG_NODE_CACHE );

        attributes.add( INPUT_FF_2_OLD );

        flags.putFlag( INPUT_FF_2_OLD, DataFlags.FLAG_SPARSE_BINARY );
        flags.putFlag( INPUT_FF_2_OLD, DataFlags.FLAG_NODE_CACHE );

        attributes.add( INPUT_FB_1_OLD );

        flags.putFlag( INPUT_FB_1_OLD, DataFlags.FLAG_SPARSE_BINARY );
        flags.putFlag( INPUT_FB_1_OLD, DataFlags.FLAG_NODE_CACHE );


        attributes.add( REGION_ACTIVITY );

        flags.putFlag( REGION_ACTIVITY, DataFlags.FLAG_SPARSE_BINARY );
        flags.putFlag( REGION_ACTIVITY, DataFlags.FLAG_NODE_CACHE );

        attributes.add( REGION_ACTIVITY_INFERRED );

        flags.putFlag( REGION_ACTIVITY_INFERRED, DataFlags.FLAG_SPARSE_BINARY );
        flags.putFlag( REGION_ACTIVITY_INFERRED, DataFlags.FLAG_NODE_CACHE );

        attributes.add( REGION_PREDICTION_WEIGHTS );

        flags.putFlag( REGION_PREDICTION_WEIGHTS, DataFlags.FLAG_PERSIST_ON_FLUSH );
        flags.putFlag( REGION_PREDICTION_WEIGHTS, DataFlags.FLAG_NODE_CACHE );

        attributes.add( REGION_PREDICTION );

        flags.putFlag( REGION_PREDICTION, DataFlags.FLAG_NODE_CACHE );

        attributes.add( REGION_LIKELIHOOD );

        flags.putFlag( REGION_LIKELIHOOD, DataFlags.FLAG_NODE_CACHE );

//        attributes.add( REGION_ERROR_HISTORY );
//
//        flags.putFlag( REGION_ERROR_HISTORY, DataFlags.FLAG_NODE_CACHE );
//
//        attributes.add( REGION_ERROR_HISTORY_INDEX );
//
//        flags.putFlag( REGION_ERROR_HISTORY_INDEX, DataFlags.FLAG_NODE_CACHE );

        attributes.add( ORGANIZER_CELL_MASK );

        flags.putFlag( ORGANIZER_CELL_MASK, DataFlags.FLAG_NODE_CACHE );

        attributes.add( ORGANIZER_CELL_WEIGHTS );

        flags.putFlag( ORGANIZER_CELL_WEIGHTS, DataFlags.FLAG_NODE_CACHE );

        attributes.add( CLASSIFIER_CELL_WEIGHTS );

        flags.putFlag( CLASSIFIER_CELL_WEIGHTS, DataFlags.FLAG_PERSIST_ON_FLUSH );
        flags.putFlag( CLASSIFIER_CELL_WEIGHTS, DataFlags.FLAG_NODE_CACHE );

        attributes.add( CLASSIFIER_CELL_ERRORS );

//        flags.putFlag( Keys.concatenate( prefix, CLASSIFIER_CELL_ERRORS ), DataFlags.FLAG_PERSIST_ON_FLUSH );
        flags.putFlag( CLASSIFIER_CELL_ERRORS, DataFlags.FLAG_NODE_CACHE );

        attributes.add( CLASSIFIER_CELL_MASK );

//        flags.putFlag( CLASSIFIER_CELL_MASK, DataFlags.FLAG_SPARSE_BINARY );
        flags.putFlag( CLASSIFIER_CELL_MASK, DataFlags.FLAG_NODE_CACHE );

        attributes.add( CLASSIFIER_CELL_STRESS );

//        flags.putFlag( Keys.concatenate( prefix, CLASSIFIER_CELL_STRESS ), DataFlags.FLAG_PERSIST_ON_FLUSH );
        flags.putFlag( CLASSIFIER_CELL_STRESS, DataFlags.FLAG_NODE_CACHE );

        attributes.add( CLASSIFIER_CELL_AGES );

//        flags.putFlag( Keys.concatenate( prefix, CLASSIFIER_CELL_AGES ), DataFlags.FLAG_PERSIST_ON_FLUSH );
        flags.putFlag( CLASSIFIER_CELL_AGES, DataFlags.FLAG_NODE_CACHE );

        attributes.add( CLASSIFIER_EDGES );

//        flags.putFlag( Keys.concatenate( prefix, CLASSIFIER_EDGES ), DataFlags.FLAG_PERSIST_ON_FLUSH );
        flags.putFlag( CLASSIFIER_EDGES, DataFlags.FLAG_NODE_CACHE );

        attributes.add( CLASSIFIER_EDGES_AGES );

//        flags.putFlag( Keys.concatenate( prefix, CLASSIFIER_EDGES_AGES ), DataFlags.FLAG_PERSIST_ON_FLUSH );
        flags.putFlag( CLASSIFIER_EDGES_AGES, DataFlags.FLAG_NODE_CACHE );

        attributes.add( CLASSIFIER_AGE_SINCE_GROWTH );

//        flags.putFlag( Keys.concatenate( prefix, CLASSIFIER_AGE_SINCE_GROWTH ), DataFlags.FLAG_PERSIST_ON_FLUSH );
        flags.putFlag( CLASSIFIER_AGE_SINCE_GROWTH, DataFlags.FLAG_NODE_CACHE );

    }

    @Override
    public Class getConfigClass() {
        return HqClRegionLayerEntityConfig.class;
    }

    protected void doUpdateSelf() {

        // Do nothing unless the input is defined
        Data inputFf1 = getData( INPUT_FF_1 );
        Data inputFf2 = getData( INPUT_FF_2 );
        Data inputFb1 = getData( INPUT_FB_1 );

        if( ( inputFf1 == null ) || ( inputFf2 == null ) || ( inputFb1 == null ) ) {
            return; // can't update yet.
        }

        // Get all the parameters:
        String regionLayerName = getName();

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Test parameters
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Feedforward size
        Point inputFf1Size = Data2d.getSize( inputFf1 );
        Point inputFf2Size = Data2d.getSize( inputFf2 );
        Point inputFb1Size = Data2d.getSize( inputFb1 );

        int input1Width = inputFf1Size.x;
        int input1Height = inputFf1Size.y;
        int input2Width = inputFf2Size.x;
        int input2Height = inputFb1Size.y;
        int ffInputArea = input1Width * input1Height + input2Width * input2Height;
        int fbInputArea = inputFb1Size.x * inputFb1Size.y;

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Algorithm specific parameters
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Region size
        HqClRegionLayerEntityConfig config = ( HqClRegionLayerEntityConfig ) _config;

        // Build the algorithm
        //RandomInstance.setSeed(randomSeed); // make the tests repeatable
        ObjectMap om = ObjectMap.GetInstance();

        BinaryTreeQuiltConfig organizerConfig = new BinaryTreeQuiltConfig();
        GrowingNeuralGasConfig classifierConfig = new GrowingNeuralGasConfig();

        String organizerName = getKey( HierarchicalQuiltedCompetitiveLearningConfig.ORGANIZER );
        String classifierName = getKey( HierarchicalQuiltedCompetitiveLearningConfig.CLASSIFIER );

        organizerConfig.setup(
            om, organizerName, _r,
            ffInputArea,
            config.regionWidthColumns, config.regionHeightColumns,
            config.intervalsX1, config.intervalsY1,
            config.intervalsX2, config.intervalsY2 );

        classifierConfig.setup(
            om, classifierName, _r,
            ffInputArea,
            config.columnWidthCells, config.columnHeightCells,
            config.classifierLearningRate,
            config.classifierLearningRateNeighbours,
            config.classifierNoiseMagnitude,
            config.classifierEdgeMaxAge,
            config.classifierStressLearningRate,
            config.classifierStressSplitLearningRate,
            config.classifierStressThreshold,
            config.classifierGrowthInterval );

        HierarchicalQuiltedCompetitiveLearningConfig hqclc = new HierarchicalQuiltedCompetitiveLearningConfig();
        hqclc.setup(
            om, regionLayerName, _r,
            organizerConfig,
            classifierConfig,
            input1Width, input1Height,
            input2Width, input2Height,
            fbInputArea,
            config.predictionLearningRate,
            config.predictionDecayRate,
            config.errorHistoryLength,
            config.classifiersPerBit1,
            config.classifiersPerBit2 );

        HierarchicalQuiltedCompetitiveLearning hqcl = new HierarchicalQuiltedCompetitiveLearning( regionLayerName, om );
        hqcl.setup( hqclc );

        // Load data, overwriting the default setup.
        copyDataFromPersistence( hqcl );

        // Update the region-layer, including optional reset and learning on/off switch
        if( config.reset ) {
            hqcl.reset();
        }

        hqcl._config.setLearn( config.learn );
        hqcl.update();

        // Save data
        copyDataToPersistence( hqcl );
    }

    protected void copyDataFromPersistence( HierarchicalQuiltedCompetitiveLearning hqcl ) {

        hqcl._ffInput1 = getData( INPUT_FF_1 );
        hqcl._ffInput2 = getData( INPUT_FF_2 );
        hqcl._fbInput1 = getData( INPUT_FB_1 );

        hqcl._ffInput1Old = getDataLazyResize( INPUT_FF_1_OLD, hqcl._ffInput1Old._dataSize );
        hqcl._ffInput2Old = getDataLazyResize( INPUT_FF_2_OLD, hqcl._ffInput2Old._dataSize );
        hqcl._fbInput1Old = getDataLazyResize( INPUT_FB_1_OLD, hqcl._fbInput1Old._dataSize );

        hqcl._regionActivity = getDataLazyResize( REGION_ACTIVITY, hqcl._regionActivity._dataSize );
        hqcl._regionActivityInferred = getDataLazyResize( REGION_ACTIVITY_INFERRED, hqcl._regionActivityInferred._dataSize );
        hqcl._regionPredictionWeights = getDataLazyResize( REGION_PREDICTION_WEIGHTS, hqcl._regionPredictionWeights._dataSize );
        hqcl._regionPrediction = getDataLazyResize( REGION_PREDICTION, hqcl._regionPrediction._dataSize );
//        hqcl._regionErrorHistory = getDataLazyResize( REGION_ERROR_HISTORY, hqcl._regionErrorHistory._dataSize );
//        hqcl._regionErrorHistoryIndex = getDataLazyResize( REGION_ERROR_HISTORY_INDEX, hqcl._regionErrorHistoryIndex._dataSize );

        Point columnSizeCells = hqcl._config._classifierConfig.getSizeCells();
        int columnInputs = hqcl._config._classifierConfig.getNbrInputs();
        int classifiers = hqcl._config.getOrganizerAreaCells();
        int areaCells = columnSizeCells.x * columnSizeCells.y;

        DataSize dataSizeWeights = DataSize.create( columnSizeCells.x, columnSizeCells.y, columnInputs );
        DataSize dataSizeCells = DataSize.create( columnSizeCells.x, columnSizeCells.y );
        DataSize dataSizeEdges   = DataSize.create( areaCells, areaCells );

        DataSize dataSizeWeightsAll = DataSize.create( dataSizeWeights.getVolume() * classifiers );
        DataSize dataSizeCellsAll   = DataSize.create( dataSizeCells  .getVolume() * classifiers );
        DataSize dataSizeEdgesAll = DataSize.create( dataSizeEdges  .getVolume() * classifiers );

        String prefix = "";//hqcl._config.CLASSIFIER;
        _classifierCellWeights    = getDataLazyResize( CLASSIFIER_CELL_WEIGHTS     , dataSizeWeightsAll );
        _classifierCellErrors     = getDataLazyResize( CLASSIFIER_CELL_ERRORS      , dataSizeCellsAll ); // deep copies the size so they each own a copy
//        _classifierCellActivity   = getDataLazyResize( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_ACTIVE           ), dataSizeCellsAll ); // deep copies the size so they each own a copy
        _classifierCellMask       = getDataLazyResize( CLASSIFIER_CELL_MASK        , dataSizeCellsAll ); // deep copies the size so they each own a copy
        _classifierCellStress     = getDataLazyResize( CLASSIFIER_CELL_STRESS      , dataSizeCellsAll );
        _classifierCellAges       = getDataLazyResize( CLASSIFIER_CELL_AGES        , dataSizeCellsAll );
        _classifierEdges          = getDataLazyResize( CLASSIFIER_EDGES            , dataSizeEdgesAll );
        _classifierEdgesAges      = getDataLazyResize( CLASSIFIER_EDGES_AGES       , dataSizeEdgesAll );
        _classifierAgeSinceGrowth = getDataLazyResize( CLASSIFIER_AGE_SINCE_GROWTH , DataSize.create( classifiers ) );

        for( int c = 0; c < classifiers; ++c ) {
            GrowingNeuralGas classifier = hqcl._classifiers.get( c );

            int weightsSize = dataSizeWeights.getVolume();
            int cellsSize = dataSizeCells.getVolume();
            int edgesSize = dataSizeEdges.getVolume();

            int weightsOffset = weightsSize * c;
            int cellsOffset = cellsSize * c;
            int edgesOffset = edgesSize * c;

            // .copyRange( that, offsetThis, offsetThat, range );
            classifier._cellWeights   .copyRange( _classifierCellWeights   , 0, weightsOffset, weightsSize );
            classifier._cellErrors    .copyRange( _classifierCellErrors    , 0, cellsOffset, cellsSize );
//            classifier._cellActivity  .copyRange( classifierCellActivity  , 0, cellsOffset, cellsSize );
            classifier._cellMask      .copyRange( _classifierCellMask      , 0, cellsOffset, cellsSize );
            classifier._cellStress    .copyRange( _classifierCellStress    , 0, cellsOffset, cellsSize );
            classifier._cellAges      .copyRange( _classifierCellAges      , 0, cellsOffset, cellsSize );
            classifier._edges         .copyRange( _classifierEdges         , 0, edgesOffset, edgesSize );
            classifier._edgesAges     .copyRange( _classifierEdgesAges     , 0, edgesOffset, edgesSize );
            classifier._ageSinceGrowth.copyRange( _classifierAgeSinceGrowth, 0, c, 1 );

        }

    }

    protected void copyDataToPersistence( HierarchicalQuiltedCompetitiveLearning hqcl ) {

        setData( INPUT_FF_1_OLD, hqcl._ffInput1Old );
        setData( INPUT_FF_2_OLD, hqcl._ffInput2Old );
        setData( INPUT_FB_1_OLD, hqcl._fbInput1Old );

        setData( REGION_ACTIVITY, hqcl._regionActivity );
        setData( REGION_ACTIVITY_INFERRED, hqcl._regionActivityInferred );
        setData( REGION_PREDICTION_WEIGHTS, hqcl._regionPredictionWeights );
        setData( REGION_PREDICTION, hqcl._regionPrediction );
        setData( REGION_LIKELIHOOD, hqcl._regionLikelihood );
//        setData( REGION_ERROR_HISTORY, hqcl._regionErrorHistory );
//        setData( REGION_ERROR_HISTORY_INDEX, hqcl._regionErrorHistoryIndex );

        setData( ORGANIZER_CELL_MASK, hqcl._organizer._cellMask );
        setData( ORGANIZER_CELL_WEIGHTS, hqcl._organizer._cellWeights );

        // 1. pack the data:
        int classifiers = hqcl._config.getOrganizerAreaCells();
        for( int c = 0; c < classifiers; ++c ) {
            GrowingNeuralGas classifier = hqcl._classifiers.get( c );

            int weightsSize = classifier._cellWeights.getSize();
            int cellsSize = classifier._cellErrors.getSize();
            int edgesSize = classifier._edges.getSize();

            int weightsOffset = weightsSize * c;
            int cellsOffset = cellsSize * c;
            int edgesOffset = edgesSize * c;

            // .copyRange( that, offsetThis, offsetThat, range );
            _classifierCellWeights   .copyRange( classifier._cellWeights   , weightsOffset, 0, weightsSize );
            _classifierCellErrors    .copyRange( classifier._cellErrors    , cellsOffset, 0, cellsSize );
//            _classifierCellActivity  .copyRange( classifier._cellActivity  , cellsOffset, 0, cellsSize );
            _classifierCellMask      .copyRange( classifier._cellMask      , cellsOffset, 0, cellsSize );
            _classifierCellStress    .copyRange( classifier._cellStress    , cellsOffset, 0, cellsSize );
            _classifierCellAges      .copyRange( classifier._cellAges      , cellsOffset, 0, cellsSize );
            _classifierEdges         .copyRange( classifier._edges         , edgesOffset, 0, edgesSize );
            _classifierEdgesAges     .copyRange( classifier._edgesAges     , edgesOffset, 0, edgesSize );
            _classifierAgeSinceGrowth.copyRange( classifier._ageSinceGrowth, c, 0, 1 );
        }

        // 2. Store the packed data.
        String prefix = "";//hqcl._config.CLASSIFIER;
        setData( CLASSIFIER_CELL_WEIGHTS, _classifierCellWeights );
        setData( CLASSIFIER_CELL_ERRORS, _classifierCellErrors );
//        setData( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_ACTIVE ), _classifierCellActivity );
        setData( CLASSIFIER_CELL_MASK, _classifierCellMask );

        setData( CLASSIFIER_CELL_STRESS, _classifierCellStress );
        setData( CLASSIFIER_CELL_AGES, _classifierCellAges );
        setData( CLASSIFIER_EDGES, _classifierEdges );
        setData( CLASSIFIER_EDGES_AGES, _classifierEdgesAges );
        setData( CLASSIFIER_AGE_SINCE_GROWTH, _classifierAgeSinceGrowth );

    }
*/
}
