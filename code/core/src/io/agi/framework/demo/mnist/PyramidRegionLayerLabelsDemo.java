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
import io.agi.core.sdr.IntegerEncoder;
import io.agi.core.util.PropertiesUtil;
import io.agi.core.util.images.BufferedImageSource.BufferedImageSourceFactory;
import io.agi.framework.Framework;
import io.agi.framework.Main;
import io.agi.framework.Node;
import io.agi.framework.entities.*;
import io.agi.framework.factories.CommonEntityFactory;
import sun.security.ssl.HandshakeInStream;

import java.util.ArrayList;
import java.util.Properties;

/**
 * Created by dave on 8/07/16.
 */
public class PyramidRegionLayerLabelsDemo {

    public static final int TEST_TYPE_IMAGE_SEQUENCE = 0;
    public static final int TEST_TYPE_TEXT_SEQUENCE  = 1;

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

//        TESTS:
//        test prediction of sequence learning ( in progress )  22-feb-17 variable order looking good on quick.txt
//        test classification of characters (in progress) 22-feb-17 looks good
//        test higher order prediction of sequence (with fixed images?) 22-feb-17 variable order looking good on quick.txt
//        test higher order prediction of sequence ( mutable images?) READY.
        // todo try rank based prediction not value DONE
        // Todo try longer history = more levels of feedback to predictor
        // TODO try giving it its own output trace, + level above ?trace or prediction?
        //      Higher cells represent longer history. So dont need current + output, only output.
        //      level above predictor gets ...?
        // connect the right feedback for prediction?
        // use density-dependent forgetting factor in output TP
// *      invert output into input both current and predicted.
// *      make a rolling log of text produced. Like a round-robin mode. Be able to view recent predictions and so forth.
// *      Classify predictions with another ANN.
//        \----> dream (generative) sequence (turn off input, or give it predicted input and let it go)
//        L5 motor control demo, encoding corrective output (needs attention? + RL module); maybe there's a corrective output based on character..?

//        int testType = TEST_TYPE_IMAGE_SEQUENCE; // a sequence of images, regardless of content
        int testType = TEST_TYPE_TEXT_SEQUENCE; // a sequence of characters e.g. A..Z or 0..9. Random exemplar for each
        int sourceFilesLabelIndex = 2; // depends on the naming format of the images used

        boolean shuffleTrainingImages = false;
        boolean shuffleTestingImages = false;

        String imagesPathTraining = null;
        String imagesPathTesting = null;

        String textFileTraining = null;
        String textFileTesting = null;

        if( testType == TEST_TYPE_IMAGE_SEQUENCE ) {
            sourceFilesLabelIndex = 2;
            shuffleTrainingImages = true;

            // Same exemplars each time
            imagesPathTraining = "/home/dave/workspace/agi.io/data/mnist/cycle10";
            imagesPathTesting = "/home/dave/workspace/agi.io/data/mnist/cycle10";

            // Varying digit images
//            imagesPathTraining = "/home/dave/workspace/agi.io/data/mnist/10k_train";
//            imagesPathTesting = "/home/dave/workspace/agi.io/data/mnist/5k_test";

            // Full set of digit images
//            imagesPathTraining = "/home/dave/workspace/agi.io/data/mnist/all/all_train";
//            imagesPathTesting = "/home/dave/workspace/agi.io/data/mnist/all/all_t10k";
        }
        else if( testType == TEST_TYPE_TEXT_SEQUENCE ) {
            sourceFilesLabelIndex = 1;
            textFileTraining = "/home/dave/workspace/agi.io/data/text/quick.txt";
            textFileTesting = "/home/dave/workspace/agi.io/data/text/quick.txt";

//            imagesPathTraining = "/home/dave/workspace/agi.io/data/nist-sd19/a2z_1x";
//            imagesPathTesting = "/home/dave/workspace/agi.io/data/nist-sd19/a2z_1x";
            imagesPathTraining = "/home/dave/workspace/agi.io/data/nist-sd19/a2z_10x";
            imagesPathTesting = "/home/dave/workspace/agi.io/data/nist-sd19/a2z_10x";
        }

        int trainingEpochs = 4000;//2000; // doubled the training time
        int testingEpochs = 1;//10;//80; // good for up to 80k
        int terminationAge = 5000;//25000;
        boolean terminateByAge = false;
        int imageRepeats = 1;//50;
        boolean cacheAllData = true;
        boolean doLogging = true;
        int queueLength = 46;

        // Define some entities
        String experimentName           = Framework.GetEntityName( "experiment" );
        String constantName             = Framework.GetEntityName( "constant" );
        String imageSourceName          = Framework.GetEntityName( "image-source" );
        String valueSeriesTruthName     = Framework.GetEntityName( "value-series-truth" );
        String classifierName           = Framework.GetEntityName( "classifier" );
        String dataQueueName           = Framework.GetEntityName( "data-queue" );

        String regionLayer1Name         = Framework.GetEntityName( "region-layer-1" );
        String regionLayer2Name         = Framework.GetEntityName( "region-layer-2" );
        String regionLayer3Name         = Framework.GetEntityName( "region-layer-3" );

        Framework.CreateEntity( experimentName, ExperimentEntity.ENTITY_TYPE, n.getName(), null ); // experiment is the root entity
        Framework.CreateEntity( constantName, ConstantMatrixEntity.ENTITY_TYPE, n.getName(), experimentName ); // ok all input to the regions is ready

        if( testType == TEST_TYPE_IMAGE_SEQUENCE ) {
            Framework.CreateEntity( imageSourceName, ImageLabelEntity.ENTITY_TYPE, n.getName(), constantName );
        }
        else if( testType == TEST_TYPE_TEXT_SEQUENCE ) {
            Framework.CreateEntity( imageSourceName, Text2ImageLabelEntity.ENTITY_TYPE, n.getName(), constantName );
        }

        // Region-layers
        String learningEntitiesAlgorithm = "";
        String lastRegionLayerName = "";
        Framework.CreateEntity( regionLayer1Name, PyramidRegionLayerEntity.ENTITY_TYPE, n.getName(), imageSourceName );
        lastRegionLayerName = regionLayer1Name;
        learningEntitiesAlgorithm = regionLayer1Name;

        Framework.CreateEntity( regionLayer2Name, PyramidRegionLayerEntity.ENTITY_TYPE, n.getName(), regionLayer1Name );
        lastRegionLayerName = regionLayer2Name;
        learningEntitiesAlgorithm = learningEntitiesAlgorithm + "," + regionLayer2Name;

        Framework.CreateEntity( regionLayer3Name, PyramidRegionLayerEntity.ENTITY_TYPE, n.getName(), regionLayer2Name );
        lastRegionLayerName = regionLayer3Name;
        learningEntitiesAlgorithm = learningEntitiesAlgorithm + "," + regionLayer3Name;
        // Region-layers

        Framework.CreateEntity( classifierName, FeedForwardNetworkEntity.ENTITY_TYPE, n.getName(), lastRegionLayerName ); // ok all input to the regions is ready

        if( doLogging ) {
            Framework.CreateEntity( valueSeriesTruthName, ValueSeriesEntity.ENTITY_TYPE, n.getName(), classifierName );
            Framework.CreateEntity( dataQueueName, DataQueueEntity.ENTITY_TYPE, n.getName(), lastRegionLayerName );

            if( cacheAllData ) {
                Framework.SetConfig( valueSeriesTruthName, "cache", String.valueOf( cacheAllData ) );
                Framework.SetConfig( dataQueueName, "cache", String.valueOf( cacheAllData ) );
            }

            Framework.SetConfig( dataQueueName, "queueLength", String.valueOf( queueLength) );
            Framework.SetDataReference( dataQueueName, DataQueueEntity.DATA_INPUT, regionLayer1Name, PyramidRegionLayerEntity.INPUT_C1_PREDICTED );
        }

        // cache all data for speed, when enabled
        Framework.SetConfig( experimentName, "cache", String.valueOf( cacheAllData ) );
        Framework.SetConfig( imageSourceName, "cache", String.valueOf( cacheAllData ) );
        Framework.SetConfig( regionLayer1Name, "cache", String.valueOf( cacheAllData ) );
        Framework.SetConfig( regionLayer2Name, "cache", String.valueOf( cacheAllData ) );
        Framework.SetConfig( regionLayer3Name, "cache", String.valueOf( cacheAllData ) );
        Framework.SetConfig( classifierName, "cache", String.valueOf( cacheAllData ) );

        // RL1
        // it is more stable with the current spikes only as feedback.. ?
        String ffInputData = PyramidRegionLayerEntity.OUTPUT;
        String fbInputData = PyramidRegionLayerEntity.CLASSIFIER_SPIKES_NEW; // this represents a trace of the output from the lower layer over time
//        String fbInputData = PyramidRegionLayerEntity.CLASSIFIER_SPIKES_NEW; // instantaneous output
        Framework.SetDataReference( regionLayer1Name, PyramidRegionLayerEntity.INPUT_C1, imageSourceName, ImageLabelEntity.OUTPUT_IMAGE );
        Framework.SetDataReference( regionLayer1Name, PyramidRegionLayerEntity.INPUT_C2, constantName, ConstantMatrixEntity.OUTPUT );
        Framework.SetDataReference( regionLayer1Name, PyramidRegionLayerEntity.INPUT_P1, regionLayer2Name, fbInputData );
        Framework.SetDataReference( regionLayer1Name, PyramidRegionLayerEntity.INPUT_P2, regionLayer3Name, fbInputData );

        // RL2
        Framework.SetDataReference( regionLayer2Name, PyramidRegionLayerEntity.INPUT_C1, regionLayer1Name, ffInputData );
        Framework.SetDataReference( regionLayer2Name, PyramidRegionLayerEntity.INPUT_C2, constantName, ConstantMatrixEntity.OUTPUT );
        Framework.SetDataReference( regionLayer2Name, PyramidRegionLayerEntity.INPUT_P1, regionLayer2Name, fbInputData ); // Feedback from above
        Framework.SetDataReference( regionLayer2Name, PyramidRegionLayerEntity.INPUT_P2, constantName, ConstantMatrixEntity.OUTPUT );

        // RL3
        Framework.SetDataReference( regionLayer3Name, PyramidRegionLayerEntity.INPUT_C1, regionLayer2Name, ffInputData );
        Framework.SetDataReference( regionLayer3Name, PyramidRegionLayerEntity.INPUT_C2, constantName, ConstantMatrixEntity.OUTPUT );
        Framework.SetDataReference( regionLayer3Name, PyramidRegionLayerEntity.INPUT_P1, constantName, ConstantMatrixEntity.OUTPUT ); // Top layer gets no feedback
        Framework.SetDataReference( regionLayer3Name, PyramidRegionLayerEntity.INPUT_P2, constantName, ConstantMatrixEntity.OUTPUT );

        // Experiment config
        if( !terminateByAge ) {
            Framework.SetConfig( experimentName, "terminationEntityName", imageSourceName );
            Framework.SetConfig( experimentName, "terminationConfigPath", "terminate" );
            Framework.SetConfig( experimentName, "terminationAge", "-1" ); // wait for mnist to decide
        }
        else {
            Framework.SetConfig( experimentName, "terminationAge", String.valueOf( terminationAge ) ); // fixed steps
        }

        // Mnist config
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
        Framework.SetConfig( imageSourceName, "sourceFilesPathTraining", imagesPathTraining );
        Framework.SetConfig( imageSourceName, "sourceFilesPathTesting", imagesPathTesting );
        Framework.SetConfig( imageSourceName, "sourceFilesLabelIndex", String.valueOf( sourceFilesLabelIndex ) );
        Framework.SetConfig( imageSourceName, "trainingEpochs", String.valueOf( trainingEpochs ) );
        Framework.SetConfig( imageSourceName, "testingEpochs", String.valueOf( testingEpochs ) );
        Framework.SetConfig( imageSourceName, "shuffleTraining", String.valueOf( shuffleTrainingImages ) );
        Framework.SetConfig( imageSourceName, "shuffleTesting", String.valueOf( shuffleTestingImages ) );
        Framework.SetConfig( imageSourceName, "imageRepeats", String.valueOf( imageRepeats ) );

        if( testType == TEST_TYPE_IMAGE_SEQUENCE ) {
        }
        else if( testType == TEST_TYPE_TEXT_SEQUENCE ) {
            Framework.SetConfig( imageSourceName, "sourceTextFileTraining", textFileTraining );
            Framework.SetConfig( imageSourceName, "sourceTextFileTesting", textFileTesting );
        }

        // Classifier config
        // Train the classifier to predict the current label given the previously generated prediction of the input.
        // i.e. the algorithm will generate an image of the character it thinks will happen next.
        // Is this worth it? I could just train it on the features? Well there might be different features that are the same digit...?
//        Need a delay entity?
//        Framework.SetDataReference( classifierName, FeedForwardNetworkEntity.INPUT_FEATURES, regionLayer1Name, PyramidRegionLayerEntity.INPUT_C1_PREDICTED_OLD );
//I could digitize with the reconstruction of input - actually with label prediction I could digitize either.
//        Framework.SetConfig( classifierName, "imageRepeats", String.valueOf(  ) );
//
//        int hiddenLayerSize = 0;
//        float regularization = 0;
//        float learningRate = 0;
//        int batchSize = 0;
//        float leakiness = 0;
//
//        public String learningMode = SupervisedLearningEntity.LEARNING_MODE_SAMPLE;
//
//        public boolean predict = false;
////    public boolean learnOnline = false; // have a forgetting factor that means older samples are forgotten
////    public boolean learnBatch = false; // accumulate and (re) train on a batch of samples at once
//        public boolean learnBatchOnce = false; // if set to true, then after one "learn batch" it won't learn again
//        public boolean learnBatchComplete = false; // was: trained
//
//        public boolean accumulateSamples = false; // whether to build a matrix of samples over time
//        public int learningPeriod = -1; // -1 = accumulate data forever, otherwise becomes a rolling window.
////    public boolean labelOneHot = false; // produces a 1-hot vector
//        public int labelClasses = 0; // number of distinct label values, or classes
//
//        // alternate source for labels: A config property
//        public String labelEntityName;
//        public String labelConfigPath;


        // data series logging
        if( doLogging ) {
            Framework.SetConfig( valueSeriesTruthName, "period", "-1" );
            Framework.SetConfig( valueSeriesTruthName, "entityName", imageSourceName );
            Framework.SetConfig( valueSeriesTruthName, "configPath", "imageLabel" );
        }

        // region layer config
        // effective constants:
        float classifierLearningRate = 0.01f;//0.005f;// 0.01
        float classifierMomentum = 0f;
        float classifierWeightsStdDev = 0.01f;
        int classifierAgeMin = 0; // age of disuse where we start to promote cells
        int classifierAgeMax = 2000;//1000;//2000/1000; // age of disuse where we maximally promote cells
        float classifierAgeTruncationFactor = 0.5f;
        float classifierAgeScale = 12f; // promotion nonlinearity
        float classifierRateScale = 5f; // inhibition nonlinearity
        float classifierRateMax = 0.15f;//0.25f; // i.e. never more than 1 in 4.
        float classifierRateLearningRate = 0.01f; // how fast the measurement of rate of cell use changes.

        float predictorLearningRate = 0.002f; // 1/5 the classifier learning rate
        int predictorHiddenCells = 500;
        int predictorBatchSize = 1; //64; =32, at 80k not very trained. CBF waiting so long.
        float predictorLeakiness = 0.01f;
        float predictorRegularization = 0.f;

//        float outputCodingSparsityFactor = 2.0f; // how sticky the output is, ie a trace of historical errors
//                                      //       1  2    3     4     5     6     7
//        float outputDecayRate = 0.6f; // 0.8:  1, 0.8, 0.64, 0.51, 0.40, 0.32, 0.26,
//                                      // 0.6:  1, 0.6, 0.36, 0.21, 0.12, 0.07, 0.04,
        float outputDecayRate = 0.9f;
        // 0.6:  1, 0.6, 0.36, 0.21, 0.12, 0.07, 0.04,

        // variables
        int widthCells = 32;//20;
        int heightCells = 32;//20;
        int classifierBatchSize = 1;
        int classifierSparsity = 20;//15; // k, the number of active cells each step 20/1024=1.9% or 2.9% with alpha factor
        float classifierSparsityOutput = 1.5f; // a factor determining the output sparsity

        String regionLayerName = regionLayer1Name;

        setRegionLayerConfig(
                regionLayerName, widthCells, heightCells,
                classifierLearningRate, classifierMomentum, classifierWeightsStdDev,
                classifierSparsityOutput, classifierSparsity,
                classifierAgeMin, classifierAgeMax, classifierAgeTruncationFactor, classifierAgeScale,
                classifierRateScale, classifierRateMax, classifierRateLearningRate,
                classifierBatchSize,
                predictorLearningRate, predictorHiddenCells, predictorLeakiness, predictorRegularization,
                predictorBatchSize,
                outputDecayRate );

        regionLayerName = regionLayer2Name;

        setRegionLayerConfig(
                regionLayerName, widthCells, heightCells,
                classifierLearningRate, classifierMomentum, classifierWeightsStdDev,
                classifierSparsityOutput, classifierSparsity,
                classifierAgeMin, classifierAgeMax, classifierAgeTruncationFactor, classifierAgeScale,
                classifierRateScale, classifierRateMax, classifierRateLearningRate,
                classifierBatchSize,
                predictorLearningRate, predictorHiddenCells, predictorLeakiness, predictorRegularization,
                predictorBatchSize,
                outputDecayRate );

        regionLayerName = regionLayer3Name;

        setRegionLayerConfig(
                regionLayerName, widthCells, heightCells,
                classifierLearningRate, classifierMomentum, classifierWeightsStdDev,
                classifierSparsityOutput, classifierSparsity,
                classifierAgeMin, classifierAgeMax, classifierAgeTruncationFactor, classifierAgeScale,
                classifierRateScale, classifierRateMax, classifierRateLearningRate,
                classifierBatchSize,
                predictorLearningRate, predictorHiddenCells, predictorLeakiness, predictorRegularization,
                predictorBatchSize,
                outputDecayRate );
    }

    public static void setRegionLayerConfig(
            String regionLayerName,
            int widthCells,
            int heightCells,
            float classifierLearningRate,
            float classifierMomentum,
            float classifierWeightsStdDev,
            float classifierSparsityOutput,
            int classifierSparsity,
            int classifierAgeMin,
            int classifierAgeMax,
            float classifierAgeTruncationFactor,
            float classifierAgeScale,
            float classifierRateScale,
            float classifierRateMax,
            float classifierRateLearningRate,
            float classifierBatchSize,
            float predictorLearningRate,
            int predictorHiddenCells,
            float predictorLeakiness,
            float predictorRegularization,
            int predictorBatchSize,
            float outputDecayRate ) {

        Framework.SetConfig( regionLayerName, "widthCells", String.valueOf( widthCells ) );
        Framework.SetConfig( regionLayerName, "heightCells", String.valueOf( heightCells ) );

        Framework.SetConfig( regionLayerName, "classifierLearningRate", String.valueOf( classifierLearningRate ) );
        Framework.SetConfig( regionLayerName, "classifierMomentum", String.valueOf( classifierMomentum ) );
        Framework.SetConfig( regionLayerName, "classifierSparsityOutput", String.valueOf( classifierSparsityOutput ) );
        Framework.SetConfig( regionLayerName, "classifierSparsity", String.valueOf( classifierSparsity ) );

        Framework.SetConfig( regionLayerName, "classifierAgeMin", String.valueOf( classifierAgeMin ) );
        Framework.SetConfig( regionLayerName, "classifierAgeMax", String.valueOf( classifierAgeMax ) );

        Framework.SetConfig( regionLayerName, "classifierAgeTruncationFactor", String.valueOf( classifierAgeTruncationFactor ) );
        Framework.SetConfig( regionLayerName, "classifierAgeScale", String.valueOf( classifierAgeScale ) );

        Framework.SetConfig( regionLayerName, "classifierRateScale", String.valueOf( classifierRateScale ) );
        Framework.SetConfig( regionLayerName, "classifierRateMax", String.valueOf( classifierRateMax ) );
        Framework.SetConfig( regionLayerName, "classifierRateLearningRate", String.valueOf( classifierRateLearningRate ) );

        Framework.SetConfig( regionLayerName, "classifierWeightsStdDev", String.valueOf( classifierWeightsStdDev ) );
        Framework.SetConfig( regionLayerName, "classifierBatchCount", String.valueOf( 0 ) );
        Framework.SetConfig( regionLayerName, "classifierBatchSize", String.valueOf( classifierBatchSize ) );

        Framework.SetConfig( regionLayerName, "predictorLearningRate", String.valueOf( predictorLearningRate ) );
        Framework.SetConfig( regionLayerName, "predictorHiddenCells", String.valueOf( predictorHiddenCells ) );
        Framework.SetConfig( regionLayerName, "predictorLeakiness", String.valueOf( predictorLeakiness ) );
        Framework.SetConfig( regionLayerName, "predictorRegularization", String.valueOf( predictorRegularization ) );
        Framework.SetConfig( regionLayerName, "predictorBatchSize", String.valueOf( predictorBatchSize ) );

        Framework.SetConfig( regionLayerName, "outputDecayRate", String.valueOf( outputDecayRate ) );

    }

}
