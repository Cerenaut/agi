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

package io.agi.framework.entities;

import io.agi.core.data.Data;
import io.agi.core.data.DataSize;
import io.agi.core.orm.ObjectMap;
import io.agi.framework.DataFlags;
import io.agi.framework.Entity;
import io.agi.framework.Framework;
import io.agi.framework.Node;
import io.agi.framework.persistence.models.ModelEntity;
import libsvm.*;

import java.util.Collection;
import java.util.Vector;

/**
 * This is a 'learning analytics' Entity
 * These Entities have two main phases, 'learn' on and off
 * <p>
 * In Learn=on (Training) phase - it simply collects the data that it needs (via a VectorSeriesEntity that is an Input)
 * In Learn=off (Testing) phase - train SVM if not already trained, and give predictions (i.e. only train once then predict)
 * <p>
 * <p>
 * <p>
 * Created by gideon on 11/07/2016.
 */
public class SVMEntity extends Entity {

    public static final String ENTITY_TYPE = "svm-entity";

    // This is a VectorSeries. It comprises the input data set.
    // It is effectively a vector of data points (each one a feature vector)
    // It can also be viewed as a matrix, of size: features x number of data points, or m x n in standard ML terminology
    public static final String ACCUMULATED_FEATURES = "accumulated-features";

    public static final String CLASS_PREDICTION = "class-prediction";

    public SVMEntity( ObjectMap om, Node n, ModelEntity model ) {
        super( om, n, model );
    }

    @Override
    public void getInputAttributes( Collection< String > attributes ) {
        attributes.add( ACCUMULATED_FEATURES );
    }

    @Override
    public void getOutputAttributes( Collection< String > attributes, DataFlags flags ) {
        attributes.add( CLASS_PREDICTION );
    }

    @Override
    public Class getConfigClass() {
        return SVMEntityConfig.class;
    }

    class point {
        point( double x, double y, byte value ) {
            this.x = x;
            this.y = y;
            this.value = value;
        }

        double x, y;
        byte value;
    }


    @Override
    protected void doUpdateSelf() {

        SVMEntityConfig config = ( SVMEntityConfig ) _config;

        Data accumulatedFeatureData = getData( ACCUMULATED_FEATURES );
        if ( accumulatedFeatureData == null ) {
            return;
        }

        // Get the input classification
        String stringClassValue = Framework.GetConfig( config.classEntityName, config.classConfigPath );
        Integer classValue = Integer.valueOf( stringClassValue );
        if ( classValue == null ) {
            classValue = 0;
        }

        // Get all the parameters:
        Data classPrediction = getDataLazyResize( CLASS_PREDICTION, DataSize.create( config.classes ) );

        if ( config.reset ) {
            svmReset();
        }


        // 1) collect data (training set)
        // ----------------------------------------------------------------------
        if ( config.learn ) {
            if ( config.onlineLearning ) {
                // could be: add a data point to window, that will be used as training data for SVM
                // NOT IMPLEMENTED
            }
            else {
                // add a data point
                // taken care of by the VectorSeries that inputs to ACCUMULATED_FEATURES
            }
        }

        classPrediction.set( 0.f );


        // 2) predict (testing set)
        // ----------------------------------------------------------------------
        int prediction = 0;
        if ( !config.learn ) {

            if ( config.trained == false ) {
                svmTrain( accumulatedFeatureData, classValue );
                config.trained = true;
            }
            else {
                svmloadSavedModel();
            }

            prediction = svmPredict();
        }

        int error = 1;
        if ( classValue == prediction ) {
            error = 0;
        }

        // update the config based on the result:
        config.classPredicted = prediction;     // the predicted class given the input features
        config.classError = error;              // 1 if the prediction didn't match the input class
        config.classTruth = classValue;         // the value that was taken as input

    }

    private void svmReset() {

    }

    private void svmloadSavedModel() {

    }


    public int svmPredict() {
        return 0;
    }

    public void svmTrain( Data accumulatedFeatureData, int classValue ) {

        SVMEntityConfig config = ( SVMEntityConfig ) _config;

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
        param.C = config.C;

        DataSize datasetSize = accumulatedFeatureData._dataSize;

        int n = datasetSize.getSize( DataSize.DIMENSION_Y );        // n = number of data points
        int m = datasetSize.getSize( DataSize.DIMENSION_X );        // m = feature vector size

        // build problem
        svm_problem prob = new svm_problem();
        prob.l = n;
        prob.y = new double[ prob.l ];
        prob.x = new svm_node[ prob.l ][ m ];

        // iterate data points (vectors in the VectorSeries - each vector is a data point)
        for ( int i = 0; i < n ; ++i ) {

            // iterate dimensions of x (elements of the vector)
            for ( int j = 0 ; j < m ; j++) {

                float xij = accumulatedFeatureData._values[ i * n + j];

                if ( xij == 0.f ) {
                    continue;
                }

                prob.x[ i ][ j ] = new svm_node();
                prob.x[ i ][ j ].index = j+1;
                prob.x[ i ][ j ].value = xij;
                prob.y[ i ] = classValue;
            }
        }

        // build model & classify
        svm_model model = svm.svm_train( prob, param );


//        if ( param.kernel_type == svm_parameter.PRECOMPUTED ) {
//        }
//        else if ( param.svm_type == svm_parameter.EPSILON_SVR ||
//                param.svm_type == svm_parameter.NU_SVR ) {
//            if ( param.gamma == 0 )
//                param.gamma = 1;
//            prob.x = new svm_node[ prob.l ][ 1 ];
//            for ( int i = 0; i < prob.l; i++ ) {
//                point p = point_list.elementAt( i );
//                prob.x[ i ][ 0 ] = new svm_node();
//                prob.x[ i ][ 0 ].index = 1;
//                prob.x[ i ][ 0 ].value = p.x;
//                prob.y[ i ] = p.y;
//            }
//
//            // build model & classify
//            svm_model model = svm.svm_train( prob, param );
//            svm_node[] x = new svm_node[ 1 ];
//            x[ 0 ] = new svm_node();
//            x[ 0 ].index = 1;
//        }
//        else {
//            if ( param.gamma == 0 ) {
//                param.gamma = 0.5;
//            }
//
//            prob.x = new svm_node[ prob.l ][ 2 ];
//            for ( int i = 0; i < prob.l; i++ ) {
//                point p = point_list.elementAt( i );
//                prob.x[ i ][ 0 ] = new svm_node();
//                prob.x[ i ][ 0 ].index = 1;
//                prob.x[ i ][ 0 ].value = p.x;
//                prob.x[ i ][ 1 ] = new svm_node();
//                prob.x[ i ][ 1 ].index = 2;
//                prob.x[ i ][ 1 ].value = p.y;
//                prob.y[ i ] = p.value;
//            }
//
//            // build model & classify
//            svm_model model = svm.svm_train( prob, param );
//            svm_node[] x = new svm_node[ 2 ];
//            x[ 0 ] = new svm_node();
//            x[ 1 ] = new svm_node();
//            x[ 0 ].index = 1;
//            x[ 1 ].index = 2;
//        }

    }
}
