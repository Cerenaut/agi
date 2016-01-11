package io.agi.core.ann.supervised;

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
     * @param r
     * @return
     */
    public abstract double f( double r );

    /**
     * Returns the derivative of r.
     * @param r
     * @return
     */
    public abstract double df( double r );

    // TODO add softmax (prob dist) and cross entropy (fast learning when wrong)

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

}
