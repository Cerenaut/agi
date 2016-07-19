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
        int terminationAge = 25000;
        int trainingBatches = 1;
        boolean terminateByAge = true;
        float defaultPredictionInhibition = 0.f; // random image classification only experiments
//        float defaultPredictionInhibition = 0.f; // where you use prediction
        boolean encodeZero = false;
        int layers = 1;

        // Define some entities
        String experimentName = "experiment";
        String imageClassName = "image-class";
        String constantName = "constant";
        String region1FfName = "image-region-1-ff";
        String region2FfName = "image-region-2-ff";
        String region3FfName = "image-region-3-ff";
        String imageEncoderName = "image-encoder";
        String classFeaturesName = "class-features";

        String valueSeriesPredictedName = "value-series-predicted";
        String valueSeriesErrorName = "value-series-error";
        String valueSeriesTruthName = "value-series-truth";

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
        if( layers > 0 ) featureDatas.add( new AbstractPair< String, String >( region1FfName, AutoRegionLayerEntity.CONTEXT_FREE_ACTIVITY_NEW ) );
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
        int widthCells = 32;
        int heightCells = 32;
        int ageMin = 0;
        int ageMax = 3000;//1000;
//        float sparseLearningRate = 0.000001f;
//        float sparseLearningRate = 0.00001f;
//        float sparseLearningRate = 0.0001f;
        float sparseLearningRate = 0.001f; // * best
//        float sparseLearningRate = 0.01f;
//        float sparseLearningRate = 0.1f;
        //float cells = (float)( widthCells * heightCells );
        float sparsityMin = 25;//cells * 0.02f;
        float sparsityMax = 150;//cells * 0.1f;
        float sparsityOutput = sparsityMin * 2.f;

//        float sparsityCells = 0.f; // determined by age
        float sparsityFactor = 3.f;//1.f;
//        float predictorLearningRate = 0.001f;
        float predictorLearningRate = 0.01f;

        setRegionLayerConfig(
            region1FfName,
            widthCells, heightCells,
            ageMin, ageMax,
            sparseLearningRate, sparsityMin, sparsityMax, sparsityFactor, sparsityOutput,
            defaultPredictionInhibition, predictorLearningRate );

        // feature-class config
        Framework.SetConfig( classFeaturesName, "classEntityName", imageClassName );
        Framework.SetConfig( classFeaturesName, "classConfigPath", "imageClass" );
        Framework.SetConfig( classFeaturesName, "classes", "10" );
        Framework.SetConfig( classFeaturesName, "onlineLearning", "true" );
        Framework.SetConfig( classFeaturesName, "onlineLearningRate", "0.001" );

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
        Framework.SetConfig( regionLayerName, "contextFreeSparsity", String.valueOf( 0 ) );
        Framework.SetConfig( regionLayerName, "contextFreeSparsityOutput", String.valueOf( sparsityFactor ) );
        Framework.SetConfig( regionLayerName, "contextFreeSparsityMin", String.valueOf( sparsityMin ) );
        Framework.SetConfig( regionLayerName, "contextFreeSparsityMax", String.valueOf( sparsityMax ) );
        Framework.SetConfig( regionLayerName, "contextFreeAgeMin", String.valueOf( ageMin ) );
        Framework.SetConfig( regionLayerName, "contextFreeAgeMax", String.valueOf( ageMax ) );
        Framework.SetConfig( regionLayerName, "contextFreeAge", String.valueOf( 0 ) );
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
