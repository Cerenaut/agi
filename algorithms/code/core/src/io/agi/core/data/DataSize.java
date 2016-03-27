/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package io.agi.core.data;

import java.util.*;

/**
 * The dimensions of a data object, an N dimensional rectangle (or hyperrectangle).
 * https://en.wikipedia.org/wiki/Hyperrectangle
 * In addition to sizes, dimensions also have human readable names.
 *
 * @author dave
 */
public class DataSize {

    // conventional (unenforced) naming
    public static final String DIMENSION_X = "x";
    public static final String DIMENSION_Y = "y";
    public static final String DIMENSION_Z = "z";

    // convenience methods up to 3D
    public static DataSize create( int size ) {
        return create( DIMENSION_X, size );
    }

    public static DataSize create( String name, int size ) {
        DataSize d = new DataSize( 1 );
        d.set( 0, size, name );
        return d;
    }

    public static DataSize create( int x, int y ) {
        return create( DIMENSION_X, x, DIMENSION_Y, y );
    }

    public static DataSize create( String xName, int x, String yName, int y ) {
        DataSize d = new DataSize( 2 );
        d.set( 0, y, yName );
        d.set( 1, x, xName ); // contiguous values in x
        return d;
    }

    public static DataSize create( int x, int y, int z ) {
        return create( DIMENSION_X, x, DIMENSION_Y, y, DIMENSION_Z, z );
    }

    public static DataSize create( String xName, int x, String yName, int y, String zName, int z ) {
        DataSize d = new DataSize( 3 );
        d.set( 0, z, zName );
        d.set( 1, y, yName );
        d.set( 2, x, xName ); // contiguous values in x
        return d;
    }

    // Implementation
    public int[] _sizes = null;
    public HashMap< String, Integer > _labels = new HashMap< String, Integer >();

    public DataSize( int dimensions ) {
        setDimensions( dimensions );
    }

    public DataSize( DataSize d ) {
        int dimensions = d.getDimensions();
        _sizes = Arrays.copyOf( d._sizes, dimensions );

        Set< Map.Entry< String, Integer > > es = d._labels.entrySet();

        Iterator i = es.iterator();

        while ( i.hasNext() ) {
            Map.Entry< String, Integer > e = ( Map.Entry< String, Integer > ) ( i.next() );
            _labels.put( e.getKey(), e.getValue() );
        }
    }

    /**
     * Set all properties of the given dimension
     *
     * @param d
     * @param size
     * @param label
     */
    public void set( int d, int size, String label ) {
        setSize( d, size );
        setLabel( d, label );
    }

    public int getDimensions() {
        return _sizes.length;
    }

    public void setDimensions( int dimensions ) {
        _sizes = new int[ dimensions ];
    }

    public boolean isSameAs( DataSize d ) {
        try {
            if ( _sizes.length != d._sizes.length ) return false;

            int i = 0;

            while ( i < _sizes.length ) {
                if ( _sizes[ i ] != d._sizes[ i ] ) {
                    return false;
                }

                ++i;
            }

            Set< String > keys = _labels.keySet();
            if ( !keys.equals( d._labels.keySet() ) ) {
                return false;
            }

            Collection< Integer > valuesThis = _labels.values();
            Collection< Integer > valuesThat = d._labels.values();

            for ( String key : keys ) {
                Integer valueThis = _labels.get( key );
                Integer valueThat = d._labels.get( key );

                if ( !valueThis.equals( valueThat ) ) {
                    return false;
                }
            }
            return true;
        }
        catch ( Exception e ) {
            return false;
        }
    }

    public int getVolume() {
        int volume = 1;

        int i = 0;

        while ( i < _sizes.length ) {

            volume *= _sizes[ i ];

            ++i;
        }

        return volume;
    }

    public int getSize( String s ) {
        Integer i = _labels.get( s );

        if ( i == null ) {
            return 0;
        }

        return _sizes[ i ];
    }

    public int getSize( int dimension ) {
        return _sizes[ dimension ];
    }

    public void setSize( int d, int size ) {
        _sizes[ d ] = size;
    }

    public void setLabel( int d, String label ) {
        _labels.put( label, d );
    }

    public String getLabel( int d ) {

        Set< Map.Entry< String, Integer > > s = _labels.entrySet();

        for ( Map.Entry< String, Integer > e : s ) {
            String label = ( String ) e.getKey();
            int n = ( Integer ) e.getValue().intValue();

            if ( n == d ) {
                return label;
            }
        }

        return null;
    }

    public Integer getIndex( String label ) {
        Integer i = _labels.get( label );

        if ( i == null ) {
            return null;//_sizes.length;
        }

        return i;//i.intValue();
    }

}
