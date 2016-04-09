package io.agi.framework.entities;

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

    public void getInputAttributes( Collection< String > attributes ) {
        attributes.add( INPUT );
    }

    public void getOutputAttributes( Collection< String > attributes, DataFlags flags ) {
        attributes.add( OUTPUT_WEIGHTS );
        attributes.add( OUTPUT_MASK );
        attributes.add( OUTPUT_ERROR );
        attributes.add( OUTPUT_ACTIVE );

        attributes.add( OUTPUT_CELL_STRESS );
        attributes.add( OUTPUT_CELL_AGES );
        attributes.add( OUTPUT_EDGES );
        attributes.add( OUTPUT_EDGES_AGES );
        attributes.add( OUTPUT_AGE_SINCE_GROWTH );
    }

    @Override
    public Class getConfigClass() {
        return io.agi.framework.entities.GrowingNeuralGasConfig.class;
    }

    protected void doUpdateSelf() {

        // Do nothing unless the input is
        Data input = getData( INPUT );

        if ( input == null ) {
            return;
        }

        // Get all the parameters:
        io.agi.framework.entities.GrowingNeuralGasConfig config = ( io.agi.framework.entities.GrowingNeuralGasConfig ) _config;

        int inputs = input.getSize();

        String implName = getName() + Keys.DELIMITER + IMPL_NAME; // the name of the object that implements

        // Create the config object:
        GrowingNeuralGasConfig c = new GrowingNeuralGasConfig();
        c.setup( _om, implName, getRandom(), inputs, config.widthCells, config.heightCells, config.learningRate, config.learningRateNeighbours, config.noiseMagnitude, config.edgeMaxAge, config.stressLearningRate, config.stressThreshold, config.growthInterval );

        // Create the implementing object itself, and copy data from persistence into it:
        GrowingNeuralGas gng = new GrowingNeuralGas( implName, _om );
        gng._c = c;
        gng._inputValues = input;

        int areaCells = config.widthCells * config.heightCells;

        DataSize dataSizeWeights = DataSize.create( config.widthCells, config.heightCells, inputs );
        DataSize dataSizeCells = DataSize.create( config.widthCells, config.heightCells );
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

        if ( config.reset ) {
            gng.reset();
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