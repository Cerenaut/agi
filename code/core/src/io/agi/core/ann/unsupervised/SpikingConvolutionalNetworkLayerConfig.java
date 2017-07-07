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

import io.agi.core.data.ConvolutionData3d;
import io.agi.core.data.Int3d;
import io.agi.core.math.Useful;

import java.util.Random;

/**
 * Created by dave on 1/05/17.
 */
public class SpikingConvolutionalNetworkLayerConfig {

    // http://cs231n.github.io/convolutional-networks/
    // 3d input (e.g. x,y,{rgb})
    // 3d output (w,h,d)
    // depth, stride and zero-padding
    // depth = z, a param
    // stride = stride of the filter
    // zero-padding (like an offset)
    // filters always extend through the full depth of the input volume. For example, if the input is [32x32x3] then
    // doing 1x1 convolutions would effectively be doing 3-dimensional dot products (since the input depth is 3 channels).

    //Synaptic weights of convolutional neurons initi-
    //ate with random values drown from a normal dis-
    //tribution with the mean of 0.8 and STD of 0.05

    public Random _r;
    public int _trainingAgeStart = 0;
    public int _trainingAgeEnd   = 0;

    public float _weightStdDev;
    public float _weightsMean;

    public float _learningRatePos;
    public float _learningRateNeg;

    public float _integrationThreshold;

    public int _inputPadding;
    public int _inputStride;

    public int _width;
    public int _height;
    public int _depth;

    public int _fieldWidth;
    public int _fieldHeight;
    public int _fieldDepth;

    public int _poolingWidth;
    public int _poolingHeight;

    public SpikingConvolutionalNetworkLayerConfig() {

    }

    public void setup(
            Random r,
            int trainingAgeStart,
            int trainingAgeEnd,
            float weightStdDev,
            float weightsMean,
            float learningRatePos,
            float learningRateNeg,
            float integrationThreshold,
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
        _trainingAgeStart = trainingAgeStart;
        _trainingAgeEnd = trainingAgeEnd;
        _weightStdDev = weightStdDev;
        _weightsMean = weightsMean;
        _learningRatePos = learningRatePos;
        _learningRateNeg = learningRateNeg;
        _integrationThreshold = integrationThreshold;
        _inputPadding = inputPadding;
        _inputStride = inputStride;
        _width = width;
        _height = height;
        _depth = depth;
        _fieldWidth = fieldWidth;
        _fieldHeight = fieldHeight;
        _fieldDepth = fieldDepth;
        _poolingWidth = poolingWidth;
        _poolingHeight = poolingHeight;
    }

    public boolean isTrainingAge( int age ) {
        if( age < _trainingAgeStart ) {
            return false;
        }
        if( age >= _trainingAgeEnd ) {
            return false;
        }
        return true;
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
