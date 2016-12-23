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
import java.util.ArrayList;

/**
 * Functions for viewing and restructuring N-dimensional FloatArray2s as paintable
 * 2D shapes. Includes default conventions for row-major storage and making
 * square shapes when only 1 dimensional data is given.
 *
 * @author dave
 */
public class Data2d {

    /**
     * Incrementally builds a matrix of vectors x time by appending new vectors as they become available.
     *
     * @param vector
     * @param period
     * @param existingVectors
     * @return
     */
    public static Data accumulateVectors( Data vector, int period, Data existingVectors ) {
        int elements = vector.getSize();

        Data newOutput;

        if( period < 0 ) {
            // keep appending
            int oldHistoryLength = 0;
            int newHistoryLength = 0;

            if( existingVectors == null ) {
                newHistoryLength = 1;
            }
            else {
                // infinite length
                oldHistoryLength = existingVectors._dataSize.getSize( DataSize.DIMENSION_Y );
                newHistoryLength = oldHistoryLength + 1;
            }

            newOutput = new Data( DataSize.create( DataSize.DIMENSION_X, elements, DataSize.DIMENSION_Y, newHistoryLength ) );

            // copy old vectors
            int offsetThis = 0;
            int offsetThat = 0;

            if( existingVectors != null ) {
                offsetThis = oldHistoryLength * elements;
                newOutput.copyRange( existingVectors, 0, 0, oldHistoryLength * elements );
            }

            // append new vector
            newOutput.copyRange( vector, offsetThis, offsetThat, elements );
        }
        else {
            // rolling window
            DataSize ds = DataSize.create( DataSize.DIMENSION_X, elements, DataSize.DIMENSION_Y, period );

            if( existingVectors == null ) {
                newOutput = new Data( ds );
            }
            else if( !existingVectors._dataSize.isSameAs( ds ) ) {
                newOutput = new Data( ds );
                newOutput.setSize( ds );
            }
            else {
                newOutput = existingVectors; // don't resize, keep old data
            }

            // it's slow, but ideally the picture makes sense when viewed. So to make this happen we need to shift all the data.
            // shift all the old values 1 place
            for( int i1 = period -2; i1 >= 0; --i1 ) {

                int i2 = i1 + 1;
                if( i2 >= period ) {
                    continue;
                }

                for( int j = 0; j < elements; ++j ) {
                    int offset1 = i1 * elements +j;
                    int offset2 = i2 * elements +j;
                    float x1 = newOutput._values[ offset1 ];
                    newOutput._values[ offset2 ] = x1;
                }
            }

            for( int j = 0; j < elements; ++j ) {
                int offset2 = 0 * elements +j;
                float x1 = vector._values[ j ];
                newOutput._values[ offset2 ] = x1;
            }
        }

        return newOutput;
    }

    /**
     * @param xRect
     * @param yRect
     * @param wRect
     * @param hRect
     * @param dataWidth
     * @param dataHeight
     * @return
     */
    public static ArrayList< Integer > getIndicesInRect( int xRect, int yRect, int wRect, int hRect, int dataWidth, int dataHeight ) {
        ArrayList< Integer > indices = new ArrayList< Integer >();

        for( int y = 0; y < hRect; ++y ) {
            for( int x = 0; x < wRect; ++x ) {

                int index = ( yRect + y ) * dataWidth
                        + ( xRect + x );

                indices.add( index );
            }
        }

        return indices;
    }

    public static void copy( int w, int h, Data d1, int x1, int y1, Data d2, int x2, int y2 ) {

        Point p1 = Data2d.getSize( d1 );
        Point p2 = Data2d.getSize( d2 );

        for( int y = 0; y < h; ++y ) {
            for( int x = 0; x < w; ++x ) {
                int xa = x1 + x;
                int ya = y1 + y;
                int xb = x2 + x;
                int yb = y2 + y;

                Integer na = getOffset( d1._dataSize, xa, ya );
                Integer nb = getOffset( d2._dataSize, xb, yb );

                float value = d1._values[ na ];
                d2._values[ nb ] = value;
            }
        }
    }

    /**
     * A function to determine a nice shape and size for a given FloatArray2, for
     * easy painting.
     *
     * @param d
     * @return Nice width and height
     */
    public static Point getSize( Data d ) {
        if( d == null ) {
            return null;
        }
        return getSize( d._dataSize );
    }

    public static Point getSize( FloatArray fa ) {
        if( fa == null ) {
            return null;
        }
        if( fa._values == null ) {
            return null;
        }
        int volume = fa._values.length;
        return getSizeSquare( volume );
    }

    public static Point getSize( DataSize d ) {
        // Look for explicit shape in w and h dimensions
        if( d == null ) {
            return null;
        }

        Point p = getSizeExplicit( d );

        if( p != null ) {
            return p;
        }

        int volume = d.getVolume();

        return getSizeSquare( volume );
    }

    public static Point getSizeExplicit( Data d ) {
        if( d == null ) {
            return null;
        }
        return getSizeExplicit( d._dataSize );
    }

    public static Point getSizeExplicit( DataSize d ) {
        if( d == null ) {
            return null;
        }
        if( d.getDimensions() == 2 ) {
            Integer ix = d.getIndex( DataSize.DIMENSION_X );
            Integer iy = d.getIndex( DataSize.DIMENSION_Y );
            if( ( ix != null ) && ( iy != null ) ) {
                int w = d.getSize( ix );
                int h = d.getSize( iy );

                Point p = new Point( w, h );
                return p;
            }
        }

        return null;
    }

    public static boolean is2D( DataSize d ) {
        return ( getSizeExplicit( d ) != null );
    }

    public static Point getSizeSquare( int volume ) {
        // Else, to get here, we don't have a 2-D shape with explicit w and h.
        // So try to make a nice square shape from the given volume.
        int w = ( int ) Math.sqrt( volume );
        int h = w;

        // expand the height til it fits.
        int sq = w * h;
        while( sq < volume ) {
            h = h + 1;
            sq = w * h;
        }

        Point p = new Point( w, h );

        return p;
    }

    public static Point getXY( DataSize d, int offset ) {
        Point p = Data2d.getSize( d );
        return getXY( p, offset );
    }

    public static Point getXY( Point p, int offset ) {
        if( p == null ) {
            return null;
        }

        int y = offset / p.x;  // / width, which is first (x)
        int x = offset % p.x;
        return new Point( x, y );
    }

    public static Integer getOffset( DataSize d, int x, int y ) {
        Point p = Data2d.getSize( d );
        if( p == null ) {
            return null;
        }

        return getOffset( p.x, x, y );
    }

    public static Integer getOffset( int stride, int x, int y ) {
        return y * stride + x;
    }

    public static Integer getX( DataSize d, int offset ) {
        Point p = Data2d.getSize( d );
        if( p == null ) {
            return null;
        }

        return getX( p.x, offset );
    }

    public static Integer getY( DataSize d, int offset ) {
        Point p = Data2d.getSize( d );
        if( p == null ) {
            return null;
        }

        return getY( p.x, offset );
    }

    public static Integer getX( int stride, int offset ) {
        int x = offset % stride;
        return x;
    }

    public static Integer getY( int stride, int offset ) {
        int y = offset / stride;
        return y;
    }

}