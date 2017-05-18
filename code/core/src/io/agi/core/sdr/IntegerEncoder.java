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

package io.agi.core.sdr;

import io.agi.core.data.Data;
import io.agi.core.data.DataSize;

/**
 * Creates a matrix with max-min cols and rows rows.
 * The rows allow repeats of the bits to attain a specific density.
 * The cols represent the current integer value as a 1 bit.
 * Created by dave on 3/11/16.
 */
public class IntegerEncoder implements SparseDistributedEncoder {

    public int _min = 1;
    public int _max = 1;
    public int _rows = 10;

    public IntegerEncoder() {

    }

    public void setup( int min, int max, int rows ) {
        _min = min;
        _max = max;
        _rows = rows;
    }

    /**
     * Create a suitably sized output structure for the given input.
     */
    public Data createEncodingOutput( Data encodingInput ) {
        if( encodingInput == null ) {
            return null;
        }

        int numbers = encodingInput.getSize(); // vector
        int bits = _max-_min +1; // e.g. 5-0 = 5
        int w = numbers * bits;
        int h = _rows;

        Data encodingOutput = new Data( w, h );

        return encodingOutput;
    }

    /**
     * Create suitably sized output structure for given input.
     *
     * @param decodingInput
     * @return
     */
    public Data createDecodingOutput( Data encodingInput, Data decodingInput ) {
        int numbers = encodingInput.getSize(); // vector
        return new Data( DataSize.create( numbers ) );
    }

    /**
     * Encode the input. Note both args are expected to be non null and nonzero
     * sized arrays.
     *
     * @param encodingInput
     * @param encodingOutput
     */
    public void encode( Data encodingInput, Data encodingOutput ) {

        if( encodingInput == null ) {
            return;
        }

        encodingOutput.set( 0.f );

        int numbers = encodingInput.getSize(); // vector
        int bits = _max-_min +1; // e.g. 5-0 = 5
        int stride = numbers * bits;

        for( int n = 0; n < numbers; ++n ) {

            int number = ( int ) encodingInput._values[ n ];
            int bit = number - _min;
            bit = Math.min( _max, bit );
            bit = Math.max( _min, bit );

            int x = bits * n + bit;

            for( int y = 0; y < _rows; ++y ) {
                int offset = y * stride + x;
                encodingOutput._values[ offset ] = 1.f;
            }

        }
    }

    /**
     * @param decodingInput
     * @param decodingOutput
     */
    public void decode( Data decodingInput, Data decodingOutput ) {

        int numbers = decodingOutput.getSize(); // vector
        int bits = _max-_min +1; // e.g. 5-0 = 5
        int stride = numbers * bits;

        for( int n = 0; n < numbers; ++n ) {

            float maxSum = 0f;
            int maxBit = 0;

            for( int bit = 0; bit < bits; ++bit ) {
                int x = bits * n + bit;

                float sum = 0f;

                for( int y = 0; y < _rows; ++y ) {
                    int offset = y * stride + x;
                    float input = decodingInput._values[ offset ];
                    sum += input;
                }

                if( sum > maxSum ) {
                    maxSum = sum;
                    maxBit = bit;
                }
            }

            float number = (float)( _min + maxBit );

            decodingOutput._values[ n ] = number;
        }

    }
}