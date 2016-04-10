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
            FloatArray2 input,
            int offset,
            int w,
            int h ) {

        float m00 = 0.f;
        float m01 = 0.f;
        float m10 = 0.f;

        int inputOffset = offset;

        for ( int y = 0; y < h; ++y ) {
            for ( int x = 0; x < w; ++x ) {
                float weight = input._values[ inputOffset ];

                ++inputOffset;

                m00 += weight;
                m10 += ( weight * ( float ) x );
                m01 += ( weight * ( float ) y );
            }
        }

        float xc = m10 / m00;
        float yc = m01 / m00;

        if ( m00 <= 0.f ) {
            xc = ( float ) w * 0.5f;
            yc = ( float ) h * 0.5f;
        }

        Point.Float p = new Point.Float( xc, yc );
        return p;
    }

}
