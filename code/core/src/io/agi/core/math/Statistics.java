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

package io.agi.core.math;

import java.util.Random;

/**
 * @author dave
 */
public class Statistics {

    // TODO: A nonhomogeneous poisson process
    // http://www.math.uah.edu/stat/poisson/Nonhomogeneous.html
    // http://web.ics.purdue.edu/~pasupath/PAPERS/2011pasB.pdf
    // http://prac.im.pwr.edu.pl/~burnecki/Simulation_of_counting_processes.pdf

    /**
     * Simulates a Poisson process. Generates events at random intervals, with frequency controlled by 'rate'.
     * When you call this function, it will return the interval until the next event such that the average rate is
     * 'rate' per time-unit. Each time an event occurs, call the function again to find the time of the next event.
     *
     * See:
     * http://preshing.com/20111007/how-to-generate-random-timings-for-a-poisson-process/
     * http://stackoverflow.com/questions/1155539/how-do-i-generate-a-poisson-process
     *
     * @param r
     * @param rate
     * @return
     */
    public static float poissonInterval( Random r, float rate ) {
        if( rate <= 0f ) {
            return 0f;
        }

        float U = r.nextFloat();
        float interval = (float)( - Math.log( 1.0 - U ) / rate );
        return interval;
    }

    /**
     * Shannon entropy with base 10.
     * http://en.wikipedia.org/wiki/Entropy_%28information_theory%29
     *
     * @param values
     * @return
     */
    public static double entropyBase10( float[] values ) {

        double sum = 0.0;
        int offset = 0;

        while( offset < values.length ) {

            double value = values[ offset ];
            ++offset;

            double r = 0.0; // in the limit x*log(x) approaches 0

            if( value > 0.0 ) {
                r = value * Math.log10( value );
            }

            sum += r;
        }

        return -sum;
    }

    /**
     * Shannon entropy with base E.
     * http://en.wikipedia.org/wiki/Entropy_%28information_theory%29
     *
     * @param values
     * @return
     */
    public static double entropyBaseE( float[] values ) {

        double sum = 0.0;

        int offset = 0;

        while( offset < values.length ) {

            double value = values[ offset ];
            ++offset;

            double r = 0.0; // in the limit x*log(x) approaches 0

            if( value > 0.0 ) {
                r = value * Math.log( value );
            }

            sum += r;
        }

        return -sum;
    }

    /**
     * For completeness, online mean requires only tracking count and cum
     *
     * @param sum
     * @param samples
     * @return
     */
    public static double mean( double sum, double samples ) {
        return sum / samples;
    }

    /**
     * mean and variance fns: standard variance (sample would normalize by n-1
     * at the end). This function is not very accurate if the numbers are large
     * especially if the variance is comparatively small. If you subtract the
     * first value from all samples it will be more accurate.
     *
     * @param sum
     * @param sumOfSq
     * @param samples
     * @return
     */
    public static double variance( double sum, double sumOfSq, double samples ) {
        if( samples <= 1.0 ) return 0.0;
        double normalizer = 1.0 / samples;
        double sumSq = sum * sum;
        double variance = sumOfSq - ( normalizer * sumSq );
        variance *= normalizer;
        return variance;
    }

    /**
     * Gaussian function
     *
     * @param x
     * @param sigma
     * @return
     */
    public static double gaussian( double x, double sigma ) {
        double r1 = 1.0 / ( Math.sqrt( 2.0 * Math.PI * sigma ) );
        double r2 = 2.0 * sigma * sigma;
        double r3 = -( ( x * x ) / r2 );
        double r4 = Math.pow( Math.E, r3 ); // exp( r3 )
        double rG = r1 * r4;
        return rG;
    }

    /**
     * This function suggests a kernel size for a Gaussian kernal with a given
     * sigma value.
     * <p/>
     * From "Image Processing, Analysis and Machine Vision", pp. 84:
     * 'Pixels more distant from the center of the operator have smaller
     * influence, and pixels farther than 3 \sigma from the center have
     * neglible influence.'
     * <p/>
     * Also, the kernel should (ideally) have odd dimension.
     *
     * @param rSigma
     * @return
     */
    public static int gaussianKernelSize( double rSigma ) {
        return ( 1 + 2 * ( ( int ) ( 3.0 * rSigma ) ) );
    }

}
