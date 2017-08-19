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

import io.agi.core.ann.convolutional.ConvolutionalNetworkConfig;
import io.agi.core.ann.convolutional.ConvolutionalNetworkLayerConfig;
import io.agi.core.data.ConvolutionData3d;
import io.agi.core.data.Int3d;
import io.agi.core.math.Useful;

import java.util.Random;

/**
 * Created by dave on 1/05/17.
 */
public class SpikingConvolutionalNetworkLayerConfig extends ConvolutionalNetworkLayerConfig {

    //Synaptic weights of convolutional neurons initi-
    //ate with random values drown from a normal dis-
    //tribution with the mean of 0.8 and STD of 0.05

//    public Random _r;

    // Kernel parameters
    public float _kernelWeightStdDev;
    public float _kernelWeightsMean;
    public float _kernelWeightsLearningRate;
//    public float _kernelSpikeFrequencyLearningRate;
//    public int _kernelFrequencyUpdatePeriod;
//    public float _kernelSpikeFrequencyTarget;

    // Kernel Homeostasis parameters
    public float _kernelSpikeControllerDefault = 1f;
    public float _kernelSpikeControllerTarget = 0f;
    public int _kernelSpikeControllerIntegrationPeriod = 0;
    public int _kernelSpikeControllerUpdatePeriod = 0;     // period over which the convolutional spikes are averaged. used for calculating an average to be used for controller.

    // Convolutional Homeostasis parameters
    public float _convSpikeControllerDefault = 1f;
    public float _convSpikeControllerTarget = 0f;
    public int _convSpikeControllerIntegrationPeriod = 0;
    public int _convSpikeControllerUpdatePeriod = 0;     // period over which the convolutional spikes are averaged. used for calculating an average to be used for controller.

    public SpikingConvolutionalNetworkLayerConfig() {

    }

    public void setup( ConvolutionalNetworkConfig config, int layer ) {
        super.setup( config, layer );
        SpikingConvolutionalNetworkConfig scnc = (SpikingConvolutionalNetworkConfig)config;
//            float kernelSpikeFrequencyLearningRate = scnc.getLayerValueFloat( config.KEY_LAYER_KERNEL_SPIKE_FREQUENCY_LEARNING_RATE, layer );
//            int kernelSpikeFrequencyUpdatePeriod = scnc.getLayerValueInteger( config.KEY_LAYER_KERNEL_SPIKE_FREQUENCY_UPDATE_PERIOD, layer );
//            float kernelSpikeFrequencyTarget = scnc.getLayerValueFloat( config.KEY_LAYER_KERNEL_SPIKE_FREQUENCY_TARGET, layer );

        float kernelSpikeControllerDefault =  scnc.getLayerValueFloat( scnc.KEY_LAYER_KERNEL_SPIKE_DENSITY_DEFAULT, layer );
        float kernelSpikeControllerTarget =  scnc.getLayerValueFloat( scnc.KEY_LAYER_KERNEL_SPIKE_DENSITY_TARGET, layer );
        int kernelSpikeControllerIntegrationPeriod = scnc.getLayerValueInteger( scnc.KEY_LAYER_KERNEL_SPIKE_INTEGRATION_PERIOD, layer );
        int kernelSpikeControllerUpdatePeriod =  scnc.getLayerValueInteger( scnc.KEY_LAYER_KERNEL_SPIKE_UPDATE_PERIOD, layer );

        float convSpikeControllerDefault =  config.getLayerValueFloat( scnc.KEY_LAYER_CONV_SPIKE_DENSITY_DEFAULT, layer );
        float convSpikeControllerTarget =  config.getLayerValueFloat( scnc.KEY_LAYER_CONV_SPIKE_DENSITY_TARGET, layer );
        int convSpikeControllerIntegrationPeriod =  config.getLayerValueInteger( scnc.KEY_LAYER_CONV_SPIKE_INTEGRATION_PERIOD, layer );
        int convSpikeControllerUpdatePeriod =  config.getLayerValueInteger( scnc.KEY_LAYER_CONV_SPIKE_UPDATE_PERIOD, layer );

        _kernelWeightStdDev = scnc.getKernelWeightsStdDev();
        _kernelWeightsMean = scnc.getKernelWeightsMean();
        _kernelWeightsLearningRate = scnc.getKernelWeightsLearningRate();
//        _kernelSpikeFrequencyLearningRate = kernelSpikeFrequencyLearningRate;
//        _kernelFrequencyUpdatePeriod = kernelFrequencyUpdatePeriod;
//        _kernelSpikeFrequencyTarget = kernelSpikeFrequencyTarget;

        _kernelSpikeControllerDefault = kernelSpikeControllerDefault;
        _kernelSpikeControllerTarget = kernelSpikeControllerTarget;
        _kernelSpikeControllerIntegrationPeriod = kernelSpikeControllerIntegrationPeriod;
        _kernelSpikeControllerUpdatePeriod = kernelSpikeControllerUpdatePeriod;

        _convSpikeControllerDefault = convSpikeControllerDefault;
        _convSpikeControllerTarget = convSpikeControllerTarget;
        _convSpikeControllerIntegrationPeriod = convSpikeControllerIntegrationPeriod;
        _convSpikeControllerUpdatePeriod = convSpikeControllerUpdatePeriod;
    }

}
