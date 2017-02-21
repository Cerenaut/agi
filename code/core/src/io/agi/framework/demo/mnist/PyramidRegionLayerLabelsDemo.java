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

// LOG
//seems to work OK and recognize sequence and produce generic digits.
//made what looked like 3->4 and 8->9 prediction errors
//Try making the output without the sticky bits and use the old FN only coding scheme, but this time allowing bidirectional links?
//    Why would this work? Well we get the history to make a prediction.
//      Why doesnt it throw it off when we lose the history because we predicted correctly? well we wont predict all the time
//      so it will be self-correcting?
//        eg a rare 3 happens. It looks like an 8. All our error bits will be rare-3-related error bits
//
// Added old FN-only (but with persistence) (excluding predicted) output .

    public static final int TEST_TYPE_IMAGE_SEQUENCE = 0;
    public static final int TEST_TYPE_DIGIT_SEQUENCE = 1;
    public static final int TEST_TYPE_TEXT_SEQUENCE  = 2;

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

        // Test #1: predictable test same exemplars each time
        String trainingImagesPath = "/home/dave/workspace/agi.io/data/mnist/cycle10";
        String testingImagesPath = "/home/dave/workspace/agi.io/data/mnist/cycle10";

        // Test #2: predictable, varying digit images.
//        String trainingImageSeqPath = "/home/dave/workspace/agi.io/data/mnist/10k_train";
//        String testingImageSeqPath = "/home/dave/workspace/agi.io/data/mnist/5k_test";

//        String trainingImageSeqPath = "/home/dave/workspace/agi.io/data/mnist/all/all_train";
//        String testingImageSeqPath = "/home/dave/workspace/agi.io/data/mnist/all/all_t10k";
//        String trainingImageSeqPath = "/home/dave/workspace/agi.io/data/mnist/cycle10";
//        String testingImageSeqPath = "/home/dave/workspace/agi.io/data/mnist/cycle10";
//        int labelIndex = 2;
        String trainingDigitSeqPath = "/home/dave/workspace/agi.io/data/number-sequence/10.txt";
        String testingDigitSeqPath = "/home/dave/workspace/agi.io/data/number-sequence/10.txt";

        String trainingImageSeqPath = "/home/dave/workspace/agi.io/data/nist-sd19/a2z_1x";
        String testingImageSeqPath = "/home/dave/workspace/agi.io/data/nist-sd19/a2z_1x";
        int labelIndex = 1;

        String trainingTextSeqPath = "/home/dave/workspace/agi.io/data/text/the.txt";
        String testingTextSeqPath = "/home/dave/workspace/agi.io/data/text/the.txt";

//        int testType = TEST_TYPE_IMAGE_SEQUENCE;
//        int testType = TEST_TYPE_DIGIT_SEQUENCE;
        int testType = TEST_TYPE_TEXT_SEQUENCE;
        int terminationAge = 5000;//25000;
        int trainingEpochs = 5;
        int testingEpochs = 2;//10;//80; // good for up to 80k
        boolean shuffleImages = false;
        boolean terminateByAge = false;
//        boolean encodeZero = false;
//        boolean featureLabelsOnline = true;
//        int labelBits = 8;
        int imageRepeats = 1;//50;
        boolean cacheAllData = true;

        // http://stats.stackexchange.com/questions/176794/how-does-rectilinear-activation-function-solve-the-vanishing-gradient-problem-in
        // http://mochajl.readthedocs.io/en/latest/user-guide/neuron.html
        // https://en.wikipedia.org/wiki/Softmax_function
        // Probably I should have a 3 layer network for prediction: input, hidden and output.
        // hidden layer would be leaky-relu to prevent saturation. Then softmax layer to classify output as a probability?
        // The leaky layer would probably be OK, but the softmax layer - could it saturate?
        // Not if it doesn't have any weights. http://stats.stackexchange.com/questions/79454/softmax-layer-in-a-neural-network
        // According to this, softmax doesn't make any sense due to need to norm outputs. So better with a single hidden layer of ReLUs. https://www.reddit.com/r/MachineLearning/comments/37ardy/softmax_vs_sigmoid_for_output_of_a_neural_network/

        // So how to measure... a label that is usually there but sometimes not? But then it has the occasionaly labels as a latch to the sequence.
        // The only thing I can think of is to classify the predictions to ensure it knows what the next label is.

        // Now for hierarchy. We have these configs:
        //  a)          b)
        //     RL2          RL2
        //      |           / \
        //     RL1      RL1a   RL1b   In (b) both RL1 get a random image input for the same digit.
        //
        // TP output is spike-decay with real clock. i.e. temporal slowness. 0.9, 0.8 etc. Only errors spike?
        // Prediction depends on both same-layer output (because now we have slowness so latest spikes are visible)
        // and feedback output
        //
        // If we assume prediction is within the cells themselves, via basal dendrite computation, then we have access to
        // both predicted and FP spikes. We concatenate these as the input.
        //
        // Now for higher order context, we'd like to ensure we don't miss anything.
        // But the next layer cells only spike on prediction errors.
        // What do they use to predict? Lower output (which is also available) and higher feedback
        // So they do have additional info.
        // *** how can we get that additional context without P-encoding? ***
        // Well from the previous 1-bit to the next it is deterministic. So theoretically are we missing nothing by taking the
        // PC output from higher level?
        //
        // State   AAABBBBBBBBBBBAAAAAAA
        // HIGHER  000100000000001000000
        // LOWER   000100010001001010000
        // State   AAABBBBCCCCDDDEEAAAAA
        //
        // i,j,k,l,E,m      E = error, not really in this case but whatever.
        // 0 0 0 0 1 0
        //
        // LOWER
        //
        //  0 0 0 0 1 0
        //
        // SEQUENCE
        //  a-b-a-b-z-b-
        //
        // But for stable encoding, it would be better not to fiddle with the FF encoding and instead have a separate
        // layer to integrate feedback. Confirmed with consilience, L2/3 axons branch into L5 (a lot, actually). But the
        // stuff that's predicted accurately in L2/3 isn't available
        //
        //








        // Define some entities
        String experimentName           = Framework.GetEntityName( "experiment" );
        String constantName             = Framework.GetEntityName( "constant" );
        String imageSourceName          = Framework.GetEntityName( "image-source" );
//        String imageEncoderName         = Framework.GetEntityName( "image-encoder" );
//        String labelEncoderName         = Framework.GetEntityName( "label-encoder" );
//        String labelDecoderName         = Framework.GetEntityName( "label-decoder" );
//        String classResultName          = Framework.GetEntityName( "class-result" );
//        String configProductName        = Framework.GetEntityName( "config-product" ); // allows you to turn off provision of the training labels
//        String supervisedLearningName   = Framework.GetEntityName( "supervised" );
//        String valueSeriesPredictedName = Framework.GetEntityName( "value-series-predicted" );
//        String valueSeriesErrorName     = Framework.GetEntityName( "value-series-error" );
        String valueSeriesTruthName     = Framework.GetEntityName( "value-series-truth" );

        String regionLayer1Name         = Framework.GetEntityName( "region-layer-1" );
        String regionLayer2Name         = Framework.GetEntityName( "region-layer-2" );
//        String regionLayer3Name         = Framework.GetEntityName( "region-layer-3" );

        // These are the properties of a pyramid region-layer that can be logged:
        // float sumClassifierError = 0;    **** this tells us whether it's learning effectively, or it is unable to find structure within the input data.
        // float sumClassifierResponse = 0; ---- ignore this one for now
        // float sumOutputSpikes = 0;       **** this would tell us whether it's predictable or random
        // float sumPredictionErrorFP = 0;  ---- ignore this one for now because we assume the learning keeps it honest (not too many FP)
        // float sumPredictionErrorFN = 0;  ---- ignore this one for now because it's a fn of output spikes.
        // float sumIntegration = 0;        ---- this would tell us how the integration dynamics works but since it's linear + and exp - maybe we can imagine.
        //                                   Actually, the above is less useful because it's a sum over all the cells which will be out of sync.0
//        String valueSeriesClassifierError1 = Framework.GetEntityName( "value-series-classifier-error-1" );
//        String valueSeriesClassifierError2 = Framework.GetEntityName( "value-series-classifier-error-2" );
//        String valueSeriesClassifierError3 = Framework.GetEntityName( "value-series-classifier-error-3" );
//
//        String valueSeriesOutputSpikes1 = Framework.GetEntityName( "value-series-output-spikes-1" );
//        String valueSeriesOutputSpikes2 = Framework.GetEntityName( "value-series-output-spikes-2" );
//        String valueSeriesOutputSpikes3 = Framework.GetEntityName( "value-series-output-spikes-3" );

        // build a history of spikes.
//        String spikes1Name        = Framework.GetEntityName( "spikes-1" );
//        String spikes2Name        = Framework.GetEntityName( "spikes-2" );
//        String spikes3Name        = Framework.GetEntityName( "spikes-3" );

        Framework.CreateEntity( experimentName, ExperimentEntity.ENTITY_TYPE, n.getName(), null ); // experiment is the root entity
        Framework.CreateEntity( constantName, ConstantMatrixEntity.ENTITY_TYPE, n.getName(), experimentName ); // ok all input to the regions is ready

        if( testType == TEST_TYPE_IMAGE_SEQUENCE ) {
            Framework.CreateEntity( imageSourceName, ImageLabelEntity.ENTITY_TYPE, n.getName(), constantName );
        }
        else if( testType == TEST_TYPE_DIGIT_SEQUENCE ) {
            Framework.CreateEntity( imageSourceName, NumberSequence2ImageLabelEntity.ENTITY_TYPE, n.getName(), constantName );
        }
        else if( testType == TEST_TYPE_TEXT_SEQUENCE ) {
            Framework.CreateEntity( imageSourceName, Text2ImageLabelEntity.ENTITY_TYPE, n.getName(), constantName );
        }

//        Framework.CreateEntity( imageEncoderName, EncoderEntity.ENTITY_TYPE, n.getName(), imageSourceName );
//        Framework.CreateEntity( labelEncoderName, EncoderEntity.ENTITY_TYPE, n.getName(), constantName );
//        Framework.CreateEntity( configProductName, ConfigProductEntity.ENTITY_TYPE, n.getName(), labelEncoderName ); // ok all input to the regions is ready

        // Region-layers
        Framework.CreateEntity( regionLayer1Name, PyramidRegionLayerEntity.ENTITY_TYPE, n.getName(), imageSourceName );
        String lastRegionLayerName = regionLayer1Name;
        String learningEntitiesAlgorithm = regionLayer1Name;

        Framework.CreateEntity( regionLayer2Name, PyramidRegionLayerEntity.ENTITY_TYPE, n.getName(), regionLayer1Name );
        lastRegionLayerName = regionLayer2Name;
        learningEntitiesAlgorithm = learningEntitiesAlgorithm + "," + regionLayer2Name;
        // Region-layers

//        Framework.CreateEntity( supervisedLearningName, SupervisedLearningEntity.ENTITY_TYPE, n.getName(), lastRegionLayerName );

//        Framework.CreateEntity( valueSeriesPredictedName, ValueSeriesEntity.ENTITY_TYPE, n.getName(), lastRegionLayerName );
//        Framework.CreateEntity( valueSeriesErrorName, ValueSeriesEntity.ENTITY_TYPE, n.getName(), lastRegionLayerName );
        Framework.CreateEntity( valueSeriesTruthName, ValueSeriesEntity.ENTITY_TYPE, n.getName(), lastRegionLayerName );

//        Framework.CreateEntity( valueSeriesClassifierError1, ValueSeriesEntity.ENTITY_TYPE, n.getName(), lastRegionLayerName );
//        Framework.CreateEntity( valueSeriesClassifierError2, ValueSeriesEntity.ENTITY_TYPE, n.getName(), lastRegionLayerName );
//        Framework.CreateEntity( valueSeriesClassifierError3, ValueSeriesEntity.ENTITY_TYPE, n.getName(), lastRegionLayerName );
//
//        Framework.CreateEntity( valueSeriesOutputSpikes1, ValueSeriesEntity.ENTITY_TYPE, n.getName(), lastRegionLayerName );
//        Framework.CreateEntity( valueSeriesOutputSpikes2, ValueSeriesEntity.ENTITY_TYPE, n.getName(), lastRegionLayerName );
//        Framework.CreateEntity( valueSeriesOutputSpikes3, ValueSeriesEntity.ENTITY_TYPE, n.getName(), lastRegionLayerName );
//
//        Framework.CreateEntity( spikes1Name, VectorSeriesEntity.ENTITY_TYPE, n.getName(), lastRegionLayerName ); // ok all input to the regions is ready
//        Framework.CreateEntity( spikes2Name, VectorSeriesEntity.ENTITY_TYPE, n.getName(), lastRegionLayerName ); // ok all input to the regions is ready
//        Framework.CreateEntity( spikes3Name, VectorSeriesEntity.ENTITY_TYPE, n.getName(), lastRegionLayerName ); // ok all input to the regions is ready
//
//        Framework.SetDataReference( spikes1Name, VectorSeriesEntity.INPUT, regionLayer1Name, PyramidRegionLayerEntity.OUTPUT_SPIKES_NEW );
//        Framework.SetDataReference( spikes2Name, VectorSeriesEntity.INPUT, regionLayer2Name, PyramidRegionLayerEntity.OUTPUT_SPIKES_NEW );
//        Framework.SetDataReference( spikes3Name, VectorSeriesEntity.INPUT, regionLayer3Name, PyramidRegionLayerEntity.OUTPUT_SPIKES_NEW );

//        Framework.CreateEntity( labelDecoderName, DecoderEntity.ENTITY_TYPE, n.getName(), topLayerName ); // produce the predicted classification for inspection by mnist next time
//        Framework.CreateEntity( classResultName, ClassificationResultEntity.ENTITY_TYPE, n.getName(), labelDecoderName ); // produce the predicted classification for inspection by mnist next time
//
        // Connect the entities' data
        // a) Image to image region, and decode
//        if( testType == TEST_TYPE_IMAGE_SEQUENCE ) {
//            Framework.SetDataReference( regionLayer1Name, PyramidRegionLayerEntity.INPUT_C1, imageSourceName, ImageLabelEntity.OUTPUT_IMAGE );
//        }
//        else if( testType == TEST_TYPE_DIGIT_SEQUENCE ) {
//            Framework.SetDataReference( regionLayer1Name, PyramidRegionLayerEntity.INPUT_C1, imageSourceName, NumberSequence2ImageLabelEntity.OUTPUT_IMAGE );
//        }
//        else if( testType == TEST_TYPE_TEXT_SEQUENCE ) {
//            Framework.SetDataReference( regionLayer1Name, PyramidRegionLayerEntity.INPUT_C1, imageSourceName, Text2ImageLabelEntity.OUTPUT_IMAGE );
//        }

//        Framework.SetDataReference( labelEncoderName, EncoderEntity.DATA_INPUT, imageSourceName, ImageLabelEntity.OUTPUT_LABEL );
//
////                Framework.SetDataReference( region2FfName, AutoRegionLayerEntity.INPUT_2, labelEncoderName, EncoderEntity.DATA_OUTPUT_ENCODED );
//        Framework.SetDataReference( configProductName, ConfigProductEntity.INPUT, labelEncoderName, EncoderEntity.DATA_OUTPUT_ENCODED );

        // cache all data for speed, when enabled
        Framework.SetConfig( experimentName, "cache", String.valueOf( cacheAllData ) );
        Framework.SetConfig( imageSourceName, "cache", String.valueOf( cacheAllData ) );
        Framework.SetConfig( regionLayer1Name, "cache", String.valueOf( cacheAllData ) );
        Framework.SetConfig( regionLayer2Name, "cache", String.valueOf( cacheAllData ) );

        // RL1
        // it is more stable with the current spikes only as feedback.. ?
        String ffInputData = PyramidRegionLayerEntity.OUTPUT;
        String fbInputData = PyramidRegionLayerEntity.OUTPUT;
//        String fbInputData = PyramidRegionLayerEntity.CLASSIFIER_SPIKES_NEW; // instantaneous output
        Framework.SetDataReference( regionLayer1Name, PyramidRegionLayerEntity.INPUT_C1, imageSourceName, ImageLabelEntity.OUTPUT_IMAGE );
        Framework.SetDataReference( regionLayer1Name, PyramidRegionLayerEntity.INPUT_C2, constantName, ConstantMatrixEntity.OUTPUT );
        Framework.SetDataReference( regionLayer1Name, PyramidRegionLayerEntity.INPUT_P1, regionLayer2Name, fbInputData );
        Framework.SetDataReference( regionLayer1Name, PyramidRegionLayerEntity.INPUT_P2, constantName, ConstantMatrixEntity.OUTPUT );

        // RL2
        Framework.SetDataReference( regionLayer2Name, PyramidRegionLayerEntity.INPUT_C1, regionLayer1Name, ffInputData );
        Framework.SetDataReference( regionLayer2Name, PyramidRegionLayerEntity.INPUT_C2, constantName, ConstantMatrixEntity.OUTPUT );
        Framework.SetDataReference( regionLayer2Name, PyramidRegionLayerEntity.INPUT_P1, regionLayer1Name, fbInputData );
        Framework.SetDataReference( regionLayer2Name, PyramidRegionLayerEntity.INPUT_P2, constantName, ConstantMatrixEntity.OUTPUT );
//
//        // RL3
//        Framework.SetDataReference( regionLayer3Name, PyramidRegionLayerEntity.INPUT_C1, regionLayer1Name, ffInputData );
////        Framework.SetDataReference( regionLayer3Name, PyramidRegionLayerEntity.INPUT_C2, regionLayer2Name, PyramidRegionLayerEntity.OUTPUT_SPIKES_NEW );
//        Framework.SetDataReference( regionLayer3Name, PyramidRegionLayerEntity.INPUT_C2, constantName, ConstantMatrixEntity.OUTPUT );
//
//        Framework.SetDataReference( regionLayer3Name, PyramidRegionLayerEntity.INPUT_P1, constantName, ConstantMatrixEntity.OUTPUT );
//        Framework.SetDataReference( regionLayer3Name, PyramidRegionLayerEntity.INPUT_P2, constantName, ConstantMatrixEntity.OUTPUT );

        // train the featureLabels entity to associate bits in the region-layers with the
//        ArrayList< AbstractPair< String, String > > featureDatas = new ArrayList< AbstractPair< String, String > >();
//        featureDatas.add( new AbstractPair< String, String >( regionLayer1Name, PyramidRegionLayerEntity.CLASSIFIER_SPIKES_NEW ) );
//        //featureDatas.add( new AbstractPair< String, String >( regionLayer2Name, PyramidRegionLayerEntity.CLASSIFIER_SPIKES_NEW ) );
//        //featureDatas.add( new AbstractPair< String, String >( regionLayer3Name, PyramidRegionLayerEntity.CLASSIFIER_SPIKES_NEW ) );
//        Framework.SetDataReferences( supervisedLearningName, SupervisedLearningEntity.INPUT_FEATURES, featureDatas ); // get current state from the region to be used to predict

        // invert the hidden layer state to produce the predicted label
//        Framework.SetDataReference( labelDecoderName, DecoderEntity.DATA_INPUT_ENCODED, topLayerName, AutoRegionLayerEntity.OUTPUT_INPUT_2 ); // the prediction of the next state
//
//        Framework.SetDataReference( classResultName, ClassificationResultEntity.INPUT_LABEL, imageClassName, ImageClassEntity.OUTPUT_LABEL ); // get current state from the region to be used to predict
//        Framework.SetDataReference( classResultName, ClassificationResultEntity.INPUT_CLASS, labelDecoderName, DecoderEntity.DATA_OUTPUT_DECODED ); // get current state from the region to be used to predict

        // Label filter - disable labels during test time
//        Framework.SetConfig( configProductName, "entityName", regionLayer2Name );
//        Framework.SetConfig( configProductName, "configPath", "learn" ); // so the value is '1' when the region is learning, and 0 otherwise.

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
        Framework.SetConfig( imageSourceName, "sourceFilesPathTraining", trainingImageSeqPath );
        Framework.SetConfig( imageSourceName, "sourceFilesPathTesting", testingImageSeqPath );
        Framework.SetConfig( imageSourceName, "sourceFilesLabelIndex", String.valueOf( labelIndex ) );
        Framework.SetConfig( imageSourceName, "trainingEpochs", String.valueOf( trainingEpochs ) );
        Framework.SetConfig( imageSourceName, "testingEpochs", String.valueOf( testingEpochs ) );
        Framework.SetConfig( imageSourceName, "shuffle", String.valueOf( shuffleImages ) );
        Framework.SetConfig( imageSourceName, "imageRepeats", String.valueOf( imageRepeats ) );

        if( testType == TEST_TYPE_IMAGE_SEQUENCE ) {
            Framework.SetConfig( imageSourceName, "sourceFilesPathTraining", trainingImagesPath );
            Framework.SetConfig( imageSourceName, "sourceFilesPathTesting", testingImagesPath );
        }
        else if( testType == TEST_TYPE_DIGIT_SEQUENCE ) {
            Framework.SetConfig( imageSourceName, "sourceTextFileTraining", trainingDigitSeqPath );
            Framework.SetConfig( imageSourceName, "sourceTextFileTesting", testingDigitSeqPath );
        }
        else if( testType == TEST_TYPE_TEXT_SEQUENCE ) {
            Framework.SetConfig( imageSourceName, "sourceTextFileTraining", trainingTextSeqPath );
            Framework.SetConfig( imageSourceName, "sourceTextFileTesting", testingTextSeqPath );
        }

/*        String learningEntitiesAnalytics = featureLabelsName;
        Framework.SetConfig( imageSourceName, "learningEntitiesAlgorithm", String.valueOf( learningEntitiesAlgorithm ) );
        Framework.SetConfig( imageSourceName, "learningEntitiesAnalytics", String.valueOf( learningEntitiesAnalytics ) );
*/
        // constant config
//        if( encodeZero ) {
//            // image encoder config
//            Framework.SetConfig( imageEncoderName, "density", "1" );
//            Framework.SetConfig( imageEncoderName, "bits", "2" );
//            Framework.SetConfig( imageEncoderName, "encodeZero", "true" );
//        }
//        else {
//            // image encoder config
//            Framework.SetConfig( imageEncoderName, "density", "1" );
//            Framework.SetConfig( imageEncoderName, "bits", "1" );
//            Framework.SetConfig( imageEncoderName, "encodeZero", "false" );
//        }

//do mrf/loopy BP calculation for predicted state vs actual

//        // Label encoder
//        Framework.SetConfig( labelEncoderName, "encoderType", IntegerEncoder.class.getSimpleName() );
//        Framework.SetConfig( labelEncoderName, "minValue", "0" );
//        Framework.SetConfig( labelEncoderName, "maxValue", "9" );
//        Framework.SetConfig( labelEncoderName, "rows", String.valueOf( labelBits ) );

        // Label DEcoder
//        Framework.SetConfig( labelDecoderName, "encoderType", IntegerEncoder.class.getSimpleName() );
//        Framework.SetConfig( labelDecoderName, "minValue", "0" );
//        Framework.SetConfig( labelDecoderName, "maxValue", "9" );
//        Framework.SetConfig( labelDecoderName, "rows", String.valueOf( labelBits ) );

//        // feature-labels config
//        Framework.SetConfig( featureLabelsName, "classEntityName", imageSourceName );
//        Framework.SetConfig( featureLabelsName, "classConfigPath", "imageClass" );
//        Framework.SetConfig( featureLabelsName, "classes", "10" );
//        Framework.SetConfig( featureLabelsName, "onlineLearning", String.valueOf( featureLabelsOnline ) );
////        Framework.SetConfig( featureLabelsName, "onlineLearningRate", "0.02" );
//        Framework.SetConfig( featureLabelsName, "onlineLearningRate", "0.005" );

        // data series logging
//        Framework.SetConfig( valueSeriesPredictedName, "period", "-1" ); // log forever
//        Framework.SetConfig( valueSeriesErrorName, "period", "-1" );
        Framework.SetConfig( valueSeriesTruthName, "period", "-1" );

//        Framework.SetConfig( valueSeriesClassifierError1, "period", "-1" );
//        Framework.SetConfig( valueSeriesClassifierError2, "period", "-1" );
//        Framework.SetConfig( valueSeriesClassifierError3, "period", "-1" );
//
//        Framework.SetConfig( valueSeriesOutputSpikes1, "period", "-1" );
//        Framework.SetConfig( valueSeriesOutputSpikes2, "period", "-1" );
//        Framework.SetConfig( valueSeriesOutputSpikes3, "period", "-1" );
//
//        Framework.SetConfig( valueSeriesPredictedName, "entityName", featureLabelsName );
//        Framework.SetConfig( valueSeriesErrorName, "entityName", featureLabelsName );
        Framework.SetConfig( valueSeriesTruthName, "entityName", imageSourceName );

//        Framework.SetConfig( valueSeriesClassifierError1, "entityName", regionLayer1Name );
//        Framework.SetConfig( valueSeriesClassifierError2, "entityName", regionLayer2Name );
//        Framework.SetConfig( valueSeriesClassifierError3, "entityName", regionLayer3Name );
//
//        Framework.SetConfig( valueSeriesOutputSpikes1, "entityName", regionLayer1Name );
//        Framework.SetConfig( valueSeriesOutputSpikes2, "entityName", regionLayer2Name );
//        Framework.SetConfig( valueSeriesOutputSpikes3, "entityName", regionLayer3Name );
//
//        Framework.SetConfig( valueSeriesPredictedName, "configPath", "classPredicted" );
//        Framework.SetConfig( valueSeriesErrorName, "configPath", "classError" );
        Framework.SetConfig( valueSeriesTruthName, "configPath", "imageLabel" );

//        Framework.SetConfig( valueSeriesClassifierError1, "configPath", "sumClassifierError" );
//        Framework.SetConfig( valueSeriesClassifierError2, "configPath", "sumClassifierError" );
//        Framework.SetConfig( valueSeriesClassifierError3, "configPath", "sumClassifierError" );
//
//        Framework.SetConfig( valueSeriesOutputSpikes1, "configPath", "sumOutputSpikes" );
//        Framework.SetConfig( valueSeriesOutputSpikes2, "configPath", "sumOutputSpikes" );
//        Framework.SetConfig( valueSeriesOutputSpikes3, "configPath", "sumOutputSpikes" );
//
        // Spike logging matrices
//        int spikeHistory = imageRepeats * 4;
//        Framework.SetConfig( spikes1Name, "period", String.valueOf( spikeHistory ) );
//        Framework.SetConfig( spikes2Name, "period", String.valueOf( spikeHistory ) );
//        Framework.SetConfig( spikes3Name, "period", String.valueOf( spikeHistory ) );

//test without sequence info - random images, same alg. No feedback. This shows how much the sequence is helping or not.

        // region layer config
        // effective constants:
        float classifierLearningRate = 0.01f;//0.005f;// 0.01
        float classifierMomentum = 0f;
        float classifierWeightsStdDev = 0.01f;
        int classifierAgeMin = 0; // age of disuse where we start to promote cells
        int classifierAgeMax = 1000;//2000/1000; // age of disuse where we maximally promote cells
        float classifierAgeTruncationFactor = 0.5f;
        float classifierAgeScale = 12f; // promotion nonlinearity
        float classifierRateScale = 5f; // inhibition nonlinearity
        float classifierRateMax = 0.15f;//0.25f; // i.e. never more than 1 in 4.
        float classifierRateLearningRate = 0.01f; // how fast the measurement of rate of cell use changes.

        float predictorLearningRate = 0.002f; // 1/5 the classifier learning rate
        int predictorHiddenCells = 500;
        float predictorLeakiness = 0.01f;
        float predictorRegularization = 0.f;

//        float outputCodingSparsityFactor = 2.0f; // how sticky the output is, ie a trace of historical errors
        float outputDecayRate = 0.6f; // 1, 0.8, 0.64, 0.51, 0.4, 0.32, 0.26,

        // variables
        int widthCells = 32;//20;
        int heightCells = 32;//20;
        int batchSize = 1;
        int classifierSparsity = 20;//15; // k, the number of active cells each step
        float classifierSparsityOutput = 1.5f; // a factor determining the output sparsity

        String regionLayerName = regionLayer1Name;

        setRegionLayerConfig(
                regionLayerName, widthCells, heightCells,
                classifierLearningRate, classifierMomentum, classifierWeightsStdDev,
                classifierSparsityOutput, classifierSparsity,
                classifierAgeMin, classifierAgeMax, classifierAgeTruncationFactor, classifierAgeScale,
                classifierRateScale, classifierRateMax, classifierRateLearningRate,
                batchSize,
                predictorLearningRate, predictorHiddenCells, predictorLeakiness, predictorRegularization,
                batchSize,
                outputDecayRate );

        regionLayerName = regionLayer2Name;

        setRegionLayerConfig(
                regionLayerName, widthCells, heightCells,
                classifierLearningRate, classifierMomentum, classifierWeightsStdDev,
                classifierSparsityOutput, classifierSparsity,
                classifierAgeMin, classifierAgeMax, classifierAgeTruncationFactor, classifierAgeScale,
                classifierRateScale, classifierRateMax, classifierRateLearningRate,
                batchSize,
                predictorLearningRate, predictorHiddenCells, predictorLeakiness, predictorRegularization,
                batchSize,
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
