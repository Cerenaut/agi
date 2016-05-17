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
 * This class puts all the math associated with the backpropagation algorithm in one place.
 * It is a collection of static methods called by other objects.
 * <p/>
 * Math based on:
 * http://neuralnetworksanddeeplearning.com/chap2.html
 * <p/>
 * w^l_{jk} = weight from neuron k in layer l-1 to neuron j in layer l
 * <p/>
 * i.e. the weights are associated with inputs to a neuron.
 * <p/>
 * b^l_j = bias for neuron j in layer l
 * <p/>
 * i^l_k = the value of input k in layer l-1 to layer l
 * <p/>
 * z^l_j = the sum of weights * inputs + bias for neuron j in layer l
 * <p/>
 * a^l_j = activation of neuron j in layer l
 * <p/>
 * This class contains the implementation of BackProp functions.
 * <p/>
 * http://neuralnetworksanddeeplearning.com/chap2.html
 * Created by dave on 3/01/16.
 */
public abstract class BackPropagation {

//    /**
//     * The result of applying the loss or cost function.
//     *
//     * @param outputs
//     * @param ideals
//     * @param losses
//     * @param lf
//     */
//    public static void losses( FloatArray2 outputs, FloatArray2 ideals, FloatArray2 losses, LossFunction lf ) {
//        int J = outputs.getSize();
//        assert( ideals.getSize() == J );
//        assert( losses.getSize() == J );
//
//        for( int j = 0; j < J; ++j ) {
//            float output = outputs._values[ j ];
//            float ideal  = ideals._values[ j ];
//            float loss = lf.loss( output, ideal );
//            losses._values[ j ] = loss;
//        }
//    }

    public static void externalErrorGradient(
            FloatArray weightedSums,
            FloatArray outputs,
            FloatArray ideals,
//            FloatArray2 losses,
            FloatArray errors,
            ActivationFunction af,
            String lossFunction,
            float l2R,
            float sumSqWeights ) {

        l2R = 0.5f * l2R * sumSqWeights; // http://cs231n.github.io/neural-networks-2/ and http://neuralnetworksanddeeplearning.com/chap3.html

        if( lossFunction.equals( LossFunction.QUADRATIC ) ) {
            externalErrorGradientQuadratic( weightedSums, outputs, ideals, errors, af, l2R );
        } else if( lossFunction.equals( LossFunction.CROSS_ENTROPY ) ) {
            externalErrorGradientDifference( outputs, ideals, errors, l2R );
        } else if( lossFunction.equals( LossFunction.LOG_LIKELIHOOD ) ) {
            externalErrorGradientDifference( outputs, ideals, errors, l2R );
        }
    }

    public static void externalErrorGradientQuadratic(
            FloatArray weightedSums,
            FloatArray outputs,
            FloatArray ideals,
            FloatArray errors,
            ActivationFunction af,
            float l2R ) {
        int J = weightedSums.getSize();
        assert ( errors.getSize() == J );

        // d = loss * derivative( weightedSum )
        for( int j = 0; j < J; ++j ) {
            float output = outputs._values[ j ];
            float ideal = ideals._values[ j ];
            float loss = LossFunction.quadratic( output, ideal );
            loss += l2R;
            float z = weightedSums._values[ j ];
            float d = ( float ) af.df( z );
            errors._values[ j ] = loss * d;
        }
    }

    public static void externalErrorGradientDifference(
            FloatArray outputs,
            FloatArray ideals,
            FloatArray errors,
            float l2R ) {
        int J = ideals.getSize();
        assert ( outputs.getSize() == J );

        for( int j = 0; j < J; ++j ) {
            float y = ideals._values[ j ];
            float a = outputs._values[ j ];
            errors._values[ j ] = ( a - y ) + l2R;
        }
    }

    public static void train(
            FloatArray inputs, // a, or i ie the activations of the layer before (l-1)
            FloatArray weights, // w
            FloatArray biases, // b
            FloatArray errorGradient, // d
            float learningRate,
            float l2R ) {
        int K = inputs.getSize(); // layer inputs ie neurons in layer l-1
        int J = errorGradient.getSize(); // layer outputs ie neurons in this layer l

        assert ( errorGradient.getSize() == J );
        assert ( weights.getSize() == ( K * J ) );

        // w_jk = w_jk - learningRate * d_j * input_k
        // b_j = b_j - learningRate * d_j

        for( int j = 0; j < J; ++j ) {

            float d = errorGradient._values[ j ];
            float bOld = biases._values[ j ];
            float bNew = bOld - learningRate * d;
            biases._values[ j ] = bNew;

            for( int k = 0; k < K; ++k ) { // computing error for each "input"

                int offset = j * K + k; // K = inputs, storage is all inputs adjacent

                float a = inputs._values[ k ];
                float gradient = d * a;

                float wOld = weights._values[ offset ];
                float wNew = wOld - learningRate * gradient;// + ( l2R * wOld );

                weights._values[ offset ] = wNew;
            }
        }
    }

    public static void internalErrorGradient(
            FloatArray weightedSumsLayer1,
            FloatArray errorsLayer1,
            FloatArray weightsLayer2,
            FloatArray errorsLayer2,
            ActivationFunction afLayer1,
            float l2R ) {

        int K = errorsLayer1.getSize(); // layer inputs ie neurons in layer l-1
        int J = errorsLayer2.getSize(); // layer outputs ie neurons in this layer l
        assert ( weightedSumsLayer1.getSize() == K );
        assert ( weightsLayer2.getSize() == ( K * J ) );

        for( int k = 0; k < K; ++k ) { // computing error for each "input"
            float sum = 0.f;

            for( int j = 0; j < J; ++j ) {
                int offset = j * K + k; // K = inputs, storage is all inputs adjacent
                float w = weightsLayer2._values[ offset ];
                float d = errorsLayer2._values[ j ]; // d_j i.e. partial derivative of loss fn with respect to the activation of j
                float product = d * w + ( l2R * w );
                sum += product;
            }

            // d^l_k = sum_j :
            float z = weightedSumsLayer1._values[ k ];
            float d = ( float ) afLayer1.df( z );
            errorsLayer1._values[ k ] = sum * d;
        }
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

//    /**
//     * little-greek-delta (output layer only)
//     * http://neuralnetworksanddeeplearning.com/chap2.html
//     *
//     * x = inputs or training samples (in time)  (confusing)
//     * a = sigma( w * a^l-1 + b )
//     * a = sigma( z )
//     * z = w * a^l-1 + b
//     *
//     * Compute
//     *  ∂C/∂w and ∂C/∂b   for all w, b and cost function C  ie the partial derivatives
//     *
//     * d^l_j = error of neuron j in layer l
//     *
//     * d^l_j === ∂C/∂z^l_j  (eqn 29)    the derivative of the cost with respect to the weighted sum + bias
//     *
//     * d^L_j = ∂C/∂a^L_j * sigma'( z^L_j )      where ∂C/∂a = rate of change of cost WRT output activation
//     *
//     * if C == y-a, ∂C/∂a^L_j = a_j - y_j
//     *
//     * d^l = ((w^l+1)^T * d^l+1) * sigma'( z^l )     how to propagate the deltas
//     *
//     * ∂C/∂b^l_j = d^l_j                 derivative for biases
//     *
//     * ∂C/∂w^l_j = a^l-1_k * d^l_j       derivative for weights
//     *
//     * Gradient descent:
//     * w = w - η * d^xl( a^xl)^T           where η  = learning rate
//     * b = b - η * d^xl
//     *
//     * Therefore, (not in the article):
//     * w = w - η * ∂C/∂w^l_j
//     * b = b - η * ∂C/∂b^l_j
//     *
//     * http://neuralnetworksanddeeplearning.com/chap3.html
//     *
//     * C = [   y    ln a
//     *     + (1-y)  ln (1-a)   ]               (eqn 57)
//     *
//     * x = training inputs to neuron
//     *
//     * ∂C/∂w^l_j = x_j     * ( sigma(z) -y )      OK this is the same as saying:
//     * ∂C/∂w^l_j = a^l-1_k * ( d_j         )      Now see if we can substitute d, the
//     *
//     * ∂C/∂b^l_j =       ( sigma(z) -y )
//     * ∂C/∂b^l_j =       ( d_j         )
//     *
//     * Eqn 63
//     *
//     * C = -1/n * sum:x sum:j [ y_j ln a^L_j    + (1-y_j) ln (1-a^L_j)  ]   NOTE sum over all J
//     *
//     * Eqn 66 (Cross Entropy)                                  Quadratic cost:
//     *
//     * d^L = a^L - y                                           d^L_j = ∂C/∂a^L_j * sigma'( z^L_j )
//     *
//     * Eqn 67: (output layer
//     *
//     * ∂C/∂w^L_j = sum:X [ a^L-1_k * ( a^L_j - y_j ) ]         ∂C/∂w^l_j = a^l-1_k * d^l_j
//     * ∂C/∂w^L_j =         a              d
//     *
//     * "cross-entropy is a measure of surprise"
//     *
//     * @param losses
//     * @param weightedSums
//     * @param errors
//     * @param af
//     */

}
