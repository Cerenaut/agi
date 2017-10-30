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
import io.agi.framework.Naming;
import io.agi.framework.Node;
import io.agi.framework.demo.mnist.ImageLabelEntity;
import io.agi.framework.entities.*;
import io.agi.framework.factories.CommonEntityFactory;
import io.agi.framework.persistence.DataJsonSerializer;
import io.agi.framework.persistence.PersistenceUtil;
import io.agi.framework.persistence.models.ModelData;
import io.agi.framework.references.DataRefUtil;

import java.util.ArrayList;
import java.util.Properties;

/**
 * Created by dave on 8/07/16.
 */
public class OnlineKSparseDemo {

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

//        String trainingPath = "./training";
//        String testingPath = "./testing";

        String trainingPath = "/home/dave/workspace/agi.io/data/mnist/1k_test";
        String  testingPath = "/home/dave/workspace/agi.io/data/mnist/1k_test";

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
//        int terminationAge = 10;//9000;
        int terminationAge = -1;
        int trainingEpochs = 5;//10;//80; // good for up to 80k
        int testingEpochs = 1;//80; // good for up to 80k
        boolean unitOutput = false;

        // Define some entities
        String experimentName           = PersistenceUtil.GetEntityName( "experiment" );
        String imageLabelName           = PersistenceUtil.GetEntityName( "image-class" );
        String autoencoderName          = PersistenceUtil.GetEntityName( "autoencoder" );
        String vectorSeriesName         = PersistenceUtil.GetEntityName( "feature-series" );
        String valueSeriesName          = PersistenceUtil.GetEntityName( "label-series" );

        PersistenceUtil.CreateEntity( experimentName, ExperimentEntity.ENTITY_TYPE, n.getName(), null ); // experiment is the root entity
        PersistenceUtil.CreateEntity( imageLabelName, ImageLabelEntity.ENTITY_TYPE, n.getName(), experimentName );
        PersistenceUtil.CreateEntity( autoencoderName, OnlineKSparseAutoencoderEntity.ENTITY_TYPE, n.getName(), imageLabelName );
        PersistenceUtil.CreateEntity( vectorSeriesName, VectorSeriesEntity.ENTITY_TYPE, n.getName(), autoencoderName ); // 2nd, class region updates after first to get its feedback
        PersistenceUtil.CreateEntity( valueSeriesName, ValueSeriesEntity.ENTITY_TYPE, n.getName(), vectorSeriesName ); // 2nd, class region updates after first to get its feedback

        // Connect the entities' data
        // a) Image to image region, and decode
        DataRefUtil.SetDataReference( autoencoderName, OnlineKSparseAutoencoderEntity.INPUT, imageLabelName, ImageLabelEntity.OUTPUT_IMAGE );

        ArrayList< AbstractPair< String, String > > featureDatas = new ArrayList< AbstractPair< String, String > >();
        if( unitOutput ) {
            featureDatas.add( new AbstractPair<>( autoencoderName, OnlineKSparseAutoencoderEntity.SPIKES_TOP_KA ) );
        }
        else {
            featureDatas.add( new AbstractPair<>( autoencoderName, OnlineKSparseAutoencoderEntity.TRANSFER_TOP_KA ) );
        }

        DataRefUtil.SetDataReferences( vectorSeriesName, VectorSeriesEntity.INPUT, featureDatas ); // get current state from the region to be used to predict

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
        PersistenceUtil.SetConfig( vectorSeriesName, "cache", String.valueOf( cacheAllData ) );
        PersistenceUtil.SetConfig( valueSeriesName, "cache", String.valueOf( cacheAllData ) );

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
        PersistenceUtil.SetConfig( imageLabelName, "sourceFilesPathTraining", trainingPath );
        PersistenceUtil.SetConfig( imageLabelName, "sourceFilesPathTesting", testingPath );
        PersistenceUtil.SetConfig( imageLabelName, "trainingEpochs", String.valueOf( trainingEpochs ) );
        PersistenceUtil.SetConfig( imageLabelName, "testingEpochs", String.valueOf( testingEpochs ) );
        PersistenceUtil.SetConfig( imageLabelName, "trainingEntities", String.valueOf( autoencoderName ) );
        if( !logDuringTraining ) {
            PersistenceUtil.SetConfig( imageLabelName, "testingEntities", vectorSeriesName + "," + valueSeriesName );
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
        int ageMax = 1000;

        float sparsity = 25f;
        float sparsityOutput = 3f;//1.5f;//3.f;

        // variables
        float learningRate = 0.01f;
        if( unitOutput ) {
//            learningRate = learningRate * 0.1f;
        }
        int batchSize = 1;
//        learningRate = learningRate;// / (float)batchSize; // Note must reduce learning rate to prevent overshoot and numerical instability

        float momentum = 0f;//0.9f;
        float weightsStdDev = 0.01f; // From paper. used at reset (only for biases in online case

        // TODO set params
        float ageScale = 15f;
        float ageTruncationFactor = 0.5f;
        float rateScale = 12f;
        float rateMax = 0.05f; // i.e. 1/20th
        float rateLearningRate = learningRate * 0.1f; // slower than the learning rate

        PersistenceUtil.SetConfig( autoencoderName, "unitOutput", String.valueOf( unitOutput ) );
        PersistenceUtil.SetConfig( autoencoderName, "learningRate", String.valueOf( learningRate ) );
        PersistenceUtil.SetConfig( autoencoderName, "momentum", String.valueOf( momentum ) );
        PersistenceUtil.SetConfig( autoencoderName, "widthCells", String.valueOf( widthCells ) );
        PersistenceUtil.SetConfig( autoencoderName, "heightCells", String.valueOf( heightCells ) );
        PersistenceUtil.SetConfig( autoencoderName, "weightsStdDev", String.valueOf( weightsStdDev ) );
        PersistenceUtil.SetConfig( autoencoderName, "sparsityOutput", String.valueOf( sparsityOutput ) );
        PersistenceUtil.SetConfig( autoencoderName, "sparsity", String.valueOf( sparsity ) );

        PersistenceUtil.SetConfig( autoencoderName, "ageMin", String.valueOf( ageMin ) );
        PersistenceUtil.SetConfig( autoencoderName, "ageMax", String.valueOf( ageMax ) );

        PersistenceUtil.SetConfig( autoencoderName, "ageTruncationFactor", String.valueOf( ageTruncationFactor ) );
        PersistenceUtil.SetConfig( autoencoderName, "ageScale", String.valueOf( ageScale ) );

        PersistenceUtil.SetConfig( autoencoderName, "rateScale", String.valueOf( rateScale ) );
        PersistenceUtil.SetConfig( autoencoderName, "rateMax", String.valueOf( rateMax ) );
        PersistenceUtil.SetConfig( autoencoderName, "rateLearningRate", String.valueOf( rateLearningRate ) );

        PersistenceUtil.SetConfig( autoencoderName, "batchSize", String.valueOf( batchSize ) );

        // Log features of the algorithm during all phases
        PersistenceUtil.SetConfig( vectorSeriesName, "encoding", DataJsonSerializer.ENCODING_SPARSE_REAL );
        PersistenceUtil.SetConfig( vectorSeriesName, "flushPeriod", String.valueOf( flushInterval ) ); // accumulate and flush, or accumulate only
        PersistenceUtil.SetConfig( vectorSeriesName, "period", String.valueOf( "-1" ) );
        PersistenceUtil.SetConfig( vectorSeriesName, "writeFilePath", flushWriteFilePath );
        PersistenceUtil.SetConfig( vectorSeriesName, "writeFilePrefix", flushWriteFilePrefixFeatures );
        PersistenceUtil.SetConfig( vectorSeriesName, "learn", String.valueOf( "true" ) );

        // Log labels of each image produced during all phases
        PersistenceUtil.SetConfig( vectorSeriesName, "writeFileEncoding", DataJsonSerializer.ENCODING_DENSE );
        PersistenceUtil.SetConfig( valueSeriesName, "flushPeriod", String.valueOf( flushInterval ) ); // accumulate and flush, or accumulate only
        PersistenceUtil.SetConfig( valueSeriesName, "period", String.valueOf( "-1" ) ); // infinite
        PersistenceUtil.SetConfig( valueSeriesName, "learn", String.valueOf( "true" ) );
        PersistenceUtil.SetConfig( valueSeriesName, "writeFilePath", flushWriteFilePath );
        PersistenceUtil.SetConfig( valueSeriesName, "writeFilePrefix", flushWriteFilePrefixTruth );
        PersistenceUtil.SetConfig( valueSeriesName, "entityName", imageLabelName );
        PersistenceUtil.SetConfig( valueSeriesName, "configPath", "imageLabel" );

    }

}
