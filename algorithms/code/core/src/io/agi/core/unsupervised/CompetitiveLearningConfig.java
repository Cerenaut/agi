package io.agi.core.unsupervised;

import io.agi.core.data.Data2d;
import io.agi.core.orm.ObjectMap;

/**
 * Parameters are stored as an ObjectMap but this provides an interface to access them.
 * Created by dave on 29/12/15.
 */
public class CompetitiveLearningConfig {

    public ObjectMap _om;
    public String _keyInputs = "i";
    public String _keyWidthCells = "w";
    public String _keyHeightCells = "h";

    public CompetitiveLearningConfig() {
    }

    public void setup( ObjectMap om, int inputs, int w, int h ) {
        _om = om;
        setNbrInputs( inputs );
        setWidthCells( w );
        setHeightCells( h );
    }

    public void setNbrInputs( int inputs ) {
        _om.put( _keyInputs, inputs );
    }

    public void setWidthCells( int w ) {
        _om.put( _keyWidthCells, w );
    }

    public void setHeightCells( int h ) {
        _om.put( _keyHeightCells, h );
    }

    public int getNbrInputs() {
        Integer i = _om.GetInteger( _keyInputs );
        return i.intValue();
    }

    public int getWidthCells() {
        Integer w = _om.GetInteger( _keyWidthCells );
        return w.intValue();
    }

    public int getHeightCells() {
        Integer h = _om.GetInteger( _keyHeightCells );
        return h.intValue();
    }

    public int getNbrCells() {
        Integer w = _om.GetInteger( _keyWidthCells );
        Integer h = _om.GetInteger( _keyHeightCells );
        return w * h;
    }

    public int getCell( int cellX, int cellY ) {
        Integer w = _om.GetInteger( _keyWidthCells );
        return Data2d.getOffset( w, cellX, cellY );
    }

    public int getCellX( CompetitiveLearningConfig c, int cell ) {
        Integer w = _om.GetInteger( _keyWidthCells );
        return Data2d.getX( w, cell);
    }
    public int getCellY( CompetitiveLearningConfig c, int cell ) {
        Integer w = _om.GetInteger( _keyWidthCells );
        return Data2d.getY( w, cell );
    }

}
