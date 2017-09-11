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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package io.agi.core.math;

/**
 * Fast approximations for some expensive functions.
 *
 * @author dave
 */
public class Fast {

    // Fast pow (a^b) function approximation thanks to:
    // http://martin.ankerl.com/2007/10/04/optimized-pow-approximation-for-java-and-c-c/
    //___________________________________________________________________________
    public static double pow( final double a, final double b ) {
        final int x = ( int ) ( Double.doubleToLongBits( a ) >> 32 );
        final int y = ( int ) ( b * ( x - 1072632447 ) + 1072632447 );
        return Double.longBitsToDouble( ( ( long ) y ) << 32 );
    }


    // Fast exp (e^x) function approximation thanks to:
    // http://martin.ankerl.com/2007/02/11/optimized-exponential-functions-for-java/
    //___________________________________________________________________________
    public static double exp( double x ) {
        final long tmp = ( long ) ( 1512775 * x + 1072632447 );
        return Double.longBitsToDouble( tmp << 32 );
    }

    // Fast natural log function approximation thanks to:
    // http://martin.ankerl.com/2007/02/11/optimized-exponential-functions-for-java/
    //___________________________________________________________________________
    public static double log( double x ) {
        return 6 * ( x - 1 ) / ( x + 1 + 4 * ( Math.sqrt( x ) ) );
    }

    // Return an approx to sin(pi/2 * x) where -1 <= x <= 1.
    // In that range it has a max absolute error of 5e-9
    // according to Hastings, Approximations For Digital Computers.
    // from: Computer Approximations by Hart
    // http://stackoverflow.com/questions/523531/fast-transcendent-trigonometric-functions-for-java
    //___________________________________________________________________________
    static double xsin( double x ) {
        double x2 = x * x;
        return ( ( ( ( .00015148419
                * x2
                - .00467376557
        )
                * x2
                + .07968967928
        )
                * x2
                - .64596371106
        )
                * x2
                + 1.57079631847
        )
                * x;
    }

}
