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

import com.sun.xml.internal.bind.v2.TODO;
import io.agi.core.orm.AbstractPair;
import io.agi.core.sdr.IntegerEncoder;
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
public class PyramidRegionLayerLabelsDemo {

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
        String testingPath = "/home/dave/workspace/agi.io/data/mnist/cycle10";

        int terminationAge = 5000;//000;//25000;
        int trainingBatches = 10000;//80; // good for up to 80k
        boolean terminateByAge = true;//false;
        boolean encodeZero = false;
        boolean featureLabelsOnline = true;
        int labelBits = 8;
        int imageRepeats = 1;//50;

        // Define some entities
        String experimentName           = Framework.GetEntityName( "experiment" );
        String imageClassName           = Framework.GetEntityName( "image-class" );
        String constantName             = Framework.GetEntityName( "constant" );
        String regionLayer1Name         = Framework.GetEntityName( "region-layer-1" );
        String regionLayer2Name         = Framework.GetEntityName( "region-layer-2" );
        String regionLayer3Name         = Framework.GetEntityName( "region-layer-3" );
        String imageEncoderName         = Framework.GetEntityName( "image-encoder" );
        String labelEncoderName         = Framework.GetEntityName( "label-encoder" );
//        String labelDecoderName         = Framework.GetEntityName( "label-decoder" );
//        String classResultName          = Framework.GetEntityName( "class-result" );
        String valueSeriesPredictedName = Framework.GetEntityName( "value-series-predicted" );
        String valueSeriesErrorName     = Framework.GetEntityName( "value-series-error" );
        String valueSeriesTruthName     = Framework.GetEntityName( "value-series-truth" );
        String configProductName        = Framework.GetEntityName( "config-product" ); // allows you to turn off provision of the training labels
        String featureLabelsName        = Framework.GetEntityName( "class-features" );

        // These are the properties of a pyramid region-layer that can be logged:
        // float sumClassifierError = 0;    **** this tells us whether it's learning effectively, or it is unable to find structure within the input data.
        // float sumClassifierResponse = 0; ---- ignore this one for now
        // float sumOutputSpikes = 0;       **** this would tell us whether it's predictable or random
        // float sumPredictionErrorFP = 0;  ---- ignore this one for now because we assume the learning keeps it honest (not too many FP)
        // float sumPredictionErrorFN = 0;  ---- ignore this one for now because it's a fn of output spikes.
        // float sumIntegration = 0;        ---- this would tell us how the integration dynamics works but since it's linear + and exp - maybe we can imagine.
        //                                   Actually, the above is less useful because it's a sum over all the cells which will be out of sync.0
        String valueSeriesClassifierError1 = Framework.GetEntityName( "value-series-classifier-error-1" );
        String valueSeriesClassifierError2 = Framework.GetEntityName( "value-series-classifier-error-2" );
        String valueSeriesClassifierError3 = Framework.GetEntityName( "value-series-classifier-error-3" );

        String valueSeriesOutputSpikes1 = Framework.GetEntityName( "value-series-output-spikes-1" );
        String valueSeriesOutputSpikes2 = Framework.GetEntityName( "value-series-output-spikes-2" );
        String valueSeriesOutputSpikes3 = Framework.GetEntityName( "value-series-output-spikes-3" );

        // build a history of spikes.
        String spikes1Name        = Framework.GetEntityName( "spikes-1" );
        String spikes2Name        = Framework.GetEntityName( "spikes-2" );
        String spikes3Name        = Framework.GetEntityName( "spikes-3" );

        Framework.CreateEntity( experimentName, ExperimentEntity.ENTITY_TYPE, n.getName(), null ); // experiment is the root entity
        Framework.CreateEntity( imageClassName, ImageClassEntity.ENTITY_TYPE, n.getName(), experimentName );
        Framework.CreateEntity( imageEncoderName, EncoderEntity.ENTITY_TYPE, n.getName(), imageClassName );
        Framework.CreateEntity( constantName, ConstantMatrixEntity.ENTITY_TYPE, n.getName(), imageEncoderName ); // ok all input to the regions is ready
        Framework.CreateEntity( labelEncoderName, EncoderEntity.ENTITY_TYPE, n.getName(), constantName );
        Framework.CreateEntity( configProductName, ConfigProductEntity.ENTITY_TYPE, n.getName(), labelEncoderName ); // ok all input to the regions is ready

        // Region-layers
        Framework.CreateEntity( regionLayer1Name, PyramidRegionLayerEntity.ENTITY_TYPE, n.getName(), configProductName );
        String lastRegionLayerName = regionLayer1Name;
        String learningEntitiesAlgorithm = regionLayer1Name;

        Framework.CreateEntity( regionLayer2Name, PyramidRegionLayerEntity.ENTITY_TYPE, n.getName(), regionLayer1Name );
        lastRegionLayerName = regionLayer2Name;
        learningEntitiesAlgorithm = learningEntitiesAlgorithm + "," + regionLayer2Name;

        Framework.CreateEntity( regionLayer3Name, PyramidRegionLayerEntity.ENTITY_TYPE, n.getName(), regionLayer2Name );
        lastRegionLayerName = regionLayer3Name;
        learningEntitiesAlgorithm = learningEntitiesAlgorithm + "," + regionLayer3Name;
        // Region-layers

        Framework.CreateEntity( featureLabelsName, FeatureLabelsEntity.ENTITY_TYPE, n.getName(), lastRegionLayerName );

        Framework.CreateEntity( valueSeriesPredictedName, ValueSeriesEntity.ENTITY_TYPE, n.getName(), featureLabelsName );
        Framework.CreateEntity( valueSeriesErrorName, ValueSeriesEntity.ENTITY_TYPE, n.getName(), featureLabelsName );
        Framework.CreateEntity( valueSeriesTruthName, ValueSeriesEntity.ENTITY_TYPE, n.getName(), featureLabelsName );

        Framework.CreateEntity( valueSeriesClassifierError1, ValueSeriesEntity.ENTITY_TYPE, n.getName(), lastRegionLayerName );
        Framework.CreateEntity( valueSeriesClassifierError2, ValueSeriesEntity.ENTITY_TYPE, n.getName(), lastRegionLayerName );
        Framework.CreateEntity( valueSeriesClassifierError3, ValueSeriesEntity.ENTITY_TYPE, n.getName(), lastRegionLayerName );

        Framework.CreateEntity( valueSeriesOutputSpikes1, ValueSeriesEntity.ENTITY_TYPE, n.getName(), lastRegionLayerName );
        Framework.CreateEntity( valueSeriesOutputSpikes2, ValueSeriesEntity.ENTITY_TYPE, n.getName(), lastRegionLayerName );
        Framework.CreateEntity( valueSeriesOutputSpikes3, ValueSeriesEntity.ENTITY_TYPE, n.getName(), lastRegionLayerName );

        Framework.CreateEntity( spikes1Name, VectorSeriesEntity.ENTITY_TYPE, n.getName(), lastRegionLayerName ); // ok all input to the regions is ready
        Framework.CreateEntity( spikes2Name, VectorSeriesEntity.ENTITY_TYPE, n.getName(), lastRegionLayerName ); // ok all input to the regions is ready
        Framework.CreateEntity( spikes3Name, VectorSeriesEntity.ENTITY_TYPE, n.getName(), lastRegionLayerName ); // ok all input to the regions is ready

        Framework.SetDataReference( spikes1Name, VectorSeriesEntity.INPUT, regionLayer1Name, PyramidRegionLayerEntity.OUTPUT_SPIKES_NEW );
        Framework.SetDataReference( spikes2Name, VectorSeriesEntity.INPUT, regionLayer2Name, PyramidRegionLayerEntity.OUTPUT_SPIKES_NEW );
        Framework.SetDataReference( spikes3Name, VectorSeriesEntity.INPUT, regionLayer3Name, PyramidRegionLayerEntity.OUTPUT_SPIKES_NEW );

//        Framework.CreateEntity( labelDecoderName, DecoderEntity.ENTITY_TYPE, n.getName(), topLayerName ); // produce the predicted classification for inspection by mnist next time
//        Framework.CreateEntity( classResultName, ClassificationResultEntity.ENTITY_TYPE, n.getName(), labelDecoderName ); // produce the predicted classification for inspection by mnist next time
//
        // Connect the entities' data
        // a) Image to image region, and decode
        Framework.SetDataReference( imageEncoderName, EncoderEntity.DATA_INPUT, imageClassName, ImageClassEntity.OUTPUT_IMAGE );
        Framework.SetDataReference( labelEncoderName, EncoderEntity.DATA_INPUT, imageClassName, ImageClassEntity.OUTPUT_LABEL );
//        Framework.SetDataReference( labelDecoderName, DecoderEntity.DATA_INPUT_DECODED, imageClassName, ImageClassEntity.OUTPUT_LABEL );

//                Framework.SetDataReference( region2FfName, AutoRegionLayerEntity.INPUT_2, labelEncoderName, EncoderEntity.DATA_OUTPUT_ENCODED );
        Framework.SetDataReference( configProductName, ConfigProductEntity.INPUT, labelEncoderName, EncoderEntity.DATA_OUTPUT_ENCODED );

        // RL1
        Framework.SetDataReference( regionLayer1Name, PyramidRegionLayerEntity.INPUT_C1, imageEncoderName, EncoderEntity.DATA_OUTPUT_ENCODED );
        Framework.SetDataReference( regionLayer1Name, PyramidRegionLayerEntity.INPUT_C2, constantName, ConstantMatrixEntity.OUTPUT );

//        Framework.SetDataReference( regionLayer1Name, PyramidRegionLayerEntity.INPUT_P1, regionLayer3Name, PyramidRegionLayerEntity.OUTPUT_SPIKES_NEW );
        Framework.SetDataReference( regionLayer1Name, PyramidRegionLayerEntity.INPUT_P1, constantName, ConstantMatrixEntity.OUTPUT );
        Framework.SetDataReference( regionLayer1Name, PyramidRegionLayerEntity.INPUT_P2, constantName, ConstantMatrixEntity.OUTPUT );

        // RL2
        Framework.SetDataReference( regionLayer2Name, PyramidRegionLayerEntity.INPUT_C1, configProductName, ConfigProductEntity.OUTPUT );
        Framework.SetDataReference( regionLayer2Name, PyramidRegionLayerEntity.INPUT_C2, constantName, ConstantMatrixEntity.OUTPUT );

//        Framework.SetDataReference( regionLayer2Name, PyramidRegionLayerEntity.INPUT_P1, regionLayer3Name, PyramidRegionLayerEntity.OUTPUT_SPIKES_NEW );
        Framework.SetDataReference( regionLayer2Name, PyramidRegionLayerEntity.INPUT_P1, constantName, ConstantMatrixEntity.OUTPUT );
        Framework.SetDataReference( regionLayer2Name, PyramidRegionLayerEntity.INPUT_P2, constantName, ConstantMatrixEntity.OUTPUT );

        // RL3
        Framework.SetDataReference( regionLayer3Name, PyramidRegionLayerEntity.INPUT_C1, regionLayer1Name, PyramidRegionLayerEntity.OUTPUT_SPIKES_NEW );
        Framework.SetDataReference( regionLayer3Name, PyramidRegionLayerEntity.INPUT_C2, regionLayer2Name, PyramidRegionLayerEntity.OUTPUT_SPIKES_NEW );

        Framework.SetDataReference( regionLayer3Name, PyramidRegionLayerEntity.INPUT_P1, constantName, ConstantMatrixEntity.OUTPUT );
        Framework.SetDataReference( regionLayer3Name, PyramidRegionLayerEntity.INPUT_P2, constantName, ConstantMatrixEntity.OUTPUT );

        // train the featureLabels entity to associate bits in the region-layers with the
        ArrayList< AbstractPair< String, String > > featureDatas = new ArrayList< AbstractPair< String, String > >();
        //featureDatas.add( new AbstractPair< String, String >( regionLayer1Name, PyramidRegionLayerEntity.CLASSIFIER_SPIKES_NEW ) );
        //featureDatas.add( new AbstractPair< String, String >( regionLayer2Name, PyramidRegionLayerEntity.CLASSIFIER_SPIKES_NEW ) );
        featureDatas.add( new AbstractPair< String, String >( regionLayer3Name, PyramidRegionLayerEntity.CLASSIFIER_SPIKES_NEW ) );
        Framework.SetDataReferences( featureLabelsName, FeatureLabelsEntity.FEATURES, featureDatas ); // get current state from the region to be used to predict

        // invert the hidden layer state to produce the predicted label
//        Framework.SetDataReference( labelDecoderName, DecoderEntity.DATA_INPUT_ENCODED, topLayerName, AutoRegionLayerEntity.OUTPUT_INPUT_2 ); // the prediction of the next state
//
//        Framework.SetDataReference( classResultName, ClassificationResultEntity.INPUT_LABEL, imageClassName, ImageClassEntity.OUTPUT_LABEL ); // get current state from the region to be used to predict
//        Framework.SetDataReference( classResultName, ClassificationResultEntity.INPUT_CLASS, labelDecoderName, DecoderEntity.DATA_OUTPUT_DECODED ); // get current state from the region to be used to predict

        // Label filter - disable labels during test time
        Framework.SetConfig( configProductName, "entityName", regionLayer2Name );
        Framework.SetConfig( configProductName, "configPath", "learn" ); // so the value is '1' when the region is learning, and 0 otherwise.

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
        Framework.SetConfig( imageClassName, "imageRepeats", String.valueOf( imageRepeats ) );


        String learningEntitiesAnalytics = featureLabelsName;
        Framework.SetConfig( imageClassName, "learningEntitiesAlgorithm", String.valueOf( learningEntitiesAlgorithm ) );
        Framework.SetConfig( imageClassName, "learningEntitiesAnalytics", String.valueOf( learningEntitiesAnalytics ) );

        // constant config
        if( encodeZero ) {
            // image encoder config
            Framework.SetConfig( imageEncoderName, "density", "1" );
            Framework.SetConfig( imageEncoderName, "bits", "2" );
            Framework.SetConfig( imageEncoderName, "encodeZero", "true" );
        }
        else {
            // image encoder config
            Framework.SetConfig( imageEncoderName, "density", "1" );
            Framework.SetConfig( imageEncoderName, "bits", "1" );
            Framework.SetConfig( imageEncoderName, "encodeZero", "false" );
        }

        // Label encoder
        Framework.SetConfig( labelEncoderName, "encoderType", IntegerEncoder.class.getSimpleName() );
        Framework.SetConfig( labelEncoderName, "minValue", "0" );
        Framework.SetConfig( labelEncoderName, "maxValue", "9" );
        Framework.SetConfig( labelEncoderName, "rows", String.valueOf( labelBits ) );

        // Label DEcoder
//        Framework.SetConfig( labelDecoderName, "encoderType", IntegerEncoder.class.getSimpleName() );
//        Framework.SetConfig( labelDecoderName, "minValue", "0" );
//        Framework.SetConfig( labelDecoderName, "maxValue", "9" );
//        Framework.SetConfig( labelDecoderName, "rows", String.valueOf( labelBits ) );

        // feature-labels config
        Framework.SetConfig( featureLabelsName, "classEntityName", imageClassName );
        Framework.SetConfig( featureLabelsName, "classConfigPath", "imageClass" );
        Framework.SetConfig( featureLabelsName, "classes", "10" );
        Framework.SetConfig( featureLabelsName, "onlineLearning", String.valueOf( featureLabelsOnline ) );
        Framework.SetConfig( featureLabelsName, "onlineLearningRate", "0.01" );

        // data series logging
        Framework.SetConfig( valueSeriesPredictedName, "period", "-1" ); // log forever
        Framework.SetConfig( valueSeriesErrorName, "period", "-1" );
        Framework.SetConfig( valueSeriesTruthName, "period", "-1" );

        Framework.SetConfig( valueSeriesClassifierError1, "period", "-1" );
        Framework.SetConfig( valueSeriesClassifierError2, "period", "-1" );
        Framework.SetConfig( valueSeriesClassifierError3, "period", "-1" );

        Framework.SetConfig( valueSeriesOutputSpikes1, "period", "-1" );
        Framework.SetConfig( valueSeriesOutputSpikes2, "period", "-1" );
        Framework.SetConfig( valueSeriesOutputSpikes3, "period", "-1" );

        Framework.SetConfig( valueSeriesPredictedName, "entityName", featureLabelsName ); // log forever
        Framework.SetConfig( valueSeriesErrorName, "entityName", featureLabelsName );
        Framework.SetConfig( valueSeriesTruthName, "entityName", featureLabelsName );

        Framework.SetConfig( valueSeriesClassifierError1, "entityName", regionLayer1Name );
        Framework.SetConfig( valueSeriesClassifierError2, "entityName", regionLayer2Name );
        Framework.SetConfig( valueSeriesClassifierError3, "entityName", regionLayer3Name );

        Framework.SetConfig( valueSeriesOutputSpikes1, "entityName", regionLayer1Name );
        Framework.SetConfig( valueSeriesOutputSpikes2, "entityName", regionLayer2Name );
        Framework.SetConfig( valueSeriesOutputSpikes3, "entityName", regionLayer3Name );

        Framework.SetConfig( valueSeriesPredictedName, "configPath", "classPredicted" ); // log forever
        Framework.SetConfig( valueSeriesErrorName, "configPath", "classError" );
        Framework.SetConfig( valueSeriesTruthName, "configPath", "classTruth" );

        Framework.SetConfig( valueSeriesClassifierError1, "configPath", "sumClassifierError" );
        Framework.SetConfig( valueSeriesClassifierError2, "configPath", "sumClassifierError" );
        Framework.SetConfig( valueSeriesClassifierError3, "configPath", "sumClassifierError" );

        Framework.SetConfig( valueSeriesOutputSpikes1, "configPath", "sumOutputSpikes" );
        Framework.SetConfig( valueSeriesOutputSpikes2, "configPath", "sumOutputSpikes" );
        Framework.SetConfig( valueSeriesOutputSpikes3, "configPath", "sumOutputSpikes" );

        // Spike logging matrices
        int spikeHistory = imageRepeats * 4;
        Framework.SetConfig( spikes1Name, "period", String.valueOf( spikeHistory ) );
        Framework.SetConfig( spikes2Name, "period", String.valueOf( spikeHistory ) );
        Framework.SetConfig( spikes3Name, "period", String.valueOf( spikeHistory ) );

        // region layer config
        // effective constants:
        float classifierLearningRate = 0.01f;
        int classifierAgeMin = 0; // age of disuse where we start to promote cells
        int classifierAgeMax = 1000; // age of disuse where we maximally promote cells
        float classifierAgeScale = 12f; // promotion nonlinearity
        float classifierRateScale = 5f; // inhibition nonlinearity
        float classifierRateMax = 0.25f; // i.e. never more than 1 in 4.
        float classifierRateLearningRate = 0.01f; // how fast the measurement of rate of cell use changes.
        float integrationDecayRate = 0.95f; // how fast the accumulated spikes decay
        float integrationSpikeWeight = 0.1f; // how much each dendrite spike contributes to the integrated value
        float predictorLearningRate = 0.01f; // how fast the prediction weights learn
        float predictorTraceDecayRate = 0.5f; // fast decay ie exact timing important
        int spikeOutputAgeMax = 4;

        // variables
        int widthCells = 20;
        int heightCells = 20;
        int classifierSparsity = 15; // k, the number of active cells each step
        float classifierSparsityOutput = 1.5f; // a factor determining the output sparsity

        setRegionLayerConfig(
                regionLayer1Name,
                widthCells, heightCells,
                classifierLearningRate, classifierSparsity, classifierSparsityOutput,
                classifierAgeMin, classifierAgeMax, classifierAgeScale, classifierRateScale, classifierRateMax, classifierRateLearningRate,
                spikeOutputAgeMax, predictorLearningRate );
//                integrationDecayRate, integrationSpikeWeight,
//                predictorTraceDecayRate, predictorLearningRate );

        setRegionLayerConfig(
                regionLayer2Name,
                widthCells, heightCells,
                classifierLearningRate, classifierSparsity, classifierSparsityOutput,
                classifierAgeMin, classifierAgeMax, classifierAgeScale, classifierRateScale, classifierRateMax, classifierRateLearningRate,
                spikeOutputAgeMax, predictorLearningRate );
//                integrationDecayRate, integrationSpikeWeight,
//                predictorTraceDecayRate, predictorLearningRate );

        setRegionLayerConfig(
                regionLayer3Name,
                widthCells, heightCells,
                classifierLearningRate, classifierSparsity, classifierSparsityOutput,
                classifierAgeMin, classifierAgeMax, classifierAgeScale, classifierRateScale, classifierRateMax, classifierRateLearningRate,
                spikeOutputAgeMax, predictorLearningRate );
//                integrationDecayRate, integrationSpikeWeight,
//                predictorTraceDecayRate, predictorLearningRate );
    }

    public static void setRegionLayerConfig(
            String regionLayerName,
            int widthCells,
            int heightCells,
            float classifierLearningRate,
            int classifierSparsity,
            float classifierSparsityOutput,
            int classifierAgeMin,
            int classifierAgeMax,
            float classifierAgeScale,
            float classifierRateScale,
            float classifierRateMax,
            float classifierRateLearningRate,
            int spikeOutputAgeMax,
//            float integrationDecayRate,
//            float integrationSpikeWeight,
//            float predictorTraceDecayRate,
            float predictorLearningRate ) {

        Framework.SetConfig( regionLayerName, "widthCells", String.valueOf( widthCells ) );
        Framework.SetConfig( regionLayerName, "heightCells", String.valueOf( heightCells ) );

        Framework.SetConfig( regionLayerName, "classifierLearningRate", String.valueOf( classifierLearningRate ) );
        Framework.SetConfig( regionLayerName, "classifierSparsity", String.valueOf( classifierSparsity ) );
        Framework.SetConfig( regionLayerName, "classifierSparsityOutput", String.valueOf( classifierSparsityOutput ) );
        Framework.SetConfig( regionLayerName, "classifierAgeMin", String.valueOf( classifierAgeMin ) );
        Framework.SetConfig( regionLayerName, "classifierAgeMax", String.valueOf( classifierAgeMax ) );
        Framework.SetConfig( regionLayerName, "classifierAgeScale", String.valueOf( classifierAgeScale ) );
        Framework.SetConfig( regionLayerName, "classifierRateScale", String.valueOf( classifierRateScale ) );
        Framework.SetConfig( regionLayerName, "classifierRateMax", String.valueOf( classifierRateMax ) );
        Framework.SetConfig( regionLayerName, "classifierRateLearningRate", String.valueOf( classifierRateLearningRate ) );

        Framework.SetConfig( regionLayerName, "spikeOutputAgeMax", String.valueOf( spikeOutputAgeMax ) );

//        Framework.SetConfig( regionLayerName, "integrationDecayRate", String.valueOf( integrationDecayRate ) );
//        Framework.SetConfig( regionLayerName, "integrationSpikeWeight", String.valueOf( integrationSpikeWeight ) );

//        Framework.SetConfig( regionLayerName, "predictorTraceDecayRate", String.valueOf( predictorTraceDecayRate ) );
        Framework.SetConfig( regionLayerName, "predictorLearningRate", String.valueOf( predictorLearningRate ) );
    }

}
