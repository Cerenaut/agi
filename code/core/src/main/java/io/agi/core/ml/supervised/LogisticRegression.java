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
import java.util.ArrayList;

/**
 * Created by gideon on 23/12/16.
 */
public class LogisticRegression extends NamedObject implements Callback, SupervisedBatchTraining<LogisticRegressionConfig> {

    protected static final Logger _logger = LogManager.getLogger();

    private LogisticRegressionConfig _config;
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
    public void setup( LogisticRegressionConfig config ) {
        this._config = config;
        loadModel();    // load model if it exists in config object
    }

    @Override
    public void reset() {
        _model = null;
        saveModel();
    }

    @Override
    public void loadModel() {
        String modelString = _config.getModelString();
        if ( modelString != null && modelString.length() != 0 ) {
            loadModel( modelString );
        }
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

    @Override
    public String getModelString() {
        return _config.getModelString();
    }

    @Override
    public String saveModel() {
        String modelString = null;
        try {
            modelString = modelString();
        }
        catch( Exception e ) {
            _logger.error( "Could not save model to config." );
            _logger.error( e.toString(), e );
        }

        _config.setModelString( modelString );
        return modelString;
    }

    /**
     * Serialise the model into a string and return.
     * @return The model as a string.
     * @throws Exception
     */
    private String modelString() throws Exception {

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
            _logger.warn( errorMessage );
        }

        return modelString;
    }

    private Problem setupProblem( Data featuresMatrixTrain, Data classTruthVector ) {

        int m = SupervisedUtil.calcMFromFeatureMatrix( featuresMatrixTrain );   // m = number of data points
        int n = SupervisedUtil.calcNFromFeatureMatrix( featuresMatrixTrain );   // n = feature vector size

        Problem problem = new Problem();

        boolean addBias = _config.getAddBias();

        if ( addBias ) {
            problem.bias = 1.f;         // if >=0, there is a bias term     --> used, but non cli version does not automatically add the bias terms
            ++n;                        // adjust n
        }
        else {
            problem.bias = -1.f;        // if <0, no bias term
        }

        problem.l = m;              // number of training examples
        problem.n = n;              // number of features (including bias feature, if it exists)
        problem.x = setupFeatureNodes( featuresMatrixTrain, addBias );
        problem.y = new double[ m ];

        // set labels vector
        for( int r = 0; r < m; ++r ) {
            float label = SupervisedUtil.getClassTruth( classTruthVector, r );
            problem.y[ r ] = label;
        }

        return problem;
    }


    private Feature[][] setupFeatureNodes( Data featuresMatrixTrain, boolean addBias ) {

        int m = SupervisedUtil.calcMFromFeatureMatrix( featuresMatrixTrain );   // m = number of data points
        int n = SupervisedUtil.calcNFromFeatureMatrix( featuresMatrixTrain );   // n = feature vector size
        int n_maxIdx = n;

        if ( addBias ) {

            // IMPLEMENTATION NOTE:
            // An alternative implementation is to simply add a column to the features matrix here.
            // However, we have chosen to mirror the implementation of the cli version of LibLinear,
            // and add within the problem construction loop.
            // Predict() should be adjusted accordingly as well.
            //      Data biasColumn = new Data( 1, m );
            //      featuresMatrixTrain = Data2d.addColumn( featuresMatrixTrain, biasColumn );

            n++;                        // adjust n
        }

        Feature[][] x = new Feature[ m ][ ];

        // iterate data points (vectors in the VectorSeries - each vector is a data point)
        for( int mi = 0; mi < m; ++mi ) {

            ArrayList< FeatureNode > tempFeatureNodes = new ArrayList<>();

            // iterate dimensions of x (elements of the vector)
            // put feature nodes in a temp array
            for( int ni = 0; ni < n; ++ni ) {

                double xi;
                if( ni == n_maxIdx ) {
                    xi = 1.f;
                }
                else {
                    xi = SupervisedUtil.getFeatureValue( featuresMatrixTrain, n_maxIdx, mi, ni );
                }

                // use sparse representation
                if( xi == 0.f ) {
                    continue;
                }

                FeatureNode fn = new FeatureNode( ni + 1, xi );
                tempFeatureNodes.add( fn );
            }

            // add the feature nodes to the Problem data structure
            x[ mi ] = new FeatureNode[ tempFeatureNodes.size() ];
            for( int i = 0; i < tempFeatureNodes.size(); ++i ) {
                FeatureNode fn = tempFeatureNodes.get( i );
                x[ mi ][ i ] = fn;
            }
        }

        return x;
    }

    private Parameter setupParameters() {

        SolverType solver = SolverType.L2R_LR; // -s 0
        double eps = 0.001; // default stopping criteria

        // values from config
        float C = _config.getConstraintsViolation();        // cost of constraints violation

        Parameter parameter = new Parameter( solver, C, eps );
        return parameter;
    }

    @Override
    public void train( Data featuresMatrixTrain, Data classTruthVector ) {

        Parameter parameters = setupParameters();

        Problem problem = setupProblem( featuresMatrixTrain, classTruthVector );

        _model = Linear.train( problem, parameters );

        saveModel();    // save the model to config object
    }


//    // Placeholder for a convenience method to wrap the predict() method for the case of a single prediction
//    // This would require creating a temp Data (1d for predictionsVector) and pull out the prediction to return
//    public float predict( Data features ) {
//
//    }

    @Override
    public void predict( Data featuresMatrix, Data predictionsVector ) {

        int m = SupervisedUtil.calcMFromFeatureMatrix( featuresMatrix );   // m = number of data points
        boolean addBias = _config.getAddBias();

        Feature[][] x = setupFeatureNodes( featuresMatrix, addBias );

        for( int mi = 0; mi < m; ++mi ) {
            predictionsVector._values[ mi ] = ( float ) Linear.predict( _model, x[ mi ] );
        }
    }



}
