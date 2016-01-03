package io.agi.core.unsupervised;

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
        super.setup( om, inputs, w, h );

        om.put(_keyLearningRate, learningRate);
        om.put( _keyLearningRateNeighbours, learningRateNeighbours );
        om.put( _keyNoiseMagnitude, noiseMagnitude );
        om.put( _keyEdgeMaxAge, (float)edgeMaxAge );
        om.put( _keyStressLearningRate, stressLearningRate );
        om.put( _keyStressThreshold, stressThreshold );
        om.put( _keyGrowthInterval, (float)growthInterval );
    }

    public float getLearningRate() {
        Float r = _om.GetFloat(_keyLearningRate );
        return r.floatValue();
    }

    public float getLearningRateNeighbours() {
        Float r = _om.GetFloat(_keyLearningRateNeighbours );
        return r.floatValue();
    }

    public float getNoiseMagnitude() {
        Float r = _om.GetFloat(_keyNoiseMagnitude );
        return r.floatValue();
    }

    public int getEdgeMaxAge() {
        Float r = _om.GetFloat(_keyEdgeMaxAge );
        return (int)r.floatValue();
    }

    public float getStressLearningRate() {
        Float r = _om.GetFloat( _keyStressLearningRate );
        return r.floatValue();
    }

    public float getStressThreshold() {
        Float r = _om.GetFloat(_keyStressThreshold );
        return r.floatValue();
    }

    public int getGrowthInterval() {
        Float r = _om.GetFloat(_keyGrowthInterval );
        return (int)r.floatValue();
    }
}
