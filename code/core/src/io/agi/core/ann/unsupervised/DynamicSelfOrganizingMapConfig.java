package io.agi.core.ann.unsupervised;

import io.agi.core.ann.NetworkConfig;
import io.agi.core.orm.ObjectMap;

import java.util.Random;

/**
 * Created by dave on 29/12/15.
 */
public class DynamicSelfOrganizingMapConfig extends CompetitiveLearningConfig {

    public static final String ELASTICITY = "elasticity";
    public static final String LEARNING_RATE = "learning-rate";

    public String _keyElasticity = ELASTICITY;
    public String _keyLearningRate = LEARNING_RATE;

    public DynamicSelfOrganizingMapConfig() {
    }

    public void setup( ObjectMap om, String name, Random r, int inputs, int w, int h, float learningRate, float elasticity ) {
        super.setup( om, name, r, inputs, w, h );

        setLearningRate( learningRate );
        setElasticity( elasticity );
    }

    public void copyFrom( NetworkConfig nc, String name ) {
        super.copyFrom( nc, name );

        DynamicSelfOrganizingMapConfig c = ( DynamicSelfOrganizingMapConfig ) nc;

        setLearningRate( c.getLearningRate() );
        setElasticity( c.getElasticity() );
    }

    public void setLearningRate( float r ) {
        _om.put( getKey( _keyLearningRate ), r );
    }

    public void setElasticity( float r ) {
        _om.put( getKey( _keyElasticity ), r );
    }

    public float getElasticity() {
        Float r = _om.getFloat( getKey( _keyElasticity ) );
        return r.floatValue();
    }

    public float getLearningRate() {
        Float r = _om.getFloat( getKey( _keyLearningRate ) );
        return r.floatValue();
    }
}
