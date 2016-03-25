/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package io.agi.core.math;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

/**
 * Basic arithmetic for circular number systems
 *
 * @author dave
 */
public class Circular {

    /**
     * circular number system difference fn.
     *
     * @param first
     * @param second
     * @param max
     * @return
     */
    public static double absoluteDifference(
            double first,
            double second,
            double max ) {
        double mid = max * 0.5;
        double diff;

        if ( first > second ) {
            diff = first - second;
        }
        else {
            diff = second - first;
        }

        if ( diff > mid ) {
            diff = max - diff;
        }

        return diff;
    }

    ;

    public static double mean( ArrayList< Double > values, double maxValue ) {

        // 1) Handle small lists of values:
        //_________________________________________________________________________
        int size = values.size();

        if ( size < 2 ) {
            if ( size == 0 ) {
                return 0.0;
            }
            // else: size is 1.

            return values.get( 0 ).doubleValue(); // return the only value we have.
        }


        // 2) Sort the values, or at least references to them!
        //_________________________________________________________________________
        ArrayList< Double > sorted = new ArrayList< Double >( values ); // different ordering, same Double objects.

        Collections.sort( sorted );


        // 3) Find the greatest empty span. First prepare for a loop thru all
        // elements.
        //_________________________________________________________________________
        double first = 0.0;
        double min = 0.0;
        double max = 0.0;
        double span = 0.0;

        double maxSpanMin = 0.0;
        double maxSpanMax = 0.0;
        double maxSpan = 0.0;

        Iterator< Double > i = sorted.iterator();

        min = i.next(); // at least 1

        first = min;


        // 4) Loop thru all elements.
        //_________________________________________________________________________
        while ( i.hasNext() ) {

            max = i.next();

            span = max - min; // set will have ordered them.

            if ( span >= maxSpan ) {
                maxSpanMin = min;
                maxSpanMax = max;
                maxSpan = span;
            }

            min = max;
        }


        // 5) There is one final span, spanning zero/max:
        //_________________________________________________________________________
        // can't occur now
//        if( maxSpan == 0.0 ) {
//            return first;
//        }

        max = first;

        span = max + ( maxValue - min );

        if ( span >= maxSpan ) {
            maxSpanMin = min;
            maxSpanMax = max;
            maxSpan = span;
        }


        // 6) The other values may now be assessed by moving them so that they do
        //    not span 0/max. this is achieved by adding the difference between
        //    maxSpanMax and maxValue to all values. The largest empty span sits
        //    just before maxValue.
        //_________________________________________________________________________
        double offset = maxValue - maxSpanMax;
        double value = 0.0;
        double sum = 0.0;

        i = sorted.iterator();

        while ( i.hasNext() ) {

            value = i.next();
            value += offset;

            if ( value >= maxValue ) {
                value -= maxValue; // wrap around
            }

            sum += value;
        }

        sum /= ( double ) size;
        sum -= offset;

        if ( sum < 0.0 ) {
            sum += maxValue;
        }

        return sum;
    }

}
