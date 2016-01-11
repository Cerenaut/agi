package io.agi.core.ann.unsupervised;

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
        super.setup( om, name, inputs, w, h );

        om.put( getKey( _keyLearningRate ), learningRate);
        om.put( getKey( _keyLearningRateNeighbours ), learningRateNeighbours );
        om.put( getKey( _keyNoiseMagnitude ), noiseMagnitude );
        om.put( getKey( _keyEdgeMaxAge ), (float)edgeMaxAge );
        om.put( getKey( _keyStressLearningRate ), stressLearningRate );
        om.put( getKey( _keyStressThreshold ), stressThreshold );
        om.put( getKey( _keyGrowthInterval ), (float)growthInterval );
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
        Float r = _om.getFloat( getKey( _keyEdgeMaxAge ) );
        return (int)r.floatValue();
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
        Float r = _om.getFloat( getKey( _keyGrowthInterval ) );
        return (int)r.floatValue();
    }
}
