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

package io.agi.framework.demo.papers;

import io.agi.core.orm.AbstractPair;
import io.agi.core.util.images.BufferedImageSource.BufferedImageSourceFactory;
import io.agi.framework.Framework;
import io.agi.framework.Node;
import io.agi.framework.demo.CreateEntityMain;
import io.agi.framework.demo.mnist.ImageLabelEntity;
import io.agi.framework.entities.*;

import java.util.ArrayList;

/**
 * K-Winner GNG
 * TESTED 89.24% / 82.40% 6x 10k train 1k test. sparsity 0.024 32x32 edgeMaxAge 1000 growth 1 learningRate 0.01 neighbours 0.01 .
 * TESTED 87.5% / 81% with edgeMaxAge 300 noticeably worse modelling and some unused cells
 * TESTED 83.8% / 78.2% with edgeMaxAge 1000 and learningRateNeighbours 0.1 of LR.
 *
 * TODO nonbinary output? Maybe the overlap? (normalized by the total weight aka max overlap possible for each cell?)
 * Created by dave on 8/07/16.
 */
public class KSparseGrowingNeuralGasDemo extends CreateEntityMain {

    public static void main( String[] args ) {
        KSparseGrowingNeuralGasDemo demo = new KSparseGrowingNeuralGasDemo();
        demo.mainImpl( args );
    }

    public void createEntities( Node n ) {

//        String trainingPath = "./training";
//        String testingPath = "./testing";

        String trainingPath = "/home/dave/workspace/agi.io/data/mnist/10k_train";
        String  testingPath = "/home/dave/workspace/agi.io/data/mnist/10k_train,/home/dave/workspace/agi.io/data/mnist/1k_test";

//        String trainingPath = "/home/dave/workspace/agi.io/data/mnist/cycle10";
//        String testingPath = "/home/dave/workspace/agi.io/data/mnist/cycle10";

        boolean cacheAllData = true;
        boolean terminateByAge = false;
        int terminationAge = 0;
        int trainingEpochs = 6;//1;
        int testingEpochs = 1;

        // Define some entities
        String experimentName           = Framework.GetEntityName( "experiment" );
        String imageLabelName           = Framework.GetEntityName( "image-class" );
        String competitiveLearningName  = Framework.GetEntityName( "competitive-learning" );
        String vectorSeriesName         = Framework.GetEntityName( "feature-series" );
        String valueSeriesName          = Framework.GetEntityName( "label-series" );

        Framework.CreateEntity( experimentName, ExperimentEntity.ENTITY_TYPE, n.getName(), null ); // experiment is the root entity
        Framework.CreateEntity( imageLabelName, ImageLabelEntity.ENTITY_TYPE, n.getName(), experimentName );
        Framework.CreateEntity( competitiveLearningName, KSparseGngEntity.ENTITY_TYPE, n.getName(), imageLabelName );
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

        float sparsity = 0.02442f; // 25/1024
        int widthCells = 32;
        int heightCells = 32;
        int edgeMaxAge = 1000; // 300 no good
        int growthInterval = 1;//50;                                        // split two most stressed after growthInterval steps
        float learningRate = 0.01f;//0.1f;//0.01f;                                     // winner, make similar to input
//        float learningRateNeighbours = learningRate * 0.2f;             // all connected neighbours make similar by this amount
//        float learningRateNeighbours = learningRate * 0.01f;             // because we have more neighbours, don't learn them to be too similar
        float learningRateNeighbours = learningRate * 0.1f;             // because we have more neighbours, don't learn them to be too similar
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
        Framework.SetConfig( competitiveLearningName, "sparsity", String.valueOf( sparsity ) );

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
