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
        t.test( args );
    }

    public void test( String[] args ) {

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
        ql.setup( qlc );//, p, policy, p );

        for( int epoch = 0; epoch < epochs; ++epoch ) {

            // TRAINING
            qlc.setLearn( true );
            policy._learn = true;

            float sumRewardTraining = 0.f;
            int rewardSamplesTraining = 0;

            for( int test = 0; test < trainingEpochSize; ++test ) {
                //p.print();

                //ql.update(); // updates the problem too
                float rewardNew = p.getReward();
                Data stateNew = p.getState();
                Data actionOld = p.getActions();
                ql.update( stateNew, rewardNew, actionOld );
                Data actionNew = new Data( actionOld._dataSize );
                policy.selectActions( stateNew, ql._actionQuality, actionNew );
                p.setActions( actionNew );
                p.update(); // update the problem
                sumRewardTraining += rewardNew;
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

                float rewardNew = p.getReward();
                Data stateNew = p.getState();
                Data actionOld = p.getActions();
                ql.update( stateNew, rewardNew, actionOld );
                Data actionNew = new Data( actionOld._dataSize );
                policy.selectActions( stateNew, ql._actionQuality, actionNew );
                p.setActions( actionNew );
                p.update(); // update the problem
                sumRewardTesting += rewardNew;
                ++rewardSamplesTesting;
                //System.out.println( "Seq: " + p._sequence + " R: " + rewardNew );
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

}