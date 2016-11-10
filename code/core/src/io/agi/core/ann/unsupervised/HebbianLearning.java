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

package io.agi.core.ann.unsupervised;

import io.agi.core.data.Data;
import io.agi.core.data.DataSize;
import io.agi.core.data.Ranking;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeMap;

/**
 * Converges on the relative frequency of transitions between sets of states.
 *
 * Can use this to predict future states.
 *
 * Created by dave on 6/07/16.
 */
public class HebbianLearning {

//    public Data _state;
    public Data _statePredicted;
    public Data _statePredictedReal;
    public Data _weights;

    public float _learningRate = 0.01f; // e.g. moves 1 part in 100 each step. Max value is 99, min is 0, giving 100 intervals.

    public void setup( int states, float learningRate ) {

        _learningRate = learningRate;

        int weights = states * states;

//        _state = new Data( DataSize.create( states ) );
        _statePredicted = new Data( DataSize.create( states ) );
        _statePredictedReal = new Data( DataSize.create( states ) );
        _weights = new Data( DataSize.create( weights ) );
    }

    /**
     * Although there's no real need for a reset, because it's completely online, this can accelerate unlearning.
     * Which might be useful for you.
     */
    public void reset() {
        _weights.set( 0.0f );
    }

    public Data getPrediction() {
        return _statePredicted;
    }

    /**
     * Generates a new prediction based on the current state and context bits.
     */
    public void predict( Data state, int density ) {
        predict( state, _weights, _statePredicted, _statePredictedReal, density );
    }

    /**
     * Predict which bits will be active next. Threshold at rank = density.
     *
     * @param state
     * @param weights
     * @param statePredicted
     * @param statePredictedRaw
     * @param density
     */
    public static void predict( Data state, Data weights, Data statePredicted, Data statePredictedRaw, int density ) {

        int states = state.getSize();

//        int s1 = state.maxAt().offset();
        statePredicted.set( 0.f );

        // One weight for each state s1, to each state s2, from each context bit.
        // w = P( s2=1 | s1=1, w_n=1 )
        // S_1 * W * S_2

        TreeMap< Float, ArrayList< Integer > > ranking = new TreeMap< Float, ArrayList< Integer > >();

        for( int s2 = 0; s2 < states; ++s2 ) {

            float wSum = 0.f;
            int wCount = 0;

            // Find the mean frequency over all the contextual inputs
            for( int s1 = 0; s1 < states; ++s1 ) {

                float active = state._values[ s1 ];

                if( active < 1.f ) {
                    continue; // says nothing about the cell s2
                }

                int offset = s1 * states + s2; // the weight from state s1, to state s2.

                float weight = weights._values[ offset ];

                wSum += weight;
                wCount += 1;
            }

            float wMean = 0.f;
            if( wCount > 0 ) {
                wMean = wSum / wCount;
            }

            Ranking.add( ranking, wMean, s2 );

            statePredictedRaw._values[ s2 ] = wMean;
        }

        int maxRank = density;
        boolean findMaxima = true; // keep the youngest
        HashSet< Integer > mostPredicted = new HashSet< Integer >();
        Ranking.getBestValues( ranking, findMaxima, maxRank, mostPredicted );

        for( Integer s2 : mostPredicted ) {
            statePredicted._values[ s2 ] = 1.f;
        }
    }

    /**
     * Call this when there is a state change.
     */
    public void trainExponential( Data stateOld, Data stateNew ) {
        HashSet< Integer > bitsOld = stateOld.indicesMoreThan( 0.5f );
        HashSet< Integer > bitsNew = stateNew.indicesMoreThan( 0.5f );

        if( bitsOld.equals( bitsNew ) ) {
            return;
        }

        int states = stateOld.getSize();

        for( int s1 = 0; s1 < states; ++s1 ) {

            // old state - train only from old active states to all new states
            float active1 = stateOld._values[ s1 ];

            if( active1 < 1.f ) {
                continue; // says nothing about any cell s2
            }

            for( int s2 = 0; s2 < states; ++s2 ) {

                float delta = 0.f;

                float active2 = stateNew._values[ s2 ];
                if( active2 != 0f ) {
                    delta = 1.f;
                }

                int offset = s1 * states + s2; // the weight from state s1, to state s2.

                float oldWeight = _weights._values[ offset ];
                float newWeight = delta * _learningRate + ( 1.f - _learningRate ) * oldWeight;

                _weights._values[ offset ] = newWeight;
            }
        }
    }

    /**
     * Call this when there is a state change.
     */
    public void trainLinear( Data stateOld, Data stateNew ) {
        HashSet< Integer > bitsOld = stateOld.indicesMoreThan( 0.5f );
        HashSet< Integer > bitsNew = stateNew.indicesMoreThan( 0.5f );

        if( bitsOld.equals( bitsNew ) ) {
            return;
        }

        int states = stateOld.getSize();

        for( int s1 = 0; s1 < states; ++s1 ) {

            // old state - train only from old active states to all new states
            float active1 = stateOld._values[ s1 ];

            if( active1 < 1.f ) {
                continue; // says nothing about any cell s2
            }

            for( int s2 = 0; s2 < states; ++s2 ) {

                float delta = - _learningRate;

                float active2 = stateNew._values[ s2 ];
                if( active2 != 0f ) {
                    delta = _learningRate;
                }

                int offset = s1 * states + s2; // the weight from state s1, to state s2.

                float oldWeight = _weights._values[ offset ];
                float newWeight = oldWeight + delta;

                newWeight = Math.max( 0, Math.min( 1f, newWeight ) );

                _weights._values[ offset ] = newWeight;
            }
        }
    }

    // * https://en.wikipedia.org/wiki/Oja%27s_rule
    // * http://www.scholarpedia.org/article/Oja_learning_rule

    // I could implement STDP rule
    // where "early" bits predict and "late" bits inhibit
    // here we would have traces before and after. Or just prior/next states
    // So if bit A was active and now bit B is active, then we reduce the weight between bit B -> A
    // http://www.scholarpedia.org/article/Spike-timing_dependent_plasticity

}
