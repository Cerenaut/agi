package io.agi.ef.entities;

import io.agi.core.ann.unsupervised.CompetitiveLearningConfig;
import io.agi.core.ann.unsupervised.DynamicSelfOrganizingMap;
import io.agi.core.ann.unsupervised.DynamicSelfOrganizingMapConfig;
import io.agi.core.data.Data;
import io.agi.core.data.DataSize;
import io.agi.core.math.RandomInstance;
import io.agi.core.orm.Keys;
import io.agi.core.orm.ObjectMap;
import io.agi.ef.Entity;
import io.agi.ef.Node;

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

    public void getInputKeys(Collection<String> keys) {
    }

    public void getOutputKeys(Collection<String> keys) {
        keys.add( OUTPUT );
    }

    protected void doUpdateSelf() {

        // Get all the parameters:
        float min = getPropertyFloat(MIN, 0.0f);
        float max = getPropertyFloat(MAX, 1.0f);
        int elements = getPropertyInt(ELEMENTS, 1);

        Data output = getDataLazyResize(OUTPUT, new DataSize(elements));
        Random r = getRandom();

        float range = max - min;

        for( int i = 0; i < elements; ++i ) {

            float x = r.nextFloat();
            x = x * range;
            x += min;

            output._values[ i ]  = x;
        }

        output.setRandom();
        setData( OUTPUT, output );
    }
}