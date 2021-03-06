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

package io.agi.framework.demo.papers;

import io.agi.core.ann.unsupervised.LifetimeSparseAutoencoder;
import io.agi.core.orm.AbstractPair;
import io.agi.core.util.images.BufferedImageSource.BufferedImageSourceFactory;
import io.agi.framework.Framework;
import io.agi.framework.Node;
import io.agi.framework.demo.CreateEntityMain;
import io.agi.framework.demo.mnist.ImageLabelEntity;
import io.agi.framework.entities.*;
import io.agi.framework.persistence.DataJsonSerializer;
import io.agi.framework.persistence.PersistenceUtil;
import io.agi.framework.persistence.models.ModelData;
import io.agi.framework.references.DataRefUtil;

import java.io.File;
import java.util.ArrayList;

/**
 * Ad-hoc experiments:
 * TESTED 81.37% / 77.20% on 10k train, 1k test, batchsize = 50, lifetime = 2, sparsity=25.
 * TESTED 89.31% / 88.66% on 60k train, 10k test, batchsize = 32, lifetime = 2, sparsity=25, momentum=0.5
 * TESTED 82.98% / 83.23% on 4x 60k train, 60+10k test, batchsize = 128, lifetime = 1 others same, learningRate = 0.01    <-- this looked pretty good in terms of reconstructions, a few errors, in the GUI
 * TESTED 50.06% / 49.92% as above but faster learningrate = 0.1, seems to be bad to increase learning rate
 * TESTED 98.95% / 97.39% learningRate 0.01 momentum 0.1 32x32 cells, sparsity 25, sparsityLifetime 2, batchSize 32 ?epochs=?
 *
 * TESTED - as above, but with biased memory feature coded and disabled:
 * 88.90% / 88.43%
 * 89.05% / 88.00% So we have a stable score.
 *
 * 1.5x output sparsity, 1 epoch (60k). New data refactor. Batch size 32, momentum 0.9
 * Errors: 2968 of 60000 = 95.05333% correct. / Errors: 574 of 10000 = 94.26% correct.
 *
 * With momentum bug fixed and training batch idle cells towards max error in reconstruction:
 * TESTED 88.9% / 84.8% 1 epoch (60k). momentum 0.9
 * TESTED 96.8% / 94.8% 1 epoch (60k) momentum 0.0
 * Existing results are momentum = 0, (old) lifetime sparsity selection. So try old sparsity selection WITH momentum.
 * TESTED 98.6% / 96.82% momentum 0.9
 *
 * Created by dave on 8/07/16.
 */
public class BatchSparseAutoencoderExpt extends CreateEntityMain {

    // TODO list:
    //- store and plot lifetime usage rates for all cells
    //- 128 x1 vs 32*2 = 1/16th vs 1/128= 8 times less learning updates. But only 4x training epochs.. vs 1 or 2.

    public static void main( String[] args ) {
        BatchSparseAutoencoderExpt demo = new BatchSparseAutoencoderExpt();
        demo.mainImpl(args);
    }

    public static String getTrainingPath() {
//        String trainingPath = "/Users/gideon/Development/ProjectAGI/AGIEF/datasets/mnist/training-small";
//        String trainingPath = "/home/dave/workspace/agi.io/data/mnist/1k_test";
//        String trainingPath = "/home/dave/workspace/agi.io/data/mnist/10k_train";
//        String trainingPath = "/home/dave/workspace/agi.io/data/mnist/cycle10";
        String trainingPath = "/home/dave/workspace/agi.io/data/mnist/all/all_train";
        return trainingPath;
    }

    public static String getTestingPath() {
//        String testingPath = "/Users/gideon/Development/ProjectAGI/AGIEF/datasets/mnist/training-small, /Users/gideon/Development/ProjectAGI/AGIEF/datasets/mnist/testing-small";
//        String testingPath = "/home/dave/workspace/agi.io/data/mnist/1k_test";
//        String testingPath = "/home/dave/workspace/agi.io/data/mnist/10k_train,/home/dave/workspace/agi.io/data/mnist/1k_test";
//        String testingPath = "/home/dave/workspace/agi.io/data/mnist/cycle10,/home/dave/workspace/agi.io/data/mnist/cycle10";
        String testingPath = "/home/dave/workspace/agi.io/data/mnist/all/all_train,/home/dave/workspace/agi.io/data/mnist/all/all_t10k";
        return testingPath;
    }

    public static String getOutputPath() {
        return "/home/dave/Desktop/agi/data/batch_sparse";
    }

    public void createEntities( Node n ) {

        String trainingPath = getTrainingPath();
        String testingPath = getTestingPath();

        String fileNameWriteFeatures = getOutputPath() + File.separator + "features.csv";
        String fileNameWriteLabels = getOutputPath() + File.separator + "labels.csv";

//        boolean logDuringTraining = true;
        boolean logDuringTraining = false;
        boolean cacheAllData = true;
        boolean terminateByAge = false;
        int terminationAge = -1;//50000;//25000;
        int trainingEpochs = 1;//16;
        int testingEpochs = 1;

        // Define some entities
        String experimentName           = PersistenceUtil.GetEntityName( "experiment" );
        String imageLabelName           = PersistenceUtil.GetEntityName( "image-class" );
        String autoencoderName          = PersistenceUtil.GetEntityName( "autoencoder" );
        String featureSeriesName        = PersistenceUtil.GetEntityName( "feature-series" );
        String labelSeriesName          = PersistenceUtil.GetEntityName( "label-series" );

        String parentName = null;
        parentName = PersistenceUtil.CreateEntity( experimentName, ExperimentEntity.ENTITY_TYPE, n.getName(), parentName ); // experiment is the root entity
        parentName = PersistenceUtil.CreateEntity( imageLabelName, ImageLabelEntity.ENTITY_TYPE, n.getName(), parentName );
        parentName = PersistenceUtil.CreateEntity( autoencoderName, LifetimeSparseAutoencoderEntity.ENTITY_TYPE, n.getName(), parentName );
//        parentName = PersistenceUtil.CreateEntity( autoencoderName, BiasedSparseAutoencoderEntity.ENTITY_TYPE, n.getName(), parentName );
        parentName = PersistenceUtil.CreateEntity( featureSeriesName, DataFileEntity.ENTITY_TYPE, n.getName(), parentName ); // 2nd, class region updates after first to get its feedback
        parentName = PersistenceUtil.CreateEntity(   labelSeriesName, DataFileEntity.ENTITY_TYPE, n.getName(), parentName ); // 2nd, class region updates after first to get its feedback

        // Connect the entities' data
        // a) Image to image region, and decode
        DataRefUtil.SetDataReference( autoencoderName, LifetimeSparseAutoencoderEntity.INPUT, imageLabelName, ImageLabelEntity.OUTPUT_IMAGE );

        ArrayList< AbstractPair< String, String > > featureDatas = new ArrayList<>();
        featureDatas.add( new AbstractPair<>( autoencoderName, LifetimeSparseAutoencoderEntity.SPIKES ) );
        DataRefUtil.SetDataReferences( featureSeriesName, DataFileEntity.INPUT_WRITE, featureDatas ); // get current state from the region to be used to predict
        DataRefUtil.SetDataReference( labelSeriesName, DataFileEntity.INPUT_WRITE, imageLabelName, ImageLabelEntity.OUTPUT_LABEL ); // get current state from the region to be used to predict

        // Experiment config
        if( !terminateByAge ) {
            PersistenceUtil.SetConfig( experimentName, "terminationEntityName", imageLabelName );
            PersistenceUtil.SetConfig( experimentName, "terminationConfigPath", "terminate" );
            PersistenceUtil.SetConfig( experimentName, "terminationAge", "-1" ); // wait for mnist to decide
        }
        else {
            PersistenceUtil.SetConfig( experimentName, "terminationAge", String.valueOf( terminationAge ) ); // fixed steps
        }

        // cache all data for speed, when enabled
        PersistenceUtil.SetConfig( experimentName, "cache", String.valueOf( cacheAllData ) );
        PersistenceUtil.SetConfig( imageLabelName, "cache", String.valueOf( cacheAllData ) );
        PersistenceUtil.SetConfig( autoencoderName, "cache", String.valueOf( cacheAllData ) );
        PersistenceUtil.SetConfig( featureSeriesName, "cache", String.valueOf( cacheAllData ) );
        PersistenceUtil.SetConfig( labelSeriesName, "cache", String.valueOf( cacheAllData ) );

        // MNIST config
        PersistenceUtil.SetConfig( imageLabelName, "receptiveField.receptiveFieldX", "0" );
        PersistenceUtil.SetConfig( imageLabelName, "receptiveField.receptiveFieldY", "0" );
        PersistenceUtil.SetConfig( imageLabelName, "receptiveField.receptiveFieldW", "28" );
        PersistenceUtil.SetConfig( imageLabelName, "receptiveField.receptiveFieldH", "28" );
        PersistenceUtil.SetConfig( imageLabelName, "resolution.resolutionX", "28" );
        PersistenceUtil.SetConfig( imageLabelName, "resolution.resolutionY", "28" );
        PersistenceUtil.SetConfig( imageLabelName, "greyscale", "true" );
        PersistenceUtil.SetConfig( imageLabelName, "invert", "true" );
        PersistenceUtil.SetConfig( imageLabelName, "sourceType", BufferedImageSourceFactory.TYPE_IMAGE_FILES );
        PersistenceUtil.SetConfig( imageLabelName, "sourceFilesPrefix", "postproc" );
        PersistenceUtil.SetConfig( imageLabelName, "sourceFilesLabelIndex", "2" );
        PersistenceUtil.SetConfig( imageLabelName, "sourceFilesPathTraining", trainingPath );
        PersistenceUtil.SetConfig( imageLabelName, "sourceFilesPathTesting", testingPath );
        PersistenceUtil.SetConfig( imageLabelName, "trainingEpochs", String.valueOf( trainingEpochs ) );
        PersistenceUtil.SetConfig( imageLabelName, "testingEpochs", String.valueOf( testingEpochs ) );
        PersistenceUtil.SetConfig( imageLabelName, "trainingEntities", String.valueOf( autoencoderName ) );
        if( !logDuringTraining ) {
            PersistenceUtil.SetConfig( imageLabelName, "testingEntities", featureSeriesName + "," + labelSeriesName );
        }

        /* Suppose we are aiming for a sparsity level of k = 15.
        Then, we start off with a large sparsity level (e.g.
        k = 100) for which the k -sparse autoencoder can train
        all the hidden units. We then linearly decrease the
        sparsity level from k = 100 to k = 15 over the first
        half of the epochs. This initializes the autoencoder in
        a good regime, for which all of the hidden units have
        a significant chance of being picked. Then, we keep
        k = 15 for the second half of the epochs. */
        int widthCells = 32; // from the paper, 32x32= ~1000 was optimal on MNIST (but with a supervised output layer)
        int heightCells = 32;
        int sparsity = 25; // k sparse confirmed err = 1.35%
        // https://raw.githubusercontent.com/stephenbalaban/convnet/master/K-Sparse%20Autoencoder.ipynb
        // "AE.train(X,X,X_test,None,\n",
        //        "         dataset_size=60000,\n",
        //        "         batch_size=128,\n",
        //        "         initial_weights=.01*nn.randn(AE.cfg.num_parameters),\n",
        //        "         momentum=.9,\n",
        //        "         learning_rate=.01,\n",
//        int batchSize = 128; // value from Ali's code.
        int batchSize = 32;//128; // want small for faster training, but large enough to do lifetime sparsity
        int sparsityLifetime = 2;//1;
        int sparsityOutput = LifetimeSparseAutoencoder.FindOutputSparsity( sparsity, 1.5f );//1;
        float learningRate = 0.01f;
//        float learningRate = 0.1f; BAD
        float momentum = 0.9f; // 0.9 in paper
//        float momentum = 0f; // 0.9 in paper
        float weightsStdDev = 0.01f; // confirmed. Sigma From paper. used at reset

//        PersistenceUtil.SetConfig( autoencoderName, "learningRate", String.valueOf( learningRate ) );
//        PersistenceUtil.SetConfig( autoencoderName, "momentum", String.valueOf( momentum ) );
//        PersistenceUtil.SetConfig( autoencoderName, "widthCells", String.valueOf( widthCells ) );
//        PersistenceUtil.SetConfig( autoencoderName, "heightCells", String.valueOf( heightCells ) );
//        PersistenceUtil.SetConfig( autoencoderName, "weightsStdDev", String.valueOf( weightsStdDev ) );
//        PersistenceUtil.SetConfig( autoencoderName, "sparsity", String.valueOf( sparsity ) );
//        PersistenceUtil.SetConfig( autoencoderName, "sparsityLifetime", String.valueOf( sparsityLifetime ) );
//        PersistenceUtil.SetConfig( autoencoderName, "batchSize", String.valueOf( batchSize ) );
        LifetimeSparseAutoencoderEntityConfig.Set(
                autoencoderName, cacheAllData,
                widthCells, heightCells,
                sparsity, sparsityLifetime, sparsityOutput,
                batchSize, learningRate, momentum, weightsStdDev );

        // Log features of the algorithm during all phases
        boolean write = true;
        boolean read = false;
        boolean append = true;
        String fileNameRead = null;
        DataFileEntityConfig.Set(
                featureSeriesName, cacheAllData, write, read, append, DataJsonSerializer.ENCODING_SPARSE_REAL, fileNameWriteFeatures, fileNameRead );
//        PersistenceUtil.SetConfig( vectorSeriesName, "encoding", ModelData.ENCODING_SPARSE_REAL );
//        PersistenceUtil.SetConfig( vectorSeriesName, "flushPeriod", String.valueOf( flushInterval ) ); // accumulate and flush, or accumulate only
//        PersistenceUtil.SetConfig( vectorSeriesName, "period", String.valueOf( "-1" ) );
//        PersistenceUtil.SetConfig( vectorSeriesName, "writeFilePath", flushWriteFilePath );
//        PersistenceUtil.SetConfig( vectorSeriesName, "writeFilePrefix", flushWriteFilePrefixFeatures );
//        PersistenceUtil.SetConfig( vectorSeriesName, "learn", String.valueOf( "true" ) );


        // Log labels of each image produced during all phases
        DataFileEntityConfig.Set(
                labelSeriesName, cacheAllData, write, read, append, DataJsonSerializer.ENCODING_DENSE, fileNameWriteLabels, fileNameRead );
//        PersistenceUtil.SetConfig( labelSeriesName, "writeFileEncoding", ModelData.ENCODING_DENSE );
//        PersistenceUtil.SetConfig( labelSeriesName, "flushPeriod", String.valueOf( flushInterval ) ); // accumulate and flush, or accumulate only
//        PersistenceUtil.SetConfig( labelSeriesName, "period", String.valueOf( "-1" ) ); // infinite
//        PersistenceUtil.SetConfig( labelSeriesName, "learn", String.valueOf( "true" ) );
//        PersistenceUtil.SetConfig( labelSeriesName, "writeFilePath", flushWriteFilePath );
//        PersistenceUtil.SetConfig( labelSeriesName, "writeFilePrefix", flushWriteFilePrefixTruth );
//        PersistenceUtil.SetConfig( labelSeriesName, "entityName", imageLabelName );
//        PersistenceUtil.SetConfig( labelSeriesName, "configPath", "imageLabel" );
    }

}
