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

package io.agi.core.ann.supervised;

import io.agi.core.data.Data;
import io.agi.core.data.FloatArray;
import io.agi.core.orm.NamedObject;
import io.agi.core.orm.ObjectMap;

import java.util.Random;

/**
 * This class represents a single layer of a feed-forward neural network.
 * <p/>
 * Terminology from:
 * http://neuralnetworksanddeeplearning.com/chap2.html
 * <p/>
 * Created by dave on 3/01/16.
 */
public class NetworkLayer extends NamedObject {

    public NetworkLayerConfig _c;
    public ActivationFunctionFactory _aff;

    public static final String INPUT = "input";
    public static final String WEIGHTS = "weights";
    public static final String BIASES = "biases";
    public static final String WEIGHTED_SUMS = "weighted-sums";
    public static final String OUTPUTS = "outputs";
    public static final String ERROR_GRADIENTS = "error-gradients";

    public Data _inputs; // x
    public Data _weights; // w
    public Data _biases; // b
    public Data _weightedSums; // z = sum of w * i +b
    public Data _outputs; // a = f( z )
    public Data _errorGradients; // d

    public NetworkLayer( String name, ObjectMap om ) {
        super( name, om );
    }

    public void setup( NetworkLayerConfig c, ActivationFunctionFactory aff ) {
        _c = c;
        _aff = aff;

        int inputs = c.getInputs();
        int cells = c.getCells();

        _inputs = new Data( inputs );
        _weights = new Data( inputs, cells );
        _biases = new Data( cells );
        _weightedSums = new Data( cells );
        _outputs = new Data( cells );
        _errorGradients = new Data( cells );

        reset( c._r );
    }

    public void reset( Random r ) {
        _weights.setRandom( r );
        _biases.setRandom( r );

        _outputs.set( 0.f );
        _errorGradients.set( 0.f );
    }

    public float getWeightsSquared() {
        int W = _weights.getSize();

        float sumSq = 0.f;

        for( int w = 0; w < W; ++w ) {
            float weight = _weights._values[ w ];
            sumSq += ( w * w );
        }

        return sumSq;
    }

    /**
     * Dynamically Create the activation function assigned to this layer, using the factory.
     *
     * @return
     */
    public TransferFunction getActivationFunction() {
        String costFunction = _c.getActivationFunction();
        TransferFunction af = _aff.create( costFunction );
        return af;
    }

    /**
     * Compute the forward output of the layer.
     */
    public void feedForward() {
        TransferFunction af = getActivationFunction();
//        BackPropagation.feedForward(_weights, _inputs, _biases, _weightedSums, af, _outputs);
        WeightedSum( _weights, _inputs, _biases, _weightedSums );
        Activate( _weightedSums, af, _outputs );
    }

    /**
     * Compute weighted sum of inputs given weights
     *
     * @param weights
     * @param inputs
     * @param biases
     * @param outputs
     */
    public static void WeightedSum( FloatArray weights, FloatArray inputs, FloatArray biases, FloatArray outputs ) {
        int K = inputs.getSize();
        int J = biases.getSize();

        assert ( outputs.getSize() == J );
        assert ( weights.getSize() == ( J * K ) );

        for( int j = 0; j < J; ++j ) {

            float sum = 0.f;

            for( int k = 0; k < K; ++k ) {
                int offset = j * K + k; // K = inputs, storage is all inputs adjacent
                float i = inputs._values[ k ];
                float w = weights._values[ offset ];
                float product = i * w;
                sum += product;
            }

            float b = biases._values[ j ];

            sum += b;

            outputs._values[ j ] = sum;
        }
    }

    /**
     * Apply the activation function to the weighted sums
     *
     * @param weightedSums
     * @param af
     * @param outputs
     */
    public static void Activate( FloatArray weightedSums, TransferFunction af, FloatArray outputs ) {
        af.f( weightedSums, outputs );
    }

    /**
     * Train the layer's weights given the error gradients.
     */
    public void train( float l2R ) {
        float learningRate = _c.getLearningRate();
//        BackPropagation.train( _inputs, _weights, _biases, _errorGradients, learningRate, l2R );
    }
}
