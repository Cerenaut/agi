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

package io.agi.core.ann.reinforcement;

import io.agi.core.ann.NetworkConfig;
import io.agi.core.orm.ObjectMap;

import java.util.Random;

/**
 * Created by dave on 28/03/17.
 */
public class QLearningConfig extends NetworkConfig {

    public static final String LEARNING_RATE = "learning-rate";
    public static final String DISCOUNT_RATE = "discount-rate";
    public static final String STATES = "states";
    public static final String ACTIONS = "actions";

    public QLearningConfig() {
    }

    public void setup( ObjectMap om, String name, Random r, int states, int actions, float learningRate, float discountRate ) {
        super.setup( om, name, r );

        setDiscountRate( discountRate );
        setLearningRate( learningRate );
        setNbrStates( states );
        setNbrActions( actions );
    }

    public void copyFrom( NetworkConfig nc, String name ) {
        super.copyFrom( nc, name );

        QLearningConfig c = ( QLearningConfig ) nc;

        setDiscountRate( c.getDiscountRate() );
        setLearningRate( c.getLearningRate() );
        setNbrStates( c.getNbrStates() );
        setNbrActions( c.getNbrActions() );
    }

    public void setNbrStates( int s ) {
        _om.put( getKey( STATES ), s );
    }

    public Integer getNbrStates() {
        return ( Integer ) _om.get( getKey( STATES ) );
    }

    public void setNbrActions( int s ) {
        _om.put( getKey( ACTIONS ), s );
    }

    public Integer getNbrActions() {
        return ( Integer ) _om.get( getKey( ACTIONS ) );
    }

    public float getLearningRate() {
        Float r = _om.getFloat( getKey( LEARNING_RATE ) );
        return r.floatValue();
    }

    public void setLearningRate( float r ) {
        _om.put( getKey( LEARNING_RATE ), r );
    }

    public float getDiscountRate() {
        Float r = _om.getFloat( getKey( DISCOUNT_RATE ) );
        return r.floatValue();
    }

    public void setDiscountRate( float r ) {
        _om.put( getKey( DISCOUNT_RATE ), r );
    }

}
