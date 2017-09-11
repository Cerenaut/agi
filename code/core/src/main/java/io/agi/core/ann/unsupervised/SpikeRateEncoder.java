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
import io.agi.core.data.DataSize;

/**
 * Converts intensities into spike rates.
 *
 * Created by dave on 4/05/17.
 */
public class SpikeRateEncoder {

//    The first layer of the network uses Difference of Gaussians (DoG) filters to detect contrasts in
//    the input image. It encodes the strength of these contrasts in the latencies of its output spikes (the
//    higher the contrast, the shorter the latency)
//
//    the more strongly a cell is activated
//            (higher contrast), the earlier it fires. In other word,
//    the order of the spikes depends on the order of the
//    contrasts. This rank-order coding is shown to be
//    efficient for obtaining V1 like edge detectors [44]
//    as well as complex visual features
//
//    DoG cells are retinotopically organized in two
//    ON-center and OFF-center maps which are respec-
//    tively sensitive to positive and negative contrasts.
//    A DoG cell is allowed to fire if its activation isabove a certain threshold. Note that this scheme
//    grantees that at most one of the two cells (posi-
//            tive or negative) can fire in each location. As men-
//    tioned above, the firing time of a DoG cell is in-
//    versely proportional to its activation value.
//
//    https://en.wikipedia.org/wiki/Difference_of_Gaussians


    public Data _input;
    public Data _accumulated;
    public Data _spikes;

    public float _threshold = 0f;

    public SpikeRateEncoder() {

    }

    public void setup( DataSize ds, float threshold ) {
        _input = new Data( ds );
        _accumulated = new Data( ds );
        _spikes = new Data( ds );
        _threshold = threshold;
    }

    public void reset() {
        clear();
    }

    public void clear() {
        // allow integrated activity to decay away
        _accumulated.set( 0f );
        _spikes.set( 0f );
    }

    public void update() {
        _spikes.set( 0f );

        int size = _input.getSize();

        for( int i = 0; i < size; ++i ) {

            float inputValue = _input._values[ i ];
            float accOld = _accumulated._values[ i ];
            float accNew = accOld + inputValue;
            float spike = 0f;

            if( accNew > _threshold ) {
                spike = 1f;
                accNew = 0f;
            }

            _accumulated._values[ i ] = accNew;
            _spikes._values[ i ] = spike;
        }
    }

}

