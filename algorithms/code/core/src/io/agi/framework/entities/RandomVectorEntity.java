package io.agi.framework.entities;

import io.agi.core.data.Data;
import io.agi.core.data.DataSize;
import io.agi.core.orm.ObjectMap;
import io.agi.framework.DataFlags;
import io.agi.framework.Entity;
import io.agi.framework.Node;

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

    public RandomVectorEntity( String entityName, ObjectMap om, String type, Node n ) {
        super( entityName, om, type, n );
    }

    public void getInputKeys( Collection< String > keys ) {
    }

    public void getOutputKeys( Collection< String > keys, DataFlags flags ) {
        keys.add( OUTPUT );
    }

    @Override
    public void getPropertyKeys( Collection< String > keys ) {
        keys.add( SUFFIX_AGE );
        keys.add( SUFFIX_SEED );
        keys.add( SUFFIX_RESET );

        keys.add( MIN );
        keys.add( MAX );
        keys.add( ELEMENTS );
    }

    protected void doUpdateSelf() {

        // Get all the parameters:
        float min = getPropertyFloat( MIN, 0.0f );
        float max = getPropertyFloat( MAX, 1.0f );
        int elements = getPropertyInt( ELEMENTS, 1 );

        Data output = getDataLazyResize( OUTPUT, DataSize.create( elements ) );
        Random r = getRandom();

        float range = max - min;

        for ( int i = 0; i < elements; ++i ) {

            float x = r.nextFloat();
            x = x * range;
            x += min;

            output._values[ i ] = x;
        }

        //output.setRandom();
        setData( OUTPUT, output );
    }
}