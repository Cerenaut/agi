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

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by dave on 14/02/17.
 */
public class ClassificationAnalysisEntityConfig extends EntityConfig {

    public int sampleOffset = 0;
    public int sampleLength = 0;

    public float errorCount = 0;
    public float errorFraction = 0;
    public float errorPercentage = 0;
    public float samples = 0;

    public float betaSq = 0; // used to calculate F-score see https://en.wikipedia.org/wiki/F1_score

    public ArrayList< String > sortedLabels = new ArrayList< String >();
    public ArrayList< String > confusionMatrix = new ArrayList< String >(); // a 2D matrix of label error frequencies

    public HashMap< String, HashMap< String, String > > labelStatistics = new HashMap< String, HashMap< String, String > >();

}
