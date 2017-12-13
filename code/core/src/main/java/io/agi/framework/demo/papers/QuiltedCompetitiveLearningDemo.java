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
import io.agi.framework.Naming;
import io.agi.framework.Node;
import io.agi.framework.demo.CreateEntityMain;
import io.agi.framework.demo.mnist.ImageLabelEntity;
import io.agi.framework.entities.*;
import io.agi.framework.factories.CommonEntityFactory;
import io.agi.framework.persistence.DataJsonSerializer;
import io.agi.framework.persistence.PersistenceUtil;
import io.agi.framework.persistence.models.ModelData;
import io.agi.framework.references.DataRefUtil;

import java.util.ArrayList;
import java.util.Properties;

/**
 * Quilted, for reasons of convolutional representation helps.
 *
 * Created by dave on 8/07/16.
 */
public class QuiltedCompetitiveLearningDemo extends CreateEntityMain {

    public static void main( String[] args ) {
        QuiltedCompetitiveLearningDemo demo = new QuiltedCompetitiveLearningDemo();
        demo.mainImpl( args );
    }

    public void createEntities( Node n ) {

//        String trainingPath = "./training";
//        String testingPath = "./testing";

//        String trainingPath = "/home/dave/workspace/agi.io/data/mnist/10k_train";
//        String  testingPath = "/home/dave/workspace/agi.io/data/mnist/10k_train,/home/dave/workspace/agi.io/data/mnist/1k_test";

        String trainingPath = "/home/dave/workspace/agi.io/data/mnist/1k_test";
        String  testingPath = "/home/dave/workspace/agi.io/data/mnist/1k_test";

//        String trainingPath = "/home/dave/workspace/agi.io/data/mnist/all/all_train";
//        String  testingPath = "/home/dave/workspace/agi.io/data/mnist/all/all_train,/home/dave/workspace/agi.io/data/mnist/all/all_t10k";

//        String trainingPath = "/home/dave/workspace/agi.io/data/mnist/cycle10";
//        String testingPath = "/home/dave/workspace/agi.io/data/mnist/cycle10,/home/dave/workspace/agi.io/data/mnist/cycle3";

        Integer seed = null; // 1;
        boolean cacheAllData = true;
        boolean terminateByAge = false;
//        int terminationAge = 10;//9000;
        int terminationAge = 50000;//25000;
        int trainingEpochs = 2;//80; // good for up to 80k
        int testingEpochs = 1;//80; // good for up to 80k

        // Define some entities
        String experimentName           = Naming.GetEntityName( "experiment" );
        String constantName             = Naming.GetEntityName( "constant" );
        String imageLabelName           = Naming.GetEntityName( "image-class" );
        String quiltName                = Naming.GetEntityName( "quilt" );
        String vectorSeriesName         = Naming.GetEntityName( "feature-series" );
        String valueSeriesName          = Naming.GetEntityName( "label-series" );

        PersistenceUtil.CreateEntity( experimentName, ExperimentEntity.ENTITY_TYPE, n.getName(), null ); // experiment is the root entity
        PersistenceUtil.CreateEntity( constantName, ConstantMatrixEntity.ENTITY_TYPE, n.getName(), experimentName ); // ok all input to the regions is ready
        PersistenceUtil.CreateEntity( imageLabelName, ImageLabelEntity.ENTITY_TYPE, n.getName(), constantName );
        PersistenceUtil.CreateEntity( quiltName, QuiltedCompetitiveLearningEntity.ENTITY_TYPE, n.getName(), imageLabelName );
        PersistenceUtil.CreateEntity( vectorSeriesName, VectorSeriesEntity.ENTITY_TYPE, n.getName(), quiltName ); // 2nd, class region updates after first to get its feedback
        PersistenceUtil.CreateEntity( valueSeriesName, ValueSeriesEntity.ENTITY_TYPE, n.getName(), vectorSeriesName ); // 2nd, class region updates after first to get its feedback

        // Connect the entities' data
        // a) Image to image region, and decode
        DataRefUtil.SetDataReference( quiltName, QuiltedCompetitiveLearningEntity.INPUT_1, imageLabelName, ImageLabelEntity.OUTPUT_IMAGE );
        DataRefUtil.SetDataReference( quiltName, QuiltedCompetitiveLearningEntity.INPUT_2, constantName, ConstantMatrixEntity.OUTPUT );

        ArrayList< AbstractPair< String, String > > featureDatas = new ArrayList<>();
        featureDatas.add( new AbstractPair<>( quiltName, QuiltedCompetitiveLearningEntity.OUTPUT_QUILT ) );
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
        PersistenceUtil.SetConfig( constantName, "cache", String.valueOf( cacheAllData ) );
        PersistenceUtil.SetConfig( imageLabelName, "cache", String.valueOf( cacheAllData ) );
        PersistenceUtil.SetConfig( quiltName, "cache", String.valueOf( cacheAllData ) );
        PersistenceUtil.SetConfig( vectorSeriesName, "cache", String.valueOf( cacheAllData ) );
        PersistenceUtil.SetConfig( valueSeriesName, "cache", String.valueOf( cacheAllData ) );

        // Fix seed for repeatable runs
        if( seed != null ) {
            PersistenceUtil.SetConfig( imageLabelName, "seed", String.valueOf( seed ) );
            PersistenceUtil.SetConfig( quiltName, "seed", String.valueOf( seed ) );
        }

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
        PersistenceUtil.SetConfig( imageLabelName, "trainingEntities", String.valueOf( quiltName ) );
        PersistenceUtil.SetConfig( imageLabelName, "testingEntities", vectorSeriesName + "," + valueSeriesName );

        boolean emit2ndBest = false;
        int edgeMaxAge = 500;
        int growthInterval = 50;//200;//50;
        float learningRate = 0.01f;
        float learningRateNeighbours = learningRate * 0.2f;
        float noiseMagnitude = 0.0f;
        float stressLearningRate = 0.01f; // not used now?
        float stressSplitLearningRate = 0.5f; // change to stress after a split
        float stressThreshold = 0.01f; // when it ceases to split
        float utilityLearningRate = stressLearningRate;
        float utilityThreshold = -1f;//5f;//-1f;   -1 disables
        float denoisePercentage = 0;

        // 25 * 49 = 1225
        // 36 * 36 = 1296
        int columnWidthCells = 5;  // 25 cells per col
        int columnHeightCells = 5;
//        int columnWidthCells = 7;  // 36 cells per col
//        int columnHeightCells = 7;

//        int quiltWidthColumns = 7;
//        int quiltHeightColumns = 7; // 49 cols
        int quiltWidthColumns = 6;
        int quiltHeightColumns = 6; // 36 cols

        // Field 2: 1x1
        int field2OffsetX = 0;
        int field2OffsetY = 0;
        int field2SizeX = 1;
        int field2SizeY = 1;
        int field2StrideX = 0;
        int field2StrideY = 0;

        // Field 1: 28x28
        //     00 01 02 03 04 05 06 07 08 09 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 |
        //  F1 -- -- -- -- -- --                                                                   |
        //  F2             -- -- -- -- -- --                                                       |
        //  F3                         -- -- -- -- -- --                                           |
        //  F4                                     -- -- -- -- -- --                               |
        //  F5                                                 -- -- -- -- -- --                   |
        //  F6                                                             -- -- -- -- -- --       |
        //  F7                                                                         -- -- -- -- -- --
        //     00 01 02 03 04 05 06 07 08 09 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 |
        //  F1       -- -- -- -- -- --                                                             |
        //  F2                -- -- -- -- -- --                                                    |
        //  F3                         -- -- -- -- -- --                                           |
        //  F4                                  -- -- -- -- -- --                                  |
        //  F5                                           -- -- -- -- -- --                         |
        //  F6                                                    -- -- -- -- -- --                |
        //  F7                                                             -- -- -- -- -- --       |
//        int field1OffsetX = 2;
//        int field1OffsetY = 2;
//        int field1SizeX = 6;
//        int field1SizeY = 6;
//        int field1StrideX = 3;
//        int field1StrideY = 3;

        //     00 01 02 03 04 05 06 07 08 09 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 |
        //  F1          -- -- -- -- -- --                                                          |
        //  F2                   -- -- -- -- -- --                                                 |
        //  F3                            -- -- -- -- -- --                                        |
        //  F4                                     -- -- -- -- -- --                               |
        //  F5                                              -- -- -- -- -- --                      |
        //  F6                                                       -- -- -- -- -- --             |
        int field1OffsetX = 3;
        int field1OffsetY = 3;

        int field1SizeX = 6;
        int field1SizeY = 6;

        int field1StrideX = 3;
        int field1StrideY = 3;

        PersistenceUtil.SetConfig( quiltName, "quiltWidth", String.valueOf( quiltWidthColumns ) );
        PersistenceUtil.SetConfig( quiltName, "quiltHeight", String.valueOf( quiltHeightColumns ) );

        PersistenceUtil.SetConfig( quiltName, "classifierWidth", String.valueOf( columnWidthCells ) );
        PersistenceUtil.SetConfig( quiltName, "classifierHeight", String.valueOf( columnHeightCells ) );

        PersistenceUtil.SetConfig( quiltName, "field1OffsetX", String.valueOf( field1OffsetX ) );
        PersistenceUtil.SetConfig( quiltName, "field1OffsetY", String.valueOf( field1OffsetY ) );
        PersistenceUtil.SetConfig( quiltName, "field1StrideX", String.valueOf( field1StrideX ) );
        PersistenceUtil.SetConfig( quiltName, "field1StrideY", String.valueOf( field1StrideY ) );
        PersistenceUtil.SetConfig( quiltName, "field1SizeX", String.valueOf( field1SizeX ) );
        PersistenceUtil.SetConfig( quiltName, "field1SizeY", String.valueOf( field1SizeY ) );

        PersistenceUtil.SetConfig( quiltName, "field2OffsetX", String.valueOf( field2OffsetX ) );
        PersistenceUtil.SetConfig( quiltName, "field2OffsetY", String.valueOf( field2OffsetY ) );
        PersistenceUtil.SetConfig( quiltName, "field2StrideX", String.valueOf( field2StrideX ) );
        PersistenceUtil.SetConfig( quiltName, "field2StrideY", String.valueOf( field2StrideY ) );
        PersistenceUtil.SetConfig( quiltName, "field2SizeX", String.valueOf( field2SizeX ) );
        PersistenceUtil.SetConfig( quiltName, "field2SizeY", String.valueOf( field2SizeY ) );

        PersistenceUtil.SetConfig( quiltName, "emit2ndBest", String.valueOf( emit2ndBest ) );

        PersistenceUtil.SetConfig( quiltName, "classifierLearningRate", String.valueOf( learningRate ) );
        PersistenceUtil.SetConfig( quiltName, "classifierLearningRateNeighbours", String.valueOf( learningRateNeighbours ) );
        PersistenceUtil.SetConfig( quiltName, "classifierNoiseMagnitude", String.valueOf( noiseMagnitude ) );
        PersistenceUtil.SetConfig( quiltName, "classifierEdgeMaxAge", String.valueOf( edgeMaxAge ) );
        PersistenceUtil.SetConfig( quiltName, "classifierStressLearningRate", String.valueOf( stressLearningRate ) );
        PersistenceUtil.SetConfig( quiltName, "classifierStressSplitLearningRate", String.valueOf( stressSplitLearningRate ) );
        PersistenceUtil.SetConfig( quiltName, "classifierStressThreshold", String.valueOf( stressThreshold ) );
        PersistenceUtil.SetConfig( quiltName, "classifierUtilityLearningRate", String.valueOf( utilityLearningRate ) );
        PersistenceUtil.SetConfig( quiltName, "classifierUtilityThreshold", String.valueOf( utilityThreshold ) );
        PersistenceUtil.SetConfig( quiltName, "classifierGrowthInterval", String.valueOf( growthInterval ) );
        PersistenceUtil.SetConfig( quiltName, "classifierDenoisePercentage", String.valueOf( denoisePercentage ) );

        // Log features of the algorithm during all phases
        PersistenceUtil.SetConfig( vectorSeriesName, "period", String.valueOf( "-1" ) ); // infinite
        PersistenceUtil.SetConfig( vectorSeriesName, "learn", String.valueOf( "true" ) ); // infinite
        PersistenceUtil.SetConfig( vectorSeriesName, "encoding", DataJsonSerializer.ENCODING_SPARSE_BINARY );

        // Log labels of each image produced during all phases
        PersistenceUtil.SetConfig( valueSeriesName, "period", "-1" );
        PersistenceUtil.SetConfig( valueSeriesName, "learn", String.valueOf( "true" ) ); // infinite
        PersistenceUtil.SetConfig( valueSeriesName, "entityName", imageLabelName ); // log forever
        PersistenceUtil.SetConfig( valueSeriesName, "configPath", "imageLabel" ); // log forever

    }

}
