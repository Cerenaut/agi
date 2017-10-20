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
import io.agi.core.orm.Keys;
import io.agi.framework.Framework;
import io.agi.framework.Node;
import io.agi.framework.demo.CreateEntityMain;
import io.agi.framework.demo.mnist.ImageLabelEntity;
import io.agi.framework.demo.mnist.ImageLabelEntityConfig;
import io.agi.framework.entities.*;
import io.agi.framework.entities.reinforcement_learning.*;
import io.agi.framework.persistence.models.ModelData;

import java.util.ArrayList;

/**
 * Adds a Q-Learning capability to a flattened hierarchy and uses it for classification.
 * This is then used to measure the improvement in unsupervised representation as a result of RL-biased memory formation.
 *
 * First step is to add the Q-Learning to the [flattened] hierarchy to assign Q-values and perform classification via
 * reinforcement learning.
 *
 * Second step is to try enhancing the memory system using the Q-values for classification confusion from RL.
 *
 * Third step is to measure any improvement both in RL classification score and supervised classification score.
 *
 * image --> classifier --> QLearning --> predictedLabel --> classification
 * imageLabel --------------------------------------------->    reward fn
 *
 * If we want to provide a scalar learning rate from the classification reward function what does it mean? We want to
 * say whether a particular cell contributed to the classification? Or whether the set of cells currently active were
 * classifiable?
 *
 * If they were classifiable, we don't want to change
 * If they weren't classifiable, we DO want to learn this input to distinguish it from others. The assumption is that
 * having a perfect model of this input would then be classified correctly. The current cells are the closest to this
 * model.
 *
 * Over many iterations cells that are poorly classified will be highly mutable until they become accurately classified.
 * This is when their learning will stop.
 *
 * The alternative is to target cells individually. Learn the reward values associated with the cells. If cells result
 * in good classifications, don't change them. If they receive bad classifications, learn them.
 *
 * The problem is timing. Need to associate the current sample in the batch with a reward. But the reward isn't known
 * until after the entity has updated.
 *
 * OR do I want to associate individual cells over time?
 *
 * batchSize 64 1x 60k train:
 * Errors: 10407 of 60000 = 82.655% correct.
 * Errors: 1746 of 10000 = 82.54% correct.
 *
 * batchSize 64 4x 60k train:
 * Errors: 6840 of 60000 = 88.6% correct.
 * Errors: 1202 of 10000 = 87.98% correct.
 *
 * Created by dave on 12/08/17.
 */
public class ReinforcedUnsupervisedExpt extends CreateEntityMain {

    public static void main( String[] args ) {
        ReinforcedUnsupervisedExpt expt = new ReinforcedUnsupervisedExpt();
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
//        String testingPath = "/home/dave/workspace/agi.io/data/mnist/1k_test";
//        String testingPath = "/home/dave/workspace/agi.io/data/mnist/10k_train,/home/dave/workspace/agi.io/data/mnist/1k_test";
//        String testingPath = "/home/dave/workspace/agi.io/data/mnist/cycle10";
        String testingPath = "/home/dave/workspace/agi.io/data/mnist/all/all_train,/home/dave/workspace/agi.io/data/mnist/all/all_t10k";
        return testingPath;
    }

    public void createEntities( Node n ) {

        // Dataset
        String trainingPath = getTrainingPath();
        String testingPath = getTestingPath();

        // Parameters
        boolean logDuringTraining = false;
        boolean debug = false;
        boolean terminateByAge = false;
        int terminationAge = 1000;//50000;//25000;
        int trainingEpochs = 4; // = 5 * 10 images * 30 repeats = 1500      30*10*30 =
        int testingEpochs = 1; // = 1 * 10 images * 30 repeats = 300
        int imageLabels = 10;

        // Entity names
        String experimentName           = Framework.GetEntityName( "experiment" );
        String imageLabelName           = Framework.GetEntityName( "image-class" );
        String featureSeriesName        = Framework.GetEntityName( "feature-series" );
        String labelSeriesName          = Framework.GetEntityName( "label-series" );
        String rewardSeriesName         = Framework.GetEntityName( "reward-series" );

        // Algorithm
        String classifierName = Framework.GetEntityName( "cnn" );
        String problemName = Framework.GetEntityName( "problem" );
        String reinforcementName = Framework.GetEntityName( "ql" );
        String policyName = Framework.GetEntityName( "policy" );
        String reward2LearningName = Framework.GetEntityName( "reward-2-learning-rate" );

        // Create entities
        String parentName = null;
        parentName = Framework.CreateEntity( experimentName, ExperimentEntity.ENTITY_TYPE, n.getName(), null ); // experiment is the root entity
        parentName = Framework.CreateEntity( imageLabelName, ImageLabelEntity.ENTITY_TYPE, n.getName(), parentName );

        // Representation
        parentName = Framework.CreateEntity( classifierName, BiasedSparseAutoencoderEntity.ENTITY_TYPE, n.getName(), parentName );

        // Reinforcement Learning
        parentName = Framework.CreateEntity( reinforcementName, QLearningEntity.ENTITY_TYPE, n.getName(), parentName );
        parentName = Framework.CreateEntity( policyName, EpsilonGreedyEntity.ENTITY_TYPE, n.getName(), parentName ); // select actions given
        parentName = Framework.CreateEntity( problemName, VectorProblemEntity.ENTITY_TYPE, n.getName(), parentName ); // update reward

        parentName = Framework.CreateEntity( reward2LearningName, Reward2LearningRateEntity.ENTITY_TYPE, n.getName(), parentName ); // update reward

        // Logging
        parentName = Framework.CreateEntity( featureSeriesName, VectorSeriesEntity.ENTITY_TYPE, n.getName(), parentName ); // 2nd, class region updates after first to get its feedback
        parentName = Framework.CreateEntity( labelSeriesName, ValueSeriesEntity.ENTITY_TYPE, n.getName(), parentName ); // 2nd, class region updates after first to get its feedback
        parentName = Framework.CreateEntity( rewardSeriesName, ValueSeriesEntity.ENTITY_TYPE, n.getName(), parentName ); // 2nd, class region updates after first to get its feedback

        // Connect the entities' data
        // Input image --> Algo
        Framework.SetDataReference( classifierName, BiasedSparseAutoencoderEntity.INPUT, imageLabelName, ImageLabelEntity.OUTPUT_IMAGE );

        // Algo --> logging for offline classifier after training
        ArrayList< AbstractPair< String, String > > featureDatas = new ArrayList<>();
        featureDatas.add( new AbstractPair<>( classifierName, BiasedSparseAutoencoderEntity.SPIKES ) );
        Framework.SetDataReferences( featureSeriesName, VectorSeriesEntity.INPUT, featureDatas ); // get current state from the region to be used to predict

        // Reinforcement learning
        String statesEntityName = classifierName;
        String statesDataName = BiasedSparseAutoencoderEntity.SPIKES;

        // Update Q-Learning first to generate action quality. Q-Learning needs latest reward, and latest state, plus OLD actions that caused this state.
        Framework.SetDataReference ( reinforcementName, QLearningEntity.INPUT_STATES_NEW, statesEntityName, statesDataName );
        Framework.SetDataReference ( reinforcementName, QLearningEntity.INPUT_ACTIONS_OLD, policyName, EpsilonGreedyEntity.OUTPUT_ACTIONS );
        Framework.SetDataReference ( reinforcementName, QLearningEntity.INPUT_REWARD_NEW, problemName, VectorProblemEntity.OUTPUT_REWARD );

        // Update the Policy next. The policy chooses the action to take given the latest state and the resultant quality of the various actions
        Framework.SetDataReference ( policyName, EpsilonGreedyEntity.INPUT_STATES_NEW, statesEntityName, statesDataName );
        Framework.SetDataReference ( policyName, EpsilonGreedyEntity.INPUT_ACTIONS_QUALITY, reinforcementName, QLearningEntity.OUTPUT_ACTIONS_QUALITY );

        // Finally update the problem to calculate the reward given the chosen action. In this case, we compare the output
        // of the Policy to the correct classification. The output label from the ImageLabelEntity has been configured to be a 1-hot vector
        // rather than a scalar with a value. This means we can compare them directly.
        Framework.SetDataReference ( problemName, VectorProblemEntity.INPUT_ACTIONS, policyName, EpsilonGreedyEntity.OUTPUT_ACTIONS );
        Framework.SetDataReference ( problemName, VectorProblemEntity.INPUT_ACTIONS_IDEAL, imageLabelName, ImageLabelEntity.OUTPUT_LABEL );

        // The reward output is 1 if the classification was correct, which means the error was zero. If the classification was wrong,
        // the reward is 0. What we need to do is tie it to the biased autoencoder as the learning rate.
        Framework.SetDataReference ( reward2LearningName, Reward2LearningRateEntity.INPUT_REWARD, problemName, VectorProblemEntity.OUTPUT_REWARD );
        Framework.SetDataReference ( classifierName, BiasedSparseAutoencoderEntity.INPUT_LEARNING_RATE, reward2LearningName, Reward2LearningRateEntity.OUTPUT_LEARNING_RATE );

        // Experiment config
        if( !terminateByAge ) {
            Framework.SetConfig( experimentName, "terminationEntityName", imageLabelName );
            Framework.SetConfig( experimentName, "terminationConfigPath", "terminate" );
            Framework.SetConfig( experimentName, "terminationAge", "-1" ); // wait for mnist to decide
        }
        else {
            Framework.SetConfig( experimentName, "terminationAge", String.valueOf( terminationAge ) ); // fixed steps
        }

        SetEpsilonGreedyEntityConfig( policyName, imageLabels );

        // MNIST config
        String trainingEntities = classifierName + "," + reinforcementName;
        String testingEntities = "";
        if( logDuringTraining ) {
            trainingEntities += "," + featureSeriesName + "," + labelSeriesName;
        }
        testingEntities = featureSeriesName + "," + labelSeriesName + "," + rewardSeriesName;
        int imageRepeats = 1;
        SetImageLabelEntityConfig( imageLabelName, trainingPath, testingPath, trainingEpochs, testingEpochs, imageRepeats, imageLabels, trainingEntities, testingEntities );

        // Algorithm config
        int widthCells = 32; // from the paper, 32x32= ~1000 was optimal on MNIST (but with a supervised output layer)
        int heightCells = 32;
        int sparsity = 25; // k sparse confirmed err = 1.35%
        int batchSize = 64; // want small for faster training, but large enough to do lifetime sparsity
        int sparsityLifetime = 2;
        float learningRate = 0.01f;
        float momentum = 0.9f; // 0.9 in paper
        float weightsStdDev = 0.01f; // confirmed. Sigma From paper. used at reset

        Framework.SetConfig( classifierName, "learningRate", String.valueOf( learningRate ) );
        Framework.SetConfig( classifierName, "momentum", String.valueOf( momentum ) );
        Framework.SetConfig( classifierName, "widthCells", String.valueOf( widthCells ) );
        Framework.SetConfig( classifierName, "heightCells", String.valueOf( heightCells ) );
        Framework.SetConfig( classifierName, "weightsStdDev", String.valueOf( weightsStdDev ) );
        Framework.SetConfig( classifierName, "sparsity", String.valueOf( sparsity ) );
        Framework.SetConfig( classifierName, "sparsityLifetime", String.valueOf( sparsityLifetime ) );
        Framework.SetConfig( classifierName, "batchSize", String.valueOf( batchSize ) );

        int states = widthCells * heightCells;
        int actions = imageLabels;
        SetQLearningEntityConfig( reinforcementName, states, actions );

        // LOGGING config
        // NOTE about logging: We accumulate the labels and features for all images, but then we only append a new sample of (features,label) every N steps
        // This timing corresponds with the change from one image to another. In essence we allow the network to respond to the image for a few steps, while recording its output
        int accumulatePeriod = imageRepeats;
        int period = -1;
        VectorSeriesEntityConfig.Set( featureSeriesName, accumulatePeriod, period, ModelData.ENCODING_SPARSE_BINARY );

        // Log image label for each set of features
        // We use the config path to get the true labels, not the Data.
        String valueSeriesInputEntityName = imageLabelName;
        String valueSeriesInputConfigPath = "imageLabel";
        String valueSeriesInputDataName = "";
        int inputDataOffset = 0;
        float accumulateFactor = 1f / imageRepeats;
        ValueSeriesEntityConfig.Set( labelSeriesName, accumulatePeriod, accumulateFactor, -1, period, valueSeriesInputEntityName, valueSeriesInputConfigPath, valueSeriesInputDataName, inputDataOffset );

        String rewardSeriesInputEntityName = problemName;
        String rewardSeriesInputDataName = Keys.concatenate( problemName, VectorProblemEntity.OUTPUT_REWARD );
        ValueSeriesEntityConfig.Set( rewardSeriesName, accumulatePeriod, accumulateFactor, -1, period, rewardSeriesInputEntityName, "", rewardSeriesInputDataName, inputDataOffset );
        // LOGGING config

        // cache all data for speed, when enabled. do this last so it's not overriden by a config object
        boolean cacheAllData = true;
        Framework.SetConfig( experimentName, "cache", String.valueOf( cacheAllData ) );
        Framework.SetConfig( imageLabelName, "cache", String.valueOf( cacheAllData ) );
        Framework.SetConfig( classifierName, "cache", String.valueOf( cacheAllData ) );
        Framework.SetConfig( featureSeriesName, "cache", String.valueOf( cacheAllData ) );
        Framework.SetConfig( labelSeriesName, "cache", String.valueOf( cacheAllData ) );
        Framework.SetConfig( rewardSeriesName, "cache", String.valueOf( cacheAllData ) );

        Framework.SetConfig( rewardSeriesName, "learn", String.valueOf( false ) ); // disable reward logging during training
    }

    protected static void SetImageLabelEntityConfig( String entityName, String trainingPath, String testingPath, int trainingEpochs, int testingEpochs, int repeats, int imageLabels, String trainingEntities, String testingEntities ) {

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

        entityConfig.imageLabelUniqueValues = imageLabels;

        Framework.SetConfig( entityName, entityConfig );
    }

    protected static void SetQLearningEntityConfig(
            String entityName,
            int states,
            int actions ) {

        float learningRate = 0.05f; // quite fast?
        float discountRate = 0.f; // because has no impact on future classifications

        QLearningEntityConfig entityConfig = new QLearningEntityConfig();

        entityConfig.cache = true;
        entityConfig.learningRate = learningRate;
        entityConfig.discountRate = discountRate;
        entityConfig.states = states;
        entityConfig.actions = actions;

        Framework.SetConfig( entityName, entityConfig );
    }

    protected static void SetEpsilonGreedyEntityConfig(
            String entityName,
            int labels ) {

        EpsilonGreedyEntityConfig entityConfig = new EpsilonGreedyEntityConfig();
        entityConfig.epsilon = 0.5f;
//        entityConfig.selectionSetSizes = String.valueOf( labels );
        entityConfig.cache = true;

        Framework.SetConfig( entityName, entityConfig );
    }

}
