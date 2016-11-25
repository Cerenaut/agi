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
import io.agi.core.util.PropertiesUtil;
import io.agi.core.util.images.BufferedImageSource.BufferedImageSourceFactory;
import io.agi.framework.Framework;
import io.agi.framework.Main;
import io.agi.framework.Node;
import io.agi.framework.entities.*;
import io.agi.framework.factories.CommonEntityFactory;

import java.util.ArrayList;
import java.util.Properties;

/**
 * Created by dave on 8/07/16.
 */
public class SpikingRegionLayerDemo {

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

        String trainingPath = "/home/dave/workspace/agi.io/data/mnist/cycle10";
        String testingPath = "/home/dave/workspace/agi.io/data/mnist/cycle3";
//        String trainingPath = "/home/dave/workspace/agi.io/data/mnist/cycle10";
//        String testingPath = "/home/dave/workspace/agi.io/data/mnist/cycle10";
//        String trainingPath = "/home/dave/workspace/agi.io/data/mnist/cycle3";
//        String testingPath = "/home/dave/workspace/agi.io/data/mnist/cycle3";
//        String trainingPath = "/home/dave/workspace/agi.io/data/mnist/cycle_twin";
//        String testingPath = "/home/dave/workspace/agi.io/data/mnist/cycle_twin";
//        String trainingPath = "/home/dave/workspace/agi.io/data/mnist/cycle_deep";
//        String testingPath = "/home/dave/workspace/agi.io/data/mnist/cycle_deep";
//        String trainingPath = "/home/dave/workspace/agi.io/data/mnist/all_train";
//        String testingPath = "/home/dave/workspace/agi.io/data/mnist/all_t10k";
//        String trainingPath = "/home/dave/workspace/agi.io/data/mnist/10k_train";
//        String testingPath = "/home/dave/workspace/agi.io/data/mnist/5k_test";
//        String trainingPath = "./training";
//        String testingPath = "./testing";
//        int terminationAge = 10;//9000;
        int terminationAge = 600000;//25000;
        int trainingBatches = 100000;//80; // good for up to 80k
        boolean terminateByAge = true;
        boolean classFeaturesOnline = true;
        int layers = 1;

        // Define some entities
        String experimentName           = Framework.GetEntityName( "experiment" );
        String imageClassName           = Framework.GetEntityName( "image-class" );
        String constantName             = Framework.GetEntityName( "constant" );
        String spikeEncoderName         = Framework.GetEntityName( "spike-encoder" );
        String regionLayer1Name         = Framework.GetEntityName( "region-layer-1" );
        String classFeaturesName        = Framework.GetEntityName( "class-features" );
        String valueSeriesPredictedName = Framework.GetEntityName( "value-series-predicted" );
        String valueSeriesErrorName     = Framework.GetEntityName( "value-series-error" );
        String valueSeriesTruthName     = Framework.GetEntityName( "value-series-truth" );

        Framework.CreateEntity( experimentName, ExperimentEntity.ENTITY_TYPE, n.getName(), null ); // experiment is the root entity
        Framework.CreateEntity( imageClassName, ImageClassEntity.ENTITY_TYPE, n.getName(), experimentName );
        Framework.CreateEntity( spikeEncoderName, SpikeEncoderEntity.ENTITY_TYPE, n.getName(), imageClassName );
        Framework.CreateEntity( constantName, ConstantMatrixEntity.ENTITY_TYPE, n.getName(), spikeEncoderName ); // ok all input to the regions is ready

        Framework.CreateEntity( regionLayer1Name, SpikingRegionLayerEntity.ENTITY_TYPE, n.getName(), constantName );
        String learningEntitiesAlgorithm = regionLayer1Name; // to know what to turn on and off
        String topLayerName = regionLayer1Name; // to build the rest of the entity graph

        Framework.CreateEntity( classFeaturesName, FeatureLabelsEntity.ENTITY_TYPE, n.getName(), topLayerName ); // 2nd, class region updates after first to get its feedback
        Framework.CreateEntity( valueSeriesPredictedName, ValueSeriesEntity.ENTITY_TYPE, n.getName(), classFeaturesName ); // 2nd, class region updates after first to get its feedback
        Framework.CreateEntity( valueSeriesErrorName, ValueSeriesEntity.ENTITY_TYPE, n.getName(), classFeaturesName ); // 2nd, class region updates after first to get its feedback
        Framework.CreateEntity( valueSeriesTruthName, ValueSeriesEntity.ENTITY_TYPE, n.getName(), classFeaturesName ); // 2nd, class region updates after first to get its feedback

        // Connect the entities' data
        // a) Image to image region, and decode
        Framework.SetDataReference( spikeEncoderName, SpikeEncoderEntity.DATA_INPUT, imageClassName, ImageClassEntity.OUTPUT_IMAGE );

        Framework.SetDataReference( regionLayer1Name, SpikingRegionLayerEntity.INPUT_1, spikeEncoderName, SpikeEncoderEntity.DATA_OUTPUT );
        Framework.SetDataReference( regionLayer1Name, SpikingRegionLayerEntity.INPUT_2, constantName, ConstantMatrixEntity.OUTPUT );

        ArrayList< AbstractPair< String, String > > featureDatas = new ArrayList< AbstractPair< String, String > >();
        if( layers > 0 ) featureDatas.add( new AbstractPair< String, String >( regionLayer1Name, SpikingRegionLayerEntity.OUTPUT ) );
//        if( layers > 1 ) featureDatas.add( new AbstractPair< String, String >( region2FfName, SpikingRegionLayerEntity.CONTEXT_FREE_ACTIVITY_NEW ) );
//        if( layers > 2 ) featureDatas.add( new AbstractPair< String, String >( region3FfName, SpikingRegionLayerEntity.CONTEXT_FREE_ACTIVITY_NEW ) );
        Framework.SetDataReferences( classFeaturesName, FeatureLabelsEntity.FEATURES, featureDatas ); // get current state from the region to be used to predict

        // Experiment config
        if( !terminateByAge ) {
            Framework.SetConfig( experimentName, "terminationEntityName", imageClassName );
            Framework.SetConfig( experimentName, "terminationConfigPath", "terminate" );
            Framework.SetConfig( experimentName, "terminationAge", "-1" ); // wait for mnist to decide
        }
        else {
            Framework.SetConfig( experimentName, "terminationAge", String.valueOf( terminationAge ) ); // fixed steps
        }

        // Mnist config
        Framework.SetConfig( imageClassName, "receptiveField.receptiveFieldX", "0" );
        Framework.SetConfig( imageClassName, "receptiveField.receptiveFieldY", "0" );
        Framework.SetConfig( imageClassName, "receptiveField.receptiveFieldW", "28" );
        Framework.SetConfig( imageClassName, "receptiveField.receptiveFieldH", "28" );
        Framework.SetConfig( imageClassName, "resolution.resolutionX", "28" );
        Framework.SetConfig( imageClassName, "resolution.resolutionY", "28" );
        Framework.SetConfig( imageClassName, "greyscale", "true" );
        Framework.SetConfig( imageClassName, "invert", "true" );
        Framework.SetConfig( imageClassName, "sourceType", BufferedImageSourceFactory.TYPE_IMAGE_FILES );
        Framework.SetConfig( imageClassName, "sourceFilesPrefix", "postproc" );
        Framework.SetConfig( imageClassName, "sourceFilesPathTraining", trainingPath );
        Framework.SetConfig( imageClassName, "sourceFilesPathTesting", testingPath );
        Framework.SetConfig( imageClassName, "trainingBatches", String.valueOf( trainingBatches ) );
        Framework.SetConfig( imageClassName, "imageRepeats", "50" );

        String learningEntitiesAnalytics = classFeaturesName;
        Framework.SetConfig( imageClassName, "learningEntitiesAlgorithm", String.valueOf( learningEntitiesAlgorithm ) );
        Framework.SetConfig( imageClassName, "learningEntitiesAnalytics", String.valueOf( learningEntitiesAnalytics ) );

        // constant config
        // spike encoder config
        Framework.SetConfig( spikeEncoderName, "maxSpikeRate", "1" );

        // region-layer config
        Framework.SetConfig( regionLayer1Name, "excitatoryCells", "400" ); // EINet: 400
        Framework.SetConfig( regionLayer1Name, "inhibitoryCells", "100" ); // EINet: 49

        Framework.SetConfig( regionLayer1Name, "spikeThresholdBatchSize", "20" );
        Framework.SetConfig( regionLayer1Name, "spikeThresholdBatchIndex", "0" );

//        Framework.SetConfig( regionLayer1Name, "timeConstantExcitatory", "1" ); // EINet: 1
//        Framework.SetConfig( regionLayer1Name, "timeConstantInhibitory", "0.5" ); // EINet: 0.5
        Framework.SetConfig( regionLayer1Name, "timeConstantExcitatory", "0.7" ); // EINet: 1
        Framework.SetConfig( regionLayer1Name, "timeConstantInhibitory", "0.9" ); // EINet: 0.5

        Framework.SetConfig( regionLayer1Name, "targetSpikeRateExcitatory", "0.002" ); // EINet: = 0.02
        Framework.SetConfig( regionLayer1Name, "targetSpikeRateInhibitory", "0.004" ); // EINet: = 0.04

        Framework.SetConfig( regionLayer1Name, "resetSpikeThreshold", "1" ); // unknown ??? [EINet confirmed: 1]

        Framework.SetConfig( regionLayer1Name, "spikeRateLearningRate", "0.005" ); // unknown; learn slowly over many inputs
        Framework.SetConfig( regionLayer1Name, "spikeTraceLearningRate", "0.5" ); // unknown; fast trace decay
        Framework.SetConfig( regionLayer1Name, "spikeThresholdLearningRate", "0.001" ); // unknown; learn slowly over many inputs

//        Framework.SetConfig( regionLayer1Name, "synapseLearningRateExternal1ToExcitatory",  "0.008" ); // EINet: 0.008 SLOW learning
//        Framework.SetConfig( regionLayer1Name, "synapseLearningRateExternal2ToExcitatory",  "0" ); // not connected
//        Framework.SetConfig( regionLayer1Name, "synapseLearningRateExcitatoryToExcitatory", "0" ); // not connected
//        Framework.SetConfig( regionLayer1Name, "synapseLearningRateInhibitoryToExcitatory", "0.028" ); // EINet: 0.028 FAST
//        Framework.SetConfig( regionLayer1Name, "synapseLearningRateExternal1ToInhibitory",  "0" ); // not connected
//        Framework.SetConfig( regionLayer1Name, "synapseLearningRateExternal2ToInhibitory",  "0.008" ); // OK (slow) [set by Dave]
//        Framework.SetConfig( regionLayer1Name, "synapseLearningRateExcitatoryToInhibitory", "0.028" ); // EINet: 0.028  FAST Learning
//        Framework.SetConfig( regionLayer1Name, "synapseLearningRateInhibitoryToInhibitory", "0.06" );  // EINet: 0.06 *VERY* FAST

        Framework.SetConfig( regionLayer1Name, "synapseLearningRateExternal1ToExcitatory",  "0.08" ); // EINet: 0.008 SLOW learning
        Framework.SetConfig( regionLayer1Name, "synapseLearningRateExternal2ToExcitatory",  "0" ); // not connected
        Framework.SetConfig( regionLayer1Name, "synapseLearningRateExcitatoryToExcitatory", "0" ); // not connected
        Framework.SetConfig( regionLayer1Name, "synapseLearningRateInhibitoryToExcitatory", "0.1" ); // EINet: 0.028 FAST
        Framework.SetConfig( regionLayer1Name, "synapseLearningRateExternal1ToInhibitory",  "0" ); // not connected
        Framework.SetConfig( regionLayer1Name, "synapseLearningRateExternal2ToInhibitory",  "0.08" ); // OK (slow) [set by Dave]
        Framework.SetConfig( regionLayer1Name, "synapseLearningRateExcitatoryToInhibitory", "0.1" ); // EINet: 0.028  FAST Learning
        Framework.SetConfig( regionLayer1Name, "synapseLearningRateInhibitoryToInhibitory", "0.2" );  // EINet: 0.06 *VERY* FAST

        Framework.SetConfig( regionLayer1Name, "inputWeightExcitatory", "0.1" ); // EINet: 1
        Framework.SetConfig( regionLayer1Name, "inputWeightInhibitory", "-1" ); // EINet: -1
        Framework.SetConfig( regionLayer1Name, "inputWeightExternal1", "1" ); // EINet: 0.2 I think
        Framework.SetConfig( regionLayer1Name, "inputWeightExternal2", "1" ); // OK depending on pop size

        // class x features config
        Framework.SetConfig( classFeaturesName, "classEntityName", imageClassName );
        Framework.SetConfig( classFeaturesName, "classConfigPath", "imageClass" );
        Framework.SetConfig( classFeaturesName, "classes", "10" );
        Framework.SetConfig( classFeaturesName, "onlineLearning", String.valueOf( classFeaturesOnline ) );
//        Framework.SetConfig( classFeaturesName, "onlineLearningRate", "0.001" );
        Framework.SetConfig( classFeaturesName, "onlineLearningRate", "0.01" );

        // data series logging
        Framework.SetConfig( valueSeriesPredictedName, "period", "-1" ); // log forever
        Framework.SetConfig( valueSeriesErrorName, "period", "-1" );
        Framework.SetConfig( valueSeriesTruthName, "period", "-1" );

        Framework.SetConfig( valueSeriesPredictedName, "entityName", classFeaturesName ); // log forever
        Framework.SetConfig( valueSeriesErrorName, "entityName", classFeaturesName );
        Framework.SetConfig( valueSeriesTruthName, "entityName", classFeaturesName );

        Framework.SetConfig( valueSeriesPredictedName, "configPath", "classPredicted" ); // log forever
        Framework.SetConfig( valueSeriesErrorName, "configPath", "classError" );
        Framework.SetConfig( valueSeriesTruthName, "configPath", "classTruth" );
    }

}
