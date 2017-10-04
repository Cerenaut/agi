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
 * Created by abdel on 14/09/17.
 */
@RunWith(value = Parameterized.class)
public class SvmTest {

    private Svm _learner = null;

    private Data _featuresMatrixTrain = null;
    private Data _predictionsVector = null;
    private Data _classTruthVector = null;

    private Data _featuresMatrixTest = null;
    private Data _predictionsVectorTest = null;
    private Data _classTruthVectorTest = null;

    // Parameters
    private String trainPath;
    private String testPath;
    private int featuresIdxMin;
    private int featuresIdxMax;
    private int classTruthIdx;

    /**
     * Sets up the parameters for the test
     *
     * @param trainPath The path to the training dataset in src/test/resources/
     * @param testPath The path to the testing dataset in src/test/resources/
     * @param featuresIdxMin The column ID where features start
     * @param featuresIdxMax The column ID where features end
     * @param classTruthIdx The column ID of the target (y) / class truth
     */
    public SvmTest(String trainPath, String testPath, int featuresIdxMin, int featuresIdxMax, int classTruthIdx) {
        this.trainPath = trainPath;
        this.testPath = testPath;
        this.featuresIdxMin = featuresIdxMin;
        this.featuresIdxMax = featuresIdxMax;
        this.classTruthIdx = classTruthIdx;
    }

    /**
     * Define the parameters to be used in the test
     *
     * @return Collection The defined parameters
     */
    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"spectf.train.csv", "spectf.test.csv", 1, 44, 0}
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
        String filePath = "src/test/resources/" + trainPath;
        _featuresMatrixTrain = Data2d.createFromCSV( filePath, featuresIdxMin, featuresIdxMax);
        _classTruthVector = Data2d.createFromCSV( filePath, classTruthIdx, classTruthIdx );

        filePath = "src/test/resources/" + testPath;
        _featuresMatrixTest = Data2d.createFromCSV( filePath, featuresIdxMin, featuresIdxMax );
        _classTruthVectorTest = Data2d.createFromCSV( filePath, classTruthIdx, classTruthIdx );

        _predictionsVector = new Data( _classTruthVector._dataSize );
        _predictionsVectorTest = new Data( _classTruthVectorTest._dataSize );

        // instantiate learner
        ObjectMap om = ObjectMap.GetInstance();
        _learner = new Svm( "svm", om );

        // setup learner
        FastRandom r = new FastRandom();
        SupervisedBatchTrainingConfig config = new SupervisedBatchTrainingConfig();
        config.setup( om, "test-svm-config", r, "", true, 100f );
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
        float _eps = 0.0000001f;

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
