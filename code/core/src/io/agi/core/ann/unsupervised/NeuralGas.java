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
import io.agi.core.data.Ranking;
import io.agi.core.orm.AbstractPair;
import io.agi.core.orm.ObjectMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.TreeMap;

/**
 * Growing Neural Gas algorithm (online unsupervised learning).
 * <p/>
 * Input can be provided as a dense Data structure, or as a sparse Array of values. In the latter case it is assumed
 * the values are all '1' and therefore the values of the sparse input are used as indices.
 * <p/>
 * http://sund.de/netze/applets/gng/full/tex/DemoGNG/node17.html#SECTION00074300000000000000
 *
 *   weight(i) = weight(i) + d
 *   d = eps(t) * h() * (best - weight(i))
 * <p/>
 * Created by dave on 29/12/15.
 */
public class NeuralGas extends CompetitiveLearning {

    public NeuralGasConfig _c;
    public ArrayList< Integer > _sparseUnitInput;
    public Data _inputValues;
    public Data _cellWeights;
    public Data _cellErrors;
    public Data _cellActivity;
    public Data _cellMask;
    public Data _cellAges;
    public Data _cellStress;

    protected int _bestCell = 0;

    public NeuralGas( String name, ObjectMap om ) {
        super( name, om );
    }

    public void setup( NeuralGasConfig c ) {
        _c = c;

        int inputs = c.getNbrInputs();
        int w = c.getWidthCells();
        int h = c.getHeightCells();

        _inputValues = new Data( inputs );
        _cellWeights = new Data( w, h, inputs );
        _cellErrors = new Data( w, h );
        _cellActivity = new Data( w, h );
        _cellMask = new Data( w, h );
        _cellAges = new Data( w, h );
        _cellStress = new Data( w, h );
    }

    public void reset() {
        _cellWeights.setRandom( _c._r );
        _cellMask.set( 1.f ); // enable all cells
        _cellAges.set( 1.f ); // enable all cells
        _cellStress.set( 0.f ); // enable all cells
    }

    public void call() {
        update();
    }

    public void update() {

        boolean learn = _c.getLearn();

        // Compute error
        ArrayList< Integer > liveCells = createCellList( _c, _cellMask );
        ArrayList< Integer > sparseUnitInput = getSparseUnitInput();
        if( sparseUnitInput != null ) {
            // Sparse input
            sumSqErrorSparseUnit( _c, liveCells, sparseUnitInput, _cellWeights, _cellErrors );
        } else {
            // Dense input
            sumSqError( _c, liveCells, _inputValues, _cellWeights, _cellErrors );
        }

        // Rank all cells
        int maxRank = _c.getNbrCells() +1; // keep em all
        boolean findMaxima = false; // find minima

        TreeMap< Float, ArrayList< Integer > > ranking1 = new TreeMap< Float, ArrayList< Integer > >();
        findBestNCells( _c, _cellMask, _cellErrors, _cellActivity, maxRank, findMaxima, ranking1 ); // activity is whether ranked
        ArrayList< Integer > bestValues1 = Ranking.getBestValues( ranking1, findMaxima, maxRank );

        int bestCell1 = bestValues1.get( 0 );

        float relativeStress = getRelativeStress( bestCell1, liveCells );

        // Re-rank all cells with promotion of old cells
        int maxAge = (int)_c.getMaxAge();

        promoteOldCells( liveCells, maxAge, relativeStress );

        TreeMap< Float, ArrayList< Integer > > ranking2 = new TreeMap< Float, ArrayList< Integer > >();
        findBestNCells( _c, _cellMask, _cellErrors, _cellActivity, maxRank, findMaxima, ranking2 ); // activity is whether ranked
        ArrayList< Integer > bestValues2 = Ranking.getBestValues( ranking2, findMaxima, maxRank );

        _bestCell = bestValues2.get( 0 );

        _cellActivity.set( 0.f );
        _cellActivity._values[ _bestCell ] = 1.f;

        if( !learn ) {
            return;
        }

        float bestError = _cellErrors._values[ _bestCell ];
        float rootBestError = (float)Math.sqrt( bestError );
        float learningRate = _c.getLearningRate();
        float neighbourhoodRange = _c.getNeighbourhoodRange();
        float minDistance = _c.getMinDistance();

        float ageDecay = 0.7f;
        float stressLearningRate = 0.01f;
        updateStress( _bestCell, rootBestError, stressLearningRate );

        int inputs = _c.getNbrInputs();
        int w = _c.getWidthCells();
        int h = _c.getHeightCells();
        int cells = w * h;
        int ranks = bestValues2.size();

        for( int rank = 0; rank < ranks; ++rank ) {
            int cell = bestValues2.get( rank );

            updateAge( cell, cells, rank, maxAge, ageDecay );

            float age = _cellAges._values[ cell ]; // after update

            if( sparseUnitInput != null ) {
                // Sparse input
                for( int i = 0; i < inputs; ++i ) {

                    float inputValue = 0.f;
                    if( sparseUnitInput.contains( i ) ) {
                        inputValue = 1.f;
                    }

                    updateWeight( cell, inputs, i, rank, age, maxAge, inputValue, rootBestError, learningRate, neighbourhoodRange, minDistance );
                }
            }
            else {
                // Dense input
                for( int i = 0; i < inputs; ++i ) {

                    float inputValue = _inputValues._values[ i ];

                    updateWeight( cell, inputs, i, rank, age, maxAge, inputValue, rootBestError, learningRate, neighbourhoodRange, minDistance );
                }
            }
        }
    }

    protected void promoteOldCells( Collection< Integer > cells, int maxAge, float relativeStress ) {
        // old cells
        // as cells age, their error becomes smaller (they become more sensitive)
        // when very old, they become hypersensitive
        // They will eventually out-compete other cells and become winners in their own right
        // http://www.wolframalpha.com/input/?i=plot+1-e%5E(-17(1-x))+for+x+%3D+0.6+to+1
//        float uniformStress = 1.f / (float)cells.size();
//        float excessStress = relativeStress - uniformStress;
//        excessStress = Math.max( 0.f, excessStress ); // so if
//        float stressFactor = relativeStress / uniformStress; // if less than uniform, is linear.
//        stressFactor = Math.min( 1.f, )
        float stressFactor = relativeStress;

        for( Integer cell : cells ) {
            float age = _cellAges._values[ cell ];
//            float ageScale = 17.0f; // how nonlinear it is. Should be a param
            float ageScale = 12.0f; // how nonlinear it is. Should be a param
            float unitAge = age / (float)maxAge; // 1 iff max age
//            float factor = (float)( 1.0 - Math.exp( -ageScale * ( 1.0f - unitAge ) ) ); // so == 1 if young, close to zero if old
            float factor = (float)( Math.exp( -ageScale * ( 1.0f - unitAge ) ) ); // 1 if old, zero if young

            if( (int)age == 200 ) {
                if( stressFactor > 0.9f ) {
                    int g = 0;
                    g++;
                }
            }
            factor *= stressFactor; // 1 if old and current cell is stressed. So if stress is low, cells will not be promoted.
            factor = 1.f - factor;
            float oldError = _cellErrors._values[ cell ]; // since inputs are unit, max error = 1
            float newError = oldError * factor;


            _cellErrors._values[ cell ] = newError;
        }
    }

//    protected void promoteOldCells( Collection< Integer > cells, int maxAge ) {
//        // old cells
//        // as cells age, their error becomes smaller (they become more sensitive)
//        // when very old, they become hypersensitive
//        // They will eventually out-compete other cells and become winners in their own right
//        // http://www.wolframalpha.com/input/?i=plot+1-e%5E(-17(1-x))+for+x+%3D+0.6+to+1
//        for( Integer cell : cells ) {
//            float age = _cellAges._values[ cell ];
//            float ageScale = 17.0f; // how nonlinear it is. Should be a param
//            float unitAge = age / (float)maxAge; // 1 iff max age
//            float factor = (float)( 1.0 - Math.exp( -ageScale * ( 1.0f - unitAge ) ) );
//            float oldError = _cellErrors._values[ cell ]; // since inputs are unit, max error = 1
//            float newError = oldError * factor;
//            _cellErrors._values[ cell ] = newError;
//        }
//    }

// TODO don't penalize stable cells - train them all with min( original rank, modified rank ) to prevent churn

    protected float getRelativeStress( int bestCell, Collection< Integer > cells ) {
        float sumStress = 0.f;
        float bestStress = 0.f;
        float maxStress = 0.f;

        for( Integer cell : cells ) {
            float stress = _cellStress._values[ cell ];
            if( cell.equals( bestCell ) ) {
                bestStress = stress;
            }
            sumStress += stress;
            maxStress = Math.max( stress, maxStress );
        }

        if( sumStress <= 0.f ) {
            return 0.f;
        }

//        float relativeStress = bestStress / sumStress;
        float relativeStress = bestStress / maxStress;
        return relativeStress;
    }

    protected void updateStress( int cell, float error, float stressLearningRate ) {
        float b = stressLearningRate;
        float a = 1.f - b;
        float oldStress = _cellStress._values[ cell ];
        float newStress = oldStress * a + error * b;
        _cellStress._values[ cell ] = newStress;
    }

    protected void updateAge( int cell, int cells, int rank, int maxAge, float ageDecay ) {
        float oldAge = _cellAges._values[ cell ];
        float newAge = Math.min( oldAge +1, maxAge );
        if( cell == _bestCell ) {
            newAge = oldAge * ageDecay;
        }

        _cellAges._values[ cell ] = newAge;
    }

//    http://www.wolframalpha.com/input/?i=plot+e%5E(-x%2F1.5)+for+x+%3D+0+to+5-
//    http://www.wolframalpha.com/input/?i=plot+e%5E(-x%2Fy)+for+x+%3D+0+to+5+and+y+%3D+1.5
//    x is rank and y is neighbourhood range
//    e^( -0/y )
//    e^( -1/y )
//    e^( -2/y )
//
//    So we need a function that returns something like a 1 if there is a lot of error, and close to (but not) zero when there is no error
//    neighbouhoorrange should be high when best is close, and low otherwise.
//    Given my inputs are unit, then basically we always have a unit error
//    We can take the sqrt of the sum sq error (which will make it bigger)
//        e.g. sqrt( 0.1 ) = 0.31
//        sqrt( 0.5 ) = 0.7
//    Then multiply by a factor that decides the real neighbourhood range (I can use existing param for this)

//    protected void updateAge( int cell, int cells, int rank, int maxAge ) {
//        float oldAge = _cellAges._values[ cell ];
//        float newAge = Math.min( oldAge +1, maxAge );
//        if( cell == _bestCell ) {
//            newAge = 0;
//        }
//
//        // what about a near-miss?
//        // The problem is that cells have to be the EXACT best to stop migrating.
//        // Whereas we actually want them to slow down when they are individually near some good value, and spread out
//        // not jump around near the long-term average input.
//        //
//        // I want it to stop moving immediately so it is gonna me max( age+1, rank-age ) where rank-age comes from the
//        // near-miss.
//        //
//        // It should only affect age when the cell is quite highly ranked.
//        // Age is unlimited when poorly ranked.
//        //
//        // http://www.wolframalpha.com/input/?i=plot+1-e%5E(-7(x))+for+x+%3D+0+to+1
//        // If the rank is near zero, the max possible age will become nearly zero
//        // If the rank is much above zero, (e.g. 0.2) then the max possible age is 0.75 of the normal max age
//        // So only for cells ranking nearly the best have truncated ages.
//        float maxRank = (float)( cells -1 ); // e.g. 64 cells, max rank (0 based) = 63 63/63 = 1
//        float unitRank = (float)rank / maxRank;
//
//        float rankScale = 9.0f; // how nonlinear it is. Should be a param. Controls what rank causes capture by a nearby cluster for a randomly walking age-motile cell
//
//        float unitRankAge = 1.f - (float)Math.exp( - rankScale * unitRank ); // if
//        int rankAge = (int)( unitRankAge * (float)maxAge );
//        float minAge = Math.min( newAge, rankAge );
//
//        _cellAges._values[ cell ] = minAge;
//    }
//
//    // Really 3 parameters:
//    // 1. Rank-Age relationship:
//    // 2. Age-Motility relationship:
//    // 3. Rank-Distance relationship:
//
//    protected void updateWeight( int cell, int inputs, int i, int rank, float age, float maxAge, float inputValue, float rootBestError, float learningRate, float neighbourhoodRange, float minDistance ) {
////        float rankDistance = 1.f - minDistance;
//
//        // http://www.wolframalpha.com/input/?i=plot+e%5E(-7(1-x))+for+x+%3D+0+to+1
//        // http://www.wolframalpha.com/input/?i=plot+e%5E(-17(1-x))+for+x+%3D+0.6+to+1
//        float ageScale = 7.0f; // how nonlinear it is. Should be a param
//        float unitAge = (float)age / (float)maxAge; // 1 iff max age
//        unitAge = 1.f - Math.min( 1.f, unitAge ); // 1 iff min age
//        float ageWeight = (float)Math.exp( - ageScale * unitAge ); // 1 iff max age
//
//        float neighbourhoodFactor = neighbourhoodRange * rootBestError;
//        int offset = cell * inputs + i;
//        float weightOld = _cellWeights._values[ offset ];
//        float signedError = ( inputValue - weightOld );
//        float rankWeight = 0.f;
//        if( neighbourhoodFactor > 0.f ) {
//            rankWeight = (float)Math.exp( - rank / neighbourhoodFactor );
//        }
//
//        float motility = Math.max( rankWeight, ageWeight );
////        float distanceFn = ( rankDistance * rankWeight ) + ( minDistance );
//        float weightNew = weightOld + learningRate * motility * signedError;
//        _cellWeights._values[ offset ] = weightNew;
//    }

    protected void updateWeight( int cell, int inputs, int i, int rank, float age, float maxAge, float inputValue, float rootBestError, float learningRate, float neighbourhoodRange, float minDistance ) {
//        float rankDistance = 1.f - minDistance;

        // http://www.wolframalpha.com/input/?i=plot+e%5E(-7(1-x))+for+x+%3D+0+to+1
        // http://www.wolframalpha.com/input/?i=plot+e%5E(-17(1-x))+for+x+%3D+0.6+to+1
        float ageScale = 7.0f; // how nonlinear it is. Should be a param
        float unitAge = (float)age / (float)maxAge; // 1 iff max age
        unitAge = 1.f - Math.min( 1.f, unitAge ); // 1 iff min age
        float ageWeight = (float)Math.exp( - ageScale * unitAge ); // 1 iff max age

        // pull wandering cells towards the current winner's INPUT in proportion to the distance from the winner and the amount of stress the winner has
//        float bestRelativeStress = ; // a fraction of the sum of all the stress in the network
//        float minStressWeight = 0.01f; // param
//
//        float stressWeight = 0.f;
//        if( bestRelativeStress > 0.f ) {
//            stressWeight = (float)Math.exp( - rank / bestRelativeStress ); // denominator somewhere
//        }
//
//        the problem is rank includes lots of non-mobile cells. We really want a rank of
//
//        stressWeight = stressWeight * (1.f-minStressWeight) + minStressWeight;
//        float stressAgeWeight = stressWeight * ageWeight;

        float neighbourhoodFactor = neighbourhoodRange * rootBestError; // allows convergence because when error = 0, the system doesn't pull other cells
        float rankWeight = 0.f;
        if( neighbourhoodFactor > 0.f ) {
            rankWeight = (float)Math.exp( - rank / neighbourhoodFactor );
        }

        // rank = 0
        // e^( - 0/(5*1  ) ) = e^ (-0.2 ) = 1
        // e^( - 0/(5*0.5) ) = e^ (-0.4 ) = 1
        // e^( - 0/(5*0.1) ) = e^ (-0.4 ) = 1

        // rank = 1
        // e^( - 1/(5*1  ) ) = e^ (-0.2 ) = 0.81   error = 1.0
        // e^( - 1/(5*0.5) ) = e^ (-0.4 ) = 0.67   error = 0.5
        // e^( - 1/(5*0.1) ) = e^ (-0.4 ) = 0.13   error = 0.1
        // e^( - 1/(5*0.01) ) = e^ (-0.4 ) = 0.0   error = 0.01

        float motility = rankWeight;
//        float motility = Math.max( rankWeight, ageWeight );
//        float motility = Math.max( rankWeight, stressAgeWeight );
//        float distanceFn = ( rankDistance * rankWeight ) + ( minDistance );
        int offset = cell * inputs + i;
        float weightOld = _cellWeights._values[ offset ];
        float signedError = ( inputValue - weightOld );
        float weightNew = weightOld + learningRate * motility * signedError;
        _cellWeights._values[ offset ] = weightNew;
    }

    public int getBestCell() {
        return _bestCell;
    }

    public ArrayList< Integer > getSparseUnitInput() {
        return _sparseUnitInput;
    }

    public void setSparseUnitInput( ArrayList< Integer > inputIndices ) {
        _sparseUnitInput = inputIndices;
    }

    public Data getInput() {
        return _inputValues;
    }

}
