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
import io.agi.core.alg.QuiltedCompetitiveLearning;
import io.agi.core.alg.QuiltedCompetitiveLearningConfig;
import io.agi.core.ann.unsupervised.GrowingNeuralGas;
import io.agi.core.ann.unsupervised.GrowingNeuralGasConfig;
import io.agi.core.ann.unsupervised.HierarchicalQuiltConfig;
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
public class QuiltedCompetitiveLearningEntity extends Entity {

    public static final String ENTITY_TYPE = "quilted-competitive-learning";

    public static final String INPUT = "input";

    public static final String QUILT_ACTIVITY = "quilt-activity";

    public static final String ORGANIZER_CELL_MASK = "organizer-cell-mask";
    public static final String ORGANIZER_CELL_WEIGHTS = "organizer-cell-weights";

    public static final String CLASSIFIER_CELL_ACTIVITY = "classifier-cell-activity";
    public static final String CLASSIFIER_CELL_WEIGHTS = "classifier-cell-weights";
    public static final String CLASSIFIER_CELL_ERRORS = "classifier-cell-errors";
    public static final String CLASSIFIER_CELL_MASK = "classifier-cell-mask";

    public static final String CLASSIFIER_CELL_STRESS = "classifier-cell-stress";
    public static final String CLASSIFIER_CELL_AGES = "classifier-cell-ages";
    public static final String CLASSIFIER_EDGES = "classifier-edges";
    public static final String CLASSIFIER_EDGES_AGES = "classifier-edges-ages";
    public static final String CLASSIFIER_AGE_SINCE_GROWTH = "classifier-age-since-growth";

    // concatenated data:
    protected Data _classifierCellActivity;
    protected Data _classifierCellWeights;
    protected Data _classifierCellErrors;
    protected Data _classifierCellMask;

    protected Data _classifierCellStress;
    protected Data _classifierCellAges;
    protected Data _classifierEdges;
    protected Data _classifierEdgesAges;
    protected Data _classifierAgeSinceGrowth;

    public QuiltedCompetitiveLearningEntity(ObjectMap om, Node n, ModelEntity model) {
        super(om, n, model);
    }

    public void getInputAttributes( Collection< String > attributes ) {
        attributes.add(INPUT);
    }

    public void getOutputAttributes( Collection< String > attributes, DataFlags flags ) {

        attributes.add(QUILT_ACTIVITY);

        flags.putFlag(QUILT_ACTIVITY, DataFlags.FLAG_SPARSE_BINARY);
        flags.putFlag( QUILT_ACTIVITY, DataFlags.FLAG_NODE_CACHE );

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

        attributes.add(CLASSIFIER_CELL_ACTIVITY);
        flags.putFlag(CLASSIFIER_CELL_ACTIVITY, DataFlags.FLAG_NODE_CACHE);

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
        return QuiltedCompetitiveLearningEntityConfig.class;
    }

    protected void doUpdateSelf() {

        // Do nothing unless the input is defined
        Data input = getData( INPUT );

        if( input == null ) {
            return; // can't update yet.
        }

        // Get all the parameters:
        String regionLayerName = getName();

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Test parameters
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Feedforward size
        Point inputSize = Data2d.getSize( input );

        int inputWidth = inputSize.x;
        int inputHeight = inputSize.y;
        int inputArea = inputWidth * inputHeight;

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Algorithm specific parameters
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Region size
        QuiltedCompetitiveLearningEntityConfig config = ( QuiltedCompetitiveLearningEntityConfig ) _config;

        // Build the algorithm
        //RandomInstance.setSeed(randomSeed); // make the tests repeatable
        ObjectMap om = ObjectMap.GetInstance();

        HierarchicalQuiltConfig organizerConfig = new HierarchicalQuiltConfig();
        GrowingNeuralGasConfig classifierConfig = new GrowingNeuralGasConfig();

        String organizerName = getKey( QuiltedCompetitiveLearningConfig.ORGANIZER );
        String classifierName = getKey( QuiltedCompetitiveLearningConfig.CLASSIFIER );

        organizerConfig.setup(
            om, organizerName, _r,
            inputArea,
            config.quiltWidthColumns, config.quiltHeightColumns,
            config.intervalsX1, config.intervalsY1,
            config.intervalsX2, config.intervalsY2 );

        classifierConfig.setup(
            om, classifierName, _r,
            inputArea,
            config.columnWidthCells, config.columnHeightCells,
            config.classifierLearningRate,
            config.classifierLearningRateNeighbours,
            config.classifierNoiseMagnitude,
            config.classifierEdgeMaxAge,
            config.classifierStressLearningRate,
            config.classifierStressSplitLearningRate,
            config.classifierStressThreshold,
            config.classifierGrowthInterval );

        QuiltedCompetitiveLearningConfig qclc = new QuiltedCompetitiveLearningConfig();
        qclc.setup(
            om, regionLayerName, _r,
            organizerConfig,
            classifierConfig,
            inputWidth, inputHeight,
            config.classifiersPerBit );

        QuiltedCompetitiveLearning qcl = new QuiltedCompetitiveLearning( regionLayerName, om );
        qcl.setup( qclc );

        // Load data, overwriting the default setup.
        copyDataFromPersistence( qcl );

        // Update the region-layer, including optional reset and learning on/off switch
        if( config.reset ) {
            qcl.reset();
        }

        qcl._config.setLearn( config.learn );
        qcl.update();

        // Save data
        copyDataToPersistence( qcl );
    }

    protected void copyDataFromPersistence( QuiltedCompetitiveLearning hqcl ) {

        hqcl._input = getData( INPUT );
        hqcl._quilt = getDataLazyResize( QUILT_ACTIVITY, hqcl._quilt._dataSize );

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

        _classifierCellActivity   = getDataLazyResize( CLASSIFIER_CELL_ACTIVITY    , dataSizeCellsAll ); // deep copies the size so they each own a copy
        _classifierCellWeights    = getDataLazyResize( CLASSIFIER_CELL_WEIGHTS     , dataSizeWeightsAll );
        _classifierCellErrors     = getDataLazyResize( CLASSIFIER_CELL_ERRORS      , dataSizeCellsAll ); // deep copies the size so they each own a copy
        _classifierCellMask       = getDataLazyResize( CLASSIFIER_CELL_MASK        , dataSizeCellsAll ); // deep copies the size so they each own a copy
        _classifierCellStress     = getDataLazyResize( CLASSIFIER_CELL_STRESS      , dataSizeCellsAll );
        _classifierCellAges       = getDataLazyResize( CLASSIFIER_CELL_AGES        , dataSizeCellsAll );
        _classifierEdges          = getDataLazyResize( CLASSIFIER_EDGES            , dataSizeEdgesAll );
        _classifierEdgesAges      = getDataLazyResize( CLASSIFIER_EDGES_AGES       , dataSizeEdgesAll );
        _classifierAgeSinceGrowth = getDataLazyResize( CLASSIFIER_AGE_SINCE_GROWTH , DataSize.create( classifiers ) );

/*        for( int c = 0; c < classifiers; ++c ) {
            GrowingNeuralGas classifier = hqcl._classifiers.get( c );

            int weightsSize = dataSizeWeights.getVolume();
            int cellsSize = dataSizeCells.getVolume();
            int edgesSize = dataSizeEdges.getVolume();

            int weightsOffset = weightsSize * c;
            int cellsOffset = cellsSize * c;
            int edgesOffset = edgesSize * c;

            // .copyRange( that, offsetThis, offsetThat, range );
            classifier._cellWeights   .copyRange(_classifierCellWeights, 0, weightsOffset, weightsSize);
            classifier._cellErrors    .copyRange(_classifierCellErrors, 0, cellsOffset, cellsSize);
            classifier._cellActivity  .copyRange(_classifierCellActivity, 0, cellsOffset, cellsSize);
            classifier._cellMask      .copyRange( _classifierCellMask      , 0, cellsOffset, cellsSize );
            classifier._cellStress    .copyRange( _classifierCellStress    , 0, cellsOffset, cellsSize );
            classifier._cellAges      .copyRange( _classifierCellAges      , 0, cellsOffset, cellsSize );
            classifier._edges         .copyRange( _classifierEdges         , 0, edgesOffset, edgesSize );
            classifier._edgesAges     .copyRange( _classifierEdgesAges     , 0, edgesOffset, edgesSize );
            classifier._ageSinceGrowth.copyRange( _classifierAgeSinceGrowth, 0, c, 1 );
        }*/

        Point organizerSize = hqcl._config.getOrganizerSizeCells();
        Point classifierSize = hqcl._config.getClassifierSizeCells();

        int weightsSize = hqcl._input.getSize();

        for( int yCol = 0; yCol < organizerSize.y; ++yCol ) {
            for( int xCol = 0; xCol < organizerSize.x; ++xCol ) {
                int classifierOffset = hqcl._config.getOrganizerOffset( xCol, yCol );
                GrowingNeuralGas classifier = hqcl._classifiers.get( classifierOffset );

                for( int yCell = 0; yCell < classifierSize.y; ++yCell ) {
                    for (int xCell = 0; xCell < classifierSize.x; ++xCell ) {

                        int xQuilt = xCol * classifierSize.x + xCell;
                        int yQuilt = yCol * classifierSize.y + yCell;
                        int wQuilt = classifierSize.x * organizerSize.x;

                        int cellOffsetQuilt = yQuilt * wQuilt + xQuilt;
                        int cellOffsetCol = yCell * classifierSize.x + xCell;
                        int weightsOffsetQuilt = cellOffsetQuilt * weightsSize;
                        int weightsOffsetCol = cellOffsetCol * weightsSize;

                        classifier._cellWeights   .copyRange( _classifierCellWeights, weightsOffsetCol, weightsOffsetQuilt, weightsSize);

                        classifier._cellErrors._values[ cellOffsetCol ] = _classifierCellErrors._values[ cellOffsetQuilt ];
                        classifier._cellActivity._values[ cellOffsetCol ] = _classifierCellActivity._values[ cellOffsetQuilt ];
                        classifier._cellMask._values[ cellOffsetCol ] = _classifierCellMask._values[ cellOffsetQuilt ];
                        classifier._cellStress._values[ cellOffsetCol ] = _classifierCellStress._values[ cellOffsetQuilt ];
                        classifier._cellAges._values[ cellOffsetCol ] = _classifierCellAges._values[ cellOffsetQuilt ];
                    }
                }

                classifier._ageSinceGrowth.copyRange( _classifierAgeSinceGrowth, 0, classifierOffset, 1 );

                int edgesSize = classifier._edges.getSize();
                int edgesOffset = edgesSize * classifierOffset;

                // NOTE: These are not quilted
                classifier._edges         .copyRange( _classifierEdges, 0, edgesOffset, edgesSize);
                classifier._edgesAges     .copyRange( _classifierEdgesAges, 0, edgesOffset, edgesSize);
            }
        }

    }

    protected void copyDataToPersistence( QuiltedCompetitiveLearning hqcl ) {

        setData( QUILT_ACTIVITY, hqcl._quilt );

        setData( ORGANIZER_CELL_MASK, hqcl._organizer._cellMask );
        setData( ORGANIZER_CELL_WEIGHTS, hqcl._organizer._cellWeights );

        // 1. pack the data:
        Point organizerSize = hqcl._config.getOrganizerSizeCells();
        Point classifierSize = hqcl._config.getClassifierSizeCells();

        int weightsSize = hqcl._input.getSize();

        for( int yCol = 0; yCol < organizerSize.y; ++yCol ) {
            for( int xCol = 0; xCol < organizerSize.x; ++xCol ) {
                int classifierOffset = hqcl._config.getOrganizerOffset( xCol, yCol );
                GrowingNeuralGas classifier = hqcl._classifiers.get( classifierOffset );

                for( int yCell = 0; yCell < classifierSize.y; ++yCell ) {
                    for (int xCell = 0; xCell < classifierSize.x; ++xCell ) {

                        int xQuilt = xCol * classifierSize.x + xCell;
                        int yQuilt = yCol * classifierSize.y + yCell;
                        int wQuilt = classifierSize.x * organizerSize.x;

                        int cellOffsetQuilt = yQuilt * wQuilt + xQuilt;
                        int cellOffsetCol = yCell * classifierSize.x + xCell;
                        int weightsOffsetQuilt = cellOffsetQuilt * weightsSize;
                        int weightsOffsetCol = cellOffsetCol * weightsSize;

                        _classifierCellWeights.copyRange(classifier._cellWeights, weightsOffsetQuilt, weightsOffsetCol, weightsSize );

                        _classifierCellErrors._values[ cellOffsetQuilt ] = classifier._cellErrors._values[ cellOffsetCol ];
                        _classifierCellActivity._values[ cellOffsetQuilt ] = classifier._cellActivity._values[ cellOffsetCol ];
                        _classifierCellMask._values[ cellOffsetQuilt ] = classifier._cellMask._values[ cellOffsetCol ];
                        _classifierCellStress._values[ cellOffsetQuilt ] = classifier._cellStress._values[ cellOffsetCol ];
                        _classifierCellAges._values[ cellOffsetQuilt ] = classifier._cellAges._values[ cellOffsetCol ];
                    }
                }

                _classifierAgeSinceGrowth.copyRange( classifier._ageSinceGrowth, classifierOffset, 0, 1 );

                int edgesSize = classifier._edges.getSize();
                int edgesOffset = edgesSize * classifierOffset;

                // NOTE: These are not quilted
                _classifierEdges         .copyRange( classifier._edges, edgesOffset, 0, edgesSize );
                _classifierEdgesAges     .copyRange( classifier._edgesAges, edgesOffset, 0, edgesSize );
            }
        }

/*        int classifiers = hqcl._config.getOrganizerAreaCells();
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
            _classifierCellErrors    .copyRange(classifier._cellErrors, cellsOffset, 0, cellsSize);
            _classifierCellActivity  .copyRange(classifier._cellActivity, cellsOffset, 0, cellsSize);
            _classifierCellMask      .copyRange( classifier._cellMask      , cellsOffset, 0, cellsSize );
            _classifierCellStress    .copyRange( classifier._cellStress    , cellsOffset, 0, cellsSize );
            _classifierCellAges      .copyRange( classifier._cellAges      , cellsOffset, 0, cellsSize );
            _classifierEdges         .copyRange( classifier._edges         , edgesOffset, 0, edgesSize );
            _classifierEdgesAges     .copyRange( classifier._edgesAges     , edgesOffset, 0, edgesSize );
            _classifierAgeSinceGrowth.copyRange( classifier._ageSinceGrowth, c, 0, 1 );
        }*/

        // 2. Store the packed data.
        String prefix = "";//hqcl._config.CLASSIFIER;
        setData( CLASSIFIER_CELL_ACTIVITY, _classifierCellActivity );
        setData( CLASSIFIER_CELL_WEIGHTS, _classifierCellWeights );
        setData( CLASSIFIER_CELL_ERRORS, _classifierCellErrors );
        setData( CLASSIFIER_CELL_MASK, _classifierCellMask );

        setData( CLASSIFIER_CELL_STRESS, _classifierCellStress );
        setData( CLASSIFIER_CELL_AGES, _classifierCellAges );
        setData( CLASSIFIER_EDGES, _classifierEdges );
        setData( CLASSIFIER_EDGES_AGES, _classifierEdgesAges );
        setData( CLASSIFIER_AGE_SINCE_GROWTH, _classifierAgeSinceGrowth );

    }
}
