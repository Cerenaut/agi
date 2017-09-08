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

package io.agi.core.alg;

import io.agi.core.data.Data;
import io.agi.core.data.DataSize;
import io.agi.core.orm.NamedObject;
import io.agi.core.orm.ObjectMap;

import java.awt.*;
import java.util.HashSet;

/**
 * A rough simulation of one layer of Pyramidal cells and associated interneurons.
 *
 * There would be a bad dynamic that the input becomes stable output, which removes the feedback it depends on for
 * stability, which makes the output unstable; however this presumes the input is actually really predictable.
 * In the presence of both spatial and temporal uncertainty this is not the case. So in all realistic examples the
 * algorithm should be meta-stable. Essentially when a classification is unexpected, it is the error bits that encode
 * the state of the sequence which enables future predictions to model that (which is what we actually measure for
 * success) ... which enables us to keep track of where we are in an ambiguous sequence.
 *
 * Essentially, a perfectly repeatable input is a pathological case.
 *
 * Any successfully predicted bits are modelled locally; unexpected bits are modelled with more resources. Effectively
 * we move the problem to wherever there is information to explain it. Which is the key objective of predictive coding.
 *
 * Created by dave on 4/07/16.
 */
public class PredictiveCoding extends NamedObject {

    public Data _inputObserved; // 0.5 and above considered active
    public Data _inputPredicted; // 0.5 and above considered predictions

    public Data _outputSpikesOld; // axon spikes
    public Data _outputSpikesNew; // axon spikes
    public Data _outputSpikesAge; // used to hold some spikes active and defer decay to ensure some pooling
    public Data _output; // integrated output over time

    public Data _predictionErrorFP; // output
    public Data _predictionErrorFN;

    public float _sumPredictionErrorFP = 0;
    public float _sumPredictionErrorFN = 0;

    // Member objects
    public PredictiveCodingConfig _rc;
    public PyramidRegionLayerTransient _transient = null;
//    public PyramidRegionLayerPredictor _predictor;

    public PredictiveCoding( String name, ObjectMap om ) {
        super( name, om );
    }

    public void setup( PredictiveCodingConfig pcc ) {
        _rc = pcc;

        Point inputCSize = _rc.getInputCSize();
        DataSize dataSizeCells = DataSize.create( inputCSize.x, inputCSize.y );

        _outputSpikesOld = new Data( dataSizeCells );
        _outputSpikesNew = new Data( dataSizeCells );
        _outputSpikesAge = new Data( dataSizeCells );
        _output = new Data( dataSizeCells );

        _predictionErrorFP = new Data( dataSizeCells );
        _predictionErrorFN = new Data( dataSizeCells );
//        _predictionOld = new Data( dataSizeCells );
        _inputPredicted = new Data( dataSizeCells );
//        _predictionNewUnit = new Data( dataSizeCells );
    }

    public void reset() {
        _transient = null;

        _predictionErrorFP.set( 0.f );
        _predictionErrorFN.set( 0.f );

        _outputSpikesOld.set( 0f );
        _outputSpikesNew.set( 0f );
        _outputSpikesAge.set( 0f );
        _output.set( 0f );
    }

    public void update() {

        _transient = new PyramidRegionLayerTransient();
        _transient._spikesNew = _inputObserved.indicesMoreThan( 0.5f );

//        updatePrediction( _inputC );//_inputClassifierSpikes );

        _outputSpikesOld.copy( _outputSpikesNew ); // backup old output, for diagnostics
        _outputSpikesNew.set( 0f );

        // 1. Set FP / FN status of all currently active bits.
        _predictionErrorFP.set( 0.f );
        _predictionErrorFN.set( 0.f );


        _transient._predictionErrorFP = new HashSet< Integer >();
        _transient._predictionErrorFN = new HashSet< Integer >();

        // calculate FP and FN errors
        for( Integer i : _transient._spikesNew ) {

            // To cause a break in cycles where cells might be continuously output, we inhibit the output of any cell
            // that becomes predicted. Since we already updated the prediction, the OLD prediction is the one we need
            // to compare against the current classification.
            float prediction = _inputPredicted._values[ i ];
            //float predictionInhibition = _predictionInhibition._values[ i ]; not used currently

            // Detect false-negative errors (active but not predicted)
            boolean errorFN = false;

            //if( predictionInhibition > 0.f ) {
            //    errorFN = true; // if you inhibit the prediction, then it can't be predicted
            //}
            //else if( prediction < 1.f ) {
            if( prediction < 0.5f ) {
                errorFN = true; // cell becomes active, wasn't predicted
            }

            // rule: don't re-add old bits. they only get added when they turn on, not when they continue to be on.
            // Is this a good thing? Maybe this bit represents something in the new input too.
            // But, this is kinda irrelevant because there's now a refractory period in the classification. [NOT TRUE]
            // So, there's not much point in this, because it means the classification has changed
            // at least N times before the bit can become active again anyway, so it'd be a genuine re-activation.
//            if( _transient._classifierSpikesOld.contains( i ) ) {
//                errorFN = false; // if already active before, don't re-add it.
//            }

            if( errorFN ) {
                _transient._predictionErrorFN.add( i );
                _predictionErrorFN._values[ i ] = 1.f;
            }
        }

        // Now look for False-positive errors (mainly for diagnostics, not integral to the algorithm)
        HashSet< Integer > predictions = _inputPredicted.indicesMoreThan( 0.5f ); //_predictor._statePredicted.indicesMoreThan( 0.5f );

        for( Integer p : predictions ) {

            boolean errorFP = false;

            if( !_transient._spikesNew.contains( p ) ) {
                errorFP = true; // predicted, but not active
            }

            if( errorFP ) {
                _transient._predictionErrorFP.add( p );
                _predictionErrorFP._values[ p ] = 1.f;
            }
        }

        // decay existing values depending on how many new bits are introduced
        // this means that when stability is achieved, history can become arbitrarily long..
        // http://www.wolframalpha.com/input/?i=y+%3D+0.9%5Ex+for+x+%3D+0+to+20
        float outputDecayRate = _rc.getOutputDecayRate();
        float errorBits = (float)_transient._predictionErrorFN.size();
        float decayRate = (float)Math.pow( outputDecayRate, errorBits ); // N.B if errorBits = 0 then decayRate = 1 (unchanged)
        _output.mul( decayRate );

        // restore any recent spikes to full output, based on age
        // This is to ensure some temporal pooling occurs.
        float ageMax = _rc.getOutputSpikeAgeMax();
        int cells = _outputSpikesAge.getSize();
        for( int c = 0; c < cells; ++c ) {
            float oldAge = _outputSpikesAge._values[ c ];
            if( oldAge <= 0f ) {
                continue; // cell is decaying or silent already
            }

            // MaxAge = 2
            // Spike Age (in) Age (out) Output
            //  1       0        1         1
            //  0       1        2         1
            //  0       2      3=>0        0
            float newAge = oldAge +1;

            if( oldAge > ageMax ) {
                newAge = 0;
            }
            else { // oldAge <= ageMax
                _output._values[ c ] = 1f;
            }

            _outputSpikesAge._values[ c ] = newAge;
        }

        // deal with new spikes
        _transient._spikesOut = new HashSet< Integer >();

        for( Integer i : _transient._predictionErrorFN ) {
            _outputSpikesNew._values[ i ] = 1.f;
            _outputSpikesAge._values[ i ] = 1.f; // nonzero
            _transient._spikesOut.add( i );
            _output._values[ i ] = 1f;
        }

        _sumPredictionErrorFP = _transient._predictionErrorFP.size();
        _sumPredictionErrorFN = _transient._predictionErrorFN.size();
    }
}
