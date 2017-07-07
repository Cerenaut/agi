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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * From Monner & Reggia 2012, "A generalized LSTM-like training algorithm for
 * second-order recurrent neural networks"
 *
 * 5.1. Distracted Sequence Recall on the standard architecture
 * In the first set of experiments, we trained different neural networks on a
 * task we call the Distracted Sequence Recall task. This task is our variation of
 * the “temporal order” task, which is arguably the most challenging task demon-
 * strated by Hochreiter & Schmidhuber (1997). The Distracted Sequence Recall
 * task involves 10 symbols, each represented locally by a single active unit in an
 * input layer of 10 units: 4 target symbols, which must be recognized and remem-
 * bered by the network, 4 distractor symbols, which never need to be remembered,
 * and 2 prompt symbols which direct the network to give an answer. A single trial
 * consists of a presentation of a temporal sequence of 24 input symbols. The first
 * 22 consist of 2 randomly chosen target symbols and 20 randomly chosen dis-
 * tractor symbols, all in random order; the remaining two symbols are the two
 * prompts, which direct the network to produce the first and second target in the
 * sequence, in order, regardless of when they occurred. Note that the targets may
 * appear at any point in the sequence, so the network cannot rely on their tempo-
 * ral position as a cue; rather, the network must recognize the symbols as targets
 * and preferentially save them, along with temporal order information, in order
 * to produce the correct output sequence. The network is trained to produce no
 * output for all symbols except the prompts, and for each prompt symbol the
 * network must produce the output symbol which corresponds to the appropriate
 * target from the sequence.
 * The major difference between the “temporal order” task and our Distracted
 * Sequence Recall task is as follows. In the former, the network is required to
 * activate one of 16 output units, each of which represents a possible ordered se-
 * quence of both target symbols. In contrast, the latter task requires the network
 * to activate one of only 4 output units, each representing a single target symbol;
 * the network must activate the correct output unit for each of the targets, in the
 * same order they were observed. Requiring the network to produce outputs in
 * sequence adds a layer of difficulty; however, extra generalization power may be
 * imparted by the fact that the network is now using the same output weights
 * to indicate the presence of a target, regardless of its position in the sequence.
 * Because the “temporal order” task was found to be unsolvable by gradient train-
 * ing methods other than LSTM (Hochreiter & Schmidhuber, 1997), we do not
 * include methods other than LSTM and LSTM-g in the comparisons.
 * @author dave
 */
public class DistractedSequenceRecallProblem implements QLearningProblem, Reward {

    //    OBSERVATIONS 10 symbols = 10 inputs, one exclusively active each time
    //    TARGETS 4 target symbols - must be recognized and remembered
    //            4 distractor symbols
    //    PROMPTS 2 prompt symbols - network should give answer.
    //
    //    Each trial:
    //        - presentation of 24 inputs in sequence
    //        - First 22 consist of 2 random target symbols and 20 random distractors
    //        - Last 2 are prompts, requiring the network to produce the 1st and 2nd
    //          target pattern in the sequence, regardless of when they occurred.
    //        - No output allowed except when prompted.
    public Random _r;
    public boolean _loop = true;

    public int _epoch = 0;
    public int _sequence = 0;
    public int _sequenceLength = 0;
    //    int _elements = 10;
    public int _distractors = 0;
    public int _targets = 0;
    public int _prompts = 0;

    public float _reward = 0;

    public Data _sequenceState;
    public Data _sequenceActions;
    public Data _state;
    public Data _actions;
    public Data _idealActions;

    /**
     * Default parameters for the problem
     */
    public void setup( Random r ) {
        int sequenceLength = 24;
        int targets = 4;
        int prompts = 2;
        int distractors = 4;
        setup( r, sequenceLength, targets, prompts, distractors );
    }

    /**
     * Generate data structures ready for use.
     * Calls reset() to generate first problem.
     *
     * @param sequenceLength
     * @param targets
     * @param prompts
     * @param distractors
     */
    public void setup( Random r, int sequenceLength, int targets, int prompts, int distractors ) {
        _r = r;
        _sequenceLength = sequenceLength;
        _targets = targets; // targets all all bits up to _targets
        _prompts = prompts;
        _distractors = distractors;

        int observations = getNbrObservations();

        _sequenceState = new Data( observations, _sequenceLength );
        _sequenceActions = new Data( _targets, _sequenceLength );
        _state = new Data( observations );
        _actions = new Data( _targets );
        _idealActions = new Data( _targets );

        reset();
    }

    public int getSequenceLength() {
        return _sequenceLength;
    }

    public int getNbrObservations() {
        return _targets + _prompts + _distractors;
    }

    public int getNbrActions() {
        return _targets;
    }

    public void reset() {
        // randomly generate a trial
        _sequence = 0;
        _sequenceState.set( 0.f );
        _sequenceActions.set( 0.f );

        ArrayList< Integer > targetBits = new ArrayList< Integer >();
        ArrayList< Integer > targetTimes = new ArrayList< Integer >();
//        HashSet< Integer > usedTargetIndices = new HashSet< Integer >();

        int observations = getNbrObservations();
        int length = _sequenceLength - _prompts;

        // pick targets for each prompt in a trial:
        // Need a 2-D pick:
        for( int t = 0; t < _prompts; ++t ) {
            int targetBit = _r.nextInt( _targets ); // allow duplicate targets (unspecified whether this is the case)

            int targetTime = 0;

            do {
                targetTime = _r.nextInt( length );//targetIndexRange );
            }
            while( targetTimes.contains( targetTime ) );  // must be dissimilar

            targetBits.add( targetBit );
            targetTimes.add( targetTime );
        }

        // now sort by target times.
        Collections.sort( targetTimes );

        // set the target inputs:
        for( int t = 0; t < _prompts; ++t ) {
            int targetBit  = targetBits.get( t );
            int targetTime = targetTimes.get( t );
            _sequenceState._values[ targetTime * observations + targetBit ] = 1.f; // these inputs are the targets
            // zero output expected during target setting
            int promptBit = _targets +t;
            int promptTime = length + t;
            _sequenceState._values[ promptTime * observations + promptBit ] = 1.f; // these inputs are the targets
            _sequenceActions._values[ promptTime * _targets + targetBit ] = 1.f; // ideal output bit is the target value
        }

        // now set some random distractor bits:
        // " A single trial
        // consists of a presentation of a temporal sequence of 24 input symbols. The first
        // 22 consist of 2 randomly chosen target symbols and 20 randomly chosen dis-
        // tractor symbols, all in random order; the remaining two symbols are the two
        // prompts, which direct the network to produce the first and second target in the
        // sequence "
        for( int t = 0; t < length; ++t ) {
            // don't set a bit when there's an target given:
            boolean hasTargetBit = false;
            for( int t2 = 0; t2 < _prompts; ++t2 ) {
                int targetTime = targetTimes.get( t2 );
                if( targetTime == t ) {
                    hasTargetBit = true;
                    break;
                }
            }
            if( hasTargetBit ) {
                continue;
            }

            // else pick a random distractor bit:
            int distractorBit = _targets + _prompts + _r.nextInt( _distractors );
            _sequenceState._values[ t * observations + distractorBit ] = 1.f; // these inputs are the targets
        }

        //print();
    }

    int getNbrTargets() {
        return _targets;
    }

    int getNbrPrompts() {
        return _prompts;
    }

    int getNbrDistractors() {
        return _distractors;
    }

    public void print() {

        // print column titles
        int observations = getNbrObservations();
        int length = observations;

        for( int c = 0; c < length; ++c ) {
            if( c < 10 ) {
                System.err.print( "0" );
            }
            System.err.print( c );
            System.err.print( " " );
        }

        System.err.print( "| " );

        for( int c = 0; c < length; ++c ) {
            if( c < 10 ) {
                System.err.print( "0" );
            }
            System.err.print( c );
            System.err.print( " " );
        }

        System.err.println();

        // Row 2: Bit assignments
        length = _targets;
        for( int c = 0; c < length; ++c ) {
            System.err.print( "T  " );
        }

        length = _prompts;
        for( int c = 0; c < length; ++c ) {
            System.err.print( "P  " );
        }

        length = _distractors;
        for( int c = 0; c < length; ++c ) {
            System.err.print( "D  " );
        }

        System.err.print( "| " );

        length = _targets;
        for( int c = 0; c < length; ++c ) {
            System.err.print( "T  " );
        }

        length = _prompts;
        for( int c = 0; c < length; ++c ) {
            System.err.print( "P  " );
        }

        length = _distractors;
        for( int c = 0; c < length; ++c ) {
            System.err.print( "D  " );
        }

        System.err.println();

        // ok now print the actual inputs and outputs
        for( int sequence = 0; sequence < _sequenceLength; ++sequence ) {

            for( int c = 0; c < observations; ++c ) {
                String s = " - ";
                float r = _sequenceState._values[ sequence * observations + c ];
                if( r > 0.f ) {
                    if( c < ( _targets + _prompts ) ) {
                        s = " * ";
                    }
                    else {
                        s = " + ";
                    }
                }

                System.err.print( s );
            }

            System.err.print( "| " );

            for( int c = 0; c < _targets; ++c ) {
                String s = " - ";
                float r = _sequenceActions._values[ sequence * _targets + c ];
                if( r > 0.f ) {
                    if( c < ( _targets + _prompts ) ) {
                        s = " * ";
                    }
                    else {
                        s = " + ";
                    }
                }
                System.err.print( s );
            }

            System.err.println();
        }
    }

    public boolean complete() {
        if( _sequence >= _sequenceLength ) {
            return true;
        }
        return false;
    }

    public float getReward() {
        return _reward;
    }

    public Data getState() {
        return _state;
    }

    public void setActions( Data actions ) {
        _actions.copy( actions );
    }

    public Data getActions() {
        return _actions;
    }

    public Data getIdealActions() {
        return _idealActions;
    }

    protected void updateState() {
        // generate state:
        int observations = getNbrObservations();
        for( int i = 0; i < observations; ++i ) {
            _state._values [ i ] = _sequenceState._values[ _sequence * observations + i ];
        }
    }

    protected void updateIdealActions() {
        // generate ideal actions
        for( int i = 0; i < _targets; ++i ) {
            _idealActions._values[ i ] = _sequenceActions._values[ _sequence * _targets + i ];
        }
    }

    public float getRandomExpectedReward() {
        float expectedReward = 1f / (float)_targets; // will be correct 25% of the time if 4 targets
        expectedReward *= (float)_prompts; // happens this number of times
        expectedReward /= (float)_sequenceLength;
        //float t = ( _sequenceLength - _prompts ) + expectedReward; // ie 22 * 1, +
        //t /= (float)_sequenceLength; // unit value
        return expectedReward;
    }

    protected void updateReward() {
        // e.g. len 10, 2 prompts, 10-2=8,9 are the test ones
        int testStart = _sequenceLength - _prompts;

        if( _sequence < testStart ) {
            _reward = 0f;
            return; // don't care.
        }

        float maxError = 0f;

//        System.err.println( "REWARD @ seq: " + _sequence );
//        for( int i = 0; i < _targets; ++i ) {
//            float ideal = _idealActions._values[ i ];
//            float actual = _actions._values[ i ];
//            float diff = Math.abs( ideal - actual );
//            System.err.println( "T: " + i + " Ideal: " + ideal + " Actual: " + actual + " Err: " + diff );
//        }

        for( int i = 0; i < _targets; ++i ) {
            float ideal = _idealActions._values[ i ];
            float actual = _actions._values[ i ];
            float diff = Math.abs( ideal - actual );
            maxError = Math.max( diff, maxError );
        }

        _reward = 1f - maxError; // error = 0, reward = 1

        if( _reward > 0.5f ) {
            int g = 0;
            g++;
        }
    }

    public void update() {

        // .. externally: setActions(), given previous state

        updateReward();

        ++_sequence;

        if( complete() ) {
            ++_epoch;
            if( _loop ) {
                reset();
            }
        }

        updateState(); // expose next state
        updateIdealActions();
    }

}