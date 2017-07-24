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

import io.agi.core.orm.AbstractPair;
import io.agi.core.util.PropertiesUtil;
import io.agi.framework.Framework;
import io.agi.framework.Main;
import io.agi.framework.Node;
import io.agi.framework.demo.CreateEntityMain;
import io.agi.framework.demo.mnist.ImageLabelEntity;
import io.agi.framework.demo.mnist.ImageLabelEntityConfig;
import io.agi.framework.entities.*;
import io.agi.framework.entities.stdp.*;
import io.agi.framework.factories.CommonEntityFactory;
import io.agi.framework.persistence.models.ModelData;

import java.util.ArrayList;
import java.util.Properties;

/**
 * Created by dave on 8/07/16.
 */
public class SpikingConvolutionalDemo extends CreateEntityMain {

    public static void main( String[] args ) {
        SpikingConvolutionalDemo demo = new SpikingConvolutionalDemo();
        demo.mainImpl(args );
    }

    public void createEntities( Node n ) {

//        String trainingPath = "/Users/gideon/Development/ProjectAGI/AGIEF/datasets/mnist/training-small";
//        String testingPath = "/Users/gideon/Development/ProjectAGI/AGIEF/datasets/mnist/training-small, /Users/gideon/Development/ProjectAGI/AGIEF/datasets/mnist/testing-small";

//        String trainingPath = "/home/dave/workspace/agi.io/data/mnist/1k_test";
//        String  testingPath = "/home/dave/workspace/agi.io/data/mnist/1k_test";

//        String trainingPath = "/home/dave/workspace/agi.io/data/mnist/10k_train";
//        String  testingPath = "/home/dave/workspace/agi.io/data/mnist/1k_test";

//        String trainingPath = "/home/dave/workspace/agi.io/data/mnist/cycle10";
//        String testingPath = "/home/dave/workspace/agi.io/data/mnist/cycle10";
        String trainingPath = "/home/dave/workspace/agi.io/data/mnist/cycle3";
        String testingPath = "/home/dave/workspace/agi.io/data/mnist/cycle3";

        // TODO after this, make a version that uses predictive encoding via feedback, which both uses feedback to help recognize and draws resources towards errors

//        int flushInterval = 20;
        int flushInterval = -1; // never flush
        String flushWriteFilePath = "/home/dave/workspace/agi.io/data/flush";
        String flushWriteFilePrefixTruth = "flushedTruth";
        String flushWriteFilePrefixFeatures = "flushedFeatures";

        boolean logDuringTraining = true;
//        boolean logDuringTraining = false;
        boolean cacheAllData = true;
        boolean terminateByAge = false;
        int terminationAge = -1;//50000;//25000;
        //int trainingEpochs = 5; // = 5 * 10 images * 30 repeats = 1500
//        int trainingEpochs = 100;//20; // = 5 * 10 images * 30 repeats = 1500      30*10*30 =
        int trainingEpochs = 50;//20; // = 5 * 10 images * 30 repeats = 1500      30*10*30 =
        int testingEpochs = 1; // = 1 * 10 images * 30 repeats = 300
        int imageRepeats = 30; // paper - 30
//        int imagesPerEpoch = 10;
//        int trainingAge0 = 0;
//        int trainingAge1 = trainingEpochs * imagesPerEpoch / 2;

        // 100 features x 60 history (1x10 for testing + 5x10 for training)
        // total 1802 steps

        // Define some entities
        String experimentName           = Framework.GetEntityName( "experiment" );
        String imageLabelName           = Framework.GetEntityName( "image-class" );
        String vectorSeriesName         = Framework.GetEntityName( "feature-series" );
        String valueSeriesName          = Framework.GetEntityName( "label-series" );

        // Algorithm
        String dogPosName               = Framework.GetEntityName( "dog-pos" );
        String dogNegName               = Framework.GetEntityName( "dog-neg" );
        String spikeEncoderName         = Framework.GetEntityName( "spike-encoder" );
        String spikingConvolutionalName = Framework.GetEntityName( "stdp-cnn" );

        String parentName = null;
        parentName = Framework.CreateEntity( experimentName, ExperimentEntity.ENTITY_TYPE, n.getName(), null ); // experiment is the root entity
        parentName = Framework.CreateEntity( imageLabelName, ImageLabelEntity.ENTITY_TYPE, n.getName(), parentName );

        parentName = Framework.CreateEntity( dogPosName, DifferenceOfGaussiansEntity.ENTITY_TYPE, n.getName(), parentName );
        parentName = Framework.CreateEntity( dogNegName, DifferenceOfGaussiansEntity.ENTITY_TYPE, n.getName(), parentName );
        parentName = Framework.CreateEntity( spikeEncoderName, ConvolutionalSpikeEncoderEntity.ENTITY_TYPE, n.getName(), parentName );
        parentName = Framework.CreateEntity( spikingConvolutionalName, SpikingConvolutionalNetworkEntity.ENTITY_TYPE, n.getName(), parentName );

        parentName = Framework.CreateEntity( vectorSeriesName, VectorSeriesEntity.ENTITY_TYPE, n.getName(), parentName ); // 2nd, class region updates after first to get its feedback
        parentName = Framework.CreateEntity( valueSeriesName, ValueSeriesEntity.ENTITY_TYPE, n.getName(), parentName ); // 2nd, class region updates after first to get its feedback

        // Debug logs
//        String vectorSeriesParentName = vectorSeriesName;
//        CreateVectorSeriesLog( "vector-series-1", vectorSeriesParentName, spikingConvolutionalName, SpikingConvolutionalNetworkEntity.DATA_LAYER_CONV_SPIKES_ + "0", ModelData.ENCODING_SPARSE_BINARY );
//        CreateVectorSeriesLog( "vector-series-2", vectorSeriesParentName, spikingConvolutionalName, SpikingConvolutionalNetworkEntity.DATA_LAYER_CONV_SPIKES_ + "1", ModelData.ENCODING_SPARSE_BINARY );
////        CreateVectorSeriesLog( "vector-series-3", vectorSeriesParentName, spikingConvolutionalName, SpikingConvolutionalNetworkEntity.DATA_LAYER_CONV_SPIKES_ + "2  ", ModelData.ENCODING_SPARSE_BINARY );
//        CreateVectorSeriesLog( "vector-series-1i", vectorSeriesParentName, spikingConvolutionalName, SpikingConvolutionalNetworkEntity.DATA_LAYER_CONV_INHIBITION_ + "0", ModelData.ENCODING_SPARSE_BINARY );
//        CreateVectorSeriesLog( "vector-series-2i", vectorSeriesParentName, spikingConvolutionalName, SpikingConvolutionalNetworkEntity.DATA_LAYER_CONV_INHIBITION_ + "1", ModelData.ENCODING_SPARSE_BINARY );
//
//        CreateVectorSeriesLog( "vector-series-1p", vectorSeriesParentName, spikingConvolutionalName, SpikingConvolutionalNetworkEntity.DATA_LAYER_POOL_SPIKES_ + "0", ModelData.ENCODING_SPARSE_BINARY );
//        CreateVectorSeriesLog( "vector-series-2p", vectorSeriesParentName, spikingConvolutionalName, SpikingConvolutionalNetworkEntity.DATA_LAYER_POOL_SPIKES_ + "1", ModelData.ENCODING_SPARSE_BINARY );

        // Connect the entities' data
        Framework.SetDataReference( dogPosName, DifferenceOfGaussiansEntity.DATA_INPUT, imageLabelName, ImageLabelEntity.OUTPUT_IMAGE );
        Framework.SetDataReference( dogNegName, DifferenceOfGaussiansEntity.DATA_INPUT, imageLabelName, ImageLabelEntity.OUTPUT_IMAGE );

        Framework.SetDataReference( spikeEncoderName, ConvolutionalSpikeEncoderEntity.DATA_INPUT_POS, dogPosName, DifferenceOfGaussiansEntity.DATA_OUTPUT );
//        Framework.SetDataReference( spikeEncoderName, ConvolutionalSpikeEncoderEntity.DATA_INPUT_NEG, dogNegName, DifferenceOfGaussiansEntity.DATA_OUTPUT );

        // a) Image to image region, and decode
        Framework.SetDataReference( spikingConvolutionalName, SpikingConvolutionalNetworkEntity.DATA_INPUT, spikeEncoderName, ConvolutionalSpikeEncoderEntity.DATA_OUTPUT );

        ArrayList< AbstractPair< String, String > > featureDatas = new ArrayList<>();
//        featureDatas.add( new AbstractPair<>( spikeEncoderName, ConvolutionalSpikeEncoderEntity.DATA_OUTPUT ) );
        featureDatas.add( new AbstractPair<>( spikingConvolutionalName, SpikingConvolutionalNetworkEntity.DATA_OUTPUT ) );
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

// QUESTIONS:
// MNIST - how many training epochs of the 60,000 image training set?
// MNIST - training is layer by layer, but what is the schedule for this - how many image before each layer is trained?
// MNIST - paper says "The first and second convolutional layers respectively consist of 30 and 100 neuronal maps with
//         5 × 5 convolution-window and firing thresholds of 15 and 10". But as far as I can tell there are only 2 layers
//         for MNIST and the section on global pooling says the threshold for the convolutional layer integration in the
//         final layer should be infinite?
// Inhibition: "Also, there is a local inter-map competition for STDP. When a neuron is allowed to do the STDP, it prevents
// the neurons in other maps within a small neighborhood around its location from doing STDP. This competition is crucial
// to encourage neurons of different maps to learn different features." I'm confused what the neighbourhood is here. It is
// never defined. Is it a neighbourhood in Z (the different models of the input at one X,Y location)? Or, is it a spatial
// neighbourhood in X,Y? How big is it?



// - Add greedy layerwise training schedule.
//        "The learning is done layer\n" +
//                "by layer, i.e., the learning in a convolutional layer\n" +
//                "starts when the learning in the previous convolu-\n" +
//                "tional layer is finalized."
//   ?Assume one epoch for now?
// DONE, TESTED

// - Add integrated potential output from final layer
//        "To compute the output of the global pooling\n" +
//                "layer, first, the threshold of neurons in the last con-\n" +
//                "volutional layer were set to be infinite, and then,\n" +
//                "their final potentials (after propagating the whole\n" +
//                "spike train generated by the input image) were\n" +
//                "measured. These final potentials can be seen as\n" +
//                "the number of early spikes in common between the\n" +
//                "current input and the stored prototypes in the last\n" +
//                "convolutional layer. Finally, the global pooling neu-\n" +
//                "rons compute the maximum potential at their cor-\n" +
//                "responding neuronal maps, as their output value."
// DONE, TESTED

//- DEBUG Invert the current classification (or any classification) (invert a final layer output, real, non unit)
// DONE, untested

// NEXT STEPS
// - Verify / fix invert function
// - Verify classifier function possible
// - 1/  First add online learning, but still with specific clear() events
// - 2/  replace clear with decay of integrated values, so it discovers the time steps
// - 3/  add prediction to help classification
// - 4/  add predictive coding (larger spike train output on prediction FN error)

//- figure out why it's not training; add the online learning rule (nuts, that doesn't work with convolutional)
//                             adapt the rule to convolutional using a measure of frequency in the shared weights.
//- Prediction: Do we have a timing rule that input from the apical dendrite must arrive before a post-spike not after, AND that it must not
//- Do we implement the spike-train encoding? [Yes, because bio evidenc for it]. Binding problem - joint handling of dynamically allocated variables.
//- Feedback: What to do with feedback? Start with all zero weights? Do we train when prediction fails? [ New evidence: we have papers showing PC and feedback reduces time to output spike or suppresses/truncates output spike ]

        float stdDev1 = 1f;
        float stdDev2 = 2f;
        int kernelSize = 7;
        SetDoGEntityConfig( dogPosName, stdDev1, stdDev2, kernelSize, 1.0f );
        SetDoGEntityConfig( dogNegName, stdDev2, stdDev1, kernelSize, 1.0f );

        float spikeThreshold = 5.0f;
        String clearFlagEntityName = imageLabelName;
        String clearFlagConfigPath = "imageChanged";
        SetSpikeEncoderEntityConfig( spikeEncoderName, spikeThreshold, clearFlagEntityName, clearFlagConfigPath );

        // cache all data for speed, when enabled
        Framework.SetConfig( experimentName, "cache", String.valueOf( cacheAllData ) );
        Framework.SetConfig( imageLabelName, "cache", String.valueOf( cacheAllData ) );
        Framework.SetConfig( spikeEncoderName, "cache", String.valueOf( cacheAllData ) );
        Framework.SetConfig( spikingConvolutionalName, "cache", String.valueOf( cacheAllData ) );
        Framework.SetConfig( vectorSeriesName, "cache", String.valueOf( cacheAllData ) );
        Framework.SetConfig( valueSeriesName, "cache", String.valueOf( cacheAllData ) );

        // MNIST config
        String trainingEntities = "";
        String testingEntities = "";
        if( !logDuringTraining ) {
            testingEntities = vectorSeriesName + "," + valueSeriesName;
        }
        SetImageLabelEntityConfig( imageLabelName, trainingPath, testingPath, trainingEpochs, testingEpochs, imageRepeats, trainingEntities, testingEntities );

        // "Synaptic weights of convolutional neurons initiate with random values drown from a normal distribution with the mean of 0.8 and STD of 0.05"
        float weightsStdDev = 0.05f;
        float weightsMean = 0.8f;
//        float learningRatePos = 0.0001f;//4f;
//        float learningRateNeg = 0.0003f;
//        float integrationThreshold = 12; // 15 and 10 by layer
        int inputWidth = 28;
        int inputHeight = 28;
//        int inputDepth = 2;
        int inputDepth = 1;

        SetSpikingConvolutionalEntityConfig(
                spikingConvolutionalName, clearFlagEntityName, clearFlagConfigPath,
                weightsStdDev, weightsMean,
                //learningRatePos, learningRateNeg,
                inputWidth, inputHeight, inputDepth,
                imageRepeats );
//                trainingAge0, trainingAge1 );

        // NOTE about logging: We accumulate the labels and features for all images, but then we only append a new sample of (features,label) every N steps
        // This timing corresponds with the change from one image to another. In essence we allow the network to respond to the image for a few steps, while recording its output

        // Log features of the algorithm during all phases
        int accumulatePeriod = imageRepeats;
        int period = -1;
        VectorSeriesEntityConfig.Set( vectorSeriesName, accumulatePeriod, period, ModelData.ENCODING_SPARSE_REAL );

        // Log image label for each set of features
        String valueSeriesInputEntityName = imageLabelName;
        String valueSeriesInputConfigPath = "imageLabel";
        String valueSeriesInputDataName = "";
        int inputDataOffset = 0;
        float accumulateFactor = 1f / imageRepeats;
        ValueSeriesEntityConfig.Set( valueSeriesName, accumulatePeriod, accumulateFactor, -1, period, valueSeriesInputEntityName, valueSeriesInputConfigPath, valueSeriesInputDataName, inputDataOffset );

        // Debug logs
//        String encoderIntSeriesName = Framework.GetEntityName( "enc-int-series" );
//        String encoderOutSeriesName = Framework.GetEntityName( "enc-out-series" );
//
//        parentName = Framework.CreateEntity( encoderIntSeriesName, VectorSeriesEntity.ENTITY_TYPE, n.getName(), parentName );
//        parentName = Framework.CreateEntity( encoderOutSeriesName, VectorSeriesEntity.ENTITY_TYPE, n.getName(), parentName );
//
//        Framework.SetDataReference( encoderIntSeriesName, VectorSeriesEntity.INPUT, spikeEncoderName, ConvolutionalSpikeEncoderEntity.DATA_INTEGRATED );
//        Framework.SetDataReference( encoderOutSeriesName, VectorSeriesEntity.INPUT, spikeEncoderName, ConvolutionalSpikeEncoderEntity.DATA_OUTPUT );
//
//        accumulatePeriod = 1;
//        VectorSeriesEntityConfig.Set( encoderIntSeriesName, accumulatePeriod, period, ModelData.ENCODING_SPARSE_REAL );
//        VectorSeriesEntityConfig.Set( encoderOutSeriesName, accumulatePeriod, period, ModelData.ENCODING_SPARSE_REAL );

        // Debug the algorithm
        period = 30 * 6;
        accumulatePeriod = 1;

        String encSeriesName = Framework.GetEntityName( "enc-series" );
        parentName = Framework.CreateEntity( encSeriesName, VectorSeriesEntity.ENTITY_TYPE, n.getName(), parentName );
        Framework.SetDataReference( encSeriesName, VectorSeriesEntity.INPUT, spikeEncoderName, ConvolutionalSpikeEncoderEntity.DATA_OUTPUT );
        VectorSeriesEntityConfig.Set( encSeriesName, accumulatePeriod, period, ModelData.ENCODING_SPARSE_REAL );

//        String controllerSeriesName = Framework.GetEntityName( "controller-series" );
//        parentName = Framework.CreateEntity( controllerSeriesName, VectorSeriesEntity.ENTITY_TYPE, n.getName(), parentName );
//        Framework.SetDataReference( controllerSeriesName, VectorSeriesEntity.INPUT, spikingConvolutionalName, SpikingConvolutionalNetworkEntity.DATA_LAYER_CONV_CONTROLLER_ + "1" );
//        VectorSeriesEntityConfig.Set( controllerSeriesName, accumulatePeriod, period, ModelData.ENCODING_SPARSE_REAL );

        int controllerPeriod = -1;
        String controllerInputSeriesName = Framework.GetEntityName( "controller-series-input" );
        parentName = Framework.CreateEntity( controllerInputSeriesName, ValueSeriesEntity.ENTITY_TYPE, n.getName(), parentName );
        ValueSeriesEntityConfig.Set( controllerInputSeriesName, accumulatePeriod, 1.f, -1, controllerPeriod, spikingConvolutionalName, "controllerInput", null, 0 );

        String controllerOutputSeriesName = Framework.GetEntityName( "controller-series-output" );
        parentName = Framework.CreateEntity( controllerOutputSeriesName, ValueSeriesEntity.ENTITY_TYPE, n.getName(), parentName );
        ValueSeriesEntityConfig.Set( controllerOutputSeriesName, accumulatePeriod, 1.f, -1, controllerPeriod, spikingConvolutionalName, "controllerOutput", null, 0 );

        String controllerErrorSeriesName = Framework.GetEntityName( "controller-series-error" );
        parentName = Framework.CreateEntity( controllerErrorSeriesName, ValueSeriesEntity.ENTITY_TYPE, n.getName(), parentName );
        ValueSeriesEntityConfig.Set( controllerErrorSeriesName, accumulatePeriod, 1.f, -1, controllerPeriod, spikingConvolutionalName, "controllerError", null, 0 );

        String netInh1SeriesName = Framework.GetEntityName( "net-inh-1-series" );
        String netInt1SeriesName = Framework.GetEntityName( "net-int-1-series" );
        String netSpk1SeriesName = Framework.GetEntityName( "net-spk-1-series" );

        String netInh2SeriesName = Framework.GetEntityName( "net-inh-2-series" );
        String netInt2SeriesName = Framework.GetEntityName( "net-int-2-series" );
        String netSpk2SeriesName = Framework.GetEntityName( "net-spk-2-series" );

        parentName = Framework.CreateEntity( netInh1SeriesName, VectorSeriesEntity.ENTITY_TYPE, n.getName(), parentName );
        parentName = Framework.CreateEntity( netInt1SeriesName, VectorSeriesEntity.ENTITY_TYPE, n.getName(), parentName );
        parentName = Framework.CreateEntity( netSpk1SeriesName, VectorSeriesEntity.ENTITY_TYPE, n.getName(), parentName );

        parentName = Framework.CreateEntity( netInh2SeriesName, VectorSeriesEntity.ENTITY_TYPE, n.getName(), parentName );
        parentName = Framework.CreateEntity( netInt2SeriesName, VectorSeriesEntity.ENTITY_TYPE, n.getName(), parentName );
        parentName = Framework.CreateEntity( netSpk2SeriesName, VectorSeriesEntity.ENTITY_TYPE, n.getName(), parentName );

        String layer = "0";
        Framework.SetDataReference( netInh1SeriesName, VectorSeriesEntity.INPUT, spikingConvolutionalName, SpikingConvolutionalNetworkEntity.DATA_LAYER_POOL_INHIBITION_ + layer );
        Framework.SetDataReference( netInt1SeriesName, VectorSeriesEntity.INPUT, spikingConvolutionalName, SpikingConvolutionalNetworkEntity.DATA_LAYER_CONV_INTEGRATED_ + layer );
//        Framework.SetDataReference( netSpk1SeriesName, VectorSeriesEntity.INPUT, spikingConvolutionalName, SpikingConvolutionalNetworkEntity.DATA_LAYER_CONV_SPIKES_ + layer );
        Framework.SetDataReference( netSpk1SeriesName, VectorSeriesEntity.INPUT, spikingConvolutionalName, SpikingConvolutionalNetworkEntity.DATA_LAYER_POOL_SPIKES_INTEGRATED_ + layer );

        layer = "1";
//        Framework.SetDataReference( netInh2SeriesName, VectorSeriesEntity.INPUT, spikingConvolutionalName, SpikingConvolutionalNetworkEntity.DATA_LAYER_CONV_INHIBITION_ + layer );
        Framework.SetDataReference( netInh2SeriesName, VectorSeriesEntity.INPUT, spikingConvolutionalName, SpikingConvolutionalNetworkEntity.DATA_LAYER_POOL_INHIBITION_ + layer );
        Framework.SetDataReference( netInt2SeriesName, VectorSeriesEntity.INPUT, spikingConvolutionalName, SpikingConvolutionalNetworkEntity.DATA_LAYER_CONV_INTEGRATED_ + layer );
//        Framework.SetDataReference( netSpk2SeriesName, VectorSeriesEntity.INPUT, spikingConvolutionalName, SpikingConvolutionalNetworkEntity.DATA_LAYER_CONV_SPIKES_ + layer );
        Framework.SetDataReference( netSpk2SeriesName, VectorSeriesEntity.INPUT, spikingConvolutionalName, SpikingConvolutionalNetworkEntity.DATA_LAYER_POOL_SPIKES_INTEGRATED_ + layer );

        VectorSeriesEntityConfig.Set( netInh1SeriesName, accumulatePeriod, period, ModelData.ENCODING_SPARSE_REAL );
        VectorSeriesEntityConfig.Set( netInt1SeriesName, accumulatePeriod, period, ModelData.ENCODING_SPARSE_REAL );
        VectorSeriesEntityConfig.Set( netSpk1SeriesName, accumulatePeriod, period, ModelData.ENCODING_SPARSE_REAL );

        VectorSeriesEntityConfig.Set( netInh2SeriesName, accumulatePeriod, period, ModelData.ENCODING_SPARSE_REAL );
        VectorSeriesEntityConfig.Set( netInt2SeriesName, accumulatePeriod, period, ModelData.ENCODING_SPARSE_REAL );
        VectorSeriesEntityConfig.Set( netSpk2SeriesName, accumulatePeriod, period, ModelData.ENCODING_SPARSE_REAL );
    }

    protected static void SetDoGEntityConfig( String entityName, float stdDev1, float stdDev2, int kernelSize, float scaling ) {
        DifferenceOfGaussiansEntityConfig entityConfig = new DifferenceOfGaussiansEntityConfig();
        entityConfig.cache = true;
        entityConfig.kernelWidth = kernelSize;
        entityConfig.kernelHeight = entityConfig.kernelWidth;
        entityConfig.stdDev1 = stdDev1;
        entityConfig.stdDev2 = stdDev2;
        entityConfig.scaling = scaling;
        entityConfig.min = -2.0f;
        entityConfig.max = 2.0f;

        Framework.SetConfig( entityName, entityConfig );
    }

    protected static void SetImageLabelEntityConfig( String entityName, String trainingPath, String testingPath, int trainingEpochs, int testingEpochs, int repeats, String trainingEntities, String testingEntities ) {

        ImageLabelEntityConfig entityConfig = new ImageLabelEntityConfig();
        entityConfig.cache = true;
        entityConfig.receptiveField.receptiveFieldX = 0;
        entityConfig.receptiveField.receptiveFieldY = 0;
        entityConfig.receptiveField.receptiveFieldW = 28;
        entityConfig.receptiveField.receptiveFieldH = 28;
        entityConfig.resolution.resolutionX = 28;
        entityConfig.resolution.resolutionY = 28;

        entityConfig.greyscale = true;
        entityConfig.invert = true;
//        entityConfig.sourceType = BufferedImageSourceFactory.TYPE_IMAGE_FILES;
//        entityConfig.sourceFilesPrefix = "postproc";
        entityConfig.sourceFilesPathTraining = trainingPath;
        entityConfig.sourceFilesPathTesting = testingPath;
        entityConfig.trainingEpochs = trainingEpochs;
        entityConfig.testingEpochs = testingEpochs;
        entityConfig.trainingEntities = trainingEntities;
        entityConfig.testingEntities = testingEntities;
        entityConfig.resolution.resolutionY = 28;

        entityConfig.shuffleTraining = false;

        entityConfig.imageRepeats = repeats;

        Framework.SetConfig( entityName, entityConfig );
    }

    protected static void SetSpikeEncoderEntityConfig( String entityName, float spikeThreshold, String clearFlagEntityName, String clearFlagConfigPath ) {

        ConvolutionalSpikeEncoderEntityConfig entityConfig = new ConvolutionalSpikeEncoderEntityConfig();
        entityConfig.cache = true;
        entityConfig.spikeThreshold = spikeThreshold;
        entityConfig.clearFlagEntityName = clearFlagEntityName;
        entityConfig.clearFlagConfigPath = clearFlagConfigPath;

        Framework.SetConfig( entityName, entityConfig );
    }

    protected static void SetSpikingConvolutionalEntityConfig(
            String entityName,
            String clearFlagEntityName,
            String clearFlagConfigPath,
            float weightsStdDev,
            float weightsMean,
//            float learningRatePos,
//            float learningRateNeg,
//            float integrationThreshold,
            int inputWidth,
            int inputHeight,
            int inputDepth,
            int imageRepeats ) {
//            int trainingAge0,
//            int trainingAge1 ) {

        SpikingConvolutionalNetworkEntityConfig entityConfig = new SpikingConvolutionalNetworkEntityConfig();
        entityConfig.clearFlagEntityName = clearFlagEntityName;
        entityConfig.clearFlagConfigPath = clearFlagConfigPath;
        entityConfig.cache = true;
        entityConfig.weightsStdDev = weightsStdDev;
        entityConfig.weightsMean = weightsMean;
//        entityConfig.learningRatePos = learningRatePos;
//        entityConfig.learningRateNeg = learningRateNeg;
        entityConfig.learningRatePos = 0.01f;
        entityConfig.learningRateNeg = 0.01f;

        // Note on configuring spike frequencies
        // Let's say we want a spike density of K=5% per image.
        // That means if we have 100 cells, then we will have 5 spikes.
        // Since we have N=30 repeats, to get those 5 spikes we actually need a per-update frequency of
        // K/N = 0.05 / 30 = 0.001666667 spikes per update.
        // This is the target frequency

        //maybe I wanna make them fire more easily, but suffer from inhibition to sparsen?

        float densityScaling = 1.f / (float)imageRepeats; // e.g. if
//        densityScaling *= 12f;
        float layerKernelSpikeFrequencyLearningRate = 0.0001f;
        float layerKernelSpikeFrequencyTarget = 0.1f * densityScaling; // how often each kernel should fire, as a measure of average density (spikes per unit area).
//        float layerKernelSpikeFrequencyLearningRate = 0.01f; // try this verify the adaptive mechanism then improve controller
        //float layerSpikeFrequencyLearningRate = 0.01f;//0.0001f;
        //float layerSpikeFrequencyTarget = 1f - ( 0.05f * densityScaling ); // ignoring area, how o  (inverted input)
        float layerSpikeFrequencyTarget = ( 0.1f * densityScaling ); // ignoring area, how o
        float layerSpikeFrequencyPeriod = 90;///2 * 3 * (float)imageRepeats;
        float layerSpikeFrequencyControllerP = 1.01f;//10.0f;
        float layerSpikeFrequencyControllerI = 100.0f;
        float layerSpikeFrequencyControllerD = 0f;
        float layerSpikeFrequencyControllerN = 1f;//(float)imageRepeats;
        float layerSpikeFrequencyControllerT = 1f;//densityScaling;
        float layerSpikeFrequencyControllerMin = -1000f;
//        float layerSpikeFrequencyControllerMin = -200f;
        float layerSpikeFrequencyControllerMax = 1000f;//20f; // TODO find some principled way to set this high enough

//        entityConfig.learningRateSpikeFrequency = 0.0001f; // ie. 0.0001 = 0.003 when you remove the influence of showing images repeatedly.
//        entityConfig.learningRateSpikeFrequency = 0.0001f; // ie. 0.0001 = 0.003 when you remove the influence of showing images repeatedly.
//        entityConfig.integrationThreshold = integrationThreshold;
        entityConfig.nbrLayers = 2;//3;

        int iw = inputWidth;
        int ih = inputHeight;
        int id = inputDepth;

        float[] layerThresholds = { 15.f, 10.f };
//        float[] layerThresholds = { 15.f, Float.MAX_VALUE }; // trigger integration forever in final layer
//        int[] layerWidths = { 9,1 };
//        int[] layerHeights = { 9,1 };
//        int[] layerDepths = { 30,100 };
        int[] layerDepths = { 28,10 }; // reduce for speed
//        int[] layerPoolingSize = { 2,8 }; // for classification in Z
        int[] layerPoolingSize = { 2,2 }; // for reconstruction
        int[] layerFieldSize = { 5,5 };
//        int[] layerInputStrides = { 3,1 };
        int[] layerInputPaddings = { 0,0 };
//        int[] layerTrainingAges = { 0,60000 }; // probably correct
//        int[] layerTrainingAges = { trainingAge0, trainingAge1 };

        // say we want each model to fire 1/n models.
        // if we have 30, then 1/30 = 0.03 is about right.
        //float[] layerSpikeFrequencies = { 0.03f, 0.02f };

        // Generate config properties from these values:
        for( int layer = 0; layer < entityConfig.nbrLayers; ++layer ) {

            String prefix = "";
            if( layer > 0 ) prefix = ",";

//            float layerSpikeFrequency = layerSpikeFrequencies[ layer ];
            float layerThreshold = layerThresholds[ layer ];
            int layerInputPadding = layerInputPaddings[ layer ];
            int layerInputStride = 1;//layerInputStrides[ layer ];
            int ld = layerDepths[ layer ];
            int pw = layerPoolingSize[ layer ];
            int ph = pw;
            int fw = layerFieldSize[ layer ];
            int fh = fw;
            int fd = id;
            int lw = iw - fw +1;//layerWidths[ layer ];;
            int lh = ih - fh +1;//layerHeights[ layer ];;

 //           entityConfig.layerTrainingAge += prefix + layerTrainingAges[ layer ];
            entityConfig.layerIntegrationThreshold +=  prefix + layerThreshold;
            entityConfig.layerInputPadding += prefix + layerInputPadding;
            entityConfig.layerInputStride  += prefix + layerInputStride;
            entityConfig.layerWidth  += prefix + lw;
            entityConfig.layerHeight += prefix + lh;
            entityConfig.layerDepth  += prefix + ld;
            entityConfig.layerfieldWidth += prefix + fw;
            entityConfig.layerfieldHeight += prefix + fh;
            entityConfig.layerfieldDepth += prefix + fd;
            entityConfig.layerPoolingWidth += prefix + pw;
            entityConfig.layerPoolingHeight += prefix + ph;
//            entityConfig.layerSpikeFrequencies += prefix + layerSpikeFrequency;

            entityConfig.layerKernelSpikeFrequencyLearningRate += prefix + layerKernelSpikeFrequencyLearningRate;
            entityConfig.layerKernelSpikeFrequencyTarget += prefix + layerKernelSpikeFrequencyTarget;
            entityConfig.layerSpikeFrequencyPeriod += prefix + layerSpikeFrequencyPeriod;
            entityConfig.layerSpikeFrequencyTarget += prefix + layerSpikeFrequencyTarget;
            entityConfig.layerSpikeFrequencyControllerP += prefix + layerSpikeFrequencyControllerP;
            entityConfig.layerSpikeFrequencyControllerI += prefix + layerSpikeFrequencyControllerI;
            entityConfig.layerSpikeFrequencyControllerD += prefix + layerSpikeFrequencyControllerD;
            entityConfig.layerSpikeFrequencyControllerN += prefix + layerSpikeFrequencyControllerN;
            entityConfig.layerSpikeFrequencyControllerT += prefix + layerSpikeFrequencyControllerT;
            entityConfig.layerSpikeFrequencyControllerMin += prefix + layerSpikeFrequencyControllerMin;
            entityConfig.layerSpikeFrequencyControllerMax += prefix + layerSpikeFrequencyControllerMax;

            // TODO auto calculate layer widths and heights
            iw = lw / pw;
            ih = lh / ph;
            id = ld;
        }

        Framework.SetConfig( entityName, entityConfig );
    }

    // Input 1: 28 x 28 (x2)
    // Window: 5x5, stride 2, padding = 0
    //     00 01 02 03 04 05 06 07 08 09 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 |
    //  F1 -- -- -- -- --                                                                      |
    //  F2          -- -- -- -- --                                                             |
    //  F3                   -- -- -- -- --                                                    |
    //  F4                            -- -- -- -- --                                           |
    //  F5                                     -- -- -- -- --                                  |
    //  F6                                              -- -- -- -- --                         |
    //  F7                                                       -- -- -- -- --
    //  F8                                                                -- -- -- -- --
    //  F9                                                                         -- -- -- -- xx

    // Max Pooling:
    // 0 1  2 3  4 5  6 7  8 *
    //  0    1    2    3    4
    // So output is 5x5

    // Input 1: 5 x 5 (x30)
    // Window: 5x5, stride 1, padding = 0
    //     00 01 02 03 04
    //  F1 -- -- -- -- --
    // Output is 1x1 by depth 100



    // Input 1: 28 x 28 (x2 in Z, for DoG + and -)
    // Window: 5x5, stride 1, padding = 0
    // iw - kw +1 = 28-5+1 = 24
    //     00 01 02 03 04 05 06 07 08 09 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 |
    //  F1 -- -- -- -- --                                                                      |
    //  F2    -- -- -- -- --                                                             |
    //  F3       -- -- -- -- --                                                    |
    //  F4          -- -- -- -- --                                           |
    //  F5             -- -- -- -- --                                  |
    //  F6                -- -- -- -- --                         |
    //  F7                   -- -- -- -- --
    //  F8                      -- -- -- -- --
    //  F9                         -- -- -- -- xx
    //  F10                           -- -- -- -- xx
    //  F11                              -- -- -- -- xx
    //  F12                                 -- -- -- -- xx
    //  F13                                    -- -- -- -- xx
    //  F14                                       -- -- -- -- xx
    //  F15                                          -- -- -- -- xx
    //  F16                                             -- -- -- -- xx
    //  F17                                                -- -- -- -- xx
    //  F18                                                   -- -- -- -- xx
    //  F19                                                      -- -- -- -- xx
    //  F20                                                         -- -- -- -- xx
    //  F21                                                            -- -- -- -- xx
    //  F22                                                               -- -- -- -- xx
    //  F23                                                                  -- -- -- -- xx
    //  F24                                                                     -- -- -- -- xx
    //     00 01 02 03 04 05 06 07 08 09 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 |

    // 24 cells wide in conv 0
    // Max pooling size=2 stride=2
    //     00 01 02 03 04 05 06 07 08 09 10 11 12 13 14 15 16 17 18 19 20 21 22 23 |
    // F1  -- --
    // F2        -- --
    // F3              -- --
    // F4                    -- --
    // F5                          -- --
    // F6                                -- --
    // F7                                      -- --
    // F8                                            -- --
    // F9                                                  -- --
    // F10                                                       -- --
    // F11                                                             -- --
    // F12                                                                   -- --
    //     00 01 02 03 04 05 06 07 08 09 10 11 12 13 14 15 16 17 18 19 20 21 22 23 |

    // Conv layer 2: 12 inputs. Needs 8 cells.
    // iw - kw +1 = 12-5+1 = 8
    //     00 01 02 03 04 05 06 07 08 09 10 11 |
    //  F1 -- -- -- -- --
    //  F2    -- -- -- -- --
    //  F3       -- -- -- -- --
    //  F4          -- -- -- -- --
    //  F5             -- -- -- -- --
    //  F6                -- -- -- -- --
    //  F7                   -- -- -- -- --
    //  F8                      -- -- -- -- --
    //     00 01 02 03 04 05 06 07 08 09 10 11 |

    // Max pooling layer 2: over all, so output 1x1 by depth.
}
