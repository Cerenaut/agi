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

package io.agi.core.ml.supervised;

import io.agi.core.data.Data;

/**
 * Created by gideon on 14/12/16.
 */
public interface SupervisedBatchTraining {

    void setup( SupervisedBatchTrainingConfig config );

    void reset();

    /**
     * Make predictions using the trained model. If there is no valid model, there will be no predictions.
     * @param featuresMatrix the input data points used to make predictions.
     *                       This is an n x m matrix [n][m], where n = feature vector size and m = number of data points.
     * @param predictionsVector For each data point (m) there is a corresponding prediction.
     *                          m x 1 vector
     */
    void predict( Data featuresMatrix, Data predictionsVector );

    /**
     * Train the model (subclasses should ensure that the model is saved to config).
     * @param featuresMatrixTrain the input data points used for training the model.
     *                       This is an n x m matrix [n][m], where n = feature vector size and m = number of data points.
     * @param classTruthVector For each data point (m), this is the true label used for supervised learning training.
     *                         m x 1 vector
     */
    void train( Data featuresMatrixTrain, Data classTruthVector );

    /**
     * Save the model to config object.
     * @return the model in serialised form as a string.
     */
    String saveModel();

    /**
     * Load model from config object (if it is not null).
     */
    void loadModel( );

    /**
     * Load model from a string (and set config to be consistent).
     * @param modelString
     */
    void loadModel( String modelString );

    /**
     * Return the up-to-date serialised model that has been created by training the algorithm.
     * It includes all the parameters of the algorithm.
     * It is everything required to make predictions plus additional information.
     * @return
     */
    String getModelString();
}
