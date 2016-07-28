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

import java.util.Collection;

/**
 * Used to perform an unsupervised test of the classification utility of some features.
 * Associates bits in a binary vector with classifications.
 *
 * Inputs:
 *
 * - Class: A config property containing a Scalar integer value representing the current class
 * - Features: A Data containing a set of binary features (1 = present, 0 = absent)
 *
 * Outputs:
 *
 *  2 Data:
 *  - ClassFeatures (A Data containing the frequencies of all features x all classes.)
 *  - ClassPrediction (A Data containing a score for each class, where higher values = more likely.)
 *
 *  3 Config properties:
 *  - classTruth: The true class label at the current time
 *  - classPredicted: The most likely class label given the model
 *  - classError: 1 if the truth and predicted don't match, else 0
 *
 * Options are online learning or all-time learning. The online learning has a forgetting factor which means it only
 * considers recent samples when associating features with class labels. This is useful when the input features are still
 * being learnt, as old feature associations may be inaccurate or misleading. However, once feature learning is complete,
 * this same class can be used to test the final configuration over all samples in the training set, and then to predict
 * over all samples in the testing set. Simply switch online learning off, to accumulate the count of all the features'
 * frequencies over the whole training set, then turn learning off in this entity to do the testing.
 *
 * The predicted class values are always available, just ignore them when you're not testing (unless you want to monitor
 * the learning process).
 *
 * Created by dave on 8/07/16.
 */
public class ClassFeaturesEntity extends Entity {

    public static final String ENTITY_TYPE = "class-features";

    public static final String FEATURES = "features";
    public static final String FEATURE_CLASS_COUNT = "feature-class-count";     // count of each of the features for a class
    public static final String CLASS_PREDICTION = "class-prediction";           // z

    public ClassFeaturesEntity( ObjectMap om, Node n, ModelEntity model ) {
        super( om, n, model );
    }

    public void getInputAttributes( Collection< String > attributes ) {
        attributes.add( FEATURES );
    }

    public void getOutputAttributes( Collection< String > attributes, DataFlags flags ) {
        attributes.add( FEATURE_CLASS_COUNT );
        attributes.add( CLASS_PREDICTION );
    }

    public Class getConfigClass() {
        return ClassFeaturesEntityConfig.class;
    }

    @Override
    protected void doUpdateSelf() {

        ClassFeaturesEntityConfig config = ( ClassFeaturesEntityConfig ) _config;

        Data featureData = getData( FEATURES );
        if( featureData == null ) {
            return;
        }

        // Get the input classification
        String stringClassValue = Framework.GetConfig( config.classEntityName, config.classConfigPath );
        Integer classValue = Integer.valueOf( stringClassValue );
        if( classValue == null ) {
            classValue = 0;
        }

        // Get all the parameters:
        int features = featureData.getSize();
        DataSize dataSizeFeatures = DataSize.create( features * config.classes );
        Data featureClassCount = getDataLazyResize( FEATURE_CLASS_COUNT, dataSizeFeatures );
        Data classPrediction = getDataLazyResize( CLASS_PREDICTION, DataSize.create( config.classes ) );

        if( config.reset ) {
            featureClassCount.set( 0.f );
        }

        // update counts
        if( config.learn ) {
            for( int i = 0; i < features; ++i ) {
                float r = featureData._values[ i ];
                if( r == 0.f ) {
                    continue;
                }

                if( config.onlineLearning ) {
                    // adjust all classes based on the most recent observation
                    for( int c = 0; c < config.classes; ++c ) {
                        float delta = 0.f;
                        if( c == classValue ) {
                            delta = 1.f;
                        }

                        int offset = i * config.classes + c;

                        float oldCount = featureClassCount._values[ offset ];
                        float newCount = delta * config.onlineLearningRate
                                       + ( 1.f - config.onlineLearningRate ) * oldCount;

                        featureClassCount._values[ offset ] = newCount;
                    }
                }
                else {
                    int offset = i * config.classes + classValue;

                    featureClassCount._values[ offset ] += 1.f;
                }
            }

            setData( FEATURE_CLASS_COUNT, featureClassCount );
        }

        // predict:
        classPrediction.set( 0.f );

        for( int i = 0; i < features; ++i ) {
            float r = featureData._values[ i ];
            if( r == 0.f ) {
                continue;
            }

            for( int c = 0; c < config.classes; ++c ) {
                int offset = i * config.classes + c;
                float count = featureClassCount._values[ offset ];
                classPrediction._values[ c ] += count;
            }
        }

        classPrediction.scaleSum( 1.f );
        setData( CLASS_PREDICTION, classPrediction );

        // calculate the result
        int maxAt = classPrediction.maxAt().offset();
        int error = 0;
        if( maxAt != classValue ) {
            error = 1;
        }

        // update the config based on the result:
        config.classPredicted = maxAt; // the predicted class given the input features
        config.classError = error; // 1 if the prediction didn't match the input class
        config.classTruth = classValue; // the value that was taken as input
    }

}
