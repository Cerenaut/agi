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
import io.agi.core.data.DataSize;
import io.agi.core.orm.Callback;
import io.agi.core.orm.NamedObject;
import io.agi.core.orm.ObjectMap;

import de.bwaldvogel.liblinear.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * Created by gideon on 23/12/16.
 */
public class LogisticRegression extends NamedObject implements Callback, Supervised {

    protected static final Logger _logger = LogManager.getLogger();

    private SupervisedLearningConfig _config;
    private Model _model = null;

    public LogisticRegression( String name, ObjectMap om ) {
        super( name, om );
    }

    @Override
    public void call() {
        update();
    }

    public void update() {

    }

    @Override
    public void setup( SupervisedLearningConfig config ) {
        this._config = config;
        loadModel();
    }

    @Override
    public void reset() {
        _model = null;
        saveModel();
    }

    @Override
    public void loadModel() {
        String modelString = _config.getModelString();
        loadModel( modelString );
    }

    @Override
    public void loadModel( String modelString ) {

        Reader stringReader = new StringReader( modelString );
        try {
            _model = Model.load( stringReader );
            saveModel();
        }
        catch( IOException e ) {
            _logger.error( "Unable to load LibLinear model." );
            _logger.error( e.toString(), e );
        }
    }

    private void saveModel() {
        String modelString = null;
        try {
            modelString = modelString();
        }
        catch( Exception e ) {
            _logger.error( "Could not save model to config." );
            _logger.error( e.toString(), e );
        }

        _config.setModelString( modelString );
    }

    public String modelString() throws Exception {

        String modelString = null;
        if( _model != null ) {
            StringWriter writer = new StringWriter(  );
            try {
                _model.save( writer );
                modelString = writer.toString();
            }
            catch( IOException e ) {
                _logger.error( "Unable to save LibLinear model." );
                _logger.error( e.toString(), e );
            }
        } else {
            String errorMessage = "Cannot save LibLinear model as it is undefined";
            _logger.error( errorMessage );
            throw new Exception( errorMessage );
        }

        return modelString;
    }

    @Override
    public void train( Data featuresMatrix, Data classTruthVector ) {

        Parameter parameters = setupParameters();

        Problem problem = setupProblem( featuresMatrix, classTruthVector );

        _model = Linear.train( problem, parameters );
    }


//    // Placeholder for a convenience method to wrap the predict() method for the case of a single prediction
//    // This would require creating a temp Data (1d for predictionsVector) and pull out the prediction to return
//    public float predict( Data features ) {
//
//    }

    @Override
    public void predict( Data featuresMatrix, Data predictionsVector ) {

        DataSize datasetSize = featuresMatrix._dataSize;
        int m = datasetSize.getSize( DataSize.DIMENSION_Y );        // m = number of data points
        int n = datasetSize.getSize( DataSize.DIMENSION_X );        // n = feature vector size

        Feature[][] x = new Feature[ n ][ m ];

        // convert input to Feature instance, then predict
        // iterate data points (vectors in the VectorSeries - each vector is a data point)
        for( int i = 0; i < m; ++i ) {

            // iterate dimensions of x (elements of the vector)
            for( int j = 0; j < n; j++ ) {
                double xij = getFeatureValue( featuresMatrix, m, i, j );
                x[ i ][ j ] = new FeatureNode( j+1, xij );
            }

            predictionsVector._values[ i ] = ( float ) Linear.predict( _model, x[ i ] );
        }
    }

    private Problem setupProblem( Data featuresMatrix, Data classTruthVector ) {

        DataSize datasetSize = featuresMatrix._dataSize;
        int m = datasetSize.getSize( DataSize.DIMENSION_Y );        // m = number of data points
        int n = datasetSize.getSize( DataSize.DIMENSION_X );        // n = feature vector size

        featuresMatrix.normalizeFeatures();

        Problem problem = new Problem();
        problem.l = m; // number of training examples
        problem.n = n; // number of features

        // iterate data points (vectors in the VectorSeries - each vector is a data point)
        for( int i = 0; i < n; ++i ) {

            // iterate dimensions of x (elements of the vector)
            for( int j = 0; j < m; j++ ) {

                float classTruth = getClassTruth( classTruthVector, i );
                double xij = getFeatureValue( featuresMatrix, m, i, j );

                if( xij == 0.f ) {
                    continue;
                }

                problem.x[ i ][ j ] = new FeatureNode( j + 1, xij );
                problem.y[ i ] = classTruth;
            }
        }

        return problem;
    }

    private Parameter setupParameters() {

        SolverType solver = SolverType.L2R_LR; // -s 0
        double eps = 0.01; // stopping criteria

        // values from config
        float C = _config.getConstraintsViolation();        // cost of constraints violation

        Parameter parameter = new Parameter( solver, C, eps );
        return parameter;
    }

    // convenience method to get the specific value from featuresMatrix
    private double getFeatureValue( Data featuresMatrix, int datasetSize, int datapointIndex, int featureIndex ) {
        float value = featuresMatrix._values[ datapointIndex * datasetSize + featureIndex ];
        return value;
    }

    // convenience method to get the truth label from classTruthVector
    private float getClassTruth( Data classTruthVector, int datapointIndex ) {
        float value = classTruthVector._values[ datapointIndex ];
        return value;
    }

}
