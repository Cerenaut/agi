/*
 * Copyright (c) 2017.
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
import io.agi.core.ml.supervised.Svm;
import io.agi.core.ml.supervised.SvmConfig;
import io.agi.core.orm.ObjectMap;
import io.agi.framework.DataFlags;
import io.agi.framework.Node;
import io.agi.framework.persistence.models.ModelEntity;

import java.util.Collection;

/**
 * Created by abdel on 30/10/17.
 */
public class SvmEntity extends SupervisedLearningEntity {

    public static final String ENTITY_TYPE = "svm-entity";

    private Svm _learner;

    public SvmEntity( ObjectMap om, Node n, ModelEntity model ) {
        super( om, n, model );
    }

    public void getInputAttributes( Collection< String > attributes ) {
        super.getInputAttributes( attributes );
    }

    public void getOutputAttributes( Collection< String > attributes, DataFlags flags ) {
        super.getOutputAttributes( attributes, flags );
    }

    public Class getConfigClass() {
        return SvmEntityConfig.class;
    }

    protected void reset( int features, int labelClasses ) {

        super.reset( features, labelClasses );

        _learner.reset();
    }

    /**
     * Override to ensure that model is persisted (get the string representation and set in the entity's config).
     */
    protected void saveModel() {

        // Set the model parameter:
        SvmEntityConfig config = ( SvmEntityConfig ) _config;
        config.modelString = _learner.getModelString();
    }

    /**
     * Override to ensure that the model is loaded from persistence.
     * Get string from entity's config object, set in learner's config and it will be used by learner.initModel()
     */
    protected void loadModel( int features, int labelClasses ) {

        // Get all the parameters:
        SvmEntityConfig config = ( SvmEntityConfig ) _config;

        SvmConfig learnerConfig = new SvmConfig();
        learnerConfig.setup( _om, "SvmConfig", _r, config.modelString, config.C, config.gamma );

        _learner = new Svm( getName(), _om );
        _learner.setup( learnerConfig );
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
        SvmEntityConfig config = ( SvmEntityConfig ) _config;
        config.learningMode = SupervisedLearningEntity.LEARNING_MODE_BATCH;
        config.learnBatchOnce = true;

        super.doUpdateSelf();
    }
}
