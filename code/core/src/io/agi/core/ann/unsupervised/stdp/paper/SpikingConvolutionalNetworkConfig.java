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

import io.agi.core.ann.NetworkConfig;
import io.agi.core.ann.convolutional.ConvolutionalNetworkConfig;
import io.agi.core.orm.ObjectMap;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by dave on 1/05/17.
 */
public class SpikingConvolutionalNetworkConfig extends ConvolutionalNetworkConfig {

    //Synaptic weights of convolutional neurons initi-
    //ate with random values drown from a normal dis-
    //tribution with the mean of 0.8 and STD of 0.05

    public static final String KEY_KERNEL_WEIGHTS_STD_DEV = "kernel-weights-std-dev";
    public static final String KEY_KERNEL_WEIGHTS_MEAN = "kernel-weights-mean";
    public static final String KEY_KERNEL_WEIGHTS_LEARNING_RATE = "kernel-weights-learning-rate";

    public static final String KEY_LAYERS = "layers";
//    public static final String KEY_LAYER_KERNEL_SPIKE_FREQUENCY_LEARNING_RATE = "layer-kernel-spike-frequency-learning-rate";
//    public static final String KEY_LAYER_KERNEL_SPIKE_FREQUENCY_UPDATE_PERIOD = "layer-kernel-spike-frequency-update-period";
//    public static final String KEY_LAYER_KERNEL_SPIKE_FREQUENCY_TARGET = "layer-kernel-spike-frequency-target";

    public static final String KEY_LAYER_KERNEL_SPIKE_DENSITY_DEFAULT = "layer-kernel-spike-density-default";
    public static final String KEY_LAYER_KERNEL_SPIKE_DENSITY_TARGET = "layer-kernel-spike-density-target";
    public static final String KEY_LAYER_KERNEL_SPIKE_INTEGRATION_PERIOD = "layer-kernel-spike-integration-period";
    public static final String KEY_LAYER_KERNEL_SPIKE_UPDATE_PERIOD = "layer-kernel-spike-update-period";

    public static final String KEY_LAYER_CONV_SPIKE_DENSITY_DEFAULT = "layer-conv-spike-density-default";
    public static final String KEY_LAYER_CONV_SPIKE_DENSITY_TARGET = "layer-conv-spike-density-target";
    public static final String KEY_LAYER_CONV_SPIKE_INTEGRATION_PERIOD = "layer-conv-spike-integration-period";
    public static final String KEY_LAYER_CONV_SPIKE_UPDATE_PERIOD = "layer-conv-spike-update-period";
    
    public SpikingConvolutionalNetworkConfig() {

    }

    public void setup(
            ObjectMap om, String name, Random r,
            float kernelWeightsStdDev,
            float kernelWeightsMean,
            float kernelWeightsLearningRate,
            int nbrLayers,
//            String layerKernelSpikeFrequencyLearningRate,
//            String layerKernelSpikeFrequencyUpdatePeriod,
//            String layerKernelSpikeFrequencyTarget,
            String layerKernelSpikeDensityDefault,
            String layerKernelSpikeDensityTarget,
            String layerKernelSpikeIntegrationPeriod,
            String layerKernelSpikeUpdatePeriod,

            String layerConvSpikeDensityDefault,
            String layerConvSpikeDensityTarget,
            String layerConvSpikeIntegrationPeriod,
            String layerConvSpikeUpdatePeriod,

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

        setKernelWeightsStdDev( kernelWeightsStdDev );
        setKernelWeightsMean( kernelWeightsMean );
        setKernelWeightsLearningRate( kernelWeightsLearningRate );

//        setLayerValues( KEY_LAYER_KERNEL_SPIKE_FREQUENCY_TARGET, layerKernelSpikeFrequencyTarget );
//        setLayerValues( KEY_LAYER_KERNEL_SPIKE_FREQUENCY_LEARNING_RATE, layerKernelSpikeFrequencyLearningRate );
//        setLayerValues( KEY_LAYER_KERNEL_SPIKE_FREQUENCY_UPDATE_PERIOD, layerKernelSpikeFrequencyUpdatePeriod );

        setLayerValues( KEY_LAYER_KERNEL_SPIKE_DENSITY_DEFAULT, layerKernelSpikeDensityDefault );
        setLayerValues( KEY_LAYER_KERNEL_SPIKE_DENSITY_TARGET, layerKernelSpikeDensityTarget );
        setLayerValues( KEY_LAYER_KERNEL_SPIKE_INTEGRATION_PERIOD, layerKernelSpikeIntegrationPeriod );
        setLayerValues( KEY_LAYER_KERNEL_SPIKE_UPDATE_PERIOD, layerKernelSpikeUpdatePeriod );

        setLayerValues( KEY_LAYER_CONV_SPIKE_DENSITY_DEFAULT, layerConvSpikeDensityDefault );
        setLayerValues( KEY_LAYER_CONV_SPIKE_DENSITY_TARGET, layerConvSpikeDensityTarget );
        setLayerValues( KEY_LAYER_CONV_SPIKE_INTEGRATION_PERIOD, layerConvSpikeIntegrationPeriod );
        setLayerValues( KEY_LAYER_CONV_SPIKE_UPDATE_PERIOD, layerConvSpikeUpdatePeriod );
    }

    public void copyFrom( NetworkConfig nc, String name ) {
        super.copyFrom( nc, name );

        SpikingConvolutionalNetworkConfig c = ( SpikingConvolutionalNetworkConfig ) nc;

        setKernelWeightsStdDev( c.getKernelWeightsStdDev() );
        setKernelWeightsMean( c.getKernelWeightsMean() );
        setKernelWeightsLearningRate( c.getKernelWeightsLearningRate() );

//        setLayerValues( KEY_LAYER_KERNEL_SPIKE_FREQUENCY_TARGET, c.getLayerValues( KEY_LAYER_KERNEL_SPIKE_FREQUENCY_TARGET ) );
//        setLayerValues( KEY_LAYER_KERNEL_SPIKE_FREQUENCY_LEARNING_RATE, c.getLayerValues( KEY_LAYER_KERNEL_SPIKE_FREQUENCY_LEARNING_RATE ) );
//        setLayerValues( KEY_LAYER_KERNEL_SPIKE_FREQUENCY_UPDATE_PERIOD, c.getLayerValues( KEY_LAYER_KERNEL_SPIKE_FREQUENCY_UPDATE_PERIOD ) );

        setLayerValues( KEY_LAYER_KERNEL_SPIKE_DENSITY_DEFAULT, c.getLayerValues( KEY_LAYER_KERNEL_SPIKE_DENSITY_DEFAULT ) );
        setLayerValues( KEY_LAYER_KERNEL_SPIKE_DENSITY_TARGET, c.getLayerValues( KEY_LAYER_KERNEL_SPIKE_DENSITY_TARGET ) );
        setLayerValues( KEY_LAYER_KERNEL_SPIKE_INTEGRATION_PERIOD, c.getLayerValues( KEY_LAYER_KERNEL_SPIKE_INTEGRATION_PERIOD ) );
        setLayerValues( KEY_LAYER_KERNEL_SPIKE_UPDATE_PERIOD, c.getLayerValues( KEY_LAYER_KERNEL_SPIKE_UPDATE_PERIOD ) );

        setLayerValues( KEY_LAYER_CONV_SPIKE_DENSITY_DEFAULT, c.getLayerValues( KEY_LAYER_CONV_SPIKE_DENSITY_DEFAULT ) );
        setLayerValues( KEY_LAYER_CONV_SPIKE_DENSITY_TARGET, c.getLayerValues( KEY_LAYER_CONV_SPIKE_DENSITY_TARGET ) );
        setLayerValues( KEY_LAYER_CONV_SPIKE_INTEGRATION_PERIOD, c.getLayerValues( KEY_LAYER_CONV_SPIKE_INTEGRATION_PERIOD ) );
        setLayerValues( KEY_LAYER_CONV_SPIKE_UPDATE_PERIOD, c.getLayerValues( KEY_LAYER_CONV_SPIKE_UPDATE_PERIOD ) );
    }

    public float getKernelWeightsMean() {
        Float r = _om.getFloat( getKey( KEY_KERNEL_WEIGHTS_MEAN ) );
        return r.floatValue();
    }

    public void setKernelWeightsMean( float r ) {
        _om.put( getKey( KEY_KERNEL_WEIGHTS_MEAN ), r );
    }

    public float getKernelWeightsStdDev() {
        Float r = _om.getFloat( getKey( KEY_KERNEL_WEIGHTS_STD_DEV ) );
        return r.floatValue();
    }

    public void setKernelWeightsStdDev( float r ) {
        _om.put( getKey( KEY_KERNEL_WEIGHTS_STD_DEV ), r );
    }

    public float getKernelWeightsLearningRate() {
        Float r = _om.getFloat( getKey( KEY_KERNEL_WEIGHTS_LEARNING_RATE ) );
        return r.floatValue();
    }

    public void setKernelWeightsLearningRate( float r ) {
        _om.put( getKey( KEY_KERNEL_WEIGHTS_LEARNING_RATE ), r );
    }

}
