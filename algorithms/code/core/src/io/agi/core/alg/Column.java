package io.agi.core.alg;

import io.agi.core.data.Data2d;
import io.agi.core.data.Ranking;
import io.agi.core.math.Geometry;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by dave on 28/12/15.
 */
public class Column {

    Region _r;
    int _x = 0;
    int _y = 0;

    public ColumnData createColumnData() {
        return new ColumnData();
    }

    public float getDepth() {
        int columnOffset = Data2d.getOffset( _r._columnDepth._d, _x, _y );

        float z = _r._columnDepth._values[ columnOffset ];
        return z;
    }

    public int getColumn() {
        Point p = _r.getRegionSizeColumns();
        int c = _y * p.x + _x;
        return c;
    }

    public float[] getReceptiveField() {
        float[] rf = new float[ Region.RECEPTIVE_FIELD_DIMENSIONS ];

        //Point p = _r.getRegionSizeColumns();

        int c = getColumn();//_y * p.x + _x;
        int offset = c * Region.RECEPTIVE_FIELD_DIMENSIONS;

        rf[ 0 ] = _r._dsom._cellWeights._values[ offset +0 ];
        rf[ 1 ] = _r._dsom._cellWeights._values[ offset +1 ];
        rf[ 2 ] = _r._dsom._cellWeights._values[ offset +2 ];

        return rf;
    }

    public void setup( Region r, int xColumn, int yColumn ) {
        _r = r;
        _x = xColumn;
        _y = yColumn;
    }

    public void update( HashSet< Integer > activeInput ) {
        // find the closest N active input to col.
        // Do this with ranking.
        Ranking r = new Ranking();

        int columnInputs = _r.getColumnInputs();
        int columnOffset = Data2d.getOffset( _r._columnDepth._d, _x, _y );

        float z_c = _r._columnDepth._values[ columnOffset ];
        float z_t = z_c -1; // i.e. a col with z_c=1 would have a z_t of 0

        float[] rf = getReceptiveField();

        for( Integer i : activeInput ) {
            float z_i = _r.getSurfaceDepth( i );

            if( z_i != z_t ) {
                continue; // can't use this as an input for this col.
            }

            Point p = Data2d.getXY( _r._surfaceInput._d, i);

            float rf_x = rf[ 0 ];
            float rf_y = rf[ 1 ];
//            float rf_z = rf[ 2 ];

            float d = Geometry.distanceEuclidean2d( (float)p.getX(), (float)p.getY(), rf_x, rf_y );
            Ranking.add( r._ranking, d, i ); // add input i with quality d (distance) to r.
        }

        boolean max = false; // ie min
        int maxRank = columnInputs;
        //Ranking.truncate( r._ranking, _inputsPerColumn, max );
        ArrayList< Integer > columnActiveInput = Ranking.getBestValues( r._ranking, max, maxRank ); // ok now we got the current set of inputs for the column

        // We now have a list of the inputs at the right Z for the Column and close to it's RF X,Y
        updateWithColumnInputs(columnActiveInput);
    }

    public void updateWithColumnInputs( ArrayList< Integer > activeInput ) {

        HashSet<Integer> activeCells = findActiveCells(activeInput);
        HashSet<Integer> predictedCells = findPredictedCells( activeCells ); // update the prediction.

        updateWithCells( activeCells, predictedCells );
    }

    public void updateWithCells( HashSet< Integer > activeCells, HashSet< Integer > predictedCells ) {
        Point surfaceSizeCells = _r.getSurfaceSizeCells();
        Point columnSizeCells = _r.getColumnSizeCells();
        Point surfaceOriginCells = _r.getColumnSurfaceOrigin(_x, _y);

        for( int y = 0; y < columnSizeCells.y; ++y ) {
            for (int x = 0; x < columnSizeCells.x; ++x) {

                // Rules:
                // Prediction must START and CONTINUE until cell becomes ACTIVE.
                // If prediction ends too early - FP, then when cell active, FN.
                // If prediction ends too late -
                // If the cell becomes active after the prediction, OK
                // If the cell doesnt become active during prediction, Bad.
                int xSurface = surfaceOriginCells.x + x;
                int ySurface = surfaceOriginCells.y + y;
                int cell = y * columnSizeCells.x + x;
                int cellSurfaceOffset = ySurface * surfaceSizeCells.x + xSurface;

                // Update prediction variables
                float predictionOld = _r._surfacePredictionNew._values[ cellSurfaceOffset ];
                float predictionNew = 0.f;
                if( predictedCells.contains( cell ) ) {
                    predictionNew = 1.f;
                }

                _r._surfacePredictionOld._values[ cellSurfaceOffset ] = predictionOld; // copied from new
                _r._surfacePredictionNew._values[ cellSurfaceOffset ] = predictionNew;

                // Update activity variables
                float activeOld = _r._surfaceActivityNew._values[ cellSurfaceOffset ];
                float activeNew = 0.f;
                if( activeCells.contains( cell ) ) {
                    activeNew = 1.f;
                }

                _r._surfaceActivityOld._values[ cellSurfaceOffset ] = activeOld;
                _r._surfaceActivityNew._values[ cellSurfaceOffset ] = activeNew;

                // for stability should I hold the errors?
                // FP error holds until next prediction. (i.e. til P 0 --> 1)
                // FN error holds while cell is active until ends.
                float errorFP = _r._surfacePredictionFP._values[ cellSurfaceOffset ];
                float errorFN = _r._surfacePredictionFN._values[ cellSurfaceOffset ];

                // Activity starts
                if( ( activeNew == 1.f ) && ( activeOld == 0.f ) ) {
                    if( predictionOld == 0.f ) { // the last prediction before it became active was not present.
                        errorFN = 1.f;
                    }
                }

                // Activity stops
                if( ( activeNew == 0.f ) && ( activeOld == 1.f ) ) {
                    errorFN = 0.f; // end FN error, if any
                }

                // Prediction stops
                // CHANGE: PREDICTION 1 --> 0
                if( ( predictionOld == 1.f) && ( predictionNew == 0.f ) ) {
                    // may be active for many steps.
                    // this window is for turning prediction off.
                    // prediction can be terminated at any time
                    if( activeNew == 0.f ) { // was predicted before becoming active
                        errorFP = 1.f;
                    }
                }

                // Prediction starts:
                if( ( predictionOld == 0.f) && ( predictionNew == 1.f ) ) {
                    errorFP = 0.f; // end FP error, if any
                }

                // store updated error state
                _r._surfacePredictionFP._values[ cellSurfaceOffset ] = errorFP;
                _r._surfacePredictionFN._values[ cellSurfaceOffset ] = errorFN;
            }
        }
    }

    public HashSet< Integer > findActiveCells( ArrayList< Integer > activeInput ) {

        int column = getColumn();
        ColumnData cd = _r.getColumnData( column );

        // have a column-sized GNG and copy the weights into it
        cd._gng.setSparseUnitInput( activeInput );
        cd._gng.update(); // trains with this sparse input.

        int bestCell = cd._gng.getBestCell();

        HashSet< Integer > hs = new HashSet< Integer >();
        hs.add( bestCell );

        return hs;
    }

    public HashSet< Integer > findPredictedCells( HashSet< Integer > activeCells ) {

        // We get a stream of activeCells. This should be used for training as well as input.
        // The stream may not change each step.
        // The objective is to predict next cells up to the time they become active.
        // Should not predict too many (self enforced only)
        //
        // have a column-sized predictor and copy the weights and inputs to it.
        // Options: Hebbian learning, MLP

        return null;
    }
/*
    float _x = 0;
    float _y = 0;

// so each dendrite has a short list of weights above background level
// the cell can become active using different subsets of these, which may or may not overlap
// The cells must compete to
// Cells should learn to use a greater number of bits simultaneously
//
//inputs is constant but RF size varies constantly
//so the total number of inputs is variable and unknowable
//we can assume it is some function of inputs per step
//
//Say we have
//
//Inputs 1 2 3 4 5 6 7 8 9
//t1     1 1 1
//t2           1 1 1
//t3                 1 1 1
//t4       1 1 1 1
//
//    http://www.demogng.de/
//Cells are either synapsed or not, for overlap purposes.
//But we could have continuous overlap...
//    https://en.wikipedia.org/wiki/Neural_gas
//
//HTM is like Hard Competitive Learning http://www.demogng.de/JavaPaper/node9.html#SECTION00500000000000000000
//If there are insufficient cells, then each cell will represent a number of input.
//
//Boosting is because we dont have a neighbourhood function. Need a way to bring cells back.
//Neural gas adapts all cells without a topology
//            Based on a ranking
//
//So GNG it is, for online ness.
//Or DSOM
//    http://www.labri.fr/perso/nrougier/coding/article/article.html
//But DSOM applies a topology, which is too restrictive and of no benefit
//
//The difference between overlap and distance is that missing bits arent counted
//So say we get each col to do winner take all.
//But each col only looks at a subset of the bits

    int _columnSizeCells = 6; // eg 6x6 = 36 cells
    int _inputsPerColumn = 16; // each time, not total
    int _inputsPerCell = 20; // upper bound
    float _overlapThreshold = 0.5f;
    float _learningRate = 0.1f;
    float _weightMin = 0.1f;

    // computed data
    Data _inputDendriteOverlap; // cells x 1
    Data _inputDendriteOverlapBoosted; // cells x 1
    Data _inputDendriteFrequency; // cells x 1
    Data _inputDendriteIndices; // cells x _inputsPerCell
    Data _inputDendriteWeights; // cells x _inputsPerCell
    Data _inputDendriteActivity; // cells x 1
    Data _cellActivity; // cells x 1
    Data _cellOutput; // cells x 1
    Data _cellPrediction; // cells x 1

    public void update( float rfx, float rfy, float rfz, HashMap< Integer, Data > zMaskedInputValues ) {

        // Get the relevant input for this column. This is precomputed to save work as there are many cols at each z.
        int z = (int)rfz;
        Data maskedInputValues = zMaskedInputValues.get( z );
        HashSet< Integer > activeInput = maskedInputValues.indicesMoreThan( 0.f ); // find all the active input bits.

        // find the closest N active input to col.
        // Do this with ranking.
        Ranking r = new Ranking();

        for( Integer i : activeInput ) {
            Point p = Data2d.getXY( maskedInputValues._d, i );
            float d = Geometry.distanceEuclidean2d( (float)p.getX(), (float)p.getY(), rfx, rfy );
            Ranking.add( r._ranking, d, i ); // add input i with quality d (distance) to r.
        }

        boolean max = false; // ie min
        int maxRank = _inputsPerColumn;
        //Ranking.truncate( r._ranking, _inputsPerColumn, max );
        ArrayList< Integer > columnActiveInput = Ranking.getBestValues( r._ranking, max, maxRank );

        // ok now calculate the overlap of each cell with the active input
        int cells = _columnSizeCells * _columnSizeCells;

        Dendrite d = new Dendrite( _inputDendriteIndices, _inputDendriteWeights ); // temporary object to encapsulate the data.

        for( int c = 0; c < cells; ++c ) {
            float overlap = d.findOverlap(c, columnActiveInput);
            _inputDendriteOverlap._values[ c ] = overlap;
            float f = _inputDendriteFrequency._values[ c ];
            float boosted = getBoostedOverlap(overlap, f);
            _inputDendriteOverlapBoosted._values[ c ] = boosted;

            float activity = 0.f;
diff combos of cells
            if( boosted > _overlapThreshold ) {
                activity = 1.f;
            }

            _inputDendriteActivity._values[ c ] = activity;

            float cellActivity = activity; // if input dendrite is active, cell is active. Regardless of context.

            // compute output based on whether cell was predicted before becoming active
            float cellOutput = 0.f;
            float cellPredicted = _cellPrediction._values[ c ];
            if( cellPredicted == 0.f && cellActivity == 1.f ) {
                cellOutput = 1.f; // false-negative error (active but not predicted)
            }

            _cellActivity._values[ c ] = cellActivity;
            _cellOutput  ._values[ c ] = cellOutput;

            // training and update:
            // frequency
            // dendrite weights


        }
    }*/

/*C5: input is from C2/3
prediction is from many
prediction starts first.
then can be inhibited (doesnt affect learning)
when disinhibited, and input active, and predicted, it becomes active.

C6: Watches patterns of C5 and C6 lower activity.    */

//    public float getBoostedOverlap( float overlap, float frequency ) {
//
//    }
        // try to make the column have no external references. i.e. it is self contained and doesnt know about the complexities of the meta-algorithm

        // So: Growing neural gas. Winner take all.

        // Plus predictive coding. This builds our hierarchy.

/*        What about subjective and executive?

        subj:
                in particular objective context, output is lower C6. (exec)

        exec:
                observe pattern of output from subjective, replay it.
*/

}
