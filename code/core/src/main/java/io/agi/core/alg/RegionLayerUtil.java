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

package io.agi.core.alg;

import io.agi.core.data.Data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Utility functions to de-clutter the RegionLayer class.
 *
 * Created by dave on 14/05/16.
 */
public class RegionLayerUtil {

    /**
     * Debugging method for quickly scanning sparse binary sets
     * @param d
     * @param prefix
     */
    public void printSet( Data d, String prefix ) {
        Collection< Integer > c = d.indicesMoreThan( 0.f ); // find all the active bits.
        printSet( c, prefix );
    }

    /**
     * Debugging method for quickly scanning sparse binary sets
     * @param c
     * @param prefix
     */
    public void printSet( Collection< Integer > c, String prefix ) {
        ArrayList< Integer > list = new ArrayList< Integer >();
        list.addAll( c );
        Collections.sort( list );

        System.err.println( prefix + "{" );
        for( Integer i : list ) {
            System.err.print( i + ", " );
        }
        System.err.println( prefix + "}" );
    }

}
