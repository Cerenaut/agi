package io.agi.framework.entities;

import io.agi.core.data.Data;
import io.agi.core.data.DataSize;
import io.agi.core.orm.ObjectMap;
import io.agi.framework.DataFlags;
import io.agi.framework.Entity;
import io.agi.framework.Framework;
import io.agi.framework.Node;
import io.agi.framework.persistence.Persistence;
import io.agi.framework.persistence.models.ModelEntity;

import java.util.Collection;
import java.util.Random;

/**
 * Creates a rolling window of values from a property, captured over time. The window is updated every update() with a
 * new value from the property. The oldest value is discarded.
 *
 * Created by dave on 2/04/16.
 */
public class ValueSeriesEntity extends Entity {

    public static final String ENTITY_TYPE = "value-series";

    public static final String OUTPUT = "output";

    public ValueSeriesEntity( ObjectMap om, Node n, ModelEntity model ) {
        super( om, n, model );
    }

    public void getInputKeys( Collection< String > keys ) {
    }

    public void getOutputKeys( Collection< String > keys, DataFlags flags ) {
        keys.add( OUTPUT );
    }

    public Class getConfigClass() {
        return ValueSeriesConfig.class;
    }

    protected void doUpdateSelf() {

        // Get all the parameters:
        ValueSeriesConfig config = (ValueSeriesConfig)_config;

        Data output = getDataLazyResize(OUTPUT, DataSize.create(config.period));

        Persistence p = _n.getPersistence();

        String stringValue = Framework.GetConfig(config.entityName, config.configPath, p);
        Float newValue = Float.valueOf( stringValue );

        if( newValue == null ) {
            newValue = 0.f;
        }

        // shift all the old values 1 place
        for( int i1 = 0; i1 < config.period; ++i1 ) {

            int i2 = i1+1;
            if( i2 >= config.period ) {
                continue;
            }

            float x1 = output._values[ i1 ];
            output._values[ i2 ] = x1;
        }

        output._values[ 0 ] = newValue;
        //output.setRandom();
        setData( OUTPUT, output );
    }
}
