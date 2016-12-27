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
import libsvm.*;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;

/**
 * Created by gideon on 14/12/16.
 */
public class Svm extends NamedObject implements Callback, Supervised {

    protected static final Logger _logger = LogManager.getLogger();

    private SupervisedLearningConfig _config;
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
    public void loadModel( ) {
        String modelString = _config.getModelString();
        loadModel( modelString );
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

    /**
     * Save the model to config object.
     * @return
     */
    private String saveModel() {
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
     * Serialise model to a string and return.
     * @return
     * @throws Exception
     */
    public String modelString() throws Exception {

        String modelString = null;

        if ( _model != null ) {
            try {
                String filename = "temp_svmmodel";
                File modelFile = new File( filename );
                svm.svm_save_model( filename, _model );
                modelString = FileUtils.readFileToString( modelFile );
            }
            catch( IOException e ) {
                _logger.error( "Unable to save svm model." );
                _logger.error( e.toString(), e );
            }
        } else {
            String errorMessage = "Cannot to save LibLinear model before it is defined";
            _logger.error( errorMessage );
            throw new Exception( errorMessage );
        }

        return modelString;
    }

    public void train( Data featuresMatrix, Data classTruthVector ) {

        svm_parameter parameters = setupParameters();

        svm_problem problem = setupProblem( featuresMatrix, classTruthVector );

        _model = svm.svm_train( problem, parameters );

        saveModel();
    }

    public void predict( Data featuresMatrix, Data predictionsVector ) {

        DataSize datasetSize = featuresMatrix._dataSize;
        int m = datasetSize.getSize( DataSize.DIMENSION_Y );        // m = number of data points ---> should be 1
        int n = datasetSize.getSize( DataSize.DIMENSION_X );        // n = feature vector size

        svm_node[][] x = new svm_node[ n ][ m ];

        // iterate data points (vectors in the VectorSeries - each vector is a data point)
        for ( int i = 0; i < m ; ++i ) {

            // iterate dimensions of x (elements of the vector)
            for( int j = 0; j < n; j++ ) {

                float xij = featuresMatrix._values[ i * m + j ];

                x[ i ][ j ] = new svm_node();
                x[ i ][ j ].index = j+1;
                x[ i ][ j ].value = xij;
            }

            predictionsVector._values[ i ] = ( float ) svm.svm_predict( _model, x[i] );
        }
    }

    private svm_problem setupProblem( Data featuresMatrix, Data classTruthVector ) {

        DataSize datasetSize = featuresMatrix._dataSize;
        int m = datasetSize.getSize( DataSize.DIMENSION_Y );        // m = number of data points
        int n = datasetSize.getSize( DataSize.DIMENSION_X );        // n = feature vector size

        featuresMatrix.normalizeFeatures( );

        svm_problem prob = new svm_problem();
        prob.l = m;
        prob.y = new double[ prob.l ];
        prob.x = new svm_node[ prob.l ][ n ];

        // iterate data points (vectors in the VectorSeries - each vector is a data point)
        for ( int i = 0; i < m ; ++i ) {

            // iterate dimensions of x (elements of the vector)
            for ( int j = 0 ; j < n ; j++ ) {

                float classTruth = getClassTruth( classTruthVector, i );
                double xij = getFeatureValue( featuresMatrix, m, i, j );

                if ( xij == 0.f ) {
                    continue;
                }

                prob.x[ i ][ j ] = new svm_node();
                prob.x[ i ][ j ].index = j+1;
                prob.x[ i ][ j ].value = xij;
                prob.y[ i ] = classTruth;
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
        param.gamma = 0;
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
