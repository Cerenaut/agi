/*
 * Copyright (c) 2017.
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

package io.agi.framework.demo.sd19;

import io.agi.core.orm.AbstractPair;
import io.agi.core.util.PropertiesUtil;
import io.agi.core.util.images.BufferedImageSource.BufferedImageSourceFactory;
import io.agi.framework.Framework;
import io.agi.framework.Main;
import io.agi.framework.Naming;
import io.agi.framework.Node;
import io.agi.framework.demo.mnist.ImageLabelEntity;
import io.agi.framework.entities.*;
import io.agi.framework.factories.CommonEntityFactory;
import io.agi.framework.persistence.PersistenceUtil;
import io.agi.framework.references.DataRefUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

/**
 * Created by dave on 8/07/16.
 */
public class PyramidRegionLayerLabelsDemo2 {

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

//        TESTS:
//        test prediction of sequence learning ( in progress )  22-feb-17 variable order looking good on quick.txt
//        test classification of characters (in progress) 22-feb-17 looks good
//        test higher order prediction of sequence (with fixed images?) 22-feb-17 variable order looking good on quick.txt
//        test higher order prediction of sequence ( mutable images?) READY.
        // to do try rank based prediction not value DONE
        // Seems to be getting confused on start of words. Does this mean not enough history? Try changing TP factor?
        // To do try longer history = more levels of feedback to predictor
        // TO do try giving the current classifier cells, which are a better more distinct signal than the small changes in the smoothed TP output?
        // TO DO try giving it its own output trace, + level above ?trace or prediction?
        // TO DO want to see the response from the input in L2, to see if it's modelling it correctly
        // TODO log FN errors with 1, 2, 3 layers over time. Add a dataqueue of the errors so I can get a picture of the FN error history
        // Temporal pooling is working. The info should be there.
        // TODO Try more cells in predictor.
        // there are 10 * 46 transitions = 460 pairs
        // sequences of length 3 have 10*10*10=1000 variations
        // sequences of length 4 have 10,000 variations
        // sequences of length 5 have 100,000 variations
        // sequences of length 6 have 1,000,000 possible paths.
        //      Higher cells represent longer history. So dont need current + output, only output.
        //      level above predictor gets ...?
        // connect the right feedback for prediction?
        // use density-dependent forgetting factor in output TP

// NEW IDEA: The concept only has to be usefully represented in a distributed way. Therefore we can test by making it the reward to predict the correct next letter.
// So build the 2nd region-layer stack to do just that. Make the output a distribution over the predictable letters. Build the central hub to coordinate and teach it.
// Initially can be just one layer but can build on the behaviour later.

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
//            imagesPathTraining = "/home/dave/workspace/agi.io/data/nist-sd19/a2z_10x";
//            imagesPathTesting = "/home/dave/workspace/agi.io/data/nist-sd19/a2z_10x";
            imagesPathTraining = "/home/dave/workspace/agi.io/data/nist-sd19/a2z_100x";
            imagesPathTesting = "/home/dave/workspace/agi.io/data/nist-sd19/a2z_100x";
        }

        // after a very long time, it will not be able to predict the appearance of digits, but it should know which digit is next.
        // i.e. FNs will not reach zero. But the appearance of the digits should be predicted recognizably.
        // Q and L seem hard to predict.
        // 0123456 - 6 step history needed
        // . the quick brown fox jumps ove
        // r the lazy dog.

        int trainingEpochs = 2000;//1000;//2000; //
        int testingEpochs = 1;//10;//80; // good for up to 80k
        int terminationAge = 5000;//25000;
        boolean terminateByAge = false;
        int imageRepeats = 1;//50;
        boolean cacheAllData = true;
        boolean doLogging = true;
        int queueLength = 46;

        String predictorEntityType = FeedForwardNetworkQuiltPredictorEntity.ENTITY_TYPE;
//        String predictorEntityType = HebbianQuiltPredictorEntity.ENTITY_TYPE;

        // Define some entities
        String experimentName           = PersistenceUtil.GetEntityName( "experiment" );
        String constantName             = PersistenceUtil.GetEntityName( "constant" );
        String imageSourceName          = PersistenceUtil.GetEntityName( "image-source" );
        String valueSeriesTruthName     = PersistenceUtil.GetEntityName( "value-series-truth" );
        String valueSeriesErrorFnName   = PersistenceUtil.GetEntityName( "value-series-error-fn" );
        String dataQueuePredictionName  = PersistenceUtil.GetEntityName( "data-queue-prediction" );

        String dataQueueOutput1Name     = PersistenceUtil.GetEntityName( "data-queue-output-1" );
        String dataQueueOutput2Name     = PersistenceUtil.GetEntityName( "data-queue-output-2" );
        String dataQueueOutput3Name     = PersistenceUtil.GetEntityName( "data-queue-output-3" );
        String dataQueueOutput4Name     = PersistenceUtil.GetEntityName( "data-queue-output-4" );
        String dataQueueOutput5Name     = PersistenceUtil.GetEntityName( "data-queue-output-5" );
        String dataQueueOutput6Name     = PersistenceUtil.GetEntityName( "data-queue-output-6" );

        String classifier1Name           = PersistenceUtil.GetEntityName( "classifier-1" );
        String classifier2Name           = PersistenceUtil.GetEntityName( "classifier-2" );
        String classifier3Name           = PersistenceUtil.GetEntityName( "classifier-3" );
        String classifier4Name           = PersistenceUtil.GetEntityName( "classifier-4" );
        String classifier5Name           = PersistenceUtil.GetEntityName( "classifier-5" );
        String classifier6Name           = PersistenceUtil.GetEntityName( "classifier-6" );

        String predictor1Name         = PersistenceUtil.GetEntityName( "predictor-1" );
        String predictor2Name         = PersistenceUtil.GetEntityName( "predictor-2" );
        String predictor3Name         = PersistenceUtil.GetEntityName( "predictor-3" );
        String predictor4Name         = PersistenceUtil.GetEntityName( "predictor-4" );
        String predictor5Name         = PersistenceUtil.GetEntityName( "predictor-5" );
        String predictor6Name         = PersistenceUtil.GetEntityName( "predictor-6" );

        String pooler1Name         = PersistenceUtil.GetEntityName( "pooler-1" );
        String pooler2Name         = PersistenceUtil.GetEntityName( "pooler-2" );
        String pooler3Name         = PersistenceUtil.GetEntityName( "pooler-3" );
        String pooler4Name         = PersistenceUtil.GetEntityName( "pooler-4" );
        String pooler5Name         = PersistenceUtil.GetEntityName( "pooler-5" );
        String pooler6Name         = PersistenceUtil.GetEntityName( "pooler-6" );

        PersistenceUtil.CreateEntity( experimentName, ExperimentEntity.ENTITY_TYPE, n.getName(), null ); // experiment is the root entity
        PersistenceUtil.CreateEntity( constantName, ConstantMatrixEntity.ENTITY_TYPE, n.getName(), experimentName ); // ok all input to the regions is ready

        if( testType == TEST_TYPE_IMAGE_SEQUENCE ) {
            PersistenceUtil.CreateEntity( imageSourceName, ImageLabelEntity.ENTITY_TYPE, n.getName(), constantName );
        }
        else if( testType == TEST_TYPE_TEXT_SEQUENCE ) {
            PersistenceUtil.CreateEntity( imageSourceName, Text2ImageLabelEntity.ENTITY_TYPE, n.getName(), constantName );
        }

        // Region-layers
//        String previousName = imageSourceName;
//        String learningEntitiesAlgorithm = "";
//        String lastRegionLayerName = "";
        ArrayList< String > learningEntities = new ArrayList< String >();

        String previousName = imageSourceName;
/*        PersistenceUtil.CreateEntity( classifier1Name, QuiltedCompetitiveLearningEntity.ENTITY_TYPE, n.getName(), previousName );
        previousName = classifier1Name;
        learningEntities.add( previousName );

        PersistenceUtil.CreateEntity( predictor1Name, QuiltPredictorEntity.ENTITY_TYPE, n.getName(), previousName );
        previousName = predictor1Name;
        learningEntities.add( previousName );

        PersistenceUtil.CreateEntity( pooler1Name, PredictiveCodingEntity.ENTITY_TYPE, n.getName(), previousName );
        previousName = pooler1Name;
        learningEntities.add( previousName );

        PersistenceUtil.CreateEntity( classifier2Name, QuiltedCompetitiveLearningEntity.ENTITY_TYPE, n.getName(), previousName );
        previousName = classifier2Name;
        learningEntities.add( previousName );

        PersistenceUtil.CreateEntity( predictor2Name, QuiltPredictorEntity.ENTITY_TYPE, n.getName(), previousName );
        previousName = predictor2Name;
        learningEntities.add( previousName );

        PersistenceUtil.CreateEntity( pooler2Name, PredictiveCodingEntity.ENTITY_TYPE, n.getName(), previousName );
        previousName = pooler2Name;
        learningEntities.add( previousName );

        PersistenceUtil.CreateEntity( classifier3Name, QuiltedCompetitiveLearningEntity.ENTITY_TYPE, n.getName(), previousName );
        previousName = classifier3Name;
        learningEntities.add( previousName );

        PersistenceUtil.CreateEntity( predictor3Name, QuiltPredictorEntity.ENTITY_TYPE, n.getName(), previousName );
        previousName = predictor3Name;
        learningEntities.add( previousName );

        PersistenceUtil.CreateEntity( pooler3Name, PredictiveCodingEntity.ENTITY_TYPE, n.getName(), previousName );
        previousName = pooler3Name;
        learningEntities.add( previousName );

        PersistenceUtil.CreateEntity( classifier4Name, QuiltedCompetitiveLearningEntity.ENTITY_TYPE, n.getName(), previousName );
        previousName = classifier4Name;
        learningEntities.add( previousName );

        PersistenceUtil.CreateEntity( predictor4Name, QuiltPredictorEntity.ENTITY_TYPE, n.getName(), previousName );
        previousName = predictor4Name;
        learningEntities.add( previousName );

        PersistenceUtil.CreateEntity( pooler4Name, PredictiveCodingEntity.ENTITY_TYPE, n.getName(), previousName );
        previousName = pooler4Name;
        learningEntities.add( previousName );

        PersistenceUtil.CreateEntity( classifier5Name, QuiltedCompetitiveLearningEntity.ENTITY_TYPE, n.getName(), previousName );
        previousName = classifier5Name;
        learningEntities.add( previousName );

        PersistenceUtil.CreateEntity( predictor5Name, QuiltPredictorEntity.ENTITY_TYPE, n.getName(), previousName );
        previousName = predictor5Name;
        learningEntities.add( previousName );

        PersistenceUtil.CreateEntity( pooler5Name, PredictiveCodingEntity.ENTITY_TYPE, n.getName(), previousName );
        previousName = pooler5Name;
        learningEntities.add( previousName );

        PersistenceUtil.CreateEntity( classifier6Name, QuiltedCompetitiveLearningEntity.ENTITY_TYPE, n.getName(), previousName );
        previousName = classifier6Name;
        learningEntities.add( previousName );

        PersistenceUtil.CreateEntity( predictor6Name, QuiltPredictorEntity.ENTITY_TYPE, n.getName(), previousName );
        previousName = predictor6Name;
        learningEntities.add( previousName );

        PersistenceUtil.CreateEntity( pooler6Name, PredictiveCodingEntity.ENTITY_TYPE, n.getName(), previousName );
        previousName = pooler6Name;
        learningEntities.add( previousName );*/
        // Region-layers

        String ffInputDataSuffix = PredictiveCodingEntity.OUTPUT;
        String fbInputDataSuffix = QuiltedCompetitiveLearningEntity.OUTPUT_QUILT;

        ArrayList< AbstractPair< String, String > > feedbackEntitySuffixes1 = new ArrayList< AbstractPair< String, String > >();
        ArrayList< AbstractPair< String, String > > feedbackEntitySuffixes2_6 = new ArrayList< AbstractPair< String, String > >();

// Feedback for variable order
//        feedbackEntitySuffixes1.add( new AbstractPair< String, String >( constantName, ConstantMatrixEntity.OUTPUT ) );
        feedbackEntitySuffixes1.add( new AbstractPair< String, String >( classifier2Name, fbInputDataSuffix ) );
//            feedbackEntitySuffixes1.add( new AbstractPair< String, String >( classifier3Name, fbInputDataSuffix ) );
//            feedbackEntitySuffixes1.add( new AbstractPair< String, String >( classifier4Name, fbInputDataSuffix ) );
//            feedbackEntitySuffixes1.add( new AbstractPair< String, String >( classifier5Name, fbInputDataSuffix ) );
//            feedbackEntitySuffixes1.add( new AbstractPair< String, String >( classifier6Name, fbInputDataSuffix ) );

        feedbackEntitySuffixes2_6.add( new AbstractPair< String, String >( constantName, ConstantMatrixEntity.OUTPUT ) );

        previousName = createRegionLayerAndDataReferences(
                n, previousName,
                classifier1Name, predictor1Name, pooler1Name,
                predictorEntityType,
                learningEntities,
                imageSourceName, ImageLabelEntity.OUTPUT_IMAGE,
                constantName, ConstantMatrixEntity.OUTPUT,
                feedbackEntitySuffixes1 );

        previousName = createRegionLayerAndDataReferences(
                n, previousName,
                classifier2Name, predictor2Name, pooler2Name,
                predictorEntityType,
                learningEntities,
                pooler1Name, ffInputDataSuffix,
                constantName, ConstantMatrixEntity.OUTPUT,
                feedbackEntitySuffixes2_6 );
/*
        previousName = createRegionLayerAndDataReferences(
                n, previousName,
                classifier3Name, predictor3Name, pooler3Name,
                predictorEntityType,
                learningEntities,
                pooler2Name, ffInputDataSuffix,
                constantName, ConstantMatrixEntity.OUTPUT,
                feedbackEntitySuffixes2_6 );

        previousName = createRegionLayerAndDataReferences(
                n, previousName,
                classifier4Name, predictor4Name, pooler4Name,
                predictorEntityType,
                learningEntities,
                pooler3Name, ffInputDataSuffix,
                constantName, ConstantMatrixEntity.OUTPUT,
                feedbackEntitySuffixes2_6 );

        previousName = createRegionLayerAndDataReferences(
                n, previousName,
                classifier5Name, predictor5Name, pooler5Name,
                predictorEntityType,
                learningEntities,
                pooler4Name, ffInputDataSuffix,
                constantName, ConstantMatrixEntity.OUTPUT,
                feedbackEntitySuffixes2_6 );

        previousName = createRegionLayerAndDataReferences(
                n, previousName,
                classifier6Name, predictor6Name, pooler6Name,
                predictorEntityType,
                learningEntities,
                pooler5Name, ffInputDataSuffix,
                constantName, ConstantMatrixEntity.OUTPUT,
                feedbackEntitySuffixes2_6 );*/

        if( doLogging ) {
            PersistenceUtil.CreateEntity( valueSeriesTruthName, ValueSeriesEntity.ENTITY_TYPE, n.getName(), previousName );
            PersistenceUtil.CreateEntity( valueSeriesErrorFnName, ValueSeriesEntity.ENTITY_TYPE, n.getName(), previousName );

            PersistenceUtil.CreateEntity( dataQueuePredictionName, DataQueueEntity.ENTITY_TYPE, n.getName(), previousName );
            PersistenceUtil.CreateEntity( dataQueueOutput1Name, DataQueueEntity.ENTITY_TYPE, n.getName(), previousName );
//            PersistenceUtil.CreateEntity( dataQueueOutput2Name, DataQueueEntity.ENTITY_TYPE, n.getName(), previousName );
//            PersistenceUtil.CreateEntity( dataQueueOutput3Name, DataQueueEntity.ENTITY_TYPE, n.getName(), previousName );
//            PersistenceUtil.CreateEntity( dataQueueOutput4Name, DataQueueEntity.ENTITY_TYPE, n.getName(), previousName );
//            PersistenceUtil.CreateEntity( dataQueueOutput5Name, DataQueueEntity.ENTITY_TYPE, n.getName(), previousName );
//            PersistenceUtil.CreateEntity( dataQueueOutput6Name, DataQueueEntity.ENTITY_TYPE, n.getName(), previousName );

            if( cacheAllData ) {
                PersistenceUtil.SetConfig( valueSeriesTruthName, "cache", String.valueOf( cacheAllData ) );
                PersistenceUtil.SetConfig( valueSeriesErrorFnName, "cache", String.valueOf( cacheAllData ) );
                PersistenceUtil.SetConfig( dataQueuePredictionName, "cache", String.valueOf( cacheAllData ) );
                PersistenceUtil.SetConfig( dataQueueOutput1Name, "cache", String.valueOf( cacheAllData ) );
//                PersistenceUtil.SetConfig( dataQueueOutput2Name, "cache", String.valueOf( cacheAllData ) );
//                PersistenceUtil.SetConfig( dataQueueOutput3Name, "cache", String.valueOf( cacheAllData ) );
//                PersistenceUtil.SetConfig( dataQueueOutput4Name, "cache", String.valueOf( cacheAllData ) );
//                PersistenceUtil.SetConfig( dataQueueOutput5Name, "cache", String.valueOf( cacheAllData ) );
//                PersistenceUtil.SetConfig( dataQueueOutput6Name, "cache", String.valueOf( cacheAllData ) );
            }

            PersistenceUtil.SetConfig( dataQueuePredictionName, "queueLength", String.valueOf( queueLength ) );
            PersistenceUtil.SetConfig( dataQueueOutput1Name, "queueLength", String.valueOf( queueLength ) );
//            PersistenceUtil.SetConfig( dataQueueOutput2Name, "queueLength", String.valueOf( queueLength ) );
//            PersistenceUtil.SetConfig( dataQueueOutput3Name, "queueLength", String.valueOf( queueLength ) );
//            PersistenceUtil.SetConfig( dataQueueOutput4Name, "queueLength", String.valueOf( queueLength ) );
//            PersistenceUtil.SetConfig( dataQueueOutput5Name, "queueLength", String.valueOf( queueLength ) );
//            PersistenceUtil.SetConfig( dataQueueOutput6Name, "queueLength", String.valueOf( queueLength ) );

            DataRefUtil.SetDataReference( dataQueuePredictionName, DataQueueEntity.DATA_INPUT, classifier1Name, QuiltedCompetitiveLearningEntity.OUTPUT_1 );
            DataRefUtil.SetDataReference( dataQueueOutput1Name, DataQueueEntity.DATA_INPUT, pooler1Name, fbInputDataSuffix );
//            DataRefUtil.SetDataReference( dataQueueOutput2Name, DataQueueEntity.DATA_INPUT, pooler2Name, fbInputDataSuffix );
//            DataRefUtil.SetDataReference( dataQueueOutput3Name, DataQueueEntity.DATA_INPUT, pooler3Name, fbInputDataSuffix );
//            DataRefUtil.SetDataReference( dataQueueOutput4Name, DataQueueEntity.DATA_INPUT, pooler4Name, fbInputDataSuffix );
//            DataRefUtil.SetDataReference( dataQueueOutput5Name, DataQueueEntity.DATA_INPUT, pooler5Name, fbInputDataSuffix );
//            DataRefUtil.SetDataReference( dataQueueOutput6Name, DataQueueEntity.DATA_INPUT, pooler6Name, fbInputDataSuffix );
        }

        // cache all data for speed, when enabled
        PersistenceUtil.SetConfig( experimentName, "cache", String.valueOf( cacheAllData ) );
        PersistenceUtil.SetConfig( imageSourceName, "cache", String.valueOf( cacheAllData ) );

        // RL1
        // it is more stable with the current spikes only as feedback.. ?
        // The current spikes represent a sequence over time, due to the slow decay
/*        {
            DataRefUtil.SetDataReference( classifier1Name, QuiltedCompetitiveLearningEntity.INPUT_1, imageSourceName, ImageLabelEntity.OUTPUT_IMAGE );
            DataRefUtil.SetDataReference( classifier1Name, QuiltedCompetitiveLearningEntity.INPUT_2, constantName, ConstantMatrixEntity.OUTPUT );
            DataRefUtil.SetDataReference( classifier1Name, QuiltedCompetitiveLearningEntity.INPUT_QUILT, regionLayer1Name, PyramidRegionLayerEntity.PREDICTION_NEW );
            DataRefUtil.SetDataReference( regionLayer1Name, PyramidRegionLayerEntity.INPUT_C, classifier1Name, QuiltedCompetitiveLearningEntity.OUTPUT_QUILT );

            ArrayList< AbstractPair< String, String > > referenceEntitySuffixes = new ArrayList< AbstractPair< String, String > >();
// 1st order
//            referenceEntitySuffixes.add( new AbstractPair< String, String >( constantName, ConstantMatrixEntity.OUTPUT ) );
// Feedback for variable order
            referenceEntitySuffixes.add( new AbstractPair< String, String >( classifier2Name, QuiltedCompetitiveLearningEntity.OUTPUT_QUILT ) );
//            referenceEntitySuffixes.add( new AbstractPair< String, String >( classifier3Name, QuiltedCompetitiveLearningEntity.OUTPUT_QUILT ) );
//            referenceEntitySuffixes.add( new AbstractPair< String, String >( classifier4Name, QuiltedCompetitiveLearningEntity.OUTPUT_QUILT ) );
//            referenceEntitySuffixes.add( new AbstractPair< String, String >( classifier5Name, QuiltedCompetitiveLearningEntity.OUTPUT_QUILT ) );
//            referenceEntitySuffixes.add( new AbstractPair< String, String >( classifier6Name, QuiltedCompetitiveLearningEntity.OUTPUT_QUILT ) );
            DataRefUtil.SetDataReferences( regionLayer1Name, PyramidRegionLayerEntity.INPUT_P, referenceEntitySuffixes );
        }

        // RL2
        {
            DataRefUtil.SetDataReference( classifier2Name, QuiltedCompetitiveLearningEntity.INPUT_1, regionLayer1Name, PyramidRegionLayerEntity.OUTPUT );
            DataRefUtil.SetDataReference( classifier2Name, QuiltedCompetitiveLearningEntity.INPUT_2, constantName, ConstantMatrixEntity.OUTPUT );
            DataRefUtil.SetDataReference( classifier2Name, QuiltedCompetitiveLearningEntity.INPUT_QUILT, regionLayer2Name, PyramidRegionLayerEntity.PREDICTION_NEW );
            DataRefUtil.SetDataReference( regionLayer2Name, PyramidRegionLayerEntity.INPUT_C, classifier2Name, QuiltedCompetitiveLearningEntity.OUTPUT_QUILT );

            ArrayList< AbstractPair< String, String > > referenceEntitySuffixes = new ArrayList< AbstractPair< String, String > >();
            referenceEntitySuffixes.add( new AbstractPair< String, String >( classifier3Name, QuiltedCompetitiveLearningEntity.OUTPUT_QUILT ) );
            DataRefUtil.SetDataReferences( regionLayer2Name, PyramidRegionLayerEntity.INPUT_P, referenceEntitySuffixes );
        }

        // RL3
        {
            DataRefUtil.SetDataReference( classifier3Name, QuiltedCompetitiveLearningEntity.INPUT_1, regionLayer2Name, PyramidRegionLayerEntity.OUTPUT );
            DataRefUtil.SetDataReference( classifier3Name, QuiltedCompetitiveLearningEntity.INPUT_2, constantName, ConstantMatrixEntity.OUTPUT );
            DataRefUtil.SetDataReference( classifier3Name, QuiltedCompetitiveLearningEntity.INPUT_QUILT, regionLayer3Name, PyramidRegionLayerEntity.PREDICTION_NEW );
            DataRefUtil.SetDataReference( regionLayer3Name, PyramidRegionLayerEntity.INPUT_C, classifier3Name, QuiltedCompetitiveLearningEntity.OUTPUT_QUILT );

            ArrayList< AbstractPair< String, String > > referenceEntitySuffixes = new ArrayList< AbstractPair< String, String > >();
            referenceEntitySuffixes.add( new AbstractPair< String, String >( constantName, ConstantMatrixEntity.OUTPUT ) );
            DataRefUtil.SetDataReferences( regionLayer3Name, PyramidRegionLayerEntity.INPUT_P, referenceEntitySuffixes );
        }

        // RL 4
        {
            DataRefUtil.SetDataReference( classifier4Name, QuiltedCompetitiveLearningEntity.INPUT_1, regionLayer3Name, PyramidRegionLayerEntity.OUTPUT );
            DataRefUtil.SetDataReference( classifier4Name, QuiltedCompetitiveLearningEntity.INPUT_2, constantName, ConstantMatrixEntity.OUTPUT );
            DataRefUtil.SetDataReference( classifier4Name, QuiltedCompetitiveLearningEntity.INPUT_QUILT, regionLayer4Name, PyramidRegionLayerEntity.PREDICTION_NEW );
            DataRefUtil.SetDataReference( regionLayer4Name, PyramidRegionLayerEntity.INPUT_C, classifier4Name, QuiltedCompetitiveLearningEntity.OUTPUT_QUILT );

            ArrayList< AbstractPair< String, String > > referenceEntitySuffixes = new ArrayList< AbstractPair< String, String > >();
            referenceEntitySuffixes.add( new AbstractPair< String, String >( constantName, ConstantMatrixEntity.OUTPUT ) );
            DataRefUtil.SetDataReferences( regionLayer4Name, PyramidRegionLayerEntity.INPUT_P, referenceEntitySuffixes );
        }

        // RL 5
        {
            DataRefUtil.SetDataReference( classifier5Name, QuiltedCompetitiveLearningEntity.INPUT_1, regionLayer4Name, PyramidRegionLayerEntity.OUTPUT );
            DataRefUtil.SetDataReference( classifier5Name, QuiltedCompetitiveLearningEntity.INPUT_2, constantName, ConstantMatrixEntity.OUTPUT );
            DataRefUtil.SetDataReference( classifier5Name, QuiltedCompetitiveLearningEntity.INPUT_QUILT, regionLayer5Name, PyramidRegionLayerEntity.PREDICTION_NEW );
            DataRefUtil.SetDataReference( regionLayer5Name, PyramidRegionLayerEntity.INPUT_C, classifier5Name, QuiltedCompetitiveLearningEntity.OUTPUT_QUILT );

            ArrayList< AbstractPair< String, String > > referenceEntitySuffixes = new ArrayList< AbstractPair< String, String > >();
            referenceEntitySuffixes.add( new AbstractPair< String, String >( constantName, ConstantMatrixEntity.OUTPUT ) );
            DataRefUtil.SetDataReferences( regionLayer5Name, PyramidRegionLayerEntity.INPUT_P, referenceEntitySuffixes );
        }

        // RL 6
        {
            DataRefUtil.SetDataReference( classifier6Name, QuiltedCompetitiveLearningEntity.INPUT_1, regionLayer5Name, PyramidRegionLayerEntity.OUTPUT );
            DataRefUtil.SetDataReference( classifier6Name, QuiltedCompetitiveLearningEntity.INPUT_2, constantName, ConstantMatrixEntity.OUTPUT );
            DataRefUtil.SetDataReference( classifier6Name, QuiltedCompetitiveLearningEntity.INPUT_QUILT, regionLayer6Name, PyramidRegionLayerEntity.PREDICTION_NEW );
            DataRefUtil.SetDataReference( regionLayer6Name, PyramidRegionLayerEntity.INPUT_C, classifier6Name, QuiltedCompetitiveLearningEntity.OUTPUT_QUILT );

            ArrayList< AbstractPair< String, String > > referenceEntitySuffixes = new ArrayList< AbstractPair< String, String > >();
            referenceEntitySuffixes.add( new AbstractPair< String, String >( constantName, ConstantMatrixEntity.OUTPUT ) );
            DataRefUtil.SetDataReferences( regionLayer6Name, PyramidRegionLayerEntity.INPUT_P, referenceEntitySuffixes );
        }*/

        // Experiment config
        if( !terminateByAge ) {
            PersistenceUtil.SetConfig( experimentName, "terminationEntityName", imageSourceName );
            PersistenceUtil.SetConfig( experimentName, "terminationConfigPath", "terminate" );
            PersistenceUtil.SetConfig( experimentName, "terminationAge", "-1" ); // wait for mnist to decide
        }
        else {
            PersistenceUtil.SetConfig( experimentName, "terminationAge", String.valueOf( terminationAge ) ); // fixed steps
        }

        // Mnist config
        PersistenceUtil.SetConfig( imageSourceName, "receptiveField.receptiveFieldX", "0" );
        PersistenceUtil.SetConfig( imageSourceName, "receptiveField.receptiveFieldY", "0" );
        PersistenceUtil.SetConfig( imageSourceName, "receptiveField.receptiveFieldW", "28" );
        PersistenceUtil.SetConfig( imageSourceName, "receptiveField.receptiveFieldH", "28" );
        PersistenceUtil.SetConfig( imageSourceName, "resolution.resolutionX", "28" );
        PersistenceUtil.SetConfig( imageSourceName, "resolution.resolutionY", "28" );
        PersistenceUtil.SetConfig( imageSourceName, "greyscale", "true" );
        PersistenceUtil.SetConfig( imageSourceName, "invert", "true" );
        PersistenceUtil.SetConfig( imageSourceName, "sourceType", BufferedImageSourceFactory.TYPE_IMAGE_FILES );
        PersistenceUtil.SetConfig( imageSourceName, "sourceFilesPrefix", "postproc" );
        PersistenceUtil.SetConfig( imageSourceName, "sourceFilesPathTraining", imagesPathTraining );
        PersistenceUtil.SetConfig( imageSourceName, "sourceFilesPathTesting", imagesPathTesting );
        PersistenceUtil.SetConfig( imageSourceName, "sourceFilesLabelIndex", String.valueOf( sourceFilesLabelIndex ) );
        PersistenceUtil.SetConfig( imageSourceName, "trainingEpochs", String.valueOf( trainingEpochs ) );
        PersistenceUtil.SetConfig( imageSourceName, "testingEpochs", String.valueOf( testingEpochs ) );
        PersistenceUtil.SetConfig( imageSourceName, "shuffleTraining", String.valueOf( shuffleTrainingImages ) );
        PersistenceUtil.SetConfig( imageSourceName, "shuffleTesting", String.valueOf( shuffleTestingImages ) );
        PersistenceUtil.SetConfig( imageSourceName, "imageRepeats", String.valueOf( imageRepeats ) );

        if( testType == TEST_TYPE_IMAGE_SEQUENCE ) {
        }
        else if( testType == TEST_TYPE_TEXT_SEQUENCE ) {
            PersistenceUtil.SetConfig( imageSourceName, "sourceTextFileTraining", textFileTraining );
            PersistenceUtil.SetConfig( imageSourceName, "sourceTextFileTesting", textFileTesting );
        }

        // data series logging
        if( doLogging ) {
            PersistenceUtil.SetConfig( valueSeriesTruthName, "period", "-1" );
            PersistenceUtil.SetConfig( valueSeriesTruthName, "entityName", imageSourceName );
            PersistenceUtil.SetConfig( valueSeriesTruthName, "configPath", "imageLabel" );

            PersistenceUtil.SetConfig( valueSeriesErrorFnName, "period", "-1" );
            PersistenceUtil.SetConfig( valueSeriesErrorFnName, "entityName", pooler1Name );
            PersistenceUtil.SetConfig( valueSeriesErrorFnName, "configPath", "sumPredictionErrorFN" );
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
        int classifierEdgeMaxAge = 500;//500;
        int classifierGrowthInterval = 50;//250;//50;
        float classifierLearningRate = 0.01f;
        float classifierLearningRateNeighbours = classifierLearningRate * 0.2f;
        float classifierNoiseMagnitude = 0.0f;
        float classifierStressLearningRate = 0.01f; // not used now?
        float classifierStressSplitLearningRate = 0.5f; // change to stress after a split
        float classifierStressThreshold = 0.01f; // when it ceases to split
// has problems predicting when the classifiers take too long to learn.
//        int predictorHiddenCells = 500;
//        int predictorHiddenCells = 900;
        int predictorHiddenCells = 250;
        int predictorBatchSize = 1;//50; //64; =32, at 80k not very trained. CBF waiting so long.
        float predictorLearningRate = 0.01f;//0.1f;//0.01f;
        float predictorLeakiness = 0.01f;
        float predictorRegularization = 0f;//0.01f;

//        float outputCodingSparsityFactor = 2.0f; // how sticky the output is, ie a trace of historical errors
//                                      //       1  2    3     4     5     6     7
//        float outputDecayRate = 0.6f; // 0.8:  1, 0.8, 0.64, 0.51, 0.40, 0.32, 0.26,
//                                      // 0.6:  1, 0.6, 0.36, 0.21, 0.12, 0.07, 0.04,
//        float outputDecayRate = 0.98f;//0.95f;
        // http://www.wolframalpha.com/input/?i=y+%3D+0.95%5Ex+for+x+%3D+0+to+35  typically, 17 bits of error on average from layer 1.
        // so at 17, we have 1, 0.4, 0.16, 0.06...
        // 0.98:
        // http://www.wolframalpha.com/input/?i=y+%3D+0.98%5Ex+for+x+%3D+0+to+35
        // at    17. we have 1, 0.7, 0.49, 0.34, 0.24
        float outputDecayRate = 0.95f;
        // 0.6:  1, 0.6, 0.36, 0.21, 0.12, 0.07, 0.04,
        int outputSpikeMaxAge = 2;

        // 25 * 49 = 1225
//        PyramidRegionLayerEntityConfig prlec = new PyramidRegionLayerEntityConfig();

//        String predictorEntityType = FeedForwardNetworkQuiltPredictorEntity.ENTITY_TYPE;
        QuiltPredictorEntityConfig qpec = null;
        if( predictorEntityType.equals( HebbianQuiltPredictorEntity.ENTITY_TYPE ) ) {
            HebbianQuiltPredictorEntityConfig hqpec = new HebbianQuiltPredictorEntityConfig();
            hqpec.predictorLearningRate = predictorLearningRate;
            qpec = hqpec;
        }
        else if( predictorEntityType.equals( FeedForwardNetworkQuiltPredictorEntity.ENTITY_TYPE ) ) {
            FeedForwardNetworkQuiltPredictorEntityConfig ffnqpec = new FeedForwardNetworkQuiltPredictorEntityConfig();
            ffnqpec.predictorLearningRate = predictorLearningRate;
            ffnqpec.predictorHiddenCells = predictorHiddenCells;
            ffnqpec.predictorLeakiness = predictorLeakiness;
            ffnqpec.predictorRegularization = predictorRegularization;
            ffnqpec.predictorBatchSize = predictorBatchSize;
            ffnqpec.predictorBatchSize = predictorBatchSize;
            qpec = ffnqpec;
        }

//        FeedForwardNetworkQuiltPredictorEntityConfig
        PredictiveCodingEntityConfig pcec = new PredictiveCodingEntityConfig();
        qpec.cache = true;
        qpec.widthCells = 0;
        qpec.heightCells = 0;
        qpec.columnWidthCells = 0;
        qpec.columnHeightCells = 0;

        pcec.cache = true;
        pcec.outputSpikeAgeMax = outputSpikeMaxAge;
        pcec.outputDecayRate = outputDecayRate;
        pcec.widthCells = 0; // varies, set later
        pcec.heightCells = 0; // varies, set later

        QuiltedCompetitiveLearningEntityConfig qclec = new QuiltedCompetitiveLearningEntityConfig();
        qclec.cache = true;
        qclec.classifierLearningRate = classifierLearningRate;
        qclec.classifierLearningRateNeighbours = classifierLearningRateNeighbours;
        qclec.classifierNoiseMagnitude = classifierNoiseMagnitude;
        qclec.classifierEdgeMaxAge = classifierEdgeMaxAge;
        qclec.classifierStressLearningRate = classifierStressLearningRate;
        qclec.classifierStressSplitLearningRate = classifierStressSplitLearningRate;
        qclec.classifierStressThreshold = classifierStressThreshold;
        qclec.classifierGrowthInterval = classifierGrowthInterval;

        // Field 2: 1x1
        qclec.field2StrideX = 0;
        qclec.field2StrideY = 0;
        qclec.field2SizeX = 1;
        qclec.field2SizeY = 1;

        // Field 1: 28x28
        // TODO allow these to be offset so they can be shuffled towards centre
        //     00 01 02 03 04 05 06 07 08 09 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 |
        //  F1 -- -- -- -- -- --                                                                   |
        //  F2             -- -- -- -- -- --                                                       |
        //  F3                         -- -- -- -- -- --                                           |
        //  F4                                     -- -- -- -- -- --                               |
        //  F5                                                 -- -- -- -- -- --                   |
        //  F6                                                             -- -- -- -- -- --       |
        //  F7                                                                         -- -- -- -- -- --
        int field1StrideX = 4;
        int field1StrideY = 4;
        int field1SizeX = 6;
        int field1SizeY = 6;

        int columnWidthCells = 5;  // 25 cells per col
        int columnHeightCells = 5;
        int quiltWidthColumns = 7;
        int quiltHeightColumns = 7; // 49 cols

        if( predictorEntityType.equals( FeedForwardNetworkQuiltPredictorEntity.ENTITY_TYPE ) ) {
            FeedForwardNetworkQuiltPredictorEntityConfig ffnqpec = ( FeedForwardNetworkQuiltPredictorEntityConfig ) qpec;
            ffnqpec.predictorHiddenCells = 250;
//        prlec.predictorHiddenCells = 700;
        }

        setRegionLayerConfig(
            quiltWidthColumns, quiltHeightColumns, columnWidthCells, columnHeightCells,
            field1StrideX, field1StrideY, field1SizeX, field1SizeY,
            qclec, qpec, pcec, classifier1Name, predictor1Name, pooler1Name );

        //     00 01 02 03 04 05 06 07 08 09 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30 31 32 33 34
        //     cc cc cc cc cc CC_CC_CC_CC_CC cc-cc-cc-cc-cc CC_CC_CC_CC_CC cc-cc-cc-cc-cc CC_CC_CC_CC_CC cc-cc-cc-cc-cc
        //  F1 -- -- -- -- -- -- -- -- -- --
        //  F2                -- -- -- -- -- -- -- -- -- --
        //  F3                               -- -- -- -- -- -- -- -- -- --
        //  F4                                              -- -- -- -- -- -- -- -- -- --
        //  F5                                                             -- -- -- -- -- -- -- -- -- --
        //  F6                                                                            -- -- -- -- -- -- -- -- -- --
        field1StrideX = columnWidthCells;
        field1StrideY = columnHeightCells; // = 5
        field1SizeX = columnWidthCells * 2; // 2:1 = 10
        field1SizeY = columnHeightCells * 2; // 2:1 = 10

        quiltWidthColumns  -= 1; // 6
        quiltHeightColumns -= 1; // 6
        columnWidthCells = 6;
        columnHeightCells = 6;

        if( predictorEntityType.equals( FeedForwardNetworkQuiltPredictorEntity.ENTITY_TYPE ) ) {
            FeedForwardNetworkQuiltPredictorEntityConfig ffnqpec = ( FeedForwardNetworkQuiltPredictorEntityConfig ) qpec;
            ffnqpec.predictorHiddenCells = 250;
        }

        setRegionLayerConfig(
                quiltWidthColumns, quiltHeightColumns, columnWidthCells, columnHeightCells,
                field1StrideX, field1StrideY, field1SizeX, field1SizeY,
                qclec, qpec, pcec, classifier2Name, predictor2Name, pooler2Name );

        //     00 01 02 03 04 05 06 07 08 09 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29
        //     cc cc cc cc cc CC_CC_CC_CC_CC cc-cc-cc-cc-cc CC_CC_CC_CC_CC cc-cc-cc-cc-cc CC_CC_CC_CC_CC
        //  F1 -- -- -- -- -- -- -- -- -- --
        //  F2                -- -- -- -- -- -- -- -- -- --
        //  F3                               -- -- -- -- -- -- -- -- -- --
        //  F4                                              -- -- -- -- -- -- -- -- -- --
        //  F5                                                             -- -- -- -- -- -- -- -- -- --
/*
        field1StrideX = columnWidthCells;
        field1StrideY = columnHeightCells; // = 6
        field1SizeX = columnWidthCells * 2; // 2:1 = 12
        field1SizeY = columnHeightCells * 2; // 2:1 = 12

        // 6 * 5 = 30 ^2 = 900 cells
        quiltWidthColumns  -= 1; // 5
        quiltHeightColumns -= 1; // 5
        columnWidthCells = 6;
        columnHeightCells = 6;

        if( predictorEntityType.equals( FeedForwardNetworkQuiltPredictorEntity.ENTITY_TYPE ) ) {
            FeedForwardNetworkQuiltPredictorEntityConfig ffnqpec = ( FeedForwardNetworkQuiltPredictorEntityConfig ) qpec;
            ffnqpec.predictorHiddenCells = 250;
        }

        setRegionLayerConfig(
                quiltWidthColumns, quiltHeightColumns, columnWidthCells, columnHeightCells,
                field1StrideX, field1StrideY, field1SizeX, field1SizeY,
                qclec, qpec, pcec, classifier3Name, predictor3Name, pooler3Name );

        field1StrideX = columnWidthCells;
        field1StrideY = columnHeightCells; // = 6
        field1SizeX = columnWidthCells * 2; // 2:1 = 12
        field1SizeY = columnHeightCells * 2; // 2:1 = 12

        // 8 * 4 = 32 ^2 = 1024 cells
        quiltWidthColumns  -= 1; // 4
        quiltHeightColumns -= 1; // 4
        columnWidthCells = 8;
        columnHeightCells = 8;

        if( predictorEntityType.equals( FeedForwardNetworkQuiltPredictorEntity.ENTITY_TYPE ) ) {
            FeedForwardNetworkQuiltPredictorEntityConfig ffnqpec = ( FeedForwardNetworkQuiltPredictorEntityConfig ) qpec;
            ffnqpec.predictorHiddenCells = 250;
        }

        setRegionLayerConfig(
                quiltWidthColumns, quiltHeightColumns, columnWidthCells, columnHeightCells,
                field1StrideX, field1StrideY, field1SizeX, field1SizeY,
                qclec, qpec, pcec, classifier4Name, predictor4Name, pooler4Name );

        field1StrideX = columnWidthCells;
        field1StrideY = columnHeightCells; // = 8
        field1SizeX = columnWidthCells * 2; // 2:1 = 16
        field1SizeY = columnHeightCells * 2; // 2:1 = 16

        // 10 * 3 = 30 ^2 = 900 cells
        quiltWidthColumns  -= 1; // 3
        quiltHeightColumns -= 1; // 3
        columnWidthCells = 10;
        columnHeightCells = 10;

        if( predictorEntityType.equals( FeedForwardNetworkQuiltPredictorEntity.ENTITY_TYPE ) ) {
            FeedForwardNetworkQuiltPredictorEntityConfig ffnqpec = ( FeedForwardNetworkQuiltPredictorEntityConfig ) qpec;
            ffnqpec.predictorHiddenCells = 250;
        }

        setRegionLayerConfig(
                quiltWidthColumns, quiltHeightColumns, columnWidthCells, columnHeightCells,
                field1StrideX, field1StrideY, field1SizeX, field1SizeY,
                qclec, qpec, pcec, classifier5Name, predictor5Name, pooler5Name );

        field1StrideX = columnWidthCells;
        field1StrideY = columnHeightCells; // = 10
        field1SizeX = columnWidthCells * 2; // 2:1 = 20
        field1SizeY = columnHeightCells * 2; // 2:1 = 20

        // 14 * 2 = 28 ^2 = 784 cells
        quiltWidthColumns  -= 1; // 2
        quiltHeightColumns -= 1; // 2
        columnWidthCells = 14;
        columnHeightCells = 14;

        if( predictorEntityType.equals( FeedForwardNetworkQuiltPredictorEntity.ENTITY_TYPE ) ) {
            FeedForwardNetworkQuiltPredictorEntityConfig ffnqpec = ( FeedForwardNetworkQuiltPredictorEntityConfig ) qpec;
            ffnqpec.predictorHiddenCells = 250;
        }

        setRegionLayerConfig(
                quiltWidthColumns, quiltHeightColumns, columnWidthCells, columnHeightCells,
                field1StrideX, field1StrideY, field1SizeX, field1SizeY,
                qclec, qpec, pcec, classifier6Name, predictor6Name, pooler6Name );
        */
    }

    protected static String createRegionLayerAndDataReferences(
            Node n,
            String parentName,
            String classifierName,
            String predictorName,
            String temporalPoolerName,
            String predictorEntityType,
            Collection< String > learningEntities,
            String inputC1Entity,
            String inputC1Suffix,
            String inputC2Entity,
            String inputC2Suffix,
            ArrayList< AbstractPair< String, String > > feedbackEntitySuffixes ) {

        String previousName = parentName;

        // Create entities
        PersistenceUtil.CreateEntity( classifierName, QuiltedCompetitiveLearningEntity.ENTITY_TYPE, n.getName(), previousName );
        previousName = classifierName;
        learningEntities.add( previousName );

        PersistenceUtil.CreateEntity( predictorName, predictorEntityType, n.getName(), previousName );
        previousName = predictorName;
        learningEntities.add( previousName );

        PersistenceUtil.CreateEntity( temporalPoolerName, PredictiveCodingEntity.ENTITY_TYPE, n.getName(), previousName );
        previousName = temporalPoolerName;
        learningEntities.add( previousName );

        // Set references
        DataRefUtil.SetDataReference ( classifierName, QuiltedCompetitiveLearningEntity.INPUT_1, inputC1Entity, inputC1Suffix );
        DataRefUtil.SetDataReference ( classifierName, QuiltedCompetitiveLearningEntity.INPUT_2, inputC2Entity, inputC2Suffix );
        DataRefUtil.SetDataReference ( classifierName, QuiltedCompetitiveLearningEntity.INPUT_QUILT, predictorName, QuiltPredictorEntity.PREDICTION_NEW );
        DataRefUtil.SetDataReference (  predictorName, QuiltPredictorEntity.INPUT_C, classifierName, QuiltedCompetitiveLearningEntity.OUTPUT_QUILT );
        DataRefUtil.SetDataReferences(  predictorName, QuiltPredictorEntity.INPUT_P, feedbackEntitySuffixes );
        DataRefUtil.SetDataReference( temporalPoolerName, PredictiveCodingEntity.INPUT_PREDICTED, predictorName, QuiltPredictorEntity.PREDICTION_OLD );
        DataRefUtil.SetDataReference( temporalPoolerName, PredictiveCodingEntity.INPUT_OBSERVED, classifierName, QuiltedCompetitiveLearningEntity.OUTPUT_QUILT );

        return previousName;
    }

    protected static void setRegionLayerConfig(
            int quiltWidthColumns,
            int quiltHeightColumns,
            int columnWidthCells,
            int columnHeightCells,
            int field1StrideX,
            int field1StrideY,
            int field1SizeX,
            int field1SizeY,
            QuiltedCompetitiveLearningEntityConfig qclec,
            QuiltPredictorEntityConfig qpec,
            PredictiveCodingEntityConfig pcec,
            String classifierName,
            String predictorName,
            String poolerName ) {

        qclec.quiltWidth = quiltWidthColumns;
        qclec.quiltHeight = quiltHeightColumns;
        qclec.classifierWidth = columnWidthCells;
        qclec.classifierHeight = columnHeightCells;

        qclec.field1StrideX = field1StrideX;
        qclec.field1StrideY = field1StrideY;
        qclec.field1SizeX = field1SizeX;
        qclec.field1SizeY = field1SizeY;

        int quiltWidthCells = columnWidthCells * quiltWidthColumns;
        int quiltHeightCells = columnHeightCells * quiltHeightColumns;

        qpec.widthCells = quiltWidthCells;
        qpec.heightCells = quiltHeightCells;
        qpec.columnWidthCells = columnWidthCells;
        qpec.columnHeightCells = columnHeightCells;

        pcec.widthCells = quiltWidthCells;
        pcec.heightCells = quiltHeightCells;

        PersistenceUtil.SetConfig( classifierName, qclec );
        PersistenceUtil.SetConfig( predictorName, qpec );
        PersistenceUtil.SetConfig( poolerName, pcec );

    }
}
