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
import io.agi.core.math.Useful;

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

    /**
     * Note we don't actually need to compute the cost, only the derivative of cost with respect to the values
     * in the output layer. We are calling this the cost gradient, but it's also the error we're trying to minimize.
     *
     * @param outputWeightedSums
     * @param outputCostGradients
     * @param outputs
     * @param ideals
     * @param af
     * @param costFunction
     */
    public static void CostGradientExternal(
            FloatArray outputWeightedSums,
            FloatArray outputCostGradients,
            FloatArray outputs,
            FloatArray ideals,
            ActivationFunction af,
            String costFunction ) {
//            float l2Regularization,
//            float sumSqWeights) {

//        l2Regularization = 0.5f * l2Regularization * sumSqWeights; // http://cs231n.github.io/neural-networks-2/ and http://neuralnetworksanddeeplearning.com/chap3.html

        int J = outputWeightedSums.getSize();
        assert ( outputCostGradients.getSize() == J );

        // Cx = 1/2n * ( ( sum y-a ) ^2 )
        // if 1 element:
        // Cx = 0.5 * ( ( y-a ) ^2 )
        // Need to compute dC / dW ie derivative of cost with respect to a weight

        // d = loss * derivative( weightedSum )
        for( int j = 0; j < J; ++j ) {
            float output = outputs._values[ j ];
            float ideal = ideals._values[ j ];

            float z = outputWeightedSums._values[ j ];
            float df = ( float ) af.fDerivative( z );

            float cost = 0f;

            if( costFunction.equals( CostFunction.QUADRATIC ) ) {
                cost = CostFunction.quadraticOutputErrorGradient( output, ideal, df );
            }
            else if( costFunction.equals( CostFunction.CROSS_ENTROPY ) ) {
                cost = CostFunction.crossEntropyOutputErrorGradient( output, ideal );
            }
//            else if( costFunction.equals( CostFunction.LOG_LIKELIHOOD ) ) {
//                cost = CostFunction.logLikelihood( ideal );
//            }

//            cost += l2Regularization;

            outputCostGradients._values[ j ] = cost;

            // now make it with respect to the weights and biases, by calculating the derivative of the activation fn
        }
    }

//    public static void OutputErrorGradientDifference(
//            FloatArray outputs,
//            FloatArray ideals,
//            FloatArray errors,
//            float l2R ) {
//        int J = ideals.getSize();
//        assert ( outputs.getSize() == J );
//
//        for( int j = 0; j < J; ++j ) {
//            float y = ideals._values[ j ];
//            float a = outputs._values[ j ];
//            errors._values[ j ] = ( a - y ) + l2R;
//        }
//    }

    // TODO replace with Stochastic Gradient Descent
/*    public static void train(
            FloatArray inputs, // a, or i ie the activations of the layer before (l-1)
            FloatArray weights, // w
            FloatArray biases, // b
            FloatArray errorGradient, // d
            float learningRate ) {
        int K = inputs.getSize(); // layer inputs ie neurons in layer l-1
        int J = errorGradient.getSize(); // layer outputs ie neurons in this layer l

        assert ( errorGradient.getSize() == J );
        assert ( weights.getSize() == ( K * J ) );

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
    }*/

    /**
     * Trains by gradient descent towards a local minima. A single layer is trained.
     *
s     * @param errorBatch The derivative of the cost function with respect to the weighted sum z (by mini batch)
     * @param weights The weights of the layer
     * @param biases The biases of the layer
     * @param inputBatch The inputs to the layer (by mini batch)
     * @param miniBatchSize
     * @param learningRate
     * @param regularization
     */
    public static void StochasticGradientDescent(
            FloatArray errorBatch,
            FloatArray weights,
            FloatArray biases,
            FloatArray inputBatch,
            int miniBatchSize,
            float learningRate,
            float regularization ) {

        float miniBatchNorm = 1.f / (float)miniBatchSize;
        float l2RegularizationTerm = 1f - ( ( learningRate * regularization ) * miniBatchNorm );

        int B = miniBatchSize;
        int K = inputBatch.getSize() / B; // layer inputs ie neurons in layer l-1
        int J = errorBatch.getSize() / B; // layer outputs ie neurons in this layer l

        assert ( biases.getSize() == J );
        assert ( weights.getSize() == ( K * J ) );

        // w_jk = w_jk - learningRate * d_j * input_k
        // b_j = b_j - learningRate * d_j

        // foreach( cell )
        for( int j = 0; j < J; ++j ) {

            // foreach( input )
            for( int k = 0; k < K; ++k ) {

                // foreach( batch sample )
                for( int b = 0; b < B; ++b ) {

                    int errorOffset = b * J + j;
                    float errorGradient = errorBatch._values[ errorOffset ];


                    int inputOffset = b * K + k;
                    float a = inputBatch._values[ inputOffset ];

                    int wOffset = j * K + k;
                    float wOld = weights._values[ wOffset ];

                    float wDelta = learningRate * ( miniBatchNorm * errorGradient ) * a;
                    float wOldRescaled = l2RegularizationTerm * wOld; // weight is unchanged when regularization is zero
                    float wNew = wOldRescaled - wDelta;

                    // http://neuralnetworksanddeeplearning.com/chap3.html#overfitting_and_regularization
                    // R = 1− ( ( η * λ ) / n )
                    // w = R * w - η * d

                    // weight clipping
                    //                float wMax = 1.f;
                    //                if( wNew > wMax ) wNew = wMax;
                    //                if( wNew < -wMax ) wNew = -wMax;

                    Useful.IsBad( wNew );

                    weights._values[ wOffset ] = wNew;
                }
            }

            // foreach( batch sample )
            float sumErrorGradient = 0f;

            for( int b = 0; b < B; ++b ) {
                int errorOffset = b * J + j;
                float errorGradient = errorBatch._values[ errorOffset ];
                sumErrorGradient += errorGradient;
            }

            float bOld = biases._values[ j ];
            float bNew = bOld - learningRate * ( miniBatchNorm * sumErrorGradient );

            biases._values[ j ] = bNew;
        }
    }

    /**
     * Computes the error gradient d for a layer l_1 which feeds forward to a layer l_2.
     * Therefore, the error gradient is backpropagated from layer 2 to layer 1
     *
     * @param z_l1 The weighted sum of layer 1
     * @param d_l1 The cost gradient in layer 1 (the output)
     * @param w_l2 The weights in layer 2.
     * @param d_l2 The cost gradient in layer 2.
     * @param f_l1 The transfer function and its derivative.
     */
    public static void CostGradientInternal(
            FloatArray z_l1,
            FloatArray d_l1,
            FloatArray w_l2,
            FloatArray d_l2,
            ActivationFunction f_l1 ) {//,
//            float l2R) {

        int K = d_l1.getSize(); // layer inputs ie neurons in layer l-1
        int J = d_l2.getSize(); // layer outputs ie neurons in this layer l
        assert ( z_l1.getSize() == K );
        assert ( w_l2.getSize() == ( K * J ) );

        for( int k = 0; k < K; ++k ) { // computing error for each "input"
            float sum = 0.f;

            for( int j = 0; j < J; ++j ) {
                int offset = j * K + k; // K = inputs, storage is all inputs adjacent
                float w = w_l2._values[ offset ];
                float d = d_l2._values[ j ]; // d_j i.e. partial derivative of loss fn with respect to the activation of j
                float product = d * w;// + ( l2R * w );

                Useful.IsBad( product );

                sum += product;
            }

            // d^l_k = sum_j :
            float z = z_l1._values[ k ];
            float d = ( float ) f_l1.fDerivative( z );
            d_l1._values[ k ] = sum * d;
        }
    }

    // TODO deprecate due to naming only
/*    public static void internalErrorGradient(
            FloatArray weightedSumsLayer1,
            FloatArray errorsLayer1,
            FloatArray weightsLayer2,
            FloatArray errorsLayer2,
            TransferFunction afLayer1,
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
            float d = ( float ) afLayer1.fDerivative( z );
            errorsLayer1._values[ k ] = sum * d;
        }
    }*/

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
