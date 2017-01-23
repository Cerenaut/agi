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

package io.agi.core.ann.unsupervised;

import io.agi.core.ann.supervised.ActivationFunction;
import io.agi.core.data.Data;
import io.agi.core.data.FloatArray;
import io.agi.core.data.Ranking;
import io.agi.core.math.Useful;
import io.agi.core.orm.ObjectMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;

/**
 * A derivation of K-Sparse Autoencoders for online, continuous or progressive learning. The input statistics can be
 * changed at any time without affecting lifetime sparsity constraints or cell activity frequency in the long term.
 *
 * Created by dave on 1/07/16.
 */
public class OnlineKSparseAutoencoder extends CompetitiveLearning {

    protected static final Logger logger = LogManager.getLogger();

//    public static float REGULARIZATION = 0.001f;

    public OnlineKSparseAutoencoderConfig _c;
    public ArrayList< Integer > _sparseUnitInput;
    public Data _inputValues;
    public Data _inputReconstructionWeightedSum;
    public Data _inputReconstructionTransfer;
    public Data _cellWeights;
    public Data _cellBiases1;
    public Data _cellBiases2;
    public Data _cellWeightsVelocity;
    public Data _cellBiases1Velocity;
    public Data _cellBiases2Velocity;
    public Data _cellErrors;
    public Data _cellWeightedSum;
    public Data _cellWeightedSumTopK;
    public Data _cellTransfer;
    public Data _cellTransferTopK;
    public Data _cellTransferTopKA;
    public Data _cellTransferPromoted;
    public Data _cellSpikesTopKA;
    public Data _cellSpikesTopK;
    public Data _cellAges; // age is zero when active, otherwise incremented
    public Data _cellRates; // rate at which cell fires
    public Data _cellPromotion; // idle cells are promoted until they are used
    public Data _cellInhibition; // idle cells are promoted until they are used

    public Data _cellGradients; // hidden layer
    public Data _inputGradients; // output layer, of dimension = inputs

    public OnlineKSparseAutoencoder(String name, ObjectMap om) {
        super( name, om );
    }

    public void setup( OnlineKSparseAutoencoderConfig c ) {
        _c = c;

        int inputs = c.getNbrInputs();
        int w = c.getWidthCells();
        int h = c.getHeightCells();

        _inputValues = new Data( inputs );
//        _inputReconstructionKA = new Data( inputs );
//        _inputReconstructionK = new Data( inputs );
        _inputReconstructionWeightedSum = new Data( inputs );
        _inputReconstructionTransfer = new Data( inputs );
        _cellWeights = new Data( w, h, inputs );
        _cellBiases1 = new Data( w, h );
        _cellBiases2 = new Data( inputs );
        _cellWeightsVelocity = new Data( w, h, inputs );
        _cellBiases1Velocity = new Data( w, h );
        _cellBiases2Velocity = new Data( inputs );
        _cellErrors = new Data( w, h );
        _cellWeightedSum = new Data( w, h );
        _cellWeightedSumTopK = new Data( w, h );
        _cellTransfer = new Data( w, h );
        _cellTransferTopK = new Data( w, h );
        _cellTransferTopKA = new Data( w, h );
        _cellTransferPromoted = new Data( w, h );
        _cellSpikesTopKA = new Data( w, h );
        _cellSpikesTopK = new Data( w, h );
        _cellAges = new Data( w, h );
        _cellRates = new Data( w, h );
        _cellPromotion = new Data( w, h );
        _cellInhibition = new Data( w, h );

        _inputGradients = new Data( inputs );
        _cellGradients = new Data( w, h );
    }

    public void reset() {

        _c.setAge( 0 );

        _cellAges.set(0f);
        _cellRates.set( 0f );

        _c.setBatchCount(0);
        _inputGradients.set( 0f );
        _cellGradients.set( 0f );

        _cellWeightsVelocity.set( 0f );
        _cellBiases1Velocity.set( 0f );
        _cellBiases2Velocity.set( 0f );

        // "We also use a Gaussian distribution with a standard deviation of sigma for initialization of the weights."
        // Better initialization of the weights:
        float weightsStdDev = _c.getWeightsStdDev();

        // http://deeplearning.net/tutorial/mlp.html
        // "The initial values for the weights of a hidden layer i should be uniformly sampled from a symmetric interval that depends on the activation function."
        // ..."where fan_{in} is the number of units in the (i-1)-th layer, and fan_{out} is the number of units in the i-th layer"
        // For sigmoid: [-4\sqrt{\frac{6}{fan_{in}+fan_{out}}},4\sqrt{\frac{6}{fan_{in}+fan_{out}}}].
        // -x : +x

        // where x = 4 * sqrt( 6 / ( fan_in + fan_out ) )
        int cells = _c.getNbrCells();
        int inputs = _inputValues.getSize(); //875.f;
        float randomScale = 4.f * (float)Math.sqrt( 6.f / ( inputs + cells ) ); // approx 0.25

        for( int i = 0; i < _cellWeights.getSize(); ++i ) {
            float r = 0f;
            // the initialization seems ot make a big difference
//            if( _unit ) {
//                double rw = ( _c._r.nextFloat() * randomScale * 2.f ) -randomScale; // for sigmoid activation fn
//                r = (float)( rw );
//            }
//            else {
                r = (float)_c._r.nextGaussian(); // mean: 0, SD: 1
                r *= weightsStdDev;
//            }

            _cellWeights._values[ i ] = r;
        }

        for( int i = 0; i < _cellBiases1.getSize(); ++i ) {
            double r = _c._r.nextGaussian(); // mean: 0, SD: 1
            r *= weightsStdDev;
            _cellBiases1._values[ i ] = (float)r;
        }

        for( int i = 0; i < _cellBiases2.getSize(); ++i ) {
            double r = _c._r.nextGaussian(); // mean: 0, SD: 1
            r *= weightsStdDev;
            _cellBiases2._values[ i ] = (float)r;
        }
    }

    public void updateInhibition() {
        // inihibition of cells that are used too often. (lifetime sparsity)
        // need to measure the rate of use - a slow moving average.
        float rateScale = _c.getRateScale(); // about 5
        float rateMax = _c.getRateMax(); // e.g. 0.1
        int cells = _c.getNbrCells();

        for( int c = 0; c < cells; ++c ) {
            float rate = _cellRates._values[ c ];

            if( rate > rateMax ) {
                rate = rateMax;
            }

            rate = rate / rateMax; // so becomes 1 @ max value
            // 0 = never used
            // 1 = always used
            // bring in the penalty for lifetime sparsity early, at around 20% (0.2)
            // penalty becomes excessive at
            // http://www.wolframalpha.com/input/?i=plot+1-(+e%5E(-5(1-x))+)+for+x+%3D+0+to+1.05
            float factor = 1f - (float)( Math.exp( -rateScale * ( 1.0f - rate ) ) ); // 1 if old, zero if young
            float inhibition = factor; // reduces the value

            _cellInhibition._values[ c ] = inhibition;
        }

    }

    public void updatePromotion() {

        // as cells age, their weight is promoted
        // when very old, they become hypersensitive
        // This is used to rank them higher for learning
        // They will eventually out-compete other cells and become winners in their own right
        // At this point the promotion is removed.

        float ageScale = _c.getAgeScale();//17f;//12f; // affects the slope of the function
        float maxAge = (float)_c.getAgeMax();
        int cells = _c.getNbrCells();

        for( int c = 0; c < cells; ++c ) {
            float age = _cellAges._values[ c ];
            float unitAge = age / (float)maxAge; // 1 iff max age
//            unitAge = Math.min( 1f, unitAge ); // clip at 1  (not doing this because why not increase promotion beyond 2x?

            // 0 = in regular use
            // 1 = never used
            // > 1 = ever increasing promotion
            // http://www.wolframalpha.com/input/?i=plot+e%5E(-17(1-x))+for+x+%3D+0.6+to+1.05
            float factor = (float)( Math.exp( -ageScale * ( 1.0f - unitAge ) ) ); // 1 if old, zero if young
            float promotion = 1f + factor; // ie don't reduce any

            _cellPromotion._values[ c ] = promotion;
        }
    }

    public void updateAges( Collection< Integer > activeCells, float ageFactor, boolean learn ) {
        if( !learn ) {
            return;
        }

        int cells = _c.getNbrCells();

        // increment all ages
        for( int c = 0; c < cells; ++c ) {
            float age = _cellAges._values[ c ];
            age += 1f;
            _cellAges._values[ c ] = age;
        }

        // zero the ages of active cells
        for( Integer c : activeCells ) {
            float age = _cellAges._values[ c ];
            age *= ageFactor;
            _cellAges._values[ c ] = age;
        }
    }

    public void updateRates( Collection< Integer > activeCells, boolean learn ) {
        if( !learn ) {
            return;
        }

        float learningRate = _c.getRateLearningRate();
        float memoryRate = 1f - learningRate;
        int cells = _c.getNbrCells();

        // increment all ages
        for( int c = 0; c < cells; ++c ) {
            float rate = 0f;
            if( activeCells.contains( c ) ) {
                rate = 1f;
            }

            float oldRate = _cellRates._values[ c ];
            float newRate = oldRate * memoryRate + rate * learningRate;
            _cellRates._values[ c ] = newRate;
        }
    }

    public void update() {
        // don't go any further unless learning is enabled
        boolean learn = _c.getLearn();
        update( learn );
    }

    public void update( boolean learn ) {

        float learningRate = _c.getLearningRate();
        float momentum = _c.getMomentum();
        float sparsityOutput = _c.getSparsityOutput(); // alpha
        float ageTruncation = _c.getAgeTruncationFactor(); //0.5f; // halve the age each time it fires
        boolean unit = _c.getUnitOutput();
        int k = _c.getSparsity();
        int ka = (int)( (float)k * sparsityOutput );

        int batchSize = _c.getBatchSize();
        int batchCount = _c.getBatchCount();

        int inputs = _c.getNbrInputs();
        int cells = _c.getNbrCells();

//        // Transpose the weights
//        int rows = cells; // major
//        int cols = inputs; // minor
//        int rowsT = inputs; // major
//        int colsT = cells; // minor

        // Update stats
        updateInhibition();
        updatePromotion();

        // Hidden layer (forward pass)
        TreeMap< Float, ArrayList< Integer > > ranking = new TreeMap< Float, ArrayList< Integer > >();
        TreeMap< Float, ArrayList< Integer > > rankingWithPromotion = new TreeMap< Float, ArrayList< Integer > >();

        for( int c = 0; c < cells; ++c ) {
            float sum = 0.f;

            for( int i = 0; i < inputs; ++i ) {

                int offset = c * inputs +i;

                float input = _inputValues._values[ i ];
                float weight = _cellWeights._values[ offset ];
                float product = input * weight;
                sum += product;
            }

            float bias = _cellBiases1._values[ c ];

            sum += bias;

            _cellWeightedSum._values[ c ] = sum;

            float transfer = (float) ActivationFunction.logisticSigmoid(sum);

            _cellTransfer._values[ c ] = transfer;

            Ranking.add( ranking, transfer, c ); // this is the new output

            float promotion  = _cellPromotion._values[ c ];
//            float inhibition = _cellInhibition._values[ c ];
            float transferPromoted = transfer * promotion;// * inhibition;
//            float transferPromoted = sum;//transfer * promotion;// * inhibition;

            _cellTransferPromoted._values[ c ] = transferPromoted;

            Ranking.add( rankingWithPromotion, transferPromoted, c ); // this is the new output
        }

        // Hidden Layer Nonlinearity: Make all except top k cells zero.
        // OUTPUT KA
        boolean findMaxima = true;

        // ka used for OUTPUT
        int maxRank = ka;
        ArrayList< Integer > activeCellsKA = Ranking.getBestValues( ranking, findMaxima, maxRank );
        _cellSpikesTopKA.set( 0.f );
        _cellTransferTopKA.set( 0f );
        for( Integer c : activeCellsKA ) {
            _cellSpikesTopKA._values[ c ] = 1f;
            _cellTransferTopKA._values[ c ] = _cellTransfer._values[ c ];
        }

        // now restrict to just k. This set is used for learning.
        // OUTPUT K
        // NOTE: Use the promoted transfer value for training
        maxRank = k;
        ArrayList< Integer > activeCellsK = Ranking.getBestValues( rankingWithPromotion, findMaxima, maxRank );
        _cellSpikesTopK.set( 0f );
        _cellWeightedSumTopK.set( 0f );
        _cellTransferTopK.set( 0f );
        for( Integer c : activeCellsK ) {
            _cellSpikesTopK._values[ c ] = 1f;
            _cellWeightedSumTopK._values[ c ] = _cellWeightedSum._values[ c ];
            _cellTransferTopK._values[ c ] = _cellTransfer._values[ c ];
        }

        // NOTE: Update ages with the *non* promoted ranking, to require a "natural" win indicating the weights have learned to be useful to zero the age
        // NOTE: I tried the above, but it just got fixated. Seems like you have to learn it once, then remove the promotion.

        // NOTE: Update ages with the *non* promoted ranking, to require a "natural" win indicating the weights have learned to be useful to zero the age
        // NOTE: I tried the above, but it just got fixated. Seems like you have to learn it once, then remove the promotion.
        updateAges( activeCellsK, ageTruncation, learn );
        updateRates( activeCellsK, learn );


        // Output layer (forward pass)
        // dont really need to do this if not learning.
//        reconstruct( _cellTransferTopK, _inputReconstructionWeightedSum, _inputReconstructionTransfer ); // for output
//        reconstruct( _cellSpikesTopK, _inputReconstructionWeightedSum, _inputReconstructionTransfer ); // for output
        if( unit ) {
            reconstruct(_cellWeightedSumTopK, _inputReconstructionWeightedSum, _inputReconstructionTransfer); // for output
        }
        else {
            reconstruct( _cellTransferTopK, _inputReconstructionWeightedSum, _inputReconstructionTransfer ); // for output
        }

        // don't go any further unless learning is enabled
        if( !learn ) {
            return;
        }

        boolean useMomentum = false;
        if( momentum != 0f ) {
            useMomentum = true;
        }

        // http://neuralnetworksanddeeplearning.com/chap2.html
        // Compute the error
        // Cost is 0.5 * mean sq error
        //        = 0.5 * ( sum( target - output ) ^2 )
        // d^L = dCost / dOutput = a_j - y_j where a = output and y = target
        // but we want:
        // d^L = dCost / dWeightedSum = a_j - y_j * derivative( z_j^L ) where a = output and y = target
        // but the derivative is 1 so ...
        // d^L = a_j - y_j where a = output and y = target
        // for other layers:
        // d^l = (( w^l+1 )T * d^l+1 ) * deriv( z^l )
        // dCost / dBias = d^l
        // dCost / dw^l_jk = a^l-1_k * d
        // b' = b + learningRate * d
        // w' = w + learningRate * d * a^l-1
//        FloatArray weightsNew = null;

        FloatArray dOutput = new FloatArray( inputs );
        FloatArray dHidden = new FloatArray( cells ); // zeroes

        // d output layer
        for( int i = 0; i < inputs; ++i ) {
            float weightedSum = _inputReconstructionWeightedSum._values[ i ]; //output; // z
            float target = _inputValues._values[ i ]; // y
            float output = 0f;
            float derivative = 1f;

            if( unit ) {
                float transfer = _inputReconstructionTransfer._values[ i ]; //output; // z
                output = transfer;
                derivative = (float) ActivationFunction.logisticSigmoidDerivative(weightedSum);
            }
            else {
                output = weightedSum;
            }

            float error = output - target; // == d^L

            dOutput._values[ i ] = error * derivative; // eqn 30
        }

        // compute gradient in hidden units. Derivative is either 1 or 0 depending whether the cell was filtered.
        for( int c = 0; c < cells; ++c ) { // computing error for each "input"
            float sum = 0.f;

////            float spikesTopK = _cellSpikesTopK._values[ c ];
            float weightedSumTopK = _cellWeightedSumTopK._values[ c ];
//            float derivative = 1f;//(float)TransferFunction.logisticSigmoidDerivative( weightedSum );

            boolean active = false;
            if( weightedSumTopK != 0f ) {
                active = true;
            }

 //           if( spikesTopK > 0f ) { // if was cell active
//           if( weightedSumTopK != 0f ) { // if was cell active
            if( active ) {

                float derivative = 1f;
                if( unit ) {
                    derivative = (float) ActivationFunction.logisticSigmoidDerivative(weightedSumTopK);
                }

                for( int i = 0; i < inputs; ++i ) {
                    //int offset = j * K + k; // K = inputs, storage is all inputs adjacent
                    int offset = c * inputs + i;
                    float w = _cellWeights._values[ offset ];
                    float d = dOutput._values[ i ]; // d_j i.e. partial derivative of loss fn with respect to the activation of j
                    float product = d * w;// + ( l2R * w );

                    Useful.IsBad( product ); // for debugging

                    sum += product;
                }

                // with linear neurons, derivative is 1, but here it is nonlinear now
                sum *= derivative;  // eqn (BP2)
            }
            // else: derivative is zero when filtered

            dHidden._values[ c ] = sum;
        }

        // accumulate the error gradients
        for( int i = 0; i < inputs; ++i ) {
            float dNew = dOutput._values[ i ];
            float dOld = _inputGradients._values[ i ];
            _inputGradients._values[ i ] = dOld + dNew;
        }

        for( int i = 0; i < cells; ++i ) {
            float dNew = dHidden._values[ i ];
            float dOld = _cellGradients._values[ i ];
            _cellGradients._values[ i ] = dOld + dNew;
        }

        // decide whether to learn or accumulate more gradients first (mini batch)
        batchCount += 1;

        if( batchCount < batchSize ) { // e.g. if was zero, then becomes 1, then we clear it and apply the gradients
            _c.setBatchCount( batchCount );
            return; // end update
        }

        // now gradient descent in the hidden->output layer
        for( int i = 0; i < inputs; ++i ) {

//            float errorGradient = dOutput._values[ i ];
            float errorGradient = _inputGradients._values[ i ];

//            if( errorGradient == 0f ) {
//                continue;
//            }

            for( int c = 0; c < cells; ++c ) { // computing error for each "input"

                int offset = c * inputs + i;

                // a is the input to the current layer's cells
                float a = 0f;
                if( unit ) {
                    a = _cellTransferTopK._values[ c ];
                }
                else {
                    a = _cellWeightedSumTopK._values[ c ];
                }

                float wOld = _cellWeights._values[ offset ];
                float wDelta = learningRate * errorGradient * a;

                if( useMomentum ) {
                    // Momentum
                    // x_k+1 = x_k + v_k
                    // v_k+1 = m_k * v_k - learningRate * derivative of error WRT k
                    float vOld = _cellWeightsVelocity._values[ offset ];
                    float vNew = ( vOld * momentum ) - wDelta;
                    float wNew = wOld + vNew;

                    Useful.IsBad( wNew );

                    _cellWeights._values[ offset ] = wNew;
                    _cellWeightsVelocity._values[ offset ] = vNew;
                }
                else {
                    // Normal
                    float wNew = wOld - wDelta;

                    Useful.IsBad( wNew );

                    _cellWeights._values[ offset ] = wNew;
                }


//                float vNew = 0f;

                // Weight decay / L2 regularization
//                float regularization = learningRate * REGULARIZATION * wOld;
//                float wNew = wOld - wDelta - regularization;

                // Weight clipping
//                float wMax = 1.f;
//                if( wNew > wMax ) wNew = wMax;
//                if( wNew < -wMax ) wNew = -wMax;
            }

            float bOld = _cellBiases2._values[ i ];
            float bDelta = learningRate * errorGradient;

            if( useMomentum ) {
                float vOld = _cellBiases2Velocity._values[ i ];
                float vNew = ( vOld * momentum ) - bDelta;
                float bNew = bOld + vNew;

                _cellBiases2._values[ i ] = bNew;
                _cellBiases2Velocity._values[ i ] = vNew;
            }
            else {
                float bNew = bOld - bDelta;

                _cellBiases2._values[ i ] = bNew;
            }
        }

        // now gradient descent in the input->hidden layer
        // can't skip this because we need to update the biases
//        float vMin = Float.MAX_VALUE;
//        float vMax = Float.MIN_VALUE;

        for( int c = 0; c < cells; ++c ) { // computing error for each "input"

//            float errorGradient = dHidden._values[ c ];
            float errorGradient = _cellGradients._values[ c ];

//            if( errorGradient == 0f ) {
//                continue;
//            }

            for( int i = 0; i < inputs; ++i ) {

                int offset = c * inputs + i;

                float a = _inputValues._values[ i ];
                float wOld = _cellWeights._values[ offset ];
                float wDelta = learningRate * errorGradient * a;

                if( useMomentum ) {
                    // Momentum
                    float wNew = wOld - wDelta;

                    Useful.IsBad( wNew );

                    _cellWeights._values[ offset ] = wNew;
                }
                else {
                    // Momentum
                    float vOld = _cellWeightsVelocity._values[ offset ];
                    float vNew = ( vOld * momentum ) - wDelta;
                    float wNew = wOld + vNew;

                    Useful.IsBad( wNew );

                    _cellWeights._values[ offset ] = wNew;
                    _cellWeightsVelocity._values[ offset ] = vNew;
                }
//                float vNew = 0f;

//                 vMax = Math.max( vMax, vNew );
//                vMin = Math.min( vMin, vNew );
                // Weight decay / L2 regularization
//                float regularization = learningRate * REGULARIZATION * wOld;
//                float wNew = wOld - wDelta - regularization;

                // Weight clipping
//                float wMax = 1.f;
//                if( wNew > wMax ) wNew = wMax;
//                if( wNew < -wMax ) wNew = -wMax;
            }

            float bOld = _cellBiases1._values[ c ];
            float bDelta = learningRate * errorGradient;

            if( useMomentum ) {
                float vOld = _cellBiases1Velocity._values[ c ];
                float vNew = ( vOld * momentum ) - bDelta;
                float bNew = bOld + vNew;

                _cellBiases1._values[ c ] = bNew;
                _cellBiases1Velocity._values[ c ] = vNew;
            }
            else {
                float bNew = bOld - bDelta;

                _cellBiases1._values[ c ] = bNew;
            }
        }

//        System.err.println( "Age: " + this._c.getAge() + " Sparsity: " + k  + " vMax = " + vMax );

        // Clear the accumulated gradients
        _c.setBatchCount( 0 );
        _inputGradients.set( 0f );
        _cellGradients.set( 0f );
    }

    protected void reconstruct( Data hiddenActivity, Data inputReconstructionWeightedSum, Data inputReconstructionTransfer ) {
        int inputs = _c.getNbrInputs();
        int cells = _c.getNbrCells();

        for( int i = 0; i < inputs; ++i ) {
            float sum = 0.f;

            for( int c = 0; c < cells; ++c ) {

                float response = hiddenActivity._values[ c ];// _cellTransferTopK._values[ c ];

                int offset = c * inputs +i;
                float weight = _cellWeights._values[ offset ];
                float product = response * weight;
                sum += product;
            }

            float bias = _cellBiases2._values[ i ];

            sum += bias; // weightedSum

            inputReconstructionWeightedSum._values[ i ] = sum;

            float transfer = (float) ActivationFunction.logisticSigmoid(sum);

            inputReconstructionTransfer._values[ i ] = transfer;
        }

    }
}
