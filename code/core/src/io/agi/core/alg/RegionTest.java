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

import io.agi.core.data.Data;
import io.agi.core.math.RandomInstance;
import io.agi.core.orm.ObjectMap;
import io.agi.core.orm.UnitTest;

import java.util.Random;

/**
 * Created by dave on 11/01/16.
 */
public class RegionTest implements UnitTest {

    public static void main( String[] args ) {
        RegionTest ffnt = new RegionTest();
        ffnt.test( args );
    }

    public void test( String[] args ) {

//        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//        // Test parameters
//        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//        int randomSeed = 1;
//
//        // Feedforward size
//        int inputWidth = 10;
//        int inputHeight = 10;
//
//        // Feedback size
//        int feedbackWidthCells = 1;
//        int feedbackHeightCells = 1;
//
//
//        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//        // Algorithm specific parameters
//        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//        // Region size
//        int regionWidthColumns = 10;
//        int regionHeightColumns = 10;
//
//        // Column Sizing
//        int classifierWidthCells = 6;
//        int classifierHeightCells = 6;
//
//        // Organizer training
//        int receptiveFieldsTrainingSamples = 6;
//        int receptiveFieldSize = 8;
//        float organizerLearningRate = 0.02f;
//        float organizerLearningRateNeighbours = 0.01f;
//        float organizerNoiseMagnitude = 0.0f;
//        int organizerEdgeMaxAge = 500;
//        float organizerStressLearningRate = 0.01f;
//        float organizerStressThreshold = 0.1f;
//        int organizerGrowthInterval = 100;
//
//        // Classifier training
//        float classifierLearningRate = 0.02f;
//        float classifierLearningRateNeighbours = 0.01f;
//        float classifierNoiseMagnitude = 0.0f;
//        int classifierEdgeMaxAge = 500;
//        float classifierStressLearningRate = 0.01f;
//        float classifierStressThreshold = 0.1f;
//        int classifierGrowthInterval = 100;
//
//        // Predictor
//        float predictorHiddenLayerScaleFactor = 1.0f;
//        float predictorLearningRate = 0.1f;
//        float predictorRegularization = 0.0f;
//
//        // Build the algorithm
//        RandomInstance.setSeed( randomSeed ); // make the tests repeatable
//        Random random = RandomInstance.getInstance();
//        ObjectMap om = ObjectMap.GetInstance();
//        String regionName = "region";
//
//        RegionFactory rf = new RegionFactory();
//
//        Region r = rf.create(
//                om, regionName, random,
//                inputWidth, inputHeight,
//                feedbackWidthCells, feedbackHeightCells,
//                regionWidthColumns, regionHeightColumns,
//                classifierWidthCells, classifierHeightCells,
//                receptiveFieldsTrainingSamples, receptiveFieldSize,
//                organizerLearningRate, organizerLearningRateNeighbours, organizerNoiseMagnitude, organizerEdgeMaxAge, organizerStressLearningRate, organizerStressThreshold, organizerGrowthInterval,
//                classifierLearningRate, classifierLearningRateNeighbours, classifierNoiseMagnitude, classifierEdgeMaxAge, classifierStressLearningRate, classifierStressThreshold, classifierGrowthInterval,
//                predictorLearningRate );
//                predictorHiddenLayerScaleFactor, predictorLearningRate, predictorRegularization );
//
//        // Run
//        while( true ) {
//            Data d = r.getFfInput(); // will not use external FB input, just internal.
//            d.setRandom( random );
//            d.thresholdLessThan( 0.5f, 1.f, 0.f );
//            r.update();
//
//            System.out.print( "." );
//        }
    }

}