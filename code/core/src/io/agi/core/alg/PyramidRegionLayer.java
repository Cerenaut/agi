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

import io.agi.core.ann.unsupervised.OnlineKSparseAutoencoder;
import io.agi.core.ann.unsupervised.SpikeOrderLearning;
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
public class PyramidRegionLayer extends NamedObject {

    public Data _inputC1; // real, unit
    public Data _inputC2;
    public Data _inputC1Predicted; // a prediction of what the input will be
    public Data _inputC2Predicted; // a prediction of what the input will be

    public Data _inputP1; // real, unit
    public Data _inputP2;

    public Data _inputPOld; // all input
    public Data _inputPNew;

    public Data _spikesOld; // apical dendrite spikes
    public Data _spikesNew;
//    public Data _spikesIntegrated; // apical dendrite spikes
    public Data _outputSpikesOld; // axon spikes
    public Data _outputSpikesNew; // axon spikes
//    public Data _outputSpikesAge; // axon spikes
    public Data _output; // integrated output over time

    public Data _predictionErrorFP;
    public Data _predictionErrorFN;
    public Data _predictionOld;
    public Data _predictionNew;
//    public Data _predictionNewReal;

    //    todo add something - maybe an external entity - that keeps a rolling history of some of these datas

    public float _sumClassifierError = 0;
    public float _sumClassifierResponse = 0;
    public float _sumOutputSpikes = 0;
    public float _sumPredictionErrorFP = 0;
    public float _sumPredictionErrorFN = 0;
//    public float _sumIntegration = 0;

    // Member objects
    public PyramidRegionLayerConfig _rc;
    public PyramidRegionLayerTransient _transient = null;
    public OnlineKSparseAutoencoder _classifier;
//    public SpikeOrderLearning _predictor;
    public PyramidRegionLayerPredictor _predictor;

    public PyramidRegionLayer( String name, ObjectMap om ) {
        super( name, om );
    }

    public void setup( PyramidRegionLayerConfig rc ) {
        _rc = rc;

        _classifier = new OnlineKSparseAutoencoder( getKey( PyramidRegionLayerConfig.SUFFIX_CLASSIFIER ), _rc._om );
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
        int predictorHiddenCells = _rc.getPredictorHiddenCells();
        float predictorLeakiness = _rc.getPredictorLeakiness();
        float predictorRegularization = _rc.getPredictorRegularization();
        int predictorBatchSize = _rc.getPredictorBatchSize();
        _predictor = new PyramidRegionLayerPredictor();//SpikeOrderLearning();
        _predictor.setup( _rc.getKey( PyramidRegionLayerConfig.SUFFIX_PREDICTOR ), _rc._om, _rc._r, predictorInputs, predictorHiddenCells, predictorOutputs, predictorLearningRate, predictorLeakiness, predictorRegularization, predictorBatchSize );//, predictorDecayRate );
    // public void setup( String name, ObjectMap om, Random r, int inputs, int hidden, int outputs, float learningRate, float leakiness, float regularization ) {

        DataSize dataSizeCells = DataSize.create( _rc._classifierConfig.getWidthCells(), _rc._classifierConfig.getHeightCells() );

        _inputC1 = new Data( dataSizeInputC1 );
        _inputC2 = new Data( dataSizeInputC2 );

        _inputC1Predicted = new Data( dataSizeInputC1 );
        _inputC2Predicted = new Data( dataSizeInputC2 );

        _inputP1 = new Data( dataSizeInputP1 );
        _inputP2 = new Data( dataSizeInputP2 );

        DataSize dataSizeInputP = DataSize.create( predictorInputs );

        _inputPOld = new Data( dataSizeInputP );
        _inputPNew = new Data( dataSizeInputP );

        _spikesOld = new Data( dataSizeCells );
        _spikesNew = new Data( dataSizeCells );
//        _spikesIntegrated = new Data( dataSizeCells );

        _outputSpikesOld = new Data( dataSizeCells );
        _outputSpikesNew = new Data( dataSizeCells );
//        _outputSpikesAge = new Data( dataSizeCells );
        _output = new Data( dataSizeCells );

        _predictionErrorFP = new Data( dataSizeCells );
        _predictionErrorFN = new Data( dataSizeCells );
        _predictionOld = new Data( dataSizeCells );
        _predictionNew = new Data( dataSizeCells );
//        _predictionNewReal = new Data( dataSizeCells );
    }

    public void reset() {
        _transient = null;
        _classifier.reset();
        _predictor.reset();

        _predictionErrorFP.set( 0.f );
        _predictionErrorFN.set( 0.f );
        _predictionOld.set( 0.f );
        _predictionNew.set( 0.f );
//        _predictionNewReal.set( 0.f );

        _outputSpikesOld.set( 0f );
        _outputSpikesNew.set( 0f );
//        _outputSpikesAge.set( 0f );
        _output.set( 0f );

        _inputC1Predicted.set( 0f );
        _inputC2Predicted.set( 0f );
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
        //updateIntegration();
        updatePrediction();
        invertPrediction();
        updateOutput();
    }

    protected void updateClassification() {
        _transient = new PyramidRegionLayerTransient();
//        _transient._inputC1Active = _inputC1.indicesMoreThan( 0.f ); // find all the active bits.
//        _transient._inputC2Active = _inputC2.indicesMoreThan( 0.f ); // find all the active bits.

        // Binary
        int size1 = _inputC1.getSize();
        int size2 = _inputC2.getSize();
        int offset = 0;

        // real:
        _classifier._inputValues.copyRange( _inputC1, offset, 0, size1 );
        offset += size1;
        _classifier._inputValues.copyRange( _inputC2, offset, 0, size2 );

        // update
        _classifier.update(); // produces a new classification
        _spikesOld.copy( _spikesNew );
        _spikesNew.copy( _classifier._cellSpikesTopKA );

        _transient._spikesNew = _spikesNew.indicesMoreThan( 0.5f );
        _transient._spikesOld = _spikesOld.indicesMoreThan( 0.5f );

//        _sumClassifierError = _classifier._sumTopKError;
//        _sumClassifierResponse = _classifier._sumResponse;
    }

    protected void invertPrediction() {

        // find top K outputs and invert through the classifier
        // I can do it by threshold OR by ranking. Trying threshold for now
        HashSet< Integer > predictions = _predictionOld.indicesMoreThan( 0.5f ); //_predictor._statePredicted.indicesMoreThan( 0.5f );

        Data hiddenActivity = new Data( _classifier._cellSpikesTopK._dataSize );

        for( Integer p : predictions ) {
            hiddenActivity._values[ p ] = 1f;
        }

        Data inputReconstructionWeightedSum = new Data( _classifier._inputValues._dataSize ); // not used
        Data inputReconstructionTransfer = new Data( _classifier._inputValues._dataSize );

        _classifier.reconstruct( hiddenActivity, inputReconstructionWeightedSum, inputReconstructionTransfer );

        inputReconstructionTransfer.clipRange( 0f, 1f ); // as NN may go wildly beyond that

        // now split the reconstruction back into the 2 inputs
        int size1 = _inputC1.getSize();
        int size2 = _inputC2.getSize();
        int offset = 0;

        // real:
        _inputC1Predicted.copyRange( inputReconstructionTransfer, offset, 0, size1 );
        offset += size1;
        _inputC2Predicted.copyRange( inputReconstructionTransfer, offset, 0, size2 );
    }

    protected void updatePrediction() {
        int density = _spikesNew.indicesMoreThan( 0f ).size();
        //     public void predict( Data inputSpikes, int density )
        //     public void train( Data inputSpikes, Data outputSpikes )
        Point inputP1Size = _rc.getInputP1Size();
        Point inputP2Size = _rc.getInputP2Size();

        DataSize dataSizeInputP1 = DataSize.create( inputP1Size.x, inputP1Size.y );
        DataSize dataSizeInputP2 = DataSize.create(inputP2Size.x, inputP2Size.y);

        int inputP1Volume = dataSizeInputP1.getVolume();
        int inputP2Volume = dataSizeInputP2.getVolume();

        int cells = _rc._classifierConfig.getNbrCells();
//        int inputs = inputP1Volume + inputP2Volume + cells;
//        Data inputSpikes = new Data( inputs );

        // copy the input spike data.
        _inputPOld.copy( _inputPNew ); // a complete copy

        int offset = 0;
        _inputPNew.copyRange( _spikesNew, offset, 0, cells );
        offset += cells;
        _inputPNew.copyRange( _inputP1, offset, 0, inputP1Volume );
        offset += inputP1Volume;
        _inputPNew.copyRange( _inputP2, offset, 0, inputP2Volume );
        //offset += inputP2Volume;

        // train the predictor
        _predictor.train( _inputPOld, density, _spikesNew );//_inputOld, _inputNew, _spikesOld, _spikesNew );

        // generate a new prediction
        _predictionOld .copy( _predictionNew );
        _predictor.predict( _inputPNew, density, _predictionNew );
//        _predictionNew .copy( _predictor._outputPredicted ); // copy the new prediction
//        _predictionNewReal.copy( _predictor._outputPredictedReal ); // copy the new prediction
    }

/*    protected void updateIntegration() {
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
    }*/

    // NOTE that prediction feedback DOES affect perception. The modelling is in terms of errors, so when prediction
    // changes the error model will be a function of the prediction.

//add prediction bias to training - somehow specialize to context
//e.g. 4/9 similarity
//make '4' cells become more attuned to 4s and less to 9s
//perhaps, if a cell wins and was predicted, learn harder?
//Or
//    if a cell doesnt win..
//    I need to pull apart the ambiguity
//    If it wins but WASNT predicted ..?
//
//alternative: select witnner with bias from context.
    /**
     * Continuous output of false-negative spikes, decaying exponentially.
     */
    protected void updateOutput() {

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
            float prediction = _predictionOld._values[ i ];
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
//            if( _transient._spikesOld.contains( i ) ) {
//                errorFN = false; // if already active before, don't re-add it.
//            }

            if( errorFN ) {
                _transient._predictionErrorFN.add( i );
                _predictionErrorFN._values[ i ] = 1.f;
            }
        }

        // Now look for False-positive errors (mainly for diagnostics, not integral to the algorithm)
        HashSet< Integer > predictions = _predictionOld.indicesMoreThan( 0.5f ); //_predictor._statePredicted.indicesMoreThan( 0.5f );

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

        float outputDecayRate = this._rc.getOutputDecayRate();

        _output.mul( outputDecayRate );

        _transient._spikesOut = new HashSet< Integer >();

        for( Integer i : _transient._predictionErrorFN ) {
            _outputSpikesNew._values[ i ] = 1.f;
            _transient._spikesOut.add( i );
            _output._values[ i ] = 1f;
        }

        _sumPredictionErrorFP = _transient._predictionErrorFP.size();
        _sumPredictionErrorFN = _transient._predictionErrorFN.size();
    }

/*    protected void updateOutput() {// ExcludePredicted() {//} HashSet< Integer > activeNew ) {

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
            float prediction = _predictionOld._values[ i ];
            //float predictionInhibition = _predictionInhibition._values[ i ]; not used currently

            // Detect false-negative errors (active but not predicted)
            boolean errorFN = false;

            //if( predictionInhibition > 0.f ) {
            //    errorFN = true; // if you inhibit the prediction, then it can't be predicted
            //}
            //else if( prediction < 1.f ) {
            if( prediction < 1.f ) {
                errorFN = true; // cell becomes active, wasn't predicted
            }

            // rule: don't re-add old bits. they only get added when they turn on, not when they continue to be on.
            // Is this a good thing? Maybe this bit represents something in the new input too.
            // But, this is kinda irrelevant because there's now a refractory period in the classification.
            // So, there's not much point in this, because it means the classification has changed
            // at least N times before the bit can become active again anyway, so it'd be a genuine re-activation.
            if( _transient._spikesOld.contains( i ) ) {
                errorFN = false; // if already active before, don't re-add it.
            }

            if( errorFN ) {
                _transient._predictionErrorFN.add( i );
                _predictionErrorFN._values[ i ] = 1.f;
            }
        }

        // Now look for False-positive errors (mainly for diagnostics, not integral to the algorithm)
        HashSet< Integer > predictions = _predictionOld.indicesMoreThan( 0.5f ); //_predictor._statePredicted.indicesMoreThan( 0.5f );

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

        // 2. Increase age of current output, and exclude anything that has become predicted
        HashSet< Integer > predictionsNew = _predictionNew.indicesMoreThan( 0.5f ); // the latest prediction

        int cells = _classifier._c.getNbrCells();

        _transient._spikesOut = new HashSet< Integer >();

        for( int i = 0; i < cells; ++i ) {
            float age = _outputSpikesAge._values[ i ];

            if( age == 0.f ) {
                continue;
            }

            age += 1.f;

            // If we want a max pooling to disambiguate the sequences where they loop, then we limit the max age
            // But age can be affected by just 1 new bit.
            // The method of limiting output density is a more robust way because one new bit barely changes the output.
            // But, a lot of new bits will change the output a lot.

            float spike = 1f; // anything with nonzero age is an existing spike

            boolean cancelOutput = false;
            if( predictionsNew.contains( i ) ) {
                cancelOutput = true;
            }

            if( cancelOutput ) {
                // cancel the existing spike:
                age = 0;
                spike = 0.f;
            }
            else {
                // handle the existing spike:
                _transient._spikesOut.add( i );
            }

            _outputSpikesAge._values[ i ] = age; // store new age, either incremented or killed
            _outputSpikesNew._values[ i ] = spike;
        }

        // 2. Set all new active FN bits in output to age = 1.
        for( Integer i : _transient._predictionErrorFN ) {
            _outputSpikesNew._values[ i ] = 1.f;
            _outputSpikesAge._values[ i ] = 1.f;
            _transient._spikesOut.add( i );
        }

        // 3. Rank all active bits in output (including historic) by age. This enables us to prune old spikes
        TreeMap< Float, ArrayList< Integer > > ranking = new TreeMap< Float, ArrayList< Integer > >();

        for( Integer i : _transient._spikesOut ) {
            float age = _outputSpikesAge._values[ i ];
            Ranking.add( ranking, age, i );
        }

        // Now we want to limit the number of bits. We do this by age.
        // 4. Prune output by age (removing oldest) until desired sparsity is reached.
        float outputCodingSparsityFactor = _rc.getOutputCodingSparsityFactor();
        float currentSparsity = _transient._spikesNew.size(); // the number of spikes added per step
        int maxRank = (int)( currentSparsity * outputCodingSparsityFactor );
        boolean findMaxima = false; // keep the youngest

        HashSet< Integer > youngOutput = new HashSet< Integer >();
        Ranking.getBestValuesRandomTieBreak( ranking, findMaxima, maxRank, youngOutput, _rc.getRandom() );

        for( Integer i : _transient._spikesOut ) {

            if( youngOutput.contains( i ) ) {
                continue; // young enough to keep
            }

            // turn it off:
            _outputSpikesNew._values[ i ] = 0.f;
            _outputSpikesAge._values[ i ] = 0.f;

            // don't need to remove from _transient._spikesOut because it's transient
        }

        // instrumentation
        _sumOutputSpikes = _outputSpikesNew.indicesMoreThan( 0.5f ).size(); // uh .. fixed
        _sumPredictionErrorFP = _transient._predictionErrorFP.size();
        _sumPredictionErrorFN = _transient._predictionErrorFN.size();
    }*/
}
