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

package io.agi.core.alg;

import io.agi.core.ann.unsupervised.HebbianLearning;
import io.agi.core.ann.unsupervised.KSparseAutoencoder;
import io.agi.core.ann.unsupervised.SpikeOrderLearning;
import io.agi.core.ann.unsupervised.SpikeTimingLearning;
import io.agi.core.data.Data;
import io.agi.core.data.DataSize;
import io.agi.core.data.Ranking;
import io.agi.core.orm.NamedObject;
import io.agi.core.orm.ObjectMap;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeMap;

/**
 * A rough simulation of one layer of Pyramidal cells and associated interneurons.
 *
 * Created by dave on 4/07/16.
 */
public class PyramidRegionLayer extends NamedObject {

    public Data _inputC1;
    public Data _inputC2;

    public Data _inputP1;
    public Data _inputP2;

    public Data _inputOld; // all input
    public Data _inputNew;

    public Data _spikesOld; // apical dendrite spikes
    public Data _spikesNew;
//    public Data _spikesIntegrated; // apical dendrite spikes
    public Data _outputSpikesOld; // axon spikes
    public Data _outputSpikesNew; // axon spikes
    public Data _outputSpikesAge; // axon spikes

    public Data _predictionErrorFP;
    public Data _predictionErrorFN;
    public Data _predictionOld;
    public Data _predictionNew;
    public Data _predictionNewReal;

    //    todo add something - maybe an external entity - that keeps a rolling history of some of these datas

    public float _sumClassifierError = 0;
    public float _sumClassifierResponse = 0;
    public float _sumOutputSpikes = 0;
    public float _sumPredictionErrorFP = 0;
    public float _sumPredictionErrorFN = 0;
    public float _sumIntegration = 0;

    // Member objects
    public PyramidRegionLayerConfig _rc;
    public PyramidRegionLayerTransient _transient = null;
    public KSparseAutoencoder _classifier;
    public SpikeOrderLearning _predictor;

    public PyramidRegionLayer( String name, ObjectMap om ) {
        super( name, om );
    }

    public void setup( PyramidRegionLayerConfig rc ) {
        _rc = rc;

        _classifier = new KSparseAutoencoder( getKey( AutoRegionLayerConfig.SUFFIX_CONTEXT_FREE ), _rc._om );
        _classifier.setup( _rc._classifierConfig );

        // Get dimensions
        Point inputC1Size = _rc.getInputC1Size();
        Point inputC2Size = _rc.getInputC2Size();
        Point inputP1Size = _rc.getInputP1Size();
        Point inputP2Size = _rc.getInputP2Size();

        DataSize dataSizeInputC1 = DataSize.create( inputC1Size.x, inputC1Size.y );
        DataSize dataSizeInputC2 = DataSize.create( inputC2Size.x, inputC2Size.y );

        DataSize dataSizeInputP1 = DataSize.create( inputP1Size.x, inputP1Size.y );
        DataSize dataSizeInputP2 = DataSize.create( inputP2Size.x, inputP2Size.y );


        // Predictor
        int cells = _rc._classifierConfig.getNbrCells();
        int predictorInputs = dataSizeInputP1.getVolume() + dataSizeInputP2.getVolume() + cells;
        int predictorOutputs = cells;
        float predictorLearningRate = rc.getPredictorLearningRate();
//        float predictorDecayRate = rc.getPredictorTraceDecayRate();

        _predictor = new SpikeOrderLearning();
        _predictor.setup( predictorInputs, predictorOutputs, predictorLearningRate );//, predictorDecayRate );

        DataSize dataSizeCells = DataSize.create( _rc._classifierConfig.getWidthCells(), _rc._classifierConfig.getHeightCells() );

        _inputC1 = new Data( dataSizeInputC1 );
        _inputC2 = new Data( dataSizeInputC2 );

        _inputP1 = new Data( dataSizeInputP1 );
        _inputP2 = new Data( dataSizeInputP2 );

        DataSize dataSizeInputC = DataSize.create( predictorInputs );

        _inputOld = new Data( dataSizeInputC );
        _inputNew = new Data( dataSizeInputC );

        _spikesOld = new Data( dataSizeCells );
        _spikesNew = new Data( dataSizeCells );
//        _spikesIntegrated = new Data( dataSizeCells );

        _outputSpikesOld = new Data( dataSizeCells );
        _outputSpikesNew = new Data( dataSizeCells );
        _outputSpikesAge = new Data( dataSizeCells );

        _predictionErrorFP = new Data( dataSizeCells );
        _predictionErrorFN = new Data( dataSizeCells );
        _predictionOld = new Data( dataSizeCells );
        _predictionNew = new Data( dataSizeCells );
        _predictionNewReal = new Data( dataSizeCells );
    }

    public void reset() {
        _transient = null;
        _classifier.reset();
        _predictor.reset();

        _predictionErrorFP.set( 0.f );
        _predictionErrorFN.set( 0.f );
        _predictionOld.set( 0.f );
        _predictionNew.set( 0.f );
        _predictionNewReal.set( 0.f );

        _outputSpikesOld.set( 0f );
        _outputSpikesNew.set( 0f );
        _outputSpikesAge.set( 0f );

        // classifier cell mask will be reset to all 1 also.
    }

    public void update() {

        // This alg is: TS/PC/SpikingNN/Sparse Coding/BP via autoencoder and STDP.
        // Data structures
        // The inputs are spikes
        // The outputs are therefore also spikes,
        // When the input arrives it is classified
        // The classification produces a current state, of fixed density
        // The prediction is updated from the current state - fixed density or threshold?
        // Prediction errors are computed and emitted.
        // But SOMETHING needs to change slowly.
        // What if the cell accumulates a charge via integration
        // we subtract the predictions
        // and we add the spikes
        // We send a spike (a 1 bit) whenever a cell net C-P value passes a threshold (ie not every step)
        // We have a leaky integrator too.
        // When a cell spikes, the integration is zeroed. We don't need a refractory period because it has to re-integrate
        // We train the classifier on every input
        // We train the predictor on every ?classifier or integrator? spike. The most recent input bits drove it over the threshold.
        // Can have short traces for prediction training
        // Every region-layer performs this process continuously.
        // If each cell changes slowly, does this imply temporal pooling? Because
        // layer 1: inputs every step
        // layer 2: 1 inputs every n steps (where n is max firing rate)
        // layer 3: 1 inputs every n*n steps . So it has temporal slowness also.

        _classifier._c.setLearn( _rc.getLearn() );

        updateClassification();
        updateIntegration();
        updatePrediction();
    }

    protected void updateIntegration() {
        // 1. leak the state of each cell
        // 2. add errors to integrated state of each cell
        // 3. Compute spikes and reset spiking states.
//        float cellDecay = _rc.getIntegrationDecayRate();
//        float spikeWeight = _rc.getIntegrationSpikeWeight();
        int cells = _classifier._c.getNbrCells();

        _outputSpikesOld.copy( _outputSpikesNew );
        _transient._spikesOut = new HashSet< Integer >();

        _sumOutputSpikes = 0;
        _sumPredictionErrorFP = 0;
        _sumPredictionErrorFN = 0;
//        _sumIntegration = 0;

        float outputSpikeAgeMax = _rc.getOutputSpikeAgeMax();

        for( int c = 0; c < cells; ++c ) {
            float classifierSpike = 0f;
            if( _transient._spikesNew.contains( c ) ) {
                classifierSpike = 1f;
            }

            float predictionSpike = _predictionNew._values[ c ]; // this is the latest prediction from last iter.

            // since we integrate dendrite spikes, we are invariant to the number of inputs.
            float classifierWeight = classifierSpike;
            float predictionWeight = predictionSpike;
            float predictionErrorFP = predictionWeight - classifierWeight;
            float predictionErrorFN = classifierWeight - predictionWeight; // ie +1 if active, but not predicted

            predictionErrorFP = Math.max( 0f, predictionErrorFP ); // 1 if predicted but not active
            predictionErrorFN = Math.max( 0f, predictionErrorFN ); // ie +1 if active, but not predicted. can't use prediction to make it negative.

            _predictionErrorFP._values[ c ] = predictionErrorFP;
            _predictionErrorFN._values[ c ] = predictionErrorFN;

            // add new FN spikes (long term) and age any existing ones
            float outputSpikeAge = _outputSpikesAge._values[ c ];
            if( predictionErrorFN > 0f ) {
                outputSpikeAge = outputSpikeAgeMax;
            }
            else {
                outputSpikeAge -= 1f;
                outputSpikeAge = Math.max( 0, outputSpikeAge );
            }

            float outputSpike = 0f;

            if(    ( outputSpikeAge > 0f )
                || ( classifierSpike > 0f ) ) {
                outputSpike = 1f;
                _transient._spikesOut.add( c );
            }

            //FN = was not active, now IS active, was not predicted. All other cases normal
            //spike active cells once, spike fns for N steps (countdown)

            // store result
            _outputSpikesNew._values[ c ] = outputSpike;
            _outputSpikesAge._values[ c ] = outputSpikeAge;
//            _spikesIntegrated._values[ c ] = newIntegration;

            // instrumentation
            _sumOutputSpikes += outputSpike;
            _sumPredictionErrorFP += predictionErrorFP;
            _sumPredictionErrorFN += predictionErrorFN;
//            _sumIntegration += newIntegration;
        }
    }

    protected void updateClassification() {
        _transient = new PyramidRegionLayerTransient();
        _transient._inputC1Active = _inputC1.indicesMoreThan( 0.f ); // find all the active bits.
        _transient._inputC2Active = _inputC2.indicesMoreThan( 0.f ); // find all the active bits.

        // Binary
        int size1 = _inputC1.getSize();
        int size2 = _inputC2.getSize();
        int offset = 0;
        /*
        _contextFreeClassifier._inputValues.set( 0.f );

        for( Integer i : _transient._input1Active ) {
            _classifier._inputValues._values[ i ] = 1.f;
        }

        offset += size1;

        // Original
        for( Integer i : _transient._input2Active ) {
            _classifier._inputValues._values[ offset + i ] = 1.f;
        }*/

        // real:
        _classifier._inputValues.copyRange( _inputC1, offset, 0, size1 );
        offset += size1;
        _classifier._inputValues.copyRange( _inputC2, offset, 0, size2 );

        // update
        _classifier.update(); // produces a new classification
        _spikesOld.copy( _spikesNew );
        _spikesNew.copy( _classifier._cellActivity );

        _transient._spikesNew = _spikesNew.indicesMoreThan( 0.5f );
        _transient._spikesOld = _spikesOld.indicesMoreThan( 0.5f );

        _sumClassifierError = _classifier._sumTopKError;
        _sumClassifierResponse = _classifier._sumResponse;
    }

    protected void updatePrediction() {
        int density = _spikesNew.indicesMoreThan( 0f ).size();
        //     public void predict( Data inputSpikes, int density )
        //     public void train( Data inputSpikes, Data outputSpikes )
        Point inputP1Size = _rc.getInputP1Size();
        Point inputP2Size = _rc.getInputP2Size();
        DataSize dataSizeInputP1 = DataSize.create( inputP1Size.x, inputP1Size.y );
        DataSize dataSizeInputP2 = DataSize.create( inputP2Size.x, inputP2Size.y );
        int inputP1Volume = dataSizeInputP1.getVolume();
        int inputP2Volume = dataSizeInputP2.getVolume();

        int cells = _rc._classifierConfig.getNbrCells();
        int inputs = inputP1Volume + inputP2Volume + cells;
//        Data inputSpikes = new Data( inputs );

        // copy the input spike data.
        _inputOld.copy( _inputNew ); // a complete copy

        int offset = 0;
        _inputNew.copyRange( _spikesNew, offset, 0, cells );
        offset += cells;
        _inputNew.copyRange( _inputP1, offset, 0, inputP1Volume );
        offset += inputP1Volume;
        _inputNew.copyRange( _inputP2, offset, 0, inputP2Volume );
        //offset += inputP2Volume;

        // train the predictor
        _predictor.train( _inputOld, _inputNew, _spikesOld, _spikesNew );

        // generate a new prediction
        _predictor.predict( _inputNew, density );
        _predictionOld .copy( _predictionNew ); // the old prediction
        _predictionNew .copy( _predictor._outputPredicted ); // copy the new prediction
        _predictionNewReal.copy( _predictor._outputPredictedReal ); // copy the new prediction
    }
}
