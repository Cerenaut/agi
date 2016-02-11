package io.agi.core.ann.unsupervised;

import io.agi.core.ann.NetworkConfig;
import io.agi.core.orm.ObjectMap;

/**
 * Created by dave on 1/01/16.
 */
public class GrowingNeuralGasConfig extends CompetitiveLearningConfig {

    public String _keyLearningRate = "learning-rate";
    public String _keyLearningRateNeighbours = "learning-rate-neighbours";
    public String _keyNoiseMagnitude = "noise-magnitude";
    public String _keyEdgeMaxAge = "edge-max-age";
    public String _keyStressLearningRate = "stress-learning-rate";
    public String _keyStressThreshold = "stress-threshold";
    public String _keyGrowthInterval = "growth-interval";

    public GrowingNeuralGasConfig() {
    }

    public void setup(
            ObjectMap om,
            String name,
            int inputs,
            int w,
            int h,
            float learningRate,
            float learningRateNeighbours,
            float noiseMagnitude,
            int edgeMaxAge,
            float stressLearningRate,
            float stressThreshold,
            int growthInterval ) {
        super.setup(om, name, inputs, w, h);

        setLearningRate(learningRate);
        setLearningRateNeighbours(learningRateNeighbours);
        setNoiseMagnitude(noiseMagnitude);
        setEdgeMaxAge(edgeMaxAge);
        setStressLearningRate(stressLearningRate);
        setStressThreshold(stressThreshold);
        setGrowthInterval(growthInterval);
    }

    public void copyFrom( NetworkConfig nc, String name ) {
        super.copyFrom(nc, name);

        GrowingNeuralGasConfig c = (GrowingNeuralGasConfig)nc;

        setLearningRate( c.getLearningRate() );
        setLearningRateNeighbours( c.getLearningRateNeighbours() );
        setNoiseMagnitude( c.getNoiseMagnitude() );
        setEdgeMaxAge( c.getEdgeMaxAge() );
        setStressLearningRate( c.getStressLearningRate() );
        setStressThreshold( c.getStressThreshold() );
        setGrowthInterval( c.getGrowthInterval() );
    }

    public void setLearningRate( float r ) {
        _om.put(getKey(_keyLearningRate), r);
    }
    public void setLearningRateNeighbours( float r ) {
        _om.put(getKey(_keyLearningRateNeighbours), r);
    }
    public void setNoiseMagnitude( float r ) {
        _om.put( getKey( _keyNoiseMagnitude ), r );
    }
    public void setEdgeMaxAge( int n ) {
        _om.put(getKey(_keyEdgeMaxAge), n);
    }
    public void setStressLearningRate( float r ) {
        _om.put(getKey(_keyStressLearningRate), r);
    }
    public void setStressThreshold( float r ) {
        _om.put(getKey(_keyStressThreshold), r);
    }
    public void setGrowthInterval( int n ) {
        _om.put(getKey(_keyGrowthInterval), n);
    }

    public float getLearningRate() {
        Float r = _om.getFloat( getKey( _keyLearningRate ) );
        return r.floatValue();
    }

    public float getLearningRateNeighbours() {
        Float r = _om.getFloat( getKey( _keyLearningRateNeighbours ) );
        return r.floatValue();
    }

    public float getNoiseMagnitude() {
        Float r = _om.getFloat( getKey( _keyNoiseMagnitude ) );
        return r.floatValue();
    }

    public int getEdgeMaxAge() {
        Integer n = _om.getInteger(getKey(_keyEdgeMaxAge));
        return n.intValue();
    }

    public float getStressLearningRate() {
        Float r = _om.getFloat( getKey( _keyStressLearningRate ) );
        return r.floatValue();
    }

    public float getStressThreshold() {
        Float r = _om.getFloat( getKey( _keyStressThreshold ) );
        return r.floatValue();
    }

    public int getGrowthInterval() {
        Integer n = _om.getInteger( getKey( _keyGrowthInterval ) );
        return n.intValue();
    }
}
