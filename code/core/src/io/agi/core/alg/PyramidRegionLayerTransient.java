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
public class PyramidRegionLayerTransient {

    public HashSet< Integer > _inputC1Active;
    public HashSet< Integer > _inputC2Active;

    public HashSet< Integer > _inputP1Active;
    public HashSet< Integer > _inputP2Active;

    public HashSet< Integer > _spikesNew;
    public HashSet< Integer > _spikesOld;
    public HashSet< Integer > _spikesOut;

    public HashSet< Integer > _predictionErrorFP;
    public HashSet< Integer > _predictionErrorFN;

}
