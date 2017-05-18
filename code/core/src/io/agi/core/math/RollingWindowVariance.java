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
 * https://en.wikipedia.org/w/index.php?title=Algorithms_for_calculating_variance&section=4#On-line_algorithm
 * Created by dave on 23/10/16.
 */
public class RollingWindowVariance {

    float K = 0;
    float n = 0;
    float ex = 0;
    float ex2 = 0;

    public void addValue( float x ) {

        if( n == 0 ) {
            K = x;
        }

        n = n + 1;
        ex += x - K;
        ex2 += (x - K) * (x - K);
    }

    public void removeValue( float x ) {
        n = n - 1;
        ex -= (x - K);
        ex2 -= (x - K) * (x - K);
    }

    public float getMean() {
        if( n < 1 ) {
            return 0;
        }

        float mean = K + ex / n;
        return mean;
    }

    public float getVariance() {

        if( n < 2 ) {
            return 0;
        }

        float sigma = ( ex2 - (ex*ex) / n )
                    / ( n-1 );
        return sigma;
    }
}
