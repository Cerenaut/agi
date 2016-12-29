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

import java.util.Random;

/**
 * Created by dave on 1/07/16.
 */
public class OnlineKSparseAutoencoderConfig extends CompetitiveLearningConfig {

    public static final String LEARNING_RATE = "learning-rate";
    public static final String MOMENTUM = "momentum";
    public static final String WEIGHTS_STD_DEV = "weights-std-dev";
    public static final String SPARSITY = "sparsity";
    public static final String SPARSITY_OUTPUT = "sparsity-output";
    public static final String AGE_MIN = "age-min";
    public static final String AGE_MAX = "age-max";
    public static final String AGE_TRUNCATION_FACTOR = "age-truncation-factor";
    public static final String AGE_SCALE = "age-scale";
    public static final String RATE_SCALE = "rate-scale";
    public static final String RATE_MAX = "rate-max";
    public static final String RATE_LEARNING_RATE = "rate-learning-rate";
    public static final String AGE = "age";
    public static final String UNIT_OUTPUT = "unit-output";
    public static final String BATCH_COUNT = "batch-age";
    public static final String BATCH_SIZE = "batch-size";

    public OnlineKSparseAutoencoderConfig() {
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
     * @param ageMin
     * @param ageMax
     * @param age
     * @param ageTruncationFactor
     * @param ageScale
     * @param rateScale
     * @param rateMax
     * @param rateLearningRate
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
            int ageMin,
            int ageMax,
            int age,
            float ageTruncationFactor,
            float ageScale,
            float rateScale,
            float rateMax,
            float rateLearningRate,
            float weightsStdDev,
            boolean unitOutput,
            int batchCount,
            int batchSize ){

        super.setup(om, name, r, inputs, w, h);

        setLearningRate(learningRate);
        setSparsityOutput(sparsityOutput);
        setSparsity(sparsity);
        setAgeMin(ageMin);
        setAgeMax(ageMax);
        setAge(age);
        setMomentum(momentum);
        setWeightsStdDev(weightsStdDev);

        // new properties
        setAgeTruncationFactor(ageTruncationFactor);
        setAgeScale(ageScale);
        setRateScale(rateScale);
        setRateMax(rateMax);
        setRateLearningRate(rateLearningRate);
        setUnitOutput(unitOutput);

        setBatchCount(batchCount);
        setBatchSize(batchSize);
    }

    public void copyFrom( NetworkConfig nc, String name ) {
        super.copyFrom(nc, name);

        OnlineKSparseAutoencoderConfig c = (OnlineKSparseAutoencoderConfig) nc;

        setLearningRate(c.getLearningRate());
        setSparsityOutput(c.getSparsityOutput());
        setSparsity(c.getSparsity());
        setAgeMin(c.getAgeMin());
        setAgeMax(c.getAgeMax());
        setAge(c.getAge());
        setMomentum(c.getMomentum());
        setWeightsStdDev(c.getWeightsStdDev());

        // new properties
        setAgeTruncationFactor(c.getAgeTruncationFactor() );
        setAgeScale( c.getAgeScale() );
        setRateScale( c.getRateScale() );
        setRateMax( c.getRateMax() );
        setRateLearningRate( c.getRateLearningRate() );
        setUnitOutput( c.getUnitOutput() );

        setBatchCount( c.getBatchCount() );
        setBatchSize( c.getBatchSize() );
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

    public void setAgeMin( int n ) {
        _om.put( getKey( AGE_MIN ), n );
    }

    public void setAgeMax( int n ) {
        _om.put( getKey( AGE_MAX ), n );
    }

    public void setAge( int n ) {
        _om.put( getKey( AGE ), n );
    }

    public void setMomentum( float r ) {
        _om.put( getKey( MOMENTUM ), r );
    }

    public void setWeightsStdDev( float r ) {
        _om.put( getKey( WEIGHTS_STD_DEV ), r );
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
        Integer n = _om.getInteger(getKey(SPARSITY));
        return n.intValue();
    }

    public int getAgeMin() {
        Integer n = _om.getInteger(getKey(AGE_MIN));
        return n.intValue();
    }

    public int getAgeMax() {
        Integer n = _om.getInteger(getKey(AGE_MAX));
        return n.intValue();
    }

    public int getAge() {
        Integer n = _om.getInteger(getKey(AGE));
        return n.intValue();
    }

    public void setAgeTruncationFactor( float r ) {
        _om.put( getKey( AGE_TRUNCATION_FACTOR ), r );
    }

    public void setAgeScale( float r ) {
        _om.put( getKey( AGE_SCALE ), r );
    }

    public void setRateScale( float r ) {
        _om.put( getKey( RATE_SCALE ), r );
    }

    public void setRateMax( float r ) {
        _om.put( getKey( RATE_MAX ), r );
    }

    public void setRateLearningRate( float r ) {
        _om.put( getKey( RATE_LEARNING_RATE ), r );
    }

    public void setUnitOutput( boolean b ) {
        _om.put( getKey( UNIT_OUTPUT ), b );
    }

    public void setBatchCount( int n ) {
        _om.put( getKey( BATCH_COUNT ), n );
    }

    public void setBatchSize( int n ) {
        _om.put( getKey( BATCH_SIZE ), n );
    }

    public float getAgeTruncationFactor() {
        Float r = _om.getFloat( getKey( AGE_TRUNCATION_FACTOR ) );
        return r.floatValue();
    }

    public float getAgeScale() {
        Float r = _om.getFloat( getKey( AGE_SCALE ) );
        return r.floatValue();
    }

    public float getRateScale() {
        Float r = _om.getFloat( getKey( RATE_SCALE ) );
        return r.floatValue();
    }

    public float getRateMax() {
        Float r = _om.getFloat( getKey( RATE_MAX ) );
        return r.floatValue();
    }

    public float getRateLearningRate() {
        Float r = _om.getFloat( getKey( RATE_LEARNING_RATE ) );
        return r.floatValue();
    }

    public boolean getUnitOutput() {
        Boolean r = _om.getBoolean(getKey(UNIT_OUTPUT));
        return r.booleanValue();
    }

    public int getBatchCount() {
        Integer n = _om.getInteger(getKey(BATCH_COUNT));
        return n.intValue();
    }

    public int getBatchSize() {
        Integer n = _om.getInteger(getKey(BATCH_SIZE));
        return n.intValue();
    }
}
