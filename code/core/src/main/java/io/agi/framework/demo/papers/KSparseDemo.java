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

package io.agi.framework.demo.papers;

import io.agi.core.orm.AbstractPair;
import io.agi.core.util.PropertiesUtil;
import io.agi.core.util.images.BufferedImageSource.BufferedImageSourceFactory;
import io.agi.framework.Framework;
import io.agi.framework.Main;
import io.agi.framework.Node;
import io.agi.framework.demo.CreateEntityMain;
import io.agi.framework.demo.mnist.ImageLabelEntity;
import io.agi.framework.entities.*;
import io.agi.framework.factories.CommonEntityFactory;
import io.agi.framework.persistence.models.ModelData;

import java.util.ArrayList;
import java.util.Properties;

/**
 * Created by dave on 8/07/16.
 */
public class KSparseDemo extends CreateEntityMain {

    public static void main( String[] args ) {
        KSparseDemo demo = new KSparseDemo();
        demo.mainImpl(args);
    }

    public void createEntities( Node n ) {

//        String trainingPath = "/Users/gideon/Development/ProjectAGI/AGIEF/datasets/mnist/training-small";
//        String testingPath = "/Users/gideon/Development/ProjectAGI/AGIEF/datasets/mnist/training-small, /Users/gideon/Development/ProjectAGI/AGIEF/datasets/mnist/testing-small";

//        String trainingPath = "/home/dave/workspace/agi.io/data/mnist/1k_test";
//        String  testingPath = "/home/dave/workspace/agi.io/data/mnist/1k_test";

        String trainingPath = "/home/dave/workspace/agi.io/data/mnist/all/all_train";
        String  testingPath = "/home/dave/workspace/agi.io/data/mnist/all/all_train,/home/dave/workspace/agi.io/data/mnist/all/all_t10k";

//        String trainingPath = "/home/dave/workspace/agi.io/data/mnist/10k_train";
//        String  testingPath = "/home/dave/workspace/agi.io/data/mnist/1k_test";

//        String trainingPath = "/home/dave/workspace/agi.io/data/mnist/cycle10";
//        String testingPath = "/home/dave/workspace/agi.io/data/mnist/cycle10";

//        int flushInterval = 20;
        int flushInterval = -1; // never flush
        String flushWriteFilePath = "/home/dave/workspace/agi.io/data/flush";
        String flushWriteFilePrefixTruth = "flushedTruth";
        String flushWriteFilePrefixFeatures = "flushedFeatures";

//        boolean logDuringTraining = true;
        boolean logDuringTraining = false;
        boolean cacheAllData = true;
        boolean terminateByAge = false;
        int terminationAge = -1;//50000;//25000;
        int trainingEpochs = 1;
        int testingEpochs = 1;
        int imagesPerEpoch = 1000;

        // Define some entities
        String experimentName           = Framework.GetEntityName( "experiment" );
        String imageLabelName           = Framework.GetEntityName( "image-class" );
        String autoencoderName          = Framework.GetEntityName( "autoencoder" );
        String vectorSeriesName         = Framework.GetEntityName( "feature-series" );
        String valueSeriesName          = Framework.GetEntityName( "label-series" );

        Framework.CreateEntity( experimentName, ExperimentEntity.ENTITY_TYPE, n.getName(), null ); // experiment is the root entity
        Framework.CreateEntity( imageLabelName, ImageLabelEntity.ENTITY_TYPE, n.getName(), experimentName );
        Framework.CreateEntity( autoencoderName, KSparseAutoencoderEntity.ENTITY_TYPE, n.getName(), imageLabelName );
        Framework.CreateEntity( vectorSeriesName, VectorSeriesEntity.ENTITY_TYPE, n.getName(), autoencoderName ); // 2nd, class region updates after first to get its feedback
        Framework.CreateEntity( valueSeriesName, ValueSeriesEntity.ENTITY_TYPE, n.getName(), vectorSeriesName ); // 2nd, class region updates after first to get its feedback

        // Connect the entities' data
        // a) Image to image region, and decode
        Framework.SetDataReference( autoencoderName, KSparseAutoencoderEntity.INPUT, imageLabelName, ImageLabelEntity.OUTPUT_IMAGE );

        ArrayList< AbstractPair< String, String > > featureDatas = new ArrayList<>();
        featureDatas.add( new AbstractPair<>( autoencoderName, KSparseAutoencoderEntity.SPIKES_TOP_KA ) );
        Framework.SetDataReferences( vectorSeriesName, VectorSeriesEntity.INPUT, featureDatas ); // get current state from the region to be used to predict

        // Experiment config
        if( !terminateByAge ) {
            Framework.SetConfig( experimentName, "terminationEntityName", imageLabelName );
            Framework.SetConfig( experimentName, "terminationConfigPath", "terminate" );
            Framework.SetConfig( experimentName, "terminationAge", "-1" ); // wait for mnist to decide
        }
        else {
            Framework.SetConfig( experimentName, "terminationAge", String.valueOf( terminationAge ) ); // fixed steps
        }

        // cache all data for speed, when enabled
        Framework.SetConfig( experimentName, "cache", String.valueOf( cacheAllData ) );
        Framework.SetConfig( imageLabelName, "cache", String.valueOf( cacheAllData ) );
        Framework.SetConfig( autoencoderName, "cache", String.valueOf( cacheAllData ) );
        Framework.SetConfig( vectorSeriesName, "cache", String.valueOf( cacheAllData ) );
        Framework.SetConfig( valueSeriesName, "cache", String.valueOf( cacheAllData ) );

        // MNIST config
        Framework.SetConfig( imageLabelName, "receptiveField.receptiveFieldX", "0" );
        Framework.SetConfig( imageLabelName, "receptiveField.receptiveFieldY", "0" );
        Framework.SetConfig( imageLabelName, "receptiveField.receptiveFieldW", "28" );
        Framework.SetConfig( imageLabelName, "receptiveField.receptiveFieldH", "28" );
        Framework.SetConfig( imageLabelName, "resolution.resolutionX", "28" );
        Framework.SetConfig( imageLabelName, "resolution.resolutionY", "28" );
        Framework.SetConfig( imageLabelName, "greyscale", "true" );
        Framework.SetConfig( imageLabelName, "invert", "true" );
        Framework.SetConfig( imageLabelName, "sourceType", BufferedImageSourceFactory.TYPE_IMAGE_FILES );
        Framework.SetConfig( imageLabelName, "sourceFilesPrefix", "postproc" );
        Framework.SetConfig( imageLabelName, "sourceFilesPathTraining", trainingPath );
        Framework.SetConfig( imageLabelName, "sourceFilesPathTesting", testingPath );
        Framework.SetConfig( imageLabelName, "trainingEpochs", String.valueOf( trainingEpochs ) );
        Framework.SetConfig( imageLabelName, "testingEpochs", String.valueOf( testingEpochs ) );
        Framework.SetConfig( imageLabelName, "trainingEntities", String.valueOf( autoencoderName ) );
        if( !logDuringTraining ) {
            Framework.SetConfig( imageLabelName, "testingEntities", vectorSeriesName + "," + valueSeriesName );
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

        int ageMin = 0;
        int ageMax = ( trainingEpochs * imagesPerEpoch ) / 2; // reaches min sparsity at this age.

        float sparsityMax = 100f; // given as example, not explicitly confirmed
        float sparsityMin = 25f; // confirmed err = 1.35%
        float sparsityOutput = 3.f; // confirmed

        // https://raw.githubusercontent.com/stephenbalaban/convnet/master/K-Sparse%20Autoencoder.ipynb
        // "AE.train(X,X,X_test,None,\n",
        //        "         dataset_size=60000,\n",
        //        "         batch_size=128,\n",
        //        "         initial_weights=.01*nn.randn(AE.cfg.num_parameters),\n",
        //        "         momentum=.9,\n",
        //        "         learning_rate=.01,\n",
        int batchSize = 128; // value from Ali's code.
        // learning rate confirmed = 0.01f
        float learningRate = 0.01f;// / (float)batchSize; // Note must reduce learning rate to prevent overshoot and numerical instability
//        float momentum = 0.1f;//0.5f;//0.9f;
        float momentum = 0.9f; // confirmed
        float weightsStdDev = 0.01f; // confirmed. Sigma From paper. used at reset

        Framework.SetConfig( autoencoderName, "learningRate", String.valueOf( learningRate ) );
        Framework.SetConfig( autoencoderName, "momentum", String.valueOf( momentum ) );
        Framework.SetConfig( autoencoderName, "widthCells", String.valueOf( widthCells ) );
        Framework.SetConfig( autoencoderName, "heightCells", String.valueOf( heightCells ) );
        Framework.SetConfig( autoencoderName, "weightsStdDev", String.valueOf( weightsStdDev ) );
        Framework.SetConfig( autoencoderName, "sparsityOutput", String.valueOf( sparsityOutput ) );
        Framework.SetConfig( autoencoderName, "sparsityMin", String.valueOf( sparsityMin ) );
        Framework.SetConfig( autoencoderName, "sparsityMax", String.valueOf( sparsityMax ) );
        Framework.SetConfig( autoencoderName, "ageMin", String.valueOf( ageMin ) );
        Framework.SetConfig( autoencoderName, "ageMax", String.valueOf( ageMax ) );
        Framework.SetConfig( autoencoderName, "batchSize", String.valueOf( batchSize ) );

        // Log features of the algorithm during all phases
        Framework.SetConfig( vectorSeriesName, "encoding", ModelData.ENCODING_SPARSE_REAL );
        Framework.SetConfig( vectorSeriesName, "flushPeriod", String.valueOf( flushInterval ) ); // accumulate and flush, or accumulate only
        Framework.SetConfig( vectorSeriesName, "period", String.valueOf( "-1" ) );
        Framework.SetConfig( vectorSeriesName, "writeFilePath", flushWriteFilePath );
        Framework.SetConfig( vectorSeriesName, "writeFilePrefix", flushWriteFilePrefixFeatures );
        Framework.SetConfig( vectorSeriesName, "learn", String.valueOf( "true" ) );

        // Log labels of each image produced during all phases
        Framework.SetConfig( vectorSeriesName, "writeFileEncoding", ModelData.ENCODING_DENSE );
        Framework.SetConfig( valueSeriesName, "flushPeriod", String.valueOf( flushInterval ) ); // accumulate and flush, or accumulate only
        Framework.SetConfig( valueSeriesName, "period", String.valueOf( "-1" ) ); // infinite
        Framework.SetConfig( valueSeriesName, "learn", String.valueOf( "true" ) );
        Framework.SetConfig( valueSeriesName, "writeFilePath", flushWriteFilePath );
        Framework.SetConfig( valueSeriesName, "writeFilePrefix", flushWriteFilePrefixTruth );
        Framework.SetConfig( valueSeriesName, "entityName", imageLabelName );
        Framework.SetConfig( valueSeriesName, "configPath", "imageLabel" );
    }

}
