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
 * @author dave
 */
public class Convolution2d {

    public static void convolve( ImageData mask, ImageData src, ImageData dest ) {

        // retrieve resolution of image
        int w = src.getWidth();
        int h = src.getHeight();

        int mw = mask.getWidth();
        int mh = mask.getHeight();

        // mask has to have odd width and height
        assert ( mw % 2 != 0 );
        assert ( mh % 2 != 0 );

        int mwr = mw / 2;       // mask width radius
        int mhr = mh / 2;       // mask height radius

        Coordinate c = src._d.begin();

        int channels = src.getChannels();
        int elements = src._d.getSize();

        float[] acc = new float[ channels ];     // accumulator

        // iterate image
        for( int y = 0; y < h; ++y ) {

            c.set( DataSize.DIMENSION_Y, y );

            for( int x = 0; x < w; ++x ) {

                c.set( DataSize.DIMENSION_X, x );

                int offset = c.offset(); // don't want to eval the 3d coord more than necessary..

                for( int i = 0; i < channels; ++i ) {
                    acc[ i ] = 0.f;
                }

                assert ( ( offset + 2 ) < elements );

                // offset for source image
                Coordinate sc = src._d.begin();

                // offset for mask
                Coordinate mc = mask._d.begin();

                // iterate mask
                for( int my = 0; my < mh; ++my ) {
                    for( int mx = 0; mx < mw; ++mx ) {

                        int yy = y - mhr + my;
                        int xx = x - mwr + mx;
                        sc.set( DataSize.DIMENSION_Y, yy );
                        sc.set( DataSize.DIMENSION_X, xx );
                        int soffset = sc.offset();

                        if( !sc.inside() ) {
                            break;
                        }

                        mc.set( DataSize.DIMENSION_Y, my );
                        mc.set( DataSize.DIMENSION_X, mx );

                        int moffset = mc.offset(); // don't want to eval the 3d coord more than necessary..

                        // for each channel
                        for( int i = 0; i < channels; ++i ) {

//                            if ( (soffset+i) >= src._dataSize._values.length ) {
//                                System.out.println("oh oh");
//                            }
//                            if ( (moffset+i) >= mask._dataSize._values.length ) {
//                                System.out.println("oh oh");
//                            }

                            // accumulate at position at centre of mask
                            float mval = mask._d._values[ moffset + i ];
                            float ival = src._d._values[ soffset + i ];
                            acc[ i ] += mval * ival;

                        }
                    }
                }

                for( int i = 0; i < channels; ++i ) {
                    dest._d._values[ offset + i ] = acc[ i ];
                }

            }
        }

    }

    /**
     * Convolve a greyscale 2d image with a 2d kernel.
     *
     * @param kernel
     * @param input
     * @param output
     */
    public static void convolve( Data kernel, Data input, Data output ) {

        // retrieve resolution of image
        Point iSize = Data2d.getSize( input );
        int iw = iSize.x;
        int ih = iSize.y;

        Point kSize = Data2d.getSize( kernel );
        int kw = kSize.x;
        int kh = kSize.y;

        // mask has to have odd width and height
        assert ( kw % 2 != 0 );
        assert ( kh % 2 != 0 );

        int kwr = kw / 2;       // mask width radius
        int khr = kh / 2;       // mask height radius

        // foreach( output pixel )
        for( int y = 0; y < ih; ++y ) {
            for( int x = 0; x < iw; ++x ) {

                float convolved = 0;

                // foreach( kernel pixel )
                for( int ky = 0; ky < kh; ++ky ) {
                    for( int kx = 0; kx < kw; ++kx ) {

                        int yy = y - khr + ky;
                        int xx = x - kwr + kx;

                        // duplicate edge pixel policy
                        xx = Math.max( 0, xx );
                        xx = Math.min( iw - 1, xx );

                        yy = Math.max( 0, yy );
                        yy = Math.min( ih - 1, yy );

                        int iOffset = yy * iw + xx;
                        int kOffset = ky * kw + kx;

                        float kv = kernel._values[ kOffset ];
                        float iv = input._values[ iOffset ];
                        convolved += kv * iv;
                    }
                }

                int oOffset = y * iw + x;
                output._values[ oOffset ] = convolved;
            }
        }

    }

}
