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
import io.agi.framework.demo.mnist.ImageLabelEntity;
import io.agi.framework.entities.*;
import io.agi.framework.factories.CommonEntityFactory;

import java.util.ArrayList;
import java.util.Properties;

/**
 * Quilted, for reasons of convolutional representation helps.
 *
 * Created by dave on 8/07/16.
 */
public class QuiltedCompetitiveLearningDemo {

    /**
     * Usage: Expects some arguments. These are:
     * 0: node.properties file
     * 1 to n: 'create' flag and/or 'prefix' flag
     * @param args
     */
    public static void main( String[] args ) {

        // Create a Node
        Main m = new Main();
        Properties p = PropertiesUtil.load( args[ 0 ] );
        m.setup( p, null, new CommonEntityFactory() );

        // Optionally set a global prefix for entities
        for( int i = 1; i < args.length; ++i ) {
            String arg = args[ i ];
            if( arg.equalsIgnoreCase( "prefix" ) ) {
                String prefix = args[ i+1 ];
                Framework.SetEntityNamePrefix( prefix );
//                Framework.SetEntityNamePrefixDateTime();
            }
        }

        // Optionally create custom entities and references
        for( int i = 1; i < args.length; ++i ) {
            String arg = args[ i ];
            if( arg.equalsIgnoreCase( "create" ) ) {
                createEntities( m._n );
            }
        }

        // Start the system
        m.run();
    }

    public static void createEntities( Node n ) {

//        String trainingPath = "./training";
//        String testingPath = "./testing";

        String trainingPath = "/home/dave/workspace/agi.io/data/mnist/1k_test";
        String  testingPath = "/home/dave/workspace/agi.io/data/mnist/1k_test";

//        String trainingPath = "/home/dave/workspace/agi.io/data/mnist/cycle10";
//        String testingPath = "/home/dave/workspace/agi.io/data/mnist/cycle10,/home/dave/workspace/agi.io/data/mnist/cycle3";

        boolean cacheAllData = true;
        boolean terminateByAge = false;
//        int terminationAge = 10;//9000;
        int terminationAge = 50000;//25000;
        int trainingEpochs = 10;//80; // good for up to 80k
        int testingEpochs = 1;//80; // good for up to 80k

        // Define some entities
        String experimentName           = Framework.GetEntityName( "experiment" );
        String constantName             = Framework.GetEntityName( "constant" );
        String imageLabelName           = Framework.GetEntityName( "image-class" );
        String quiltName                = Framework.GetEntityName( "quilt" );
        String vectorSeriesName         = Framework.GetEntityName( "feature-series" );
        String valueSeriesName          = Framework.GetEntityName( "label-series" );

        Framework.CreateEntity( experimentName, ExperimentEntity.ENTITY_TYPE, n.getName(), null ); // experiment is the root entity
        Framework.CreateEntity( constantName, ConstantMatrixEntity.ENTITY_TYPE, n.getName(), experimentName ); // ok all input to the regions is ready
        Framework.CreateEntity( imageLabelName, ImageLabelEntity.ENTITY_TYPE, n.getName(), constantName );
        Framework.CreateEntity( quiltName, QuiltedCompetitiveLearningEntity.ENTITY_TYPE, n.getName(), imageLabelName );
        Framework.CreateEntity( vectorSeriesName, VectorSeriesEntity.ENTITY_TYPE, n.getName(), quiltName ); // 2nd, class region updates after first to get its feedback
        Framework.CreateEntity( valueSeriesName, ValueSeriesEntity.ENTITY_TYPE, n.getName(), vectorSeriesName ); // 2nd, class region updates after first to get its feedback

        // Connect the entities' data
        // a) Image to image region, and decode
        Framework.SetDataReference( quiltName, QuiltedCompetitiveLearningEntity.INPUT_1, imageLabelName, ImageLabelEntity.OUTPUT_IMAGE );
        Framework.SetDataReference( quiltName, QuiltedCompetitiveLearningEntity.INPUT_2, constantName, ConstantMatrixEntity.OUTPUT );

        ArrayList< AbstractPair< String, String > > featureDatas = new ArrayList< AbstractPair< String, String > >();
        featureDatas.add( new AbstractPair< String, String >( quiltName, QuiltedCompetitiveLearningEntity.OUTPUT_QUILT ) );
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
        Framework.SetConfig( constantName, "cache", String.valueOf( cacheAllData ) );
        Framework.SetConfig( imageLabelName, "cache", String.valueOf( cacheAllData ) );
        Framework.SetConfig( quiltName, "cache", String.valueOf( cacheAllData ) );
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
        Framework.SetConfig( imageLabelName, "trainingEntities", String.valueOf( quiltName ) );
        Framework.SetConfig( imageLabelName, "testingEntities", vectorSeriesName + "," + valueSeriesName );

        boolean emit2ndBest = false;
        int edgeMaxAge = 500;
        int growthInterval = 200;//50;
        float learningRate = 0.01f;
        float learningRateNeighbours = learningRate * 0.2f;
        float noiseMagnitude = 0.0f;
        float stressLearningRate = 0.01f; // not used now?
        float stressSplitLearningRate = 0.5f; // change to stress after a split
        float stressThreshold = 0.01f; // when it ceases to split
        float utilityLearningRate = stressLearningRate;
        float utilityThreshold = -1f;//5f;//-1f;

        // 25 * 49 = 1225
        // 36 * 36 = 1296
//        int columnWidthCells = 5;  // 25 cells per col
//        int columnHeightCells = 5;
        int columnWidthCells = 6;  // 36 cells per col
        int columnHeightCells = 6;

//        int quiltWidthColumns = 7;
//        int quiltHeightColumns = 7; // 49 cols
        int quiltWidthColumns = 6;
        int quiltHeightColumns = 6; // 36 cols

        // TODO add a field offset
        // Field 2: 28x28
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
/*        int field1OffsetX = 2;
        int field1OffsetY = 2;

        int field1SizeX = 6;
        int field1SizeY = 6;

        int field1StrideX = 4;
        int field1StrideY = 4;*/

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

        // Field 2: 1x1
        int field2StrideX = 0;
        int field2StrideY = 0;

        int field2SizeX = 1;
        int field2SizeY = 1;

        Framework.SetConfig( quiltName, "quiltWidth", String.valueOf( quiltWidthColumns ) );
        Framework.SetConfig( quiltName, "quiltHeight", String.valueOf( quiltHeightColumns ) );

        Framework.SetConfig( quiltName, "classifierWidth", String.valueOf( columnWidthCells ) );
        Framework.SetConfig( quiltName, "classifierHeight", String.valueOf( columnHeightCells ) );

        Framework.SetConfig( quiltName, "field1OffsetX", String.valueOf( field1OffsetX ) );
        Framework.SetConfig( quiltName, "field1OffsetY", String.valueOf( field1OffsetY ) );
        Framework.SetConfig( quiltName, "field1StrideX", String.valueOf( field1StrideX ) );
        Framework.SetConfig( quiltName, "field1StrideY", String.valueOf( field1StrideY ) );
        Framework.SetConfig( quiltName, "field1SizeX", String.valueOf( field1SizeX ) );
        Framework.SetConfig( quiltName, "field1SizeY", String.valueOf( field1SizeY ) );

        Framework.SetConfig( quiltName, "field2StrideX", String.valueOf( field2StrideX ) );
        Framework.SetConfig( quiltName, "field2StrideY", String.valueOf( field2StrideY ) );
        Framework.SetConfig( quiltName, "field2SizeX", String.valueOf( field2SizeX ) );
        Framework.SetConfig( quiltName, "field2SizeY", String.valueOf( field2SizeY ) );

        Framework.SetConfig( quiltName, "emit2ndBest", String.valueOf( emit2ndBest ) );

        Framework.SetConfig( quiltName, "classifierLearningRate", String.valueOf( learningRate ) );
        Framework.SetConfig( quiltName, "classifierLearningRateNeighbours", String.valueOf( learningRateNeighbours ) );
        Framework.SetConfig( quiltName, "classifierNoiseMagnitude", String.valueOf( noiseMagnitude ) );
        Framework.SetConfig( quiltName, "classifierEdgeMaxAge", String.valueOf( edgeMaxAge ) );
        Framework.SetConfig( quiltName, "classifierStressLearningRate", String.valueOf( stressLearningRate ) );
        Framework.SetConfig( quiltName, "classifierStressSplitLearningRate", String.valueOf( stressSplitLearningRate ) );
        Framework.SetConfig( quiltName, "classifierStressThreshold", String.valueOf( stressThreshold ) );
        Framework.SetConfig( quiltName, "classifierUtilityLearningRate", String.valueOf( utilityLearningRate ) );
        Framework.SetConfig( quiltName, "classifierUtilityThreshold", String.valueOf( utilityThreshold ) );
        Framework.SetConfig( quiltName, "classifierGrowthInterval", String.valueOf( growthInterval ) );

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
