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
import io.agi.core.ann.supervised.FeedForwardNetwork;
import io.agi.core.ann.supervised.FeedForwardNetworkConfig;
import io.agi.core.ann.supervised.CostFunction;
import io.agi.core.data.Data;
import io.agi.core.data.Data2d;
import io.agi.core.data.Ranking;
import io.agi.core.orm.ObjectMap;

import java.util.ArrayList;
import java.util.Random;
import java.util.TreeMap;

/**
 * Created by dave on 20/01/17.
 */
public class PyramidRegionLayerPredictor {

    public FeedForwardNetwork _ffn;

    public PyramidRegionLayerPredictor() {

    }

    /**
     * Configure the predictor.
     * @param inputs
     * @param outputs
     * @param learningRate
     */
    public void setup( String name, ObjectMap om, Random r, int inputs, int hidden, int outputs, float learningRate, float leakiness, float regularization, int batchSize ) {
        _ffn = new FeedForwardNetwork( name, om );

        FeedForwardNetworkConfig c = new FeedForwardNetworkConfig();

        String lossFunction = CostFunction.QUADRATIC; // must be
        int layers = 2; // fixed
        String layerSizes = hidden + "," + outputs;
        String layerActivationFns = ActivationFunctionFactory.LEAKY_RELU + "," + ActivationFunctionFactory.LEAKY_RELU; // better mutability online

        c.setup( om, name, r, lossFunction, inputs, layers, layerSizes, layerActivationFns, regularization, learningRate, batchSize );

        ActivationFunctionFactory aff = new ActivationFunctionFactory();
        aff.leak = leakiness; // this is how we fix the param
        _ffn.setup( c, aff );
    }

    /**
     * Reset the learned weights of the predictor.
     */
    public void reset( int density, int regionWidth, int regionHeight, int columnWidth, int columnHeight ) {
        _ffn.reset();
    }

    /**
     * Train the predictor to predict the output given the input. Input and output will be a real unit valued vector.
     *
     * @param inputOld
     * @param density A hint about the number of bits in the output
     * @param outputNew
     */
    public void train( Data inputOld, Data outputNew, int density, int regionWidth, int regionHeight, int columnWidth, int columnHeight ) {
        Data input = _ffn.getInput();
        input.copy( inputOld );

        Data ideal = _ffn.getIdeal();
        ideal.copy( outputNew );

        _ffn.feedForward();
        _ffn.feedBackward();
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
        Data input = _ffn.getInput();
        input.copy( inputNew );
        _ffn.feedForward();
        Data output = _ffn.getOutput();
        outputPrediction.copy( output );
        outputPrediction.clipRange( 0f, 1f ); // as NN may go wildly beyond that
        outputPredictionSparse.set( 0f );

        if( ( columnWidth == 0 ) || ( columnHeight == 0 ) ) {
            // rank predictions
            TreeMap< Float, ArrayList< Integer > > ranking = new TreeMap< Float, ArrayList< Integer > >();
            int cells = output.getSize();
            for( int c = 0; c < cells; ++c ){
                float p = output._values[ c ];
                Ranking.add( ranking, p, c );
            }

            // keep top n most predicted cells
            boolean findMaxima = true;
            int maxRank = density;
            ArrayList< Integer > predictedCells = Ranking.getBestValues( ranking, findMaxima, maxRank );

            for( Integer c : predictedCells ) {
                outputPredictionSparse._values[ c ] = 1f;
            }
        }
        else {
            // keep max in each column
            int quiltWidth  = regionWidth  / columnWidth;
            int quiltHeight = regionHeight / columnHeight;

            for( int yColumn = 0; yColumn < quiltHeight; ++yColumn ) {
                for( int xColumn = 0; xColumn < quiltWidth; ++xColumn ) {

                    float pMax = -Float.MAX_VALUE;
                    int cMax = -1;

                    for( int yCell = 0; yCell < columnHeight; ++yCell ) {
                        for( int xCell = 0; xCell < columnWidth; ++xCell ) {

                            int xRegion = xColumn * columnWidth  + xCell;
                            int yRegion = yColumn * columnHeight + yCell;
                            int cRegion = yRegion * regionWidth + xRegion;

                            float p = output._values[ cRegion ];

                            if( p >= pMax ) {
                                pMax = p;
                                cMax = cRegion;
                            }
                        }
                    }

                    if( cMax >= 0 ) {
                        outputPredictionSparse._values[ cMax ] = 1f;
                    }

                }
            }

        }

    }

}
