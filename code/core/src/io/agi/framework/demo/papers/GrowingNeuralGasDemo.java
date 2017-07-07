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
        String experimentName           = Framework.GetEntityName( "experiment" );
        String imageLabelName           = Framework.GetEntityName( "image-class" );
        String competitiveLearningName  = Framework.GetEntityName( "competitive-learning" );
        String vectorSeriesName         = Framework.GetEntityName( "feature-series" );
        String valueSeriesName          = Framework.GetEntityName( "label-series" );

        Framework.CreateEntity( experimentName, ExperimentEntity.ENTITY_TYPE, n.getName(), null ); // experiment is the root entity
        Framework.CreateEntity( imageLabelName, ImageLabelEntity.ENTITY_TYPE, n.getName(), experimentName );
        Framework.CreateEntity( competitiveLearningName, GrowingNeuralGasEntity.ENTITY_TYPE, n.getName(), imageLabelName );
        Framework.CreateEntity( vectorSeriesName, VectorSeriesEntity.ENTITY_TYPE, n.getName(), competitiveLearningName ); // 2nd, class region updates after first to get its feedback
        Framework.CreateEntity( valueSeriesName, ValueSeriesEntity.ENTITY_TYPE, n.getName(), vectorSeriesName ); // 2nd, class region updates after first to get its feedback

        // Connect the entities' data
        // a) Image to image region, and decode
        Framework.SetDataReference( competitiveLearningName, GrowingNeuralGasEntity.INPUT, imageLabelName, ImageLabelEntity.OUTPUT_IMAGE );

        ArrayList< AbstractPair< String, String > > featureDatas = new ArrayList<>();
        featureDatas.add( new AbstractPair<>( competitiveLearningName, GrowingNeuralGasEntity.OUTPUT_ACTIVE ) );
        Framework.SetDataReferences( vectorSeriesName, VectorSeriesEntity.INPUT, featureDatas ); // get current state from the region to be used to predict


        // Experiment config
        if( !terminateByAge ) {
            Framework.SetConfig( experimentName, "terminationEntityName", imageLabelName );
            Framework.SetConfig( experimentName, "terminationConfigPath", "terminate" );
            Framework.SetConfig( experimentName, "terminationAge", "-1" ); // wait for mnist to decide
        }
        else {
            Framework.SetConfig( experimentName, "terminationAge", String.valueOf( terminationAge ) ); // fixed steps
        }

        // cache all data for speed, when enabled
        Framework.SetConfig( experimentName, "cache", String.valueOf( cacheAllData ) );
        Framework.SetConfig( imageLabelName, "cache", String.valueOf( cacheAllData ) );
        Framework.SetConfig( competitiveLearningName, "cache", String.valueOf( cacheAllData ) );
        Framework.SetConfig( vectorSeriesName, "cache", String.valueOf( cacheAllData ) );
        Framework.SetConfig( valueSeriesName, "cache", String.valueOf( cacheAllData ) );

        // MNIST config
        Framework.SetConfig( imageLabelName, "receptiveField.receptiveFieldX", "0" );
        Framework.SetConfig( imageLabelName, "receptiveField.receptiveFieldY", "0" );
        Framework.SetConfig( imageLabelName, "receptiveField.receptiveFieldW", "28" );
        Framework.SetConfig( imageLabelName, "receptiveField.receptiveFieldH", "28" );
        Framework.SetConfig( imageLabelName, "resolution.resolutionX", "28" );
        Framework.SetConfig( imageLabelName, "resolution.resolutionY", "28" );
        Framework.SetConfig( imageLabelName, "greyscale", "true" );
        Framework.SetConfig( imageLabelName, "invert", "true" );
        Framework.SetConfig( imageLabelName, "sourceType", BufferedImageSourceFactory.TYPE_IMAGE_FILES );
        Framework.SetConfig( imageLabelName, "sourceFilesPrefix", "postproc" );
        Framework.SetConfig( imageLabelName, "sourceFilesPathTraining", trainingPath );
        Framework.SetConfig( imageLabelName, "sourceFilesPathTesting", testingPath );
        Framework.SetConfig( imageLabelName, "trainingEpochs", String.valueOf( trainingEpochs ) );
        Framework.SetConfig( imageLabelName, "testingEpochs", String.valueOf( testingEpochs ) );
        Framework.SetConfig( imageLabelName, "trainingEntities", String.valueOf( competitiveLearningName ) );
        Framework.SetConfig( imageLabelName, "testingEntities", vectorSeriesName + "," + valueSeriesName );

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

        Framework.SetConfig( competitiveLearningName, "learningRate", String.valueOf( learningRate ) );
        Framework.SetConfig( competitiveLearningName, "widthCells", String.valueOf( widthCells ) );
        Framework.SetConfig( competitiveLearningName, "heightCells", String.valueOf( heightCells ) );
        Framework.SetConfig( competitiveLearningName, "learningRateNeighbours", String.valueOf( learningRateNeighbours ) );
        Framework.SetConfig( competitiveLearningName, "noiseMagnitude", String.valueOf( noiseMagnitude ) );
        Framework.SetConfig( competitiveLearningName, "edgeMaxAge", String.valueOf( edgeMaxAge ) );
        Framework.SetConfig( competitiveLearningName, "stressLearningRate", String.valueOf( stressLearningRate ) );
        Framework.SetConfig( competitiveLearningName, "stressSplitLearningRate", String.valueOf( stressSplitLearningRate ) );
        Framework.SetConfig( competitiveLearningName, "stressThreshold", String.valueOf( stressThreshold ) );
        Framework.SetConfig( competitiveLearningName, "growthInterval", String.valueOf( growthInterval ) );

        // Log features of the algorithm during all phases
        Framework.SetConfig( vectorSeriesName, "period", String.valueOf( "-1" ) ); // infinite
        Framework.SetConfig( vectorSeriesName, "learn", String.valueOf( "true" ) ); // infinite

        // Log labels of each image produced during all phases
        Framework.SetConfig( valueSeriesName, "period", "-1" );
        Framework.SetConfig( valueSeriesName, "learn", String.valueOf( "true" ) ); // infinite
        Framework.SetConfig( valueSeriesName, "entityName", imageLabelName ); // log forever
        Framework.SetConfig( valueSeriesName, "configPath", "imageLabel" ); // log forever

    }

}
