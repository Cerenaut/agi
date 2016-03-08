package io.agi.ef.demo;

import io.agi.core.data.Data;
import io.agi.core.data.DataSize;
import io.agi.core.math.Unit;
import io.agi.core.orm.ObjectMap;
import io.agi.ef.Entity;
import io.agi.ef.Node;

import java.util.Collection;

/**
 * Created by dave on 20/02/16.
 */
public class LightSource extends Entity {

    public static final String ENTITY_TYPE = "light-source";
    public static final String CONTROL_INPUT = "control-input";
    public static final String LIGHT_OUTPUT = "light-output";
    public static final String RANDOM_OUTPUT = "random-output";
    public static final String MATRIX_OUTPUT = "matrix-output";
    public static final String LEARNING_RATE = "learning-rate";

    public LightSource( String entityName, ObjectMap om, String type, String parent, Node n ) {
        super( entityName, om, type, parent, n );
    }

    public void getInputKeys( Collection<String> keys ) {
        keys.add( CONTROL_INPUT );
    }

    public void getOutputKeys( Collection<String> keys ) {
        keys.add( LIGHT_OUTPUT );
        keys.add( RANDOM_OUTPUT );
        keys.add( MATRIX_OUTPUT );
    }

    protected void doUpdateSelf() {

        float learningRate = getPropertyFloat( LEARNING_RATE, 0.1f );

        Data input = getData( CONTROL_INPUT );

        if ( input == null ) {
            return;
        }

        Data output = getData( LIGHT_OUTPUT, new DataSize( input._dataSize ) );

        int elements = input._dataSize.getVolume();

        for ( int i = 0; i < elements; ++i ) {
            float inputValue = input._values[ i ];
            float oldOutputValue = output._values[ i ];
            float newOutputValue = getLight( inputValue, oldOutputValue, learningRate );
            output._values[ i ] = newOutputValue;
        }

        setData( LIGHT_OUTPUT, output );

        Data random = new Data( 10 );
        random.setRandomNormal();
        setData( RANDOM_OUTPUT, random );

        Data matrix = new Data( 20, 20 );
        matrix.setRandom();
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
