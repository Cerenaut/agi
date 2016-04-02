/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package io.agi.core.sdr;

import io.agi.core.data.Data;

/**
 * Encodes some input to a binary, sparse, distributed representation.
 * The input size can be modified but the output size is computed by the
 * encoder.
 * <p>
 * SDRs are inherently hard to decode (i.e. reverse-encoding process).
 * But it's useful to try for debugging.
 *
 * @author dave
 */
public interface SparseDistributedEncoder {

    /**
     * Setup the encoder.
     *
     * @param bits    The size (in bits) of the output of the encoder.
     * @param density The number of 'on' bits in the output SDR.
     */
    void setup( int bits, int density );

    /**
     * Create a suitably sized output structure for the given input.
     */
    Data createEncodingOutput( Data encodingInput );

    /**
     * Encode the input. Note both args are expected to be non null and nonzero
     * sized arrays.
     *
     * @param encodingInput
     * @param encodingOutput
     */
    void encode( Data encodingInput, Data encodingOutput );

    /**
     * @param decodingInput
     * @param decodingOutput
     */
    void decode( Data decodingInput, Data decodingOutput );

}
