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

import io.agi.core.data.DataSize;
import io.agi.core.data.Int3d;
import io.agi.core.math.Useful;

import java.util.Random;

/**
 * Created by dave on 11/08/17.
 */
public class ConvolutionalNetworkLayerConfig {

    public Random _r;

    public int _layer;

    // Input Dimensions
    public int _inputPadding;
    public int _inputStride;

    // Layer Dimensions
    public int _width;
    public int _height;
    public int _depth;

    public int _poolingWidth;
    public int _poolingHeight;

    // Receptive Field Dimensions
    public int _fieldWidth;
    public int _fieldHeight;
    public int _fieldDepth;

    public ConvolutionalNetworkLayerConfig() {

    }

    public void setup( ConvolutionalNetworkConfig config, int layer ) {

        int inputPadding = config.getLayerInputPadding( layer );
        int inputStride = config.getLayerInputStride( layer );
        int layerWidth = config.getLayerWidth( layer );
        int layerHeight = config.getLayerHeight( layer );
        int layerDepth = config.getLayerDepth( layer );
        int fieldWidth = config.getLayerFieldWidth( layer );
        int fieldHeight = config.getLayerFieldHeight( layer );
        int fieldDepth = config.getLayerFieldDepth( layer );
        int poolingWidth = config.getLayerPoolingWidth( layer );
        int poolingHeight = config.getLayerPoolingHeight( layer );

        setup(
            config._r, layer,
            inputPadding, inputStride,
            layerWidth, layerHeight, layerDepth,
            fieldWidth, fieldHeight, fieldDepth,
            poolingWidth, poolingHeight );
    }

    public void setup(
            Random r,
            int layer,
            int inputPadding,
            int inputStride,
            int width,
            int height,
            int depth,
            int fieldWidth,
            int fieldHeight,
            int fieldDepth,
            int poolingWidth,
            int poolingHeight ) {

        _r = r;
        _layer = layer;

        _inputPadding = inputPadding;
        _inputStride = inputStride;

        _width = width;
        _height = height;
        _depth = depth;
        _poolingWidth = poolingWidth;
        _poolingHeight = poolingHeight;

        _fieldWidth = fieldWidth;
        _fieldHeight = fieldHeight;
        _fieldDepth = fieldDepth;
    }

    public DataSize getConvDataSize() {
        DataSize convDataSize = DataSize.create( _width, _height, _depth );
        return convDataSize;
    }

    public DataSize getPoolDataSize() {
        int pw = getPooledWidth();
        int ph = getPooledHeight();
        int pd = getPooledDepth();
        DataSize poolDataSize = DataSize.create( pw, ph, pd );
        return poolDataSize;
    }

    public int getPooledWidth() {
        int pooledWidth = Useful.DivideRoundUp( _width, _poolingWidth );
        return pooledWidth;
    }

    public int getPooledHeight() {
        int pooledHeight = Useful.DivideRoundUp( _height, _poolingHeight );
        return pooledHeight;
    }

    public int getPooledDepth() {
        return _depth;
    }

    public Int3d getConvSize() {
        Int3d i3d = new Int3d( _width, _height, _depth );
        return i3d;
    }

    public Int3d getPoolSize() {
        int ow = Useful.DivideRoundUp( _width, _poolingWidth );
        int oh = Useful.DivideRoundUp( _height, _poolingHeight );
        int od = _depth;
        Int3d i3d = new Int3d( ow, oh, od );

        return i3d;
    }
}
