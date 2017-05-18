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

import io.agi.core.ann.NetworkConfig;
import io.agi.core.data.Data2d;
import io.agi.core.orm.ObjectMap;

import java.awt.*;
import java.util.Random;

/**
 * Parameters are stored as an ObjectMap but this provides an interface to access them.
 * Created by dave on 29/12/15.
 */
public class CompetitiveLearningConfig extends NetworkConfig {

    public static final String INPUTS = "inputs";
    public static final String WIDTH_CELLS = "width-cells";
    public static final String HEIGHT_CELLS = "height-cells";

    public String _keyInputs = INPUTS;
    public String _keyWidthCells = WIDTH_CELLS;
    public String _keyHeightCells = HEIGHT_CELLS;

    public CompetitiveLearningConfig() {
    }

    public void setup( ObjectMap om, String name, Random r, int inputs, int w, int h ) {
        super.setup( om, name, r );
        setNbrInputs( inputs );
        setWidthCells( w );
        setHeightCells( h );
    }

    public void copyFrom( NetworkConfig nc, String name ) {
        super.copyFrom( nc, name );

        CompetitiveLearningConfig c = ( CompetitiveLearningConfig ) nc;

        setNbrInputs( c.getNbrInputs() );
        setWidthCells( c.getWidthCells() );
        setHeightCells( c.getHeightCells() );
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
        return  _om.getInteger( getKey( _keyInputs ) );
    }

    public int getWidthCells() {
        return _om.getInteger( getKey( _keyWidthCells ) );
    }

    public int getHeightCells() {
        return _om.getInteger( getKey( _keyHeightCells ) );
    }

    public Point getSizeCells() {
        return new Point( getWidthCells(), getHeightCells() );
    }

    public int getNbrCells() {
        return _om.getInteger( getKey( _keyWidthCells ) ) * _om.getInteger( getKey( _keyHeightCells ) );
    }

    public int getCell( int cellX, int cellY ) {
        Integer w = _om.getInteger( getKey( _keyWidthCells ) );
        return Data2d.getOffset( w, cellX, cellY );
    }

    public int getCellX( int cell ) {
        Integer w = _om.getInteger( getKey( _keyWidthCells ) );
        return Data2d.getX( w, cell );
    }

    public int getCellY( int cell ) {
        Integer w = _om.getInteger( getKey( _keyWidthCells ) );
        return Data2d.getY( w, cell );
    }

}
