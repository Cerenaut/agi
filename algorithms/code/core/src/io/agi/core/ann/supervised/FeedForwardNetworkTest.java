package io.agi.core.ann.supervised;

import io.agi.core.data.Data;
import io.agi.core.math.RandomInstance;
import io.agi.core.orm.ObjectMap;
import io.agi.core.orm.UnitTest;

/**
 * Unit test
 *
 * Created by dave on 10/01/16.
 */
public class FeedForwardNetworkTest implements UnitTest {

    public static void main( String[] args ) throws Exception {
        FeedForwardNetworkTest ffnt = new FeedForwardNetworkTest();
        ffnt.test( args );
    }

    public enum Logic{ AND, OR, XOR };

    public Logic _l;
    public int _epochs;
    public int _batch;
    public float _meanErrorThreshold;
    public FeedForwardNetwork _ffn;

    public void setup( Logic l, int epochs, int batch, float learningRate, float meanErrorThreshold ) {

        _l = l;
        _epochs = epochs;
        _batch = batch;
        _meanErrorThreshold = meanErrorThreshold;

        int inputs = 2;
        int hidden = 3;
        int outputs = 1;
//        int layers = 1;
        int layers = 2;

        String name = "ffn";
        String lossFunction = LossFunctionFactory.QUADRATIC;
        String activationFunction = ActivationFunctionFactory.LOG_SIGMOID;

        ObjectMap om = new ObjectMap();
        FeedForwardNetworkConfig ffnc = new FeedForwardNetworkConfig();
        ffnc.setup(om, name, lossFunction, inputs, outputs, layers);

        ActivationFunctionFactory aff = new ActivationFunctionFactory();
        LossFunctionFactory lff = new LossFunctionFactory();

        _ffn = new FeedForwardNetwork( name, om );
        _ffn.setup(ffnc, aff, lff);

// Single layer test:
//        _ffn.setupLayer(0, inputs, outputs, learningRate, activationFunction);
// Twin layer test:
        _ffn.setupLayer( 0, inputs, hidden, learningRate, activationFunction );
        _ffn.setupLayer( 1, hidden, outputs, learningRate, activationFunction );
    }

    public static float ideal( float x1, float x2, Logic l ) {
        // AND
        if( l == Logic.AND ) {
            if( ( x1 > 0.5 ) && ( x2 > 0.5 ) ) {
                return 1.0f;
            }
            return 0.0f;
        }

        // OR
        if( l == Logic.OR ) {
            if( ( x1 > 0.5 ) || ( x2 > 0.5 ) ) {
                return 1.0f;
            }
            return 0.0f;
        }

        // XOR
        if( l == Logic.XOR ) {
            if( ( ( x1 > 0.5 ) && ( x2 < 0.5 ) )
                ||
                ( ( x2 > 0.5 ) && ( x1 < 0.5 ) ) ) {
                return 1.0f;
            }
            return 0.0f;
        }

        return 0.0f;
    }

    public int test( String[] args ) {

        Logic l = Logic.XOR;
//        Logic l = Logic.AND;
        int epochs = 2000;
        int batch = 100;
        float learningRate = 0.5f;
        float meanErrorThreshold = 0.01f;

        setup( l, epochs, batch, learningRate, meanErrorThreshold );

        return epochs();
    }

    public int epochs() {

        // perform tests in batches until the mean error for a batch is below threshold.
        // Otherwise, fail test.
        for( int epoch = 0; epoch < _epochs; ++epoch ) {

            float sumError = 0.f;

            for( int test = 0; test < _batch; ++test ) {
                float error = step();
                sumError += error;
            }

            float meanError = 0.f;

            if( sumError > 0.f ) {
                meanError = sumError / (float)_batch;
            }

            System.out.println( "Epoch: " +epoch+ " Mean error: " + meanError );

            if( meanError < _meanErrorThreshold ) {
                System.out.println( "Success: Error below threshold for epoch." );
                return 0;
            }
        }

        System.out.println( "Failure: Error did not go below threshold for any epoch." );
        return -1;
    }

    public float step() {

        float x1 = RandomInstance.randomInt( 2 );
        float x2 = RandomInstance.randomInt( 2 );
        float idealValue = ideal(x1, x2, _l);

//        if( x1 < 0.5 ) x1 = 0.0;
//        else           x1 = 1.0;
//
//        if( x2 < 0.5 ) x2 = 0.0;
//        else           x2 = 1.0;

        Data input = _ffn.getInput();
        Data ideal = _ffn.getIdeal();
        Data output = _ffn.getOutput();

        input._values[ 0 ] = x1;
        input._values[ 1 ] = x2;
        ideal._values[ 0 ] = idealValue;

        _ffn.feedForward();
        _ffn.feedBackward();

        float outputValue = output._values[ 0 ];
        float errorValue = Math.abs( idealValue - outputValue );

//        System.out.println( "Input: " +x1+ "," +x2+ " Ideal: " + idealValue + " Output: " + outputValue + " Error: " + errorValue );

        return errorValue;
    }

}
