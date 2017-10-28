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
import io.agi.core.util.images.BufferedImageSource.BufferedImageSourceFactory;
import io.agi.framework.Framework;
import io.agi.framework.Node;
import io.agi.framework.demo.CreateEntityMain;
import io.agi.framework.demo.mnist.ImageLabelEntity;
import io.agi.framework.entities.*;
import io.agi.framework.persistence.PersistenceUtil;
import io.agi.framework.references.DataRefUtil;

import java.util.ArrayList;

/**
 * Created by dave on 16/10/17.
 */
public class SparseNetworkExpt extends CreateEntityMain {

    public static final int TEST_TYPE_IMAGE_SEQUENCE = 0;
    public static final int TEST_TYPE_TEXT_SEQUENCE  = 1;

    public static void main( String[] args ) {
        SparseNetworkExpt expt = new SparseNetworkExpt();
        expt.mainImpl( args );
    }

    public void createEntities( Node n ) {

//        int testType = TEST_TYPE_TEXT_SEQUENCE; // a sequence of characters e.g. A..Z or 0..9. Random exemplar for each
        int testType = TEST_TYPE_IMAGE_SEQUENCE; // a sequence of characters e.g. A..Z or 0..9. Random exemplar for each
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

        int trainingEpochs = 2000;
        int testingEpochs = 1;//10;//80; // good for up to 80k
        int terminationAge = 5000;//25000;
        boolean terminateByAge = false;
        int imageRepeats = 1;//50;
        boolean cacheAllData = true;
        boolean doLogging = true;
        int queueLength = 46;
        int layers = 1;

        // Define some entities
        String experimentName           = PersistenceUtil.GetEntityName( "experiment" );
        String constantName             = PersistenceUtil.GetEntityName( "constant" );
        String imageSourceName          = PersistenceUtil.GetEntityName( "image-source" );
//        String valueSeriesTruthName     = PersistenceUtil.GetEntityName( "value-series-truth" );
//        String valueSeriesErrorFnName   = PersistenceUtil.GetEntityName( "value-series-error-fn" );
        String dataQueueClassificationName  = PersistenceUtil.GetEntityName( "data-queue-classification" );
        String dataQueuePredictionName  = PersistenceUtil.GetEntityName( "data-queue-prediction" );

//        String dataQueueOutputName = PersistenceUtil.GetEntityName( "data-queue-output-" );
        String classifierName = PersistenceUtil.GetEntityName( "classifier-" );
        String predictorName = PersistenceUtil.GetEntityName( "predictor-" );
        String poolingName = PersistenceUtil.GetEntityName( "pooler-" );

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

        if( testType == TEST_TYPE_IMAGE_SEQUENCE ) {
        }
        else if( testType == TEST_TYPE_TEXT_SEQUENCE ) {
            PersistenceUtil.SetConfig( imageSourceName, "sourceTextFileTraining", textFileTraining );
            PersistenceUtil.SetConfig( imageSourceName, "sourceTextFileTesting", textFileTesting );
        }

        // Region-layers
        ArrayList< String > learningEntities = new ArrayList< String >();

        String ffEntityName = imageSourceName;
        String ffDataSuffix = ImageLabelEntity.OUTPUT_IMAGE;

        int[] layerSizes = new int[ layers ];
        layerSizes[ 0 ] = 32;

        for( int layer = 0; layer < layers; ++layer ) {

            int widthCells = layerSizes[ layer ];
            int heightCells = widthCells;
            int areaCells = widthCells * heightCells;

            int sparsity = 25; // k sparse confirmed err = 1.35%
            int batchSize = 32;//128; // want small for faster training, but large enough to do lifetime sparsity
            int sparsityLifetime = 2;//1;
            int sparsityOutputClassifier = LifetimeSparseAutoencoder.FindOutputSparsity( sparsity, 1.5f );//1;
            int sparsityOutputPredictor = sparsity;

            float learningRate = 0.01f;
            float momentum = 0.5f; // 0.9 in paper, seems better
            float weightsStdDev = 0.01f; // confirmed. Sigma From paper. used at reset

            String classifierEntityName = classifierName + layer;
            learningEntities.add( classifierEntityName );
            parentName = PersistenceUtil.CreateEntity( classifierEntityName, LifetimeSparseAutoencoderEntity.ENTITY_TYPE, n.getName(), parentName );
            LifetimeSparseAutoencoderEntityConfig.Set( classifierEntityName, cacheAllData, widthCells, heightCells, sparsity, sparsityLifetime, sparsityOutputClassifier, batchSize, learningRate, momentum, weightsStdDev );

            String delayEntityName = "delay-" + layer;
            parentName = PersistenceUtil.CreateEntity( delayEntityName, DelayEntity.ENTITY_TYPE, n.getName(), parentName );

            float outputDecayRate = 0.95f;
            int outputSpikeAgeMax = 20;

            String poolingEntityName = poolingName + layer;
            parentName = PersistenceUtil.CreateEntity( poolingEntityName, PredictiveCodingEntity.ENTITY_TYPE, n.getName(), parentName );
            PredictiveCodingEntityConfig.Set( poolingEntityName, cacheAllData, widthCells, heightCells, outputSpikeAgeMax, outputDecayRate );

            // generate a new prediction given current state
            int predictorOutputs = areaCells;
            String predictorEntityName = predictorName + layer;
            learningEntities.add( predictorEntityName );
            parentName = PersistenceUtil.CreateEntity( predictorEntityName, BatchSparseNetworkEntity.ENTITY_TYPE, n.getName(), parentName );
            BatchSparseNetworkEntityConfig.Set( predictorEntityName, cacheAllData, widthCells, heightCells, predictorOutputs, sparsity, sparsityLifetime, sparsityOutputPredictor, batchSize, learningRate, momentum, weightsStdDev );


            // Now connect the entities Data together:

            // Connect inputs to the classifier
            DataRefUtil.SetDataReference( classifierEntityName, LifetimeSparseAutoencoderEntity.INPUT, ffEntityName, ffDataSuffix );

            // Delay the classifier spikes
            DataRefUtil.SetDataReference( delayEntityName, DelayEntity.DATA_INPUT, classifierEntityName, LifetimeSparseAutoencoderEntity.SPIKES );

            // Connect inputs to the temporal pooling
            DataRefUtil.SetDataReference( poolingEntityName, PredictiveCodingEntity.INPUT_OBSERVED, classifierEntityName, LifetimeSparseAutoencoderEntity.SPIKES );
            DataRefUtil.SetDataReference( poolingEntityName, PredictiveCodingEntity.INPUT_PREDICTED, predictorEntityName, BatchSparseNetworkEntity.OUTPUT_TESTING_OUTPUT );

            // Connect inputs to the predictor
            DataRefUtil.SetDataReference( predictorEntityName, BatchSparseNetworkEntity.INPUT_TESTING_INPUT, classifierEntityName, LifetimeSparseAutoencoderEntity.SPIKES );
            DataRefUtil.SetDataReference( predictorEntityName, BatchSparseNetworkEntity.INPUT_TRAINING_INPUT, delayEntityName, DelayEntity.DATA_OUTPUT ); // use old spikes
            DataRefUtil.SetDataReference( predictorEntityName, BatchSparseNetworkEntity.INPUT_TRAINING_OUTPUT, classifierEntityName, LifetimeSparseAutoencoderEntity.SPIKES ); // to predict new spikes

            // Connect the predictor back to the classifier to invert the prediction into the input space
            if( layer == 0 ) {
                DataRefUtil.SetDataReference( classifierEntityName, LifetimeSparseAutoencoderEntity.INPUT_SPIKES, predictorEntityName, BatchSparseNetworkEntity.OUTPUT_TESTING_OUTPUT ); // current prediction

                // Add data queues for debugging
                parentName = PersistenceUtil.CreateEntity( dataQueueClassificationName, DataQueueEntity.ENTITY_TYPE, n.getName(), parentName );
                DataRefUtil.SetDataReference( dataQueueClassificationName, DataQueueEntity.DATA_INPUT, classifierEntityName, LifetimeSparseAutoencoderEntity.OUTPUT_RECONSTRUCTION ); // current prediction
                PersistenceUtil.SetConfig( dataQueueClassificationName, "queueLength", String.valueOf( queueLength ) );
                PersistenceUtil.SetConfig( dataQueueClassificationName, "cache", String.valueOf( cacheAllData ) );

                parentName = PersistenceUtil.CreateEntity( dataQueuePredictionName, DataQueueEntity.ENTITY_TYPE, n.getName(), parentName );
                DataRefUtil.SetDataReference( dataQueuePredictionName, DataQueueEntity.DATA_INPUT, classifierEntityName, LifetimeSparseAutoencoderEntity.OUTPUT_DECODED ); // current prediction
                PersistenceUtil.SetConfig( dataQueuePredictionName, "queueLength", String.valueOf( queueLength ) );
                PersistenceUtil.SetConfig( dataQueuePredictionName, "cache", String.valueOf( cacheAllData ) );
            }

            // Update the feed-forward data reference:
            ffEntityName = poolingEntityName;
            ffDataSuffix = PredictiveCodingEntity.OUTPUT;
        }

    }

}
