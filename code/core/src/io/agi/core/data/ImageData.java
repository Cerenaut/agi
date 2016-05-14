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
import java.awt.image.BufferedImage;

/**
 * @author dave
 */
public class ImageData {

    public Data _d = null;

    public ImageData( Data d ) {
        _d = d;
    }

    public ImageData( Point size, int channels ) {
        DataSize d = DataSize.create( size.x, size.y, channels );
        _d = new Data( d );
    }

    public Data getData() {
        return _d;
    }

    public int getWidth() {
        return _d._dataSize.getSize( DataSize.DIMENSION_X );
    }

    public int getHeight() {
        return _d._dataSize.getSize( DataSize.DIMENSION_Y );
    }

    public int getChannels() {
        Integer n = _d._dataSize.getSize( DataSize.DIMENSION_Z );
        if( n != null ) {
            return n;
        }
        return 1;
    }

    public int getOffset( int x, int y, int channel ) {
        Coordinate c = _d.begin();
        c.set( DataSize.DIMENSION_X, x );
        c.set( DataSize.DIMENSION_Y, y );
        c.set( DataSize.DIMENSION_Z, channel );
        int offset = c.offset();
        return offset;
    }

    public float get( int x, int y, int c ) {
        int offset = getOffset( x, y, c );
        return _d._values[ offset ];
    }

    /**
     * Set all pixels to the value 'val'.
     */
    public void set( int val ) {
        _d.set( val );
    }

    public void set( int x, int y, int c, int value ) {
        int offset = getOffset( x, y, c );
        _d._values[ offset ] = value;
    }

    public void setWithBufferedImage( BufferedImage bi ) {

        // retrieve resolution of image
        int vw = getWidth();
        int vh = getHeight();

        int[] sample = new int[ 4 ]; // RGB24

        Coordinate c = _d.begin();

        c.set( DataSize.DIMENSION_Z, 0 );

        int channels = getChannels();
        int elements = _d.getSize();
        int pixelMaxValue = 255;
        float vReciprocal = 1.0f / ( float ) pixelMaxValue;

        for( int y = 0; y < vh; ++y ) {

            c.set( DataSize.DIMENSION_Y, y );

            for( int x = 0; x < vw; ++x ) {

                c.set( DataSize.DIMENSION_X, x );

                int offset = c.offset(); // don't want to eval the 3d coord more than necessary..

                bi.getRaster().getPixel( x, y, sample );

                int r = sample[ 0 ];
                int g = sample[ 1 ];
                int b = sample[ 2 ];

                // scale the pixel values to normalize them
                float bScaled = ( float ) b * vReciprocal;
                float gScaled = ( float ) g * vReciprocal;
                float rScaled = ( float ) r * vReciprocal;

                assert ( ( offset + 2 ) < elements );

                if( channels == 3 ) {
                    _d._values[ offset + 0 ] = rScaled;
                    _d._values[ offset + 1 ] = gScaled;
                    _d._values[ offset + 2 ] = bScaled;
                } else { // greyscale
                    _d._values[ offset + 0 ] = ( rScaled + gScaled + bScaled ) / 3.0f;
                }
            }
        }
    }

    public static void convolve2D( ImageData mask, ImageData src, ImageData dest ) {
    }

//    public static void convolve2D( ImageData mask, ImageData src, ImageData dest ) {
//
//        // retrieve resolution of image
//        int w = src.getWidth();
//        int h = src.getHeight();
//
//        int mw = mask.getWidth();
//        int mh = mask.getHeight();
//
//        // mask has to have odd width and height
//        assert ( mw % 2 != 0 );
//        assert ( mh % 2 != 0 );
//
//        int mwr = mw / 2;       // mask width radius
//        int mhr = mh / 2;       // mask height radius
//
//        Coordinate c = src._fa.start();
//        c.set( C, 0 );
//
//        int channels = src.getChannels();
//        int elements = src._fa.volume();
//
//        float[] acc = new float[ channels ];     // accumulator
//
//        // iterate image
//        for( int y = 0; y < h; ++y ) {
//            c.set( Y, y );
//            for( int x = 0; x < w; ++x ) {
//                c.set( X, x );
//                int offset = c.offset(); // don't want to eval the 3d coord more than necessary..
//
//                for( int i = 0 ; i < channels ; ++i ) {
//                    acc[i] = 0.f;
//                }
//
//                assert( (offset+2) < elements );
//
//                // offset for source image
//                Coordinate sc = src._fa.start();
//                sc.set( C, 0 );
//
//                // offset for mask
//                Coordinate mc = mask._fa.start();
//                mc.set( C, 0 );
//
//                // iterate mask
//                for ( int my = 0; my < mh ; ++my ) {
//                    for ( int mx = 0; mx < mw; ++mx ) {
//
//                        int yy = y - mhr + my;
//                        int xx = x - mwr + mx;
//                        sc.set( Y, yy );
//                        sc.set( X, xx );
//                        int soffset = sc.offset();
//
//                        if ( !sc.valid() ) {
//                            break;
//                        }
//
//                        mc.set( Y, my );
//                        mc.set( X, mx );
//                        int moffset = mc.offset(); // don't want to eval the 3d coord more than necessary..
//
//                        // for each channel
//                        for( int i = 0 ; i < channels ; ++i ) {
//
//                            if ( (soffset+i) >= src._fa._values.length ) {
//                                System.out.println("oh oh");
//                            }
//                            if ( (moffset+i) >= mask._fa._values.length ) {
//                                System.out.println("oh oh");
//                            }
//
//                            // accumulate at position at centre of mask
//                            float mval = mask._fa._values[ moffset + i ];
//                            float ival = src._fa._values[ soffset + i ];
//                            acc[i] += mval * ival;
//
//                        }
//                    }
//                }
//
//                for( int i = 0 ; i < channels ; ++i ) {
//                    dest._fa._values[ offset + i ] = acc[ i ];
//                }
//
//            }
//        }
//      }
}
