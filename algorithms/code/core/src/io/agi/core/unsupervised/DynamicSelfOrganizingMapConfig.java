package io.agi.core.unsupervised;

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

    public void setup( ObjectMap om, int inputs, int w, int h, float elasticity, float learningRate ) {
        super.setup( om, inputs, w, h );

        om.put( _keyElasticity, elasticity );
        om.put( _keyLearningRate, learningRate );
        om.put( _keyScaleUnit, false );
        om.put( _keyScaleFactor, 1.0f );
    }

    public boolean getScaleUnit() {
        Boolean b = _om.GetBoolean(_keyScaleUnit);
        return b;
    }

    public float getScaleFactor() {
        Float r = _om.GetFloat(_keyScaleFactor);
        return r.floatValue();
    }

    public float getElasticity() {
        Float r = _om.GetFloat(_keyElasticity );
        return r.floatValue();
    }

    public float getLearningRate() {
        Float r = _om.GetFloat(_keyLearningRate );
        return r.floatValue();
    }
}
