/*
 * Copyright (c) 2017.
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

import io.agi.core.data.Data;
import io.agi.core.data.FloatArray;
import io.agi.core.data.Ranking;
import io.agi.core.math.Unit;
import io.agi.core.math.Useful;
import io.agi.core.orm.ObjectMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;

/**
 * An implementation of K-Sparse Autoencoders by Alireza Makhzani and Brendan Frey
 *
 * Full paper at: http://arxiv.org/abs/1312.5663
 *
 * One small difference: I saw in a later paper the authors set the gradients to zero to implement the sparsening. But,
 * in this paper I wasn't sure. Emailed them, and Ali says:
 *
 * "The implementation is only one line of code, just like dropout. In dropout, you randomly set hidden units to zero,
 * in k-sparse autoencoder you set the k largest units to zero in the forward pass. If you set the activation to zero,
 * then the gradient doesn't go back through that hidden unit. So you only need to modify the forward pass.
 *
 * Also, I bumped into Eric Laukien on the internet and he said he had better results setting the hidden activation of
 * the k hidden cells to a unit value, so I've added that as an option but it isn't tested yet.
 *
 * Created by dave on 1/07/16.
 */
public class CompetitiveKSparseAutoencoder extends CompetitiveLearning {

    protected static final Logger logger = LogManager.getLogger();

    public CompetitiveKSparseAutoencoderConfig _c;
    public Data _inputValues;
    public Data _inputReconstructionKA;
    public Data _inputReconstructionK;
    public Data _cellWeights;
    public Data _cellBiases1;
    public Data _cellBiases2;
    public Data _cellWeightsVelocity;
    public Data _cellBiases1Velocity;
    public Data _cellBiases2Velocity;
    public Data _cellErrors;
    public Data _cellWeightedSum;
    public Data _cellSpikesTopKA;
    public Data _cellSpikesTopK;
    public Data _cellAges; // age is zero when active, otherwise incremented
    public Data _cellRates; //
    public Data _cellCorrelation; //

    public Data _cellGradients; // hidden layer
    public Data _inputGradients; // output layer, of dimension = inputs

    public CompetitiveKSparseAutoencoder( String name, ObjectMap om ) {
        super( name, om );
    }

    public void setup( CompetitiveKSparseAutoencoderConfig c ) {
        _c = c;

        int inputs = c.getNbrInputs();
        int w = c.getWidthCells();
        int h = c.getHeightCells();
        int cells = w * h;

        _inputValues = new Data( inputs );
        _inputReconstructionKA = new Data( inputs );
        _inputReconstructionK = new Data( inputs );
        _cellWeights = new Data( w, h, inputs );
        _cellBiases1 = new Data( w, h );
        _cellBiases2 = new Data( inputs );
        _cellWeightsVelocity = new Data( w, h, inputs );
        _cellBiases1Velocity = new Data( w, h );
        _cellBiases2Velocity = new Data( inputs );
        _cellErrors = new Data( w, h );
        _cellWeightedSum = new Data( w, h );
        _cellSpikesTopKA = new Data( w, h );
        _cellSpikesTopK = new Data( w, h );

//        _cellAges = new Data( w, h );
        _cellRates = new Data( w, h );
        _cellCorrelation = new Data( cells, cells );

        _inputGradients = new Data( inputs );
        _cellGradients = new Data( w, h );
    }

    public void reset() {

//        _cellAges.set(0f);
        _cellRates.set( 0f );
        _cellCorrelation.set( 0f );

        _c.setBatchCount( 0 );
        _c.setBisectionCount( 0 );
        _inputGradients.set( 0f );
        _cellGradients.set( 0f );

        _cellWeightsVelocity.set( 0f );
        _cellBiases1Velocity.set( 0f );
        _cellBiases2Velocity.set( 0f );

        // "We also use a Gaussian distribution with a standard deviation of sigma for initialization of the weights."
        // Better initialization of the weights:
        float weightsStdDev = _c.getWeightsStdDev();

        for( int i = 0; i < _cellWeights.getSize(); ++i ) {
            double r = _c._r.nextGaussian(); // mean: 0, SD: 1
            r *= weightsStdDev;
            _cellWeights._values[ i ] = (float)r;// / sqRtInputs;
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

//    public void updateAges( Collection< Integer > activeCells, float ageFactor, boolean learn ) {
//        if( !learn ) {
//            return;
//        }
//
//        int cells = _c.getNbrCells();
//
//        // increment all ages
//        for( int c = 0; c < cells; ++c ) {
//            float age = _cellAges._values[ c ];
//            age += 1f;
//            _cellAges._values[ c ] = age;
//        }
//
//        // zero the ages of active cells
//        for( Integer c : activeCells ) {
//            float age = _cellAges._values[ c ];
//            age *= ageFactor;
//            _cellAges._values[ c ] = age;
//        }
//    }
//
    public void update() {

        boolean learn = _c.getLearn();

        // have declining sparsity
        // z = W^T x  +b
        // x^ = W z + b'    note second set of biases
        // set k largest of z to same, others to zero
        // min. mean sq. error E = || x^ - x ||2
        // backprop through the k active cells only
        // https://en.wikipedia.org/wiki/Delta_rule
        // http://deeplearning.stanford.edu/wiki/index.php/Deriving_gradients_using_the_backpropagation_idea
        // http://www.ericlwilkinson.com/blog/2014/11/19/deep-learning-sparse-autoencoders
        // https://en.wikipedia.org/wiki/Backpropagation
        // for encoding, activate alpha * k largest activations
        // mean sq err is quadratic cost function http://neuralnetworksanddeeplearning.com/chap1.html
//        float friction = 0.2f;
        float learningRate = _c.getLearningRate();
        float momentum = _c.getMomentum();
        float sparsityOutput = _c.getSparsityOutput(); // alpha

        int k = _c.getSparsity();
        int ka = (int)( (float)k * sparsityOutput );

        int batchSize = _c.getBatchSize();
        int batchCount = _c.getBatchCount();

        int inputs = _c.getNbrInputs();
        int cells = _c.getNbrCells();

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

            Ranking.add( ranking, sum, c );
        }

        // Hidden Layer Nonlinearity: Make all except top k cells zero.
        // OUTPUT KA
        boolean findMaxima = true;

        // ka used for OUTPUT
        int maxRank = ka;
        ArrayList< Integer > activeCellsKA = Ranking.getBestValues( ranking, findMaxima, maxRank );
        _cellSpikesTopKA.set( 0.f );
        for( Integer c : activeCellsKA ) {
            float transfer = _cellWeightedSum._values[ c ]; // otherwise zero
            _cellSpikesTopKA._values[ c ] = transfer;
        }

        // now restrict to just k. This set is used for learning.
        // OUTPUT K
        maxRank = k;
        ArrayList< Integer > activeCellsK = Ranking.getBestValues( ranking, findMaxima, maxRank );
        _cellSpikesTopK.set( 0f );
        for( Integer c : activeCellsK ) {
            float transfer = _cellWeightedSum._values[ c ]; // otherwise zero
            _cellSpikesTopK._values[ c ] = transfer;
        }

//        float ageFactor = 0f;
//        updateAges( activeCellsKA, ageFactor, learn ); // make ages zero on firing

        // Output layer (forward pass)
        // dont really need to do this if not learning.
        reconstruct( _cellSpikesTopKA, _inputReconstructionKA ); // for learning
        reconstruct( _cellSpikesTopK, _inputReconstructionK ); // for output

        // don't go any further unless learning is enabled
        if( !learn ) {
            return;
        }

        updateRates( activeCellsK );
        updateCorrelation( activeCellsK );

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

        // Compute gradients for this current input only
        FloatArray dOutput = new FloatArray( inputs );
        FloatArray dHidden = new FloatArray( cells ); // zeroes

        // d output layer
        for( int i = 0; i < inputs; ++i ) {
            float target = _inputValues._values[ i ]; // y
            float output = _inputReconstructionK._values[ i ]; // a
            float error = output - target; // == d^L
            //float weightedSum = output; // z
            float derivative = 1f;//(float)TransferFunction.logisticSigmoidDerivative( weightedSum );

            dOutput._values[ i ] = error * derivative; // eqn 30
        }

        // compute gradient in hidden units. Derivative is either 1 or 0 depending whether the cell was filtered.
        for( int c = 0; c < cells; ++c ) { // computing error for each "input"
            float sum = 0.f;

            float transferTopK = _cellSpikesTopK._values[ c ];
//            float weightedSum = _cellWeightedSum._values[ c ];
            float derivative = 1f;//(float)TransferFunction.logisticSigmoidDerivative( weightedSum );

            if( transferTopK > 0f ) { // if was cell active
                for( int i = 0; i < inputs; ++i ) {
                    //int offset = j * K + k; // K = inputs, storage is all inputs adjacent
                    int offset = c * inputs + i;
                    float w = _cellWeights._values[ offset ];
                    float d = dOutput._values[ i ]; // d_j i.e. partial derivative of loss fn with respect to the activation of j
                    float product = d * w;// + ( l2R * w );

                    if( Useful.IsBad( product ) ) {
                        String error = "Autoencoder error derivative update produced a bad value: " + product;
                        logger.error( error );
                        logger.traceExit();
                        System.exit( -1 );
                    }

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

        float miniBatchNorm = 1f / (float)batchSize;

        // now gradient descent in the hidden->output layer
        for( int i = 0; i < inputs; ++i ) {

//            float errorGradient = dOutput._values[ i ];
            float errorGradient = _inputGradients._values[ i ];

//            if( errorGradient == 0f ) {
//                continue;
//            }

            for( int c = 0; c < cells; ++c ) { // computing error for each "input"

                int offset = c * inputs + i;

                float a = _cellSpikesTopK._values[ c ];
                float wOld = _cellWeights._values[ offset ];
                float wDelta = learningRate * miniBatchNorm * errorGradient * a;

                if( useMomentum ) {
                    // Momentum
                    // x_k+1 = x_k + v_k
                    // v_k+1 = m_k * v_k - learningRate * derivative of error WRT k
                    float vOld = _cellWeightsVelocity._values[ offset ];
                    float vNew = ( vOld * momentum ) - wDelta;
                    float wNew = wOld + vNew;

                    if( Useful.IsBad( wNew ) ) {
                        String error = "Autoencoder weight update produced a bad value: " + wNew;
                        logger.error( error );
                        logger.traceExit();
                        System.exit( -1 );
                    }

                    _cellWeights._values[ offset ] = wNew;
                    _cellWeightsVelocity._values[ offset ] = vNew;
                }
                else {
                    // Normal
                    float wNew = wOld - wDelta;

                    if( Useful.IsBad( wNew ) ) {
                        String error = "Autoencoder weight update produced a bad value: " + wNew;
                        logger.error( error );
                        logger.traceExit();
                        System.exit( -1 );
                    }

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
            float bDelta = learningRate * miniBatchNorm * errorGradient;

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
                float wDelta = learningRate * miniBatchNorm * errorGradient * a;

                if( useMomentum ) {
                    // Momentum
                    float wNew = wOld - wDelta;

                    if( Useful.IsBad( wNew ) ) {
                        String error = "Autoencoder weight update produced a bad value: " + wNew;
                        logger.error( error );
                        logger.traceExit();
                        System.exit( -1 );
                    }

                    _cellWeights._values[ offset ] = wNew;
                }
                else {
                    // Momentum
                    float vOld = _cellWeightsVelocity._values[ offset ];
                    float vNew = ( vOld * momentum ) - wDelta;
                    float wNew = wOld + vNew;

                    if( Useful.IsBad( wNew ) ) {
                        String error = "Autoencoder weight update produced a bad value: " + wNew;
                        logger.error( error );
                        logger.traceExit();
                        System.exit( -1 );
                    }

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
            float bDelta = learningRate * miniBatchNorm * errorGradient;

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

        // now look at cell lifetime sparsity.
        // if we have reached a point
        int bisectionCount = _c.getBisectionCount();
        bisectionCount += 1;
        int bisectionInterval = _c.getBisectionInterval();
        if( bisectionCount < bisectionInterval ) {
            _c.setBisectionCount( bisectionCount );
            return;
        }

        // TODO consider stochastic choice
        int minRateCell = 0;
        int maxRateCell1 = 0;
        float minRate = 1f;
        float maxRate = 0f;
        for( int c = 0; c < cells; ++c ) { // computing error for each "input"

            float rate = _cellRates._values[ c ];

            if( rate <= minRate ) {
                minRate = rate;
                minRateCell = c;
            }

            if( rate <= maxRate ) {
                maxRate = rate;
                maxRateCell1 = c;
            }
        }

        // most correlated partner
        // TODO consider stochastic choice
        float maxCorrelation = 0f;
        int maxRateCell2 = 0;

        for( int c = 0; c < cells; ++c ) { // computing error for each "input"

            int c1 = maxRateCell1;
            int c2 = c;
            if( c2 < c1 ) {
                c1 = c;
                c2 = maxRateCell1;
            }

            int offset = c1 * cells + c2;

            float correlation = _cellCorrelation._values[ offset ];

            if( correlation >= maxCorrelation ) {
                maxCorrelation = correlation;
                maxRateCell2 = c;
            }
        }

        // only recycle when cell has frequency less than threshold?
//        float cellRateThreshold = ;
//
//        if( minRate >= cellRateThreshold ) {
//            return; // no growth due to all cells being used
//        }

        // find a high frequency cell and its most correlated partner.
        // bisect these cells into a new cell
        // Hopefully this will displace other cells into disuse
        bisectCells( maxRateCell1, maxRateCell2, minRateCell );

        // reduce the rates on the old cells?
        // nah they have time to adjust

        _c.setBisectionCount( 0 );
    }

    protected void bisectCells( int cellA, int cellB, int cellAB ) {
        int inputs = _c.getNbrInputs();

        for( int i = 0; i < inputs; ++i ) {

            int offsetA = cellA * inputs +i;
            int offsetB = cellB * inputs +i;
            int offsetAB = cellAB * inputs +i;

            float weightA = _cellWeights._values[ offsetA ];
            float weightB = _cellWeights._values[ offsetB ];
            float weightAB = ( weightA + weightB ) * 0.5f;

            _cellWeights._values[ offsetAB ] = weightAB;
        }

        float bias1A = _cellBiases1._values[ cellA ];
        float bias1B = _cellBiases1._values[ cellB ];
        float bias1AB = ( bias1A + bias1B ) * 0.5f;

        _cellBiases1._values[ cellAB ] = bias1AB;

        // give the new cell lots of time
        _cellRates._values[ cellAB ] = 1f;
    }

    public void updateCorrelation( Collection< Integer > activeCells ) {
        float learningRate = _c.getCorrelationLearningRate();
        int cells = _c.getNbrCells();

        // increment all ages
        for( int c1 = 0; c1 < cells; ++c1 ) {
            for( int c2 = 0; c2 < cells; ++c2 ) {

                if( c2 <= c1 ) {
                    continue;
                }

                float correlation = 0f;
                if( activeCells.contains( c1 ) ) {
                    if( activeCells.contains( c2 ) ) {
                        correlation = 1f;
                    }
                }

                int offset = c1 * cells + c2;
                float oldCorrelation = _cellCorrelation._values[ offset ];
                float newCorrelation = Unit.lerp( oldCorrelation, correlation, 1f-learningRate );

                _cellCorrelation._values[ offset ] = newCorrelation;
            }
        }
    }

    public void updateRates( Collection< Integer > activeCells ) {

        float learningRate = _c.getRateLearningRate();
        int cells = _c.getNbrCells();

        // increment all ages
        for( int c = 0; c < cells; ++c ) {
            float rate = 0f;
            if( activeCells.contains( c ) ) {
                rate = 1f;
            }

            float oldRate = _cellRates._values[ c ];
            float newRate = Unit.lerp( oldRate, rate, 1f-learningRate );
            _cellRates._values[ c ] = newRate;
        }
    }

    protected void reconstruct( Data hiddenActivity, Data inputReconstruction ) {
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

            sum += bias;

            inputReconstruction._values[ i ] = sum;
        }

    }

}
