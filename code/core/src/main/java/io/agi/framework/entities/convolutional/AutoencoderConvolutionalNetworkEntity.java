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

package io.agi.framework.entities.convolutional;

import io.agi.core.ann.convolutional.ConvolutionalNetwork;
import io.agi.core.ann.convolutional.autoencoder.AutoencoderConvolutionalNetworkConfig;
import io.agi.core.ann.convolutional.autoencoder.AutoencoderConvolutionalNetworkFactory;
import io.agi.core.ann.convolutional.autoencoder.AutoencoderConvolutionalNetworkLayer;
import io.agi.core.data.Data;
import io.agi.core.orm.ObjectMap;
import io.agi.framework.DataFlags;
import io.agi.framework.Entity;
import io.agi.framework.Node;
import io.agi.framework.persistence.models.ModelEntity;

import java.util.Collection;

/**
 * Created by dave on 12/08/17.
 */
public class AutoencoderConvolutionalNetworkEntity extends Entity {

    public static final String ENTITY_TYPE = "autoencoder-convolutional-network";

    // Network data
    public static final String DATA_INPUT = "input";
    public static final String DATA_OUTPUT = "output";
    public static final String DATA_INVERSE = "inverse";
    public static final String DATA_INVERSE_SELECTED = "inverse-selected";

    public static final String DATA_LAYER_CONV_ERROR_ = "layer-conv-error-";
    public static final String DATA_LAYER_CONV_BEST_ = "layer-conv-best-";
    public static final String DATA_LAYER_POOL_ERROR_ = "layer-pool-error-";
    public static final String DATA_LAYER_POOL_BEST_ = "layer-pool-best-";

    public static final String DATA_LAYER_WEIGHTS_ = "layer-weights-";
    public static final String DATA_LAYER_BIASES1_ = "layer-biases1-";
    public static final String DATA_LAYER_BIASES2_ = "layer-biases2-";
    public static final String DATA_LAYER_WEIGHTS_VELOCITY_ = "layer-weights-velocity-";
    public static final String DATA_LAYER_BIASES1_VELOCITY_ = "layer-biases1-velocity-";
    public static final String DATA_LAYER_BIASES2_VELOCITY_ = "layer-biases2-velocity-";

    public static final String DATA_LAYER_BATCH_OUTPUT_OUTPUT_ = "layer-batch-output-output-";
    public static final String DATA_LAYER_BATCH_OUTPUT_INPUT_ = "layer-batch-output-input-";
    public static final String DATA_LAYER_BATCH_OUTPUT_INPUT_LIFETIME_ = "layer-batch-output-input-lifetime-";
    public static final String DATA_LAYER_BATCH_OUTPUT_ERRORS_ = "layer-batch-output-errors-";
    public static final String DATA_LAYER_BATCH_HIDDEN_INPUT_ = "layer-batch-hidden-input-";
    public static final String DATA_LAYER_BATCH_HIDDEN_WEIGHTED_SUM_ = "layer-batch-hidden-weighted-sum-";
    public static final String DATA_LAYER_BATCH_HIDDEN_ERRORS_ = "layer-batch-hidden-errors-";

    public AutoencoderConvolutionalNetworkEntity( ObjectMap om, Node n, ModelEntity model ) {
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

        attributes.add( DATA_INVERSE_SELECTED );
        flags.putFlag( DATA_INVERSE_SELECTED, DataFlags.FLAG_NODE_CACHE );

        AutoencoderConvolutionalNetworkConfig networkConfig = createNetworkConfig();

        int layers = networkConfig.getNbrLayers();

        for( int layer = 0; layer < layers; ++layer ) {

            attributes.add( DATA_LAYER_CONV_ERROR_ + layer );
            attributes.add( DATA_LAYER_CONV_BEST_ + layer );
            attributes.add( DATA_LAYER_POOL_ERROR_ + layer );
            attributes.add( DATA_LAYER_POOL_BEST_ + layer );

            attributes.add( DATA_LAYER_WEIGHTS_ + layer );
            attributes.add( DATA_LAYER_BIASES1_ + layer );
            attributes.add( DATA_LAYER_BIASES2_ + layer );
            attributes.add( DATA_LAYER_WEIGHTS_VELOCITY_ + layer );
            attributes.add( DATA_LAYER_BIASES1_VELOCITY_ + layer );
            attributes.add( DATA_LAYER_BIASES2_VELOCITY_ + layer );

            attributes.add( DATA_LAYER_BATCH_OUTPUT_OUTPUT_ + layer );
            attributes.add( DATA_LAYER_BATCH_OUTPUT_INPUT_ + layer );
            attributes.add( DATA_LAYER_BATCH_OUTPUT_INPUT_LIFETIME_ + layer );
            attributes.add( DATA_LAYER_BATCH_OUTPUT_ERRORS_ + layer );
            attributes.add( DATA_LAYER_BATCH_HIDDEN_INPUT_ + layer );
            attributes.add( DATA_LAYER_BATCH_HIDDEN_WEIGHTED_SUM_ + layer );
            attributes.add( DATA_LAYER_BATCH_HIDDEN_ERRORS_ + layer );
        }
    }

    @Override
    public Class getConfigClass() {
        return AutoencoderConvolutionalNetworkEntityConfig.class;
    }

    protected AutoencoderConvolutionalNetworkConfig createNetworkConfig() {
        AutoencoderConvolutionalNetworkEntityConfig config = ( AutoencoderConvolutionalNetworkEntityConfig ) _config;
        AutoencoderConvolutionalNetworkFactory cnf = new AutoencoderConvolutionalNetworkFactory();
        AutoencoderConvolutionalNetworkConfig cnc = (AutoencoderConvolutionalNetworkConfig)cnf.createConfig();
        cnc.setup(
                _om, _name, _r,
                config.learningRate,
                config.momentum,
                config.weightsStdDev,
                config.batchSize,
                config.layerSparsity,
                config.layerSparsityLifetime,
                config.layerSparsityOutput,
                config.nbrLayers,
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

        return cnc;
    }

    public void doUpdateSelf() {

        Data input = getData( DATA_INPUT );
        if( input == null ) {
            return; // can't update yet.
        }

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Algorithm specific parameters
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        AutoencoderConvolutionalNetworkEntityConfig config = ( AutoencoderConvolutionalNetworkEntityConfig ) _config;
        AutoencoderConvolutionalNetworkFactory cnf = new AutoencoderConvolutionalNetworkFactory();
        AutoencoderConvolutionalNetworkConfig cnc = createNetworkConfig();
        ConvolutionalNetwork cn = cnf.create();
        cn.setup( cnf, cnc );
        cn.setInput( input );
        cn.resize();

        copyDataFromPersistence( cn );

        // Update the region-layer, including optional reset and learning on/off switch
        if( config.reset ) {
            cn.reset();
        }

        cn._config.setLearn( config.learn );
        cn.update();

//        Data output = scn.getOutput(); // the potential max-pooling
        AutoencoderConvolutionalNetworkLayer cnl = (AutoencoderConvolutionalNetworkLayer)cn._layers.get( cn._layers.size() -1 );
        Data output = cnl._poolBest;
        Data inverted = cn.invert( output );

        setData( DATA_OUTPUT, output );
        setData( DATA_INVERSE, inverted );

        // Invert a selection of final-layer cells:
        Data outputSelection = new Data( output._dataSize );

        if( config.invertSelection.length() > 0 ) {
            try {
                String[] splitSelection = config.invertSelection.split( "," );
                for( String selection : splitSelection ) {
                    int n = Integer.valueOf( selection );
                    outputSelection._values[ n ] = 1f;
                }
            }
            catch( Exception e ) {
                outputSelection._values[ 0 ] = 1f;
            }

        }

        Data invertedSelection = cn.invert( outputSelection );
        setData( DATA_INVERSE_SELECTED, invertedSelection );

        // Save computed config properties
        copyDataToPersistence( cn );
    }

    protected void copyDataFromPersistence( ConvolutionalNetwork cn ) {

        int layers = cn._config.getNbrLayers();

        for( int layer = 0; layer < layers; ++layer ) {
            AutoencoderConvolutionalNetworkLayer nl = (AutoencoderConvolutionalNetworkLayer)cn._layers.get( layer );

            nl._convError = getDataLazyResize( DATA_LAYER_CONV_ERROR_ + layer, nl._convError._dataSize );
            nl._convBest = getDataLazyResize( DATA_LAYER_CONV_BEST_ + layer, nl._convBest._dataSize );
            nl._poolError = getDataLazyResize( DATA_LAYER_POOL_ERROR_ + layer, nl._poolError._dataSize );
            nl._poolBest = getDataLazyResize( DATA_LAYER_POOL_BEST_ + layer, nl._poolBest._dataSize );

            nl._classifier._cellWeights = getDataLazyResize( DATA_LAYER_WEIGHTS_ + layer, nl._classifier._cellWeights._dataSize );
            nl._classifier._cellBiases1 = getDataLazyResize( DATA_LAYER_BIASES1_ + layer, nl._classifier._cellBiases1._dataSize );
            nl._classifier._cellBiases2 = getDataLazyResize( DATA_LAYER_BIASES2_ + layer, nl._classifier._cellBiases2._dataSize );
            nl._classifier._cellWeightsVelocity = getDataLazyResize( DATA_LAYER_WEIGHTS_VELOCITY_ + layer, nl._classifier._cellWeightsVelocity._dataSize );
            nl._classifier._cellBiases1Velocity = getDataLazyResize( DATA_LAYER_BIASES1_VELOCITY_ + layer, nl._classifier._cellBiases1Velocity._dataSize );
            nl._classifier._cellBiases2Velocity = getDataLazyResize( DATA_LAYER_BIASES2_VELOCITY_ + layer, nl._classifier._cellBiases2Velocity._dataSize );

            nl._classifier._batchOutputOutput = getDataLazyResize( DATA_LAYER_BATCH_OUTPUT_OUTPUT_ + layer, nl._classifier._batchOutputOutput._dataSize );
            nl._classifier._batchOutputInput = getDataLazyResize( DATA_LAYER_BATCH_OUTPUT_INPUT_ + layer, nl._classifier._batchOutputInput._dataSize );
            nl._classifier._batchOutputInputLifetime = getDataLazyResize( DATA_LAYER_BATCH_OUTPUT_INPUT_LIFETIME_ + layer, nl._classifier._batchOutputInputLifetime._dataSize );
            nl._classifier._batchOutputErrors = getDataLazyResize( DATA_LAYER_BATCH_OUTPUT_ERRORS_ + layer, nl._classifier._batchOutputErrors._dataSize );
            nl._classifier._batchHiddenInput = getDataLazyResize( DATA_LAYER_BATCH_HIDDEN_INPUT_ + layer, nl._classifier._batchHiddenInput._dataSize );
            nl._classifier._batchHiddenWeightedSum = getDataLazyResize( DATA_LAYER_BATCH_HIDDEN_WEIGHTED_SUM_ + layer, nl._classifier._batchHiddenWeightedSum._dataSize );
            nl._classifier._batchHiddenErrors = getDataLazyResize( DATA_LAYER_BATCH_HIDDEN_ERRORS_ + layer, nl._classifier._batchHiddenErrors._dataSize );
        }
    }

    protected void copyDataToPersistence( ConvolutionalNetwork cn ) {

        int layers = cn._config.getNbrLayers();

        for( int layer = 0; layer < layers; ++layer ) {
            AutoencoderConvolutionalNetworkLayer nl = (AutoencoderConvolutionalNetworkLayer)cn._layers.get( layer );

            setData( DATA_LAYER_CONV_ERROR_ + layer, nl._convError );
            setData( DATA_LAYER_CONV_BEST_ + layer, nl._convBest );
            setData( DATA_LAYER_POOL_ERROR_ + layer, nl._poolError );
            setData( DATA_LAYER_POOL_BEST_ + layer, nl._poolBest );

            setData( DATA_LAYER_WEIGHTS_ + layer, nl._classifier._cellWeights );
            setData( DATA_LAYER_BIASES1_ + layer, nl._classifier._cellBiases1 );
            setData( DATA_LAYER_BIASES2_ + layer, nl._classifier._cellBiases2 );
            setData( DATA_LAYER_WEIGHTS_VELOCITY_ + layer, nl._classifier._cellWeightsVelocity );
            setData( DATA_LAYER_BIASES1_VELOCITY_ + layer, nl._classifier._cellBiases1Velocity );
            setData( DATA_LAYER_BIASES2_VELOCITY_ + layer, nl._classifier._cellBiases2Velocity );

            setData( DATA_LAYER_BATCH_OUTPUT_OUTPUT_ + layer, nl._classifier._batchOutputOutput );
            setData( DATA_LAYER_BATCH_OUTPUT_INPUT_ + layer, nl._classifier._batchOutputInput );
            setData( DATA_LAYER_BATCH_OUTPUT_INPUT_LIFETIME_ + layer, nl._classifier._batchOutputInputLifetime );
            setData( DATA_LAYER_BATCH_OUTPUT_ERRORS_ + layer, nl._classifier._batchOutputErrors );
            setData( DATA_LAYER_BATCH_HIDDEN_INPUT_ + layer, nl._classifier._batchHiddenInput );
            setData( DATA_LAYER_BATCH_HIDDEN_WEIGHTED_SUM_ + layer, nl._classifier._batchHiddenWeightedSum );
            setData( DATA_LAYER_BATCH_HIDDEN_ERRORS_ + layer, nl._classifier._batchHiddenErrors );
        }
    }
}