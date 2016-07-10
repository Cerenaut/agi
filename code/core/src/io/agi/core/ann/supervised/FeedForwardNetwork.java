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
import io.agi.core.orm.Keys;
import io.agi.core.orm.NamedObject;
import io.agi.core.orm.ObjectMap;

import java.util.ArrayList;
import java.util.Random;

/**
 * A feed-forward artificial neural network trained by error backpropagation.
 * <p/>
 * Created by dave on 3/01/16.
 */
public class FeedForwardNetwork extends NamedObject {

    public static final String LAYER = "layer";

    public FeedForwardNetworkConfig _c;
    public ActivationFunctionFactory _aff;

    // Data within layers
    public ArrayList< NetworkLayer > _layers = new ArrayList< NetworkLayer >();

    // Data that is not part of every layer:
    public Data _ideals; // output size.

    public FeedForwardNetwork( String name, ObjectMap om ) {
        super( name, om );
    }

    public void setup( FeedForwardNetworkConfig c, ActivationFunctionFactory aff ) {
        _c = c;
        _aff = aff;

        int outputs = _c.getNbrOutputs();
        int layers = _c.getNbrLayers();

        _ideals = new Data( outputs );

        for( int l = 0; l < layers; ++l ) {
            String layerName = getLayerName( l );
            NetworkLayer nl = new NetworkLayer( layerName, _om );
            _layers.add( nl );
        }

    }

    public void reset() {
        int layers = _c.getNbrLayers();

        for( int l = 0; l < layers; ++l ) {
            NetworkLayer nl = _layers.get( l );
            nl.reset( _c._r );
        }
    }

    /**
     * Setup every layer of the network individually.
     *
     * @param layer
     * @param cells
     * @param learningRate
     * @param activationFunction
     */
    public void setupLayer( Random r, int layer, int inputs, int cells, float learningRate, String activationFunction ) {

        String layerName = getLayerName( layer );

        NetworkLayerConfig nlc = new NetworkLayerConfig();

        nlc.setup( _om, layerName, r, inputs, cells, learningRate, activationFunction );

        NetworkLayer nl = _layers.get( layer );
        nl.setup( nlc, _aff );
    }

    /**
     * Systematically constructs a unique String to identify the layer.
     *
     * @param layer
     * @return
     */
    public String getLayerName( int layer ) {
        String layerName = getKey( Keys.concatenate( getKey( LAYER ), String.valueOf( layer ) ) );
        return layerName;
    }

    /**
     * Returns the data input to the first layer.
     * Use this function to modify the input data returned.
     *
     * @return
     */
    public Data getInput() {
        int layers = _layers.size();
        if( layers == 0 ) {
            return null;
        }

        return _layers.get( 0 )._inputs;
    }

    /**
     * Returns the output from the final layer.
     *
     * @return
     */
    public Data getOutput() {
        int layers = _layers.size();
        if( layers == 0 ) {
            return null;
        }

        return _layers.get( layers - 1 )._outputs;
    }

    /**
     * Returns the data structure that defines the ideal (correct) output.
     * Use this function to modify the data returned.
     * This will be used for training.
     *
     * @return
     */
    public Data getIdeal() {
        return _ideals;
    }

    public float getWeightsSquared() {
        int L = _layers.size();

        float sumSq = 0.f;

        for( int layer = 0; layer < L; ++layer ) {
            NetworkLayer nl = _layers.get( layer );
            sumSq += nl.getWeightsSquared();
        }

        return sumSq;
    }

    /**
     * Run the network in a forward direction, producing an output
     */
    public void feedForward() {
        int layers = _layers.size();

        for( int layer = 0; layer < layers; ++layer ) {

            NetworkLayer nl = _layers.get( layer );

            if( layer > 0 ) {
                NetworkLayer nlBelow = _layers.get( layer - 1 );
                nl._inputs.copy( nlBelow._outputs );
            }

            nl.feedForward();
        }
    }

    /**
     * Run the network backwards, training it using the ideal output.
     */
    public void feedBackward() {

        float l2R = _c.getL2Regularization();
        float sumSqWeights = 0.f;
        if( l2R > 0.f ) {
            sumSqWeights = getWeightsSquared();
        }

        int layers = _layers.size();
        int L = layers - 1;

        for( int layer = L; layer >= 0; --layer ) {

            NetworkLayer nl = _layers.get( layer );
            TransferFunction af = nl.getActivationFunction();
            if( layer == L ) {
                String lossFunction = _c.getLossFunction();
                BackPropagation.OutputErrorGradient( nl._weightedSums, nl._outputs, _ideals, nl._errorGradients, af, lossFunction, l2R, sumSqWeights );
            } else { // layer < L
                NetworkLayer nl2 = _layers.get( layer + 1 );
                BackPropagation.ErrorGradient( nl._weightedSums, nl._errorGradients, nl2._weights, nl2._errorGradients, af, l2R );
            }

            nl.train( l2R ); // using the error gradients, d
        }
    }

}
