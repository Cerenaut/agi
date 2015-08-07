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
     * @param m1
     * @param m2
     * @param rows1
     * @param cols1
     * @param rows2
     * @param cols2
     * @return 
     */
    public static FloatArray2 mulSafe( FloatArray2 m1, FloatArray2 m2, int rows1, int cols1, int rows2, int cols2 ) {
        int rows3 = rows1;
        int cols3 = cols2;
        int size = rows3 * cols3;
        FloatArray2 m3 = new FloatArray2( size );
        mulSafe( m1, m2, m3, rows1, cols1, rows2, cols2 );
        return m3;
    }

    /**
     * Matrix multiplication with precision error handling.
     * @param m1
     * @param m2
     * @param m3
     * @param rows1
     * @param cols1
     * @param rows2
     * @param cols2 
     */
    public static void mulSafe( FloatArray2 m1, FloatArray2 m2, FloatArray2 m3, int rows1, int cols1, int rows2, int cols2 ) {
        int rows3 = rows1;
        int cols3 = cols2;

        FloatArray2 sumColsPerRow = sumColsPerRow( m1, rows1, cols1 ); // foreach( row ), sum all col of A
        FloatArray2 sumRowsPerCol = sumRowsPerCol( m2, rows2, cols2 ); // foreach( col ), sum all row of B
        
        for( int y = 0; y < rows3; ++y ) {
            
            float value1 = sumColsPerRow._values[ y ];
            
            for( int x = 0; x < cols3; ++x ) {
                
                float value2 = sumRowsPerCol._values[ x ];
                float product = value1 * value2;

                if( Float.isInfinite( product ) ) {
                    if( ( ( value1 > 0.f ) && ( value2 > 0.f ) ) ||
                        ( ( value1 < 0.f ) && ( value2 < 0.f ) ) ) {
                        product = Float.MAX_VALUE;
                    }
                    else {
                        product = 0.f -Float.MAX_VALUE;
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
    public static FloatArray2 mul( FloatArray2 m1, FloatArray2 m2, int rows1, int cols1, int rows2, int cols2 ) {
        int rows3 = rows1;
        int cols3 = cols2;
        int size = rows3 * cols3;
        FloatArray2 m3 = new FloatArray2( size );
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
    public static void mul( FloatArray2 m1, FloatArray2 m2, FloatArray2 m3, int rows1, int cols1, int rows2, int cols2 ) {
        int rows3 = rows1;
        int cols3 = cols2;

        FloatArray2 sumColsPerRow = sumColsPerRow( m1, rows1, cols1 ); // foreach( row ), sum all col of A
        FloatArray2 sumRowsPerCol = sumRowsPerCol( m2, rows2, cols2 ); // foreach( col ), sum all row of B
        
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
    
    public static FloatArray2 add( FloatArray2 m1, FloatArray2 m2 ) {
        FloatArray2 m3 = new FloatArray2( m1 );
        m3.add( m2 );
        return m3;
    }
    
    public static void add( FloatArray2 m1, FloatArray2 m2, FloatArray2 m3 ) {
        m3.add( m1, m2 );
    }

    public static int getOffset( int rows, int cols, int row, int col ) {
        int offset = row * cols + col;
        return offset;
    }
    
    public static float get( FloatArray2 m1, int rows, int cols, int row, int col ) {
        int offset = getOffset( rows, cols, row, col );
        float value = m1._values[ offset ];
        return value;
    }

    public static void set( FloatArray2 m1, int rows, int cols, int row, int col, float value ) {
        int offset = getOffset( rows, cols, row, col );
        m1._values[ offset ] = value;
    }

    public static FloatArray2 sumRowsPerCol( FloatArray2 m1, int rows, int cols ) {
        FloatArray2 m3 = new FloatArray2( cols );
        
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

    public static FloatArray2 sumColsPerRow( FloatArray2 m1, int rows, int cols ) {
        FloatArray2 m3 = new FloatArray2( rows );
        
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
    
    public static FloatArray2 transpose( FloatArray2 m1, int rows1, int cols1 ) {
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
        FloatArray2 m3 = new FloatArray2( size );
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

    public static FloatArray2 scalarMul( FloatArray2 m1, float scalar ) {
        FloatArray2 m3 = new FloatArray2( m1 );
        m3.mul( scalar );
        return m3;
    }
    
}
