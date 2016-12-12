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
import io.agi.core.util.PropertiesUtil;
import io.agi.core.util.images.BufferedImageSource.BufferedImageSourceFactory;
import io.agi.framework.Framework;
import io.agi.framework.Main;
import io.agi.framework.Node;
import io.agi.framework.entities.*;

import java.util.ArrayList;
import java.util.Properties;

/**
 * Created by gideon on 14/03/2016.
 */
public class DeepMNISTDemo {

    public static void main( String[] args ) {

        // Create a Node
        Main m = new Main();
        Properties p = PropertiesUtil.load( args[ 0 ] );
        m.setup( p, null, new MnistEntityFactory() );

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

//- cells aren't staying active 2nd step anymore.
//- areas at the edges aren't learning so good
//- does it age when there's no update? shouldn't
//- otherwise, L1 seems to learn well, but only 60% correct
// 1. turned on the emit-unchanged
// 2. then try slower learning rate.

    public static void createEntities( Node n ) {
//show real FP/FN errors only in gui
//somehow illustrate dormant cols
//how to plumb up the fb pathway?
//inverted region - gets FN errors from above
//use the state of the inverted region to read out the predictions?
//        String trainingPath = "/home/dave/workspace/agi.io/data/mnist/cycle10";
//        String testingPath = "/home/dave/workspace/agi.io/data/mnist/cycle3";
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
        String trainingPath = "./data/training";
        String testingPath = "./data/testing";
        int terminationAge = 25000;
        int trainingBatches = 2;
        boolean terminateByAge = false;
        float defaultPredictionInhibition = 1.f; // random image classification only experiments
//        float defaultPredictionInhibition = 0.f; // where you use prediction
        boolean encodeZero = false;
        boolean classFeaturesOnline = false;
        int layers = 3;

        ArrayList<LearningEntitiesAnalyticsType> learningEntitiesAnalyticsTypes = new ArrayList<>();
        learningEntitiesAnalyticsTypes.add( LearningEntitiesAnalyticsType.SvmEntity );
        learningEntitiesAnalyticsTypes.add( LearningEntitiesAnalyticsType.LabelFeatures );

        // Define some entities' names
        String experimentName            = Framework.GetEntityName( "experiment" );
        String imageClassName            = Framework.GetEntityName( "image-class" );
        String imageEncoderName          = Framework.GetEntityName( "image-encoder" );
        String constantName              = Framework.GetEntityName( "constant" );
        String region1FfName             = Framework.GetEntityName( "image-region-1-ff" );
        String region2FfName             = Framework.GetEntityName( "image-region-2-ff" );
        String region3FfName             = Framework.GetEntityName( "image-region-3-ff" );
        String activityImageDecoderName  = Framework.GetEntityName( "activity-image-decoder" );
        String predictedImageDecoderName = Framework.GetEntityName( "predicted-image-decoder" );

        String svmEntitySeriesPredictedName  = Framework.GetEntityName( "svm-value-series-predicted" );
        String svmEntitySeriesErrorName      = Framework.GetEntityName( "svm-value-series-error" );
        String svmEntitySeriesTruthName      = Framework.GetEntityName( "svm-value-series-truth" );

        String svmEntitySeriesFeatures   = Framework.GetEntityName( "svm-vector-series-features" );

        String featureLabelsSeriesPredictedName  = Framework.GetEntityName( "feature-labels-value-series-predicted" );
        String featureLabelsSeriesErrorName      = Framework.GetEntityName( "feature-labels-value-series-error" );
        String featureLabelsSeriesTruthName      = Framework.GetEntityName( "feature-labels-value-series-truth" );


        // Create Entities
        Framework.CreateEntity( experimentName, ExperimentEntity.ENTITY_TYPE, n.getName(), null ); // experiment is the root entity
        Framework.CreateEntity( imageClassName, ImageClassEntity.ENTITY_TYPE, n.getName(), experimentName );
        Framework.CreateEntity( imageEncoderName, EncoderEntity.ENTITY_TYPE, n.getName(), imageClassName );
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

        String learningEntitiesAlgorithm = region1FfName;
        String featureLabelsName = null;
        if ( learningEntitiesAnalyticsTypes.contains( LearningEntitiesAnalyticsType.LabelFeatures ) ) {
            featureLabelsName = Framework.GetEntityName( "feature-labels" );
            Framework.CreateEntity( featureLabelsName, FeatureLabelsEntity.ENTITY_TYPE, n.getName(), topLayerName ); // 2nd, class region updates after first to get its feedback

            Framework.CreateEntity( activityImageDecoderName, DecoderEntity.ENTITY_TYPE, n.getName(), featureLabelsName );
            Framework.CreateEntity( predictedImageDecoderName, DecoderEntity.ENTITY_TYPE, n.getName(), featureLabelsName );

            Framework.CreateEntity( featureLabelsSeriesPredictedName, ValueSeriesEntity.ENTITY_TYPE, n.getName(), featureLabelsName ); // 2nd, class region updates after first to get its feedback
            Framework.CreateEntity( featureLabelsSeriesErrorName, ValueSeriesEntity.ENTITY_TYPE, n.getName(), featureLabelsName ); // 2nd, class region updates after first to get its feedback
            Framework.CreateEntity( featureLabelsSeriesTruthName, ValueSeriesEntity.ENTITY_TYPE, n.getName(), featureLabelsName ); // 2nd, class region updates after first to get its feedback
        }

        String svmEntityName = null;
        if (learningEntitiesAnalyticsTypes.contains( LearningEntitiesAnalyticsType.SvmEntity ) ) {
            svmEntityName = Framework.GetEntityName( "svm-eval" );
            Framework.CreateEntity( svmEntityName, SVMEntity.ENTITY_TYPE, n.getName(), topLayerName ); // 2nd, class region updates after first to get its feedback

            Framework.CreateEntity( svmEntitySeriesPredictedName, ValueSeriesEntity.ENTITY_TYPE, n.getName(), svmEntityName ); // 2nd, class region updates after first to get its feedback
            Framework.CreateEntity( svmEntitySeriesErrorName, ValueSeriesEntity.ENTITY_TYPE, n.getName(), svmEntityName ); // 2nd, class region updates after first to get its feedback
            Framework.CreateEntity( svmEntitySeriesTruthName, ValueSeriesEntity.ENTITY_TYPE, n.getName(), svmEntityName ); // 2nd, class region updates after first to get its feedback

            // vector series to accumulate features over time (the training data set) :  input = features, output = svmEntity
            Framework.CreateEntity( svmEntitySeriesFeatures, VectorSeriesEntity.ENTITY_TYPE, n.getName(), topLayerName );
        }



//        Framework.CreateEntity( classRegionName, RegionLayerEntity.ENTITY_TYPE, n.getName(), topLayerName ); // 2nd, class region updates after first to get its feedback
//        Framework.CreateEntity( classDecoderName, DecoderEntity.ENTITY_TYPE, n.getName(), classRegionName ); // produce the predicted classification for inspection by mnist next time


        // Connect the entities' data
        // a) Image to image region, and decode
        Framework.SetDataReference( imageEncoderName, EncoderEntity.DATA_INPUT, imageClassName, ImageClassEntity.OUTPUT_IMAGE );

        Framework.SetDataReference( region1FfName, RegionLayerEntity.FF_INPUT_1, imageEncoderName, EncoderEntity.DATA_OUTPUT_ENCODED );
        Framework.SetDataReference( region1FfName, RegionLayerEntity.FF_INPUT_2, constantName, ConstantMatrixEntity.OUTPUT );
        Framework.SetDataReference( region1FfName, RegionLayerEntity.FB_INPUT, constantName, ConstantMatrixEntity.OUTPUT ); // feedback to this region is just a constant

        if( layers > 1 ) {
            Framework.SetDataReference( region2FfName, RegionLayerEntity.FF_INPUT_1, region1FfName, RegionLayerEntity.PREDICTION_FN );
            Framework.SetDataReference( region2FfName, RegionLayerEntity.FF_INPUT_2, constantName, ConstantMatrixEntity.OUTPUT );
            Framework.SetDataReference( region2FfName, RegionLayerEntity.FB_INPUT, constantName, ConstantMatrixEntity.OUTPUT ); // feedback to this region is just a constant
            learningEntitiesAlgorithm = learningEntitiesAlgorithm + "," + region2FfName;
        }

        if( layers > 2 ) {
            Framework.SetDataReference( region3FfName, RegionLayerEntity.FF_INPUT_1, region2FfName, RegionLayerEntity.PREDICTION_FN );
            Framework.SetDataReference( region3FfName, RegionLayerEntity.FF_INPUT_2, constantName, ConstantMatrixEntity.OUTPUT );
// Option to provide class-label directly to layer 3 for inclusion in classification
//            Framework.SetDataReference( region3FfName, RegionLayerEntity.FF_INPUT_2, labelEncoderName, EncoderEntity.DATA_OUTPUT_ENCODED );
            Framework.SetDataReference( region3FfName, RegionLayerEntity.FB_INPUT, constantName, ConstantMatrixEntity.OUTPUT ); // feedback to this region is just a constant
            learningEntitiesAlgorithm = learningEntitiesAlgorithm + "," + region3FfName;
        }

        Framework.SetDataReference( activityImageDecoderName, DecoderEntity.DATA_INPUT_ENCODED, region1FfName, RegionLayerEntity.FB_OUTPUT_1_UNFOLDED_ACTIVITY );
        Framework.SetDataReference( predictedImageDecoderName, DecoderEntity.DATA_INPUT_ENCODED, region1FfName, RegionLayerEntity.FB_OUTPUT_1_UNFOLDED_PREDICTION );

        // a) Class to class region, and decode
//        Framework.SetDataReference( labelEncoderName, EncoderEntity.DATA_INPUT, mnistName, MnistEntity.OUTPUT_IMAGE_LABEL );
//        Framework.SetDataReference( classEncoderName, EncoderEntity.DATA_INPUT, mnistName, MnistEntity.OUTPUT_CLASSIFICATION );
//        Framework.SetDataReference( classRegionName, RegionLayerEntity.FF_INPUT_1, classEncoderName, EncoderEntity.DATA_OUTPUT_ENCODED );
//        Framework.SetDataReference( classRegionName, RegionLayerEntity.FF_INPUT_2, constantName, ConstantMatrixEntity.OUTPUT );
//
//        ArrayList< AbstractPair< String, String > > referenceEntitySuffixes = new ArrayList< AbstractPair< String, String > >();
////        referenceEntitySuffixes.add( new AbstractPair< String, String >( region1FfName, RegionLayerEntity.PREDICTION_FN ) );
//        if( layers > 1 ) referenceEntitySuffixes.add( new AbstractPair< String, String >( region2FfName, RegionLayerEntity.PREDICTION_FN ) );
//        if( layers > 2 ) referenceEntitySuffixes.add( new AbstractPair< String, String >( region3FfName, RegionLayerEntity.PREDICTION_FN ) );



        ArrayList< AbstractPair< String, String > > featureDatas = new ArrayList< AbstractPair< String, String > >();
        if( layers > 0 ) featureDatas.add( new AbstractPair< String, String >( region1FfName, RegionLayerEntity.PREDICTION_FN ) );
        if( layers > 1 ) featureDatas.add( new AbstractPair< String, String >( region2FfName, RegionLayerEntity.PREDICTION_FN ) );
        if( layers > 2 ) featureDatas.add( new AbstractPair< String, String >( region3FfName, RegionLayerEntity.PREDICTION_FN ) );

        if ( learningEntitiesAnalyticsTypes.contains( LearningEntitiesAnalyticsType.LabelFeatures ) ) {
            Framework.SetDataReferences( featureLabelsName, FeatureLabelsEntity.FEATURES, featureDatas ); // get current state from the region to be used to predict
        }

        if ( learningEntitiesAnalyticsTypes.contains( LearningEntitiesAnalyticsType.SvmEntity ) ) {
            Framework.SetDataReferences( svmEntityName, FeatureLabelsEntity.FEATURES, featureDatas );           // get current state from the region to be used to predict
            Framework.SetDataReferences( svmEntitySeriesFeatures, VectorSeriesEntity.INPUT, featureDatas );     // accumulate data set for input to SVM for training
            Framework.SetDataReference( svmEntityName, SVMEntity.ACCUMULATED_FEATURES, svmEntitySeriesFeatures, VectorSeriesEntity.OUTPUT );  // connect it to the SVM
        }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//        // Special test of accuracy in static classification:
//        referenceEntitySuffixes.add( new AbstractPair< String, String >( region3FfName, RegionLayerEntity.ACTIVITY_1 ) );
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

//        Framework.SetDataReferences( classRegionName, RegionLayerEntity.FB_INPUT, referenceEntitySuffixes ); // get current state from the region to be used to predict
//        Framework.SetDataReference( classDecoderName, DecoderEntity.DATA_INPUT_ENCODED, classRegionName, RegionLayerEntity.FB_OUTPUT_1_UNFOLDED_PREDICTION ); // the prediction of the next state
//        Framework.SetDataReference( mnistName, MnistEntity.INPUT_CLASSIFICATION, classDecoderName, DecoderEntity.DATA_OUTPUT_DECODED ); // the (decoded) prediction of the next state

        // Experiment config
        if( !terminateByAge ) {
            Framework.SetConfig( experimentName, "terminationEntityName", imageClassName );
            Framework.SetConfig( experimentName, "terminationConfigPath", "terminate" );
            Framework.SetConfig( experimentName, "terminationAge", "-1" ); // wait for mnist to decide
        } else {
            Framework.SetConfig( experimentName, "terminationAge", String.valueOf( terminationAge ) ); // fixed steps
        }

        // Mnist config
        Framework.SetConfig( imageClassName, "receptiveField.receptiveFieldX", "0" );
        Framework.SetConfig( imageClassName, "receptiveField.receptiveFieldY", "0" );
        Framework.SetConfig( imageClassName, "receptiveField.receptiveFieldW", "28" );
        Framework.SetConfig( imageClassName, "receptiveField.receptiveFieldH", "28" );
        Framework.SetConfig( imageClassName, "resolution.resolutionX", "28" );
        Framework.SetConfig( imageClassName, "resolution.resolutionY", "28" );
        Framework.SetConfig( imageClassName, "greyscale", "true" );
        Framework.SetConfig( imageClassName, "invert", "true" );
        Framework.SetConfig( imageClassName, "sourceType", BufferedImageSourceFactory.TYPE_IMAGE_FILES );
        Framework.SetConfig( imageClassName, "sourceFilesPrefix", "postproc" );
        Framework.SetConfig( imageClassName, "sourceFilesPathTraining", trainingPath );
        Framework.SetConfig( imageClassName, "sourceFilesPathTesting", testingPath );
        Framework.SetConfig( imageClassName, "trainingBatches", String.valueOf( trainingBatches ) );

        Framework.SetConfig( imageClassName, "learningEntitiesAlgorithm", String.valueOf( learningEntitiesAlgorithm ) );

        if ( learningEntitiesAnalyticsTypes.contains( LearningEntitiesAnalyticsType.LabelFeatures ) ) {
            Framework.SetConfig( imageClassName, "learningEntitiesAnalytics", String.valueOf( featureLabelsName ) );
        }

        if ( learningEntitiesAnalyticsTypes.contains( LearningEntitiesAnalyticsType.SvmEntity ) ) {
            String configVal = Framework.GetConfig( imageClassName, "learningEntitiesAnalytics" );

            String newConfig = null;
            if ( configVal != null ) {
                newConfig = configVal + "," + svmEntityName;
            }
            else {
                newConfig = svmEntityName;
            }

            Framework.SetConfig( imageClassName, "learningEntitiesAnalytics", String.valueOf( newConfig ) );
        }

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
        } else {

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

//        // class encoder config
//        Framework.SetConfig( labelEncoderName, "encoderType", NumberEncoder.class.getSimpleName() );
//        Framework.SetConfig( labelEncoderName, "digits", "2" );
//        Framework.SetConfig( labelEncoderName, "numbers", "1" );
//
//        // class encoder config
//        Framework.SetConfig( classEncoderName, "encoderType", NumberEncoder.class.getSimpleName() );
//        Framework.SetConfig( classEncoderName, "digits", "2" );
//        Framework.SetConfig( classEncoderName, "numbers", "1" );
//
//        // class decoder config
//        Framework.SetConfig( classDecoderName, "encoderType", NumberEncoder.class.getSimpleName() );
//        Framework.SetConfig( classDecoderName, "digits", "2" );
//        Framework.SetConfig( classDecoderName, "numbers", "1" );

        if ( learningEntitiesAnalyticsTypes.contains( LearningEntitiesAnalyticsType.LabelFeatures ) ) {
            // feature-class config
            Framework.SetConfig( featureLabelsName, "classEntityName", imageClassName );
            Framework.SetConfig( featureLabelsName, "classConfigPath", "imageClass" );
            Framework.SetConfig( featureLabelsName, "classes", "10" );
            Framework.SetConfig( featureLabelsName, "onlineLearning", String.valueOf( classFeaturesOnline ) );
            Framework.SetConfig( featureLabelsName, "onlineLearningRate", "0.001" );
        }

        if ( learningEntitiesAnalyticsTypes.contains( LearningEntitiesAnalyticsType.SvmEntity ) ) {
            // svm config
            Framework.SetConfig( svmEntityName, "classEntityName", imageClassName );
            Framework.SetConfig( svmEntityName, "classConfigPath", "imageClass" );
            Framework.SetConfig( svmEntityName, "classes", "10" );
            Framework.SetConfig( svmEntityName, "onlineLearning", String.valueOf( false ) );
            Framework.SetConfig( svmEntityName, "onlineLearningRate", "0.0" );
        }

        // data series logging
        Framework.SetConfig( featureLabelsSeriesPredictedName, "period", "-1" ); // log forever
        Framework.SetConfig( featureLabelsSeriesErrorName, "period", "-1" );
        Framework.SetConfig( featureLabelsSeriesTruthName, "period", "-1" );

        if ( learningEntitiesAnalyticsTypes.contains( LearningEntitiesAnalyticsType.LabelFeatures ) ) {
            Framework.SetConfig( featureLabelsSeriesPredictedName, "entityName", featureLabelsName );
            Framework.SetConfig( featureLabelsSeriesErrorName, "entityName", featureLabelsName );
            Framework.SetConfig( featureLabelsSeriesTruthName, "entityName", featureLabelsName );

            Framework.SetConfig( featureLabelsSeriesPredictedName, "configPath", "classPredicted" );
            Framework.SetConfig( featureLabelsSeriesErrorName, "configPath", "classError" );
            Framework.SetConfig( featureLabelsSeriesTruthName, "configPath", "classTruth" );
        }

        if ( learningEntitiesAnalyticsTypes.contains( LearningEntitiesAnalyticsType.SvmEntity ) ) {
            Framework.SetConfig( svmEntitySeriesPredictedName, "entityName", svmEntityName );
            Framework.SetConfig( svmEntitySeriesErrorName, "entityName", svmEntityName );
            Framework.SetConfig( svmEntitySeriesTruthName, "entityName", svmEntityName );

            Framework.SetConfig( svmEntitySeriesPredictedName, "configPath", "classPredicted" );
            Framework.SetConfig( svmEntitySeriesErrorName, "configPath", "classError" );
            Framework.SetConfig( svmEntitySeriesTruthName, "configPath", "classTruth" );

        }



        // image region config
        boolean emitUnchangedCells = false; // if there are no input bits, the classifier isn't updated.
        int organizerWidth = 8;
        int organizerHeight = 8;
        int classifierWidth = 5;//5
        int classifierHeight = 5;
        int classifierDepth = 1;
        int classifiersPerBit1 = 4;//5;
        int classifiersPerBit2 = 1;

// Plastic NG region:
//        float classifierStressLearningRate = 0.01f;//0.005f;//0.01f;
//        float classifierRankLearningRate = 0.1f;//0.05f;// 0.1f;
//        float classifierRankScale = 1.0f;//25.0f;//7.f;//100.f;//15.f;//10.0f;
//        int classifierAgeMax = 600;
//        float classifierAgeDecay = 0.5f;
//        float classifierAgeScale = 12.0f;

//        int predictorLearningRate = 500;
//        float predictorLearningRate = 0.001f;
        float predictorLearningRate = 0.005f;
//        float predictorLearningRate = 0.01f;
        int edgeMaxAge = 400;
        int classifierGrowthInterval = 120;

        // GNG region layer:
        organizerWidth = 8;
        organizerHeight = 8;
        classifiersPerBit1 = 4;
        classifierWidth = 5;
        classifierHeight = 4;

//            setPlasticRegionLayerConfig(
//                region1FfName, defaultPredictionInhibition, emitUnchangedCells, organizerWidth, organizerHeight,
//                classifiersPerBit1, classifiersPerBit2, classifierWidth, classifierHeight, classifierDepth,
//                classifierStressLearningRate, classifierRankLearningRate, classifierRankScale,
//                classifierAgeMax, classifierAgeDecay, classifierAgeScale, predictorLearningRate );
        if( layers > 0 ) {
            setRegionLayerConfig(
                region1FfName, defaultPredictionInhibition, emitUnchangedCells,
                classifiersPerBit1, classifiersPerBit2,
                classifierWidth, classifierHeight, classifierDepth,
                organizerWidth, organizerHeight, edgeMaxAge, predictorLearningRate, classifierGrowthInterval );
        }

// Plastic:
//        organizerWidth = 8;
//        organizerHeight = 8;
//        classifiersPerBit1 = 3;//9;//11;//7; revert to 5
//        classifierWidth = 6; // 5+2 = 7 (significant increase)
//        classifierHeight = 6;
//        classifierRankScale = 10.0f;//25.0f;//7.f;//100.f;//15.f;//10.0f;
        organizerWidth = 7;
        organizerHeight = 7;
        classifiersPerBit1 = 3;//4;
//        classifierWidth = 5;
//        classifierHeight = 5;
        classifierWidth = 6;
        classifierHeight = 6;

        if( layers > 1 ) {
//            setPlasticRegionLayerConfig(
//                region2FfName, defaultPredictionInhibition, emitUnchangedCells, organizerWidth, organizerHeight,
//                classifiersPerBit1, classifiersPerBit2, classifierWidth, classifierHeight, classifierDepth,
//                classifierStressLearningRate, classifierRankLearningRate, classifierRankScale,
//                classifierAgeMax, classifierAgeDecay, classifierAgeScale, predictorLearningRate );
            setRegionLayerConfig(
                region2FfName, defaultPredictionInhibition, emitUnchangedCells,
                classifiersPerBit1, classifiersPerBit2,
                classifierWidth, classifierHeight, classifierDepth,
                organizerWidth, organizerHeight, edgeMaxAge, predictorLearningRate, classifierGrowthInterval );
        }

// Plastic:
//        organizerWidth = 7;
//        organizerHeight = 7;
//        classifiersPerBit1 = 3;//9;//11;//7; revert to 5
//        classifierWidth = 6; // 5+2 = 7 (significant increase)
//        classifierHeight = 6;
//        classifierRankScale = 10.0f;//25.0f;//7.f;//100.f;//15.f;//10.0f;

//        organizerWidth = 3;
//        organizerHeight = 3; // wide coverage
//
//        classifiersPerBit1 = 4;
////        classifiersPerBit2 = 1;//organizerWidth * organizerHeight; // i.e. input 2 innervates all columns
//
//        classifierWidth = 9; // 5+2 = 7 (significant increase)
//        classifierHeight = 9;
        organizerWidth = 2;
        organizerHeight = 2;
        classifiersPerBit1 = 3;
        classifierWidth = 8;
        classifierHeight = 8;

        if( layers > 2 ) {
//            setPlasticRegionLayerConfig(
//                region3FfName, defaultPredictionInhibition, emitUnchangedCells, organizerWidth, organizerHeight,
//                classifiersPerBit1, classifiersPerBit2, classifierWidth, classifierHeight, classifierDepth,
//                classifierStressLearningRate, classifierRankLearningRate, classifierRankScale,
//                classifierAgeMax, classifierAgeDecay, classifierAgeScale, predictorLearningRate );
            setRegionLayerConfig(
                region3FfName, defaultPredictionInhibition, emitUnchangedCells,
                classifiersPerBit1, classifiersPerBit2,
                classifierWidth, classifierHeight, classifierDepth,
                organizerWidth, organizerHeight, edgeMaxAge, predictorLearningRate, classifierGrowthInterval );
        }

// Plastic:
//        classifierStressLearningRate = 0.01f;
//        classifierRankLearningRate = 0.05f;
//        classifierRankScale = 10.0f;
//        classifierAgeMax = 500;
//        classifierAgeDecay = 0.7f;
//        classifierAgeScale = 12.0f;

        organizerWidth = 2;
        organizerHeight = 2;
        classifiersPerBit1 = 5;
        classifierWidth = 5;
        classifierHeight = 5;

//        setPlasticRegionLayerConfig(
//            classRegionName, defaultPredictionInhibition, emitUnchangedCells, organizerWidth, organizerHeight,
//            classifiersPerBit1, classifiersPerBit2, classifierWidth,classifierHeight, classifierDepth,
//            classifierStressLearningRate, classifierRankLearningRate, classifierRankScale,
//            classifierAgeMax, classifierAgeDecay, classifierAgeScale, predictorLearningRate );
//        setRegionLayerConfig(
//            classRegionName, defaultPredictionInhibition, emitUnchangedCells,
//            classifiersPerBit1, classifiersPerBit2,
//            classifierWidth, classifierHeight, classifierDepth,
//            organizerWidth, organizerHeight, edgeMaxAge, predictorLearningRate, classifierGrowthInterval );


/*        // class region config
//        Framework.SetConfig( classRegionName, "predictorLearningRate", "100" );
        Framework.SetConfig( classRegionName, "predictorLearningRate", "500" );
        Framework.SetConfig( classRegionName, "receptiveFieldsTrainingSamples", "0.1" );
        Framework.SetConfig( classRegionName, "classifiersPerBit", "5" );

//        Framework.SetConfig( classRegionName, "organizerStressThreshold", "0.0" );
//        Framework.SetConfig( classRegionName, "organizerGrowthInterval", "1" );
//        Framework.SetConfig( classRegionName, "organizerEdgeMaxAge", "1000" );
//        Framework.SetConfig( classRegionName, "organizerNoiseMagnitude", "0.0" );
////        Framework.SetConfig( classRegionName, "organizerLearningRate", "0.002" );
//        Framework.SetConfig( classRegionName, "organizerLearningRate", "0.02" );
//        Framework.SetConfig( classRegionName, "organizerElasticity", "1.5" );
//        Framework.SetConfig( classRegionName, "organizerLearningRateNeighbours", "0.001" );
        Framework.SetConfig( classRegionName, "organizerWidthCells", "2" );
        Framework.SetConfig( classRegionName, "organizerHeightCells", "2" );
        Framework.SetConfig( classRegionName, "organizerNeighbourhoodRange", "2" );

        Framework.SetConfig( classRegionName, "organizerIntervalsInput1X", "2" );
        Framework.SetConfig( classRegionName, "organizerIntervalsInput2X", "1" );
        Framework.SetConfig( classRegionName, "organizerIntervalsInput1Y", "2" );
        Framework.SetConfig( classRegionName, "organizerIntervalsInput2Y", "1" );

//        Framework.SetConfig( classRegionName, "classifierWidthCells", "4" );
//        Framework.SetConfig( classRegionName, "classifierHeightCells", "4" );
        Framework.SetConfig( classRegionName, "classifierWidthCells", "5" );
        Framework.SetConfig( classRegionName, "classifierHeightCells", "5" );
        Framework.SetConfig( classRegionName, "classifierDepthCells", "1" );

        Framework.SetConfig( classRegionName, "classifierStressThreshold", "0.1" );
        Framework.SetConfig( classRegionName, "classifierGrowthInterval", "120" );
//        Framework.SetConfig( classRegionName, "classifierEdgeMaxAge", "100" );
//        Framework.SetConfig( classRegionName, "classifierGrowthInterval", "120" );
//        Framework.SetConfig( classRegionName, "classifierEdgeMaxAge", "250" );
//        Framework.SetConfig( classRegionName, "classifierEdgeMaxAge", "400" );
        Framework.SetConfig( classRegionName, "classifierEdgeMaxAge", "600" );
        Framework.SetConfig( classRegionName, "classifierGrowthInterval", "120" );

        Framework.SetConfig( classRegionName, "classifierLearningRate", "0.1" );
        Framework.SetConfig( classRegionName, "classifierLearningRateNeighbours", "0.005" );
        Framework.SetConfig( classRegionName, "classifierStressLearningRate", "0.1" );
        Framework.SetConfig( classRegionName, "classifierStressSplitLearningRate", "0.5" );
        Framework.SetConfig( classRegionName, "classifierNoiseMagnitude", "0.1" );
        */
    }

    public static void setPlasticRegionLayerConfig(
            String regionLayerName,
            float defaultPredictionInhibition,
            boolean emitUnchangedCells,
            int organizerWidth,
            int organizerHeight,
            int classifiersPerBit1,
            int classifiersPerBit2,
            int classifierWidth,
            int classifierHeight,
            int classifierDepth,
            float classifierStressLearningRate,
            float classifierRankLearningRate,
            float classifierRankScale,
            int classifierAgeMax,
            float classifierAgeDecay,
            float classifierAgeScale,
            int predictorLearningRate ) {

        Framework.SetConfig( regionLayerName, "emitUnchangedCells", String.valueOf( emitUnchangedCells ) );
        Framework.SetConfig( regionLayerName, "defaultPredictionInhibition", String.valueOf( defaultPredictionInhibition ) );
        Framework.SetConfig( regionLayerName, "classifiersPerBit1", String.valueOf( classifiersPerBit1 ) );
        Framework.SetConfig( regionLayerName, "classifiersPerBit2", String.valueOf( classifiersPerBit2 ) );

        Framework.SetConfig( regionLayerName, "organizerWidthCells", String.valueOf( organizerWidth ) );
        Framework.SetConfig( regionLayerName, "organizerHeightCells", String.valueOf( organizerHeight ) );
        Framework.SetConfig( regionLayerName, "organizerIntervalsInput1X", String.valueOf( organizerWidth ) );
        Framework.SetConfig( regionLayerName, "organizerIntervalsInput2X", "1" ); // because no 2nd input.
        Framework.SetConfig( regionLayerName, "organizerIntervalsInput1Y", String.valueOf( organizerHeight ) );
        Framework.SetConfig( regionLayerName, "organizerIntervalsInput2Y", "1" ); // because no 2nd input.

        Framework.SetConfig( regionLayerName, "classifierWidthCells", String.valueOf( classifierWidth ) );
        Framework.SetConfig( regionLayerName, "classifierHeightCells", String.valueOf( classifierHeight ) );
        Framework.SetConfig( regionLayerName, "classifierDepthCells", String.valueOf( classifierDepth ) );

        Framework.SetConfig( regionLayerName, "classifierStressLearningRate", String.valueOf( classifierStressLearningRate ) );
        Framework.SetConfig( regionLayerName, "classifierRankLearningRate", String.valueOf( classifierRankLearningRate ) );
        Framework.SetConfig( regionLayerName, "classifierRankScale", String.valueOf( classifierRankScale ) );
        Framework.SetConfig( regionLayerName, "classifierAgeMax", String.valueOf( classifierAgeMax ) );
        Framework.SetConfig( regionLayerName, "classifierAgeDecay", String.valueOf( classifierAgeDecay ) );
        Framework.SetConfig( regionLayerName, "classifierAgeScale", String.valueOf( classifierAgeScale ) );

        Framework.SetConfig( regionLayerName, "predictorLearningRate", String.valueOf( predictorLearningRate ) );
    }

    public static void setRegionLayerConfig(
            String regionLayerName,
            float defaultPredictionInhibition,
            boolean emitUnchangedCells,
            int classifiersPerBit1,
            int classifiersPerBit2,
            int classifierWidth,
            int classifierHeight,
            int classifierDepth,
            int organizerWidth,
            int organizerHeight,
            int edgeMaxAge,
            float predictorLearningRate,
            int classifierGrowthInterval ) {

//        Framework.SetConfig( regionLayerName, "organizerTrainOnChange", String.valueOf( organizerTrainOnChange ) );
        Framework.SetConfig( regionLayerName, "emitUnchangedCells", String.valueOf( emitUnchangedCells ) );
        Framework.SetConfig( regionLayerName, "predictorLearningRate", String.valueOf( predictorLearningRate ) );
        Framework.SetConfig( regionLayerName, "receptiveFieldsTrainingSamples", "0.1" );
        Framework.SetConfig( regionLayerName, "defaultPredictionInhibition", String.valueOf( defaultPredictionInhibition ) );

        Framework.SetConfig( regionLayerName, "classifiersPerBit1", String.valueOf( classifiersPerBit1 ) );
        Framework.SetConfig( regionLayerName, "classifiersPerBit2", String.valueOf( classifiersPerBit2 ) );

        Framework.SetConfig( regionLayerName, "organizerWidthCells", String.valueOf( organizerWidth ) );//"8" );
        Framework.SetConfig( regionLayerName, "organizerHeightCells", String.valueOf( organizerHeight ) );//"8" );

        Framework.SetConfig( regionLayerName, "organizerIntervalsInput1X", String.valueOf( organizerWidth ) );
        Framework.SetConfig( regionLayerName, "organizerIntervalsInput2X", "1" ); // because no 2nd input.
        Framework.SetConfig( regionLayerName, "organizerIntervalsInput1Y", String.valueOf( organizerHeight ) );
        Framework.SetConfig( regionLayerName, "organizerIntervalsInput2Y", "1" ); // because no 2nd input.

        Framework.SetConfig( regionLayerName, "classifierWidthCells", String.valueOf( classifierWidth ) );//"5" );
        Framework.SetConfig( regionLayerName, "classifierHeightCells", String.valueOf( classifierHeight ) );//"6" );
        Framework.SetConfig( regionLayerName, "classifierDepthCells", String.valueOf( classifierDepth ) );
        Framework.SetConfig( regionLayerName, "classifierStressThreshold", "0.0" ); // use all cells

//        Framework.SetConfig( regionLayerName, "classifierLearningRate", "0.05" );
//        Framework.SetConfig( regionLayerName, "classifierLearningRate", "0.01" );
        Framework.SetConfig( regionLayerName, "classifierLearningRate", "0.03" );
        Framework.SetConfig( regionLayerName, "classifierLearningRateNeighbours", "0.001" );
        Framework.SetConfig( regionLayerName, "classifierStressLearningRate", "0.001" );
        Framework.SetConfig( regionLayerName, "classifierStressSplitLearningRate", "0.5" );

        Framework.SetConfig( regionLayerName, "classifierGrowthInterval", String.valueOf( classifierGrowthInterval ) );
        Framework.SetConfig( regionLayerName, "classifierEdgeMaxAge", String.valueOf( edgeMaxAge ) );//"600" );
//        Framework.SetConfig( regionLayerName, "classifierNoiseMagnitude", "0.1" );
    }


}
