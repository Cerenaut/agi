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
import io.agi.core.orm.ObjectMap;

import java.util.Random;

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
    public void reset() {
        _ffn.reset();
    }

    /**
     * Train the predictor to predict the output given the input. Input and output will be a real unit valued vector.
     *
     * @param inputOld
     * @param density A hint about the number of bits in the output
     * @param outputNew
     */
    public void train( Data inputOld, int density, Data outputNew ) {
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
    public void predict( Data inputNew, int density, Data outputPrediction ) {
        Data input = _ffn.getInput();
        input.copy( inputNew );
        _ffn.feedForward();
        Data output = _ffn.getOutput();
        outputPrediction.copy( output );
        outputPrediction.clipRange( 0f, 1f ); // as NN may go wildly beyond that
    }

}
