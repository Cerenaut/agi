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
import io.agi.core.data.Data2d;
import io.agi.core.data.DataSize;
import io.agi.core.orm.ObjectMap;
import io.agi.framework.DataFlags;
import io.agi.framework.Entity;
import io.agi.framework.Framework;
import io.agi.framework.Node;
import io.agi.framework.persistence.models.ModelEntity;

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
public class SupervisedLearningEntity extends Entity {

    public static final String ENTITY_TYPE = "supervised-learning-entity";

    public static final String LEARNING_MODE_ONLINE = "online";
    public static final String LEARNING_MODE_SAMPLE = "sample";
    public static final String LEARNING_MODE_BATCH = "batch";

    // This is the input data set implemented with a VectorSeries.
    // It is a series of data points (each one a feature vector).
    // It can be viewed as a matrix of size: 'number of features' x 'number of data points', or n x m in standard ML terminology
    public static final String FEATURES_BY_TIME = "features-by-time";
    public static final String LABELS_BY_TIME = "labels-by-time";

    public static final String INPUT_FEATURES = "input-features";
    public static final String INPUT_LABELS = "input-labels";

    public static final String OUTPUT_LABELS_TRUTH = "output-labels-truth";
    public static final String OUTPUT_LABELS_PREDICTED = "output-labels-predicted";
    public static final String OUTPUT_LABELS_ERROR = "output-labels-error";

    public SupervisedLearningEntity( ObjectMap om, Node n, ModelEntity model ) {
        super( om, n, model );
    }


    @Override
    public void getInputAttributes( Collection< String > attributes ) {
        attributes.add( INPUT_FEATURES );
        attributes.add( INPUT_LABELS );
    }

    @Override
    public void getOutputAttributes( Collection< String > attributes, DataFlags flags ) {
        SupervisedLearningEntityConfig config = ( SupervisedLearningEntityConfig ) _config;

        if( config.accumulateSamples ) {
            attributes.add( FEATURES_BY_TIME );
            attributes.add( LABELS_BY_TIME );
        }

        attributes.add( OUTPUT_LABELS_TRUTH );
        attributes.add( OUTPUT_LABELS_PREDICTED );
        attributes.add( OUTPUT_LABELS_ERROR );

        addFlags( attributes, flags );
    }

    /**
     * Override if you know more about the data and can compress it better, using more flags.
     *
     * @param attributes
     * @param flags
     */
    protected void addFlags( Collection< String > attributes, DataFlags flags ) {
        SupervisedLearningEntityConfig config = ( SupervisedLearningEntityConfig ) _config;

        if( config.accumulateSamples ) {
            flags.putFlag( FEATURES_BY_TIME, DataFlags.FLAG_NODE_CACHE );
            flags.putFlag( LABELS_BY_TIME, DataFlags.FLAG_NODE_CACHE );
        }
    }

    @Override
    public Class getConfigClass() {
        return SupervisedLearningEntityConfig.class;
    }

    protected void resetModel() {
        SupervisedLearningEntityConfig config = ( SupervisedLearningEntityConfig ) _config;

        config.learnBatchComplete = false;

        Data newOutputFeatures = new Data( 0 );
        setData( FEATURES_BY_TIME, newOutputFeatures );

        Data newOutputLabels = new Data( 0 );
        setData( LABELS_BY_TIME, newOutputLabels );
    }

    protected void loadModel( ) {
        // Implement as needed in subclasses
    }

    /**
     * Train the algorithm given the entire history of training samples provided.
     *
     * @param featuresTimeMatrix History of features
     * @param labelsTimeMatrix History of labels
     * @param features Nbr of features in each sample
     */
    protected void trainBatch( Data featuresTimeMatrix, Data labelsTimeMatrix, int features ) {
        // Implement as needed in subclasses
    }

    /**
     * Incrementally train the algorithm using a single sample update.
     *
     * @param features
     * @param labels
     */
    protected void trainSample( Data features, Data labels ) {
        // Implement as needed in subclasses
    }

    /**
     * Train the algorithm using an online update.
     *
     * @param features
     * @param labels
     */
    protected void trainOnline( Data features, Data labels ) {
        // Implement as needed in subclasses
    }

    /**
     * Generate a prediction from the model and copy it into predictedLabels.
     *
     * @param features
     * @param predictedLabels
     */
    protected void predict( Data features, Data predictedLabels ) {
        // Implement as needed in subclasses
    }

    protected void accumulate( Data features, Data labels ) {
        SupervisedLearningEntityConfig config = ( SupervisedLearningEntityConfig ) _config;

        Data oldOutputFeatures = getData( FEATURES_BY_TIME );
        Data newOutputFeatures = Data2d.accumulateVectors( features, config.learningPeriod, oldOutputFeatures );
        setData( FEATURES_BY_TIME, newOutputFeatures );

        Data oldOutputLabels = getData( LABELS_BY_TIME );
        Data newOutputLabels = Data2d.accumulateVectors( labels, config.learningPeriod, oldOutputLabels );
        setData( LABELS_BY_TIME, newOutputLabels );
    }

    /**
     * Evaluates the model and saves various stats about its performance as both config and Data formats.
     *
     * @param features
     * @param labels
     * @param predictedLabels
     */
    protected void evaluate( Data features, Data labels, Data predictedLabels ) {

        // Config outputs
        SupervisedLearningEntityConfig config = ( SupervisedLearningEntityConfig ) _config;

        Data errors;// = new Data( 1 );

        config.labelsError = 0;

        if( config.labelOneHot ) {
            int maxAt = labels.maxAt().offset();
            int maxAtPredicted = predictedLabels.maxAt().offset();
            if( maxAt != maxAtPredicted ) {
                config.labelsError = 1;
            }

            errors = new Data( 1 );
            errors._values[ 0 ] = config.labelsError;

            config.labelsPredicted = maxAtPredicted; // the predicted class given the input features
            config.labelsTruth = maxAt;
        }
        else {
            int elements = labels.getSize();

            errors = new Data( elements );

            for( int i = 0; i < elements; ++i ) {
                float labelValue = labels._values[ i ];
                float predictedLabelValue = predictedLabels._values[ i ];
                int errorValue = 0;
                if( labelValue != predictedLabelValue ) {
                    errorValue = 1;
                }

                config.labelsError += errorValue;
                errors._values[ i ] = errorValue;

                if( elements == 1 ) {
                    config.labelsPredicted = (int)predictedLabelValue; // the predicted class given the input features
                    config.labelsTruth = (int)labelValue;
                }
                else {
                    config.labelsPredicted = 0; // can't be expressed
                    config.labelsTruth = 0; // can't be expressed
                }
            }
        }

        // Data outputs
        setData( OUTPUT_LABELS_TRUTH, labels );
        setData( OUTPUT_LABELS_PREDICTED, predictedLabels );
        setData( OUTPUT_LABELS_ERROR, errors );
    }

    protected Data getLabelData() {
        SupervisedLearningEntityConfig config = ( SupervisedLearningEntityConfig ) _config;

        if( config.labelEntityName.length() == 0 ) {
            // get labels from supplied data
            Data labels = getData( INPUT_LABELS );
            return labels;
        }

        // get labels from config-path
        String stringLabelValue = Framework.GetConfig( config.labelEntityName, config.labelConfigPath );
        Integer labelValue = Integer.valueOf( stringLabelValue );

        // catch bad value
        if( labelValue == null ) {
            labelValue = 0;
        }

        // catch value outside range
        if( labelValue >= config.labelClasses ) {
            labelValue = 0;
        }

        // now decide if a 1-hot class vector or a single integer label
        if( config.labelOneHot ) {
            Data labels = new Data( config.labelClasses );
            labels._values[ labelValue ] = 1f;
            return labels;
        }

        // return a single element with the appropriate integer value
        Data labels = new Data( 1 );
        labels._values[ 0 ] = labelValue;

        return labels;
    }

    @Override
    protected void doUpdateSelf() {

        // Behaviour on reset:
        // clear the accumulated

        // Behaviour during training:
        // 1 2 - takes in a single label (or vector of labels)
        // 1   - appends it to a vector of labels x time (training)
        // 1 2 - takes in a single vector of features.
        // 1   - appends it to a vector of features x time


        // 1 2 - does a retraining on all data
        // 1 2 - makes a prediction of the label from the model
        // 1 2 - computes whether the prediction is correct
        // 1 2 - outputs predictions[s] error mask and truths (these are externally logged over time), all samples not just training samples

        // Behaviour during testing:

        SupervisedLearningEntityConfig config = ( SupervisedLearningEntityConfig ) _config;

        Data features = getData( INPUT_FEATURES ); // only appended when learning enabled
        if( features == null ) {
            return;
        }

        Data labels = getLabelData(); // always returns a vector with 1 or more elements

        loadModel( ); // load a saved model, ie from persistence

        if( config.learn ) {

            if( config.accumulateSamples ) {
                accumulate( features, labels );
            }

            if( config.learningMode.equalsIgnoreCase( LEARNING_MODE_ONLINE ) ) {
                // train incrementally using one latest sample, continuously forgetting older samples
                trainOnline( features, labels );
            }
            else if( config.learningMode.equalsIgnoreCase( LEARNING_MODE_SAMPLE ) ) {
                // train incrementally using one latest sample
                trainSample( features, labels );
            }
            else if( config.learningMode.equalsIgnoreCase( LEARNING_MODE_BATCH ) ) {
                // re-train completely using a history of samples
                if( config.learnBatchOnce && config.learnBatchComplete ) {
                    // skip
                }
                else { // either learnBatch is "every step" or we didn't do it yet
                    Data outputFeatures = getData( FEATURES_BY_TIME );
                    Data outputLabels = getData( FEATURES_BY_TIME );

                    trainBatch( outputFeatures, outputLabels, features.getSize() );
                    config.learnBatchComplete = true;
                }
            }
        }

        if ( config.reset ) {
            resetModel(); // reset the model. Will be saved later
        }

        Data predictedLabels = new Data( new DataSize( labels._dataSize ) );

        if( config.predict ) {
            predict( features, predictedLabels );
        }

        evaluate( features, labels, predictedLabels );

    }


}
