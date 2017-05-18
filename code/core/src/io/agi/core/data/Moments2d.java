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

package io.agi.core.data;

import java.awt.*;

/**
 * Hu Moments
 *
 * @author dave
 */
public class Moments2d {

    public static Point.Float weightedCentreOfMass(
            FloatArray input,
            int offset,
            int w,
            int h ) {

        float m00 = 0.f;
        float m01 = 0.f;
        float m10 = 0.f;

        int inputOffset = offset;

        for( int y = 0; y < h; ++y ) {
            for( int x = 0; x < w; ++x ) {
                float weight = input._values[ inputOffset ];

                ++inputOffset;

                m00 += weight;
                m10 += ( weight * ( float ) x );
                m01 += ( weight * ( float ) y );
            }
        }

        float xc = m10 / m00;
        float yc = m01 / m00;

        if( m00 <= 0.f ) {
            xc = ( float ) w * 0.5f;
            yc = ( float ) h * 0.5f;
        }

        Point.Float p = new Point.Float( xc, yc );
        return p;
    }

}
