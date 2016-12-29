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

package test.resources;

import io.agi.core.data.Data;
import io.agi.core.math.FastRandom;
import io.agi.core.ml.supervised.LogisticRegression;
import io.agi.core.ml.supervised.SupervisedLearningConfig;
import io.agi.core.orm.ObjectMap;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by gideon on 27/12/16.
 */
public class LogisticRegressionTest {

    LogisticRegression _learner = null;
    String _modelString;

    Data _featuresMatrixTrain = null;
    Data _classTruthVector = null;

    Data _featuresMatrixTest = null;
    Data _predictionsVectorTest = null;
    Data _classTruthVectorTest = null;

    float _eps = 0.0000001f;
    private ObjectMap _om;
    private FastRandom _r;

    @Before
    public void setUp() throws Exception {

        // get data from file
        String filePath = "src/test/resources/supervised-linear-data.csv";
        _featuresMatrixTrain = Data.createFromCSV( filePath, 0, 1 );
        _classTruthVector = Data.createFromCSV( filePath, 2, 2 );

        filePath = "src/test/resources/supervised-linear-data-test.csv";
        _featuresMatrixTest = Data.createFromCSV( filePath, 0, 1 );
        _predictionsVectorTest = new Data( _classTruthVector._dataSize );
        _classTruthVectorTest = new Data( _classTruthVector._dataSize.getSize( "Y" ) );

        // instantiate learner
        _om = ObjectMap.GetInstance();
        _learner = new LogisticRegression( "logisticRegression", _om );

        // setup learner
        _r = new FastRandom(  );
        SupervisedLearningConfig config = new SupervisedLearningConfig( );
        config.setup( _om, "test-logistic-config", _r, 0.01f);
        _learner.setup( config );

        // train model
        _learner.train( _featuresMatrixTrain, _classTruthVector );
        _modelString = _learner.modelString();
        assertTrue( _modelString != null );
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

        // test on the training data (for unit test this is good, but not the way to test the effectiveness of ml algorithm)
        _learner.predict( _featuresMatrixTrain, _predictionsVectorTest );

        // set values to 1 if error, 0 if not
        _predictionsVectorTest.approxEquals( _classTruthVector, _eps );

        // count how many errors - an error is where the diff between prediction and label is greater than eps
        double meanError = _predictionsVectorTest.mean();

        System.out.println( "Accuracy = " + meanError * 100 + "%" );

        assertTrue( meanError > 0.89 );
    }

}