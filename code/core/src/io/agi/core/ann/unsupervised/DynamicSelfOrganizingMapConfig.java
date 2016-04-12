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
 * Created by dave on 29/12/15.
 */
public class DynamicSelfOrganizingMapConfig extends CompetitiveLearningConfig {

    public static final String ELASTICITY = "elasticity";
    public static final String LEARNING_RATE = "learning-rate";

    public String _keyElasticity = ELASTICITY;
    public String _keyLearningRate = LEARNING_RATE;

    public DynamicSelfOrganizingMapConfig() {
    }

    public void setup( ObjectMap om, String name, Random r, int inputs, int w, int h, float learningRate, float elasticity ) {
        super.setup( om, name, r, inputs, w, h );

        setLearningRate( learningRate );
        setElasticity( elasticity );
    }

    public void copyFrom( NetworkConfig nc, String name ) {
        super.copyFrom( nc, name );

        DynamicSelfOrganizingMapConfig c = ( DynamicSelfOrganizingMapConfig ) nc;

        setLearningRate( c.getLearningRate() );
        setElasticity( c.getElasticity() );
    }

    public void setLearningRate( float r ) {
        _om.put( getKey( _keyLearningRate ), r );
    }

    public void setElasticity( float r ) {
        _om.put( getKey( _keyElasticity ), r );
    }

    public float getElasticity() {
        Float r = _om.getFloat( getKey( _keyElasticity ) );
        return r.floatValue();
    }

    public float getLearningRate() {
        Float r = _om.getFloat( getKey( _keyLearningRate ) );
        return r.floatValue();
    }
}
