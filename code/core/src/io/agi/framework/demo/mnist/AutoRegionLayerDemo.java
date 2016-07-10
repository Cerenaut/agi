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

import io.agi.core.orm.AbstractPair;
import io.agi.core.sdr.NumberEncoder;
import io.agi.core.util.images.BufferedImageSource.BufferedImageSourceFactory;
import io.agi.framework.Framework;
import io.agi.framework.Main;
import io.agi.framework.Node;
import io.agi.framework.entities.*;
import io.agi.framework.factories.CommonEntityFactory;

import java.util.ArrayList;

/**
 * Created by dave on 8/07/16.
 */
public class AutoRegionLayerDemo {

    public static void main( String[] args ) {

        // Create a Node
        Main m = new Main();
        m.setup( args[ 0 ], null, new CommonEntityFactory() );

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

        String trainingPath = "/home/dave/workspace/agi.io/data/mnist/cycle10";
        String testingPath = "/home/dave/workspace/agi.io/data/mnist/cycle10";
//        String trainingPath = "/home/dave/workspace/agi.io/data/mnist/cycle3";
//        String testingPath = "/home/dave/workspace/agi.io/data/mnist/cycle3";
//        String trainingPath = "/home/dave/workspace/agi.io/data/mnist/cycle_twin";
//        String testingPath = "/home/dave/workspace/agi.io/data/mnist/cycle_twin";
//        String trainingPath = "/home/dave/workspace/agi.io/data/mnist/cycle_deep";
//        String testingPath = "/home/dave/workspace/agi.io/data/mnist/cycle_deep";
//        String trainingPath = "/home/dave/workspace/agi.io/data/mnist/all_train";
//        String testingPath = "/home/dave/workspace/agi.io/data/mnist/all_t10k";
//        String trainingPath = "/home/dave/workspace/agi.io/data/mnist/10k_train";
//        String testingPath = "/home/dave/workspace/agi.io/data/mnist/5k_test";
//        String trainingPath = "./training";
//        String testingPath = "./testing";
//        int terminationAge = 10;//9000;
        int terminationAge = 50;//25000;
        int trainingBatches = 1;
        boolean terminateByAge = true;
        float defaultPredictionInhibition = 1.f; // random image classification only experiments
//        float defaultPredictionInhibition = 0.f; // where you use prediction
        boolean encodeZero = false;
        int layers = 3;

        // Define some entities
        String experimentName = "experiment";
        String mnistName = "mnist";
        String imageEncoderName = "image-encoder";
        String classEncoderName = "class-encoder";
        String classDecoderName = "class-decoder";
        String labelEncoderName = "label-encoder";
        String constantName = "constant";
        String region1FfName = "image-region-1-ff";
//        String region2FfName = "image-region-2-ff";
//        String region3FfName = "image-region-3-ff";
//        String classRegionName = "class-region";
        String activityImageDecoderName = "activity-image-decoder";
        String predictedImageDecoderName = "predicted-image-decoder";

//create a classifier entity based on most associated class.
/*
        Framework.CreateEntity( experimentName, ExperimentEntity.ENTITY_TYPE, n.getName(), null ); // experiment is the root entity
        Framework.CreateEntity( mnistName, MnistEntity.ENTITY_TYPE, n.getName(), experimentName );
        Framework.CreateEntity( classEncoderName, EncoderEntity.ENTITY_TYPE, n.getName(), mnistName );
        Framework.CreateEntity( labelEncoderName, EncoderEntity.ENTITY_TYPE, n.getName(), classEncoderName );
        Framework.CreateEntity( imageEncoderName, EncoderEntity.ENTITY_TYPE, n.getName(), labelEncoderName );
        Framework.CreateEntity( constantName, ConstantMatrixEntity.ENTITY_TYPE, n.getName(), imageEncoderName ); // ok all input to the regions is ready

        Framework.CreateEntity( region1FfName, RegionLayerEntity.ENTITY_TYPE, n.getName(), constantName );
        String topLayerName = region1FfName;
        if( layers > 1 ) {
            Framework.CreateEntity( region2FfName, RegionLayerEntity.ENTITY_TYPE, n.getName(), region1FfName );
            topLayerName = region2FfName;
        }
        if( layers > 2 ) {
            Framework.CreateEntity( region3FfName, RegionLayerEntity.ENTITY_TYPE, n.getName(), region2FfName );
            topLayerName = region3FfName;
        }

        Framework.CreateEntity( classRegionName, RegionLayerEntity.ENTITY_TYPE, n.getName(), topLayerName ); // 2nd, class region updates after first to get its feedback

        Framework.CreateEntity( classDecoderName, DecoderEntity.ENTITY_TYPE, n.getName(), classRegionName ); // produce the predicted classification for inspection by mnist next time
        Framework.CreateEntity( activityImageDecoderName, DecoderEntity.ENTITY_TYPE, n.getName(), classRegionName );
        Framework.CreateEntity( predictedImageDecoderName, DecoderEntity.ENTITY_TYPE, n.getName(), classRegionName );

        // Connect the entities' data
        // a) Image to image region, and decode
        Framework.SetDataReference( imageEncoderName, EncoderEntity.DATA_INPUT, mnistName, MnistEntity.OUTPUT_IMAGE );

        Framework.SetDataReference( region1FfName, RegionLayerEntity.FF_INPUT_1, imageEncoderName, EncoderEntity.DATA_OUTPUT_ENCODED );
        Framework.SetDataReference( region1FfName, RegionLayerEntity.FF_INPUT_2, constantName, ConstantMatrixEntity.OUTPUT );
        Framework.SetDataReference( region1FfName, RegionLayerEntity.FB_INPUT, constantName, ConstantMatrixEntity.OUTPUT ); // feedback to this region is just a constant

        if( layers > 1 ) {
            Framework.SetDataReference( region2FfName, RegionLayerEntity.FF_INPUT_1, region1FfName, RegionLayerEntity.PREDICTION_FN );
            Framework.SetDataReference( region2FfName, RegionLayerEntity.FF_INPUT_2, constantName, ConstantMatrixEntity.OUTPUT );
            Framework.SetDataReference( region2FfName, RegionLayerEntity.FB_INPUT, constantName, ConstantMatrixEntity.OUTPUT ); // feedback to this region is just a constant
        }

        if( layers > 2 ) {
            Framework.SetDataReference( region3FfName, RegionLayerEntity.FF_INPUT_1, region2FfName, RegionLayerEntity.PREDICTION_FN );
            Framework.SetDataReference( region3FfName, RegionLayerEntity.FF_INPUT_2, constantName, ConstantMatrixEntity.OUTPUT );
// Option to provide class-label directly to layer 3 for inclusion in classification
//            Framework.SetDataReference( region3FfName, RegionLayerEntity.FF_INPUT_2, labelEncoderName, EncoderEntity.DATA_OUTPUT_ENCODED );
            Framework.SetDataReference( region3FfName, RegionLayerEntity.FB_INPUT, constantName, ConstantMatrixEntity.OUTPUT ); // feedback to this region is just a constant
        }

        Framework.SetDataReference(  activityImageDecoderName, DecoderEntity.DATA_INPUT_ENCODED, region1FfName, RegionLayerEntity.FB_OUTPUT_1_UNFOLDED_ACTIVITY );
        Framework.SetDataReference( predictedImageDecoderName, DecoderEntity.DATA_INPUT_ENCODED, region1FfName, RegionLayerEntity.FB_OUTPUT_1_UNFOLDED_PREDICTION );

        // a) Class to class region, and decode
        Framework.SetDataReference( labelEncoderName, EncoderEntity.DATA_INPUT, mnistName, MnistEntity.OUTPUT_IMAGE_LABEL );
        Framework.SetDataReference( classEncoderName, EncoderEntity.DATA_INPUT, mnistName, MnistEntity.OUTPUT_CLASSIFICATION );
        Framework.SetDataReference( classRegionName, RegionLayerEntity.FF_INPUT_1, classEncoderName, EncoderEntity.DATA_OUTPUT_ENCODED );
        Framework.SetDataReference( classRegionName, RegionLayerEntity.FF_INPUT_2, constantName, ConstantMatrixEntity.OUTPUT );

        ArrayList< AbstractPair< String, String > > referenceEntitySuffixes = new ArrayList< AbstractPair< String, String > >();
        referenceEntitySuffixes.add( new AbstractPair< String, String >( region1FfName, RegionLayerEntity.PREDICTION_FN ) );
//        if( layers > 1 ) referenceEntitySuffixes.add( new AbstractPair< String, String >( region2FfName, RegionLayerEntity.PREDICTION_FN ) );
        if( layers > 2 ) referenceEntitySuffixes.add( new AbstractPair< String, String >( region3FfName, RegionLayerEntity.PREDICTION_FN ) );

        Framework.SetDataReferences( classRegionName, RegionLayerEntity.FB_INPUT, referenceEntitySuffixes ); // get current state from the region to be used to predict

        Framework.SetDataReference( classDecoderName, DecoderEntity.DATA_INPUT_ENCODED, classRegionName, RegionLayerEntity.FB_OUTPUT_1_UNFOLDED_PREDICTION ); // the prediction of the next state
        Framework.SetDataReference( mnistName, MnistEntity.INPUT_CLASSIFICATION, classDecoderName, DecoderEntity.DATA_OUTPUT_DECODED ); // the (decoded) prediction of the next state


        // Experiment config
        if( !terminateByAge ) {
            Framework.SetConfig( experimentName, "terminationEntityName", mnistName );
            Framework.SetConfig( experimentName, "terminationConfigPath", "terminate" );
            Framework.SetConfig( experimentName, "terminationAge", "-1" ); // wait for mnist to decide
        }
        else {
            Framework.SetConfig( experimentName, "terminationAge", String.valueOf( terminationAge ) ); // fixed steps
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
        if( encodeZero ) {

            // image encoder config
            Framework.SetConfig( imageEncoderName, "density", "1" );
            Framework.SetConfig( imageEncoderName, "bits", "2" );
            Framework.SetConfig( imageEncoderName, "encodeZero", "true" );

            // image decoder config x2
            Framework.SetConfig( activityImageDecoderName, "density", "1" );
            Framework.SetConfig( activityImageDecoderName, "bits", "2" );
            Framework.SetConfig( activityImageDecoderName, "encodeZero", "true" );

            Framework.SetConfig( predictedImageDecoderName, "density", "1" );
            Framework.SetConfig( predictedImageDecoderName, "bits", "2" );
            Framework.SetConfig( predictedImageDecoderName, "encodeZero", "true" );
        }
        else {

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
        }

        // class encoder config
        Framework.SetConfig( labelEncoderName, "encoderType", NumberEncoder.class.getSimpleName() );
        Framework.SetConfig( labelEncoderName, "digits", "2" );
        Framework.SetConfig( labelEncoderName, "numbers", "1" );

        // class encoder config
        Framework.SetConfig( classEncoderName, "encoderType", NumberEncoder.class.getSimpleName() );
        Framework.SetConfig( classEncoderName, "digits", "2" );
        Framework.SetConfig( classEncoderName, "numbers", "1" );

        // class decoder config
        Framework.SetConfig( classDecoderName, "encoderType", NumberEncoder.class.getSimpleName() );
        Framework.SetConfig( classDecoderName, "digits", "2" );
        Framework.SetConfig( classDecoderName, "numbers", "1" );

        // image region config

        setPlasticRegionLayerConfig(
                region1FfName, defaultPredictionInhibition, emitUnchangedCells, organizerWidth, organizerHeight,
                classifiersPerBit1, classifiersPerBit2, classifierWidth, classifierHeight, classifierDepth,
                classifierStressLearningRate, classifierRankLearningRate, classifierRankScale,
                classifierAgeMax, classifierAgeDecay, classifierAgeScale, predictorLearningRate );

        if( layers > 1 )
            setPlasticRegionLayerConfig(
                    region2FfName, defaultPredictionInhibition, emitUnchangedCells, organizerWidth, organizerHeight,
                    classifiersPerBit1, classifiersPerBit2, classifierWidth, classifierHeight, classifierDepth,
                    classifierStressLearningRate, classifierRankLearningRate, classifierRankScale,
                    classifierAgeMax, classifierAgeDecay, classifierAgeScale, predictorLearningRate );


        if( layers > 2 )
            setPlasticRegionLayerConfig(
                    region3FfName, defaultPredictionInhibition, emitUnchangedCells, organizerWidth, organizerHeight,
                    classifiersPerBit1, classifiersPerBit2, classifierWidth,classifierHeight, classifierDepth,
                    classifierStressLearningRate, classifierRankLearningRate, classifierRankScale,
                    classifierAgeMax, classifierAgeDecay, classifierAgeScale, predictorLearningRate );
*/
    }

    public static void setRegionLayerConfig( String regionLayerName, float defaultPredictionInhibition, boolean emitUnchangedCells, int classifiersPerBit1, int classifiersPerBit2, int classifierWidth, int classifierHeight, int organizerWidth, int organizerHeight, int edgeMaxAge ) {
    }

}
