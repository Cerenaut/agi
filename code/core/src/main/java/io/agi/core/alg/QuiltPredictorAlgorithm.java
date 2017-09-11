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
import java.util.Random;
import java.util.TreeMap;

/**
 * Created by dave on 20/01/17.
 */
public interface QuiltPredictorAlgorithm {

    void setup( QuiltPredictorConfig config );

    /**
     * Reset the learned weights of the predictor.
     */
    void reset( int density, int regionWidth, int regionHeight, int columnWidth, int columnHeight );

    /**
     * Train the predictor to predict the output given the input. Input and output will be a real unit valued vector.
     *
     * @param inputOld
     * @param density A hint about the number of bits in the output
     * @param outputNew
     */
    void train( Data inputOld, Data outputNew, int density, int regionWidth, int regionHeight, int columnWidth, int columnHeight );

    /**
     * Provide an output predicting the next state of the system. Output should be unit values, with threshold 0.5 used
     * to determine whether a cell was "predicted" or not.
     *
     * @param inputNew
     * @param density A hint about the number of bits in the output
     * @param outputPrediction
     */
    void predict( Data inputNew, Data outputPrediction, Data outputPredictionSparse, int density, int regionWidth, int regionHeight, int columnWidth, int columnHeight );
}
