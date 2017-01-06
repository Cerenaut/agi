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
import io.agi.core.ml.supervised.SupervisedBatchTraining;
import io.agi.core.ml.supervised.SupervisedLearningConfig;
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

    public static final String ENTITY_TYPE = "svm-entity";

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
        return SVMEntityConfig.class;
    }

    protected void reset() {
        super.reset();
        _learner.reset();
    }

    protected void loadModel( int features, int labels, int labelClasses ) {
        // Get all the parameters:
        SVMEntityConfig config = ( SVMEntityConfig ) _config;

        // Create the config object:
        SupervisedLearningConfig svmConfig = new SupervisedLearningConfig();
        svmConfig.setup( _om, "svmConfig", _r, config.bias, config.C );

        // Create the implementing object itself, and copy data from persistence into it:
        _learner = new Svm( getName(), _om );
        _learner.setup( svmConfig );
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
     * Incrementally train the model with one additional sample.
     *
     * @param features Latest sample
     * @param labels Latest sample
     */
    protected void trainSample( Data features, Data labels ) {
            throw new java.lang.UnsupportedOperationException();
    }

    /**
     * Train the algorithm using an online update, which implies forgetting older updates.
     *
     * @param features
     * @param labels
     */
    protected void trainOnline( Data features, Data labels ) {
        throw new java.lang.UnsupportedOperationException();
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

}
