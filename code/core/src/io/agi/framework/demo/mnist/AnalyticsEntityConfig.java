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
 * Created by gideon on 27/03/2016.
 */
public class AnalyticsEntityConfig extends EntityConfig {

    public static final String PHASE_TRAINING = "training";
    public static final String PHASE_TESTING = "testing";

    public String testingEntities = "";    // the subscribed entities for performing the analytics

    public int trainSetSize = 0;
    public int testSetSize = 0;

    public boolean terminate = false;      // trigger to stop the experiment
    public String phase = PHASE_TRAINING;  // can only be in one of two phases
    public int count = 0;                  // dataset index count within the current phase
}
