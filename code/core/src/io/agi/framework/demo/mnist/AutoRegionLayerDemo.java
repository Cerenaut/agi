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

import io.agi.core.ann.unsupervised.KSparseAutoencoder;
import io.agi.core.orm.AbstractPair;
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

    /**
     * Usage: Expects some arguments. These are:
     * 0: node.properties file
     * 1 to n: 'create' flag and/or 'prefix' flag
     * @param args
     */
    public static void main( String[] args ) {

        // Create a Node
        Main m = new Main();
        m.setup( args[ 0 ], null, new CommonEntityFactory() );

        // Optionally set a global prefix for entities
        for( int i = 1; i < args.length; ++i ) {
            String arg = args[ i ];
            if( arg.equalsIgnoreCase( "prefix" ) ) {
                Framework.SetEntityNamePrefixDateTime();
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
//        String testingPath = "/home/dave/workspace/agi.io/data/mnist/cycle10";
//        String trainingPath = "/home/dave/workspace/agi.io/data/mnist/cycle3";
//        String testingPath = "/home/dave/workspace/agi.io/data/mnist/cycle3";
//        String trainingPath = "/home/dave/workspace/agi.io/data/mnist/cycle_twin";
//        String testingPath = "/home/dave/workspace/agi.io/data/mnist/cycle_twin";
//        String trainingPath = "/home/dave/workspace/agi.io/data/mnist/cycle_deep";
//        String testingPath = "/home/dave/workspace/agi.io/data/mnist/cycle_deep";
//        String trainingPath = "/home/dave/workspace/agi.io/data/mnist/all_train";
//        String testingPath = "/home/dave/workspace/agi.io/data/mnist/all_t10k";
        String trainingPath = "/home/dave/workspace/agi.io/data/mnist/10k_train";
        String testingPath = "/home/dave/workspace/agi.io/data/mnist/5k_test";
//        String trainingPath = "./training";
//        String testingPath = "./testing";
//        int terminationAge = 10;//9000;
        int terminationAge = 50000;//25000;
        int trainingBatches = 80; // good for up to 80k
        boolean terminateByAge = true;
        float defaultPredictionInhibition = 1.f; // random image classification only experiments
//        float defaultPredictionInhibition = 0.f; // where you use prediction
        boolean encodeZero = false;
        int layers = 2;

        // Define some entities
        String experimentName           = Framework.GetEntityName( "experiment" );
        String imageClassName           = Framework.GetEntityName( "image-class" );
        String constantName             = Framework.GetEntityName( "constant" );
        String region1FfName            = Framework.GetEntityName( "image-region-1-ff" );
        String region2FfName            = Framework.GetEntityName( "image-region-2-ff" );
        String region3FfName            = Framework.GetEntityName( "image-region-3-ff" );
        String imageEncoderName         = Framework.GetEntityName( "image-encoder" );
        String classFeaturesName        = Framework.GetEntityName( "class-features" );
        String valueSeriesPredictedName = Framework.GetEntityName( "value-series-predicted" );
        String valueSeriesErrorName     = Framework.GetEntityName( "value-series-error" );
        String valueSeriesTruthName     = Framework.GetEntityName( "value-series-truth" );

        Framework.CreateEntity( experimentName, ExperimentEntity.ENTITY_TYPE, n.getName(), null ); // experiment is the root entity
        Framework.CreateEntity( imageClassName, ImageClassEntity.ENTITY_TYPE, n.getName(), experimentName );
        Framework.CreateEntity( imageEncoderName, EncoderEntity.ENTITY_TYPE, n.getName(), imageClassName );
        Framework.CreateEntity( constantName, ConstantMatrixEntity.ENTITY_TYPE, n.getName(), imageEncoderName ); // ok all input to the regions is ready

        Framework.CreateEntity( region1FfName, AutoRegionLayerEntity.ENTITY_TYPE, n.getName(), constantName );
        String topLayerName = region1FfName;
        if( layers > 1 ) {
            Framework.CreateEntity( region2FfName, AutoRegionLayerEntity.ENTITY_TYPE, n.getName(), region1FfName );
            topLayerName = region2FfName;
        }
        if( layers > 2 ) {
            Framework.CreateEntity( region3FfName, AutoRegionLayerEntity.ENTITY_TYPE, n.getName(), region2FfName );
            topLayerName = region3FfName;
        }

        Framework.CreateEntity( classFeaturesName, ClassFeaturesEntity.ENTITY_TYPE, n.getName(), topLayerName ); // 2nd, class region updates after first to get its feedback
        Framework.CreateEntity( valueSeriesPredictedName, ValueSeriesEntity.ENTITY_TYPE, n.getName(), classFeaturesName ); // 2nd, class region updates after first to get its feedback
        Framework.CreateEntity( valueSeriesErrorName, ValueSeriesEntity.ENTITY_TYPE, n.getName(), classFeaturesName ); // 2nd, class region updates after first to get its feedback
        Framework.CreateEntity( valueSeriesTruthName, ValueSeriesEntity.ENTITY_TYPE, n.getName(), classFeaturesName ); // 2nd, class region updates after first to get its feedback

        // Connect the entities' data
        // a) Image to image region, and decode
        Framework.SetDataReference( imageEncoderName, EncoderEntity.DATA_INPUT, imageClassName, ImageClassEntity.OUTPUT_IMAGE );

        Framework.SetDataReference( region1FfName, AutoRegionLayerEntity.INPUT_1, imageEncoderName, EncoderEntity.DATA_OUTPUT_ENCODED );
        Framework.SetDataReference( region1FfName, AutoRegionLayerEntity.INPUT_2, constantName, ConstantMatrixEntity.OUTPUT );

        if( layers > 1 ) {
            Framework.SetDataReference( region2FfName, AutoRegionLayerEntity.INPUT_1, region1FfName, AutoRegionLayerEntity.OUTPUT );
            Framework.SetDataReference( region2FfName, AutoRegionLayerEntity.INPUT_2, constantName, ConstantMatrixEntity.OUTPUT );
        }

        if( layers > 2 ) {
            Framework.SetDataReference( region3FfName, AutoRegionLayerEntity.INPUT_1, region2FfName, AutoRegionLayerEntity.OUTPUT );
            Framework.SetDataReference( region3FfName, AutoRegionLayerEntity.INPUT_2, constantName, ConstantMatrixEntity.OUTPUT );
        }

        ArrayList< AbstractPair< String, String > > featureDatas = new ArrayList< AbstractPair< String, String > >();
//        if( layers > 0 ) featureDatas.add( new AbstractPair< String, String >( region1FfName, AutoRegionLayerEntity.CONTEXT_FREE_ACTIVITY_NEW ) );
        if( layers > 1 ) featureDatas.add( new AbstractPair< String, String >( region2FfName, AutoRegionLayerEntity.CONTEXT_FREE_ACTIVITY_NEW ) );
        if( layers > 2 ) featureDatas.add( new AbstractPair< String, String >( region3FfName, AutoRegionLayerEntity.CONTEXT_FREE_ACTIVITY_NEW ) );
        Framework.SetDataReferences( classFeaturesName, ClassFeaturesEntity.FEATURES, featureDatas ); // get current state from the region to be used to predict

        // Experiment config
        if( !terminateByAge ) {
            Framework.SetConfig( experimentName, "terminationEntityName", imageClassName );
            Framework.SetConfig( experimentName, "terminationConfigPath", "terminate" );
            Framework.SetConfig( experimentName, "terminationAge", "-1" ); // wait for mnist to decide
        }
        else {
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

        // image region config
        int widthCells = 32; // from the paper, this was optimal on MNIST
        int heightCells = 32;
        int ageMin = 0;
        int ageMax = 1400;//700;//1000;
        float ageScale = 12f;//17f;//15f; // maybe try 12 or 17

//        float sparseLearningRate = 0.000001f;
//        float sparseLearningRate = 0.00001f;
//        float sparseLearningRate = 0.0001f;
        float sparseLearningRate = 0.001f; // * best
//        float sparseLearningRate = 0.01f;
//        float sparseLearningRate = 0.1f;

//        sparseLearningRate = 0.01f;//0.001f; // test
//        sparseLearningRate = 0.05f; // test saturated at 0.6, but now with improved output
//        sparseLearningRate = 0.1f; //
        sparseLearningRate = 0.01f;

        //float cells = (float)( widthCells * heightCells );
        float sparsityMin = 25;//30;//25;//cells * 0.02f;
        float sparsityMax = (int)( (widthCells * heightCells) * 0.9f );
        float sparsityOutput = 2.5f;//2.f; // temporal pooling, off if 1f

//        float sparsityCells = 0.f; // determined by age
        float sparsityFactor = sparsityOutput;//1.f; // the "alpha" term in the paper
//        float predictorLearningRate = 0.001f;
        float predictorLearningRate = 0.01f;

//        KSparseAutoencoder.REGULARIZATION = 0.001f;

        setRegionLayerConfig(
                region1FfName,
                widthCells, heightCells,
                ageMin, ageMax, ageScale,
                sparseLearningRate, sparsityMin, sparsityMax, sparsityFactor, sparsityOutput,
                defaultPredictionInhibition, predictorLearningRate );

        // smaller region
        widthCells = 20;
        heightCells = 20;
        //ageMin = ageMax;
        //ageMax = ageMax * 3;
        sparsityMin = 12;//12;//10;//25;//cells * 0.02f;
        sparsityMax = ( (widthCells * heightCells) * 0.9f );
        sparsityOutput = 2.25f;//2.f; // temporal pooling, off if 1f
        sparsityFactor = sparsityOutput; // added was missing

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

        setRegionLayerConfig(
                region2FfName,
                widthCells, heightCells,
                ageMin, ageMax, ageScale,
                sparseLearningRate, sparsityMin, sparsityMax, sparsityFactor, sparsityOutput,
                defaultPredictionInhibition, predictorLearningRate );

        // feature-class config
        Framework.SetConfig( classFeaturesName, "classEntityName", imageClassName );
        Framework.SetConfig( classFeaturesName, "classConfigPath", "imageClass" );
        Framework.SetConfig( classFeaturesName, "classes", "10" );
        Framework.SetConfig( classFeaturesName, "onlineLearning", "true" );
//        Framework.SetConfig( classFeaturesName, "onlineLearningRate", "0.001" );
        Framework.SetConfig( classFeaturesName, "onlineLearningRate", "0.01" );

        // data series logging
        Framework.SetConfig( valueSeriesPredictedName, "period", "-1" ); // log forever
        Framework.SetConfig( valueSeriesErrorName, "period", "-1" );
        Framework.SetConfig( valueSeriesTruthName, "period", "-1" );

        Framework.SetConfig( valueSeriesPredictedName, "entityName", classFeaturesName ); // log forever
        Framework.SetConfig( valueSeriesErrorName, "entityName", classFeaturesName );
        Framework.SetConfig( valueSeriesTruthName, "entityName", classFeaturesName );

        Framework.SetConfig( valueSeriesPredictedName, "configPath", "classPredicted" ); // log forever
        Framework.SetConfig( valueSeriesErrorName, "configPath", "classError" );
        Framework.SetConfig( valueSeriesTruthName, "configPath", "classTruth" );
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
            float predictorLearningRate ) {

        Framework.SetConfig( regionLayerName, "contextFreeLearningRate", String.valueOf( sparseLearningRate ) );
        Framework.SetConfig( regionLayerName, "contextFreeWidthCells", String.valueOf( widthCells ) );
        Framework.SetConfig( regionLayerName, "contextFreeHeightCells", String.valueOf( heightCells ) );
        Framework.SetConfig( regionLayerName, "contextFreeBinaryOutput", String.valueOf( true ) );
        Framework.SetConfig( regionLayerName, "contextFreeSparsity", String.valueOf( 0 ) );
        Framework.SetConfig( regionLayerName, "contextFreeSparsityOutput", String.valueOf( sparsityFactor ) );
        Framework.SetConfig( regionLayerName, "contextFreeSparsityMin", String.valueOf( sparsityMin ) );
        Framework.SetConfig( regionLayerName, "contextFreeSparsityMax", String.valueOf( sparsityMax ) );
        Framework.SetConfig( regionLayerName, "contextFreeAgeMin", String.valueOf( ageMin ) );
        Framework.SetConfig( regionLayerName, "contextFreeAgeMax", String.valueOf( ageMax ) );
        Framework.SetConfig( regionLayerName, "contextFreeAge", String.valueOf( 0 ) );
        Framework.SetConfig( regionLayerName, "contextFreeAgeScale", String.valueOf( ageScale ) );
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

        Framework.SetConfig( regionLayerName, "outputSparsity", String.valueOf( sparsityOutput ) );
        Framework.SetConfig( regionLayerName, "defaultPredictionInhibition", String.valueOf( defaultPredictionInhibition ) );
        Framework.SetConfig( regionLayerName, "predictorLearningRate", String.valueOf( predictorLearningRate ) );

    }

}
