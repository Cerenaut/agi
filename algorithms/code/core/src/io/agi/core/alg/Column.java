package io.agi.core.alg;

import io.agi.core.ann.supervised.FeedForwardNetwork;
import io.agi.core.ann.unsupervised.GrowingNeuralGas;
import io.agi.core.data.Data;
import io.agi.core.data.Data2d;
import io.agi.core.data.Ranking;
import io.agi.core.math.Geometry;
import io.agi.core.math.Unit;
import io.agi.core.orm.Keys;
import io.agi.core.orm.NamedObject;
import io.agi.core.orm.ObjectMap;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by dave on 28/12/15.
 */
public class Column extends NamedObject {

//    public String _keyInputColumnsLearningRate = "input-columns-learning-rate";
//    public String _keyInputColumnsFrequencyThreshold = "input-columns-frequency-threshold";

    public Region _r;
    public int _x = 0;
    public int _y = 0;
    public GrowingNeuralGas _gng;
    public FeedForwardNetwork _ffn;

    public Data _inputColumnFrequency;
    public Data _inputColumnMask;

    // transient: redefined every update, used for efficiency only
    public HashSet< Integer > _ffActiveInputCells = new HashSet< Integer >();  // active cells that are within RF
    public HashSet< Integer > _ffInputColumns = new HashSet< Integer >();  // columns that are usually having cells within RF
    public HashSet< Integer > _fbInputColumns = new HashSet< Integer >();  // columns that are usually having cells within RF
    public HashSet< Integer > _fbInputCells = new HashSet< Integer >();  // cells within cols defined by hierarchy rules, given ff input cols.
    public HashSet< Integer > _fbActiveInputCells = new HashSet<Integer>();

    public static String getName( String parentName, int x, int y ) {
        String suffix = String.valueOf( x ) + "," + String.valueOf( y );
        String name = Keys.concatenate(parentName, suffix);
        return name;
    }

    public Column( String name, ObjectMap om ) {
        super( name, om );
    }

    public int getColumnOffset() {
        return _r.getColumnOffset( _x, _y );
    }

    public float[] getReceptiveField() {
        float[] rf = new float[ Region.RECEPTIVE_FIELD_DIMENSIONS ];

        //Point p = _r.getRegionSizeColumns();
        Point pInternal = _r._rc.getInternalColumnGivenSurfaceColumn( _x, _y );
        int xInternal = pInternal.x;
        int yInternal = pInternal.y;

        //int c = getColumnOffset( x, y ); //y * p.x + x;
        int cInternal = _r._rc.getInternalColumnOffset(xInternal, yInternal);//* internalSizeColumns.x + xInternal;

        int offset = cInternal * Region.RECEPTIVE_FIELD_DIMENSIONS;

        rf[ 0 ] = _r._dsom._cellWeights._values[ offset +0 ];
        rf[ 1 ] = _r._dsom._cellWeights._values[ offset +1 ];
        rf[ 2 ] = _r._dsom._cellWeights._values[ offset +2 ];

        return rf;
    }

    public void setup(
            Region r,
            int xColumn,
            int yColumn ) {
        _r = r;
        _x = xColumn;
        _y = yColumn;

//        setInputColumnsLearningRate(inputColumnsLearningRate);
//        setInputColumnsFrequencyThreshold(inputColumnsFrequencyThreshold );

        Point p = r._rc.getSurfaceSizeColumns();
        _inputColumnFrequency = new Data( p.x, p.y );
        _inputColumnMask = new Data( p.x, p.y );

        _gng = r._rf.createClassifier( r, _x, _y );
        _ffn = r._rf.createPredictor(r, _x, _y);
//        _gng = new GrowingNeuralGas( classifierConfig._name, classifierConfig._om );
//        _gng.setup( classifierConfig );

//        _ffn = new FeedForwardNetwork( predictorConfig._name, predictorConfig._om );
//        _ffn.setup( predictorConfig, predictorFactory);
    }

    public String getClassifierName() {
        return Keys.concatenate( getName(), "classifier" );
    }
    public String getPredictorName() {
        return Keys.concatenate( getName(), "predictor" );
    }

//    public void setInputColumnsLearningRate( float learningRate ) {
//        _om.put(getKey(_keyInputColumnsLearningRate), learningRate);
//    }
//
//    public Float getInputColumnsLearningRate() {
//        return (Float)_om.get(getKey(_keyInputColumnsLearningRate) );
//    }
//
//    public void setInputColumnsFrequencyThreshold( float frequencyThreshold ) {
//        _om.put(getKey(_keyInputColumnsLearningRate), frequencyThreshold );
//    }
//
//    public Float getInputColumnsFrequencyThreshold() {
//        return (Float)_om.get(getKey(_keyInputColumnsFrequencyThreshold) );
//    }

    public void updateForwardInput( HashSet< Integer > surfaceActiveInput ) {
        // find the closest N active input to col.
        // Do this with ranking.
        int columnInputs = _r._rc.getColumnInputs();
        int columnOffset = Data2d.getOffset(_r._columnDepth._dataSize, _x, _y);

        float z_c = _r._columnDepth._values[columnOffset];
        float z_t = z_c - 1; // i.e. a col with z_c=1 would have a z_t of 0

        float[] rf = getReceptiveField();

        Ranking r = new Ranking();

        for (Integer i : surfaceActiveInput) {
            float z_i = _r.getSurfaceDepth(i);

            if (z_i != z_t) {
                continue; // can't use this as an input for this col.
            }

            Point p = Data2d.getXY(_r._surfaceInput._dataSize, i);

            float rf_x = rf[0];
            float rf_y = rf[1];
//            float rf_z = rf[ 2 ];

            float d = Geometry.distanceEuclidean2d((float) p.getX(), (float) p.getY(), rf_x, rf_y);
            Ranking.add(r._ranking, d, i); // add input i with quality d (distance) to r.
        }

        boolean max = false; // ie min
        int maxRank = columnInputs;
        //Ranking.truncate( r._ranking, _inputsPerColumn, max );
        ArrayList<Integer> columnActiveInput = Ranking.getBestValues(r._ranking, max, maxRank); // ok now we got the current set of inputs for the column

        _ffActiveInputCells.clear();
        _ffActiveInputCells.addAll(columnActiveInput);

        // now update the mask, and the mask active input:
        updateForwardInputColumns();

        // We now have a list of the inputs at the right Z for the Column and close to it's RF X,Y
//        updateWithColumnInputs(columnActiveInput);
    }

    public void updateForwardInputColumns() {

        // 1. update the frequency of all inputs to this column
        float learningRate = _r._rc.getInputColumnsFrequencyLearningRate();
        float threshold = _r._rc.getInputColumnsFrequencyThreshold();

        Ranking r = new Ranking();

        Point sizeColumns = _r._rc.getSurfaceSizeColumns();
        int columns = sizeColumns.x * sizeColumns.y;
        Data d = new Data( columns );

        for( Integer i : _ffActiveInputCells) {
            Point c_xy = _r.getSurfaceColumnGivenSurfaceOffset( i ); // surface, ie not an internal col
            int column = _r.getColumnOffset( c_xy.x, c_xy.y ); // surface, not internal
            d._values[ column ] = 1.f; // this column provided input
        }

        _ffInputColumns.clear();

        for( int c = 0; c < columns; ++c ) {
            float x = d._values[ c ];
            float w1 = _inputColumnFrequency._values[ c ];
            float w2 = Unit.lerp( x, w1, learningRate );
            _inputColumnFrequency._values[ c ] = w2;

            float t = 0.f;
            if( w2 >= threshold ) {
                t = 1.f;
                _ffInputColumns.add( c ); // surface
            }

            _inputColumnMask._values[ c ]  = t;
        }

        // 2. update the mask   <-- what is the point of the mask? ans: it defines the hierarchy.
        // Since the GNG weights take time to learn, there's no harm in training them on non-mask items.
        // find the N closest cols *ON AVERAGE OVER TIME*.
//        int columnInputs = _r.getColumnInputs() * _r.getColumnAreaCells();
    }

    public boolean hasInputColumn( int c ) {
        return _ffInputColumns.contains( c );
    }

    public void updateFeedbackInput() {

        // Use a series of rules to find the cells that can be used as feedback input.
        _fbInputColumns.clear();
        _fbInputCells.clear();

        // 1. Find all columns C that have us within their ff inputmask.
        // 2. All the cells in C are the fb input mask for this column.
        // [EDIT: We could define it more widely.. e.g. the siblings of this column]
        int offsetThis = getColumnOffset();
        Point sizeColumns = _r._rc.getSurfaceSizeColumns();
        int columns = sizeColumns.x * sizeColumns.y;

        for( int offsetThat = 0; offsetThat < columns; ++offsetThat ) {
            Column that = _r.getColumn( offsetThat );
            if( that == null ) {
                continue; // external column
            }

            if( that.hasInputColumn( offsetThis ) ) {
                Point xyColumnThat = _r.getColumnGivenColumnOffset(offsetThat);
                addFeedbackInputColumn(xyColumnThat.x, xyColumnThat.y );
            }
        }

        // Add our own cells
        addFeedbackInputColumn( _x, _y ); // note: This is included in
    }

    public void addFeedbackInputColumn( int xColumn, int yColumn ) {
        int column = getColumnOffset();
        _fbInputColumns.add( column );
        ArrayList< Integer > cells = _r.getColumnSurfaceCells(xColumn, yColumn);
        _fbInputCells.addAll(cells);
    }

    public HashSet< Integer > findActiveCells( HashSet< Integer > allActiveCells, ArrayList< Integer > validCells ) {
        // now find the active cells of the input cells.
        HashSet< Integer > activeCells = new HashSet< Integer >();

        for( Integer validCell : validCells ) {
            if( allActiveCells.contains( validCell ) ) {
                activeCells.add( validCell );
            }
        }

        return activeCells;
    }

    public void update( HashSet< Integer > surfaceActiveInput ) {

        // classify the input
        ArrayList< Integer > localCells = _r.getColumnSurfaceCells(_x, _y); // all cells in col.

        HashSet< Integer > oldActiveLocalCells = findActiveCells( surfaceActiveInput, localCells );
        HashSet< Integer > newActiveLocalCells = findMatchingCells(_ffActiveInputCells);

        boolean activeLocalCellsChanged = !oldActiveLocalCells.equals( newActiveLocalCells );

        if( activeLocalCellsChanged ) {
            trainPrediction(newActiveLocalCells); // needs: FF to generate prediction, or everything serialized from last time
        }

        _fbActiveInputCells = findActiveCells( surfaceActiveInput, new ArrayList< Integer >( _fbInputCells ) );
        _fbActiveInputCells.addAll( newActiveLocalCells );

        HashSet<Integer> predictedLocalCells = findPredictedCells( _fbActiveInputCells ); // update the prediction.

//        if( activeLocalCellsChanged ) {
//            /*
//             * Takes:
//             * - OldPrediction
//             * - NewPrediction
//             */
        updateSurface( newActiveLocalCells, predictedLocalCells, activeLocalCellsChanged ); // Needs: New prediction, New active local cells.
//        }
    }

    public void updateSurface( HashSet< Integer > activeCells, HashSet< Integer > predictedCells, boolean activeLocalCellsChanged ) {
        Point surfaceSizeCells = _r._rc.getSurfaceSizeCells();
        Point columnSizeCells = _r._rc.getColumnSizeCells();
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
                if( !activeLocalCellsChanged ) {
                    continue;
                }

                // update regional output
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

    public HashSet< Integer > findMatchingCells( HashSet< Integer > activeInput ) {

//        int column = getColumn();
//        ColumnData cd = _r.getColumnData( column );

        // have a column-sized GNG and copy the weights into it
        ArrayList< Integer > al = new ArrayList< Integer >();
        al.addAll( activeInput );
        _gng.setSparseUnitInput(al);
        _gng.update(); // trains with this sparse input.

        int bestCell = _gng.getBestCell();

        HashSet< Integer > hs = new HashSet< Integer >();
        hs.add(bestCell);

        return hs;
    }

    public HashSet< Integer > findPredictedCells( HashSet< Integer > fbActiveInputCells ) {

        // We get a stream of activeCells. This should be used for training as well as input.
        // The stream may not change each step.
        // The objective is to predict next cells up to the time they become active.
        // Should not predict too many (self enforced only)
        //
        // have a column-sized predictor and copy the weights and inputs to it.
        // Options: Hebbian learning, MLP
        //
        // Note that the prediction input doesn't affect the output, except that it may improve prediction. It doesnt
        // change the winning cell. So we can in fact use any cell to predict without consequences, except that prediction
        // improves or worsens and we have too many inputs.
        // So one way to do it would be to provide all cells at level +1 ... +N to predict. Or all cells with reciprocal relations.
        // That will scale well...
        Data input = _ffn.getInput();

        input.set(0.f);

        for( Integer i : fbActiveInputCells ) {
            input._values[ i ] = 1.f;
        }

        _ffn.feedForward(); // generate a prediction

        Data output = _ffn.getOutput();

        float max = 0.f;
        int maxOffset = 0;

        int columnArea = output.getSize();

        for( int i = 0; i < columnArea; ++i ) {

            float value = output._values[ i ];

            if( value >= max ) {
                max = value;
                maxOffset = i;
            }
        }

        // convert column to surface. TODO make this a method, it's really complex
        Point columnSize = _r._rc.getColumnSizeCells();
        Point surfaceSize = _r._rc.getSurfaceSizeCells();

        int yColumn = maxOffset / columnSize.x;
        int xColumn = maxOffset % columnSize.x;

//        Point columnOrigin = _r.getInternalColumnSurfaceOrigin( _x, _y );
        Point columnOrigin = _r.getColumnSurfaceOrigin( _x, _y );

        int xSurface = columnOrigin.x + xColumn;
        int ySurface = columnOrigin.y + yColumn;

        int surfaceOffset = ySurface * surfaceSize.x + xSurface;

        HashSet< Integer > activeOutput = new HashSet< Integer >();
        activeOutput.add( surfaceOffset );
        return activeOutput;
    }

    public void trainPrediction( HashSet< Integer > activeLocalCells ) {
        Data ideal = _ffn.getIdeal();

        ideal.set(0.f);

        for( Integer i : activeLocalCells ) {
            ideal._values[ i ] = 1.f;
        }

        _ffn.feedBackward(); // train based on a transition
    }

}
