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
 * Derived from the LifetimeSparseAutoencoder but now using a sparse coding approximation to perform arbitrary functions.
 * Created by dave on 16/10/17.
 */
public class BatchSparseNetwork extends CompetitiveLearning {

    protected static final Logger logger = LogManager.getLogger();

    public BatchSparseNetworkConfig _c;

    public Data _testingInputValues; // input
    public Data _trainingInputValues; // input
    public Data _trainingOutputValues; // input

    public Data _testingOutputValues; // output

    public Data _cellWeights1;
    public Data _cellWeights2;
    public Data _cellBiases1;
    public Data _cellBiases2;
    public Data _cellWeights1Velocity;
    public Data _cellWeights2Velocity;
    public Data _cellBiases1Velocity;
    public Data _cellBiases2Velocity;
//    public Data _cellErrors;
    public Data _testingHiddenWeightedSum;
    public Data _testingHiddenSpikes;

    public Data _batchOutputIdeal; // new
    public Data _batchOutputOutput;
    public Data _batchOutputInput;
    public Data _batchOutputInputLifetime;
    public Data _batchOutputErrors;
    public Data _batchHiddenInput;
    public Data _batchHiddenWeightedSum;
    public Data _batchHiddenErrors;

    public BatchSparseNetwork( String name, ObjectMap om ) {
        super( name, om );
    }

    public void setInput( Data testingInput, Data trainingInput, Data trainingOutput ) {
        _testingInputValues.copy( testingInput );
        _trainingInputValues.copy( trainingInput );
        _trainingOutputValues.copy( trainingOutput );
    }

    public void setup( BatchSparseNetworkConfig c ) {
        _c = c;

        int batchSize = c.getBatchSize();
        int inputs = c.getNbrInputs();
        int outputs = c.getOutputs();
        int w = c.getWidthCells();
        int h = c.getHeightCells();
        int cells = w * h;

        _testingInputValues = new Data( inputs );
        _trainingInputValues = new Data( inputs );
        _trainingOutputValues = new Data( outputs );
        _testingOutputValues = new Data( outputs );

        _cellWeights1 = new Data( inputs, cells );
        _cellWeights2 = new Data( cells, outputs );
        _cellBiases1 = new Data( w, h ); // 1 bias per cell
//        _cellBiases2 = new Data( inputs );
        _cellBiases2 = new Data( outputs ); // 1 bias per output
        _cellWeights1Velocity = new Data( inputs, cells );
        _cellWeights2Velocity = new Data( cells, outputs );
        _cellBiases1Velocity = new Data( w, h );
//        _cellBiases2Velocity = new Data( inputs );
        _cellBiases2Velocity = new Data( outputs );
//        _cellErrors = new Data( w, h );
        _testingHiddenWeightedSum = new Data( w, h );
        _testingHiddenSpikes = new Data( w, h );

        _batchOutputIdeal = new Data( outputs, batchSize );
//        _batchOutputOutput = new Data( inputs, batchSize );
        _batchOutputOutput = new Data( outputs, batchSize );
        _batchOutputInput = new Data( cells, batchSize );
        _batchOutputInputLifetime = new Data( cells, batchSize );
//        _batchOutputErrors = new Data( inputs, batchSize );
        _batchOutputErrors = new Data( outputs, batchSize );
        _batchHiddenInput = new Data( inputs, batchSize );
        _batchHiddenWeightedSum = new Data( cells, batchSize );
        _batchHiddenErrors = new Data( cells, batchSize );
    }

    public void reset() {

        _c.setBatchCount( 0 );
//        _outputErrors.set( 0f );
//        _hiddenErrors.set( 0f );

        _batchOutputInput.set( 0f );
        _batchOutputErrors.set( 0f );
        _batchHiddenInput.set( 0f );
        _batchHiddenErrors.set( 0f );

        _cellWeights1Velocity.set( 0f );
        _cellWeights2Velocity.set( 0f );
        _cellBiases1Velocity.set( 0f );
        _cellBiases2Velocity.set( 0f );

        float weightsStdDev = _c.getWeightsStdDev();

        for( int i = 0; i < _cellWeights1.getSize(); ++i ) {
            double r = _c._r.nextGaussian(); // mean: 0, SD: 1
            r *= weightsStdDev;
            _cellWeights1._values[ i ] = ( float ) r;// / sqRtInputs;
        }

        for( int i = 0; i < _cellWeights2.getSize(); ++i ) {
            double r = _c._r.nextGaussian(); // mean: 0, SD: 1
            r *= weightsStdDev;
            _cellWeights2._values[ i ] = ( float ) r;// / sqRtInputs;
        }

        for( int i = 0; i < _cellBiases1.getSize(); ++i ) {
            double r = _c._r.nextGaussian(); // mean: 0, SD: 1
            r *= weightsStdDev;
            _cellBiases1._values[ i ] = ( float ) r;
        }

        for( int i = 0; i < _cellBiases2.getSize(); ++i ) {
            double r = _c._r.nextGaussian(); // mean: 0, SD: 1
            r *= weightsStdDev;
            _cellBiases2._values[ i ] = ( float ) r;
        }
    }

    public static void feedForwardLayer(
            int nbrLayerInputs,
            int nbrLayerOutputs,
            Data cellWeights,
            Data cellBiases,
            Data layerInputs,
            Data output,
            TreeMap< Float, ArrayList< Integer > > ranking ) {

        for( int i = 0; i < nbrLayerOutputs; ++i ) {
            float sum = 0.f;

            for( int j = 0; j < nbrLayerInputs; ++j ) {

                float layerInput = layerInputs._values[ j ];

                int offset = i * nbrLayerInputs +j;
                float weight = cellWeights._values[ offset ];
                float product = layerInput * weight;
                sum += product;
            }

            float bias = cellBiases._values[ i ];

            sum += bias;

            output._values[ i ] = sum;

            if( ranking != null ) {
                Ranking.add( ranking, sum, i );
            }
        }

    }

    public static void feedForward(
            Data inputValues, // input
            Data cellWeights1,
            Data cellWeights2,
            Data cellBiases1,
            Data cellBiases2,
            Data cellWeightedSum, // hidden
            Data cellSpikes, // hidden
            Data outputValues, // output
            int sparsity ) {
        // Hidden layer (forward pass)
        TreeMap< Float, ArrayList< Integer > > ranking = new TreeMap< Float, ArrayList< Integer > >();

        int inputs = inputValues.getSize();
        int outputs = outputValues.getSize();
        int cells = cellSpikes.getSize();

        feedForwardLayer( inputs, cells, cellWeights1, cellBiases1, inputValues, cellWeightedSum, ranking );

        // Hidden Layer Nonlinearity: Make all except top k cells zero.
        boolean findMaxima = true;
        int maxRank = sparsity;
        ArrayList< Integer > activeCells = Ranking.getBestValues( ranking, findMaxima, maxRank );
        cellSpikes.set( 0.f );
        for( Integer c : activeCells ) {
            float transfer = cellWeightedSum._values[ c ]; // otherwise zero
            cellSpikes._values[ c ] = transfer;
        }

        // now produce an output
        feedForwardLayer( cells, outputs, cellWeights2, cellBiases2, cellSpikes, outputValues, null );
    }

    public void update() {

        // TODO consider increased hidden sparsity for hidden output?
        boolean learn = _c.getLearn();
        int sparsity = _c.getSparsity();

        feedForward(
            _testingInputValues, // input layer
            _cellWeights1,
            _cellWeights2,
            _cellBiases1,
            _cellBiases2,
            _testingHiddenWeightedSum,
            _testingHiddenSpikes, // hidden layer
            _testingOutputValues, // output layer
            sparsity );

        // don't go any further unless learning is enabled
        if( !learn ) {
            return;
        }

        // Training sample
        Data hiddenLayerInput       = _trainingInputValues;
        Data hiddenLayerWeightedSum = new Data( _testingHiddenWeightedSum._dataSize );
        Data outputLayerInput       = new Data( _testingHiddenSpikes._dataSize ); // == hiddenLayerSpikes
        Data outputLayerOutput      = new Data( _testingOutputValues._dataSize );
        Data outputLayerIdeal       = _trainingOutputValues;

        feedForward(
                hiddenLayerInput,
                _cellWeights1,
                _cellWeights2,
                _cellBiases1,
                _cellBiases2,
                hiddenLayerWeightedSum, //_testingHiddenWeightedSum,
                outputLayerInput,//_testingHiddenSpikes,
                outputLayerOutput, // not actually used?
                sparsity );

        int batchSize = _c.getBatchSize();
        int batchCount = _c.getBatchCount();

        Data hiddenLayerWeightedSumBatch = _batchHiddenWeightedSum;
        Data hiddenLayerInputBatch = _batchHiddenInput;
        Data hiddenLayerErrorBatch = _batchHiddenErrors;
        Data outputLayerInputBatch = _batchOutputInput;
        Data outputLayerInputBatchLifetime = _batchOutputInputLifetime;
        Data outputLayerErrorBatch = _batchOutputErrors;
        Data outputLayerOutputBatch = _batchOutputOutput;
        Data outputLayerIdealBatch = _batchOutputIdeal;

        batchAccumulate(
                _c,

                hiddenLayerInput,
                hiddenLayerWeightedSum,
                outputLayerInput,
                outputLayerIdeal,

                hiddenLayerInputBatch,
                hiddenLayerWeightedSumBatch,
                outputLayerInputBatch,
                outputLayerIdealBatch,

                batchCount );

        // decide whether to learn or accumulate more gradients first (mini batch)
        batchCount += 1;

        if( batchCount < batchSize ) { // e.g. if was zero, then becomes 1, then we clear it and apply the gradients
            _c.setBatchCount( batchCount );
            return; // end update
        }

        // add the winning cells from each column PLUS the
        batchSelectHiddenCells( // OK
                _c,
                _cellWeights2,
                _cellBiases2,
                hiddenLayerWeightedSumBatch, // raw unfiltered output of hidden layer cells
                outputLayerInputBatch, // original winning cells
                outputLayerInputBatchLifetime, // calculated: original winning cells AND lifetime sparsity winning cells
                outputLayerOutputBatch ); // calculated: output given superset of winning cells

        batchBackpropagateError( // OK
                _c,
                _cellWeights2,
                outputLayerIdealBatch,
                hiddenLayerErrorBatch, // calculated
                outputLayerInputBatchLifetime,
                outputLayerErrorBatch, // calculated
                outputLayerOutputBatch );

        batchTrain( // OK
                _c,
                _cellWeights1,
                _cellWeights2,
                _cellWeights1Velocity,
                _cellWeights2Velocity,
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

//    Data outputLayerOutputBatch = _batchOutputOutput;

    public static void batchTrain(
            BatchSparseNetworkConfig config,
            Data cellWeights1,
            Data cellWeights2,
            Data cellWeights1Velocity,
            Data cellWeights2Velocity,
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
        int outputs = config.getOutputs();
        int cells = config.getNbrCells();
        int batchSize = config.getBatchSize();

        // now gradient descent in the hidden->output layer
        int inputSize = cells;
        int layerSize = outputs;//inputs;
        boolean weightsInputMajor = false;//true; no longer tied weights

        KSparseAutoencoder.StochasticGradientDescent(
                inputSize, layerSize, batchSize, learningRate, momentum, weightsInputMajor,
                outputLayerInputBatch, outputLayerErrorBatch,
                cellWeights2, cellWeights2Velocity, cellBiases2, cellBiases2Velocity );

        // now gradient descent in the input->hidden layer. can't skip this because we need to update the biases
        inputSize = inputs;
        layerSize = cells;
        weightsInputMajor = false;

        KSparseAutoencoder.StochasticGradientDescent(
                inputSize, layerSize, batchSize, learningRate, momentum, weightsInputMajor,
                hiddenLayerInputBatch, hiddenLayerErrorBatch,
                cellWeights1, cellWeights1Velocity, cellBiases1, cellBiases1Velocity );

//        System.err.println( "Age: " + this._c.getAge() + " Sparsity: " + k  + " vMax = " + vMax );
    }

    public static void batchAccumulate(
            BatchSparseNetworkConfig config,

            Data hiddenLayerInput,
            Data hiddenLayerWeightedSum,
            Data outputLayerInput,
            Data outputLayerIdeal,

            Data hiddenLayerInputBatch,
            Data hiddenLayerWeightedSumBatch,
            Data outputLayerInputBatch,
            Data outputLayerIdealBatch,

            int batchIndex ) {

        int inputs = config.getNbrInputs();
        int outputs = config.getOutputs();
        int cells = config.getNbrCells();

        // accumulate the error gradients and inputs over the batch
        int b = batchIndex;

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

        for( int i = 0; i < cells; ++i ) {
            float r = outputLayerInput._values[ i ];
            int batchOffset = b * cells + i;
            outputLayerInputBatch._values[ batchOffset ] = r;
        }

        for( int i = 0; i < outputs; ++i ) {
            float r = outputLayerIdeal._values[ i ];
            int batchOffset = b * outputs + i;
            outputLayerIdealBatch._values[ batchOffset ] = r;
        }

    }

    public static void batchBackpropagateError(
            BatchSparseNetworkConfig config,
//            Data cellWeights1,
            Data cellWeights2,
//            Data hiddenLayerInputBatch,
            Data outputLayerIdealBatch,
            Data hiddenLayerErrorBatch, // calculated
            Data outputLayerInputBatch,
            Data outputLayerErrorBatch, // calculated
            Data outputLayerOutputBatch ) {

//        int inputs = config.getNbrInputs();
        int outputs = config.getOutputs();
        int cells = config.getNbrCells();
        int batchSize = config.getBatchSize();

        for( int b = 0; b < batchSize; ++b ) {

            // OUTPUT LAYER
            // d output layer
            for( int i = 0; i < outputs; ++i ) {
                int batchOffset = b * outputs + i;
                float target = outputLayerIdealBatch ._values[ batchOffset ]; // y
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

                if( transferTopK > 0f ) { // if was cell active, ie input nonzero
                    for( int i = 0; i < outputs; ++i ) {
                        //int offset = j * K + k; // K = inputs, storage is all inputs adjacent
                        int offset = i * cells + c;// was: c * inputs + i; ? But now the weight from c --> i in output layer
                        float w = cellWeights2._values[ offset ];
                        //float d = dOutput._values[ i ]; // d_j i.e. partial derivative of loss fn with respect to the activation of j
                        int batchOffsetInput = b * outputs + i;
                        float d = outputLayerErrorBatch._values[ batchOffsetInput ]; // d_j i.e. partial derivative of loss fn with respect to the activation of j
                        float product = d * w;// + ( l2R * w );
                        product = BackPropagation.ClipErrorGradient( product, 10.f );

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
//        System.err.println( "Batch gradient E range : " + minValE + " / " + maxValE + " W range: " + minValW + " / " + maxValW + " D range: " + minValD + " / " + maxValD );
    }

    public static void batchSelectHiddenCells(
            BatchSparseNetworkConfig config,
            Data cellWeights2,
            Data cellBiases2,
            Data hiddenLayerActivityBatch, // pre-binarization of winners ie weighted sums
            Data hiddenLayerSpikesBatch, // original winning cells
            Data outputLayerInputBatch, // calculated
            Data outputLayerOutputBatch ) { // calculated

        // filter all except top-k activations for
        //int inputs = config.getNbrInputs();
        int outputs = config.getOutputs();
        int cells = config.getNbrCells();
        int batchSize = config.getBatchSize();
        int sparsityLifetime = config.getSparsityLifetime(); // different, because related to batch size

        outputLayerInputBatch.set( 0f );

        // add all the spikes we found based on winner-take-all
        for( int b = 0; b < batchSize; ++b ) {
            for( int i = 0; i < cells; ++i ) {
                int batchOffset = b * cells + i;
                float r = hiddenLayerSpikesBatch._values[ batchOffset ];
                if( r > 0f ) {
                    float transfer = hiddenLayerActivityBatch._values[ batchOffset ];
//                    outputLayerInputBatch._values[ batchOffset ] = 1f;
                    outputLayerInputBatch._values[ batchOffset ] = transfer;
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
                int batchOffset = b * cells + i;
                float oldActivity = outputLayerInputBatch._values[ batchOffset ];
                float newActivity = oldActivity;
                if( bestBatchIndices.contains( b ) ) {
                    float transfer = hiddenLayerActivityBatch._values[ batchOffset ];
//                    newActivity = 1f;
                    newActivity = transfer;
                }

                // should get the old value here.. ie the winner PLUS the lifetime sparsity bits
                outputLayerInputBatch._values[ batchOffset ] = newActivity;
            }
        }

        // Now calculate the output based on this new pattern of hidden layer activity
        Data outputLayerInput = new Data( cells );
        Data outputLayerOutput = new Data( outputs );

        for( int b = 0; b < batchSize; ++b ) {
            int offsetThis = 0;
            int offsetThat = b * cells;
            outputLayerInput.copyRange( outputLayerInputBatch, offsetThis, offsetThat, cells );
            //decode( config, cellWeights, cellBiases2, outputLayerInput, outputLayerOutput ); // for output
            feedForwardLayer( cells, outputs, cellWeights2, cellBiases2, outputLayerInput, outputLayerOutput, null );
            offsetThis = b * outputs;//b * inputs;
            offsetThat = 0;
            outputLayerOutputBatch.copyRange( outputLayerOutput, offsetThis, offsetThat, outputs );
        }

    }

}
