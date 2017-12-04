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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;

/**
 * Created by gideon on 27/12/16.
 */
@RunWith(value = Parameterized.class)
public class LogisticRegressionTest {

    private LogisticRegression _learner = null;

    private Data _featuresMatrixTrain = null;
    private Data _predictionsVector = null;
    private Data _classTruthVector = null;

    private Data _featuresMatrixTest = null;
    private Data _predictionsVectorTest = null;
    private Data _classTruthVectorTest = null;

    // Parameters
    private String _trainPath;
    private String _testPath;
    private int _featuresIdxMin;
    private int _featuresIdxMax;
    private int _classTruthIdx;
    private double _trainAccuracy;
    private double _testAccuracy;

    /**
     * Sets up the parameters for the test
     *
     * @param trainPath The path to the training dataset in src/test/resources/
     * @param testPath The path to the testing dataset in src/test/resources/
     * @param featuresIdxMin The column ID where features start
     * @param featuresIdxMax The column ID where features end
     * @param classTruthIdx The column ID of the target (y) / class truth
     * @param trainAccuracy The expected training accuracy for assertion
     * @param testAccuracy The expected test accuracy for assertion
     */
    public LogisticRegressionTest(String trainPath, String testPath, int featuresIdxMin, int featuresIdxMax, int classTruthIdx, double trainAccuracy, double testAccuracy) {
        this._trainPath = trainPath;
        this._testPath = testPath;
        this._featuresIdxMin = featuresIdxMin;
        this._featuresIdxMax = featuresIdxMax;
        this._classTruthIdx = classTruthIdx;
        this._trainAccuracy = trainAccuracy;
        this._testAccuracy = testAccuracy;
    }

    /**
     * Define the parameters to be used in the test
     *
     * @return Collection The defined parameters
     */
    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                // trainPath, testPath, featuresIdxMin, featuresIdxMax, classTruthIdx, trainAccuracy, testAccuracy
                {"iris.train.csv", "iris.test.csv", 0, 3, 4, 0.99, 0.95},
                {"spectf.train.csv", "spectf.test.csv", 1, 44, 0, 1, 0.59},
                {"skin.train.sample.csv", "skin.test.sample.csv", 0, 2, 3, 0.92, 0.92}
        });
    }

    @Test
    public void evaluate() {
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

    /**
     * Sets up the model and trains it on the dataset
     *
     * @throws Exception Failed to setup or train the model
     */
    public void setUp() throws Exception {

        // get data from file
        String filePath = "src/test/resources/" + _trainPath;
        _featuresMatrixTrain = Data2d.createFromCSV( filePath, _featuresIdxMin, _featuresIdxMax);
        _classTruthVector = Data2d.createFromCSV( filePath, _classTruthIdx, _classTruthIdx );

        filePath = "src/test/resources/" + _testPath;
        _featuresMatrixTest = Data2d.createFromCSV( filePath, _featuresIdxMin, _featuresIdxMax );
        _classTruthVectorTest = Data2d.createFromCSV( filePath, _classTruthIdx, _classTruthIdx );

        _predictionsVector = new Data( _classTruthVector._dataSize );
        _predictionsVectorTest = new Data( _classTruthVectorTest._dataSize );

        // instantiate learner
        ObjectMap om = ObjectMap.GetInstance();
        _learner = new LogisticRegression( "logisticRegression", om );

        // setup learner
        FastRandom r = new FastRandom(  );
        LogisticRegressionConfig config = new LogisticRegressionConfig( );
        config.setup( om, "test-logistic-config", r, "", 100f, true);
        _learner.setup( config );

        // train model
        _learner.train( _featuresMatrixTrain, _classTruthVector );
        String modelString = _learner.getModelString();
        assertTrue( modelString != null );
    }

    /**
     * Tests loading the model and predicting
     *
     * @throws Exception Failed to make predictions
     */
    private void predict() throws Exception {

        boolean log = false;

        // Evaluate on training data
        _learner.predict( _featuresMatrixTrain, _predictionsVector );

        // Evaluate on testing data
        _learner.predict( _featuresMatrixTest, _predictionsVectorTest );

        if ( log )
        {
            String features = Data2d.toString( _featuresMatrixTest );
            String predictions = Data2d.toString( _predictionsVectorTest );
            String classTruth = Data2d.toString( _classTruthVectorTest );

            System.out.println( "Features" );
            System.out.println( features );

            System.out.println( "Predictions" );
            System.out.println( predictions );

            System.out.println( "ClassTruth" );
            System.out.println( classTruth );
        }

        // set values to 1 if correct, 0 if not
        _predictionsVector.equals( _classTruthVector );
        _predictionsVectorTest.equals( _classTruthVectorTest );

        if ( log ) {
            String error = Data2d.toString( _predictionsVectorTest );
            System.out.println( "Error" );
            System.out.println( error );
        }

        // total correct values / total values
        double trainAccuracy = _predictionsVector.mean();
        double testAccuracy = _predictionsVectorTest.mean();

        System.out.println( "Model = " + _learner.getModelString() );
        System.out.println( "Training Accuracy = " + trainAccuracy * 100 + "%" );
        System.out.println( "Testing Accuracy = " + testAccuracy * 100 + "%" );

        assertTrue( trainAccuracy >= _trainAccuracy );
        assertTrue( testAccuracy >= _testAccuracy );
    }

}