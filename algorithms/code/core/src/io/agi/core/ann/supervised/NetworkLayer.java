package io.agi.core.ann.supervised;

import io.agi.core.data.Data;
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
    public Data _errors; // d

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
        _errors = new Data( cells );
    }

    public ActivationFunction getActivationFunction() {
        String costFunction = _c.getActivationFunction();
        ActivationFunction af = _aff.create( costFunction );
        return af;
    }

    public void feedForward() {
        ActivationFunction af = getActivationFunction();
        BackPropagation.feedForward(_weights, _inputs, _biases, _weightedSums, af, _outputs);
    }

    public void train() {
        float learningRate = _c.getLearningRate();
        BackPropagation.train(_inputs, _weights, _biases, _errors, learningRate );
    }
}
