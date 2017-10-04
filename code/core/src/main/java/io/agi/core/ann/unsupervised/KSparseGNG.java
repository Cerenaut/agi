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

import io.agi.core.data.Data;
import io.agi.core.data.FloatArray;
import io.agi.core.data.Ranking;
import io.agi.core.orm.ObjectMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;

/**
 * Produces a sparse set of K active cells with maximal overlap with the input. Overlap differs from the Sum of Squared
 * Errors distance metric in that "missing" input bits don't matter. Therefore partial patterns can be recognized, and
 * we can find a set of cells that collectively represent the input, rather than individually representing ALL the input.
 *
 * Note that KSparse Autoencoders also kinda perform an overlap metric, because cells that are maximally active in the
 * hidden layer have responded to a subset of the input bits...
 *
 * Created by dave on 14/09/17.
 */
public class KSparseGNG extends GrowingNeuralGas {

    public Data _inputUnderlap;
    public Data _cellOverlap;

    public KSparseGNG( String name, ObjectMap om ) {
        super( name, om );
    }

    public void setup( KSparseGNGConfig c ) {
        super.setup( c );

        int inputs = c.getNbrInputs();
        int w = c.getWidthCells();
        int h = c.getHeightCells();

        _inputUnderlap = new Data( inputs );
        _cellOverlap = new Data( w, h );
    }

    public void reset() {
        _inputUnderlap.set( 0f );
        _cellOverlap.set( 0f );
        _cellWeights.set( 0.5f );
    }

    public void update() {

        // given current cell mask local, ensure that there are at least 2 cells locally.
        if( !addCellPairLazy() ) {
            return; // no cells available to model any input.
        }

        KSparseGNGConfig config = (KSparseGNGConfig)_c;

        // 1. rank cells by overlap
        // Overlap differs from sum-sq-error in that additional inputs are not penalized, meaning that it's possible
        // for cells to respond to partial patterns *when the input is sparse*. Example.
        // Bits      0 1 2 3 4 5 6 7 8 9  SSE Overlap
        // Input:    1 1 0 0 0 1 1 0 0 0
        // Cell A:   1 1 0 0 0 0 0 0 0 0  2   2     --- match to sub-pattern of bits
        // Cell B:   0 0 0 0 0 0 0 0 1 1  2   0     --- all low weights, or missing pattern, just as good as cell A.

        // Errors were used for ranking, stress, and utility calculations. Now used for none of these.
        ArrayList< Integer > liveCells = createCellList( _c, _cellMask );
        if( _sparseUnitInput != null ) {
            // Sparse input
            sumSqErrorSparseUnit( _c, liveCells, _sparseUnitInput, _cellWeights, _cellErrors ); // error only needed for diagnostics now
            findOverlapSparseUnit( liveCells, _sparseUnitInput );
        } else {
            // Dense input
            sumSqError( _c, liveCells, _inputValues, _cellWeights, _cellErrors ); // error only needed for diagnostics now
            findOverlap( liveCells, _inputValues );
        }

        // add a tiny amount of overlap to all live cells so that in the event of zero overlap they are still ranked
        float minOverlap = 0.00001f;
        for( Integer c : liveCells ) {
            _cellOverlap._values[ c ] += minOverlap;
        }

        // 2. Declare the K-most overlapping cells to be the winners. This produces the output of the cell
        // (i.e. its sparse cell set).
        // Get the top 2 cells, A and B
        float sparsity = config.getSparsity();
        int area = config.getAreaCells();
        int maxRank = Math.max( 2, (int)( area * sparsity ) ); // can't have less than 2 active cells

        boolean findMaxima = true; // find minima
        TreeMap< Float, ArrayList< Integer > > ranking = new TreeMap<>();
        _cellActivity.set( 0.f );
        findBestNCells( _c, _cellMask, _cellOverlap, _cellActivity, maxRank, findMaxima, ranking ); // activity is whether ranked
        ArrayList< Integer > bestCells = Ranking.getBestValues( ranking, findMaxima, maxRank );
        if( bestCells.size() < 2 ) {
            // This can happen when we have a local cell mask; we just need to assign two cells.
            return;
        }

        if( _sparseUnitInput != null ) {
            findInputUnderlapSparseUnit( bestCells, _sparseUnitInput );
        }
        else {
            findInputUnderlap( bestCells, _inputValues );
        }
//        _bestCell = -1; //bestValues.get( 0 );
//        _2ndBestCell = -1; //bestValues.get( 1 );

        // don't age or do learning/maintenance when input isn't changing. This is achieved by setting learning to false
        // when this occurs
        boolean learn = _c.getLearn();
        if( !learn ) {
            return;
        }

        // 3. Train the winning cells to be more like the input. This causes weights to decrease when not in the current
        // input and increase when they ARE in the current input.
        // For now we assume that partial patterns can be learned by their inconsistency in the input observed when cells
        // win. Partial patterns are possible because you only need a partial pattern to win.
        // Before training:
        // Bits      0 1 2 3 4  5  6 7  8  9  SSE Overlap
        // Input:    1 1 0 0 0  1  1 0  0  0
        // Cell A:   1 1 0 0 0  0  0 0  0  0
        // After training:
        // Bits      0 1 2 3 4  5  6 7  8  9
        // Input:    1 1 0 0 0  1  1 0  0  0
        // Cell A:   1 1 0 0 0 .2 .2 0  0  0
        // After training on a different input, but same sub-pattern:
        // Bits      0 1 2 3 4  5  6 7  8  9
        // Input:    1 1 0 0 0  1  1 0  1  1
        // Cell A:   1 1 0 0 0  0  0 0 .2 .2
        // Note that weights do not persist unless they are part of a clique of inputs that are often observed together.
        // Cells will learn entire cliques if they are consistent, but not if they are only rarely observed together.

        reduceStress();
        reduceUtility();

        // GNG stress is based on the best cell individually. It is a measure of how poorly represented the input is.
        // Overlap stress should be based on any missing overlap from the winning set. If the winning set have a lot of
        // missing signal, they have high stress because the input is under-represented.
        updateOverlapStress( bestCells );

        // update various statistics for all pairs of winning cells
        // NOTE: The important thing about neighbouring cells is that they are trained towards winners' input, just not
        // as much. All neighbours are trained, even if not current input.
        for( Integer c1 : bestCells ) {

            // find cell with max overlap in the k-best set and make this the neighbour. reduces the density of neighbours
            int bestOverlapNeighbour = findOverlapNeighbour( c1, bestCells );
            int c2 = bestOverlapNeighbour;
//            for( Integer c2 : bestCells ) {

            _bestCell    = c1; // these are now just temporary values
            _2ndBestCell = c2;

//            // Create the edge A,B if it doesn't already exist:
//            updateEdges();
            addEdge( _bestCell, _2ndBestCell );

            // update the utility of the best cell, given the nearest alternative
            updateOverlapUtility();
        }

//        for( Integer c1 : bestCells ) {
//            for( Integer c2 : bestCells ) {
//                //if( c1.equals( c2 ) ) {
//                if( c2 <= c1 ) {
//                    continue; // no self edges, ignore bidirectional
//                }
//
//                _bestCell    = c1; // these are now just temporary values
//                _2ndBestCell = c2;
//
//                // Create the edge A,B if it doesn't already exist:
//                //updateEdges();
//                addEdge( _bestCell, _2ndBestCell );
//            }
//        }

        removeOldEdges();

        // remove detached cells (without edges) when utility is disabled (i.e., normal GNG)
        float utilityThreshold = _c.getUtilityThreshold();
        if( utilityThreshold < 0f ) {
            removeDetachedCells();
        }

        _bestCell    = -1; // these are now just temporary values
        _2ndBestCell = -1;

        // update stats for winning cells
        for( Integer c1 : bestCells ) {
            _bestCell    = c1; // these are now just temporary values

            // train winner A and its neighbours towards the input.
            // NOTE: Doesn't have to be a current neighbour, an anytime neighbour is also trained
            trainCells();

            // update utility - a function of how much is value is provided by each cell.
//            updateOverlapUtility( c1, bestCells );
        }

        _bestCell    = -1; // these are now just temporary values
        _2ndBestCell = -1;

        // Create new cells where the space is poorly represented (the cells are stressed)
        int ageSinceGrowth = ( int ) _ageSinceGrowth._values[ 0 ];
        if( ageSinceGrowth >= _c.getGrowthInterval() ) {
            removeLowUtilityCell();
            if( addCells() ) {
                ageSinceGrowth = 0;
            }
        }

        _ageSinceGrowth._values[ 0 ] = ageSinceGrowth + 1;
        updateCellsAges(); // all live cells are aged. Only used to enforce min-age for deletion criterion
    }

    protected int findOverlapNeighbour( int bestCell1, Collection< Integer > bestCells ) {
        // find the cell with the maximum overlap with bestCell, which will be its neighbour (alternate or backup) if bestCell were removed.
        // If cells cease to have neighbours, then it also follows that they have ceased to win.
        int inputs = _c.getNbrInputs();

        float maxSumOverlap = 0f;
        Integer neighbour = null;

        for( Integer bestCell2 : bestCells ) {
            if( bestCell1 == bestCell2 ) {
                continue; // don't care about reciprocal relations or comparison with self.
            }

            float sumOverlap = 0f;

            for( int i = 0; i < inputs; ++i ) {
                float weight1 = _cellWeights._values[ bestCell1 * inputs + i ]; // error from ci to cell
                float weight2 = _cellWeights._values[ bestCell2 * inputs + i ]; // error from ci to cell
                float overlap = Math.min( weight1, weight2 );
                sumOverlap += overlap;
            }

            if( sumOverlap >= maxSumOverlap ) {
                neighbour = bestCell2;
                maxSumOverlap = sumOverlap;
            }
        }

        return neighbour;
    }

    protected void findInputUnderlapSparseUnit( Collection< Integer > bestCells, Collection< Integer > sparseInput ) {
        //the input not overlapped by the winning cells.
        //This should be the min underlap over all cells, ie. if any cell overlaps then its OK.
        int inputs = _c.getNbrInputs();

        for( int i = 0; i < inputs; ++i ) {

            float input = 0f;
            if( sparseInput.contains( i ) ) {
                input = 1f;
            }

            float minUnderlap = Float.MAX_VALUE;

            for( Integer cell : bestCells ) {

                float weight = _cellWeights._values[ cell * inputs + i ]; // error from ci to cell
                float underlap = input - weight; // ie if input = 1 and weight = 0, then underlap = 1. If input = 0 and weight = 1 then underlap = -1 = 0

                underlap = Math.max( 0f, underlap ); // if negative, then weight is larger than input and there's no underlap

                minUnderlap = Math.min( minUnderlap, underlap );
            }

            _inputUnderlap._values[ i ] = minUnderlap;
        }
    }

    protected void findInputUnderlap( Collection< Integer > bestCells, FloatArray inputValues ) {
        //the input not overlapped by the winning cells.
        //This should be the min underlap over all cells, ie. if any cell overlaps then its OK.
        int inputs = _c.getNbrInputs();

        for( int i = 0; i < inputs; ++i ) {

            float minUnderlap = Float.MAX_VALUE;
            float input = _inputValues._values[ i ];

            for( Integer cell : bestCells ) {

                float weight = _cellWeights._values[ cell * inputs + i ]; // error from ci to cell
                float underlap = input - weight;

                underlap = Math.max( 0f, underlap ); // if negative, then weight is larger than input and there's no underlap

                minUnderlap = Math.min( minUnderlap, underlap );
            }

            _inputUnderlap._values[ i ] = minUnderlap;
        }
    }

    protected void updateOverlapStress( Collection< Integer > bestCells ) {

        // this should now be what - the unoverlapped input?
        // given all cells or just this cell?
        // Id like it to be given all cells, as we measure their contribution and they are active jointly.

        // Yanir: _cellStress._values[_bestCell] += _cellErrors._values[_bestCell];
        float underlap = _inputUnderlap.sum(); // use abs errors instead of sq errors?
        float stressOld = _cellStress._values[ _bestCell ];
        float stressNew = stressOld + underlap; // ( float ) Unit.lerp( stressOld, bestSumSqError, cellStressAlpha );
        _cellStress._values[ _bestCell ] = stressNew;
    }

    protected void updateOverlapUtility() {// int bestCell, Collection< Integer > bestCells ) {

        // increase utility of winner
        // U_winner = U_winner + error_2nd - error_winner
        // So if error_winner = 0 and error_2nd = 1
        // utility = 1-0
        // if winner = 1 and error = 1.1, then
        // utility = 1.1 - 1 = 0.1 (low utility, because 2nd best almost as good)
        // error B >= A by definition, cos A won.
        float overlapA = _cellOverlap._values[ _bestCell ]; // use abs errors instead of sq errors?
        float overlapB = _cellOverlap._values[ _2ndBestCell ]; // use abs errors instead of sq errors?
        float utility = overlapA - overlapB; // ie how much more overlap we got from A than B. If zero or neg, then B was better than A and A had no utility
        utility = Math.max( 0f, utility );
        float utilityOld = _cellUtility._values[ _bestCell ];
        float utilityNew = utilityOld + utility;
        _cellUtility._values[ _bestCell ] = utilityNew;
    }

    protected void findOverlapSparseUnit( Collection< Integer > liveCells, Collection< Integer > inputValues ) {
        int inputs = _c.getNbrInputs();

        for( Integer cell : liveCells ) {

            float sumOverlap = 0.f;

            // We assume there are few inputs that are nonzero.
            // first sum all the weights and square them
            for( Integer i : inputValues ) {
                float input = 1.f;
                float weight = _cellWeights._values[ cell * inputs + i ]; // error from ci to cell

                float overlap = Math.min( input, weight );

                sumOverlap += overlap;
            }

            _cellOverlap._values[ cell ] = sumOverlap;
        }
    }

    protected void findOverlap( Collection< Integer > liveCells, FloatArray inputValues ) {
        int inputs = _c.getNbrInputs();

        for( Integer cell : liveCells ) {

            float sumOverlap = 0.f;

            // We assume there are few inputs that are nonzero.
            // first sum all the weights and square them
            for( int i = 0; i < inputs; ++i ) {
                float input = inputValues._values[ i ];
                float weight = _cellWeights._values[ cell * inputs + i ]; // error from ci to cell
                float overlap = Math.min( input, weight );

                sumOverlap += overlap;
            }

            _cellOverlap._values[ cell ] = sumOverlap;
        }
    }

/*    protected void trainCells() {
        float bestCellLearningRate = _c.getLearningRate();
        float neighboursLearningRate = _c.getLearningRateNeighbours();
        int inputs = _c.getNbrInputs();
        int cells = _c.getNbrCells();
        for( int cell = 0; cell < cells; ++cell ) {

            // skip invalid cells and cells that aren't neighbours of the best cells
            float maskValue = _cellMask._values[ cell ];
            if( maskValue == 0.f ) {
                continue; // not a valid cell
            }

            float cellLearningRate = neighboursLearningRate;

            if( cell == _bestCell ) {
                cellLearningRate = bestCellLearningRate;
            } else { // not the winner, maybe a neighbour:
                if( !areNeighbours( _bestCell, cell ) ) {
                    continue;
                }
            }

            for( int i = 0; i < inputs; ++i ) {
                float inputValue = 0.f;
                if( _sparseUnitInput == null ) {
                    inputValue = _inputValues._values[ i ];
                }
                else if( _sparseUnitInput.contains( i ) ) {
                    inputValue = 1.f;
                }

                updateWeight( cell, inputs, i, inputValue, cellLearningRate );
            }
        }
    }*/

    protected void updateWeight( int cell, int inputs, int i, float inputValue, float cellLearningRate ) {

//        saturating weight rule
//        allow asymmetric + and - weight updates so weights tend to zero.
//        if you keep winning, eventually other weights go down
//        but input keeps changing, so all weights go down faster than they go up
//        float newWeightValue = oldWeightValue + learningRate * oldWeightValue * ( 1f - oldWeightValue );

        int offset = cell * inputs + i;
        //float noise = getNoiseSample();
        float weightOld = _cellWeights._values[ offset ];

        float error = inputValue - weightOld;
        if( error > 0.0f ) {
            // increase weight
            error *= 0.5f; // learn increases more slowly
        }
        else {
            // decrease weight
        }

        float weightNew = weightOld + cellLearningRate * ( error );// + noise;
        //float weightNew = weightOld + cellLearningRate * ( inputValue - weightOld );// + noise;
        _cellWeights._values[ offset ] = weightNew;
    }

}
