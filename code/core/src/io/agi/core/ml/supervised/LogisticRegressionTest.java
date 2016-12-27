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

    float _eps = 0.0000001f;

    @Before
    public void setUp() throws Exception {

        // instantiate learner
        ObjectMap om = ObjectMap.GetInstance();
        _learner = new LogisticRegression( "logisticRegression", om );

        // setup learner
        SupervisedLearningConfig config = new SupervisedLearningConfig();
        config.setConstraintsViolation( 0.3f );
        _learner.setup( config );

        _learner.train( _featuresMatrixTrain, _classTruthVector );
        _modelString = _learner.modelString();

        assertTrue( _modelString != null );
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void loadModel() throws Exception {
        _learner.loadModel( _modelString );
        _modelString = _learner.modelString();

        assertTrue( _modelString != null );
    }

    /**
     * Tests loading the model and predicting
     * @throws Exception
     */
    @Test
    public void predict() throws Exception {

        _learner.predict( _featuresMatrixTest, _predictionsVectorTest );
        float error = calculateError( _predictionsVectorTest, _classTruthVector );

        assertTrue( error - 0.01 < _eps );
    }

    private float calculateError( Data predictionsVectorTest, Data classTruthVector ) {
        return 0;
    }

}