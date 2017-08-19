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

package io.agi.core.ann.convolutional;

import io.agi.core.data.Data;

import java.util.ArrayList;

/**
 * Created by dave on 11/08/17.
 */
public class ConvolutionalNetwork {

    public ConvolutionalNetworkFactory _factory;
    public ConvolutionalNetworkConfig _config;

    public ArrayList< ConvolutionalNetworkLayer > _layers = new ArrayList< ConvolutionalNetworkLayer >();

    protected Data _input;

    public ConvolutionalNetwork() {

    }

    public void setup( ConvolutionalNetworkFactory factory, ConvolutionalNetworkConfig config ) {
        _factory = factory;
        _config = config;

        int layers = _config.getNbrLayers();

        for( int layer = 0; layer < layers; ++layer ) {

            ConvolutionalNetworkLayerConfig cnlc = _factory.createLayerConfig();
            cnlc.setup( _config, layer );

            ConvolutionalNetworkLayer cnl = _factory.createLayer();
            cnl.setup( cnlc );
            _layers.add( cnl );
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
        ConvolutionalNetworkLayer cnl = _layers.get( outputLayer );
        Data output = cnl.getOutput();
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
                ConvolutionalNetworkLayer cnl = _layers.get( layer -1 );
                input = cnl.getOutput();
            }

            ConvolutionalNetworkLayer cnl = _layers.get( layer );
            cnl.resize( input );
        }
    }

    public void reset() {
        int layers = _config.getNbrLayers();

        for( int layer = 0; layer < layers; ++layer ) {
            ConvolutionalNetworkLayer cnl =  _layers.get( layer );
            cnl.reset();
        }
    }

    public void clear() {
        int layers = _config.getNbrLayers();

        for( int layer = 0; layer < layers; ++layer ) {
            ConvolutionalNetworkLayer cnl =  _layers.get( layer );
            cnl.clear();
        }
    }

    public void update() {
        boolean learn = _config.getLearn();
        int layers = _config.getNbrLayers();

        for( int layer = 0; layer < layers; ++layer ) {
            Data input = null;
            if( layer == 0 ) {
                input = getInput();
            }
            else {
                ConvolutionalNetworkLayer scnl = _layers.get( layer -1 );
                input = scnl.getOutput();
            }

            ConvolutionalNetworkLayer scnl = _layers.get( layer );

            scnl.setInput( input );
            scnl.update( learn );
        }
    }

    public Data invert( Data output ) {
        int layers = _config.getNbrLayers();

        Data poolInput = output;
        for( int layer = layers -1; layer >= 0; --layer ) {
            ConvolutionalNetworkLayer cnl = _layers.get( layer );

            Data inverted = cnl.invert( poolInput );
            poolInput = inverted;
        }

        return poolInput;
    }
}
