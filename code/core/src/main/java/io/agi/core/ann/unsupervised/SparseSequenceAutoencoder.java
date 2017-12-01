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
import io.agi.core.data.DataSize;
import io.agi.core.data.Ranking;
import io.agi.core.orm.ObjectMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * Created by dave on 10/11/17.
 */
public class SparseSequenceAutoencoder extends CompetitiveLearning {

    protected static final Logger logger = LogManager.getLogger();

    public SparseSequenceAutoencoderConfig _c;

    public LifetimeSparseAutoencoder _encoderF;
    public LifetimeSparseAutoencoder _encoderB;

    public Data _cellMappings;
    public Data _cellMaskB;
    public Data _cellMaskBatch;
    public Data _output;
    public Data _ageSinceSpikeB;
    public Data _ageSinceSpikeF;

    public float _encodingErrorF = 0f;
    public float _encodingErrorB = 0f;

    public int _uniqueRows = 0;

    public SparseSequenceAutoencoder( String name, ObjectMap om ) {
        super( name, om );
    }

    public void setInputF( Data input ) {
        _encoderF._inputValues = new Data( input );
    }
    public void setInputB( Data input ) {
        _encoderB._inputValues = new Data( input );
//        _inputValuesB = new Data( input );
    }

    public Data getInputF() {
        return _encoderF._inputValues;
    }

    public Data getInputB() {
        return _encoderB._inputValues;
//        return _inputValuesB;
    }

    public void setup( SparseSequenceAutoencoderConfig c ) {
        _c = c;

        LifetimeSparseAutoencoderConfig cf = new LifetimeSparseAutoencoderConfig();
        LifetimeSparseAutoencoderConfig cb = new LifetimeSparseAutoencoderConfig();

        String nameF = _name + "-f";
        String nameB = _name + "-b";

        cf.setup(
            _c._om, nameF, _c._r,
            _c.getNbrInputs(),
            _c.getWidthCellsF(), _c.getHeightCellsF(),
            _c.getLearningRate(), _c.getMomentum(),
            _c.getSparsityTrainingF(), _c.getSparsityBatchF(), _c.getSparsityOutputF(),
            _c.getWeightsStdDev(),
            _c.getBatchCountF(), _c.getBatchSizeF() );

//        int batchCountB = 0;
//        int batchSizeB = 1;
//        float learningRateB = _c.getLearningRate() * 1f;
//        int sparsityBatchB = 0; // aka sparsity lifetime
        int batchCountB = _c.getBatchCountB();
        int batchSizeB = _c.getBatchSizeB();
        float learningRateB = _c.getLearningRate();// * 2f;
        int sparsityBatchB = _c.getSparsityBatchB(); // aka sparsity lifetime
        int widthCellsB = _c.getWidthCellsB();// * 2; // PARAMETER ***********************

        cb.setup(
                _c._om, nameB, _c._r,
                _c.getInputsB(),
                widthCellsB, _c.getHeightCellsB(),
                learningRateB, _c.getMomentum(),
//                _c.getSparsityTrainingB(), _c.getSparsityBatchB(), _c.getSparsityOutputB(),
                _c.getSparsityTrainingB(), sparsityBatchB, _c.getSparsityOutputB(),
                _c.getWeightsStdDev(),
                batchCountB, batchSizeB );

        _encoderF = new LifetimeSparseAutoencoder( nameF, _om );
        _encoderB = new LifetimeSparseAutoencoder( nameB, _om );

        _encoderF.setup( cf );
        _encoderB.setup( cb );

        _ageSinceSpikeF = new Data( cf.getWidthCells(), cf.getHeightCells() );
        _ageSinceSpikeB = new Data( cb.getWidthCells(), cb.getHeightCells() );

        int cellsF = cf.getNbrCells();
        int cellsB = cb.getNbrCells();

        _cellMappings = new Data( cellsB, cellsF );
        _cellMaskB = new Data( _ageSinceSpikeB._dataSize );
        _cellMaskBatch = new Data( _cellMaskB.getSize(), batchSizeB );
        _output = new Data( _ageSinceSpikeB._dataSize );
    }

    public void reset() {
        _cellMaskBatch.set( 0f );
        _ageSinceSpikeB.set( 0f );
        _ageSinceSpikeF.set( 0f );
        _encoderF.reset();
        _encoderB.reset();

        // generate a random fixed mapping between cell populations F and B.
        int cellsF = _encoderF._c.getNbrCells();
        int cellsB = _encoderB._c.getNbrCells();

        int cellsFPerCellB = _c.getCellMappingDensity();//10; // ********************* PARAMETER

        _cellMappings.set( 0f );

//        for( int cellB = 0; cellB < cellsB; ++cellB ) {
        for( int cellF = 0; cellF < cellsF; ++cellF ) {

            // Each cellF has X cellsB (guaranteed)
            HashSet< Integer > mappedCellsB = new HashSet< Integer >();

            while( mappedCellsB.size() < cellsFPerCellB ) {
                int cellB = _encoderF._c._r.nextInt( cellsB );
                mappedCellsB.add( cellB );
            }

            for( Integer cellB : mappedCellsB ) {
                int offset = cellB * cellsF + cellF;
                _cellMappings._values[ offset ] = 1f;
            }
        }
    }

    public Collection< Integer > getCellsF( int cellB ) {
        int cellsF = _encoderF._c.getNbrCells();

        HashSet< Integer > mappedCellsF = new HashSet< Integer >();

        for( int cellF = 0; cellF < cellsF; ++cellF ) {
            int offset = cellB * cellsF + cellF;
            float mapping = _cellMappings._values[ offset ];
            if( mapping > 0f ) {
                mappedCellsF.add( cellF );
            }
        }

        return mappedCellsF;
    }

    public void update() {

        boolean learn = _c.getLearn();
        _encoderF._c.setLearn( learn );
        _encoderB._c.setLearn( learn );

        int sparsityF = _encoderF._c.getSparsity();
        int sparsityB = _encoderB._c.getSparsity();

        Collection< Integer > activeCellsF = _encoderF.encode(
                _encoderF._inputValues,
                _encoderF._cellWeights,
                _encoderF._cellBiases1,
                _encoderF._cellWeightedSum,
                _encoderF._cellSpikes, sparsityF );

        // build the mask of allowed cells B:
        _cellMaskB.set( 0f );
        HashMap< Integer, Collection< Integer > > mappedCells = new HashMap< Integer, Collection< Integer > >();
        HashSet< Integer > maskedCellsB = new HashSet< Integer >();

        int cellsB = _encoderB._c.getNbrCells();

        for( int cellB = 0; cellB < cellsB; ++cellB ) {

            Collection< Integer > mappedCellsF = getCellsF( cellB );

            mappedCells.put( cellB, mappedCellsF );

            for( Integer cellF : activeCellsF ) {
                if( mappedCellsF.contains( cellF ) ) {
                    maskedCellsB.add( cellB );
                    _cellMaskB._values[ cellB ] = 1f; // allowed cells
                }
            }
        }

        Collection< Integer > activeCellsB = _encoderF.encodeWithMask(
                _encoderB._inputValues,
                _cellMaskB,
                _encoderB._cellWeights,
                _encoderB._cellBiases1,
                _encoderB._cellWeightedSum,
                _encoderB._cellSpikes, sparsityB );

        // binarize the output
        _output.set( 0f );
        for( Integer cellB : activeCellsB ) {
            _output._values[ cellB ] = 1f;
        }

        // DEBUGGING
        // measure compression loss by reconstructing the input from the active cell set
        _encoderF._inputReconstruction.setSize( _encoderF._inputValues._dataSize ); // copy the size of the current input
        _encoderF.decode( _encoderF._c, _encoderF._cellWeights, _encoderF._cellBiases2, _encoderF._cellSpikes, _encoderF._inputReconstruction ); // for output

        _encoderB._inputReconstruction.setSize( _encoderB._inputValues._dataSize ); // copy the size of the current input
        _encoderB.decode( _encoderB._c, _encoderB._cellWeights, _encoderB._cellBiases2, _encoderB._cellSpikes, _encoderB._inputReconstruction ); // for output

        _encodingErrorF = compressionError( _encoderF._inputValues, _encoderF._inputReconstruction );
        _encodingErrorB = compressionError( _encoderB._inputValues, _encoderB._inputReconstruction );

        // calculate encoded prediction error:
        // And update spike ages
        _ageSinceSpikeF.add( 1f );
        int cellsF = _encoderF._c.getNbrCells();
        for( int cell = 0; cell < cellsF; ++cell ) {
            float spikeF = _encoderF._cellSpikes._values[ cell ];
            if( spikeF > 0f ) {
                _ageSinceSpikeF._values[ cell ] = 0f;
            }
        }

        _ageSinceSpikeB.add( 1f );
        for( int cell = 0; cell < cellsB; ++cell ) {
            float spikeB = _encoderB._cellSpikes._values[ cell ];
            if( spikeB > 0f ) {
                _ageSinceSpikeB._values[ cell ] = 0f;
            }
        }

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        int batchCount = _encoderB._c.getBatchCount();
        int batchSize = _encoderB._c.getBatchSize();
        if( (batchCount+1) == batchSize ) { // e.g. if was zero, then becomes 1, then we clear it and apply the gradients

            HashSet< String > uniqueRows = new HashSet< String >();
            int cells = _encoderB._c.getNbrCells();
            for( int b = 0; b < batchSize; ++b ) {
                String row = "";
                for( int cell = 0; cell < cells; ++cell ) {
                    int off = b * cells + cell;
                    float f = _encoderB._batchOutputInput._values[ off ];
                    if( f > 0f ) {
                        row += String.valueOf( cell ) + " ";
                    }
                }
                if( uniqueRows.contains( row ) ) {
//                int g = 0;
//                g++;
                } else {
                    uniqueRows.add( row ); // 9!?
                }
            }
            _uniqueRows = uniqueRows.size();
            System.err.println( "Batch has " + _uniqueRows + " unique rows in " + batchSize + " samples." );
        }
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        // TRAINING
        // don't go any further unless learning is enabled
        if( !learn ) {
            return;
        }

        _encoderF.train( _encoderF._inputValues, _encoderF._cellWeightedSum, _encoderF._cellSpikes );
//        _encoderB.train( _encoderB._inputValues, _encoderB._cellWeightedSum, _encoderB._cellSpikes );
        singleTrainWithCells( _encoderB, _encoderB._cellSpikes ); // only trains the winners (B)
        batchTrainWithMask( _encoderB, _encoderB._inputValues, _encoderB._cellWeightedSum, _encoderB._cellSpikes, _cellMaskB, _cellMaskBatch );

//        _c.setBatchCount( _encoderF._c.getBatchCount() );
    }

    protected float compressionError( Data input, Data reconstructed ) {
        float error = (float)( Math.sqrt( input.sumSqDiff( reconstructed ) ) );
        return error;
    }

    protected static void batchTrainWithMask(
            LifetimeSparseAutoencoder encoder,
            Data hiddenLayerInput,
            Data hiddenLayerWeightedSum,
            Data outputLayerInput,
            Data hiddenLayerMask,
            Data hiddenLayerMaskBatch ) {

        int batchSize = encoder._c.getBatchSize();
        int batchCount = encoder._c.getBatchCount();
        boolean trainWinners = false;

        Data hiddenLayerWeightedSumBatch = encoder._batchHiddenWeightedSum;
        Data hiddenLayerInputBatch = encoder._batchHiddenInput;
        Data hiddenLayerErrorBatch = encoder._batchHiddenErrors;
        Data outputLayerInputBatch = encoder._batchOutputInput;
        Data outputLayerInputBatchLifetime = encoder._batchOutputInputLifetime;
        Data outputLayerErrorBatch = encoder._batchOutputErrors;
        Data outputLayerOutputBatch = encoder._batchOutputOutput;

        encoder.batchAccumulate(
                encoder._c,
                hiddenLayerInput,
                hiddenLayerWeightedSum,
                outputLayerInput,
                hiddenLayerInputBatch,
                hiddenLayerWeightedSumBatch,
                outputLayerInputBatch,
                batchCount );

        batchAccumulateMasks( encoder._c, hiddenLayerMask, hiddenLayerMaskBatch, batchCount );

        // decide whether to learn or accumulate more gradients first (mini batch)
        batchCount += 1;

        if( batchCount < batchSize ) { // e.g. if was zero, then becomes 1, then we clear it and apply the gradients
            encoder._c.setBatchCount( batchCount );
            return;
        }

        encoder.batchSelectHiddenCellsWithMask(
                encoder,
                encoder._cellWeights,
                encoder._cellBiases2,
                hiddenLayerInputBatch,
                hiddenLayerMaskBatch, // mask 1 = cell is allowed to fire for given input
                hiddenLayerWeightedSumBatch, // pre-binarization of winners ie weighted sums
                outputLayerInputBatch, // original winning cells
                outputLayerInputBatchLifetime, // calculated
                outputLayerOutputBatch,
                trainWinners );

        encoder.batchBackpropagateError(
                encoder._c,
                encoder._cellWeights,
                hiddenLayerInputBatch,
                hiddenLayerErrorBatch, // calculated
                outputLayerInputBatchLifetime,
                outputLayerErrorBatch, // calculated
                outputLayerOutputBatch );

        encoder.batchTrain(
                encoder._c,
                encoder._cellWeights,
                encoder._cellWeightsVelocity,
                encoder._cellBiases1,
                encoder._cellBiases2,
                encoder._cellBiases1Velocity,
                encoder._cellBiases2Velocity,
                hiddenLayerInputBatch,
                hiddenLayerErrorBatch,
//                outputLayerInputBatch, ???? bug ????
                outputLayerInputBatchLifetime,
                outputLayerErrorBatch );

        encoder._c.setBatchCount( 0 );

        // Clear the accumulated gradients
        hiddenLayerInputBatch.set( 0f );
        hiddenLayerErrorBatch.set( 0f );
        hiddenLayerWeightedSumBatch.set( 0f );

        outputLayerInputBatch.set( 0f );
        outputLayerInputBatchLifetime.set( 0f );
        outputLayerErrorBatch.set( 0f );
        outputLayerOutputBatch.set( 0f );

        hiddenLayerMaskBatch.set( 0f );
    }

    public static void batchAccumulateMasks(
            LifetimeSparseAutoencoderConfig config,
            Data hiddenLayerMask,
            Data hiddenLayerMaskBatch,
            int batchIndex ) {

        int cells = config.getNbrCells();

        // accumulate the error gradients and inputs over the batch
        int b = batchIndex;

        for( int i = 0; i < cells; ++i ) {
            float r = hiddenLayerMask._values[ i ];
            int batchOffset = b * cells + i;
            hiddenLayerMaskBatch._values[ batchOffset ] = r;
        }
    }

    protected static void singleTrainWithCells(
            LifetimeSparseAutoencoder encoder,
            Data cellSpikes ) {

        int batchSize = 1;//encoder._c.getBatchSize();
//        int batchCount = 0;//encoder._c.getBatchCount();
//
//        // actually we don't use this sparsity at all, we get the combined set from elsewhere.
//        int sparsityTraining = 0;//GetSparsityTraining( encoder._c ); // encoder._c.getSparsity();
//
//        encoder.encode( encoder._inputValues, encoder._cellWeights, encoder._cellBiases1, cellWeightedSumTraining, cellSpikesTraining, sparsityTraining );
//
//        // Adjust the winning set:
//        setActiveCells( activeCells, cellWeightedSumTraining, cellSpikesTraining );

        Data hiddenLayerInput = encoder._inputValues;
//        Data hiddenLayerWeightedSum = cellWeightedSum;
        Data outputLayerInput = cellSpikes;

        DataSize hiddenLayerSize = encoder._cellSpikes._dataSize;
        DataSize outputLayerSize = encoder._inputValues._dataSize;

        Data hiddenLayerError = new Data( hiddenLayerSize );
        Data outputLayerError = new Data( outputLayerSize );
        Data outputLayerOutput = new Data( outputLayerSize );

//        encoder.batchAccumulate(
//                encoder._c,
//                hiddenLayerInput,
//                hiddenLayerWeightedSum,
//                outputLayerInput,
////                outputLayerOutput,
//                hiddenLayerInputBatch,
//                hiddenLayerWeightedSumBatch,
//                outputLayerInputBatch,
////                outputLayerOutputBatch,
//                batchCount );


        // add the winning cells from each column PLUS the lifetime samples to ensure all cells learn
//        encoder.batchSelectHiddenCells(
//                encoder._c,
//                encoder._cellWeights,
//                encoder._cellBiases2,
//                hiddenLayerWeightedSumBatch, // raw unfiltered output of hidden layer cells
//                outputLayerInputBatch, // original winning cells
//                outputLayerInputBatchLifetime, // calculated: original winning cells AND lifetime sparsity winning cells
//                outputLayerOutputBatch ); //

        // the selection of hidden cells included decoding to produce the output layer output.
        // Do that here instead
        encoder.decode( encoder._c, encoder._cellWeights, encoder._cellBiases2, outputLayerInput, outputLayerOutput ); // for output

        encoder.backpropagateError(
                encoder._c,
                encoder._cellWeights,
                hiddenLayerInput,
                hiddenLayerError, // calculated
                outputLayerInput,
                outputLayerError, // calculated
                outputLayerOutput );

        batchTrainWithBatchSize(
                encoder._c,
                batchSize,
                encoder._cellWeights,
                encoder._cellWeightsVelocity,
                encoder._cellBiases1,
                encoder._cellBiases2,
                encoder._cellBiases1Velocity,
                encoder._cellBiases2Velocity,
                hiddenLayerInput,
                hiddenLayerError,
                outputLayerInput,
                outputLayerError );
    }

    public static void batchTrainWithBatchSize(
            LifetimeSparseAutoencoderConfig config,
            int batchSize,
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
//        int batchSize = config.getBatchSize();

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

// The origin of this algo:
//what if the ties are fixed but many?
//say
//    2000 B "cells"
//1000 F
//Each B is connected to a number Z of F.
//Z = 10
//
//We pick K=25 winners in F
//Chance of any b having an f in K = (25/1000)*10 = 0.25 ie 1/4 of B cells CAN fire
//Apply the mask
//Train the F normally
//Train the B normally
//
//No relation between F and B except the masking
//No change in learning rate.
//
//Say we have 2 B cells i and j that both respond to F cell k.
//k fires
//both i and j are masked in
//but the winner depends only on the prior input to these cells
//
//On average we have B/Z = 2000/10 = 200 cells available
//From those 200 we have to pick e.g. 25.
//
//What is the chance of 10 in 25 overlap between the F cells?
//
//Probability of picking same cell is 10/1000 = 0.01
//Probability of picking 2 cells in common is 0.01 * 0.01 = 0.00001
//Prob of picking 25 cells in common is 0
//
//Adding more layers gives you more chances to encode the variable paths
//
//i will only fire given prior context match, otherwise one of the other 200 cells will fire.
//For each different F set K, we enable a different set of B cells
//
//The mask might as well be striped as theres no locality (is this true?)
//ie Bcell1 -> 0:9 Bcell2 -> 10:19

// say I fire F cell i
// there are 2000 B cells
// each one has 10 F cell inputs.
// change of including i = 1/1000 * 10 = 0.01
// Expected number of B cells matching F cell i = 0.01 * 2000 = 20
}
