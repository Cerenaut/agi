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

package io.agi.core.ann.convolutional.autoencoder;

import io.agi.core.ann.convolutional.ConvolutionalNetworkConfig;
import io.agi.core.ann.convolutional.ConvolutionalNetworkLayerConfig;
import io.agi.core.ann.unsupervised.LifetimeSparseAutoencoderConfig;

/**
 * Created by dave on 19/08/17.
 */
public class AutoencoderConvolutionalNetworkLayerConfig extends ConvolutionalNetworkLayerConfig {

    public LifetimeSparseAutoencoderConfig _classifierConfig;

    public AutoencoderConvolutionalNetworkLayerConfig() {

    }

    public void setup( ConvolutionalNetworkConfig config, int layer ) {
        super.setup( config, layer );
        AutoencoderConvolutionalNetworkConfig acnc = ( AutoencoderConvolutionalNetworkConfig ) config;

        String classifierName = acnc._classifierConfig._name + "-" + layer;

        _classifierConfig = new LifetimeSparseAutoencoderConfig();
        _classifierConfig.copyFrom( acnc._classifierConfig, classifierName );

        // set layer-specific geometry:
        int inputs = _fieldWidth * _fieldHeight * _fieldDepth;

        _classifierConfig.setNbrInputs( inputs );
        _classifierConfig.setWidthCells( _depth );
        _classifierConfig.setHeightCells( 1 );

        // adapt time-scales to layer area:
        float areaCells = _width * _height; // this is how many times it will learn during one step
//        float invAreaCells = 1f / areaCells;

        // LearningRate
        // TODO adapt learning rate by area? There is more than 1... but number of updates is related to cell count?
//        float learningRate = _classifierConfig.getLearningRate();
//        float areaLearningRate = learningRate * invAreaCells;
//        _classifierConfig.setLearningRate( areaLearningRate );

        // adapt the batch size by area
        int batchSize = acnc._classifierConfig.getBatchSize();
        int layerBatchSize = batchSize * (int)areaCells;
        _classifierConfig.setBatchSize( layerBatchSize );

        // set sparsity per layer:
        int sparsity = acnc.getLayerValueInteger( AutoencoderConvolutionalNetworkConfig.KEY_LAYER_SPARSITY, 0 );
        _classifierConfig.setSparsity( sparsity );

        // set lifetime sparsity to account for the fact that batch includes many updates in different positions, and batch is correspondingly larger
//        int sparsityLifetime = acnc.getLayerValueInteger( AutoencoderConvolutionalNetworkConfig.KEY_LAYER_SPARSITY_LIFETIME, 0 );
//        int layerSparsityLifetime = sparsityLifetime * (int)areaCells;
//        _classifierConfig.setSparsityLifetime( layerSparsityLifetime );
    }
}