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

import io.agi.core.ann.supervised.ActivationFunctionFactory;
import io.agi.core.ann.supervised.FeedForwardNetwork;
import io.agi.core.ann.supervised.FeedForwardNetworkConfig;
import io.agi.core.ann.supervised.LossFunction;
import io.agi.core.ann.unsupervised.GrowingNeuralGas;
import io.agi.core.ann.unsupervised.GrowingNeuralGasConfig;
import io.agi.core.orm.ObjectMap;

import java.util.Random;

/**
 * Factory for all the Region objects - Regions, Columns and internal parts (Classifier, Predictor, Organizer)
 * Allows each component to be replaced with a derived & modified version.
 * <p/>
 * Created by dave on 28/12/15.
 */
public class RegionFactory {

    public RegionConfig _rc;

    public RegionFactory() {

    }

    public Region create(
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

            // Organizer training
            float receptiveFieldsTrainingSamples,
            int receptiveFieldSize,
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
            float predictorHiddenLayerScaleFactor,
            float predictorLearningRate,
            float predictorRegularization ) {

        RegionConfig rc = new RegionConfig();

        // Computed or fixed parameters
        int classifierInputs = inputWidth * inputHeight;
        int feedbackAreaCells = feedbackWidthCells * feedbackHeightCells;
        int regionAreaCells = regionWidthColumns * classifierWidthCells * regionHeightColumns * classifierHeightCells;
        int predictorInputs = regionAreaCells + feedbackAreaCells;
        int predictorOutputs = regionAreaCells;
        int predictorLayers = Region.PREDICTOR_LAYERS;
        int organizerInputs = Region.RECEPTIVE_FIELD_DIMENSIONS;
        int hiddenLayerSize = ( int ) ( ( float ) regionAreaCells * predictorHiddenLayerScaleFactor );
        String predictorLayerSizes = String.valueOf( hiddenLayerSize ); // 6 * 6 * 1.something
        String predictorLossFunction = LossFunction.CROSS_ENTROPY;
        String predictorActivationFunction = ActivationFunctionFactory.LOG_SIGMOID;

        GrowingNeuralGasConfig organizerConfig = new GrowingNeuralGasConfig();
        organizerConfig.setup(
                om, RegionConfig.SUFFIX_ORGANIZER, random, // temp name
                organizerInputs, regionWidthColumns, regionHeightColumns,
                organizerLearningRate, organizerLearningRateNeighbours, organizerNoiseMagnitude,
                organizerEdgeMaxAge, organizerStressLearningRate, organizerStressThreshold, organizerGrowthInterval );

        GrowingNeuralGasConfig classifierConfig = new GrowingNeuralGasConfig();
        classifierConfig.setup(
                om, RegionConfig.SUFFIX_CLASSIFIER, random, // temp name
                classifierInputs, classifierWidthCells, classifierHeightCells,
                classifierLearningRate, classifierLearningRateNeighbours, classifierNoiseMagnitude,
                classifierEdgeMaxAge, classifierStressLearningRate, classifierStressThreshold, classifierGrowthInterval );

        FeedForwardNetworkConfig predictorConfig = new FeedForwardNetworkConfig();
        predictorConfig.setup(
                om, RegionConfig.SUFFIX_PREDICTOR, random, // temp name
                predictorLossFunction, predictorActivationFunction,
                predictorInputs, predictorOutputs,
                predictorLayers, predictorLayerSizes,
                predictorRegularization, predictorLearningRate );

        rc.setup( om, regionName, random, organizerConfig, classifierConfig, predictorConfig, inputWidth, inputHeight, feedbackAreaCells, receptiveFieldsTrainingSamples, receptiveFieldSize );
        this.setup( rc );

        Region region = this.createRegion( regionName );
        return region;
    }

    public void setup( RegionConfig rc ) {
        _rc = rc;
    }

    public RegionConfig getRegionConfig() {
        return _rc;
    }

    public Region createRegion( String name ) {
        Region r = new Region( name, _rc._om );
        r.setup( this );
        return r;
    }

    public GrowingNeuralGas createOrganizer( Region r ) {

        String name = r.getKey( RegionConfig.SUFFIX_ORGANIZER );
        GrowingNeuralGasConfig c = new GrowingNeuralGasConfig();
        c.copyFrom( _rc._organizerConfig, name );

        GrowingNeuralGas gng = new GrowingNeuralGas( c._name, c._om );
        gng.setup( c );

        return gng;
    }

    public GrowingNeuralGas createClassifier( Region r ) {

        String name = r.getKey( RegionConfig.SUFFIX_CLASSIFIER );
        GrowingNeuralGasConfig c = new GrowingNeuralGasConfig();
        c.copyFrom( _rc._classifierConfig, name );

        GrowingNeuralGas gng = new GrowingNeuralGas( c._name, c._om );
        gng.setup( c );

        return gng;
    }

    public FeedForwardNetwork createPredictor( Region r ) {

        String name = r.getKey( RegionConfig.SUFFIX_PREDICTOR );
        FeedForwardNetworkConfig c = new FeedForwardNetworkConfig();
        c.copyFrom( _rc._predictorConfig, name );

        ActivationFunctionFactory aff = new ActivationFunctionFactory();

        FeedForwardNetwork ffn = new FeedForwardNetwork( c._name, c._om );
        ffn.setup( c, aff );

        // Twin layer test:
        String lossFunction = c.getLossFunction();
        String activationFunction = c.getActivationFunction();
        float learningRate = c.getLearningRate();
        int inputs = c.getNbrInputs();
        int outputs = c.getNbrOutputs();
        String layerSizes = c.getLayerSizes();
        int hidden = Integer.valueOf( layerSizes );

        // hardcoded for 2 layers
        ffn.setupLayer( _rc._r, 0, inputs, hidden, learningRate, activationFunction );

        if( lossFunction.equals( LossFunction.LOG_LIKELIHOOD ) ) {
            ffn.setupLayer( _rc._r, 1, hidden, outputs, learningRate, ActivationFunctionFactory.SOFTMAX );
        } else {
            ffn.setupLayer( _rc._r, 1, hidden, outputs, learningRate, activationFunction );
        }

        return ffn;
    }
}
