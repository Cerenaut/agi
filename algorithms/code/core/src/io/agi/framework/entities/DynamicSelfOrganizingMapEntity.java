package io.agi.framework.entities;

import io.agi.core.ann.unsupervised.CompetitiveLearningConfig;
import io.agi.core.ann.unsupervised.DynamicSelfOrganizingMap;
import io.agi.core.ann.unsupervised.DynamicSelfOrganizingMapConfig;
import io.agi.core.data.Data;
import io.agi.core.data.DataSize;
import io.agi.core.orm.Keys;
import io.agi.core.orm.ObjectMap;
import io.agi.framework.Entity;
import io.agi.framework.Node;

import java.util.Collection;

/**
 * Created by dave on 12/03/16.
 */
public class DynamicSelfOrganizingMapEntity extends Entity {

    public static final String ENTITY_TYPE = "dsom";

    public static final String IMPL_NAME = "dsom";

    public static final String INPUT = "input";

    public static final String OUTPUT_WEIGHTS = "output-weights";
    public static final String OUTPUT_MASK = "output-mask";
    public static final String OUTPUT_ERROR = "output-error";
    public static final String OUTPUT_ACTIVE = "output-active";

    public DynamicSelfOrganizingMapEntity( String entityName, ObjectMap om, String type, Node n ) {
        super( entityName, om, type, n );
    }

    public void getInputKeys( Collection<String> keys ) {
        keys.add( INPUT );
    }

    public void getOutputKeys( Collection<String> keys ) {
        keys.add( OUTPUT_WEIGHTS );
        keys.add( OUTPUT_MASK );
        keys.add( OUTPUT_ERROR );
        keys.add( OUTPUT_ACTIVE );
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
        float learningRate = getPropertyFloat( DynamicSelfOrganizingMapConfig.LEARNING_RATE, 0.1f );
        float elasticity = getPropertyFloat( DynamicSelfOrganizingMapConfig.ELASTICITY, 0.1f );
        int widthCells = getPropertyInt( CompetitiveLearningConfig.WIDTH_CELLS, 10 );
        int heightCells = getPropertyInt( CompetitiveLearningConfig.HEIGHT_CELLS, 10 );

        String implName = getName() + Keys.DELIMITER + IMPL_NAME; // the name of the object that implements

        // Create the config object:
        DynamicSelfOrganizingMapConfig dsomc = new DynamicSelfOrganizingMapConfig();
        dsomc.setup( _om, implName, inputs, widthCells, heightCells, learningRate, elasticity );

        // Create the implementing object itself, and copy data from persistence into it:
        DynamicSelfOrganizingMap dsom = new DynamicSelfOrganizingMap( implName, _om );
        dsom._c = dsomc;
        dsom._inputValues = input;

        DataSize dataSizeWeights = DataSize.create( widthCells, heightCells, inputs );
        DataSize dataSizeCells = DataSize.create( widthCells, heightCells );

        Data weights = getDataLazyResize( OUTPUT_WEIGHTS, dataSizeWeights );
        Data errors = getDataLazyResize( OUTPUT_ERROR, dataSizeCells ); // deep copies the size so they each own a copy
        Data activity = getDataLazyResize( OUTPUT_ACTIVE, dataSizeCells ); // deep copies the size so they each own a copy
        Data mask = getDataLazyResize( OUTPUT_MASK, dataSizeCells ); // deep copies the size so they each own a copy

        dsom._inputValues = input;
        dsom._cellWeights = weights;
        dsom._cellErrors = errors;
        dsom._cellActivity = activity;
        dsom._cellMask = mask;

        if ( reset ) {
            dsom.reset();
            setPropertyBoolean( SUFFIX_RESET, false ); // turn off
        }

        dsom.update();

        setData( OUTPUT_WEIGHTS, weights );
        setData( OUTPUT_ERROR, errors );
        setData( OUTPUT_ACTIVE, activity );
        setData( OUTPUT_MASK, mask );
    }
}