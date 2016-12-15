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
import io.agi.core.ml.supervised.Svm;
import io.agi.core.ml.supervised.SvmConfig;
import io.agi.core.orm.Keys;
import io.agi.core.orm.ObjectMap;
import io.agi.framework.DataFlags;
import io.agi.framework.Entity;
import io.agi.framework.Framework;
import io.agi.framework.Node;
import io.agi.framework.persistence.models.ModelEntity;
import libsvm.*;

import java.util.Collection;

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

    // This is the input data set implemented with a VectorSeries.
    // It is a series of data points (each one a feature vector).
    // It can be viewed as a matrix of size: 'number of features' x 'number of data points', or n x m in standard ML terminology
    public static final String FEATURES_MATRIX = "features-matrix";

    // The labels in the input data set (y)
    public static final String CLASS_TRUTH_VECTOR = "class-truth-vector";

    public static final String CLASS_PREDICTION = "class-prediction";


    public SVMEntity( ObjectMap om, Node n, ModelEntity model ) {
        super( om, n, model );
    }

    @Override
    public void getInputAttributes( Collection< String > attributes ) {
        attributes.add( FEATURES_MATRIX );
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

        // Get all the parameters:
        SVMEntityConfig config = ( SVMEntityConfig ) _config;

        // Create the config object:
        SvmConfig svmConfig = new SvmConfig();
        svmConfig.setup( config.C );

        // Create the implementing object itself, and copy data from persistence into it:
        Svm svm = new Svm( getName(), _om );
        svm.setup( svmConfig );

        Data featuresMatrix = getData( FEATURES_MATRIX );
        if ( featuresMatrix == null ) {
            return;
        }

        Data classTruthVector = getData( CLASS_TRUTH_VECTOR );
        if ( classTruthVector == null ) {
            return;
        }

        // Get the input classification (for this time step)
        String stringClassValue = Framework.GetConfig( config.classEntityName, config.classConfigPath );
        Integer classValue = Integer.valueOf( stringClassValue );
        if ( classValue == null ) {
            classValue = 0;
        }

        // Get all the parameters:
        Data classPrediction = getDataLazyResize( CLASS_PREDICTION, DataSize.create( config.classes ) );

        if ( config.reset ) {
            svm.reset();
        }


        // 1) collect data (training set)
        // ----------------------------------------------------------------------
        if ( config.learn ) {
            if ( config.onlineLearning ) {
                // could be: add a data point to window, that will be used as training data for SVM
                // NOT IMPLEMENTED
            }
            else {
                // this could be 'add a data point',
                // but taken care of by the VectorSeries that inputs to FEATURES_MATRIX
            }
        }

        classPrediction.set( 0.f );


        // 2) predict (testing set)
        // ----------------------------------------------------------------------
        int prediction = 0;
        if ( !config.learn ) {

            // not in training mode, so if not already trained, build a model
            if ( !config.trained ) {
                svm.train( featuresMatrix, classTruthVector );
                config.trained = true;
            }
            // or else load the saved model
            else {
                svm.loadSavedModel();
            }

            // and make the prediction
            prediction = svm.predict();
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


}
