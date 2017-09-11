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

import java.awt.*;

/**
 * @author dave
 */
public class Geometry {

    /**
     * Angle of vector x,y
     *
     * @param x
     * @param y
     * @return
     */
    public double angle( double x, double y ) {
        double r = Math.atan2( y, x ); // range -pi : pi
        return r;
    }

    /**
     * Rotate the given coordinates in 2d by angle
     *
     * @param x
     * @param y
     * @param angle
     * @return
     */
    public Point.Double rotate( double x, double y, double angle ) { // about the origin
        double ct = Math.cos( angle );
        double st = Math.sin( angle );
        double xr = ( x * ct )
                - ( y * st );
        double yr = ( x * st )
                + ( y * ct );

        Point.Double p = new Point.Double( xr, yr );
        return p;
    }

    public static double degrees( double radians ) {
        double degrees = Constants.RADIANS_TO_DEGREES * radians;
        return degrees;
    }

    public static double radians( double degrees ) {
        double radians = Constants.DEGREES_TO_RADIANS * degrees;
        return radians;
    }

    public static double distanceEuclidean2d( double x1, double y1, double x2, double y2 ) {
        double dx = ( x1 - x2 );
        double dy = ( y1 - y2 );
        double d = Math.sqrt( dx * dx + dy * dy );
        return d;
    }

    public static float distanceEuclidean2d( float x1, float y1, float x2, float y2 ) {
        float dx = ( x1 - x2 );
        float dy = ( y1 - y2 );
        float d = ( float ) Math.sqrt( dx * dx + dy * dy );
        return d;
    }

}
