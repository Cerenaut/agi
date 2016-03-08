package io.agi.core.alg;

import io.agi.core.data.Data;
import io.agi.core.data.Data2d;
import io.agi.core.math.RandomInstance;
import io.agi.core.orm.Callback;
import io.agi.core.orm.NamedObject;
import io.agi.core.orm.ObjectMap;
import io.agi.core.ann.unsupervised.DynamicSelfOrganizingMap;
import io.agi.core.ann.unsupervised.DynamicSelfOrganizingMapConfig;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * This class will instantiate a hierarchical generative model.
 *
 * Created by dave on 27/12/15.
 */
public class Region extends NamedObject implements Callback {

    public static final int RECEPTIVE_FIELD_DIMENSIONS = 3;

    public static final String SUFFIX_ORGANIZER = "organizer";
    public static final String SUFFIX_CLASSIFIER = "classifier";
    public static final String SUFFIX_PREDICTOR = "predictor";

    public Data _externalInput; // cell space.
    public Data _surfaceInput; // must be 2d, w * h
    public Data _surfaceActivityNew; // must be 2d, w * h
    public Data _surfaceActivityOld; // must be 2d, w * h
    public Data _surfacePredictionNew; // must be 2d, w * h
    public Data _surfacePredictionOld; // must be 2d, w * h
    public Data _surfacePredictionFP; // must be 2d, w * h
    public Data _surfacePredictionFN; // must be 2d, w * h

    public Data _columnDepth; // z position of every input in the surface.

    public HashMap< Integer, ArrayList< Integer > > _depthColumns; // key: depth. Values: Columns at this depth

    public HashSet< Integer > _ffInputActive = new HashSet< Integer >();
    public HashSet< Integer > _fbInputActive = new HashSet< Integer >();

//    public int _columnWidth;
//    public int _columnHeight;

    public RegionConfig _rc;
    public DynamicSelfOrganizingMap _dsom;
    public RegionFactory _rf;
    public HashMap< Integer, Column > _columns = new HashMap< Integer, Column >();

    public Region( String name, ObjectMap om ) {
        super( name, om );
    }

    public void setup(
            RegionConfig rc,
            RegionFactory rf
/*
            // Column Sizing
//            int columnWidth,
//            int columnHeight,
//            int columnInputs,
//
//            // Region sizing
//            int externalHeightColumns,
//            int internalWidthColumns,
//            int internalHeightColumns,

            // Hierarchy training
            int receptiveFieldsTrainingSamples,
            float receptiveFieldsElasticity,
            float receptiveFieldsLearningRate,
            float inputMaskLearningRate,

            // Column training
            float columnLearningRate,
            float columnLearningRateNeighbours,
            float columnNoiseMagnitude,
            int columnEdgeMaxAge,
            float columnStressLearningRate,
            float columnStressThreshold,
            int columnGrowthInterval ) {*/
    ) {

        _rc = rc;
        _rf = rf;
        //the surface must be rectangular
        //you must add the input as a number of extra column rows.
        //this means the input has a fixed shape.
//        _columnWidth = columnWidth;
//        _columnHeight = columnHeight;
//
//        int surfaceWidthColumns = ( internalWidthColumns );
//        int surfaceHeightColumns = ( internalHeightColumns + externalHeightColumns );
//
//        int surfaceWidth = _columnWidth * surfaceWidthColumns;
//        int surfaceHeight = _columnHeight * surfaceHeightColumns;
//        int surfaceSizeCells = surfaceWidth * surfaceHeight;
//
//        int externalWidth = surfaceWidth;
//        int externalHeight = _columnHeight * ( externalHeightColumns );
        Point externalSizeCells = _rc.getExternalSizeCells();
        Point internalSizeColumns = _rc.getInternalSizeColumns();
        Point externalSizeColumns = _rc.getExternalSizeColumns();
        Point surfaceSizeCells = _rc.getSurfaceSizeCells();
        Point surfaceSizeColumns = _rc.getSurfaceSizeColumns();

        int internalWidthColumns = internalSizeColumns.x;
        int internalHeightColumns = internalSizeColumns.y;

        int externalWidthCells = externalSizeCells.x;
        int externalHeightCells = externalSizeCells.y;

        int surfaceWidthCells = surfaceSizeCells.x;
        int surfaceHeightCells = surfaceSizeCells.y;

        int surfaceWidthColumns = surfaceSizeColumns.x;
        int surfaceHeightColumns = surfaceSizeColumns.y;

        _externalInput = new Data( externalWidthCells, externalHeightCells );
        _columnDepth = new Data( surfaceWidthColumns, surfaceHeightColumns );
        _surfaceInput = new Data( surfaceWidthCells, surfaceHeightCells );
        _surfaceActivityNew = new Data( surfaceWidthCells, surfaceHeightCells );
        _surfaceActivityOld = new Data( surfaceWidthCells, surfaceHeightCells );
        _surfacePredictionNew = new Data( surfaceWidthCells, surfaceHeightCells );
        _surfacePredictionOld = new Data( surfaceWidthCells, surfaceHeightCells );
        _surfacePredictionFP = new Data( surfaceWidthCells, surfaceHeightCells );
        _surfacePredictionFN = new Data( surfaceWidthCells, surfaceHeightCells );

        // Create the module that will learn column receptive fields
        String dsomName = getKey(SUFFIX_ORGANIZER);
        int surfaceDimensions = RECEPTIVE_FIELD_DIMENSIONS; // x, y, z . We might do a x1, y1, x2, y2, z also
        float receptiveFieldsElasticity = rc.getReceptiveFieldsElasticity();
        float receptiveFieldsLearningRate = rc.getReceptiveFieldsLearningRate();

        // only model receptive fields for internal columns
        DynamicSelfOrganizingMapConfig dsomc = new DynamicSelfOrganizingMapConfig();
        dsomc.setup( _om, dsomName, surfaceDimensions, internalWidthColumns, internalHeightColumns, receptiveFieldsElasticity, receptiveFieldsLearningRate );

        _dsom = new DynamicSelfOrganizingMap( dsomName, _om );
        _dsom.setup( dsomc );

        // only create internal columns
//        for( int y = 0; y < internalHeightColumns; ++y ) {
//            for (int x = 0; x < internalWidthColumns; ++x) {
        for( int y = 0; y < surfaceHeightColumns; ++y ) {
            for (int x = 0; x < surfaceWidthColumns; ++x) {

                if( _rc.isExternalColumn( x, y ) ) {
                    continue;
                }

                Column c = _rf.createColumn( this, x, y );
                int offset = getColumnOffset( x, y );
                _columns.put( offset, c );
            }
        }
    }

//    public static Point GetSurfaceSize( int internalWidthColumns, int internalHeightColumns, int externalHeightColumns, int columnWidth, int columnHeight ) {
//        int surfaceWidthColumns = ( internalWidthColumns );
//        int surfaceHeightColumns = ( internalHeightColumns + externalHeightColumns );
//
//        int surfaceWidth = columnWidth * surfaceWidthColumns;
//        int surfaceHeight = columnHeight * surfaceHeightColumns;
//        return new Point( surfaceWidth, surfaceHeight );
//    }
//
//    public int getSurfaceAreaColumns() {
//        Point p = getSurfaceSizeColumns();
//        return p.x * p.y;
//    }
//
//    public Point getSurfaceSizeColumns() {
//        Point p = Data2d.getSizeExplicit(_surfaceInput); // the entire surface @ cellular size
//        p.x = p.x / _columnWidth;
//        p.y = p.y / _columnHeight;
//        return p;
//    }
//
//    public Point getRegionSizeColumns() {
//        Point p = Data2d.getSizeExplicit(_dsom._cellActivity); // the entire surface @ cellular size
//        return p;
//    }
//
//    public int getColumnAreaCells() {
//        Point p = getColumnSizeCells();
//        return p.x * p.y;
//    }
//
//    public Point getColumnSizeCells() {
//        return new Point( _columnWidth, _columnHeight );
//    }
//
//    public Point getSurfaceSizeCells() {
//        return Data2d.getSizeExplicit( _surfaceInput ); // the entire surface @ cellular size
//    }

    public Point getColumnSurfaceOrigin( int xColumn, int yColumn ) {
        Point columnSizeCells = _rc.getColumnSizeCells();

        int xSurface = xColumn * columnSizeCells.x;//_columnWidth;
        int ySurface = yColumn * columnSizeCells.y;//_columnHeight;

//        Point externalInputSize = Data2d.getSize(_externalInput);
//
//        ySurface += externalInputSize.y; // the active columns are offset by this much

        return new Point( xSurface, ySurface );
    }
//
//    public Point getColumnsSurfaceSize() {
//        Point sizeColumns = _rc.getSurfaceSizeColumns();
//        Point columnSizeCells = _rc.getColumnSizeCells();
//
//        int xColumnsSurface = sizeColumns.x * columnSizeCells.x;//_columnWidth;
//        int yColumnsSurface = sizeColumns.y * columnSizeCells.y;//_columnHeight;
//
//        return new Point( xColumnsSurface, yColumnsSurface );
//    }

    /**
     * Returns the column coordinate for any given surface cell coordinate.
     * @param offsetSurface
     * @return
     */
    public Point getSurfaceColumnGivenSurfaceOffset( int offsetSurface ) {
        Point surfaceSize = _rc.getSurfaceSizeCells();
        int xSurface = offsetSurface % surfaceSize.x;
        int ySurface = offsetSurface / surfaceSize.x;
        return getSurfaceColumnGivenSurfaceCell(xSurface, ySurface );
    }

    public Point getSurfaceColumnGivenSurfaceCell( int xSurface, int ySurface ) {
        Point columnSizeCells = _rc.getColumnSizeCells();

        int xColumn = xSurface / columnSizeCells.x;
        int yColumn = ySurface / columnSizeCells.y;
        return new Point( xColumn, yColumn );
    }

    public Point getInternalColumnGivenSurfaceCell( int xSurface, int ySurface ) {
        Point externalInputSize = Data2d.getSize(_externalInput);

        if( ySurface < externalInputSize.y ) { // is it an internal input? ie from a column?
            return null;
        }

        Point columnSizeCells = _rc.getColumnSizeCells();

        //xSurface -= externalInputSize.x;
        ySurface -= externalInputSize.y;

        int xColumn = xSurface / columnSizeCells.x;
        int yColumn = ySurface / columnSizeCells.y;
        return new Point( xColumn, yColumn );
    }

    public int getSurfaceDepth( int offset ) {
        Point xySurface = Data2d.getXY( _surfaceInput._dataSize, offset );
        return getSurfaceDepth(xySurface.x, xySurface.y);
    }

    public int getSurfaceDepth( int xSurface, int ySurface ) {
        Point xyInternalColumn = getInternalColumnGivenSurfaceCell(xSurface, ySurface);

        if( xyInternalColumn == null ) {
            return 0;
        }

        Point columnsSize = _rc.getInternalSizeColumns();
        int c = xyInternalColumn.y * columnsSize.x + xyInternalColumn.x;

if( c < 0 ) {
    int g = 0;
    g++;
}
        int z = (int)_columnDepth._values[ c ]; // at least 1

        return z;
    }

//    public float getDepth() {
//        int columnOffset = Data2d.getOffset(_r._columnDepth._dataSize, _x, _y);
//        float z = _r._columnDepth._values[ columnOffset ];
//        return z;
//    }

    public Data getExternalInput() {
        return _externalInput;
    }

//    public boolean isSurfaceExternal( int xSurface, int ySurface ) {
//        Point xyExternal = _rc.getExternalSizeCells();
//        if( ySurface < xyExternal.y ) {
//            return true;
//        }
//
//        return false;
//    }
//
//    public boolean isColumnExternal( int xColumn, int yColumn ) {
//        Point xyExternal = _rc.getExternalSizeColumns();
//        if( yColumn < xyExternal.y ) {
//            return true;
//        }
//
//        return false;
//    }

    public int getColumnOffset( int x, int y ) {
        Point p = _rc.getSurfaceSizeColumns();
        int offset = y * p.x + x;
        return offset;
    }

    public Point getColumnGivenColumnOffset( int offset ) {
        Point sizeColumns = _rc.getSurfaceSizeColumns();

        int xColumn = offset % sizeColumns.x;
        int yColumn = offset / sizeColumns.x;
        return new Point( xColumn, yColumn );
    }

    public Column getColumn( int c ) {
        return _columns.get( c );
    }

    public Column getColumn( int x, int y ) {
        int offset = getColumnOffset( x, y );
        return _columns.get( offset );
    }

    public ArrayList< Integer > getColumnSurfaceCells( int xColumn, int yColumn ) {
        Point columnSizeCells = _rc.getColumnSizeCells();
        Point surfaceSizeCells = _rc.getSurfaceSizeCells();
//        Point externalSizeColumns = _rc.getExternalSizeColumns();//Data2d.getSize(_externalInput);

        int xRect =   xColumn                           * columnSizeCells.x;
//        int yRect = ( yColumn + externalSizeColumns.y ) * columnSizeCells.y;
        int yRect = ( yColumn  ) * columnSizeCells.y;
        int wRect = columnSizeCells.x;
        int hRect = columnSizeCells.y;

        ArrayList< Integer > indices = Data2d.getIndicesInRect( xRect, yRect, wRect, hRect, surfaceSizeCells.x, surfaceSizeCells.y );

        return indices;
    }

//    public HashSet< Integer > getColumnSurfaceCells( int xColumn, int yColumn ) {
//        HashSet< Integer > columnSurfaceCells = new HashSet< Integer >();
//
//        Point pSurfaceSize = getSurfaceSizeCells();
//        Point pColumnOrigin = getColumnSurfaceOrigin( xColumn, yColumn );
//        Point pColumnSize = getColumnSizeCells();
//
//        for( int y = 0; y < pColumnSize.y; ++y ) {
//            for (int x = 0; x < pColumnSize.x; ++x) {
//                int xCell = pColumnOrigin.x + x;
//                int yCell = pColumnOrigin.y + y;
//
//                int offset = yCell * pSurfaceSize.x + xCell;
//                columnSurfaceCells.add( offset );
//            }
//        }
//
//        return columnSurfaceCells;
//    }

    public void call() {
        update();
    }

    public void update() {
        copyExternalInput(); // .. into the surface
        copyInternalInput(); // .. into the surface
        updateColumnDepths();
        updateColumnReceptiveFields();
        updateHierarchy();
        updateColumnState();
    }

    public void copyExternalInput() {
        // copy input into columnar format.
        Point p = Data2d.getSize(_externalInput);
        Data2d.copy(p.x, p.y, _externalInput, 0, 0, _surfaceInput, 0, 0);
    }

    public void copyInternalInput() {
        // copy internally generated output as an input.
        Point p = Data2d.getSize(_externalInput);
        Point sizeColumnsSurface = _rc.getInternalSizeCells(); //getColumnsSurfaceSize();

        // w, h, from, from xy, to, to xy
        Data2d.copy( sizeColumnsSurface.x, sizeColumnsSurface.y, _surfacePredictionFN, 0, p.y, _surfaceInput, 0, p.y ); // note
    }

//    public int getColumnInputs() {
//        int inputs = _dsom._c._om.GetInteger(_keyColumnInputs);
//        return inputs;
//    }
//
//    public int getReceptiveFieldTrainingSamples() {
//        int samples = _dsom._c._om.GetInteger(_keyReceptiveFieldsTrainingSamples);
//        return samples;
//    }

    public void updateColumnReceptiveFields() {
        // train the DSOM
        HashSet< Integer > hs = _surfaceInput.indicesMoreThan(0.f); // find all the active bits.
        int nbrActiveInput = hs.size();

        if( nbrActiveInput == 0 ) {
            return; // can't train
        }

        Object[] activeInput = hs.toArray();

//        Point p = getSizeColumns();
        Data inputValues = _dsom.getInput();
//        Point externalInputSize = Data2d.getSize(_externalInput);
//        Point columnsSize = getSizeColumns();

        // randomly
        int samples = _rc.getReceptiveFieldsTrainingSamples();

        for( int s = 0; s < samples; ++s ) {

            int sample = RandomInstance.randomInt(nbrActiveInput);

            Integer offset = (Integer)activeInput[ sample ];

            Point p = Data2d.getXY( _surfaceInput._dataSize, offset );

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

        Point internalSizeColumns = _rc.getInternalSizeColumns();
        Point externalSizeColumns = _rc.getExternalSizeColumns();
        Point surfaceSizeColumns = _rc.getSurfaceSizeColumns();
        int surfaceWidthColumns = surfaceSizeColumns.x;
        int surfaceHeightColumns = surfaceSizeColumns.y;

        for( int y = 0; y < surfaceHeightColumns; ++y ) {
            for (int x = 0; x < surfaceWidthColumns; ++x) {

                if( _rc.isExternalColumn( x, y ) ) {
                    continue;
                }

                Point pInternal = _rc.getInternalColumnGivenSurfaceColumn( x, y );
                int xInternal = pInternal.x;
                int yInternal = pInternal.y;

                int c = getColumnOffset( x, y ); //y * p.x + x;
                int cInternal = _rc.getInternalColumnOffset( xInternal, yInternal );//* internalSizeColumns.x + xInternal;
                int offset = cInternal * RECEPTIVE_FIELD_DIMENSIONS +2; // ie z

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

    public void updateHierarchy() {

        // Find these useful sets of cells ONCE
        _ffInputActive = _surfaceInput.indicesMoreThan( 0.f ); // find all the active bits.
        _fbInputActive = _surfaceActivityNew.indicesMoreThan( 0.f ); // find all the active bits.
//        int nbrActiveInput = _ffInputActive.size();
//
//        if( nbrActiveInput == 0 ) {
//            return; // can't train
//        }

        // create transient column object to encapsulate the logic.
        // But we dont want cost of many data structures, or copying data.
//        Column c = _rf.create();//new Column();

        // hierarchy structure is determined in the forward direction
        Point surfaceSizeColumns = _rc.getSurfaceSizeColumns();
        int surfaceWidthColumns = surfaceSizeColumns.x;
        int surfaceHeightColumns = surfaceSizeColumns.y;

        for( int y = 0; y < surfaceHeightColumns; ++y ) {
            for (int x = 0; x < surfaceWidthColumns; ++x) {

                if( _rc.isExternalColumn( x, y ) ) {
                    continue;
                }

                Column c = getColumn(x, y);

                // update the actual column
                c.updateForwardInput( _ffInputActive );
            }
        }

        // ... but feedback inputs are also determined by the hierarchy, and must be updated
        for( int y = 0; y < surfaceHeightColumns; ++y ) {
            for (int x = 0; x < surfaceWidthColumns; ++x) {

                if( _rc.isExternalColumn( x, y ) ) {
                    continue;
                }

                Column c = getColumn(x, y);

                // update the actual column
                c.updateFeedbackInput();
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
        Point surfaceSizeColumns = _rc.getSurfaceSizeColumns();
        int surfaceWidthColumns = surfaceSizeColumns.x;
        int surfaceHeightColumns = surfaceSizeColumns.y;

        for( int y = 0; y < surfaceHeightColumns; ++y ) {
            for (int x = 0; x < surfaceWidthColumns; ++x) {

                if( _rc.isExternalColumn( x, y ) ) {
                    continue;
                }

                Column c = getColumn( x, y );

                // update the actual column
                c.update( _ffInputActive );
            }
        }
    }
}
