package io.agi.core.ann.unsupervised;

import io.agi.core.ann.NetworkConfig;
import io.agi.core.orm.ObjectMap;

import java.util.Random;

/**
 * Created by dave on 1/01/16.
 */
public class GrowingNeuralGasConfig extends CompetitiveLearningConfig {

    public static final String LEARNING_RATE = "learning-rate";
    public static final String LEARNING_RATE_NEIGHBOURS = "learning-rate-neighbours";
    public static final String NOISE_MAGNITUDE = "noise-magnitude";
    public static final String EDGE_MAX_AGE = "edge-max-age";
    public static final String STRESS_LEARNING_RATE = "stress-learning-rate";
    public static final String STRESS_THRESHOLD = "stress-threshold";
    public static final String GROWTH_INTERVAL = "growth-interval";

    public GrowingNeuralGasConfig() {
    }

    public void setup(
            ObjectMap om,
            String name,
            Random r,
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
        super.setup( om, name, r, inputs, w, h );

        setLearningRate( learningRate );
        setLearningRateNeighbours( learningRateNeighbours );
        setNoiseMagnitude( noiseMagnitude );
        setEdgeMaxAge( edgeMaxAge );
        setStressLearningRate( stressLearningRate );
        setStressThreshold( stressThreshold );
        setGrowthInterval( growthInterval );
    }

    public void copyFrom( NetworkConfig nc, String name ) {
        super.copyFrom( nc, name );

        GrowingNeuralGasConfig c = ( GrowingNeuralGasConfig ) nc;

        setLearningRate( c.getLearningRate() );
        setLearningRateNeighbours( c.getLearningRateNeighbours() );
        setNoiseMagnitude( c.getNoiseMagnitude() );
        setEdgeMaxAge( c.getEdgeMaxAge() );
        setStressLearningRate( c.getStressLearningRate() );
        setStressThreshold( c.getStressThreshold() );
        setGrowthInterval( c.getGrowthInterval() );
    }

    public void setLearningRate( float r ) {
        _om.put( getKey( LEARNING_RATE ), r );
    }

    public void setLearningRateNeighbours( float r ) {
        _om.put( getKey( LEARNING_RATE_NEIGHBOURS ), r );
    }

    public void setNoiseMagnitude( float r ) {
        _om.put( getKey( NOISE_MAGNITUDE ), r );
    }

    public void setEdgeMaxAge( int n ) {
        _om.put( getKey( EDGE_MAX_AGE ), n );
    }

    public void setStressLearningRate( float r ) {
        _om.put( getKey( STRESS_LEARNING_RATE ), r );
    }

    public void setStressThreshold( float r ) {
        _om.put( getKey( STRESS_THRESHOLD ), r );
    }

    public void setGrowthInterval( int n ) {
        _om.put( getKey( GROWTH_INTERVAL ), n );
    }

    public float getLearningRate() {
        Float r = _om.getFloat( getKey( LEARNING_RATE ) );
        return r.floatValue();
    }

    public float getLearningRateNeighbours() {
        Float r = _om.getFloat( getKey( LEARNING_RATE_NEIGHBOURS ) );
        return r.floatValue();
    }

    public float getNoiseMagnitude() {
        Float r = _om.getFloat( getKey( NOISE_MAGNITUDE ) );
        return r.floatValue();
    }

    public int getEdgeMaxAge() {
        Integer n = _om.getInteger( getKey( EDGE_MAX_AGE ) );
        return n.intValue();
    }

    public float getStressLearningRate() {
        Float r = _om.getFloat( getKey( STRESS_LEARNING_RATE ) );
        return r.floatValue();
    }

    public float getStressThreshold() {
        Float r = _om.getFloat( getKey( STRESS_THRESHOLD ) );
        return r.floatValue();
    }

    public int getGrowthInterval() {
        Integer n = _om.getInteger( getKey( GROWTH_INTERVAL ) );
        return n.intValue();
    }
}
