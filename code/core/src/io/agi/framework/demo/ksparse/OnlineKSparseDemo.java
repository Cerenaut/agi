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

package io.agi.framework.demo.ksparse;

import io.agi.core.orm.AbstractPair;
import io.agi.core.util.PropertiesUtil;
import io.agi.core.util.images.BufferedImageSource.BufferedImageSourceFactory;
import io.agi.framework.Framework;
import io.agi.framework.Main;
import io.agi.framework.Node;
import io.agi.framework.demo.mnist.ImageLabelEntity;
import io.agi.framework.entities.*;
import io.agi.framework.factories.CommonEntityFactory;

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

//        String trainingPath = "./training";
//        String testingPath = "./testing";

        String trainingPath = "/home/dave/workspace/agi.io/data/mnist/1k_test";
        String  testingPath = "/home/dave/workspace/agi.io/data/mnist/1k_test";

//        String trainingPath = "/home/dave/workspace/agi.io/data/mnist/cycle10";
//        String testingPath = "/home/dave/workspace/agi.io/data/mnist/cycle10";

        boolean cacheAllData = true;
        boolean terminateByAge = false;
//        int terminationAge = 10;//9000;
        int terminationAge = 50000;//25000;
        int trainingEpochs = 10;//80; // good for up to 80k
        int testingEpochs = 1;//80; // good for up to 80k
        boolean unitOutput = true;

        // Define some entities
        String experimentName           = Framework.GetEntityName( "experiment" );
        String imageLabelName           = Framework.GetEntityName( "image-class" );
        String autoencoderName          = Framework.GetEntityName( "autoencoder" );
        String vectorSeriesName         = Framework.GetEntityName( "feature-series" );
        String valueSeriesName         = Framework.GetEntityName( "label-series" );

        Framework.CreateEntity( experimentName, ExperimentEntity.ENTITY_TYPE, n.getName(), null ); // experiment is the root entity
        Framework.CreateEntity( imageLabelName, ImageLabelEntity.ENTITY_TYPE, n.getName(), experimentName );
        Framework.CreateEntity( autoencoderName, OnlineKSparseAutoencoderEntity.ENTITY_TYPE, n.getName(), imageLabelName );
        Framework.CreateEntity( vectorSeriesName, VectorSeriesEntity.ENTITY_TYPE, n.getName(), autoencoderName ); // 2nd, class region updates after first to get its feedback
        Framework.CreateEntity( valueSeriesName, ValueSeriesEntity.ENTITY_TYPE, n.getName(), vectorSeriesName ); // 2nd, class region updates after first to get its feedback

        // Connect the entities' data
        // a) Image to image region, and decode
        Framework.SetDataReference( autoencoderName, OnlineKSparseAutoencoderEntity.INPUT, imageLabelName, ImageLabelEntity.OUTPUT_IMAGE );

        ArrayList< AbstractPair< String, String > > featureDatas = new ArrayList< AbstractPair< String, String > >();
        if( unitOutput ) {
            featureDatas.add( new AbstractPair< String, String >( autoencoderName, OnlineKSparseAutoencoderEntity.SPIKES_TOP_KA ) );
        }
        else {
            featureDatas.add( new AbstractPair< String, String >( autoencoderName, OnlineKSparseAutoencoderEntity.TRANSFER_TOP_KA ) );
        }

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

        // Mnist config
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
        Framework.SetConfig( imageLabelName, "testingEntities", vectorSeriesName + "," + valueSeriesName );

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
        float sparsityOutput = 3.f;

        // variables
        float learningRate = 0.01f;
        if( unitOutput ) {
//            learningRate = learningRate * 0.1f;
        }
        float momentum = 0f;//0.9f;
        float weightsStdDev = 0.01f; // From paper. used at reset (only for biases in online case

        // TODO set params
        float ageScale = 15f;
        float ageTruncationFactor = 0.5f;
        float rateScale = 12f;
        float rateMax = 0.05f; // i.e. 1/20th
        float rateLearningRate = learningRate * 0.1f; // slower than the learning rate

        Framework.SetConfig( autoencoderName, "unitOutput", String.valueOf( unitOutput ) );
        Framework.SetConfig( autoencoderName, "learningRate", String.valueOf( learningRate ) );
        Framework.SetConfig( autoencoderName, "momentum", String.valueOf( momentum ) );
        Framework.SetConfig( autoencoderName, "widthCells", String.valueOf( widthCells ) );
        Framework.SetConfig( autoencoderName, "heightCells", String.valueOf( heightCells ) );
        Framework.SetConfig( autoencoderName, "weightsStdDev", String.valueOf( weightsStdDev ) );
        Framework.SetConfig( autoencoderName, "sparsityOutput", String.valueOf( sparsityOutput ) );
        Framework.SetConfig( autoencoderName, "sparsity", String.valueOf( sparsity ) );

        Framework.SetConfig( autoencoderName, "ageMin", String.valueOf( ageMin ) );
        Framework.SetConfig( autoencoderName, "ageMax", String.valueOf( ageMax ) );

        Framework.SetConfig( autoencoderName, "ageTruncationFactor", String.valueOf( ageTruncationFactor ) );
        Framework.SetConfig( autoencoderName, "ageScale", String.valueOf( ageScale ) );

        Framework.SetConfig( autoencoderName, "rateScale", String.valueOf( rateScale ) );
        Framework.SetConfig( autoencoderName, "rateMax", String.valueOf( rateMax ) );
        Framework.SetConfig( autoencoderName, "rateLearningRate", String.valueOf( rateLearningRate ) );

        // Log features of the algorithm during all phases
        Framework.SetConfig( vectorSeriesName, "period", String.valueOf( "-1" ) ); // infinite
        Framework.SetConfig( vectorSeriesName, "learn", String.valueOf( "true" ) ); // infinite

        // Log labels of each image produced during all phases
        Framework.SetConfig( valueSeriesName, "period", "-1" );
        Framework.SetConfig( valueSeriesName, "learn", String.valueOf( "true" ) ); // infinite
        Framework.SetConfig( valueSeriesName, "entityName", imageLabelName ); // log forever
        Framework.SetConfig( valueSeriesName, "configPath", "imageLabel" ); // log forever

    }

}
