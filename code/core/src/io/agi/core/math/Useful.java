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

package io.agi.core.math;

/**
 * Created by dave on 26/01/16.
 */
public class Useful {

    /**
     * A quick handy function to work out how many groups you need if you know the size of each group, and the number
     * of individual elements.
     *
     * @param total
     * @param groupSize
     * @return
     */
    public static int GetNbrGroups( int total, int groupSize ) {
        int sets = total / groupSize;
        if( total > ( groupSize * sets ) ) {
            ++sets;
        }
        return sets;
    }

    public static boolean IsBad( float r ) {
        boolean breakpoint = false;
        if( Float.isInfinite( r ) ) {
            breakpoint = true;
        }
        if( Float.isNaN( r ) ) {
            breakpoint = true;
        }

        if( breakpoint ) {
            int g = 0;
            g++;
        }

        return breakpoint;
    }

    public static void IsBad( double r ) {
        boolean breakpoint = false;
        if( Double.isInfinite( r ) ) {
            breakpoint = true;
        }
        if( Double.isNaN( r ) ) {
            breakpoint = true;
        }

        if( breakpoint ) {
            int g = 0;
            g++;
        }
    }
}
