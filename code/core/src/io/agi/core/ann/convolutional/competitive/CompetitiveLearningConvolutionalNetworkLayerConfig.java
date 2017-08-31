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
import io.agi.core.ann.convolutional.ConvolutionalNetworkLayerConfig;
import io.agi.core.ann.unsupervised.GrowingNeuralGasConfig;
import io.agi.core.ann.unsupervised.stdp.paper.SpikingConvolutionalNetworkConfig;
import io.agi.core.orm.ObjectMap;

import java.util.Random;

/**
 * Created by dave on 11/08/17.
 */
public class CompetitiveLearningConvolutionalNetworkLayerConfig extends ConvolutionalNetworkLayerConfig {

    public GrowingNeuralGasConfig _classifierConfig;

    public CompetitiveLearningConvolutionalNetworkLayerConfig() {

    }

    public void setup( ConvolutionalNetworkConfig config, int layer ) {
        super.setup( config, layer );
        CompetitiveLearningConvolutionalNetworkConfig clcnc = ( CompetitiveLearningConvolutionalNetworkConfig ) config;

        String classifierName = clcnc._classifierConfig._name + "-" + layer;

        _classifierConfig = new GrowingNeuralGasConfig();
        _classifierConfig.copyFrom( clcnc._classifierConfig, classifierName );

        // set layer-specific geometry:
        int inputs = _fieldWidth * _fieldHeight * _fieldDepth;

        _classifierConfig.setNbrInputs( inputs );
        _classifierConfig.setWidthCells( _depth );
        _classifierConfig.setHeightCells( 1 );

        // adapt time-scales to layer area:
        float areaCells = _width * _height; // this is how many times it will learn during one step
        float invAreaCells = 1f / areaCells;

        // LearningRate
        float learningRate = _classifierConfig.getLearningRate();
        float areaLearningRate = learningRate * invAreaCells;
        _classifierConfig.setLearningRate( areaLearningRate );

        // LearningRateNeighbours
        float learningRateNeighbours = _classifierConfig.getLearningRateNeighbours();
        float areaLearningRateNeighbours = learningRateNeighbours * invAreaCells;
        _classifierConfig.setLearningRateNeighbours( areaLearningRateNeighbours );

        // stressLearningRate
        float stressLearningRate = _classifierConfig.getStressLearningRate();
        float areaStressLearningRate = stressLearningRate * invAreaCells;
        _classifierConfig.setStressLearningRate( areaStressLearningRate );

        // stressSplitLearningRate
        float stressSplitLearningRate = _classifierConfig.getStressSplitLearningRate();
        float areaStressSplitLearningRate = stressSplitLearningRate * invAreaCells;
        _classifierConfig.setStressSplitLearningRate( areaStressSplitLearningRate );

        // stressThreshold
//        float stressThreshold = _classifierConfig.getStressThreshold();
//        float areaStressThreshold = stressThreshold * invAreaCells;
//        _classifierConfig.setStressThreshold( areaStressThreshold );

        // utilityLearningRate
        float utilityLearningRate = _classifierConfig.getUtilityLearningRate();
        float areaUtilityLearningRate = utilityLearningRate * invAreaCells;
        _classifierConfig.setUtilityLearningRate( areaUtilityLearningRate );

        // utilityThreshold
//        float utilityThreshold = _classifierConfig.getUtilityThreshold();
//        float areaUtilityThreshold = utilityThreshold * invAreaCells;
//        _classifierConfig.setUtilityThreshold( areaUtilityThreshold );

        // Edge max age
        int edgeMaxAge = _classifierConfig.getEdgeMaxAge();
        int areaEdgeMaxAge = edgeMaxAge * (int)areaCells;
        _classifierConfig.setEdgeMaxAge( areaEdgeMaxAge );

        // Growth interval
        int growthInterval = _classifierConfig.getGrowthInterval();
        int areaGrowthInterval = growthInterval * (int)areaCells;
        _classifierConfig.setGrowthInterval( areaGrowthInterval );
    }

}
