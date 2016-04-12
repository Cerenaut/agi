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
public class Constants {

    public static float QUITE_SMALL = 0.00001f;
    public static double SQRT2 = Math.sqrt( 2.0 ); // ~= 1.4142135623;
    public static double RECIPROCAL_PI = 1.0 / Math.PI; // reciprocal of pi, allows fast / pi

    public static int RANDOM_NORMAL_SAMPLES = 12;

    protected static double RADIANS_TO_DEGREES = 180.0 / Math.PI;
    protected static double DEGREES_TO_RADIANS = Math.PI / 180.0;

    public static boolean check( float value ) {

        boolean ok = true;

        if( Float.isNaN( value ) ) {
            ok = false;
        }
        if( Float.isInfinite( value ) ) {
            ok = false;
        }

        return ok;
    }

    public static boolean check( double value ) {

        boolean ok = true;

        if( Double.isNaN( value ) ) {
            ok = false;
        }
        if( Double.isInfinite( value ) ) {
            ok = false;
        }

        return ok;
    }

}
