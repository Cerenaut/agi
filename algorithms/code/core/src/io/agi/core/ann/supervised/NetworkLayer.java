package io.agi.core.ann.supervised;

import io.agi.core.data.Data;
import io.agi.core.data.FloatArray2;
import io.agi.core.orm.NamedObject;
import io.agi.core.orm.ObjectMap;

/**
 * This class represents a single layer of a feed-forward neural network.
 *
 * Terminology from:
 * http://neuralnetworksanddeeplearning.com/chap2.html
 *
 * Created by dave on 3/01/16.
 */
public class NetworkLayer extends NamedObject {

    public NetworkLayerConfig _c;
    public ActivationFunctionFactory _aff;

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
        _weights.setRandom();
        _biases = new Data( cells );
        _biases.setRandom();
        _weightedSums = new Data( cells );
        _outputs = new Data( cells );
        _errorGradients = new Data( cells );
    }

    /**
     * Dynamically create the activation function assigned to this layer, using the factory.
     *
     * @return
     */
    public ActivationFunction getActivationFunction() {
        String costFunction = _c.getActivationFunction();
        ActivationFunction af = _aff.create( costFunction );
        return af;
    }

    /**
     * Compute the forward output of the layer.
     */
    public void feedForward() {
        ActivationFunction af = getActivationFunction();
//        BackPropagation.feedForward(_weights, _inputs, _biases, _weightedSums, af, _outputs);
        WeightedSum( _weights, _inputs, _biases, _weightedSums );
        Activate( _weightedSums, af, _outputs );
    }

    /**
     * Compute weighted sum of inputs given weights
     * @param weights
     * @param inputs
     * @param biases
     * @param outputs
     */
    public static void WeightedSum( FloatArray2 weights, FloatArray2 inputs, FloatArray2 biases, FloatArray2 outputs ) {
        int K = inputs.getSize();
        int J = biases.getSize();

        assert( outputs.getSize() == J );
        assert( weights.getSize() == (J*K) );

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
     * @param cf
     * @param outputs
     */
    public static void Activate( FloatArray2 weightedSums, ActivationFunction cf, FloatArray2 outputs ) {
        int J = weightedSums.getSize();

        assert( outputs.getSize() == J );

        for( int j = 0; j < J; ++j ) {

            float z = weightedSums._values[ j ];
            double a = cf.f( z );

            outputs._values[ j ] = (float)a;
        }
    }

    /**
     * Train the layer's weights given the error gradients.
     */
    public void train() {
        float learningRate = _c.getLearningRate();
        BackPropagation.train( _inputs, _weights, _biases, _errorGradients, learningRate );
    }
}
