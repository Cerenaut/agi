package io.agi.core.ann.supervised;

/**
 * Aka Cost function or objective function.
 * Measures the error between output and ideal.
 * See http://neuralnetworksanddeeplearning.com/chap2.html
 *
 * Created by dave on 4/01/16.
 */
public abstract class LossFunction {

    public static final String QUADRATIC = "quadratic";
    public static final String CROSS_ENTROPY = "cross-entropy";

    public static float quadratic( float output, float ideal ) {
        // per-sample x, where x is a vector, cost C_x = 0.5 * ||y-a^L||^2
        // C = average over all C_x i.e. C = 1/n * sum_x C_x
        // delta_aC = a^L -y
        return output - ideal;
    }

    public static float crossEntropy( float output, float ideal ) {
        // a = output
        // y = ideal
        // C = y * ln( a ) + (1-y) * ln( 1-a )
        // C = ideal * ln( output ) + (1-ideal) * ln( 1-output )
        double term1 = ideal * Math.log( output );
        double term2 = ( 1.0 - ideal ) * Math.log( 1.0 - output );
        double C = - ( term1 + term2 );
        return (float)C;
    }

}
