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

package io.agi.framework.entities.stdp;

import io.agi.core.ann.unsupervised.stdp.paper.*;
import io.agi.core.data.Data;
import io.agi.core.orm.ObjectMap;
import io.agi.framework.DataFlags;
import io.agi.framework.Entity;
import io.agi.framework.Framework;
import io.agi.framework.Node;
import io.agi.framework.persistence.models.ModelEntity;

import java.util.Collection;

/**
 * Created by dave on 6/05/17.
 */
public class GreedySpikingConvolutionalNetworkEntity extends Entity {

    public static final String ENTITY_TYPE = "spiking-convolutional-network";

    // data
    public static final String DATA_INPUT = "input";
    public static final String DATA_OUTPUT = "output";
    public static final String DATA_INVERSE = "inverse";

    public static final String DATA_LAYER_INPUT_SPIKES_ = "layer-input-spikes-";
    public static final String DATA_LAYER_INPUT_TRACES_ = "layer-input-traces-";
    public static final String DATA_LAYER_KERNEL_WEIGHTS_ = "layer-kernel-weights-";
    public static final String DATA_LAYER_CONV_SUMS_ = "layer-conv-sums-";
    public static final String DATA_LAYER_CONV_INHIBITION_ = "layer-conv-inhibition-";
    public static final String DATA_LAYER_CONV_INTEGRATED_ = "layer-conv-integrated-";
    public static final String DATA_LAYER_CONV_SPIKES_ = "layer-conv-spikes-";
    public static final String DATA_LAYER_POOL_SPIKES_ = "layer-pool-spikes-";
    public static final String DATA_LAYER_POOL_INHIBITION_ = "layer-pool-inhibition-";

    public GreedySpikingConvolutionalNetworkEntity(ObjectMap om, Node n, ModelEntity model) {
        super( om, n, model );
    }

    @Override
    public void getInputAttributes( Collection< String > attributes ) {
        attributes.add( DATA_INPUT );
    }

    @Override
    public void getOutputAttributes( Collection< String > attributes, DataFlags flags ) {
        attributes.add( DATA_OUTPUT );
        flags.putFlag( DATA_OUTPUT, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( DATA_OUTPUT, DataFlags.FLAG_SPARSE_BINARY );

        attributes.add( DATA_INVERSE );
        flags.putFlag( DATA_INVERSE, DataFlags.FLAG_NODE_CACHE );

        GreedySpikingConvolutionalNetworkConfig networkConfig = createNetworkConfig();

        int layers = networkConfig.getNbrLayers();

        for( int layer = 0; layer < layers; ++layer ) {

            attributes.add( DATA_LAYER_INPUT_SPIKES_ + layer );
            flags.putFlag( DATA_LAYER_INPUT_SPIKES_, DataFlags.FLAG_NODE_CACHE );

            attributes.add( DATA_LAYER_INPUT_TRACES_ + layer );
            flags.putFlag( DATA_LAYER_INPUT_TRACES_, DataFlags.FLAG_NODE_CACHE );

            attributes.add( DATA_LAYER_KERNEL_WEIGHTS_ + layer );
            flags.putFlag( DATA_LAYER_KERNEL_WEIGHTS_, DataFlags.FLAG_NODE_CACHE );

            attributes.add( DATA_LAYER_CONV_SUMS_ + layer );
            flags.putFlag( DATA_LAYER_CONV_SUMS_, DataFlags.FLAG_NODE_CACHE );

            attributes.add( DATA_LAYER_CONV_INHIBITION_ + layer );
            flags.putFlag( DATA_LAYER_CONV_INHIBITION_, DataFlags.FLAG_NODE_CACHE );

            attributes.add( DATA_LAYER_CONV_INTEGRATED_ + layer );
            flags.putFlag( DATA_LAYER_CONV_INTEGRATED_, DataFlags.FLAG_NODE_CACHE );

            attributes.add( DATA_LAYER_CONV_SPIKES_ + layer );
            flags.putFlag( DATA_LAYER_CONV_SPIKES_, DataFlags.FLAG_NODE_CACHE );

            attributes.add( DATA_LAYER_POOL_SPIKES_ + layer );
            flags.putFlag( DATA_LAYER_POOL_SPIKES_, DataFlags.FLAG_NODE_CACHE );

            attributes.add( DATA_LAYER_POOL_INHIBITION_ + layer );
            flags.putFlag( DATA_LAYER_POOL_INHIBITION_, DataFlags.FLAG_NODE_CACHE );
        }
    }

    @Override
    public Class getConfigClass() {
        return SpikingConvolutionalNetworkEntityConfig.class;
    }

    protected GreedySpikingConvolutionalNetworkConfig createNetworkConfig() {
        GreedySpikingConvolutionalNetworkEntityConfig config = ( GreedySpikingConvolutionalNetworkEntityConfig ) _config;
        GreedySpikingConvolutionalNetworkConfig networkConfig = new GreedySpikingConvolutionalNetworkConfig();
        String name = getName();
        ObjectMap om = ObjectMap.GetInstance();

        networkConfig.setup(
                om, name, _r,
                config.trainingAge,
                config.weightsStdDev,
                config.weightsMean,
                config.learningRatePos,
                config.learningRateNeg,
                config.nbrLayers,
                config.layerTrainingAge,
                config.layerIntegrationThreshold,
                config.layerInputPadding,
                config.layerInputStride,
                config.layerWidth,
                config.layerHeight,
                config.layerDepth,
                config.layerfieldWidth,
                config.layerfieldHeight,
                config.layerfieldDepth,
                config.layerPoolingWidth,
                config.layerPoolingHeight );

        return networkConfig;
    }

    public void doUpdateSelf() {

        Data input = getData( DATA_INPUT );
        if( input == null ) {
            return; // can't update yet.
        }

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Algorithm specific parameters
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Region size
        GreedySpikingConvolutionalNetworkEntityConfig config = ( GreedySpikingConvolutionalNetworkEntityConfig ) _config;
        GreedySpikingConvolutionalNetworkConfig networkConfig = createNetworkConfig(); // copies config from EntityConfig
        GreedySpikingConvolutionalNetwork scn = new GreedySpikingConvolutionalNetwork();
        scn.setup( networkConfig );
        scn.setInput( input );
        scn.resize();

        copyDataFromPersistence( scn );

        // Update the region-layer, including optional reset and learning on/off switch
        if( config.reset ) {
            scn.reset();
        }

        boolean clear = false;

        try {
            String stringValue = Framework.GetConfig( config.clearFlagEntityName, config.clearFlagConfigPath );
            clear = Boolean.valueOf( stringValue );
        }
        catch( Exception e ) {
        }

        clear |= config.clear;

        if( clear ) {
            //System.err.println(" Clearing " + getName() );
            scn.clear();
            config.clear = false;
        }
        else {
            //System.err.println( " Not clearing " );
        }

        scn._config.setLearn( config.learn );
        scn.update();

        Data output = scn.getOutput(); // the potential max-pooling
        Data inverted = scn.invert( output );

        setData( DATA_OUTPUT, output );
        setData( DATA_INVERSE, inverted );

        // Save computed config properties
        config.trainingAge = scn._config.getAge();
        copyDataToPersistence( scn );
    }

    protected void copyDataFromPersistence( GreedySpikingConvolutionalNetwork scn ) {

        int layers = scn._config.getNbrLayers();

        for( int layer = 0; layer < layers; ++layer ) {
            GreedySpikingConvolutionalNetworkLayer scnl =  scn._layers.get( layer );

            scnl._inputSpikes = getDataLazyResize( DATA_LAYER_INPUT_SPIKES_ + layer, scnl._inputSpikes._dataSize );
            scnl._inputTrace = getDataLazyResize( DATA_LAYER_INPUT_TRACES_ + layer, scnl._inputTrace._dataSize );
            scnl._kernelWeights = getDataLazyResize( DATA_LAYER_KERNEL_WEIGHTS_ + layer, scnl._kernelWeights._dataSize );
            scnl._convSums = getDataLazyResize( DATA_LAYER_CONV_SUMS_ + layer, scnl._convSums._dataSize );
            scnl._convInhibition = getDataLazyResize( DATA_LAYER_CONV_INHIBITION_ + layer, scnl._convInhibition._dataSize );
            scnl._convIntegrated = getDataLazyResize( DATA_LAYER_CONV_INTEGRATED_ + layer, scnl._convIntegrated._dataSize );
            scnl._convSpikes = getDataLazyResize( DATA_LAYER_CONV_SPIKES_ + layer, scnl._convSpikes._dataSize );
            scnl._poolSpikes = getDataLazyResize( DATA_LAYER_POOL_SPIKES_ + layer, scnl._poolSpikes._dataSize );
            scnl._poolInhibition = getDataLazyResize( DATA_LAYER_POOL_INHIBITION_ + layer, scnl._poolInhibition._dataSize );
        }
    }

    protected void copyDataToPersistence( GreedySpikingConvolutionalNetwork scn ) {

        int layers = scn._config.getNbrLayers();

        for( int layer = 0; layer < layers; ++layer ) {
            GreedySpikingConvolutionalNetworkLayer scnl =  scn._layers.get( layer );

            setData( DATA_LAYER_INPUT_SPIKES_ + layer, scnl._inputSpikes );
            setData( DATA_LAYER_INPUT_TRACES_ + layer, scnl._inputTrace );
            setData( DATA_LAYER_KERNEL_WEIGHTS_ + layer, scnl._kernelWeights );
            setData( DATA_LAYER_CONV_SUMS_ + layer, scnl._convSums );
            setData( DATA_LAYER_CONV_INHIBITION_ + layer, scnl._convInhibition );
            setData( DATA_LAYER_CONV_INTEGRATED_ + layer, scnl._convIntegrated );
            setData( DATA_LAYER_CONV_SPIKES_ + layer, scnl._convSpikes );
            setData( DATA_LAYER_POOL_SPIKES_ + layer, scnl._poolSpikes );
            setData( DATA_LAYER_POOL_INHIBITION_ + layer, scnl._poolInhibition );
        }

    }
}