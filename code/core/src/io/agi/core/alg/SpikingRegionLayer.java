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

import io.agi.core.ann.unsupervised.SpikingNeuralNetwork;
import io.agi.core.ann.unsupervised.SpikingNeuralNetworkConfig;
import io.agi.core.data.Data;

import java.util.Random;

/**
 * Wraps a Spiking Neural Network and maps inputs and outputs into RegionLayer format.
 *
 * Created by dave on 20/07/16.
 */
public class SpikingRegionLayer {

    // Data structures
    public Data _input1;
    public Data _input2;
    public Data _output; // the spiking excitatory cells
    // TODO add another output - the "predicted" cells which are ?? the ones inhibited by active inhib. cells? But not active now?
    // HMM have to think about how to access the "predictions"

    public SpikingNeuralNetwork _snn;

    public SpikingRegionLayer() {

    }

    public void setup( SpikingNeuralNetworkConfig c ) {
        _snn = new SpikingNeuralNetwork();
        _snn.setup( c );

        // the output of the region-layer is the set of active excitatory cells.
        int excitatoryCells = c.getExcitatorySize();

        _output = new Data( excitatoryCells ); // 1d vector
    }

    public void reset() {
        _snn.reset();
    }

    public void update() {
        _snn.update( _input1, _input2 );

        int excitatoryOffset = _snn._c.getCellTypeInputOffset( SpikingNeuralNetworkConfig.CELL_TYPE_EXCITATORY );
        int excitatorySize = _snn._c.getExcitatorySize();

        int offsetThis = 0;
        int offsetThat = excitatoryOffset;
        int range = excitatorySize;

        _output.copyRange( _snn._inputSpikesNew, offsetThis, offsetThat, range );
    }
}
