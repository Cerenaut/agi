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

package io.agi.core.alg;

import io.agi.core.ann.unsupervised.GrowingNeuralGas;
import io.agi.core.ann.unsupervised.GrowingNeuralGasConfig;
import io.agi.core.orm.ObjectMap;

import java.util.Random;

/**
 * Created by dave on 14/05/16.
 */
public class RegionLayerFactory {

    public RegionLayerConfig _rc;

    public RegionLayerFactory() {

    }

    public RegionLayer create(
            ObjectMap om,
            String regionName,
            Random random,

            // Feedforward input size
            int inputWidth,
            int inputHeight,

            // Feedback input size
            int feedbackWidthCells,
            int feedbackHeightCells,

            // Algorithm specific parameters
            // Region size
            int regionWidthColumns,
            int regionHeightColumns,

            // Column Sizing
            int classifierWidthCells,
            int classifierHeightCells,
            int classifierDepthCells,

            // Organizer training
            float receptiveFieldsTrainingSamples,
            int classifiersPerBit,
            float organizerLearningRate,
            float organizerLearningRateNeighbours,
            float organizerNoiseMagnitude,
            int organizerEdgeMaxAge,
            float organizerStressLearningRate,
            float organizerStressThreshold,
            int organizerGrowthInterval,

            // Classifier training
            float classifierLearningRate,
            float classifierLearningRateNeighbours,
            float classifierNoiseMagnitude,
            int classifierEdgeMaxAge,
            float classifierStressLearningRate,
            float classifierStressThreshold,
            int classifierGrowthInterval,

            // Predictor
            float predictorLearningRate ) {

        RegionLayerConfig rc = new RegionLayerConfig();

        // Computed or fixed parameters
        int classifierInputs = inputWidth * inputHeight;
        int feedbackAreaCells = feedbackWidthCells * feedbackHeightCells;
//        int regionAreaCells = regionWidthColumns * classifierWidthCells * regionHeightColumns * classifierHeightCells;
//        int predictorInputs = regionAreaCells + feedbackAreaCells;
//        int predictorOutputs = regionAreaCells;
//        int predictorLayers = Region.PREDICTOR_LAYERS;
        int organizerInputs = Region.RECEPTIVE_FIELD_DIMENSIONS;

        GrowingNeuralGasConfig organizerConfig = new GrowingNeuralGasConfig();
        organizerConfig.setup(
                om, RegionLayerConfig.SUFFIX_ORGANIZER, random, // temp name
                organizerInputs, regionWidthColumns, regionHeightColumns,
                organizerLearningRate, organizerLearningRateNeighbours, organizerNoiseMagnitude,
                organizerEdgeMaxAge, organizerStressLearningRate, organizerStressThreshold, organizerGrowthInterval );

        GrowingNeuralGasConfig classifierConfig = new GrowingNeuralGasConfig();
        classifierConfig.setup(
                om, RegionLayerConfig.SUFFIX_CLASSIFIER, random, // temp name
                classifierInputs, classifierWidthCells, classifierHeightCells,
                classifierLearningRate, classifierLearningRateNeighbours, classifierNoiseMagnitude,
                classifierEdgeMaxAge, classifierStressLearningRate, classifierStressThreshold, classifierGrowthInterval );

        rc.setup( om, regionName, random, organizerConfig, classifierConfig, inputWidth, inputHeight, feedbackAreaCells, predictorLearningRate, receptiveFieldsTrainingSamples, classifiersPerBit, classifierDepthCells );

        this.setup( rc );

        RegionLayer regionLayer = this.createRegionLayer( regionName );
        return regionLayer;
    }

    public void setup( RegionLayerConfig rc ) {
        _rc = rc;
    }

    public RegionLayerConfig getRegionLayerConfig() {
        return _rc;
    }

    public RegionLayer createRegionLayer( String name ) {
        RegionLayer r = new RegionLayer( name, _rc._om );
        r.setup( this, _rc );
        return r;
    }

    public GrowingNeuralGas createOrganizer( RegionLayer r ) {

        String name = r.getKey( RegionLayerConfig.SUFFIX_ORGANIZER );
        GrowingNeuralGasConfig c = new GrowingNeuralGasConfig();
        c.copyFrom( _rc._organizerConfig, name );

        GrowingNeuralGas gng = new GrowingNeuralGas( c._name, c._om );
        gng.setup( c );

        return gng;
    }

    public GrowingNeuralGas createClassifier( RegionLayer r ) {

        String name = r.getKey( RegionLayerConfig.SUFFIX_CLASSIFIER );
        GrowingNeuralGasConfig c = new GrowingNeuralGasConfig();
        c.copyFrom( _rc._classifierConfig, name );

        GrowingNeuralGas gng = new GrowingNeuralGas( c._name, c._om );
        gng.setup( c );

        return gng;
    }
}
