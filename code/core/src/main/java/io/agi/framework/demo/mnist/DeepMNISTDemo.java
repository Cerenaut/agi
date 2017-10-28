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
import io.agi.framework.Naming;
import io.agi.framework.Node;
import io.agi.framework.entities.*;
import io.agi.framework.persistence.PersistenceUtil;
import io.agi.framework.references.DataRefUtil;

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
                Naming.SetEntityNamePrefix( prefix );
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
        String experimentName            = PersistenceUtil.GetEntityName( "experiment" );
        String imageClassName            = PersistenceUtil.GetEntityName( "image-class" );
        String imageEncoderName          = PersistenceUtil.GetEntityName( "image-encoder" );
        String constantName              = PersistenceUtil.GetEntityName( "constant" );
        String region1FfName             = PersistenceUtil.GetEntityName( "image-region-1-ff" );
        String region2FfName             = PersistenceUtil.GetEntityName( "image-region-2-ff" );
        String region3FfName             = PersistenceUtil.GetEntityName( "image-region-3-ff" );
        String activityImageDecoderName  = PersistenceUtil.GetEntityName( "activity-image-decoder" );
        String predictedImageDecoderName = PersistenceUtil.GetEntityName( "predicted-image-decoder" );

        String svmEntitySeriesPredictedName  = PersistenceUtil.GetEntityName( "svm-value-series-predicted" );
        String svmEntitySeriesErrorName  = PersistenceUtil.GetEntityName( "svm-value-series-error" );
        String svmEntitySeriesTruthName  = PersistenceUtil.GetEntityName( "svm-value-series-truth" );
        String svmEntitySeriesFeatures   = PersistenceUtil.GetEntityName( "svm-series-features" );


        String featureLabelsSeriesPredictedName  = PersistenceUtil.GetEntityName( "feature-labels-value-series-predicted" );
        String featureLabelsSeriesErrorName      = PersistenceUtil.GetEntityName( "feature-labels-value-series-error" );
        String featureLabelsSeriesTruthName      = PersistenceUtil.GetEntityName( "feature-labels-value-series-truth" );


        // Create Entities
        PersistenceUtil.CreateEntity( experimentName, ExperimentEntity.ENTITY_TYPE, n.getName(), null ); // experiment is the root entity
        PersistenceUtil.CreateEntity( imageClassName, ImageLabelEntity.ENTITY_TYPE, n.getName(), experimentName );
        PersistenceUtil.CreateEntity( imageEncoderName, EncoderEntity.ENTITY_TYPE, n.getName(), imageClassName );
        PersistenceUtil.CreateEntity( constantName, ConstantMatrixEntity.ENTITY_TYPE, n.getName(), imageEncoderName ); // ok all input to the regions is ready

        PersistenceUtil.CreateEntity( region1FfName, RegionLayerEntity.ENTITY_TYPE, n.getName(), constantName );
        String topLayerName = region1FfName;
        if( layers > 1 ) {
            PersistenceUtil.CreateEntity( region2FfName, RegionLayerEntity.ENTITY_TYPE, n.getName(), region1FfName );
            topLayerName = region2FfName;
        }
        if( layers > 2 ) {
            PersistenceUtil.CreateEntity( region3FfName, RegionLayerEntity.ENTITY_TYPE, n.getName(), region2FfName );
            topLayerName = region3FfName;
        }

        String learningEntitiesAlgorithm = region1FfName;
        String featureLabelsName = null;
        if ( learningEntitiesAnalyticsTypes.contains( LearningEntitiesAnalyticsType.LabelFeatures ) ) {
            featureLabelsName = PersistenceUtil.GetEntityName( "feature-labels" );
            PersistenceUtil.CreateEntity( featureLabelsName, FeatureLabelsCorrelationEntity.ENTITY_TYPE, n.getName(), topLayerName ); // 2nd, class region updates after first to get its feedback

            PersistenceUtil.CreateEntity( activityImageDecoderName, DecoderEntity.ENTITY_TYPE, n.getName(), featureLabelsName );
            PersistenceUtil.CreateEntity( predictedImageDecoderName, DecoderEntity.ENTITY_TYPE, n.getName(), featureLabelsName );

            PersistenceUtil.CreateEntity( featureLabelsSeriesPredictedName, ValueSeriesEntity.ENTITY_TYPE, n.getName(), featureLabelsName ); // 2nd, class region updates after first to get its feedback
            PersistenceUtil.CreateEntity( featureLabelsSeriesErrorName, ValueSeriesEntity.ENTITY_TYPE, n.getName(), featureLabelsName ); // 2nd, class region updates after first to get its feedback
            PersistenceUtil.CreateEntity( featureLabelsSeriesTruthName, ValueSeriesEntity.ENTITY_TYPE, n.getName(), featureLabelsName ); // 2nd, class region updates after first to get its feedback
        }

        String svmEntityName = null;
        if (learningEntitiesAnalyticsTypes.contains( LearningEntitiesAnalyticsType.SvmEntity ) ) {
            svmEntityName = PersistenceUtil.GetEntityName( "svm-eval" );
            PersistenceUtil.CreateEntity( svmEntityName, SupervisedBatchTrainingEntity.ENTITY_TYPE, n.getName(), topLayerName ); // 2nd, class region updates after first to get its feedback

            PersistenceUtil.CreateEntity( svmEntitySeriesPredictedName, ValueSeriesEntity.ENTITY_TYPE, n.getName(), svmEntityName ); // 2nd, class region updates after first to get its feedback
            PersistenceUtil.CreateEntity( svmEntitySeriesErrorName, ValueSeriesEntity.ENTITY_TYPE, n.getName(), svmEntityName ); // 2nd, class region updates after first to get its feedback
            PersistenceUtil.CreateEntity( svmEntitySeriesTruthName, ValueSeriesEntity.ENTITY_TYPE, n.getName(), svmEntityName ); // 2nd, class region updates after first to get its feedback

            // vector series to accumulate features over time :  input = features, output = svmEntity
            PersistenceUtil.CreateEntity( svmEntitySeriesFeatures, VectorSeriesEntity.ENTITY_TYPE, n.getName(), topLayerName );

        }



//        PersistenceUtil.CreateEntity( classRegionName, RegionLayerEntity.ENTITY_TYPE, n.getName(), topLayerName ); // 2nd, class region updates after first to get its feedback
//        PersistenceUtil.CreateEntity( classDecoderName, DecoderEntity.ENTITY_TYPE, n.getName(), classRegionName ); // produce the predicted classification for inspection by mnist next time


        // Connect the entities' data
        // a) Image to image region, and decode
        DataRefUtil.SetDataReference( imageEncoderName, EncoderEntity.DATA_INPUT, imageClassName, ImageLabelEntity.OUTPUT_IMAGE );

        DataRefUtil.SetDataReference( region1FfName, RegionLayerEntity.FF_INPUT_1, imageEncoderName, EncoderEntity.DATA_OUTPUT_ENCODED );
        DataRefUtil.SetDataReference( region1FfName, RegionLayerEntity.FF_INPUT_2, constantName, ConstantMatrixEntity.OUTPUT );
        DataRefUtil.SetDataReference( region1FfName, RegionLayerEntity.FB_INPUT, constantName, ConstantMatrixEntity.OUTPUT ); // feedback to this region is just a constant

        if( layers > 1 ) {
            DataRefUtil.SetDataReference( region2FfName, RegionLayerEntity.FF_INPUT_1, region1FfName, RegionLayerEntity.PREDICTION_FN );
            DataRefUtil.SetDataReference( region2FfName, RegionLayerEntity.FF_INPUT_2, constantName, ConstantMatrixEntity.OUTPUT );
            DataRefUtil.SetDataReference( region2FfName, RegionLayerEntity.FB_INPUT, constantName, ConstantMatrixEntity.OUTPUT ); // feedback to this region is just a constant
            learningEntitiesAlgorithm = learningEntitiesAlgorithm + "," + region2FfName;
        }

        if( layers > 2 ) {
            DataRefUtil.SetDataReference( region3FfName, RegionLayerEntity.FF_INPUT_1, region2FfName, RegionLayerEntity.PREDICTION_FN );
            DataRefUtil.SetDataReference( region3FfName, RegionLayerEntity.FF_INPUT_2, constantName, ConstantMatrixEntity.OUTPUT );
// Option to provide class-label directly to layer 3 for inclusion in classification
//            DataRefUtil.SetDataReference( region3FfName, RegionLayerEntity.FF_INPUT_2, labelEncoderName, EncoderEntity.DATA_OUTPUT_ENCODED );
            DataRefUtil.SetDataReference( region3FfName, RegionLayerEntity.FB_INPUT, constantName, ConstantMatrixEntity.OUTPUT ); // feedback to this region is just a constant
            learningEntitiesAlgorithm = learningEntitiesAlgorithm + "," + region3FfName;
        }

        DataRefUtil.SetDataReference( activityImageDecoderName, DecoderEntity.DATA_INPUT_ENCODED, region1FfName, RegionLayerEntity.FB_OUTPUT_1_UNFOLDED_ACTIVITY );
        DataRefUtil.SetDataReference( predictedImageDecoderName, DecoderEntity.DATA_INPUT_ENCODED, region1FfName, RegionLayerEntity.FB_OUTPUT_1_UNFOLDED_PREDICTION );

        // a) Class to class region, and decode
//        DataRefUtil.SetDataReference( labelEncoderName, EncoderEntity.DATA_INPUT, mnistName, MnistEntity.OUTPUT_IMAGE_LABEL );
//        DataRefUtil.SetDataReference( classEncoderName, EncoderEntity.DATA_INPUT, mnistName, MnistEntity.OUTPUT_CLASSIFICATION );
//        DataRefUtil.SetDataReference( classRegionName, RegionLayerEntity.FF_INPUT_1, classEncoderName, EncoderEntity.DATA_OUTPUT_ENCODED );
//        DataRefUtil.SetDataReference( classRegionName, RegionLayerEntity.FF_INPUT_2, constantName, ConstantMatrixEntity.OUTPUT );
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
            DataRefUtil.SetDataReferences( featureLabelsName, SupervisedLearningEntity.INPUT_FEATURES, featureDatas ); // get current state from the region to be used to predict
        }

        if ( learningEntitiesAnalyticsTypes.contains( LearningEntitiesAnalyticsType.SvmEntity ) ) {
            // get current state from the region to be used to predict
            DataRefUtil.SetDataReferences( svmEntityName, SupervisedLearningEntity.INPUT_FEATURES, featureDatas );

            // accumulate data set (X) for input to SVM for training
            DataRefUtil.SetDataReferences( svmEntitySeriesFeatures, VectorSeriesEntity.INPUT, featureDatas );
            DataRefUtil.SetDataReference( svmEntityName, SupervisedLearningEntity.INPUT_FEATURES, svmEntitySeriesFeatures, VectorSeriesEntity.OUTPUT );  // connect it to the SVM

            // TO COMMENT DAVE: This value series is an input and an output .... possible?

            // have access to the labels (class truth) vector (y)
            DataRefUtil.SetDataReference( svmEntityName, SupervisedLearningEntity.OUTPUT_LABELS_TRUTH, svmEntitySeriesTruthName, ValueSeriesEntity.OUTPUT );
        }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//        // Special test of accuracy in static classification:
//        referenceEntitySuffixes.add( new AbstractPair< String, String >( region3FfName, RegionLayerEntity.ACTIVITY_1 ) );
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

//        DataRefUtil.SetDataReferences( classRegionName, RegionLayerEntity.FB_INPUT, referenceEntitySuffixes ); // get current state from the region to be used to predict
//        DataRefUtil.SetDataReference( classDecoderName, DecoderEntity.DATA_INPUT_ENCODED, classRegionName, RegionLayerEntity.FB_OUTPUT_1_UNFOLDED_PREDICTION ); // the prediction of the next state
//        DataRefUtil.SetDataReference( mnistName, MnistEntity.INPUT_CLASSIFICATION, classDecoderName, DecoderEntity.DATA_OUTPUT_DECODED ); // the (decoded) prediction of the next state

        // Experiment config
        if( !terminateByAge ) {
            PersistenceUtil.SetConfig( experimentName, "terminationEntityName", imageClassName );
            PersistenceUtil.SetConfig( experimentName, "terminationConfigPath", "terminate" );
            PersistenceUtil.SetConfig( experimentName, "terminationAge", "-1" ); // wait for mnist to decide
        } else {
            PersistenceUtil.SetConfig( experimentName, "terminationAge", String.valueOf( terminationAge ) ); // fixed steps
        }

        // Mnist config
        PersistenceUtil.SetConfig( imageClassName, "receptiveField.receptiveFieldX", "0" );
        PersistenceUtil.SetConfig( imageClassName, "receptiveField.receptiveFieldY", "0" );
        PersistenceUtil.SetConfig( imageClassName, "receptiveField.receptiveFieldW", "28" );
        PersistenceUtil.SetConfig( imageClassName, "receptiveField.receptiveFieldH", "28" );
        PersistenceUtil.SetConfig( imageClassName, "resolution.resolutionX", "28" );
        PersistenceUtil.SetConfig( imageClassName, "resolution.resolutionY", "28" );
        PersistenceUtil.SetConfig( imageClassName, "greyscale", "true" );
        PersistenceUtil.SetConfig( imageClassName, "invert", "true" );
        PersistenceUtil.SetConfig( imageClassName, "sourceType", BufferedImageSourceFactory.TYPE_IMAGE_FILES );
        PersistenceUtil.SetConfig( imageClassName, "sourceFilesPrefix", "postproc" );
        PersistenceUtil.SetConfig( imageClassName, "sourceFilesPathTraining", trainingPath );
        PersistenceUtil.SetConfig( imageClassName, "sourceFilesPathTesting", testingPath );
        PersistenceUtil.SetConfig( imageClassName, "trainingBatches", String.valueOf( trainingBatches ) );

        PersistenceUtil.SetConfig( imageClassName, "learningEntitiesAlgorithm", String.valueOf( learningEntitiesAlgorithm ) );

        if ( learningEntitiesAnalyticsTypes.contains( LearningEntitiesAnalyticsType.LabelFeatures ) ) {
            PersistenceUtil.SetConfig( imageClassName, "learningEntitiesAnalytics", String.valueOf( featureLabelsName ) );
        }

        if ( learningEntitiesAnalyticsTypes.contains( LearningEntitiesAnalyticsType.SvmEntity ) ) {
            String configVal = PersistenceUtil.GetConfig( imageClassName, "learningEntitiesAnalytics" );

            String newConfig = null;
            if ( configVal != null ) {
                newConfig = configVal + "," + svmEntityName;
            }
            else {
                newConfig = svmEntityName;
            }

            PersistenceUtil.SetConfig( imageClassName, "learningEntitiesAnalytics", String.valueOf( newConfig ) );
        }

        // constant config
        if( encodeZero ) {

            // image encoder config
            PersistenceUtil.SetConfig( imageEncoderName, "density", "1" );
            PersistenceUtil.SetConfig( imageEncoderName, "bits", "2" );
            PersistenceUtil.SetConfig( imageEncoderName, "encodeZero", "true" );

            // image decoder config x2
            PersistenceUtil.SetConfig( activityImageDecoderName, "density", "1" );
            PersistenceUtil.SetConfig( activityImageDecoderName, "bits", "2" );
            PersistenceUtil.SetConfig( activityImageDecoderName, "encodeZero", "true" );

            PersistenceUtil.SetConfig( predictedImageDecoderName, "density", "1" );
            PersistenceUtil.SetConfig( predictedImageDecoderName, "bits", "2" );
            PersistenceUtil.SetConfig( predictedImageDecoderName, "encodeZero", "true" );
        } else {

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
        }

//        // class encoder config
//        PersistenceUtil.SetConfig( labelEncoderName, "encoderType", NumberEncoder.class.getSimpleName() );
//        PersistenceUtil.SetConfig( labelEncoderName, "digits", "2" );
//        PersistenceUtil.SetConfig( labelEncoderName, "numbers", "1" );
//
//        // class encoder config
//        PersistenceUtil.SetConfig( classEncoderName, "encoderType", NumberEncoder.class.getSimpleName() );
//        PersistenceUtil.SetConfig( classEncoderName, "digits", "2" );
//        PersistenceUtil.SetConfig( classEncoderName, "numbers", "1" );
//
//        // class decoder config
//        PersistenceUtil.SetConfig( classDecoderName, "encoderType", NumberEncoder.class.getSimpleName() );
//        PersistenceUtil.SetConfig( classDecoderName, "digits", "2" );
//        PersistenceUtil.SetConfig( classDecoderName, "numbers", "1" );

        if ( learningEntitiesAnalyticsTypes.contains( LearningEntitiesAnalyticsType.LabelFeatures ) ) {
            // feature-class config
            PersistenceUtil.SetConfig( featureLabelsName, "classEntityName", imageClassName );
            PersistenceUtil.SetConfig( featureLabelsName, "classConfigPath", "imageClass" );
            PersistenceUtil.SetConfig( featureLabelsName, "classes", "10" );
            PersistenceUtil.SetConfig( featureLabelsName, "onlineLearning", String.valueOf( classFeaturesOnline ) );
            PersistenceUtil.SetConfig( featureLabelsName, "onlineLearningRate", "0.001" );

            PersistenceUtil.SetConfig( featureLabelsSeriesPredictedName, "period", "-1" ); // log forever
            PersistenceUtil.SetConfig( featureLabelsSeriesErrorName, "period", "-1" );
            PersistenceUtil.SetConfig( featureLabelsSeriesTruthName, "period", "-1" );
        }

        if ( learningEntitiesAnalyticsTypes.contains( LearningEntitiesAnalyticsType.SvmEntity ) ) {
            // svm config
            PersistenceUtil.SetConfig( svmEntityName, "classEntityName", imageClassName );
            PersistenceUtil.SetConfig( svmEntityName, "classConfigPath", "imageClass" );
            PersistenceUtil.SetConfig( svmEntityName, "classes", "10" );
            PersistenceUtil.SetConfig( svmEntityName, "onlineLearning", String.valueOf( false ) );
            PersistenceUtil.SetConfig( svmEntityName, "onlineLearningRate", "0.0" );
        }


        if ( learningEntitiesAnalyticsTypes.contains( LearningEntitiesAnalyticsType.LabelFeatures ) ) {
            PersistenceUtil.SetConfig( featureLabelsSeriesPredictedName, "entityName", featureLabelsName );
            PersistenceUtil.SetConfig( featureLabelsSeriesErrorName, "entityName", featureLabelsName );
            PersistenceUtil.SetConfig( featureLabelsSeriesTruthName, "entityName", featureLabelsName );

            PersistenceUtil.SetConfig( featureLabelsSeriesPredictedName, "configPath", "classPredicted" );
            PersistenceUtil.SetConfig( featureLabelsSeriesErrorName, "configPath", "classError" );
            PersistenceUtil.SetConfig( featureLabelsSeriesTruthName, "configPath", "classTruth" );
        }

        if ( learningEntitiesAnalyticsTypes.contains( LearningEntitiesAnalyticsType.SvmEntity ) ) {
            PersistenceUtil.SetConfig( svmEntitySeriesPredictedName, "entityName", svmEntityName );
            PersistenceUtil.SetConfig( svmEntitySeriesErrorName, "entityName", svmEntityName );
            PersistenceUtil.SetConfig( svmEntitySeriesTruthName, "entityName", svmEntityName );

            PersistenceUtil.SetConfig( svmEntitySeriesPredictedName, "configPath", "classPredicted" );
            PersistenceUtil.SetConfig( svmEntitySeriesErrorName, "configPath", "classError" );
            PersistenceUtil.SetConfig( svmEntitySeriesTruthName, "configPath", "classTruth" );
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
//        PersistenceUtil.SetConfig( classRegionName, "predictorLearningRate", "100" );
        PersistenceUtil.SetConfig( classRegionName, "predictorLearningRate", "500" );
        PersistenceUtil.SetConfig( classRegionName, "receptiveFieldsTrainingSamples", "0.1" );
        PersistenceUtil.SetConfig( classRegionName, "classifiersPerBit", "5" );

//        PersistenceUtil.SetConfig( classRegionName, "organizerStressThreshold", "0.0" );
//        PersistenceUtil.SetConfig( classRegionName, "organizerGrowthInterval", "1" );
//        PersistenceUtil.SetConfig( classRegionName, "organizerEdgeMaxAge", "1000" );
//        PersistenceUtil.SetConfig( classRegionName, "organizerNoiseMagnitude", "0.0" );
////        PersistenceUtil.SetConfig( classRegionName, "organizerLearningRate", "0.002" );
//        PersistenceUtil.SetConfig( classRegionName, "organizerLearningRate", "0.02" );
//        PersistenceUtil.SetConfig( classRegionName, "organizerElasticity", "1.5" );
//        PersistenceUtil.SetConfig( classRegionName, "organizerLearningRateNeighbours", "0.001" );
        PersistenceUtil.SetConfig( classRegionName, "organizerWidthCells", "2" );
        PersistenceUtil.SetConfig( classRegionName, "organizerHeightCells", "2" );
        PersistenceUtil.SetConfig( classRegionName, "organizerNeighbourhoodRange", "2" );

        PersistenceUtil.SetConfig( classRegionName, "organizerIntervalsInput1X", "2" );
        PersistenceUtil.SetConfig( classRegionName, "organizerIntervalsInput2X", "1" );
        PersistenceUtil.SetConfig( classRegionName, "organizerIntervalsInput1Y", "2" );
        PersistenceUtil.SetConfig( classRegionName, "organizerIntervalsInput2Y", "1" );

//        PersistenceUtil.SetConfig( classRegionName, "classifierWidthCells", "4" );
//        PersistenceUtil.SetConfig( classRegionName, "classifierHeightCells", "4" );
        PersistenceUtil.SetConfig( classRegionName, "classifierWidthCells", "5" );
        PersistenceUtil.SetConfig( classRegionName, "classifierHeightCells", "5" );
        PersistenceUtil.SetConfig( classRegionName, "classifierDepthCells", "1" );

        PersistenceUtil.SetConfig( classRegionName, "classifierStressThreshold", "0.1" );
        PersistenceUtil.SetConfig( classRegionName, "classifierGrowthInterval", "120" );
//        PersistenceUtil.SetConfig( classRegionName, "classifierEdgeMaxAge", "100" );
//        PersistenceUtil.SetConfig( classRegionName, "classifierGrowthInterval", "120" );
//        PersistenceUtil.SetConfig( classRegionName, "classifierEdgeMaxAge", "250" );
//        PersistenceUtil.SetConfig( classRegionName, "classifierEdgeMaxAge", "400" );
        PersistenceUtil.SetConfig( classRegionName, "classifierEdgeMaxAge", "600" );
        PersistenceUtil.SetConfig( classRegionName, "classifierGrowthInterval", "120" );

        PersistenceUtil.SetConfig( classRegionName, "classifierLearningRate", "0.1" );
        PersistenceUtil.SetConfig( classRegionName, "classifierLearningRateNeighbours", "0.005" );
        PersistenceUtil.SetConfig( classRegionName, "classifierStressLearningRate", "0.1" );
        PersistenceUtil.SetConfig( classRegionName, "classifierStressSplitLearningRate", "0.5" );
        PersistenceUtil.SetConfig( classRegionName, "classifierNoiseMagnitude", "0.1" );
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

        PersistenceUtil.SetConfig( regionLayerName, "emitUnchangedCells", String.valueOf( emitUnchangedCells ) );
        PersistenceUtil.SetConfig( regionLayerName, "defaultPredictionInhibition", String.valueOf( defaultPredictionInhibition ) );
        PersistenceUtil.SetConfig( regionLayerName, "classifiersPerBit1", String.valueOf( classifiersPerBit1 ) );
        PersistenceUtil.SetConfig( regionLayerName, "classifiersPerBit2", String.valueOf( classifiersPerBit2 ) );

        PersistenceUtil.SetConfig( regionLayerName, "organizerWidthCells", String.valueOf( organizerWidth ) );
        PersistenceUtil.SetConfig( regionLayerName, "organizerHeightCells", String.valueOf( organizerHeight ) );
        PersistenceUtil.SetConfig( regionLayerName, "organizerIntervalsInput1X", String.valueOf( organizerWidth ) );
        PersistenceUtil.SetConfig( regionLayerName, "organizerIntervalsInput2X", "1" ); // because no 2nd input.
        PersistenceUtil.SetConfig( regionLayerName, "organizerIntervalsInput1Y", String.valueOf( organizerHeight ) );
        PersistenceUtil.SetConfig( regionLayerName, "organizerIntervalsInput2Y", "1" ); // because no 2nd input.

        PersistenceUtil.SetConfig( regionLayerName, "classifierWidthCells", String.valueOf( classifierWidth ) );
        PersistenceUtil.SetConfig( regionLayerName, "classifierHeightCells", String.valueOf( classifierHeight ) );
        PersistenceUtil.SetConfig( regionLayerName, "classifierDepthCells", String.valueOf( classifierDepth ) );

        PersistenceUtil.SetConfig( regionLayerName, "classifierStressLearningRate", String.valueOf( classifierStressLearningRate ) );
        PersistenceUtil.SetConfig( regionLayerName, "classifierRankLearningRate", String.valueOf( classifierRankLearningRate ) );
        PersistenceUtil.SetConfig( regionLayerName, "classifierRankScale", String.valueOf( classifierRankScale ) );
        PersistenceUtil.SetConfig( regionLayerName, "classifierAgeMax", String.valueOf( classifierAgeMax ) );
        PersistenceUtil.SetConfig( regionLayerName, "classifierAgeDecay", String.valueOf( classifierAgeDecay ) );
        PersistenceUtil.SetConfig( regionLayerName, "classifierAgeScale", String.valueOf( classifierAgeScale ) );

        PersistenceUtil.SetConfig( regionLayerName, "predictorLearningRate", String.valueOf( predictorLearningRate ) );
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

//        PersistenceUtil.SetConfig( regionLayerName, "organizerTrainOnChange", String.valueOf( organizerTrainOnChange ) );
        PersistenceUtil.SetConfig( regionLayerName, "emitUnchangedCells", String.valueOf( emitUnchangedCells ) );
        PersistenceUtil.SetConfig( regionLayerName, "predictorLearningRate", String.valueOf( predictorLearningRate ) );
        PersistenceUtil.SetConfig( regionLayerName, "receptiveFieldsTrainingSamples", "0.1" );
        PersistenceUtil.SetConfig( regionLayerName, "defaultPredictionInhibition", String.valueOf( defaultPredictionInhibition ) );

        PersistenceUtil.SetConfig( regionLayerName, "classifiersPerBit1", String.valueOf( classifiersPerBit1 ) );
        PersistenceUtil.SetConfig( regionLayerName, "classifiersPerBit2", String.valueOf( classifiersPerBit2 ) );

        PersistenceUtil.SetConfig( regionLayerName, "organizerWidthCells", String.valueOf( organizerWidth ) );//"8" );
        PersistenceUtil.SetConfig( regionLayerName, "organizerHeightCells", String.valueOf( organizerHeight ) );//"8" );

        PersistenceUtil.SetConfig( regionLayerName, "organizerIntervalsInput1X", String.valueOf( organizerWidth ) );
        PersistenceUtil.SetConfig( regionLayerName, "organizerIntervalsInput2X", "1" ); // because no 2nd input.
        PersistenceUtil.SetConfig( regionLayerName, "organizerIntervalsInput1Y", String.valueOf( organizerHeight ) );
        PersistenceUtil.SetConfig( regionLayerName, "organizerIntervalsInput2Y", "1" ); // because no 2nd input.

        PersistenceUtil.SetConfig( regionLayerName, "classifierWidthCells", String.valueOf( classifierWidth ) );//"5" );
        PersistenceUtil.SetConfig( regionLayerName, "classifierHeightCells", String.valueOf( classifierHeight ) );//"6" );
        PersistenceUtil.SetConfig( regionLayerName, "classifierDepthCells", String.valueOf( classifierDepth ) );
        PersistenceUtil.SetConfig( regionLayerName, "classifierStressThreshold", "0.0" ); // use all cells

//        PersistenceUtil.SetConfig( regionLayerName, "classifierLearningRate", "0.05" );
//        PersistenceUtil.SetConfig( regionLayerName, "classifierLearningRate", "0.01" );
        PersistenceUtil.SetConfig( regionLayerName, "classifierLearningRate", "0.03" );
        PersistenceUtil.SetConfig( regionLayerName, "classifierLearningRateNeighbours", "0.001" );
        PersistenceUtil.SetConfig( regionLayerName, "classifierStressLearningRate", "0.001" );
        PersistenceUtil.SetConfig( regionLayerName, "classifierStressSplitLearningRate", "0.5" );

        PersistenceUtil.SetConfig( regionLayerName, "classifierGrowthInterval", String.valueOf( classifierGrowthInterval ) );
        PersistenceUtil.SetConfig( regionLayerName, "classifierEdgeMaxAge", String.valueOf( edgeMaxAge ) );//"600" );
//        PersistenceUtil.SetConfig( regionLayerName, "classifierNoiseMagnitude", "0.1" );
    }


}
