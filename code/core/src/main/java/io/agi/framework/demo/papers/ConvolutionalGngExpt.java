/*
 * Copyright (c) 2018.
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
import io.agi.framework.Naming;
import io.agi.framework.Node;
import io.agi.framework.demo.CreateEntityMain;
import io.agi.framework.demo.mnist.ImageLabelEntity;
import io.agi.framework.demo.mnist.ImageLabelEntityConfig;
import io.agi.framework.entities.DataFileEntity;
import io.agi.framework.entities.DataFileEntityConfig;
import io.agi.framework.entities.ExperimentEntity;
import io.agi.framework.entities.convolutional.*;
import io.agi.framework.persistence.DataJsonSerializer;
import io.agi.framework.persistence.PersistenceUtil;
import io.agi.framework.references.DataRefUtil;

import java.io.File;
import java.util.ArrayList;

/**
 * 3-layer Convolutional GNG model
 *
 * 50% - sparse binary, 10k
 *
 * 67% - real, faster training, 10k
 *
 * 80% - 60/10k
 *
 * Try producing reverse-rank-output rather than variable error - then make it a nonlinear ranking to disregard
 * misrankings in the hidden layer. Only the most significant rankings will count.
 *
 * 88% - 10k/1k with rank-relative output. This probably could go into the mid-90s with more training time.
 *
 * 1 epoch training:
 * Errors: 3310 of 60000 = 94.48334% correct.
 * Errors: 647 of 10000 = 93.53% correct.
 *
 *
 * Created by dave on 12/08/17.
 */
public class ConvolutionalGngExpt extends CreateEntityMain {

    public static void main( String[] args ) {
        ConvolutionalGngExpt expt = new ConvolutionalGngExpt();
        expt.mainImpl(args );
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
//        String testingPath = "/home/dave/workspace/agi.io/data/mnist/10k_train,/home/dave/workspace/agi.io/data/mnist/1k_test";
//        String testingPath = "/home/dave/workspace/agi.io/data/mnist/10k_train,/home/dave/workspace/agi.io/data/mnist/1k_test";
//        String testingPath = "/home/dave/workspace/agi.io/data/mnist/cycle10";
        String testingPath = "/home/dave/workspace/agi.io/data/mnist/all/all_train,/home/dave/workspace/agi.io/data/mnist/all/all_t10k";
        return testingPath;
    }

    public static String getOutputPath() {
        return "/home/dave/Desktop/agi/data/conv_gng";
    }

    public void createEntities( Node n ) {

        // Dataset
        String trainingPath = getTrainingPath();
        String testingPath = getTestingPath();

        String fileNameWriteFeatures = getOutputPath() + File.separator + "features.csv";
        String fileNameWriteLabels = getOutputPath() + File.separator + "labels.csv";

        // Parameters
        boolean logDuringTraining = false;
        boolean debug = false;
//        boolean logDuringTraining = false;
        boolean cacheAllData = true;
        boolean terminateByAge = false;
        int terminationAge = -1;
        int trainingEpochs = 1; // = 5 * 10 images * 30 repeats = 1500      30*10*30 =
        int testingEpochs = 1; // = 1 * 10 images * 30 repeats = 300

        // Entity names
        String experimentName           = Naming.GetEntityName( "experiment" );
        String imageLabelName           = Naming.GetEntityName( "image-class" );
        String featureSeriesName        = Naming.GetEntityName( "feature-series" );
        String labelSeriesName          = Naming.GetEntityName( "label-series" );

        // Algorithm
        String convolutionalName = Naming.GetEntityName( "cnn" );

        // Create entities
        String parentName = null;
        parentName = PersistenceUtil.CreateEntity( experimentName, ExperimentEntity.ENTITY_TYPE, n.getName(), null ); // experiment is the root entity
        parentName = PersistenceUtil.CreateEntity( imageLabelName, ImageLabelEntity.ENTITY_TYPE, n.getName(), parentName );
        parentName = PersistenceUtil.CreateEntity( convolutionalName, CompetitiveLearningConvolutionalNetworkEntity.ENTITY_TYPE, n.getName(), parentName );
        parentName = PersistenceUtil.CreateEntity( featureSeriesName, DataFileEntity.ENTITY_TYPE, n.getName(), parentName ); // 2nd, class region updates after first to get its feedback
        parentName = PersistenceUtil.CreateEntity(   labelSeriesName, DataFileEntity.ENTITY_TYPE, n.getName(), parentName ); // 2nd, class region updates after first to get its feedback

        // Connect the entities' data
        DataRefUtil.SetDataReference( convolutionalName, CompetitiveLearningConvolutionalNetworkEntity.DATA_INPUT, imageLabelName, ImageLabelEntity.OUTPUT_IMAGE );

        ArrayList< AbstractPair< String, String > > featureDatas = new ArrayList<>();
        featureDatas.add( new AbstractPair<>( convolutionalName, CompetitiveLearningConvolutionalNetworkEntity.DATA_OUTPUT ) );

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

        // MNIST config
        String trainingEntities = convolutionalName;
        String testingEntities = "";
        if( logDuringTraining ) {
            trainingEntities += "," + featureSeriesName + "," + labelSeriesName;
        }
        testingEntities = featureSeriesName + "," + labelSeriesName;
        int imageRepeats = 1;
        SetImageLabelEntityConfig( imageLabelName, trainingPath, testingPath, trainingEpochs, testingEpochs, imageRepeats, trainingEntities, testingEntities );

        // Algorithm config
        int inputWidth = 28;
        int inputHeight = 28;
        int inputDepth = 1;

        SetConvolutionalEntityConfig(
                convolutionalName,
                inputWidth, inputHeight, inputDepth );

        // LOGGING config
        boolean write = true;
        boolean read = false;
        boolean append = true;
        String fileNameRead = null;
        DataFileEntityConfig.Set(
                featureSeriesName, cacheAllData, write, read, append, DataJsonSerializer.ENCODING_SPARSE_REAL, fileNameWriteFeatures, fileNameRead );

        // Log image label for each set of features
        DataFileEntityConfig.Set(
                labelSeriesName, cacheAllData, write, read, append, DataJsonSerializer.ENCODING_DENSE, fileNameWriteLabels, fileNameRead );
        // LOGGING config
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

    protected static void SetConvolutionalEntityConfig(
            String entityName,
            int inputWidth,
            int inputHeight,
            int inputDepth ) {

        ConvolutionalNetworkEntityConfig entityConfig = null;
        CompetitiveLearningConvolutionalNetworkEntityConfig ec = new CompetitiveLearningConvolutionalNetworkEntityConfig();

        // Slower, but probably better:
        ec.edgeMaxAge = 500;
        ec.growthInterval = 200;

        // Faster, but not as good?
        ec.edgeMaxAge = 250;
        ec.growthInterval = 20;

        // Fixed parameters
        ec.noiseMagnitude = 0f;
        ec.learningRate = 0.015f;
        ec.learningRateNeighbours = 0.002f;
        ec.stressLearningRate = 0.01f;
        ec.stressSplitLearningRate = 0.5f;
        ec.stressThreshold = 0.f;
        ec.utilityLearningRate = 0.01f;
        ec.utilityThreshold = -1f;

        entityConfig = ec;

        // https://www.tensorflow.org/tutorials/layers
        // Convolutional Layer #1: Applies 32 5x5 filters (extracting 5x5-pixel subregions), with ReLU activation function
        // Pooling Layer #1: Performs max pooling with a 2x2 filter and stride of 2 (which specifies that pooled regions do not overlap)
        // Convolutional Layer #2: Applies 64 5x5 filters, with ReLU activation function
        // Pooling Layer #2: Again, performs max pooling with a 2x2 filter and stride of 2
        // Dense Layer #1: 1,024 neurons, with dropout regularization rate of 0.4 (probability of 0.4 that any given element will be dropped during training)
        // Dense Layer #2 (Logits Layer): 10 neurons, one for each digit target class (0–9).

        int nbrLayers = 2;
        int[] layerDepths = { 64,1024 };
        int[] layerPoolingSize = { 2,2 };
        int[] layerFieldSize = { 5,5 };
        int[] layerInputPaddings = { 0,0 };
        int[] layerInputStrides = { 2,1 };

        int[] layerSizes = { 12,2 };

        // Conv 1
        //     00 01 02 03 04 05 06 07 08 09 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 |
        //  F0 -- -- -- -- --                                                                      |
        //  F1       -- -- -- -- --                                                                |
        //  F2             -- -- -- -- --                                                          |
        //  F3                   -- -- -- -- --                                                    |
        //  F4                         -- -- -- -- --                                              |
        //  F5                               -- -- -- -- --                                        |
        //  F6                                     -- -- -- -- --
        //  F7                                           -- -- -- -- --
        //  F8                                                 -- -- -- -- --
        //  F9                                                       -- -- -- -- --
        // F10                                                             -- -- -- -- --
        // F11                                                                   -- -- -- -- --     so 12
        // = 12

        // = 12 x 12 x 64 = 9216

        // Pool 1
        //     00 01 02 03 04 05 06 07 08 09 10 11  |
        //  F0 -- --
        //  F1       -- --
        //  F2             -- --
        //  F3                   -- --
        //  F4                         -- --
        //  F5                               -- --
        // = 6

        // = 6 x 6 x 64 = 4608

        // Conv 2
        //     00 01 02 03 04 05  |
        //  F0 -- -- -- -- --     |
        //  F1    -- -- -- -- --  |

        // Pool 2
        //     00 01  |
        //  F0 -- --  |

        // = 1 x 1 x 1024


////////////////////////////////////////////
// EXPT 1 OK
//        int nbrLayers = 2;
//        int[] layerDepths = { 8,64 };
//        int[] layerPoolingSize = { 2,2 };
//        int[] layerFieldSize = { 3,3 };
//        int[] layerInputPaddings = { 0,0 };
//        int[] layerInputStrides = { 1,1 };

////////////////////////////////////////////
// EXPT 2
//        int nbrLayers = 1;
//        int[] layerDepths = { 64 };
//        int[] layerPoolingSize = { 2 };
//        int[] layerFieldSize = { 6,6 };
//        int[] layerInputPaddings = { 0 };
//        int[] layerInputStrides = { 3 };

////////////////////////////////////////////
// AD-HOC
/*
        int nbrLayers = 2;

//        int[] layerDepths = { 30,100 }; // from paper
//        int[] layerDepths = { 30,70 }; //
//        int[] layerDepths = { 40,70 }; //
        int[] layerDepths = { 40,120 }; //
//        int[] layerPoolingSize = { 2,2 }; // for classification in Z
//        int[] layerPoolingSize = { 2,8 }; // for classification in Z
        int[] layerPoolingSize = { 2,1 }; // for classification in Z
//        int[] layerPoolingSize = { 2,4 }; // for reconstruction, reduce pooling in 2nd layer
//        int[] layerPoolingSize = { 2,2 }; // for reconstruction, reduce pooling in 2nd layer
        int[] layerFieldSize = { 5,5 };
        int[] layerInputPaddings = { 0,0 };
        int[] layerInputStrides = { 1,1 };

//        int nbrLayers = 3;
//
//        int[] layerDepths = { 30,100,200 }; // from paper
//        int[] layerPoolingSize = { 2,2,1 }; // for classification in Z
//        int[] layerFieldSize = { 5,5,4 };
//        int[] layerInputPaddings = { 0,0,0 };
//        int[] layerInputStrides = { 1,1,1 };

// */
////////////////////////////////////////////

        ConvolutionalNetworkEntityConfig.Set(
            entityConfig,
            inputDepth, nbrLayers,
            layerSizes, layerInputPaddings, layerInputStrides, layerDepths, layerPoolingSize, layerFieldSize );

//        ConvolutionalNetworkEntityConfig.Set(
//                entityConfig,
//                inputWidth, inputHeight, inputDepth, nbrLayers,
//                layerInputPaddings, layerInputStrides, layerDepths, layerPoolingSize, layerFieldSize );

        entityConfig.cache = true;

        PersistenceUtil.SetConfig( entityName, entityConfig );
    }

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

    // Layer 1 pooling: 24x24 cells with 2x2 pooling brings it to 12x12 input to layer 2.

    // Conv layer 2: 12 inputs. Needs 8 cells.
    // iw - kw +1 = 12-5+1 = 8
    //     00 01 02 03 04 05 06 07 08 09 10 11 |
    //  F1 -- -- -- -- --                          input width = 5 * 2 = 10 + 2
    //  F2    -- -- -- -- --
    //  F3       -- -- -- -- --
    //  F4          -- -- -- -- --
    //  F5             -- -- -- -- --
    //  F6                -- -- -- -- --
    //  F7                   -- -- -- -- --
    //  F8                      -- -- -- -- --
    //     00 01 02 03 04 05 06 07 08 09 10 11 |

    // Max pooling layer 2: over all, so output 1x1 by depth.
    // Layer 2 pooling: 8x8 cells with 2x2 pooling brings it to 4x4 input to layer 3.

    // Conv layer 3: 4 inputs. Needs 1 cells.
    //     00 01 02 03 |
    //  F1 -- -- -- --
    //     00 01 02 03 |

    //   C1        P1    C2     i.e. C2 has a 14 pixel span, or about half the 28 pixel image
    //*00 |              _00
    // 01 | |
    //*02 |-|-----|_01   _01
    // 03 | |-----|
    //*04 | |            _02
    // 05   |
    //*06                _03
    // 07
    //*08 |              _04
    // 09 | |
    // 10 |-|-----|_04
    // 11 | |-----|
    // 12 | |
    // 13   |
    // 14
    // 15
    // 16

//    FIELD    LAYER   DEPTH POOL  OUTPUT
//    3x3      26x26   8     2x2   13x13 x8
//    3x3      11x11   64    2x2   6x6   x64 = 2304
//
//    FIELD    LAYER   DEPTH POOL  OUTPUT
//    6x6      8x8     50    2x2   4x4   x 50   = 800


    // 6x6 field
    //     00 01 02 03 04 05 06 07 08 09 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 |
    //  F1 -- -- -- -- -- --
    //  F2          -- -- -- -- -- --
    //  F3                   -- -- -- -- -- --
    //  F4                            -- -- -- -- -- --
    //  F5                                     -- -- -- -- -- --
    //  F6                                              -- -- -- -- -- --
    //  F7                                                       -- -- -- -- -- --
    //  F8                                                                -- -- -- -- -- --


    // Layer 1
    // 3x3 field 26x26 2x2 13x13
    //     00 01 02 03 04 05 06 07 08 09 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 |
    //  F1 -- -- --
    //  F2    -- -- --
    //  F3       -- -- --
    //  F4          -- -- --
    //  F5             -- -- --
    //  F6                -- -- --
    //  F7                   -- -- --
    //  F8                      -- -- --
    //  F9                         -- -- --
    //  F10                           -- -- --
    //  F11                              -- -- --
    //  F12                                 -- -- --
    //  F13                                    -- -- --
    //  F14                                       -- -- --
    //  F15                                          -- -- --
    //  F16                                             -- -- --
    //  F17                                                -- -- --
    //  F18                                                   -- -- --
    //  F19                                                      -- -- --
    //  F20                                                         -- -- --
    //  F21                                                            -- -- --
    //  F22                                                               -- -- --
    //  F23                                                                  -- -- --
    //  F24                                                                     -- -- --
    //  F25                                                                        -- -- --
    //  F26                                                                           -- -- --
    //     00 01 02 03 04 05 06 07 08 09 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 |

    // Layer 2
    // 3x3 field input 13x13 layer 11x11 2x2    output 6x6
    //     00 01 02 03 04 05 06 07 08 09 10 11 12 |
    //  F1 -- -- --
    //  F2    -- -- --
    //  F3       -- -- --
    //  F4          -- -- --
    //  F5             -- -- --
    //  F6                -- -- --
    //  F7                   -- -- --
    //  F8                      -- -- --
    //  F9                         -- -- --
    //  F10                           -- -- --
    //  F11                              -- -- --
    //     00 01 02 03 04 05 06 07 08 09 10 11 12 |





