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

package io.agi.framework.entities;

import io.agi.core.ann.unsupervised.LifetimeSparseAutoencoder;
import io.agi.core.ann.unsupervised.SparseSequenceAutoencoder;
import io.agi.core.ann.unsupervised.SparseSequenceAutoencoderConfig;
import io.agi.core.data.Data;
import io.agi.core.data.Data2d;
import io.agi.core.orm.ObjectMap;
import io.agi.framework.DataFlags;
import io.agi.framework.Entity;
import io.agi.framework.Node;
import io.agi.framework.persistence.models.ModelEntity;

import java.awt.*;
import java.util.Collection;

/**
 * Created by dave on 10/11/17.
 */
public class SparseSequenceAutoencoderEntity extends Entity {

    public static final String ENTITY_TYPE = "sequence-sparse-autoencoder";

    public static final String INPUT_F = "input-f";
    public static final String INPUT_B = "input-b";

    public static final String OUTPUT_AGE_SINCE_SPIKE_B = "age-since-spike-b";
    public static final String OUTPUT_AGE_SINCE_SPIKE_F = "age-since-spike-f";
    public static final String OUTPUT_CELL_MAPPINGS = "cell-mappings";
    public static final String OUTPUT_CELL_MASK_B = "cell-mask-b";
    public static final String OUTPUT_CELL_MASK_BATCH = "cell-mask-batch";
    public static final String OUTPUT = "output";

    public static final String SUFFIX_F = "-f";
    public static final String SUFFIX_B = "-b";

    public SparseSequenceAutoencoderEntity( ObjectMap om, Node n, ModelEntity model ) {
        super( om, n, model );
    }

    public void getInputAttributes( Collection< String > attributes ) {
        attributes.add( INPUT_F );
        attributes.add( INPUT_B );

        attributes.add( LifetimeSparseAutoencoderEntity.INPUT_SPIKES );
    }

    public void getOutputAttributes( Collection< String > attributes, DataFlags flags ) {
        getOutputAttributes( attributes, flags, SUFFIX_F );
        getOutputAttributes( attributes, flags, SUFFIX_B );

        attributes.add( SparseSequenceAutoencoderEntity.OUTPUT_AGE_SINCE_SPIKE_B );
        attributes.add( SparseSequenceAutoencoderEntity.OUTPUT_AGE_SINCE_SPIKE_F );

        attributes.add( SparseSequenceAutoencoderEntity.OUTPUT_CELL_MAPPINGS );
        attributes.add( SparseSequenceAutoencoderEntity.OUTPUT_CELL_MASK_B );
        attributes.add( SparseSequenceAutoencoderEntity.OUTPUT_CELL_MASK_BATCH );
        attributes.add( SparseSequenceAutoencoderEntity.OUTPUT );
    }

    public void getOutputAttributes( Collection< String > attributes, DataFlags flags, String suffix ) {

        attributes.add( LifetimeSparseAutoencoderEntity.OUTPUT_DECODED + suffix );

        attributes.add( LifetimeSparseAutoencoderEntity.WEIGHTS + suffix );
        attributes.add( LifetimeSparseAutoencoderEntity.BIASES_1 + suffix );
        attributes.add( LifetimeSparseAutoencoderEntity.BIASES_2 + suffix );

        attributes.add( LifetimeSparseAutoencoderEntity.WEIGHTS_VELOCITY + suffix );
        attributes.add( LifetimeSparseAutoencoderEntity.BIASES_1_VELOCITY + suffix );
        attributes.add( LifetimeSparseAutoencoderEntity.BIASES_2_VELOCITY + suffix );

        attributes.add( LifetimeSparseAutoencoderEntity.ERRORS + suffix );
        attributes.add( LifetimeSparseAutoencoderEntity.SPIKES + suffix );
        attributes.add( LifetimeSparseAutoencoderEntity.WEIGHTED_SUM + suffix );
        attributes.add( LifetimeSparseAutoencoderEntity.OUTPUT_RECONSTRUCTION + suffix );

        attributes.add( LifetimeSparseAutoencoderEntity.BATCH_OUTPUT_OUTPUT + suffix );
        attributes.add( LifetimeSparseAutoencoderEntity.BATCH_OUTPUT_INPUT + suffix );
        attributes.add( LifetimeSparseAutoencoderEntity.BATCH_OUTPUT_INPUT_LIFETIME + suffix );
        attributes.add( LifetimeSparseAutoencoderEntity.BATCH_OUTPUT_ERRORS + suffix );
        attributes.add( LifetimeSparseAutoencoderEntity.BATCH_HIDDEN_OUTPUT + suffix );
        attributes.add( LifetimeSparseAutoencoderEntity.BATCH_HIDDEN_WEIGHTED_SUM + suffix );
        attributes.add( LifetimeSparseAutoencoderEntity.BATCH_HIDDEN_ERRORS + suffix );
    }

    @Override
    public Class getConfigClass() {
        return SparseSequenceAutoencoderEntityConfig.class;
    }

    protected void doUpdateSelf() {

        // Do nothing unless the input is defined
        Data inputF = getData( INPUT_F );
        Data inputB = getData( INPUT_B );

        SparseSequenceAutoencoderEntityConfig config = ( SparseSequenceAutoencoderEntityConfig ) _config;

        if( ( inputF == null ) || ( inputB == null ) ) {
            // output an empty matrix for recurrent connections
            Data outputF = new Data( config.widthCellsF, config.heightCellsF );
            Data outputB = new Data( config.widthCellsB, config.heightCellsB );
            Data output  = new Data( config.widthCellsB, config.heightCellsB );
            setData( LifetimeSparseAutoencoderEntity.SPIKES + SUFFIX_F, outputF );
            setData( LifetimeSparseAutoencoderEntity.SPIKES + SUFFIX_B, outputB );
            setData( SparseSequenceAutoencoderEntity.OUTPUT, output );

            if( config.reset ) {
                config.resetDelayed = true;
            }

            return; // can't update yet.
        }

        // Get all the parameters:
        String name = getName();

        // Feedforward size
        Point inputFSize = Data2d.getSize( inputF );
        int inputFWidth = inputFSize.x;
        int inputFHeight = inputFSize.y;
        int inputFArea = inputFWidth * inputFHeight;

        // Feedback size
        Point inputBSize = Data2d.getSize( inputB );
        int inputBWidth = inputBSize.x;
        int inputBHeight = inputBSize.y;
        int inputBArea = inputBWidth * inputBHeight;

        // Build the algorithm
        ObjectMap om = ObjectMap.GetInstance();

        SparseSequenceAutoencoderConfig autoencoderConfig = new SparseSequenceAutoencoderConfig();

        autoencoderConfig.setup(
                om, name, _r,
                inputFArea, inputBArea,
                config.widthCellsF, config.heightCellsF,
                config.widthCellsB, config.heightCellsB,
                config.cellMappingDensity,
                config.sparsityTrainingF,
                config.sparsityOutputF,
                config.sparsityBatchF,
                config.sparsityTrainingB,
                config.sparsityOutputB,
                config.sparsityBatchB,
                config.learningRate, config.momentum, config.weightsStdDev,
                config.batchCountF,
                config.batchCountB,
                config.batchSizeF,
                config.batchSizeB );

        SparseSequenceAutoencoder ssa = new SparseSequenceAutoencoder( name, om );

        ssa.setup( autoencoderConfig );
//System.err.println( "1 SSA WB min,max" + ssa._cellWeightsB.getMinMax().getX() + " " + ssa._cellWeightsB.getMinMax().getY() );
        // Load data, overwriting the default setup.
        copyDataFromPersistence( ssa );
//System.err.println( "2 SSA WB min,max" + ssa._cellWeightsB.getMinMax().getX() + " " + ssa._cellWeightsB.getMinMax().getY() );

        // Update the region-layer, including optional reset and learning on/off switch
        if( config.reset || config.resetDelayed ) {
            ssa.reset();
            config.resetDelayed = false;
        }

        ssa._uniqueRows = config.uniqueRows;
        ssa._c.setLearn( config.learn );
        ssa.update();

        // Save computed properties
//        config.predictionError = ssa._predictionError;
        config.batchCountF = ssa._encoderF._c.getBatchCount();
        config.batchCountB = ssa._encoderB._c.getBatchCount();
        config.compressionErrorF = ssa._encodingErrorF;
        config.compressionErrorB = ssa._encodingErrorB;
        config.uniqueRows = ssa._uniqueRows;

//System.err.println( "3 SSA WB min,max" + ssa._cellWeightsB.getMinMax().getX() + " " + ssa._cellWeightsB.getMinMax().getY() );

        // Look for a hidden layer spike pattern to invert
        // This is for external prediction. But now it does prediction internally?
        Data inputSpikes = getData( LifetimeSparseAutoencoderEntity.INPUT_SPIKES );
        if( inputSpikes != null ) {
            Data decodedF = new Data( inputF._dataSize );
            Data decodedB = new Data( inputB._dataSize );

            ssa._encoderF.decode( inputSpikes, decodedF );
            ssa._encoderB.decode( inputSpikes, decodedB );

            setData( LifetimeSparseAutoencoderEntity.OUTPUT_DECODED + SUFFIX_F, decodedF );
            setData( LifetimeSparseAutoencoderEntity.OUTPUT_DECODED + SUFFIX_B, decodedB );
        }

        // Save data
        copyDataToPersistence( ssa );

//System.err.println( "4 SSA WB min,max" + ssa._cellWeightsB.getMinMax().getX() + " " + ssa._cellWeightsB.getMinMax().getY() );
    }

    protected void copyDataFromPersistence( SparseSequenceAutoencoder ssa ) {
        ssa.setInputF( getData( INPUT_F ) );
        ssa.setInputB( getData( INPUT_B ) );

        copyDataFromPersistence( ssa._encoderF, SUFFIX_F );
        copyDataFromPersistence( ssa._encoderB, SUFFIX_B );

        ssa._ageSinceSpikeF = getDataLazyResize( OUTPUT_AGE_SINCE_SPIKE_F, ssa._ageSinceSpikeF._dataSize );
        ssa._ageSinceSpikeB = getDataLazyResize( OUTPUT_AGE_SINCE_SPIKE_B, ssa._ageSinceSpikeB._dataSize );

        ssa._cellMappings = getDataLazyResize( OUTPUT_CELL_MAPPINGS, ssa._cellMappings._dataSize );
        ssa._cellMaskB = getDataLazyResize( OUTPUT_CELL_MASK_B, ssa._cellMaskB._dataSize );
        ssa._cellMaskBatch = getDataLazyResize( OUTPUT_CELL_MASK_BATCH, ssa._cellMaskBatch._dataSize );
    }

    protected void copyDataFromPersistence( LifetimeSparseAutoencoder ksa, String suffix ) {

        ksa._cellWeights = getDataLazyResize( LifetimeSparseAutoencoderEntity.WEIGHTS + suffix, ksa._cellWeights._dataSize );
        ksa._cellBiases1 = getDataLazyResize( LifetimeSparseAutoencoderEntity.BIASES_1 + suffix, ksa._cellBiases1._dataSize );
        ksa._cellBiases2 = getDataLazyResize( LifetimeSparseAutoencoderEntity.BIASES_2 + suffix, ksa._cellBiases2._dataSize );

        ksa._cellWeightsVelocity = getDataLazyResize( LifetimeSparseAutoencoderEntity.WEIGHTS_VELOCITY + suffix, ksa._cellWeightsVelocity._dataSize );
        ksa._cellBiases1Velocity = getDataLazyResize( LifetimeSparseAutoencoderEntity.BIASES_1_VELOCITY + suffix, ksa._cellBiases1Velocity._dataSize );
        ksa._cellBiases2Velocity = getDataLazyResize( LifetimeSparseAutoencoderEntity.BIASES_2_VELOCITY + suffix, ksa._cellBiases2Velocity._dataSize );

        ksa._cellErrors = getDataLazyResize( LifetimeSparseAutoencoderEntity.ERRORS + suffix, ksa._cellErrors._dataSize );
        ksa._cellWeightedSum = getDataLazyResize( LifetimeSparseAutoencoderEntity.WEIGHTED_SUM + suffix, ksa._cellWeightedSum._dataSize );
        ksa._cellSpikes = getDataLazyResize( LifetimeSparseAutoencoderEntity.SPIKES + suffix, ksa._cellSpikes._dataSize );
        ksa._inputReconstruction = getDataLazyResize( LifetimeSparseAutoencoderEntity.OUTPUT_RECONSTRUCTION + suffix, ksa._inputReconstruction._dataSize );

        ksa._batchOutputOutput = getDataLazyResize( LifetimeSparseAutoencoderEntity.BATCH_OUTPUT_OUTPUT + suffix, ksa._batchOutputOutput._dataSize );
        ksa._batchOutputInput = getDataLazyResize( LifetimeSparseAutoencoderEntity.BATCH_OUTPUT_INPUT + suffix, ksa._batchOutputInput._dataSize );
        ksa._batchOutputInputLifetime = getDataLazyResize( LifetimeSparseAutoencoderEntity.BATCH_OUTPUT_INPUT_LIFETIME + suffix, ksa._batchOutputInputLifetime._dataSize );
        ksa._batchOutputErrors = getDataLazyResize( LifetimeSparseAutoencoderEntity.BATCH_OUTPUT_ERRORS + suffix, ksa._batchOutputErrors._dataSize );
        ksa._batchHiddenInput = getDataLazyResize( LifetimeSparseAutoencoderEntity.BATCH_HIDDEN_OUTPUT + suffix, ksa._batchHiddenInput._dataSize );
        ksa._batchHiddenWeightedSum = getDataLazyResize( LifetimeSparseAutoencoderEntity.BATCH_HIDDEN_WEIGHTED_SUM + suffix, ksa._batchHiddenWeightedSum._dataSize );
        ksa._batchHiddenErrors = getDataLazyResize( LifetimeSparseAutoencoderEntity.BATCH_HIDDEN_ERRORS + suffix, ksa._batchHiddenErrors._dataSize );
    }

    protected void copyDataToPersistence( SparseSequenceAutoencoder ssa ) {
        copyDataToPersistence( ssa._encoderF, SUFFIX_F );
        copyDataToPersistence( ssa._encoderB, SUFFIX_B );

        setData( OUTPUT_AGE_SINCE_SPIKE_F, ssa._ageSinceSpikeF );
        setData( OUTPUT_AGE_SINCE_SPIKE_B, ssa._ageSinceSpikeB );

        setData( OUTPUT_CELL_MAPPINGS, ssa._cellMappings );
        setData( OUTPUT_CELL_MASK_B, ssa._cellMaskB );
        setData( OUTPUT_CELL_MASK_BATCH, ssa._cellMaskBatch );
        setData( OUTPUT, ssa._output );
    }

    protected void copyDataToPersistence( LifetimeSparseAutoencoder ksa, String suffix ) {

        setData( LifetimeSparseAutoencoderEntity.WEIGHTS + suffix, ksa._cellWeights );
        setData( LifetimeSparseAutoencoderEntity.BIASES_1 + suffix, ksa._cellBiases1 );
        setData( LifetimeSparseAutoencoderEntity.BIASES_2 + suffix, ksa._cellBiases2 );

        setData( LifetimeSparseAutoencoderEntity.WEIGHTS_VELOCITY + suffix, ksa._cellWeightsVelocity );
        setData( LifetimeSparseAutoencoderEntity.BIASES_1_VELOCITY + suffix, ksa._cellBiases1Velocity );
        setData( LifetimeSparseAutoencoderEntity.BIASES_2_VELOCITY + suffix, ksa._cellBiases2Velocity );

        setData( LifetimeSparseAutoencoderEntity.ERRORS + suffix, ksa._cellErrors );
        setData( LifetimeSparseAutoencoderEntity.WEIGHTED_SUM + suffix, ksa._cellWeightedSum );
        setData( LifetimeSparseAutoencoderEntity.SPIKES + suffix, ksa._cellSpikes );
        setData( LifetimeSparseAutoencoderEntity.OUTPUT_RECONSTRUCTION + suffix, ksa._inputReconstruction );
//            setData( LifetimeSparseAutoencoderEntity.OUTPUT_RECONSTRUCTION + suffix, ksa._inputValues );

        setData( LifetimeSparseAutoencoderEntity.BATCH_OUTPUT_OUTPUT + suffix, ksa._batchOutputOutput );
        setData( LifetimeSparseAutoencoderEntity.BATCH_OUTPUT_INPUT + suffix, ksa._batchOutputInput );
        setData( LifetimeSparseAutoencoderEntity.BATCH_OUTPUT_INPUT_LIFETIME + suffix, ksa._batchOutputInputLifetime );
        setData( LifetimeSparseAutoencoderEntity.BATCH_OUTPUT_ERRORS + suffix, ksa._batchOutputErrors );
        setData( LifetimeSparseAutoencoderEntity.BATCH_HIDDEN_OUTPUT + suffix, ksa._batchHiddenInput );
        setData( LifetimeSparseAutoencoderEntity.BATCH_HIDDEN_WEIGHTED_SUM + suffix, ksa._batchHiddenWeightedSum );
        setData( LifetimeSparseAutoencoderEntity.BATCH_HIDDEN_ERRORS + suffix, ksa._batchHiddenErrors );
    }

}