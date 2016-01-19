package io.agi.core.ann.supervised;

import io.agi.core.data.Data;
import io.agi.core.orm.Keys;
import io.agi.core.orm.NamedObject;
import io.agi.core.orm.ObjectMap;

import java.util.ArrayList;

/**
 * A feed-forward artificial neural network trained by error backpropagation.
 *
 * Created by dave on 3/01/16.
 */
public class FeedForwardNetwork extends NamedObject {

    public static final String LAYER = "layer";

    public FeedForwardNetworkConfig _c;
    public ActivationFunctionFactory _aff;
//    public LossFunctionFactory _lff;

    // Data within layers
    public ArrayList< NetworkLayer > _layers = new ArrayList< NetworkLayer >();

    // Data that is not part of every layer:
    public Data _ideals; // output size.
//    public Data _losses; // output size.

    public FeedForwardNetwork( String name, ObjectMap om ) {
        super( name, om );
    }

    public void setup( FeedForwardNetworkConfig c, ActivationFunctionFactory aff ) {//, LossFunctionFactory lff ) {
        _c = c;
        _aff = aff;
//        _lff = lff;

//        int inputs = _c.getNbrInputs();
        int outputs = _c.getNbrOutputs();
        int layers = _c.getNbrLayers();

        _ideals = new Data( outputs );
//        _losses = new Data( outputs );

        for( int l = 0; l < layers; ++l ) {
            String layerName = getLayerName( l );
            NetworkLayer nl = new NetworkLayer( layerName, _om );
            _layers.add( nl );
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
    public void setupLayer( int layer, int inputs, int cells, float learningRate, String activationFunction ) {

        String layerName = getLayerName( layer );

        NetworkLayerConfig nlc = new NetworkLayerConfig();

        nlc.setup(_om, layerName, inputs, cells, learningRate, activationFunction );

        NetworkLayer nl = _layers.get( layer );
        nl.setup( nlc, _aff );
    }

    public String getLayerName( int layer ) {
        String layerName = getKey( Keys.concatenate( getKey( LAYER ), String.valueOf( layer ) ) );
        return layerName;
    }

    /**
     * Returns the data input to the first layer
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
     * Returns the output from the final layer
     * @return
     */
    public Data getOutput() {
        int layers = _layers.size();
        if( layers == 0 ) {
            return null;
        }

        return _layers.get( layers -1 )._outputs;
    }

    /**
     * Returns the data structure that defines the ideal (correct) output.
     * This will be used for training.
     * @return
     */
    public Data getIdeal() {
        return _ideals;
    }

//    public LossFunction getLossFunction() {
//        String function = _c.getLossFunction();
//        LossFunction lf = _lff.create( function );
//        return lf;
//    }

    public void feedForward() {
        int layers = _layers.size();

        for( int layer = 0; layer < layers; ++layer ) {

            NetworkLayer nl = _layers.get( layer );

            if( layer > 0 ) {
                NetworkLayer nlBelow = _layers.get( layer -1 );
                nl._inputs.copy( nlBelow._outputs );
            }

            nl.feedForward();
        }
    }

    public void feedBackward() {
        int layers = _layers.size();

        int L = layers -1;
        for( int layer = L; layer >= 0; --layer ) {

            NetworkLayer nl = _layers.get( layer );
            ActivationFunction af = nl.getActivationFunction();

            if( layer == L ) {
// Quadratic
//                LossFunction lf = getLossFunction();
//                BackPropagation.losses( nl._outputs, _ideals, _losses, lf );
//                BackPropagation.externalErrorGradient( _losses, nl._weightedSums, nl._errors, nl.getActivationFunction() );
// Cross-Entropy
//                BackPropagation.externalErrorGradient(_losses, _ideals, nl._outputs, nl._errors );//, lf );
// Generic:
                String lossFunction = _c.getLossFunction();
                BackPropagation.externalErrorGradient( nl._weightedSums, nl._outputs, _ideals, nl._errors, af, lossFunction );
            }
            else { // layer < L
                NetworkLayer nl2 = _layers.get(layer + 1);
                BackPropagation.internalErrorGradient( nl2._weights, nl2._errors, nl._errors, nl._weightedSums, af );
            }

            nl.train(); // using the error gradients, d
        }
    }

}
