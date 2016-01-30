package io.agi.core.ann.unsupervised;

import io.agi.core.data.Data;
import io.agi.core.math.RandomInstance;
import io.agi.core.orm.Callback;
import io.agi.core.orm.ObjectMap;
import io.agi.core.orm.UnitTest;

/**
 * Created by dave on 11/01/16.
 */
public class DynamicSelfOrganizingMapTest implements UnitTest {

    public static void main( String[] args ) {
        DynamicSelfOrganizingMapTest t = new DynamicSelfOrganizingMapTest();
        t.test( args );
    }

    public static final String DSOM = "dsom";

    public void test( String[] args ) {

        // Test parameters
        int epochs = 100;
        int batch = 100;
        int randomSeed = 1;
        int inputs = 2; // x and y.
        int widthCells = 10;
        int heightCells = 10;
        float meanErrorThreshold = 0.01f;

        // Algorithm specific parameters
        float learningRate = 0.25f;
        float elasticity = 1.f;

        RandomInstance.setSeed(randomSeed); // make the tests repeatable
        ObjectMap om = ObjectMap.GetInstance();
        DynamicSelfOrganizingMapConfig clc = new DynamicSelfOrganizingMapConfig();

        clc.setup( om, DSOM, inputs, widthCells, heightCells, learningRate, elasticity );

        DynamicSelfOrganizingMap cl = new DynamicSelfOrganizingMap( DSOM, om );
        cl.setup( clc );

        run( epochs, batch, meanErrorThreshold );
    }

    public DynamicSelfOrganizingMap getAlgorithm() {
        ObjectMap om = ObjectMap.GetInstance();
        DynamicSelfOrganizingMap cl = (DynamicSelfOrganizingMap)om.get( DSOM );
        return cl;
    }

    public float getDiscreteRandom() {
        float gridSize = 6.f;
        float x = (float)RandomInstance.random() * ( gridSize - 0.f );
        int nx = (int)x;
        x = (float)nx;
        x /= gridSize;
        x += ((1.f / ( gridSize )) * 0.5f );
        return x;
    }

    public void call() {
        // 1. generate input data
        float x = getDiscreteRandom();
        float y = getDiscreteRandom();

        // 2. update algorithm
        DynamicSelfOrganizingMap cl = getAlgorithm();
        cl._inputValues._values[ 0 ] = x;
        cl._inputValues._values[ 1 ] = y;
        cl.call();
    }

    public int run( int epochs, int batch, float meanErrorThreshold ) {

        // perform tests in batches until the mean error for a batch is below threshold.
        // Otherwise, fail test.
        for( int epoch = 0; epoch < epochs; ++epoch ) {

            float sumError = 0.f;

            for( int test = 0; test < batch; ++test ) {
                float error = step();
                sumError += error;
            }

            float meanError = 0.f;

            if( sumError > 0.f ) {
                meanError = sumError / (float)batch;
            }

            System.out.println( "Epoch: " +epoch+ " Mean error: " + meanError );

            if( meanError < meanErrorThreshold ) {
                System.out.println( "Success: Error below threshold for epoch." );
                return 0;
            }
        }

        System.out.println( "Failure: Error did not go below threshold for any epoch." );
        return -1;
    }

    public float step() {

        call();

        DynamicSelfOrganizingMap cl = getAlgorithm();

        Data input = cl.getInput();
        Data weights = cl._cellWeights;

        int inputs = input.getSize();
        int bestCell = cl._cellActivity.maxAt().offset();//getBestCell();

        float sumError = 0.f;

        for( int i = 0; i < inputs; ++i ) {
            int offset = bestCell * inputs + i;
            float x = input._values[ i ];
            float w = weights._values[ offset ];
            float d = Math.abs( x - w );
            sumError += d;
        }

        float errorValue = sumError / (float)inputs;

        float x = input._values[ 0 ];
        float y = input._values[ 1 ];

        System.out.println( "Input: " +x+ "," +y+ " Error: " + errorValue );

        return errorValue;
    }

}