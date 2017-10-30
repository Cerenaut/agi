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
import io.agi.core.util.PropertiesUtil;
import io.agi.core.util.images.BufferedImageSource.BufferedImageSourceFactory;
import io.agi.framework.Framework;
import io.agi.framework.Main;
import io.agi.framework.Node;
import io.agi.framework.entities.*;
import io.agi.framework.persistence.PersistenceUtil;
import io.agi.framework.references.DataRefUtil;

import java.util.Properties;

/**
 * Created by gideon on 14/03/2016.
 */
public class MNISTDemo {

    public static void main( String[] args ) {

        // Create a Node
        Main m = new Main();
        Properties p = PropertiesUtil.load( args[ 0 ] );
        m.setup( p, null, new MnistEntityFactory() );

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
//        String trainingPath = "/home/dave/workspace/agi.io/data/mnist/cycle_deep";
//        String testingPath = "/home/dave/workspace/agi.io/data/mnist/cycle_deep";
        String trainingPath = "./training";
        String testingPath = "./testing";
        int terminationAge = 2000;
        int trainingBatches = 2;
        boolean testClassRegion = false;
//        boolean terminateByAge = true;
        boolean terminateByAge = false;

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

        PersistenceUtil.CreateEntity( experimentName, ExperimentEntity.ENTITY_TYPE, n.getName(), null ); // experiment is the root entity
        PersistenceUtil.CreateEntity( mnistName, MnistEntity.ENTITY_TYPE, n.getName(), experimentName );
        PersistenceUtil.CreateEntity( classEncoderName, EncoderEntity.ENTITY_TYPE, n.getName(), mnistName );
        PersistenceUtil.CreateEntity( imageEncoderName, EncoderEntity.ENTITY_TYPE, n.getName(), classEncoderName );
        PersistenceUtil.CreateEntity( constantName, ConstantMatrixEntity.ENTITY_TYPE, n.getName(), imageEncoderName ); // ok all input to the regions is ready

        if( testClassRegion ) {
            PersistenceUtil.CreateEntity( imageRegionName, RegionLayerEntity.ENTITY_TYPE, n.getName(), null ); // disconnect image region
            PersistenceUtil.CreateEntity( classRegionName, RegionLayerEntity.ENTITY_TYPE, n.getName(), constantName ); // test
        }
        else {
            PersistenceUtil.CreateEntity( imageRegionName, RegionLayerEntity.ENTITY_TYPE, n.getName(), constantName );
            PersistenceUtil.CreateEntity( classRegionName, RegionLayerEntity.ENTITY_TYPE, n.getName(), imageRegionName ); // 2nd, class region updates after first to get its feedback
        }

        PersistenceUtil.CreateEntity( classDecoderName, DecoderEntity.ENTITY_TYPE, n.getName(), classRegionName ); // produce the predicted classification for inspection by mnist next time
        PersistenceUtil.CreateEntity( activityImageDecoderName, DecoderEntity.ENTITY_TYPE, n.getName(), classRegionName );
        PersistenceUtil.CreateEntity( predictedImageDecoderName, DecoderEntity.ENTITY_TYPE, n.getName(), classRegionName );

        // Connect the entities' data
        // a) Image to image region, and decode
        DataRefUtil.SetDataReference( imageEncoderName, EncoderEntity.DATA_INPUT, mnistName, MnistEntity.OUTPUT_IMAGE );
        DataRefUtil.SetDataReference( imageRegionName, RegionLayerEntity.FF_INPUT_1, imageEncoderName, EncoderEntity.DATA_OUTPUT_ENCODED );
        DataRefUtil.SetDataReference( imageRegionName, RegionLayerEntity.FF_INPUT_2, constantName, ConstantMatrixEntity.OUTPUT );
        DataRefUtil.SetDataReference( imageRegionName, RegionLayerEntity.FB_INPUT, constantName, ConstantMatrixEntity.OUTPUT ); // feedback to this region is just a constant
        DataRefUtil.SetDataReference( activityImageDecoderName, DecoderEntity.DATA_INPUT_ENCODED, imageRegionName, RegionLayerEntity.FB_OUTPUT_1_UNFOLDED_ACTIVITY );
        DataRefUtil.SetDataReference( predictedImageDecoderName, DecoderEntity.DATA_INPUT_ENCODED, imageRegionName, RegionLayerEntity.FB_OUTPUT_1_UNFOLDED_PREDICTION );

        // a) Class to class region, and decode
        DataRefUtil.SetDataReference( classEncoderName, EncoderEntity.DATA_INPUT, mnistName, MnistEntity.OUTPUT_CLASSIFICATION );
        DataRefUtil.SetDataReference( classRegionName, RegionLayerEntity.FF_INPUT_1, classEncoderName, EncoderEntity.DATA_OUTPUT_ENCODED );
        DataRefUtil.SetDataReference( classRegionName, RegionLayerEntity.FF_INPUT_2, constantName, ConstantMatrixEntity.OUTPUT );

        if( testClassRegion ) {
            DataRefUtil.SetDataReference( classRegionName, RegionLayerEntity.FB_INPUT, constantName, ConstantMatrixEntity.OUTPUT ); // get current state from the region to be used to predict
        }
        else { // whole system, use image classification to predict
            DataRefUtil.SetDataReference( classRegionName, RegionLayerEntity.FB_INPUT, imageRegionName, RegionLayerEntity.ACTIVITY_NEW ); // get current state from the region to be used to predict
        }

        DataRefUtil.SetDataReference( classDecoderName, DecoderEntity.DATA_INPUT_ENCODED, classRegionName, RegionLayerEntity.FB_OUTPUT_1_UNFOLDED_PREDICTION ); // the prediction of the next state
        DataRefUtil.SetDataReference( mnistName, MnistEntity.INPUT_CLASSIFICATION, classDecoderName, DecoderEntity.DATA_OUTPUT_DECODED ); // the (decoded) prediction of the next state

        // Experiment config
        if( !terminateByAge ) {
            PersistenceUtil.SetConfig( experimentName, "terminationEntityName", mnistName );
            PersistenceUtil.SetConfig( experimentName, "terminationConfigPath", "terminate" );
            PersistenceUtil.SetConfig( experimentName, "terminationAge", "-1" ); // wait for mnist to decide
        }
        else {
            PersistenceUtil.SetConfig( experimentName, "terminationAge", String.valueOf( terminationAge ) ); // fixed steps
        }

        // Mnist config
        PersistenceUtil.SetConfig( mnistName, "receptiveField.receptiveFieldX", "0" );
        PersistenceUtil.SetConfig( mnistName, "receptiveField.receptiveFieldY", "0" );
        PersistenceUtil.SetConfig( mnistName, "receptiveField.receptiveFieldW", "28" );
        PersistenceUtil.SetConfig( mnistName, "receptiveField.receptiveFieldH", "28" );
        PersistenceUtil.SetConfig( mnistName, "resolution.resolutionX", "28" );
        PersistenceUtil.SetConfig( mnistName, "resolution.resolutionY", "28" );
        PersistenceUtil.SetConfig( mnistName, "greyscale", "true" );
        PersistenceUtil.SetConfig( mnistName, "invert", "true" );
        PersistenceUtil.SetConfig( mnistName, "sourceType", BufferedImageSourceFactory.TYPE_IMAGE_FILES );
        PersistenceUtil.SetConfig( mnistName, "sourceFilesPrefix", "postproc" );
        PersistenceUtil.SetConfig( mnistName, "sourceFilesPathTraining", trainingPath );
        PersistenceUtil.SetConfig( mnistName, "sourceFilesPathTesting", testingPath );
        PersistenceUtil.SetConfig( mnistName, "trainingBatches", String.valueOf( trainingBatches ) );

        // constant config

        // image encoder config
        PersistenceUtil.SetConfig( imageEncoderName, "density", "1" );
        PersistenceUtil.SetConfig( imageEncoderName, "bits", "1" );
        PersistenceUtil.SetConfig( imageEncoderName, "encodeZero", "false" );

        // image decoder config x2
        PersistenceUtil.SetConfig( activityImageDecoderName, "density", "1" );
        PersistenceUtil.SetConfig( activityImageDecoderName, "bits", "1" );
        PersistenceUtil.SetConfig( activityImageDecoderName, "encodeZero", "false" );

        PersistenceUtil.SetConfig( predictedImageDecoderName, "density", "1" );
        PersistenceUtil.SetConfig( predictedImageDecoderName, "bits", "1" );
        PersistenceUtil.SetConfig( predictedImageDecoderName, "encodeZero", "false" );

        // class encoder config
        PersistenceUtil.SetConfig( classEncoderName, "encoderType", NumberEncoder.class.getSimpleName() );
        PersistenceUtil.SetConfig( classEncoderName, "digits", "2" );
        PersistenceUtil.SetConfig( classEncoderName, "numbers", "1" );

        // class decoder config
        PersistenceUtil.SetConfig( classDecoderName, "encoderType", NumberEncoder.class.getSimpleName() );
        PersistenceUtil.SetConfig( classDecoderName, "digits", "2" );
        PersistenceUtil.SetConfig( classDecoderName, "numbers", "1" );

        // image region config
        PersistenceUtil.SetConfig( imageRegionName, "predictorLearningRate", "100" );
        PersistenceUtil.SetConfig( imageRegionName, "receptiveFieldsTrainingSamples", "0.1" );
        PersistenceUtil.SetConfig( imageRegionName, "classifiersPerBit", "5" );
        PersistenceUtil.SetConfig( imageRegionName, "organizerStressThreshold", "0.0" );
        PersistenceUtil.SetConfig( imageRegionName, "organizerGrowthInterval", "1" );
        PersistenceUtil.SetConfig( imageRegionName, "organizerEdgeMaxAge", "1000" );
        PersistenceUtil.SetConfig( imageRegionName, "organizerNoiseMagnitude", "0.0" );
        PersistenceUtil.SetConfig( imageRegionName, "organizerLearningRate", "0.002" );
        PersistenceUtil.SetConfig( imageRegionName, "organizerLearningRateNeighbours", "0.001" );
        PersistenceUtil.SetConfig( imageRegionName, "organizerWidthCells", "8" );
        PersistenceUtil.SetConfig( imageRegionName, "organizerHeightCells", "8" );
        PersistenceUtil.SetConfig( imageRegionName, "classifierWidthCells", "5" );
        PersistenceUtil.SetConfig( imageRegionName, "classifierHeightCells", "5" );
        PersistenceUtil.SetConfig( imageRegionName, "classifierStressThreshold", "0.0" );
        PersistenceUtil.SetConfig( imageRegionName, "classifierGrowthInterval", "10" );
        PersistenceUtil.SetConfig( imageRegionName, "classifierEdgeMaxAge", "12" );

        // class region config
        PersistenceUtil.SetConfig( classRegionName, "predictorLearningRate", "100" );
        PersistenceUtil.SetConfig( classRegionName, "receptiveFieldsTrainingSamples", "0.1" );
        PersistenceUtil.SetConfig( classRegionName, "classifiersPerBit", "5" );
        PersistenceUtil.SetConfig( classRegionName, "organizerStressThreshold", "0.0" );
        PersistenceUtil.SetConfig( classRegionName, "organizerGrowthInterval", "1" );
        PersistenceUtil.SetConfig( classRegionName, "organizerEdgeMaxAge", "1000" );
        PersistenceUtil.SetConfig( classRegionName, "organizerNoiseMagnitude", "0.0" );
        PersistenceUtil.SetConfig( classRegionName, "organizerLearningRate", "0.002" );
        PersistenceUtil.SetConfig( classRegionName, "organizerLearningRateNeighbours", "0.001" );
        PersistenceUtil.SetConfig( classRegionName, "organizerWidthCells", "2" );
        PersistenceUtil.SetConfig( classRegionName, "organizerHeightCells", "2" );
//        PersistenceUtil.SetConfig( classRegionName, "classifierWidthCells", "4" );
//        PersistenceUtil.SetConfig( classRegionName, "classifierHeightCells", "4" );
        PersistenceUtil.SetConfig( classRegionName, "classifierWidthCells", "5" );
        PersistenceUtil.SetConfig( classRegionName, "classifierHeightCells", "5" );
        PersistenceUtil.SetConfig( classRegionName, "classifierStressThreshold", "0.0" );
        PersistenceUtil.SetConfig( classRegionName, "classifierGrowthInterval", "10" );
        PersistenceUtil.SetConfig( classRegionName, "classifierEdgeMaxAge", "30" );
    }

}
