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

import io.agi.core.ann.supervised.TransferFunction;
import io.agi.core.data.Data;
import io.agi.core.data.FloatArray;
import io.agi.core.data.Otsu;
import io.agi.core.data.Ranking;
import io.agi.core.math.Useful;
import io.agi.core.orm.ObjectMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.TreeMap;

/**
 * A derivation of of K-Sparse Autoencoders by Alireza Makhzani and Brendan Frey
 *
 * Full paper at: http://arxiv.org/abs/1312.5663
 *
 * Created by dave on 1/07/16.
 */
public class ConsensusAutoencoder extends CompetitiveLearning {

    public ConsensusAutoencoderConfig _c;
    public ArrayList< Integer > _sparseUnitInput;
    public Data _inputValues;
    public Data _inputReconstructionWeightedSum;
    public Data _inputReconstruction;
    public Data _cellWeights;
    public Data _cellBiases1;
    public Data _cellBiases2;
    public Data _cellErrors;
    public Data _cellWeightedSum;
    public Data _cellTransfer;
    public Data _cellTransferTopK;
    public Data _cellActivity;
    public Data _cellAges; // age is zero when active, otherwise incremented
    public Data _cellPromotion; // idle cells are promoted until they are used
    public Data _cellInhibitionWeights;
    public Data _inhibitionHistory;
    public Data _concensusHistory;

    protected boolean _ageLearn = true;

    protected ArrayList< Integer > _activeCells = new ArrayList< Integer >();

    public ConsensusAutoencoder( String name, ObjectMap om ) {
        super( name, om );
    }

    public void setup( ConsensusAutoencoderConfig c ) {
        _c = c;

        int inputs = c.getNbrInputs();
        int w = c.getWidthCells();
        int h = c.getHeightCells();

        _inputValues = new Data( inputs );
        _inputReconstructionWeightedSum = new Data( inputs );
        _inputReconstruction = new Data( inputs );
        _cellWeights = new Data( w, h, inputs );
        _cellBiases1 = new Data( w, h );
        _cellBiases2 = new Data( inputs );
        _cellErrors = new Data( w, h );
        _cellWeightedSum = new Data( w, h );
        _cellTransfer = new Data( w, h );
        _cellTransferTopK = new Data( w, h );
        _cellActivity = new Data( w, h );
        _cellAges = new Data( w, h );
        _cellPromotion = new Data( w, h );

        int concensusSteps = c.getConsensusSteps(); // TODO from config
        int cells = w * h;
        _cellInhibitionWeights = new Data( cells, cells );
        _inhibitionHistory = new Data( cells, concensusSteps );
         _concensusHistory = new Data( cells, concensusSteps );
    }

    public void reset() {

        // Better initialization of the weights:
        // http://neuralnetworksanddeeplearning.com/chap3.html
        // init weights to SD = 1/ sqrt( n ) where n is number of inputs.
        int inputs = _inputValues.getSize(); //875.f;
//        float sqRtInputs = (float)Math.sqrt( (float)inputs );
/*        for( int i = 0; i < _cellWeights.getSize(); ++i ) {
            double r = _c._r.nextGaussian(); // mean: 0, SD: 1
            _cellWeights._values[ i ] = (float)r / sqRtInputs;
        }*/

        for( int i = 0; i < _cellBiases1.getSize(); ++i ) {
            double r = _c._r.nextGaussian(); // mean: 0, SD: 1
            _cellBiases1._values[ i ] = (float)r;
        }

        for( int i = 0; i < _cellBiases2.getSize(); ++i ) {
            double r = _c._r.nextGaussian(); // mean: 0, SD: 1
            _cellBiases2._values[ i ] = (float)r;
        }

        _cellInhibitionWeights.set( 0.01f ); // no association

        // From the textbook:
        //        Then we shall initialize those weights as Gaussian random variables with mean 00 and standard deviation 1/nin−−−√1/nin.
        //        That is, we'll squash the Gaussians down, making it less likely that our neuron will saturate.
        //        We'll continue to choose the bias as a Gaussian with mean 00 and standard deviation 11, for reasons
        //        I'll return to in a moment. With these choices, the weighted sum z=∑jwjxj+bz=∑jwjxj+b will again be a
        //        Gaussian random variable with mean 00, but it'll be much more sharply peaked than it was before.
        //        Suppose, as we did earlier, that 500500 of the inputs are zero and 500500 are 11. Then it's easy to
        //        show (see the exercise below) that zz has a Gaussian distribution with mean 00 and standard deviation
        //        3/2−−−√=1.22…3/2=1.22…. This is much more sharply peaked than before, so much so that even the graph
        //        below understates the situation, since I've had to rescale the vertical axis, when compared to the earlier graph:

        //        _cellWeights.random( _c._r );
        //        _cellBiases1.random( _c._r );
        //        _cellBiases2.random( _c._r );

        // http://deeplearning.net/tutorial/mlp.html
        // "The initial values for the weights of a hidden layer i should be uniformly sampled from a symmetric interval that depends on the activation function."
        // ..."where fan_{in} is the number of units in the (i-1)-th layer, and fan_{out} is the number of units in the i-th layer"
        // For sigmoid: [-4\sqrt{\frac{6}{fan_{in}+fan_{out}}},4\sqrt{\frac{6}{fan_{in}+fan_{out}}}].
        // -x : +x
        // where x = 4 * sqrt( 6 / ( fan_in + fan_out ) )
        int cells = _c.getNbrCells();
        float randomScale = 4.f * (float)Math.sqrt( 6.f / ( inputs + cells ) );

        for( int i = 0; i < _cellWeights.getSize(); ++i ) {
            float r = ( _c._r.nextFloat() * randomScale * 2.f ) -randomScale;
            _cellWeights._values[ i ] = r;
        }

        _c.setAge( 0 );
    }

    public void call() {
        update();
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

    public void updateAges( Collection< Integer > activeCells, float ageFactor ) {
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

    public int updateSparsity() {
        int kMin = _c.getSparsityMin();
//        int kMax = _c.getSparsityMax();
//        int ageMin = _c.getAgeMin();
//        int ageMax = _c.getAgeMax();
        int age = _c.getAge();
        int k = kMin;
        _c.setSparsity( k );
        _c.setAge( age + 1 );

        return k;
    }

    public void update() {
        // don't go any further unless learning is enabled
        boolean learn = _c.getLearn();
        learn = learn & _ageLearn;
        update( learn );
    }

    protected static void trainInhibition(
            ConsensusAutoencoderConfig config,
//            Data cellActivation,
            ArrayList< Integer > activeCells,
            Data inhibitionWeights ) {

        int cells = config.getNbrCells();

        // new parameters:
        float concensusLearningRate = config.getConsensusLearningRate();// 0.01f;
        float concensusDecayRate = config.getConsensusDecayRate();// 0.99f; // the system is nonstationary so we want it to be able to reduce weights not just grow more slowly

//        HashSet< Integer > activeCells = initialActiveCells.indicesMoreThan( 0f );
        HashSet< Integer > activeCellSet = new HashSet< Integer >();
        activeCellSet.addAll( activeCells );

        for( int j = 0; j < cells; ++j ) { // all cells (probably doesnt matter as y will be small when cell is not active
//        for( Integer j : activeCells ) { // only active cells

            float y = 0;

            for( int i = 0; i < cells; ++i ) {
                int inhibitionOffset = getInhibitionOffset( j, i, cells );
                float w_ji = inhibitionWeights._values[ inhibitionOffset ];
                float x_i = 0f;
                if( activeCellSet.contains( i ) ) {
                    x_i = 1f;
                }
                float y_i = x_i * w_ji;
                y += y_i;
            }

            float ySq = y * y;

            for( int i = 0; i < cells; ++i ) {

// Not sure about this
//                if( c1 > c2 ) {
//                    continue;
//                }


                // how much does c1 inhibit c2?
                // the inverse of the association between them
                // use ojas rule to train inhibition?
                // https://en.wikipedia.org/wiki/Oja%27s_rule
                // http://www.scholarpedia.org/article/Oja_learning_rule
                int inhibitionOffset = getInhibitionOffset( j, i, cells );
                float w1 = inhibitionWeights._values[ inhibitionOffset ];
                float w_ji = w1;
                float x_i = 0f;
                if( activeCellSet.contains( i ) ) {
                    x_i = 1f;
                }
                // e.g. x = 1, y = 0.01 (wrong input)
                // 0.01 * 0.01 = 0.001
                //
                float delta = concensusLearningRate * ( x_i * y - ySq * w_ji );
                float w2 = w1 * concensusDecayRate + delta;
                inhibitionWeights._values[ inhibitionOffset ] = w2;
            }
        }
    }

    protected static int getInhibitionOffset( int c1, int c2, int cells ) {
//        int ca = c1;
//        int cb = c2;
//        if( c2 < c1 ) {
//            ca = c2;
//            cb = c1;
//        }
//        int inhibitionOffset = ca * cells + cb;
        int inhibitionOffset = c1 * cells + c2;
        return inhibitionOffset;
    }

    protected static HashSet< Integer > findConsensus(
            ConsensusAutoencoderConfig config,
//            Data initialActiveCells,
            ArrayList< Integer > initialActiveCells,
            Data cellWeightedSums,
            Data cellActivation,
            Data concensusHistory,
            Data inhibitionHistory,
            Data inhibitionWeights ) {
        // find the concensus without promotion. That's only for learning.
        // given a set of k active cells
        // use these to inhibit other cells
//        int inputs = _c.getNbrInputs();
        int cells = config.getNbrCells();
        int k = config.getSparsity();
        float sparsityOutput = config.getSparsityOutput(); // alpha

        // new parameters:
        int maxConcensusSteps = config.getConsensusSteps();
        float consensusStrength = config.getConsensusStrength();// 0.1f;

        // preparation:
        int k2 = (int)( (float)k * sparsityOutput );
        int maxRank = k2;
        boolean findMaxima = true;
        concensusHistory.set( 0f );

//        HashSet< Integer > activeCells = initialActiveCells.indicesMoreThan( 0f );
        HashSet< Integer > activeCells = new HashSet< Integer >();
        activeCells.addAll( initialActiveCells );

        // since we are using Oja's rule the association metric is unbounded.
        float maxSupport = inhibitionWeights.max();

        // can either inhibit missing associations, or promote extra evidence.
        for( int step = 0; step < maxConcensusSteps; ++step ) {

            // calculate max support at this time:
            for( int c2 = 0; c2 < cells; ++c2 ) {
                float sumSupport = 0;

                for( Integer c1 : activeCells ) {

                    if( c1.equals( c2 ) ) {
                        continue; // no self support
                    }
                    // how much does c1 inhibit c2?
                    // the inverse of the association between them
                    // use ojas rule to train inhibition?
                    int inhibitionOffset = getInhibitionOffset( c1, c2, cells );
                    float association = inhibitionWeights._values[ inhibitionOffset ];
//                    association *= reciprocalMax; // now 1 if maximally associated globally
//                    float inhibition = 1f - association; // ie if assoc = 1, ie always together, then inh = 0
                    // if always orthogonal, then assoc = 0. inh = 1;
                    sumSupport += association;
                }

                maxSupport = Math.max( sumSupport, maxSupport );
            }

            float reciprocalMax = 0f;
            if( maxSupport > 0f ) {
                reciprocalMax = 1f / maxSupport; // max = 3, 1/3 = 0.3     val * 0.3 = val /3
            }

            // calculate inhibition for each cell
            for( int c2 = 0; c2 < cells; ++c2 ) {

// inactive cells don't inhibit
//                for( int c1 = 0; c1 < cells; ++c1 ) {
//                }
                float sumSupport = 0;

                for( Integer c1 : activeCells ) {

                    if( c1.equals( c2 ) ) {
                        continue; // no self support
                    }

                    // how much does c1 support c2?
                    int inhibitionOffset = getInhibitionOffset( c1, c2, cells );
                    float association = inhibitionWeights._values[ inhibitionOffset ];
                    association *= reciprocalMax; // now 1 if maximally associated globally
//                    float inhibition = 1f - association; // ie if assoc = 1, ie always together, then inh = 0
                    // if always orthogonal, then assoc = 0. inh = 1;
                    sumSupport += association;
                }

                int nbrActiveCells = activeCells.size();
                if( nbrActiveCells > 0 ) {
                    sumSupport /= ( float ) nbrActiveCells;
                }
                int inhibitionOffset = step * cells + c2;
                inhibitionHistory._values[ inhibitionOffset ] = sumSupport; // a unit value
            }

            TreeMap< Float, ArrayList< Integer > > rankingWithInhibition = new TreeMap< Float, ArrayList< Integer > >(); // for output

            for( int c = 0; c < cells; ++c ) {
        /*            float sum = 0.f;
                for( int i = 0; i < inputs; ++i ) {
                    int offset = c * inputs +i;
                    float input = _inputValues._values[ i ];
                    float weight = _cellWeights._values[ offset ];
                    float product = input * weight;
                    sum += product;
                }
                float bias = _cellBiases1._values[ c ];
                sum += bias;*/

                int inhibitionOffset = step * cells + c;
                float support = inhibitionHistory._values[ inhibitionOffset ]; // a unit value

                // now inhibit the weighted sums? or the nonlinearity?
//                float sum = cellWeightedSums._values[ c ]; // a unit value, at most
//
//                float promotion = 1f + (support * consensusStrength ); // so basically a unit value * some strength factor e.g. 1.1
//                float promoted = sum * promotion;
//                float transfer = (float) TransferFunction.logisticSigmoid( promoted );
                float transfer = cellActivation._values[ c ]; // a unit value
                float promotion = 1f + (support * consensusStrength ); // so basically a unit value * some strength factor e.g. 1.1
                float promoted = transfer * promotion;
                Ranking.add( rankingWithInhibition, promoted, c ); // this is the new output
            }

            ArrayList< Integer > nextActiveCells = Ranking.getBestValues( rankingWithInhibition, findMaxima, maxRank );

            // record which cells were active at each step of the search
            for( Integer c : nextActiveCells ) {
                int offset = step * cells + c;
                concensusHistory._values[ offset ] = 1;
            }

            // if concensus reached, stop.
            // but, since it won't change anymore, we dont need to implement this
            // it will cost a lot of unnecessary work tho

            activeCells.clear();
            activeCells.addAll( nextActiveCells );
        } // steps

        return activeCells;
    }

    public void update( boolean learn ) {

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
        float ageFactor = 0.5f; // halve the age each time it fires
        float noiseMagnitude = 0.001f; // small magnitude
        float learningRate = _c.getLearningRate();
        updatePromotion();
        int k = updateSparsity();
        float sparsityOutput = _c.getSparsityOutput(); // alpha
//        boolean binaryOutput = false;//_c.getBinaryOutput();
        int k2 = (int)( (float)k * sparsityOutput );

        int inputs = _c.getNbrInputs();
        int cells = _c.getNbrCells();

        // Transpose the weights
        int rows = cells; // major
        int cols = inputs; // minor
        int rowsT = inputs; // major
        int colsT = cells; // minor
//        FloatArray weightsT = FloatMatrix.transpose( _cellWeights, rows, cols );

        // add a small amount of noise to the inputs
        for( int i = 0; i < inputs; ++i ) {
            float noise = _c._r.nextFloat() * noiseMagnitude;
            _inputValues._values[ i ] = Math.max( noise, _inputValues._values[ i ] );
        }

        // Hidden layer (forward pass)
        _cellActivity.set( 0.f );

        TreeMap< Float, ArrayList< Integer > > rankingSansPromotionForOutput = new TreeMap< Float, ArrayList< Integer > >(); // for output
        TreeMap< Float, ArrayList< Integer > > rankingWithPromotionForTraining = new TreeMap< Float, ArrayList< Integer > >(); // for learning

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

            float transfer = (float) TransferFunction.logisticSigmoid( sum );

            _cellTransfer._values[ c ] = transfer;

            // without promotion
//            Ranking.add( ranking, sum, c );
            Ranking.add( rankingSansPromotionForOutput, transfer, c ); // this is the new output

            // add promotion
            float promotion = _cellPromotion._values[ c ];
            float transferPromoted = transfer * promotion;

            Ranking.add( rankingWithPromotionForTraining, transferPromoted, c ); // this is the new output
        }

        // Hidden Layer Nonlinearity: Make all except top k cells zero.
        int maxRank = k2;
        boolean findMaxima = true;
        //Ranking.truncate( ranking, maxRank, findMaxima ); whats the point

//        findActiveHiddenCellsRank( rankingSansPromotionForOutput, findMaxima, maxRank );
        maxRank = k; // just to see if this shakes things up a bit
        ArrayList< Integer > activeCellsSansPromotion = Ranking.getBestValues( rankingSansPromotionForOutput, findMaxima, maxRank );

        // now restrict to just k. This set is used for learning.
        // Hence, we now swap to the "promoted" scores
        maxRank = k;

        ArrayList< Integer > activeCells = Ranking.getBestValues( rankingWithPromotionForTraining, findMaxima, maxRank );

        // Produce the concensus output:
        HashSet< Integer > consensusCells = findConsensus( _c, activeCellsSansPromotion, _cellWeightedSum, _cellTransfer, _concensusHistory, _inhibitionHistory, _cellInhibitionWeights );

        _activeCells.clear();
        _activeCells.addAll( consensusCells );

        for( Integer c : _activeCells ) {
            _cellActivity._values[ c ] = 1.f;
        }

        // Train the concensus system:
        // Don't use consensus cells, so there's no runaway feedback effect
        // We use promoted values for this to pre-train them
        trainInhibition( _c, activeCells, _cellInhibitionWeights );


        _cellTransferTopK.set( 0f );

        // NOTE: Use the *non* promoted transfer value for forward and backward passes
        for( Integer c : activeCells ) {
            float transfer = _cellTransfer._values[ c ]; // otherwise zero
            _cellTransferTopK._values[ c ] = transfer;
        }

        // NOTE: Update ages with the *non* promoted ranking, to require a "natural" win indicating the weights have learned to be useful to zero the age
        // NOTE: I tried the above, but it just got fixated. Seems like you have to learn it once, then remove the promotion.
        updateAges( activeCells, ageFactor );
        //updateAges( _activeCells );

        // Output layer (forward pass)
        // dont really need to do this if not learning.
        for( int i = 0; i < inputs; ++i ) {
            float sum = 0.f;

            for( int c = 0; c < cells; ++c ) {

                float response = _cellTransferTopK._values[ c ];

                int offset = c * inputs +i;
                float weight = _cellWeights._values[ offset ];
                float product = response * weight;
                sum += product;
            }

            float bias = _cellBiases2._values[ i ];

            sum += bias;

            _inputReconstructionWeightedSum._values[ i ] = sum;

            float transfer = (float)TransferFunction.logisticSigmoid( sum );

            _inputReconstruction._values[ i ] = transfer;
        }

        // don't go any further unless learning is enabled
        if( !learn ) {
            return;
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
        float totalError = 0.f;

        for( int i = 0; i < inputs; ++i ) {
            float target = _inputValues._values[ i ]; // y
            float output = _inputReconstruction._values[ i ]; // a
            float error = output - target; // == d^L

            float weightedSum = _inputReconstructionWeightedSum._values[ i ]; // z
            float derivative = (float)TransferFunction.logisticSigmoidDerivative( weightedSum );

            dOutput._values[ i ] = error * derivative; // eqn 30

            totalError += ( error * error ); // fyi only
        }

        System.err.println( "Total error: " + totalError );

        // compute gradient in hidden units. Derivative is either 1 or 0 depending whether the cell was filtered.
        for( int c = 0; c < cells; ++c ) { // computing error for each "input"
            float sum = 0.f;

//            float response = cellResponseTruncated._values[ c ];
            float transferTopK = _cellTransferTopK._values[ c ];
            float weightedSum = _cellWeightedSum._values[ c ];
            float derivative = (float)TransferFunction.logisticSigmoidDerivative( weightedSum );

            if( transferTopK > 0f ) {
                for( int i = 0; i < inputs; ++i ) {
                    //int offset = j * K + k; // K = inputs, storage is all inputs adjacent
                    int offset = c * inputs + i;
                    float w = _cellWeights._values[ offset ];
                    float d = dOutput._values[ i ]; // d_j i.e. partial derivative of loss fn with respect to the activation of j
                    float product = d * w;// + ( l2R * w );

                    Useful.IsBad( product );

                    sum += product;
                }

                // with linear neurons, derivative is 1, but here it is nonlinear now
                sum *= derivative;  // eqn (BP2)
            }
            // else: derivative is zero when filtered

            dHidden._values[ c ] = sum;
        }

        // now gradient descent in the hidden->output layer
        for( int i = 0; i < inputs; ++i ) {

            float errorGradient = dOutput._values[ i ];

            for( int c = 0; c < cells; ++c ) { // computing error for each "input"

                int offset = c * inputs + i;

                float a = _cellTransferTopK._values[ c ];
                float wOld = _cellWeights._values[ offset ];
                float wDelta = learningRate * errorGradient * a;

                // Normal
                float wNew = wOld - wDelta;

                Useful.IsBad( wNew );

                _cellWeights._values[ offset ] = wNew;
            }

            float bOld = _cellBiases2._values[ i ];
            float bDelta = learningRate * errorGradient;
            float bNew = bOld - bDelta;

            _cellBiases2._values[ i ] = bNew;
        }

        // now gradient descent in the input->hidden layer
        for( int c = 0; c < cells; ++c ) { // computing error for each "input"

            float errorGradient = dHidden._values[ c ];

            for( int i = 0; i < inputs; ++i ) {

                int offset = c * inputs + i;

                float a = _inputValues._values[ i ];
                float wOld = _cellWeights._values[ offset ];
                float wDelta = learningRate * errorGradient * a;

                // Normal
                float wNew = wOld - wDelta;

                Useful.IsBad( wNew );

                _cellWeights._values[ offset ] = wNew;
            }

            float bOld = _cellBiases1._values[ c ];
            float bDelta = learningRate * errorGradient;
            float bNew = bOld - bDelta;

            _cellBiases1._values[ c ] = bNew;
        }

    }

    protected void findActiveHiddenCellsOtsu() {

        // Rank based:
        int precision = 100; // 0.01 intervals of sigmoid activation
//        Otsu.apply( _cellTransfer, _cellActivity, precision, 0f, 1f );
        FloatArray weightedSum = new FloatArray( _cellWeightedSum );
        weightedSum.scaleRange( 0f, 1f );
        Otsu.apply( weightedSum, _cellActivity, precision, 0f, 1f );

        _activeCells = new ArrayList< Integer >( _cellActivity.indicesMoreThan( 0f ) );
    }

    protected void findActiveHiddenCellsRank( TreeMap< Float, ArrayList< Integer > > ranking, boolean findMaxima, int maxRank ) {
        _activeCells = Ranking.getBestValues( ranking, findMaxima, maxRank );

        for( Integer c : _activeCells ) {
            _cellActivity._values[ c ] = 1.f;
        }
    }
}
