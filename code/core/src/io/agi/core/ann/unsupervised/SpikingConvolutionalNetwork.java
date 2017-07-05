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

package io.agi.core.ann.unsupervised;

import io.agi.core.data.Data;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by dave on 5/05/17.
 */
public class SpikingConvolutionalNetwork {

    public SpikingConvolutionalNetworkConfig _config;

    public ArrayList< SpikingConvolutionalNetworkLayer > _layers = new ArrayList< SpikingConvolutionalNetworkLayer >();

    protected Data _input;

    public SpikingConvolutionalNetwork() {

    }

    public void setup( SpikingConvolutionalNetworkConfig config ) {
        _config = config;

        int layers = _config.getNbrLayers();

        for( int layer = 0; layer < layers; ++layer ) {

            float integrationThreshold = _config.getIntegrationThreshold( layer );
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
            int layerTrainingAgeStart = _config.getLayerTrainingAge( layer );
            int layerTrainingAgeEnd = Integer.MAX_VALUE;
            if( layer < (layers -1) ) {
                layerTrainingAgeEnd = _config.getLayerTrainingAge( layer +1 ); // ends when next layer start, or when learn switched off
            }

            SpikingConvolutionalNetworkLayerConfig scnlc = new SpikingConvolutionalNetworkLayerConfig();
            scnlc.setup(
                _config._r,
                layerTrainingAgeStart, layerTrainingAgeEnd,
                _config.getWeightsStdDev(), _config.getWeightsMean(),
                _config.getLearningRatePos(), _config.getLearningRateNeg(),
                integrationThreshold,
                inputPadding, inputStride,
                layerWidth, layerHeight, layerDepth,
                fieldWidth, fieldHeight, fieldDepth,
                poolingWidth, poolingHeight );

            SpikingConvolutionalNetworkLayer scnl = new SpikingConvolutionalNetworkLayer();
            scnl.setup( scnlc, layer );
            _layers.add( scnl );
        }
    }

    public void setInput( Data input ) {
        _input = input;
    }

    public Data getInput() {
        return _input;
    }

    public Data getOutput() {
        int layers = _config.getNbrLayers();
        int outputLayer = layers -1;
        SpikingConvolutionalNetworkLayer scnl =  _layers.get( outputLayer );
        Data output = scnl.getOutput();
        return output;
    }

    public void resize() {
        int layers = _config.getNbrLayers();

        for( int layer = 0; layer < layers; ++layer ) {
            Data input = null;
            if( layer == 0 ) {
                input = getInput();
            }
            else {
                SpikingConvolutionalNetworkLayer scnl =  _layers.get( layer -1 );
                input = scnl.getOutput();
            }

            SpikingConvolutionalNetworkLayer scnl =  _layers.get( layer );
            scnl.resize( input );
        }
    }

    public void reset() {
        int layers = _config.getNbrLayers();

        for( int layer = 0; layer < layers; ++layer ) {
            SpikingConvolutionalNetworkLayer scnl =  _layers.get( layer );
            scnl.reset();
        }

        _config.setAge( 0 );
    }

    public void clear() {
        int layers = _config.getNbrLayers();

        for( int layer = 0; layer < layers; ++layer ) {
            SpikingConvolutionalNetworkLayer scnl =  _layers.get( layer );
            scnl.clear();
        }
    }

    public void update() {
        boolean learn = _config.getLearn();
        int layers = _config.getNbrLayers();
        int age = _config.getAge();

        for( int layer = 0; layer < layers; ++layer ) {
            Data input = null;
            if( layer == 0 ) {
                input = getInput();
            }
            else {
                SpikingConvolutionalNetworkLayer scnl =  _layers.get( layer -1 );
                input = scnl.getOutput();
            }

            boolean maxPooling = false;
            if( layer == (layers-1) ) {
                maxPooling = true;
            }

            SpikingConvolutionalNetworkLayer scnl =  _layers.get( layer );

            // Implement greedy layerwise training schedule.
            boolean isTraining = scnl._config.isTrainingAge( age );
            boolean train = isTraining & learn;

            scnl.setInput( input );
            scnl.update( train, maxPooling );
        }

        _config.setAge( age +1 );
    }

    public Data invert( Data output ) {
        int layers = _config.getNbrLayers();

        Data poolInput = output;
        for( int layer = layers -1; layer >= 0; --layer ) {
            SpikingConvolutionalNetworkLayer scnl = _layers.get( layer );

            Data inverted = scnl.invert( poolInput );
            poolInput = inverted;
        }

        return poolInput;
    }

}
