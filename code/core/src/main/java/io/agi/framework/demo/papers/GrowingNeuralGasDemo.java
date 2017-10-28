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

package io.agi.framework.demo.papers;

import io.agi.core.orm.AbstractPair;
import io.agi.core.util.PropertiesUtil;
import io.agi.core.util.images.BufferedImageSource.BufferedImageSourceFactory;
import io.agi.framework.Framework;
import io.agi.framework.Main;
import io.agi.framework.Node;
import io.agi.framework.demo.CreateEntityMain;
import io.agi.framework.demo.mnist.ImageLabelEntity;
import io.agi.framework.entities.*;
import io.agi.framework.factories.CommonEntityFactory;
import io.agi.framework.persistence.PersistenceUtil;
import io.agi.framework.references.DataRefUtil;

import java.util.ArrayList;
import java.util.Properties;

/**
 * A single winner per update. One bit of output.
 *
 * Created by dave on 8/07/16.
 */
public class GrowingNeuralGasDemo extends CreateEntityMain {

    public static void main( String[] args ) {
        GrowingNeuralGasDemo demo = new GrowingNeuralGasDemo();
        demo.mainImpl( args );
    }

    public void createEntities( Node n ) {

//        String trainingPath = "./training";
//        String testingPath = "./testing";

        String trainingPath = "/home/dave/workspace/agi.io/data/mnist/1k_test";
        String  testingPath = "/home/dave/workspace/agi.io/data/mnist/1k_test";

//        String trainingPath = "/home/dave/workspace/agi.io/data/mnist/cycle10";
//        String testingPath = "/home/dave/workspace/agi.io/data/mnist/cycle10";

        boolean cacheAllData = true;
        boolean terminateByAge = false;
//        int terminationAge = 10;//9000;
        int terminationAge = 50000;//25000;
        int trainingEpochs = 2;//80; // good for up to 80k
        int testingEpochs = 1;//80; // good for up to 80k
        int imagesPerEpoch = 1000;

        // Define some entities
        String experimentName           = PersistenceUtil.GetEntityName( "experiment" );
        String imageLabelName           = PersistenceUtil.GetEntityName( "image-class" );
        String competitiveLearningName  = PersistenceUtil.GetEntityName( "competitive-learning" );
        String vectorSeriesName         = PersistenceUtil.GetEntityName( "feature-series" );
        String valueSeriesName          = PersistenceUtil.GetEntityName( "label-series" );

        PersistenceUtil.CreateEntity( experimentName, ExperimentEntity.ENTITY_TYPE, n.getName(), null ); // experiment is the root entity
        PersistenceUtil.CreateEntity( imageLabelName, ImageLabelEntity.ENTITY_TYPE, n.getName(), experimentName );
        PersistenceUtil.CreateEntity( competitiveLearningName, GrowingNeuralGasEntity.ENTITY_TYPE, n.getName(), imageLabelName );
        PersistenceUtil.CreateEntity( vectorSeriesName, VectorSeriesEntity.ENTITY_TYPE, n.getName(), competitiveLearningName ); // 2nd, class region updates after first to get its feedback
        PersistenceUtil.CreateEntity( valueSeriesName, ValueSeriesEntity.ENTITY_TYPE, n.getName(), vectorSeriesName ); // 2nd, class region updates after first to get its feedback

        // Connect the entities' data
        // a) Image to image region, and decode
        DataRefUtil.SetDataReference( competitiveLearningName, GrowingNeuralGasEntity.INPUT, imageLabelName, ImageLabelEntity.OUTPUT_IMAGE );

        ArrayList< AbstractPair< String, String > > featureDatas = new ArrayList<>();
        featureDatas.add( new AbstractPair<>( competitiveLearningName, GrowingNeuralGasEntity.OUTPUT_ACTIVE ) );
        DataRefUtil.SetDataReferences( vectorSeriesName, VectorSeriesEntity.INPUT, featureDatas ); // get current state from the region to be used to predict


        // Experiment config
        if( !terminateByAge ) {
            PersistenceUtil.SetConfig( experimentName, "terminationEntityName", imageLabelName );
            PersistenceUtil.SetConfig( experimentName, "terminationConfigPath", "terminate" );
            PersistenceUtil.SetConfig( experimentName, "terminationAge", "-1" ); // wait for mnist to decide
        }
        else {
            PersistenceUtil.SetConfig( experimentName, "terminationAge", String.valueOf( terminationAge ) ); // fixed steps
        }

        // cache all data for speed, when enabled
        PersistenceUtil.SetConfig( experimentName, "cache", String.valueOf( cacheAllData ) );
        PersistenceUtil.SetConfig( imageLabelName, "cache", String.valueOf( cacheAllData ) );
        PersistenceUtil.SetConfig( competitiveLearningName, "cache", String.valueOf( cacheAllData ) );
        PersistenceUtil.SetConfig( vectorSeriesName, "cache", String.valueOf( cacheAllData ) );
        PersistenceUtil.SetConfig( valueSeriesName, "cache", String.valueOf( cacheAllData ) );

        // MNIST config
        PersistenceUtil.SetConfig( imageLabelName, "receptiveField.receptiveFieldX", "0" );
        PersistenceUtil.SetConfig( imageLabelName, "receptiveField.receptiveFieldY", "0" );
        PersistenceUtil.SetConfig( imageLabelName, "receptiveField.receptiveFieldW", "28" );
        PersistenceUtil.SetConfig( imageLabelName, "receptiveField.receptiveFieldH", "28" );
        PersistenceUtil.SetConfig( imageLabelName, "resolution.resolutionX", "28" );
        PersistenceUtil.SetConfig( imageLabelName, "resolution.resolutionY", "28" );
        PersistenceUtil.SetConfig( imageLabelName, "greyscale", "true" );
        PersistenceUtil.SetConfig( imageLabelName, "invert", "true" );
        PersistenceUtil.SetConfig( imageLabelName, "sourceType", BufferedImageSourceFactory.TYPE_IMAGE_FILES );
        PersistenceUtil.SetConfig( imageLabelName, "sourceFilesPrefix", "postproc" );
        PersistenceUtil.SetConfig( imageLabelName, "sourceFilesPathTraining", trainingPath );
        PersistenceUtil.SetConfig( imageLabelName, "sourceFilesPathTesting", testingPath );
        PersistenceUtil.SetConfig( imageLabelName, "trainingEpochs", String.valueOf( trainingEpochs ) );
        PersistenceUtil.SetConfig( imageLabelName, "testingEpochs", String.valueOf( testingEpochs ) );
        PersistenceUtil.SetConfig( imageLabelName, "trainingEntities", String.valueOf( competitiveLearningName ) );
        PersistenceUtil.SetConfig( imageLabelName, "testingEntities", vectorSeriesName + "," + valueSeriesName );

        int widthCells = 32;
        int heightCells = 32;
        int edgeMaxAge = 500;
        int growthInterval = 50;                                        // split two most stressed after growthInterval steps
        float learningRate = 0.01f;                                     // winner, make similar to input
        float learningRateNeighbours = learningRate * 0.2f;             // all connected neighbours make similar by this amount
        float noiseMagnitude = 0.0f;
        float stressLearningRate = 0.01f; // not used now?
        float stressSplitLearningRate = 0.5f; // change to stress after a split
        float stressThreshold = 0.01f; // when it ceases to split

        PersistenceUtil.SetConfig( competitiveLearningName, "learningRate", String.valueOf( learningRate ) );
        PersistenceUtil.SetConfig( competitiveLearningName, "widthCells", String.valueOf( widthCells ) );
        PersistenceUtil.SetConfig( competitiveLearningName, "heightCells", String.valueOf( heightCells ) );
        PersistenceUtil.SetConfig( competitiveLearningName, "learningRateNeighbours", String.valueOf( learningRateNeighbours ) );
        PersistenceUtil.SetConfig( competitiveLearningName, "noiseMagnitude", String.valueOf( noiseMagnitude ) );
        PersistenceUtil.SetConfig( competitiveLearningName, "edgeMaxAge", String.valueOf( edgeMaxAge ) );
        PersistenceUtil.SetConfig( competitiveLearningName, "stressLearningRate", String.valueOf( stressLearningRate ) );
        PersistenceUtil.SetConfig( competitiveLearningName, "stressSplitLearningRate", String.valueOf( stressSplitLearningRate ) );
        PersistenceUtil.SetConfig( competitiveLearningName, "stressThreshold", String.valueOf( stressThreshold ) );
        PersistenceUtil.SetConfig( competitiveLearningName, "growthInterval", String.valueOf( growthInterval ) );

        // Log features of the algorithm during all phases
        PersistenceUtil.SetConfig( vectorSeriesName, "period", String.valueOf( "-1" ) ); // infinite
        PersistenceUtil.SetConfig( vectorSeriesName, "learn", String.valueOf( "true" ) ); // infinite

        // Log labels of each image produced during all phases
        PersistenceUtil.SetConfig( valueSeriesName, "period", "-1" );
        PersistenceUtil.SetConfig( valueSeriesName, "learn", String.valueOf( "true" ) ); // infinite
        PersistenceUtil.SetConfig( valueSeriesName, "entityName", imageLabelName ); // log forever
        PersistenceUtil.SetConfig( valueSeriesName, "configPath", "imageLabel" ); // log forever

    }

}
