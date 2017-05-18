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

package io.agi.core.alg;

import java.util.HashSet;

/**
 * Created by dave on 6/07/16.
 */
public class AutoRegionLayerTransient {

    public HashSet< Integer > _input1Active;
    public HashSet< Integer > _input2Active;

    public HashSet< Integer > _contextFreeActive;
    public HashSet< Integer > _contextFreeActiveNew;
    public HashSet< Integer > _contextFreeActiveOld;

    public HashSet< Integer > _predictionFP;
    public HashSet< Integer > _predictionFN;

    public HashSet< Integer > _output;

//    public HashSet< Integer > _contextualActive;
//    public HashSet< Integer > _contextualActiveNew;
//    public HashSet< Integer > _contextualActiveOld;
//
//    public HashSet< Integer > _contextualPredictionFP;
//    public HashSet< Integer > _contextualPredictionFN;
//
//    public HashSet< Integer > _contextualOutput;
}
