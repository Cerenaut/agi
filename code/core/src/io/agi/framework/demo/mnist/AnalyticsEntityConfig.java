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

package io.agi.framework.demo.mnist;

import io.agi.framework.EntityConfig;

/**
 * Created by gideon on 08/01/2017.
 */
public class AnalyticsEntityConfig extends EntityConfig {

    public static final String PHASE_TRAINING = "training";
    public static final String PHASE_TESTING = "testing";
    public static final String PHASE_TERMINATING = "terminating";

    public String testingEntities = "";    // the subscribed entities for performing the analytics

    // there is one feature and one label matrix, that is segmented into training and test sets using these params
    // the sets can overlap, using testSetOffset
    public int trainSetSize = 0;
    public int testSetSize = 0;
    public int trainSetOffset = 0;           // offset in the dataset from which to start using the data for training
    public int testSetOffset = 0;           // offset in the dataset from which to start using the data for testing
                                            // if you want to test on the entire dataset (training+testing), then make this zero

    // REMOVING THIS FUNCTIONALITY FOR NOW (may or may not use it)
//    public String datasetExpPrefix = "";   // use data from entities in the experiment that used this prefix
//    public String datasetEntity = "";      // the entity that produced the data (e.g. KSparseAutoencoderEntity)
//    public String datasetFeaturesAttribute = "";
//    public String datasetLabelsAttribute = "";

    public boolean predictDuringTraining = false;
    public boolean batchMode = true;
    public boolean terminate = false;      // trigger to stop the experiment
    public String phase = PHASE_TRAINING;  // can be training or testing
    public int count = 0;                  // dataset index count within the current phase

}
