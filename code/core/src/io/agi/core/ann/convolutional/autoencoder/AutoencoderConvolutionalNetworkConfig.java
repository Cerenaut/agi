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

import io.agi.core.ann.NetworkConfig;
import io.agi.core.ann.convolutional.ConvolutionalNetworkConfig;
import io.agi.core.ann.unsupervised.LifetimeSparseAutoencoderConfig;
import io.agi.core.orm.ObjectMap;

import java.util.Random;

/**
 * Created by dave on 19/08/17.
 */
public class AutoencoderConvolutionalNetworkConfig extends ConvolutionalNetworkConfig {

    public static final String KEY_LAYER_SPARSITY = "layer-sparsity";
    public static final String KEY_LAYER_SPARSITY_LIFETIME = "layer-sparsity-lifetime";

    LifetimeSparseAutoencoderConfig _classifierConfig;

    public AutoencoderConvolutionalNetworkConfig() {

    }

    public void setup(
            ObjectMap om, String name, Random r,

            // These don't vary by layer
//            int sparsity,
            float learningRate,
            float momentum,
            float weightsStdDev,
            int batchSize,

            String layerSparsity,
            String layerSparsityLifetime,

            int nbrLayers,

            String layerInputPadding,
            String layerInputStride,
            String layerWidth,
            String layerHeight,
            String layerDepth,
            String layerfieldWidth,
            String layerfieldHeight,
            String layerfieldDepth,
            String layerPoolingWidth,
            String layerPoolingHeight
    ) {
        super.setup( om, name, r, nbrLayers, layerInputPadding, layerInputStride, layerWidth, layerHeight, layerDepth, layerfieldWidth, layerfieldHeight, layerfieldDepth, layerPoolingWidth, layerPoolingHeight );

        setLayerValues( KEY_LAYER_SPARSITY, layerSparsity );
        setLayerValues( KEY_LAYER_SPARSITY_LIFETIME, layerSparsityLifetime );

        String classifierName = name + "-classifier";

        // These parameters vary according to the geometry of the layers:
        int classifierInputs = 0;
        int classifierWidth = 0;
        int classifierHeight = 0;

        int sparsity = 0; // per layer
        int sparsityLifetime = 0; // per layer
        int batchCount = 0; // reset to 0

        _classifierConfig = new LifetimeSparseAutoencoderConfig();
        _classifierConfig.setup(
                om, classifierName, r,
                classifierInputs,
                classifierWidth,
                classifierHeight,
                learningRate,
                momentum,
                sparsity,
                sparsityLifetime,
                weightsStdDev,
                batchCount,
                batchSize );
    }

    public void copyFrom( NetworkConfig nc, String name ) {
        super.copyFrom( nc, name );

        AutoencoderConvolutionalNetworkConfig c = ( AutoencoderConvolutionalNetworkConfig ) nc;

        setLayerValues( KEY_LAYER_SPARSITY, c.getLayerValues( KEY_LAYER_SPARSITY ) );
        setLayerValues( KEY_LAYER_SPARSITY_LIFETIME, c.getLayerValues( KEY_LAYER_SPARSITY_LIFETIME ) );

        _classifierConfig.copyFrom( c._classifierConfig, _classifierConfig._name );
    }

}
