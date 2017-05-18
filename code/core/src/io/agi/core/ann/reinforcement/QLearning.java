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

import io.agi.core.data.Data;
import io.agi.core.orm.NamedObject;
import io.agi.core.orm.ObjectMap;

import java.util.HashSet;

/**
 * https://en.wikipedia.org/wiki/Q-learning
 *
 * Created by dave on 28/03/17.
 */
public class QLearning extends NamedObject {

    public QLearningConfig _c;

    public Data _expectedRewards;
    public Data _actionQuality;
    public Data _stateOld;
    public Data _stateNew;
    public Data _actionOld;
    public Data _rewardNew;

    public QLearning( String name, ObjectMap om ) {
        super( name, om );
    }

    public void setup( QLearningConfig c ) {
        _c = c;

        int S = c.getNbrStates();
        int A = c.getNbrActions();

        _rewardNew = new Data( 1 );
        _stateOld = new Data( S );
        _stateNew = new Data( S );
        _actionOld = new Data( A );

        _actionQuality = new Data( A );
        _expectedRewards = new Data( S, A );
    }

    public void reset() {
        _expectedRewards.set( 1f ); // encourage exploration
    }

    public void update() {
        update( _actionOld, _stateNew, _rewardNew, _actionQuality );
    }

    /**
     * Train the reward values and assign expected rewards to all possible actions given current state.
     */
    public void update( Data a1, Data s2, Data r2, Data aq ) {

        int A = a1.getSize();
        Data s1 = _stateOld;

        if( _c.getLearn() ) {
            train( s1, a1, s2, r2 );
        }

        findExpectedReward( s1, A, aq );

        _stateOld.copy( s2 );
    }

    /**
     * Returns the
     * @param s1 Current state, all nonzero elements are considered "active"
     * @param A Number of unique actions
     * @param er Expected reward of each action
     */
    public void findExpectedReward( Data s1, int A, Data er ) {
        int S = s1.getSize();

        HashSet< Integer > s1Active = s1.indicesMoreThan( 0.5f );

        for( int a1Bit = 0; a1Bit < A; ++a1Bit ) {
            float QS1A = 0f;

            for( Integer s1Bit : s1Active ) { // now in state s2
                int offsetS1A1 = getExpectedRewardOffset( s1Bit, a1Bit, S, A );
                float QS1A1 = _expectedRewards._values[ offsetS1A1 ];

                QS1A += QS1A1;
            }

            // normalize
            float denominator = 1f;
            int s1ActiveSize = s1Active.size();
            if( s1ActiveSize > 0 ) {
                denominator = (float)s1ActiveSize;
            }

            QS1A /= denominator;

            er._values[ a1Bit ] = QS1A;
        }
    }

    public int getExpectedRewardOffset( int s1, int a1, int S, int A ) {
        int offset = s1 * A + a1;
        return offset;
    }

    /**
     * Update discounted expected reward.
     *
     * @param s1 Previous state
     * @param a1 Previous action[s]
     * @param s2 Current state
     * @param r2 Current reward
     */
    public void train( Data s1, Data a1, Data s2, Data r2 ) {
        // Q( s_{t},a_{t} ) = Q of doing a in state s.
        // Q( s_{t},a_{t} ) = Q( s_{t},a_{t} ) + learingRate * delta
        // delta = ( r_{t+1} + discountRate * maxQ - Q( s_{t},a_{t} ) )
        // maxQ = max( over all a ) Q( s_{t+1}, a_{t} )
        // where r_{t+1} is the reward observed after performing a_t in s_{t}

        // Shifting to not require future knowledge:
        // Q( s_{t-1},a_{t-1} ) = Q( s_{t-1},a_{t-1} ) + learingRate * delta
        // delta = ( r_{t} + discountRate * maxQ - Q( s_{t-1},a_{t-1} ) )
        // maxQ = max( over all a ) Q( s_{t}, a_{t} )

        // we may have multiple states at the same time (distributed representation).

        float learningRate = _c.getLearningRate();
        float discountRate = _c.getDiscountRate();

        float r2Value = r2._values[ 0 ];

        HashSet< Integer > s1Active = s1.indicesMoreThan( 0.5f );
        HashSet< Integer > s2Active = s2.indicesMoreThan( 0.5f );
        HashSet< Integer > a1Active = a1.indicesMoreThan( 0.5f ); // chosen actions

        int S = s1.getSize();
        int A = a1.getSize();

        for( Integer s1Bit : s1Active ) { // was in state s1
            for( Integer a1Bit : a1Active ) { // did action a1
                int offsetS1A1 = getExpectedRewardOffset( s1Bit, a1Bit, S, A );
                float oldQS1A1 = _expectedRewards._values[ offsetS1A1 ];

                // find max Q:
                float maxQS2A = 0f;

                for( Integer s2Bit : s2Active ) { // now in state s2
                    float maxQS2A2 = 0f;

                    for( int a2Bit = 0; a2Bit < A; ++a2Bit ) {
                        int offsetS2A2 = getExpectedRewardOffset( s2Bit, a2Bit, S, A );
                        float QS2A2 = _expectedRewards._values[ offsetS2A2 ];

                        if( QS2A2 > maxQS2A2 ) {
                            maxQS2A2 = QS2A2;
                        }
                    }

                    maxQS2A += maxQS2A2;
                }

                // take average of Q( s2, max( a2 ) ) for all active states s2.
                float denominator = 1f;
                int s2ActiveSize = s2Active.size();
                if( s2ActiveSize > 0 ) {
                    denominator = (float)s2ActiveSize;
                }

                maxQS2A /= denominator;

                // plumb into Q learning equation
                float deltaQ = ( r2Value + discountRate * maxQS2A - oldQS1A1 );
                float newQS1A1 = oldQS1A1 + learningRate * deltaQ;

                _expectedRewards._values[ offsetS1A1 ] = newQS1A1;
            }
        }
    }

}
