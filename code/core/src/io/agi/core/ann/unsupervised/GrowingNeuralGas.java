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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.IntStream;

/**
 * Growing Neural Gas algorithm [with Utility] (unsupervised learning).
 * <p/>
 * Input can be provided as a dense Data structure, or as a sparse Array of values. In the latter case it is assumed
 * the values are all '1' and therefore the values of the sparse input are used as indices.
 * <p/>
 * Based on pseudocode @ http://www.juergenwiki.de/work/wiki/doku.php?id=public:growing_neural_gas
 * <p/>
 * Created by dave on 29/12/15.
 */
public class GrowingNeuralGas extends CompetitiveLearning {
    private static final Logger _logger = LogManager.getLogger();

    public GrowingNeuralGasConfig _c;
    private Set< Integer > _sparseUnitInput;
    public Data _inputValues;
    public Data _cellWeights;
    public Data _cellErrors;
    public Data _cellActivity;
    public Data _cellMask;

    public Data _cellUtility;
    public Data _cellStress;
    public Data _cellAges;
    public Data _edges;
    public Data _edgesAges;
    public Data _ageSinceGrowth; // 1 element

    private int _bestCell = 0;
    private int _2ndBestCell = 0;

    private Set< Integer > _originalSparseUnitInput;
    private Data _originalInputValues;

    public GrowingNeuralGas( String name, ObjectMap om ) {
        super( name, om );
        _logger.debug( "GNG constructed: {}", name );
    }

    public void setup( GrowingNeuralGasConfig c ) {
        _c = c;

        int inputs = c.getNbrInputs();
        int w = c.getWidthCells();
        int h = c.getHeightCells();
        int cells = c.getNbrCells();

        _inputValues = new Data( inputs );
        _cellWeights = new Data( w, h, inputs );
//        _cellWeights.setRandom(); // start with no cells
        _cellErrors = new Data( w, h );
        _cellActivity = new Data( w, h );
        _cellMask = new Data( w, h );
        _cellMask.set( 0.f ); // disable all cells

        _cellUtility = new Data( w, h );
        _cellStress = new Data( w, h );
        _cellAges = new Data( w, h );
        _edges = new Data( cells, cells );
        _edgesAges = new Data( cells, cells );
        _ageSinceGrowth = new Data( 1 );
    }

    public void reset() {
        _cellMask.set( 0.f ); // disable all cells
        _cellStress.set( 0.f ); // disable all cells
        _cellUtility.set( 0.f ); // disable all cells
    }

    public void update() {
        //denoiseInput();

        // given current cell mask local, ensure that there are at least 2 cells locally.
        if( !addCellPairLazy() ) {
            return; // no cells available to model any input.
        }

        // Compute error
        ArrayList< Integer > liveCells = createCellList( _c, _cellMask );
        if( _sparseUnitInput != null ) {
            // Sparse input
            sumSqErrorSparseUnit( _c, liveCells, _sparseUnitInput, _cellWeights, _cellErrors );
        } else {
            // Dense input
            sumSqError( _c, liveCells, _inputValues, _cellWeights, _cellErrors );
        }

        // Get the top 2 cells, A and B
        int maxRank = 2;
        boolean findMaxima = false; // find minima
        TreeMap< Float, ArrayList< Integer > > ranking = new TreeMap<>();
        _cellActivity.set( 0.f );
        findBestNCells( _c, _cellMask, _cellErrors, _cellActivity, maxRank, findMaxima, ranking ); // activity is whether ranked
        ArrayList< Integer > bestValues = Ranking.getBestValues( ranking, findMaxima, maxRank );
        if( bestValues.size() < 2 ) {
            // This can happen when we have a local cell mask; we just need to assign two cells.
            return;
        }

        _bestCell = bestValues.get( 0 );
        _2ndBestCell = bestValues.get( 1 );

        // don't age or do learning/maintenance when input isn't changing. This is achieved by setting learning to false
        // when this occurs
        boolean learn = _c.getLearn();
        if( !learn ) {
            return;
        }

        // Create the edge A,B if it doesn't already exist:
        updateEdges();

        // remove detached cells (without edges) when utility is disabled (i.e., normal GNG) 
        float utilityThreshold = _c.getUtilityThreshold();
        if( utilityThreshold < 0f ) {
            removeDetachedCells();
        }

        // Update learned error of the winning cell A
        // errorA = errorA + distance to input vector
        // Stress is defined as the sum of errors for the input, over a moving recent average.
        // http://www.demogng.de/JavaPaper/node19.html  - cumulative forever, not an average.
        reduceStress();
        updateStress();

        // train winner A and its neighbours towards the input
        trainCells();

        // update utility
        reduceUtility();
        updateUtility();

        // Create new cells where the space is poorly represented (the cells are stressed)
        int ageSinceGrowth = ( int ) _ageSinceGrowth._values[ 0 ];
        if( ageSinceGrowth >= _c.getGrowthInterval() ) {
            removeLowUtilityCell();
            if( addCells() ) {
                ageSinceGrowth = 0;
            }
        }

        _ageSinceGrowth._values[ 0 ] = ageSinceGrowth + 1;
        updateCellsAges();

        //undoDenoiseInput();
    }

    /**
     * Denoise the input by selecting a random set of pixels to set to zero.
     */
    private void denoiseInput() {
        if( _c.getDenoisePercentage() > 0 ) {
            _originalSparseUnitInput = _sparseUnitInput;
            _originalInputValues = _inputValues;
            int numZeroIndices = ( int ) ( _c.getNbrInputs() * _c.getDenoisePercentage() );
            _logger.debug( "Setting {} inputs to zero", numZeroIndices );
            IntStream zeroIndices = _c._r.ints( _c.getNbrInputs(), 0, numZeroIndices );
            if( _sparseUnitInput == null ) {
                _inputValues = new Data( _inputValues );
                zeroIndices.forEach( i -> _inputValues._values[ i ] = 0 );
            } else {
                _sparseUnitInput = new HashSet<>( _sparseUnitInput );
                zeroIndices.forEach( i -> _sparseUnitInput.remove( i ) );
            }
        } 
    }

    /**
     * Undo input denoising by resetting the inputs back to what they were before the last call to denoiseInput().  
     */
    private void undoDenoiseInput() {
        if( _c.getDenoisePercentage() > 0 ) {
            _sparseUnitInput = _originalSparseUnitInput;
            _inputValues = _originalInputValues;
        }
    }

    private void removeLowUtilityCell() {
        // Do nothing if the utility is disabled (i.e., normal GNG)
        float threshold = _c.getUtilityThreshold();
        if( threshold < 0f ) {
            return; 
        }

        int cells = _c.getNbrCells();
        int activeCells = (int) _cellMask.sum();
        // don't recycle just 2 cells or when we don't need to.
        if( ( activeCells <= 2 ) || ( activeCells < cells ) ) {
            return;
        }

        // find cells with max stress and min utility
        int minUtilityCell = 0;

        float minUtility = Float.MAX_VALUE;
        float maxStress = 0f;

        for( int cell1 = 0; cell1 < cells; ++cell1 ) {

            if( _cellMask._values[ cell1 ] == 0.f ) {
                continue; // already dead
            }

            // cells are only eligible for removal when they're old enough to have proven their value (or not)
            int minAge = _c.getEdgeMaxAge();
            int age = (int)_cellAges._values[ cell1 ];
            if( age < minAge ) {
                continue; // not eligible for removal
            }

            float stress = _cellStress._values[ cell1 ];
            float utility = _cellUtility._values[ cell1 ];

            if( stress > maxStress ) {
                maxStress = stress;
            }

            if( utility < minUtility ) {
                minUtility = utility;
                minUtilityCell = cell1;
            }
        }

        // error = sumsqerror over time
        // remove 1 cell with low utility iff
        //   high error / low utility
        // ( error_j / U_i ) > K
        // where j = node with greatest error (stress)
        // i = smallest utility
        // Say maxStress = 100
        // minUtility = 20
        // 100/20 = 5  - good
        // minUtility = 2
        // 100/2 = 50  - bad
        float ratio = maxStress / ( minUtility + 0.0001f );
        if( ratio <= threshold ) {
            return;
        }

        // get rid of the low utility cell:
        _cellMask._values[ minUtilityCell ] = 0.f;
        _cellAges._values[ minUtilityCell ] = 0.f;

        for( int cell2 = 0; cell2 < cells; ++cell2 ) {

            int offset = getEdgeOffset( minUtilityCell, cell2 );

            _edges._values[ offset ] = 0; // clear all edges with this cell
        }
    }

    public int getBestCell() {
        return _bestCell;
    }

    public int get2ndBestCell() {
        return _2ndBestCell;
    }

    public void setSparseUnitInput( ArrayList< Integer > inputIndices ) {
        _sparseUnitInput = new HashSet<>( inputIndices );
    }

    public void setInput( Data input ) {
        _inputValues.copy( input );
    }

    public Data getInput() {
        return _inputValues;
    }

    private int getCellCount() { // can be local or global cell count
        int cells = _c.getNbrCells();
        int cellCount = 0;

        for( int cell = 0; cell < cells; ++cell ) {
            float maskValue = _cellMask._values[ cell ];
            if( maskValue > 0.f ) {
                ++cellCount;
            }
        }

        return cellCount;
    }

    private boolean addCellPairLazy() {

        //find 2 globally available cells, and assign them to the local cell mask.
        // activate 2 cells ONLY
        // an edge will form between them when they are subsequently picked as the best (and only) match.
        int cellCount = getCellCount();

        while( cellCount < 2 ) {
            Integer freeCell = findFreeCell();
            if( freeCell == null ) {
                return false; // no free cell
            }

            _cellMask._values[ freeCell ] = 1.f;
            _cellAges._values[ freeCell ] = 0.f;

//            Fritzke does not define the initialisation of the utility variable for a new node.
//            However, in the DemoGNG v1.5 implementation [6] it is defined as the mean of
//            Uu and Uv
//            . Note also that there is no mention of a decrease of the utilities of nodes
//            u and v corresponding to the error decrease in GNG after a new node has been
//            inserted. It stands to reason that the utilities of u and v should be decreased in the
//            same manner as the errors.

            ++cellCount;
        }

        return true;
    }

    /**
     * @return the first cell with mask value of zero, or null if no such cell can be found.
     */
    private Integer findFreeCell() {
        int cells = _c.getNbrCells();
        for( int cell = 0; cell < cells; ++cell ) {
            if( _cellMask._values[ cell ] == 0.f ) {
                return cell;
            }
        }
        return null;
    }

    private boolean addCells() {
        Integer freeCell = findFreeCell(); // Note: look for globally available cell
        if( freeCell == null ) {
            //System.err.println( "ERR: no free cell");
            return false; // can't add any cells at the moment
        }

        AbstractPair< Integer, Integer > stressedCells = findStressedCells();
        if( stressedCells == null ) {
            return false;
        }
        bisectCells( stressedCells._first, stressedCells._second, freeCell );
        return true;
    }

    private AbstractPair< Integer, Integer > findStressedCells() {
        // find the cell with the largest error
        float minStressThreshold = _c.getStressThreshold();

        Integer worstCell = null;
        float worstStress = 0.f;

        int cells = _c.getNbrCells();

        for( int cell = 0; cell < cells; ++cell ) {

            // Note: look for maximum stress globally.??
            float maskValue = _cellMask._values[ cell ];
            if( maskValue == 0.f ) {
                continue; // not a valid cell
            }

            float stress = _cellStress._values[ cell ];
            if( stress >= worstStress ) {
                worstStress = stress;
                worstCell = cell;
            }
        }

        if( worstStress < minStressThreshold ) {
            //System.err.println( "ERR: not stressed");
            return null;
        }

        // find the worst neighbour of worstCell
        Integer worstCell2 = null;
        float worstStress2 = 0.f;

        for( int cell2 = 0; cell2 < cells; ++cell2 ) {

            // find an actual neighbour of worstCell
            if( !areNeighbours( worstCell, cell2 ) ) {
                continue;
            }

            // OK cell2 is a neighbour:
            float stress = _cellStress._values[ cell2 ];
            if( stress >= worstStress2 ) {
                worstStress2 = stress;
                worstCell2 = cell2;
            }
        }

        if( worstCell2 == null ) {
            //System.err.println( "ERR: no worst cell #2");
            return null;
        }

        return new AbstractPair<>( worstCell, worstCell2 );
    }

    private void bisectCells( int worstCell, int worstCell2, int freeCell ) {

        // OK now we have the two cells we want to divide by putting a new cell
        // between them

        // set weights of the new cell as median of the two worst cells
        int inputs = _c.getNbrInputs();

        for( int i = 0; i < inputs; ++i ) {
            float weightWorstCell  = _cellWeights._values[ worstCell * inputs + i ];
            float weightWorstCell2 = _cellWeights._values[ worstCell2 * inputs + i ];

            float weightF  = ( weightWorstCell + weightWorstCell2 ) * 0.5f;
            //float noise = getNoiseSample();
            weightF = weightF;// + noise;
            _cellWeights._values[ freeCell * inputs + i ] = weightF;
        }

        // activate the new cell
        _cellMask._values[ freeCell ] = 1.f;
//        cellMaskLocal._values[ freeCell ] = 1.f;

        // Create edges: worst, free; worst2, free;
        // remove edges: worst, worst2
        int offsetWW2 = getEdgeOffset( worstCell , worstCell2 );
        int offsetWF  = getEdgeOffset( worstCell, freeCell );
        int offsetW2F = getEdgeOffset( worstCell2, freeCell );

        _edgesAges._values[ offsetWW2 ] = 0.f; // this one was just used
        _edgesAges._values[ offsetWF  ] = 0.f; // this one was just used
        _edgesAges._values[ offsetW2F ] = 0.f; // this one was just used

        _edges._values[ offsetWW2 ] = 0.f; // remove the edge
        _edges._values[ offsetWF  ] = 1.f; // Create the edge
        _edges._values[ offsetW2F ] = 1.f; // Create the edge

        // reset stress of all cells.
        // Note, this means I mustn't add any new cells until the new stresses
        // have had time to work out
//        _cellStress.set( 0.f ); // give it time to accumulate
        float stressWorst1 = _cellStress._values[ worstCell  ];
        float stressWorst2 = _cellStress._values[ worstCell2 ];
        float stressFreeNew = ( stressWorst1 + stressWorst2 ) * 0.5f;

        float cellStressSplitLearningRate = _c.getStressSplitLearningRate();
        float stressWorst1New = stressWorst1 - (stressWorst1 * cellStressSplitLearningRate );
        float stressWorst2New = stressWorst2 - (stressWorst2 * cellStressSplitLearningRate );

        _cellStress._values[ worstCell  ] = stressWorst1New;
        _cellStress._values[ worstCell2 ] = stressWorst2New;
        _cellStress._values[ freeCell ] = stressFreeNew;

        // bisect utility of new cell
        // U_new = ( U_worst1 + U_worst2 ) / 2
        float utilityWorst1 = _cellStress._values[ worstCell  ];
        float utilityWorst2 = _cellStress._values[ worstCell2 ];
        float utilityFreeNew = ( utilityWorst1 + utilityWorst2 ) * 0.5f;

        _cellUtility._values[ worstCell  ] = utilityFreeNew;
        _cellUtility._values[ worstCell2 ] = utilityFreeNew;
        _cellUtility._values[ freeCell   ] = utilityFreeNew;

        _cellAges._values[ worstCell  ] = 0;
        _cellAges._values[ worstCell2 ] = 0;
        _cellAges._values[ freeCell   ] = 0;

//        Fritzke does not define the initialisation of the utility variable for a new node.
//        However, in the DemoGNG v1.5 implementation [6] it is defined as the mean of
//        Uu and Uv
//        . Note also that there is no mention of a decrease of the utilities of nodes
//        u and v corresponding to the error decrease in GNG after a new node has been
//        inserted. It stands to reason that the utilities of u and v should be decreased in the
//        same manner as the errors.
    }

    /**
     * Stress is defined as the sum of errors for the input, over a moving recent average.
     * http://www.demogng.de/JavaPaper/node19.html  - cumulative forever, not an average.
     */
    public void updateStress() {
        // Yanir: _cellStress._values[_bestCell] += _cellErrors._values[_bestCell];
        float bestSumSqError = _cellErrors._values[ _bestCell ]; // use abs errors instead of sq errors?
        float stressOld = _cellStress._values[ _bestCell ];
        float stressNew = stressOld + bestSumSqError; // ( float ) Unit.lerp( stressOld, bestSumSqError, cellStressAlpha );
        _cellStress._values[ _bestCell ] = stressNew;
    }

    protected void updateUtility() {

        // increase utility of winner
        // U_winner = U_winner + error_2nd - error_winner
        // So if error_winner = 0 and error_2nd = 1
        // utility = 1-0
        // if winner = 1 and error = 1.1, then
        // utility = 1.1 - 1 = 0.1 (low utility, because 2nd best almost as good)
        // error B >= A by definition, cos A won.
        float sumSqErrorA = _cellErrors._values[ _bestCell ]; // use abs errors instead of sq errors?
        float sumSqErrorB = _cellErrors._values[ _2ndBestCell ]; // use abs errors instead of sq errors?
        float utility = sumSqErrorB - sumSqErrorA; // error B >= A by definition, cos A won.
        float utilityOld = _cellUtility._values[ _bestCell ];
        float utilityNew = utilityOld + utility;
        _cellUtility._values[ _bestCell ] = utilityNew;
    }

    private void reduceStress() {
        // exponentially decay stress
        // S = S - Eta * S
        float cellStressLearningRate = _c.getStressLearningRate();
        for( int i = 0; i < _cellStress._values.length; ++i ) {
            _cellStress._values[ i ] -= _cellStress._values[ i ] * cellStressLearningRate;
        }
    }

    private void reduceUtility() {
        // exponentially decay utility
        // U = U - Beta * U
        float cellUtilityLearningRate = _c.getUtilityLearningRate();
        for( int i = 0; i < _cellUtility._values.length; ++i ) {
            _cellUtility._values[ i ] -= _cellUtility._values[ i ] * cellUtilityLearningRate;
        }
    }

    private void trainCells() {
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
    }

    protected void updateWeight( int cell, int inputs, int i, float inputValue, float cellLearningRate ) {
        int offset = cell * inputs + i;
        //float noise = getNoiseSample();
        float weightOld = _cellWeights._values[ offset ];
        float weightNew = weightOld + cellLearningRate * ( inputValue - weightOld );// + noise;
        _cellWeights._values[ offset ] = weightNew;
    }

    protected float getNoiseSample() {
        float magnitude = _c.getNoiseMagnitude();

        float noise = 0.f;
        if( magnitude > 0.f ) {
            noise = ( float ) ( ( _c._r.nextDouble() - 0.5 ) * magnitude );
        }

        return noise;
    }

    protected void updateEdges() {
        int maxEdgeAge = _c.getEdgeMaxAge();
        int cells = _c.getNbrCells();

        int offset = getEdgeOffset( _bestCell, _2ndBestCell );
//        edgeAges.add( 1.f ); only increment ages of neighbours of bestCellA
        _edgesAges._values[ offset ] = 0.f; // this one was just used
        _edges._values[ offset ] = 1.f; // Create the edge if not already

        // now go and prune
        for( int cell1 = 0; cell1 < cells; ++cell1 ) {
            for( int cell2 = cell1 + 1; cell2 < cells; ++cell2 ) {
                offset = cell1 * cells + cell2;
                float edge = _edges._values[ offset ];
                if( edge <= 0.f ) {
                    continue; // not a neighbour
                }

                // is a neighbour:
                _edgesAges._values[ offset ] += 1.f;

                float edgeAge = _edgesAges._values[ offset ];
                if( edgeAge > maxEdgeAge ) {
                    _edges._values[ offset ] = 0.f; // no longer an edge
                    _edgesAges._values[ offset ] = 0.f; // no point in storing larger ages
                }
            }
        }
    }

    protected void updateCellsAges() {
        int cells = _c.getNbrCells();
        // now go and prune
        for( int cell = 0; cell < cells; ++cell ) {
            // only update live cells
            if( _cellMask._values[ cell ] != 0 ) {
                _cellAges._values[ cell ]++;
            }
        }
    }

    protected void removeDetachedCells() {
        int cells = _c.getNbrCells();

        // now go and prune
        for( int cell1 = 0; cell1 < cells; ++cell1 ) {

            if( _cellMask._values[ cell1 ] == 0.f ) {
                continue; // already dead
            }

            boolean hasEdge = false;

            for( int cell2 = 0; cell2 < cells; ++cell2 ) {

                int offset = getEdgeOffset( cell1, cell2 );

                float edge = _edges._values[ offset ];
                if( edge > 0.f ) {
                    hasEdge = true;
                    break; // this cell is OK
                }
            }

            if( hasEdge ) {
                continue; // don't remove
            }

            // get rid of this cell:
            // already has no edges, so dont need to delete.
            _cellMask._values[ cell1 ] = 0.f;
            _cellAges._values[ cell1 ] = 0.f;
        }
    }

    public void setNeighbours(
        int cellA,
        int cellB,
        boolean isNeighbour ) {

        if( cellA == cellB ) {
            return;
        }

        int offset = getEdgeOffset( cellA, cellB );
        float value = 0.f;

        if( isNeighbour ) {
            value = 1.f;
        }

        _edges._values[ offset ] = value;
    }

    public boolean areNeighbours( int cellA, int cellB ) {
        //return cellA != cellB && _edges._values[ getEdgeOffset( cellA, cellB ) ] > 0;
        if( cellA == cellB ) {
            return false;
        }

        int offset = getEdgeOffset( cellA, cellB );
        float value = _edges._values[ offset ];

        if( value > 0.f ) {
            return true;
        }

        return false;
    }

    public int getEdgeOffset( int cellA, int cellB ) {
        // the smaller value is used as index.
        return Math.min( cellA, cellB ) * _c.getNbrCells() + Math.max( cellA, cellB );
    }
}
