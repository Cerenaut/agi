/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package io.agi.core.nn;

/**
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
 * @author dave
 */
public class Nonlinear {
 
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
        double r = Nonlinear.tanh( x );
        double d = 1.0 - ( r * r ); // equation 3.6
        return d;
    }

    
}
