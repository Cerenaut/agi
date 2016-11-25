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

import io.agi.core.data.FloatArray;

/**
 * Based on:
 * Inhibitory Interneurons Decorrelate Excitatory Cells to Drive Sparse Code Formation in a Spiking Model of V1
 * by Paul D. King, Joel Zylberberg and Michael R. DeWeese
 * The Journal of Neuroscience, March 27, 2013
 *
 * The cited paper implements an iterative STDP scheme, with 2 cell populations - excitatory and inhibitory. Their
 * implementation is called E-I Net which isn't a very nice class name :) It is derived from SAILnet.
 *
 * Created by dave on 3/09/16.
 */
public class SpikeTimingDependentPlasticityNetwork {

    public static final int LEARNING_RULE_NONE = 0;
    public static final int LEARNING_RULE_OJA = 1;
    public static final int LEARNING_RULE_CM = 2;

    // u     = cell array (not vec) of size 2 or 3 groups, each cells x 1
    // y     = spikes and
    // y_ave = same

    public FloatArray _potentialsOld; // membrane potentials
    public FloatArray _potentialsNew; // membrane potentials
    public FloatArray _spikesOld; // spikes
    public FloatArray _spikesNew; // spikes
    public FloatArray _spikesRate; // spikes

    FloatArray _cellsExternal;
    FloatArray _cellMembraneTimeConstants;
    FloatArray _cellPotentialThresholds;
    FloatArray _cellPotentialsOld;
    FloatArray _cellPotentialsNew;
    FloatArray _cellSpikeHistory;
    FloatArray _cellSpikesOld;
    FloatArray _cellSpikesNew;
    FloatArray _cellTypeWeights;
    FloatArray _cellInputWeights;
    FloatArray _cellSpikeRates;
    FloatArray _cellSpikeRateTargets;
    FloatArray _cellSpikeThresholds;
    FloatArray _cellSpikeThresholdDeltas;
    FloatArray _cellCellSynapseWeights;
    FloatArray _cellCellSynapseRules;
    FloatArray _cellSpikeTraces; // a faster interpolation of spikes (short term average)

    public FloatArray _W; // weights

    public void setup( int inputs, int cells ) {
        // There are 3 cell populations:
        // 1. inputs
        // 2. excitatory cells
        // 3. inhibitory cells
        int total = inputs + cells * 2;
        int totalSq = total * total;

        _cellCellSynapseRules = new FloatArray( totalSq );

        // input -> exc   +1
        // exc   -> inh   +1
        // inh   -| exc   -1
        // inh   -| inh   -1

    }


    public void update() {

        // copy new -> old, before we calculate new again:
        int historyIndex = 0;
        int cells = 0;
        int inputs = 0;
        int simCells = inputs + cells;
        float timeInterval = 0.01f;

        float spikeRateLearningRate = 0f;
        float spikeThresholdLearningRate = 0f;
        float timeSinceThresholdDeltaOld = 0f;
        float timeSinceThresholdDeltaMax = 0f;
        float synapseLearningRate = 0f;

        updateExternals();

        // calculate new state
//        updateExternalInput();
        updatePotentialsAndSpikes(
                timeInterval, cells, historyIndex,
                _cellsExternal,
                _cellMembraneTimeConstants,
                _cellPotentialThresholds,
                _cellPotentialsOld,
                _cellPotentialsNew,
                _cellSpikeHistory,
                _cellSpikesOld,
                _cellSpikesNew,
                _cellTypeWeights,
                _cellInputWeights );

        updateSpikeRatesAndThresholds(
                cells,
                _cellsExternal,
                _cellSpikesNew,
                _cellSpikeRates,
                _cellSpikeRateTargets,
                _cellSpikeThresholds,
                _cellSpikeThresholdDeltas,
                spikeRateLearningRate,
        spikeThresholdLearningRate,
        timeInterval,
        timeSinceThresholdDeltaOld,
        timeSinceThresholdDeltaMax );

        updateSynapseWeights(
            cells,
            _cellsExternal,
            _cellCellSynapseWeights,
            _cellCellSynapseRules,
            _cellSpikeTraces, // a faster interpolation of spikes (short term average)
            _cellSpikeRates, // a slower interpolation of spikes (long term average)
            synapseLearningRate,
            timeInterval );

    }

    public void updateExternals() {

    }

    public void updatePotentialsAndSpikes(
            float timeInterval,
            int cells,
            int historyIndex,
            FloatArray cellsExternal,
            FloatArray cellMembraneTimeConstants,
            FloatArray cellPotentialThresholds,
            FloatArray cellPotentialsOld,
            FloatArray cellPotentialsNew,
            FloatArray cellSpikeHistory,
            FloatArray cellSpikesOld,
            FloatArray cellSpikesNew,
            FloatArray cellTypeWeights,
            FloatArray cellInputWeights
        ) {

        for( int c = 0; c < cells; ++c ) {

            float cellExternal = cellsExternal._values[ c ];
            if( cellExternal != 0f ) {
                continue; // don't change anything about this cell - it's just an input.
            }

            // % compute the new potential and spike output of each cell
            // u_i = exp(-model.simTimeStep / cg_i.membraneTC) * u{i} + u_fixedInput{i};
            float membraneTimeConstant = cellMembraneTimeConstants._values[ c ];
            float exponential = -timeInterval / membraneTimeConstant;
            float potentialOld = cellPotentialsOld._values[ c ];
            float potentialNew = ( float ) Math.exp( exponential );
            potentialNew *= potentialOld;

// Allow for non fixed externals by having simply dead cells for these.
//            for( int i = 0; i < externalInputs; ++i ) {
//                // k = cg{i}.inputBlock(b).srcId;
//                // W = cg{i}.inputBlock(b).weight;
//                // u_fixedInput{i} = u_fixedInput{i} + bw{i}(b) * W * y0{k};
//                int inputTypeOffset = 0;
//                float cellTypeWeight = inputTypeWeights._values[ inputTypeOffset ]; // this maybe the B term which is 1, -1, 0 depending if inhibitory or not
//                float externalInput = cellTypeWeight * cellExternalInputWeight *
//
//                potentialNew += externalInput;
//            }


            // for b = enabledBlocks_nonFixed{i}
            // u_i = u_i + bw{i}(b) * W * y_prev{k};
            for( int i = 0; i < cells; ++i ) {

                float spikeOld = cellSpikesOld._values[ i ];
                if( spikeOld < 1f ) {
                    continue; // nothing to add
                }

                int cellTypeOffset = 0;
                float cellTypeWeight = cellTypeWeights._values[ cellTypeOffset ]; // this maybe the B term which is 1, -1, 0 depending if inhibitory or not

                int cellInputOffset = 0;
                float cellInputWeight = cellInputWeights._values[ cellInputOffset ];
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
            cellSpikesNew._values[ c ] = cellSpikeNew;

            // y_history{i}(:,:,t) = y{i};
            int cellSpikeHistoryOffset = 0;
            cellSpikeHistory._values[ cellSpikeHistoryOffset ] = cellSpikeNew;
        }

        historyIndex += 1; // advance
    }

    public float updateSpikeRatesAndThresholds(
            int cells,
            FloatArray cellsExternal,
            FloatArray cellSpikesNew,
            FloatArray cellSpikeRates,
            FloatArray cellSpikeRateTargets,
            FloatArray cellSpikeThresholds,
            FloatArray cellSpikeThresholdDeltas,
            float spikeRateLearningRate,
            float spikeThresholdLearningRate,
            float timeInterval,
            float timeSinceThresholdDeltaOld,
            float timeSinceThresholdDeltaMax
    ) {
        float timeSinceThresholdDeltaNew = timeSinceThresholdDeltaOld + timeInterval;

        boolean updateThresholds = false;

        if( timeSinceThresholdDeltaNew >= timeSinceThresholdDeltaMax ) {
            timeSinceThresholdDeltaNew = 0;
            updateThresholds = true;
        }

        // thisSpikeRate = [];
        // %compute spike rate for all cell groups, including input
        // for i = 1:numel( cg )
        for( int c = 0; c < cells; ++c ) {

            float cellExternal = cellsExternal._values[ c ];
            if( cellExternal != 0f ) {
                continue; // don't change anything about this cell - it's just an input.
            }

            // elseif fullInputProvided(i)
            //   thisSpikeRate{i} = mean(   reshape( initialState.y{i}, cg{i}.numCells,[] ), 2) / model.simTimeStep;
            // elseif cg{i}.isExternal
            //   thisSpikeRate{i} = mean(            y0{i}                                 , 2) / model.simTimeStep;
            // else
            //   thisSpikeRate{i} = mean(   reshape( y_history{i}     , cg{i}.numCells,[] ), 2) / model.simTimeStep;
            // end

            float spikeNew = cellSpikesNew._values[ c ];
            float spikeRateOld = cellSpikeRates._values[ c ];
            float spikeRateNew = spikeRateLearningRate * spikeNew
                               + (1f - spikeRateLearningRate ) * spikeRateOld;

            cellSpikeRates._values[ c ] = spikeRateNew;

            // NOTE: Must / timeInterval before use
//            spikeRateNew /= timeInterval;

            // sr                = cg{i}.spikeRate;  %D a structure
            // stats_eta         = 1 - exp(- numTotalIterations / sr.meanWindow);
            // sr.meanBiased     = (1-stats_eta) .* sr.meanBiased + stats_eta * thisSpikeRate{i};
            // sr.meanBiased_max = (1-stats_eta) .* sr.meanBiased_max + stats_eta;
            // sr.mean           = sr.meanBiased ./ sr.meanBiased_max;
            // sr.popMean        = mean(sr.mean);
            // cg{i}.spikeRate   = sr;

            // Spike threshold

            // % update thresholds with Foldiak's rule: keep each neuron firing near target
            // % from SAILnet: delta_theta_i = gamma * (n_i - p)
            // numTotalTimeUnits  = numTotalIterations * model.simTimeStep;
            // lrate              = model.learningRate * model.lrateScale * cg{i}.threshAdaptRate;
            // delta_spikeThresh  = lrate * (thisSpikeRate{i} - cg{i}.targetSpikeRate) * numTotalTimeUnits;
            float spikeRateTarget = cellSpikeRateTargets._values[ c ];
            float spikeRateError = spikeRateNew - spikeRateTarget;
            float spikeThresholdDelta = spikeThresholdLearningRate
                                      * spikeRateError;
//                                      * timeInterval; // TODO check this

            // This is the change to the spike threshold.
            // cg{i}.dspikeThresh = cg{i}.dspikeThresh + delta_spikeThresh;
            float spikeThresholdDeltaOld = cellSpikeThresholdDeltas._values[ c ];
            float spikeThresholdDeltaNew = spikeThresholdDeltaOld + spikeThresholdDelta;

            // accumulate the changes and apply them occasionally. makes sense. Remember when last done.
            // % only update spike threshold (spikeThresh) occasionally
            // cg{i}.updateThreshWait = cg{i}.updateThreshWait + numTotalIterations;
            // if cg{i}.updateThreshEvery > 0 ...
            // && cg{i}.updateThreshWait >= cg{i}.updateThreshEvery
            //    cg{i}.spikeThresh      = cg{i}.spikeThresh + cg{i}.dspikeThresh;
            //    cg{i}.dspikeThresh(:)  = 0;
            //    cg{i}.updateThreshWait = 0;
            // end

            if( updateThresholds ) {
                float spikeThresholdOld = cellSpikeThresholds._values[ c ];
                float spikeThresholdNew = spikeThresholdOld + spikeThresholdDeltaNew;
                cellSpikeThresholds._values[ c ] = spikeThresholdNew;
                spikeThresholdDeltaNew = 0f;
            }

            cellSpikeThresholdDeltas._values[ c ] = spikeThresholdDeltaNew; // may have been zeroed
        }

        return timeSinceThresholdDeltaNew;
    }

    public void updateSynapseWeights(
            int cells,
            FloatArray cellsExternal,
            FloatArray cellCellSynapseWeights,
            FloatArray cellCellSynapseRules,
            FloatArray cellSpikeTraces, // a faster interpolation of spikes (short term average)
            FloatArray cellSpikeRates, // a slower interpolation of spikes (long term average)
            float synapseLearningRate,
            float timeInterval
    ) {
        for( int c2 = 0; c2 < cells; ++c2 ) {

            float cellExternal = cellsExternal._values[ c2 ];
            if( cellExternal != 0f ) {
                continue; // don't change anything about this cell - it's just an input.
            }

            float postSpikeTrace = cellSpikeTraces._values[ c2 ]; // y
            float postSpikeRate  = cellSpikeRates._values[ c2 ]; // <y>

            for( int c1 = 0; c1 < cells; ++c1 ) {

                // for i = activeCellGroupIds
                //   for b = enabledBlocks{i}
                //     k = cg{i}.inputBlock(b).srcId;
                //     W = cg{i}.inputBlock(b).weight;
                //     X = inputSpikeRate{k};
                //     Y = outputSpikeRate{i};
                float preSpikeTrace = cellSpikeTraces._values[ c1 ]; // x
                float preSpikeRate   = cellSpikeRates._values[ c1 ]; // <x>

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

                int synapseOffset = 0;
                float deltaWeight = 0f;
                float rule = cellCellSynapseRules._values[ synapseOffset ];
                float oldWeight = cellCellSynapseWeights._values[ synapseOffset ];

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
                if( rule == LEARNING_RULE_OJA ) {
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

                else if( rule == LEARNING_RULE_CM ) {
                    float prePostSpikeRates = preSpikeRate * postSpikeRate;
                    deltaWeight = preSpikeTrace * postSpikeTrace - prePostSpikeRates * ( 1f + oldWeight );
                }

                deltaWeight *= synapseLearningRate; // make it learn slowly

                // % store updated dweight back into cellgroup
                // cg{i}.inputBlock(b).dweight = cg{i}.inputBlock(b).dweight + dW;
                float newWeight = oldWeight + deltaWeight;
                cellCellSynapseWeights._values[ synapseOffset ] = newWeight;
            }
        }

    }

    public void update2() {
        // C = neuron class
        float B_e = 1f; // excitatory
        float B_i = -1f; // inhibitory
        float B_c = 0; // is the sign of the impact of class C 2 neurons on class C 1 neurons: +1 for excitatory connections and -1 for inhibitory connections.

        // configuration
        int C = 2; // exc and inh
        int tMax = 10;
        int cells = 100;

        // parameters
        //The membrane time constants controlling the decay of
        //the membrane potential to a baseline of zero, which worked best when
        //faster spike rates were paired with faster time constants, were tau(E)= 1 and tau(I) = 0.5.
        float tau_e = 1;
        float tau_i = 0.5f;
        float tau_C = 0; // is the membrane time constant governing the membrane potential decay rate for neurons of class C;
        float mu = 0.1f; // is the simulation time step size in arbitrary simulation time units (0.1 arbitrary time units here);

        float u_c_i = 0; // is the membrane potential of neuron i of neuron class C at time t;
        float z_c_i = 0; // is the spike output of neuron i of neuron class C at time t (either 0 for no spike, or 1 for spike);
        float theta_c_i= 0; // is the spike threshold of neuron i of neuron class C;
        float w_i_j = 0; // is the connection weight from neuron j of class C 2 to neuron i of class C 1

        // For each simulation time step t
        for( int t = 0; t < tMax; ++t ) {
            // for each neuron i of class C, the neuron state is updated as follows:
            for( int i = 0; i < cells; ++i ) {
                float u_i1 = 0f;// previous value
                float term2 = ( float ) Math.exp( -mu / tau_C );
                float term3 = 0f;
                for( int c = 0; c < C; ++c ) {
                    float sum = 0f;

                    for( int j = 0; j < cells; ++j ) {
                        float z_c_j = 0; // @t
                        //                float w_i_j = 0;
                        float product = z_c_j * w_i_j;
                        sum = sum + product;
                    }

                    sum = sum * B_c;

                    term3 += sum;

                }
                float u_c_i2 = u_i1 * term2 + term3;
            }

            // after
            for( int i = 0; i < cells; ++i ) {
                theta_c_i = 0; // @t+1
                u_c_i = 0; // t+1 - calculated in pass above
                z_c_i = 0; // @t+1;
                if( u_c_i >= theta_c_i ) {
                    z_c_i = 1f;
                    u_c_i = 0f; // after spike
                }

            }

            // TODO: Input cell current
            // The input image patch is represented as graded values rather than
            // spikes. X i represents the value of the whitened image patch at pixel i,
            // which may be positive or negative. The following rule is used to convert
            // X i into a suitable input value z_i_in(t), which can be viewed as the aggregate
            // contrast information at that point in visual space summarized as a cur-
            // rent injection introduced into the neuron over time as follows:
            float X_i_t = 0;
            float z_i_in_t = mu * X_i_t;

            float B_in_to_exitatory = 5;
            float pixel_i = 0;
            X_i_t = (1/5)* pixel_i;// , where
            // pixel i is the value of the ith pixel after the whitened image patch has been
            // normalized to zero mean and unit variance; and B_in_to_exitatory = 5;

            // NB:
            // https://en.wikipedia.org/wiki/Whitening_transformation
            // A whitening transformation is a linear transformation that transforms a vector of random variables with a
            // known covariance matrix into a set of new variables whose covariance is the identity matrix meaning that
            // they are uncorrelated and all have variance 1.

            // TODO Spike Threshold update
            // Homeostatic spike rate regulation. As with SAILnet, the threshold at
            // which a neuron fires is adjusted up or down according to a threshold
            // adaptation rule, originally from Foldiak (1990) to achieve a target spike
            // rate over the long term that is set in advance as a network parameter:
            float p_c = 0;// is the target mean spike rate for neurons of class C
            // For our simulation, we used spike rates of p (E ) ϭ 0.02 and p (I ) ϭ 0.04 spikes per time unit.
float            z_i_c_avg = 0f;
            float delta_theta_i_c = z_i_c_avg - p_c;// is proportional to <z_i_c> - p_c

            // TODO: weight update
            // The weights from the image patch to the excitatory cells, W_in_e, are
            // updated according to Oja’s variant of the Hebbian learning rule (Oja, 1982), labeled “HO.”
            // All remaining weights, W_e_i, W_i_e, and W_i_i, learn using the Correlation Measuring rule introduced here and labeled “CM.”
            float W_ij = 0;
            float y_i = 0; // represents the spike rate of postsynaptic (output) neuron i.
            float x_j = 0; // refers to the spike rate of presynaptic (input) neuron j
            // The spike rates are a moving average of the individual spikes <z_i_C>dt over time, where dt represents the temporal window of the
            // moving average weighted with exponential decay.

            // Weight changes are computed on each time step in a simulation of symmetric STDP although using
            // sample-averaged spike rates computed once per sample produces similar
            // results. Note that the STDP used here for inhibitory neurons is independent
            // of pre-post spike order, a type of plasticity that, interestingly, has
            // been observed in GABAergic neurons in hippocampus
            // To stabilize network behavior during training, weight changes are accumulated
            // separately and applied in aggregate after every 100 image patch training samples.

            // https://www.quora.com/What-is-the-meaning-of-angle-bracket
            // This notation is sometimes used for average (mean value). ⟨x⟩⟨x⟩ means average of x.
            // http://mathworld.wolfram.com/AngleBracket.html
            // The expression <X> is also commonly used to denote the expectation value of a variable X.

            // The following learning rates were used:
            float a_in_e = 0.008f;
            float a_e_i = 0.028f;
            float a_i_e = 0.028f;
            float a_i_i = 0.06f;



            // HO - Ojas rule
            // http://www.scholarpedia.org/article/Oja_learning_rule
            // hebb rule:
            // delta_w_i = alpha * x_i * y where x_i is input i and y is the output of the neuron
            // Ojas rule:
            // delta_w_i = alpha * ( x_i * y - y*y * w_i )
            float delta_W_ij = y_i * x_j - ( y_i * y_i ) * W_ij;
            // The squared output y^2 guarantees that the larger the output of the neuron becomes, the stronger is this balancing effect.

            // CM
            // Weights adjusted with the CM rule are further constrained to be non-negative.

        } // time steps


    } // update

}
