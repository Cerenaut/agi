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
 * Created by dave on 23/10/16.
 */
public class MnistHQCLDemo {
/*
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

    public static void createEntities( Node n ) {

//        String trainingPath = "/home/dave/workspace/agi.io/data/mnist/cycle10";
//        String testingPath = "/home/dave/workspace/agi.io/data/mnist/cycle3";
        String trainingPath = "/home/dave/workspace/agi.io/data/mnist/10k_train";
        String testingPath = "/home/dave/workspace/agi.io/data/mnist/5k_test";
//        String trainingPath = "./data/training";
//        String testingPath = "./data/testing";
        int terminationAge = 25;//000;
        int trainingBatches = 3;
        boolean terminateByAge = false;
        boolean encodeZero = false;
        boolean classFeaturesOnline = false;
        int layers = 3;

        // Define some entities' names
        String experimentName            = PersistenceUtil.GetEntityName( "experiment" );
        String imageClassName            = PersistenceUtil.GetEntityName( "image-class" );
        String imageEncoderName          = PersistenceUtil.GetEntityName( "image-encoder" );
        String constantName              = PersistenceUtil.GetEntityName( "constant" );
        String region1FfName             = PersistenceUtil.GetEntityName( "image-region-1-ff" );
        String region2FfName             = PersistenceUtil.GetEntityName( "image-region-2-ff" );
        String region3FfName             = PersistenceUtil.GetEntityName( "image-region-3-ff" );
        String classFeaturesName         = PersistenceUtil.GetEntityName( "class-features" );
//        String activityImageDecoderName  = PersistenceUtil.GetEntityName( "activity-image-decoder" );
//        String predictedImageDecoderName = PersistenceUtil.GetEntityName( "predicted-image-decoder" );
        String valueSeriesPredictedName  = PersistenceUtil.GetEntityName( "value-series-predicted" );
        String valueSeriesErrorName      = PersistenceUtil.GetEntityName( "value-series-error" );
        String valueSeriesTruthName      = PersistenceUtil.GetEntityName( "value-series-truth" );

        // Create Entities
        PersistenceUtil.CreateEntity( experimentName, ExperimentEntity.ENTITY_TYPE, n.getName(), null ); // experiment is the root entity
        PersistenceUtil.CreateEntity( imageClassName, ImageLabelEntity.ENTITY_TYPE, n.getName(), experimentName );
        PersistenceUtil.CreateEntity( imageEncoderName, EncoderEntity.ENTITY_TYPE, n.getName(), imageClassName );
        PersistenceUtil.CreateEntity( constantName, ConstantMatrixEntity.ENTITY_TYPE, n.getName(), imageEncoderName ); // ok all input to the regions is ready

        PersistenceUtil.CreateEntity( region1FfName, HqClRegionLayerEntity.ENTITY_TYPE, n.getName(), constantName );
        String topLayerName = region1FfName;
        if( layers > 1 ) {
            PersistenceUtil.CreateEntity( region2FfName, HqClRegionLayerEntity.ENTITY_TYPE, n.getName(), region1FfName );
            topLayerName = region2FfName;
        }
        if( layers > 2 ) {
            PersistenceUtil.CreateEntity( region3FfName, HqClRegionLayerEntity.ENTITY_TYPE, n.getName(), region2FfName );
            topLayerName = region3FfName;
        }

        PersistenceUtil.CreateEntity( classFeaturesName, FeatureLabelsCorrelationEntity.ENTITY_TYPE, n.getName(), topLayerName ); // 2nd, class region updates after first to get its feedback
//        PersistenceUtil.CreateEntity( activityImageDecoderName, DecoderEntity.ENTITY_TYPE, n.getName(), classFeaturesName );
//        PersistenceUtil.CreateEntity( predictedImageDecoderName, DecoderEntity.ENTITY_TYPE, n.getName(), classFeaturesName );

        PersistenceUtil.CreateEntity( valueSeriesPredictedName, ValueSeriesEntity.ENTITY_TYPE, n.getName(), classFeaturesName ); // 2nd, class region updates after first to get its feedback
        PersistenceUtil.CreateEntity( valueSeriesErrorName, ValueSeriesEntity.ENTITY_TYPE, n.getName(), classFeaturesName ); // 2nd, class region updates after first to get its feedback
        PersistenceUtil.CreateEntity( valueSeriesTruthName, ValueSeriesEntity.ENTITY_TYPE, n.getName(), classFeaturesName ); // 2nd, class region updates after first to get its feedback


        // Connect the entities' data
        // a) Image to image region, and decode
        DataRefUtil.SetDataReference( imageEncoderName, EncoderEntity.DATA_INPUT, imageClassName, ImageLabelEntity.OUTPUT_IMAGE );

        DataRefUtil.SetDataReference( region1FfName, HqClRegionLayerEntity.INPUT_FF_1, imageEncoderName, EncoderEntity.DATA_OUTPUT_ENCODED );
        DataRefUtil.SetDataReference( region1FfName, HqClRegionLayerEntity.INPUT_FF_2, constantName, ConstantMatrixEntity.OUTPUT );
        DataRefUtil.SetDataReference( region1FfName, HqClRegionLayerEntity.INPUT_FB_1, constantName, ConstantMatrixEntity.OUTPUT ); // feedback to this region is just a constant

        String learningEntitiesAlgorithm = region1FfName;
        String regionLayerFfOutputSuffix = HqClRegionLayerEntity.REGION_ACTIVITY;
//        String regionLayerFfOutputSuffix = HqClRegionLayerEntity.REGION_ACTIVITY_INFERRED;
        String regionLayerFbOutputSuffix = HqClRegionLayerEntity.REGION_ACTIVITY_INFERRED;

        if( layers > 1 ) {
            DataRefUtil.SetDataReference( region2FfName, HqClRegionLayerEntity.INPUT_FF_1, region1FfName, regionLayerFfOutputSuffix );
            DataRefUtil.SetDataReference( region2FfName, HqClRegionLayerEntity.INPUT_FF_2, constantName, ConstantMatrixEntity.OUTPUT );
            DataRefUtil.SetDataReference( region2FfName, HqClRegionLayerEntity.INPUT_FB_1, constantName, ConstantMatrixEntity.OUTPUT ); // feedback to this region is just a constant
            learningEntitiesAlgorithm = learningEntitiesAlgorithm + "," + region2FfName;
        }

        if( layers > 2 ) {
            DataRefUtil.SetDataReference( region3FfName, HqClRegionLayerEntity.INPUT_FF_1, region2FfName, regionLayerFfOutputSuffix );
            DataRefUtil.SetDataReference( region3FfName, HqClRegionLayerEntity.INPUT_FF_2, constantName, ConstantMatrixEntity.OUTPUT );
// Option to provide class-label directly to layer 3 for inclusion in classification
//            DataRefUtil.SetDataReference( region3FfName, HqClRegionLayerEntity.FF_INPUT_2, labelEncoderName, EncoderEntity.DATA_OUTPUT_ENCODED );
            DataRefUtil.SetDataReference( region3FfName, HqClRegionLayerEntity.INPUT_FB_1, constantName, ConstantMatrixEntity.OUTPUT ); // feedback to this region is just a constant
            learningEntitiesAlgorithm = learningEntitiesAlgorithm + "," + region3FfName;
        }

//        DataRefUtil.SetDataReference( activityImageDecoderName, DecoderEntity.DATA_INPUT_ENCODED, region1FfName, RegionLayerEntity.FB_OUTPUT_1_UNFOLDED_ACTIVITY );
//        DataRefUtil.SetDataReference( predictedImageDecoderName, DecoderEntity.DATA_INPUT_ENCODED, region1FfName, RegionLayerEntity.FB_OUTPUT_1_UNFOLDED_PREDICTION );

        // a) Class to class region, and decode
        ArrayList< AbstractPair< String, String > > featureDatas = new ArrayList< AbstractPair< String, String > >();
        //featureDatas.add( new AbstractPair< String, String >( region1FfName, regionLayerOutputSuffix ) );
//        featureDatas.add( new AbstractPair< String, String >( region2FfName, regionLayerOutputSuffix ) );
        if( layers == 1 ) featureDatas.add( new AbstractPair< String, String >( region1FfName, regionLayerFbOutputSuffix ) );
        if( layers == 2 ) featureDatas.add( new AbstractPair< String, String >( region2FfName, regionLayerFbOutputSuffix ) );
        if( layers == 3 ) featureDatas.add( new AbstractPair< String, String >( region3FfName, regionLayerFbOutputSuffix ) );
        DataRefUtil.SetDataReferences( classFeaturesName, SupervisedLearningEntity.INPUT_FEATURES, featureDatas ); // get current state from the region to be used to predict

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

        String learningEntitiesAnalytics = classFeaturesName;
        PersistenceUtil.SetConfig( imageClassName, "learningEntitiesAlgorithm", String.valueOf( learningEntitiesAlgorithm ) );
        PersistenceUtil.SetConfig( imageClassName, "learningEntitiesAnalytics", String.valueOf( learningEntitiesAnalytics ) );

        // image encoder config
        if( encodeZero ) {
            PersistenceUtil.SetConfig( imageEncoderName, "density", "1" );
            PersistenceUtil.SetConfig( imageEncoderName, "bits", "2" );
            PersistenceUtil.SetConfig( imageEncoderName, "encodeZero", "true" );
        } else {
            PersistenceUtil.SetConfig( imageEncoderName, "density", "1" );
            PersistenceUtil.SetConfig( imageEncoderName, "bits", "1" );
            PersistenceUtil.SetConfig( imageEncoderName, "encodeZero", "false" );
        }

        // feature-class config
        PersistenceUtil.SetConfig( classFeaturesName, "classEntityName", imageClassName );
        PersistenceUtil.SetConfig( classFeaturesName, "classConfigPath", "imageClass" );
        PersistenceUtil.SetConfig( classFeaturesName, "classes", "10" );
        PersistenceUtil.SetConfig( classFeaturesName, "onlineLearning", String.valueOf( classFeaturesOnline ) );
        PersistenceUtil.SetConfig( classFeaturesName, "onlineLearningRate", "0.001" );

        // data series logging
        PersistenceUtil.SetConfig( valueSeriesPredictedName, "period", "-1" ); // log forever
        PersistenceUtil.SetConfig( valueSeriesErrorName, "period", "-1" );
        PersistenceUtil.SetConfig( valueSeriesTruthName, "period", "-1" );

        PersistenceUtil.SetConfig( valueSeriesPredictedName, "entityName", classFeaturesName );
        PersistenceUtil.SetConfig( valueSeriesErrorName, "entityName", classFeaturesName );
        PersistenceUtil.SetConfig( valueSeriesTruthName, "entityName", classFeaturesName );

        PersistenceUtil.SetConfig( valueSeriesPredictedName, "configPath", "classPredicted" );
        PersistenceUtil.SetConfig( valueSeriesErrorName, "configPath", "classError" );
        PersistenceUtil.SetConfig( valueSeriesTruthName, "configPath", "classTruth" );

        // image region config
        // 5*5 * 8*8 = 1600 cells
        int errorHistoryLength = 30; // not used
        int organizerWidth = 0;
        int organizerHeight = 0;
        int classifierWidth = 0;
        int classifierHeight = 0;
        int classifiersPerBit1 = 0;
        int classifiersPerBit2 = 1;
        float predictionLearningRate = 0.05f;
        float predictionDecayRate = 0.98f; // slower
        int edgeMaxAge = 500;
        int classifierGrowthInterval = 120;

        // GNG region layer:
        organizerWidth = 8;
        organizerHeight = 8; // 8x8 = 64 bits
        classifiersPerBit1 = 3; // relatively local
        classifierWidth = 6;
        classifierHeight = 6; // 8x8x6x6 = 2304 cells

        if( layers > 0 ) {
            setRegionLayerConfig( region1FfName,
                    classifiersPerBit1, classifiersPerBit2, classifierWidth, classifierHeight, organizerWidth, organizerHeight, edgeMaxAge, predictionLearningRate, predictionDecayRate, errorHistoryLength, classifierGrowthInterval );
        }

        organizerWidth = 8;
        organizerHeight = 8;
        classifiersPerBit1 = 5;
        classifierWidth = 8;
        classifierHeight = 8;

        if( layers > 1 ) {
            setRegionLayerConfig(
                    region2FfName,
                    classifiersPerBit1, classifiersPerBit2, classifierWidth, classifierHeight, organizerWidth, organizerHeight, edgeMaxAge, predictionLearningRate, predictionDecayRate, errorHistoryLength, classifierGrowthInterval );
        }

        // apex
        organizerWidth = 6; // maybe too ambitious?  8 -> 6 -> 3
        organizerHeight = 6;
        classifiersPerBit1 = 9;
        classifierWidth = 12;
        classifierHeight = 12; // 144 cells each

        if( layers > 2 ) {
            setRegionLayerConfig(
                    region3FfName,
                    classifiersPerBit1, classifiersPerBit2, classifierWidth, classifierHeight, organizerWidth, organizerHeight, edgeMaxAge, predictionLearningRate, predictionDecayRate, errorHistoryLength, classifierGrowthInterval );
        }

    }

    public static void setRegionLayerConfig(
            String regionLayerName,
            int classifiersPerBit1,
            int classifiersPerBit2,
            int classifierWidth,
            int classifierHeight,
            int organizerWidth,
            int organizerHeight,
            int edgeMaxAge,
            float predictionLearningRate,
            float predictionDecayRate,
            int errorHistoryLength,
            int classifierGrowthInterval ) {

        PersistenceUtil.SetConfig( regionLayerName, "columnWidthCells", String.valueOf( classifierWidth ) );//"5" );
        PersistenceUtil.SetConfig( regionLayerName, "columnHeightCells", String.valueOf( classifierHeight ) );//"6" );

        PersistenceUtil.SetConfig( regionLayerName, "regionWidthColumns", String.valueOf( organizerWidth ) );//"8" );
        PersistenceUtil.SetConfig( regionLayerName, "regionHeightColumns", String.valueOf( organizerHeight ) );//"8" );

        PersistenceUtil.SetConfig( regionLayerName, "intervalsX1", String.valueOf( organizerWidth ) );
        PersistenceUtil.SetConfig( regionLayerName, "intervalsY1", String.valueOf( organizerHeight ) );
        PersistenceUtil.SetConfig( regionLayerName, "intervalsX2", "1" ); // because no 2nd input.
        PersistenceUtil.SetConfig( regionLayerName, "intervalsY2", "1" ); // because no 2nd input.

        PersistenceUtil.SetConfig( regionLayerName, "predictionLearningRate", String.valueOf( predictionLearningRate ) );
        PersistenceUtil.SetConfig( regionLayerName, "predictionDecayRate", String.valueOf( predictionDecayRate ) );
        PersistenceUtil.SetConfig( regionLayerName, "errorHistoryLength", String.valueOf( errorHistoryLength ) );

        PersistenceUtil.SetConfig( regionLayerName, "classifiersPerBit1", String.valueOf( classifiersPerBit1 ) );
        PersistenceUtil.SetConfig( regionLayerName, "classifiersPerBit2", String.valueOf( classifiersPerBit2 ) );

        PersistenceUtil.SetConfig( regionLayerName, "classifierLearningRate", "0.03" );
        PersistenceUtil.SetConfig( regionLayerName, "classifierLearningRateNeighbours", "0.001" );
        PersistenceUtil.SetConfig( regionLayerName, "classifierNoiseMagnitude", "0.0" );
        PersistenceUtil.SetConfig( regionLayerName, "classifierEdgeMaxAge", String.valueOf( edgeMaxAge ) );
        PersistenceUtil.SetConfig( regionLayerName, "classifierStressLearningRate", "0.001" );
        PersistenceUtil.SetConfig( regionLayerName, "classifierStressSplitLearningRate", "0.5" );
        PersistenceUtil.SetConfig( regionLayerName, "classifierStressThreshold", "0.0" ); // use all cells
        PersistenceUtil.SetConfig( regionLayerName, "classifierGrowthInterval", String.valueOf( classifierGrowthInterval ) );

    }
    */
}