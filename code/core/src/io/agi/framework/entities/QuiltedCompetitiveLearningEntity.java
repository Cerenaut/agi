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

import io.agi.core.alg.QuiltedCompetitiveLearning;
import io.agi.core.alg.QuiltedCompetitiveLearningConfig;
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
public class QuiltedCompetitiveLearningEntity extends Entity {

    public static final String ENTITY_TYPE = "quilted-competitive-learning";

    public static final String INPUT_1 = "input-1";
    public static final String INPUT_2 = "input-2";
    public static final String INPUT_QUILT = "input-quilt";
    public static final String OUTPUT_QUILT = "output-quilt";
    public static final String OUTPUT_1 = "output-1";
    public static final String OUTPUT_2 = "output-2";

    public static final String QUILT_MASK = "quilt-mask";
    public static final String QUILT_INPUT_MASK = "quilt-input-mask";

    public static final String CLASSIFIER_CELL_ACTIVITY = "classifier-cell-activity";
    public static final String CLASSIFIER_CELL_WEIGHTS = "classifier-cell-weights";
    public static final String CLASSIFIER_CELL_ERRORS = "classifier-cell-errors";
    public static final String CLASSIFIER_CELL_MASK = "classifier-cell-mask";

    public static final String CLASSIFIER_CELL_STRESS = "classifier-cell-stress";
    public static final String CLASSIFIER_CELL_AGES = "classifier-cell-ages";
    public static final String CLASSIFIER_EDGES = "classifier-edges";
    public static final String CLASSIFIER_EDGES_AGES = "classifier-edges-ages";
    public static final String CLASSIFIER_AGE_SINCE_GROWTH = "classifier-age-since-growth";

    // concatenated data over all classifiers:
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
        super( om, n, model );
    }

    public void getInputAttributes( Collection< String > attributes ) {
        attributes.add( INPUT_1 );
        attributes.add( INPUT_2 );
        attributes.add( INPUT_QUILT );
    }

    public void getOutputAttributes( Collection< String > attributes, DataFlags flags ) {

        attributes.add( OUTPUT_1 );
        attributes.add( OUTPUT_2 );
        attributes.add( OUTPUT_QUILT );

        flags.putFlag( OUTPUT_1, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( OUTPUT_2, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( OUTPUT_QUILT, DataFlags.FLAG_NODE_CACHE );

        attributes.add( QUILT_MASK );
        attributes.add( QUILT_INPUT_MASK );

        flags.putFlag( OUTPUT_QUILT, DataFlags.FLAG_SPARSE_BINARY );
        flags.putFlag( QUILT_INPUT_MASK, DataFlags.FLAG_SPARSE_BINARY );

        flags.putFlag( QUILT_MASK, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( QUILT_INPUT_MASK, DataFlags.FLAG_NODE_CACHE );

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

        QuiltedCompetitiveLearningEntityConfig config = (QuiltedCompetitiveLearningEntityConfig) _config;

        // Do nothing unless the input is defined
        Data input1 = getData( INPUT_1 );
        Data input2 = getData( INPUT_2 );
        Data inputQ = getData( INPUT_QUILT );

        if( ( input1 == null ) || ( input2 == null ) ) {
            int quiltWidthCells  = config.quiltWidth  * config.classifierWidth;
            int quiltHeightCells = config.quiltHeight * config.classifierHeight;

            Data quiltCells = new Data( quiltWidthCells, quiltHeightCells );

            setData( OUTPUT_QUILT, quiltCells );

            if( config.reset ) {
                config.resetDelayed = true;
            }

            return; // can't update yet.
        }

        // Get all the parameters:
        String entityName = getName();

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Test parameters
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Feedforward size
        Point input1Size = Data2d.getSize( input1 );
        Point input2Size = Data2d.getSize( input2 );

        int input1Width = input1Size.x;
        int input2Width = input2Size.x;
        int input1Height = input1Size.y;
        int input2Height = input2Size.y;
        int input1Area = input1Width * input1Height;
        int input2Area = input2Width * input2Height;
        int inputArea = input1Area + input2Area;

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Algorithm specific parameters
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        ObjectMap om = ObjectMap.GetInstance();

        BinaryTreeQuiltConfig quiltConfig = new BinaryTreeQuiltConfig();
        GrowingNeuralGasConfig classifierConfig = new GrowingNeuralGasConfig();

        String organizerName = getKey( QuiltedCompetitiveLearningConfig.QUILT );
        String classifierName = getKey( QuiltedCompetitiveLearningConfig.CLASSIFIER );

        quiltConfig.setup(
            om, organizerName, _r,
            config.quiltWidth, config.quiltHeight,
            input1Width, input1Height,
            input2Width, input2Height,
            config.field1StrideX, config.field1StrideY,
            config.field2StrideX, config.field2StrideY,
            config.field1SizeX, config.field1SizeY,
            config.field2SizeX, config.field2SizeY );

        classifierConfig.setup(
            om, classifierName, _r,
            inputArea,
            config.classifierWidth, config.classifierHeight,
            config.classifierLearningRate,
            config.classifierLearningRateNeighbours,
            config.classifierNoiseMagnitude,
            config.classifierEdgeMaxAge,
            config.classifierStressLearningRate,
            config.classifierStressSplitLearningRate,
            config.classifierStressThreshold,
            config.classifierGrowthInterval );

        QuiltedCompetitiveLearningConfig qclc = new QuiltedCompetitiveLearningConfig();
        qclc.setup( om, entityName, _r, quiltConfig, classifierConfig );

        QuiltedCompetitiveLearning qcl = new QuiltedCompetitiveLearning( entityName, om );
        qcl.setup( qclc );

        // Load data, overwriting the default setup.
        qcl._input1 = input1;
        qcl._input2 = input2;

        copyDataFromPersistence( qcl );

        // Update the classification (forward) output, including optional reset and learning on/off switch
        if( config.reset || config.resetDelayed ) {
            qcl.reset();
            config.resetDelayed = false;
        }

        qcl._config.setLearn( config.learn );
        qcl.update();

        // update the inverted output
        Data output1 = new Data( input1._dataSize );
        Data output2 = new Data( input2._dataSize );

        if( inputQ == null ) {
            inputQ = new Data( qcl._quiltCells._dataSize ); // produce an output even if not given input
        }

        qcl.invert( inputQ, output1, output2 );

        setData( OUTPUT_1, output1 );
        setData( OUTPUT_2, output2 );

        // Save data
        copyDataToPersistence( qcl );

        // Save config changes - eg stats about the entity
    }

    protected void copyDataFromPersistence( QuiltedCompetitiveLearning hqcl ) {

        Point columnSizeCells = hqcl._config._classifierConfig.getSizeCells();
        int columnInputs = hqcl._config._classifierConfig.getNbrInputs();
        int classifiers = hqcl._config._quiltConfig.getQuiltArea();
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

        Point quiltSize = hqcl._config._quiltConfig.getQuiltSize();
        Point classifierSize = hqcl._config._classifierConfig.getSizeCells();

        int weightsSize = hqcl._input.getSize();

        for( int yCol = 0; yCol < quiltSize.y; ++yCol ) {
            for( int xCol = 0; xCol < quiltSize.x; ++xCol ) {
                int classifierOffset = hqcl._config._quiltConfig.getQuiltOffset( xCol, yCol );
                GrowingNeuralGas classifier = hqcl._classifiers.get( classifierOffset );

                for( int yCell = 0; yCell < classifierSize.y; ++yCell ) {
                    for (int xCell = 0; xCell < classifierSize.x; ++xCell ) {

                        int xQuilt = xCol * classifierSize.x + xCell;
                        int yQuilt = yCol * classifierSize.y + yCell;
                        int wQuilt = classifierSize.x * quiltSize.x;

                        int cellOffsetQuilt = yQuilt * wQuilt + xQuilt;
                        int cellOffsetCol = yCell * classifierSize.x + xCell;
                        int weightsOffsetQuilt = cellOffsetQuilt * weightsSize;
                        int weightsOffsetCol = cellOffsetCol * weightsSize;

                        classifier._cellWeights.copyRange( _classifierCellWeights, weightsOffsetCol, weightsOffsetQuilt, weightsSize );

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

        setData( OUTPUT_QUILT, hqcl._quiltCells );

        setData( QUILT_MASK, hqcl._quilt._quiltMask );
        setData( QUILT_INPUT_MASK, hqcl._quilt._quiltInputMask );

        // 1. pack the data:
        Point quiltSize = hqcl._config._quiltConfig.getQuiltSize();
        Point classifierSize = hqcl._config._classifierConfig.getSizeCells();

        int weightsSize = hqcl._input.getSize();

        for( int yCol = 0; yCol < quiltSize.y; ++yCol ) {
            for( int xCol = 0; xCol < quiltSize.x; ++xCol ) {
                int classifierOffset = hqcl._config._quiltConfig.getQuiltOffset( xCol, yCol );
                GrowingNeuralGas classifier = hqcl._classifiers.get( classifierOffset );

                for( int yCell = 0; yCell < classifierSize.y; ++yCell ) {
                    for (int xCell = 0; xCell < classifierSize.x; ++xCell ) {

                        int xQuilt = xCol * classifierSize.x + xCell;
                        int yQuilt = yCol * classifierSize.y + yCell;
                        int wQuilt = classifierSize.x * quiltSize.x;

                        int cellOffsetQuilt = yQuilt * wQuilt + xQuilt;
                        int cellOffsetCol = yCell * classifierSize.x + xCell;
                        int weightsOffsetQuilt = cellOffsetQuilt * weightsSize;
                        int weightsOffsetCol = cellOffsetCol * weightsSize;

                        _classifierCellWeights.copyRange( classifier._cellWeights, weightsOffsetQuilt, weightsOffsetCol, weightsSize );

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
