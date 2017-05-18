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

package io.agi.core.math;

/******************************************************************************
 * COPIED FROM: http://introcs.cs.princeton.edu/java/22library/Gaussian.java.html
 *
 *  Compilation:  javac Gaussian.java
 *  Execution:    java Gaussian x mu sigma
 *
 *  Function to compute the Gaussian pdf (probability density function)
 *  and the Gaussian cdf (cumulative density function)
 *
 *  % java Gaussian 820 1019 209
 *  0.17050966869132111
 *
 *  % java Gaussian 1500 1019 209
 *  0.9893164837383883
 *
 *  % java Gaussian 1500 1025 231
 *  0.9801220907365489
 *
 *  The approximation is accurate to absolute error less than 8 * 10^(-16).
 *  Reference: Evaluating the Normal Distribution by George Marsaglia.
 *  http://www.jstatsoft.org/v11/a04/paper
 *
 ******************************************************************************/

public class Gaussian {

    // return pdf(x) = standard Gaussian pdf
    public static double pdf(double x) {
        return Math.exp(-x*x / 2) / Math.sqrt(2 * Math.PI);
    }

    // return pdf(x, mu, signma) = Gaussian pdf with mean mu and stddev sigma
    public static double pdf(double x, double mu, double sigma) {
        return phi((x - mu) / sigma) / sigma;
    }

    // return cdf(z) = standard Gaussian cdf using Taylor approximation
    public static double cdf(double z) {
        if (z < -8.0) return 0.0;
        if (z >  8.0) return 1.0;
        double sum = 0.0, term = z;
        for (int i = 3; sum + term != sum; i += 2) {
            sum  = sum + term;
            term = term * z * z / i;}
        return 0.5 + sum * phi(z);
    }

    // return cdf(z, mu, sigma) = Gaussian cdf with mean mu and stddev sigma
    public static double cdf( double z, double mu, double sigma) {
        if( sigma == 0f ) {
            return 1;
        }

        return cdf((z - mu) / sigma);
    }

    // Compute z such that cdf(z) = y via bisection search
    public static double inverseCDF(double y) {
        return inverseCDF(y, 0.00000001, -8, 8);
    }

    // bisection search
    private static double inverseCDF(double y, double delta, double lo, double hi) {
        double mid = lo + (hi - lo) / 2;
        if (hi - lo < delta) return mid;
        if (cdf(mid) > y) return inverseCDF(y, delta, lo, mid);
        else              return inverseCDF(y, delta, mid, hi);
    }


    // return phi(x) = standard Gaussian pdf
    @Deprecated
    public static double phi(double x) {
        return pdf(x);
    }

    // return phi(x, mu, signma) = Gaussian pdf with mean mu and stddev sigma
    @Deprecated
    public static double phi( double x, double mu, double sigma) {
        return pdf(x, mu, sigma);
    }

    // return Phi(z) = standard Gaussian cdf using Taylor approximation
    @Deprecated
    public static double Phi(double z) {
        return cdf(z);
    }

    // return Phi(z, mu, sigma) = Gaussian cdf with mean mu and stddev sigma
    @Deprecated
    public static double Phi( double z, double mu, double sigma) {
        return cdf(z, mu, sigma);
    }

    // Compute z such that Phi(z) = y via bisection search
    @Deprecated
    public static double PhiInverse(double y) {
        return inverseCDF(y);
    }

}