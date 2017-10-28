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
import libsvm.*;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;

/**
 * Created by gideon on 14/12/16.
 */
public class Svm extends NamedObject implements Callback, SupervisedBatchTraining {

    protected static final Logger _logger = LogManager.getLogger();

    private SupervisedBatchTrainingConfig _config;
    svm_model _model = null;

    public Svm( String name, ObjectMap om ) {
        super( name, om );
    }

    @Override
    public void call() {
        update();
    }

    public void update() {

    }

    @Override
    public void setup( SupervisedBatchTrainingConfig config ) {
        this._config = config;
        loadModel();    // load model if it exists in config object
    }

    @Override
    public void reset() {
        _model = null;
        saveModel();
    }

    @Override
    public void loadModel( ) {
        String modelString = _config.getModelString();
        if ( modelString != null && modelString.length() != 0 ) {
            loadModel( modelString );
        }
    }

    @Override
    public void loadModel( String modelString ) {
        try {
            _model = svm.svm_load_model( modelString );
            saveModel();
        }
        catch( IOException e ) {
            _logger.error( "Unable to load svm model." );
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

        if ( _model != null ) {
            try {
                String filename = "temp_svm.model";
                File modelFile = new File( filename );
                svm.svm_save_model( filename, _model );
                modelString = FileUtils.readFileToString( modelFile );
            }
            catch( IOException e ) {
                _logger.error( "Unable to save svm model." );
                _logger.error( e.toString(), e );
            }
        } else {
            String errorMessage = "Cannot to save svm model before it is defined";
            _logger.error( errorMessage );
            throw new Exception( errorMessage );
        }

        return modelString;
    }

    public void train( Data featuresMatrix, Data classTruthVector ) {

        int n = SupervisedUtil.calcNFromFeatureMatrix( featuresMatrix );

        svm_parameter parameters = setupParameters();

        svm_problem problem = setupProblem( featuresMatrix, classTruthVector );

        _model = svm.svm_train( problem, parameters );

        saveModel();    // save the model to config object
    }

    public void predict( Data featuresMatrixTrain, Data predictionsVector ) {

        int m = SupervisedUtil.calcMFromFeatureMatrix( featuresMatrixTrain );   // m = number of data points
        int n = SupervisedUtil.calcNFromFeatureMatrix( featuresMatrixTrain );   // n = feature vector size

        svm_node[][] x = new svm_node[ m ][ n ];

        // iterate data points (vectors in the VectorSeries - each vector is a data point)
        for ( int j = 0; j < m ; ++j ) {

            // iterate dimensions of x (elements of the vector)
            for( int i = 0; i < n; ++i ) {

                double xi = SupervisedUtil.getFeatureValue( featuresMatrixTrain, n, j, i );

                x[ j ][ i ] = new svm_node();
                x[ j ][ i ].index = i + 1;
                x[ j ][ i ].value = xi;
            }

            predictionsVector._values[ j ] = ( float ) svm.svm_predict( _model, x[ j ] );
        }
    }

    private svm_problem setupProblem( Data featuresMatrix, Data classTruthVector ) {

        int m = SupervisedUtil.calcMFromFeatureMatrix( featuresMatrix );   // m = number of data points
        int n = SupervisedUtil.calcNFromFeatureMatrix( featuresMatrix );   // n = feature vector size

        svm_problem prob = new svm_problem();
        prob.l = m;
        prob.y = new double[ prob.l ];
        prob.x = new svm_node[ prob.l ][ n ];

        // iterate data points (vectors in the VectorSeries - each vector is a data point)
        for ( int j = 0; j < m ; ++j ) {

            // iterate dimensions of x (elements of the vector)
            for ( int i = 0 ; i < n ; ++i ) {

                float classTruth = SupervisedUtil.getClassTruth( classTruthVector, j );
                double xi = SupervisedUtil.getFeatureValue( featuresMatrix, n, j, i );

                prob.x[ j ][ i ] = new svm_node();
                prob.x[ j ][ i ].index = i + 1;
                prob.x[ j ][ i ].value = xi;
                prob.y[ j ] = classTruth;
            }
        }



        return prob;
    }

    private svm_parameter setupParameters() {
        svm_parameter param = new svm_parameter();

        // default values
        param.svm_type = svm_parameter.C_SVC;
        param.kernel_type = svm_parameter.RBF;
        param.degree = 3;
        param.gamma = 0.1;
        param.coef0 = 0;
        param.nu = 0.5;
        param.cache_size = 40;
        param.eps = 1e-3;
        param.p = 0.1;
        param.shrinking = 1;
        param.probability = 0;
        param.nr_weight = 0;
        param.weight_label = new int[ 0 ];
        param.weight = new double[ 0 ];

        // values from config
        param.C = _config.getConstraintsViolation();

        return param;
    }

}
