package io.agi.framework.entities;

import io.agi.core.alg.Region;
import io.agi.core.alg.RegionConfig;
import io.agi.core.alg.RegionFactory;
import io.agi.core.ann.supervised.NetworkLayer;
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
 * <p>
 * Created by dave on 12/03/16.
 */
public class RegionEntity extends Entity {

    public static final String ENTITY_TYPE = "region";

    public static final String FF_INPUT = "ff-input";
    public static final String FB_INPUT = "fb-input";
    public static final String FB_INPUT_OLD = "fb-input";

    public static final String ACTIVITY_OLD = "activity-old";
    public static final String ACTIVITY_NEW = "activity-new";
    public static final String ACTIVITY = "activity";

    public static final String PREDICTION_OLD = "prediction-old";
    public static final String PREDICTION_NEW = "prediction-new";

    public static final String PREDICTION_FP = "prediction-fp";
    public static final String PREDICTION_FN = "prediction-fn";

    public RegionEntity( ObjectMap om, Node n, ModelEntity model ) {
        super( om, n, model );
    }

    public void getInputKeys( Collection< String > keys ) {
        keys.add( FF_INPUT );
        keys.add( FB_INPUT );
    }

    public void getOutputKeys( Collection< String > keys, DataFlags flags ) {
        keys.add( FB_INPUT_OLD );

        flags.putFlag( FB_INPUT_OLD, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( FB_INPUT_OLD, DataFlags.FLAG_SPARSE_UNIT );

        keys.add( ACTIVITY_OLD );
        keys.add( ACTIVITY_NEW );
        keys.add( ACTIVITY );

        flags.putFlag( ACTIVITY_OLD, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( ACTIVITY_NEW, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( FB_INPUT_OLD, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( ACTIVITY_OLD, DataFlags.FLAG_SPARSE_UNIT );
        flags.putFlag( ACTIVITY_NEW, DataFlags.FLAG_SPARSE_UNIT );
        flags.putFlag( ACTIVITY, DataFlags.FLAG_SPARSE_UNIT );
        flags.putFlag( ACTIVITY_OLD, DataFlags.FLAG_LAZY_PERSIST );
        flags.putFlag( ACTIVITY_NEW, DataFlags.FLAG_LAZY_PERSIST );
        flags.putFlag( ACTIVITY, DataFlags.FLAG_LAZY_PERSIST );

        keys.add( PREDICTION_OLD );
        keys.add( PREDICTION_NEW );

        flags.putFlag( PREDICTION_OLD, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( PREDICTION_NEW, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( PREDICTION_OLD, DataFlags.FLAG_SPARSE_UNIT );
        flags.putFlag( PREDICTION_NEW, DataFlags.FLAG_SPARSE_UNIT );

        keys.add( PREDICTION_FP );
        keys.add( PREDICTION_FN );

        flags.putFlag( PREDICTION_FP, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( PREDICTION_FN, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( PREDICTION_FP, DataFlags.FLAG_SPARSE_UNIT );
        flags.putFlag( PREDICTION_FN, DataFlags.FLAG_SPARSE_UNIT );

        // The organizer
        getClassifierOutputKeys( keys, flags, RegionConfig.SUFFIX_ORGANIZER );

        // The classifiers
        io.agi.framework.entities.RegionConfig config = ( io.agi.framework.entities.RegionConfig ) _config;

        for ( int y = 0; y < config.organizerHeightCells; ++y ) {
            for ( int x = 0; x < config.organizerWidthCells; ++x ) {
                String prefix = Keys.concatenate( RegionConfig.SUFFIX_CLASSIFIER, String.valueOf( x ), String.valueOf( y ) );
                getClassifierOutputKeys( keys, flags, prefix );
            }
        }

        // Predictor
        int predictorLayers = Region.PREDICTOR_LAYERS;

        for ( int l = 0; l < predictorLayers; ++l ) {
            String prefix = Keys.concatenate( RegionConfig.SUFFIX_PREDICTOR, String.valueOf( l ) );

            keys.add( Keys.concatenate( prefix, NetworkLayer.INPUT ) );
            keys.add( Keys.concatenate( prefix, NetworkLayer.WEIGHTS ) );
            keys.add( Keys.concatenate( prefix, NetworkLayer.BIASES ) );
            keys.add( Keys.concatenate( prefix, NetworkLayer.WEIGHTED_SUMS ) );
            keys.add( Keys.concatenate( prefix, NetworkLayer.OUTPUTS ) );
            keys.add( Keys.concatenate( prefix, NetworkLayer.ERROR_GRADIENTS ) );

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
        }
    }

    public void getClassifierOutputKeys( Collection< String > keys, DataFlags flags, String prefix ) {
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
        flags.putFlag( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_ACTIVE ), DataFlags.FLAG_SPARSE_UNIT );
        flags.putFlag( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_MASK ), DataFlags.FLAG_SPARSE_UNIT );
        flags.putFlag( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_EDGES ), DataFlags.FLAG_SPARSE_UNIT );

        // These rarely change:
        flags.putFlag( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_EDGES ), DataFlags.FLAG_LAZY_PERSIST );

        // These are written by only me, so can be cached:
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
        flags.putFlag( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_WEIGHTS ), DataFlags.FLAG_PERSIST_ON_FLUSH );
        flags.putFlag( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_ERROR ), DataFlags.FLAG_PERSIST_ON_FLUSH );
        flags.putFlag( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_ACTIVE ), DataFlags.FLAG_PERSIST_ON_FLUSH );
        flags.putFlag( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_MASK ), DataFlags.FLAG_PERSIST_ON_FLUSH );

        flags.putFlag( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_CELL_STRESS ), DataFlags.FLAG_PERSIST_ON_FLUSH );
        flags.putFlag( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_CELL_AGES ), DataFlags.FLAG_PERSIST_ON_FLUSH );
        //flags.putFlag(Keys.concatenate(prefix, GrowingNeuralGasEntity.OUTPUT_EDGES), DataFlags.FLAG_PERSIST_ON_FLUSH); lazy
        flags.putFlag( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_EDGES_AGES ), DataFlags.FLAG_PERSIST_ON_FLUSH );
        flags.putFlag( Keys.concatenate( prefix, GrowingNeuralGasEntity.OUTPUT_AGE_SINCE_GROWTH ), DataFlags.FLAG_PERSIST_ON_FLUSH );
    }

    @Override
    public Class getConfigClass() {
        return io.agi.framework.entities.RegionConfig.class;
    }

    protected void doUpdateSelf() {

        // Do nothing unless the input is defined
        Data ffInput = getData( FF_INPUT );
        Data fbInput = getData( FB_INPUT );

        if ( ( ffInput == null ) || ( fbInput == null ) ) {
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
        io.agi.framework.entities.RegionConfig config = ( io.agi.framework.entities.RegionConfig ) _config;

        // Build the algorithm
        //RandomInstance.setSeed(randomSeed); // make the tests repeatable
        ObjectMap om = ObjectMap.GetInstance();
        RegionFactory rf = new RegionFactory();

        Region r = rf.create(
                om, regionName, getRandom(),
                inputWidth, inputHeight,
                feedbackWidthCells, feedbackHeightCells,
                config.organizerWidthCells, config.organizerHeightCells,
                config.classifierWidthCells, config.classifierHeightCells,
                config.receptiveFieldsTrainingSamples, config.receptiveFieldSize,
                config.organizerLearningRate, config.organizerLearningRateNeighbours, config.organizerNoiseMagnitude, config.organizerEdgeMaxAge, config.organizerStressLearningRate, config.organizerStressThreshold, config.organizerGrowthInterval,
                config.classifierLearningRate, config.classifierLearningRateNeighbours, config.classifierNoiseMagnitude, config.classifierEdgeMaxAge, config.classifierStressLearningRate, config.classifierStressThreshold, config.classifierGrowthInterval,
                config.predictorHiddenLayerScaleFactor, config.predictorLearningRate, config.predictorRegularization );

        // Load data, overwriting the default setup.
        copyDataFromPersistence( r );

        // Process
        if ( config.reset ) {
            r.reset();
        }

        r.update();

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
        r._regionActivity = getDataLazyResize( ACTIVITY, dataSizeRegion );

        r._regionPredictionOld = getDataLazyResize( PREDICTION_OLD, dataSizeRegion );
        r._regionPredictionNew = getDataLazyResize( PREDICTION_NEW, dataSizeRegion );

        r._regionPredictionFP = getDataLazyResize( PREDICTION_FP, dataSizeRegion );
        r._regionPredictionFN = getDataLazyResize( PREDICTION_FN, dataSizeRegion );

        // The organizer
        Data organizerInput = new Data( DataSize.create( Region.RECEPTIVE_FIELD_DIMENSIONS ) );
        copyDataFromPersistence( RegionConfig.SUFFIX_ORGANIZER, r._organizer, organizerWidthCells, organizerHeightCells, organizerInput );

        // The classifiers
        Data classifierInput = r._ffInput;
        for ( int y = 0; y < organizerSize.y; ++y ) {
            for ( int x = 0; x < organizerSize.x; ++x ) {
                int regionOffset = r._rc.getOrganizerOffset( x, y );
                GrowingNeuralGas classifier = r._classifiers.get( regionOffset );
                String prefix = Keys.concatenate( RegionConfig.SUFFIX_CLASSIFIER, String.valueOf( x ), String.valueOf( y ) );
                copyDataFromPersistence( prefix, classifier, classifierWidthCells, classifierHeightCells, classifierInput );
            }
        }

        // Predictor
        int layers = r._predictor._layers.size();

        for ( int l = 0; l < layers; ++l ) {
            NetworkLayer nl = r._predictor._layers.get( l );
            String prefix = Keys.concatenate( RegionConfig.SUFFIX_PREDICTOR, String.valueOf( l ) );

            nl._inputs = getData( Keys.concatenate( prefix, NetworkLayer.INPUT ), nl._inputs._dataSize );
            nl._weights = getData( Keys.concatenate( prefix, NetworkLayer.WEIGHTS ), nl._weights._dataSize );
            nl._biases = getData( Keys.concatenate( prefix, NetworkLayer.BIASES ), nl._biases._dataSize );
            nl._weightedSums = getData( Keys.concatenate( prefix, NetworkLayer.WEIGHTED_SUMS ), nl._weightedSums._dataSize );
            nl._outputs = getData( Keys.concatenate( prefix, NetworkLayer.OUTPUTS ), nl._outputs._dataSize );
            nl._errorGradients = getData( Keys.concatenate( prefix, NetworkLayer.ERROR_GRADIENTS ), nl._errorGradients._dataSize );
        }
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
        setData( FF_INPUT, r._ffInput );
        setData( FB_INPUT, r._fbInput );
        setData( FB_INPUT_OLD, r._fbInputOld );

        setData( ACTIVITY_OLD, r._regionActivityOld );
        setData( ACTIVITY_NEW, r._regionActivityNew );
        setData( ACTIVITY, r._regionActivity );

        setData( PREDICTION_OLD, r._regionPredictionOld );
        setData( PREDICTION_NEW, r._regionPredictionNew );

        setData( PREDICTION_FP, r._regionPredictionFP );
        setData( PREDICTION_FN, r._regionPredictionFN );

        // The organizer
        copyDataToPersistence( RegionConfig.SUFFIX_ORGANIZER, r._organizer );

        // The classifiers
        Point p = r._rc.getOrganizerSizeCells();

        for ( int y = 0; y < p.y; ++y ) {
            for ( int x = 0; x < p.x; ++x ) {
                int regionOffset = r._rc.getOrganizerOffset( x, y );
                GrowingNeuralGas classifier = r._classifiers.get( regionOffset );
                String prefix = Keys.concatenate( RegionConfig.SUFFIX_CLASSIFIER, String.valueOf( x ), String.valueOf( y ) );
                copyDataToPersistence( prefix, classifier );
            }
        }

        // Predictor
        int layers = r._predictor._layers.size();

        for ( int l = 0; l < layers; ++l ) {
            NetworkLayer nl = r._predictor._layers.get( l );
            String prefix = Keys.concatenate( RegionConfig.SUFFIX_PREDICTOR, String.valueOf( l ) );

            setData( Keys.concatenate( prefix, NetworkLayer.INPUT ), nl._inputs );
            setData( Keys.concatenate( prefix, NetworkLayer.WEIGHTS ), nl._weights );
            setData( Keys.concatenate( prefix, NetworkLayer.BIASES ), nl._biases );
            setData( Keys.concatenate( prefix, NetworkLayer.WEIGHTED_SUMS ), nl._weightedSums );
            setData( Keys.concatenate( prefix, NetworkLayer.OUTPUTS ), nl._outputs );
            setData( Keys.concatenate( prefix, NetworkLayer.ERROR_GRADIENTS ), nl._errorGradients );
        }
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
