/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package io.agi.core.math;

/**
 *
 * @author dave
 */
public class Statistics {

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
        double r3 = -( (x*x) / r2 );
        double r4 = Math.pow( Math.E, r3 ); // exp( r3 )
        double rG = r1 * r4;
        return rG;
    }

    /**
     * This function suggests a kernel size for a Gaussian kernal with a given
     * sigma value.
     * 
     * From "Image Processing, Analysis and Machine Vision", pp. 84:
     *  'Pixels more distant from the center of the operator have smaller
     *   influence, and pixels farther than 3 \sigma from the center have
     *   neglible influence.'
     * 
     * Also, the kernel should (ideally) have odd dimension.
     * 
     * @param rSigma
     * @return 
     */
    public static int gaussianKernelSize( double rSigma ) {
        return( 1 + 2 * ( (int)( 3.0 * rSigma ) ) );
    }
    
}