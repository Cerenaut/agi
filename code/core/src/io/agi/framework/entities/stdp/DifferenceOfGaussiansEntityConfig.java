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

package io.agi.framework.entities.stdp;

import io.agi.framework.EntityConfig;

/**
 * Created by dave on 6/05/17.
 */
public class DifferenceOfGaussiansEntityConfig extends EntityConfig {

    public String kernelDescCached = "";

    public int kernelWidth = 0;
    public int kernelHeight = 0;

    public float stdDev1 = 0;
    public float stdDev2 = 0;

    public float scaling = 0;
    public float min = 0;
    public float max = 0;

}
