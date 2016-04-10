package io.agi.framework.entities;

import io.agi.core.data.Data;
import io.agi.core.data.DataSize;
import io.agi.core.orm.ObjectMap;
import io.agi.framework.DataFlags;
import io.agi.framework.Entity;
import io.agi.framework.Node;
import io.agi.framework.persistence.models.ModelEntity;

import java.util.Collection;
import java.util.Random;

/**
 * Created by dave on 12/03/16.
 */
public class RandomVectorEntity extends Entity {

    public static final String ENTITY_TYPE = "random-vector";

    public static final String ELEMENTS = "elements";
    public static final String MIN = "min";
    public static final String MAX = "max";

    public static final String OUTPUT = "output";

    public RandomVectorEntity( ObjectMap om, Node n, ModelEntity model ) {
        super( om, n, model );
    }

    public void getInputAttributes( Collection< String > attributes ) {
    }

    public void getOutputAttributes( Collection< String > attributes, DataFlags flags ) {
        attributes.add( OUTPUT );
    }

    @Override
    public Class getConfigClass() {
        return RandomVectorConfig.class;
    }

    protected void doUpdateSelf() {

        // Get all the parameters:
        RandomVectorConfig config = ( RandomVectorConfig ) _config;

        Data output = getDataLazyResize( OUTPUT, DataSize.create( config.elements ) );
        Random r = getRandom();

        float range = config.max - config.min;

        for ( int i = 0; i < config.elements; ++i ) {

            float x = r.nextFloat();
            x = x * range;
            x += config.min;

            output._values[ i ] = x;
        }

        //output.setRandom();
        setData( OUTPUT, output );
    }
}