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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package io.agi.core.sdr;


import io.agi.core.data.Data;
import io.agi.core.data.Data2d;
import io.agi.core.orm.AbstractPair;

import java.awt.*;

/**
 * Encodes to binary matrix by encoding values as subsets of bits.
 * <p/>
 * The properties of SDRs are that similar input should have similar bits.
 * So there must be some redundancy - you can't have one bit per feature.
 * <p/>
 * This encoder assumes that each scalar input is represented as an array
 * of bits.
 * <p/>
 * This is an adaptation of the Nupic ScalarEncoder
 * https://github.com/numenta/nupic/wiki/Encoders
 *
 * @author dave
 */
public class ScalarEncoder implements SparseDistributedEncoder {

    public boolean _encodeZero = false;
    public int _bits = 0;
    public int _density = 0;

    public ScalarEncoder() {

    }

    public void setup( int bits, int density, boolean encodeZero ) {
        _bits = bits;
        _density = density;
        _encodeZero = encodeZero;
    }

    public int getBins() {
        // density = 2  bits = 4
        // 1100 0110 0011 = 3 bins = 4-2 +1
        // density = 2  bits = 5
        // 11000 01100 00110 00011 = 4 bins = 5-2 +1
        // density = 3  bits = 5
        // 11100 01110 00111 = 3 bins = 5-3 +1 = 3
        // density = 3  bits = 8
        // 1110 0000 
        // 0111 0000 
        // 0011 1000 
        // 0001 1100
        // 0000 1110
        // 0000 0111 = 6 bins = 8-3 +1 = 6
        return _bits - _density + 1;
    }

    @Override
    public Data createEncodingOutput( Data encodingInput ) {
        int inputs = encodingInput.getSize();

        // Force the dimensions into a 2d (rectangular) shape.
        // Increase width to cope with extra bits.
        Point size = Data2d.getSize( encodingInput );

        // Dimensions.DIMENSION_X, outputs
        int w = size.x * _bits;
        int h = size.x;

        Data encodingOutput = new Data( w, h );

        return encodingOutput;
    }

    @Override
    public Data createDecodingOutput( Data decodingInput ) {
        int inputs = decodingInput.getSize();

        // Assuming the same type of encoder did the encoding, we can reverse the geometric changes.
        // However, we assume the input was 2d
        Point size = Data2d.getSize( decodingInput );

        // Dimensions.DIMENSION_X, outputs
        int w = size.x / _bits;
        int h = size.y;

        Data decodingOutput = new Data( w, h );
        return decodingOutput;
    }

    @Override
    public void encode( Data encodingInput, Data encodingOutput ) {
        int inputs = encodingInput.getSize();
        int bins = getBins();

        for( int i = 0; i < inputs; ++i ) {
            float inputValue = encodingInput._values[ i ];

            int offset = i * _bits;

            if( ( !_encodeZero ) && ( inputValue == 0.f ) ) {
                for( int b = 0; b < _bits; ++b ) {
                    encodingOutput._values[ offset + b ] = 0.f;
                }
                continue;
            }

            // density = 2 bits = 5
            // possible patterns are:
            // 11000
            // 01100
            // 00110
            // 00011 = 4 possible bins = 5-2 +1 = 4
            //
            // density = 3 bits = 8
            // 1110 0000
            // 0111 0000
            // 0011 1000
            // 0001 1100
            // 0000 1110
            // 0000 0111 = 6 bins = 8-3 +1 = 6

            // 0     0.25  0.5   0.75
            // 0    * 4 = [0,2)
            // 0.24 * 4 = [0,2)
            // 0.25 * 4 = [1,3)
            // 0.49 * 4 = [1,3)
            // 0.5  * 4 = [2,4)
            // 0.75 * 4 = [3,5)
            // 0.99 * 4 = [3,5)
            // 1    * 4 = [4,5)  <-- bad, so limit it
            float bin = inputValue * ( float ) bins;
            int bin1 = Math.min( bins-1, (int)bin ); // captured case where value is 1.
            int bin2 = bin1 + _density; // excluding this value

            for( int b = 0; b < _bits; ++b ) {
                float bitValue = 0.f;
                if( ( b < bin2 ) && ( b >= bin1 ) ) {
                    bitValue = 1.f;
                }
                encodingOutput._values[ offset + b ] = bitValue;
            }
        }
    }

    @Override
    public void decode( Data decodingInput, Data decodingOutput ) {
        // computes the decode by taking the mean position of the encoded '1' bits.
        // e.g.
        // density = 2  bits = 5
        // 11000 01100 00110 00011 = 4 bins = 5-2 +1
        //
        //Example: A scalar encoder with a range from 0 to 100 with n=12 and w=3 will
        //produce the following encoders:
        //
        //1 becomes 111000000000
        //7 becomes 111000000000
        //15 becomes 011100000000
        //36 becomes 000111000000
        int inputs = decodingInput.getSize();
        int outputs = inputs / _bits;
        int bins = getBins();

        float reciprocalBits = 1.f / (float)_bits;

        for( int i = 0; i < outputs; ++i ) {

            int offset = i * _bits;

            float sum = 0.f;
            float count = 0.f;

            for( int b = 0; b < _bits; ++b ) {
                float bitValue = decodingInput._values[ offset + b ];

                if( bitValue < 1.f ) {
                    continue;
                }

                // density = 2 bits = 5
                // possible patterns are:                      bit / (bits-1)
                // 11000                                 10000 0/4 = 0.0
                // 01100                                 01000 1/4 = 0.25
                // 00110                                 00100 2/4 = 0.5
                // 00011 = 4 possible bins = 5-2 +1 = 4  00010 3/4 = 0.75
                //                                       00001 4/4 = 1.0
                // density = 3 bits = 8
                // 1110 0000                             0/7 = 0.0
                // 0111 0000                             1/7 = 0.14
                // 0011 1000                             2/7 = 0.28
                // 0001 1100                             3/7 = 0.42
                // 0000 1110                             4/7 = 0.57
                // 0000 0111 = 6 bins = 8-3 +1 = 6       5/7 = 0.71
                //                                       6/7 = 0.85
                //                                       7/7 = 1.0
                float bitWeight = (float)b / (float)( _bits -1 ); // so between zero and 1 inclusive

                sum += bitWeight;
                count += 1.f;
            }

            // e.g. 11000     = 0.0 + 0.25 / 2 = 0.125
            // e.g. 00011     = 0.75 + 1.0 / 2 = 0.875
            // e.g. 1110 0000 = 0.0 + 0.14 + 0.28 / 3 = 0.14   0.14 would've been encoded as 0.14*(6) = 0.84 = bin 0,1,2
            float meanBit = 0.f;
            if( count > 0.f ) {
                meanBit = sum / count; // mean
            }

            float output = meanBit * reciprocalBits;
            decodingOutput._values[ i ] = output;
        }
    }

}
