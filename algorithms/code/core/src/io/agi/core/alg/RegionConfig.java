package io.agi.core.alg;

import io.agi.core.math.Useful;

/**
 * Created by dave on 26/01/16.
 */
public class RegionConfig {

    // Input size
    int _inputWidth;
    int _inputHeight;

    // Column Sizing
    int _columnWidth;
    int _columnHeight;
    int _columns;

    // Computed:
    int _externalHeightColumns = 0;
    int _internalWidthColumns = 0;
    int _internalHeightColumns = 0;

    /**
     * Use this constructor to create an object that will describe the configuration of the region, given the parameters
     * provided. You specify the constraints - input size - and the resources available per column. You also specify the
     * total computational resource available (the number of columns).
     *
     * The 2D layout of these resources is computed from these constraints.
     *
     * @param inputWidth
     * @param inputHeight
     * @param columnWidth
     * @param columnHeight
     * @param columns
     */
    public RegionConfig( int inputWidth, int inputHeight, int columnWidth, int columnHeight, int columns ) {
        _inputWidth = inputWidth;
        _inputHeight = inputHeight;
        _columnWidth = columnWidth;
        _columnHeight = columnHeight;
        _columns = columns;

        // width cols = inputWidth / columnWidth;
//        int widthColumns = _inputWidth / _columnWidth;
//        if( ( _inputWidth % _columnWidth ) != 0 ) {
//            ++widthColumns;
//        }
        _internalWidthColumns = Useful.GetNbrGroups( _inputWidth, _columnWidth );
        _externalHeightColumns = Useful.GetNbrGroups( _inputHeight, _columnHeight );
        _internalHeightColumns = Useful.GetNbrGroups( columns, _internalWidthColumns );
    }
}
