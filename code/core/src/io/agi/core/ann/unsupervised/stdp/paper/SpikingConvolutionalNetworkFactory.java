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

import io.agi.core.ann.convolutional.*;

/**
 * Created by dave on 12/08/17.
 */
public class SpikingConvolutionalNetworkFactory implements ConvolutionalNetworkFactory {

    public ConvolutionalNetwork create() {
        return new ConvolutionalNetwork();
    }

    public ConvolutionalNetworkConfig createConfig() {
        return new SpikingConvolutionalNetworkConfig();
    }

    public ConvolutionalNetworkLayer createLayer() {
        return new SpikingConvolutionalNetworkLayer();
    }

    public ConvolutionalNetworkLayerConfig createLayerConfig() {
        return new SpikingConvolutionalNetworkLayerConfig();
    }

}