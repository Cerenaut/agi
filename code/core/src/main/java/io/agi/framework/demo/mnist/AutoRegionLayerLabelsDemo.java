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
import io.agi.framework.Naming;
import io.agi.framework.Node;
import io.agi.framework.entities.*;
import io.agi.framework.factories.CommonEntityFactory;
import io.agi.framework.persistence.PersistenceUtil;
import io.agi.framework.references.DataRefUtil;

import java.util.Properties;

/**
 * Created by dave on 8/07/16.
 */
public class AutoRegionLayerLabelsDemo {

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

// so I'm getting around 0.7 with layer 2 and 0.6 with layer 3

//Ideas:
//- Present all possible inputs and see which one responds most strongly, rather than an absence of input
//                Do this by hacking the class when learn is off.(make assumptions about input)
//- move onto sequence data to boost recognition
//          FF with PC
//                FB with both FF and FB inference.

//        String trainingPath = "/home/dave/workspace/agi.io/data/mnist/cycle10";
//        String testingPath = "/home/dave/workspace/agi.io/data/mnist/cycle3";
        String trainingPath = "/home/dave/workspace/agi.io/data/mnist/10k_train";
        String testingPath = "/home/dave/workspace/agi.io/data/mnist/5k_test";
//        String trainingPath = "./training";
//        String testingPath = "./testing";
//        int terminationAge = 10;//9000;
        int terminationAge = 50000;//25000;
        int trainingBatches = 1;//80; // good for up to 80k
        boolean terminateByAge = false;
        float defaultPredictionInhibition = 1.f; // random image classification only experiments
        boolean encodeZero = false;
        int layers = 2;//3;
        int labelBits = 8;

        // Define some entities
        String experimentName           = PersistenceUtil.GetEntityName( "experiment" );
        String imageClassName           = PersistenceUtil.GetEntityName( "image-class" );
        String constantName             = PersistenceUtil.GetEntityName( "constant" );
        String region1FfName            = PersistenceUtil.GetEntityName( "image-region-1-ff" );
        String region2FfName            = PersistenceUtil.GetEntityName( "image-region-2-ff" );
        String region3FfName            = PersistenceUtil.GetEntityName( "image-region-3-ff" );
        String imageEncoderName         = PersistenceUtil.GetEntityName( "image-encoder" );
        String labelEncoderName         = PersistenceUtil.GetEntityName( "label-encoder" );
        String labelDecoderName         = PersistenceUtil.GetEntityName( "label-decoder" );
        String classResultName          = PersistenceUtil.GetEntityName( "class-result" );
        String valueSeriesPredictedName = PersistenceUtil.GetEntityName( "value-series-predicted" );
        String valueSeriesErrorName     = PersistenceUtil.GetEntityName( "value-series-error" );
        String valueSeriesTruthName     = PersistenceUtil.GetEntityName( "value-series-truth" );
        String configProductName        = PersistenceUtil.GetEntityName( "config-product" );


        PersistenceUtil.CreateEntity( experimentName, ExperimentEntity.ENTITY_TYPE, n.getName(), null ); // experiment is the root entity
        PersistenceUtil.CreateEntity( imageClassName, ImageLabelEntity.ENTITY_TYPE, n.getName(), experimentName );
        PersistenceUtil.CreateEntity( imageEncoderName, EncoderEntity.ENTITY_TYPE, n.getName(), imageClassName );
        PersistenceUtil.CreateEntity( constantName, ConstantMatrixEntity.ENTITY_TYPE, n.getName(), imageEncoderName ); // ok all input to the regions is ready
        PersistenceUtil.CreateEntity( labelEncoderName, EncoderEntity.ENTITY_TYPE, n.getName(), constantName );
        PersistenceUtil.CreateEntity( configProductName, ConfigProductEntity.ENTITY_TYPE, n.getName(), labelEncoderName ); // ok all input to the regions is ready

        PersistenceUtil.CreateEntity( region1FfName, AutoRegionLayerEntity.ENTITY_TYPE, n.getName(), configProductName );
        String learningEntitiesAlgorithm = region1FfName;
        String topLayerName = region1FfName;
        if( layers > 1 ) {
            PersistenceUtil.CreateEntity( region2FfName, AutoRegionLayerEntity.ENTITY_TYPE, n.getName(), region1FfName );
            topLayerName = region2FfName;
            learningEntitiesAlgorithm = learningEntitiesAlgorithm + "," + region2FfName;
        }
        if( layers > 2 ) {
            PersistenceUtil.CreateEntity( region3FfName, AutoRegionLayerEntity.ENTITY_TYPE, n.getName(), region2FfName );
            topLayerName = region3FfName;
            learningEntitiesAlgorithm = learningEntitiesAlgorithm + "," + region3FfName;
        }

        PersistenceUtil.CreateEntity( labelDecoderName, DecoderEntity.ENTITY_TYPE, n.getName(), topLayerName ); // produce the predicted classification for inspection by mnist next time
        PersistenceUtil.CreateEntity( classResultName, ClassificationResultEntity.ENTITY_TYPE, n.getName(), labelDecoderName ); // produce the predicted classification for inspection by mnist next time

        PersistenceUtil.CreateEntity( valueSeriesPredictedName, ValueSeriesEntity.ENTITY_TYPE, n.getName(), classResultName ); // 2nd, class region updates after first to get its feedback
        PersistenceUtil.CreateEntity( valueSeriesErrorName, ValueSeriesEntity.ENTITY_TYPE, n.getName(), classResultName ); // 2nd, class region updates after first to get its feedback
        PersistenceUtil.CreateEntity( valueSeriesTruthName, ValueSeriesEntity.ENTITY_TYPE, n.getName(), classResultName ); // 2nd, class region updates after first to get its feedback

        // Connect the entities' data
        // a) Image to image region, and decode
        DataRefUtil.SetDataReference( imageEncoderName, EncoderEntity.DATA_INPUT, imageClassName, ImageLabelEntity.OUTPUT_IMAGE );
        DataRefUtil.SetDataReference( labelEncoderName, EncoderEntity.DATA_INPUT, imageClassName, ImageLabelEntity.OUTPUT_LABEL );
        DataRefUtil.SetDataReference( labelDecoderName, DecoderEntity.DATA_INPUT_DECODED, imageClassName, ImageLabelEntity.OUTPUT_LABEL );

//                DataRefUtil.SetDataReference( region2FfName, AutoRegionLayerEntity.INPUT_2, labelEncoderName, EncoderEntity.DATA_OUTPUT_ENCODED );
        DataRefUtil.SetDataReference( configProductName, ConfigProductEntity.INPUT, labelEncoderName, EncoderEntity.DATA_OUTPUT_ENCODED );

        DataRefUtil.SetDataReference( region1FfName, AutoRegionLayerEntity.INPUT_1, imageEncoderName, EncoderEntity.DATA_OUTPUT_ENCODED );
        DataRefUtil.SetDataReference( region1FfName, AutoRegionLayerEntity.INPUT_2, constantName, ConstantMatrixEntity.OUTPUT );

        if( layers > 1 ) {
            DataRefUtil.SetDataReference( region2FfName, AutoRegionLayerEntity.INPUT_1, region1FfName, AutoRegionLayerEntity.OUTPUT );
            if( layers == 2 ) {
//                DataRefUtil.SetDataReference( region2FfName, AutoRegionLayerEntity.INPUT_2, labelEncoderName, EncoderEntity.DATA_OUTPUT_ENCODED );
                DataRefUtil.SetDataReference( region2FfName, AutoRegionLayerEntity.INPUT_2, configProductName, ConfigProductEntity.OUTPUT );
            }
            else {
                DataRefUtil.SetDataReference( region2FfName, AutoRegionLayerEntity.INPUT_2, constantName, ConstantMatrixEntity.OUTPUT );
            }
        }

        if( layers > 2 ) {
            DataRefUtil.SetDataReference( region3FfName, AutoRegionLayerEntity.INPUT_1, region2FfName, AutoRegionLayerEntity.OUTPUT );
            if( layers == 3 ) {
//                DataRefUtil.SetDataReference( region3FfName, AutoRegionLayerEntity.INPUT_2, labelEncoderName, EncoderEntity.DATA_OUTPUT_ENCODED );
                DataRefUtil.SetDataReference( region3FfName, AutoRegionLayerEntity.INPUT_2, configProductName, ConfigProductEntity.OUTPUT );
            }
            else {
                DataRefUtil.SetDataReference( region3FfName, AutoRegionLayerEntity.INPUT_2, constantName, ConstantMatrixEntity.OUTPUT );
            }
        }

        // invert the hidden layer state to produce the predicted label
        DataRefUtil.SetDataReference( labelDecoderName, DecoderEntity.DATA_INPUT_ENCODED, topLayerName, AutoRegionLayerEntity.OUTPUT_INPUT_2 ); // the prediction of the next state

        DataRefUtil.SetDataReference( classResultName, ClassificationResultEntity.INPUT_LABELS, imageClassName, ImageLabelEntity.OUTPUT_LABEL ); // get current state from the region to be used to predict
        DataRefUtil.SetDataReference( classResultName, ClassificationResultEntity.INPUT_CLASSIFICATIONS, labelDecoderName, DecoderEntity.DATA_OUTPUT_DECODED ); // get current state from the region to be used to predict

        // Label filter
        PersistenceUtil.SetConfig( configProductName, "entityName", region1FfName );
        PersistenceUtil.SetConfig( configProductName, "configPath", "learn" ); // so the value is '1' when the region is learning, and 0 otherwise.

        // Experiment config
        if( !terminateByAge ) {
            PersistenceUtil.SetConfig( experimentName, "terminationEntityName", imageClassName );
            PersistenceUtil.SetConfig( experimentName, "terminationConfigPath", "terminate" );
            PersistenceUtil.SetConfig( experimentName, "terminationAge", "-1" ); // wait for mnist to decide
        }
        else {
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

        String learningEntitiesAnalytics = "";//classFeaturesName;
        PersistenceUtil.SetConfig( imageClassName, "learningEntitiesAlgorithm", String.valueOf( learningEntitiesAlgorithm ) );
        PersistenceUtil.SetConfig( imageClassName, "learningEntitiesAnalytics", String.valueOf( learningEntitiesAnalytics ) );

        // constant config
        if( encodeZero ) {
            // image encoder config
            PersistenceUtil.SetConfig( imageEncoderName, "density", "1" );
            PersistenceUtil.SetConfig( imageEncoderName, "bits", "2" );
            PersistenceUtil.SetConfig( imageEncoderName, "encodeZero", "true" );
        }
        else {
            // image encoder config
            PersistenceUtil.SetConfig( imageEncoderName, "density", "1" );
            PersistenceUtil.SetConfig( imageEncoderName, "bits", "1" );
            PersistenceUtil.SetConfig( imageEncoderName, "encodeZero", "false" );
        }
// TODO juyst need to add the switch to disable the labels.
        // Label encoder
        PersistenceUtil.SetConfig( labelEncoderName, "encoderType", IntegerEncoder.class.getSimpleName() );
        PersistenceUtil.SetConfig( labelEncoderName, "minValue", "0" );
        PersistenceUtil.SetConfig( labelEncoderName, "maxValue", "9" );
        PersistenceUtil.SetConfig( labelEncoderName, "rows", String.valueOf( labelBits ) );

        // Label DEcoder
        PersistenceUtil.SetConfig( labelDecoderName, "encoderType", IntegerEncoder.class.getSimpleName() );
        PersistenceUtil.SetConfig( labelDecoderName, "minValue", "0" );
        PersistenceUtil.SetConfig( labelDecoderName, "maxValue", "9" );
        PersistenceUtil.SetConfig( labelDecoderName, "rows", String.valueOf( labelBits ) );

        // image region config
        // effective constants:
        float predictorLearningRate = 0.01f;
        int ageMin = 0;
        int ageMax = 750;//700;//1000;
        float ageScale = 12f;//17f;//15f; // maybe try 12 or 17
        float rateScale = 5f;
        float rateLearningRate = 0.01f;
        float sparsityMax = 0;// no longer used - adaptive cell promotion/inh (int)( (widthCells * heightCells) * 0.9f );

        // variables
//        float sparseLearningRate = 0.000001f;
//        float sparseLearningRate = 0.00001f;
//        float sparseLearningRate = 0.0001f;
//        float sparseLearningRate = 0.001f; // * best
//        float sparseLearningRate = 0.01f;
//        float sparseLearningRate = 0.1f;
//        sparseLearningRate = 0.01f;//0.001f; // test
//        sparseLearningRate = 0.05f; // test saturated at 0.6, but now with improved output
//        sparseLearningRate = 0.1f; //
        float sparseLearningRate = 0.01f;
//        sparseLearningRate = 0.05f;

        int widthCells = 32; // from the paper, 32x32=1024 was optimal on MNIST (but with a supervised output layer)
        int heightCells = 32;
        float sparsityMin = 25f;//25;//30;//25;//cells * 0.02f;
        float sparsityOutput = 2.f;//2.f; // temporal pooling, off if 1f
        float sparsityFactor = sparsityOutput;//1.f; // the "alpha" term in the paper

//        KSparseAutoencoder.REGULARIZATION = 0.001f;

        setRegionLayerConfig(
                region1FfName,
                widthCells, heightCells,
                ageMin, ageMax, ageScale,
                sparseLearningRate, sparsityMin, sparsityMax, sparsityFactor, sparsityOutput,
                defaultPredictionInhibition, predictorLearningRate,
                rateScale, rateLearningRate );

        // smaller region
        widthCells = 32;
        heightCells = 32;
        sparsityMin = 8;//12;//10;//25;//cells * 0.02f;
        sparsityOutput = 2.f;//2.f; // temporal pooling, off if 1f
        sparsityFactor = sparsityOutput; // added was missing

        setRegionLayerConfig(
                region2FfName,
                widthCells, heightCells,
                ageMin, ageMax, ageScale,
                sparseLearningRate, sparsityMin, sparsityMax, sparsityFactor, sparsityOutput,
                defaultPredictionInhibition, predictorLearningRate,
                rateScale, rateLearningRate );

//        setRegionLayerConfig(
//                region3FfName,
//                widthCells, heightCells,
//                ageMin, ageMax, ageScale,
//                sparseLearningRate, sparsityMin, sparsityMax, sparsityFactor, sparsityOutput,
//                defaultPredictionInhibition, predictorLearningRate,
//                rateScale, rateLearningRate );

        // look at weight decay, l2 norm, momentum, batches
        // TODO LIST:
        //   try changing the time constant of the classifier (it measures better with 0.01 than 0.001): Better with shorter, i.e. otherwise affected by learning.
        //   better weight randomization (see email for log-sigmoid specific fn): Helped a lot.
        //   L2 weight regularization?
        //    Weight decay =a technique sometimes known as weight decay or L2 regularization
        //    the regularization term does't include the biases. : no benefit
        //   Momentum: No clear benefit from tests.
        // - Batch gradient calc
        //   k * a output - tested, got 0.7 so far, so a bump of 10% due to this feature
        //    Try: 1.5, 2.25, 3.0
        // - k >= 0.9k output set (anything n% of best score): This looks less promising looking at the distributions, need to see the transfer unfiltered
        // - otsu output set (with min max bounds): Not looking viable from weighted sum: Much worse
        //   otsu weighted sum. : Much worse.
        // - test pred with both levels
        //*   - try adding more cells to L2
        //*   - much higher learning rate (+momentum?)
        // * longer max age
        //* - Alan's idea: Iteratively feed in the output (without learning) until the classification stabilizes. Or interact with next layer to see what it finds
        // - train with labels half the time? Or 2 regions feeding into another. Even if this is only worth 5% it's good.
        //
        // NB the UI active includes the extra x active cells, not the set used for training
        // NB log-sigmoid saturates at 5.

        // feature-class config
//        PersistenceUtil.SetConfig( classFeaturesName, "classEntityName", imageClassName );
//        PersistenceUtil.SetConfig( classFeaturesName, "classConfigPath", "imageClass" );
//        PersistenceUtil.SetConfig( classFeaturesName, "classes", "10" );
//        PersistenceUtil.SetConfig( classFeaturesName, "onlineLearning", String.valueOf( classFeaturesOnline ) );
////        PersistenceUtil.SetConfig( classFeaturesName, "onlineLearningRate", "0.001" );
//        PersistenceUtil.SetConfig( classFeaturesName, "onlineLearningRate", "0.01" );

        // data series logging
        PersistenceUtil.SetConfig( valueSeriesPredictedName, "period", "-1" ); // log forever
        PersistenceUtil.SetConfig( valueSeriesErrorName, "period", "-1" );
        PersistenceUtil.SetConfig( valueSeriesTruthName, "period", "-1" );

        PersistenceUtil.SetConfig( valueSeriesPredictedName, "entityName", classResultName ); // log forever
        PersistenceUtil.SetConfig( valueSeriesErrorName, "entityName", classResultName );
        PersistenceUtil.SetConfig( valueSeriesTruthName, "entityName", classResultName );

        PersistenceUtil.SetConfig( valueSeriesPredictedName, "configPath", "classPredicted" ); // log forever
        PersistenceUtil.SetConfig( valueSeriesErrorName, "configPath", "classError" );
        PersistenceUtil.SetConfig( valueSeriesTruthName, "configPath", "classTruth" );
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
            float sparsityMax,
//            float sparsityCells,
            float sparsityFactor,
            float sparsityOutput,
            float defaultPredictionInhibition,
            float predictorLearningRate,
            float rateScale,
            float rateLearningRate ) {

        PersistenceUtil.SetConfig( regionLayerName, "contextFreeLearningRate", String.valueOf( sparseLearningRate ) );
        PersistenceUtil.SetConfig( regionLayerName, "contextFreeWidthCells", String.valueOf( widthCells ) );
        PersistenceUtil.SetConfig( regionLayerName, "contextFreeHeightCells", String.valueOf( heightCells ) );
        PersistenceUtil.SetConfig( regionLayerName, "contextFreeBinaryOutput", String.valueOf( true ) );
        PersistenceUtil.SetConfig( regionLayerName, "contextFreeSparsity", String.valueOf( 0 ) );
        PersistenceUtil.SetConfig( regionLayerName, "contextFreeSparsityOutput", String.valueOf( sparsityFactor ) );
        PersistenceUtil.SetConfig( regionLayerName, "contextFreeSparsityMin", String.valueOf( sparsityMin ) );
        PersistenceUtil.SetConfig( regionLayerName, "contextFreeSparsityMax", String.valueOf( sparsityMax ) );
        PersistenceUtil.SetConfig( regionLayerName, "contextFreeAgeMin", String.valueOf( ageMin ) );
        PersistenceUtil.SetConfig( regionLayerName, "contextFreeAgeMax", String.valueOf( ageMax ) );
        PersistenceUtil.SetConfig( regionLayerName, "contextFreeAge", String.valueOf( 0 ) );
        PersistenceUtil.SetConfig( regionLayerName, "contextFreeAgeScale", String.valueOf( ageScale ) );

        PersistenceUtil.SetConfig( regionLayerName, "rateScale", String.valueOf( rateScale ) );
        PersistenceUtil.SetConfig( regionLayerName, "rateLearningRate", String.valueOf( rateLearningRate ) );
//        PersistenceUtil.SetConfig( regionLayerName, "contextualLearningRate", String.valueOf( sparseLearningRate ) );
//        PersistenceUtil.SetConfig( regionLayerName, "contextualWidthCells", String.valueOf( widthCells ) );
//        PersistenceUtil.SetConfig( regionLayerName, "contextualHeightCells", String.valueOf( heightCells ) );
//        PersistenceUtil.SetConfig( regionLayerName, "contextualSparsity", String.valueOf( 0 ) );
//        PersistenceUtil.SetConfig( regionLayerName, "contextualSparsityOutput", String.valueOf( sparsityFactor ) );
//        PersistenceUtil.SetConfig( regionLayerName, "contextualSparsityMin", String.valueOf( sparsityMin ) );
//        PersistenceUtil.SetConfig( regionLayerName, "contextualSparsityMax", String.valueOf( sparsityMax ) );
//        PersistenceUtil.SetConfig( regionLayerName, "contextualAgeMin", String.valueOf( ageMin ) );
//        PersistenceUtil.SetConfig( regionLayerName, "contextualAgeMax", String.valueOf( ageMax ) );
//        PersistenceUtil.SetConfig( regionLayerName, "contextualAge", String.valueOf( 0 ) );

        PersistenceUtil.SetConfig( regionLayerName, "outputSparsity", String.valueOf( sparsityOutput ) );
        PersistenceUtil.SetConfig( regionLayerName, "defaultPredictionInhibition", String.valueOf( defaultPredictionInhibition ) );
        PersistenceUtil.SetConfig( regionLayerName, "predictorLearningRate", String.valueOf( predictorLearningRate ) );

    }

}
