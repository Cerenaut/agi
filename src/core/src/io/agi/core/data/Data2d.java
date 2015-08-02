/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package io.agi.core.data;

import java.awt.Point;

/**
 * Functions for viewing and restructuring N-dimensional FloatArray2s as paintable
 * 2D shapes. Includes default conventions for row-major storage and making 
 * square shapes when only 1 dimensional data is given.
 *  
 * @author dave
 */
public class Data2d {
    
    /**
     * A function to determine a nice shape and size for a given FloatArray2, for
     * easy painting.
     * 
     * @param fa
     * @return Nice width and height
     */
    public static Point getSize( Data d ) {
        if( d == null ) {
            return null;
        }
        return getSize( d._d );
    }

    public static Point getSize( FloatArray2 fa ) {
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
        return getSizeExplicit( d._d );
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
        int w = (int)Math.sqrt( volume );
        int h = w;

        int sq = w * w;
        if( sq < volume ) {
            h = h +1;
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
        return y * stride +x;
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