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
import io.agi.core.data.Data2d;
import io.agi.core.data.DataSize;

import java.awt.*;
import java.util.HashSet;

/**
 * Treats the uncoded input as a vector of integer numbers. Each number is turned into part of a sparse matrix.
 * The matrix has 10 bits per digit of the number. The number of digits to be encoded, and the number of numbers, must
 * be specified in advance so the size of the data structures can be computed.
 *
 * Created by dave on 1/05/16.
 */
public class NumberEncoder implements SparseDistributedEncoder {

    public int _digits = 1;
    public int _numbers = 1;
    public int _base = 10;

    public NumberEncoder() {

    }

    public void setup( int digits, int numbers ) {
        _digits = digits;
        _numbers = numbers;
    }

    /**
     * Create a suitably sized output structure for the given input.
     */
    public Data createEncodingOutput( Data encodingInput ) {
        if( encodingInput == null ) {
            return null;
        }

        int numbers = encodingInput.getSize(); // vector

        int base = _base;
        int bits = numbers * _digits * base; // e.g. 2 numbers, 2 digits each = 2* 2* 10 = 4 values, each 10 bits, = 40 bits.

        Point size = Data2d.getSizeSquare( bits ); // may be some padding to make it square

        int w = size.x;
        int h = size.y;

        Data encodingOutput = new Data( w, h );

        return encodingOutput;
    }

    /**
     * Create suitably sized output structure for given input.
     * @param decodingInput
     * @return
     */
    public Data createDecodingOutput( Data decodingInput ) {
        return new Data( DataSize.create( _numbers ) );
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

        HashSet< Integer > activeBits = new HashSet< Integer >();

        int numbers = encodingInput.getSize(); // vector
        int base = _base;

        for( int n = 0; n < numbers; ++n ) {

            int number = (int)encodingInput._values[ n ];

            // least significant digit is first
            for( int digit = 0; digit < _digits; ++digit ) {
                int value = number % base; // the remainder

                // now encode value.
                int bit = n * _digits * base
                          +    digit  * base
                          +             value;
                activeBits.add( bit );

                number = number / base; // next digit
            }
        }

        encodingOutput.set( 0.f );

        for( Integer bit : activeBits ) {
            encodingOutput._values[ bit ] = 1.f;
        }
    }

    /**
     * @param decodingInput
     * @param decodingOutput
     */
    public void decode( Data decodingInput, Data decodingOutput ) {

        int numbers = decodingOutput.getSize(); // vector
        for( int n = 0; n < numbers; ++n ) {

            int scale = 1;
            int base = _base;
            int number = 0;

            // least significant digit is first
            for( int digit = 0; digit < _digits; ++digit ) {

                int valueMax = 0;
                float maxWeight = 0.f;

                for( int value = 0; value < base; ++value ) {

                    int offset = n * _digits * base
                               +      digit  * base
                               +               value;

                    float weight = decodingInput._values[ offset ];
                    if( weight > maxWeight ) {
                        maxWeight = value;
                        valueMax = value;
                    }
                }

                int digitValue = valueMax * scale;
                number = number + digitValue;
                scale = scale * base; // next digit
            }

            decodingOutput._values[ n ] = number;
        }

    }

}
