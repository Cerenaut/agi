package io.agi.framework.demo.light;

import io.agi.core.data.Data;
import io.agi.core.data.DataSize;
import io.agi.core.math.Unit;
import io.agi.core.orm.ObjectMap;
import io.agi.framework.DataFlags;
import io.agi.framework.Entity;
import io.agi.framework.Node;
import io.agi.framework.persistence.models.ModelEntity;

import java.util.Collection;

/**
 * Created by dave on 20/02/16.
 */
public class LightSourceEntity extends Entity {

    public static final String ENTITY_TYPE = "light-source";
    public static final String CONTROL_INPUT = "control-input";
    public static final String LIGHT_OUTPUT = "light-output";
    public static final String RANDOM_OUTPUT = "random-output";
    public static final String MATRIX_OUTPUT = "matrix-output";

    public LightSourceEntity( ObjectMap om, Node n, ModelEntity me ) {
        super( om, n, me );
    }

    public void getInputAttributes( Collection< String > attributes ) {
        attributes.add( CONTROL_INPUT );
    }

    public void getOutputAttributes( Collection< String > attributes, DataFlags flags ) {
        attributes.add( LIGHT_OUTPUT );
        attributes.add( RANDOM_OUTPUT );
        attributes.add( MATRIX_OUTPUT );
    }

    @Override
    public Class getConfigClass() {
        return LightSourceEntityConfig.class;
    }

    protected void doUpdateSelf() {

        LightSourceEntityConfig config = ( LightSourceEntityConfig ) _config;

        Data input = getData( CONTROL_INPUT );

        if ( input == null ) {
            return;
        }

        Data output = getData( LIGHT_OUTPUT, new DataSize( input._dataSize ) );

        int elements = input._dataSize.getVolume();

        for ( int i = 0; i < elements; ++i ) {
            float inputValue = input._values[ i ];
            float oldOutputValue = output._values[ i ];
            float newOutputValue = getLight( inputValue, oldOutputValue, config.learningRate );
            output._values[ i ] = newOutputValue;
        }

        setData( LIGHT_OUTPUT, output );

        Data random = new Data( 10 );
        random.setRandomNormal( _r );
        setData( RANDOM_OUTPUT, random );

        Data matrix = new Data( 20, 20 );
        matrix.setRandom( _r );
        setData( MATRIX_OUTPUT, matrix );
    }

    protected float getLight( float input, float oldOutput, float learningRate ) {
        float x = 0.f;
        if ( input > 0.5 ) {
            x = 1.f;
        }
        float newOutput = Unit.lerp( x, oldOutput, learningRate );
        return newOutput;
    }
}
