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
public class KSparseAutoencoderConfig  extends CompetitiveLearningConfig {

    public static final String LEARNING_RATE = "learning-rate";
    public static final String SPARSITY = "sparsity";
    public static final String SPARSITY_MAX = "sparsity-max";
    public static final String SPARSITY_MIN = "sparsity-min";
    public static final String SPARSITY_OUTPUT = "sparsity-output";
    public static final String BINARY_OUTPUT = "binary-output";
    public static final String AGE_MIN = "age-min";
    public static final String AGE_MAX = "age-max";
    public static final String AGE = "age";
    public static final String AGE_SCALE = "age-scale";

    public static final String RATE_SCALE = "rate-scale";
    public static final String RATE_MAX = "rate-max";
    public static final String RATE_LEARNING_RATE = "rate-learning-rate";

    public KSparseAutoencoderConfig() {
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
     * @param sparsityMin
     * @param sparsityMax
     * @param ageMin
     * @param ageMax
     * @param age
     */
    public void setup(
            ObjectMap om,
            String name,
            Random r,
            int inputs,
            int w,
            int h,
            float learningRate,
            boolean binaryOutput,
            float sparsityOutput,
            int sparsity,
            int sparsityMin,
            int sparsityMax,
            int ageMin,
            int ageMax,
            int age,
            float ageScale,
            float rateScale,
            float rateMax,
            float rateLearningRate ){

        super.setup( om, name, r, inputs, w, h );

        setLearningRate( learningRate );
        setBinaryOutput( binaryOutput );
        setSparsityOutput( sparsityOutput );
        setSparsity( sparsity );
        setSparsityMin( sparsityMin );
        setSparsityMax( sparsityMax );
        setAgeMin( ageMin );
        setAgeMax( ageMax );
        setAge( age );
        setAgeScale( ageScale );
        setRateScale( rateScale );
        setRateMax( rateMax );
        setRateLearningRate( rateLearningRate );
    }

    public void copyFrom( NetworkConfig nc, String name ) {
        super.copyFrom( nc, name );

        KSparseAutoencoderConfig c = ( KSparseAutoencoderConfig ) nc;

        setLearningRate( c.getLearningRate() );
        setBinaryOutput( c.getBinaryOutput() );
        setSparsityOutput( c.getSparsityOutput() );
        setSparsity( c.getSparsity() );
        setSparsityMin( c.getSparsityMin() );
        setSparsityMax( c.getSparsityMax() );
        setAgeMin( c.getAgeMin() );
        setAgeMax( c.getAgeMax() );
        setAge( c.getAge() );
        setAgeScale( c.getAgeScale() );
        setRateScale( c.getRateScale() );
        setRateMax( c.getRateMax() );
        setRateLearningRate( c.getRateLearningRate() );
    }

    public void setLearningRate( float r ) {
        _om.put( getKey( LEARNING_RATE ), r );
    }

    public void setBinaryOutput( boolean b ) {
        _om.put( getKey( BINARY_OUTPUT ), b );
    }

    public void setSparsityOutput( float r ) {
        _om.put( getKey( SPARSITY_OUTPUT ), r );
    }

    public void setSparsity( int n ) {
        _om.put( getKey( SPARSITY ), n );
    }

    public void setSparsityMin( int n ) {
        _om.put( getKey( SPARSITY_MIN ), n );
    }

    public void setSparsityMax( int n ) {
        _om.put( getKey( SPARSITY_MAX ), n );
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

    public float getLearningRate() {
        Float r = _om.getFloat( getKey( LEARNING_RATE ) );
        return r.floatValue();
    }

    public boolean getBinaryOutput() {
        Boolean b = _om.getBoolean( getKey( BINARY_OUTPUT ) );
        return b.booleanValue();
    }

    public float getSparsityOutput() {
        Float r = _om.getFloat( getKey( SPARSITY_OUTPUT ) );
        return r.floatValue();
    }

    public int getSparsity() {
        Integer n = _om.getInteger( getKey( SPARSITY ) );
        return n.intValue();
    }

    public int getSparsityMin() {
        Integer n = _om.getInteger( getKey( SPARSITY_MIN ) );
        return n.intValue();
    }

    public int getSparsityMax() {
        Integer n = _om.getInteger( getKey( SPARSITY_MAX ) );
        return n.intValue();
    }

    public int getAgeMin() {
        Integer n = _om.getInteger( getKey( AGE_MIN ) );
        return n.intValue();
    }

    public int getAgeMax() {
        Integer n = _om.getInteger( getKey( AGE_MAX ) );
        return n.intValue();
    }

    public int getAge() {
        Integer n = _om.getInteger( getKey( AGE ) );
        return n.intValue();
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

}
