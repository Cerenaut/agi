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
import io.agi.core.data.FloatArray;

/**
 * Based on other recurrent, spiking neural networks with a pair of excitatory & inhibitory layers.
 * Spiking Neural Networks use Spike-Timing Dependent Plasticity (STDP) to learn structure over time.
 *
 * This implementation and architecture is based on two papers:
 *
 * PAPER 1:
 * -------------------------
 * Unsupervised learning of digit recognition using spike-timing-dependent plasticity
 * by Diehl & Cook
 * Front. Comput. Neurosci., 03 August 2015
 *
 * http://journal.frontiersin.org/article/10.3389/fncom.2015.00099/full
 * https://github.com/peter-u-diehl/stdp-mnist/blob/master/Diehl%26Cook_spiking_MNIST.py
 *
 * PAPER 2:
 * -------------------------
 * Inhibitory Interneurons Decorrelate Excitatory Cells to Drive Sparse Code Formation in a Spiking Model of V1
 * by Paul D. King, Joel Zylberberg and Michael R. DeWeese
 * The Journal of Neuroscience, March 27, 2013
 *
 * The cited paper implements an iterative STDP scheme, with 2 cell populations - excitatory and inhibitory. Their
 * implementation is called E-I Net which isn't a very nice class name :) It is derived from SAILnet.

 * Created by dave on 24/09/16.
 */
public class SpikingNeuralNetwork {

    public SpikingNeuralNetworkConfig _c;

    // cells
    public Data _cellMembraneTimeConstants = null;
    public Data _cellPotentialsOld = null;
    public Data _cellPotentialsNew = null;
    public Data _cellSpikeRateTargets = null;
    public Data _cellSpikeThresholds = null;
    public Data _cellSpikeThresholdDeltas = null;
    public Data _cellSpikeThresholdVelocities = null;

    // cells x inputs
    public Data _cellsInputWeights = null;

    // inputs
    public Data _inputSpikesOld = null;
    public Data _inputSpikesNew = null;
    public Data _inputWeights = null; // e.g. -1 (inh), +1 (exc). A cell or an input has this weight regardless of target.
    public Data _inputSpikeRates = null;
    public Data _inputSpikeTraces = null;

    public void setup( SpikingNeuralNetworkConfig c ) {

        // store variables
        _c = c;

        // create data structures
        int cells = _c.getCellsSize();
        int inputs = _c.getInputSize();

        DataSize cellsSize = DataSize.create( cells );
        DataSize inputsSize = DataSize.create( inputs );
        DataSize cellsInputsSize = DataSize.create( cells, inputs );

        _cellMembraneTimeConstants = new Data( cellsSize );
//        _cellPotentialThresholds = new Data( cellsSize );
        _cellPotentialsOld = new Data( cellsSize );
        _cellPotentialsNew = new Data( cellsSize );
        _cellSpikeRateTargets = new Data( cellsSize );
        _cellSpikeThresholds = new Data( cellsSize );
        _cellSpikeThresholdDeltas = new Data( cellsSize );
        _cellSpikeThresholdVelocities = new Data( cellsSize );

        _cellsInputWeights = new Data( cellsInputsSize );

        _inputSpikesOld = new Data( inputsSize );
        _inputSpikesNew = new Data( inputsSize );
        _inputWeights = new Data( inputsSize );
        _inputSpikeRates = new Data( inputsSize );
        _inputSpikeTraces = new Data( inputsSize );

        // initialization
        reset();
    }

    public void reset() {

        // default values for computed structures
        _c.resetSpikeThresholdBatchIndex();

        _cellPotentialsOld.set( 0f ); // this is measured per step
        _cellPotentialsNew.set( 0f );
        _cellSpikeThresholdDeltas.set( 0f ); // this accumulates over time
        _cellSpikeThresholdVelocities.set( 0f );

        _inputSpikesOld.set( 0f ); // computed
        _inputSpikesNew.set( 0f );
        _inputSpikeTraces.set( 0f );

        _inputSpikeRates.set( 0f );

        // special fixed or initial values
        int cells = _c.getCellsSize();
        int inputs = _c.getInputSize();

        for( int c = 0; c < cells; ++c ) {
            float timeConstant = _c.getCellTimeConstant( c );
            float targetSpikeRate = _c.getCellTargetSpikeRate( c );
            float spikeThreshold = _c.getResetCellSpikeThreshold( c, targetSpikeRate );

            _cellMembraneTimeConstants._values[ c ] = timeConstant;
            _cellSpikeRateTargets._values[ c ] = targetSpikeRate;
            _cellSpikeThresholds._values[ c ] = spikeThreshold;

            // % initialize membrane potentials to random values to reduce time synchronization
            float potential = _c.getResetCellPotential( c, spikeThreshold ); // a fraction of the threshold
            _cellPotentialsOld.set( potential );
        }

        for( int c = 0; c < cells; ++c ) {
            int inputDegree = _c.getCellInputDegree( c );

            for( int i = 0; i < inputs; ++i ) {
                float weight = _c.getResetSynapseWeight( c, i, inputDegree );
                int offset = _c.getCellsInputsOffset( c, i );
                _cellsInputWeights._values[ offset ] = weight;
            }
        }

        for( int i = 0; i < inputs; ++i ) {
            float inputWeight = _c.getInputWeight( i );
            _inputWeights._values[ i ] = inputWeight;
        }

    }

    public void update( FloatArray externalSpikes1, FloatArray externalSpikes2 ) {

        // all things that are learned:
        // -- synapses
        // -- spike thresholds
        // other things adapt, but they are ancillary to calculating these things.

        // try make it probability of firing not threshold.
        //        so it will learn even while the threshold is bad.

        int cells = _c.getCellsSize(); // all types
        int inputs = _c.getInputSize(); // externals + cells
//        float potentialDecayRate = 0.8f;

        // copy new to old
        _inputSpikesOld.copy( _inputSpikesNew ); // copy latest internally generated spikes
        _cellPotentialsOld.copy( _cellPotentialsNew );

        // copy external 1 spikes to new
        // assume an external input spiker
        int offsetThis = _c.getInputTypeInputOffset( SpikingNeuralNetworkConfig.INPUT_TYPE_EXTERNAL_1 );
        int offsetThat = 0;
        int range      = externalSpikes1.getSize();
        _inputSpikesNew.copyRange( externalSpikes1, offsetThis, offsetThat, range );

        // copy external 1 spikes to new
        // assume an external input spiker
        offsetThis = _c.getInputTypeInputOffset( SpikingNeuralNetworkConfig.INPUT_TYPE_EXTERNAL_2 );
        offsetThat = 0;
        range      = externalSpikes2.getSize();
        _inputSpikesNew.copyRange( externalSpikes2, offsetThis, offsetThat, range );



        // update internal state
        updatePotentialsAndSpikes(
                cells, inputs,
//                potentialDecayRate,
                _cellMembraneTimeConstants,
//                _cellPotentialThresholds,
                _cellSpikeThresholds,
                _cellPotentialsOld,
                _cellPotentialsNew,
                _inputSpikesOld,
                _inputSpikesNew,
                _inputWeights, // e.g. -1 (inh), +1 (exc)
                _cellsInputWeights );

        updateSpikeRates(
                inputs,
                _c._spikeRateLearningRate,
                _inputSpikesNew,
                _inputSpikeRates );

        updateSpikeTraces(
                inputs,
                _c._spikeTraceLearningRate,
                _inputSpikesNew,
                _inputSpikeTraces );

        boolean batchUpdate = _c.doSpikeThresholdBatchUpdate();

        updateSpikeThresholds(
                cells,
                _c._spikeThresholdLearningRate,
                batchUpdate,
                _inputSpikeRates,
                _cellSpikeRateTargets,
                _cellSpikeThresholds,
                _cellSpikeThresholdDeltas,
                _cellSpikeThresholdVelocities );

        updateSynapseWeights(
                cells, inputs,
                _cellsInputWeights,
                _inputSpikeTraces, // a faster interpolation of spikes (short term average)
                _inputSpikeRates );  // a slower interpolation of spikes (long term average)

//        _c.updateSpikeHistoryIndex();
        _c.updateSpikeThresholdBatchIndex();

    }

    public void updatePotentialsAndSpikes(
            int cells, // all types
            int inputs, // externals + cells
//            float potentialDecayRate,
            FloatArray cellMembraneTimeConstants,
            FloatArray cellPotentialThresholds,
            FloatArray cellPotentialsOld,
            FloatArray cellPotentialsNew,
//            FloatArray inputSpikesHistory,
            FloatArray inputSpikesOld,
            FloatArray inputSpikesNew,
            FloatArray inputWeights, // e.g. -1 (inh), +1 (exc)
            FloatArray cellsInputWeights
    ) {
//        float timeInterval = 0.05f;

        for( int c = 0; c < cells; ++c ) {

            if( c > 4000  ) {
                int g = 0;
                g++;
            }
            // % compute the new potential and spike output of each cell
            // u_i = exp(-model.simTimeStep / cg_i.membraneTC) * u{i} + u_fixedInput{i};
            float membraneTimeConstant = cellMembraneTimeConstants._values[ c ];
//            float exponential = -timeInterval / membraneTimeConstant;
            float potentialOld = cellPotentialsOld._values[ c ];
//            float potentialNew = ( float ) Math.exp( exponential );
//            potentialNew *= potentialOld; // about 0.3 ie fast decay.
            float potentialNew = potentialOld * membraneTimeConstant; // leakiness

            // for b = enabledBlocks_nonFixed{i}
            // u_i = u_i + bw{i}(b) * W * y_prev{k};
            boolean hasInputSpikes = false;

            for( int i = 0; i < inputs; ++i ) {

                float spikeOld = inputSpikesOld._values[ i ]; // either 0 or 1
                if( spikeOld < 1f ) {
                    continue; // nothing to add
                }

                if( !_c.isConnected( c, i ) ) {
                    continue;
                }

                hasInputSpikes = true;

                float cellTypeWeight = inputWeights._values[ i ]; // this maybe the B term which is 1, -1, 0 depending if inhibitory or not

                int cellInputOffset = _c.getCellsInputsOffset( c, i );//cells * inputs + i;
                float cellInputWeight = cellsInputWeights._values[ cellInputOffset ];

                float cellInput = cellTypeWeight * cellInputWeight * spikeOld;

                potentialNew += cellInput;
            }

            // y{i}      = (u_i >= thresh{i});           % spike output if potential exceeds threshold
            float cellSpikeNew = 0f;
            float potentialThreshold = cellPotentialThresholds._values[ c ];

            if( potentialNew >= potentialThreshold ) {
                cellSpikeNew = 1f;
                // u_i(y{i}) = 0;                            % reset potential of spiked cells
                potentialNew = 0f;
            }

            // u{i}      = u_i;
            cellPotentialsNew._values[ c ] = potentialNew;

            int iPost = _c.getCellInputOffset( c ); //externals + c;
            inputSpikesNew._values[ iPost ] = cellSpikeNew;

//            // y_history{i}(:,:,t) = y{i};
//            int inputSpikesHistoryOffset = _c.getInputsHistoryOffset( iPost );//inputOffset * historyLength + historyIndex;
//            inputSpikesHistory._values[ inputSpikesHistoryOffset ] = cellSpikeNew;
        }

    }

    public void updateSpikeRates(
            int inputs,
            float spikeRateLearningRate,
            FloatArray inputSpikesNew,
            FloatArray inputSpikeRates
    ) {
        for( int i = 0; i < inputs; ++i ) {

            float spikesNew = inputSpikesNew._values[ i ];
            float spikeRateOld = inputSpikeRates._values[ i ];
            float spikeRateNew = spikeRateLearningRate * spikesNew
                    + ( 1f - spikeRateLearningRate ) * spikeRateOld;

            inputSpikeRates._values[ i ] = spikeRateNew;
        }
    }

    public void updateSpikeTraces(
        int inputs,
        float spikeTraceLearningRate,
        FloatArray inputSpikesNew,
        FloatArray inputSpikeTraces
        ) {
            for( int i = 0; i < inputs; ++i ) {

                float spikesNew = inputSpikesNew._values[ i ];
                float spikeTraceOld = inputSpikeTraces._values[ i ];
                float spikeTraceOldDecay = spikeTraceLearningRate * spikeTraceOld;
                float spikeTraceNew = Math.max( spikesNew, spikeTraceOldDecay );

                inputSpikeTraces._values[ i ] = spikeTraceNew;
            }
    }

    public void updateSpikeThresholds(
            int cells,
//            float spikeRateLearningRate,
            float spikeThresholdLearningRate,
            boolean batchUpdate,
//            FloatArray cellsExternal,
//            FloatArray inputSpikesNew,
            FloatArray inputSpikeRates,
            FloatArray cellSpikeRateTargets,
            FloatArray cellSpikeThresholds,
            FloatArray cellSpikeThresholdDeltas,
            FloatArray cellSpikeThresholdVelocities
    ) {
        for( int c = 0; c < cells; ++c ) {

            int i = _c.getCellInputOffset( c );
//            float spikesNew    = inputSpikesNew._values[ i ];
//
            float spikeRate = inputSpikeRates._values[ i ];
//            float spikeRateNew = spikeRateLearningRate * spikesNew
//                    + (1f - spikeRateLearningRate ) * spikeRateOld;
//
//            cellSpikeRates._values[ c ] = spikeRateNew;

            // NOTE: Must / timeInterval before use
//            spikeRateNew /= timeInterval;

            // Spike threshold
            // % update thresholds with Foldiak's rule: keep each neuron firing near target
            float spikeRateTarget = cellSpikeRateTargets._values[ c ];
            float spikeRateError = spikeRate - spikeRateTarget;
            float spikeThresholdDelta = spikeThresholdLearningRate * spikeRateError;
//                                      * timeInterval; // TODO check this

            // This is the change to the spike threshold.
            float spikeThresholdDeltaOld = cellSpikeThresholdDeltas._values[ c ];
            float spikeThresholdDeltaNew = spikeThresholdDeltaOld + spikeThresholdDelta;

            // accumulate the changes and apply them occasionally. makes sense. Remember when last done.
            if( batchUpdate ) {
                float v = cellSpikeThresholdVelocities._values[ c ] * 0.9f; // old speed
                v = v + spikeThresholdDeltaNew; // the acceleration
                float spikeThresholdOld = cellSpikeThresholds._values[ c ];
//                float spikeThresholdNew = spikeThresholdOld + spikeThresholdDeltaNew;
                float spikeThresholdNew = spikeThresholdOld + v;
                cellSpikeThresholds._values[ c ] = spikeThresholdNew;
                spikeThresholdDeltaNew = 0f;
                cellSpikeThresholdVelocities._values[ c ] = v;
            }

            cellSpikeThresholdDeltas._values[ c ] = spikeThresholdDeltaNew; // may have been zeroed
        }
    }

    public void updateSynapseWeights(
            int cells,
            int inputs,
//            float synapseLearningRate,
//            FloatArray inputSpikesHistory,
            FloatArray cellsInputWeights,
//            FloatArray cellCellSynapseRules,
            FloatArray inputSpikeTraces, // a faster interpolation of spikes (short term average)
            FloatArray inputSpikeRates  // a slower interpolation of spikes (long term average)
    ) {
        // CONCEPTS:
        // A 'trace' is a fast moving recent history of a spike - spike and decay
        // A 'rate' is a slower moving average rate of spiking over a longer period of time, over many spikes

        float timeInterval = 1f;

        for( int c = 0; c < cells; ++c ) {

            int iPost = _c.getCellInputOffset( c );
            float postSpikeTrace = inputSpikeTraces._values[ iPost ]; // y
            float postSpikeRate  = inputSpikeRates ._values[ iPost ]; // <y>

            for( int iPre = 0; iPre < inputs; ++iPre ) {

                // for i = activeCellGroupIds
                //   for b = enabledBlocks{i}
                //     k = cg{i}.inputBlock(b).srcId;
                //     W = cg{i}.inputBlock(b).weight;
                //     X = inputSpikeRate{k};
                //     Y = outputSpikeRate{i};
                float preSpikeTrace = inputSpikeTraces._values[ iPre ]; // x
                float preSpikeRate  = inputSpikeRates ._values[ iPre ]; // <x>

                // TODO prohibit self connections?

                // <x> represents the lifetime average value of x
                // x_j is the presynaptic spike rate, y_i is the postsynaptic spike rate
                // but to allow some temporal flexibility we want to model recent 'traces' of spikes.
                // I think the paper is saying this happens due to exponential decays in the spike history values.

                // Paper:
                // "The spike rates are a moving average of the individual spikes
                // over time, <z_i^(C)>_dt, where dt represents the temporal window of the
                // moving average weighted with exponential decay. Weight changes are
                // computed on each time step in a simulation of symmetric STDP
                //
                // Note that the STDP used here for inhibitory neurons is independent of pre-post spike order, a type of
                // plasticity that, interestingly, has been observed in GABAergic neurons in hippocampus

                // So looking at the code:
                // 'expDecay_continuous_all'
                //
                // y_ave_history{i} = zeros(cg{i}.numCells, numNetworks, numIterations, model.precisionHint);
                // for t = 1:numIterations
                //   y_ave{i} = (1-ave_eta(i))*y_ave{i} + ave_eta(i) * y0{i};
                //   y_ave_history{i}(:,:,t) = y_ave{i};
                // end
                //       ie
                // y_ave{i} = (1-ave_eta(i) ) * y_ave{i}
                //          +    ave_eta(i)   *    y0{i};    ie a lerp
                //
                // AND 'expDecay_continuous', 19
                //
                // for t = 1:numIterations
                //   y_ave{i} = (1-ave_eta(i))*y_ave{i} + ave_eta(i) * y_history{i}(:,:,t);
                //   y_ave_history{i}(:,:,t) = y_ave{i};
                // end
                //       ie
                //   y_ave{i} = (1-ave_eta(i)) * y_ave{i}
                //            +    ave_eta(i)  * y_history{i}(:,:,t);
                //

                // % y_ave_history{numCG}(:,:,:) = moving average of output spikes, post-spike

                // % rehape into a 2D matrix that is one large collection of time samples
                // outputSpikeRate{i}  = reshape(y_ave_history{i}, [], numTotalIterations);

                // inputSpikeRate{i} = outputSpikeRate{i};

                // %    inputSpikeRate{numCG}(:,:)   =  input spike rates (spikes/iteration, pre-spike)
                // %   outputSpikeRate{numCG}(:,:)   = output spike rates (spikes/iteration, post-spike)

                int rule = _c.getSynapseLearningRule( c, iPre ); //cellCellSynapseRules._values[ synapseOffset ];

                if( rule == SpikingNeuralNetworkConfig.LEARNING_RULE_NONE ) {
                    continue; // no learning, e.g. disconnected
                }

                float synapseLearningRate = _c.getSynapseLearningRate( c, iPre );
                float deltaWeight = 0f;

                // Learning rules
                int cellInputOffset = _c.getCellsInputsOffset( c, iPre );//cells * inputs + i;
                float oldWeight = cellsInputWeights._values[ cellInputOffset ];

                // %D Oja (HO) Used for input to excitatory cells
                // %D diag() :  D = diag(v) returns a square diagonal matrix with the elements of vector v on the main diagonal.
                // case 'hebbian_oja'
                //        % Oja variant of Hebbian learning rule (dW = y x - y^2 W)
                //        % (attenuates weight growth and learns linear models)
                //    dW = Y * X' - diag( sum(Y.^2,2) ) * W;
                //
                // http://www.scholarpedia.org/article/Oja_learning_rule
                // In many neuron models, another term representing "forgetting" has been used: the value of the weight
                // itself should be subtracted from the right hand side. The central idea in the Oja learning rule is to
                // make this forgetting term proportional, not only to the value of the weight, but also to the square
                // of the output of the neuron.
                // y = output (post)
                // x = input (pre)
                // Hebb: dW_i = a * ( x_i * y              )
                //  Oja: dW_i = a * ( x_i * y  -   y^2 w_i )
                if( rule == SpikingNeuralNetworkConfig.LEARNING_RULE_OJA ) {
                    float postSpikeTraceSq = postSpikeTrace * postSpikeTrace;
                    deltaWeight = preSpikeTrace * postSpikeTrace - postSpikeTraceSq * oldWeight;
                }

//                say w = 0.5
//                dw = 0.1 * ( 1 * 1 -0.1 * 0.5 )
//                        = 0.095
//                // make the target rate higher:
//                dw = 0.1 * ( 1 * 1 -0.2 * 0.5 )
//                        = 0.09
//                // make the target rate higher:
//                dw = 0.1 * ( 1 * 1 -0.5 * 0.5 )
//                        = 0.075
//                dw = 0.1 * ( 1 * 1 -0.99 * 0.5 )
//                        = 0.0505
//                // make weight saturate:
//                dw = 0.1 * ( 1 * 1 -0.99 * 0.99 )
//                        = 0.00199    -- it stops changing

                // %D All remaining weights learn with Corr Meas. rule
                // case 'correlation_measuring'
                //     % Foldiak-inspired rule that converges despite correlations (dW = y x - <y> <x> (1 + W))
                //     % pq is the moving average of the input and output cell spike rates
                //     % (adjusted to be per-iteration rather than per-time-unit)
                //    pq = ( cg{i}.spikeRate.lMean
                //       *  cg{k}.spikeRate.lMean')
                //       * model.simTimeStep^2;
                //    dW = (Y * X') - pq .* numSamples .* (1 + W);
                //
                // "Thus the weights learned by the CM rule are
                // proportional to the covariance between the
                // neurons. The weight will be zero if the neurons
                // are uncorrelated (or anticorrelated) and will
                // grow linearly as the degree of correlation
                // increases.
                //         The net result is that when an I cell spikes, it
                // sends more inhibition to those E cells whose
                // firing rates are more strongly correlated with
                // that I cell’s own firing rate

                else if( rule == SpikingNeuralNetworkConfig.LEARNING_RULE_CM ) {
                    float prePostSpikeRates = preSpikeRate * postSpikeRate;
                    deltaWeight = preSpikeTrace * postSpikeTrace - prePostSpikeRates * ( 1f + oldWeight );
                }

                deltaWeight *= synapseLearningRate; // make it learn slowly

                // % store updated dweight back into cellgroup
                // cg{i}.inputBlock(b).dweight = cg{i}.inputBlock(b).dweight + dW;
                float newWeight = oldWeight + deltaWeight;
                cellsInputWeights._values[ cellInputOffset ] = newWeight;
            }
        }

    }

}

