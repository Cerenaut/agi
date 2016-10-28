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
import io.agi.core.orm.ObjectMap;

import java.util.Random;

/**
 * Created by dave on 29/12/15.
 */
public class HierarchicalQuiltConfig extends CompetitiveLearningConfig {

    public static final String INTERVAL_INPUT_1_X = "interval-input-1-x";
    public static final String INTERVAL_INPUT_1_Y = "interval-input-1-y";
    public static final String INTERVAL_INPUT_2_X = "interval-input-2-x";
    public static final String INTERVAL_INPUT_2_Y = "interval-input-2-y";

    public HierarchicalQuiltConfig() {
    }

    public void setup( ObjectMap om, String name, Random r, int inputs, int w, int h, int i1x, int i1y, int i2x, int i2y ) {
        super.setup( om, name, r, inputs, w, h );

        setIntervalInput1X( i1x );
        setIntervalInput1Y( i1y );
        setIntervalInput2X( i2x );
        setIntervalInput2Y( i2y );
    }

    public void copyFrom( NetworkConfig nc, String name ) {
        super.copyFrom( nc, name );

        HierarchicalQuiltConfig c = ( HierarchicalQuiltConfig ) nc;

        setIntervalInput1X( c.getIntervalInput1X() );
        setIntervalInput1Y( c.getIntervalInput1Y() );
        setIntervalInput2X( c.getIntervalInput2X() );
        setIntervalInput2Y( c.getIntervalInput2Y() );
    }

    public void setIntervalInput1X( int interval ) {
        _om.put( getKey( INTERVAL_INPUT_1_X ), interval );
    }
    public void setIntervalInput1Y( int interval ) {
        _om.put( getKey( INTERVAL_INPUT_1_Y ), interval );
    }
    public void setIntervalInput2X( int interval ) {
        _om.put( getKey( INTERVAL_INPUT_2_X ), interval );
    }
    public void setIntervalInput2Y( int interval ) {
        _om.put( getKey( INTERVAL_INPUT_2_Y ), interval );
    }

    public int getIntervalInput1X() {
        Integer n = _om.getInteger( getKey( INTERVAL_INPUT_1_X ) );
        return n.intValue();
    }
    public int getIntervalInput1Y() {
        Integer n = _om.getInteger( getKey( INTERVAL_INPUT_1_Y ) );
        return n.intValue();
    }
    public int getIntervalInput2X() {
        Integer n = _om.getInteger( getKey( INTERVAL_INPUT_2_X ) );
        return n.intValue();
    }
    public int getIntervalInput2Y() {
        Integer n = _om.getInteger( getKey( INTERVAL_INPUT_2_Y ) );
        return n.intValue();
    }

}
