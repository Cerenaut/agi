/*
 * Copyright (c) 2016.
 *
 * This file is part of Project AGI. <http://agi.io>
 *
 * Project AGI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Project AGI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Project AGI.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.agi.core.ann.unsupervised;

import io.agi.core.ann.NetworkConfig;
import io.agi.core.orm.ObjectMap;

import java.awt.*;
import java.util.Random;

/**
 * Note, stress threshold is compared to recent-moving-average (exponentially weighted) of sum of input error.
 * Created by dave on 1/01/16.
 */
public class GrowingNeuralGasConfig extends CompetitiveLearningConfig {

    public static final String LEARNING_RATE = "learning-rate";
    public static final String LEARNING_RATE_NEIGHBOURS = "learning-rate-neighbours";
    public static final String STRESS_LEARNING_RATE = "stress-learning-rate";
    public static final String STRESS_SPLIT_LEARNING_RATE = "stress-split-learning-rate";
    public static final String STRESS_THRESHOLD = "stress-threshold";

    public static final String UTILITY_LEARNING_RATE = "utility-learning-rate";
    public static final String UTILITY_THRESHOLD = "utility-threshold";

    public static final String GROWTH_INTERVAL = "growth-interval";
    public static final String EDGE_MAX_AGE = "edge-max-age";
    public static final String NOISE_MAGNITUDE = "noise-magnitude";
    public static final String DENOISE_PERCENTAGE = "denoise-percentage";

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
            float stressSplitLearningRate,
            float stressThreshold,
            float utilityLearningRate,
            float utilityThreshold,
            int growthInterval,
            float denoisePercentage ) {
        super.setup( om, name, r, inputs, w, h );

        setLearningRate( learningRate );
        setLearningRateNeighbours( learningRateNeighbours );
        setNoiseMagnitude( noiseMagnitude );
        setEdgeMaxAge( edgeMaxAge );
        setStressLearningRate( stressLearningRate );
        setStressSplitLearningRate( stressSplitLearningRate );
        setStressThreshold( stressThreshold );
        setUtilityLearningRate( utilityLearningRate );
        setUtilityThreshold( utilityThreshold );
        setGrowthInterval( growthInterval );
        setDenoisePercentage( denoisePercentage );
    }

    public void copyFrom( NetworkConfig nc, String name ) {
        super.copyFrom( nc, name );

        GrowingNeuralGasConfig c = ( GrowingNeuralGasConfig ) nc;

        setLearningRate( c.getLearningRate() );
        setLearningRateNeighbours( c.getLearningRateNeighbours() );
        setNoiseMagnitude( c.getNoiseMagnitude() );
        setEdgeMaxAge( c.getEdgeMaxAge() );
        setStressLearningRate( c.getStressLearningRate() );
        setStressSplitLearningRate( c.getStressSplitLearningRate() );
        setStressThreshold( c.getStressThreshold() );
        setUtilityLearningRate( c.getUtilityLearningRate() );
        setUtilityThreshold( c.getUtilityThreshold() );
        setGrowthInterval( c.getGrowthInterval() );
        setDenoisePercentage( c.getDenoisePercentage() );
    }

    public Point getSizeCells() {
        int width = getWidthCells();
        int height = getHeightCells();
        return new Point( width, height );
    }

    public int getAreaCells() {
        int width = getWidthCells();
        int height = getHeightCells();
        return width * height;
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

    public void setStressSplitLearningRate( float r ) {
        _om.put( getKey( STRESS_SPLIT_LEARNING_RATE ), r );
    }

    public void setStressThreshold( float r ) {
        _om.put( getKey( STRESS_THRESHOLD ), r );
    }

    public void setUtilityLearningRate( float r ) {
        _om.put( getKey( UTILITY_LEARNING_RATE ), r );
    }

    public void setUtilityThreshold( float r ) {
        _om.put( getKey( UTILITY_THRESHOLD ), r );
    }

    public void setGrowthInterval( int n ) {
        _om.put( getKey( GROWTH_INTERVAL ), n );
    }

    public void setDenoisePercentage( float denoisePercentage ) {
        _om.put( getKey( DENOISE_PERCENTAGE ), denoisePercentage );
    }

    public float getLearningRate() {
        return _om.getFloat( getKey( LEARNING_RATE ) );
    }

    public float getLearningRateNeighbours() {
        return _om.getFloat( getKey( LEARNING_RATE_NEIGHBOURS ) );
    }

    public float getNoiseMagnitude() {
        return _om.getFloat( getKey( NOISE_MAGNITUDE ) );
    }

    public int getEdgeMaxAge() {
        return _om.getInteger( getKey( EDGE_MAX_AGE ) );
    }

    public float getStressLearningRate() {
        return _om.getFloat( getKey( STRESS_LEARNING_RATE ) );
    }

    public float getStressSplitLearningRate() {
        return _om.getFloat( getKey( STRESS_SPLIT_LEARNING_RATE ) );
    }

    public float getStressThreshold() {
        return _om.getFloat( getKey( STRESS_THRESHOLD ) );
    }

    public float getUtilityLearningRate() {
        return _om.getFloat( getKey( UTILITY_LEARNING_RATE ) );
    }

    public float getUtilityThreshold() {
        return _om.getFloat( getKey( UTILITY_THRESHOLD ) );
    }

    public int getGrowthInterval() {
        return _om.getInteger( getKey( GROWTH_INTERVAL ) );
    }

    public float getDenoisePercentage() {
        return _om.getFloat( getKey( DENOISE_PERCENTAGE ) );
    }
}
