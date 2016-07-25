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

import io.agi.core.ann.supervised.BackPropagation;
import io.agi.core.ann.supervised.TransferFunction;
import io.agi.core.data.*;
import io.agi.core.math.Useful;
import io.agi.core.orm.ObjectMap;

import java.util.ArrayList;
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
public class KSparseAutoencoder extends CompetitiveLearning {

    public KSparseAutoencoderConfig _c;
    public ArrayList< Integer > _sparseUnitInput;
    public Data _inputValues;
    public Data _inputReconstruction;
    public Data _cellWeights;
    public Data _cellBiases1;
    public Data _cellBiases2;
    public Data _cellErrors;
    public Data _cellWeightedSum;
    public Data _cellTransfer;
    public Data _cellActivity;

    protected boolean _ageLearn = true;

    protected ArrayList< Integer > _activeCells = new ArrayList< Integer >();

    public KSparseAutoencoder( String name, ObjectMap om ) {
        super( name, om );
    }

    public void setup( KSparseAutoencoderConfig c ) {
        _c = c;

        int inputs = c.getNbrInputs();
        int w = c.getWidthCells();
        int h = c.getHeightCells();

        _inputValues = new Data( inputs );
        _inputReconstruction = new Data( inputs );
        _cellWeights = new Data( w, h, inputs );
        _cellBiases1 = new Data( w, h );
        _cellBiases2 = new Data( inputs );
        _cellErrors = new Data( w, h );
        _cellWeightedSum = new Data( w, h );
        _cellTransfer = new Data( w, h );
        _cellActivity = new Data( w, h );
    }

    public void reset() {

        // Better initialization of the weights:
        // http://neuralnetworksanddeeplearning.com/chap3.html
        // init weights to SD = 1/ sqrt( n ) where n is number of inputs.
        int inputs = _inputValues.getSize(); //875.f;
        float sqRtInputs = (float)Math.sqrt( (float)inputs );
        for( int i = 0; i < _cellWeights.getSize(); ++i ) {
            double r = _c._r.nextGaussian(); // mean: 0, SD: 1
            _cellWeights._values[ i ] = (float)r / sqRtInputs;
        }

        for( int i = 0; i < _cellBiases1.getSize(); ++i ) {
            double r = _c._r.nextGaussian(); // mean: 0, SD: 1
            _cellBiases1._values[ i ] = (float)r;
        }

        for( int i = 0; i < _cellBiases2.getSize(); ++i ) {
            double r = _c._r.nextGaussian(); // mean: 0, SD: 1
            _cellBiases2._values[ i ] = (float)r;
        }

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

        _c.setAge( 0 );
    }

    public void call() {
        update();
    }

    public int updateSparsity() {
        int kMin = _c.getSparsityMin();
        int kMax = _c.getSparsityMax();
        int ageMin = _c.getAgeMin();
        int ageMax = _c.getAgeMax();
        int age = _c.getAge();

        _ageLearn = true;

        int k = 0;
        if( age < ageMin ) {
            k = kMax;
            _ageLearn = false;
        }
        else if( k > ageMax ){
            k = kMin;
        }
        else {
            int age2 = age - ageMin;
            int ageRange = ageMax - ageMin;
            float relativeAge = (float)age2 / (float)ageRange;
            relativeAge = Math.min( 1.f, relativeAge );
            relativeAge = 1.f - relativeAge;
            int kRange = kMax - kMin;
            int kRel = (int)( (float)kRange * relativeAge );
            k = kMin + kRel;
        }

        //System.err.println( "Age: " + age + " Sparsity: " + k );
        _c.setSparsity( k );
        _c.setAge( age +1 );

        return k;
    }

    public void update() {
        // don't go any further unless learning is enabled
        boolean learn = _c.getLearn();
        learn = learn & _ageLearn;
        update( learn );
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
        float learningRate = _c.getLearningRate();
        int k = updateSparsity();
        float sparsityOutput = _c.getSparsityOutput(); // alpha
        boolean binaryOutput = false;//_c.getBinaryOutput();
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
            float noise = this._c._r.nextFloat() * 0.001f; // small magnitude
            _inputValues._values[ i ] = Math.max( noise, _inputValues._values[ i ] );
        }

        // Hidden layer (forward pass)
        _cellActivity.set( 0.f );

        TreeMap< Float, ArrayList< Integer > > ranking = new TreeMap< Float, ArrayList< Integer > >();

        for( int c = 0; c < cells; ++c ) {
            float sum = 0.f;

            for( int i = 0; i < inputs; ++i ) {

//                int row = c;
//                int col = i;
//                int offset = FloatMatrix.getOffset( rows, cols, row, col );
                int offset = c * inputs +i;

                float input = _inputValues._values[ i ];
                float weight = _cellWeights._values[ offset ];
                float product = input * weight;
                sum += product;
            }

            float bias = _cellBiases1._values[ c ];

            sum += bias;

            _cellResponse._values[ c ] = sum;

            Ranking.add( ranking, sum, c );
        }

        // Hidden Layer Nonlinearity: Make all except top k cells zero.
        int maxRank = k2;
        boolean findMaxima = true;
        //Ranking.truncate( ranking, maxRank, findMaxima );

        _activeCells = Ranking.getBestValues( ranking, findMaxima, maxRank );

        for( Integer c : _activeCells ) {
            _cellActivity._values[ c ] = 1.f;
        }

        // now restrict to just k:
        maxRank = k;

        FloatArray cellResponseTruncated = new FloatArray( cells );

        ArrayList< Integer > activeCells = Ranking.getBestValues( ranking, findMaxima, maxRank );

        for( Integer c : activeCells ) {
            float activeValue = 1.f;
//            if( !binaryOutput ) {
                activeValue = _cellResponse._values[ c ]; // otherwise zero
//            }
            cellResponseTruncated._values[ c ] = activeValue;
        }

        // Output layer (forward pass)
        // dont really need to do this if not learning.
        for( int i = 0; i < inputs; ++i ) {
            float sum = 0.f;

            for( int c = 0; c < cells; ++c ) {

//                float active = _cellActivity._values[ c ];
                float response = cellResponseTruncated._values[ c ];
//                float responseActive = response * active; // i.e. 0 if not in k support set

//                int rowT = i;
//                int colT = c;
//                int offset = FloatMatrix.getOffset( rowsT, colsT, rowT, colT );
                int offset = c * inputs +i;

                float weight = _cellWeights._values[ offset ];
//                float weight = weightsT._values[ offset ];
                float product = response * weight;
                sum += product;
            }

            float bias = _cellBiases2._values[ i ];

            sum += bias;

            _inputReconstruction._values[ i ] = sum;
        }

        // TODO seems like I should test for a classification change here, to prevent overlearning a constant sample

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
        FloatArray dHidden = new FloatArray( cells );

        // d output layer
        float totalError = 0.f;

        for( int i = 0; i < inputs; ++i ) {
            float target = _inputValues._values[ i ]; // y
            float output = _inputReconstruction._values[ i ]; // a
            float error = output - target; // == d^L

            dOutput._values[ i ] = error;

            totalError += ( error * error );
        }

        System.err.println( "Total error: " + totalError );

        // compute gradient in hidden units. Derivative is either 1 or 0 depending whether the cell was filtered.
        for( int c = 0; c < cells; ++c ) { // computing error for each "input"
            float sum = 0.f;

            float response = cellResponseTruncated._values[ c ];

            if( response > 0f ) {
                for( int i = 0; i < inputs; ++i ) {
                    //int offset = j * K + k; // K = inputs, storage is all inputs adjacent
                    int offset = c * inputs + i;
                    float w = _cellWeights._values[ offset ];
                    float d = dOutput._values[ i ]; // d_j i.e. partial derivative of loss fn with respect to the activation of j
                    float product = d * w;// + ( l2R * w );

                    Useful.IsBad( product );

                    sum += product;
                }
            }

            dHidden._values[ c ] = sum;
        }

        // now gradient descent in the hidden->output layer
        for( int i = 0; i < inputs; ++i ) {

            float errorGradient = dOutput._values[ i ];

            for( int c = 0; c < cells; ++c ) { // computing error for each "input"

                int offset = c * inputs + i;

                float a = cellResponseTruncated._values[ c ];
                float wOld = _cellWeights._values[ offset ];
                float wDelta = learningRate * errorGradient * a;
                float wNew = wOld - wDelta;

                float wMax = 1.f;
                if( wNew > wMax ) wNew = wMax;
                if( wNew < -wMax ) wNew = -wMax;

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
                float wNew = wOld - wDelta;

                float wMax = 1.f;
                if( wNew > wMax ) wNew = wMax;
                if( wNew < -wMax ) wNew = -wMax;

                Useful.IsBad( wNew );

                _cellWeights._values[ offset ] = wNew;
            }

            float bOld = _cellBiases1._values[ c ];
            float bDelta = learningRate * errorGradient;
            float bNew = bOld - bDelta;

            _cellBiases1._values[ c ] = bNew;
        }

//        - investigate k sparse weights view : they look good in reconstruction - due to vaRYING MAGS
//        - add data stats view
//            - read paper re why so many idle (epochs)
//        - why dont they get closer eg learning of static active cells
//
//competitive - when promoted, promote until naturally wins, then zero the promotion.
//nonlinear - saturates at 1. so the nodes are flat.
//looks like need sigmoid

//        FloatArray iHidden = _inputValues;
////        FloatArray iOutput = _cellResponse;//cellResponseTruncated; // input reconstructed as output
//        FloatArray iOutput = cellResponseTruncated; // input reconstructed as output
//
//        FloatArray bHidden = _cellBiases1;
//        FloatArray bOutput = _cellBiases2;
//
//        FloatArray wHidden = _cellWeights;
////        FloatArray wOutput = weightsT;
//
//        // normally z is the weighted sum + bias, but in this case the transfer function is linear so it is the same as the post-transfer (output) value
////        FloatArray zHidden = _cellResponse;//cellResponseTruncated; // i.e. these are mostly zeros
//        FloatArray zHidden = cellResponseTruncated; // i.e. these are mostly zeros
//        FloatArray zOutput = _inputReconstruction;
//
//        FloatArray mOutput = new FloatArray( inputs );
//        mOutput.set( 1.f );
//
//        FloatArray mHidden = new FloatArray( cells );
//        mHidden.set( 0.f );
//        for( Integer c : _activeCells ) {
//            mHidden._values[ c ] = 1.f;
//        }
//
//        TransferFunction f = TransferFunction.createLinear();

        // Compute the delta terms for the hidden layer
//        float l2R = 0.f;
//        BackPropagation.ErrorGradient( zHidden, dHidden, wOutput, dOutput, f, l2R );

        // Mask out the gradients for the inactive cells.
//        for( int c = 0; c < cells; ++c ) {
//            if( activeCells.contains( c ) ) {
//                continue;
//            }
//
//            dHidden._values[ c ] = 0.f;
//        }

        // This will back propagate to any inputs that were nonzero, because inputs to the cell layer that are zero won't be adjusted
//        BackPropagation.StochasticGradientDescent( mOutput, dOutput, wOutput, bOutput, iOutput, learningRate );

        // Since we've modified the weights, we need to transpose them and copy before we can modify them again
//        FloatArray weightsUnT = FloatMatrix.transpose( wOutput, rowsT, colsT );
//        wHidden.copy( weightsUnT );

        // Don't need to calculate the error gradient for the input layer, as it has no weights
        //BackPropagation.ErrorGradient( zInput, dInput, wHidden, dHidden, f, l2R ); // not required

//        BackPropagation.StochasticGradientDescent( mHidden, dHidden, wHidden, bHidden, iHidden, learningRate );
    }

}
