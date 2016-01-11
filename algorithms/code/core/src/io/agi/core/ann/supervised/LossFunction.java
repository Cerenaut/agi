package io.agi.core.ann.supervised;

/**
 * Aka Cost function or objective function.
 * Measures the error between output and ideal.
 * See http://neuralnetworksanddeeplearning.com/chap2.html
 *
 * Created by dave on 4/01/16.
 */
public abstract class LossFunction {

    public abstract float loss( float output, float ideal );

    public static float quadratic( float output, float ideal ) {
        // per-sample x, where x is a vector, cost C_x = 0.5 * ||y-a^L||^2
        // C = average over all C_x i.e. C = 1/n * sum_x C_x
        // delta_aC = a^L -y
        return output - ideal;
    }

    public static float crossEntropy( float output, float ideal ) {
        // TODO
        return output - ideal;
    }

    public static LossFunction createQuadratic() {
        return new LossFunction() {
            public float loss( float output, float ideal ) {
                return LossFunction.quadratic( output, ideal );
            }
        };
    }

    public static LossFunction createCrossEntropy() {
        return new LossFunction() {
            public float loss( float output, float ideal ) {
                return LossFunction.crossEntropy(output, ideal);
            }
        };
    }
}
