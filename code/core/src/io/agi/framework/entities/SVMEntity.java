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
import io.agi.core.ml.supervised.SupervisedLearningConfig;
import io.agi.core.ml.supervised.Svm;
import io.agi.core.orm.ObjectMap;
import io.agi.framework.DataFlags;
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
public class SVMEntity extends SupervisedLearningEntity {

    public static final String ENTITY_TYPE = "svm-entity";

//    public static final String FEATURE_LABEL_COUNT = "feature-label-count";     // count of each of the features for a class
//    protected Data _featureLabelCount;
    Svm _svm;

    public SVMEntity( ObjectMap om, Node n, ModelEntity model ) {
        super( om, n, model );
    }

    public void getInputAttributes( Collection< String > attributes ) {
        super.getInputAttributes( attributes );
    }

    public void getOutputAttributes( Collection< String > attributes, DataFlags flags ) {
        super.getOutputAttributes( attributes, flags );

        // TODO add any Data you need to persist your SVM model
        // attributes.add( FEATURE_LABEL_COUNT );
    }

    public Class getConfigClass() {
        return SVMEntityConfig.class;
    }

    protected void resetModel() {
        super.resetModel();
        _svm.reset();
    }

    protected void loadModel( int features, int labels, int labelClasses ) {
        // Get all the parameters:
        SVMEntityConfig config = ( SVMEntityConfig ) _config;

        // Create the config object:
        SupervisedLearningConfig svmConfig = new SupervisedLearningConfig();
        svmConfig.setup( config.C );

        // Create the implementing object itself, and copy data from persistence into it:
        _svm = new Svm( getName(), _om );
        _svm.setup( svmConfig );
        _svm.loadModel( );
    }

    /**
     * Train the algorithm given the entire history of training samples provided.
     *
     * @param featuresTimeMatrix History of features
     * @param labelsTimeMatrix History of labels
     * @param features Nbr of features in each sample
     */
    protected void trainBatch( Data featuresTimeMatrix, Data labelsTimeMatrix, int features ) {
        _svm.train( featuresTimeMatrix, labelsTimeMatrix );
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
        _svm.predict( features, predictedLabels );
    }

}
