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
public class SpikeOrderLearning {

//    public Data _inputOutputTraces;
//    public Data _outputTraces;
    public Data _outputPredicted;
    public Data _outputPredictedReal;
    public Data _weights;

    public float _learningRate = 0.01f; // e.g. moves 1 part in 100 each step. Max value is 99, min is 0, giving 100 intervals.
    public float _traceDecayRate = 0.5f; // e.g. moves 1 part in 100 each step. Max value is 99, min is 0, giving 100 intervals.

    public void setup( int inputs, int outputs, float learningRate ) {//, float traceDecayRate ) {

        _learningRate = learningRate;
//        _traceDecayRate = traceDecayRate;

//        _inputOutputTraces = new Data( DataSize.create( inputs, outputs ) );
//        _outputTraces = new Data( DataSize.create( outputs ) );
        _outputPredicted = new Data( DataSize.create( outputs ) );
        _outputPredictedReal = new Data( DataSize.create( outputs ) );
        _weights = new Data( DataSize.create( inputs, outputs ) );
    }

    /**
     * Although there's no real need for a reset, because it's completely online, this can accelerate unlearning.
     * Which might be useful for you.
     */
    public void reset() {
        _weights.set( 0.0f );
//        _inputOutputTraces.set( 0f );
//        _outputTraces.set( 0f );
    }

    public Data getPrediction() {
        return _outputPredicted;
    }

    /**
     * Generates a new prediction based on the current state and context bits.
     */
    public void predict( Data inputSpikes, int density ) {
        predict( inputSpikes, _weights, _outputPredicted, _outputPredictedReal, density );
    }

    /**
     * Predict which bits will be active next. Threshold at rank = density.
     *
     */
    public static void predict( Data inputSpikes, Data weights, Data outputPredicted, Data outputPredictedRaw, int density ) {

        int inputs = inputSpikes.getSize();
        int outputs = outputPredicted.getSize();

        // One weight for each state s1, to each state s2, from each context bit.
        // w = P( s2=1 | s1=1, w_n=1 )
        // S_1 * W * S_2

        TreeMap< Float, ArrayList< Integer > > ranking = new TreeMap< Float, ArrayList< Integer > >();

        float wMax = 0f;

        for( int j = 0; j < outputs; ++j ) {

            float wSum = 0.f;
//            int wCount = 0;

            // Find the mean frequency over all the contextual inputs
            for( int i = 0; i < inputs; ++i ) {

//                float active = input._values[ s1 ];
//
//                if( active < 1.f ) {
//                    continue; // says nothing about the cell s2
//                }

                int offset = i * outputs + j; // the weight from state i, to state j.

                float trace = inputSpikes._values[ i ];
                float weight = weights._values[ offset ];

                wSum += ( weight * trace);
//                wCount += 1;
            }

            if( wSum > wMax ) {
                wMax = wSum;
            }

            Ranking.add( ranking, wSum, j );

            outputPredictedRaw._values[ j ] = wSum;
        }

        // normalize the raw weights:
        if( wMax > 0f ) {
            float scaling = 1f / wMax; // e.g. if wMax = 0.5. 1/0.5 = 2. So 0.5 => 1
            for( int j = 0; j < outputs; ++j ) {
                outputPredictedRaw._values[ j ] *= scaling;
            }
        }

        int maxRank = density;
        boolean findMaxima = true; // keep the youngest
        HashSet< Integer > mostPredicted = new HashSet< Integer >();
        Ranking.getBestValues( ranking, findMaxima, maxRank, mostPredicted );

        outputPredicted.set( 0.f );

        for( Integer s2 : mostPredicted ) {
            outputPredicted._values[ s2 ] = 1.f;
        }
    }

    /**
     * Call this when there is a post-
     */
    public void train( Data inputSpikesOld, Data inputSpikesNew, Data outputSpikesOld, Data outputSpikesNew ) {

        // I could implement STDP rule
        // where "early" bits predict and "late" bits inhibit
        // here we would have traces before and after. Or just prior/next states
        // So if bit A was active and now bit B is active, then we reduce the weight between bit B -> A
        // http://www.scholarpedia.org/article/Spike-timing_dependent_plasticity

        // we treat the old state as synapses (presynaptic spikes) for a postsynaptic cell which is the new state.
        // The synapse strengthens if the pre-syn. bit is active BEFORE (not simultaneously) the post-syn bit.
        // If the pre-syn bit is active AFTER the post-syn bit BECOMES active, then it is weakened.
        //
        // So the post syn spike occurs when old=0 and new=1
        //   On a post-syn spike:
        // The good pre-syn spike occurs when old=1 and new=0                             +w
        // The  bad pre-syn spike is     when old=1 and new=1                             -w or 0?
        // If the pre-syn cell is             old=0 and new=0 then no learning occurs
        // If the pre-syn cell is             old=0 and new=1 then                        -w
        //
        // When no post syn spike:
        // According to STDP, no learning. If they're random (no association) then pre and post will occur in the bad
        // timing more often, esp if a cell stays active a lot. This means the weight will decay.

        // Continuous trace version: there are traces on the input spikes to make the timing a little more flexible.
        // we only

        int inputs = inputSpikesOld.getSize();
        int outputs = outputSpikesOld.getSize();

        // update the post-synaptic traces for each output
/*        for( int j = 0; j < outputs; ++j ) { // post
            float traceOld = _outputTraces._values[ j ];
            float traceNew = traceOld * _traceDecayRate; // default: decay the trace.
            float postNew = outputSpikes._values[ j ];
            if( postNew == 1f ) {
                traceNew = 1f; // new spikes have a value of 1.
            }

            _outputTraces._values[ j ] = traceNew;
        }*/

        // update the weights given EXISTING, older pre-synaptic traces with each output and new input spikes and new post-synaptic spike/traces.
        for( int i = 0; i < inputs; ++i ) { // pre

            float preSpikeOld = inputSpikesOld._values[ i ];
            float preSpikeNew = inputSpikesNew._values[ i ];

            if(    ( preSpikeOld == 0f )     //
                && ( preSpikeNew == 0f ) ) { // pre never active
                continue;
            }

            for( int j = 0; j < outputs; ++j ) { // post

                int offset = i * outputs + j; // the weight from state s1, to state s2.

                float postOld = outputSpikesOld._values[ j ];
                float postNew = outputSpikesNew._values[ j ];

                float delta = 0f;

                if(    ( postOld == 1f )     // post was already active, not a new spike
                    || ( postNew == 0f ) ) { // post isn't active
                    continue;
                }

                // ie post went from 0 to 1, a new spike
                if( ( preSpikeOld == 0f ) && ( preSpikeNew == 1f ) ) { // became active at same time
                    delta = - _learningRate; // bad - the input is late
                }
                else { // already active, because at least one was non zero, or continuously active which is predictive
                    delta = _learningRate; // ... except this one. The input was early or ongoing
                }

                // at least 1 is active, possibly both but that'd be rare as it's supposed to have a wait period
                float oldWeight = _weights._values[ offset ];
                float newWeight = oldWeight + delta;
                newWeight = Math.max( 0, Math.min( 1f, newWeight ) );
                _weights._values[ offset ] = newWeight;
            }
        }

        // update the pre-synaptic traces for each output
/*        for( int i = 0; i < inputs; ++i ) { // pre

            float spike = inputSpikes._values[ i ];

            for( int j = 0; j < outputs; ++j ) { // post
                int offset = i * outputs + j; // the weight from state s1, to state s2.

                float traceOld = _inputOutputTraces._values[ offset ];
                float traceNew = traceOld * _traceDecayRate; // default: decay the trace.
                if( spike == 1f ) {
                    traceNew = 1f; // new spikes have a value of 1.
                }

                _inputOutputTraces._values[ offset ] = traceNew;
            }
        }*/

    }

}
