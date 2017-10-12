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

import io.agi.core.ann.supervised.BackPropagation;
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
public class BiasedSparseAutoencoder extends LifetimeSparseAutoencoder {

    public Data _inputLearningRate;
    public Data _inputValuesOld;
    public Data _batchLearningRates;

    public BiasedSparseAutoencoder( String name, ObjectMap om ) {
        super( name, om );
    }

    public void setup( LifetimeSparseAutoencoderConfig c ) {
        super.setup( c );
        int batchSize = c.getBatchSize();
        _inputLearningRate = new Data( 1 );
        _inputValuesOld = new Data( _inputValues._dataSize );
        _batchLearningRates = new Data( batchSize );
    }

    public void reset() {
        super.reset();
        _inputLearningRate.set( 1f );
        _inputValuesOld.set( 0f );
        _batchLearningRates.set( 0f );
    }

    public void update( boolean learn ) {
        encode();

        // Output layer (forward pass)
        // dont really need to do this if not learning.
        decode( _c, _cellWeights, _cellBiases2, _cellSpikes, _inputReconstruction ); // for output

        // don't go any further unless learning is enabled
        if( !learn ) {
            return;
        }

        // Result of encoding:
        int sparsity = _c.getSparsity();
        Data cellWeightedSumOld = new Data( _cellWeightedSum._dataSize );
        Data cellSpikesOld = new Data( _cellSpikes._dataSize );
        encode( _inputValuesOld, _cellWeights, _cellBiases1, cellWeightedSumOld, cellSpikesOld, sparsity );

        Data inputLearningRate = _inputLearningRate; // new value of input learning rate
        Data hiddenLayerInput = _inputValuesOld; // old values of input i.e. delayed feedback
        Data hiddenLayerWeightedSum = cellWeightedSumOld;
        Data outputLayerInput = cellSpikesOld;

        updateBatch( inputLearningRate, hiddenLayerInput, hiddenLayerWeightedSum, outputLayerInput );

        _inputValuesOld.copy( _inputValues ); // i.e. ready for next time
    }

    /**
     * Update the batch given a new matched set of inputs.
     * @param inputLearningRate
     * @param hiddenLayerInput
     * @param hiddenLayerWeightedSum
     * @param outputLayerInput
     */
    protected void updateBatch(
            Data inputLearningRate,
            Data hiddenLayerInput,
            Data hiddenLayerWeightedSum,
            Data outputLayerInput  ) {

        // Batch data
        Data hiddenLayerWeightedSumBatch   = _batchHiddenWeightedSum;
        Data hiddenLayerInputBatch         = _batchHiddenInput;
        Data hiddenLayerErrorBatch         = _batchHiddenErrors;
        Data outputLayerInputBatch         = _batchOutputInput;
        Data outputLayerInputBatchLifetime = _batchOutputInputLifetime;
        Data outputLayerErrorBatch         = _batchOutputErrors;
        Data outputLayerOutputBatch        = _batchOutputOutput;

        int batchSize = _c.getBatchSize();
        int batchCount = _c.getBatchCount();

        _batchLearningRates._values[ batchCount ] = inputLearningRate._values[ 0 ]; // new feedback associated with previous input values

        batchAccumulate(
                _c,
                hiddenLayerInput,
                hiddenLayerWeightedSum,
                outputLayerInput,
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

        // NEW - bias the error gradients
        int outputLayerSize = _c.getNbrInputs();
        int hiddenLayerSize = _c.getNbrCells();

        // output layer
        biasBatchErrors(
            batchSize,
            outputLayerSize,
            outputLayerErrorBatch,
            _batchLearningRates );

        // hidden layer
        biasBatchErrors(
            batchSize,
            hiddenLayerSize,
            hiddenLayerErrorBatch,
            _batchLearningRates );
        // NEW - bias the error gradients

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
        _batchLearningRates.set( 0f );
        hiddenLayerInputBatch.set( 0f );
        hiddenLayerErrorBatch.set( 0f );
        hiddenLayerWeightedSumBatch.set( 0f );

        outputLayerInputBatch.set( 0f );
        outputLayerInputBatchLifetime.set( 0f );
        outputLayerErrorBatch.set( 0f );
        outputLayerOutputBatch.set( 0f );
    }

//    Data outputLayerOutputBatch = _batchOutputOutput;

/*    public static void batchTrain(
            LifetimeSparseAutoencoderConfig config,
            Data cellWeights,
            Data cellWeightsVelocity,
            Data cellBiases1,
            Data cellBiases2,
            Data cellBiases1Velocity,
            Data cellBiases2Velocity,
            Data learningRatesBatch,
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

        StochasticGradientDescentWithVariableLearningRate(
                inputSize, layerSize, batchSize, learningRate, momentum, weightsInputMajor,
                learningRatesBatch, outputLayerInputBatch, outputLayerErrorBatch,
                cellWeights, cellWeightsVelocity, cellBiases2, cellBiases2Velocity );

        // now gradient descent in the input->hidden layer. can't skip this because we need to update the biases
        inputSize = inputs;
        layerSize = cells;
        weightsInputMajor = false;

        StochasticGradientDescentWithVariableLearningRate(
                inputSize, layerSize, batchSize, learningRate, momentum, weightsInputMajor,
                learningRatesBatch, hiddenLayerInputBatch, hiddenLayerErrorBatch,
                cellWeights, cellWeightsVelocity, cellBiases1, cellBiases1Velocity );

//        System.err.println( "Age: " + this._c.getAge() + " Sparsity: " + k  + " vMax = " + vMax );
    }*/

    protected static void biasBatchErrors(
            int batchSize,
            int layerSize,
            Data batchErrors,
            Data batchLearningRates ) {
        for( int c = 0; c < layerSize; ++c ) { // computing error for each "input"
            for( int b = 0; b < batchSize; ++b ) {
                int errorOffset = b * layerSize + c;
                float oldErrorGradient = batchErrors._values[ errorOffset ];// * a;
                float batchLearningRate = batchLearningRates._values[ b ];
                float newErrorGradient = oldErrorGradient * batchLearningRate; // this is equivalent to multiplying the learningRate by the batch learning rate.
                batchErrors._values[ errorOffset ] = newErrorGradient;
            }
        }
    }

/*    public static void StochasticGradientDescentWithVariableLearningRate(
            int inputSize,
            int layerSize,
            int batchSize,
            float learningRate,
            float momentum,
            boolean weightsInputMajor,
            Data batchLearningRates,
            Data batchInput,
            Data batchErrors,
            Data weights,
            Data weightsVelocity,
            Data biases,
            Data biasesVelocity ) {
        boolean useMomentum = false;
        if( momentum != 0f ) {
            useMomentum = true;
        }

        float miniBatchNorm = 1f / (float)batchSize;

        for( int c = 0; c < layerSize; ++c ) { // computing error for each "input"

            for( int i = 0; i < inputSize; ++i ) {

                // foreach( batch sample )
                float sumErrorGradient = 0f;

                for( int b = 0; b < batchSize; ++b ) {

                    // tied weights
//                    int weightsOffset = c * inputSize + i;
//                    if( weightsInputMajor ) {
//                        weightsOffset = i * layerSize + c;
//                    }
//
                    int inputOffset = b * inputSize + i;
                    int errorOffset = b * layerSize + c;

                    float a = batchInput._values[ inputOffset ];

                    //float errorGradient = _cellGradients._values[ c ];
                    float errorGradient = batchErrors._values[ errorOffset ] * a;

                    float batchLearningRate = batchLearningRates._values[ b ];
                    errorGradient *= batchLearningRate; // this is equivalent to multiplying the learningRate by the batch learning rate.

                    sumErrorGradient += errorGradient;
                }

                float errorGradient = miniBatchNorm * sumErrorGradient;
                errorGradient = BackPropagation.ClipErrorGradient( errorGradient, BackPropagation.AbsMaxErrorGradient );

//                for( int b = 0; b < batchSize; ++b ) {

                int weightsOffset = c * inputSize + i;
                if( weightsInputMajor ) {
                    weightsOffset = i * layerSize + c;
                }

                //float a = _inputValues._values[ i ];
                float wOld = weights._values[ weightsOffset ];
                float wDelta = learningRate * errorGradient;// * a;

                if( useMomentum ) {
                    // Momentum
                    float wNew = wOld - wDelta;

                    if( Useful.IsBad( wNew ) ) {
                        String error = "Autoencoder weight update produced a bad value: " + wNew;
                        logger.error( error );
                        logger.traceExit();
                        System.exit( -1 );
                    }

                    weights._values[ weightsOffset ] = wNew;
                } else {
                    // Momentum
                    float vOld = weightsVelocity._values[ weightsOffset ];
                    float vNew = ( vOld * momentum ) - wDelta;
                    float wNew = wOld + vNew;

                    if( Useful.IsBad( wNew ) ) {
                        String error = "Autoencoder weight update produced a bad value: " + wNew;
                        logger.error( error );
                        logger.traceExit();
                        System.exit( -1 );
                    }

                    weights._values[ weightsOffset ] = wNew;
                    weightsVelocity._values[ weightsOffset ] = vNew;
                } // momentum
//                } // batch
            } // inputs

            float sumErrorGradient = 0f;

            for( int b = 0; b < batchSize; ++b ) {
                int errorOffset = b * layerSize + c;
                float errorGradient = batchErrors._values[ errorOffset ];
 combine with batch learning rate
                sumErrorGradient += errorGradient;
            }

            float errorGradient = miniBatchNorm * sumErrorGradient;
            errorGradient = BackPropagation.ClipErrorGradient( errorGradient, BackPropagation.AbsMaxErrorGradient );

            float bOld = biases._values[ c ];
            float bDelta = learningRate * errorGradient;

            if( useMomentum ) {
                float vOld = biasesVelocity._values[ c ];
                float vNew = ( vOld * momentum ) - bDelta;
                float bNew = bOld + vNew;

                biases._values[ c ] = bNew;
                biasesVelocity._values[ c ] = vNew;
            } else {
                float bNew = bOld - bDelta;

                biases._values[ c ] = bNew;
            }
        }
    }*/

}
