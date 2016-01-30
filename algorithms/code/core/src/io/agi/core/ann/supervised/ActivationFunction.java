package io.agi.core.ann.supervised;

import io.agi.core.data.FloatArray2;

/**
 * Activation functions that implement the nonlinearities in a feed-forward neural network.
 * For back-propagation purposes, each also has a derivative function.
 *
 * Nonlinear activation functions for neural networks.
 *
 * From "Supervised Sequence Labelling with Recurrent Neural Networks"
 * by Alex Graves
 * Section 3.1.1 page 13
 *
 * "This means that any function computed by a neural network with a hidden layer
 * of tanh units can be computed by another network with logistic sigmoid units
 * and vice-versa. They are therefore largely equivalent as activation functions.
 * However one reason to distinguish between them is that their output ranges are
 * different; in particular if an output between 0 and 1 is required (for example, if
 * the output represents a probability) then the logistic sigmoid should be used."
 *
 * Created by dave on 3/01/16.
 */
public abstract class ActivationFunction {

    /**
     * Returns a nonlinear scalar function of r.
     * @param weightedSums
     * @param outputs
     * @return
     */
    public void f( FloatArray2 weightedSums, FloatArray2 outputs ) {
        int J = weightedSums.getSize();

        assert( outputs.getSize() == J );

        for( int j = 0; j < J; ++j ) {

            float z = weightedSums._values[ j ];
            double a = f(z);

            outputs._values[ j ] = (float)a;
        }
    }

    /**
     * Computes the activation for a single neuron based on weighted sum only. Convenience function.
     *
     * @param r
     * @return
     */
    public double f( double r ) { return 0.0; };

    /**
     * Returns the derivative of r.
     * @param r
     * @return
     */
    public abstract double df( double r );

    public static ActivationFunction createLogisticSigmoid() {
        return new ActivationFunction() {
            public double f( double r ) {
                return ActivationFunction.logisticSigmoid(r);
            }
            public double df( double r ) {
                return ActivationFunction.logisticSigmoidDerivative(r);
            }
        };
    }

    public static ActivationFunction createTanh() {
        return new ActivationFunction() {
            public double f( double r ) {
                return ActivationFunction.tanh(r);
            }
            public double df( double r ) {
                return ActivationFunction.tanhDerivative(r);
            }
        };
    }

    public static ActivationFunction createSoftmax() {
        return new ActivationFunction() {
            public void f( FloatArray2 weightedSums, FloatArray2 outputs ) {
                softmax( weightedSums, outputs );
            }
            public double df( double r ) {
                return ActivationFunction.softmaxDerivative(r);
            }
        };
    }

    public static void softmax( FloatArray2 weightedSums, FloatArray2 outputs ) {
        int J = weightedSums.getSize();

        assert( outputs.getSize() == J );

        double sum = Double.MIN_VALUE; // ensure avoid /0 error

        for( int j = 0; j < J; ++j ) {

            float z = weightedSums._values[ j ];
            double e_z = Math.exp( z );
            outputs._values[ j ] = (float)e_z;
            sum += e_z;
        }

        for( int j = 0; j < J; ++j ) {
            float e_z = outputs._values[j];
            double a = e_z / sum;
            outputs._values[j] = (float) a;
        }
    }

    public static double logisticSigmoid( double x ) {
        double denominator = 1.0 + Math.pow( Math.E, -x );
        double y = 1.0 / denominator; // equation 3.4
        return y;
    }

    public static double logisticSigmoidDerivative( double x ) {
        double r = logisticSigmoid( x );
        double d = r * ( 1.0 - r ); // equation 3.7
        return d;
    }

    public static double tanh( double x ) {
        return Math.tanh( x );
    }

    public static double tanhDerivative( double x ) {
        double r = ActivationFunction.tanh(x);
        double d = 1.0 - ( r * r ); // equation 3.6
        return d;
    }

    public static double softmaxDerivative( double x ) {
        double d = 0.0;//Math.log( x ); // is this correct?
        return d;
    }
}
