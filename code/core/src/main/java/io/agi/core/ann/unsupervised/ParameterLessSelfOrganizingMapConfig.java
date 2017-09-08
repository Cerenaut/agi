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
 * Created by dave on 1/06/16.
 */
public class ParameterLessSelfOrganizingMapConfig  extends CompetitiveLearningConfig {

    public static final String NEIGHBOURHOOD_RANGE = "neighbourhood-range";
//    public static final String INPUT_DIAMETER = "input-diameter";
//    public static final String MAX_ERROR = "max-error";

    public String _keyNeighbourhoodRange = NEIGHBOURHOOD_RANGE;
//    public String _keyInputDiameter = INPUT_DIAMETER;
//    public String _keyMaxError = MAX_ERROR;

    public ParameterLessSelfOrganizingMapConfig() {
    }

    public void setup( ObjectMap om, String name, Random r, int inputs, int w, int h, float neighbourhoodRange ) {//, float inputDiameter ) {
        super.setup( om, name, r, inputs, w, h );

        setNeighbourhoodRange( neighbourhoodRange );
//        setInputDiameter( inputDiameter );
//        setMaxError( 0.f );
    }

    public static int getBoundsSize( int inputs ) {
        // structure of bounds: [mask bit][inputvalues] * k
        int inputDimensions = inputs;
        int k = inputDimensions +1;
        int boundsSize = k * ( inputs +1 ); // +1 for the mask bits
        return boundsSize;
    }

    public void copyFrom( NetworkConfig nc, String name ) {
        super.copyFrom( nc, name );

        ParameterLessSelfOrganizingMapConfig c = ( ParameterLessSelfOrganizingMapConfig ) nc;

        setNeighbourhoodRange( c.getNeighbourhoodRange() );
//        setInputDiameter( c.getInputDiameter() );
    }

    public void setNeighbourhoodRange( float r ) {
        _om.put( getKey( _keyNeighbourhoodRange ), r );
    }

    public float getNeighbourhoodRange() {
        Float r = _om.getFloat( getKey( _keyNeighbourhoodRange ) );
        return r.floatValue();
    }

//    public void setInputDiameter( float r ) {
//        _om.put( getKey( _keyInputDiameter ), r );
//    }
//
//    public float getInputDiameter() {
//        Float r = _om.getFloat( getKey( _keyInputDiameter ) );
//        return r.floatValue();
//    }

//    public void setMaxError( float r ) {
//        _om.put( getKey( _keyMaxError ), r );
//    }
//
//    public float getMaxError() {
//        Float r = _om.getFloat( getKey( _keyMaxError ) );
//        return r.floatValue();
//    }
}