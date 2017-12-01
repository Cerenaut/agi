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

import io.agi.core.ann.unsupervised.LifetimeSparseAutoencoder;
import io.agi.core.orm.AbstractPair;
import io.agi.core.util.images.BufferedImageSource.BufferedImageSourceFactory;
import io.agi.framework.Node;
import io.agi.framework.demo.CreateEntityMain;
import io.agi.framework.demo.mnist.ImageLabelEntity;
import io.agi.framework.entities.*;
import io.agi.framework.persistence.DataJsonSerializer;
import io.agi.framework.persistence.PersistenceUtil;
import io.agi.framework.references.DataRefUtil;

import java.io.File;
import java.util.ArrayList;

/**
 * TODO: Measure rate of error bits in the classifier, so get a 1-d metric of performance
 * TODO: Change to ReLu units? EDIT: They're already linear, but with no threshold. But this raises the point that they
 * could self-threshold if they were Relu. For feed-forward output, we can produce the number of bits that are over the
 * relu threshold. For training, we select just a few?
 *
 * Created by dave on 16/10/17.
 */
public class VariableOrderTextExpt extends CreateEntityMain {

    public static final int TEST_TYPE_IMAGE_SEQUENCE = 0;
    public static final int TEST_TYPE_TEXT_SEQUENCE  = 1;

    public static void main( String[] args ) {
        VariableOrderTextExpt expt = new VariableOrderTextExpt();
        expt.mainImpl( args );
    }

    public static String getOutputPath() {
        return "/home/dave/Desktop/agi/data/variable_order_text";
    }

    public void createEntities( Node n ) {

        int testType = TEST_TYPE_TEXT_SEQUENCE; // a sequence of characters e.g. A..Z or 0..9. Random exemplar for each
//        int testType = TEST_TYPE_IMAGE_SEQUENCE; // a sequence of characters e.g. A..Z or 0..9. Random exemplar for each
        int sourceFilesLabelIndex = 2; // depends on the naming format of the images used
        boolean asciiEncoding = false;
        boolean logDuringTraining = false;

        boolean shuffleTrainingImages = false;
        boolean shuffleTestingImages = false;

        String imagesPathTraining = null;
        String imagesPathTesting = null;

        String textFileTraining = null;
        String textFileTesting = null;

        String fileNameWriteFeatures = getOutputPath() + File.separator + "features.csv";
        String fileNameWriteLabels = getOutputPath() + File.separator + "labels.csv";

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
//            sourceFilesLabelIndex = 1; // SD19
            sourceFilesLabelIndex = 2; // MNIST
//            textFileTraining = "/home/dave/workspace/agi.io/data/text/quick.txt";
//            textFileTesting = "/home/dave/workspace/agi.io/data/text/quick.txt";
//            textFileTraining = "/home/dave/workspace/agi.io/data/text/variable_order_abc.txt";
//            textFileTesting = "/home/dave/workspace/agi.io/data/text/variable_order_abc.txt";
//            textFileTraining = "/home/dave/workspace/agi.io/data/text/variable_order.txt";
//            textFileTesting = "/home/dave/workspace/agi.io/data/text/variable_order.txt";
            textFileTraining = "/home/dave/workspace/agi.io/data/text/more_confusing.txt";
            textFileTesting = "/home/dave/workspace/agi.io/data/text/more_confusing.txt";
//            textFileTraining = "/home/dave/workspace/agi.io/data/text/less_confusing.txt";
//            textFileTesting = "/home/dave/workspace/agi.io/data/text/less_confusing.txt";

//            imagesPathTraining = "/home/dave/workspace/agi.io/data/nist-sd19/1_to_10_1x";
//            imagesPathTesting = "/home/dave/workspace/agi.io/data/nist-sd19/1_to_10_1x";

// need to map to ascii numbers
//            imagesPathTraining = "/home/dave/workspace/agi.io/data/mnist/10k_train";
//            imagesPathTesting = "/home/dave/workspace/agi.io/data/mnist/10k_train";
//            imagesPathTesting = "/home/dave/workspace/agi.io/data/mnist/5k_test";
            // Full set of digit images
//            imagesPathTraining = "/home/dave/workspace/agi.io/data/mnist/all/all_train";
//            imagesPathTesting = "/home/dave/workspace/agi.io/data/mnist/all/all_t10k";
            // One image per digit
            imagesPathTraining = "/home/dave/workspace/agi.io/data/mnist/cycle10";
            imagesPathTesting = "/home/dave/workspace/agi.io/data/mnist/cycle10";

//            imagesPathTraining = "/home/dave/workspace/agi.io/data/nist-sd19/a2z_1x";
//            imagesPathTesting = "/home/dave/workspace/agi.io/data/nist-sd19/a2z_1x";
//            imagesPathTraining = "/home/dave/workspace/agi.io/data/nist-sd19/a2z_10x";
//            imagesPathTesting = "/home/dave/workspace/agi.io/data/nist-sd19/a2z_10x";
//            imagesPathTraining = "/home/dave/workspace/agi.io/data/nist-sd19/a2z_100x";
//            imagesPathTesting = "/home/dave/workspace/agi.io/data/nist-sd19/a2z_100x";
        }

        // 10k images, 5k useful. 1 epoch = 2 = 2500 epochs
// ERROR: Was not getting input, and learning during testing so basically random number
// With a single example of each input image:
//        Errors: 32 of 2000 = 98.4% correct.
//                Confusion:
//        <--- PREDICTED --->
//        0      1      2      3      4
//        0    395      0      5      0      0
//        1      0    394      0      2      4
//        2      1      0    393      0      6
//        3      7      1      0    392      0
//        4      0      3      3      0    394
//
//        F-Score:
//        Label     Err     TP     FP     TN     FN      T      F  F-Score
//        0          13    395      8   1592      5    400   1600   0.9801
//        1          10    394      4   1596      6    400   1600   0.9899
//        2          15    393      8   1592      7    400   1600   0.9800
//        3          10    392      2   1598      8    400   1600   0.9949
//        4          16    394     10   1590      6    400   1600   0.9752
//
//        Overall F-Score: 0.9840 (micro) 0.9841 (macro)

// variable_order.txt 0123404321 Need t,t-1 to predict t+1
//        int trainingEpochs = 2500;//1500;//5000;//00;
//        int testingEpochs = 500+100;// needs more (40%)
//        int trainingEpochs = 5000;//1500;//5000;//00;
//        int testingEpochs = 5000+500;// needs more    44%
//        int trainingEpochs = 200;//1500;//5000;//00; 98.4%
//        int testingEpochs = 200+200;// needs more
//        int trainingEpochs = 2000;
//        int testingEpochs = 2000+2000; // 99.875%/99.27%

//        Errors: 25 of 20000 = 99.875% correct.
//                Confusion:
//        <--- PREDICTED --->
//        0      1      2      3      4
//        0   4000      0      0      0      0
//        1      0   3986      0      4     10
//        2      0      0   3998      0      2
//        3      0      3      0   3997      0
//        4      0      6      0      0   3994
//
//        F-Score:
//        Label     Err     TP     FP     TN     FN      T      F  F-Score
//        0           0   4000      0  16000      0   4000  16000   1.0000
//        1          23   3986      9  15991     14   4000  16000   0.9977
//        2           2   3998      0  16000      2   4000  16000   1.0000
//        3           7   3997      4  15996      3   4000  16000   0.9990
//        4          18   3994     12  15988      6   4000  16000   0.9970
//
//        Overall F-Score: 0.9987 (micro) 0.9988 (macro)
//
//                Errors: 146 of 20000 = 99.27% correct.
//                Confusion:
//        <--- PREDICTED --->
//        0      1      2      3      4
//        0   3983      0      8      9      0
//        1      0   3947      0     21     32
//        2      4      0   3984      0     12
//        3     11     15      0   3974      0
//        4      0     24     10      0   3966
//
//        F-Score:
//        Label     Err     TP     FP     TN     FN      T      F  F-Score
//        0          32   3983     15  15985     17   4000  16000   0.9962
//        1          92   3947     39  15961     53   4000  16000   0.9902
//        2          34   3984     18  15982     16   4000  16000   0.9955
//        3          56   3974     30  15970     26   4000  16000   0.9925
//        4          78   3966     44  15956     34   4000  16000   0.9890
//
//        Overall F-Score: 0.9927 (micro) 0.9927 (macro)

// more_confusing.txt 0123406234
// breaks down as:
// 0 1234
// 6 1234     The 6/0 ambiguity depends on a t-5 history
// Expected value is to be correct 9/10 or 90%. 80% predictable, and 50% chance on 20% = 90%.
// But it's only correct 2/3 of the time. where is the randomization coming from?
// Why isn't it fully orthogonal?
//
//        Errors: 1372 of 20000 = 93.14% correct.
//                Confusion:
//        <--- PREDICTED --->
//               0      1      2      3      4      6
//        0   1320      0      0      0      0    680
//        1      0   4000      0      0      0      0
//        2      0      0   4000      0      0      0
//        3      0      0      0   4000      0      0
//        4      0      0      0      0   4000      0
//        6    692      0      0      0      0   1308
//
//        F-Score:
//        Label     Err     TP     FP     TN     FN      T      F  F-Score
//        0        1372   1320    692  17308    680   2000  18000   0.6561
//        1           0   4000      0  16000      0   4000  16000   1.0000
//        2           0   4000      0  16000      0   4000  16000   1.0000
//        3           0   4000      0  16000      0   4000  16000   1.0000
//        4           0   4000      0  16000      0   4000  16000   1.0000
//        6        1372   1308    680  17320    692   2000  18000   0.6579
//
//        Overall F-Score: 0.9314 (micro) 0.8857 (macro)
//
//        int trainingEpochs = 2000;
//        int testingEpochs = 2000+2000;

// 100% in 3 of 6 train=300e test=200/200e with batchsize=1 and batchsparse=0.
// Maybe having period=10 and batchsize=16 meant that there was too much disruption with batch-sparse to involve more cells.

// new training: 100% 3 of 3

        int trainingEpochs = 300;//500; // 100% in 1 of 2 goes @ 500.  100% in 0 of 3 goes @ 1000. (9,8,8 unique rows)
        int testingEpochs = 200+200;

        int terminationAge = 5000;//25000;
        boolean terminateByAge = false;
        int imageRepeats = 1;//50;
        boolean cacheAllData = true;
//        boolean doLogging = true;
        int queueLength = 46;
        int layers = 1;

        // Define some entities
        String experimentName           = PersistenceUtil.GetEntityName( "experiment" );
        String constantName             = PersistenceUtil.GetEntityName( "constant" );
        String imageSourceName          = PersistenceUtil.GetEntityName( "image-source" );
//        String valueSeriesTruthName     = PersistenceUtil.GetEntityName( "value-series-truth" );
//        String valueSeriesPredictionErrorName   = PersistenceUtil.GetEntityName( "prediction-error" );
        String valueSeriesUniqueName    = PersistenceUtil.GetEntityName( "value-series-unique" );
        String delayEntityName          = PersistenceUtil.GetEntityName( "delay" );;
        String featureSeriesName        = PersistenceUtil.GetEntityName( "feature-series" );
        String labelSeriesName          = PersistenceUtil.GetEntityName( "label-series" );

//        String dataQueueOutputName = PersistenceUtil.GetEntityName( "data-queue-output-" );
        String classifierName = PersistenceUtil.GetEntityName( "classifier-" );
//        String predictorName = PersistenceUtil.GetEntityName( "predictor-" );
//        String poolingName = PersistenceUtil.GetEntityName( "pooler0-" );

        String parentName = null;
        parentName = PersistenceUtil.CreateEntity( experimentName, ExperimentEntity.ENTITY_TYPE, n.getName(), parentName ); // experiment is the root entity
        parentName = PersistenceUtil.CreateEntity( constantName, ConstantMatrixEntity.ENTITY_TYPE, n.getName(), parentName ); // ok all input to the regions is ready

        if( testType == TEST_TYPE_IMAGE_SEQUENCE ) {
            parentName = PersistenceUtil.CreateEntity( imageSourceName, ImageLabelEntity.ENTITY_TYPE, n.getName(), parentName );
        }
        else if( testType == TEST_TYPE_TEXT_SEQUENCE ) {
            parentName = PersistenceUtil.CreateEntity( imageSourceName, Text2ImageLabelEntity.ENTITY_TYPE, n.getName(), parentName );
        }

        // Experiment
        PersistenceUtil.SetConfig( experimentName, "cache", String.valueOf( cacheAllData ) );

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
        PersistenceUtil.SetConfig( imageSourceName, "cache", String.valueOf( cacheAllData ) );
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

        PersistenceUtil.SetConfig( imageSourceName, "trainingEntities", String.valueOf( classifierName ) );
        if( !logDuringTraining ) {
            PersistenceUtil.SetConfig( imageSourceName, "testingEntities", featureSeriesName + "," + labelSeriesName );
        }

        if( testType == TEST_TYPE_IMAGE_SEQUENCE ) {
        }
        else if( testType == TEST_TYPE_TEXT_SEQUENCE ) {
            PersistenceUtil.SetConfig( imageSourceName, "sourceTextFileTraining", textFileTraining );
            PersistenceUtil.SetConfig( imageSourceName, "sourceTextFileTesting", textFileTesting );
            PersistenceUtil.SetConfig( imageSourceName, "asciiEncoding", String.valueOf( asciiEncoding ) );
        }

        // Region-layers
//        ArrayList< String > learningEntities = new ArrayList< String >();

        String ffEntityName = imageSourceName;
        String ffDataSuffix = ImageLabelEntity.OUTPUT_IMAGE;

        int[] layerSizes = new int[ layers ];
//        layerSizes[ 0 ] = 24;//16;// 16*16 = 256
        layerSizes[ 0 ] = 32;

        int layer = 0;
//        for( int layer = 0; layer < layers; ++layer ) {

            int widthCellsF = layerSizes[ layer ];
            int heightCellsF = widthCellsF;
            int widthCellsB = widthCellsF * 2; // double the number of cells
            int heightCellsB = widthCellsF;
//            int areaCells = widthCells * heightCells;

            float sparsityOutputFactor = 1f;//.5f;
            int batchSizeF = 16;//16;//5;32;//64;//40;//40;//128;//32;//128; // want small for faster training, but large enough to do lifetime sparsity
            int batchSizeB = 20;//16;//5;32;//64;//40;//40;//128;//32;//128; // want small for faster training, but large enough to do lifetime sparsity
            int sparsityBatchF = 1;//4;//1;
            int sparsityBatchB = 2;//4;//1;
//            int sparsityTrainingF = 40;//40;
//            int sparsityTrainingB = 10;
            int sparsityTrainingF = 25;//18;//18;//20;//40;
            int sparsityTrainingB = 25;
            int sparsityOutputF = LifetimeSparseAutoencoder.FindOutputSparsity( sparsityTrainingF, sparsityOutputFactor );//1;
            int sparsityOutputB = LifetimeSparseAutoencoder.FindOutputSparsity( sparsityTrainingB, sparsityOutputFactor );//1;

            float learningRate = 0.01f;
            float momentum = 0.5f; // 0.9 in paper, seems better
            float weightsStdDev = 0.01f; // confirmed. Sigma From paper. used at reset

            int cellMappingDensity = 10;

//            String classifierEntityName = classifierName + layer;
//            learningEntities.add( classifierEntityName );
            parentName = PersistenceUtil.CreateEntity( classifierName, SparseSequenceAutoencoderEntity.ENTITY_TYPE, n.getName(), parentName );
            SparseSequenceAutoencoderEntityConfig.Set(
                    classifierName, cacheAllData,
                    cellMappingDensity,
                    sparsityTrainingF,
                    sparsityOutputF,
                    sparsityBatchF,
                    sparsityTrainingB,
                    sparsityOutputB,
                    sparsityBatchB,
                    widthCellsF, heightCellsF,
                    widthCellsB, heightCellsB,
                    batchSizeF, batchSizeB,
                    learningRate, momentum, weightsStdDev );

        parentName = PersistenceUtil.CreateEntity(   delayEntityName, DelayEntity   .ENTITY_TYPE, n.getName(), parentName );
        parentName = PersistenceUtil.CreateEntity( featureSeriesName, DataFileEntity.ENTITY_TYPE, n.getName(), parentName ); // 2nd, class region updates after first to get its feedback
        parentName = PersistenceUtil.CreateEntity(   labelSeriesName, DataFileEntity.ENTITY_TYPE, n.getName(), parentName ); // 2nd, class region updates after first to get its feedback

//Delay 1
//?,1 class -> A    A -> Delay -> B
//B,2 class -> B    B -> Delay -> A
//A,3 class -> C    C -> Delay -> B

//No delay
//?,1 class -> A
//A,2 class -> B
//B,3 class -> C
//C,1 class -> A

        // Connect inputs to the classifier
        DataRefUtil.SetDataReference( classifierName, SparseSequenceAutoencoderEntity.INPUT_F, ffEntityName, ffDataSuffix );
//            DataRefUtil.SetDataReference( classifierEntityName, SequenceSparseAutoencoderEntity.INPUT_B, delayEntityName, DelayEntity.DATA_OUTPUT );
//            DataRefUtil.SetDataReference( classifierEntityName, SequenceSparseAutoencoderEntity.INPUT_B, classifierEntityName, LifetimeSparseAutoencoderEntity.SPIKES + SequenceSparseAutoencoderEntity.SUFFIX_F );
//            DataRefUtil.SetDataReference( classifierEntityName, SequenceSparseAutoencoderEntity.INPUT_B, classifierEntityName, LifetimeSparseAutoencoderEntity.SPIKES + SequenceSparseAutoencoderEntity.SUFFIX_B );
        DataRefUtil.SetDataReference( classifierName, SparseSequenceAutoencoderEntity.INPUT_B, classifierName, SparseSequenceAutoencoderEntity.OUTPUT );

        // Delay the classifier spikes
        ArrayList< AbstractPair< String, String > > predictionDatas = new ArrayList<>();
//        predictionDatas.add( new AbstractPair<>( classifierEntityName, LifetimeSparseAutoencoderEntity.SPIKES + SequenceSparseAutoencoderEntity.SUFFIX_B ) );
        predictionDatas.add( new AbstractPair<>( classifierName, SparseSequenceAutoencoderEntity.OUTPUT ) );
        DataRefUtil.SetDataReferences( delayEntityName, DelayEntity.DATA_INPUT, predictionDatas );

        // Associate the delayed spikes with the current label
        ArrayList< AbstractPair< String, String > > featureDatas = new ArrayList<>();
        featureDatas.add( new AbstractPair<>( delayEntityName, DelayEntity.DATA_OUTPUT ) );
        DataRefUtil.SetDataReferences( featureSeriesName, DataFileEntity.INPUT_WRITE, featureDatas ); // get current state from the region to be used to predict
        DataRefUtil.SetDataReference( labelSeriesName, DataFileEntity.INPUT_WRITE, imageSourceName, ImageLabelEntity.OUTPUT_LABEL ); // get current state from the region to be used to predict

        // Log features of the algorithm during all phases
        boolean write = true;
        boolean read = false;
        boolean append = true;
        String fileNameRead = null;
        DataFileEntityConfig.Set(
                featureSeriesName, cacheAllData, write, read, append, DataJsonSerializer.ENCODING_SPARSE_REAL, fileNameWriteFeatures, fileNameRead );

        // Log labels of each image produced during all phases
        DataFileEntityConfig.Set(
                labelSeriesName, cacheAllData, write, read, append, DataJsonSerializer.ENCODING_DENSE, fileNameWriteLabels, fileNameRead );



            // Connect the predictor back to the classifier to invert the prediction into the input space
//            if( layer == 0 ) {
//                DataRefUtil.SetDataReference( classifierEntityName, LifetimeSparseAutoencoderEntity.INPUT_SPIKES, delayEntityName, DelayEntity.DATA_OUTPUT ); // current prediction

                // Log number of unique state-paths in each batch
                parentName = PersistenceUtil.CreateEntity( valueSeriesUniqueName, ValueSeriesEntity.ENTITY_TYPE, n.getName(), parentName );
                PersistenceUtil.SetConfig( valueSeriesUniqueName, "period", String.valueOf( "-1" ) ); // infinite
                PersistenceUtil.SetConfig( valueSeriesUniqueName, "learn", String.valueOf( "true" ) );
                PersistenceUtil.SetConfig( valueSeriesUniqueName, "entityName", classifierName );
                PersistenceUtil.SetConfig( valueSeriesUniqueName, "configPath", "uniqueRows" );

                // Add data queues for debugging
        String dataQueueClassificationName  = PersistenceUtil.GetEntityName( "data-queue-classification" );
        String dataQueuePredictionName  = PersistenceUtil.GetEntityName( "data-queue-prediction" );
                parentName = PersistenceUtil.CreateEntity( dataQueueClassificationName, DataQueueEntity.ENTITY_TYPE, n.getName(), parentName );
                DataRefUtil.SetDataReference( dataQueueClassificationName, DataQueueEntity.DATA_INPUT, classifierName, SparseSequenceAutoencoderEntity.OUTPUT );
                PersistenceUtil.SetConfig( dataQueueClassificationName, "queueLength", String.valueOf( queueLength ) );
                PersistenceUtil.SetConfig( dataQueueClassificationName, "cache", String.valueOf( cacheAllData ) );

//                parentName = PersistenceUtil.CreateEntity( dataQueuePredictionName, DataQueueEntity.ENTITY_TYPE, n.getName(), parentName );
//                DataRefUtil.SetDataReference( dataQueuePredictionName, DataQueueEntity.DATA_INPUT, classifierEntityName, SequenceSparseAutoencoderEntity.OUTPUT_DECODED_PREDICTION ); // current prediction
//                PersistenceUtil.SetConfig( dataQueuePredictionName, "queueLength", String.valueOf( queueLength ) );
//                PersistenceUtil.SetConfig( dataQueuePredictionName, "cache", String.valueOf( cacheAllData ) );
//            }

            // Update the feed-forward data reference:
//            ffEntityName = classifierEntityName;
//            ffDataSuffix = LifetimeSparseAutoencoderEntity.SPIKES + SequenceSparseAutoencoderEntity.SUFFIX_F;
//        }//*/
    }

}
