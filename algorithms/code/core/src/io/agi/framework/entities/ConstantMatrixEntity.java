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
 * Produces a constant output.
 *
 * Useful for creating stub data for connecting to entities that require some form of input to be provided.
 *
 * Created by dave on 26/03/16.
 */
public class ConstantMatrixEntity extends Entity {

    public static final String ENTITY_TYPE = "constant-matrix";

    public static final String WIDTH = "width";
    public static final String HEIGHT = "height";
    public static final String VALUE = "value";

    public static final String OUTPUT = "output";

    public ConstantMatrixEntity(String entityName, ObjectMap om, String type, Node n) {
        super( entityName, om, type, n );
    }

    public void getInputKeys(Collection<String> keys) {
    }

    public void getOutputKeys(Collection<String> keys, DataFlags flags) {
        keys.add( OUTPUT );
    }

    @Override
    public void getPropertyKeys( Collection< String > keys ) {
        keys.add( VALUE );
        keys.add( WIDTH );
        keys.add( HEIGHT );
    }

    protected void doUpdateSelf() {

        // Get all the parameters:
        float defaultValue = getPropertyFloat(VALUE, 0.0f);
        int width = getPropertyInt( WIDTH, 1);
        int height = getPropertyInt( HEIGHT, 1);

        Data output = getDataLazyResize(OUTPUT, DataSize.create( width, height ) );
        output.set( defaultValue );

        setData( OUTPUT, output );
    }
}
