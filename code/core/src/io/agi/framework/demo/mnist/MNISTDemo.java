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

package io.agi.framework.demo.mnist;

import io.agi.core.sdr.NumberEncoder;
import io.agi.core.util.images.BufferedImageSource.BufferedImageSourceFactory;
import io.agi.framework.Framework;
import io.agi.framework.Main;
import io.agi.framework.Node;
import io.agi.framework.entities.*;
import io.agi.framework.factories.CommonEntityFactory;

/**
 * Created by gideon on 14/03/2016.
 */
public class MNISTDemo {

    public static void main( String[] args ) {

        // Create a Node
        Main m = new Main();
//        m.setup( args[ 0 ], null, new CommonEntityFactory() );
        m.setup( args[ 0 ], null, new MnistEntityFactory() );

        // Create custom entities and references
        if( args.length > 1 ) {
            if( args[ 1 ].equalsIgnoreCase( "create" ) ) {
                // Programmatic hook to create entities and references..
                createEntities( m._n );
            }
        }

        // Start the system
        m.run();

    }

    public static void createEntities( Node n ) {

//        String trainingPath = "/home/dave/workspace/agi.io/data/mnist/cycle10";
//        String testingPath = "/home/dave/workspace/agi.io/data/mnist/cycle10";
//        String trainingPath = "/home/dave/workspace/agi.io/data/mnist/cycle3";
//        String testingPath = "/home/dave/workspace/agi.io/data/mnist/cycle3";
//        String trainingPath = "/home/dave/workspace/agi.io/data/mnist/cycle_twin";
//        String testingPath = "/home/dave/workspace/agi.io/data/mnist/cycle_twin";
        String trainingPath = "./training";
        String testingPath = "./testing";
//        int terminationAge = -1; // run until complete
        int terminationAge = 2000;
        int trainingBatches = 3;
        boolean testClassRegion = false;
        boolean terminateByAge = true;
//        boolean terminateByAge = false;

        // Define some entities
        String experimentName = "experiment";
        String mnistName = "mnist";
        String imageEncoderName = "image-encoder";
        String classEncoderName = "class-encoder";
        String classDecoderName = "class-decoder";
        String constantName = "constant";
        String imageRegionName = "image-region";
        String classRegionName = "class-region";
        String activityImageDecoderName = "activity-image-decoder";
        String predictedImageDecoderName = "predicted-image-decoder";

        Framework.CreateEntity( experimentName, ExperimentEntity.ENTITY_TYPE, n.getName(), null ); // experiment is the root entity
        Framework.CreateEntity( mnistName, MnistEntity.ENTITY_TYPE, n.getName(), experimentName );
        Framework.CreateEntity( classEncoderName, EncoderEntity.ENTITY_TYPE, n.getName(), mnistName );
        Framework.CreateEntity( imageEncoderName, EncoderEntity.ENTITY_TYPE, n.getName(), classEncoderName );
        Framework.CreateEntity( constantName, ConstantMatrixEntity.ENTITY_TYPE, n.getName(), imageEncoderName ); // ok all input to the regions is ready

        if( testClassRegion ) {
            Framework.CreateEntity( imageRegionName, RegionLayerEntity.ENTITY_TYPE, n.getName(), null ); // disconnect image region
            Framework.CreateEntity( classRegionName, RegionLayerEntity.ENTITY_TYPE, n.getName(), constantName ); // test
        }
        else {
            Framework.CreateEntity( imageRegionName, RegionLayerEntity.ENTITY_TYPE, n.getName(), constantName );
            Framework.CreateEntity( classRegionName, RegionLayerEntity.ENTITY_TYPE, n.getName(), imageRegionName ); // 2nd, class region updates after first to get its feedback
        }

        Framework.CreateEntity( classDecoderName, DecoderEntity.ENTITY_TYPE, n.getName(), classRegionName ); // produce the predicted classification for inspection by mnist next time
        Framework.CreateEntity( activityImageDecoderName, DecoderEntity.ENTITY_TYPE, n.getName(), classRegionName );
        Framework.CreateEntity( predictedImageDecoderName, DecoderEntity.ENTITY_TYPE, n.getName(), classRegionName );

        // Connect the entities' data
        // a) Image to image region, and decode
        Framework.SetDataReference( imageEncoderName, EncoderEntity.DATA_INPUT, mnistName, MnistEntity.OUTPUT_IMAGE );
        Framework.SetDataReference( imageRegionName, RegionLayerEntity.FF_INPUT, imageEncoderName, EncoderEntity.DATA_OUTPUT_ENCODED );
        Framework.SetDataReference( imageRegionName, RegionLayerEntity.FB_INPUT, constantName, ConstantMatrixEntity.OUTPUT ); // feedback to this region is just a constant
        Framework.SetDataReference( activityImageDecoderName, DecoderEntity.DATA_INPUT_ENCODED, imageRegionName, RegionLayerEntity.FB_OUTPUT_UNFOLDED_ACTIVITY );
        Framework.SetDataReference( predictedImageDecoderName, DecoderEntity.DATA_INPUT_ENCODED, imageRegionName, RegionLayerEntity.FB_OUTPUT_UNFOLDED_PREDICTION );

        // a) Class to class region, and decode
        Framework.SetDataReference( classEncoderName, EncoderEntity.DATA_INPUT, mnistName, MnistEntity.OUTPUT_CLASSIFICATION );
        Framework.SetDataReference( classRegionName, RegionLayerEntity.FF_INPUT, classEncoderName, EncoderEntity.DATA_OUTPUT_ENCODED );

        if( testClassRegion ) {
            Framework.SetDataReference( classRegionName, RegionLayerEntity.FB_INPUT, constantName, ConstantMatrixEntity.OUTPUT ); // get current state from the region to be used to predict
        }
        else { // whole system, use image classification to predict
            Framework.SetDataReference( classRegionName, RegionLayerEntity.FB_INPUT, imageRegionName, RegionLayerEntity.ACTIVITY_NEW ); // get current state from the region to be used to predict
        }

        Framework.SetDataReference( classDecoderName, DecoderEntity.DATA_INPUT_ENCODED, classRegionName, RegionLayerEntity.FB_OUTPUT_UNFOLDED_PREDICTION ); // the prediction of the next state
        Framework.SetDataReference( mnistName, MnistEntity.INPUT_CLASSIFICATION, classDecoderName, DecoderEntity.DATA_OUTPUT_DECODED ); // the (decoded) prediction of the next state

        // Experiment config
        Framework.SetConfig( experimentName, "terminationAge", String.valueOf( terminationAge ) ); // fixed steps
//        Framework.SetConfig( experimentName, "terminationAge", "-1" ); // wait for mnist to decide
        if( !terminateByAge ) {
            Framework.SetConfig( experimentName, "terminationEntityName", mnistName );
            Framework.SetConfig( experimentName, "terminationConfigPath", "terminate" );
        }

        // Mnist config
        Framework.SetConfig( mnistName, "receptiveField.receptiveFieldX", "0" );
        Framework.SetConfig( mnistName, "receptiveField.receptiveFieldY", "0" );
        Framework.SetConfig( mnistName, "receptiveField.receptiveFieldW", "28" );
        Framework.SetConfig( mnistName, "receptiveField.receptiveFieldH", "28" );
        Framework.SetConfig( mnistName, "resolution.resolutionX", "28" );
        Framework.SetConfig( mnistName, "resolution.resolutionY", "28" );
        Framework.SetConfig( mnistName, "greyscale", "true" );
        Framework.SetConfig( mnistName, "invert", "true" );
        Framework.SetConfig( mnistName, "sourceType", BufferedImageSourceFactory.TYPE_IMAGE_FILES );
        Framework.SetConfig( mnistName, "sourceFilesPrefix", "postproc" );
        Framework.SetConfig( mnistName, "sourceFilesPathTraining", trainingPath );
        Framework.SetConfig( mnistName, "sourceFilesPathTesting", testingPath );
        Framework.SetConfig( mnistName, "trainingBatches", String.valueOf( trainingBatches ) );

        // constant config

        // image encoder config
        Framework.SetConfig( imageEncoderName, "density", "1" );
        Framework.SetConfig( imageEncoderName, "bits", "1" );
        Framework.SetConfig( imageEncoderName, "encodeZero", "false" );

        // image decoder config x2
        Framework.SetConfig( activityImageDecoderName, "density", "1" );
        Framework.SetConfig( activityImageDecoderName, "bits", "1" );
        Framework.SetConfig( activityImageDecoderName, "encodeZero", "false" );

        Framework.SetConfig( predictedImageDecoderName, "density", "1" );
        Framework.SetConfig( predictedImageDecoderName, "bits", "1" );
        Framework.SetConfig( predictedImageDecoderName, "encodeZero", "false" );

        // class encoder config
        Framework.SetConfig( classEncoderName, "encoderType", NumberEncoder.class.getSimpleName() );
        Framework.SetConfig( classEncoderName, "digits", "2" );
        Framework.SetConfig( classEncoderName, "numbers", "1" );

        // class decoder config
        Framework.SetConfig( classDecoderName, "encoderType", NumberEncoder.class.getSimpleName() );
        Framework.SetConfig( classDecoderName, "digits", "2" );
        Framework.SetConfig( classDecoderName, "numbers", "1" );

        // image region config
        Framework.SetConfig( imageRegionName, "predictorLearningRate", "100" );
        Framework.SetConfig( imageRegionName, "receptiveFieldsTrainingSamples", "0.1" );
        Framework.SetConfig( imageRegionName, "classifiersPerBit", "5" );
        Framework.SetConfig( imageRegionName, "organizerStressThreshold", "0.0" );
        Framework.SetConfig( imageRegionName, "organizerGrowthInterval", "1" );
        Framework.SetConfig( imageRegionName, "organizerEdgeMaxAge", "1000" );
        Framework.SetConfig( imageRegionName, "organizerNoiseMagnitude", "0.0" );
        Framework.SetConfig( imageRegionName, "organizerLearningRate", "0.002" );
        Framework.SetConfig( imageRegionName, "organizerLearningRateNeighbours", "0.001" );
        Framework.SetConfig( imageRegionName, "organizerWidthCells", "8" );
        Framework.SetConfig( imageRegionName, "organizerHeightCells", "8" );
        Framework.SetConfig( imageRegionName, "classifierWidthCells", "5" );
        Framework.SetConfig( imageRegionName, "classifierHeightCells", "5" );
        Framework.SetConfig( imageRegionName, "classifierStressThreshold", "0.0" );
        Framework.SetConfig( imageRegionName, "classifierGrowthInterval", "10" );
        Framework.SetConfig( imageRegionName, "classifierEdgeMaxAge", "12" );

        // class region config
        Framework.SetConfig( classRegionName, "predictorLearningRate", "100" );
        Framework.SetConfig( classRegionName, "receptiveFieldsTrainingSamples", "0.1" );
        Framework.SetConfig( classRegionName, "classifiersPerBit", "5" );
        Framework.SetConfig( classRegionName, "organizerStressThreshold", "0.0" );
        Framework.SetConfig( classRegionName, "organizerGrowthInterval", "1" );
        Framework.SetConfig( classRegionName, "organizerEdgeMaxAge", "1000" );
        Framework.SetConfig( classRegionName, "organizerNoiseMagnitude", "0.0" );
        Framework.SetConfig( classRegionName, "organizerLearningRate", "0.002" );
        Framework.SetConfig( classRegionName, "organizerLearningRateNeighbours", "0.001" );
        Framework.SetConfig( classRegionName, "organizerWidthCells", "2" );
        Framework.SetConfig( classRegionName, "organizerHeightCells", "2" );
//        Framework.SetConfig( classRegionName, "classifierWidthCells", "4" );
//        Framework.SetConfig( classRegionName, "classifierHeightCells", "4" );
        Framework.SetConfig( classRegionName, "classifierWidthCells", "5" );
        Framework.SetConfig( classRegionName, "classifierHeightCells", "5" );
        Framework.SetConfig( classRegionName, "classifierStressThreshold", "0.0" );
        Framework.SetConfig( classRegionName, "classifierGrowthInterval", "10" );
        Framework.SetConfig( classRegionName, "classifierEdgeMaxAge", "30" );
    }

}
