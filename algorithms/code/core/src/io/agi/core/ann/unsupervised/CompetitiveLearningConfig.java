package io.agi.core.ann.unsupervised;

import io.agi.core.ann.NetworkConfig;
import io.agi.core.data.Data2d;
import io.agi.core.orm.ObjectMap;

/**
 * Parameters are stored as an ObjectMap but this provides an interface to access them.
 * Created by dave on 29/12/15.
 */
public class CompetitiveLearningConfig extends NetworkConfig {

//    public ObjectMap _om;
    public String _keyInputs = "i";
    public String _keyWidthCells = "w";
    public String _keyHeightCells = "h";

    public CompetitiveLearningConfig() {
    }

    public void setup( ObjectMap om, String name, int inputs, int w, int h ) {
        super.setup(om, name);
        setNbrInputs( inputs );
        setWidthCells( w );
        setHeightCells( h );
    }

    public void copyFrom( NetworkConfig nc, String name ) {
        super.copyFrom(nc, name);

        CompetitiveLearningConfig c = (CompetitiveLearningConfig)nc;

        setNbrInputs(c.getNbrInputs());
        setWidthCells(c.getWidthCells());
        setHeightCells(c.getHeightCells());
    }

    public void setNbrInputs( int inputs ) {
        _om.put( getKey( _keyInputs ), inputs );
    }

    public void setWidthCells( int w ) {
        _om.put( getKey( _keyWidthCells ), w );
    }

    public void setHeightCells( int h ) {
        _om.put( getKey( _keyHeightCells ), h );
    }

    public int getNbrInputs() {
        Integer i = _om.getInteger( getKey( _keyInputs ) );
        return i.intValue();
    }

    public int getWidthCells() {
        Integer w = _om.getInteger( getKey( _keyWidthCells ) );
        return w.intValue();
    }

    public int getHeightCells() {
        Integer h = _om.getInteger( getKey( _keyHeightCells ) );
        return h.intValue();
    }

    public int getNbrCells() {
        Integer w = _om.getInteger( getKey( _keyWidthCells ) );
        Integer h = _om.getInteger( getKey( _keyHeightCells ) );
        return w * h;
    }

    public int getCell( int cellX, int cellY ) {
        Integer w = _om.getInteger( getKey( _keyWidthCells ) );
        return Data2d.getOffset( w, cellX, cellY );
    }

    public int getCellX( CompetitiveLearningConfig c, int cell ) {
        Integer w = _om.getInteger( getKey( _keyWidthCells ) );
        return Data2d.getX( w, cell);
    }
    public int getCellY( CompetitiveLearningConfig c, int cell ) {
        Integer w = _om.getInteger( getKey( _keyWidthCells ) );
        return Data2d.getY( w, cell );
    }

}
