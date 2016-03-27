/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package io.agi.core.sdr;


import io.agi.core.data.Data;
import io.agi.core.orm.AbstractPair;

/**
 * Encodes to binary matrix by encoding values as subsets of bits.
 * 
 * The properties of SDRs are that similar input should have similar bits.
 * So there must be some redundancy - you can't have one bit per feature.
 * 
 * This encoder assumes that each scalar input is represented as an array
 * of bits. 
 * 
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
    
    public void setup( int bits, int density ) {
        _bits = bits;
        _density = density;
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
        return _bits - _density +1;
    }

    @Override
    public Data createEncodingOutput( Data encodingInput ) {
        int inputs = encodingInput.getSize();
//        int outputs = inputs * _bits;

        // Force the dimensions into a 2d (rectangular) shape.
        // Increase width to cope with extra bits.
        AbstractPair< Integer, Integer > size = Data.getSizeSquare( inputs );

        // Dimensions.DIMENSION_X, outputs
        int w = size._first * _bits;
        int h = size._second;

        Data encodingOutput = new Data( w, h );

        return encodingOutput;
    }
    
    @Override
    public void encode( Data encodingInput, Data encodingOutput ) {
        // Nupic:
        //Our scalar encoders use a few parameters to determine the encoding for a
        //given value. There is a minval and a maxval that determines the absolute 
        //value range. Then there is a number of bits, n, and a width, w. The encoding 
        //will have n total bits with w on bits (1's). Values are put into buckets. 
        //There are (n-w)+1 buckets that each represent an equally-sized value range 
        //between minval and maxval. The smallest bucket is represented with the first 
        //w bits on and the rest off. The next larger bucket is represented by 
        //shifting the on bits to the right by one position. In this way, adjacent 
        //buckets have the most overlap which helps to capture the semantics of scalar 
        //values.
        //
        //Example: A scalar encoder with a range from 0 to 100 with n=12 and w=3 will 
        //produce the following encoders:
        //
        //1 becomes 111000000000
        //7 becomes 111000000000
        //15 becomes 011100000000
        //36 becomes 000111000000
        //The first thing to note is that values that fall into the same bucket are 
        //represented identically as you can see with 1 and 7. For values that fall 
        //into separate buckets, however, the closest buckets share the most 
        //overlapping bits. For instance, here are two overlapping bits for 7 and 
        //15 but only one for 15 and 36. And there aren't any for 7 and 36.
        
        int inputs = encodingInput.getSize();
//        int outputs = inputs * _bits;
        int bins = getBins();
        
//        Dimensions d = Dimensions.create1D( Dimensions.DIMENSION_X, outputs );
//        encodingOutput.setDimensions( d );

        for( int i = 0; i < inputs; ++i ) {
            float inputValue = encodingInput._values[ i ];

            int offset = i * _bits;

            if( ( !_encodeZero ) && ( inputValue == 0.f ) ) {
                for( int b = 0; b < _bits; ++b ) {
                    encodingOutput._values[ offset +b ] = 0.f;
                }            
                continue;
            }
            
            // density = 2  bits = 5
            // 11000 01100 00110 00011 = 4 bins = 5-2 +1
            // 0 * 4 = 0,2
            // 0.24 * 4 = 0,2
            // 0.25 * 4 = 1,3 etc
            float bin = inputValue * (float)bins;
            int bin1 = (int)bin;
            int bin2 = bin1 + _density;
            
            for( int b = 0; b < _bits; ++b ) {
                float bitValue = 0.f;
                if( ( b < bin2 ) && ( b >= bin1 ) ) {
                    bitValue = 1.f;
                }
                encodingOutput._values[ offset +b ] = bitValue;
            }            
        }
    }
    
    @Override
    public void decode( Data decodingInput, Data decodingOutput ) {
        // computes the decode by averaging the values of the encoded bits.
        // e.g.
        // density = 2  bits = 5
        // 11000 01100 00110 00011 = 4 bins = 5-2 +1
        //
        // therefore 01000 decodes as:
        // 
        int inputs = decodingInput.getSize();
        int outputs = inputs / _bits;

        for( int i = 0; i < outputs; ++i ) {

            int offset = i * _bits;
            
            float sum = 0.f;
            float count = 0.f;
            
            for( int b = 0; b < _bits; ++b ) {
                float bitValue = decodingInput._values[ offset +b ];

                if( bitValue < 1.f ) {
                    continue;
                }

                float bin = b / _bits;

                sum += bin;
                count += 1.f;
            }

            float output = 0.f;
            if( count > 0.f ) {
                output = sum / count; // mean
            }
            
            decodingOutput._values[ i ] = output;
        }            
    }
    
}
