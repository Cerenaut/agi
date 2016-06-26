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
 * Created by dave on 2/04/16.
 */
public class NeuralGasEntityConfig extends EntityConfig {

    // 5x5 discrete - 1 cell per cluster
    int widthCells = 5;
    int heightCells = 5;
    int maxAge = 200;
    float learningRate = 0.1f;
    float neighbourhoodRange = 10.0f;//120.5f; // this gives a drop-off of 1,0.5,0.25,0.12 approx
    float ageFactor = 0.0f;

    // Worked 6x6 discrete
//    int widthCells = 6;
//    int heightCells = 6;
//    int maxAge = 200;
//    float learningRate = 0.1f;
//    float neighbourhoodRange = 30.0f;//120.5f; // this gives a drop-off of 1,0.5,0.25,0.12 approx
//    float ageFactor = 0.0f;

    // Worked 6x6 continuous
//    int widthCells = 6;
//    int heightCells = 6;
//    int maxAge = 200;
//    float learningRate = 0.1f;
//    float neighbourhoodRange = 10.0f;//120.5f; // this gives a drop-off of 1,0.5,0.25,0.12 approx
//    float ageFactor = 0.0f;

    // worked @ 8x8
//    int widthCells = 8;
//    int heightCells = 8;
//    int maxAge = 200;
//    float learningRate = 0.1f;
//    float neighbourhoodRange = 60.0f;//120.5f; // this gives a drop-off of 1,0.5,0.25,0.12 approx
//    float ageFactor = 0.0f;


    float noiseMagnitude = 0.0f;
    float minDistance = 0.001f; // all cells adapt by 0.01 * learningRate

//    int maxAge = 200;
//    float learningRate = 0.05f;
//    float noiseMagnitude = 0.0f;
//    float neighbourhoodRange = 60.0f;//120.5f; // this gives a drop-off of 1,0.5,0.25,0.12 approx
//    float minDistance = 0.001f; // all cells adapt by 0.01 * learningRate

}
