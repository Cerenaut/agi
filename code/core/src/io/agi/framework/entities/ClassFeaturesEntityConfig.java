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
 * Created by dave on 9/07/16.
 */
public class ClassFeaturesEntityConfig extends EntityConfig {

    public String classEntityName;
    public String classConfigPath;
    public int classes = 0;

    public int onlineMaxCount = 500; // if online learning mode, then count can't exceed this value.
    public boolean onlineLearning = false;

    public int classPredicted = 0; // the predicted class given the input features
    public int classError = 0; // 1 if the prediction didn't match the input class
    public int classTruth = 0; // the value that was taken as input

}
