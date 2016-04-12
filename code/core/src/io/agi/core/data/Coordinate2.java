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

import java.util.*;

/**
 * A point within an N-dimensional hyperrectangle.
 *
 * @author dave
 */
public class Coordinate2 implements Cloneable {

    public DataSize _d = null;
    public int[] _indices = null;

    public Coordinate2( DataSize d ) {
        _d = d;
        _indices = new int[ _d.getDimensions() ];
        setMin();
    }

    public Coordinate2( Coordinate2 c ) {
        _d = c._d;
        _indices = Arrays.copyOf( c._indices, c._indices.length );
    }

    public
    @Override
    Coordinate2 clone() {
        Coordinate2 c = null;

        try {
            c = ( Coordinate2 ) super.clone(); // will clone array.
        }
        catch( CloneNotSupportedException cnse ) {
            return null;
        }

        return c;
    }

    public boolean equivalent( Coordinate2 c ) {
        return Arrays.equals( _indices, c._indices );
    }

    @Override
    public boolean equals( Object o ) {
        if( this == o ) {
            return true;
        }

        if( !( o instanceof Coordinate2 ) ) {
            return false;
        }

        Coordinate2 c = ( Coordinate2 ) o; // allow throw of exception otherwise

        return equivalent( c );//Arrays.equals( _indices, c._indices );
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode( _indices );
    }

    @Override
    public String toString() {
        String s = new String();
        for( int d = 0; d < _indices.length; ++d ) {
            if( d > 0 ) s = s + ", ";
            s = s + _d.getLabel( d ) + "=" + _indices[ d ];
        }
        return s;
    }

    public void set( HashMap< String, Integer > dimensionValues ) {

        Set< Map.Entry< String, Integer > > s = dimensionValues.entrySet();

        for( Map.Entry< String, Integer > e : s ) {
            String label = ( String ) e.getKey();
            int n = ( Integer ) e.getValue().intValue();

            int index = _d.getIndex( label );

            if( index < _indices.length ) {
                set( index, n );
            }
        }
    }

    public int get( String d ) {
        int dIndex = _d.getIndex( d );
        return get( dIndex );
    }

    public int get( int d ) {
        return _indices[ d ];
    }

    public void set( String d, int n ) {
        int index = _d.getIndex( d );
        if( index < _indices.length ) {
            set( index, n );
        }
    }

    public void set( int d, int n ) {
        _indices[ d ] = n;
    }

    public void set( int n ) {
        Arrays.fill( _indices, n );
    }

    public void setMax() {
        int d = 0;
        int dimensions = _d.getDimensions();

        while( d < dimensions ) {
            _indices[ d ] = _d.getSize( d ) - 1;

            ++d;
        }
    }

    public void setMin() {
        int d = 0;
        int dimensions = _d.getDimensions();

        while( d < dimensions ) {
            _indices[ d ] = 0;

            ++d;
        }
    }

    public void setMin( String d ) {
        setMin( _d.getIndex( d ) );
    }

    public void setMin( int d ) {
        _indices[ d ] = 0;
    }

    public void setMax( String d ) {
        int index = _d.getIndex( d );
        if( index < _indices.length ) {
            setMax( index );
        }
    }

    public void setMax( int d ) {
        _indices[ d ] = _d.getSize( d ) - 1;
    }

    /**
     * Add the specified coordinate as offsets in each dimension.
     *
     * @param translations
     */
    public void translate( Coordinate2 translations ) {
        int dimensions = _indices.length;
        int d = 0;

        while( d < dimensions ) {

            int i = _indices[ d ];
            int delta = translations._indices[ d ];

            i += delta;

            _indices[ d ] = i;
            ++d;
        }
    }

    public double euclidean( Coordinate2 c ) {
        int sumSqDiff = sumSqDiff( c );
        return Math.sqrt( ( double ) sumSqDiff );
    }

    public int sumSqDiff( Coordinate2 c ) {
        int dimensions = _indices.length;
        int d = 0;
        int sumSq = 0;

        while( d < dimensions ) {

            int i1 = _indices[ d ];
            int i2 = c._indices[ d ];
            int diff = i1 - i2;
            int diffSq = diff * diff;

            sumSq += diffSq;
            ++d;
        }

        return sumSq;
    }

    public boolean next() {

        // watch out - never returns first [0,0,0]!
        // dimension 0 moves most slowly...
        // dimension n-1 moves fastest
        int dimensions = _d.getDimensions();
        int d = dimensions - 1;

        while( d >= 0 ) { // from (quantity-1) to 0
            if( _indices[ d ] < ( _d.getSize( d ) - 1 ) ) { // if not finished @ this dim
                ++_indices[ d ]; // increment this one

                // zero all subsequent quantity:
                ++d; //int d2 = d + 1;
                while( d < dimensions ) { // so at last dim, nothing else incremented.
                    _indices[ d ] = 0;
                    ++d; //int d2 = d + 1;
                }
                return true; // valid coordinate found
            }

            // we've reached the end of a dimension:
            --d;
        }

        return false; // finished.
    }

    /**
     * True if the coordinate is inside the specified dimensions' ranges.
     *
     * @return
     */
    public boolean inside() {
        int d = 0;
        int dimensions = _d.getDimensions();

        while( d < dimensions ) {
            int n = _indices[ d ];
            if( ( n < 0 )
                    || ( n >= _d.getSize( d ) ) ) {
                return false;
            }
            ++d;
        }

        return true;
    }

    public void setWithOffset( int offset0 ) {
//        offset0 = 24;
        //           dimension
        //                                     0           1
        //              0                      1           2
        //              /w*h                   /w         /1
        //
        // 3d        o = z * (width * height) + y * width + x
        // 2d        o =                        y * width + x
        // 1d        o =                                    x
        set( 0 );

        int dimensions = _d.getDimensions();
        int d = 0;
        int dMax = dimensions - 1;
        int offset = offset0;

        int[] subVolumes = new int[ dimensions ];
        int cumulative = 1;

        d = dMax;

        subVolumes[ d ] = cumulative;

        --d;

        while( d >= 0 ) {

            cumulative *= _d.getSize( d ); // e.g.

            subVolumes[ d ] = cumulative;

            --d;
        }

        d = 0;

        while( d <= dMax ) {
            int v = subVolumes[ d ];
            int q = offset / v;
            _indices[ d ] = q;
            offset %= v;
            ++d;
        }

        assert ( offset() == offset0 );
    }

    public int offset() {

        // 3d        o = z * (width * height) + y * width + x
        // 2d        o =                        y * width + x
        // 1d        o =                                    x
        int offset = 0;//o._indices[ 0 ];
        int cumulative = 1;
        int dimensions = _d.getDimensions();
        int d = dimensions - 1;

        while( d >= 0 ) {

            offset += ( _indices[ d ] * cumulative );

            cumulative *= _d.getSize( d );

            --d;
        }

        return offset;
    }
/*    public int offset() {

        // 3d        o = z * (width * height) + y * width + x
        // 2d        o =                        y * width + x
        // 1d        o =                                    x
        int offset = 0;//o._indices[ 0 ];
        int cumulative = 1;
        int dimension = 0;
        int quantity = Dimensions.quantity();

        while( dimension < quantity ) {

            offset += ( _indices[ dimension ] * cumulative );

            cumulative *= Dimensions.size( dimension );

            ++dimension;
        }

        return offset;
    }*/

    public boolean anyLessThan( Coordinate2 o ) {
        int dimension = 0;
        int dimensions = _d.getDimensions();

        while( dimension < dimensions ) {

            int nThis = _indices[ dimension ];
            int nThat = o._indices[ dimension ];

            ++dimension;

            if( nThis < nThat ) {
                return true;
            }
        }

        return false;
    }

    public boolean anyGreaterThan( Coordinate2 o ) {
        int dimension = 0;
        int dimensions = _d.getDimensions();

        while( dimension < dimensions ) {

            int nThis = _indices[ dimension ];
            int nThat = o._indices[ dimension ];

            ++dimension;

            if( nThis > nThat ) {
                return true;
            }
        }

        return false;
    }

    public void add( Coordinate2 c ) {
        int dimension = 0;
        int dimensions = _d.getDimensions();

        while( dimension < dimensions ) {

            _indices[ dimension ] += c._indices[ dimension ];

            ++dimension;
        }
    }

    public void mul( Coordinate2 c ) {
        int dimension = 0;
        int dimensions = _d.getDimensions();

        while( dimension < dimensions ) {

            _indices[ dimension ] *= c._indices[ dimension ];

            ++dimension;
        }
    }

    public void add( double value ) {
        int dimension = 0;
        int dimensions = _d.getDimensions();

        while( dimension < dimensions ) {

            _indices[ dimension ] += value;

            ++dimension;
        }
    }

    public void mul( double value ) {
        int dimension = 0;
        int dimensions = _d.getDimensions();

        while( dimension < dimensions ) {

            _indices[ dimension ] *= value;

            ++dimension;
        }
    }

    public static int volume( Coordinate2 c1, Coordinate2 c2 ) {

        int dimension = 0;
        int volume = 1;

        while( dimension < c1._indices.length ) {

            int n1 = c1._indices[ dimension ];
            int n2 = c2._indices[ dimension ];

            int range = Math.abs( n2 - n1 ) + 1;

            volume *= range;

            ++dimension;
        }

        return volume;
    }

    public static Coordinate2 lowerBound( Coordinate2 c1, Coordinate2 c2 ) {
        Coordinate2 c3 = new Coordinate2( c1._d );

        int dimensions = c1._d.getDimensions();
        int d = dimensions - 1;

        while( d >= 0 ) { // from (quantity-1) to 0

            int n1 = c1._indices[ d ];
            int n2 = c2._indices[ d ];
            int min = Math.min( n1, n2 );
//            int max = Math.max( n1, n2 ) +1;

            c3._indices[ d ] = min;

            --d;
        }

        return c3;
    }

    public boolean nextBounded( Coordinate2 c1, Coordinate2 c2 ) {

        // dimension 0 moves most slowly...
        // dimension n-1 moves fastest
        int dimensions = c1._d.getDimensions();
        int d = dimensions - 1;

        while( d >= 0 ) { // from (quantity-1) to 0

            int n1 = c1._indices[ d ];
            int n2 = c2._indices[ d ];
            int max = Math.max( n1, n2 ) + 1;

            if( _indices[ d ] < max ) { // if not finished @ this dim
                ++_indices[ d ]; // increment this one

                // zero all subsequent quantity:
                ++d; // start with d+1

                while( d < dimensions ) { // so at last dim, nothing else incremented.

                    n1 = c1._indices[ d ];
                    n2 = c2._indices[ d ];
                    int min = Math.min( n1, n2 );

                    _indices[ d ] = min;
                    ++d; //int d2 = d + 1;
                }
                return true; // valid coordinate found
            }

            // we've reached the end of a dimension:
            --d;
        }

        return false; // finished.
    }

    public static int volumeBounded( Coordinate2 c1, Coordinate2 c2 ) { // size of the volume defined by these 2 ordinates,

        // dimension 0 moves most slowly...
        int size = 1;
        int d = 0;
        int dimensions = c1._d.getDimensions();

        while( d < dimensions ) {
            int n1 = c1._indices[ d ];
            int n2 = c2._indices[ d ];

            int range = Math.abs( n2 - n1 ) + 1;

            size *= range;

            ++d;
        }

        return size;
    }

//    public void randomize() {
//        randomize( RandomInstance.NodeInstance() );
//    }

    public void randomize( Random r ) {

        // dimension 0 moves most slowly...
        int d = 0;
        int dimensions = _d.getDimensions();

        while( d < dimensions ) {

            int size = _d.getSize( d );

            _indices[ d ] = r.nextInt( size );

            ++d;
        }
    }

}
