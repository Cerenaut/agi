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

package io.agi.core.alg;

import io.agi.core.ann.supervised.ActivationFunctionFactory;
import io.agi.core.ann.supervised.CostFunction;
import io.agi.core.ann.supervised.FeedForwardNetwork;
import io.agi.core.ann.supervised.FeedForwardNetworkConfig;
import io.agi.core.data.Data;
import io.agi.core.data.Ranking;
import io.agi.core.orm.ObjectMap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.TreeMap;

/**
 * Created by dave on 20/01/17.
 */
public class HebbianQuiltPredictor implements QuiltPredictorAlgorithm {

    public Data _weights;
    public HebbianQuiltPredictorConfig _c;

    public HebbianQuiltPredictor() {

    }

    /**
     * Configure the predictor.
     */
    public void setup( QuiltPredictorConfig config ) {

        HebbianQuiltPredictorConfig c = (HebbianQuiltPredictorConfig)config;
        _c = c;

        int inputs = _c.getPredictorInputs();
        int outputs = _c.getPredictorOutputs();

        _weights = new Data( outputs, inputs );
    }

    /**
     * Reset the learned weights of the predictor.
     */
    public void reset( int density, int regionWidth, int regionHeight, int columnWidth, int columnHeight ) {
        _weights.set( 1f );
    }

    /**
     * Train the predictor to predict the output given the input. Input and output will be a real unit valued vector.
     *
     * @param inputOld
     * @param density A hint about the number of bits in the output
     * @param outputNew
     */
    public void train( Data inputOld, Data outputNew, int density, int regionWidth, int regionHeight, int columnWidth, int columnHeight ) {

//        HashSet< Integer > activeOld = inputOld.indicesMoreThan( 0.5f );
//        HashSet< Integer > activeNew = inputOld.indicesMoreThan( 0.5f );
//
//        System.err.println( "OLD: " );
//        for( Integer i : activeOld ) {
//            System.err.print( i + "," );
//        }
//        System.err.println( "NEW: " );
//        for( Integer i : activeNew ) {
//            System.err.print( i + "," );
//        }
//        System.err.println();
        float learningRate = _c.getPredictorLearningRate();

        int cells = regionWidth * regionHeight;
        int inputs = inputOld.getSize();

        HashSet< Integer > activeInput = inputOld.indicesMoreThan( 0.5f );

        // only train for active input - the ones that are nonzero
        for( Integer i : activeInput ) {
            for( int c = 0; c < cells; ++c ) {
                int offset = c * inputs + i;
                float weightOld = _weights._values[ offset ];
                float postSynaptic = outputNew._values[ c ];

                // linear
//                float delta = -learningRate;
//                if( postSynaptic > 0.5f ) {
//                    delta = learningRate;
//                }
//                float weightNew = weightOld + delta;
//                weightNew = Math.min( 1f, Math.max( 0f, weightNew ) );

                // Oja's rule:
                // w' = w + a * y * ( x - w*y )
                // y = output 0 or 1
                // x = input 0 or 1
                // x=1, y=0
                // w' = w + a * 0 * ( 1 - w*0 ) no output, no learning
                // x=1, y=1, w=0
                // w' = w + a * 1 * ( 1 - 0*1 )  = +1
                // x=1, y=1, w=0.5
                // w' = w + a * 1 * ( 1 - 0.5*1 ) = +0.5

                // derived from Oja's rule. But not the same
                float delta = 0f;
                if( postSynaptic > 0.5f ) {
                    delta = learningRate * ( 1f - weightOld ); // if w=0, d=a. if w=1, d=0
                }
                else { // decrease association
                    delta = - learningRate * ( weightOld ); // if w=0, d=0. if w=1, d=a
                }

                float weightNew = weightOld + delta;

                _weights._values[ offset ] = weightNew;
            }
        }

    }

    /**
     * Provide an output predicting the next state of the system. Output should be unit values, with threshold 0.5 used
     * to determine whether a cell was "predicted" or not.
     *
     * @param inputNew
     * @param density A hint about the number of bits in the output
     * @param outputPrediction
     */
    public void predict( Data inputNew, Data outputPrediction, Data outputPredictionSparse, int density, int regionWidth, int regionHeight, int columnWidth, int columnHeight ) {

        int cells = regionWidth * regionHeight;
        int inputs = inputNew.getSize();

        HashSet< Integer > activeInput = inputNew.indicesMoreThan( 0.5f );

        int nbrActiveInput = activeInput.size();

        for( int c = 0; c < cells; ++c ) {
            double sum = 0.0;
            for( Integer i : activeInput ) {
                int offset = c * inputs + i;
                float weight = _weights._values[ offset ];

                float sumWeight = 0f;

                for( int c2 = 0; c2 < cells; ++c2 ) {
                    int offset2 = c2 * inputs + i;
                    float weight2 = _weights._values[ offset2 ];
                    sumWeight += weight2;
                }

                if( sumWeight > 0f ) {
                    weight /= sumWeight;
                }
//                double log = Math.log( weight );
//                sum += log; // product of weights with less precision loss
                sum += weight;
            }

//            double exp = Math.exp( sum );
//            double norm = 0.0;
//            if( exp > 0.0 ) {
//                norm = exp / (double)nbrActiveInput;
//            }
//            float p = (float)norm;
            float denominator = (float)Math.max( 1, nbrActiveInput );
            float p = (float)sum / denominator;
            outputPrediction._values[ c ] = p;
        }

        // note pass unclipped prediction
        FeedForwardNetworkQuiltPredictor.SparsenPrediction( outputPrediction, outputPredictionSparse, density, regionWidth, regionHeight, columnWidth, columnHeight );

        outputPrediction.clipRange( 0f, 1f ); // as NN may go wildly beyond that
    }

}
