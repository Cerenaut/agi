package io.agi.core.ann.supervised;

import io.agi.core.data.FloatArray2;

/**
 * http://neuralnetworksanddeeplearning.com/chap2.html
 *
 * w^l_{jk} = weight from neuron k in layer l-1 to neuron j in layer l
 *
 *  i.e. the weights are associated with inputs to a neuron.
 *
 * b^l_j = bias for neuron j in layer l
 *
 * i^l_k = the value of input k in layer l-1 to layer l
 *
 * z^l_j = the sum of weights * inputs + bias for neuron j in layer l
 *
 * a^l_j = activation of neuron j in layer l
 *
 * This class contains the implementation of BackProp functions.
 *
 * http://neuralnetworksanddeeplearning.com/chap2.html
 * Created by dave on 3/01/16.
 */
public class BackPropagation {

    public BackPropagation() {

    }

    public static void feedForward(
            FloatArray2 weights,
            FloatArray2 inputs,
            FloatArray2 biases,
            FloatArray2 weightedSums,
            ActivationFunction cf,
            FloatArray2 outputs ) {
        weightedSum( weights, inputs, biases, weightedSums );
        activate( weightedSums, cf, outputs );
    }

    public static void feedBackward(
            FloatArray2 weights,
            FloatArray2 inputs,
            FloatArray2 biases,
            FloatArray2 weightedSums,
            ActivationFunction cf,
            FloatArray2 outputs ) {
    }

    public static void activate( FloatArray2 weightedSums, ActivationFunction cf, FloatArray2 outputs ) {
        int J = weightedSums.getSize();

        assert( outputs.getSize() == J );

        for( int j = 0; j < J; ++j ) {

            float z = weightedSums._values[ j ];
            double a = cf.f( z );

            outputs._values[ j ] = (float)a;
        }
    }

    public static void weightedSum( FloatArray2 weights, FloatArray2 inputs, FloatArray2 biases, FloatArray2 outputs ) {
        int K = inputs.getSize();
        int J = biases.getSize();

        assert( outputs.getSize() == J );
        assert( weights.getSize() == (J*K) );

        for( int j = 0; j < J; ++j ) {

            float sum = 0.f;

            for( int k = 0; k < K; ++k ) {
                int offset = j * K + k; // K = inputs, storage is all inputs adjacent
                float i = inputs._values[ k ];
                float w = weights._values[ offset ];
                float product = i * w;
                sum += product;
            }

            float b = biases._values[ j ];

            sum += b;

            outputs._values[ j ] = sum;
        }
    }

    /**
     * The result of applying the loss or cost function.
     *
     * @param outputs
     * @param ideals
     * @param losses
     * @param lf
     */
    public static void losses( FloatArray2 outputs, FloatArray2 ideals, FloatArray2 losses, LossFunction lf ) {
        int J = outputs.getSize();
        assert( ideals.getSize() == J );
        assert( losses.getSize() == J );

        for( int j = 0; j < J; ++j ) {
            float output = outputs._values[ j ];
            float ideal  = ideals._values[ j ];
            float loss = lf.loss( output, ideal );
            losses._values[ j ] = loss;
        }
    }

    /**
     * little-greek-delta (output layer only)
     *
     * @param losses
     * @param weightedSums
     * @param errors
     * @param af
     */
    public static void outputErrors(
            FloatArray2 losses,
            FloatArray2 weightedSums,
            FloatArray2 errors,
            ActivationFunction af ) {
        int J = losses.getSize();
        assert( weightedSums.getSize() == J );
        assert( errors.getSize() == J );

        // d = loss * derivative( weightedSum )
        for( int j = 0; j < J; ++j ) {
            float partial = losses._values[ j ]; // i.e. partial derivative of loss fn with respect to the activation of j
            float z = weightedSums._values[ j ];
            float d = (float)af.df( z );
            errors._values[ j ] = partial * d; // ho
        }
    }

    public static void train(
            FloatArray2 inputs, // a, or i ie the activations of the layer before (l-1)
            FloatArray2 weights, // w
            FloatArray2 biases, // b
            FloatArray2 errors, // d
            float learningRate ) {
        int K = inputs.getSize(); // layer inputs ie neurons in layer l-1
        int J = errors.getSize(); // layer outputs ie neurons in this layer l

        assert( errors.getSize() == J );
        assert( weights.getSize() == (K*J) );

        // w_jk = w_jk - learningRate * d_j * input_k
        // b_j = b_j - learningRate * d_j

        for( int j = 0; j < J; ++j ) {

            float d = errors._values[ j ];
            float bOld = biases._values[ j ];
            float bNew = bOld - learningRate * d;
            biases._values[ j ] = bNew;

            for (int k = 0; k < K; ++k) { // computing error for each "input"

                int offset = j * K + k; // K = inputs, storage is all inputs adjacent

                float a = inputs._values[ k ];
                float gradient = d * a;

                float wOld = weights._values[ offset ];
                float wNew = wOld - learningRate * gradient;

                weights._values[ offset ] = wNew;
            }
        }
    }

    public static void internalErrors(
            FloatArray2 weightsLayer2,
            FloatArray2 errorsLayer2,
            FloatArray2 errorsLayer1,
            FloatArray2 weightedSumsLayer1,
            ActivationFunction afLayer1 ) {

        int K = errorsLayer1.getSize(); // layer inputs ie neurons in layer l-1
        int J = errorsLayer2.getSize(); // layer outputs ie neurons in this layer l
        assert( weightedSumsLayer1.getSize() == K );
        assert( weightsLayer2.getSize() == (K*J) );

        for( int k = 0; k < K; ++k ) { // computing error for each "input"
            float sum = 0.f;

            for( int j = 0; j < J; ++j ) {
                int offset = j * K + k; // K = inputs, storage is all inputs adjacent
                float w = weightsLayer2._values[ offset ];
                float d = errorsLayer2._values[j]; // d_j i.e. partial derivative of loss fn with respect to the activation of j
                float product = d * w;
                sum += product;
            }

            // d^l_k = sum_j :
            float z = weightedSumsLayer1._values[ k ];
            float d = (float)afLayer1.df( z );
            errorsLayer1._values[k] = sum * d;
        }

// Equations from:
//
// http://neuralnetworksanddeeplearning.com/chap2.html
//
//        https://en.wikipedia.org/wiki/Index_notation
//        w_jk
//                 j = neurons   (rows)
//                 k = inputs    (cols)
// w^{l+1}T matmul delta^l+t
//  C = AB   then COLS in A must equal ROWS in B
// So we have delta having
//           _ k _
//          |
//        j |
//          |
// Transpose:
//            COLS
//           _ j _      then since J must equal cols. J must be same elements as delta, which is true.
//          |        |
//        k |      * |d  ROWS
//          |        |
// if A = n x m
// &  B = m x p
// AB =   n x p  == rows(A) x cols(B)
//                  rows A = k
//                  cols B = 1
// So result is a vector of length k .
// So we sum W x delta
// EAch sum is over all the weights from the particular neuron to the next layer, * the error in the next layer.

    }

}
