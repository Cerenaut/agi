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

import io.agi.core.sdr.IntegerEncoder;
import io.agi.core.util.PropertiesUtil;
import io.agi.core.util.images.BufferedImageSource.BufferedImageSourceFactory;
import io.agi.framework.Framework;
import io.agi.framework.Main;
import io.agi.framework.Node;
import io.agi.framework.entities.*;
import io.agi.framework.factories.CommonEntityFactory;

import java.util.Properties;

/**
 * Created by dave on 8/07/16.
 */
public class AutoRegionLayerTextDemo {

    //       FB0 <-- FB1
    //                ^
    // Input --> FF1 --> FF2
    //

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

        // 3 layer ladder hierarchy
        // FF-1 ---> FF-2 ---> FF-3
        //   |        |         |
        //   v        v         v
        // FB-1 <--- FB-2 <--- FB-3

        // TODO if real valued input, then consider probability or real valued image input.
//        String trainingTextPath = "/home/dave/workspace/agi.io/data/text/abc.txt"; // 065 066 067 010
//        String testingTextPath = "/home/dave/workspace/agi.io/data/text/abc.txt"; // 065 066 067 010
//        String trainingTextPath = "/home/dave/workspace/agi.io/data/text/quick.txt";
//        String testingTextPath = "/home/dave/workspace/agi.io/data/text/quick.txt";
//        String trainingTextPath = "/home/dave/workspace/agi.io/data/text/qui.txt";
//        String testingTextPath = "/home/dave/workspace/agi.io/data/text/qui.txt";
        String trainingTextPath = "/home/dave/workspace/agi.io/data/text/the.txt";
        String testingTextPath = "/home/dave/workspace/agi.io/data/text/the.txt";

        String trainingPath = "/home/dave/workspace/agi.io/data/mnist/cycle10"; // verify it can model this with image certainty
        String testingPath = "/home/dave/workspace/agi.io/data/mnist/cycle10";
//        String trainingPath = "/home/dave/workspace/agi.io/data/mnist/10k_train";
//        String testingPath = "/home/dave/workspace/agi.io/data/mnist/5k_test";
//        String trainingPath = "./training";
//        String testingPath = "./testing";
//        int terminationAge = 10;
        int textLength = 45;
        int terminationAge = 5000; //6000;//25000;
        int trainingBatches = ( terminationAge / ( textLength * 2 ) ) +1; // approximate hack to get right number of iters from a given piece of text
        boolean terminateByAge = true;
        boolean encodeZero = false;
        boolean doFb = true;
        int labelBits = 8;

        // Define some entities
        String experimentName           = Framework.GetEntityName( "experiment" );
        String imageSourceName          = Framework.GetEntityName( "image-source" );
        String constantName             = Framework.GetEntityName( "constant" );
        String region1FfName            = Framework.GetEntityName( "image-region-1-ff" );
        String region2FfName            = Framework.GetEntityName( "image-region-2-ff" );
        String region3FfName            = Framework.GetEntityName( "image-region-3-ff" );
        String region4FfName            = Framework.GetEntityName( "image-region-4-ff" );

        String region1FbName            = Framework.GetEntityName( "image-region-1-fb" );
        String region2FbName            = Framework.GetEntityName( "image-region-2-fb" );
        String region3FbName            = Framework.GetEntityName( "image-region-3-fb" );
        String region4FbName            = Framework.GetEntityName( "image-region-4-fb" );

        String imageEncoderName         = Framework.GetEntityName( "image-encoder" );
//        String labelEncoderName         = Framework.GetEntityName( "label-encoder" );
//        String labelDecoderName         = Framework.GetEntityName( "label-decoder" );
//        String classResultName          = Framework.GetEntityName( "class-result" );
//        String valueSeriesPredictedName = Framework.GetEntityName( "value-series-predicted" );
//        String valueSeriesErrorName     = Framework.GetEntityName( "value-series-error" );
        String valueSeriesTruthName     = Framework.GetEntityName( "value-series-truth" );
//        String configProductName        = Framework.GetEntityName( "config-product" );
        String valueSeriesDigitName = Framework.GetEntityName( "value-series-digit" );

//        String binaryErrorFf1Name = Framework.GetEntityName( "binary-error-ff1" );
//        String binaryErrorFb1Name = Framework.GetEntityName( "binary-error-fb1" );

        String valueSeriesFf1FnName = Framework.GetEntityName( "value-series-ff1-fn" );
        String valueSeriesFf2FnName = Framework.GetEntityName( "value-series-ff2-fn" );
        String valueSeriesFf3FnName = Framework.GetEntityName( "value-series-ff3-fn" );
        String valueSeriesFf4FnName = Framework.GetEntityName( "value-series-ff4-fn" );

        String valueSeriesFb1FnName = Framework.GetEntityName( "value-series-fb1-fn" );
        String valueSeriesFb2FnName = Framework.GetEntityName( "value-series-fb2-fn" );

        String valueSeriesFf1Err1Name = Framework.GetEntityName( "value-series-ff1-err1" );
        String valueSeriesFb1Err1Name = Framework.GetEntityName( "value-series-fb1-err1" );
        String valueSeriesFb1Err2Name = Framework.GetEntityName( "value-series-fb1-err2" );

        Framework.CreateEntity( experimentName, ExperimentEntity.ENTITY_TYPE, n.getName(), null ); // experiment is the root entity
        Framework.CreateEntity( imageSourceName, Text2ImageLabelEntity.ENTITY_TYPE, n.getName(), experimentName );
        Framework.CreateEntity( imageEncoderName, EncoderEntity.ENTITY_TYPE, n.getName(), imageSourceName );
        Framework.CreateEntity( constantName, ConstantMatrixEntity.ENTITY_TYPE, n.getName(), imageEncoderName ); // ok all input to the regions is ready
//        Framework.CreateEntity( labelEncoderName, EncoderEntity.ENTITY_TYPE, n.getName(), constantName );
//        Framework.CreateEntity( configProductName, ConfigProductEntity.ENTITY_TYPE, n.getName(), labelEncoderName ); // ok all input to the regions is ready

        String lastRegionLayer = constantName;
        Framework.CreateEntity( region1FfName, AutoRegionLayerEntity.ENTITY_TYPE, n.getName(), lastRegionLayer );
        String learningEntitiesAlgorithm = region1FfName;
        lastRegionLayer = region1FfName;

        Framework.CreateEntity( region2FfName, AutoRegionLayerEntity.ENTITY_TYPE, n.getName(), lastRegionLayer );
        learningEntitiesAlgorithm = learningEntitiesAlgorithm + "," + region2FfName;
        lastRegionLayer = region2FfName;

        Framework.CreateEntity( region3FfName, AutoRegionLayerEntity.ENTITY_TYPE, n.getName(), lastRegionLayer );
        learningEntitiesAlgorithm = learningEntitiesAlgorithm + "," + region3FfName;
        lastRegionLayer = region3FfName;

        Framework.CreateEntity( region4FfName, AutoRegionLayerEntity.ENTITY_TYPE, n.getName(), lastRegionLayer );
        learningEntitiesAlgorithm = learningEntitiesAlgorithm + "," + region4FfName;
        lastRegionLayer = region4FfName;

        if( doFb ) {
            Framework.CreateEntity( region4FbName, AutoRegionLayerEntity.ENTITY_TYPE, n.getName(), lastRegionLayer );
            learningEntitiesAlgorithm = learningEntitiesAlgorithm + "," + region4FbName;
            lastRegionLayer = region4FbName;

            Framework.CreateEntity( region3FbName, AutoRegionLayerEntity.ENTITY_TYPE, n.getName(), lastRegionLayer );
            learningEntitiesAlgorithm = learningEntitiesAlgorithm + "," + region3FbName;
            lastRegionLayer = region3FbName;

            Framework.CreateEntity( region2FbName, AutoRegionLayerEntity.ENTITY_TYPE, n.getName(), lastRegionLayer );
            learningEntitiesAlgorithm = learningEntitiesAlgorithm + "," + region2FbName;
            lastRegionLayer = region2FbName;

            Framework.CreateEntity( region1FbName, AutoRegionLayerEntity.ENTITY_TYPE, n.getName(), lastRegionLayer );
            learningEntitiesAlgorithm = learningEntitiesAlgorithm + "," + region1FbName;
            lastRegionLayer = region1FbName;
        }

//        Framework.CreateEntity( labelDecoderName, DecoderEntity.ENTITY_TYPE, n.getName(), topLayerName ); // produce the predicted classification for inspection by mnist next time
//        Framework.CreateEntity( classResultName, ClassificationResultEntity.ENTITY_TYPE, n.getName(), labelDecoderName ); // produce the predicted classification for inspection by mnist next time
//
//        Framework.CreateEntity( valueSeriesPredictedName, ValueSeriesEntity.ENTITY_TYPE, n.getName(), classResultName ); // 2nd, class region updates after first to get its feedback
//        Framework.CreateEntity( valueSeriesErrorName, ValueSeriesEntity.ENTITY_TYPE, n.getName(), classResultName ); // 2nd, class region updates after first to get its feedback
        Framework.CreateEntity( valueSeriesTruthName, ValueSeriesEntity.ENTITY_TYPE, n.getName(), lastRegionLayer ); // 2nd, class region updates after first to get its feedback
        Framework.CreateEntity( valueSeriesDigitName, ValueSeriesEntity.ENTITY_TYPE, n.getName(), lastRegionLayer ); // 2nd, class region updates after first to get its feedback

//        Framework.CreateEntity( binaryErrorFf1Name, BinaryErrorEntity.ENTITY_TYPE, n.getName(), valueSeriesDigitName ); // experiment is the root entity
//        Framework.CreateEntity( binaryErrorFb1Name, BinaryErrorEntity.ENTITY_TYPE, n.getName(), binaryErrorFf1Name ); // experiment is the root entity

        Framework.CreateEntity( valueSeriesFf1FnName, ValueSeriesEntity.ENTITY_TYPE, n.getName(), lastRegionLayer ); // 2nd, class region updates after first to get its feedback
        Framework.CreateEntity( valueSeriesFf2FnName, ValueSeriesEntity.ENTITY_TYPE, n.getName(), lastRegionLayer ); // 2nd, class region updates after first to get its feedback
        Framework.CreateEntity( valueSeriesFf3FnName, ValueSeriesEntity.ENTITY_TYPE, n.getName(), lastRegionLayer ); // 2nd, class region updates after first to get its feedback
        Framework.CreateEntity( valueSeriesFf4FnName, ValueSeriesEntity.ENTITY_TYPE, n.getName(), lastRegionLayer ); // 2nd, class region updates after first to get its feedback

        if( doFb ) {
            Framework.CreateEntity( valueSeriesFb1FnName, ValueSeriesEntity.ENTITY_TYPE, n.getName(), lastRegionLayer ); // 2nd, class region updates after first to get its feedback
            Framework.CreateEntity( valueSeriesFb2FnName, ValueSeriesEntity.ENTITY_TYPE, n.getName(), lastRegionLayer ); // 2nd, class region updates after first to get its feedback

            Framework.CreateEntity( valueSeriesFf1Err1Name, ValueSeriesEntity.ENTITY_TYPE, n.getName(), lastRegionLayer ); // 2nd, class region updates after first to get its feedback
            Framework.CreateEntity( valueSeriesFb1Err1Name, ValueSeriesEntity.ENTITY_TYPE, n.getName(), lastRegionLayer ); // 2nd, class region updates after first to get its feedback
            Framework.CreateEntity( valueSeriesFb1Err2Name, ValueSeriesEntity.ENTITY_TYPE, n.getName(), lastRegionLayer ); // 2nd, class region updates after first to get its feedback
        }

        // Connect the entities' data
        // a) Image to image region, and decode
        Framework.SetDataReference( imageEncoderName, EncoderEntity.DATA_INPUT, imageSourceName, ImageClassEntity.OUTPUT_IMAGE );
//        Framework.SetDataReference( labelEncoderName, EncoderEntity.DATA_INPUT, imageClassName, ImageClassEntity.OUTPUT_LABEL );
//        Framework.SetDataReference( labelDecoderName, DecoderEntity.DATA_INPUT_DECODED, imageClassName, ImageClassEntity.OUTPUT_LABEL );

//                Framework.SetDataReference( region2FfName, AutoRegionLayerEntity.INPUT_2, labelEncoderName, EncoderEntity.DATA_OUTPUT_ENCODED );
//        Framework.SetDataReference( configProductName, ConfigProductEntity.INPUT, labelEncoderName, EncoderEntity.DATA_OUTPUT_ENCODED );

        // FF
        Framework.SetDataReference( region1FfName, AutoRegionLayerEntity.INPUT_1, imageEncoderName, EncoderEntity.DATA_OUTPUT_ENCODED );
        Framework.SetDataReference( region1FfName, AutoRegionLayerEntity.INPUT_2, constantName, ConstantMatrixEntity.OUTPUT );
        Framework.SetDataReference( region1FfName, AutoRegionLayerEntity.INPUT_3, constantName, ConstantMatrixEntity.OUTPUT );

        Framework.SetDataReference( region2FfName, AutoRegionLayerEntity.INPUT_1, region1FfName, AutoRegionLayerEntity.OUTPUT );
        Framework.SetDataReference( region2FfName, AutoRegionLayerEntity.INPUT_2, constantName, ConstantMatrixEntity.OUTPUT );
        Framework.SetDataReference( region2FfName, AutoRegionLayerEntity.INPUT_3, constantName, ConstantMatrixEntity.OUTPUT );

        Framework.SetDataReference( region3FfName, AutoRegionLayerEntity.INPUT_1, region2FfName, AutoRegionLayerEntity.OUTPUT );
        Framework.SetDataReference( region3FfName, AutoRegionLayerEntity.INPUT_2, constantName, ConstantMatrixEntity.OUTPUT );
        Framework.SetDataReference( region3FfName, AutoRegionLayerEntity.INPUT_3, constantName, ConstantMatrixEntity.OUTPUT );

        Framework.SetDataReference( region4FfName, AutoRegionLayerEntity.INPUT_1, region3FfName, AutoRegionLayerEntity.OUTPUT );
        Framework.SetDataReference( region4FfName, AutoRegionLayerEntity.INPUT_2, constantName, ConstantMatrixEntity.OUTPUT );
        Framework.SetDataReference( region4FfName, AutoRegionLayerEntity.INPUT_3, constantName, ConstantMatrixEntity.OUTPUT );

        // FB
        // NOTE: I'm taking the current rather than pooled output
        if( doFb ) {
            Framework.SetDataReference( region4FbName, AutoRegionLayerEntity.INPUT_1, region3FfName, AutoRegionLayerEntity.OUTPUT );
            Framework.SetDataReference( region4FbName, AutoRegionLayerEntity.INPUT_2, region4FfName, AutoRegionLayerEntity.OUTPUT );
            Framework.SetDataReference( region4FbName, AutoRegionLayerEntity.INPUT_3, constantName, ConstantMatrixEntity.OUTPUT );

            Framework.SetDataReference( region3FbName, AutoRegionLayerEntity.INPUT_1, region2FfName, AutoRegionLayerEntity.OUTPUT );
            Framework.SetDataReference( region3FbName, AutoRegionLayerEntity.INPUT_2, region3FfName, AutoRegionLayerEntity.OUTPUT );
            Framework.SetDataReference( region3FbName, AutoRegionLayerEntity.INPUT_3, region4FbName, AutoRegionLayerEntity.OUTPUT );

            Framework.SetDataReference( region2FbName, AutoRegionLayerEntity.INPUT_1, region1FfName, AutoRegionLayerEntity.OUTPUT );
            Framework.SetDataReference( region2FbName, AutoRegionLayerEntity.INPUT_2, region2FfName, AutoRegionLayerEntity.OUTPUT );
            Framework.SetDataReference( region2FbName, AutoRegionLayerEntity.INPUT_3, region3FbName, AutoRegionLayerEntity.OUTPUT );

            Framework.SetDataReference( region1FbName, AutoRegionLayerEntity.INPUT_1, imageEncoderName, EncoderEntity.DATA_OUTPUT_ENCODED );
            Framework.SetDataReference( region1FbName, AutoRegionLayerEntity.INPUT_2, region1FfName, AutoRegionLayerEntity.OUTPUT );
            Framework.SetDataReference( region1FbName, AutoRegionLayerEntity.INPUT_3, region2FbName, AutoRegionLayerEntity.OUTPUT );
        }

        // measuring prediction error rates
//        Framework.SetDataReference( binaryErrorFf1Name, BinaryErrorEntity.INPUT_TEST , region1FfName, AutoRegionLayerEntity.PREDICTION_OLD );
//        Framework.SetDataReference( binaryErrorFf1Name, BinaryErrorEntity.INPUT_TRUTH, region1FfName, AutoRegionLayerEntity.CONTEXT_FREE_ACTIVITY_NEW );
//
//        Framework.SetDataReference( binaryErrorFb1Name, BinaryErrorEntity.INPUT_TEST , region1FbName, AutoRegionLayerEntity.PREDICTION_OLD );
//        Framework.SetDataReference( binaryErrorFb1Name, BinaryErrorEntity.INPUT_TRUTH, region1FbName, AutoRegionLayerEntity.CONTEXT_FREE_ACTIVITY_NEW );

//        // invert the hidden layer state to produce the predicted label
//        Framework.SetDataReference( labelDecoderName, DecoderEntity.DATA_INPUT_ENCODED, topLayerName, AutoRegionLayerEntity.OUTPUT_INPUT_2 ); // the prediction of the next state
//
//        Framework.SetDataReference( classResultName, ClassificationResultEntity.INPUT_LABEL, imageClassName, ImageClassEntity.OUTPUT_LABEL ); // get current state from the region to be used to predict
//        Framework.SetDataReference( classResultName, ClassificationResultEntity.INPUT_CLASS, labelDecoderName, DecoderEntity.DATA_OUTPUT_DECODED ); // get current state from the region to be used to predict
//
//        // Label filter
//        Framework.SetConfig( configProductName, "entityName", region1FfName );
//        Framework.SetConfig( configProductName, "configPath", "learn" ); // so the value is '1' when the region is learning, and 0 otherwise.

        // Experiment config
        if( !terminateByAge ) {
            Framework.SetConfig( experimentName, "terminationEntityName", imageSourceName );
            Framework.SetConfig( experimentName, "terminationConfigPath", "terminate" );
            Framework.SetConfig( experimentName, "terminationAge", "-1" ); // wait for mnist to decide
        }
        else {
            Framework.SetConfig( experimentName, "terminationAge", String.valueOf( terminationAge ) ); // fixed steps
        }

        // Image source config
        Framework.SetConfig( imageSourceName, "receptiveField.receptiveFieldX", "0" );
        Framework.SetConfig( imageSourceName, "receptiveField.receptiveFieldY", "0" );
        Framework.SetConfig( imageSourceName, "receptiveField.receptiveFieldW", "28" );
        Framework.SetConfig( imageSourceName, "receptiveField.receptiveFieldH", "28" );
        Framework.SetConfig( imageSourceName, "resolution.resolutionX", "28" );
        Framework.SetConfig( imageSourceName, "resolution.resolutionY", "28" );
        Framework.SetConfig( imageSourceName, "greyscale", "true" );
        Framework.SetConfig( imageSourceName, "invert", "true" );
        Framework.SetConfig( imageSourceName, "sourceType", BufferedImageSourceFactory.TYPE_IMAGE_FILES );
        Framework.SetConfig( imageSourceName, "sourceFilesPrefix", "postproc" );
        Framework.SetConfig( imageSourceName, "sourceFilesPathTraining", trainingPath );
        Framework.SetConfig( imageSourceName, "sourceFilesPathTesting", testingPath );
        Framework.SetConfig( imageSourceName, "trainingBatches", String.valueOf( trainingBatches ) );
        Framework.SetConfig( imageSourceName, "sourceTextFileTraining", trainingTextPath );
        Framework.SetConfig( imageSourceName, "sourceTextFileTesting", testingTextPath );

        String learningEntitiesAnalytics = "";//classFeaturesName;
        Framework.SetConfig( imageSourceName, "learningEntitiesAlgorithm", String.valueOf( learningEntitiesAlgorithm ) );
        Framework.SetConfig( imageSourceName, "learningEntitiesAnalytics", String.valueOf( learningEntitiesAnalytics ) );

        // constant config
        if( encodeZero ) {
            // image encoder config
            Framework.SetConfig( imageEncoderName, "density", "1" );
            Framework.SetConfig( imageEncoderName, "bits", "2" );
            Framework.SetConfig( imageEncoderName, "encodeZero", "true" );
        }
        else {
            // image encoder config
            Framework.SetConfig( imageEncoderName, "density", "1" );
            Framework.SetConfig( imageEncoderName, "bits", "1" );
            Framework.SetConfig( imageEncoderName, "encodeZero", "false" );
        }

//        // Label encoder
//        Framework.SetConfig( labelEncoderName, "encoderType", IntegerEncoder.class.getSimpleName() );
//        Framework.SetConfig( labelEncoderName, "minValue", "0" );
//        Framework.SetConfig( labelEncoderName, "maxValue", "9" );
//        Framework.SetConfig( labelEncoderName, "rows", String.valueOf( labelBits ) );
//
//        // Label DEcoder
//        Framework.SetConfig( labelDecoderName, "encoderType", IntegerEncoder.class.getSimpleName() );
//        Framework.SetConfig( labelDecoderName, "minValue", "0" );
//        Framework.SetConfig( labelDecoderName, "maxValue", "9" );
//        Framework.SetConfig( labelDecoderName, "rows", String.valueOf( labelBits ) );

        // image region config
        int ageMin = 0;
        int ageMax = 0;//700;//1000;
        float ageScale = 0f;//17f;//15f; // maybe try 12 or 17
        float rateScale = 0f;
        float rateLearningRate = 0f;
        float sparseLearningRate = 0f;
        int widthCells = 0; // from the paper, 32x32=1024 was optimal on MNIST (but with a supervised output layer)
        int heightCells = 0;
        float sparsityMin = 0f;
        float sparsityOutput = 0f; // the extra cells active for classification but not training
        float sparsitySlow = 0f; // the temporal slowness pooling factor
        float predictorLearningRate = 0f;
        float defaultPredictionInhibition = 0.f; // sequence experiments

        // effective constants:
        defaultPredictionInhibition = 0.f; // if 1, then won't use PC.
        ageMin = 0;
        ageMax = 750;
        ageScale = 12f;
        rateScale = 5f;
        rateLearningRate = 0.01f;
        sparseLearningRate = 0.01f;
        predictorLearningRate = 0.01f;
        sparsityOutput = 1.5f; // temporal pooling, none if 1. Will pool down to double size.
        sparsitySlow = 2.0f; // temporal pooling, none if 1. Will pool down to double size.
//        sparsitySlow = 1.0f;// worked surprisingly well - rediced to worst case 5-6 bits error in FF-2
//        sparsitySlow = 0.5f; // exponential learning rate of nonbinary temporal slowness

        // per region
        // FF
        widthCells = 24;//32;
        heightCells = 24;//32;
        sparsityMin = 16;//24f;

        setRegionLayerConfig(
                region1FfName,
                widthCells, heightCells,
                ageMin, ageMax, ageScale,
                sparseLearningRate, sparsityMin, sparsityOutput, sparsitySlow,
                defaultPredictionInhibition, predictorLearningRate,
                rateScale, rateLearningRate );

        sparsityMin = 16;

        setRegionLayerConfig(
                region2FfName,
                widthCells, heightCells,
                ageMin, ageMax, ageScale,
                sparseLearningRate, sparsityMin, sparsityOutput, sparsitySlow,
                defaultPredictionInhibition, predictorLearningRate,
                rateScale, rateLearningRate );

        sparsityMin = 16;//8;

        setRegionLayerConfig(
                region3FfName,
                widthCells, heightCells,
                ageMin, ageMax, ageScale,
                sparseLearningRate, sparsityMin, sparsityOutput, sparsitySlow,
                defaultPredictionInhibition, predictorLearningRate,
                rateScale, rateLearningRate );

        setRegionLayerConfig(
                region4FfName,
                widthCells, heightCells,
                ageMin, ageMax, ageScale,
                sparseLearningRate, sparsityMin, sparsityOutput, sparsitySlow,
                defaultPredictionInhibition, predictorLearningRate,
                rateScale, rateLearningRate );

        // FB
        widthCells = 24;//32;
        heightCells = 24;//32;
        sparsityMin = 16;//8;

        setRegionLayerConfig(
                region4FbName,
                widthCells, heightCells,
                ageMin, ageMax, ageScale,
                sparseLearningRate, sparsityMin, sparsityOutput, sparsitySlow,
                defaultPredictionInhibition, predictorLearningRate,
                rateScale, rateLearningRate );

        setRegionLayerConfig(
                region3FbName,
                widthCells, heightCells,
                ageMin, ageMax, ageScale,
                sparseLearningRate, sparsityMin, sparsityOutput, sparsitySlow,
                defaultPredictionInhibition, predictorLearningRate,
                rateScale, rateLearningRate );

        setRegionLayerConfig(
                region2FbName,
                widthCells, heightCells,
                ageMin, ageMax, ageScale,
                sparseLearningRate, sparsityMin, sparsityOutput, sparsitySlow,
                defaultPredictionInhibition, predictorLearningRate,
                rateScale, rateLearningRate );

        setRegionLayerConfig(
                region1FbName,
                widthCells, heightCells,
                ageMin, ageMax, ageScale,
                sparseLearningRate, sparsityMin, sparsityOutput, sparsitySlow,
                defaultPredictionInhibition, predictorLearningRate,
                rateScale, rateLearningRate );

        // feature-class config
//        Framework.SetConfig( classFeaturesName, "classEntityName", imageClassName );
//        Framework.SetConfig( classFeaturesName, "classConfigPath", "imageClass" );
//        Framework.SetConfig( classFeaturesName, "classes", "10" );
//        Framework.SetConfig( classFeaturesName, "onlineLearning", String.valueOf( classFeaturesOnline ) );
////        Framework.SetConfig( classFeaturesName, "onlineLearningRate", "0.001" );
//        Framework.SetConfig( classFeaturesName, "onlineLearningRate", "0.01" );

//        // data series logging
//        Framework.SetConfig( valueSeriesPredictedName, "period", "-1" ); // log forever
//        Framework.SetConfig( valueSeriesErrorName, "period", "-1" );
//
//        Framework.SetConfig( valueSeriesPredictedName, "entityName", classResultName ); // log forever
//        Framework.SetConfig( valueSeriesErrorName, "entityName", classResultName );
        Framework.SetConfig( valueSeriesTruthName, "period", "-1" );
        Framework.SetConfig( valueSeriesTruthName, "entityName", imageSourceName );
        Framework.SetConfig( valueSeriesTruthName, "configPath", "imageClass" );
//
//        Framework.SetConfig( valueSeriesPredictedName, "configPath", "classPredicted" ); // log forever
//        Framework.SetConfig( valueSeriesErrorName, "configPath", "classError" );
        Framework.SetConfig( valueSeriesDigitName, "period", "-1" );
        Framework.SetConfig( valueSeriesDigitName, "entityName", imageSourceName );
        Framework.SetConfig( valueSeriesDigitName, "configPath", "characterCode" );

        // error rates
        Framework.SetConfig( valueSeriesFf1FnName, "period", "-1" );
        Framework.SetConfig( valueSeriesFf1FnName, "entityName", region1FfName );
        Framework.SetConfig( valueSeriesFf1FnName, "configPath", "sumFalseNegativeErrors" );

        Framework.SetConfig( valueSeriesFb1FnName, "period", "-1" );
        Framework.SetConfig( valueSeriesFb1FnName, "entityName", region1FbName );
        Framework.SetConfig( valueSeriesFb1FnName, "configPath", "sumFalseNegativeErrors" );

        // prediction error stats
        Framework.SetConfig( valueSeriesFf1Err1Name, "period", "-1" );
        Framework.SetConfig( valueSeriesFf1Err1Name, "entityName", region1FfName );
        Framework.SetConfig( valueSeriesFf1Err1Name, "configPath", "sumAbsPredictionErrorInput1" );

        Framework.SetConfig( valueSeriesFb1Err1Name, "period", "-1" );
        Framework.SetConfig( valueSeriesFb1Err1Name, "entityName", region1FbName );
        Framework.SetConfig( valueSeriesFb1Err1Name, "configPath", "sumAbsPredictionErrorInput1" );

        Framework.SetConfig( valueSeriesFb1Err2Name, "period", "-1" );
        Framework.SetConfig( valueSeriesFb1Err2Name, "entityName", region1FbName );
        Framework.SetConfig( valueSeriesFb1Err2Name, "configPath", "sumAbsPredictionErrorInput1" );

//todo- change pred so it only learns a FP error when ceases to be predicted?
//and learns a FN error when active but not immediately prior?
//synaptic errors:
//a->b
//if a goes from 1->0 or a -> 0-1>
// P bit      A bit
// 0-0
// 0-1
// 0-0
// 0-0



        // level 2
        Framework.SetConfig( valueSeriesFf2FnName, "period", "-1" );
        Framework.SetConfig( valueSeriesFf2FnName, "entityName", region2FfName );
        Framework.SetConfig( valueSeriesFf2FnName, "configPath", "sumFalseNegativeErrors" );

        Framework.SetConfig( valueSeriesFb2FnName, "period", "-1" );
        Framework.SetConfig( valueSeriesFb2FnName, "entityName", region2FbName );
        Framework.SetConfig( valueSeriesFb2FnName, "configPath", "sumFalseNegativeErrors" );

        // 3
        Framework.SetConfig( valueSeriesFf3FnName, "period", "-1" );
        Framework.SetConfig( valueSeriesFf3FnName, "entityName", region3FfName );
        Framework.SetConfig( valueSeriesFf3FnName, "configPath", "sumFalseNegativeErrors" );

        // 4
        Framework.SetConfig( valueSeriesFf4FnName, "period", "-1" );
        Framework.SetConfig( valueSeriesFf4FnName, "entityName", region4FfName );
        Framework.SetConfig( valueSeriesFf4FnName, "configPath", "sumFalseNegativeErrors" );
    }

    public static void setRegionLayerConfig(
            String regionLayerName,
            int widthCells,
            int heightCells,
            int ageMin,
            int ageMax,
            float ageScale,
            float sparseLearningRate,
            float sparsityMin,
//            float sparsityMax,
//            float sparsityCells,
            float sparsityOutput,
            float sparsitySlow,
            float defaultPredictionInhibition,
            float predictorLearningRate,
            float rateScale,
            float rateLearningRate ) {

        float sparsityMax = 0f; // not used

        Framework.SetConfig( regionLayerName, "contextFreeLearningRate", String.valueOf( sparseLearningRate ) );
        Framework.SetConfig( regionLayerName, "contextFreeWidthCells", String.valueOf( widthCells ) );
        Framework.SetConfig( regionLayerName, "contextFreeHeightCells", String.valueOf( heightCells ) );
        Framework.SetConfig( regionLayerName, "contextFreeBinaryOutput", String.valueOf( true ) );
        Framework.SetConfig( regionLayerName, "contextFreeSparsity", String.valueOf( 0 ) );
        Framework.SetConfig( regionLayerName, "contextFreeSparsityOutput", String.valueOf( sparsityOutput ) );
        Framework.SetConfig( regionLayerName, "contextFreeSparsityMin", String.valueOf( sparsityMin ) );
        Framework.SetConfig( regionLayerName, "contextFreeSparsityMax", String.valueOf( sparsityMax ) );
        Framework.SetConfig( regionLayerName, "contextFreeAgeMin", String.valueOf( ageMin ) );
        Framework.SetConfig( regionLayerName, "contextFreeAgeMax", String.valueOf( ageMax ) );
        Framework.SetConfig( regionLayerName, "contextFreeAge", String.valueOf( 0 ) );
        Framework.SetConfig( regionLayerName, "contextFreeAgeScale", String.valueOf( ageScale ) );

        Framework.SetConfig( regionLayerName, "rateScale", String.valueOf( rateScale ) );
        Framework.SetConfig( regionLayerName, "rateLearningRate", String.valueOf( rateLearningRate ) );
//        Framework.SetConfig( regionLayerName, "contextualLearningRate", String.valueOf( sparseLearningRate ) );
//        Framework.SetConfig( regionLayerName, "contextualWidthCells", String.valueOf( widthCells ) );
//        Framework.SetConfig( regionLayerName, "contextualHeightCells", String.valueOf( heightCells ) );
//        Framework.SetConfig( regionLayerName, "contextualSparsity", String.valueOf( 0 ) );
//        Framework.SetConfig( regionLayerName, "contextualSparsityOutput", String.valueOf( sparsityFactor ) );
//        Framework.SetConfig( regionLayerName, "contextualSparsityMin", String.valueOf( sparsityMin ) );
//        Framework.SetConfig( regionLayerName, "contextualSparsityMax", String.valueOf( sparsityMax ) );
//        Framework.SetConfig( regionLayerName, "contextualAgeMin", String.valueOf( ageMin ) );
//        Framework.SetConfig( regionLayerName, "contextualAgeMax", String.valueOf( ageMax ) );
//        Framework.SetConfig( regionLayerName, "contextualAge", String.valueOf( 0 ) );

        Framework.SetConfig( regionLayerName, "slowSparsity", String.valueOf( sparsitySlow ) );
        Framework.SetConfig( regionLayerName, "defaultPredictionInhibition", String.valueOf( defaultPredictionInhibition ) );
        Framework.SetConfig( regionLayerName, "predictorLearningRate", String.valueOf( predictorLearningRate ) );

    }

}
