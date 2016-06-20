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

import io.agi.core.alg.*;
import io.agi.core.ann.unsupervised.DynamicSelfOrganizingMap;
import io.agi.core.ann.unsupervised.GrowingNeuralGas;
import io.agi.core.ann.unsupervised.ParameterLessSelfOrganizingMap;
import io.agi.core.data.Data;
import io.agi.core.data.Data2d;
import io.agi.core.data.DataSize;
import io.agi.core.orm.Keys;
import io.agi.core.orm.ObjectMap;
import io.agi.framework.DataFlags;
import io.agi.framework.Entity;
import io.agi.framework.Node;
import io.agi.framework.persistence.models.ModelEntity;

import java.awt.*;
import java.util.Collection;

/**
 * Wraps the RegionLayer concept as a single Entity.
 * <p/>
 * Created by dave on 12/03/16.
 */
public class RegionLayerEntity extends Entity {

    public static final String ENTITY_TYPE = "region-layer";

    public static final String FF_INPUT_1     = "ff-input-1";
    public static final String FF_INPUT_1_OLD = "ff-input-1-old";
    public static final String FF_INPUT_2     = "ff-input-2";
    public static final String FF_INPUT_2_OLD = "ff-input-2-old";
    public static final String FB_INPUT     = "fb-input";
    public static final String FB_INPUT_OLD = "fb-input-old";

    public static final String FB_OUTPUT_1_UNFOLDED_ACTIVITY_RAW   = "fb-output-1-unfolded-activity-raw";
    public static final String FB_OUTPUT_1_UNFOLDED_ACTIVITY       = "fb-output-1-unfolded-activity";
    public static final String FB_OUTPUT_1_UNFOLDED_PREDICTION_RAW = "fb-output-1-unfolded-prediction-raw";
    public static final String FB_OUTPUT_1_UNFOLDED_PREDICTION     = "fb-output-1-unfolded-prediction";

    public static final String FB_OUTPUT_2_UNFOLDED_ACTIVITY_RAW   = "fb-output-2-unfolded-activity-raw";
    public static final String FB_OUTPUT_2_UNFOLDED_ACTIVITY       = "fb-output-2-unfolded-activity";
    public static final String FB_OUTPUT_2_UNFOLDED_PREDICTION_RAW = "fb-output-2-unfolded-prediction-raw";
    public static final String FB_OUTPUT_2_UNFOLDED_PREDICTION     = "fb-output-2-unfolded-prediction";

    public static final String ACTIVITY_OLD = "activity-old";
    public static final String ACTIVITY_NEW = "activity-new";
    public static final String ACTIVITY     = "activity";

    public static final String PREDICTION_OLD = "prediction-old";
    public static final String PREDICTION_NEW = "prediction-new";
    public static final String PREDICTION_RAW = "prediction-raw";

    public static final String PREDICTION_FP = "prediction-fp";
    public static final String PREDICTION_FN = "prediction-fn";
    public static final String PREDICTION_INHIBITION = "prediction-inhibition";

    public static final String PREDICTOR_CONTEXTS = "predictor-contexts";
    public static final String PREDICTOR_WEIGHTS = "predictor-weights";

    public static final String ORGANIZER_BOUNDS = "organizer-bounds";

    public static final String LOG_FN_COUNT = "log-fn-count";

    protected Data _classifierCellWeights;
    protected Data _classifierCellErrors;
    protected Data _classifierCellActivity;
    protected Data _classifierCellMask;

    protected Data _classifierCellStress;
    protected Data _classifierCellAges;
    protected Data _classifierEdges;
    protected Data _classifierEdgesAges;
    protected Data _classifierAgeSinceGrowth;

    public RegionLayerEntity( ObjectMap om, Node n, ModelEntity model ) {
        super( om, n, model );
    }

    public void getInputAttributes( Collection< String > attributes ) {
        attributes.add( FF_INPUT_1 );
        attributes.add( FF_INPUT_2 );
        attributes.add( FB_INPUT );
//        attributes.add( PREDICTION_INHIBITION );
    }

    public void getOutputAttributes( Collection< String > attributes, DataFlags flags ) {

        attributes.add( LOG_FN_COUNT );

        attributes.add( FB_OUTPUT_1_UNFOLDED_ACTIVITY_RAW );
        attributes.add( FB_OUTPUT_1_UNFOLDED_ACTIVITY );
        attributes.add( FB_OUTPUT_1_UNFOLDED_PREDICTION_RAW );
        attributes.add( FB_OUTPUT_1_UNFOLDED_PREDICTION );

        attributes.add( FB_OUTPUT_2_UNFOLDED_ACTIVITY_RAW );
        attributes.add( FB_OUTPUT_2_UNFOLDED_ACTIVITY );
        attributes.add( FB_OUTPUT_2_UNFOLDED_PREDICTION_RAW );
        attributes.add( FB_OUTPUT_2_UNFOLDED_PREDICTION );

        flags.putFlag( FB_OUTPUT_1_UNFOLDED_ACTIVITY, DataFlags.FLAG_PERSIST_ONLY ); // never read
        flags.putFlag( FB_OUTPUT_2_UNFOLDED_ACTIVITY, DataFlags.FLAG_SPARSE_BINARY );

        flags.putFlag( FB_OUTPUT_1_UNFOLDED_PREDICTION, DataFlags.FLAG_PERSIST_ONLY ); // never read
        flags.putFlag( FB_OUTPUT_2_UNFOLDED_PREDICTION, DataFlags.FLAG_SPARSE_BINARY );

        attributes.add( FB_INPUT_OLD );
        attributes.add( FF_INPUT_1_OLD );
        attributes.add( FF_INPUT_2_OLD );

        flags.putFlag( FF_INPUT_1_OLD, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( FF_INPUT_2_OLD, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( FF_INPUT_1_OLD, DataFlags.FLAG_SPARSE_BINARY );
        flags.putFlag( FF_INPUT_2_OLD, DataFlags.FLAG_SPARSE_BINARY );

        flags.putFlag( FB_INPUT_OLD, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( FB_INPUT_OLD, DataFlags.FLAG_SPARSE_BINARY );

        attributes.add( ACTIVITY_OLD );
        attributes.add( ACTIVITY_NEW );
        attributes.add( ACTIVITY );

        flags.putFlag( ACTIVITY_OLD, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( ACTIVITY_NEW, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( ACTIVITY,     DataFlags.FLAG_NODE_CACHE );

        flags.putFlag( ACTIVITY_OLD, DataFlags.FLAG_SPARSE_BINARY );
        flags.putFlag( ACTIVITY_NEW, DataFlags.FLAG_SPARSE_BINARY );
        flags.putFlag( ACTIVITY,     DataFlags.FLAG_SPARSE_BINARY );

        flags.putFlag( ACTIVITY_OLD, DataFlags.FLAG_LAZY_PERSIST );
        flags.putFlag( ACTIVITY_NEW, DataFlags.FLAG_LAZY_PERSIST );
        flags.putFlag( ACTIVITY, DataFlags.FLAG_LAZY_PERSIST );

        attributes.add( PREDICTION_OLD );
        attributes.add( PREDICTION_NEW );
        attributes.add( PREDICTION_RAW );

        flags.putFlag( PREDICTION_OLD, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( PREDICTION_NEW, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( PREDICTION_RAW, DataFlags.FLAG_NODE_CACHE );

        flags.putFlag( PREDICTION_OLD, DataFlags.FLAG_SPARSE_BINARY );
        flags.putFlag( PREDICTION_NEW, DataFlags.FLAG_SPARSE_BINARY );
        flags.putFlag( PREDICTION_RAW, DataFlags.FLAG_SPARSE_REAL );

        attributes.add( PREDICTION_FP );
        attributes.add( PREDICTION_FN );
        attributes.add( PREDICTION_INHIBITION );

        flags.putFlag( PREDICTION_FP, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( PREDICTION_FN, DataFlags.FLAG_NODE_CACHE );

        flags.putFlag( PREDICTION_FP, DataFlags.FLAG_SPARSE_BINARY );
        flags.putFlag( PREDICTION_FN, DataFlags.FLAG_SPARSE_BINARY );

        // Predictor:
        attributes.add( PREDICTOR_CONTEXTS );
        attributes.add( PREDICTOR_WEIGHTS );

        flags.putFlag( PREDICTOR_CONTEXTS, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( PREDICTOR_CONTEXTS, DataFlags.FLAG_SPARSE_BINARY );

        flags.putFlag( PREDICTOR_WEIGHTS, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( PREDICTOR_WEIGHTS, DataFlags.FLAG_PERSIST_ON_FLUSH );
        flags.putFlag( PREDICTOR_WEIGHTS, DataFlags.FLAG_SPARSE_REAL ); // drops from about 2.2 to 1.5 sec.

        // The organizer
        getClassifierOutputKeys( attributes, flags, RegionConfig.SUFFIX_ORGANIZER );//, false );

        attributes.add( ORGANIZER_BOUNDS );

        // The classifiers
        RegionLayerEntityConfig config = ( RegionLayerEntityConfig ) _config;

        String prefix = RegionConfig.SUFFIX_CLASSIFIER;
        getClassifierOutputKeys( attributes, flags, prefix );
    }

    public void getClassifierOutputKeys( Collection< String > keys, DataFlags flags, String prefix ) {//}, boolean flag ) {
        keys.add( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_WEIGHTS ) );
        keys.add( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_ERROR ) );
        keys.add( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_ACTIVE ) );
        keys.add( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_MASK ) );

        keys.add( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_CELL_STRESS ) );
        keys.add( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_CELL_AGES ) );
        keys.add( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_EDGES ) );
        keys.add( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_EDGES_AGES ) );
        keys.add( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_AGE_SINCE_GROWTH ) );

        // These can be sparse:
        flags.putFlag( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_ACTIVE ), DataFlags.FLAG_SPARSE_BINARY );
        flags.putFlag( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_MASK ), DataFlags.FLAG_SPARSE_BINARY );
        flags.putFlag( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_EDGES ), DataFlags.FLAG_SPARSE_BINARY );

        flags.putFlag( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_WEIGHTS ), DataFlags.FLAG_SPARSE_REAL );

        // These rarely change:
        flags.putFlag( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_EDGES ), DataFlags.FLAG_LAZY_PERSIST );

        // These are written by only me, so can be cached, avoiding the read.
        flags.putFlag( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_WEIGHTS ), DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_ERROR ), DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_ACTIVE ), DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_MASK ), DataFlags.FLAG_NODE_CACHE );

        flags.putFlag( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_CELL_STRESS ), DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_CELL_AGES ), DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_EDGES ), DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_EDGES_AGES ), DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_AGE_SINCE_GROWTH ), DataFlags.FLAG_NODE_CACHE );

        // These are only written on a flush event:
//        if( flag ) {
            flags.putFlag( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_WEIGHTS ), DataFlags.FLAG_PERSIST_ON_FLUSH );
            flags.putFlag( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_ERROR ), DataFlags.FLAG_PERSIST_ON_FLUSH );
            flags.putFlag( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_ACTIVE ), DataFlags.FLAG_PERSIST_ON_FLUSH );
            flags.putFlag( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_MASK ), DataFlags.FLAG_PERSIST_ON_FLUSH );
//        }

        flags.putFlag( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_CELL_STRESS ), DataFlags.FLAG_PERSIST_ON_FLUSH );
        flags.putFlag( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_CELL_AGES ), DataFlags.FLAG_PERSIST_ON_FLUSH );
        //flags.putFlag(Keys.concatenate(prefix, GrowingNeuralGasEntity.OUTPUT_EDGES), DataFlags.FLAG_PERSIST_ON_FLUSH); lazy
        flags.putFlag( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_EDGES_AGES ), DataFlags.FLAG_PERSIST_ON_FLUSH );
        flags.putFlag( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_AGE_SINCE_GROWTH ), DataFlags.FLAG_PERSIST_ON_FLUSH );
    }

    @Override
    public Class getConfigClass() {
        return RegionLayerEntityConfig.class;
    }

    protected void doUpdateSelf() {

        // Do nothing unless the input is defined
        Data ffInput1 = getData( FF_INPUT_1 );
        Data ffInput2 = getData( FF_INPUT_2 );
        Data fbInput = getData( FB_INPUT );

        if( ( ffInput2 == null ) || ( ffInput1 == null ) || ( fbInput == null ) ) {
            return; // can't update yet.
        }

        // Get all the parameters:
        String regionLayerName = getName();

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Test parameters
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //int randomSeed = 1;

        // Feedforward size
        Point ffInput1Size = Data2d.getSize( ffInput1 );
        Point ffInput2Size = Data2d.getSize( ffInput2 );
//        Point fbInputSize  = //Data2d.getSize( fbInput  );

        int input1Width  = ffInput1Size.x;
        int input1Height = ffInput1Size.y;

        int input2Width  = ffInput2Size.x;
        int input2Height = ffInput2Size.y;

        // Feedback size
        int feedbackWidthCells  = fbInput.getSize(); //fbInputSize.x; // we dont care about the shape of the feedback.
        int feedbackHeightCells = 1;//fbInputSize.y;


        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Algorithm specific parameters
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Region size
        RegionLayerEntityConfig config = ( RegionLayerEntityConfig ) _config;

        // Build the algorithm
        //RandomInstance.setSeed(randomSeed); // make the tests repeatable
        ObjectMap om = ObjectMap.GetInstance();
        RegionLayerFactory rf = new RegionLayerFactory();

        // since each classifier is given config.receptiveFieldSize inputs, the max error is 1 * config.receptiveFieldSize
        // (i.e. all the inputs are wrong). Actually this isn't strictly true - there might be no overlap between the
        // actual bits of the column and the current input bits, so say it is 2 * config.receptiveFieldSize
        float classifierStressThreshold = config.classifierStressThreshold; // sum

        RegionLayer r = rf.create(
                om, regionLayerName, getRandom(),
                input1Width, input1Height,
                input2Width, input2Height,
                feedbackWidthCells, feedbackHeightCells,
                config.organizerWidthCells, config.organizerHeightCells,
                config.classifierWidthCells, config.classifierHeightCells, config.classifierDepthCells,
                config.organizerTrainOnChange, config.emitUnchangedCells,
                config.receptiveFieldsTrainingSamples, config.defaultPredictionInhibition, config.classifiersPerBit1, config.classifiersPerBit2, //config.receptiveFieldSize,
                config.organizerNeighbourhoodRange, //config.organizerLearningRate, config.organizerElasticity, //config.organizerLearningRateNeighbours, config.organizerNoiseMagnitude, config.organizerEdgeMaxAge, config.organizerStressLearningRate, config.organizerStressThreshold, config.organizerGrowthInterval,
                config.classifierLearningRate, config.classifierLearningRateNeighbours, config.classifierNoiseMagnitude, config.classifierEdgeMaxAge, config.classifierStressLearningRate, config.classifierStressSplitLearningRate, classifierStressThreshold, config.classifierGrowthInterval,
                config.predictorLearningRate );

        r._organizerIntervalsInput1X = config.organizerIntervalsInput1X;
        r._organizerIntervalsInput2X = config.organizerIntervalsInput2X;
        r._organizerIntervalsInput1Y = config.organizerIntervalsInput1Y;
        r._organizerIntervalsInput2Y = config.organizerIntervalsInput2Y;

        // Load data, overwriting the default setup.
        copyDataFromPersistence( r );

        // Update the region-layer, including optional reset and learning on/off switch
        if( config.reset ) {
            r.reset();
        }
        r._rc.setLearn( config.learn );
        r.update(); // 120-150ms. The rest of doUpdateSelf() is maybe 50ms.

        // update data logging
        int fnCount = r._regionPredictionFN.indicesMoreThan( 0.f ).size();
        updateDataLog( LOG_FN_COUNT, (float)fnCount );

        // Save data
        copyDataToPersistence( r );
    }

    protected void updateDataLog( String logSuffix, float value ) {
        Data dataLog = getData( logSuffix ); // error in classification (0,1)

        int oldLength = 0;
        if( dataLog != null ) {
            oldLength = dataLog.getSize();
        }

        Data dataLogNew = new Data( DataSize.create( oldLength +1 ) );

        for( int i = 0; i < oldLength; ++i ) {
            dataLogNew._values[ i ] = dataLog._values[ i ];
        }

        dataLogNew._values[ oldLength ] = value;

        setData( logSuffix, dataLogNew );
    }

    protected void copyDataFromPersistence( RegionLayer r ) {

        // The region itself
        r._ffInput1 = getData( FF_INPUT_1 );
        r._ffInput2 = getData( FF_INPUT_2 );
        r._ffInput1Old = getDataLazyResize( FF_INPUT_1_OLD, r._ffInput1._dataSize );
        r._ffInput2Old = getDataLazyResize( FF_INPUT_2_OLD, r._ffInput2._dataSize );
        r._fbInput = getData( FB_INPUT );
        r._fbInputOld = getDataLazyResize( FB_INPUT_OLD, r._fbInput._dataSize );

        // copy the raw unfolded data back in
        r._output1UnfoldedActivityRaw   = getDataLazyResize( FB_OUTPUT_1_UNFOLDED_ACTIVITY_RAW, r._ffInput1._dataSize );
        r._output1UnfoldedPredictionRaw = getDataLazyResize( FB_OUTPUT_1_UNFOLDED_PREDICTION_RAW, r._ffInput1._dataSize );

        r._output2UnfoldedActivityRaw   = getDataLazyResize( FB_OUTPUT_2_UNFOLDED_ACTIVITY_RAW  , r._ffInput2._dataSize );
        r._output2UnfoldedPredictionRaw = getDataLazyResize( FB_OUTPUT_2_UNFOLDED_PREDICTION_RAW, r._ffInput2._dataSize );

        Point organizerSize = r._rc.getOrganizerSizeCells();
        Point classifierSize = r._rc.getClassifierSizeCells();
        Point columnSize = r._rc.getColumnSizeCells();

        int organizerWidthCells = organizerSize.x;
        int organizerHeightCells = organizerSize.y;
        int columnWidthCells = columnSize.x;
        int columnHeightCells = columnSize.y;
        int classifierWidthCells = classifierSize.x;
        int classifierHeightCells = classifierSize.y;
        int regionWidthCells  = organizerWidthCells  * columnWidthCells;
        int regionHeightCells = organizerHeightCells * columnHeightCells;

        DataSize dataSizeRegion = DataSize.create( regionWidthCells, regionHeightCells );

        r._regionActivityOld = getDataLazyResize( ACTIVITY_OLD, dataSizeRegion );
        r._regionActivityNew = getDataLazyResize( ACTIVITY_NEW, dataSizeRegion );
        r._regionActivity    = getDataLazyResize( ACTIVITY,     dataSizeRegion );

        r._regionPredictionOld        = getDataLazyResize( PREDICTION_OLD       , dataSizeRegion );
        r._regionPredictionNew        = getDataLazyResize( PREDICTION_NEW       , dataSizeRegion );
        r._regionPredictionFP         = getDataLazyResize( PREDICTION_FP        , dataSizeRegion );
        r._regionPredictionFN         = getDataLazyResize( PREDICTION_FN        , dataSizeRegion );
        r._regionPredictionInhibition = getDataLazyResize( PREDICTION_INHIBITION, dataSizeRegion );

        // The organizer
        Data organizerInput = new Data( DataSize.create( 2 * 2 ) );
        copyDataFromPersistence( RegionConfig.SUFFIX_ORGANIZER, r._organizer, organizerWidthCells, organizerHeightCells, organizerInput );

        int organizerInputs = 2 * 2;
        int boundsSize = r._organizer._c.getBoundsSize( organizerInputs );
        r._organizer._inputBounds = getDataLazyResize( ORGANIZER_BOUNDS, DataSize.create( boundsSize ) );

        // The classifiers
        // 1. Copy packed data from persistence
//        Data classifierInput = r._ffInput;
        int nbrClassifiers = organizerSize.x * organizerSize.y;
        int areaCells = classifierWidthCells * classifierHeightCells;
        int inputs = r._ffInput1.getSize() + r._ffInput2.getSize(); //classifierInput.getSize();
        DataSize dataSizeWeights = DataSize.create( classifierWidthCells, classifierHeightCells, inputs );
        DataSize dataSizeCells   = DataSize.create( classifierWidthCells, classifierHeightCells );
        DataSize dataSizeEdges   = DataSize.create( areaCells, areaCells );

        DataSize dataSizeWeightsAll = DataSize.create( dataSizeWeights.getVolume() * nbrClassifiers );
        DataSize dataSizeCellsAll   = DataSize.create( dataSizeCells  .getVolume() * nbrClassifiers );
        DataSize dataSizeEdgesAll   = DataSize.create( dataSizeEdges  .getVolume() * nbrClassifiers );

        String prefix = RegionConfig.SUFFIX_CLASSIFIER;
        _classifierCellWeights    = getDataLazyResize( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_WEIGHTS          ), dataSizeWeightsAll );
        _classifierCellErrors     = getDataLazyResize( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_ERROR            ), dataSizeCellsAll ); // deep copies the size so they each own a copy
        _classifierCellActivity   = getDataLazyResize( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_ACTIVE           ), dataSizeCellsAll ); // deep copies the size so they each own a copy
        _classifierCellMask       = getDataLazyResize( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_MASK             ), dataSizeCellsAll ); // deep copies the size so they each own a copy
        _classifierCellStress     = getDataLazyResize( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_CELL_STRESS      ), dataSizeCellsAll );
        _classifierCellAges       = getDataLazyResize( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_CELL_AGES        ), dataSizeCellsAll );
        _classifierEdges          = getDataLazyResize( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_EDGES            ), dataSizeEdgesAll );
        _classifierEdgesAges      = getDataLazyResize( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_EDGES_AGES       ), dataSizeEdgesAll );
        _classifierAgeSinceGrowth = getDataLazyResize( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_AGE_SINCE_GROWTH ), DataSize.create( nbrClassifiers ) );

        // 2. Unpack into the individual classifiers
        for( int y = 0; y < organizerSize.y; ++y ) {
            for( int x = 0; x < organizerSize.x; ++x ) {
                int regionOffset = r._rc.getOrganizerOffset( x, y );
                GrowingNeuralGas classifier = r._classifiers.get( regionOffset );

                int weightsSize = dataSizeWeights.getVolume();
                int cellsSize = dataSizeCells.getVolume();
                int edgesSize = dataSizeEdges.getVolume();

                int weightsOffset = weightsSize * regionOffset;
                int cellsOffset = cellsSize * regionOffset;
                int edgesOffset = edgesSize * regionOffset;

                // .copyRange( that, offsetThis, offsetThat, range );
//                classifier._inputValues = classifierInput;
                classifier._cellWeights   .copyRange( _classifierCellWeights   , 0, weightsOffset, weightsSize );
                classifier._cellErrors    .copyRange( _classifierCellErrors    , 0, cellsOffset, cellsSize );
                classifier._cellActivity  .copyRange( _classifierCellActivity  , 0, cellsOffset, cellsSize );
                classifier._cellMask      .copyRange( _classifierCellMask      , 0, cellsOffset, cellsSize );
                classifier._cellStress    .copyRange( _classifierCellStress    , 0, cellsOffset, cellsSize );
                classifier._cellAges      .copyRange( _classifierCellAges      , 0, cellsOffset, cellsSize );
                classifier._edges         .copyRange( _classifierEdges         , 0, edgesOffset, edgesSize );
                classifier._edgesAges     .copyRange( _classifierEdgesAges     , 0, edgesOffset, edgesSize );
                classifier._ageSinceGrowth.copyRange( _classifierAgeSinceGrowth, 0, regionOffset, 1 );
            }
        }

        // Predictor:
        int predictorContextSize = r._predictor._context.getSize();
        int predictorWeightsSize = r._predictor._weights.getSize();
        int hebbianPredictorContext = r._rc.getHebbianPredictorContextSizeRegion( predictorContextSize );
        int hebbianPredictorWeights = r._rc.getHebbianPredictorWeightsSizeRegion( predictorWeightsSize );
        r._regionPredictorContext = getDataLazyResize( PREDICTOR_CONTEXTS, DataSize.create( hebbianPredictorContext ) );
        r._regionPredictorWeights = getDataLazyResize( PREDICTOR_WEIGHTS , DataSize.create( hebbianPredictorWeights ) );
    }

    protected void copyDataFromPersistence( String prefix, ParameterLessSelfOrganizingMap dsom, int widthCells, int heightCells, Data input ) {
        int areaCells = widthCells * heightCells;
        int inputs = input.getSize();

        DataSize dataSizeWeights = DataSize.create( widthCells, heightCells, inputs );
        DataSize dataSizeCells   = DataSize.create( widthCells, heightCells );

        Data weights  = getDataLazyResize( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_WEIGHTS ), dataSizeWeights );
        Data errors   = getDataLazyResize( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_ERROR ), dataSizeCells ); // deep copies the size so they each own a copy
        Data activity = getDataLazyResize( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_ACTIVE ), dataSizeCells ); // deep copies the size so they each own a copy
        Data mask     = getDataLazyResize( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_MASK ), dataSizeCells ); // deep copies the size so they each own a copy

        dsom._inputValues = input;
        dsom._cellWeights = weights;
        dsom._cellErrors = errors;
        dsom._cellActivity = activity;
        dsom._cellMask = mask;
    }

    protected void copyDataFromPersistence( String prefix, GrowingNeuralGas gng, int widthCells, int heightCells, Data input ) {
        int areaCells = widthCells * heightCells;
        int inputs = input.getSize();

        DataSize dataSizeWeights = DataSize.create( widthCells, heightCells, inputs );
        DataSize dataSizeCells   = DataSize.create( widthCells, heightCells );
        DataSize dataSizeEdges   = DataSize.create( areaCells, areaCells );

        Data weights  = getDataLazyResize( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_WEIGHTS ), dataSizeWeights );
        Data errors   = getDataLazyResize( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_ERROR ), dataSizeCells ); // deep copies the size so they each own a copy
        Data activity = getDataLazyResize( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_ACTIVE ), dataSizeCells ); // deep copies the size so they each own a copy
        Data mask     = getDataLazyResize( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_MASK ), dataSizeCells ); // deep copies the size so they each own a copy

        Data cellStress     = getDataLazyResize( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_CELL_STRESS ), dataSizeCells );
        Data cellAges       = getDataLazyResize( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_CELL_AGES ), dataSizeCells );
        Data edges          = getDataLazyResize( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_EDGES ), dataSizeEdges );
        Data edgesAges      = getDataLazyResize( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_EDGES_AGES ), dataSizeEdges );
        Data ageSinceGrowth = getDataLazyResize( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_AGE_SINCE_GROWTH ), DataSize.create( 1 ) );

        gng._inputValues = input;
        gng._cellWeights = weights;
        gng._cellErrors = errors;
        gng._cellActivity = activity;
        gng._cellMask = mask;

        gng._cellStress = cellStress;
        gng._cellAges = cellAges;
        gng._edges = edges;
        gng._edgesAges = edgesAges;
        gng._ageSinceGrowth = ageSinceGrowth;
    }

    protected void copyDataToPersistence( RegionLayer r ) {

        // The region itself
        setData( FF_INPUT_1,     r._ffInput1 );
        setData( FF_INPUT_1_OLD, r._ffInput1Old );
        setData( FF_INPUT_2,     r._ffInput2 );
        setData( FF_INPUT_2_OLD, r._ffInput2Old );
        setData( FB_INPUT,     r._fbInput );
        setData( FB_INPUT_OLD, r._fbInputOld );

        setData( FB_OUTPUT_1_UNFOLDED_ACTIVITY_RAW,   r._output1UnfoldedActivityRaw );
        setData( FB_OUTPUT_1_UNFOLDED_ACTIVITY,       r._output1UnfoldedActivity );
        setData( FB_OUTPUT_1_UNFOLDED_PREDICTION_RAW, r._output1UnfoldedPredictionRaw );
        setData( FB_OUTPUT_1_UNFOLDED_PREDICTION,     r._output1UnfoldedPrediction );

        setData( FB_OUTPUT_2_UNFOLDED_ACTIVITY_RAW,   r._output2UnfoldedActivityRaw );
        setData( FB_OUTPUT_2_UNFOLDED_ACTIVITY,       r._output2UnfoldedActivity );
        setData( FB_OUTPUT_2_UNFOLDED_PREDICTION_RAW, r._output2UnfoldedPredictionRaw );
        setData( FB_OUTPUT_2_UNFOLDED_PREDICTION,     r._output2UnfoldedPrediction );

        setData( ACTIVITY_OLD, r._regionActivityOld );
        setData( ACTIVITY_NEW, r._regionActivityNew );
        setData( ACTIVITY,     r._regionActivity );

        setData( PREDICTION_OLD       , r._regionPredictionOld );
        setData( PREDICTION_NEW       , r._regionPredictionNew );
        setData( PREDICTION_RAW       , r._regionPredictionRaw );
        setData( PREDICTION_FP        , r._regionPredictionFP  );
        setData( PREDICTION_FN        , r._regionPredictionFN  );
        setData( PREDICTION_INHIBITION, r._regionPredictionInhibition );

        // The organizer
        copyDataToPersistence( RegionConfig.SUFFIX_ORGANIZER, r._organizer );

        setData( ORGANIZER_BOUNDS, r._organizer._inputBounds );

        // The classifiers
        // 1. Pack the data.
        Point p = r._rc.getOrganizerSizeCells();

        for( int y = 0; y < p.y; ++y ) {
            for( int x = 0; x < p.x; ++x ) {
                int regionOffset = r._rc.getOrganizerOffset( x, y );
                GrowingNeuralGas classifier = r._classifiers.get( regionOffset );

                int weightsSize = classifier._cellWeights.getSize();
                int cellsSize = classifier._cellErrors.getSize();
                int edgesSize = classifier._edges.getSize();

                int weightsOffset = weightsSize * regionOffset;
                int cellsOffset = cellsSize * regionOffset;
                int edgesOffset = edgesSize * regionOffset;

                // .copyRange( that, offsetThis, offsetThat, range );
                _classifierCellWeights   .copyRange( classifier._cellWeights   , weightsOffset, 0, weightsSize );
                _classifierCellErrors    .copyRange( classifier._cellErrors    , cellsOffset, 0, cellsSize );
                _classifierCellActivity  .copyRange( classifier._cellActivity  , cellsOffset, 0, cellsSize );
                _classifierCellMask      .copyRange( classifier._cellMask      , cellsOffset, 0, cellsSize );
                _classifierCellStress    .copyRange( classifier._cellStress    , cellsOffset, 0, cellsSize );
                _classifierCellAges      .copyRange( classifier._cellAges      , cellsOffset, 0, cellsSize );
                _classifierEdges         .copyRange( classifier._edges         , edgesOffset, 0, edgesSize );
                _classifierEdgesAges     .copyRange( classifier._edgesAges     , edgesOffset, 0, edgesSize );
                _classifierAgeSinceGrowth.copyRange( classifier._ageSinceGrowth, regionOffset, 0, 1 );
            }
        }

        // 2. Store the packed data.
        String prefix = RegionConfig.SUFFIX_CLASSIFIER;
        setData( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_WEIGHTS ), _classifierCellWeights );
        setData( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_ERROR ), _classifierCellErrors );
        setData( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_ACTIVE ), _classifierCellActivity );
        setData( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_MASK ), _classifierCellMask );

        setData( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_CELL_STRESS ), _classifierCellStress );
        setData( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_CELL_AGES ), _classifierCellAges );
        setData( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_EDGES ), _classifierEdges );
        setData( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_EDGES_AGES ), _classifierEdgesAges );
        setData( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_AGE_SINCE_GROWTH ), _classifierAgeSinceGrowth );

        // Predictor:
        setData( PREDICTOR_CONTEXTS, r._regionPredictorContext );
        setData( PREDICTOR_WEIGHTS, r._regionPredictorWeights );
    }

    protected void copyDataToPersistence( String prefix, ParameterLessSelfOrganizingMap dsom ) {
        setData( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_WEIGHTS ), dsom._cellWeights );
        setData( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_ERROR ), dsom._cellErrors );
        setData( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_ACTIVE ), dsom._cellActivity );
        setData( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_MASK ), dsom._cellMask );
    }

    protected void copyDataToPersistence( String prefix, DynamicSelfOrganizingMap dsom ) {
        setData( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_WEIGHTS ), dsom._cellWeights );
        setData( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_ERROR ), dsom._cellErrors );
        setData( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_ACTIVE ), dsom._cellActivity );
        setData( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_MASK ), dsom._cellMask );
    }

    protected void copyDataToPersistence( String prefix, GrowingNeuralGas gng ) {
        setData( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_WEIGHTS ), gng._cellWeights );
        setData( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_ERROR ), gng._cellErrors );
        setData( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_ACTIVE ), gng._cellActivity );
        setData( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_MASK ), gng._cellMask );

        setData( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_CELL_STRESS ), gng._cellStress );
        setData( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_CELL_AGES ), gng._cellAges );
        setData( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_EDGES ), gng._edges );
        setData( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_EDGES_AGES ), gng._edgesAges );
        setData( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_AGE_SINCE_GROWTH ), gng._ageSinceGrowth );
    }
}
