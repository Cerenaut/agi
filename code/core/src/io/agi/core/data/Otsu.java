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

/**
 * Dynamically threshold data (e.g. image) into 2 classes by finding the best
 * threshold.
 *
 * @author dave
 */
public class Otsu {

    public static void apply( FloatArray2 v1, FloatArray2 v2, int precision, float lo, float hi ) {

        int values = v1.getSize();
        FloatArray2 vh = v1.getHistogram( precision, 0.0f, 1.0f );

        if( v2 == null ) {
            v2 = new FloatArray2( v1 );
        } else {
            v2.copy( v1 );
        }

        int thresholdBin = findThreshold( vh._values, values );
        float thresholdValue = ( float ) thresholdBin / ( float ) precision;

        v2.thresholdMoreThanEqual( thresholdValue, hi, lo );
    }

    public static int findThreshold( float[] histogram, int values ) {

        int bins = histogram.length;

        float sum = 0.0f;
        for( int i = 0; i < bins; ++i ) {
            sum += ( float ) i * histogram[ i ];
        }

        float sumB = 0.0f;
        int wB = 0;
        int wF = 0;
        float mB = 0.0f;
        float mF = 0.0f;
        float max = 0.0f;
        float between = 0.0f;
        int threshold = 0;

        for( int i = 0; i < bins; ++i ) {
            wB += histogram[ i ];

            if( wB == 0 ) {
                continue;
            }

            wF = values - wB;
            if( wF == 0 ) {
                break;
            }

            sumB += ( ( float ) i * histogram[ i ] );
            mB = sumB / wB;
            mF = ( sum - sumB ) / wF;
            between = wB * wF * ( float ) Math.pow( mB - mF, 2.0 );

            if( between > max ) {
                max = between;
                threshold = i;
            }
        }

        return threshold;
    }

}
