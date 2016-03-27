package io.agi.core.ann.unsupervised;

import io.agi.core.data.Data;
import io.agi.core.data.Ranking;
import io.agi.core.math.RandomInstance;
import io.agi.core.math.Unit;
import io.agi.core.orm.AbstractPair;
import io.agi.core.orm.ObjectMap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeMap;

/**
 * Growing Neural Gas algorithm (online unsupervised learning).
 * <p>
 * Input can be provided as a dense Data structure, or as a sparse Array of values. In the latter case it is assumed
 * the values are all '1' and therefore the values of the sparse input are used as indices.
 * <p>
 * Based on pseudocode @ http://www.juergenwiki.de/work/wiki/doku.php?id=public:growing_neural_gas
 * <p>
 * Created by dave on 29/12/15.
 */
public class GrowingNeuralGas extends CompetitiveLearning {

    public GrowingNeuralGasConfig _c;
    public ArrayList< Integer > _sparseUnitInput;
    public Data _inputValues;
    public Data _cellWeights;
    public Data _cellErrors;
    public Data _cellActivity;
    public Data _cellMask;

    public Data _cellStress;
    public Data _cellAges;
    public Data _edges;
    public Data _edgesAges;
    public Data _ageSinceGrowth; // 1 element

    int _bestCellA = 0;
    int _bestCellB = 0;

    public GrowingNeuralGas( String name, ObjectMap om ) {
        super( name, om );
    }

    public void setup( GrowingNeuralGasConfig c ) {
        _c = c;

        int inputs = c.getNbrInputs();
        int w = c.getWidthCells();
        int h = c.getWidthCells();
        int cells = c.getNbrCells();

        _inputValues = new Data( inputs );
        _cellWeights = new Data( w, h, inputs );
//        _cellWeights.setRandom(); // start with no cells
        _cellErrors = new Data( w, h );
        _cellActivity = new Data( w, h );
        _cellMask = new Data( w, h );
        _cellMask.set( 0.f ); // disable all cells

        _cellStress = new Data( w, h );
        _cellAges = new Data( w, h );
        _edges = new Data( cells, cells );
        _edgesAges = new Data( cells, cells );
        _ageSinceGrowth = new Data( 1 );
    }

    public void reset() {
        _cellMask.set( 0.f ); // disable all cells
    }

    public void call() {
        update();
    }

    public void update() {

        // given current cell mask local, ensure that there are at least 2 cells locally.
        boolean b = addCellPairLazy();
        if ( !b ) {
            return; // no cells available to model any input.
        }

        // Compute error
        ArrayList< Integer > liveCells = createCellList( _c, _cellMask );
        ArrayList< Integer > sparseUnitInput = getSparseUnitInput();
        if ( sparseUnitInput != null ) {
            // Sparse input
            sumSqErrorSparseUnit( _c, liveCells, sparseUnitInput, _cellWeights, _cellErrors );
        }
        else {
            // Dense input
            sumSqError( _c, liveCells, _inputValues, _cellWeights, _cellErrors );
        }

        // Get the top 2 cells, A and B
        int maxRank = 2;
        boolean findMaxima = false; // find minima
        TreeMap< Float, ArrayList< Integer > > ranking = new TreeMap< Float, ArrayList< Integer > >();
        _cellActivity.set( 0.f );
        findBestNCells( _c, _cellMask, _cellErrors, _cellActivity, maxRank, findMaxima, ranking ); // activity is whether ranked
        ArrayList< Integer > bestValues = Ranking.getBestValues( ranking, findMaxima, maxRank );
        if ( bestValues.size() < 2 ) {
            // This can happen when we have a local cell mask; we just need to assign two cells.
            return;
        }

        int bestCellA = bestValues.get( 0 );
        int bestCellB = bestValues.get( 1 );

        _cellAges._values[ bestCellA ] += 1.f;
        _cellAges._values[ bestCellB ] += 1.f;

        // Create the edge A,B if it doesn't already exist:
        updateEdges( bestCellA, bestCellB );

        // remove detached cells (without edges)
        removeDetachedCells();

        // Update learned error of the winning cell A
        // errorA = errorA + distance to input vector
        updateStress( bestCellA );

        // train winner A and its neighbours towards the input
        trainCells( bestCellA );

        // Create new cells where the space is poorly represented (the cells are stressed)
        int ageSinceGrowth = ( int ) _ageSinceGrowth._values[ 0 ];

        if ( ageSinceGrowth >= _c.getGrowthInterval() ) {
            b = addCells();
            if ( b ) {
                ageSinceGrowth = 0;
            }
        }

        ++ageSinceGrowth;

        _ageSinceGrowth._values[ 0 ] = ( float ) ageSinceGrowth;

        _bestCellA = bestCellA;
        _bestCellB = bestCellB;

        //System.out.println( "best: " + _bestCellA + " population: " + _cellMask.sum() + " age since growth: " + _ageSinceGrowth._values[ 0 ] );
    }

    public int getBestCell() {
        return _bestCellA;
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

    public int getCellCount() { // can be local or global cell count

        int w = _c.getWidthCells();
        int h = _c.getHeightCells();

        int cells = w * h;
        int cellCount = 0;

        for ( int cell = 0; cell < cells; ++cell ) {

            float maskValue = _cellMask._values[ cell ];
            if ( maskValue > 0.f ) {
                ++cellCount;
            }
        }

        return cellCount;
    }

    public boolean addCellPairLazy() {

        //find 2 globally available cells, and assign them to the local cell mask.
        // activate 2 cells ONLY
        // an edge will form between them when they are subsequently picked as the best (and only) match.
        int cellCount = getCellCount();

        while ( cellCount < 2 ) {
            Integer freeCell = findFreeCell();
            if ( freeCell == null ) {
                return false; // no free cell
            }

            _cellMask._values[ freeCell ] = 1.f;

            ++cellCount;
        }

        return true;
    }

    public Integer findFreeCell() {
        Integer freeCell = null;

        int w = _c.getWidthCells();
        int h = _c.getHeightCells();
        int cells = w * h;

        for ( int cell = 0; cell < cells; ++cell ) {

            float maskValue = _cellMask._values[ cell ];
            if ( maskValue == 0.f ) {
                freeCell = cell; // pick any available cell
                break; // not a valid cell
            }
        }

        return freeCell;
    }

    public boolean addCells() {

        Integer freeCell = findFreeCell(); // Note: look for globally available cell

        if ( freeCell == null ) {
            //System.err.println( "ERR: no free cell");
            return false; // can't add any cells at the moment
        }

        AbstractPair< Integer, Integer > ap = findStressedCells();

        if ( ap == null ) {
            return false;
        }

        bisectCells( ap._first, ap._second, freeCell );

        return true;
    }

    public AbstractPair< Integer, Integer > findStressedCells() {
        // find the cell with the largest error
        float minStressThreshold = _c.getStressThreshold();

        Integer worstCell = null;
        float worstStress = 0.f;

        int w = _c.getWidthCells();
        int h = _c.getHeightCells();
        int cells = w * h;

        for ( int cell = 0; cell < cells; ++cell ) {

            // Note: look for maximum stress globally.??
            float maskValue = _cellMask._values[ cell ];
            if ( maskValue == 0.f ) {
                continue; // not a valid cell
            }

            float stress = _cellStress._values[ cell ];
            if ( stress >= worstStress ) {
                worstStress = stress;
                worstCell = cell;
            }
        }

        if ( worstStress < minStressThreshold ) {
            //System.err.println( "ERR: not stressed");
            return null;
        }

        // find the worst neighbour of worstCell
        Integer worstCell2 = null;
        float worstStress2 = 0.f;

        for ( int cell2 = 0; cell2 < cells; ++cell2 ) {

            // find an actual neighbour of worstCell
            if ( !areNeighbours( worstCell, cell2 ) ) {
                continue;
            }

            // OK cell2 is a neighbour:
            float stress = _cellStress._values[ cell2 ];
            if ( stress >= worstStress2 ) {
                worstStress2 = stress;
                worstCell2 = cell2;
            }
        }

        if ( worstCell2 == null ) {
            //System.err.println( "ERR: no worst cell #2");
            return null;
        }

        return new AbstractPair< Integer, Integer >( worstCell, worstCell2 );
    }

    public void bisectCells(
            int worstCell,
            int worstCell2,
            int freeCell ) {

        // OK now we have the two cells we want to divide by putting a new cell
        // between them

        // set weights of the new cell as median of the two worst cells
        int inputs = _c.getNbrInputs();

        for ( int i = 0; i < inputs; ++i ) {
            int offsetW = worstCell * inputs + i;
            int offsetW2 = worstCell2 * inputs + i;
            int offsetF = freeCell * inputs + i;

            float weightW = _cellWeights._values[ offsetW ];
            float weightW2 = _cellWeights._values[ offsetW2 ];
            float weightF = ( weightW + weightW2 ) * 0.5f;
            _cellWeights._values[ offsetF ] = weightF;
        }

        // activate the new cell
        _cellMask._values[ freeCell ] = 1.f;
//        cellMaskLocal._values[ freeCell ] = 1.f;

        // Create edges: worst, free; worst2, free;
        // remove edges: worst, worst2
        int offsetWW2 = getEdgeOffset( worstCell, worstCell2 );
        int offsetWF = getEdgeOffset( worstCell, freeCell );
        int offsetW2F = getEdgeOffset( worstCell2, freeCell );

        _edgesAges._values[ offsetWW2 ] = 0.f; // this one was just used
        _edgesAges._values[ offsetWF ] = 0.f; // this one was just used
        _edgesAges._values[ offsetW2F ] = 0.f; // this one was just used

        _edges._values[ offsetWW2 ] = 0.f; // remove the edge
        _edges._values[ offsetWF ] = 1.f; // Create the edge
        _edges._values[ offsetW2F ] = 1.f; // Create the edge

        // reset stress of all cells.
        // Note, this means I mustn't add any new cells until the new stresses
        // have had time to work out
        _cellStress.set( 0.f ); // give it time to accumulate
    }

    public void updateStress( int bestCell ) {
        float cellStressAlpha = 1.f - _c.getStressLearningRate();
        float bestSumSqError = _cellErrors._values[ bestCell ];
        float bestUnitError = ( float ) Math.sqrt( bestSumSqError );
//              bestUnitError /= inputs;
        float stressOld = _cellStress._values[ bestCell ];
        float stressNew = ( float ) Unit.lerp( stressOld, bestUnitError, cellStressAlpha );
        _cellStress._values[ bestCell ] = stressNew;
    }

    public void trainCells( int bestCell ) {

        ArrayList< Integer > sparseUnitInput = getSparseUnitInput();
        HashSet< Integer > hs = new HashSet< Integer >();
        if ( sparseUnitInput != null ) {
            hs.addAll( sparseUnitInput );
        }

        float learningRate = _c.getLearningRate();
        float learningRateNeighbours = _c.getLearningRateNeighbours();

        int inputs = _c.getNbrInputs();
        int w = _c.getWidthCells();
        int h = _c.getHeightCells();
        int cells = w * h;

        for ( int cell = 0; cell < cells; ++cell ) {

            float maskValue = _cellMask._values[ cell ];
            if ( maskValue == 0.f ) {
                continue; // not a valid cell
            }

            float cellLearningRate = learningRateNeighbours;

            if ( cell == bestCell ) {
                cellLearningRate = learningRate;
            }
            else { // not the winner, maybe a neighbour:
                if ( !areNeighbours( bestCell, cell ) ) {
                    continue;
                }
            }

            if ( sparseUnitInput != null ) {
                // Sparse input
                for ( int i = 0; i < inputs; ++i ) {

                    float inputValue = 0.f;
                    if ( hs.contains( i ) ) {
                        inputValue = 1.f;
                    }
//                float inputMaskValue = _inputMask._values[ i ];
//                if( inputMaskValue == 0.f ) {
//                    inputValue = 0.f;
//                }

                    updateWeight( cell, inputs, i, inputValue, cellLearningRate );
                }
            }
            else {
                // Dense input
                for ( int i = 0; i < inputs; ++i ) {

                    float inputValue = _inputValues._values[ i ];
//                float inputMaskValue = _inputMask._values[ i ];
//                if( inputMaskValue == 0.f ) {
//                    inputValue = 0.f;
//                }

                    updateWeight( cell, inputs, i, inputValue, cellLearningRate );
                }
            }
        }
    }

    protected void updateWeight( int cell, int inputs, int i, float inputValue, float cellLearningRate ) {
        int offset = cell * inputs + i;

        float noise = ( float ) ( ( _c._r.nextDouble() - 0.5 ) * _c.getNoiseMagnitude() );
        float weightOld = _cellWeights._values[ offset ];
        float weightNew = weightOld + cellLearningRate * ( inputValue - weightOld ) + noise;
        _cellWeights._values[ offset ] = weightNew;
    }

    public void updateEdges(
            int bestCellA,
            int bestCellB ) {

        int maxEdgeAge = _c.getEdgeMaxAge();
        int w = _c.getWidthCells();
        int h = _c.getHeightCells();
        int cells = w * h;

        int offset = getEdgeOffset( bestCellA, bestCellB );
//        edgeAges.add( 1.f ); only increment ages of neighbours of bestCellA
        _edgesAges._values[ offset ] = 0.f; // this one was just used
        _edges._values[ offset ] = 1.f; // Create the edge

        // now go and prune
        for ( int cell1 = 0; cell1 < cells; ++cell1 ) {
            for ( int cell2 = cell1 + 1; cell2 < cells; ++cell2 ) {
                offset = cell1 * cells + cell2;
                float edge = _edges._values[ offset ];
                if ( edge <= 0.f ) {
                    continue; // not a neighbour
                }

                // is a neighbour:
                _edgesAges._values[ offset ] += 1.f;

                float edgeAge = _edgesAges._values[ offset ];
                if ( edgeAge > maxEdgeAge ) {
                    _edges._values[ offset ] = 0.f; // no longer an edge
                    _edgesAges._values[ offset ] = 0.f; // no point in storing larger ages
                }
            }
        }
    }

    public void removeDetachedCells() {

        int w = _c.getWidthCells();
        int h = _c.getHeightCells();
        int cells = w * h;

        // now go and prune
        for ( int cell1 = 0; cell1 < cells; ++cell1 ) {

            if ( _cellMask._values[ cell1 ] == 0.f ) {
                continue; // already dead
            }

            boolean hasEdge = false;

            for ( int cell2 = 0; cell2 < cells; ++cell2 ) {

                int offset = getEdgeOffset( cell1, cell2 );

                float edge = _edges._values[ offset ];
                if ( edge > 0.f ) {
                    hasEdge = true;
                    break; // this cell is OK
                }
            }

            if ( hasEdge ) {
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

        if ( cellA == cellB ) {
            return;
        }

        int offset = getEdgeOffset( cellA, cellB );

        float value = 0.f;

        if ( isNeighbour ) {
            value = 1.f;
        }

        _edges._values[ offset ] = value;
    }

    public boolean areNeighbours(
            int cellA,
            int cellB ) {

        if ( cellA == cellB ) {
            return false;
        }

        int offset = getEdgeOffset( cellA, cellB );
        float value = _edges._values[ offset ];

        if ( value > 0.f ) {
            return true;
        }

        return false;
    }

    public int getEdgeOffset( int cellA, int cellB ) {
        // the smaller value is used as index.
        int cell1 = cellA;
        int cell2 = cellB;
        if ( cellA > cellB ) {
            cell1 = cellB;
            cell2 = cellA;
        }

        int w = _c.getWidthCells();
        int h = _c.getHeightCells();
        int cells = w * h;
        int offset = cell1 * cells + cell2;
        return offset;
    }
}
