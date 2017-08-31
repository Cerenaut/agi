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

package io.agi.core.ann.unsupervised.stdp.paper;

import io.agi.core.ann.convolutional.ConvolutionalNetwork;
import io.agi.core.data.Data;

import java.util.ArrayList;

/**
 * Created by dave on 5/05/17.
 */
public class SpikingConvolutionalNetwork extends ConvolutionalNetwork {

    public SpikingConvolutionalNetwork() {

    }

/*    public void setup( SpikingConvolutionalNetworkConfig config ) {
        _config = config;

        int layers = _config.getNbrLayers();

        for( int layer = 0; layer < layers; ++layer ) {

//            float kernelSpikeFrequencyLearningRate = _config.getLayerValueFloat( config.KEY_LAYER_KERNEL_SPIKE_FREQUENCY_LEARNING_RATE, layer );
//            int kernelSpikeFrequencyUpdatePeriod = _config.getLayerValueInteger( config.KEY_LAYER_KERNEL_SPIKE_FREQUENCY_UPDATE_PERIOD, layer );
//            float kernelSpikeFrequencyTarget = _config.getLayerValueFloat( config.KEY_LAYER_KERNEL_SPIKE_FREQUENCY_TARGET, layer );

            float kernelSpikeControllerDefault = _config.getLayerValueFloat( config.KEY_LAYER_KERNEL_SPIKE_DENSITY_DEFAULT, layer );
            float kernelSpikeControllerTarget = _config.getLayerValueFloat( config.KEY_LAYER_KERNEL_SPIKE_DENSITY_TARGET, layer );
            int kernelSpikeControllerIntegrationPeriod = _config.getLayerValueInteger( config.KEY_LAYER_KERNEL_SPIKE_INTEGRATION_PERIOD, layer );
            int kernelSpikeControllerUpdatePeriod = _config.getLayerValueInteger( config.KEY_LAYER_KERNEL_SPIKE_UPDATE_PERIOD, layer );

            float convSpikeControllerDefault = _config.getLayerValueFloat( config.KEY_LAYER_CONV_SPIKE_DENSITY_DEFAULT, layer );
            float convSpikeControllerTarget = _config.getLayerValueFloat( config.KEY_LAYER_CONV_SPIKE_DENSITY_TARGET, layer );
            int convSpikeControllerIntegrationPeriod = _config.getLayerValueInteger( config.KEY_LAYER_CONV_SPIKE_INTEGRATION_PERIOD, layer );
            int convSpikeControllerUpdatePeriod = _config.getLayerValueInteger( config.KEY_LAYER_CONV_SPIKE_UPDATE_PERIOD, layer );

            int inputPadding = _config.getLayerInputPadding( layer );
            int inputStride = _config.getLayerInputStride( layer );
            int layerWidth = _config.getLayerWidth( layer );
            int layerHeight = _config.getLayerHeight( layer );
            int layerDepth = _config.getLayerDepth( layer );
            int fieldWidth = _config.getLayerFieldWidth( layer );
            int fieldHeight = _config.getLayerFieldHeight( layer );
            int fieldDepth = _config.getLayerFieldDepth( layer );
            int poolingWidth = _config.getLayerPoolingWidth( layer );
            int poolingHeight = _config.getLayerPoolingHeight( layer );

            SpikingConvolutionalNetworkLayerConfig scnlc = new SpikingConvolutionalNetworkLayerConfig();
            scnlc.setup(
                _config._r,
                _config.getKernelWeightsStdDev(),
                _config.getKernelWeightsMean(),
                _config.getKernelWeightsLearningRate(),
//                kernelSpikeFrequencyLearningRate,
//                kernelSpikeFrequencyUpdatePeriod,
//                kernelSpikeFrequencyTarget,

                kernelSpikeControllerDefault,
                kernelSpikeControllerTarget,
                kernelSpikeControllerIntegrationPeriod,
                kernelSpikeControllerUpdatePeriod,

                convSpikeControllerDefault,
                convSpikeControllerTarget,
                convSpikeControllerIntegrationPeriod,
                convSpikeControllerUpdatePeriod,

                inputPadding, inputStride,
                layerWidth, layerHeight, layerDepth,
                fieldWidth, fieldHeight, fieldDepth,
                poolingWidth, poolingHeight );

            SpikingConvolutionalNetworkLayer scnl = new SpikingConvolutionalNetworkLayer();
            scnl.setup( scnlc, layer );
            _layers.add( scnl );
        }
    }*/

}
