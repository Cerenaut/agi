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

import io.agi.core.math.RandomInstance;
import io.agi.core.orm.AbstractPair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

/**
 * A FloatArray with an associated DataSize object that describes the array as
 * an N-dimensional Hyperrectangle.
 *
 * @author dave
 */
public class Data extends FloatArray {

    public DataSize _dataSize = null;

    public static Data Vector( FloatArray fa ) {
        int size = fa.getSize();
        Data d = new Data( size );
        d.copy( fa );
        return d;
    }

    /**
     * Creates a 1-dimensional vector.
     *
     * @param size
     */
    public Data( int size ) {
        super( size );
        _dataSize = DataSize.create( size );
    }

    /**
     * Creates a 2-dimensional matrix or 32 bit float greyscale image.
     *
     * @param width
     * @param height
     */
    public Data( int width, int height ) {
        DataSize d = DataSize.create( width, height );
        setSize( d );
    }

    /**
     * Creates a 3D matrix
     *
     * @param width
     * @param height
     * @param depth
     */
    public Data( int width, int height, int depth ) {
        DataSize d = DataSize.create( width, height, depth );
        setSize( d );
    }

    /**
     * Creates a new data structure with the specified dimensions.
     *
     * @param d
     */
    public Data( DataSize d ) {
        setSize( d );
    }

    /**
     * Wraps the existing objects in a new Data object.
     * The elements and sizes are not copied.
     *
     * @param d
     * @param fa
     */
    public Data( DataSize d, FloatArray fa ) {
        _dataSize = d;
        _values = fa._values;
    }

    /**
     * Creates a Data as a deep copy of the parameter.
     *
     * @param d
     */
    public Data( Data d ) {
        _dataSize = new DataSize( d._dataSize );
        copy( d );
    }

    /**
     * Try to make a nice square shape from the given volume.
     * e.g. we don't have a 2-D shape with explicit w and h.
     */
    public static AbstractPair< Integer, Integer > getSizeSquare( int volume ) {

        int w = ( int ) Math.sqrt( volume );
        int h = w;

        int sq = w * w;
        if( sq < volume ) {
            h = h + 1;
        }

        AbstractPair< Integer, Integer > ap = new AbstractPair< Integer, Integer >( w, h );

        return ap;
    }

    public void setSize( DataSize d ) {
        _dataSize = new DataSize( d );
        int size = _dataSize.getVolume();
        setSize( size );
    }

    public float get( Coordinate c ) {
        int offset = c.offset();
        if( offset < _values.length ) {
            return _values[ offset ];
        }
        return 0.f;
    }

    public void set( Coordinate c, float value ) {
        int offset = c.offset();
        _values[ offset ] = value;
    }

    public boolean isSameAs( Data d ) {
        if( !_dataSize.isSameAs( d._dataSize ) ) {
            return false;
        }

        return super.isSameAs( ( FloatArray ) d );
    }

    public Coordinate begin() {
        return new Coordinate( _dataSize );
    }

    public Coordinate end() {
        Coordinate c = new Coordinate( _dataSize );
        c.setMax();
        return c;
    }

    public Data translate( Coordinate translations ) {

        Data v = new Data( _dataSize );

        Coordinate c1 = begin();

        int offset = 0;

        do { // for-each( value in volume )
            float value = _values[ offset ]; // don't want linearization again and validity checks

            ++offset;

            Coordinate c2 = new Coordinate( c1 );
            c2.translate( translations );

            v.set( c2, value ); // checks for validity
        } while( c1.next() ); // for-each( value in volume )

        return v;
    }

    public Collection< Coordinate > maxN( int n ) {
        Data v = new Data( _dataSize );

        float min = Float.MIN_VALUE;

        ArrayList< Coordinate > al = new ArrayList< Coordinate >();

        while( al.size() < n ) {
            Coordinate c = v.maxAt();

            al.add( c );

            v.set( c, min );
        }

        return al;
    }

    public Collection< Coordinate > minN( int n ) {
        Data v = new Data( _dataSize );

        float max = Float.MAX_VALUE;

        ArrayList< Coordinate > al = new ArrayList< Coordinate >();

        while( al.size() < n ) {
            Coordinate c = v.minAt();

            al.add( c );

            v.set( c, max );
        }

        return al;
    }

    public Coordinate maxAt() {

        float max = Float.MIN_VALUE;

        Coordinate c = begin();
        Coordinate cMax = c;

        do { // for-each( value in volume )
            float value = get( c );

            if( value > max ) {
                max = value;
                cMax = new Coordinate( c );
            }
        } while( c.next() ); // for-each( value in volume )

        return cMax;
    }

    public Coordinate minAt() {

        float min = Float.MAX_VALUE;

        Coordinate c = begin();
        Coordinate cMin = c;

        do { // for-each( value in volume )
            float value = get( c );

            if( value < min ) {
                min = value;
                cMin = new Coordinate( c );
            }
        } while( c.next() ); // for-each( value in volume )

        return cMin;
    }

    public Coordinate random( Random r ) {  // select a voxel proportional to its value as a weight, over all values

        // first sum the values, and handle the all-zero case as totally random:
        Coordinate c = begin();

        c.randomize( r );
        return c;
    }

    public ArrayList< Coordinate > roulette( int samples, Random r ) {  // select a voxel proportional to its value as a weight, over all values
        // first sum the values, and handle the all-zero case as totally random:

        ArrayList< Coordinate > al = new ArrayList< Coordinate >();

        double sum = sum();

        for( int i = 0; i < samples; ++i ) {
            if( sum <= 0.0 ) {
                Coordinate c = begin();
                c.randomize( r );
                al.add( c );
            } else {
                Coordinate c = roulette( r, sum );
                al.add( c );
            }
        }

        return al;
    }

    public Coordinate roulette() {  // select a voxel proportional to its value as a weight, over all values
        return roulette( RandomInstance.getInstance(), sum() );
    }

    public Coordinate roulette( Random r, double sum ) {  // select a voxel proportional to its value as a weight, over all values

        // first sum the values, and handle the all-zero case as totally random:
        Coordinate c = begin();

        if( sum <= 0.0 ) {
            c.randomize( r );
            return c;
        }

        // OK so now do a roulette selection:
        double random = r.nextDouble() * sum;
        double accumulated = 0.0;

        do { // for-each( value in volume )
            double value = get( c );

            accumulated += value;

            if( accumulated >= random ) {
                return c;
            }
        } while( c.next() ); // for-each( value in volume )

        return end(); // shouldn't happen!
    }

    public float sumSubVolume( int dimensionsExcluded, Coordinate included ) {

        // First we assume the user has specified the indices in all excluded
        // dimensions using the param "included".
        // e.g. in 5-d if dimsExcluded = 3, included = 12300
        // Then we compute a second coordinate which is 1 position beyond the
        // included range (ie the first excluded coordinate).
        // excluded should be 12400
        Coordinate excluded = new Coordinate( included );
        Coordinate offset = new Coordinate( _dataSize );
        offset._indices[ dimensionsExcluded - 1 ] = 1;

        excluded.add( offset );

        // Compute the offsets that these coordinates define, and apply within
        // this range:
        int offset1 = included.offset();
        int offset2 = excluded.offset();

        float sum = 0.0f;

        while( offset1 < offset2 ) {
            sum += _values[ offset1 ];
            ++offset1;
        }

        return sum;
    }

    public void mulSubVolume( int dimensionsExcluded, Coordinate included, float value ) {

        Coordinate excluded = new Coordinate( included );
        Coordinate offset = new Coordinate( _dataSize );
        offset._indices[ dimensionsExcluded - 1 ] = 1;

        excluded.add( offset );

        // Compute the offsets that these coordinates define, and apply within
        // this range:
        int offset1 = included.offset();
        int offset2 = excluded.offset();

        while( offset1 < offset2 ) {
            _values[ offset1 ] *= value;
            ++offset1;
        }
    }

    public void scaleSubVolume( int dimensionsExcluded, Coordinate included, float total ) {

        // formula:
        // x = x / sum
        // as reciprocal:
        // x = x * (1/sum)
        float sum = sumSubVolume( dimensionsExcluded, included );

        if( sum <= 0.0f ) {
            return;
        }

        float reciprocal = total / sum;

        mulSubVolume( dimensionsExcluded, included, reciprocal );
    }

    public int getVolumeExcluding( String invariantDimension ) {
        int volume = getSize();
        int size = _dataSize.getSize( invariantDimension );

        if( size == 0 ) {
            return volume;
        }

        volume /= size;

        return volume;
    }

}
