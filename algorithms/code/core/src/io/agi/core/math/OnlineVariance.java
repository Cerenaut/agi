/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package io.agi.core.math;

/**
 * Utility for computing accurate and numerically-stable variance, online (ie
 * while continuously receiving data).
 * <p>
 * From Knuth 1998
 * http://en.wikipedia.org/w/index.php?title=Algorithms_for_calculating_variance&section=4#On-line_algorithm
 *
 * @author davidjr
 */
public class OnlineVariance {

    double _samples = 0.0;
    double _mean = 0.0;
    double _meanSq = 0.0;

    public OnlineVariance() {

    }

    public void reset() {
        _samples = 0.0;
        _mean = 0.0;
        _meanSq = 0.0;
    }

    public double mean() {
        return _mean;
    }

    public double sampleVariance() { // if this represents a sample from the population
        double variance = _meanSq / ( _samples - 1.0 );
        return variance;
    }

    public double populationVariance() {
        double variance = _meanSq / ( _samples );
        return variance;
    }

    public void update( double value ) {

        double delta = value - _mean;
        _samples += 1.0;
        _mean += ( delta / _samples ); // ouch

        if ( _samples > 1.0 ) {
            _meanSq += delta * ( value - _mean );
        }
    }

}
