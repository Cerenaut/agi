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
import io.agi.core.ml.supervised.LogisticRegression;
import io.agi.core.ml.supervised.SupervisedBatchTraining;
import io.agi.core.ml.supervised.SupervisedBatchTrainingConfig;
import io.agi.core.ml.supervised.Svm;
import io.agi.core.orm.ObjectMap;
import io.agi.framework.DataFlags;
import io.agi.framework.Node;
import io.agi.framework.persistence.models.ModelEntity;

import java.util.Collection;

/**
 *
 *
 * Created by gideon on 11/07/2016.
 */
public class SupervisedBatchTrainingEntity extends SupervisedLearningEntity {

    public static final String ENTITY_TYPE = "supervised-batch-training-entity";

    SupervisedBatchTraining _learner;

    public SupervisedBatchTrainingEntity( ObjectMap om, Node n, ModelEntity model ) {
        super( om, n, model );
    }

    public void getInputAttributes( Collection< String > attributes ) {
        super.getInputAttributes( attributes );
    }

    public void getOutputAttributes( Collection< String > attributes, DataFlags flags ) {
        super.getOutputAttributes( attributes, flags );
    }

    public Class getConfigClass() {
        return SupervisedBatchTrainingEntityConfig.class;
    }

    protected void reset() {
        super.reset();
        _learner.reset();
    }

    /**
     * Override to ensure that model is persisted (get the string representation and set in the entity's config).
     */
    protected void saveModel() {

        // Set the model parameter:
        SupervisedBatchTrainingEntityConfig config = ( SupervisedBatchTrainingEntityConfig ) _config;
        config.modelString = _learner.getModelString();
    }

    /**
     * Override to ensure that the model is loaded from persistence.
     * Get string from entity's config object, set in learner's config and it will be used by learner.initModel()
     */
    protected void loadModel( int features, int labels, int labelClasses ) {

        // Get all the parameters:
        SupervisedBatchTrainingEntityConfig config = ( SupervisedBatchTrainingEntityConfig ) _config;

        // Create the config object for the supervised learning algorithm:
        SupervisedBatchTrainingConfig learnerConfig = new SupervisedBatchTrainingConfig();
        learnerConfig.setup( _om, "SupervisedBatchTrainingConfig", _r, config.modelString, config.bias, config.C );

        // Create the implementing object itself, using data from persistence:
        if ( config.algorithm.equalsIgnoreCase( SupervisedBatchTrainingEntityConfig.ALGORITHM_SVM ) ) {
            _learner = new Svm( getName(), _om );
        }
        else if  ( config.algorithm.equalsIgnoreCase( SupervisedBatchTrainingEntityConfig.ALGORITHM_LOGISTIC_REGRESSION ) ) {
            _learner = new LogisticRegression( getName(), _om );
        }
        else {
            throw new java.lang.UnsupportedOperationException( "Algorithm type not supported" );
        }

        _learner.setup( learnerConfig );
        _learner.loadModel( );
    }

    /**
     * Train the algorithm given the entire history of training samples provided.
     *
     * @param featuresTimeMatrix History of features
     * @param labelsTimeMatrix History of labels
     * @param features Nbr of features in each sample
     */
    protected void trainBatch( Data featuresTimeMatrix, Data labelsTimeMatrix, int features ) {
        _learner.train( featuresTimeMatrix, labelsTimeMatrix );
    }

    /**
     * Generate a prediction from the model and copy it into predictedLabels.
     *
     * @param features
     * @param predictedLabels
     */
    protected void predict( Data features, Data predictedLabels ) {
        predictedLabels.set( 0f );
        _learner.predict( features, predictedLabels );
    }

    protected void doUpdateSelf() {
        SupervisedBatchTrainingEntityConfig config = ( SupervisedBatchTrainingEntityConfig ) _config;
        config.learningMode = SupervisedLearningEntity.LEARNING_MODE_BATCH;
        config.learnBatchOnce = true;

        super.doUpdateSelf();
    }

}
