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

package io.agi.core.ann.reinforcement;

import io.agi.core.ann.unsupervised.GrowingNeuralGas;
import io.agi.core.ann.unsupervised.GrowingNeuralGasConfig;
import io.agi.core.data.Data;
import io.agi.core.orm.ObjectMap;
import io.agi.core.orm.UnitTest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

/**
 * Created by dave on 19/05/17.
 */
public class QLearningTest implements UnitTest {

    public static void main( String[] args ) throws Exception {
        QLearningTest t = new QLearningTest();
        t.testFrozenLake( args );
        //t.test( args );
        //t.test2( args );
        //t.test3( args );
    }

    public void testFrozenLake( String[] args ) {

        String name = "ql";
        ObjectMap om = new ObjectMap();
        Random r = new Random();
        float learningRate = 0.1f;
        float discountRate = 0.9f;
        int epochs = 10000;
        int trainingEpochSize = 100;
        int testingEpochSize = 100;
        float minPath = 8f;
        float meanRewardThreshold = 1f/minPath;
        float epsilon = 0.5f;

        FrozenLakeProblem p = new FrozenLakeProblem();

        int size = 5;
//        int size = 4;
        ArrayList< Integer > holes = new ArrayList< Integer >();
//        holes.add( 5 );
//        holes.add( 7 );
//        holes.add( 11 );
//        holes.add( 12 );

        holes.add( 5 );
        holes.add( 7 );
        holes.add( 10 );
        holes.add( 12 );
        holes.add( 20 );
        holes.add( 22 );
        p.setup( r, size, holes );
        p.print();

        int states = p.getNbrStates();
        int actions = p.getNbrActions();

        EpsilonGreedyQLearningPolicy policy = new EpsilonGreedyQLearningPolicy();

        policy.setup( r, epsilon );

        QLearningConfig qlc = new QLearningConfig();
        qlc.setup( om, name, r, states, actions, learningRate, discountRate );

        QLearning ql = new QLearning();
        ql.setup( qlc, p, policy, p );

        for( int epoch = 0; epoch < epochs; ++epoch ) {

            // TRAINING
            qlc.setLearn( true );
            policy._learn = true;

            float sumRewardTraining = 0.f;
            int rewardSamplesTraining = 0;

            for( int test = 0; test < trainingEpochSize; ++test ) {
                //p.print();

                ql.update(); // updates the problem too
                float reward = ql._r.getReward();
                sumRewardTraining += reward;
                ++rewardSamplesTraining;
            }

            float meanRewardTraining = 0.f;
            if( sumRewardTraining > 0.f ) {
                meanRewardTraining = sumRewardTraining / ( float ) rewardSamplesTraining;
            }

            // TESTING
            qlc.setLearn( false );
            policy._learn = false;

            float sumRewardTesting = 0.f;
            float rewardSamplesTesting = 0;

            for( int test = 0; test < testingEpochSize; ++test ) {
                //p.print();

                //System.err.println( "Testing... " + p._sequence );

                ql.update(); // updates the problem too
                float reward = ql._r.getReward(); // includes the reward for the last action
                //System.out.println( "Seq: " + p._sequence + " R: " + reward );
                sumRewardTesting += reward;
                ++rewardSamplesTesting;

                //System.err.println( "Tested " + p._sequence );
            }

            float meanRewardTesting = 0.f;
            if( sumRewardTesting > 0.f ) {
                meanRewardTesting = sumRewardTesting / ( float ) rewardSamplesTesting;
            }

            System.out.println( epoch + ", TESTING, " + meanRewardTesting + ", TRAINING, " + meanRewardTraining );

            if( meanRewardTesting > meanRewardThreshold ) {
                System.out.println( "Success: Reward above threshold " + meanRewardThreshold + " in epoch " + epoch );
                return;
            }
        }

        System.out.println( "Failure: Reward did not go above threshold " + meanRewardThreshold + " for any epoch." );
    }

    public void test( String[] args ) {

        String name = "ql";
        ObjectMap om = new ObjectMap();
        Random r = new Random();
        float learningRate = 0.1f;
        float discountRate = 0.1f;
        int epochs = 10;
        int trainingEpochSize = 1000;
        int testingEpochSize = 300;
        float meanRewardThreshold = 0.999f;
        float epsilon = 0.5f;

        DistractedSequenceRecallProblem p = new DistractedSequenceRecallProblem();

        p.setup( r );

        System.out.println( "Random expected value: " + p.getRandomExpectedReward() );

        int states = p.getNbrObservations();
        int actions = p.getNbrTargets();

        EpsilonGreedyQLearningPolicy policy = new EpsilonGreedyQLearningPolicy();
        policy.setup( r, epsilon );

        QLearningConfig qlc = new QLearningConfig();
        qlc.setup( om, name, r, states, actions, learningRate, discountRate );

        QLearning ql = new QLearning();
        ql.setup( qlc, p, policy, p );


        for( int epoch = 0; epoch < epochs; ++epoch ) {

            // TRAINING
            qlc.setLearn( true );

            float sumReward = 0.f;
            int rewardSamples = 0;

            for( int test = 0; test < trainingEpochSize; ++test ) {
                //p.print();

                do {
                    ql.update(); // updates the problem too
                    float reward = ql._r.getReward();
                    sumReward += reward;
                    ++rewardSamples;
                }
                while( p._sequence != 0 );
            }

            float meanReward = 0.f;
            if( sumReward > 0.f ) {
                meanReward = sumReward / ( float ) rewardSamples;
            }

            System.out.println( "TRAINING Epoch: " + epoch + " Mean reward: " + meanReward );

            // TESTING
            qlc.setLearn( false );

            sumReward = 0.f;
            rewardSamples = 0;

            for( int test = 0; test < testingEpochSize; ++test ) {
                //p.print();

                //System.err.println( "Testing... " + p._sequence );

                do {
                    ql.update(); // updates the problem too
                    float reward = ql._r.getReward(); // includes the reward for the last action
                    //System.out.println( "Seq: " + p._sequence + " R: " + reward );
                    sumReward += reward;
                    ++rewardSamples;
                }
                while( p._sequence != 0 );

                //System.err.println( "Tested " + p._sequence );
            }

            meanReward = 0.f;
            if( sumReward > 0.f ) {
                meanReward = sumReward / ( float ) rewardSamples;
            }

            System.out.println( "TESTING  Epoch: " + epoch + " Mean reward: " + meanReward );

            if( meanReward > meanRewardThreshold ) {
                System.out.println( "Success: Reward above threshold " + meanRewardThreshold + " in epoch " + epoch );
                return;
            }
        }

        System.out.println( "Failure: Reward did not go above threshold " + meanRewardThreshold + " for any epoch." );
    }

    public void test2( String[] args ) {

        String name = "ql";
        ObjectMap om = new ObjectMap();
        Random r = new Random();
        float learningRate = 0.1f;
        float discountRate = 0.1f;
        int epochs = 100;
        int trainingEpochSize = 100;
        int testingEpochSize = 30;
        float meanRewardThreshold = 0.999f;
        float epsilon = 0.5f;

        DistractedSequenceRecallProblem p = new DistractedSequenceRecallProblem();
        p.setup( r );

        System.out.println( "Random expected value: " + p.getRandomExpectedReward() );

        int observations = p.getNbrObservations();
        int memory = observations;

        GatedRecurrentMemory grm = new GatedRecurrentMemory();
        grm.setup( observations );

        int motors = p.getNbrTargets();
        int gates = grm.getNbrGates();
        int input = observations + memory;
        int states = 1 << input;
        int actions = motors + gates;

        EpsilonGreedyQLearningPolicy policy = new EpsilonGreedyQLearningPolicy();
        policy.setup( r, epsilon );

        QLearningConfig qlc = new QLearningConfig();
        qlc.setup( om, name, r, states, actions, learningRate, discountRate );

        QLearning ql = new QLearning();
        ql.setup( qlc, p, policy, null );

        for( int epoch = 0; epoch < epochs; ++epoch ) {

            // TRAINING
            qlc.setLearn( true );
            policy._learn = true;

            float sumReward = 0.f;
            int rewardSamples = 0;

            for( int test = 0; test < trainingEpochSize; ++test ) {
                // p.print();

                do {
                    //ql.update(); // updates the problem too

                    update( p, ql, grm, observations, memory, motors, gates, states );

                    // update reward for this sequence
                    float reward = ql._r.getReward();
                    sumReward += reward;
                    ++rewardSamples;
                }
                while( p._sequence != 0 );
            }

            float meanReward = 0.f;
            if( sumReward > 0.f ) {
                meanReward = sumReward / ( float ) rewardSamples;
            }

            System.out.println( "TRAINING Epoch: " + epoch + " Mean reward: " + meanReward );

            // TESTING
            qlc.setLearn( false );
            policy._learn = false;

            sumReward = 0.f;
            rewardSamples = 0;

            for( int test = 0; test < testingEpochSize; ++test ) {
                //p.print();

                //System.err.println( "Testing... " + p._sequence );

                do {
                    //ql.update(); // updates the problem too

                    update( p, ql, grm, observations, memory, motors, gates, states );

                    // update reward for this sequence
                    float reward = ql._r.getReward();
                    sumReward += reward;
                    ++rewardSamples;
                }
                while( p._sequence != 0 );

                //System.err.println( "Tested " + p._sequence );
            }

            meanReward = 0.f;
            if( sumReward > 0.f ) {
                meanReward = sumReward / ( float ) rewardSamples;
            }

            System.out.println( "TESTING  Epoch: " + epoch + " Mean reward: " + meanReward );

            if( meanReward > meanRewardThreshold ) {
                System.out.println( "Success: Reward above threshold " + meanRewardThreshold + " in epoch " + epoch );
                return;
            }
        }

        System.out.println( "Failure: Reward did not go above threshold " + meanRewardThreshold + " for any epoch." );
    }

    protected static void update(
            DistractedSequenceRecallProblem p,
            QLearning ql,
            GatedRecurrentMemory grm,
            int observations,
            int memory,
            int motors,
            int gates,
            int states ) {
        float rewardNew = p.getReward();
        //Data stateNew = _w.getState();

        // Combine current observations and memory read as a vector
        Data observed = p.getState(); // not compressed into a single state yet
        Data remembered = grm.getOutput(); // from last update
        Data combined = new Data( states );
        int offsetThis = 0;
        combined.copyRange( observed  , offsetThis, 0, observations );
        offsetThis = observations;
        combined.copyRange( remembered, offsetThis, 0, memory );

        // Jointly encode the observations as a single state:
        HashSet< Integer > observedBits = combined.indicesMoreThan( 0.5f );
        int state = 0;
        for( Integer bit : observedBits ) {
            int stateBit = 1 << bit;
            state |= stateBit;
        }
        Data stateNew = new Data( states );
        stateNew._values[ state ] = 1f; // this is the current state

        // update Q Learning
        // observe targets, then set memory to write/clear as appropriate
        ql.update( stateNew, rewardNew, ql._actionNew );

        // unpack the actions selected
        Data selectedActions = ql._actionNew;
        Data motorActions = new Data( motors );
        motorActions.copyRange( selectedActions, 0, 0, motors );
        Data memoryActions = new Data( gates );
        memoryActions.copyRange( selectedActions, 0, motors, gates );

        // update memory
        grm.setInput( observed );
        grm.setGates( memoryActions );
        grm.update();

        // update world
        p.setActions( motorActions );
        p.update(); // update the world in response to the action
    }

    static String percentage( float r ) {
        int n = (int)( r * 100 );
        n = Math.min( n, 99 );
        String s = String.valueOf( n );

        while( s.length() < 2 ) {
            s = "0" + s;
        }
        return s;
    }

    public void test3( String[] args ) {

        String name = "ql";
        ObjectMap om = new ObjectMap();
        Random r = new Random();
        float learningRate = 0.1f;
        float discountRate = 0.1f;
        int epochs = 1000;
        int trainingEpochSize = 100;
        int testingEpochSize = 30;
        float meanRewardThreshold = 0.999f;
        float epsilon = 0.5f;
        int sizeCells = 10;
        int memoryBanks = 2;

        DistractedSequenceRecallProblem p = new DistractedSequenceRecallProblem();
        p.setup( r );

        System.out.println( "Random expected value: " + p.getRandomExpectedReward() );

        int observations = p.getNbrObservations();

        ArrayList< SimpleGatedRecurrentMemory > memories = new ArrayList<>();
        int gates = 0;
        for( int i = 0; i < memoryBanks; ++i ) {
            SimpleGatedRecurrentMemory grm = new SimpleGatedRecurrentMemory();
            grm.setup( observations );
            gates += grm.getNbrGates();
            memories.add( grm );
        }

        int motors = p.getNbrTargets();
        int inputs = observations + memoryBanks * observations;
        int actions = motors + gates; // ie 4 + 2 + 2 = 8

        GrowingNeuralGasConfig c = new GrowingNeuralGasConfig();
        String gngName = "gng";
        float gngLearningRate = 0.01f;
        float learningRateNeighbours = gngLearningRate * 0.2f;
        float noiseMagnitude = 0f;
        int edgeMaxAge = 1000;
        float stressLearningRate = learningRate;
        float stressSplitLearningRate = 0.5f;
        float stressThreshold = 0.01f;
        float utilityLearningRate = stressLearningRate;
        float utilityThreshold = -1f;
        int growthInterval = 12;

        c.setup( om, gngName, r, inputs, sizeCells, sizeCells, gngLearningRate, learningRateNeighbours, noiseMagnitude, edgeMaxAge, stressLearningRate, stressSplitLearningRate, stressThreshold, utilityLearningRate, utilityThreshold, growthInterval );
        GrowingNeuralGas classifier = new GrowingNeuralGas( gngName, om );
        classifier.setup( c );

        EpsilonGreedyQLearningPolicy policy = new EpsilonGreedyQLearningPolicy();
        policy.setup( r, epsilon );

        QLearningConfig qlc = new QLearningConfig();
        int states = c.getAreaCells();
        qlc.setup( om, name, r, states, actions, learningRate, discountRate );

        QLearning ql = new QLearning();
        ql.setup( qlc, p, policy, null );

        for( int epoch = 0; epoch < epochs; ++epoch ) {

            // TRAINING
            qlc.setLearn( true );
            policy._learn = true;

            float sumRewardTraining = 0.f;
            int rewardSamplesTraining = 0;

            for( int test = 0; test < trainingEpochSize; ++test ) {
                // p.print();

                do {
                    //ql.update(); // updates the problem too

                    update3( p, ql, classifier, memories, observations, motors, states );

                    // update reward for this sequence
                    float reward = ql._r.getReward();
                    sumRewardTraining += reward;
                    ++rewardSamplesTraining;
                }
                while( p._sequence != 0 );
            }

            float meanRewardTraining = 0.f;
            if( sumRewardTraining > 0.f ) {
                meanRewardTraining = sumRewardTraining / ( float ) rewardSamplesTraining;
            }

            // TESTING
            qlc.setLearn( false );
            policy._learn = false;

            float sumRewardTesting = 0.f;
            int rewardSamplesTesting = 0;

            for( int test = 0; test < testingEpochSize; ++test ) {
                //p.print();

                //System.err.println( "Testing... " + p._sequence );

                do {
                    //ql.update(); // updates the problem too

                    update3( p, ql, classifier, memories, observations, motors, states );

                    // update reward for this sequence
                    float reward = ql._r.getReward();
                    sumRewardTesting += reward;
                    ++rewardSamplesTesting;
                }
                while( p._sequence != 0 );

                //System.err.println( "Tested " + p._sequence );
            }

            float meanRewardTesting = 0.f;
            if( sumRewardTesting > 0.f ) {
                meanRewardTesting = sumRewardTesting / ( float ) rewardSamplesTesting;
            }

            System.out.println( "Epoch: " + epoch + " TESTING reward: " + meanRewardTesting + " TRAINING reward: " + meanRewardTraining );

            if( meanRewardTesting > meanRewardThreshold ) {
                System.out.println( "Success: Reward above threshold " + meanRewardThreshold + " in epoch " + epoch );
                return;
            }
        }

        System.out.println( "Failure: Reward did not go above threshold " + meanRewardThreshold + " for any epoch." );
    }

    protected static void update3(
            DistractedSequenceRecallProblem p,
            QLearning ql,
            GrowingNeuralGas classifier,
            ArrayList< SimpleGatedRecurrentMemory > memories,
            int observations,
            int motors,
            int states ) {
        float rewardNew = p.getReward();
        //Data stateNew = _w.getState();

        int M = memories.size();
        int classifierInputSize = observations * (M+1);
        Data classifierInput = new Data( classifierInputSize );
        Data observed = p.getState(); // not compressed into a single state yet
        int offsetThis = 0;
        classifierInput.copyRange( observed, offsetThis, 0, observations );
        offsetThis = observations;

        // Combine current observations and memory read as a vector
        for( int m = 0; m < M; ++m ) {
            SimpleGatedRecurrentMemory grm = memories.get( m );
            Data remembered = grm.getOutput();
            classifierInput.copyRange( remembered, offsetThis, 0, observations );
            offsetThis += observations;
        }

        classifier._inputValues.copy( classifierInput );
        classifier.update();
        int state = classifier.getBestCell();

        //float pop = classifier._cellMask.sum();
        //System.err.println( "Class. Pop: " + pop );

        Data stateNew = new Data( states );
        stateNew._values[ state ] = 1f; // this is the current state

        // update Q Learning
        // observe targets, then set memory to write/clear as appropriate
        ql.update( stateNew, rewardNew, ql._actionNew );

        // unpack the actions selected
        Data selectedActions = ql._actionNew;
        Data motorActions = new Data( motors );
        motorActions.copyRange( selectedActions, 0, 0, motors );

        // update memory
        for( int m = 0; m < M; ++m ) {
            SimpleGatedRecurrentMemory grm = memories.get( m );
            grm.setInput( observed );
            int gates = grm.getNbrGates();
            int writeGateOffset = motors + m * gates;
            int clearGateOffset = writeGateOffset +1;
            float writeGateValue = selectedActions._values[ writeGateOffset ];
            float clearGateValue = selectedActions._values[ clearGateOffset ];
            grm.setWriteGate( writeGateValue );
            grm.setClearGate( clearGateValue );
            grm.update();
        }

        // update world
        p.setActions( motorActions );
        p.update(); // update the world in response to the action
    }

}