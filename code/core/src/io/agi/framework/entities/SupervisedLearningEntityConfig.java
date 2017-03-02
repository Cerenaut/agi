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

import io.agi.framework.EntityConfig;

/**
 * Created by gideon on 11/07/2016.
 */
public class SupervisedLearningEntityConfig extends EntityConfig {

    public String learningMode = SupervisedLearningEntity.LEARNING_MODE_SAMPLE;

    public boolean predict = false;
//    public boolean learnOnline = false; // have a forgetting factor that means older samples are forgotten
//    public boolean learnBatch = false; // accumulate and (re) train on a batch of samples at once
    public boolean learnBatchOnce = false; // if set to true, then after one "learn batch" it won't learn again
    public boolean learnBatchComplete = false; // was: trained

    public boolean accumulateSamples = false; // whether to build a matrix of samples over time
    public boolean learnAccumulatedSamples = false; // whether to use the accumulated samples for training

    public int learningPeriod = -1; // -1 = accumulate data forever, otherwise becomes a rolling window.
//    public boolean labelOneHot = false; // produces a 1-hot vector
    public int labelClasses = 0; // number of distinct label values, or classes

    // alternate source for labels: A config property
    public String labelEntityName;
    public String labelConfigPath;

    // output:
    public int labelsTruth = 0; // the value that was taken as input
    public int labelsPredicted = 0; // the predicted class given the input features
    public int labelsError = 0; // 1 if the prediction didn't match the input class

}
