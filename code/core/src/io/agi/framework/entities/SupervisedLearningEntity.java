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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;


/**
 * This is a 'learning analytics' Entity which uses a provided label to classify input and measure correct label prediction.
 * <p>
 * There are 2 inputs: Features, and Labels. Features are vectors of real or binary values that comprise the input
 * data set. Labels are the associated classes for classification or values in the case of regression.
 * <p>
 * It can support training and testing simultaneously. Standard use case of supervised learning is train then test.
 * Some algorithms can be trained online, in which case training and testing can be done simultaneously.
 * <p>
 * It optionally can accumulate the training data into a batch, and train once at the end.
 * <p>
 * It has 3 modes for accumulating data:
 * <p>
 * a) ONLINE
 * b) SAMPLE
 * c) BATCH
 * <p>
 * In Online mode samples are presented one at a time. It is assumed that the input features may change in meaning over
 * time, therefore some amount of forgetting is required / implied.
 * <p>
 * In Sample mode it is the same as Online except no forgetting factor is required. Every sample is judged to be valid
 * which occurs if the algorithm generating the samples is fixed and no longer learning.
 * <p>
 * In Batch mode, the algorithm is trained on a large number of samples which are supplied and used in one go.
 *      *NOTE:* currently batch mode uses the accumulated data - it does not support input of a batch of data.
 * <p>
 * The 'model' refers to the parameters of the learning algorithm - at times not just the hypothesis parameters
 *      (e.g. for the batch supervised learning algorithms implemented at this time).
 * They are saved to and loaded form persistence every iteration.
 * <p>
 * <p>
 * Created by gideon on 11/07/2016.
 */
public class SupervisedLearningEntity extends Entity {

    protected static final Logger _logger = LogManager.getLogger();

    public static final String ENTITY_TYPE = "supervised-learning-entity";

    public static final String LEARNING_MODE_ONLINE = "online";
    public static final String LEARNING_MODE_SAMPLE = "sample";
    public static final String LEARNING_MODE_BATCH = "batch";

    // Features and Labels by time comprise the input data set.

    // Features_by_time is a series of data points (each one a feature vector).
    // It can be viewed as a matrix of size: 'number of features' x 'number of data points', or n x m in standard ML terminology
    public static final String FEATURES_BY_TIME = "features-by-time";
    // Labels_by_time is a series of labels (either single label or label vector)
    public static final String LABELS_BY_TIME = "labels-by-time";

    // In 'accumulate' mode, INPUT_FEATURES/LABELS comprise the feature vector and input label at an instant in time
    public static final String INPUT_FEATURES = "input-features";
    public static final String INPUT_LABELS = "input-labels";

    public static final String OUTPUT_LABELS_TRUTH = "output-labels-truth";
    public static final String OUTPUT_LABELS_PREDICTED = "output-labels-predicted";
    public static final String OUTPUT_LABELS_ERROR = "output-labels-error";

    protected SupervisedLearningEntity( ObjectMap om, Node n, ModelEntity model ) {
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
     * Default implementation clears accumulated training data; override to clear model data of the given algorithm.
     */
    protected void reset( int features, int labelClasses ) {
        SupervisedLearningEntityConfig config = ( SupervisedLearningEntityConfig ) _config;

        config.learnBatchComplete = false;

        // clear the accumulated data
        Data newOutputFeatures = new Data( 0 );
        setData( FEATURES_BY_TIME, newOutputFeatures );

        Data newOutputLabels = new Data( 0 );
        setData( LABELS_BY_TIME, newOutputLabels );
    }

    /**
     * Initialise model data structure, and load it with parameters from persistence.
     * It should be ready for training if not already trained.
     *
     * @param features
     * @param labelClasses
     */
    protected void loadModel( int features, int labelClasses ) {
        // Implement as needed in subclasses
        _logger.warn( "loadModel has not been overridden by your supervised learning algorithm - model will not be loaded from persistence" );
    }

    /**
     * Save the model to persistence.
     */
    protected void saveModel() {
        // Implement as needed in subclasses
        _logger.warn( "saveModel has not been overridden by your supervised learning algorithm - model will not be persisted" );
    }

    /**
     * Train the algorithm given the entire history of training samples provided.
     *
     * @param featuresTimeMatrix History of features
     * @param labelsTimeMatrix   History of labels
     * @param features           Nbr of features in each sample
     */
    protected void trainBatch( Data featuresTimeMatrix, Data labelsTimeMatrix, int features ) {
        // Implement as needed in subclasses
        throw new java.lang.UnsupportedOperationException( "train batch is not supported by this algorithm" );
    }

    /**
     * Incrementally train the algorithm using a single sample update.
     *
     * @param features
     * @param labels
     */
    protected void trainSample( Data features, Data labels ) {
        // Implement as needed in subclasses
        throw new java.lang.UnsupportedOperationException( "train sample is not supported by this algorithm" );
    }

    /**
     * Train the algorithm using an online update.
     *
     * @param features
     * @param labels
     */
    protected void trainOnline( Data features, Data labels ) {
        // Implement as needed in subclasses
        throw new java.lang.UnsupportedOperationException( "Online training is not supported by this algorithm" );
    }

    /**
     * Generate a prediction from the model and copy it into predictedLabels.
     *
     * @param features
     * @param predictedLabels
     */
    protected void predict( Data features, Data predictedLabels ) {
        // Implement as needed in subclasses
        throw new java.lang.UnsupportedOperationException( "Prediction not supported by this algorithm" );
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
     * @param features Always a matrix of features x time, but time may be 1 step
     * @param labels Always a matrix of (one-hot label-class vector) x time, but time may be 1 step
     * @param predictedLabels Always a matrix of (label-class vector) x time, but time may be 1 step
     */
    protected void evaluate( Data features, Data labels, Data predictedLabels ) {

        // Config outputs
        SupervisedLearningEntityConfig config = ( SupervisedLearningEntityConfig ) _config;

        int labelClasses = config.labelClasses;
        int labelsSize = labels.getSize();
        int featuresSize = features.getSize();
        int nbrSamples = labelsSize / labelClasses;
        int nbrFeatures = features.getSize() / nbrSamples;

        assert( predictedLabels.getSize() == labelsSize );
        assert( nbrFeatures * nbrSamples == featuresSize );


        Data errors;// = new Data( 1 );

        config.labelsError = 0;

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
                config.labelsPredicted = (int) predictedLabelValue; // the predicted class given the input features
                config.labelsTruth = (int) labelValue;
            }
            else {
                config.labelsPredicted = 0; // can't be expressed
                config.labelsTruth = 0; // can't be expressed
            }
        }

        // Data outputs
        setData( OUTPUT_LABELS_TRUTH, labels );
        setData( OUTPUT_LABELS_PREDICTED, predictedLabels );
        setData( OUTPUT_LABELS_ERROR, errors );
    }

    /**
     * Obtain a vector of labels from EITHER config properties or from a Data reference.
     * <p>
     * We always encode the label class as a 1-hot binary vector.
     * <p>
     * e.g. label = 5, possible label classes = 10
     * <p>
     * One hot:
     * [0,0,0,0,0,1,0,0,0,0]
     *
     * @return
     */
    protected Data getInputLabels() {
        SupervisedLearningEntityConfig config = ( SupervisedLearningEntityConfig ) _config;

        // First look for a reference Data that contains labels.
        // look for config first
        if(    ( config.labelEntityName != null )
            && ( config.labelEntityName.length() > 0 )
            && ( config.labelConfigPath != null )
            && ( config.labelConfigPath.length() > 0 ) ) {
            String stringLabelValue = Framework.GetConfig( config.labelEntityName, config.labelConfigPath );
            Integer label = Integer.valueOf( stringLabelValue );

            // Now sanitize
            // catch bad value
            if( label == null ) {
                label = 0;
            }

            // catch value outside range
            if( label >= config.labelClasses ) {
                label = 0;
            }

            Data labels = new Data( 1 );
            labels._values[ 0 ] = label;
        }
        else {         // Get labels from data reference (default)
            // config source not valid. Use data ref
            // get labels from supplied data
            Data input = getData( INPUT_LABELS );
            return input;
        }

        return null;
    }

    @Override
    protected void doUpdateSelf() {

        SupervisedLearningEntityConfig config = ( SupervisedLearningEntityConfig ) _config;

        // Note that labels *may* be generated from a config property rather than a data.
        // Therefore, we directly get the features data and have a method to get the label data.
        Data features = getData( INPUT_FEATURES ); // only appended when learning enabled
        if( features == null ) {
            return;
        }

        Data labels = getInputLabels(); // only appended when learning enabled
        if( labels == null ) {
            return;
        }

        if( config.learn || config.predict || config.reset ) {
            _logger.info( "Loading old model..." );
            loadModel( features.getSize(), config.labelClasses ); // load a saved model, ie from persistence
        }

        if( config.reset ) {
            _logger.info( "Resetting..." );
            reset( features.getSize(), config.labelClasses );           // reset the model
        }

        // If learning mode, then update model
        if( config.learn ) {

            _logger.info( "Training new model..." );
            if( config.accumulateSamples ) {
                accumulate( features, labels );
            }

            if( config.learningMode.equals( LEARNING_MODE_ONLINE ) ) {
                trainOnline( features, labels );
            } else if( config.learningMode.equals( LEARNING_MODE_SAMPLE ) ) {
                trainSample( features, labels );
            } else if( config.learningMode.equals( LEARNING_MODE_BATCH ) ) {
                if( config.learnBatchOnce && config.learnBatchComplete ) {
                    // skip
                } else { // either learnBatch is "every step" or we didn't do it yet

                    _logger.info( "Train learner, batch mode" );

                    Data trainingFeatures = features;
                    Data trainingLabels = labels;

                    if( config.learnAccumulatedSamples ) {
                        Data featuresByTime = getData( FEATURES_BY_TIME );
                        Data labelsByTime = getData( LABELS_BY_TIME );
                        trainingFeatures = featuresByTime;
                        trainingLabels = labelsByTime;
                    }

                    int featuresSize = trainingFeatures.getSize();
                    int labelsSize = trainingLabels.getSize();
                    int nbrSamples = labelsSize;
                    int nbrFeatures = featuresSize / nbrSamples;

                    trainBatch( trainingFeatures, trainingLabels, nbrFeatures ); // train on a batch of data.

                    config.learnBatchComplete = true; // it doesn't need to be done again
                }
            }
        }

        // if model changed for any reason, save it.
        if( config.reset || config.learn ) {
            _logger.info( "Saving new model..." );
            saveModel();
        }

        // We may be doing predictions during training (i.e. with an incomplete model, to see how it is progressing) or
        // during testing (with the presumably complete model).
        if( config.predict ) { // optional switch

            _logger.info( "Predicting..." );
//            int featuresSize = features.getSize();
            int labelsSize = labels.getSize();
            int nbrSamples = labelsSize;
            Data predictedLabels = new Data( nbrSamples );


            predict( features, predictedLabels );

            // shouldn't evaluate without a prediction
            evaluate( features, labels, predictedLabels );      // convenient here and necessary if there is some type of error feedback
        }
    }

    /**
     * Converts a label into a one-hot vector
     *
     * @param label
     * @param labelClasses
     * @return
     */
    public Data label2OneHot( int label, int labelClasses ) {
        Data labels = new Data( labelClasses );
        labels._values[ label ] = 1f;
        return labels;
    }

    /**
     * Converts from a one-hot vector over all label classes to the selected label.
     *
     * @param oneHot
     * @return
     */
    public int oneHot2Label( Data oneHot ) {
        int label = oneHot.maxAt().offset();
        return label;
    }

}
