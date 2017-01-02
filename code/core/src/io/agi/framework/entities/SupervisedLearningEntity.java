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
 * This is a 'learning analytics' Entity which uses a provided label to classify input and measure correct label prediction.
 *
 * There are 2 inputs: Features, and Labels. Features are real or binary values that represent the state of some algorithm
 * or system. Labels are the output we desire the supervised learning entity to produce given a particular sample.
 *
 * It can do any combination of training and testing simultaneously. Normal use case is train +test, then test only.
 * This allows you to view progress on incomplete training (and e.g. abort early).
 *
 * It optionally can accumulate the training data into a batch, and train once at the end.
 *
 * It has 3 modes for accumulating data:
 *
 * a) ONLINE
 * b) SAMPLE
 * c) BATCH
 *
 * In Online mode samples are presented one at a time. It is assumed that the input features may change in meaning over
 * time, therefore some amount of forgetting is required / implied.
 *
 * In Sample mode it is the same as Online except no forgetting factor is required. Every sample is judged to be valid
 * which occurs if the algorithm generating the samples is fixed and no longer learning.
 *
 * In Batch mode, the algorithm is trained on a large number of samples which are supplied and used in one go.
 *
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

    /**
     * Default implementation clears accumulated training data; override to clear any additional model data.
     */
    protected void resetModel() {
        SupervisedLearningEntityConfig config = ( SupervisedLearningEntityConfig ) _config;

        config.learnBatchComplete = false;

        // clear the accumulated data
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

    /**
     * The purpose of this method is to obtain a vector of labels from EITHER config properties or from a Data reference.
     *
     * If the label comes from a config property, we can encode it as a binary vector or as an integer.
     *
     * e.g. label = 5, possible labels = 10
     *
     *  One hot:
     *  [0,0,0,0,0,1,0,0,0,0]
     *
     *  Integer:
     *  [5]
     *
     * @return
     */
    protected Data getLabelData() {
        SupervisedLearningEntityConfig config = ( SupervisedLearningEntityConfig ) _config;

        // First look for a reference Data that contains labels.
        if( config.labelEntityName.length() == 0 ) {
            // get labels from supplied data
            Data labels = getData( INPUT_LABELS );
            return labels;
        }

        // Failing that, get labels from config-path
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
        // 1-hot means a single '1' bit. The alternative is to put an integer value in a single element (see below).
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

        SupervisedLearningEntityConfig config = ( SupervisedLearningEntityConfig ) _config;

        // Has 2 inputs: FEATURES x TIME and LABELS x TIME
        // However, labels *may* be generated from a config property rather than a data.
        // Therefore, we directly get the features data and have a method to get the label data.
        Data features = getData( INPUT_FEATURES ); // only appended when learning enabled
        if( features == null ) {
            return;
        }

        Data labels = getLabelData(); // always returns a vector with 1 or more elements

        // If we are incrementally building a model from a store of data, we need to load it now from persistence.
        // This means loading any Data objects that are the serialized form of the model. The model is e.g. the weights
        // of the supervised learning algorithm.
        loadModel( ); // load a saved model, ie from persistence

        // Now decide if we are updating the model, or just testing it. If learn == true, we are updating the model.
        if( config.learn ) {

            // We might need to train the model, but not want to accumulate any more data (it might already be a complete set)
            // Therefore we have a switch that decides whether to add more samples to the training data.
            if( config.accumulateSamples ) {
                accumulate( features, labels );
            }

            // We have 3 learning modes: ONLINE, SAMPLE and BATCH. Each has different interface and rules.
            // The implementing algorithm may only be able to support some modes, depending on what it's API looks like.
            if( config.learningMode.equalsIgnoreCase( LEARNING_MODE_ONLINE ) ) {
                // train incrementally using one latest sample, continuously forgetting older samples
                trainOnline( features, labels );
            }
            else if( config.learningMode.equalsIgnoreCase( LEARNING_MODE_SAMPLE ) ) {
                // train incrementally using one latest sample
                trainSample( features, labels );
            }
            else if( config.learningMode.equalsIgnoreCase( LEARNING_MODE_BATCH ) ) {

                // Batch: re-train completely using a history of samples
                // In batch mode, we might only want to do this once and then skip further retrainings. Therefore we have
                // a switch to turn it off once it has run once.
                if( config.learnBatchOnce && config.learnBatchComplete ) {
                    // skip
                }
                else { // either learnBatch is "every step" or we didn't do it yet
                    Data outputFeatures = getData( FEATURES_BY_TIME );
                    Data outputLabels = getData( FEATURES_BY_TIME );

                    trainBatch( outputFeatures, outputLabels, features.getSize() ); // train on a batch of data.

                    config.learnBatchComplete = true; // note that it doesn't need to be done again
                }
            }
        }

        // We might optionally be reseting the model. Do that now. This means the resulting model will be completely empty
        // (no samples).
        if ( config.reset ) {
            resetModel(); // reset the model. Will be saved later
        }

        // We may be doing predictions during training (i.e. with an incomplete model, to see how it is progressing) or
        // during testing (with the presumably complete model). Produce some
        Data predictedLabels = new Data( new DataSize( labels._dataSize ) );

        if( config.predict ) { // optional switch
            predict( features, predictedLabels );
        }

        // Evaluates and outputs the predicted model against the true label. This is convenient to do here because we
        // have access to a) the features b) the correct label and c) the predicted label. We can therefore compute the
        // error. Also we pretty much have to compute the error here if we have some kinda error feedback.
        evaluate( features, labels, predictedLabels );

    }


}
