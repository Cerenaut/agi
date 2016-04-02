package io.agi.framework.entities;

import io.agi.core.ann.unsupervised.CompetitiveLearningConfig;
import io.agi.core.ann.unsupervised.GrowingNeuralGas;
import io.agi.core.ann.unsupervised.GrowingNeuralGasConfig;
import io.agi.core.data.Data;
import io.agi.core.data.DataSize;
import io.agi.core.orm.Keys;
import io.agi.core.orm.ObjectMap;
import io.agi.framework.DataFlags;
import io.agi.framework.Entity;
import io.agi.framework.Node;
import io.agi.framework.persistence.models.ModelEntity;

import java.util.Collection;

/**
 * Created by dave on 12/03/16.
 */
public class GrowingNeuralGasEntity extends Entity {

    public static final String ENTITY_TYPE = "gng";

    public static final String IMPL_NAME = "gng";

    public static final String INPUT = "input";

    public static final String OUTPUT_WEIGHTS = "output-weights";
    public static final String OUTPUT_MASK = "output-mask";
    public static final String OUTPUT_ERROR = "output-error";
    public static final String OUTPUT_ACTIVE = "output-active";

    public static final String OUTPUT_CELL_STRESS = "output-cell-stress";
    public static final String OUTPUT_CELL_AGES = "output-cell-ages";
    public static final String OUTPUT_EDGES = "output-edges";
    public static final String OUTPUT_EDGES_AGES = "output-edges-ages";
    public static final String OUTPUT_AGE_SINCE_GROWTH = "output-age-since-growth";

    public GrowingNeuralGasEntity( ObjectMap om, Node n, ModelEntity model ) {
        super( om, n, model );
    }

    public void getInputKeys( Collection< String > keys ) {
        keys.add(INPUT);
    }

    public void getOutputKeys( Collection< String > keys, DataFlags flags ) {
        keys.add( OUTPUT_WEIGHTS );
        keys.add( OUTPUT_MASK );
        keys.add( OUTPUT_ERROR );
        keys.add( OUTPUT_ACTIVE );

        keys.add( OUTPUT_CELL_STRESS );
        keys.add( OUTPUT_CELL_AGES );
        keys.add( OUTPUT_EDGES );
        keys.add( OUTPUT_EDGES_AGES );
        keys.add( OUTPUT_AGE_SINCE_GROWTH );
    }

    @Override
    public Class getConfigClass() {
        return GrowingNeuralGasConfig.class;
    }

    @Override
    public void getPropertyKeys( Collection< String > keys ) {
        keys.add( SUFFIX_AGE );
        keys.add( SUFFIX_SEED );
        keys.add( SUFFIX_RESET );
    }

    protected void doUpdateSelf() {

        // Do nothing unless the input is
        Data input = getData( INPUT );

        if ( input == null ) {
            return;
        }

        // Get all the parameters:
        int inputs = input.getSize();

        boolean reset = getPropertyBoolean( Entity.SUFFIX_RESET, false );
        float learningRate = getPropertyFloat( GrowingNeuralGasConfig.LEARNING_RATE, 0.1f );
        int widthCells = getPropertyInt( CompetitiveLearningConfig.WIDTH_CELLS, 8 );
        int heightCells = getPropertyInt( CompetitiveLearningConfig.HEIGHT_CELLS, 8 );

        float learningRateNeighbours = getPropertyFloat( GrowingNeuralGasConfig.LEARNING_RATE_NEIGHBOURS, 0.05f );
        float noiseMagnitude = getPropertyFloat( GrowingNeuralGasConfig.NOISE_MAGNITUDE, 0.005f );
        int edgeMaxAge = getPropertyInt( GrowingNeuralGasConfig.EDGE_MAX_AGE, 200 );
        float stressLearningRate = getPropertyFloat( GrowingNeuralGasConfig.STRESS_LEARNING_RATE, 0.15f );
        float stressThreshold = getPropertyFloat( GrowingNeuralGasConfig.STRESS_THRESHOLD, 0.01f );
        int growthInterval = getPropertyInt( GrowingNeuralGasConfig.GROWTH_INTERVAL, 2 );

        String implName = getName() + Keys.DELIMITER + IMPL_NAME; // the name of the object that implements

        // Create the config object:
        GrowingNeuralGasConfig c = new GrowingNeuralGasConfig();
        c.setup( _om, implName, getRandom(), inputs, widthCells, heightCells, learningRate, learningRateNeighbours, noiseMagnitude, edgeMaxAge, stressLearningRate, stressThreshold, growthInterval );

        // Create the implementing object itself, and copy data from persistence into it:
        GrowingNeuralGas gng = new GrowingNeuralGas( implName, _om );
        gng._c = c;
        gng._inputValues = input;

        int areaCells = widthCells * heightCells;

        DataSize dataSizeWeights = DataSize.create( widthCells, heightCells, inputs );
        DataSize dataSizeCells = DataSize.create( widthCells, heightCells );
        DataSize dataSizeEdges = DataSize.create( areaCells, areaCells );

        Data weights = getDataLazyResize( OUTPUT_WEIGHTS, dataSizeWeights );
        Data errors = getDataLazyResize( OUTPUT_ERROR, dataSizeCells ); // deep copies the size so they each own a copy
        Data activity = getDataLazyResize( OUTPUT_ACTIVE, dataSizeCells ); // deep copies the size so they each own a copy
        Data mask = getDataLazyResize( OUTPUT_MASK, dataSizeCells ); // deep copies the size so they each own a copy

        Data cellStress = getDataLazyResize( OUTPUT_CELL_STRESS, dataSizeCells );
        Data cellAges = getDataLazyResize( OUTPUT_CELL_AGES, dataSizeCells );
        Data edges = getDataLazyResize( OUTPUT_EDGES, dataSizeEdges );
        Data edgesAges = getDataLazyResize( OUTPUT_EDGES_AGES, dataSizeEdges );
        Data ageSinceGrowth = getDataLazyResize( OUTPUT_AGE_SINCE_GROWTH, DataSize.create( 1 ) );

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

        if ( reset ) {
            gng.reset();
            setPropertyBoolean( SUFFIX_RESET, false ); // turn off
            setPropertyInt( SUFFIX_AGE, 0 ); // turn off
        }

        gng.update();

        setData( OUTPUT_WEIGHTS, weights );
        setData( OUTPUT_ERROR, errors );
        setData( OUTPUT_ACTIVE, activity );
        setData( OUTPUT_MASK, mask );

        setData( OUTPUT_CELL_STRESS, cellStress );
        setData( OUTPUT_CELL_AGES, cellAges );
        setData( OUTPUT_EDGES, edges );
        setData( OUTPUT_EDGES_AGES, edgesAges );
        setData( OUTPUT_AGE_SINCE_GROWTH, ageSinceGrowth );
    }
}