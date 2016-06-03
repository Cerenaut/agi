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

import io.agi.core.data.Data;
import io.agi.core.data.FloatArray;
import io.agi.core.orm.ObjectMap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeMap;

/**
 * Created by dave on 1/06/16.
 */
public class ParameterLessSelfOrganizingMap extends CompetitiveLearning {

    public ParameterLessSelfOrganizingMapConfig _c;
    public Data _inputValues;
    public Data _inputBounds;
    public Data _cellWeights;
    public Data _cellErrors;
    public Data _cellActivity;
    public Data _cellMask;

    public ParameterLessSelfOrganizingMap( String name, ObjectMap om ) {
        super( name, om );
    }

    public void setup( ParameterLessSelfOrganizingMapConfig c ) {
        _c = c;

        int inputs = c.getNbrInputs();
        int w = c.getWidthCells();
        int h = c.getHeightCells();

        // structure of bounds: [mask bit][inputvalues] * k
//        int inputDimensions = inputs;
//        int k = inputDimensions +1;
//        int boundsSize = k * ( inputs +1 ); // +1 for the mask bits
        int boundsSize = _c.getBoundsSize( inputs );

        _inputValues = new Data( inputs );
        _inputBounds = new Data( boundsSize ); // let k = 1 + n where n is the number of input dimensions
        _cellWeights = new Data( w, h, inputs );
        _cellErrors = new Data( w, h );
        _cellActivity = new Data( w, h );
        _cellMask = new Data( w, h );
    }

    public void reset() {
        _cellMask.set( 1.f );
        _inputBounds.set( 0.f ); // masks set to zero, vectors zeroed
//        _c.setInputDiameter( -1.f ); // these are tracked over time
//        _c.setMaxError( 0.f ); // these are tracked over time
        _cellWeights.setRandom( _c._r );
    }

    public Data getInput() {
        return _inputValues;
    }

    public void call() {
        update();
    }

    public void update() {
        CompetitiveLearning.sumSqError( _c, _inputValues, _cellWeights, _cellErrors );

        // get the top N cells
        int maxRank = 1;
        TreeMap< Float, ArrayList< Integer > > ranking = new TreeMap< Float, ArrayList< Integer > >();
        CompetitiveLearning.findBestNCells( _c, _cellMask, _cellErrors, _cellActivity, maxRank, false, ranking );

        if( ranking.isEmpty() ) {
            return;
        }

        Float bestError = ranking.keySet().iterator().next();
        int bestCell = ranking.get( bestError ).get( 0 );

        float diameter = updateDiameter( _c, _inputValues, _inputBounds );
        trainWithInput( _c, _inputValues, _cellWeights, _cellErrors, bestCell, diameter );
    }

    public static float updateDiameter(
            ParameterLessSelfOrganizingMapConfig c,
            FloatArray inputValues,
            FloatArray inputBounds ) {
        float S = findDiameter( inputValues, inputBounds, false );//c.getInputDiameter();
        float s = findDiameter( inputValues, inputBounds, true );
        if( s >= S ) {
            replaceClosest( inputValues, inputBounds );
//            c.setInputDiameter( s );
            return s;
        }

        return S;
    }

    public static float findDiameter(
            FloatArray inputValues,
            FloatArray inputBounds,
            boolean includeInputValues ) {
        int inputDimensions = inputValues.getSize();
        int k = inputDimensions +1;
        int stride = inputDimensions +1;

        float maxSumSqError = 0.f;

        for( int j1 = 0; j1 < k; ++j1 ) {

            int offset1 = stride * j1;
            float maskBit1 = inputBounds._values[ offset1 ];
            if( maskBit1 < 1.f ) {
                continue;
            }

            float sumSqError = 0.f;

            if( includeInputValues ) {
                for( int i = 0; i < inputDimensions; ++i ) {
                    float boundValue1 = inputBounds._values[ offset1 + 1 + i ];
                    float boundValue2 = inputValues._values[ i ];

                    float sqError = boundValue2 - boundValue1;
                    sqError *= sqError;
                    sumSqError += sqError;
                }

                if( sumSqError > maxSumSqError ) {
                    maxSumSqError = sumSqError;
                }
            }

            // compare to all other elements in set:
            for( int j2 = 0; j2 < k; ++j2 ) {

                if( j2 == j1 ) {
                    continue;
                }

                int offset2 = stride * j2;
                float maskBit2 = inputBounds._values[ offset2 ];
                if( maskBit2 < 1.f ) {
                    continue;
                }

                sumSqError = 0.f;

                for( int i = 0; i < inputDimensions; ++i ) {
                    float boundValue1 = inputBounds._values[ offset1 +1 +i ];
                    float boundValue2 = inputBounds._values[ offset2 +1 +i ];

                    float sqError = boundValue2 - boundValue1;
                    sqError *= sqError;
                    sumSqError += sqError;
                }

                if( sumSqError > maxSumSqError ) {
                    maxSumSqError = sumSqError;
                }
            }
        }

        return maxSumSqError;
    }

    public static void replaceClosest(
            FloatArray inputValues,
            FloatArray inputBounds ) {

        // structure of bounds: [mask bit][inputvalues] * k
        int inputDimensions = inputValues.getSize();
        int k = inputDimensions +1;
        int stride = inputDimensions +1;

        int jClosest = -1;
        float minSumSqError = Float.MAX_VALUE;

        for( int j = 0; j < k; ++j ) {

            int offset = stride * j;

            float maskBit = inputBounds._values[ offset ];
            if( maskBit < 1.f ) {
                jClosest = j;
                break; // replace this value
            }
        }

        // if no empty slots were found:
        if( jClosest < 0 ) {
            for( int j = 0; j < k; ++j ) {

                int offset = stride * j;

                float maskBit = inputBounds._values[ offset ];
                if( maskBit < 1.f ) {
                    jClosest = j;
                    break; // replace this value
                }

                float sumSqError = 0.f;

                for( int i = 0; i < inputDimensions; ++i ) {

                    float inputValue = inputValues._values[ i ];
                    float boundValue = inputBounds._values[ offset + 1 + i ];
                    float sqError = inputValue - boundValue;
                    sqError *= sqError;
                    sumSqError += sqError;
                }

                if( sumSqError < minSumSqError ) {
                    minSumSqError = sumSqError;
                    jClosest = j;
                }
            }
        }

        int offset = stride * jClosest;
        inputBounds._values[ offset ] = 1.f; // set mask bit

        for( int i = 0; i < inputDimensions; ++i ) {
            float inputValue = inputValues._values[ i ];
            inputBounds._values[ offset +1 +i ] = inputValue; // copy input vector
        }
    }

//    public static void trainWithInput(
//            ParameterLessSelfOrganizingMapConfig c,
//            FloatArray inputValues,
//            FloatArray cellWeights,  // Size = cells * inputs
//            FloatArray cellSumSqError ) {
//        int winningCell = cellSumSqError.minValueIndex();
//        trainWithInput( c, inputValues, cellWeights, cellSumSqError, winningCell );
//    }

    public static void trainWithInput(
            ParameterLessSelfOrganizingMapConfig c,
            FloatArray inputValues,
            FloatArray cellWeights,  // Size = cells * inputs
            FloatArray cellSumSqError,
            int winningCell,
            float diameter ) {

        if( diameter <= 0.f) {
            return; // can't train (yet)
        }

        int inputs = c.getNbrInputs();
        int cells = c.getNbrCells();
        int w = c.getWidthCells();
        int h = c.getHeightCells();

        int yw = winningCell / w;
        int xw = winningCell % w;

        float winningError = ( float ) Math.sqrt( cellSumSqError._values[ winningCell ] ); // err(t) or eqn 1 in plsom2 paper
//        float r1 = c.getMaxError();
//        float r2 = Math.max( r1, winningError );
//        c.setMaxError( r2 ); // max error ever observed
//
//        float normalizedError = winningError / r2; // normalized error, aka d(t) or eqn 5 in plsom2 paper
//        float diameter = c.getInputDiameter();

        if( winningError <= 0.f ) {
            return; // no training
        }

        float normalizedError = winningError / diameter; // normalized error, aka d(t) or eqn 7 in plsom2 paper
        normalizedError = Math.min( 1.f, normalizedError );

        float neighbourhoodRange = c.getNeighbourhoodRange();

        // update weights to be closer to observations
        int cell = 0;
        for( int y = 0; y < h; ++y ) { // for each som cell
            for( int x = 0; x < w; ++x ) { // for each som cell

                float dx = ( x - xw );// / ( float ) w;
                float dy = ( y - yw );// / ( float ) h;
                float cellDistanceSq = ( dx * dx + dy * dy );// / maxDistanceSq;

                double log = 1.f + normalizedError * ( Math.E -1 );
                float denominator = (float)( neighbourhoodRange * Math.log( log ) );

                double exponent = ( -cellDistanceSq ) / denominator;
                float neighbourhoodFn = ( float ) Math.exp( exponent );

                for( int i = 0; i < inputs; ++i ) { // for each input

                    int inputOffset = cell * inputs + i;

                    float inputValue = inputValues._values[ i ]; // error from ci to cell
                    float weightOld = cellWeights._values[ inputOffset ]; // error from ci to cell
                    float sign = inputValue - weightOld;
                    float delta = normalizedError * neighbourhoodFn * sign;
                    float weightNew = weightOld + delta;

if( Float.isNaN( weightNew ) ) {
    int g = 0;
    g++;
}
                    cellWeights._values[ inputOffset ] = weightNew; // error from ci to cell
                }

                ++cell;
            }
        }
    }

}