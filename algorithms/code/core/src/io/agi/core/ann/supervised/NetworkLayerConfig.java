package io.agi.core.ann.supervised;

import io.agi.core.ann.NetworkConfig;
import io.agi.core.orm.ObjectMap;

import java.util.Random;

/**
 * Created by dave on 3/01/16.
 */
public class NetworkLayerConfig extends NetworkConfig {

//    public ObjectMap _om;

    public static final String ACTIVATION_FUNCTION = "activation-function";
    public static final String LEARNING_RATE = "learning-rate";
    public static final String INPUTS = "i";
    public static final String CELLS = "w";

    public NetworkLayerConfig() {
    }

    public void setup( ObjectMap om, String name, Random r, int inputs, int cells, float learningRate, String activationFunction ) {
        super.setup( om, name, r );

        setInputs( inputs );
        setCells( cells );
        setLearningRate( learningRate );
        setActivationFunction( activationFunction );
    }

    public void copyFrom( NetworkConfig nc, String name ) {
        super.copyFrom( nc, name );

        NetworkLayerConfig c = ( NetworkLayerConfig ) nc;

        setInputs( c.getInputs() );
        setCells( c.getCells() );
        setLearningRate( c.getLearningRate() );
        setActivationFunction( c.getActivationFunction() );
    }

    public int getInputs() {
        Integer i = _om.getInteger( getKey( INPUTS ) );
        return i.intValue();
    }

    public void setInputs( int inputs ) {
        _om.put( getKey( INPUTS ), inputs );
    }

    public void setCells( int cells ) {
        _om.put( getKey( CELLS ), cells );
    }

    public int getCells() {
        Integer w = _om.getInteger( getKey( CELLS ) );
        return w.intValue();
    }

    public void setLearningRate( float learningRate ) {
        _om.put( getKey( LEARNING_RATE ), learningRate );
    }

    public float getLearningRate() {
        Float r = _om.getFloat( getKey( LEARNING_RATE ) );
        return r.floatValue();
    }

    public void setActivationFunction( String costFunction ) {
        _om.put( getKey( ACTIVATION_FUNCTION ), costFunction );
    }

    public String getActivationFunction() {
        return ( String ) _om.get( getKey( ACTIVATION_FUNCTION ) );
    }
}
