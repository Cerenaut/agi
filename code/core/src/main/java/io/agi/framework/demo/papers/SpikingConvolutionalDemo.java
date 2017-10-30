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
import io.agi.framework.persistence.DataJsonSerializer;
import io.agi.framework.persistence.PersistenceUtil;
import io.agi.framework.persistence.models.ModelData;
import io.agi.framework.references.DataRefUtil;

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

// Big picture:
// - 2/  replace clear with decay of integrated values, so it discovers the time steps
// - 3/  add prediction to help classification
// - 4/  add predictive coding (larger spike train output on prediction FN error)

// FIRST RUN of our STDP variant
// 85% / 78%
// DONE DISABLE LEARNING WHEN NOT LEARN INC GAIN AND THRESHOLD!
// 83% / 75% -- more training may help!
// TODO Code a slowdown in learning as it approaches saturation
// 89% / 83%
// TODO Kernel gain may be too strong - with layerDepth=28, and repeats=30, then 0.0011. What I observe is:
// T:  0.0011         0.0011
// L0: 0.0004 <= f <= 0.0005  --- artificially flat?
// L1: 0.0008 <= f <= 0.0012  --- about right?
// Solution: Implemented a relative frequency so it's not tied (so closely) to the rate of spiking and inhibition policy.
// Also all long term changes are now based on summation over time windows so easier to control for very slow learning.
// 88% / 79.9%
// TODO Increase layer 2 pooling size and Z to 100 as per paper
// 29.2% / 29.6%
// TODO check not too many outputs - actually there's just 1? Due to inhibition.
// (checked, it is 1 of 100 per row. not uniform but that's reasonable, some digits more varied in appearance)
// TODO use Saeed's method of providing output to classification
// 11.71% / 11.9%

// TODO check the encoder produces viable bits for classification. Is the distribution peaky enough?
// OK so we're missing the local normalization and the ordering encoding is wrong.
// Re-check with 0-9 cycle that we're getting a steady rate of spikes for all inputs - not a step up and down in rate.
// Then re-run with the new encoding.
// Check PASSED OK. see feature-series-output. Re test 10k/1k:
// With saeed's output: 11%.
// With max-binary output: 26.7%

// Ideas list
// TODO implement the lateral inhibition between cols. in layer 1 by receptive field size.
// TODO check that we're not spiking too fast - that would
// TODO Restore input from other DoG encoder (neg features)
// TODO Learning rate may be too fast in general a+ = 0.004 and a− = 0.003 in paper, I'm using 0.01 (30x faster)
// TODO double the training epochs see if any effect
// TODO Allow weights to increase more easily than decrease, so each cell can potentially represent more than 1 thing

// When working on image classification:
// TODO after this, make a version that uses predictive encoding via feedback, which both uses feedback to help recognize and draws resources towards errors
//- Prediction: Do we have a timing rule that input from the apical dendrite must arrive before a post-spike not after, AND that it must not
//- Do we implement the spike-train encoding? [Yes, because bio evidenc for it]. Binding problem - joint handling of dynamically allocated variables.
//- Feedback: What to do with feedback? Start with all zero weights? Do we train when prediction fails? [ New evidence: we have papers showing PC and feedback reduces time to output spike or suppresses/truncates output spike ]

    public void createEntities( Node n ) {

        // Dataset
//        String trainingPath = "/Users/gideon/Development/ProjectAGI/AGIEF/datasets/mnist/training-small";
//        String testingPath = "/Users/gideon/Development/ProjectAGI/AGIEF/datasets/mnist/training-small, /Users/gideon/Development/ProjectAGI/AGIEF/datasets/mnist/testing-small";

//        String trainingPath = "/home/dave/workspace/agi.io/data/mnist/1k_test";
//        String  testingPath = "/home/dave/workspace/agi.io/data/mnist/1k_test";

        String trainingPath = "/home/dave/workspace/agi.io/data/mnist/10k_train";
        String  testingPath = "/home/dave/workspace/agi.io/data/mnist/10k_train,/home/dave/workspace/agi.io/data/mnist/1k_test";

//        String trainingPath = "/home/dave/workspace/agi.io/data/mnist/cycle10";
//        String testingPath = "/home/dave/workspace/agi.io/data/mnist/cycle10";
//        String trainingPath = "/home/dave/workspace/agi.io/data/mnist/cycle3";
//        String testingPath = "/home/dave/workspace/agi.io/data/mnist/cycle3";

//        String trainingPath = "/Users/gideon/Development/ProjectAGI/AGIEF/datasets/mnist/training-small";
//        String testingPath = "/Users/gideon/Development/ProjectAGI/AGIEF/datasets/mnist/testing-small";

        // Parameters
//        int flushInterval = 20;
//        int flushInterval = -1; // never flush
//        String flushWriteFilePath = "/home/dave/workspace/agi.io/data/flush";
//        String flushWriteFilePrefixTruth = "flushedTruth";
//        String flushWriteFilePrefixFeatures = "flushedFeatures";

        boolean logDuringTraining = false;
        boolean debug = false;
//        boolean logDuringTraining = false;
        boolean cacheAllData = true;
        boolean terminateByAge = false;
        int terminationAge = 1000;//50000;//25000;
//        int trainingEpochs = 250;//20; // = 5 * 10 images * 30 repeats = 1500      30*10*30 =
//        int trainingEpochs = 50;//20; // = 5 * 10 images * 30 repeats = 1500      30*10*30 =
        int trainingEpochs = 1;//20; // = 5 * 10 images * 30 repeats = 1500      30*10*30 =
        int testingEpochs = 1; // = 1 * 10 images * 30 repeats = 300
        int imageRepeats = 30; // paper - 30
//        int imagesPerEpoch = 10;

        // Entity names
        String experimentName           = PersistenceUtil.GetEntityName( "experiment" );
        String imageLabelName           = PersistenceUtil.GetEntityName( "image-class" );
        String vectorSeriesName         = PersistenceUtil.GetEntityName( "feature-series" );
        String valueSeriesName          = PersistenceUtil.GetEntityName( "label-series" );

        // Algorithm
        String dogPosName               = PersistenceUtil.GetEntityName( "dog-pos" );
        String dogNegName               = PersistenceUtil.GetEntityName( "dog-neg" );
        String normPosName              = PersistenceUtil.GetEntityName( "norm-pos" );
        String normNegName              = PersistenceUtil.GetEntityName( "norm-neg" );
        String spikeEncoderName         = PersistenceUtil.GetEntityName( "spike-encoder" );
        String spikingConvolutionalName = PersistenceUtil.GetEntityName( "stdp-cnn" );

        // Create entities
        String parentName = null;
        parentName = PersistenceUtil.CreateEntity( experimentName, ExperimentEntity.ENTITY_TYPE, n.getName(), null ); // experiment is the root entity
        parentName = PersistenceUtil.CreateEntity( imageLabelName, ImageLabelEntity.ENTITY_TYPE, n.getName(), parentName );

        parentName = PersistenceUtil.CreateEntity( dogPosName, DifferenceOfGaussiansEntity.ENTITY_TYPE, n.getName(), parentName );
        parentName = PersistenceUtil.CreateEntity( dogNegName, DifferenceOfGaussiansEntity.ENTITY_TYPE, n.getName(), parentName );
        parentName = PersistenceUtil.CreateEntity( normPosName, LocalNormalizationEntity.ENTITY_TYPE, n.getName(), parentName );
        parentName = PersistenceUtil.CreateEntity( normNegName, LocalNormalizationEntity.ENTITY_TYPE, n.getName(), parentName );
        parentName = PersistenceUtil.CreateEntity( spikeEncoderName, ConvolutionalSpikeEncoderEntity.ENTITY_TYPE, n.getName(), parentName );
        parentName = PersistenceUtil.CreateEntity( spikingConvolutionalName, SpikingConvolutionalNetworkEntity.ENTITY_TYPE, n.getName(), parentName );

        parentName = PersistenceUtil.CreateEntity( vectorSeriesName, VectorSeriesEntity.ENTITY_TYPE, n.getName(), parentName ); // 2nd, class region updates after first to get its feedback
        parentName = PersistenceUtil.CreateEntity( valueSeriesName, ValueSeriesEntity.ENTITY_TYPE, n.getName(), parentName ); // 2nd, class region updates after first to get its feedback

        // Connect the entities' data
        DataRefUtil.SetDataReference( dogPosName, DifferenceOfGaussiansEntity.DATA_INPUT, imageLabelName, ImageLabelEntity.OUTPUT_IMAGE );
        DataRefUtil.SetDataReference( dogNegName, DifferenceOfGaussiansEntity.DATA_INPUT, imageLabelName, ImageLabelEntity.OUTPUT_IMAGE );
        DataRefUtil.SetDataReference( normPosName, LocalNormalizationEntity.DATA_INPUT, dogPosName, DifferenceOfGaussiansEntity.DATA_OUTPUT );
        DataRefUtil.SetDataReference( normNegName, LocalNormalizationEntity.DATA_INPUT, dogNegName, DifferenceOfGaussiansEntity.DATA_OUTPUT );

        DataRefUtil.SetDataReference( spikeEncoderName, ConvolutionalSpikeEncoderEntity.DATA_INPUT_POS, normPosName, LocalNormalizationEntity.DATA_OUTPUT );
//        DataRefUtil.SetDataReference( spikeEncoderName, ConvolutionalSpikeEncoderEntity.DATA_INPUT_NEG, dogNegName, DifferenceOfGaussiansEntity.DATA_OUTPUT );

        // a) Image to image region, and decode
        DataRefUtil.SetDataReference( spikingConvolutionalName, SpikingConvolutionalNetworkEntity.DATA_INPUT, spikeEncoderName, ConvolutionalSpikeEncoderEntity.DATA_OUTPUT );

        ArrayList< AbstractPair< String, String > > featureDatas = new ArrayList<>();
        featureDatas.add( new AbstractPair<>( spikingConvolutionalName, SpikingConvolutionalNetworkEntity.DATA_OUTPUT ) );
//        featureDatas.add( new AbstractPair<>( spikingConvolutionalName, SpikingConvolutionalNetworkEntity.DATA_LAYER_POOL_INTEGRATED_ + "1" ) );
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

        float stdDev1 = 1f;
        float stdDev2 = 2f;
        int kernelSize = 7;
//        SetDoGEntityConfig( dogPosName, stdDev1, stdDev2, kernelSize );//, 1.0f );
//        SetDoGEntityConfig( dogNegName, stdDev2, stdDev1, kernelSize );//, 1.0f );
        DifferenceOfGaussiansEntityConfig.Set( dogPosName, stdDev1, stdDev2, kernelSize, 1.0f, 0f, 1000f, 0.0f, 1.0f );
        DifferenceOfGaussiansEntityConfig.Set( dogNegName, stdDev2, stdDev1, kernelSize, 1.0f, 0f, 1000f, 0.0f, 1.0f );

        int radius = 10;
        LocalNormalizationEntityConfig.Set( normPosName, radius );
        LocalNormalizationEntityConfig.Set( normNegName, radius );

//        float spikeThreshold = 5.0f;
//        float spikeDensity = 0.05f; // 5% per step; 1/30 = 0.03. But some will be zero.
        float spikeDensity = 1f / (float)imageRepeats;
        String clearFlagEntityName = imageLabelName;
        String clearFlagConfigPath = "imageChanged";
        SetSpikeEncoderEntityConfig( spikeEncoderName, spikeDensity, clearFlagEntityName, clearFlagConfigPath );

        // cache all data for speed, when enabled
        PersistenceUtil.SetConfig( experimentName, "cache", String.valueOf( cacheAllData ) );
        PersistenceUtil.SetConfig( imageLabelName, "cache", String.valueOf( cacheAllData ) );
        PersistenceUtil.SetConfig( spikeEncoderName, "cache", String.valueOf( cacheAllData ) );
        PersistenceUtil.SetConfig( spikingConvolutionalName, "cache", String.valueOf( cacheAllData ) );
        PersistenceUtil.SetConfig( vectorSeriesName, "cache", String.valueOf( cacheAllData ) );
        PersistenceUtil.SetConfig( valueSeriesName, "cache", String.valueOf( cacheAllData ) );


        // MNIST config
        String trainingEntities = spikingConvolutionalName;
        String testingEntities = "";
        if( logDuringTraining ) {
            trainingEntities += "," + vectorSeriesName + "," + valueSeriesName;
        }
        testingEntities = vectorSeriesName + "," + valueSeriesName;
        SetImageLabelEntityConfig( imageLabelName, trainingPath, testingPath, trainingEpochs, testingEpochs, imageRepeats, trainingEntities, testingEntities );


        // Algorithm config
        int inputWidth = 28;
        int inputHeight = 28;
//        int inputDepth = 2;
        int inputDepth = 1;

        SetSpikingConvolutionalEntityConfig(
                spikingConvolutionalName, clearFlagEntityName, clearFlagConfigPath,
                inputWidth, inputHeight, inputDepth,
                imageRepeats );


        // LOGGING config
        // NOTE about logging: We accumulate the labels and features for all images, but then we only append a new sample of (features,label) every N steps
        // This timing corresponds with the change from one image to another. In essence we allow the network to respond to the image for a few steps, while recording its output
        int accumulatePeriod = imageRepeats;
        int period = -1;
//        VectorSeriesEntityConfig.Set( vectorSeriesName, accumulatePeriod, period, ModelData.ENCODING_SPARSE_REAL );
        VectorSeriesEntityConfig.Set( vectorSeriesName, accumulatePeriod, period, DataJsonSerializer.ENCODING_DENSE );

        // Log image label for each set of features
        String valueSeriesInputEntityName = imageLabelName;
        String valueSeriesInputConfigPath = "imageLabel";
        String valueSeriesInputDataName = "";
        int inputDataOffset = 0;
        float accumulateFactor = 1f / imageRepeats;
        ValueSeriesEntityConfig.Set( valueSeriesName, accumulatePeriod, accumulateFactor, -1, period, valueSeriesInputEntityName, valueSeriesInputConfigPath, valueSeriesInputDataName, inputDataOffset );
        // LOGGING

        // Debug the algorithm
        if( debug == false ) {
            return; // we're done
        }

        // Debug the kernel gain controllers
        accumulatePeriod = 100 * imageRepeats;//1;//dataRepeatPeriod * imageRepeats * 2;
        String kernelGainsSeriesName = PersistenceUtil.GetEntityName( "kernel-gains-series" );
        parentName = PersistenceUtil.CreateEntity( kernelGainsSeriesName, VectorSeriesEntity.ENTITY_TYPE, n.getName(), parentName );
        DataRefUtil.SetDataReference( kernelGainsSeriesName, VectorSeriesEntity.INPUT, spikingConvolutionalName, SpikingConvolutionalNetworkEntity.DATA_LAYER_KERNEL_GAINS_ + "0" );
        VectorSeriesEntityConfig.Set( kernelGainsSeriesName, accumulatePeriod, period, DataJsonSerializer.ENCODING_DENSE );

        // Debug the spike threshold controller
        period = imageRepeats * 15;
        accumulatePeriod = 1;

        int controllerPeriod = -1;
        parentName = CreateControllerLogs( n, parentName, spikingConvolutionalName, accumulatePeriod, controllerPeriod );

        String encSeriesName = PersistenceUtil.GetEntityName( "enc-series" );
        parentName = PersistenceUtil.CreateEntity( encSeriesName, VectorSeriesEntity.ENTITY_TYPE, n.getName(), parentName );
        DataRefUtil.SetDataReference( encSeriesName, VectorSeriesEntity.INPUT, spikeEncoderName, ConvolutionalSpikeEncoderEntity.DATA_OUTPUT );
        VectorSeriesEntityConfig.Set( encSeriesName, accumulatePeriod, period, DataJsonSerializer.ENCODING_SPARSE_REAL );

        String netInh1SeriesName = PersistenceUtil.GetEntityName( "net-inh-1-series" );
        String netInt1SeriesName = PersistenceUtil.GetEntityName( "net-int-1-series" );
        String netSpk1SeriesName = PersistenceUtil.GetEntityName( "net-spk-1-series" );

        String netInh2SeriesName = PersistenceUtil.GetEntityName( "net-inh-2-series" );
        String netInt2SeriesName = PersistenceUtil.GetEntityName( "net-int-2-series" );
        String netSpk2SeriesName = PersistenceUtil.GetEntityName( "net-spk-2-series" );

        parentName = PersistenceUtil.CreateEntity( netInh1SeriesName, VectorSeriesEntity.ENTITY_TYPE, n.getName(), parentName );
        parentName = PersistenceUtil.CreateEntity( netInt1SeriesName, VectorSeriesEntity.ENTITY_TYPE, n.getName(), parentName );
        parentName = PersistenceUtil.CreateEntity( netSpk1SeriesName, VectorSeriesEntity.ENTITY_TYPE, n.getName(), parentName );

        parentName = PersistenceUtil.CreateEntity( netInh2SeriesName, VectorSeriesEntity.ENTITY_TYPE, n.getName(), parentName );
        parentName = PersistenceUtil.CreateEntity( netInt2SeriesName, VectorSeriesEntity.ENTITY_TYPE, n.getName(), parentName );
        parentName = PersistenceUtil.CreateEntity( netSpk2SeriesName, VectorSeriesEntity.ENTITY_TYPE, n.getName(), parentName );

        String layer = "0";
        DataRefUtil.SetDataReference( netInh1SeriesName, VectorSeriesEntity.INPUT, spikingConvolutionalName, SpikingConvolutionalNetworkEntity.DATA_LAYER_POOL_INHIBITION_ + layer );
        DataRefUtil.SetDataReference( netInt1SeriesName, VectorSeriesEntity.INPUT, spikingConvolutionalName, SpikingConvolutionalNetworkEntity.DATA_LAYER_CONV_INTEGRATED_ + layer );
//        DataRefUtil.SetDataReference( netSpk1SeriesName, VectorSeriesEntity.INPUT, spikingConvolutionalName, SpikingConvolutionalNetworkEntity.DATA_LAYER_CONV_SPIKES_ + layer );
        DataRefUtil.SetDataReference( netSpk1SeriesName, VectorSeriesEntity.INPUT, spikingConvolutionalName, SpikingConvolutionalNetworkEntity.DATA_LAYER_POOL_SPIKES_INTEGRATED_ + layer );

        layer = "1";
//        DataRefUtil.SetDataReference( netInh2SeriesName, VectorSeriesEntity.INPUT, spikingConvolutionalName, SpikingConvolutionalNetworkEntity.DATA_LAYER_CONV_INHIBITION_ + layer );
        DataRefUtil.SetDataReference( netInh2SeriesName, VectorSeriesEntity.INPUT, spikingConvolutionalName, SpikingConvolutionalNetworkEntity.DATA_LAYER_POOL_INHIBITION_ + layer );
        DataRefUtil.SetDataReference( netInt2SeriesName, VectorSeriesEntity.INPUT, spikingConvolutionalName, SpikingConvolutionalNetworkEntity.DATA_LAYER_CONV_INTEGRATED_ + layer );
//        DataRefUtil.SetDataReference( netSpk2SeriesName, VectorSeriesEntity.INPUT, spikingConvolutionalName, SpikingConvolutionalNetworkEntity.DATA_LAYER_CONV_SPIKES_ + layer );
        DataRefUtil.SetDataReference( netSpk2SeriesName, VectorSeriesEntity.INPUT, spikingConvolutionalName, SpikingConvolutionalNetworkEntity.DATA_LAYER_POOL_SPIKES_INTEGRATED_ + layer );

        VectorSeriesEntityConfig.Set( netInh1SeriesName, accumulatePeriod, period, DataJsonSerializer.ENCODING_SPARSE_REAL );
        VectorSeriesEntityConfig.Set( netInt1SeriesName, accumulatePeriod, period, DataJsonSerializer.ENCODING_SPARSE_REAL );
        VectorSeriesEntityConfig.Set( netSpk1SeriesName, accumulatePeriod, period, DataJsonSerializer.ENCODING_SPARSE_REAL );

        VectorSeriesEntityConfig.Set( netInh2SeriesName, accumulatePeriod, period, DataJsonSerializer.ENCODING_SPARSE_REAL );
        VectorSeriesEntityConfig.Set( netInt2SeriesName, accumulatePeriod, period, DataJsonSerializer.ENCODING_SPARSE_REAL );
        VectorSeriesEntityConfig.Set( netSpk2SeriesName, accumulatePeriod, period, DataJsonSerializer.ENCODING_SPARSE_REAL );
    }

    protected static String CreateControllerLogs( Node n, String parentName, String spikingConvolutionalName, int accumulatePeriod, int controllerPeriod ) {

        String controllerInputSeriesName = PersistenceUtil.GetEntityName( "controller-input" );
        parentName = PersistenceUtil.CreateEntity( controllerInputSeriesName, ValueSeriesEntity.ENTITY_TYPE, n.getName(), parentName );
        ValueSeriesEntityConfig.Set( controllerInputSeriesName, accumulatePeriod, 1.f, -1, controllerPeriod, spikingConvolutionalName, "controllerInput", null, 0 );

        String controllerInputAccumulatedSeriesName = PersistenceUtil.GetEntityName( "controller-input-accumulated" );
        parentName = PersistenceUtil.CreateEntity( controllerInputAccumulatedSeriesName, ValueSeriesEntity.ENTITY_TYPE, n.getName(), parentName );
        ValueSeriesEntityConfig.Set( controllerInputAccumulatedSeriesName, accumulatePeriod, 1.f, -1, controllerPeriod, spikingConvolutionalName, "controllerInputAccumulated", null, 0 );

        String controllerErrIntSeriesName = PersistenceUtil.GetEntityName( "controller-error-integrated" );
        parentName = PersistenceUtil.CreateEntity( controllerErrIntSeriesName, ValueSeriesEntity.ENTITY_TYPE, n.getName(), parentName );
        ValueSeriesEntityConfig.Set( controllerErrIntSeriesName, accumulatePeriod, 1.f, -1, controllerPeriod, spikingConvolutionalName, "controllerErrorIntegrated", null, 0 );

        String controllerOutputSeriesName = PersistenceUtil.GetEntityName( "controller-output" );
        parentName = PersistenceUtil.CreateEntity( controllerOutputSeriesName, ValueSeriesEntity.ENTITY_TYPE, n.getName(), parentName );
        ValueSeriesEntityConfig.Set( controllerOutputSeriesName, accumulatePeriod, 1.f, -1, controllerPeriod, spikingConvolutionalName, "controllerOutput", null, 0 );

        String controllerErrorSeriesName = PersistenceUtil.GetEntityName( "controller-error" );
        parentName = PersistenceUtil.CreateEntity( controllerErrorSeriesName, ValueSeriesEntity.ENTITY_TYPE, n.getName(), parentName );
        ValueSeriesEntityConfig.Set( controllerErrorSeriesName, accumulatePeriod, 1.f, -1, controllerPeriod, spikingConvolutionalName, "controllerError", null, 0 );

        String controllerThresholdSeriesName = PersistenceUtil.GetEntityName( "controller-threshold" );
        parentName = PersistenceUtil.CreateEntity( controllerThresholdSeriesName, ValueSeriesEntity.ENTITY_TYPE, n.getName(), parentName );
        ValueSeriesEntityConfig.Set( controllerThresholdSeriesName, accumulatePeriod, 1.f, -1, controllerPeriod, spikingConvolutionalName, "controllerThreshold", null, 0 );

        return parentName;
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

        PersistenceUtil.SetConfig( entityName, entityConfig );
    }

    protected static void SetSpikeEncoderEntityConfig( String entityName, float spikeDensity, String clearFlagEntityName, String clearFlagConfigPath ) {

        ConvolutionalSpikeEncoderEntityConfig entityConfig = new ConvolutionalSpikeEncoderEntityConfig();
        entityConfig.cache = true;
//        entityConfig.spikeThreshold = spikeThreshold;
        entityConfig.spikeDensity = spikeDensity;
        entityConfig.clearFlagEntityName = clearFlagEntityName;
        entityConfig.clearFlagConfigPath = clearFlagConfigPath;

        PersistenceUtil.SetConfig( entityName, entityConfig );
    }

    protected static void SetSpikingConvolutionalEntityConfig(
            String entityName,
            String clearFlagEntityName,
            String clearFlagConfigPath,
            int inputWidth,
            int inputHeight,
            int inputDepth,
            int imageRepeats ) {

        SpikingConvolutionalNetworkEntityConfig entityConfig = new SpikingConvolutionalNetworkEntityConfig();

        entityConfig.cache = true;

        // This flag is used to clear inhibition and accumulated potential on new image.
        entityConfig.clearFlagEntityName = clearFlagEntityName;
        entityConfig.clearFlagConfigPath = clearFlagConfigPath;

        // STDP Learning rule
        // "Synaptic weights of convolutional neurons initiate with random values drown from a normal distribution with the mean of 0.8 and STD of 0.05"
        entityConfig.kernelWeightsStdDev = 0.05f;
        entityConfig.kernelWeightsMean = 0.8f;
        entityConfig.kernelWeightsLearningRate = 0.01f;

        // Kernel homeostasis PI controller
        int dataRepeatPeriod = 10; // over how many data samples do you want to measure the average spike density, whichs gives the P for the PI controller?
//        int layerKernelSpikeFrequencyUpdatePeriod = imageRepeats * dataRepeatPeriod; // how often to update kernel gain
//        float kernelSpikeFrequencyTargetScale = 0.3f; // ie each kernel should try to be used 1/3 of uniform frequency at least

        // check what happens to gain when its above idea frequency. I hope givn the clamp it will simply not change it
        float layerKernelGainDefault = 1.0f; // Default gain value
        float layerKernelGainTargetFactor = 0.2f; // fraction e.g. 0.2= 20% of uniform frequency
        int layerKernelGainUpdatePeriod = dataRepeatPeriod * imageRepeats * 1; // updates controller and proportional value, with latest integral value.
        int layerKernelGainIntegrationPeriod = 5; // how many updatePeriods are integrated to compute the integral term for the PI controller

        // Spike density PI controller
        float layerConvSpikeDensityDefault = 0f; // Default threshold value
        float layerConvSpikeDensityTarget = 0.1f; // What density of spikes do you want per sample? Note, this is BEFORE inhibition, which will sparsen the result.
        int layerConvSpikeUpdatePeriod = dataRepeatPeriod * imageRepeats * 1; // updates controller and proportional value, with latest integral value.
        int layerConvSpikeIntegrationPeriod = 10; // how many updatePeriods are integrated to compute the integral term for the PI controller

//        entityConfig.learningRatePos = 0.0001f;//4f; // from paper
//        entityConfig.learningRateNeg = 0.0003f; // from paper

        // Note on configuring spike frequencies
        // Let's say we want a spike density of K=5% per image.
        // That means if we have 100 cells, then we will have 5 spikes.
        // Since we have N=30 repeats, to get those 5 spikes we actually need a per-update frequency of
        // K/N = 0.05 / 30 = 0.001666667 spikes per update.
        // This is the target frequency
        //maybe I wanna make them fire more easily, but suffer from inhibition to sparsen? Yes (current thinking)

        entityConfig.nbrLayers = 2;//3;

//        int[] layerDepths = { 28,28 }; // reduce for speed
        int[] layerDepths = { 30,100 }; // from paper
        int[] layerPoolingSize = { 2,8 }; // for classification in Z
//        int[] layerPoolingSize = { 2,2 }; // for reconstruction, reduce pooling in 2nd layer
        int[] layerFieldSize = { 5,5 };
        int[] layerInputPaddings = { 0,0 };

        int iw = inputWidth;
        int ih = inputHeight;
        int id = inputDepth;

        // Generate config properties from these values:
        for( int layer = 0; layer < entityConfig.nbrLayers; ++layer ) {

            // Geometry of layer
            String prefix = "";
            if( layer > 0 ) prefix = ",";

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

            // Kernel training parameters
            // Duty cycle per kernel. The rate is averaged over the whole area in X and Y. What we want to define is how
            // often the kernels are used across Z and T (time). This also depends on how many models we have.
            // Also it only really matters the relative gain for each kernel, because the threshold adapts on a per-layer
            // basis to control the spike density.
            //float timeScaling = 1.f / (float)imageRepeats; // i.e. 1 if 1 repeat per image, or 0.5 if 2 repeats/image
            float uniform = 1.f / (float)ld; // uniform
            //float area = lw * lh;
            //float areaNorm = 1f / area;
//            float layerKernelGainTarget = uniform * areaNorm * layerKernelGainTargetFactor;
            float layerKernelGainTarget = ( uniform * layerKernelGainTargetFactor ) / imageRepeats;
//            float layerKernelSpikeFrequencyLearningRate = 0.0001f; // Learn very slowly
//            float layerKernelSpikeFrequencyTarget = zScaling * kernelSpikeFrequencyTargetScale; // how often each kernel should fire, as a measure of average density (spikes per unit area).
//
//            entityConfig.layerKernelSpikeFrequencyLearningRate += prefix + layerKernelSpikeFrequencyLearningRate;
//            entityConfig.layerKernelSpikeFrequencyUpdatePeriod += prefix + layerKernelSpikeFrequencyUpdatePeriod;
//            entityConfig.layerKernelSpikeFrequencyTarget += prefix + layerKernelSpikeFrequencyTarget;

            entityConfig.layerKernelSpikeDefault += prefix + layerKernelGainDefault;
            entityConfig.layerKernelSpikeTarget += prefix + layerKernelGainTarget;
            entityConfig.layerKernelSpikeIntegrationPeriod += prefix + layerKernelGainIntegrationPeriod;
            entityConfig.layerKernelSpikeUpdatePeriod += prefix + layerKernelGainUpdatePeriod;

            // Spike Learning and training parameters, time constants and spike density
            entityConfig.layerConvSpikeDefault += prefix + layerConvSpikeDensityDefault;
            entityConfig.layerConvSpikeTarget += prefix + layerConvSpikeDensityTarget;
            entityConfig.layerConvSpikeIntegrationPeriod += prefix + layerConvSpikeIntegrationPeriod;
            entityConfig.layerConvSpikeUpdatePeriod += prefix + layerConvSpikeUpdatePeriod;

            // Geometric parameters:
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

            // Auto calculate layer widths and heights
            iw = lw / pw;
            ih = lh / ph;
            id = ld;
        }

        PersistenceUtil.SetConfig( entityName, entityConfig );
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
    //  F1 -- -- -- -- --                                                                      
    //  F2    -- -- -- -- --                                                             
    //  F3       -- -- -- -- --                                                    
    //  F4          -- -- -- -- --                                           
    //  F5             -- -- -- -- --                                  
    //  F6                -- -- -- -- --                         
    //  F7                   -- -- -- -- --
    //  F8                      -- -- -- -- --
    //  F9                         -- -- -- -- --
    //  F10                           -- -- -- -- --
    //  F11                              -- -- -- -- --
    //  F12                                 -- -- -- -- --
    //  F13                                    -- -- -- -- --
    //  F14                                       -- -- -- -- --
    //  F15                                          -- -- -- -- --
    //  F16                                             -- -- -- -- --
    //  F17                                                -- -- -- -- --
    //  F18                                                   -- -- -- -- --
    //  F19                                                      -- -- -- -- --
    //  F20                                                         -- -- -- -- --
    //  F21                                                            -- -- -- -- --
    //  F22                                                               -- -- -- -- --
    //  F23                                                                  -- -- -- -- --
    //  F24                                                                     -- -- -- -- --
    //     00 01 02 03 04 05 06 07 08 09 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 |

    // Layer 1 24x24 cells with 2x2 pooling brings it to 12x12 input to layer 2.

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
