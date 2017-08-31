/*
 * Copyright (c) 2017.
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

package io.agi.core.data;

import io.agi.core.math.Geometry;
import io.agi.core.math.Statistics;

/**
 * Created by dave on 5/05/17.
 */
public class Kernels2d {

    public static Data DifferenceOfGaussians( int w, int h, float stdDev1, float stdDev2 ) {
        Data g1 = Gaussian( w, h, stdDev1 );
        Data g2 = Gaussian( w, h, stdDev2 );
        Data dog = new Data( g1 );
        dog.sub( g2 );
        return dog;
    }

    public static Data Gaussian( int w, int h, float stdDev ) {

        assert ( w % 2 != 0 );
        assert ( h % 2 != 0 );

        Data kernel = new Data( DataSize.create( w, h ) );

        int cx = w / 2;       // mask width radius
        int cy = h / 2;       // mask height radius

        for( int ky = 0; ky < h; ++ky ) {
            for( int kx = 0; kx < w; ++kx ) {

                float d = Geometry.distanceEuclidean2d( (float)kx, (float)ky, (float)cx, (float)cy );
                float value = (float) Statistics.gaussian( d, stdDev );

                int kOffset = ky * w + kx;

                kernel._values[ kOffset ] = value;
            }
        }

        return kernel;
    }

}
