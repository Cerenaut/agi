package io.agi.core.alg;

import io.agi.core.data.Data;
import io.agi.core.data.Data2d;
import io.agi.core.math.RandomInstance;
import io.agi.core.orm.NamedObject;
import io.agi.core.orm.ObjectMap;
import io.agi.core.ann.unsupervised.DynamicSelfOrganizingMap;
import io.agi.core.ann.unsupervised.DynamicSelfOrganizingMapConfig;
import io.agi.core.ann.unsupervised.GrowingNeuralGasConfig;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * This class will instantiate a hierarchical generative model.
 *
 * Created by dave on 27/12/15.
 */
public class Region extends NamedObject {

    public static final int RECEPTIVE_FIELD_DIMENSIONS = 3;

    public static final String SUFFIX_HIERARCHY_MODULE = "hierarchy-module";
    public static final String SUFFIX_COLUMN_MODULE = "column-module";

    public String _keyReceptiveFieldsTrainingSamples = "receptive-fields-training-samples";
    public String _keyColumnInputs = "column-inputs";

    public Data _externalInput; // cell space.
    public Data _surfaceInput; // must be 2d, w * h
    public Data _surfaceActivityNew; // must be 2d, w * h
    public Data _surfaceActivityOld; // must be 2d, w * h
    public Data _surfacePredictionNew; // must be 2d, w * h
    public Data _surfacePredictionOld; // must be 2d, w * h
    public Data _surfacePredictionFP; // must be 2d, w * h
    public Data _surfacePredictionFN; // must be 2d, w * h

    public Data _columnDepth; // z position of every input in the surface.

    HashMap< Integer, ArrayList< Integer > > _depthColumns; // key: depth. Values: Columns at this depth

    public int _columnWidth;
    public int _columnHeight;

    public DynamicSelfOrganizingMap _dsom;
    public ColumnFactory _cf;
    public HashMap< Integer, ColumnData > _columnData = new HashMap< Integer, ColumnData >();

    public Region( String name, ObjectMap om ) {
        super( name, om );
    }

    public void setup(
            ColumnFactory cf,

            // Column Sizing
            int columnWidth,
            int columnHeight,
            int columnInputs,

            // Region sizing
            int externalHeightColumns,
            int internalWidthColumns,
            int internalHeightColumns,

            // Hierarchy training
            int receptiveFieldsTrainingSamples,
            float receptiveFieldsElasticity,
            float receptiveFieldsLearningRate,

            // Column training
            float columnLearningRate,
            float columnLearningRateNeighbours,
            float columnNoiseMagnitude,
            int columnEdgeMaxAge,
            float columnStressLearningRate,
            float columnStressThreshold,
            int columnGrowthInterval ) {

        _cf = cf;
        //the surface must be rectangular
        //you must add the input as a number of extra column rows.
        //this means the input has a fixed shape.
        int _columnWidth = columnWidth;
        int _columnHeight = columnHeight;

        int surfaceWidthColumns = _columnWidth * ( internalWidthColumns );
        int surfaceHeightColumns = _columnHeight * ( internalHeightColumns + externalHeightColumns );

        int surfaceWidth = _columnWidth * surfaceWidthColumns;
        int surfaceHeight = _columnHeight * surfaceHeightColumns;
        int surfaceSizeCells = surfaceWidth * surfaceHeight;

        int externalWidth = surfaceWidth;
        int externalHeight = _columnHeight * ( externalHeightColumns );

        _externalInput = new Data( externalWidth, externalHeight );
        _surfaceInput = new Data( surfaceWidth, surfaceHeight );
//        _surfaceDepth = new Data( surfaceWidth, surfaceHeight );

        _columnDepth = new Data( internalWidthColumns, internalHeightColumns );

        // Create the module that will learn column receptive fields
        String dsomName = getKey( SUFFIX_HIERARCHY_MODULE );

        int surfaceDimensions = RECEPTIVE_FIELD_DIMENSIONS; // x, y, z . We might do a x1, y1, x2, y2, z also
        DynamicSelfOrganizingMapConfig dsomc = new DynamicSelfOrganizingMapConfig();
        dsomc.setup( _om, dsomName, surfaceDimensions, internalWidthColumns, internalHeightColumns, receptiveFieldsElasticity, receptiveFieldsLearningRate );

        _dsom = new DynamicSelfOrganizingMap( dsomName, _om );
        _dsom.setup( dsomc );

        _dsom._c._om.put(_keyReceptiveFieldsTrainingSamples, receptiveFieldsTrainingSamples);
        _dsom._c._om.put(_keyColumnInputs, columnInputs);

        // Create the module that will learn column state and do prediction
        String gngName = getKey( SUFFIX_COLUMN_MODULE );
        GrowingNeuralGasConfig gngc = new GrowingNeuralGasConfig();
        gngc.setup(
                _om, gngName, surfaceSizeCells, _columnWidth, _columnHeight,
                columnLearningRate, columnLearningRateNeighbours, columnNoiseMagnitude,
                columnEdgeMaxAge, columnStressLearningRate, columnStressThreshold, columnGrowthInterval );

        int columns = internalWidthColumns * internalHeightColumns;

        Column column = _cf.create();//new Column();

        for( int c = 0; c < columns; ++c ) {
            ColumnData cd = column.createColumnData();
            cd.setup( gngc );
            _columnData.put( c, cd );
        }
    }

    public Point getSurfaceSizeColumns() {
        Point p = Data2d.getSizeExplicit( _surfaceInput ); // the entire surface @ cellular size
        p.x = p.x / _columnWidth;
        p.y = p.y / _columnHeight;
        return p;
    }

    public Point getRegionSizeColumns() {
        Point p = Data2d.getSizeExplicit( _dsom._cellActivity ); // the entire surface @ cellular size
        return p;
    }

    public Point getColumnSizeCells() {
        return new Point( _columnWidth, _columnHeight );
    }

    public Point getSurfaceSizeCells() {
        return Data2d.getSizeExplicit( _surfaceInput ); // the entire surface @ cellular size
    }

    public Point getColumnSurfaceOrigin( int xColumn, int yColumn ) {
        int xSurface = xColumn * _columnWidth;
        int ySurface = yColumn * _columnHeight;

        Point externalInputSize = Data2d.getSize(_externalInput);

        ySurface += externalInputSize.y; // the active columns are offset by this much

        return new Point( xSurface, ySurface );
    }

    public Point getColumnsSurfaceSize() {
        Point sizeColumns = getRegionSizeColumns();

        int xColumnsSurface = sizeColumns.x * _columnWidth;
        int yColumnsSurface = sizeColumns.y * _columnHeight;

        return new Point( xColumnsSurface, yColumnsSurface );
    }

    public Point getColumnGivenSurface( int xSurface, int ySurface ) {
        Point externalInputSize = Data2d.getSize(_externalInput);

        if( ySurface < externalInputSize.y ) { // is it an internal input? ie from a column?
            return null;
        }

        int xColumn = xSurface / _columnWidth;
        int yColumn = ySurface / _columnHeight;
        return new Point( xColumn, yColumn );
    }

    public int getSurfaceDepth( int offset ) {
        Point xySurface = Data2d.getXY( _surfaceInput._d, offset );
        return getSurfaceDepth(xySurface.x, xySurface.y);
    }

    public int getSurfaceDepth( int xSurface, int ySurface ) {
        Point xyColumn = getColumnGivenSurface(xSurface, ySurface);

        if( xyColumn == null ) {
            return 0;
        }

        Point columnsSize = getRegionSizeColumns();
        int c = xyColumn.y * columnsSize.x + xyColumn.x;

        int z = (int)_columnDepth._values[ c ]; // at least 1

        return z;
    }

    public Data getExternalInput() {
        return _externalInput;
    }

    public ObjectMap getObjectMap() {
        return _dsom._c._om;
    }

    public void update() {

        copyExternalInput(); // .. into the surface
        copyInternalInput(); // .. into the surface

        updateColumnDepths();
        updateColumnReceptiveFields();
        updateColumnState();
    }

    public void copyExternalInput() {
        // copy input into columnar format.
        Point p = Data2d.getSize(_externalInput);
        Data2d.copy(p.x, p.y, _externalInput, 0, 0, _surfaceInput, 0, 0);
    }

    public void copyInternalInput() {
        // copy internally generated output as an input.
        Point sizeSurface = getSurfaceSizeCells();
        Point sizeColumnsSurface = getColumnsSurfaceSize();

        // w, h, from, from xy, to, to xy
        Data2d.copy( sizeColumnsSurface.x, sizeColumnsSurface.y, _surfacePredictionFN, 0, sizeSurface.y, _surfaceInput, 0, sizeSurface.y ); // note
    }

    public ColumnData getColumnData( int column ) {
        return _columnData.get( column );
    }

    public int getColumnInputs() {
        int inputs = _dsom._c._om.GetInteger(_keyColumnInputs);
        return inputs;
    }

    public int getReceptiveFieldTrainingSamples() {
        int samples = _dsom._c._om.GetInteger(_keyReceptiveFieldsTrainingSamples);
        return samples;
    }

    public void updateColumnReceptiveFields() {
        // train the DSOM
        HashSet< Integer > hs = _surfaceInput.indicesMoreThan(0.f); // find all the active bits.
        int nbrActiveInput = hs.size();

        if( nbrActiveInput == 0 ) {
            return; // can't train
        }

        Integer[] activeInput = (Integer[])hs.toArray();

//        Point p = getSizeColumns();
        Data inputValues = _dsom.getInput();
//        Point externalInputSize = Data2d.getSize(_externalInput);
//        Point columnsSize = getSizeColumns();

        // randomly
        int samples = getReceptiveFieldTrainingSamples();

        for( int s = 0; s < samples; ++s ) {

            int sample = RandomInstance.randomInt(nbrActiveInput);

            int offset = activeInput[ sample ];

            Point p = Data2d.getXY( _surfaceInput._d, offset );

            float x_i = p.x;
            float y_i = p.y;
            int z_i = getSurfaceDepth( p.x, p.y );

            inputValues._values[ 0 ] = x_i;
            inputValues._values[ 1 ] = y_i;
            inputValues._values[ 2 ] = z_i;

            _dsom.update(); // train the DSOM to look for this value.
        }
    }

    public void updateColumnDepths() {
        _depthColumns = new HashMap< Integer, ArrayList< Integer > >();

        Point p = getRegionSizeColumns();

        for( int y = 0; y < p.y; ++y ) {
            for (int x = 0; x < p.x; ++x) {

                int c = y * p.x + x;
                int offset = c * RECEPTIVE_FIELD_DIMENSIONS +2; // ie z

                //note: weights tend towards target value which is 0,1,2 etc.
                //so 0.99 will be 0 but it is closer to 1.
                //better to make < 0.5 = 0,
                //0.5 <= w < 1.5 = 1 etc
                float w = _dsom._cellWeights._values[ offset ];
//                int z = (int)w; // rounds down.
                int z = Math.round( w ); // rounds to nearest int by adding 0.5 then floor.
                z += 1; // so if w = 0, z of col = 1

                        // add to structure:
                ArrayList< Integer > al = _depthColumns.get( z );
                if( al == null ) {
                    al = new ArrayList<>();
                    _depthColumns.put( z, al );
                }

                al.add( c ); // add this col at this z.

                _columnDepth._values[ c ] = z;
            }
        }
    }

    public void updateColumnState() {

        HashSet< Integer > hs = _surfaceInput.indicesMoreThan( 0.f ); // find all the active bits.
        int nbrActiveInput = hs.size();

        if( nbrActiveInput == 0 ) {
            return; // can't train
        }

        // create transient column object to encapsulate the logic.
        // But we dont want cost of many data structures, or copying data.
        Column c = _cf.create();//new Column();

        Point p = getRegionSizeColumns();

        for( int y = 0; y < p.y; ++y ) {
            for( int x = 0; x < p.x; ++x ) {
                c.setup( this, x, y );

                // update the actual column
                c.update( hs );
            }
        }
    }
}
