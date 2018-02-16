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

import io.agi.core.data.Data;
import io.agi.core.math.RandomInstance;
import io.agi.core.orm.ObjectMap;
import io.agi.core.orm.UnitTest;

import java.util.Random;

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
        int epochs = 500;
        int batch = 100;
        int randomSeed = 1;
        int inputs = 2; // x and y.
        int widthCells = 20;
        int heightCells = 20;
        float meanErrorThreshold = 0.005f;

        // Algorithm specific parameters
        float learningRate = 0.1f;
        float elasticity = 1.f;

        RandomInstance.setSeed( randomSeed ); // make the tests repeatable
        Random random = RandomInstance.getInstance();
        ObjectMap om = ObjectMap.GetInstance();
        DynamicSelfOrganizingMapConfig clc = new DynamicSelfOrganizingMapConfig();

        clc.setup( om, DSOM, random, inputs, widthCells, heightCells, learningRate, elasticity );

        DynamicSelfOrganizingMap cl = new DynamicSelfOrganizingMap( DSOM, om );
        cl.setup( clc );
        cl.reset();

        run( epochs, batch, meanErrorThreshold );
    }

    public DynamicSelfOrganizingMap getAlgorithm() {
        ObjectMap om = ObjectMap.GetInstance();
        DynamicSelfOrganizingMap cl = ( DynamicSelfOrganizingMap ) om.get( DSOM );
        return cl;
    }

    public float getDiscreteRandom() {
        return getDiscreteRandom( RandomInstance.getInstance() );
    }

    public float getDiscreteRandom( Random r ) {
        float gridSize = 6.f;
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

        float errorValue = sumError / ( float ) inputs;

        float x = input._values[ 0 ];
        float y = input._values[ 1 ];

//        System.out.println( "Input: " + x + "," + y + " Error: " + errorValue );

        return errorValue;
    }

}