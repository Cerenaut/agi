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
        String experimentName            = Framework.GetEntityName( "experiment" );
        String imageClassName            = Framework.GetEntityName( "image-class" );
        String imageEncoderName          = Framework.GetEntityName( "image-encoder" );
        String constantName              = Framework.GetEntityName( "constant" );
        String region1FfName             = Framework.GetEntityName( "image-region-1-ff" );
        String region2FfName             = Framework.GetEntityName( "image-region-2-ff" );
        String region3FfName             = Framework.GetEntityName( "image-region-3-ff" );
        String classFeaturesName         = Framework.GetEntityName( "class-features" );
//        String activityImageDecoderName  = Framework.GetEntityName( "activity-image-decoder" );
//        String predictedImageDecoderName = Framework.GetEntityName( "predicted-image-decoder" );
        String valueSeriesPredictedName  = Framework.GetEntityName( "value-series-predicted" );
        String valueSeriesErrorName      = Framework.GetEntityName( "value-series-error" );
        String valueSeriesTruthName      = Framework.GetEntityName( "value-series-truth" );

        // Create Entities
        Framework.CreateEntity( experimentName, ExperimentEntity.ENTITY_TYPE, n.getName(), null ); // experiment is the root entity
        Framework.CreateEntity( imageClassName, ImageClassEntity.ENTITY_TYPE, n.getName(), experimentName );
        Framework.CreateEntity( imageEncoderName, EncoderEntity.ENTITY_TYPE, n.getName(), imageClassName );
        Framework.CreateEntity( constantName, ConstantMatrixEntity.ENTITY_TYPE, n.getName(), imageEncoderName ); // ok all input to the regions is ready

        Framework.CreateEntity( region1FfName, HqClRegionLayerEntity.ENTITY_TYPE, n.getName(), constantName );
        String topLayerName = region1FfName;
        if( layers > 1 ) {
            Framework.CreateEntity( region2FfName, HqClRegionLayerEntity.ENTITY_TYPE, n.getName(), region1FfName );
            topLayerName = region2FfName;
        }
        if( layers > 2 ) {
            Framework.CreateEntity( region3FfName, HqClRegionLayerEntity.ENTITY_TYPE, n.getName(), region2FfName );
            topLayerName = region3FfName;
        }

        Framework.CreateEntity( classFeaturesName, ClassFeaturesEntity.ENTITY_TYPE, n.getName(), topLayerName ); // 2nd, class region updates after first to get its feedback
//        Framework.CreateEntity( activityImageDecoderName, DecoderEntity.ENTITY_TYPE, n.getName(), classFeaturesName );
//        Framework.CreateEntity( predictedImageDecoderName, DecoderEntity.ENTITY_TYPE, n.getName(), classFeaturesName );

        Framework.CreateEntity( valueSeriesPredictedName, ValueSeriesEntity.ENTITY_TYPE, n.getName(), classFeaturesName ); // 2nd, class region updates after first to get its feedback
        Framework.CreateEntity( valueSeriesErrorName, ValueSeriesEntity.ENTITY_TYPE, n.getName(), classFeaturesName ); // 2nd, class region updates after first to get its feedback
        Framework.CreateEntity( valueSeriesTruthName, ValueSeriesEntity.ENTITY_TYPE, n.getName(), classFeaturesName ); // 2nd, class region updates after first to get its feedback


        // Connect the entities' data
        // a) Image to image region, and decode
        Framework.SetDataReference( imageEncoderName, EncoderEntity.DATA_INPUT, imageClassName, ImageClassEntity.OUTPUT_IMAGE );

        Framework.SetDataReference( region1FfName, HqClRegionLayerEntity.INPUT_FF_1, imageEncoderName, EncoderEntity.DATA_OUTPUT_ENCODED );
        Framework.SetDataReference( region1FfName, HqClRegionLayerEntity.INPUT_FF_2, constantName, ConstantMatrixEntity.OUTPUT );
        Framework.SetDataReference( region1FfName, HqClRegionLayerEntity.INPUT_FB_1, constantName, ConstantMatrixEntity.OUTPUT ); // feedback to this region is just a constant

        String learningEntitiesAlgorithm = region1FfName;
        String regionLayerFfOutputSuffix = HqClRegionLayerEntity.REGION_ACTIVITY;
//        String regionLayerFfOutputSuffix = HqClRegionLayerEntity.REGION_ACTIVITY_INFERRED;
        String regionLayerFbOutputSuffix = HqClRegionLayerEntity.REGION_ACTIVITY_INFERRED;

        if( layers > 1 ) {
            Framework.SetDataReference( region2FfName, HqClRegionLayerEntity.INPUT_FF_1, region1FfName, regionLayerFfOutputSuffix );
            Framework.SetDataReference( region2FfName, HqClRegionLayerEntity.INPUT_FF_2, constantName, ConstantMatrixEntity.OUTPUT );
            Framework.SetDataReference( region2FfName, HqClRegionLayerEntity.INPUT_FB_1, constantName, ConstantMatrixEntity.OUTPUT ); // feedback to this region is just a constant
            learningEntitiesAlgorithm = learningEntitiesAlgorithm + "," + region2FfName;
        }

        if( layers > 2 ) {
            Framework.SetDataReference( region3FfName, HqClRegionLayerEntity.INPUT_FF_1, region2FfName, regionLayerFfOutputSuffix );
            Framework.SetDataReference( region3FfName, HqClRegionLayerEntity.INPUT_FF_2, constantName, ConstantMatrixEntity.OUTPUT );
// Option to provide class-label directly to layer 3 for inclusion in classification
//            Framework.SetDataReference( region3FfName, HqClRegionLayerEntity.FF_INPUT_2, labelEncoderName, EncoderEntity.DATA_OUTPUT_ENCODED );
            Framework.SetDataReference( region3FfName, HqClRegionLayerEntity.INPUT_FB_1, constantName, ConstantMatrixEntity.OUTPUT ); // feedback to this region is just a constant
            learningEntitiesAlgorithm = learningEntitiesAlgorithm + "," + region3FfName;
        }

//        Framework.SetDataReference( activityImageDecoderName, DecoderEntity.DATA_INPUT_ENCODED, region1FfName, RegionLayerEntity.FB_OUTPUT_1_UNFOLDED_ACTIVITY );
//        Framework.SetDataReference( predictedImageDecoderName, DecoderEntity.DATA_INPUT_ENCODED, region1FfName, RegionLayerEntity.FB_OUTPUT_1_UNFOLDED_PREDICTION );

        // a) Class to class region, and decode
        ArrayList< AbstractPair< String, String > > featureDatas = new ArrayList< AbstractPair< String, String > >();
        //featureDatas.add( new AbstractPair< String, String >( region1FfName, regionLayerOutputSuffix ) );
//        featureDatas.add( new AbstractPair< String, String >( region2FfName, regionLayerOutputSuffix ) );
        if( layers == 1 ) featureDatas.add( new AbstractPair< String, String >( region1FfName, regionLayerFbOutputSuffix ) );
        if( layers == 2 ) featureDatas.add( new AbstractPair< String, String >( region2FfName, regionLayerFbOutputSuffix ) );
        if( layers == 3 ) featureDatas.add( new AbstractPair< String, String >( region3FfName, regionLayerFbOutputSuffix ) );
        Framework.SetDataReferences( classFeaturesName, ClassFeaturesEntity.FEATURES, featureDatas ); // get current state from the region to be used to predict

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

        String learningEntitiesAnalytics = classFeaturesName;
        Framework.SetConfig( imageClassName, "learningEntitiesAlgorithm", String.valueOf( learningEntitiesAlgorithm ) );
        Framework.SetConfig( imageClassName, "learningEntitiesAnalytics", String.valueOf( learningEntitiesAnalytics ) );

        // image encoder config
        if( encodeZero ) {
            Framework.SetConfig( imageEncoderName, "density", "1" );
            Framework.SetConfig( imageEncoderName, "bits", "2" );
            Framework.SetConfig( imageEncoderName, "encodeZero", "true" );
        } else {
            Framework.SetConfig( imageEncoderName, "density", "1" );
            Framework.SetConfig( imageEncoderName, "bits", "1" );
            Framework.SetConfig( imageEncoderName, "encodeZero", "false" );
        }

        // feature-class config
        Framework.SetConfig( classFeaturesName, "classEntityName", imageClassName );
        Framework.SetConfig( classFeaturesName, "classConfigPath", "imageClass" );
        Framework.SetConfig( classFeaturesName, "classes", "10" );
        Framework.SetConfig( classFeaturesName, "onlineLearning", String.valueOf( classFeaturesOnline ) );
        Framework.SetConfig( classFeaturesName, "onlineLearningRate", "0.001" );

        // data series logging
        Framework.SetConfig( valueSeriesPredictedName, "period", "-1" ); // log forever
        Framework.SetConfig( valueSeriesErrorName, "period", "-1" );
        Framework.SetConfig( valueSeriesTruthName, "period", "-1" );

        Framework.SetConfig( valueSeriesPredictedName, "entityName", classFeaturesName );
        Framework.SetConfig( valueSeriesErrorName, "entityName", classFeaturesName );
        Framework.SetConfig( valueSeriesTruthName, "entityName", classFeaturesName );

        Framework.SetConfig( valueSeriesPredictedName, "configPath", "classPredicted" );
        Framework.SetConfig( valueSeriesErrorName, "configPath", "classError" );
        Framework.SetConfig( valueSeriesTruthName, "configPath", "classTruth" );

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
        classifiersPerBit1 = 9;
        classifierWidth = 8;
        classifierHeight = 8;

        if( layers > 1 ) {
            setRegionLayerConfig(
                    region2FfName,
                    classifiersPerBit1, classifiersPerBit2, classifierWidth, classifierHeight, organizerWidth, organizerHeight, edgeMaxAge, predictionLearningRate, predictionDecayRate, errorHistoryLength, classifierGrowthInterval );
        }

        // apex
        organizerWidth = 8; // maybe too ambitious?  8 -> 6 -> 3
        organizerHeight = 8;
        classifiersPerBit1 = 9;
        classifierWidth = 8;
        classifierHeight = 8; // 100 cells each

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

        Framework.SetConfig( regionLayerName, "columnWidthCells", String.valueOf( classifierWidth ) );//"5" );
        Framework.SetConfig( regionLayerName, "columnHeightCells", String.valueOf( classifierHeight ) );//"6" );

        Framework.SetConfig( regionLayerName, "regionWidthColumns", String.valueOf( organizerWidth ) );//"8" );
        Framework.SetConfig( regionLayerName, "regionHeightColumns", String.valueOf( organizerHeight ) );//"8" );

        Framework.SetConfig( regionLayerName, "intervalsX1", String.valueOf( organizerWidth ) );
        Framework.SetConfig( regionLayerName, "intervalsY1", String.valueOf( organizerHeight ) );
        Framework.SetConfig( regionLayerName, "intervalsX2", "1" ); // because no 2nd input.
        Framework.SetConfig( regionLayerName, "intervalsY2", "1" ); // because no 2nd input.

        Framework.SetConfig( regionLayerName, "predictionLearningRate", String.valueOf( predictionLearningRate ) );
        Framework.SetConfig( regionLayerName, "predictionDecayRate", String.valueOf( predictionDecayRate ) );
        Framework.SetConfig( regionLayerName, "errorHistoryLength", String.valueOf( errorHistoryLength ) );

        Framework.SetConfig( regionLayerName, "classifiersPerBit1", String.valueOf( classifiersPerBit1 ) );
        Framework.SetConfig( regionLayerName, "classifiersPerBit2", String.valueOf( classifiersPerBit2 ) );

        Framework.SetConfig( regionLayerName, "classifierLearningRate", "0.03" );
        Framework.SetConfig( regionLayerName, "classifierLearningRateNeighbours", "0.001" );
        Framework.SetConfig( regionLayerName, "classifierNoiseMagnitude", "0.0" );
        Framework.SetConfig( regionLayerName, "classifierEdgeMaxAge", String.valueOf( edgeMaxAge ) );
        Framework.SetConfig( regionLayerName, "classifierStressLearningRate", "0.001" );
        Framework.SetConfig( regionLayerName, "classifierStressSplitLearningRate", "0.5" );
        Framework.SetConfig( regionLayerName, "classifierStressThreshold", "0.0" ); // use all cells
        Framework.SetConfig( regionLayerName, "classifierGrowthInterval", String.valueOf( classifierGrowthInterval ) );

    }
}