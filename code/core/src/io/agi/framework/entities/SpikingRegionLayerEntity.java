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

package io.agi.framework.entities;

import io.agi.core.alg.AutoRegionLayer;
import io.agi.core.alg.AutoRegionLayerConfig;
import io.agi.core.alg.SpikingRegionLayer;
import io.agi.core.ann.unsupervised.KSparseAutoencoderConfig;
import io.agi.core.ann.unsupervised.SpikingNeuralNetworkConfig;
import io.agi.core.data.Data;
import io.agi.core.data.Data2d;
import io.agi.core.orm.ObjectMap;
import io.agi.framework.DataFlags;
import io.agi.framework.Entity;
import io.agi.framework.Node;
import io.agi.framework.persistence.models.ModelEntity;

import java.awt.*;
import java.util.Collection;
import java.util.Random;

/**
 * Created by dave on 30/09/16.
 */
public class SpikingRegionLayerEntity extends Entity {

    public static final String ENTITY_TYPE = "spiking-region-layer";

    public static final String INPUT_1 = "input-1";
    public static final String INPUT_2 = "input-2";
    public static final String OUTPUT = "output";

    public static final String CELL_MEMBRANE_TIME_CONSTANTS = "cell-membrane-time-constants";
    public static final String CELL_POTENTIALS_OLD = "cell-potentials-old";
    public static final String CELL_POTENTIALS_NEW = "cell-potentials-new";
    public static final String CELL_SPIKE_RATE_TARGETS = "cell-spike-rate-targets";
    public static final String CELL_SPIKE_THRESHOLDS = "cell-spike-thresholds";
    public static final String CELL_SPIKE_THRESHOLD_DELTAS = "cell-spike-threshold-deltas";
    public static final String CELL_SPIKE_THRESHOLD_VELOCITIES = "cell-spike-threshold-velocities";

    public static final String CELLS_INPUT_WEIGHTS = "cells-input-weights";

    public static final String INPUT_SPIKES_OLD = "input-spikes-old";
    public static final String INPUT_SPIKES_NEW = "input-spikes-new";
    public static final String INPUT_WEIGHTS = "input-weights";
    public static final String INPUT_SPIKE_RATES = "input-spike-rates";
    public static final String INPUT_SPIKE_TRACES = "input-spike-traces";

    public SpikingRegionLayerEntity( ObjectMap om, Node n, ModelEntity model ) {
        super( om, n, model );
    }

        public void getInputAttributes( Collection< String > attributes ) {
            attributes.add( INPUT_1 );
            attributes.add( INPUT_2 );
        }

        public void getOutputAttributes( Collection< String > attributes, DataFlags flags ) {

            attributes.add( OUTPUT );

            attributes.add( CELL_MEMBRANE_TIME_CONSTANTS );
            attributes.add( CELL_POTENTIALS_OLD );
            attributes.add( CELL_POTENTIALS_NEW );
            attributes.add( CELL_SPIKE_RATE_TARGETS );
            attributes.add( CELL_SPIKE_THRESHOLDS );
            attributes.add( CELL_SPIKE_THRESHOLD_DELTAS );
            attributes.add( CELL_SPIKE_THRESHOLD_VELOCITIES );

            attributes.add( CELLS_INPUT_WEIGHTS );

            attributes.add( INPUT_SPIKES_OLD );
            attributes.add( INPUT_SPIKES_NEW );
            attributes.add( INPUT_WEIGHTS );
            attributes.add( INPUT_SPIKE_RATES );
            attributes.add( INPUT_SPIKE_TRACES );

            // optimize IO
            flags.putFlag( OUTPUT, DataFlags.FLAG_SPARSE_BINARY );

            flags.putFlag( CELL_MEMBRANE_TIME_CONSTANTS, DataFlags.FLAG_LAZY_PERSIST ); // persist only on change, ie rarely change
            flags.putFlag( CELL_MEMBRANE_TIME_CONSTANTS, DataFlags.FLAG_NODE_CACHE ); // not modified externally to node. avoids read

            flags.putFlag( CELL_SPIKE_RATE_TARGETS, DataFlags.FLAG_LAZY_PERSIST ); // persist only on change, ie rarely change
            flags.putFlag( CELL_SPIKE_RATE_TARGETS, DataFlags.FLAG_NODE_CACHE ); // not modified externally to node. avoids read

            flags.putFlag( INPUT_WEIGHTS, DataFlags.FLAG_LAZY_PERSIST ); // persist only on change, ie rarely change
            flags.putFlag( INPUT_WEIGHTS, DataFlags.FLAG_NODE_CACHE ); // not modified externally to node. avoids read

            flags.putFlag( CELL_POTENTIALS_OLD, DataFlags.FLAG_NODE_CACHE ); // not modified externally to node. avoids read
            flags.putFlag( CELL_POTENTIALS_NEW, DataFlags.FLAG_NODE_CACHE ); // not modified externally to node. avoids read
            flags.putFlag( CELL_SPIKE_THRESHOLDS, DataFlags.FLAG_NODE_CACHE ); // not modified externally to node. avoids read
            flags.putFlag( CELL_SPIKE_THRESHOLD_DELTAS, DataFlags.FLAG_NODE_CACHE ); // not modified externally to node. avoids read
            flags.putFlag( CELL_SPIKE_THRESHOLD_VELOCITIES, DataFlags.FLAG_NODE_CACHE ); // not modified externally to node. avoids read

            flags.putFlag( CELLS_INPUT_WEIGHTS, DataFlags.FLAG_NODE_CACHE ); // not modified externally to node. avoids read
            flags.putFlag( CELLS_INPUT_WEIGHTS, DataFlags.FLAG_PERSIST_ON_FLUSH ); // 5000 x 5800 = 30,000,000

            flags.putFlag( INPUT_SPIKES_OLD, DataFlags.FLAG_NODE_CACHE ); // not modified externally to node. avoids read
            flags.putFlag( INPUT_SPIKES_NEW, DataFlags.FLAG_NODE_CACHE ); // not modified externally to node. avoids read
            flags.putFlag( INPUT_SPIKE_RATES, DataFlags.FLAG_NODE_CACHE ); // not modified externally to node. avoids read
            flags.putFlag( INPUT_SPIKE_TRACES, DataFlags.FLAG_NODE_CACHE ); // not modified externally to node. avoids read

            flags.putFlag( INPUT_SPIKES_OLD, DataFlags.FLAG_SPARSE_BINARY );
            flags.putFlag( INPUT_SPIKES_NEW, DataFlags.FLAG_SPARSE_BINARY );

            flags.putFlag( INPUT_SPIKE_TRACES, DataFlags.FLAG_SPARSE_REAL ); // mostly empty, because it decays quickly


//            flags.putFlag( XXX, DataFlags.FLAG_PERSIST_ONLY ); // never read, ie regenerated each time
//            flags.putFlag( XXX, DataFlags.FLAG_PERSIST_ON_FLUSH );

        }

        @Override
        public Class getConfigClass() {
            return SpikingRegionLayerEntityConfig.class;
        }

        protected void doUpdateSelf() {

            // Do nothing unless the input is defined
            Data input1 = getData( INPUT_1 );
            Data input2 = getData( INPUT_2 );

            if( ( input2 == null ) || ( input1 == null ) ) {
                return; // can't update yet.
            }

            // Get all the parameters:
            String regionLayerName = getName();

            ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // Test parameters
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // Feedforward size
            Point input1Size = Data2d.getSize( input1 );
            Point input2Size = Data2d.getSize( input2 );

            int input1Width  = input1Size.x;
            int input1Height = input1Size.y;
            int input2Width  = input2Size.x;
            int input2Height = input2Size.y;

            int input1Area = input1Width * input1Height;
            int input2Area = input2Width * input2Height;

            ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // Algorithm specific parameters
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // Region size
            SpikingRegionLayerEntityConfig config = ( SpikingRegionLayerEntityConfig ) _config;

            // Build the algorithm
            //RandomInstance.setSeed(randomSeed); // make the tests repeatable
            ObjectMap om = ObjectMap.GetInstance();
            String name = getName();

            SpikingNeuralNetworkConfig rlc = new SpikingNeuralNetworkConfig();
            rlc.setup(
                    om,
                    name,
                    _r,
                    config.excitatoryCells,
                    config.inhibitoryCells,
                    input1Area,
                    input2Area,

                    config.spikeThresholdBatchSize,
                    config.spikeThresholdBatchIndex,
                    config.timeConstantExcitatory,
                    config.timeConstantInhibitory,
                    config.targetSpikeRateExcitatory,
                    config.targetSpikeRateInhibitory,

                    config.resetSpikeThreshold,

                    config.spikeRateLearningRate,
                    config.spikeTraceLearningRate,
                    config.spikeThresholdLearningRate,

                    config.synapseLearningRateExternal1ToExcitatory   ,
                    config.synapseLearningRateExternal2ToExcitatory   ,
                    config.synapseLearningRateExcitatoryToExcitatory ,
                    config.synapseLearningRateInhibitoryToExcitatory ,
                    config.synapseLearningRateExternal1ToInhibitory   ,
                    config.synapseLearningRateExternal2ToInhibitory   ,
                    config.synapseLearningRateExcitatoryToInhibitory ,
                    config.synapseLearningRateInhibitoryToInhibitory ,

                    config.inputWeightExcitatory,
                    config.inputWeightInhibitory,
                    config.inputWeightExternal1,
                    config.inputWeightExternal2 );

            SpikingRegionLayer rl = new SpikingRegionLayer();
            rl.setup( rlc );

            // Load data, overwriting the default setup.
            rlc._spikeThresholdBatchIndex = config.spikeThresholdBatchIndex;

            long t1 = System.currentTimeMillis();
            copyDataFromPersistence( rl );

            // Update the region-layer, including optional reset and learning on/off switch
            if( config.reset ) {
                rl.reset();
            }

            rl._snn._c.setLearn( config.learn );
            rl.update();

            // store auto config changes
            config.spikeThresholdBatchIndex = rl._snn._c._spikeThresholdBatchIndex;

            // Save data
            copyDataToPersistence( rl );

            long t2 = System.currentTimeMillis();
            long elapsed = t2 -t1;
            System.err.println( " Update dt: " + elapsed );
        }

        protected void copyDataFromPersistence( SpikingRegionLayer rl ) {

            rl._input1 = getData( INPUT_1 );
            rl._input2 = getData( INPUT_2 );

            rl._snn._cellMembraneTimeConstants = getDataLazyResize( CELL_MEMBRANE_TIME_CONSTANTS, rl._snn._cellMembraneTimeConstants._dataSize );
            rl._snn._cellPotentialsOld = getDataLazyResize( CELL_POTENTIALS_OLD, rl._snn._cellPotentialsOld._dataSize );
            rl._snn._cellPotentialsNew = getDataLazyResize( CELL_POTENTIALS_NEW, rl._snn._cellPotentialsNew._dataSize );
            rl._snn._cellSpikeRateTargets = getDataLazyResize( CELL_SPIKE_RATE_TARGETS, rl._snn._cellSpikeRateTargets._dataSize );
            rl._snn._cellSpikeThresholds = getDataLazyResize( CELL_SPIKE_THRESHOLDS, rl._snn._cellSpikeThresholds._dataSize );
            rl._snn._cellSpikeThresholdDeltas = getDataLazyResize( CELL_SPIKE_THRESHOLD_DELTAS, rl._snn._cellSpikeThresholdDeltas._dataSize );
            rl._snn._cellSpikeThresholdVelocities = getDataLazyResize( CELL_SPIKE_THRESHOLD_VELOCITIES, rl._snn._cellSpikeThresholdVelocities._dataSize );

            rl._snn._cellsInputWeights = getDataLazyResize( CELLS_INPUT_WEIGHTS, rl._snn._cellsInputWeights._dataSize );

            rl._snn._inputSpikesOld = getDataLazyResize( INPUT_SPIKES_OLD, rl._snn._inputSpikesOld._dataSize );
            rl._snn._inputSpikesNew = getDataLazyResize( INPUT_SPIKES_NEW, rl._snn._inputSpikesNew._dataSize );
            rl._snn._inputWeights = getDataLazyResize( INPUT_WEIGHTS, rl._snn._inputWeights._dataSize );
            rl._snn._inputSpikeRates = getDataLazyResize( INPUT_SPIKE_RATES, rl._snn._inputSpikeRates._dataSize );
            rl._snn._inputSpikeTraces = getDataLazyResize( INPUT_SPIKE_TRACES, rl._snn._inputSpikeTraces._dataSize );
        }

        protected void copyDataToPersistence( SpikingRegionLayer rl ) {

            setData( OUTPUT, rl._output );

            setData( CELL_MEMBRANE_TIME_CONSTANTS, rl._snn._cellMembraneTimeConstants );
            setData( CELL_POTENTIALS_OLD, rl._snn._cellPotentialsOld );
            setData( CELL_POTENTIALS_NEW, rl._snn._cellPotentialsNew );
            setData( CELL_SPIKE_RATE_TARGETS, rl._snn._cellSpikeRateTargets );
            setData( CELL_SPIKE_THRESHOLDS, rl._snn._cellSpikeThresholds );
            setData( CELL_SPIKE_THRESHOLD_DELTAS, rl._snn._cellSpikeThresholdDeltas );
            setData( CELL_SPIKE_THRESHOLD_VELOCITIES, rl._snn._cellSpikeThresholdVelocities );

            setData( CELLS_INPUT_WEIGHTS, rl._snn._cellsInputWeights );

            setData( INPUT_SPIKES_OLD, rl._snn._inputSpikesOld );
            setData( INPUT_SPIKES_NEW, rl._snn._inputSpikesNew );
            setData( INPUT_WEIGHTS, rl._snn._inputWeights );
            setData( INPUT_SPIKE_RATES, rl._snn._inputSpikeRates );
            setData( INPUT_SPIKE_TRACES, rl._snn._inputSpikeTraces );
        }

}
