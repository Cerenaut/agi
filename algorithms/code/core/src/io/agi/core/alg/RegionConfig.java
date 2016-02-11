package io.agi.core.alg;

import io.agi.core.ann.NetworkConfig;
import io.agi.core.math.Useful;
import io.agi.core.orm.ObjectMap;

import java.awt.*;

/**
 * Created by dave on 26/01/16.
 */
public class RegionConfig extends NetworkConfig {

    public String _keyInputWidth = "input-width";
    public String _keyInputHeight = "input-height";

    public String _keyRegionAreaColumns = "region-area-columns";
    public String _keyColumnInputs = "column-inputs";
    public String _keyColumnWidthCells = "column-width-cells";
    public String _keyColumnHeightCells = "column-height-cells";

    public String _keyReceptiveFieldsTrainingSamples = "receptive-fields-training-samples";
    public String _keyReceptiveFieldsElasticity = "receptive-fields-elasticity";
    public String _keyReceptiveFieldsLearningRate = "receptive-fields-learning-rate";
    public String _keyInputColumnsFrequencyLearningRate = "input-columns-frequency-learning-rate";
    public String _keyInputColumnsFrequencyThreshold = "input-columns-frequency-threshold";


    public RegionConfig() {
    }

    /**
     * Use this constructor to create an object that will describe the configuration of the region, given the parameters
     * provided. You specify the constraints - input size - and the resources available per column. You also specify the
     * total computational resource available (the number of columns).
     *
     * The 2D layout of these resources is computed from these constraints.
     *
     * @param inputWidth
     * @param inputHeight
     * @param columnWidthCells
     * @param columnHeightCells
     * @param regionAreaColumns
     * @param inputColumnsFrequencyLearningRate
     * @param inputColumnsFrequencyThreshold
     */
    public void setup(
            ObjectMap om,
            String name,
            int inputWidth,
            int inputHeight,
            int columnInputs,
            int columnWidthCells,
            int columnHeightCells,
            int regionAreaColumns,
            int receptiveFieldsTrainingSamples,
            float receptiveFieldsElasticity,
            float receptiveFieldsLearningRate,
            float inputColumnsFrequencyLearningRate,
            float inputColumnsFrequencyThreshold) {
        super.setup(om, name);

        setInputWidth(inputWidth);
        setInputHeight(inputHeight);
        setColumnInputs(columnInputs);
        setColumnWidthCells(columnWidthCells);
        setColumnHeightCells(columnHeightCells);
        setRegionAreaColumns(regionAreaColumns);
        setReceptiveFieldsTrainingSamples(receptiveFieldsTrainingSamples);
        setReceptiveFieldsElasticity(receptiveFieldsElasticity);
        setReceptiveFieldsLearningRate( receptiveFieldsLearningRate );
        setInputColumnsFrequencyLearningRate(inputColumnsFrequencyLearningRate );
        setInputColumnsFrequencyThreshold( inputColumnsFrequencyThreshold );
    }

    public void copyFrom( NetworkConfig nc, String name ) {
        super.copyFrom(nc, name);

        RegionConfig c = (RegionConfig)nc;

        setInputWidth(c.getInputWidth());
        setInputHeight(c.getInputHeight());
        setColumnInputs(c.getColumnInputs());
        setColumnWidthCells(c.getColumnWidthCells());
        setColumnHeightCells(c.getColumnHeightCells());
        setRegionAreaColumns(c.getRegionAreaColumns());
        setReceptiveFieldsTrainingSamples(c.getReceptiveFieldsTrainingSamples());
        setReceptiveFieldsElasticity(c.getReceptiveFieldsElasticity());
        setReceptiveFieldsLearningRate( c.getReceptiveFieldsLearningRate() );
        setInputColumnsFrequencyLearningRate(c.getInputColumnsFrequencyLearningRate() );
        setInputColumnsFrequencyThreshold( c.getInputColumnsFrequencyThreshold() );
    }

    public Point getSurfaceSizeCells() {
        Point sizeColumns = getSurfaceSizeColumns();
        int columnWidthCells = getColumnWidthCells();
        int columnHeightCells = getColumnHeightCells();
        int surfaceWidth  = sizeColumns.x * columnWidthCells;
        int surfaceHeight = sizeColumns.y * columnHeightCells;
        return new Point( surfaceWidth, surfaceHeight );
    }

    public int getSurfaceAreaCells() {
        Point sizeCells = getSurfaceSizeCells();
        int surfaceArea = sizeCells.x * sizeCells.y;
        return surfaceArea;
    }

    public Point getSurfaceSizeColumns() {
        Point internalSize = getInternalSizeColumns();
        Point externalSize = getExternalSizeColumns();
        return new Point( internalSize.x, internalSize.y + externalSize.y );
    }

//    public int getRegionAreaColumns() {
//        Point sizeColumns = getRegionSizeColumns();
//        return sizeColumns.x * sizeColumns.y;
//    }

    public Point getColumnSizeCells() {
        int columnWidthCells = getColumnWidthCells();
        int columnHeightCells = getColumnHeightCells();
        return new Point( columnWidthCells, columnHeightCells );
    }

    public int getColumnAreaCells() {
        int columnWidthCells = getColumnWidthCells();
        int columnHeightCells = getColumnHeightCells();
        return columnWidthCells * columnHeightCells;
    }

    public Point getInternalColumnGivenSurfaceColumn( int xSurfaceColumn, int ySurfaceColumn ) {
        Point externalSizeColumns = getExternalSizeColumns();

        int xInternal = xSurfaceColumn;
        int yInternal = ySurfaceColumn - externalSizeColumns.y;

        return new Point( xInternal, yInternal );
    }

    public int getInternalColumnOffset( int xInternalColumn, int yInternalColumn ) {
        Point internalSizeColumns = getExternalSizeColumns();
        int cInternal = yInternalColumn * internalSizeColumns.x + xInternalColumn;
        return cInternal;
    }

    public boolean isExternalColumn( int xColumn, int yColumn ) {
        Point externalSizeColumns = getExternalSizeColumns();
        if( yColumn < externalSizeColumns.y ) {
            return true;
        }

        return false;
    }

    public Point getInternalSizeColumns() {
        int inputWidth = getInputWidth();
        int columnWidth = getColumnWidthCells();
        int columns = getRegionAreaColumns();

        int internalWidthColumns = Useful.GetNbrGroups(inputWidth, columnWidth);
        int internalHeightColumns = Useful.GetNbrGroups(columns, internalWidthColumns);

        return new Point( internalWidthColumns, internalHeightColumns );
    }

    public Point getExternalSizeColumns() {
        Point internalSize = getInternalSizeColumns();
        int inputHeight = getInputHeight();
        int columnHeight = getColumnHeightCells();
        int externalWidthColumns = internalSize.x; // same as internal
        int externalHeightColumns = Useful.GetNbrGroups( inputHeight, columnHeight );
        return new Point( externalWidthColumns, externalHeightColumns );
    }

    public Point getExternalSizeCells() {
        Point columnSizeCells = getColumnSizeCells();
        Point externalSizeColumns = getExternalSizeColumns();
        Point externalSizeCells = new Point( externalSizeColumns.x * columnSizeCells.x, externalSizeColumns.y * columnSizeCells.y );
        return externalSizeCells;
    }

    public Point getInternalSizeCells() {
        Point columnSizeCells = getColumnSizeCells();
        Point internalSizeColumns = getInternalSizeColumns();
        Point internalSizeCells = new Point( internalSizeColumns.x * columnSizeCells.x, internalSizeColumns.y * columnSizeCells.y );
        return internalSizeCells;
    }

    public void setInputWidth( int inputWidth ) {
        _om.put( getKey( _keyInputWidth ), inputWidth );
    }

    public void setInputHeight( int inputHeight ) {
        _om.put( getKey( _keyInputHeight ), inputHeight );
    }

    public void setRegionAreaColumns( int columns ) {
        _om.put( getKey( _keyRegionAreaColumns ), columns );
    }

    public void setColumnInputs( int n ) {
        _om.put( getKey( _keyColumnInputs ), n );
    }

    public void setColumnWidthCells( int n ) {
        _om.put( getKey( _keyColumnWidthCells ), n );
    }

    public void setColumnHeightCells( int n ) {
        _om.put( getKey( _keyColumnHeightCells ), n );
    }

    public void setReceptiveFieldsTrainingSamples( int n ) {
        _om.put( getKey( _keyReceptiveFieldsTrainingSamples ), n );
    }

    public void setReceptiveFieldsElasticity( float r ) {
        _om.put( getKey( _keyReceptiveFieldsElasticity ), r );
    }

    public void setReceptiveFieldsLearningRate( float r ) {
        _om.put( getKey( _keyReceptiveFieldsLearningRate ), r );
    }

    public void setInputColumnsFrequencyLearningRate( float r ) {
        _om.put( getKey( _keyInputColumnsFrequencyLearningRate ), r );
    }

    public void setInputColumnsFrequencyThreshold( float r ) {
        _om.put( getKey( _keyInputColumnsFrequencyThreshold ), r );
    }

    public int getInputWidth() {
        Integer i = _om.getInteger( getKey( _keyInputWidth ) );
        return i.intValue();
    }

    public int getInputHeight() {
        Integer i = _om.getInteger( getKey( _keyInputHeight ) );
        return i.intValue();
    }
    public int getRegionAreaColumns() {
        Integer i = _om.getInteger( getKey( _keyRegionAreaColumns ) );
        return i.intValue();
    }

    public int getColumnInputs() {
        Integer i = _om.getInteger( getKey( _keyColumnInputs ) );
        return i.intValue();
    }

    public int getColumnWidthCells() {
        Integer i = _om.getInteger( getKey( _keyColumnWidthCells ) );
        return i.intValue();
    }

    public int getColumnHeightCells() {
        Integer i = _om.getInteger( getKey( _keyColumnHeightCells ) );
        return i.intValue();
    }

    public int getReceptiveFieldsTrainingSamples() {
        Integer i = _om.getInteger(getKey(_keyReceptiveFieldsTrainingSamples));
        return i.intValue();
    }

    public float getReceptiveFieldsElasticity() {
        Float r = _om.getFloat(getKey(_keyReceptiveFieldsElasticity));
        return r.floatValue();
    }

    public float getReceptiveFieldsLearningRate() {
        Float r = _om.getFloat(getKey(_keyReceptiveFieldsLearningRate));
        return r.floatValue();
    }

    public float getInputColumnsFrequencyLearningRate() {
        Float r = _om.getFloat(getKey(_keyInputColumnsFrequencyLearningRate));
        return r.floatValue();
    }

    public float getInputColumnsFrequencyThreshold() {
        Float r = _om.getFloat(getKey(_keyInputColumnsFrequencyThreshold ) );
        return r.floatValue();
    }

}
