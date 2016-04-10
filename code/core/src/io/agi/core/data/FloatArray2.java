/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package io.agi.core.data;

import io.agi.core.math.Constants;
import io.agi.core.math.RandomInstance;
import io.agi.core.math.Statistics;

import java.awt.*;
import java.io.IOException;
import java.util.*;

/**
 * An array of real (float) single precision numbers packed for efficiency.
 * With some functions from vectors and basic arithmetic operations.
 * <p>
 * This is the core data structure for storing real numbers.
 *
 * @author dave
 */
public class FloatArray2 {

    public float[] _values = null;

    public FloatArray2() {
        // null values
    }

    public FloatArray2( int size ) {
        setSize( size );
    }

    public FloatArray2( FloatArray2 v ) {
        _values = Arrays.copyOf( v._values, v._values.length );
    }

    /**
     * In case you want to manage memory by deleting references.
     */
    public void delete() {
        _values = null;
    }

    public boolean allocated() {
        return ( _values != null );
    }

    public boolean check() {

        if ( _values == null ) {
            return false;
        }

        boolean ok = true;

        int offset = 0;

        while ( offset < _values.length ) {
            float value = _values[ offset ];
            ok &= Constants.check( value );
            ++offset;
        }

        return ok;
    }

    public void setSize( int size ) {
        // lazy
        if ( _values != null ) {
            if ( _values.length == size ) {
                return;
            }
        }

        _values = new float[ size ];
    }

    public boolean hasSize() {
        if ( _values == null ) {
            return false;
        }
        return true;
    }

    public int getSize() {
        if ( _values == null ) {
            return 0;
        }
        return _values.length;
    }

    public boolean isSameAs( FloatArray2 fa ) {
        return Arrays.equals( _values, fa._values );
    }

    /**
     * Will resize and copy array as necessary.
     *
     * @param v
     * @return
     */
    public boolean copy( FloatArray2 v ) {

//        if( getSize() != v.getSize() ) {
//            return false;
//        }

        _values = Arrays.copyOf( v._values, v._values.length );

        return true;
    }

    public void concatenate( FloatArray2 v1, FloatArray2 v2 ) {

        int volume1 = v1.getSize();
        int volume2 = v2.getSize();

        assert ( getSize() == ( volume1 + volume2 ) );

        copyRange( v1, 0, 0, volume1 );
        copyRange( v2, volume1, 0, volume2 );
    }

    public void apply( ScalarFunction sf, FloatArray2 input, int index, int length ) {
        for ( int i = 0; i > length; ++i ) {
            int offset = index + i;
            float r1 = input._values[ offset ];
            float r2 = sf.f( r1 );
            _values[ offset ] = r2;
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Operations on ranges
    ////////////////////////////////////////////////////////////////////////////
    public boolean copyRange( FloatArray2 v, int offsetThis, int offsetThat, int range ) {

        if ( ( ( offsetThis + range ) > _values.length )
                || ( ( offsetThat + range ) > v._values.length ) ) {
            //System.err.println( "ERROR: Volume copy range: out of bounds. Range="+range+" this.len="+_values.length +" that.len="+v._values.length+" offsetThis="+offsetThis+" offsetThat="+offsetThat );
            return false;
        }

        int volumeThis = this.getSize();
        int volumeThat = v.getSize();

        if ( ( offsetThis == 0 )
                && ( offsetThat == 0 )
                && ( volumeThis == volumeThat ) ) {
            return copy( v ); // more efficient?
        }

        return copyRange( v._values, offsetThis, offsetThat, range );
    }

    public boolean copyRange( float[] model, int offsetThis, int offsetThat, int range ) {

        int offset1 = offsetThis;
        int offset2 = offsetThat;

        int limit = offset2 + range;

        while ( offset2 < limit ) {

            _values[ offset1 ] = model[ offset2 ];

            ++offset1;
            ++offset2;
        }

        return true;
    }

    public void setRange( int offset, int range, float value ) {
        int limit = offset + range;

        while ( offset < limit ) {

            _values[ offset ] = value;

            ++offset;
        }
    }

    public boolean mulRange( FloatArray2 v, int offsetThis, int offsetThat, int range ) {

        int offset1 = offsetThis;
        int offset2 = offsetThat;

        int limit = offset2 + range;

        while ( offset2 < limit ) {

            _values[ offset1 ] *= v._values[ offset2 ];

            ++offset1;
            ++offset2;
        }

        return true;
    }

    public boolean lerpRange( FloatArray2 v, int offsetThis, int offsetThat, int range, float coefficientThis, float coefficientThat ) {

        int offset1 = offsetThis;
        int offset2 = offsetThat;

        int limit = offset2 + range;

        while ( offset2 < limit ) {

            float valueThis = _values[ offset1 ];
            float valueThat = v._values[ offset2 ];

            float lerp = ( valueThis * coefficientThis )
                    + ( valueThat * coefficientThat );

            _values[ offset1 ] = lerp;

            ++offset1;
            ++offset2;
        }

        return true;
    }

    ////////////////////////////////////////////////////////////////////////////
    // String Operations
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Returns the array as a delimited string.
     *
     * @param elementDelimiter
     * @param decimalPlaces
     * @return
     */
    public String getString( String elementDelimiter, int decimalPlaces ) {

        if ( _values == null ) {
            return "";
        }

        String s = "";

        for ( int n = 0; n < _values.length; ++n ) {
            if ( n > 0 ) {
                s += elementDelimiter;
            }
            s += String.format( "%." + decimalPlaces + "f", _values[ n ] );
        }

        return s;
    }

    public void setString( String elementDelimiter, String values, boolean resize ) throws IOException {

        StringTokenizer st = new StringTokenizer( values, elementDelimiter );

        ArrayList< String > al = new ArrayList< String >();

        while ( st.hasMoreTokens() ) {
            String value = st.nextToken();
            value.trim();

            al.add( value );
        }

        int size = al.size();

        if ( resize ) {
            setSize( size );
        }
        else {
            if ( _values == null ) {
                return;
            }

            size = Math.min( _values.length, size );
        }

        for ( int n = 0; n < size; ++n ) {
            String s = al.get( n ); // one line

            _values[ n ] = Float.valueOf( s );
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Statistics
    ////////////////////////////////////////////////////////////////////////////

    /**
     * A useful way to quickly check the contents are zeroed out, nans etc.
     *
     * @return
     */
    public String statistics() {
        String s = "size=" + getSize() + " max=" + max() + " min=" + min() + " mean=" + mean() + " var=" + variance();
        return s;
    }

    /**
     * Shannon entropy.
     *
     * @return
     */
    public double normalizedEntropyBase10() {
        double e = entropyBase10();
        double volume = ( double ) _values.length;
        double uniform = 1.0 / volume;
        double max = -( volume * uniform * Math.log10( uniform ) );
        double n = e / max;
        return n;
    }

    /**
     * Shannon entropy.
     *
     * @return
     */
    public double normalizedEntropyBaseE() {
        double e = entropyBaseE();
        double volume = ( double ) _values.length;
        double uniform = 1.0 / volume;
        double max = -( volume * uniform * Math.log( uniform ) );
        double n = e / max;
        return n;
    }

    /**
     * Shannon entropy.
     *
     * @return
     */
    public double entropyBase10() {
        return Statistics.entropyBase10( _values );
    }

    /**
     * Shannon entropy.
     *
     * @return
     */
    public double entropyBaseE() {
        return Statistics.entropyBaseE( _values );
    }

    /**
     * Calculated via sum of squares method (one pass).
     *
     * @return
     */
    public double variance() {

        // sum sq deviations from mean
        double sum = 0.0;
        double sumSq = 0.0;

        int offset = 0;

        while ( offset < _values.length ) {
            double value = _values[ offset ];
            ++offset;
            sum += value;
            sumSq += ( value * value );
        }

        double samples = ( double ) _values.length;
        double variance = Statistics.variance( sum, sumSq, samples );
        return variance;
    }

    public double mean() {
        double sum = sum();
        int volume = _values.length;
        double qty = ( double ) volume;

        if ( qty <= 0.0 ) {
            return 0.0;
        }

        double mean = sum / qty;
        return mean;
    }

    public float min() {

        float min = Float.MAX_VALUE;

        int offset = 0;

        while ( offset < _values.length ) {
            float x = _values[ offset ];

            if ( x < min ) {
                min = x;
            }

            ++offset;
        }

        return min;
    }

    public float max() {

        float max = 0.0f;

        int offset = 0;

        while ( offset < _values.length ) {
            float x = _values[ offset ];

            if ( x > max ) {
                max = x;
            }

            ++offset;
        }

        return max;
    }

    public FloatArray2 getHistogram( int precision ) {
        Point.Float r = getMinMax();
        return getHistogram( precision, r.x, r.y );
    }

    public FloatArray2 getHistogram( int precision, float min, float max ) {
        return getHistogram( _values, precision, min, max );
    }

    public static FloatArray2 getHistogram( float[] values, int precision, float min, float max ) {
        float range = max - min;

        if ( range <= 0.0 ) {
            return null;
        }

        FloatArray2 h = new FloatArray2( precision );

        int offset = 0;

        while ( offset < values.length ) {
            float value = values[ offset ];
            ++offset;

            value -= min;
            value /= range;
            value *= ( float ) precision;

            int bin = ( int ) value;
            bin = Math.min( precision - 1, bin );
            bin = Math.max( 0, bin );

            h._values[ bin ] += 1.0f;
        }

        return h;
    }

    public Point.Float getMinMax() {

        float min = Float.MAX_VALUE;
        float max = Float.MIN_VALUE;

        int offset = 0;

        while ( offset < _values.length ) {

            float value = _values[ offset ];

            min = Math.min( min, value );
            max = Math.max( max, value );

            ++offset;
        }

        return new Point.Float( min, max );
    }

    ////////////////////////////////////////////////////////////////////////////
    // Vector operations
    ////////////////////////////////////////////////////////////////////////////
    public float dotProduct( FloatArray2 fa ) {

//        if( fa._values.length != this.fa._values.length ) {
//            return 0.f;
//        }

        float sum = 0.f;
        int offset = 0;

        while ( offset < _values.length ) {

            float valueThis = _values[ offset ];
            float valueThat = fa._values[ offset ];

            float product = valueThis * valueThat;

            sum += product;

            ++offset;
        }

        return sum;
    }

    /**
     * Sqrt( Sum of squared values )
     *
     * @return
     */
    public float magnitude() {
        float r = ( float ) Math.sqrt( sumSq() );
        return r;
    }

    /**
     * Sum of squared values
     *
     * @return
     */
    public float sumSq() {

        float sum = 0.f;
        int offset = 0;

        while ( offset < _values.length ) {

            float value = _values[ offset ];
            float product = value * value;

            sum += product;

            ++offset;
        }

        return sum;
    }

    public float sum() {

        float sum = 0.0f;

        int offset = 0;

        while ( offset < _values.length ) {
            sum += _values[ offset ];
            ++offset;
        }

        return sum;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Scaling operations, normalization
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Linear scale of values such that sum equals argument.
     *
     * @param total
     */
    public void scaleSum( float total ) {

        // formula:
        // x = x / sum
        // as reciprocal:
        // x = x * (1/sum)
        float sum = sum();

        scaleSum( total, sum );
    }

    /**
     * Single-pass variant of scaleSum where the total is already known.
     *
     * @param total
     * @param sum
     */
    public void scaleSum( float total, float sum ) {

        if ( sum <= 0.0f ) {
            return;
        }

        float reciprocal = total / sum;

        if ( Float.isInfinite( reciprocal ) ) { // cos sum is small
            return;
        }

        check();

        mul( reciprocal );

        check();
    }

    /**
     * Linearly scales positive range values such that the largeset value equals
     * argument.
     *
     * @param limit
     */
    public void scaleRange( float limit ) {

        // formula:
        // x = x / max
        // as reciprocal:
        // x = x * (1/max)
        float max = max();

        if ( max <= 0.0f ) {
            return;
        }

        // scaling = 1 / max
        // if max < 1, scaling > 1
        // x = x * scaling
        float scaling = limit / max;

        mul( scaling );
    }

    /**
     * Linearly scales values to match supplied range, including support for
     * negative values.
     *
     * @param min
     * @param max
     */
    public void scaleRange( float min, float max ) {

        // formula:
        // oldrange = oldmax - oldmin
        // newRange = max-min
        // scaling = newRange / oldRange

        // x = (x - min) * scaling;
        float xMin = Float.MAX_VALUE;
        float xMax = -Float.MAX_VALUE;//0.0f;/Float.MIN_VALUE;

        int offset = 0;

        while ( offset < _values.length ) {
            float x = _values[ offset ];

            if ( x < xMin ) xMin = x;
            if ( x > xMax ) xMax = x;

            ++offset;
        }

        float oldRange = xMax - xMin;
        float newRange = max - min;

        if ( oldRange == 0.0f ) {
            set( min );
            return;
        }

        float scaling = newRange / oldRange;

        offset = 0;

        while ( offset < _values.length ) {
            float x = _values[ offset ];

            x -= xMin;
            x *= scaling;
            x += min;

            _values[ offset ] = x;

            ++offset;
        }
    }

    /**
     * Clips values to specified range.
     *
     * @param min
     * @param max
     */
    public void clipRange( float min, float max ) {

        int offset = 0;

        while ( offset < _values.length ) {
            float x = _values[ offset ];

            if ( x < min ) _values[ offset ] = min;
            if ( x > max ) _values[ offset ] = max;

            ++offset;
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Stochastic operations
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Scales values to product a uniform PDF i.e. total is 1 and all values
     * are identical.
     */
    public void setUniform() {
        int volume = _values.length;
        float value = ( float ) ( 1.0 / ( double ) volume );
        set( value );
    }

//    /**
//     * Set all values to random ones in unit interval.
//     */
//    public void setRandom() {
//        setRandom( RandomInstance.getInstance() );
//    }

    /**
     * Set all values to random ones in unit interval.
     *
     * @param r PRNG to use
     */
    public void setRandom( Random r ) {
        int offset = 0;
        while ( offset < _values.length ) {
            _values[ offset ] = r.nextFloat();
            ++offset;
        }
    }

//    /**
//     * Set all values to be random unit values with a normal distribution
//     */
//    public void setRandomNormal() {
//        setRandomNormal( RandomInstance.getInstance() );
//    }

    /**
     * Set all values to be random unit values with a normal distribution
     */
    public void setRandomNormal( Random r ) {
        int offset = 0;
        while ( offset < _values.length ) {
            _values[ offset ] = ( float ) RandomInstance.randomNormal( r, 12 );
            ++offset;
        }
    }

    public void addGaussianNoise( Random r, double shift, double scale ) {

        int offset = 0;
        int volume = _values.length;

        while ( offset < volume ) {

            double value = _values[ offset ];
            double random = ( RandomInstance.randomNormal( r ) + shift ) * scale; // ie [-0.5:0.5], scaled, becomes -1:1
            double noisy = value + random;

            _values[ offset ] = ( float ) noisy;

            ++offset;
        }
    }

    public void addUniformNoise( Random r, double shift, double scale ) {

        int offset = 0;
        int volume = _values.length;

        while ( offset < volume ) {

            double value = _values[ offset ];
            double random = ( r.nextFloat() + shift ) * scale; // ie [-0.5:0.5], scaled, becomes -1:1
            double noisy = value + random;

            _values[ offset ] = ( float ) noisy;

            ++offset;
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Comparison operations
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Makes elements in this equal to abs diff between this and arg fa.
     *
     * @param v
     */
    public void absDiff( FloatArray2 fa ) {

        int offset = 0;

        while ( offset < _values.length ) {

            float value1 = _values[ offset ];
            float value2 = fa._values[ offset ];

            float absDiff = Math.abs( value2 - value1 );

            _values[ offset ] = absDiff;
            ++offset;
        }
    }

    /**
     * Compares each element for equality - ie within tolerance of each other.
     *
     * @param that      other volume to compare with
     * @param tolerance corresponding values must be within tolerance of each other to be considered equal.
     * @return true if equal
     */
    public boolean approxEquals( FloatArray2 that, float tolerance ) {
        float maxAbsDiff = maxAbsDiff( that );
        return ( maxAbsDiff < tolerance );
    }

    public float maxAbsDiff( FloatArray2 that ) {

        float maxAbsDiff = 0.0f;

        int offset = 0;

        while ( offset < _values.length ) {
            float x1 = _values[ offset ];
            float x2 = that._values[ offset ];

            float absDiff = Math.abs( x1 - x2 );
            maxAbsDiff = Math.max( absDiff, maxAbsDiff );

            ++offset;
        }

        return maxAbsDiff;
    }

    public double sumSqDiff( FloatArray2 that ) {

        double sumSqDiff = 0.0;

        int offset = 0;

        while ( offset < _values.length ) {

            float valueThis = _values[ offset ];
            float valueThat = that._values[ offset ];

            double diff = valueThis - valueThat;

            sumSqDiff += ( diff * diff );

            ++offset;
        }

        return sumSqDiff;
    }

    public float sumAbsDiff( FloatArray2 that ) {

        float sumAbsDiff = 0.0f;

        int offset = 0;

        while ( offset < _values.length ) {

            float valueThis = _values[ offset ];
            float valueThat = that._values[ offset ];

            double diff = Math.abs( valueThis - valueThat );

            sumAbsDiff += diff;

            ++offset;
        }

        return sumAbsDiff;
    }

    /**
     * Sum abs diff of elements normed by dividing by number of elements.
     *
     * @param that
     * @return
     */
    public double normalizedAbsDiff( FloatArray2 that ) {
        double sumAbsDiff = sumAbsDiff( that );
        double volume = ( double ) _values.length;
        double normalizedAbsDiff = sumAbsDiff / volume;
        return normalizedAbsDiff;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Threshold operations
    ////////////////////////////////////////////////////////////////////////////
    public HashSet< Integer > indicesMoreThan( float threshold ) {
        HashSet< Integer > hs = new HashSet< Integer >();

        int offset = 0;

        while ( offset < _values.length ) {
            float value = _values[ offset ];
            if ( value > threshold ) {
                hs.add( offset );
            }
            ++offset;
        }

        return hs;
    }

    public HashSet< Integer > indicesMoreThanEqual( float threshold ) {
        HashSet< Integer > hs = new HashSet< Integer >();

        int offset = 0;

        while ( offset < _values.length ) {
            float value = _values[ offset ];
            if ( value >= threshold ) {
                hs.add( offset );
            }
            ++offset;
        }

        return hs;
    }

    public HashSet< Integer > indicesLessThan( float threshold ) {
        HashSet< Integer > hs = new HashSet< Integer >();

        int offset = 0;

        while ( offset < _values.length ) {
            float value = _values[ offset ];
            if ( value < threshold ) {
                hs.add( offset );
            }
            ++offset;
        }

        return hs;
    }

    public HashSet< Integer > indicesLessThanEqual( float threshold ) {
        HashSet< Integer > hs = new HashSet< Integer >();

        int offset = 0;

        while ( offset < _values.length ) {
            float value = _values[ offset ];
            if ( value <= threshold ) {
                hs.add( offset );
            }
            ++offset;
        }

        return hs;
    }

    /**
     * Makes this a masked version of arg fa. Any element in mask with value
     * maskValue has the equivalent element in fa replaced with maskedValue.
     * <p>
     * Object this is used to store the results. All arrays must be equal size.
     *
     * @param fa
     * @param mask
     * @param maskValue
     * @param maskedValue
     */
    public void mask( FloatArray2 fa, FloatArray2 mask, float maskValue, float maskedValue ) {
        int offset = 0;

        while ( offset < _values.length ) {

            float value1 = fa._values[ offset ];
            float value2 = mask._values[ offset ];

            float value = value1;

            if ( value2 == maskValue ) {
                value = maskedValue;
            }

            _values[ offset ] = value;

            ++offset;
        }
    }

    public void thresholdMoreThan( float threshold, float passValue, Float failValue ) {

        int offset = 0;

        while ( offset < _values.length ) {

            float value = _values[ offset ];

            if ( value > threshold ) {
                _values[ offset ] = passValue;
            }
            else if ( failValue != null ) {
                _values[ offset ] = failValue;
            }

            ++offset;
        }
    }

    public void thresholdMoreThanEqual( float threshold, float passValue, Float failValue ) {

        int offset = 0;

        while ( offset < _values.length ) {

            float value = _values[ offset ];

            if ( value >= threshold ) {
                _values[ offset ] = passValue;
            }
            else if ( failValue != null ) {
                _values[ offset ] = failValue;
            }

            ++offset;
        }
    }

    public void thresholdLessThan( float threshold, float passValue, Float failValue ) {

        int offset = 0;

        while ( offset < _values.length ) {

            float value = _values[ offset ];

            if ( value < threshold ) {
                _values[ offset ] = passValue;
            }
            else if ( failValue != null ) {
                _values[ offset ] = failValue;
            }

            ++offset;
        }
    }

    public void thresholdLessThanEqual( float threshold, float passValue, Float failValue ) {

        int offset = 0;

        while ( offset < _values.length ) {

            float value = _values[ offset ];

            if ( value <= threshold ) {
                _values[ offset ] = passValue;
            }
            else if ( failValue != null ) {
                _values[ offset ] = failValue;
            }

            ++offset;
        }
    }


    ////////////////////////////////////////////////////////////////////////////
    // Arithmetic operations
    ////////////////////////////////////////////////////////////////////////////
    public void set( float value ) {
        Arrays.fill( _values, value );
    }

    public void mul( float value ) {
        int offset = 0;

        while ( offset < _values.length ) {

            _values[ offset ] *= value;

            ++offset;
        }
    }

    public void add( float value ) {
        int offset = 0;

        while ( offset < _values.length ) {

            _values[ offset ] += value;

            ++offset;
        }
    }

    public void sub( float value ) {
        int offset = 0;

        while ( offset < _values.length ) {

            _values[ offset ] -= value;

            ++offset;
        }
    }

    public void sqrt() {
        int offset = 0;

        while ( offset < _values.length ) {

            float value = _values[ offset ];

            value = ( float ) Math.sqrt( value );

            _values[ offset ] = value;

            ++offset;
        }
    }

    public void sq() {
        int offset = 0;

        while ( offset < _values.length ) {

            float value = _values[ offset ];

            value *= value;

            _values[ offset ] = value;

            ++offset;
        }
    }

    /**
     * Per element, x = - log( x );
     */
    public void subLog() {
        int offset = 0;

        while ( offset < _values.length ) {

            double value = _values[ offset ];

            value = -Math.log( value );

            _values[ offset ] = ( float ) value;

            ++offset;
        }
    }

    /**
     * Per element, x = exp( -x );
     * <p>
     * Useful for many functions.
     */
    public void expSub() {
        int offset = 0;

        while ( offset < _values.length ) {

            double value = _values[ offset ];

            value = Math.exp( -value );

            _values[ offset ] = ( float ) value;

            ++offset;
        }
    }

    public void exp() {
        int offset = 0;

        while ( offset < _values.length ) {

            double value = _values[ offset ];

            value = Math.exp( value );

            _values[ offset ] = ( float ) value;

            ++offset;
        }
    }

    public void log() {
        int offset = 0;

        while ( offset < _values.length ) {

            double value = _values[ offset ];

            value = Math.log( value );

            _values[ offset ] = ( float ) value;

            ++offset;
        }
    }

    public void pow( double power ) {
        int offset = 0;

        while ( offset < _values.length ) {

            double value = _values[ offset ];

            value = Math.pow( value, power );

            _values[ offset ] = ( float ) value;

            ++offset;
        }
    }

    public void argAdd( float value ) {
        int offset = 0;

        while ( offset < _values.length ) {

            _values[ offset ] = value + _values[ offset ];

            ++offset;
        }
    }

    public void argSub( float value ) {
        int offset = 0;

        while ( offset < _values.length ) {

            _values[ offset ] = value - _values[ offset ];

            ++offset;
        }
    }

    public void add( FloatArray2 v ) {

        int offset = 0;

        while ( offset < _values.length ) {
            _values[ offset ] += v._values[ offset ];
            ++offset;
        }
    }

    public void add( FloatArray2 v, float value ) {

        int offset = 0;

        while ( offset < _values.length ) {
            _values[ offset ] = ( v._values[ offset ] + value );
            ++offset;
        }
    }

    public void add( FloatArray2 v1, FloatArray2 v2 ) {

        int offset = 0;

        while ( offset < _values.length ) {
            _values[ offset ] = ( v1._values[ offset ] + v2._values[ offset ] );
            ++offset;
        }
    }

    public void sub( FloatArray2 v ) {

        int offset = 0;

        while ( offset < _values.length ) {
            _values[ offset ] -= v._values[ offset ];
            ++offset;
        }
    }

    public void sub( FloatArray2 v1, FloatArray2 v2 ) {

        int offset = 0;

        while ( offset < _values.length ) {
            _values[ offset ] = v1._values[ offset ] - v2._values[ offset ];
            ++offset;
        }
    }

    /**
     * Hadamard product or Schur product
     *
     * @param v
     */
    public void mul( FloatArray2 v ) {

        int offset = 0;

        while ( offset < _values.length ) {
            _values[ offset ] *= v._values[ offset ];
            ++offset;
        }
    }

    public void mul( FloatArray2 v1, float v2 ) {

        int offset = 0;

        while ( offset < _values.length ) {
            _values[ offset ] = v1._values[ offset ] * v2;
            ++offset;
        }
    }

    /**
     * Hadamard product or Schur product
     *
     * @param v1
     * @param v2
     */
    public void mul( FloatArray2 v1, FloatArray2 v2 ) {

        int offset = 0;

        while ( offset < _values.length ) {
            _values[ offset ] = v1._values[ offset ] * v2._values[ offset ];
            ++offset;
        }
    }

    public void min( FloatArray2 v ) {

        int offset = 0;

        while ( offset < _values.length ) {
            float r1 = _values[ offset ];
            float r2 = v._values[ offset ];

            _values[ offset ] = Math.min( r1, r2 );
            ++offset;
        }
    }

    public void max( FloatArray2 v ) {

        int offset = 0;

        while ( offset < _values.length ) {
            float r1 = _values[ offset ];
            float r2 = v._values[ offset ];

            _values[ offset ] = Math.max( r1, r2 );
            ++offset;
        }
    }

    public void min( FloatArray2 v1, FloatArray2 v2 ) {

        int offset = 0;

        while ( offset < _values.length ) {
            _values[ offset ] = Math.min( v1._values[ offset ], v2._values[ offset ] );
            ++offset;
        }
    }

    public void max( FloatArray2 v1, FloatArray2 v2 ) {

        int offset = 0;

        while ( offset < _values.length ) {
            _values[ offset ] = Math.max( v1._values[ offset ], v2._values[ offset ] );
            ++offset;
        }
    }

    /**
     * mac = Multiply Accumulate i.e. add to product.
     *
     * @param v
     * @param accumulate
     */
    public void mac( FloatArray2 v, float accumulate ) {

        int offset = 0;

        while ( offset < _values.length ) {
            float value = _values[ offset ];

            value *= v._values[ offset ];
            value += accumulate;

            _values[ offset ] = value;

            ++offset;
        }
    }

    public void mac( FloatArray2 v1, float v2, float add ) {

        int offset = 0;

        while ( offset < _values.length ) {
            _values[ offset ] = ( v1._values[ offset ] * v2 ) + add;
            ++offset;
        }
    }

    public void mac( FloatArray2 v1, FloatArray2 v2, float add ) {

        int offset = 0;

        while ( offset < _values.length ) {
            _values[ offset ] = ( v1._values[ offset ] * v2._values[ offset ] ) + add;
            ++offset;
        }
    }

    public void addMul( FloatArray2 v, float accumulate ) {

        int offset = 0;

        while ( offset < _values.length ) {
            float value = _values[ offset ];

            value += accumulate;
            value *= ( v._values[ offset ] + accumulate );

            _values[ offset ] = value;

            ++offset;
        }
    }

    public void addMul( FloatArray2 v1, FloatArray2 v2, float add ) {

        int offset = 0;

        while ( offset < _values.length ) {
            _values[ offset ] = ( v1._values[ offset ] + add ) * ( v2._values[ offset ] + add );
            ++offset;
        }
    }

    public void div( FloatArray2 v ) {

        int offset = 0;

        while ( offset < _values.length ) {
            _values[ offset ] /= v._values[ offset ];
            ++offset;
        }
    }

    public void div( float r ) {

        int offset = 0;

        while ( offset < _values.length ) {
            _values[ offset ] /= r;
            ++offset;
        }
    }

    /**
     * Adds a small uniform value to all elements to avoid /0 error or testing
     * for zero value. Useful hack.
     *
     * @param v
     * @param uniform
     */
    public void addDiv( FloatArray2 v, float uniform ) {

        int offset = 0;

        while ( offset < _values.length ) {
            _values[ offset ] /= ( v._values[ offset ] + uniform );
            ++offset;
        }
    }

    public void div( FloatArray2 numerator, FloatArray2 denominator, float uniform ) {

        int offset = 0;

        while ( offset < _values.length ) {
            float n = numerator._values[ offset ];
            float d = denominator._values[ offset ];
            _values[ offset ] = n / ( d + uniform );
            ++offset;
        }
    }

    /**
     * Linear Interpolation.
     * x = x * w_x + y * w_y
     *
     * @param that
     * @param weightThis
     * @param weightThat
     */
    public void lerp( FloatArray2 that, float weightThis, float weightThat ) {

        int offset = 0;

        while ( offset < _values.length ) {

            float valueThis = _values[ offset ];
            float valueThat = that._values[ offset ];

            float lerp = ( weightThis * valueThis )
                    + ( weightThat * valueThat );

            _values[ offset ] = lerp;

            ++offset;
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Min and Max location
    ////////////////////////////////////////////////////////////////////////////
    public float minValue() {

        float min = Float.MAX_VALUE;

        int offset = 0;

        while ( offset < _values.length ) {
            min = Math.min( min, _values[ offset ] );
            ++offset;
        }

        return min;
    }

    public float maxValue() {

        float max = Float.MIN_VALUE;

        int offset = 0;

        while ( offset < _values.length ) {
            max = Math.max( max, _values[ offset ] );
            ++offset;
        }

        return max;
    }

    public int minValueIndex() {

        float min = Float.MAX_VALUE;
        int index = 0;
        int offset = 0;

        while ( offset < _values.length ) {
            float value = _values[ offset ];
            if ( min <= value ) {
                min = value;
                index = offset;
            }
            ++offset;
        }

        return index;
    }

    public int maxValueIndex() {

        float max = Float.MIN_VALUE;
        int index = 0;
        int offset = 0;

        while ( offset < _values.length ) {
            float value = _values[ offset ];
            if ( max >= value ) {
                max = value;
                index = offset;
            }
            ++offset;
        }

        return index;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Sorting and Ranking
    ////////////////////////////////////////////////////////////////////////////
    public TreeMap< Float, ArrayList< Integer > > sort() { // produces a ranked list of model indices (1-d ordinates)
        TreeMap< Float, ArrayList< Integer > > tm = new TreeMap< Float, ArrayList< Integer > >(); // in name order, arraylist for duplicates.

        int index = 0;
        int volume = _values.length;

        while ( index < volume ) {
            Float f = new Float( _values[ index ] );
            ArrayList< Integer > al = tm.get( f );

            if ( al == null ) {
                al = new ArrayList< Integer >();
                tm.put( f, al );
            }
            al.add( index );

            ++index;
        }

        return tm;
    }

    public int rankDescending() { // ie rank = 1 is lowest value, returns max ranking
        TreeMap< Float, ArrayList< Integer > > tm = sort(); // ascending order

        Set< Float > s = tm.keySet();

        int nextRank = 0;

        Iterator i = s.iterator();

        while ( i.hasNext() ) {
            Float f = ( Float ) i.next();
            ArrayList< Integer > al = tm.get( f );

            for ( Integer n : al ) {
                _values[ n ] = ( float ) ( nextRank );

                ++nextRank;
            }
        }

        int ranks = nextRank - 1;
        return ranks;
    }

    public int multiRankDescending() { // ie rank = 1 is lowest value, returns max ranking
        TreeMap< Float, ArrayList< Integer > > tm = sort(); // ascending order

        Set< Float > s = tm.keySet();

        int nextRank = 0;

        Iterator i = s.iterator();

        while ( i.hasNext() ) {
            Float f = ( Float ) i.next();
            ArrayList< Integer > al = tm.get( f );

            for ( Integer n : al ) {
                _values[ n ] = ( float ) ( nextRank );
            }

            ++nextRank;
        }

        int ranks = nextRank - 1;
        return ranks;
    }

    public void linearRanking() {
        TreeMap< Float, ArrayList< Integer > > tm = sort();
        Set< Float > s = tm.keySet();

        // An Introduction to Genetic Algorithms By Melanie Mitchell
        // rank in order of increasing fitness (prob)
        // ranks from 1 to N
        // param: max >= 0, ev of individual rank N
        // paramn: min = ex. val. of individual rank 1
        // note: 1<=max<=2
        // and min = 2-max
        // baker: recommend max= 1.1
        // expected value of individual i @ time t =
        // ev( i,t ) = min + (max-min) * ( (rank(i,t)-1) / (N-1) )
        int rank = 1;
        int ranks = s.size(); // nbr ranks (some have equal rank)

        double expectedValueMax = 1.1;
        double expectedValueMin = 2.0 - expectedValueMax;
        double linearScaling = expectedValueMin + ( expectedValueMax - expectedValueMin );
        double reciprocal = 1.0 / ( double ) Math.max( ranks - 1, 1 );

        Iterator i = s.iterator();

        while ( i.hasNext() ) {
            Float f = ( Float ) i.next();
            ArrayList< Integer > al = tm.get( f );

            for ( Integer n : al ) {
                double expectedValue = ( double ) ( rank - 1 ) * reciprocal;
                expectedValue *= linearScaling;
                _values[ n ] = ( float ) expectedValue;
            }

            ++rank;
        }
    }

}
