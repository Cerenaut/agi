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
        // Seems to be getting confused on start of words. Does this mean not enough history? Try changing TP factor?
        // Todo try longer history = more levels of feedback to predictor
        // TOdo try giving the current classifier cells, which are a better more distinct signal than the small changes in the smoothed TP output?
        // TODO try giving it its own output trace, + level above ?trace or prediction?
        // TODO want to see the response from the input in L2, to see if it's modelling it correctly
        // TODO log FN errors with 1, 2, 3 layers over time. Add a dataqueue of the errors so I can get a picture of the FN error history
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
//            shuffleTrainingImages = true; // ********** DISABLE THIS LINE FOR IMAGES IN ORDER

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

        int trainingEpochs = 1000;//2000; //
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
        String valueSeriesErrorFnName   = Framework.GetEntityName( "value-series-error-fn" );
        String dataQueuePredictionName  = Framework.GetEntityName( "data-queue-prediction" );
        String dataQueueOutput1Name     = Framework.GetEntityName( "data-queue-output-1" );
        String dataQueueOutput2Name     = Framework.GetEntityName( "data-queue-output-2" );
        String dataQueueOutput3Name     = Framework.GetEntityName( "data-queue-output-3" );

        String classifier1Name           = Framework.GetEntityName( "classifier-1" );
        String classifier2Name           = Framework.GetEntityName( "classifier-2" );
        String classifier3Name           = Framework.GetEntityName( "classifier-3" );

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
//        String previousName = imageSourceName;
//        String learningEntitiesAlgorithm = "";
//        String lastRegionLayerName = "";
        ArrayList< String > learningEntities = new ArrayList< String >();

        String previousName = imageSourceName;
        Framework.CreateEntity( classifier1Name, QuiltedCompetitiveLearningEntity.ENTITY_TYPE, n.getName(), previousName );
        previousName = classifier1Name;
        learningEntities.add( previousName );

        Framework.CreateEntity( regionLayer1Name, PyramidRegionLayerEntity.ENTITY_TYPE, n.getName(), previousName );
        previousName = regionLayer1Name;
        learningEntities.add( previousName );

        Framework.CreateEntity( classifier2Name, QuiltedCompetitiveLearningEntity.ENTITY_TYPE, n.getName(), previousName );
        previousName = classifier2Name;
        learningEntities.add( previousName );

        Framework.CreateEntity( regionLayer2Name, PyramidRegionLayerEntity.ENTITY_TYPE, n.getName(), previousName );
        previousName = regionLayer2Name;
        learningEntities.add( previousName );

        Framework.CreateEntity( classifier3Name, QuiltedCompetitiveLearningEntity.ENTITY_TYPE, n.getName(), previousName );
        previousName = classifier3Name;
        learningEntities.add( previousName );

        Framework.CreateEntity( regionLayer3Name, PyramidRegionLayerEntity.ENTITY_TYPE, n.getName(), previousName );
        previousName = regionLayer3Name;
        learningEntities.add( previousName );
        // Region-layers

//        Framework.CreateEntity( classifierName, FeedForwardNetworkEntity.ENTITY_TYPE, n.getName(), previousName ); // ok all input to the regions is ready

        if( doLogging ) {
            Framework.CreateEntity( valueSeriesTruthName, ValueSeriesEntity.ENTITY_TYPE, n.getName(), previousName );
            Framework.CreateEntity( valueSeriesErrorFnName, ValueSeriesEntity.ENTITY_TYPE, n.getName(), previousName );

            Framework.CreateEntity( dataQueuePredictionName, DataQueueEntity.ENTITY_TYPE, n.getName(), previousName );
            Framework.CreateEntity( dataQueueOutput1Name, DataQueueEntity.ENTITY_TYPE, n.getName(), previousName );
            Framework.CreateEntity( dataQueueOutput2Name, DataQueueEntity.ENTITY_TYPE, n.getName(), previousName );
            Framework.CreateEntity( dataQueueOutput3Name, DataQueueEntity.ENTITY_TYPE, n.getName(), previousName );

            if( cacheAllData ) {
                Framework.SetConfig( valueSeriesTruthName, "cache", String.valueOf( cacheAllData ) );
                Framework.SetConfig( valueSeriesErrorFnName, "cache", String.valueOf( cacheAllData ) );
                Framework.SetConfig( dataQueuePredictionName, "cache", String.valueOf( cacheAllData ) );
                Framework.SetConfig( dataQueueOutput1Name, "cache", String.valueOf( cacheAllData ) );
                Framework.SetConfig( dataQueueOutput2Name, "cache", String.valueOf( cacheAllData ) );
                Framework.SetConfig( dataQueueOutput3Name, "cache", String.valueOf( cacheAllData ) );
            }

            Framework.SetConfig( dataQueuePredictionName, "queueLength", String.valueOf( queueLength ) );
            Framework.SetConfig( dataQueueOutput1Name, "queueLength", String.valueOf( queueLength ) );
            Framework.SetConfig( dataQueueOutput2Name, "queueLength", String.valueOf( queueLength ) );
            Framework.SetConfig( dataQueueOutput3Name, "queueLength", String.valueOf( queueLength ) );

            Framework.SetDataReference( dataQueuePredictionName, DataQueueEntity.DATA_INPUT, classifier1Name, QuiltedCompetitiveLearningEntity.OUTPUT_1 );
            Framework.SetDataReference( dataQueueOutput1Name, DataQueueEntity.DATA_INPUT, regionLayer1Name, PyramidRegionLayerEntity.OUTPUT );
            Framework.SetDataReference( dataQueueOutput2Name, DataQueueEntity.DATA_INPUT, regionLayer2Name, PyramidRegionLayerEntity.OUTPUT );
            Framework.SetDataReference( dataQueueOutput3Name, DataQueueEntity.DATA_INPUT, regionLayer3Name, PyramidRegionLayerEntity.OUTPUT );
        }

        // cache all data for speed, when enabled
        Framework.SetConfig( experimentName, "cache", String.valueOf( cacheAllData ) );
        Framework.SetConfig( imageSourceName, "cache", String.valueOf( cacheAllData ) );
//        Framework.SetConfig( regionLayer2Name, "cache", String.valueOf( cacheAllData ) );
//        Framework.SetConfig( regionLayer3Name, "cache", String.valueOf( cacheAllData ) );
  //      Framework.SetConfig( classifierName, "cache", String.valueOf( cacheAllData ) );

        // RL1
        // it is more stable with the current spikes only as feedback.. ?
        // The current spikes represent a sequence over time, due to the slow decay
        {
            Framework.SetDataReference( classifier1Name, QuiltedCompetitiveLearningEntity.INPUT_1, imageSourceName, ImageLabelEntity.OUTPUT_IMAGE );
            Framework.SetDataReference( classifier1Name, QuiltedCompetitiveLearningEntity.INPUT_2, constantName, ConstantMatrixEntity.OUTPUT );
            Framework.SetDataReference( classifier1Name, QuiltedCompetitiveLearningEntity.INPUT_QUILT, regionLayer1Name, PyramidRegionLayerEntity.PREDICTION_NEW );
            Framework.SetDataReference( regionLayer1Name, PyramidRegionLayerEntity.INPUT_C, classifier1Name, QuiltedCompetitiveLearningEntity.OUTPUT_QUILT );

            ArrayList< AbstractPair< String, String > > referenceEntitySuffixes = new ArrayList< AbstractPair< String, String > >();
// 1st order
//            referenceEntitySuffixes.add( new AbstractPair< String, String >( constantName, ConstantMatrixEntity.OUTPUT ) );
// Feedback for variable order
            referenceEntitySuffixes.add( new AbstractPair< String, String >( classifier2Name, QuiltedCompetitiveLearningEntity.OUTPUT_QUILT ) );
            referenceEntitySuffixes.add( new AbstractPair< String, String >( classifier3Name, QuiltedCompetitiveLearningEntity.OUTPUT_QUILT ) );
            Framework.SetDataReferences( regionLayer1Name, PyramidRegionLayerEntity.INPUT_P, referenceEntitySuffixes );
        }

        // RL2
        {
            Framework.SetDataReference( classifier2Name, QuiltedCompetitiveLearningEntity.INPUT_1, regionLayer1Name, PyramidRegionLayerEntity.OUTPUT );
            Framework.SetDataReference( classifier2Name, QuiltedCompetitiveLearningEntity.INPUT_2, constantName, ConstantMatrixEntity.OUTPUT );
            Framework.SetDataReference( classifier2Name, QuiltedCompetitiveLearningEntity.INPUT_QUILT, regionLayer2Name, PyramidRegionLayerEntity.PREDICTION_NEW );
            Framework.SetDataReference( regionLayer2Name, PyramidRegionLayerEntity.INPUT_C, classifier2Name, QuiltedCompetitiveLearningEntity.OUTPUT_QUILT );

            ArrayList< AbstractPair< String, String > > referenceEntitySuffixes = new ArrayList< AbstractPair< String, String > >();
            referenceEntitySuffixes.add( new AbstractPair< String, String >( classifier3Name, QuiltedCompetitiveLearningEntity.OUTPUT_QUILT ) );
            Framework.SetDataReferences( regionLayer2Name, PyramidRegionLayerEntity.INPUT_P, referenceEntitySuffixes );
        }

        // RL3
        {
            Framework.SetDataReference( classifier3Name, QuiltedCompetitiveLearningEntity.INPUT_1, regionLayer2Name, PyramidRegionLayerEntity.OUTPUT );
            Framework.SetDataReference( classifier3Name, QuiltedCompetitiveLearningEntity.INPUT_2, constantName, ConstantMatrixEntity.OUTPUT );
            Framework.SetDataReference( classifier3Name, QuiltedCompetitiveLearningEntity.INPUT_QUILT, regionLayer3Name, PyramidRegionLayerEntity.PREDICTION_NEW );
            Framework.SetDataReference( regionLayer3Name, PyramidRegionLayerEntity.INPUT_C, classifier3Name, QuiltedCompetitiveLearningEntity.OUTPUT_QUILT );

            ArrayList< AbstractPair< String, String > > referenceEntitySuffixes = new ArrayList< AbstractPair< String, String > >();
            referenceEntitySuffixes.add( new AbstractPair< String, String >( constantName, ConstantMatrixEntity.OUTPUT ) );
            Framework.SetDataReferences( regionLayer3Name, PyramidRegionLayerEntity.INPUT_P, referenceEntitySuffixes );
        }

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

            Framework.SetConfig( valueSeriesErrorFnName, "period", "-1" );
            Framework.SetConfig( valueSeriesErrorFnName, "entityName", regionLayer1Name );
            Framework.SetConfig( valueSeriesErrorFnName, "configPath", "sumPredictionErrorFN" );
        }

        // region layer config
        // effective constants:

//        float classifierLearningRate = 0.03f;//0.005f;// 0.01
//        float classifierMomentum = 0f;
//        float classifierWeightsStdDev = 0.01f;
//        int classifierAgeMin = 0; // age of disuse where we start to promote cells
//        int classifierAgeMax = 4000;//1000;//2000/1000; // age of disuse where we maximally promote cells
//        float classifierAgeTruncationFactor = 0.5f;
//        float classifierAgeScale = 12f; // promotion nonlinearity
//        float classifierRateScale = 5f; // inhibition nonlinearity
//        float classifierRateMax = 0.15f;//0.25f; // i.e. never more than 1 in 4.
//        float classifierRateLearningRate = 0.005f; // how fast the measurement of rate of cell use changes.
        int classifierEdgeMaxAge = 500;
        int classifierGrowthInterval = 50;
        float classifierLearningRate = 0.01f;
        float classifierLearningRateNeighbours = classifierLearningRate * 0.2f;
        float classifierNoiseMagnitude = 0.0f;
        float classifierStressLearningRate = 0.01f; // not used now?
        float classifierStressSplitLearningRate = 0.5f; // change to stress after a split
        float classifierStressThreshold = 0.01f; // when it ceases to split

        float predictorLearningRate = 0.01f; // 1/5 the classifier learning rate
        int predictorHiddenCells = 500;
        int predictorBatchSize = 1; //64; =32, at 80k not very trained. CBF waiting so long.
        float predictorLeakiness = 0.01f;
        float predictorRegularization = 0.f;

//        float outputCodingSparsityFactor = 2.0f; // how sticky the output is, ie a trace of historical errors
//                                      //       1  2    3     4     5     6     7
//        float outputDecayRate = 0.6f; // 0.8:  1, 0.8, 0.64, 0.51, 0.40, 0.32, 0.26,
//                                      // 0.6:  1, 0.6, 0.36, 0.21, 0.12, 0.07, 0.04,
        float outputDecayRate = 0.98f;//0.95f;
        // 0.6:  1, 0.6, 0.36, 0.21, 0.12, 0.07, 0.04,

        // 25 * 49 = 1225
        int columnWidthCells = 5;  // 25 cells per col
        int columnHeightCells = 5;

        int quiltWidthColumns = 7;
        int quiltHeightColumns = 7; // 49 cols

        int quiltWidthCells = columnWidthCells * quiltWidthColumns;
        int quiltHeightCells = columnHeightCells * quiltHeightColumns; // 5*7=35 ^2 = 1225

        PyramidRegionLayerEntityConfig prlec = new PyramidRegionLayerEntityConfig();
        prlec.cache = true;
        prlec.widthCells = quiltWidthCells;
        prlec.heightCells = quiltHeightCells;
        prlec.columnWidthCells = columnWidthCells;
        prlec.columnHeightCells = columnHeightCells;
        prlec.predictorLearningRate = predictorLearningRate;
        prlec.predictorHiddenCells = predictorHiddenCells;
        prlec.predictorLeakiness = predictorLeakiness;
        prlec.predictorRegularization = predictorRegularization;
        prlec.predictorBatchSize = predictorBatchSize;
        prlec.outputDecayRate = outputDecayRate;

        // Field 2: 28x28
        // TODO allow these to be offset so they can be shuffled towards centre
        //     00 01 02 03 04 05 06 07 08 09 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 |
        //  F1 -- -- -- -- -- --                                                                   |
        //  F2             -- -- -- -- -- --                                                       |
        //  F3                         -- -- -- -- -- --                                           |
        //  F4                                     -- -- -- -- -- --                               |
        //  F5                                                 -- -- -- -- -- --                   |
        //  F6                                                             -- -- -- -- -- --       |
        //  F7                                                                         -- -- -- -- -- --
        int field1SizeX = 6;
        int field1SizeY = 6;

        int field1StrideX = 4;
        int field1StrideY = 4;

        // Field 2: 1x1
        int field2StrideX = 0;
        int field2StrideY = 0;

        int field2SizeX = 1;
        int field2SizeY = 1;

        QuiltedCompetitiveLearningEntityConfig qclec = new QuiltedCompetitiveLearningEntityConfig();
        qclec.cache = true;
        qclec.quiltWidth = quiltWidthColumns;
        qclec.quiltHeight = quiltHeightColumns;
        qclec.classifierWidth = columnWidthCells;
        qclec.classifierHeight = columnHeightCells;

        qclec.field1StrideX = field1StrideX;
        qclec.field1StrideY = field1StrideY;
        qclec.field2StrideX = field2StrideX;
        qclec.field2StrideY = field2StrideY;
        qclec.field1SizeX = field1SizeX;
        qclec.field1SizeY = field1SizeY;
        qclec.field2SizeX = field2SizeX;
        qclec.field2SizeY = field2SizeY;

        qclec.classifierLearningRate = classifierLearningRate;
        qclec.classifierLearningRateNeighbours = classifierLearningRateNeighbours;
        qclec.classifierNoiseMagnitude = classifierNoiseMagnitude;
        qclec.classifierEdgeMaxAge = classifierEdgeMaxAge;
        qclec.classifierStressLearningRate = classifierStressLearningRate;
        qclec.classifierStressSplitLearningRate = classifierStressSplitLearningRate;
        qclec.classifierStressThreshold = classifierStressThreshold;
        qclec.classifierGrowthInterval = classifierGrowthInterval;

        // variables
        Framework.SetConfig( classifier1Name, qclec );

        //     00 01 02 03 04 05 06 07 08 09 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30 31 32 33 34
        //     cc cc cc cc cc CC_CC_CC_CC_CC cc-cc-cc-cc-cc CC_CC_CC_CC_CC cc-cc-cc-cc-cc CC_CC_CC_CC_CC cc-cc-cc-cc-cc
        //  F1 -- -- -- -- -- -- -- -- -- --
        //  F2                -- -- -- -- -- -- -- -- -- --
        //  F3                               -- -- -- -- -- -- -- -- -- --
        //  F4                                              -- -- -- -- -- -- -- -- -- --
        //  F5                                                             -- -- -- -- -- -- -- -- -- --
        //  F6                                                                            -- -- -- -- -- -- -- -- -- --
        qclec.field1StrideX = columnWidthCells;
        qclec.field1StrideY = columnHeightCells;
        qclec.field1SizeX = columnWidthCells * 2;
        qclec.field1SizeY = columnHeightCells * 2;
        qclec.quiltWidth = 6;
        qclec.quiltHeight = 6;

        Framework.SetConfig( classifier2Name, qclec );

        //     00 01 02 03 04 05 06 07 08 09 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29
        //     cc cc cc cc cc CC_CC_CC_CC_CC cc-cc-cc-cc-cc CC_CC_CC_CC_CC cc-cc-cc-cc-cc CC_CC_CC_CC_CC
        //  F1 -- -- -- -- -- -- -- -- -- --
        //  F2                -- -- -- -- -- -- -- -- -- --
        //  F3                               -- -- -- -- -- -- -- -- -- --
        //  F4                                              -- -- -- -- -- -- -- -- -- --
        //  F5                                                             -- -- -- -- -- -- -- -- -- --
        qclec.quiltWidth = 5;
        qclec.quiltHeight = 5;

        Framework.SetConfig( classifier3Name, qclec );

        Framework.SetConfig( regionLayer1Name, prlec );
        Framework.SetConfig( regionLayer2Name, prlec );
        Framework.SetConfig( regionLayer3Name, prlec );

    }

}
