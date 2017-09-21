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

package io.agi.core.ml.supervised;

import io.agi.core.data.Data;
import io.agi.core.data.Data2d;
import io.agi.core.math.FastRandom;
import io.agi.core.orm.ObjectMap;
import io.agi.core.orm.UnitTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import java.util.Arrays;

/**
 * Created by abdel on 14/09/17.
 */
public class SvmTest implements UnitTest {

    private Svm _learner = null;

    private Data _featuresMatrixTrain = null;
    private Data _predictionsVector = null;
    private Data _classTruthVector = null;

    private Data _featuresMatrixTest = null;
    private Data _predictionsVectorTest = null;
    private Data _classTruthVectorTest = null;

    private float _eps = 0.0000001f;
    private ObjectMap _om;
    private FastRandom _r;

    public static void main( String[] args ) {
        SvmTest svmTest = new SvmTest();
        svmTest.test( args );
    }

    @Override
    public void test( String[] args ) {

        try {
            setUp();
        }
        catch( Exception e ) {
            e.printStackTrace();
        }
        try {
            predict();
        }
        catch( Exception e ) {
            e.printStackTrace();
        }
    }

    @Before
    public void setUp() throws Exception {

        // get data from file
        String filePath = "src/test/resources/SPECTF.train.csv";
        _featuresMatrixTrain = Data2d.createFromCSV( filePath, 1, 44 );
        _classTruthVector = Data2d.createFromCSV( filePath, 0, 0 );

        filePath = "src/test/resources/SPECTF.test.csv";
        _featuresMatrixTest = Data2d.createFromCSV( filePath, 1, 44 );
        _classTruthVectorTest = Data2d.createFromCSV( filePath, 0, 0 );

        _predictionsVector = new Data( _classTruthVector._dataSize );
        _predictionsVectorTest = new Data( _classTruthVectorTest._dataSize );

        // instantiate learner
        _om = ObjectMap.GetInstance();
        _learner = new Svm( "svm", _om );

        // setup learner
        _r = new FastRandom(  );
        SupervisedBatchTrainingConfig config = new SupervisedBatchTrainingConfig( );
        config.setup( _om, "test-svm-config", _r, "", true, 100f);
        _learner.setup( config );

        // train model
        _learner.train( _featuresMatrixTrain, _classTruthVector );
        String modelString = _learner.getModelString();

        assertTrue( modelString != null );
    }

    @After
    public void tearDown() throws Exception {

    }

    /**
     * Tests loading the model and predicting
     * @throws Exception
     */
    @Test
    public void predict() throws Exception {

        boolean log = false;

        // Evaluate on training data
        _learner.predict( _featuresMatrixTrain, _predictionsVector );

        // Evaluate on testing data
        _learner.predict( _featuresMatrixTest, _predictionsVectorTest );

        if ( log )
        {
            String features = Data2d.toString( _featuresMatrixTrain );
            String predictions = Data2d.toString( _predictionsVector );
            String classTruth = Data2d.toString( _classTruthVector );

            System.out.println( "Features" );
            System.out.println( features );

            System.out.println( "Predictions" );
            System.out.println( predictions );

            System.out.println( "ClassTruth" );
            System.out.println( classTruth );

        }

        // set values to 1 if error, 0 if not
        _predictionsVector.approxEquals( _classTruthVector, _eps );
        _predictionsVectorTest.approxEquals( _classTruthVectorTest, _eps );

        if ( log ) {
            String error = Data2d.toString( _predictionsVector );
            System.out.println( "Error" );
            System.out.println( error );
        }


        // count how many errors - an error is where the diff between prediction and label is greater than eps
        double trainMeanError = _predictionsVector.mean();
        double testMeanError = _predictionsVectorTest.mean();

        System.out.println( "Model = " + _learner.getModelString() );
        System.out.println( "Training Accuracy = " + trainMeanError * 100 + "%" );
        System.out.println( "Testing Accuracy = " + testMeanError * 100 + "%" );

        assertTrue( trainMeanError > 0.89 );
        assertTrue( testMeanError > 0.89 );
    }

}
