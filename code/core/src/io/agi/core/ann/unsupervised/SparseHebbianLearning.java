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
import io.agi.core.math.Unit;

import java.util.Collection;
import java.util.HashSet;

/**
 * Has 2 inputs:
 * a) A current state (an exclusive bit/cell in a set)
 * b) Some contextual inputs
 *
 * And one output:
 * c) A predicted state (a different exclusive bit/cell in a set)
 *
 * We discover the conditional probability of each future state given the current state and the contextual inputs.
 * It is assumed that the input is sparse binary. Therefore, inactive bits give no evidence.
 *
 * The conditional probabilities are approximated by observed frequency.
 *
 * Created by dave on 28/04/16.
 */
public class SparseHebbianLearning {

    public Data _state;
//    public Data _stateOld;
    public Data _statePredicted;
    public Data _context;
    public Data _weights;

    public float _learningRate = 0.1f;

    public void setup( int states, int context, float learningRate ) {

        _learningRate = learningRate;

        int weights = states * context * states;

        _state = new Data( DataSize.create( states ) );
        _statePredicted = new Data( DataSize.create( states ) );
        _context = new Data( DataSize.create( context ) );
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
    public void predict() {
        predict( _state, _context, _weights, _statePredicted );
    }

    public static void predict( Data state, Data context, Data weights, Data statePredicted ) {

        int states = state.getSize();
        int contexts = context.getSize();

        int s1 = state.maxAt().offset();

        statePredicted.set( 0.f );

        // One weight for each state s1, to each state s2, from each context bit.
        // w = P( s2=1 | s1=1, w_n=1 )
        // S_1 * W * S_2

        int s2Best = 0;
        float wMeanBest = 0.f;

        for( int s2 = 0; s2 < states; ++s2 ) {

            float wSum = 0.f;
            int wCount = 0;

            // Find the mean frequency over all the contextual inputs
            for( int c = 0; c < contexts; ++c ) {

                float active = context._values[ c ];

                if( active < 1.f ) {
                    continue; // says nothing about the cell
                }

                int offset = s1 * contexts * states
                           +      c        * s2; // the weight from state s1, with context bit c, to state s2.

                float weight = weights._values[ offset ];

                wSum += weight;
                wCount += 1;
            }

            float wMean = 0.f;
            if( wCount > 0 ) {
                wMean = wSum / wCount;
            }

            if( wMean > wMeanBest ) {
                wMeanBest = wMean;
                s2Best = s2;
            }
        }

        statePredicted._values[ s2Best ] = 1.f;
    }

    /**
     * Accumulate any bits seen during the state transition, as they may occur asynchronously
     * @param context
     */
    public void updateContext( Data context ) {
        HashSet< Integer > activeContext = context.indicesMoreThan( 0f );
        updateContext( activeContext );
    }

    /**
     * Accumulate any bits seen during the state transition, as they may occur asynchronously
     * @param activeContext
     */
    public void updateContext( Collection< Integer > activeContext ) {
        for( Integer c : activeContext ) {
            _context._values[ c ] = 1.f;
        }
    }

    /**
     * Call this when there is a state change.
     */
    public void train( Data stateNew ) {
        int states = _state.getSize();
        int context = _context.getSize();

        int s2Best = stateNew.maxAt().offset();

        HashSet< Integer > activeContext = _context.indicesMoreThan( 0f );

        for( int s1 = 0; s1 < states; ++s1 ) {
            for( Integer c : activeContext ) { // don't train for context bits that were not present.. we don't have any opinion on their influence.
                for( int s2 = 0; s2 < states; ++s2 ) {

                    float observation = 0.f; // transition wasn't to s2 given this original state and the context.
                    if( s2 == s2Best ) {
                        observation = 1.f; // i.e. there was a transition to s2 from s1.
                    }

                    int offset = s1 * context * states
                               +      c       * s2; // the weight from state s1, with context bit c, to state s2.

                    float oldWeight = _weights._values[ offset ];
                    float newWeight = Unit.lerp( observation, oldWeight, _learningRate );

                    _weights._values[ offset ] = newWeight;
                }
            }
        }

//        _stateOld.copy( _state );
        _state.copy( stateNew );
        _context.set( 0.f ); // clear all context
    }

}
