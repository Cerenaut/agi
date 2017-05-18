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
 * Online algorithm with parameters to determine how input feature density affects cell density (elasticity).
 * See "Dynamic Self-Organising Map" by Nicolas Rougier and Yann Boniface (2010)
 * <p/>
 * EDIT by Dave: I could never make this work in a stable and robust manner. I have re-read the equations 3 times and I
 * can't see the error. I looked at Rougier's homepage and he has a different implementation in Python that doesn't
 * match the paper. So I am abandoning this algorithm, despite its appeal.
 *
 * Created by dave on 29/12/15.
 */
public class DynamicSelfOrganizingMap extends CompetitiveLearning {

    public DynamicSelfOrganizingMapConfig _c;
    public Data _inputValues;
    public Data _cellWeights;
    public Data _cellErrors;
    public Data _cellActivity;
    public Data _cellMask;

    public DynamicSelfOrganizingMap( String name, ObjectMap om ) {
        super( name, om );
    }

    public void setup( DynamicSelfOrganizingMapConfig c ) {
        _c = c;

        int inputs = c.getNbrInputs();
        int w = c.getWidthCells();
        int h = c.getHeightCells();

        _inputValues = new Data( inputs );
        _cellWeights = new Data( w, h, inputs );
        _cellErrors = new Data( w, h );
        _cellActivity = new Data( w, h );
        _cellMask = new Data( w, h );
    }

    public void reset() {
        _cellMask.set( 1.f );
        _cellWeights.setRandom( _c._r );
//        _cellWeights.set( 0.5f );
    }

    public Data getInput() {
        return _inputValues;
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

        trainWithInput( _c, _inputValues, _cellWeights, _cellErrors, bestCell );
    }

    public static void trainWithInput(
            DynamicSelfOrganizingMapConfig c,
            FloatArray inputValues,
            FloatArray cellWeights,  // Size = cells * inputs
            FloatArray cellSumSqError ) {
        int winningCell = cellSumSqError.minValueIndex();
        trainWithInput( c, inputValues, cellWeights, cellSumSqError, winningCell );
    }

    protected static float findMaxSumSqError( FloatArray cellWeights, int inputs, int cells ) {
        float maxSumSqError = 0.f;

        for( int c1 = 0; c1 < cells; ++c1 ) {
            for( int c2 = 0; c2 < cells; ++c2 ) {

                float sumSqError = 0.f;

                for( int i = 0; i < inputs; ++i ) { // for each input

                    int offset1 = c1 * inputs + i;
                    int offset2 = c2 * inputs + i;

                    float w1 = cellWeights._values[ offset1 ]; // error from ci to cell
                    float w2 = cellWeights._values[ offset2 ]; // error from ci to cell

                    float error = w1 - w2;
                    sumSqError += ( error * error );
                }

                if( sumSqError > maxSumSqError ) {
                    maxSumSqError = sumSqError;
                }
            }
        }

        return maxSumSqError;
    }

    public static void trainWithInput(
            DynamicSelfOrganizingMapConfig c,
            FloatArray inputValues,
            FloatArray cellWeights,  // Size = cells * inputs
            FloatArray cellSumSqError,
            int winningCell ) {

        int inputs = c.getNbrInputs();
        int cells = c.getNbrCells();
        int w = c.getWidthCells();
        int h = c.getHeightCells();
        float elasticity = c.getElasticity();
        float learningRate = c.getLearningRate();

        float inputMin = 0.f;
        float inputMax = 1.f;

        int yw = winningCell / w;
        int xw = winningCell % w;

        // compute the normalized norm
        float maxSumSqError = findMaxSumSqError( cellWeights, inputs, cells );
        float maxError = (float)Math.sqrt( maxSumSqError );
        float winningError = (float)Math.sqrt( cellSumSqError._values[ winningCell ] );
        float winningNormError = winningError / maxError;
        float winningNormErrorSq = winningNormError * winningNormError;

//        float maxDistanceSq = (float)Math.sqrt( w * w + h * h );

        // update weights to be closer to observations
        int cell = 0;
        for( int y = 0; y < h; ++y ) { // for each som cell
            for( int x = 0; x < w; ++x ) { // for each som cell

                float dx = ( x - xw ) / (float)w;
                float dy = ( y - yw ) / (float)h;

//float neighbourhoodScale = 1.0f;
//dx *= neighbourhoodScale;
//dy *= neighbourhoodScale;
                // http://en.wikipedia.org/wiki/Norm_%28mathematics%29
                // || x ||  = sqrt( sum Sq Error )
                float cellDistanceSq = ( dx * dx + dy * dy );// / maxDistanceSq;
                float cellDistance = (float)Math.sqrt( cellDistanceSq );
//if e^-x is about zero when less than about -5
//and elasticity = 1
//then the ratio d / eSq must be in the range 0 <= n <= 5
//If the eSq is 0 <= n <= 1
//then d / 0.5 = 5
//5 * 0.5 = d
//d = 0.5
                float sumSqError = cellSumSqError._values[ cell ];
                float error = ( float ) Math.sqrt( sumSqError );
                float normError = error / maxError;
                normError = Math.min( 1.f, normError );

                // TODO make nFn = 0 where winningNormErrorSq = 0
                float neighbourhoodFn = 0.f;
                if( winningNormErrorSq > 0.f ) {
                    double exponent = -( 1.0 / ( elasticity * elasticity ) ) * ( cellDistanceSq / winningNormErrorSq );
//                    double exponent = -( 1.0 / ( elasticity * elasticity ) ) * ( cellDistance / winningNormError );
                    neighbourhoodFn = ( float ) Math.exp( exponent );
                }

                for( int i = 0; i < inputs; ++i ) { // for each input

                    int inputOffset = cell * inputs + i;

                    float inputValue = inputValues._values[ i ]; // error from ci to cell
                    float weightOld = cellWeights._values[ inputOffset ]; // error from ci to cell
                    float sign = inputValue - weightOld;
                    float delta = learningRate * normError * neighbourhoodFn * sign;
                    float weightNew = weightOld + delta;

//                    if( normError  > 1.f ) {
//                        int g = 0;
//                        g++;
//                    }
//                    if( neighbourhoodFn  > 1.f ) {
//                        int g = 0;
//                        g++;
//                    }
//                    if( sign  > 1.f ) {
//                        int g = 0;
//                        g++;
//                    }
                    cellWeights._values[ inputOffset ] = weightNew; // error from ci to cell
                }

                ++cell;
            }
        }
    }

    public static void trainWithSparseInput(
            DynamicSelfOrganizingMapConfig c,
            FloatArray inputValues,
            FloatArray cellWeights,  // Size = cells * inputs
            FloatArray cellSumSqError,
            int winningCell ) {

        HashSet< Integer > activeInputValues = new HashSet< Integer >();
        int inputs = c.getNbrInputs();
        for( int i = 0; i < inputs; ++i ) { // for each input
            float inputValue = inputValues._values[ i ]; // error from ci to cell

            if( inputValue > 0.f ) {
                activeInputValues.add( i );
            }
        }

        trainWithSparseInput( c, activeInputValues, cellWeights, cellSumSqError, winningCell );
    }

    public static void trainWithSparseInput(
            DynamicSelfOrganizingMapConfig c,
            HashSet< Integer > activeInputValues,
            FloatArray cellWeights,  // Size = cells * inputs
            FloatArray cellSumSqError,
            int winningCell ) {
        int inputs = c.getNbrInputs();
        int cells = c.getNbrCells();
        int w = c.getWidthCells();
        int h = c.getHeightCells();
        float elasticity = c.getElasticity();
        float learningRate = c.getLearningRate();

        float inputMin = 0.f;
        float inputMax = 1.f;

        int yw = winningCell / w;
        int xw = winningCell % w;

        // compute the normalized norm
        float maxSumSqError = findMaxSumSqError( cellWeights, inputs, cells );
        float maxError = (float)Math.sqrt( maxSumSqError );
        float winningError = (float)Math.sqrt( cellSumSqError._values[ winningCell ] );
        float winningNormError = winningError / maxError;
        float winningNormErrorSq = winningNormError * winningNormError;

        // update weights to be closer to observations
        int cell = 0;
        for( int y = 0; y < h; ++y ) { // for each som cell
            for( int x = 0; x < w; ++x ) { // for each som cell

                float dx = x - xw;
                float dy = y - yw;

                // http://en.wikipedia.org/wiki/Norm_%28mathematics%29
                // || x ||  = sqrt( sum Sq Error )
                float cellDistanceSq = dx * dx + dy * dy;

                float sumSqError = cellSumSqError._values[ cell ];
                float error = ( float ) Math.sqrt( sumSqError );
                float normError = error / maxError;
                double exponent = - ( 1.0/( elasticity * elasticity ) ) * ( cellDistanceSq / winningNormErrorSq );
                float neighbourhoodFn = (float)Math.exp( exponent );

                int inputOffset = cell * inputs;

                for( int i = 0; i < inputs; ++i ) { // for each input

                    //int inputOffset = cell * c._i + i;

                    float inputValue = 0.f; //inputValues._values[ i ]; // error from ci to cell
                    if( activeInputValues.contains( i ) ) {
                        inputValue = 1.f;
                    }

                    float weightOld = cellWeights._values[ inputOffset ]; // error from ci to cell
                    float sign = inputValue - weightOld;
                    float weightNew = weightOld + learningRate * normError * neighbourhoodFn * sign;

                    cellWeights._values[ inputOffset ] = weightNew; // error from ci to cell

                    ++inputOffset;
                }

                ++cell;
            }
        }
    }

/*    public static float getUpdatedWeight(
            float value,
            float weightOld,
            float weightMin,
            float weightMax,
            float learningRateWeight,
            float cellDistanceWeight ) {

        float diff = value - weightOld; // gives sign and weight
        float delta = learningRateWeight * cellDistanceWeight * diff; // if error = 0 no change.

        float weightNew = weightOld;
        weightNew += delta;
//        weightNew += ( ( RandomInstance.random() - 0.5 ) * noiseMagnitude );
        weightNew = Math.min( weightMax, weightNew );
        weightNew = Math.max( weightMin, weightNew );
        //Unit..check( weightNew );
        return weightNew;
    }

    public static float getCellDistanceWeight( float elasticity, float cellDistanceSq, float valueDistanceSq ) {
        if( valueDistanceSq <= 0.f ) {
            return 0.f;
        }
        float b = cellDistanceSq / valueDistanceSq;
        float a = 1.f / ( elasticity * elasticity );
        float product = a * b;
        float result = ( float ) Math.exp( -product );
        //Maths.check( result );
        return result;
    }*/

}
