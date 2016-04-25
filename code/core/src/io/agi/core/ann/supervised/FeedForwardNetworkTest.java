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
import io.agi.core.math.RandomInstance;
import io.agi.core.orm.ObjectMap;
import io.agi.core.orm.UnitTest;

import java.util.Random;

/**
 * Unit test
 * <p/>
 * Created by dave on 10/01/16.
 */
public class FeedForwardNetworkTest implements UnitTest {

    public static void main( String[] args ) throws Exception {
        FeedForwardNetworkTest ffnt = new FeedForwardNetworkTest();
        ffnt.test( args );
    }

    public void test( String[] args ) {

        Logic l = Logic.XOR;
//        Logic l = Logic.AND;

        int epochs = 2000;
        int batch = 100;
        int hidden = 3;
        float meanErrorThreshold = 0.01f;
        float learningRate = 0.0f; // quadratic
        float regularization = 0.0f;
        String lossFunction = null;

        if( args.length > 0 ) {
            lossFunction = args[ 0 ];
        }

        if( lossFunction.equals( LossFunction.QUADRATIC ) ) {
            learningRate = 0.5f; // quadratic
//            learningRate = 0.1f; // quadratic
        } else if( lossFunction.equals( LossFunction.CROSS_ENTROPY ) ) {
            learningRate = 0.1f; // cross entropy
        } else if( lossFunction.equals( LossFunction.LOG_LIKELIHOOD ) ) {
            learningRate = 0.1f;
        }

        setup( l, epochs, batch, hidden, learningRate, meanErrorThreshold, regularization, lossFunction );

        epochs();
    }

    public enum Logic {AND, OR, XOR };

    public Logic _l;
    public int _epochs;
    public int _batch;
    public float _meanErrorThreshold;
    public FeedForwardNetwork _ffn;

    public void setup( Logic l, int epochs, int batch, int hidden, float learningRate, float meanErrorThreshold, float regularization, String lossFunction ) {

        _l = l;
        _epochs = epochs;
        _batch = batch;
        _meanErrorThreshold = meanErrorThreshold;

        int inputs = 2;
        int outputs = 1;

        int layers = 2;
        if( hidden == 0 ) {
            layers = 1;
        }

        if( lossFunction.equals( LossFunction.LOG_LIKELIHOOD ) ) {
            outputs = 2; // outcome A or B.
        }

        String name = "feed-forward-network";
        String activationFunction = ActivationFunctionFactory.LOG_SIGMOID;
        String layerSizes = String.valueOf( hidden );
        float l2R = regularization;//0.0001f;

        ObjectMap om = new ObjectMap();
        Random r = RandomInstance.getInstance();
        FeedForwardNetworkConfig ffnc = new FeedForwardNetworkConfig();
        ffnc.setup( om, name, r, lossFunction, activationFunction, inputs, outputs, layers, layerSizes, l2R, learningRate );
        ActivationFunctionFactory aff = new ActivationFunctionFactory();

        _ffn = new FeedForwardNetwork( name, om );
        _ffn.setup( ffnc, aff );

        if( layers == 1 ) {
            // Single layer test:
            _ffn.setupLayer( r, 0, inputs, outputs, learningRate, activationFunction );
        } else if( layers == 2 ) {
            // Twin layer test:
            _ffn.setupLayer( r, 0, inputs, hidden, learningRate, activationFunction );

            if( lossFunction.equals( LossFunction.LOG_LIKELIHOOD ) ) {
                _ffn.setupLayer( r, 1, hidden, outputs, learningRate, ActivationFunctionFactory.SOFTMAX );
            } else {
                _ffn.setupLayer( r, 1, hidden, outputs, learningRate, activationFunction );
            }
        } else {
            System.err.println( "Bad configuration - layers > 2." );
        }
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
                meanError = sumError / ( float ) _batch;
            }

            System.out.println( "Epoch: " + epoch + " Mean error: " + meanError );

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
        float idealValue = ideal( x1, x2, _l );

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

        if( ideal.getSize() == 1 ) {
            ideal._values[ 0 ] = idealValue;
        } else {
            assert ( ideal.getSize() == 2 );
            if( idealValue == 0.f ) {
                ideal._values[ 0 ] = 1.f;
                ideal._values[ 1 ] = 0.f;
            } else {
                ideal._values[ 0 ] = 0.f;
                ideal._values[ 1 ] = 1.f;
            }
        }

        _ffn.feedForward();
        _ffn.feedBackward();

//        float outputValue = output._values[ 0 ];
//        float errorValue = Math.abs( idealValue - outputValue );

        float errorValue = output.sumAbsDiff( ideal );

//        System.out.println( "Input: " +x1+ "," +x2+ " Ideal: " + idealValue + " Error: " + errorValue );

        return errorValue;
    }

}
