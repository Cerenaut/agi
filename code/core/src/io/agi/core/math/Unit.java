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
 * @author dave
 */
public class Unit {

    // Converting real unit values to discrete integer range

    /**
     * Convert an integer to a real unit value
     *
     * @param n
     * @param range
     * @return
     */
    public static double fromInt( int n, int range ) {
        double unit = ( float ) n / ( float ) ( range - 1 );
        double r = clamp( unit );
        return r;
    }

    /**
     * Convert a real unit value to an integer
     *
     * @param r
     * @param range
     * @return
     */
    public static int toInt( double r, int range ) {
        double unit = clamp( r );
        double scaled = unit * ( double ) range;
        int n = ( int ) scaled; // rounds down
        if( n == range ) --n;
        return n;
    }

    /**
     * Convert a real unit value to a byte
     *
     * @param r
     * @return
     */
    public static int toByte( double r ) {
        int n = ( int ) ( r * 255.0 );
        n = Math.min( Math.max( n, 0 ), 255 );
        return n;
    }

    /**
     * Clamps the range to unit values
     *
     * @param x
     * @return
     */
    public static double clamp( double x ) {
        return ( Math.min( Math.max( x, 0.0 ), 1.0 ) );
    }

    /**
     * Should be like normal add (ie linear) but with saturation approaching 1.
     * This is roughly what you get with the (atan(x)/(pi/2)) function.
     *
     * @param x sum of two unit values
     * @return saturated result
     */
    public static double saturate( double x ) {

        if( x < 0.0 ) return 0.0;
        if( x >= 1.0 ) return 1.0; // max value

        // x is now 0:1
        // xsin = sin( (pi/2)*x ) ie
        // x==1   := pi/2 == sin(pi/2) == 1
        // x==0   := 0    == sin( 0  ) == 0
        return Fast.xsin( x );
    }

    /**
     * Linear interpolate function
     *
     * @param x     Old value
     * @param xt    One value
     * @param alpha Weight of old value
     * @return New value
     */
    public static double lerp( double x, double xt, double alpha ) {
        double beta = 1.0 - alpha;
        x = alpha * x + beta * xt;
        return x;
    }

    /**
     * Linear interpolate function
     *
     * @param x     Old value
     * @param xt    One value
     * @param alpha Weight of old value
     * @return New value
     */
    public static float lerp( float x, float xt, float alpha ) {
        float beta = 1.0f - alpha;
        x = alpha * x + beta * xt;
        return x;
    }

    /**
     * Safely multiply two small unit values without forcing result to zero.
     *
     * @param v1
     * @param v2
     * @return
     */
    public static float mulSmall( float v1, float v2 ) {
        double product = ( Constants.QUITE_SMALL + v1 )
                * ( Constants.QUITE_SMALL + v2 );
        float result = ( float ) Math.min( 1.f, product );
        return result;
    }

}
