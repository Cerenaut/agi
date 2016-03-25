package io.agi.core.alg;

import io.agi.core.ann.supervised.ActivationFunctionFactory;
import io.agi.core.ann.supervised.FeedForwardNetwork;
import io.agi.core.ann.supervised.FeedForwardNetworkConfig;
import io.agi.core.ann.supervised.LossFunction;
import io.agi.core.ann.unsupervised.GrowingNeuralGas;
import io.agi.core.ann.unsupervised.GrowingNeuralGasConfig;

/**
 * Factory for all the Region objects - Regions, Columns and internal parts (Classifier, Predictor)
 * Created by dave on 28/12/15.
 */
public class RegionFactory {

    public RegionConfig _rc;
    public GrowingNeuralGasConfig _gngc;
    public FeedForwardNetworkConfig _ffnc;

    public RegionFactory() {

    }

    public void setup( RegionConfig rc, GrowingNeuralGasConfig gngc, FeedForwardNetworkConfig ffnc ) {
        _rc = rc;
        _gngc = gngc;
        _ffnc = ffnc;
    }

    public Region createRegion( String name ) {
        RegionConfig rc = new RegionConfig();
        rc.copyFrom( _rc, name );
        Region r = new Region( rc._name, rc._om );
        r.setup( rc, this );
        return r;
    }

    public Column createColumn( Region r, int x, int y ) {//}, GrowingNeuralGasConfig gngc, FeedForwardNetworkConfig ffnc, ActivationFunctionFactory ) {
        String parentName = r.getName();
        String columnName = Column.getName( parentName, x, y );
        Column c = new Column( columnName, r._rc._om );
        c.setup( r, x, y );
        return c;
    }

    public GrowingNeuralGas createClassifier( Region r, int x, int y ) {

        String name = r.getKey( Region.SUFFIX_CLASSIFIER );
        GrowingNeuralGasConfig c = new GrowingNeuralGasConfig();
        c.copyFrom( _gngc, name );

        GrowingNeuralGas gng = new GrowingNeuralGas( c._name, c._om );
        gng.setup( c );
        return gng;
    }

    public FeedForwardNetwork createPredictor( Region r, int x, int y ) {

        String name = r.getKey( Region.SUFFIX_PREDICTOR );
        FeedForwardNetworkConfig c = new FeedForwardNetworkConfig();
        c.copyFrom( _ffnc, name );

        ActivationFunctionFactory aff = new ActivationFunctionFactory();

        FeedForwardNetwork ffn = new FeedForwardNetwork( c._name, c._om );
        ffn.setup( c, aff );

        // Twin layer test:
        String lossFunction = c.getLossFunction();
        String activationFunction = c.getActivationFunction();
        float learningRate = c.getLearningRate();
        int inputs = c.getNbrInputs();
        int outputs = c.getNbrOutputs();
        String layerSizes = c.getLayerSizes();
        int hidden = Integer.valueOf( layerSizes );

        // hardcoded for 2 layers
        ffn.setupLayer( 0, inputs, hidden, learningRate, activationFunction );

        if ( lossFunction.equals( LossFunction.LOG_LIKELIHOOD ) ) {
            ffn.setupLayer( 1, hidden, outputs, learningRate, ActivationFunctionFactory.SOFTMAX );
        }
        else {
            ffn.setupLayer( 1, hidden, outputs, learningRate, activationFunction );
        }

        return ffn;
    }
}
