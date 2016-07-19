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

package io.agi.core.ann.unsupervised;

import io.agi.core.math.RandomInstance;
import io.agi.core.orm.ObjectMap;
import io.agi.core.orm.UnitTest;

import java.util.Random;

/**
 * Created by dave on 11/01/16.
 */
public class KSparseAutoencoderTest implements UnitTest {

    public static void main( String[] args ) {
        KSparseAutoencoderTest t = new KSparseAutoencoderTest();
        t.test( args );
    }

    public static final String NAME = "autoenc";
    public static final int GRID_SIZE = 3;

    public void test( String[] args ) {

        // Test parameters
        int epochs = 5000;
        int batch = 500;
        int randomSeed = 1;
        int inputs = 2; // x and y.
        int widthCells = 5;
        int heightCells = 5;
        float meanErrorThreshold = 0.001f;

        // Algorithm specific parameters
        float learningRate = 0.001f;
        boolean binaryOutput = false;
        float sparsityOutput = 1.5f;
        int sparsity = 0; // 3 x 3 = 9 possible inputs, * 2 (sparsity) = 18, 25 cells available
        int sparsityMin = 2;
        int sparsityMax = 5;
        int ageMin = 100;
        int ageMax = 1000;
        int age = 0;

        RandomInstance.setSeed( randomSeed ); // make the tests repeatable
        Random random = RandomInstance.getInstance();
        ObjectMap om = ObjectMap.GetInstance();
        KSparseAutoencoderConfig c = new KSparseAutoencoderConfig();
        c.setup(
            om, NAME, random,
            inputs, widthCells, heightCells,
            learningRate, binaryOutput, sparsityOutput, sparsity, sparsityMin, sparsityMax, ageMin, ageMax, age );

        KSparseAutoencoder ae = new KSparseAutoencoder( NAME, om );
        ae.setup( c );
        ae.reset();

        run( epochs, batch, meanErrorThreshold );
    }

    public KSparseAutoencoder getAlgorithm() {
        ObjectMap om = ObjectMap.GetInstance();
        KSparseAutoencoder ae = ( KSparseAutoencoder ) om.get( NAME );
        return ae;
    }

    public float getDiscreteRandom() {
        return getDiscreteRandom( RandomInstance.getInstance() );
    }

    public float getDiscreteRandom( Random r ) {
        float gridSize = GRID_SIZE;
        float x = ( float ) r.nextFloat() * ( gridSize - 0.f );
        int nx = ( int ) x;
        x = ( float ) nx;
        x /= gridSize;
        x += ( ( 1.f / ( gridSize ) ) * 0.5f );
        return x;
    }

    public void call() {
        // 1. generate input data
        float x = getDiscreteRandom();
        float y = getDiscreteRandom();

        // 2. update algorithm
        KSparseAutoencoder cl = getAlgorithm();
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
                meanError = sumError / ( float ) batch;
            }

            System.out.println( "Epoch: " + epoch + " Mean error: " + meanError );

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

        KSparseAutoencoder ae = getAlgorithm();

        // measure the reconstruction error:
        int inputs = ae._inputValues.getSize();

        float sumError = 0.f;

        for( int i = 0; i < inputs; ++i ) {

            float x1 = ae._inputValues._values[ i ];
            float x2 = ae._inputReconstruction._values[ i ];
            float d = Math.abs( x1 - x2 );
            sumError += d;
        }

        float errorValue = sumError / ( float ) inputs;

        float x = ae._inputValues._values[ 0 ];
        float y = ae._inputValues._values[ 1 ];

//        System.out.println( "Input: " + x + "," + y + " Error: " + errorValue );

        return errorValue;
    }

}