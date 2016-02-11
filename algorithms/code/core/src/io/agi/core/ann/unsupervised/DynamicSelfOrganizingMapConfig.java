package io.agi.core.ann.unsupervised;

import io.agi.core.ann.NetworkConfig;
import io.agi.core.orm.ObjectMap;

/**
 * Created by dave on 29/12/15.
 */
public class DynamicSelfOrganizingMapConfig extends CompetitiveLearningConfig {

    public String _keyElasticity = "elasticity";
    public String _keyLearningRate = "learning-rate";
    public String _keyScaleUnit = "scale-unit";
    public String _keyScaleFactor = "scale-factor";

    public DynamicSelfOrganizingMapConfig() {
    }

    public void setup( ObjectMap om, String name, int inputs, int w, int h, float learningRate, float elasticity ) {
        super.setup(om, name, inputs, w, h);

        setLearningRate(learningRate);
        setElasticity(elasticity);
        setScaleUnit(false);
        setScaleFactor(1.0f);
    }

    public void copyFrom(NetworkConfig nc, String name ) {
        super.copyFrom(nc, name);

        DynamicSelfOrganizingMapConfig c = (DynamicSelfOrganizingMapConfig)nc;

        setLearningRate(c.getLearningRate());
        setElasticity(c.getElasticity());
        setScaleUnit(c.getScaleUnit());
        setScaleFactor(c.getScaleFactor());
    }

    public void setLearningRate( float r ) {
        _om.put(getKey(_keyLearningRate), r);
    }

    public void setElasticity( float r ) {
        _om.put(getKey(_keyElasticity), r);
    }

    public void setScaleUnit( boolean b ) {
        _om.put(getKey(_keyScaleUnit), b);
    }

    public void setScaleFactor( float r ) {
        _om.put(getKey(_keyScaleFactor), r);
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
