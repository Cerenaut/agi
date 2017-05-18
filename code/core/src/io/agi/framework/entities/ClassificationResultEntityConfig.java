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
 * Created by dave on 3/11/16.
 */
public class ClassificationResultEntityConfig extends EntityConfig {

    public float labelPredicted = 0; // the predicted class given the input features
    public float labelTruth = 0; // the value that was taken as input
    public int labelError = 0; // 1 if the prediction didn't match the input class
    public int labelErrorSum = 0; // sum of errors (if vector input)

}
