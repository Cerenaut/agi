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
import io.agi.core.data.Ranking;
import io.agi.core.math.Useful;
import io.agi.core.orm.ObjectMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeMap;

/**
 * A variation on "Winner-Take-All Autoencoders" by Alireza Makhzani, Brendan Frey. Their system is a convolutional
 * autoencoder with ReLU and enforced spatial and lifetime sparsity.
 *
 * The changes are that this doesn't have the separate deconvolution process, so is more similar to the K-Sparse auto
 * encoder. However, they warn that this may cause the convolutional use of this to produce lots of compressed copies
 * of the input, and then decompress with deconvolution.
 *
 * Created by dave on 1/07/16.
 */
public class WinnerTakeAllAutoencoder extends CompetitiveLearning {

    protected static final Logger logger = LogManager.getLogger();

    public WinnerTakeAllAutoencoderConfig _c;
    public Data _inputValues;
    public Data _inputReconstruction;
    public Data _cellWeights;
    public Data _cellBiases1;
    public Data _cellBiases2;
    public Data _cellWeightsVelocity;
    public Data _cellBiases1Velocity;
    public Data _cellBiases2Velocity;
    public Data _cellErrors;
    public Data _cellWeightedSum;
    public Data _cellSpikes;

//    public Data _outputErrors; // was inputGradients
//    public Data _hiddenErrors; // was cellGradients

    public Data _batchOutputOutput;
    public Data _batchOutputInput;
    public Data _batchOutputInputLifetime;
    public Data _batchOutputErrors;
    public Data _batchHiddenInput;
    public Data _batchHiddenWeightedSum;
    public Data _batchHiddenErrors;

    public WinnerTakeAllAutoencoder( String name, ObjectMap om ) {
        super( name, om );
    }

    public void setInput( Data input ) {
        _inputValues.copy( input );
    }

    public Data getInput() {
        return _inputValues;
    }

    public void setup( WinnerTakeAllAutoencoderConfig c ) {
        _c = c;

        int batchSize = c.getBatchSize();
        int inputs = c.getNbrInputs();
        int w = c.getWidthCells();
        int h = c.getHeightCells();
        int cells = w * h;

        _inputValues = new Data( inputs );
        _inputReconstruction = new Data( inputs );
        _cellWeights = new Data( w, h, inputs );
        _cellBiases1 = new Data( w, h );
        _cellBiases2 = new Data( inputs );
        _cellWeightsVelocity = new Data( w, h, inputs );
        _cellBiases1Velocity = new Data( w, h );
        _cellBiases2Velocity = new Data( inputs );
        _cellErrors = new Data( w, h );
        _cellWeightedSum = new Data( w, h );
        _cellSpikes = new Data( w, h );

//        _outputErrors = new Data( inputs );
//        _hiddenErrors = new Data( w, h );

        _batchOutputOutput = new Data( inputs, batchSize );
        _batchOutputInput = new Data(  cells, batchSize );
        _batchOutputInputLifetime = new Data(  cells, batchSize );
        _batchOutputErrors = new Data( inputs, batchSize );
        _batchHiddenInput = new Data( inputs, batchSize );
        _batchHiddenWeightedSum = new Data( cells, batchSize );
        _batchHiddenErrors = new Data(  cells, batchSize );
    }

    public void reset() {

        _c.setBatchCount( 0 );
//        _outputErrors.set( 0f );
//        _hiddenErrors.set( 0f );

        _batchOutputInput.set( 0f );
        _batchOutputErrors.set( 0f );
        _batchHiddenInput.set( 0f );
        _batchHiddenErrors.set( 0f );

        _cellWeightsVelocity.set( 0f );
        _cellBiases1Velocity.set( 0f );
        _cellBiases2Velocity.set( 0f );

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

    public void update() {
        boolean learn = _c.getLearn();
        update( learn );
    }

    public void encode() {
        // Hidden layer (forward pass)
        TreeMap< Float, ArrayList< Integer > > ranking = new TreeMap< Float, ArrayList< Integer > >();

        int inputs = _c.getNbrInputs();
        int cells = _c.getNbrCells();
        int sparsity = _c.getSparsity();

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
        boolean findMaxima = true;
        int maxRank = sparsity;
        ArrayList< Integer > activeCells = Ranking.getBestValues( ranking, findMaxima, maxRank );
        _cellSpikes.set( 0.f );
        for( Integer c : activeCells ) {
            float transfer = _cellWeightedSum._values[ c ]; // otherwise zero
            _cellSpikes._values[ c ] = transfer;
        }

    }

    public void update( boolean learn ) {
        encode();

        // Output layer (forward pass)
        // dont really need to do this if not learning.
        reconstruct( _c, _cellWeights, _cellBiases2, _cellSpikes, _inputReconstruction ); // for output

        // don't go any further unless learning is enabled
        if( !learn ) {
            return;
        }

        int batchSize = _c.getBatchSize();
        int batchCount = _c.getBatchCount();

        Data hiddenLayerInput = _inputValues;
        Data hiddenLayerWeightedSum = _cellWeightedSum;
        Data hiddenLayerOutput = _cellSpikes;

        Data hiddenLayerWeightedSumBatch = _batchHiddenWeightedSum;
        Data hiddenLayerInputBatch = _batchHiddenInput;
        Data hiddenLayerErrorBatch = _batchHiddenErrors;
        Data outputLayerInputBatch = _batchOutputInput;
        Data outputLayerInputBatchLifetime = _batchOutputInputLifetime;
        Data outputLayerErrorBatch = _batchOutputErrors;
        Data outputLayerOutputBatch = _batchOutputOutput;

        batchAccumulate(
                _c,
                hiddenLayerInput,
                hiddenLayerWeightedSum,
                hiddenLayerOutput,
//                outputLayerOutput,
                hiddenLayerInputBatch,
                hiddenLayerWeightedSumBatch,
                outputLayerInputBatch,
//                outputLayerOutputBatch,
                batchCount );

        // decide whether to learn or accumulate more gradients first (mini batch)
        batchCount += 1;

        if( batchCount < batchSize ) { // e.g. if was zero, then becomes 1, then we clear it and apply the gradients
            _c.setBatchCount( batchCount );
            return; // end update
        }

        // add the winning cells from each column PLUS the
        batchSelectHiddenCells(
            _c,
            _cellWeights,
            _cellBiases2,
            hiddenLayerWeightedSumBatch, // raw unfiltered output of hidden layer cells
            outputLayerInputBatch, // original winning cells
            outputLayerInputBatchLifetime, // calculated: original winning cells AND lifetime sparsity winning cells
            outputLayerOutputBatch ); //

        batchBackpropagateError(
                _c,
                _cellWeights,
                hiddenLayerInputBatch,
                hiddenLayerErrorBatch, // calculated
                outputLayerInputBatchLifetime,
                outputLayerErrorBatch, // calculated
                outputLayerOutputBatch );

        batchTrain(
                _c,
                _cellWeights,
                _cellWeightsVelocity,
                _cellBiases1,
                _cellBiases2,
                _cellBiases1Velocity,
                _cellBiases2Velocity,
                hiddenLayerInputBatch,
                hiddenLayerErrorBatch,
                outputLayerInputBatch,
                outputLayerErrorBatch );

        _c.setBatchCount( 0 );

        // Clear the accumulated gradients
        hiddenLayerInputBatch.set( 0f );
        hiddenLayerErrorBatch.set( 0f );
        hiddenLayerWeightedSumBatch.set( 0f );

        outputLayerInputBatch.set( 0f );
        outputLayerInputBatchLifetime.set( 0f );
        outputLayerErrorBatch.set( 0f );
        outputLayerOutputBatch.set( 0f );
    }

    Data outputLayerOutputBatch = _batchOutputOutput;

    public static void batchTrain(
            WinnerTakeAllAutoencoderConfig config,
            Data cellWeights,
            Data cellWeightsVelocity,
            Data cellBiases1,
            Data cellBiases2,
            Data cellBiases1Velocity,
            Data cellBiases2Velocity,
            Data hiddenLayerInputBatch,
            Data hiddenLayerErrorBatch,
            Data outputLayerInputBatch,
            Data outputLayerErrorBatch ) {

        float learningRate = config.getLearningRate();
        float momentum = config.getMomentum();
        int inputs = config.getNbrInputs();
        int cells = config.getNbrCells();
        int batchSize = config.getBatchSize();

        // now gradient descent in the hidden->output layer
        int inputSize = cells;
        int layerSize = inputs;
        boolean weightsInputMajor = true;

        KSparseAutoencoder.StochasticGradientDescent(
                inputSize, layerSize, batchSize, learningRate, momentum, weightsInputMajor,
                outputLayerInputBatch, outputLayerErrorBatch,
                cellWeights, cellWeightsVelocity, cellBiases2, cellBiases2Velocity );

        // now gradient descent in the input->hidden layer. can't skip this because we need to update the biases
        inputSize = inputs;
        layerSize = cells;
        weightsInputMajor = false;

        KSparseAutoencoder.StochasticGradientDescent(
                inputSize, layerSize, batchSize, learningRate, momentum, weightsInputMajor,
                hiddenLayerInputBatch, hiddenLayerErrorBatch,
                cellWeights, cellWeightsVelocity, cellBiases1, cellBiases1Velocity );

//        System.err.println( "Age: " + this._c.getAge() + " Sparsity: " + k  + " vMax = " + vMax );
    }

    public static void batchAccumulate(
            WinnerTakeAllAutoencoderConfig config,
            Data hiddenLayerInput,
            Data hiddenLayerWeightedSum,
            Data outputLayerInput,
//            Data outputLayerOutput,

            Data hiddenLayerInputBatch,
            Data hiddenLayerWeightedSumBatch,
            Data outputLayerInputBatch,
//            Data outputLayerOutputBatch,

            int batchIndex ) {

        int inputs = config.getNbrInputs();
        int cells = config.getNbrCells();

        // accumulate the error gradients and inputs over the batch
        int b = batchIndex;

//        for( int i = 0; i < inputs; ++i ) {
//            float r = outputLayerOutput._values[ i ];
//            int batchOffset = b * inputs + i;
//            outputLayerOutputBatch._values[ batchOffset ] = r;
//        }

//        for( int i = 0; i < inputs; ++i ) {
//            float dNew = outputLayerError._values[ i ];
//            int batchOffset = b * inputs + i;
//            outputLayerErrorBatch._values[ batchOffset ] = dNew;
//        }
//
//        for( int i = 0; i < cells; ++i ) {
//            float dNew = hiddenLayerError._values[ i ];
//            int batchOffset = b * cells + i;
//            hiddenLayerErrorBatch._values[ batchOffset ] = dNew;
//        }

        for( int i = 0; i < cells; ++i ) {
            float r = outputLayerInput._values[ i ];
            int batchOffset = b * cells + i;
            outputLayerInputBatch._values[ batchOffset ] = r;
        }

        for( int i = 0; i < inputs; ++i ) {
            float r = hiddenLayerInput._values[ i ];
            int batchOffset = b * inputs + i;
            hiddenLayerInputBatch._values[ batchOffset ] = r;
        }

        for( int i = 0; i < cells; ++i ) {
            float r = hiddenLayerWeightedSum._values[ i ];
            int batchOffset = b * cells + i;
            hiddenLayerWeightedSumBatch._values[ batchOffset ] = r;
        }
    }

    public static void batchBackpropagateError(
            WinnerTakeAllAutoencoderConfig config,
            Data cellWeights,
            Data hiddenLayerInputBatch,
            Data hiddenLayerErrorBatch, // calculated
            Data outputLayerInputBatch,
            Data outputLayerErrorBatch, // calculated
            Data outputLayerOutputBatch ) {

        int inputs = config.getNbrInputs();
        int cells = config.getNbrCells();
        int batchSize = config.getBatchSize();

        for( int b = 0; b < batchSize; ++b ) {

            // OUTPUT LAYER
            // d output layer
            for( int i = 0; i < inputs; ++i ) {
                int batchOffset = b * inputs + i;
                float target = hiddenLayerInputBatch._values[ batchOffset ]; // y
                float output = outputLayerOutputBatch._values[ batchOffset ]; // a
                float error = output - target; // == d^L
                //float weightedSum = output; // z
                float derivative = 1f;//(float)TransferFunction.logisticSigmoidDerivative( weightedSum );
                outputLayerErrorBatch._values[ batchOffset ] = error * derivative; // eqn 30
            }

            // HIDDEN LAYER
            // compute gradient in hidden units. Derivative is either 1 or 0 depending whether the cell was filtered.
            for( int c = 0; c < cells; ++c ) { // computing error for each "input"
                float sum = 0.f;
                int batchOffsetCell = b * cells + c;

                float transferTopK = outputLayerInputBatch._values[ batchOffsetCell ];
                float derivative = 1f;//(float)TransferFunction.logisticSigmoidDerivative( weightedSum );

                if( transferTopK > 0f ) { // if was cell active
                    for( int i = 0; i < inputs; ++i ) {
                        //int offset = j * K + k; // K = inputs, storage is all inputs adjacent
                        int offset = c * inputs + i;
                        float w = cellWeights._values[ offset ];
                        //float d = dOutput._values[ i ]; // d_j i.e. partial derivative of loss fn with respect to the activation of j
                        int batchOffsetInput = b * inputs + i;
                        float d = outputLayerErrorBatch._values[ batchOffsetInput ]; // d_j i.e. partial derivative of loss fn with respect to the activation of j
                        float product = d * w;// + ( l2R * w );

                        // TODO add gradient clipping
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

                //dHidden._values[ c ] = sum;
                hiddenLayerErrorBatch._values[ batchOffsetCell ] = sum;
            } // cells
        } // batch index
    }

    public static void batchSelectHiddenCells(
            WinnerTakeAllAutoencoderConfig config,
            Data cellWeights,
            Data cellBiases2,
            Data hiddenLayerActivityBatch,
            Data hiddenLayerSpikesBatch,
            Data outputLayerInputBatch, // calculated
            Data outputLayerOutputBatch ) { // calculated

        // filter all except top-k activations for
        int inputs = config.getNbrInputs();
        int cells = config.getNbrCells();
        int batchSize = config.getBatchSize();
        int sparsityLifetime = config.getSparsity(); // same?

        outputLayerInputBatch.set( 0f );

        // add all the spikes we found based on winner-take-all
        for( int b = 0; b < batchSize; ++b ) {
            for( int i = 0; i < cells; ++i ) {
                int batchOffset = b * cells + i;
                float r = hiddenLayerSpikesBatch._values[ batchOffset ];
                if( r > 0f ) {
                    outputLayerInputBatch._values[ batchOffset ] = 1f;
                }
            }
        }

        // accumulate the error gradients and inputs over the batch
        for( int i = 0; i < cells; ++i ) {

            // find the top k
            TreeMap< Float, ArrayList< Integer > > ranking = new TreeMap< Float, ArrayList< Integer > >();

            for( int b = 0; b < batchSize; ++b ) {
                int batchOffset = b * cells + i;
                float r = hiddenLayerActivityBatch._values[ batchOffset ];
                Ranking.add( ranking, r, b );
            }

            // rank the batch responses for this hidden unit
            HashSet< Integer > bestBatchIndices = new HashSet< Integer >();
            int maxRank = sparsityLifetime;
            boolean findMaxima = true; // biggest activity
            Ranking.getBestValuesRandomTieBreak( ranking, findMaxima, maxRank, bestBatchIndices, config._r );

            // Set hidden activation to zero for all other batch indices, and 1 for the best
            for( int b = 0; b < batchSize; ++b ) {
                float activity = 0f;
                if( bestBatchIndices.contains( b ) ) {
                    activity = 1f;
                }

                int batchOffset = b * cells + i;
                outputLayerInputBatch._values[ batchOffset ] = activity;
            }
        }

        // Now calculate the output based on this new pattern of hidden layer activity
        Data outputLayerInput = new Data( cells );
        Data outputLayerOutput = new Data( inputs );

        for( int b = 0; b < batchSize; ++b ) {
            int offsetThis = 0;
            int offsetThat = b * cells;
            outputLayerInput.copyRange( outputLayerInputBatch, offsetThis, offsetThat, cells );
            reconstruct( config, cellWeights, cellBiases2, outputLayerInput, outputLayerOutput ); // for output
            offsetThis = b * inputs;
            offsetThat = 0;
            outputLayerOutputBatch.copyRange( outputLayerOutput, offsetThis, offsetThat, inputs );
        }

    }

//    public static void StochasticGradientDescent(
//            int inputSize,
//            int layerSize,
//            int batchSize,
//            float learningRate,
//            float momentum,
//            boolean weightsInputMajor,
//            Data batchInput,
//            Data batchErrors,
//            Data weights,
//            Data weightsVelocity,
//            Data biases,
//            Data biasesVelocity ) {
//
//        boolean useMomentum = false;
//        if( momentum != 0f ) {
//            useMomentum = true;
//        }
//
//        float miniBatchNorm = 1f / (float)batchSize;
//
//        for( int c = 0; c < layerSize; ++c ) { // computing error for each "input"
//
//            for( int i = 0; i < inputSize; ++i ) {
//
//                // foreach( batch sample )
//                for( int b = 0; b < batchSize; ++b ) {
//
//                    // tied weights
//                    int weightsOffset = c * inputSize + i;
//                    if( weightsInputMajor ) {
//                        weightsOffset = i * layerSize + c;
//                    }
//
//                    int inputOffset = b * inputSize + i;
//                    int errorOffset = b * layerSize + c;
//
//                    //float errorGradient = _cellGradients._values[ c ];
//                    float errorGradient = batchErrors._values[ errorOffset ];
//
//                    //float a = _inputValues._values[ i ];
//                    float a = batchInput._values[ inputOffset ];
//                    float wOld = weights._values[ weightsOffset ];
//                    float wDelta = learningRate * miniBatchNorm * errorGradient * a;
//
//                    if( useMomentum ) {
//                        // Momentum
//                        float wNew = wOld - wDelta;
//
//                        if( Useful.IsBad( wNew ) ) {
//                            String error = "Autoencoder weight update produced a bad value: " + wNew;
//                            logger.error( error );
//                            logger.traceExit();
//                            System.exit( -1 );
//                        }
//
//                        weights._values[ weightsOffset ] = wNew;
//                    } else {
//                        // Momentum
//                        float vOld = weightsVelocity._values[ weightsOffset ];
//                        float vNew = ( vOld * momentum ) - wDelta;
//                        float wNew = wOld + vNew;
//
//                        if( Useful.IsBad( wNew ) ) {
//                            String error = "Autoencoder weight update produced a bad value: " + wNew;
//                            logger.error( error );
//                            logger.traceExit();
//                            System.exit( -1 );
//                        }
//
//                        weights._values[ weightsOffset ] = wNew;
//                        weightsVelocity._values[ weightsOffset ] = vNew;
//                    } // momentum
//                } // batch
//            } // inputs
//
//            for( int b = 0; b < batchSize; ++b ) {
//                int errorOffset = b * layerSize + c;
//                float errorGradient = batchErrors._values[ errorOffset ];
//
//                float bOld = biases._values[ c ];
//                float bDelta = learningRate * miniBatchNorm * errorGradient;
//
//                if( useMomentum ) {
//                    float vOld = biasesVelocity._values[ c ];
//                    float vNew = ( vOld * momentum ) - bDelta;
//                    float bNew = bOld + vNew;
//
//                    biases._values[ c ] = bNew;
//                    biasesVelocity._values[ c ] = vNew;
//                } else {
//                    float bNew = bOld - bDelta;
//
//                    biases._values[ c ] = bNew;
//                }
//            }
//        }
//    }

    public void reconstruct(
            Data hiddenActivity,
            Data inputReconstruction ) {
        reconstruct( _c, _cellWeights, _cellBiases2, hiddenActivity, inputReconstruction );
    }

    public static void reconstruct(
            WinnerTakeAllAutoencoderConfig config,
            Data cellWeights,
            Data cellBiases2,
            Data hiddenActivity,
            Data inputReconstruction ) {
        int inputs = config.getNbrInputs();
        int cells = config.getNbrCells();

        for( int i = 0; i < inputs; ++i ) {
            float sum = 0.f;

            for( int c = 0; c < cells; ++c ) {

                float response = hiddenActivity._values[ c ];// _cellTransferTopK._values[ c ];

                int offset = c * inputs +i;
                float weight = cellWeights._values[ offset ];
                float product = response * weight;
                sum += product;
            }

            float bias = cellBiases2._values[ i ];

            sum += bias;

            inputReconstruction._values[ i ] = sum;
        }

    }
}
