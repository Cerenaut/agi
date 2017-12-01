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

package io.agi.core.ann.supervised;

import io.agi.core.data.FloatArray;

/**
 * Activation functions that implement the nonlinearities in a feed-forward neural network.
 * For back-propagation purposes, each also has a derivative function.
 * <p/>
 * Nonlinear activation functions for neural networks.
 * <p/>
 * From "Supervised Sequence Labelling with Recurrent Neural Networks"
 * by Alex Graves
 * Section 3.1.1 page 13
 * <p/>
 * "This means that any function computed by a neural network with a hidden layer
 * of tanh units can be computed by another network with logistic sigmoid units
 * and vice-versa. They are therefore largely equivalent as activation functions.
 * However one reason to distinguish between them is that their output ranges are
 * different; in particular if an output between 0 and 1 is required (for example, if
 * the output represents a probability) then the logistic sigmoid should be used."
 * <p/>
 * Created by dave on 3/01/16.
 */
public abstract class ActivationFunction {

    /**
     * Returns a nonlinear scalar function of r.
     *
     * @param weightedSums
     * @param outputs
     * @return
     */
    public void f( FloatArray weightedSums, FloatArray outputs ) {
        int J = weightedSums.getSize();

        assert ( outputs.getSize() == J );

        for( int j = 0; j < J; ++j ) {

            float z = weightedSums._values[ j ];
            double a = f( z );

            outputs._values[ j ] = ( float ) a;
        }
    }

    /**
     * Computes the activation for a single neuron based on weighted sum only. Convenience function.
     *
     * @param r
     * @return
     */
    public double f( double r ) {
        return 0.0;
    }

    /**
     * Returns the derivative of r.
     * Always a positive quantity.
     * @param r
     * @return
     */
    public abstract double fDerivative( double r );

//    public static TransferFunction createLinear() {
//        return new TransferFunction() {
//            public double f( double r ) {
//                return r;
//            }
//            public double fDerivative( double r ) {
//                return 1.0;
//            }
//        };
//    }

    public static ActivationFunction createLogisticSigmoid() {
        return new ActivationFunction() {
            public double f( double r ) {
                return ActivationFunction.logisticSigmoid(r);
            }

            public double fDerivative( double r ) {
                return ActivationFunction.logisticSigmoidDerivative(r);
            }
        };
    }

    public static ActivationFunction createTanh() {
        return new ActivationFunction() {
            public double f( double r ) {
                return ActivationFunction.tanh(r);
            }

            public double fDerivative( double r ) {
                return ActivationFunction.tanhDerivative(r);
            }
        };
    }

    public static ActivationFunction createSoftmax() {
        return new ActivationFunction() {
            public void f( FloatArray weightedSums, FloatArray outputs ) {
                softmax(weightedSums, outputs);
            }

            public double fDerivative( double r ) {
                return ActivationFunction.softmaxDerivative(r);
            }
        };
    }

    public static ActivationFunction createLeakyReLU( final float leak ) {
        return new ActivationFunction() {
            public void f( FloatArray weightedSums, FloatArray outputs ) {
                leakyReLU(weightedSums, outputs, leak );
            }

            public double fDerivative( double r ) {
                return ActivationFunction.leakyReLUDerivative(r, leak);
            }
        };
    }

    public static void leakyReLU( FloatArray weightedSums, FloatArray outputs, float leak ) {
        int J = weightedSums.getSize();

        assert ( outputs.getSize() == J );

        for( int j = 0; j < J; ++j ) {

            float z = weightedSums._values[ j ];
            float a = z; // linear
            if( a < 0f ) {
                a *= leak; //
            }
            outputs._values[ j ] = a;
        }
    }

    public static double leakyReLUDerivative( double x, float leak ) {
        if( x < 0f ) {
            return leak;
        }
        return 1f;
    }

    public static void softmax( FloatArray weightedSums, FloatArray outputs ) {
        int J = weightedSums.getSize();

        assert ( outputs.getSize() == J );

        double sum = Double.MIN_VALUE; // ensure avoid /0 error

        for( int j = 0; j < J; ++j ) {

            float z = weightedSums._values[ j ];
            double e_z = Math.exp( z );
            outputs._values[ j ] = ( float ) e_z;
            sum += e_z;
        }

        for( int j = 0; j < J; ++j ) {
            float e_z = outputs._values[ j ];
            double a = e_z / sum;
            outputs._values[ j ] = ( float ) a;
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

    /**
     * https://theclevermachine.wordpress.com/2014/09/08/derivation-derivatives-for-common-neural-network-activation-functions/
     * Similar to the derivative for the logistic sigmoid, the derivative of g_{tanh}(z) is a function of feed-forward
     * activation evaluated at z, namely (1-g_{tanh}(z)^2).
     * @param x
     * @return
     */
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
