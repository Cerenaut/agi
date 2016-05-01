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

import io.agi.core.alg.Region;
import io.agi.core.alg.RegionConfig;
import io.agi.core.alg.RegionFactory;
import io.agi.core.ann.unsupervised.GrowingNeuralGas;
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
 * Wraps the Region concept as a single Entity.
 * <p/>
 * Created by dave on 12/03/16.
 */
public class RegionEntity extends Entity {

    public static final String ENTITY_TYPE = "region";

    public static final String FF_INPUT     = "ff-input";
    public static final String FB_INPUT     = "fb-input";
    public static final String FB_INPUT_OLD = "fb-input-old";
    public static final String FB_OUTPUT_UNFOLDED_ACTIVITY   = "fb-output-unfolded-activity";
    public static final String FB_OUTPUT_UNFOLDED_PREDICTION = "fb-output-unfolded-prediction";

    public static final String ACTIVITY_OLD = "activity-old";
    public static final String ACTIVITY_NEW = "activity-new";
    public static final String ACTIVITY     = "activity";

    public static final String PREDICTION_OLD = "prediction-old";
    public static final String PREDICTION_NEW = "prediction-new";
    public static final String PREDICTION_RAW = "prediction-raw";

    public static final String PREDICTION_FP = "prediction-fp";
    public static final String PREDICTION_FN = "prediction-fn";

    public static final String HEBBIAN_PREDICTOR_CONTEXTS = "hebbian-predictor-contexts";
    public static final String HEBBIAN_PREDICTOR_WEIGHTS = "hebbian-predictor-weights";

    protected Data _classifierCellWeights;
    protected Data _classifierCellErrors;
    protected Data _classifierCellActivity;
    protected Data _classifierCellMask;

    protected Data _classifierCellStress;
    protected Data _classifierCellAges;
    protected Data _classifierEdges;
    protected Data _classifierEdgesAges;
    protected Data _classifierAgeSinceGrowth;

    public RegionEntity( ObjectMap om, Node n, ModelEntity model ) {
        super( om, n, model );
    }

    public void getInputAttributes( Collection< String > attributes ) {
        attributes.add( FF_INPUT );
        attributes.add( FB_INPUT );
    }

    public void getOutputAttributes( Collection< String > attributes, DataFlags flags ) {

        attributes.add( FB_OUTPUT_UNFOLDED_ACTIVITY );
        attributes.add( FB_OUTPUT_UNFOLDED_PREDICTION );

        flags.putFlag( FB_OUTPUT_UNFOLDED_ACTIVITY, DataFlags.FLAG_PERSIST_ONLY ); // never read
        flags.putFlag( FB_OUTPUT_UNFOLDED_PREDICTION, DataFlags.FLAG_PERSIST_ONLY ); // never read
        flags.putFlag( FB_OUTPUT_UNFOLDED_ACTIVITY, DataFlags.FLAG_SPARSE_BINARY );
        flags.putFlag( FB_OUTPUT_UNFOLDED_PREDICTION, DataFlags.FLAG_SPARSE_BINARY );

        attributes.add( FB_INPUT_OLD );

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

        flags.putFlag( PREDICTION_FP, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( PREDICTION_FN, DataFlags.FLAG_NODE_CACHE );

        flags.putFlag( PREDICTION_FP, DataFlags.FLAG_SPARSE_BINARY );
        flags.putFlag( PREDICTION_FN, DataFlags.FLAG_SPARSE_BINARY );

        // Hebbian predictor:
        attributes.add( HEBBIAN_PREDICTOR_CONTEXTS );
        attributes.add( HEBBIAN_PREDICTOR_WEIGHTS );

        flags.putFlag( HEBBIAN_PREDICTOR_CONTEXTS, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( HEBBIAN_PREDICTOR_CONTEXTS, DataFlags.FLAG_SPARSE_BINARY );

        flags.putFlag( HEBBIAN_PREDICTOR_WEIGHTS, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( HEBBIAN_PREDICTOR_WEIGHTS, DataFlags.FLAG_PERSIST_ON_FLUSH );
        flags.putFlag( HEBBIAN_PREDICTOR_WEIGHTS, DataFlags.FLAG_SPARSE_REAL ); // drops from about 2.2 to 1.5 sec.

        // The organizer
        getClassifierOutputKeys( attributes, flags, RegionConfig.SUFFIX_ORGANIZER );//, false );

        // The classifiers
        RegionEntityConfig config = ( RegionEntityConfig ) _config;

//        for( int y = 0; y < config.organizerHeightCells; ++y ) {
//            for( int x = 0; x < config.organizerWidthCells; ++x ) {
//                String prefix = Keys.concatenate( RegionConfig.SUFFIX_CLASSIFIER, String.valueOf( x ), String.valueOf( y ) );
                String prefix = RegionConfig.SUFFIX_CLASSIFIER;
                getClassifierOutputKeys( attributes, flags, prefix );//, true );
//            }
//        }

        // Predictor
/*        int predictorLayers = Region.PREDICTOR_LAYERS;

        for( int l = 0; l < predictorLayers; ++l ) {
            String prefix = Keys.concatenate( RegionConfig.SUFFIX_PREDICTOR, String.valueOf( l ) );

            attributes.add( Keys.concatenate( prefix, NetworkLayer.INPUT ) );
            attributes.add( Keys.concatenate( prefix, NetworkLayer.WEIGHTS ) );
            attributes.add( Keys.concatenate( prefix, NetworkLayer.BIASES ) );
            attributes.add( Keys.concatenate( prefix, NetworkLayer.WEIGHTED_SUMS ) );
            attributes.add( Keys.concatenate( prefix, NetworkLayer.OUTPUTS ) );
            attributes.add( Keys.concatenate( prefix, NetworkLayer.ERROR_GRADIENTS ) );

            flags.putFlag( Keys.concatenate( prefix, NetworkLayer.INPUT ), DataFlags.FLAG_NODE_CACHE );
            flags.putFlag( Keys.concatenate( prefix, NetworkLayer.WEIGHTS ), DataFlags.FLAG_NODE_CACHE );
            flags.putFlag( Keys.concatenate( prefix, NetworkLayer.BIASES ), DataFlags.FLAG_NODE_CACHE );
            flags.putFlag( Keys.concatenate( prefix, NetworkLayer.WEIGHTED_SUMS ), DataFlags.FLAG_NODE_CACHE );
            flags.putFlag( Keys.concatenate( prefix, NetworkLayer.OUTPUTS ), DataFlags.FLAG_NODE_CACHE );
            flags.putFlag( Keys.concatenate( prefix, NetworkLayer.ERROR_GRADIENTS ), DataFlags.FLAG_NODE_CACHE );

            flags.putFlag( Keys.concatenate( prefix, NetworkLayer.INPUT ), DataFlags.FLAG_PERSIST_ON_FLUSH );
            flags.putFlag( Keys.concatenate( prefix, NetworkLayer.WEIGHTS ), DataFlags.FLAG_PERSIST_ON_FLUSH );
            flags.putFlag( Keys.concatenate( prefix, NetworkLayer.BIASES ), DataFlags.FLAG_PERSIST_ON_FLUSH );
            flags.putFlag( Keys.concatenate( prefix, NetworkLayer.WEIGHTED_SUMS ), DataFlags.FLAG_PERSIST_ON_FLUSH );
            flags.putFlag( Keys.concatenate( prefix, NetworkLayer.OUTPUTS ), DataFlags.FLAG_PERSIST_ON_FLUSH );
            flags.putFlag( Keys.concatenate( prefix, NetworkLayer.ERROR_GRADIENTS ), DataFlags.FLAG_PERSIST_ON_FLUSH );
        }*/
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
        return RegionEntityConfig.class;
    }

    protected void doUpdateSelf() {

        // Do nothing unless the input is defined
        Data ffInput = getData( FF_INPUT );
        Data fbInput = getData( FB_INPUT );

        if( ( ffInput == null ) || ( fbInput == null ) ) {
            return; // can't update yet.
        }

        // Get all the parameters:
        String regionName = getName();

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Test parameters
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //int randomSeed = 1;

        // Feedforward size
        Point ffInputSize = Data2d.getSize( ffInput );
        Point fbInputSize = Data2d.getSize( fbInput );

        int inputWidth = ffInputSize.x;
        int inputHeight = ffInputSize.y;

        // Feedback size
        int feedbackWidthCells = fbInputSize.x;
        int feedbackHeightCells = fbInputSize.y;


        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Algorithm specific parameters
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Region size
        RegionEntityConfig config = ( RegionEntityConfig ) _config;

        // Build the algorithm
        //RandomInstance.setSeed(randomSeed); // make the tests repeatable
        ObjectMap om = ObjectMap.GetInstance();
        RegionFactory rf = new RegionFactory();

        // since each classifier is given config.receptiveFieldSize inputs, the max error is 1 * config.receptiveFieldSize
        // (i.e. all the inputs are wrong). Actually this isn't strictly true - there might be no overlap between the
        // actual bits of the column and the current input bits, so say it is 2 * config.receptiveFieldSize
        float classifierStressThreshold = config.classifierStressThreshold; // sum

//      should stop adding cells when error is < 1 bit, which is
//        0.1 * 0.1 = 0.01
//        0.5 * 0.5 = 0.25
//        sum = 0.26
//        sqrt( sum ) = 0.509 which is less than the sum of errors.

        Region r = rf.create(
                om, regionName, getRandom(),
                inputWidth, inputHeight,
                feedbackWidthCells, feedbackHeightCells,
                config.organizerWidthCells, config.organizerHeightCells,
                config.classifierWidthCells, config.classifierHeightCells,
                config.receptiveFieldsTrainingSamples, config.receptiveFieldSize,
                config.organizerLearningRate, config.organizerLearningRateNeighbours, config.organizerNoiseMagnitude, config.organizerEdgeMaxAge, config.organizerStressLearningRate, config.organizerStressThreshold, config.organizerGrowthInterval,
                config.classifierLearningRate, config.classifierLearningRateNeighbours, config.classifierNoiseMagnitude, config.classifierEdgeMaxAge, config.classifierStressLearningRate, classifierStressThreshold, config.classifierGrowthInterval,
                config.predictorLearningRate );
//                config.predictorHiddenLayerScaleFactor, config.predictorLearningRate, config.predictorRegularization );

        // Load data, overwriting the default setup.
        copyDataFromPersistence( r );

        // Process
        if( config.reset ) {
            r.reset();
        }

        r.update(); // 120-150ms. The rest of doUpdateSelf() is maybe 50ms.

        // Save data
        copyDataToPersistence( r );
    }

    protected void copyDataFromPersistence( Region r ) {

        // The region itself
        r._ffInput = getData( FF_INPUT );
        r._fbInput = getData( FB_INPUT );
        r._fbInputOld = getData( FB_INPUT_OLD );

        Point organizerSize = r._rc.getOrganizerSizeCells();
        Point classifierSize = r._rc.getClassifierSizeCells();

        int organizerWidthCells = organizerSize.x;
        int organizerHeightCells = organizerSize.y;
        int classifierWidthCells = classifierSize.x;
        int classifierHeightCells = classifierSize.y;
        int regionWidthCells = organizerWidthCells * classifierWidthCells;
        int regionHeightCells = organizerHeightCells * classifierHeightCells;

        DataSize dataSizeRegion = DataSize.create( regionWidthCells, regionHeightCells );

        r._regionActivityOld = getDataLazyResize( ACTIVITY_OLD, dataSizeRegion );
        r._regionActivityNew = getDataLazyResize( ACTIVITY_NEW, dataSizeRegion );
        r._regionActivity    = getDataLazyResize( ACTIVITY,     dataSizeRegion );

        r._regionPredictionOld = getDataLazyResize( PREDICTION_OLD, dataSizeRegion );
        r._regionPredictionNew = getDataLazyResize( PREDICTION_NEW, dataSizeRegion );

        r._regionPredictionFP = getDataLazyResize( PREDICTION_FP, dataSizeRegion );
        r._regionPredictionFN = getDataLazyResize( PREDICTION_FN, dataSizeRegion );

        // The organizer
        Data organizerInput = new Data( DataSize.create( Region.RECEPTIVE_FIELD_DIMENSIONS ) );
        copyDataFromPersistence( RegionConfig.SUFFIX_ORGANIZER, r._organizer, organizerWidthCells, organizerHeightCells, organizerInput );

        // The classifiers
        // 1. Copy packed data from persistence
        Data classifierInput = r._ffInput;
        int nbrClassifiers = organizerSize.x * organizerSize.y;
        int areaCells = classifierWidthCells * classifierHeightCells;
        int inputs = classifierInput.getSize();
        DataSize dataSizeWeights = DataSize.create( classifierWidthCells, classifierHeightCells, inputs );
        DataSize dataSizeCells = DataSize.create( classifierWidthCells, classifierHeightCells );
        DataSize dataSizeEdges = DataSize.create( areaCells, areaCells );

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
                classifier._inputValues = classifierInput;
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

/*        Data classifierInput = r._ffInput;
        for( int y = 0; y < organizerSize.y; ++y ) {
            for( int x = 0; x < organizerSize.x; ++x ) {
                int regionOffset = r._rc.getOrganizerOffset( x, y );
                GrowingNeuralGas classifier = r._classifiers.get( regionOffset );
                String prefix = Keys.concatenate( RegionConfig.SUFFIX_CLASSIFIER, String.valueOf( x ), String.valueOf( y ) );
                copyDataFromPersistence( prefix, classifier, classifierWidthCells, classifierHeightCells, classifierInput );
            }
        }*/

        // Hebbian predictor:
        int hebbianPredictorContext = r.getHebbianPredictorContextSizeRegion();
        int hebbianPredictorWeights = r.getHebbianPredictorWeightsSizeRegion();
        r._hebbianPredictorContext = getDataLazyResize( HEBBIAN_PREDICTOR_CONTEXTS, DataSize.create( hebbianPredictorContext ) );
        r._hebbianPredictorWeights = getDataLazyResize( HEBBIAN_PREDICTOR_WEIGHTS, DataSize.create( hebbianPredictorWeights ) );

/*        // Predictor
        int layers = r._predictor._layers.size();

        for( int l = 0; l < layers; ++l ) {
            NetworkLayer nl = r._predictor._layers.get( l );
            String prefix = Keys.concatenate( RegionConfig.SUFFIX_PREDICTOR, String.valueOf( l ) );

            nl._inputs         = getData( Keys.concatenate( prefix, NetworkLayer.INPUT ), nl._inputs._dataSize );
            nl._weights        = getData( Keys.concatenate( prefix, NetworkLayer.WEIGHTS ), nl._weights._dataSize );
            nl._biases         = getData( Keys.concatenate( prefix, NetworkLayer.BIASES ), nl._biases._dataSize );
            nl._weightedSums   = getData( Keys.concatenate( prefix, NetworkLayer.WEIGHTED_SUMS ), nl._weightedSums._dataSize );
            nl._outputs        = getData( Keys.concatenate( prefix, NetworkLayer.OUTPUTS ), nl._outputs._dataSize );
            nl._errorGradients = getData( Keys.concatenate( prefix, NetworkLayer.ERROR_GRADIENTS ), nl._errorGradients._dataSize );
        }*/
    }

    protected void copyDataFromPersistence( String prefix, GrowingNeuralGas gng, int widthCells, int heightCells, Data input ) {
        int areaCells = widthCells * heightCells;
        int inputs = input.getSize();

        DataSize dataSizeWeights = DataSize.create( widthCells, heightCells, inputs );
        DataSize dataSizeCells = DataSize.create( widthCells, heightCells );
        DataSize dataSizeEdges = DataSize.create( areaCells, areaCells );

        Data weights = getDataLazyResize( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_WEIGHTS ), dataSizeWeights );
        Data errors = getDataLazyResize( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_ERROR ), dataSizeCells ); // deep copies the size so they each own a copy
        Data activity = getDataLazyResize( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_ACTIVE ), dataSizeCells ); // deep copies the size so they each own a copy
        Data mask = getDataLazyResize( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_MASK ), dataSizeCells ); // deep copies the size so they each own a copy

        Data cellStress = getDataLazyResize( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_CELL_STRESS ), dataSizeCells );
        Data cellAges = getDataLazyResize( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_CELL_AGES ), dataSizeCells );
        Data edges = getDataLazyResize( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_EDGES ), dataSizeEdges );
        Data edgesAges = getDataLazyResize( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_EDGES_AGES ), dataSizeEdges );
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

    protected void copyDataToPersistence( Region r ) {

        // The region itself
        setData( FF_INPUT,     r._ffInput );
        setData( FB_INPUT,     r._fbInput );
        setData( FB_INPUT_OLD, r._fbInputOld );

        setData( FB_OUTPUT_UNFOLDED_ACTIVITY,   r._fbOutputUnfoldedActivity );
        setData( FB_OUTPUT_UNFOLDED_PREDICTION, r._fbOutputUnfoldedPrediction );

        setData( ACTIVITY_OLD, r._regionActivityOld );
        setData( ACTIVITY_NEW, r._regionActivityNew );
        setData( ACTIVITY,     r._regionActivity );

        setData( PREDICTION_OLD, r._regionPredictionOld );
        setData( PREDICTION_NEW, r._regionPredictionNew );
        setData( PREDICTION_RAW, r._regionPredictionRaw );

        setData( PREDICTION_FP, r._regionPredictionFP );
        setData( PREDICTION_FN, r._regionPredictionFN );

        // The organizer
        copyDataToPersistence( RegionConfig.SUFFIX_ORGANIZER, r._organizer );

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

/*        for( int y = 0; y < p.y; ++y ) {
            for( int x = 0; x < p.x; ++x ) {
                int regionOffset = r._rc.getOrganizerOffset( x, y );
                GrowingNeuralGas classifier = r._classifiers.get( regionOffset );
                String prefix = Keys.concatenate( RegionConfig.SUFFIX_CLASSIFIER, String.valueOf( x ), String.valueOf( y ) );
                copyDataToPersistence( prefix, classifier );
            }
        }*/

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

        // Hebbian predictor:
        setData( HEBBIAN_PREDICTOR_CONTEXTS, r._hebbianPredictorContext );
        setData( HEBBIAN_PREDICTOR_WEIGHTS, r._hebbianPredictorWeights );

/*        // Predictor
        int layers = r._predictor._layers.size();

        for( int l = 0; l < layers; ++l ) {
            NetworkLayer nl = r._predictor._layers.get( l );
            String prefix = Keys.concatenate( RegionConfig.SUFFIX_PREDICTOR, String.valueOf( l ) );

            setData( Keys.concatenate( prefix, NetworkLayer.INPUT ), nl._inputs );
            setData( Keys.concatenate( prefix, NetworkLayer.WEIGHTS ), nl._weights );
            setData( Keys.concatenate( prefix, NetworkLayer.BIASES ), nl._biases );
            setData( Keys.concatenate( prefix, NetworkLayer.WEIGHTED_SUMS ), nl._weightedSums );
            setData( Keys.concatenate( prefix, NetworkLayer.OUTPUTS ), nl._outputs );
            setData( Keys.concatenate( prefix, NetworkLayer.ERROR_GRADIENTS ), nl._errorGradients );
        }*/
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
