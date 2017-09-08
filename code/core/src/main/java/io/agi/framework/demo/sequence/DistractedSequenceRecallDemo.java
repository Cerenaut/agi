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

package io.agi.framework.demo.sequence;

import io.agi.core.orm.AbstractPair;
import io.agi.core.util.PropertiesUtil;
import io.agi.framework.Framework;
import io.agi.framework.Main;
import io.agi.framework.Node;
import io.agi.framework.entities.*;
import io.agi.framework.entities.reinforcement_learning.*;
import io.agi.framework.entities.stdp.ConvolutionalSpikeEncoderEntity;
import io.agi.framework.entities.stdp.ConvolutionalSpikeEncoderEntityConfig;
import io.agi.framework.entities.stdp.DifferenceOfGaussiansEntity;
import io.agi.framework.entities.stdp.SpikingConvolutionalNetworkEntity;
import io.agi.framework.factories.CommonEntityFactory;
import io.agi.framework.persistence.models.ModelData;

import java.util.ArrayList;
import java.util.Properties;

/**
 * Created by dave on 19/05/17.
 */
public class DistractedSequenceRecallDemo {

    /**
     * Usage: Expects some arguments. These are:
     * 0: node.properties file
     * 1 to n: 'create' flag and/or 'prefix' flag
     *
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
                String prefix = args[ i + 1 ];
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

        boolean update = false;
        for( int i = 1; i < args.length; ++i ) {
            String arg = args[ i ];
            if( arg.equalsIgnoreCase( "update" ) ) {
                update = true;
            }
        }

        if( update ) {
            int delay = 500;
            Thread t = new Thread() {
                public void run() {
                    try {
                        Thread.sleep( delay );
                    }
                    catch( InterruptedException e ) {
                    }
                    System.err.println( "Requesting Update... " );
                    m._n.doUpdate( "experiment" );
                    System.err.println( "Requested Update. " );
                }
            };
            t.start();
        }

        // Start the system
        m.run();
    }

    public static void createEntities( Node n ) {

        // Define some entities
        String experimentName = Framework.GetEntityName( "experiment" );
        String trainingScheduleName = Framework.GetEntityName( "training-schedule" );
        String stateClassifierName  = Framework.GetEntityName( "state-classifier" );
        String problemName = Framework.GetEntityName( "problem" );
        String qLearningName = Framework.GetEntityName( "q-learning" );
        String policyName = Framework.GetEntityName( "policy" );
        String copyAction0Name = Framework.GetEntityName( "copy-action-0" );
        String copyAction1Name = Framework.GetEntityName( "copy-action-1" );
        String copyAction2Name = Framework.GetEntityName( "copy-action-2" );
        String copyAction3Name = Framework.GetEntityName( "copy-action-3" );
        String copyAction4Name = Framework.GetEntityName( "copy-action-4" );
        String memory1Name = Framework.GetEntityName( "memory-1" );
        String memory2Name = Framework.GetEntityName( "memory-2" );
        String valueSeriesRewardTrainingName = Framework.GetEntityName( "vs-reward-training" );
        String valueSeriesRewardTestingName = Framework.GetEntityName( "vs-reward-testing" );

        String parentName = null;
        parentName = Framework.CreateEntity( experimentName, ExperimentEntity.ENTITY_TYPE, n.getName(), parentName ); // experiment is the root entity
        parentName = Framework.CreateEntity( trainingScheduleName, TrainingScheduleEntity.ENTITY_TYPE, n.getName(), parentName );
        parentName = Framework.CreateEntity( problemName, DistractedSequenceRecallEntity.ENTITY_TYPE, n.getName(), parentName );
        parentName = Framework.CreateEntity( stateClassifierName, GrowingNeuralGasEntity.ENTITY_TYPE, n.getName(), parentName );
        parentName = Framework.CreateEntity( qLearningName, QLearningEntity.ENTITY_TYPE, n.getName(), parentName );
        parentName = Framework.CreateEntity( policyName, EpsilonGreedyEntity.ENTITY_TYPE, n.getName(), parentName );
        parentName = Framework.CreateEntity( valueSeriesRewardTrainingName, ValueSeriesEntity.ENTITY_TYPE, n.getName(), parentName ); // 2nd, class region updates after first to get its feedback
        parentName = Framework.CreateEntity( valueSeriesRewardTestingName, ValueSeriesEntity.ENTITY_TYPE, n.getName(), parentName ); // 2nd, class region updates after first to get its feedback

        parentName = Framework.CreateEntity( copyAction0Name, VectorCopyRangeEntity.ENTITY_TYPE, n.getName(), parentName );
        parentName = Framework.CreateEntity( copyAction1Name, VectorCopyRangeEntity.ENTITY_TYPE, n.getName(), parentName );
        parentName = Framework.CreateEntity( copyAction2Name, VectorCopyRangeEntity.ENTITY_TYPE, n.getName(), parentName );
        parentName = Framework.CreateEntity( copyAction3Name, VectorCopyRangeEntity.ENTITY_TYPE, n.getName(), parentName );
        parentName = Framework.CreateEntity( copyAction4Name, VectorCopyRangeEntity.ENTITY_TYPE, n.getName(), parentName );

        parentName = Framework.CreateEntity( memory1Name, GatedRecurrentMemoryEntity.ENTITY_TYPE, n.getName(), parentName );
        parentName = Framework.CreateEntity( memory2Name, GatedRecurrentMemoryEntity.ENTITY_TYPE, n.getName(), parentName );

        // Connect the entities' data
        // state + memory are input to state classifier
        ArrayList< AbstractPair< String, String > > classifierInputDatas = new ArrayList<>();
        classifierInputDatas.add( new AbstractPair<>( problemName, DistractedSequenceRecallEntity.OUTPUT_STATES ) );
        classifierInputDatas.add( new AbstractPair<>( memory1Name, GatedRecurrentMemoryEntity.OUTPUT_CONTENT ) );
        classifierInputDatas.add( new AbstractPair<>( memory2Name, GatedRecurrentMemoryEntity.OUTPUT_CONTENT ) );
        Framework.SetDataReferences( stateClassifierName, GrowingNeuralGasEntity.INPUT, classifierInputDatas );

        Framework.SetDataReference( qLearningName, QLearningEntity.INPUT_REWARD, problemName, DistractedSequenceRecallEntity.OUTPUT_REWARD );
        Framework.SetDataReference( qLearningName, QLearningEntity.INPUT_STATES_NEW, stateClassifierName, GrowingNeuralGasEntity.OUTPUT_ACTIVE );
        Framework.SetDataReference( qLearningName, QLearningEntity.INPUT_ACTIONS_NEW, policyName, EpsilonGreedyEntity.OUTPUT_ACTIONS );

        Framework.SetDataReference( policyName, EpsilonGreedyEntity.INPUT_STATES_NEW, problemName, DistractedSequenceRecallEntity.OUTPUT_STATES );
        Framework.SetDataReference( policyName, EpsilonGreedyEntity.INPUT_ACTIONS_QUALITY, qLearningName, QLearningEntity.OUTPUT_ACTIONS_QUALITY );

        // unpack actions
        Framework.SetDataReference( copyAction0Name, VectorCopyRangeEntity.INPUT, policyName, EpsilonGreedyEntity.OUTPUT_ACTIONS );
        Framework.SetDataReference( copyAction1Name, VectorCopyRangeEntity.INPUT, policyName, EpsilonGreedyEntity.OUTPUT_ACTIONS );
        Framework.SetDataReference( copyAction2Name, VectorCopyRangeEntity.INPUT, policyName, EpsilonGreedyEntity.OUTPUT_ACTIONS );
        Framework.SetDataReference( copyAction3Name, VectorCopyRangeEntity.INPUT, policyName, EpsilonGreedyEntity.OUTPUT_ACTIONS );
        Framework.SetDataReference( copyAction4Name, VectorCopyRangeEntity.INPUT, policyName, EpsilonGreedyEntity.OUTPUT_ACTIONS );

        // connect actions to memory
        Framework.SetDataReference( memory1Name, GatedRecurrentMemoryEntity.INPUT_CONTENT    , problemName, DistractedSequenceRecallEntity.OUTPUT_STATES );
        Framework.SetDataReference( memory1Name, GatedRecurrentMemoryEntity.INPUT_GATES_WRITE, copyAction1Name, VectorCopyRangeEntity.OUTPUT );
        Framework.SetDataReference( memory1Name, GatedRecurrentMemoryEntity.INPUT_GATES_CLEAR, copyAction2Name, VectorCopyRangeEntity.OUTPUT );

        Framework.SetDataReference( memory2Name, GatedRecurrentMemoryEntity.INPUT_CONTENT    , problemName, DistractedSequenceRecallEntity.OUTPUT_STATES );
        Framework.SetDataReference( memory2Name, GatedRecurrentMemoryEntity.INPUT_GATES_WRITE, copyAction3Name, VectorCopyRangeEntity.OUTPUT );
        Framework.SetDataReference( memory2Name, GatedRecurrentMemoryEntity.INPUT_GATES_CLEAR, copyAction4Name, VectorCopyRangeEntity.OUTPUT );

// TODO flight back: enable training of Q and classifier during testing?
// TODO flight back: observe long term see if it is able to learn the sequence of improvements one by one?


//        Framework.SetDataReference( problemName, DistractedSequenceRecallEntity.INPUT_ACTIONS, policyName, EpsilonGreedyEntity.OUTPUT_ACTIONS_NEW );
        Framework.SetDataReference( problemName, DistractedSequenceRecallEntity.INPUT_ACTIONS, copyAction0Name, VectorCopyRangeEntity.OUTPUT );

        // Configs
        int terminationAge = -1;
        SetExperimentEntityConfig( experimentName, terminationAge, trainingScheduleName, "terminate" );

        int totalEpochs = 10000;
        int trainingEpochs =50;
        int testingEpochs = 10;
        String rewardEntityName = problemName;
        String rewardConfigPath = "reward";
        String epochEntityName = problemName;
        String epochConfigPath = "epoch";
        String trainingEntities = qLearningName + "," + policyName;// + "," + valueSeriesRewardTrainingName;
        String testingEntities = "";//valueSeriesRewardTestingName;

        SetTrainingScheduleEntityConfig(
                trainingScheduleName,
                totalEpochs,
                trainingEpochs,
                testingEpochs,
                rewardEntityName,
                rewardConfigPath,
                epochEntityName,
                epochConfigPath,
                trainingEntities,
                testingEntities );

        // Define the RL problem
        int sequenceLength = 24;
        int targets = 4;
        int prompts = 2;
        int distractors = 4;
        int stateBits = targets + prompts + distractors;

        SetDistractedSequenceRecallEntityConfig( problemName, sequenceLength, distractors, targets, prompts );

        boolean selectNone = true; // do we allow selection of zero bits as an additional action?
        int memoryBanks = 2; // how many separate memory systems? Each containing all the bits
        int gatesPerBit = 2; // write, clear
        int memoryBankSize = stateBits;
        int memoryBankGates = memoryBankSize * gatesPerBit;
        int actions = targets + memoryBankGates * memoryBanks;

        int selectNoneSize = 0;
        if( selectNone ) {
            selectNoneSize = 1;
            int extraActions = 1 + gatesPerBit * memoryBanks;
            actions += extraActions;
        }

        float classifierLearningRate = 0.01f;
        float classifierLearningRateNeighbours = 0.002f;
        int classifierSizeCells = 8;
        int edgeMaxAge = 1000;
        float stressLearningRate = classifierLearningRate;
        float stressSplitLearningRate = 0.5f;
        float stressThreshold = 0.01f;
        float utilityLearningRate = 0;
        float utilityThreshold = -1f;
        int growthInterval = 90;

        SetGrowingNeuralGasEntityConfig( 
                stateClassifierName, classifierLearningRate,
                classifierSizeCells, classifierSizeCells,
                classifierLearningRateNeighbours,
                edgeMaxAge,
                stressLearningRate,
                stressSplitLearningRate,
                stressThreshold,
                utilityLearningRate,
                utilityThreshold,
                growthInterval );
                
        float learningRate = 0.01f;
        float discountRate = 0.95f;
        int classifiedStateSize = classifierSizeCells * classifierSizeCells;
        SetQLearningEntityConfig( qLearningName, learningRate, discountRate, classifiedStateSize, actions );

        SetGatedRecurrentMemoryEntityConfig( memory1Name );
        SetGatedRecurrentMemoryEntityConfig( memory2Name );

        float epsilon = 0.5f;
        ArrayList< Integer > selectionSetSizes = new ArrayList<>();
        selectionSetSizes.add( targets   + selectNoneSize );  // 0 Problem actions, + optional "select none"
        selectionSetSizes.add( stateBits + selectNoneSize );  // 1 Bank 1 Gates Write
        selectionSetSizes.add( stateBits + selectNoneSize );  // 2 Bank 1 Gates Clear
        selectionSetSizes.add( stateBits + selectNoneSize );  // 3 Bank 2 Gates Write
        selectionSetSizes.add( stateBits + selectNoneSize );  // 4 Bank 2 Gates Clear
        SetPolicyEntityConfig( policyName, epsilon, selectionSetSizes );//, selectNone );

        int offsetInput = 0;
        int offsetOutput = 0;
        int range = targets;
        SetVectorCopyRangeEntityConfig( copyAction0Name, offsetInput, offsetOutput, range );

        offsetInput = 0 * ( stateBits + selectNoneSize ) + targets + selectNoneSize;
        range = stateBits;
        SetVectorCopyRangeEntityConfig( copyAction1Name, offsetInput, offsetOutput, range );

        offsetInput = 1 * ( stateBits + selectNoneSize ) + targets + selectNoneSize;
        range = stateBits;
        SetVectorCopyRangeEntityConfig( copyAction2Name, offsetInput, offsetOutput, range );

        offsetInput = 2 * ( stateBits + selectNoneSize ) + targets + selectNoneSize;
        range = stateBits;
        SetVectorCopyRangeEntityConfig( copyAction3Name, offsetInput, offsetOutput, range );

        offsetInput = 3 * ( stateBits + selectNoneSize ) + targets + selectNoneSize;
        range = stateBits;
        SetVectorCopyRangeEntityConfig( copyAction4Name, offsetInput, offsetOutput, range );

        // Data logging
//        String valueSeriesInputEntityName = "";
//        String valueSeriesInputConfigPath = "";
//        String valueSeriesInputDataName = Framework.GetDataKey( problemName, DistractedSequenceRecallEntity.OUTPUT_REWARD );
        String valueSeriesInputEntityName = trainingScheduleName;
        String valueSeriesInputConfigPath = "rewardTraining";
        String valueSeriesInputDataName = "";
        int inputDataOffset = 0;
        int accumulatePeriod = 1;
        float accumulateFactor = 1f;
        ValueSeriesEntityConfig.Set( valueSeriesRewardTrainingName, accumulatePeriod, accumulateFactor, -1, -1, valueSeriesInputEntityName, valueSeriesInputConfigPath, valueSeriesInputDataName, inputDataOffset );
        valueSeriesInputConfigPath = "rewardTesting";
        ValueSeriesEntityConfig.Set( valueSeriesRewardTestingName, accumulatePeriod, accumulateFactor, -1, -1, valueSeriesInputEntityName, valueSeriesInputConfigPath, valueSeriesInputDataName, inputDataOffset );
    }

    protected static void SetGrowingNeuralGasEntityConfig(
            String entityName,
            float learningRate,
            int widthCells,
            int heightCells,
            float learningRateNeighbours,
            int edgeMaxAge,
            float stressLearningRate,
            float stressSplitLearningRate,
            float stressThreshold,
            float utilityLearningRate,
            float utilityThreshold,
            int growthInterval ) {
        GrowingNeuralGasEntityConfig entityConfig = new GrowingNeuralGasEntityConfig();

        entityConfig.learningRate = learningRate;
        entityConfig.widthCells = widthCells;
        entityConfig.heightCells = heightCells;
        entityConfig.learningRateNeighbours = learningRateNeighbours;
        entityConfig.edgeMaxAge = edgeMaxAge;
        entityConfig.stressLearningRate = stressLearningRate;
        entityConfig.stressSplitLearningRate = stressSplitLearningRate;
        entityConfig.stressThreshold = stressThreshold;
        entityConfig.utilityLearningRate = utilityLearningRate;
        entityConfig.utilityThreshold = utilityThreshold;
        entityConfig.growthInterval = growthInterval;
        entityConfig.noiseMagnitude = 0;

        Framework.SetConfig( entityName, entityConfig );
    }

    protected static void SetVectorCopyRangeEntityConfig( String entityName, int offsetInput, int offsetOutput, int range ) {
        VectorCopyRangeEntityConfig entityConfig = new VectorCopyRangeEntityConfig();

        entityConfig.cache = true;
        entityConfig.offsetInput = offsetInput;
        entityConfig.offsetOutput = offsetOutput;
        entityConfig.range = range;

        Framework.SetConfig( entityName, entityConfig );
    }

    protected static void SetExperimentEntityConfig( String entityName, int terminationAge, String terminationEntityName, String terminationEntityConfigPath ) {
        ExperimentEntityConfig entityConfig = new ExperimentEntityConfig();

        if( terminationAge < 0 ) {
            entityConfig.terminationEntityName = terminationEntityName;
            entityConfig.terminationConfigPath = terminationEntityConfigPath;
            entityConfig.terminationAge = -1;
        } else {
            entityConfig.terminationAge = terminationAge;
        }

        Framework.SetConfig( entityName, entityConfig );
    }

    protected static void SetPolicyEntityConfig( String entityName, float epsilon, ArrayList< Integer > selectionSetSizes ) {//}, boolean selectNone ) {//, int simultaneousActions ) {
        EpsilonGreedyEntityConfig entityConfig = new EpsilonGreedyEntityConfig();

        entityConfig.cache = true;
        entityConfig.epsilon = epsilon;
//        entityConfig.selectNone = selectNone;
//        entityConfig.simultaneousActions = simultaneousActions;
        entityConfig.setSelectionSetSizes( selectionSetSizes );

        Framework.SetConfig( entityName, entityConfig );
    }

    protected static void SetQLearningEntityConfig( String entityName, float learningRate, float discountRate, int states, int actions ) {
        QLearningEntityConfig entityConfig = new QLearningEntityConfig();

        entityConfig.cache = true;
        entityConfig.learningRate = learningRate;
        entityConfig.discountRate = discountRate;
        entityConfig.states = states;
        entityConfig.actions = actions;

        Framework.SetConfig( entityName, entityConfig );
    }

    protected static void SetGatedRecurrentMemoryEntityConfig( String entityName ) {
        GatedRecurrentMemoryEntityConfig entityConfig = new GatedRecurrentMemoryEntityConfig();

        entityConfig.cache = true;

        Framework.SetConfig( entityName, entityConfig );
    }

    protected static void SetDistractedSequenceRecallEntityConfig( String entityName, int sequenceLength, int distractors, int targets, int prompts ) {
        DistractedSequenceRecallEntityConfig entityConfig = new DistractedSequenceRecallEntityConfig();

        entityConfig.cache = true;
        entityConfig.sequenceLength = sequenceLength;
        entityConfig.distractors = distractors;
        entityConfig.targets = targets;
        entityConfig.prompts = prompts;

        Framework.SetConfig( entityName, entityConfig );
    }

    protected static void SetTrainingScheduleEntityConfig(
            String entityName,
            int totalEpochs,
            int trainingEpochs,
            int testingEpochs,
            String rewardEntityName,
            String rewardConfigPath,
            String epochEntityName,
            String epochConfigPath,
            String trainingEntities,
            String testingEntities ) {
        TrainingScheduleEntityConfig entityConfig = new TrainingScheduleEntityConfig();

        entityConfig.cache = true;
        entityConfig.totalEpochs = totalEpochs;
        entityConfig.trainingEpochs = trainingEpochs;
        entityConfig.testingEpochs = testingEpochs;
        entityConfig.rewardEntityName = rewardEntityName;
        entityConfig.rewardConfigPath = rewardConfigPath;
        entityConfig.epochEntityName = epochEntityName;
        entityConfig.epochConfigPath = epochConfigPath;
        entityConfig.trainingEntities = trainingEntities;
        entityConfig.testingEntities = testingEntities;

        Framework.SetConfig( entityName, entityConfig );
    }

}