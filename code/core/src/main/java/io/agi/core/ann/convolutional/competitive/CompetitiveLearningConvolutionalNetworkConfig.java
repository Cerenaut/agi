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

package io.agi.core.ann.convolutional.competitive;

import io.agi.core.ann.NetworkConfig;
import io.agi.core.ann.convolutional.ConvolutionalNetworkConfig;
import io.agi.core.ann.unsupervised.GrowingNeuralGasConfig;
import io.agi.core.orm.ObjectMap;

import java.util.Random;

/**
 * Created by dave on 11/08/17.
 */
public class CompetitiveLearningConvolutionalNetworkConfig extends ConvolutionalNetworkConfig {

    GrowingNeuralGasConfig _classifierConfig;

    public CompetitiveLearningConvolutionalNetworkConfig() {

    }

    public void setup(
            ObjectMap om, String name, Random r,

            // These don't vary by layer
            float learningRate,
            float learningRateNeighbours,
            int edgeMaxAge,
            float stressLearningRate,
            float stressSplitLearningRate,
            float stressThreshold,
            float utilityLearningRate,
            float utilityThreshold,
            int growthInterval,

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

        String classifierName = name + "-classifier";

        // These parameters vary according to the geometry of the layers:
        int classifierInputs = 0;
        int classifierWidth = 0;
        int classifierHeight = 0;

        // These parameters are disused
        float noiseMagnitude = 0f;
        float denoisePercentage = 0f;

        _classifierConfig = new GrowingNeuralGasConfig();
        _classifierConfig.setup(
                om, classifierName, r,
                classifierInputs,
                classifierWidth,
                classifierHeight,
                learningRate,
                learningRateNeighbours,
                noiseMagnitude,
                edgeMaxAge,
                stressLearningRate,
                stressSplitLearningRate,
                stressThreshold,
                utilityLearningRate,
                utilityThreshold,
                growthInterval,
                denoisePercentage );
    }

    public void copyFrom( NetworkConfig nc, String name ) {
        super.copyFrom( nc, name );

        CompetitiveLearningConvolutionalNetworkConfig c = ( CompetitiveLearningConvolutionalNetworkConfig ) nc;

        _classifierConfig.copyFrom( c._classifierConfig, _classifierConfig._name );
    }

}
