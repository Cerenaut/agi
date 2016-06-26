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
 * A variant of Neural Gas which allows continuous learning.
 *
 * Created by dave on 1/01/16.
 */
public class PlasticNeuralGasConfig extends CompetitiveLearningConfig {

    public static final String STRESS_LEARNING_RATE = "stress-learning-rate";
    public static final String RANK_LEARNING_RATE = "rank-learning-rate";
    public static final String RANK_SCALE = "rank-scale";
    public static final String AGE_MAX = "age-max";
    public static final String AGE_DECAY = "age-decay";
    public static final String AGE_SCALE = "age-scale";

    public PlasticNeuralGasConfig() {
    }

    public void setup(
            ObjectMap om,
            String name,
            Random r,
            int inputs,
            int w,
            int h,
            float stressLearningRate,
            float rankLearningRate,
            float rankScale,
            int ageMax,
            float ageDecay,
            float ageScale ){

        super.setup( om, name, r, inputs, w, h );

        setStressLearningRate( stressLearningRate );
        setRankLearningRate( rankLearningRate );
        setRankScale( rankScale );
        setAgeMax( ageMax );
        setAgeDecay( ageDecay );
        setAgeScale( ageScale );
    }

    public void copyFrom( NetworkConfig nc, String name ) {
        super.copyFrom( nc, name );

        PlasticNeuralGasConfig c = ( PlasticNeuralGasConfig ) nc;

        setStressLearningRate( c.getStressLearningRate() );
        setRankLearningRate( c.getRankLearningRate() );
        setRankScale( c.getRankScale() );
        setAgeMax( c.getAgeMax() );
        setAgeDecay( c.getAgeDecay() );
        setAgeScale( c.getAgeScale() );
    }

    public void setStressLearningRate( float r ) {
        _om.put( getKey( STRESS_LEARNING_RATE ), r );
    }

    public void setRankLearningRate( float r ) {
        _om.put( getKey( RANK_LEARNING_RATE ), r );
    }

    public void setRankScale( float r ) {
        _om.put( getKey( RANK_SCALE ), r );
    }

    public void setAgeMax( int n ) {
        _om.put( getKey( AGE_MAX ), n );
    }

    public void setAgeDecay( float r ) {
        _om.put( getKey( AGE_DECAY ), r );
    }

    public void setAgeScale( float r ) {
        _om.put( getKey( AGE_SCALE ), r );
    }

    public float getStressLearningRate() {
        Float r = _om.getFloat( getKey( STRESS_LEARNING_RATE ) );
        return r.floatValue();
    }

    public float getRankLearningRate() {
        Float r = _om.getFloat( getKey( RANK_LEARNING_RATE ) );
        return r.floatValue();
    }

    public float getRankScale() {
        Float r = _om.getFloat( getKey( RANK_SCALE ) );
        return r.floatValue();
    }

    public int getAgeMax() {
        Integer n = _om.getInteger( getKey( AGE_MAX ) );
        return n.intValue();
    }

    public float getAgeDecay() {
        Float r = _om.getFloat( getKey( AGE_DECAY ) );
        return r.floatValue();
    }

    public float getAgeScale() {
        Float r = _om.getFloat( getKey( AGE_SCALE ) );
        return r.floatValue();
    }
}
