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
public class LifetimeSparseAutoencoderConfig extends CompetitiveLearningConfig {

    public static final String LEARNING_RATE = "learning-rate";
    public static final String MOMENTUM = "momentum";
    public static final String WEIGHTS_STD_DEV = "weights-std-dev";
    public static final String SPARSITY = "sparsity";
    public static final String SPARSITY_LIFETIME = "sparsity-lifetime";
    public static final String BATCH_COUNT = "batch-age";
    public static final String BATCH_SIZE = "batch-size";

    public LifetimeSparseAutoencoderConfig() {
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
            int sparsity,
            int sparsityLifetime,
            float weightsStdDev,
            int batchCount,
            int batchSize ){

        super.setup( om, name, r, inputs, w, h );

        setLearningRate( learningRate );
        setSparsity( sparsity );
        setSparsityLifetime( sparsityLifetime );
        setMomentum(momentum);
        setWeightsStdDev(weightsStdDev);
        setBatchCount(batchCount);
        setBatchSize(batchSize);
    }

    public void copyFrom( NetworkConfig nc, String name ) {
        super.copyFrom( nc, name );

        LifetimeSparseAutoencoderConfig c = ( LifetimeSparseAutoencoderConfig ) nc;

        setLearningRate( c.getLearningRate() );
        setSparsity(c.getSparsity());
        setSparsityLifetime(c.getSparsityLifetime());
        setMomentum(c.getMomentum());
        setWeightsStdDev(c.getWeightsStdDev());
        setBatchCount( c.getBatchCount() );
        setBatchSize( c.getBatchSize() );
    }

    public void setLearningRate( float r ) {
        _om.put( getKey( LEARNING_RATE ), r );
    }

    public void setSparsity( int n ) {
        _om.put( getKey( SPARSITY ), n );
    }

    public void setSparsityLifetime( int n ) {
        _om.put( getKey( SPARSITY_LIFETIME ), n );
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

    public int getSparsity() {
        Integer n = _om.getInteger( getKey( SPARSITY ) );
        return n.intValue();
    }

    public int getSparsityLifetime() {
        Integer n = _om.getInteger( getKey( SPARSITY_LIFETIME ) );
        return n.intValue();
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
