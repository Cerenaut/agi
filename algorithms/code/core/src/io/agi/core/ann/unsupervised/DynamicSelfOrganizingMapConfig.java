package io.agi.core.ann.unsupervised;

import io.agi.core.orm.ObjectMap;

/**
 * Created by dave on 29/12/15.
 */
public class DynamicSelfOrganizingMapConfig extends CompetitiveLearningConfig {

    public String _keyElasticity = "i";
    public String _keyLearningRate = "learning-rate";
    public String _keyScaleUnit = "scale-unit";
    public String _keyScaleFactor = "scale-factor";

    public DynamicSelfOrganizingMapConfig() {
    }

    public void setup( ObjectMap om, String name, int inputs, int w, int h, float elasticity, float learningRate ) {
        super.setup( om, name, inputs, w, h );

        om.put( getKey( _keyElasticity ), elasticity );
        om.put( getKey( _keyLearningRate ), learningRate );
        om.put( getKey( _keyScaleUnit ), false );
        om.put( getKey( _keyScaleFactor ), 1.0f );
    }

    public boolean getScaleUnit() {
        Boolean b = _om.getBoolean(getKey( _keyScaleUnit) );
        return b;
    }

    public float getScaleFactor() {
        Float r = _om.getFloat(getKey( _keyScaleFactor) );
        return r.floatValue();
    }

    public float getElasticity() {
        Float r = _om.getFloat(getKey( _keyElasticity ) );
        return r.floatValue();
    }

    public float getLearningRate() {
        Float r = _om.getFloat(getKey( _keyLearningRate ) );
        return r.floatValue();
    }
}
