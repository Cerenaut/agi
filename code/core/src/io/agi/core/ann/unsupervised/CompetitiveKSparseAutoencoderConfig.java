/*
 * Copyright (c) 2017.
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

import java.util.Random;

/**
 * Created by dave on 1/07/16.
 */
public class CompetitiveKSparseAutoencoderConfig extends CompetitiveLearningConfig {

    public static final String LEARNING_RATE = "learning-rate";
    public static final String MOMENTUM = "momentum";
    public static final String WEIGHTS_STD_DEV = "weights-std-dev";
    public static final String SPARSITY = "sparsity";
    public static final String SPARSITY_OUTPUT = "sparsity-output";
    public static final String BATCH_COUNT = "batch-age";
    public static final String BATCH_SIZE = "batch-size";
    public static final String BISECTION_COUNT = "bisection-interval";
    public static final String BISECTION_INTERVAL = "bisection-interval";
    public static final String RATE_LEARNING_RATE = "rate-learning-rate";
    public static final String CORRELATION_LEARNING_RATE = "correlation-learning-rate";

    public CompetitiveKSparseAutoencoderConfig() {
    }

    /**
     *
     * @param om
     * @param name
     * @param r
     * @param inputs
     * @param w
     * @param h
     * @param learningRate
     * @param sparsityOutput
     * @param sparsity
     * @param weightsStdDev
     * @param batchCount
     * @param batchSize
     */
    public void setup(
            ObjectMap om,
            String name,
            Random r,
            int inputs,
            int w,
            int h,
            float learningRate,
            float momentum,
            float sparsityOutput,
            int sparsity,
            float weightsStdDev,
            int batchCount,
            int batchSize,
            int bisectionCount,
            int bisectionInterval,
            float rateLearningRate,
            float correlationLearningRate ){

        super.setup( om, name, r, inputs, w, h );

        setLearningRate( learningRate );
        setSparsityOutput( sparsityOutput );
        setSparsity( sparsity );
        setMomentum( momentum );
        setWeightsStdDev( weightsStdDev );
        setBatchCount( batchCount );
        setBatchSize( batchSize );

        setBisectionCount( bisectionCount );
        setBisectionInterval( bisectionInterval );
        setRateLearningRate( rateLearningRate );
        setCorrelationLearningRate( correlationLearningRate );
    }

    public void copyFrom( NetworkConfig nc, String name ) {
        super.copyFrom( nc, name );

        CompetitiveKSparseAutoencoderConfig c = ( CompetitiveKSparseAutoencoderConfig ) nc;

        setLearningRate( c.getLearningRate() );
        setSparsityOutput( c.getSparsityOutput() );
        setSparsity( c.getSparsity() );
        setMomentum( c.getMomentum() );
        setWeightsStdDev( c.getWeightsStdDev() );
        setBatchCount( c.getBatchCount() );
        setBatchSize( c.getBatchSize() );

        setBisectionCount( c.getBisectionCount() );
        setBisectionInterval( c.getBisectionInterval() );
        setRateLearningRate( c.getRateLearningRate() );
        setCorrelationLearningRate( c.getCorrelationLearningRate() );
    }

    public void setLearningRate( float r ) {
        _om.put( getKey( LEARNING_RATE ), r );
    }

    public void setSparsityOutput( float r ) {
        _om.put( getKey( SPARSITY_OUTPUT ), r );
    }

    public void setSparsity( int n ) {
        _om.put( getKey( SPARSITY ), n );
    }

    public void setMomentum( float r ) {
        _om.put( getKey( MOMENTUM ), r );
    }

    public void setWeightsStdDev( float r ) {
        _om.put( getKey( WEIGHTS_STD_DEV ), r );
    }

    public void setBatchCount( int n ) {
        _om.put( getKey( BATCH_COUNT ), n );
    }

    public void setBatchSize( int n ) {
        _om.put( getKey( BATCH_SIZE ), n );
    }

    public void setBisectionCount( int n ) {
        _om.put( getKey( BISECTION_COUNT ), n );
    }

    public void setBisectionInterval( int n ) {
        _om.put( getKey( BISECTION_INTERVAL ), n );
    }

    public void setRateLearningRate( float n ) {
        _om.put( getKey( RATE_LEARNING_RATE ), n );
    }

    public void setCorrelationLearningRate( float n ) {
        _om.put( getKey( CORRELATION_LEARNING_RATE ), n );
    }

    public float getWeightsStdDev() {
        Float r = _om.getFloat( getKey( WEIGHTS_STD_DEV ) );
        return r.floatValue();
    }

    public float getMomentum() {
        Float r = _om.getFloat( getKey( MOMENTUM ) );
        return r.floatValue();
    }

    public float getLearningRate() {
        Float r = _om.getFloat( getKey( LEARNING_RATE ) );
        return r.floatValue();
    }

    public float getSparsityOutput() {
        Float r = _om.getFloat( getKey( SPARSITY_OUTPUT ) );
        return r.floatValue();
    }

    public int getSparsity() {
        Integer n = _om.getInteger( getKey( SPARSITY ) );
        return n.intValue();
    }

    public int getBisectionCount() {
        Integer n = _om.getInteger( getKey( BISECTION_COUNT ) );
        return n.intValue();
    }

    public int getBisectionInterval() {
        Integer n = _om.getInteger( getKey( BISECTION_INTERVAL ) );
        return n.intValue();
    }

    public float getRateLearningRate() {
        Float r = _om.getFloat( getKey( RATE_LEARNING_RATE ) );
        return r.floatValue();
    }

    public float getCorrelationLearningRate() {
        Float r = _om.getFloat( getKey( CORRELATION_LEARNING_RATE ) );
        return r.floatValue();
    }

    public int getBatchCount() {
        Integer n = _om.getInteger( getKey( BATCH_COUNT ) );
        return n.intValue();
    }

    public int getBatchSize() {
        Integer n = _om.getInteger( getKey( BATCH_SIZE ) );
        return n.intValue();
    }

}
