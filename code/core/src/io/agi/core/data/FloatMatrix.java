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
 * Basic Matrix functions for a 2D matrix stored as a FloatArray.
 *
 * @author dave
 */
public class FloatMatrix {

    /**
     * Matrix multiplication with precision error handling.
     *
     * @param m1
     * @param m2
     * @param rows1
     * @param cols1
     * @param rows2
     * @param cols2
     * @return
     */
    public static FloatArray mulSafe( FloatArray m1, FloatArray m2, int rows1, int cols1, int rows2, int cols2 ) {
        int rows3 = rows1;
        int cols3 = cols2;
        int size = rows3 * cols3;
        FloatArray m3 = new FloatArray( size );
        mulSafe( m1, m2, m3, rows1, cols1, rows2, cols2 );
        return m3;
    }

    /**
     * Matrix multiplication with precision error handling.
     *
     * @param m1
     * @param m2
     * @param m3
     * @param rows1
     * @param cols1
     * @param rows2
     * @param cols2
     */
    public static void mulSafe( FloatArray m1, FloatArray m2, FloatArray m3, int rows1, int cols1, int rows2, int cols2 ) {
        int rows3 = rows1;
        int cols3 = cols2;

        FloatArray sumColsPerRow = sumColsPerRow( m1, rows1, cols1 ); // foreach( row ), sum all col of A
        FloatArray sumRowsPerCol = sumRowsPerCol( m2, rows2, cols2 ); // foreach( col ), sum all row of B

        for( int y = 0; y < rows3; ++y ) {

            float value1 = sumColsPerRow._values[ y ];

            for( int x = 0; x < cols3; ++x ) {

                float value2 = sumRowsPerCol._values[ x ];
                float product = value1 * value2;

                if( Float.isInfinite( product ) ) {
                    if( ( ( value1 > 0.f ) && ( value2 > 0.f ) ) ||
                            ( ( value1 < 0.f ) && ( value2 < 0.f ) ) ) {
                        product = Float.MAX_VALUE;
                    } else {
                        product = 0.f - Float.MAX_VALUE;
                    }
                }
                int offset3 = getOffset( rows3, cols3, y, x ); // y = row, x=col
                m3._values[ offset3 ] = product;
            }
        }
    }

    /**
     * Matrix multiplication.
     *
     * @param m1
     * @param m2
     * @param rows1
     * @param cols1
     * @param rows2
     * @param cols2
     * @return
     */
    public static FloatArray mul( FloatArray m1, FloatArray m2, int rows1, int cols1, int rows2, int cols2 ) {
        int rows3 = rows1;
        int cols3 = cols2;
        int size = rows3 * cols3;
        FloatArray m3 = new FloatArray( size );
        mul( m1, m2, m3, rows1, cols1, rows2, cols2 );
        return m3;
    }

    /**
     * Matrix multiplication. Result is preallocated.
     *
     * @param m1
     * @param m2
     * @param m3
     * @param rows1
     * @param cols1
     * @param rows2
     * @param cols2
     */
    public static void mul( FloatArray m1, FloatArray m2, FloatArray m3, int rows1, int cols1, int rows2, int cols2 ) {
        int rows3 = rows1;
        int cols3 = cols2;

        FloatArray sumColsPerRow = sumColsPerRow( m1, rows1, cols1 ); // foreach( row ), sum all col of A
        FloatArray sumRowsPerCol = sumRowsPerCol( m2, rows2, cols2 ); // foreach( col ), sum all row of B

        for( int y = 0; y < rows3; ++y ) {

            float value1 = sumColsPerRow._values[ y ];

            for( int x = 0; x < cols3; ++x ) {

                float value2 = sumRowsPerCol._values[ x ];
                float product = value1 * value2;

                int offset3 = getOffset( rows3, cols3, y, x ); // y = row, x=col
                m3._values[ offset3 ] = product;
            }
        }
    }

    public static FloatArray add( FloatArray m1, FloatArray m2 ) {
        FloatArray m3 = new FloatArray( m1 );
        m3.add( m2 );
        return m3;
    }

    public static void add( FloatArray m1, FloatArray m2, FloatArray m3 ) {
        m3.add( m1, m2 );
    }

    public static int getOffset( int rows, int cols, int row, int col ) {
        int offset = row * cols + col;
        return offset;
    }

    public static float get( FloatArray m1, int rows, int cols, int row, int col ) {
        int offset = getOffset( rows, cols, row, col );
        float value = m1._values[ offset ];
        return value;
    }

    public static void set( FloatArray m1, int rows, int cols, int row, int col, float value ) {
        int offset = getOffset( rows, cols, row, col );
        m1._values[ offset ] = value;
    }

    public static FloatArray sumRowsPerCol( FloatArray m1, int rows, int cols ) {
        FloatArray m3 = new FloatArray( cols );

        for( int x = 0; x < cols; ++x ) {

            float sum = 0.f;

            for( int y = 0; y < rows; ++y ) {
                int offset = getOffset( rows, cols, y, x );
                float value = m1._values[ offset ];
                sum += value;
            }

            m3._values[ x ] = sum; // col of 1 col all rows
        }

        return m3;
    }

    public static FloatArray sumColsPerRow( FloatArray m1, int rows, int cols ) {
        FloatArray m3 = new FloatArray( rows );

        for( int y = 0; y < rows; ++y ) {

            float sum = 0.f;

            for( int x = 0; x < cols; ++x ) {
                int offset = getOffset( rows, cols, y, x ); // y = row, x=col
                float value = m1._values[ offset ];
                sum += value;
            }

            m3._values[ y ] = sum; // col of 1 col all rows
        }

        return m3;
    }

    public static FloatArray transpose( FloatArray m1, int rows1, int cols1 ) {
        // vectors too
        // storage in 1d as col-dominant or row-dominant?
        // Images are row-dominant but matlab is cols:
        // http://www.eedsp.gatech.edu/Information/MATLAB_User_Guide/node26.html
        int rows2 = cols1;
        int cols2 = rows1;
//        1 2         1 3 5 
//        3 4   ==>   2 4 6
//        5 6 
        int size = rows1 * cols1;
        FloatArray m3 = new FloatArray( size );
        for( int y = 0; y < rows1; ++y ) {
            for( int x = 0; x < cols1; ++x ) {
                int offset1 = getOffset( rows1, cols1, y, x );
                float value = m1._values[ offset1 ];//y * cols1 + x ];
                int offset2 = getOffset( rows2, cols2, x, y );
                m3._values[ offset2 ] = value;//x * rows1 + y ] = value;
            }
        }
// test code: https://en.wikipedia.org/wiki/Transpose        
//        FloatArray2 t0 = new FloatArray2( 6 );
//        t0._values[ 0 ] = 1;
//        t0._values[ 1 ] = 2;
//        t0._values[ 2 ] = 3;
//        t0._values[ 3 ] = 4;
//        t0._values[ 4 ] = 5;
//        t0._values[ 5 ] = 6;
//        FloatArray2 t = FloatArray2.matrixTranspose( t0, 3, 2 );

        return m3;
    }

    public static FloatArray scalarMul( FloatArray m1, float scalar ) {
        FloatArray m3 = new FloatArray( m1 );
        m3.mul( scalar );
        return m3;
    }

}
