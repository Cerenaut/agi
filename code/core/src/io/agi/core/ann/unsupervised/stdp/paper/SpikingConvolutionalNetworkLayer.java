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

package io.agi.core.ann.unsupervised.stdp.paper;

import io.agi.core.data.*;
import io.agi.core.math.Unit;
import io.agi.core.math.Useful;
import io.agi.core.opt.DiscreteTimePIDController;

import java.util.HashMap;

/**
 * Created by dave on 1/05/17.
 */
public class SpikingConvolutionalNetworkLayer {

    // TODO rename these
    public static final int LAYER_STATISTICS_SPIKE_DENSITY = 0; // instantaneous
    public static final int LAYER_STATISTICS_WINDOW_SUM = 1;    // window stats
    public static final int LAYER_STATISTICS_WINDOW_COUNT = 2;  // window stats
    public static final int LAYER_STATISTICS_CONTROLLER_THRESHOLD = 3;         // controller variables
    public static final int LAYER_STATISTICS_CONTROLLER_MEASURED = 4;          // controller variables
    public static final int LAYER_STATISTICS_CONTROLLER_ERROR = 5;             // controller variables
    public static final int LAYER_STATISTICS_CONTROLLER_ERROR_INTEGRATED = 6;  // controller variables
    public static final int LAYER_STATISTICS_CONTROLLER_DELTA = 7;             // controller variables
//    public static final int LAYER_STATISTICS_KERNEL_GAIN_TIMER = 8; // kernel relative frequency
    public static final int LAYER_STATISTICS_SIZE = 8;

    public SpikingConvolutionalNetworkLayerConfig _config;

    //public DiscreteTimePIDController _convSpikeThresholdController;

    public int _layer = 0;

    public Data _inputInverse;
    public Data _inputSpikes;
    public Data _inputTrace;

    public Data _kernelWeights;
    public Data _kernelFrequency;
    public Data _kernelGains;
    public Data _kernelControllerErrorIntegral;

    public Data _controllerStatistics;

    public Data _convControllerErrorIntegral;
    public Data _convInverse;
    public Data _convSums;
    public Data _convInhibition;
    public Data _convIntegrated;
    public Data _convSpikes;
//    public Data _convSpikeStats;
//    public Data _convSpikeControllerWindow;

    public Data _poolSpikes;
    public Data _poolSpikesIntegrated;
    public Data _poolIntegrated;
    public Data _poolInhibition;
    public Data _poolInput;

    public SpikingConvolutionalNetworkLayer() {

    }

    public void setup( SpikingConvolutionalNetworkLayerConfig config, int layer ) {
        _config = config;
        _layer = layer;

//        _convSpikeStats = new Data( DataSize.create( LAYER_STATISTICS_SIZE ) );
//        _convSpikeControllerWindow = new Data( DataSize.create( config._convSpikeControllerIntegrationPeriod ) );

        int nbrControllers = _config._depth +1;
        DataSize controllerStatisticsDataSize = DataSize.create( LAYER_STATISTICS_SIZE * nbrControllers );
        _controllerStatistics = new Data( controllerStatisticsDataSize );

        DataSize convErrorIntegralDataSize = DataSize.create( _config._convSpikeControllerIntegrationPeriod * 1 );
        _convControllerErrorIntegral = new Data( convErrorIntegralDataSize );

        DataSize kernelErrorIntegralDataSize = DataSize.create( _config._kernelSpikeControllerIntegrationPeriod * _config._depth );
        _kernelControllerErrorIntegral = new Data( kernelErrorIntegralDataSize );

        int kernelSize = _config._fieldWidth * _config._fieldHeight * _config._fieldDepth * _config._depth;
        DataSize kernelDataSize = DataSize.create( kernelSize );
        _kernelWeights = new Data( kernelDataSize );

        int kernelFrequencySize = _config._depth;
        DataSize kernelFrequencyDataSize = DataSize.create( kernelFrequencySize );
        _kernelFrequency = new Data( kernelFrequencyDataSize );
        _kernelGains = new Data( kernelFrequencyDataSize );

        DataSize convDataSize = DataSize.create( _config._width, _config._height, _config._depth );
        DataSize convInhibitionDataSize = DataSize.create( _config._width, _config._height );

        _convInverse = new Data( convDataSize );
        _convSums = new Data( convDataSize );
        _convInhibition = new Data( convInhibitionDataSize );
//        _convInhibition = new Data( convDataSize );
        _convIntegrated = new Data( convDataSize );
        _convSpikes = new Data( convDataSize );

        int pw = _config.getPooledWidth();
        int ph = _config.getPooledHeight();
        int pd = _config.getPooledDepth();

        DataSize poolDataSize = DataSize.create( pw, ph, pd );
        DataSize poolInhibitionDataSize = DataSize.create( pw, ph );

        _poolSpikes = new Data( poolDataSize );
        _poolSpikesIntegrated = new Data( poolDataSize );
        _poolIntegrated = new Data( poolDataSize );
//        _poolInhibition = new Data( poolDataSize );
        _poolInhibition = new Data( poolInhibitionDataSize );
        _poolInput = new Data( poolDataSize );
    }

    public Data getOutput() {
        return _poolSpikes;
    }

    public void setInput( Data inputSpikes ) {
        resize( inputSpikes );
        _inputSpikes.copy( inputSpikes );
    }

    public void resize( Data input ) {
        if( ( _inputSpikes == null ) || ( _inputSpikes.getSize() != input.getSize() ) ) {
            _inputSpikes = new Data( input );
            _inputTrace = new Data( input._dataSize );
            _inputInverse = new Data( input._dataSize );
        }
    }

    public void reset() {
        // reset the weights in the kernels
        // "Synaptic weights of convolutional neurons initiate with random values drown from a normal distribution with the mean of 0.8 and STD of 0.05"
        // set inhibition to zero
//        _convSpikeStats.set( 0f );

        int weights = _kernelWeights.getSize();
        for( int i = 0; i < weights; ++i ) {
            double w = _config._r.nextGaussian(); // mean: 0, SD: 1
            w *= _config._kernelWeightStdDev; // scale stddev
            w += _config._kernelWeightsMean; // offset mean
            _kernelWeights._values[ i ] = (float)w;
        }

        // reset gains
        _kernelFrequency.set( 0f ); // only used for diagnostics
        _kernelGains.set( 1f );

        // reset controllers:
        _controllerStatistics.set( 0f );

        int nbrControllers = _config._depth +1;
        for( int c = 0; c < nbrControllers; ++c ) {
            float controllerOutputDefault = _config._kernelSpikeControllerDefault;
            if( c == 0 ) {
                controllerOutputDefault = _config._convSpikeControllerDefault;
            }

            int offset = c * LAYER_STATISTICS_SIZE + LAYER_STATISTICS_CONTROLLER_THRESHOLD;
            _controllerStatistics._values[ offset ] = controllerOutputDefault;
        }

        // clear integrated error
        _convControllerErrorIntegral.set( 0f );
        _kernelControllerErrorIntegral.set( 0f );

        clear();
    }

    public void clear() {
        // allow integrated activity to decay away
        _inputTrace.set( 0f );

        _convInhibition.set( 0f );
        _convIntegrated.set( 0f );

        // TODO I should add a decay to the inhibition rather than reset them each time.
        // Currently they are reset on new image - need to remove this dependency.
        // allow inhibition to end
        _poolInhibition.set( 0f );
        _poolIntegrated.set( 0f );
        _poolSpikesIntegrated.set( 0f );
    }

    public void update( boolean train ) {
        // Check whether its right to reset these spikes, because they inhibit other spikes so maybe I need to keep them.
        // They do need to be reset each step - we remember the inhibition, but these spikes are only for one time step.
        //System.err.println( "Layer: " + this._layer + " Train: " + train );//+ " maxPooling: " + maxPooling );

        // spikes cleared every time
        _convSpikes.set( 0f );
        _poolSpikes.set( 0f );

        updateInputSpikeTrace( _inputSpikes, _inputTrace );
        convolve( _config, _kernelWeights, _inputSpikes, _convSums );
        //float inhSum = _convInhibition.sum();
        //integrate( _config, _kernelWeights, _kernelFrequency, _inputTrace, _convSums, _convInhibition, _convIntegrated, _convSpikes, _convSpikeFrequency, _convSpikeThreshold, train );
//        integrate( _config, _convSpikeStats, _convSpikeControllerWindow, _kernelWeights, _kernelFrequency, _kernelGains, _inputTrace, _convSums, _convInhibition, _convIntegrated, _convSpikes, train );
        integrate( _config, _controllerStatistics, _convControllerErrorIntegral, _kernelControllerErrorIntegral,  _kernelWeights, _kernelFrequency, _kernelGains, _inputTrace, _convSums, _convInhibition, _convIntegrated, _convSpikes, train );

        poolMax( _config, _convIntegrated, _poolIntegrated );
        poolSpike( _config, _convSpikes, _poolSpikes, _poolInhibition );

        _poolSpikesIntegrated.add( _poolSpikes );
//        invert( _config, _kernelWeights, _poolInput, _convInverse, _inputInverse );
    }

    public Data invert( Data poolInput ) {
        _poolInput.copy( poolInput );
        invert( _config, _kernelWeights, _poolInput, _convInverse, _inputInverse );
        return _inputInverse;
    }

    public static void invert(
            SpikingConvolutionalNetworkLayerConfig config,
            Data kernelWeights,
            Data poolValues,
            Data convInverse,
            Data inputInverse ) {
        Int3d i3d = ConvolutionData3d.getSize( inputInverse );
        int iw = i3d.getWidth();
        int ih = i3d.getHeight();
        int id = i3d.getDepth();

        Int3d convSize = config.getConvSize();
        int cw = convSize.getWidth();
        int ch = convSize.getHeight();
        int cd = convSize.getDepth();

        int pw = config._poolingWidth;
        int ph = config._poolingHeight;

        Int3d poolSize = config.getPoolSize();
        int ow = poolSize.getWidth();
        int oh = poolSize.getHeight();
        int od = poolSize.getDepth();

        int kernelSize = config._fieldWidth * config._fieldHeight * config._fieldDepth;

        convInverse.set( 0f );
        inputInverse.set( 0f );

        // 1. undo the pooling, by duplicating them to each input.
        for( int oy = 0; oy < oh; oy++ ) {
            for( int ox = 0; ox < ow; ox++ ) {
                for( int oz = 0; oz < od; ++oz ) {

                    int poolOffset = ConvolutionData3d.getOffset( ox, oy, oz, ow, oh, od );
                    float poolValue = poolValues._values[ poolOffset ];

                    if( poolValue == 0f ) {
                        continue; // avoid work, if spiky input
                    }

                    for( int py = 0; py < ph; py++ ) {
                        for( int px = 0; px < pw; px++ ) {

                            int cx = ox * pw + px;
                            int cy = oy * ph + py;

                            if( ( cx >= cw ) || ( cy >= ch ) ) {
                                continue;
                            }

                            int convolvedOffset = ConvolutionData3d.getOffset( cx, cy, oz, cw, ch, cd );

                            convInverse._values[ convolvedOffset ] = poolValue;
                        } // px
                    } // py

                } // out z
            } // out x
        } // out y

        // 2. find the max model z at each conv. x,y, and invert it.
        for( int cy = 0; cy < config._height; cy++ ) {
            for( int cx = 0; cx < config._width; cx++ ) {

                // foreach( model in the kernel )
                float max = 0f;
                int maxAt = -1;

                for( int cz = 0; cz < config._depth; ++cz ) {

                    int convolvedOffset = ConvolutionData3d.getOffset( cx, cy, cz, config._width, config._height, config._depth );

                    float c = convInverse._values[ convolvedOffset ];

                    if( c > max ) { // over threshold
                        max = c;
                        maxAt = cz;
                    }
                }

                if( maxAt < 0 ) {
                    continue; // nothing to invert, in spiky case
                }

                int cz = maxAt;
                // invert cz:
                // for each element in the field
                for( int fy = 0; fy < config._fieldHeight; fy++ ) {
                    for( int fx = 0; fx < config._fieldWidth; fx++ ) {

                        // e.g. padding = 2 stride = 1
                        // image:         0  1  2
                        //                1  1  1
                        // padded:  0  0  1  1  1
                        //          0  1  2  3  4
                        //         -2 -1  0
                        // i = 0 := -2
                        int ix = cx * config._inputStride - config._inputPadding + fx;
                        int iy = cy * config._inputStride - config._inputPadding + fy;

                        if( ( ix < 0 ) || ( iy < 0 ) || ( ix >= iw ) || ( iy >= ih ) ) {
                            continue;
                        }

                        for( int iz = 0; iz < id; iz++ ) {

                            int fz = iz;
                            int inputOffset = ConvolutionData3d.getOffset( ix, iy, iz, iw, ih, id );
                            int kernelOffset = ConvolutionData3d.getOffset( fx, fy, fz, config._fieldWidth, config._fieldHeight, config._fieldDepth );
                            int kernelsOffset = cz * kernelSize + kernelOffset;
                            float weightValue = kernelWeights._values[ kernelsOffset ];
//if( inputOffset >= inputInverse._values.length ) {
//    int g = 0;
//    g++;
//}
                            float oldInputValue = inputInverse._values[ inputOffset ];
                            float invInputValue = weightValue * max; // invert the output weight through the kernel
                            float newInputValue = oldInputValue + invInputValue;
                            inputInverse._values[ inputOffset ] = newInputValue;
                        } // input z

                    } // field x
                } // field y
            }
        }

    }

// Global pooling output:
//    To compute the output of the global pooling
//    layer, first, the threshold of neurons in the last con-
//    volutional layer were set to be infinite, and then,
//    their final potentials (after propagating the whole
//            spike train generated by the input image) were
//    measured. These final potentials can be seen as
//    the number of early spikes in common between the
//    current input and the stored prototypes in the last
//    convolutional layer. Finally, the global pooling neu-
//    rons compute the maximum potential at their cor-
//    responding neuronal maps, as their output value.

    public static void updateInputSpikeTrace( Data input, Data inputTrace ) {
        // For paper implementation, set inputTrace to max( input, inputTrace ) with no decay.
        // Ie if it ever fires, then make it 1.
        int inputs = input.getSize();
        for( int i = 0; i < inputs; ++i ) {
            float inputValue = input._values[ i ];
            if( inputValue > 0f ) {
                inputTrace._values[ i ] = 1f;
            }
        }
    }

    public static void train(
            SpikingConvolutionalNetworkLayerConfig config,
            Data kernelWeights,
            Data inputSpikeTrace, int cx, int cy, int cz ) {
        // train convolutional cells only
        // i = postsynaptic (output)
        // j = presynaptic (input)
        // t = spike times
        // the exact time difference between two spikes does not affect the weight change, but only its sign is considered
        // dW_ij = a * w_ij * (1-w_ij)
        //                                         a=0.1    w=0    1-w=1      w' = 0.1* 0 * 1      = 0 (note, irrecoverable)
        //                                         a=0.1    w=0.1  1-w=0.9    w' = 0.1* 0.1 * 0.9  = 0.009   slow
        //                                         a=0.1    w=0.9  1-w=0.1    w' = 0.1* 0.1 * 0.9  = 0.009   slow
        //                                         a=0.1    w=0.5  1-w=0.5    w' = 0.1* 0.5 * 0.5  = 0.025   fast
        //                                         a=0.1    w=0.6  1-w=0.4    w' = 0.1* 0.6 * 0.4  = 0.024   fast
        //
        // ... where a = a+ or a- depending on:

        // if t_j − t_i ≤ 0:           (pre)j=5     (post)i=10     5-10 = -5   ie same time or pre first
        // a = a+

        // if t_j − t_i > 0:           (pre)j=10    (post)i=5      10-5 = 5   ie post first
        // a = a-
        //
        // "it is assumed that if a presynaptic neuron does not fire before the postsynaptic one, it will fire later."
        // i.e. once the post cell fires, we can train.

        // During the learning of a convolutional layer, neurons in the same map, detecting the same feature in
        // different locations, integrate input spikes and compete with each other to do the STDP. The first
        // neuron which reaches the threshold and fires, if any, is the winner (global intra-map competition)

        // The winner triggers the STDP and updates its synaptic weights.
        //
        // As mentioned before, neurons in different locations of the same map have the same input
        // synaptic weights (i.e., weight sharing) to be selective to the same feature. Hence, the winner neuron
        // prevents other neurons in its own map to do STDP and duplicates its updated synaptic weights into them.
        //
        // Also, there is a local inter-map competition for STDP. When a neuron is allowed to do the STDP,
        // it prevents the neurons in other maps within a small neighborhood around its location from doing STDP
        // This competition is crucial to encourage neurons of different maps to learn different features.
        //
        // it is probable that some competitor neurons fire at the same time step. One possible scenario is to pick one randomly and allow
        // it to do STDP. But a better alternative is to pick the one which has the highest potential indicating
        // higher similarity between its learned feature and input pattern (do a max).
        Int3d i3d = ConvolutionData3d.getSize( inputSpikeTrace );
        int iw = i3d.getWidth();
        int ih = i3d.getHeight();
        int id = i3d.getDepth();

        assert( id <= config._fieldDepth );

        int kernelSize = config._fieldWidth * config._fieldHeight * config._fieldDepth;

        for( int fy = 0; fy < config._fieldHeight; fy++ ) {
            for( int fx = 0; fx < config._fieldWidth; fx++ ) {

                int ix = cx * config._inputStride - config._inputPadding + fx;
                int iy = cy * config._inputStride - config._inputPadding + fy;

                if( ( ix < 0 ) || ( iy < 0 ) || ( ix >= iw ) || ( iy >= ih ) ) {
                    continue;
                }

                for( int iz = 0; iz < id; iz++ ) {

                    int fz = iz;
                    int inputOffset = ConvolutionData3d.getOffset( ix, iy, iz, iw, ih, id );
                    int kernelOffset = ConvolutionData3d.getOffset( fx, fy, fz, config._fieldWidth, config._fieldHeight, config._fieldDepth );
                    int kernelsOffset = cz * kernelSize + kernelOffset;

                    float inputValue = inputSpikeTrace._values[ inputOffset ];
                    float learningRate = config._kernelWeightsLearningRate;

                    // Dave's training enhancements:
                    // Nonstationary learning rule: Add random amt to active pre-synaptic when post-syn firing rate too low (disused)
                    //                              Do this until the cell fires before inhibition, at which point it is close tot he usable space.
                    // Nonsaturation rule: Do something to avoid weight being fixed at 1 ever.

                    float oldWeightValue = kernelWeights._values[ kernelsOffset ];
                    //float newWeightValue = oldWeightValue + learningRate * oldWeightValue * ( 1f - oldWeightValue );

                    // weight = 0.5, then diff = 0,   *2 = 0, 1- = 1
                    // weight = 0.0, then diff = 0.5, *2 = 1, 1- = 0
// but stops learning at ends..
//                    float weightRate = 1.0f - ( Math.abs( 0.5f - oldWeightValue ) * 2.f );
//                    float deltaValue =
//                    float newWeightValue = oldWeightValue + learningRate * deltaValue;

// OLD
//                    float newWeightValue = Unit.lerp( inputValue, oldWeightValue, learningRate );
// NEW
                    float d = ( inputValue - oldWeightValue ); // max: if i=0 and w=1 then -1
                    float addWeightValue = d * Math.abs( d );  // -1 * 1 = -1
                    // if oldW = 1 and input =1 then delta = 0
                    addWeightValue *= learningRate;
                    float newWeightValue = oldWeightValue + addWeightValue;
                    // w1   i   d=(i-w1)*r         d^2  learningRate r = 0.1
                    // 0.0  0   0.0       +0.00    0
                    // 0.0  1   1.0       +0.10    0.01
                    // 0.7  1   0.3       +0.03    0.0009
                    // 0.5  0   0.5       -0.05    0.0025
                    // 0.5  1   0.5       +0.05    0.0025
                    // 1.0  0   1.0       -0.10    0.01
                    // 1.0  1   0.0       +0.00    0

// Sanitize:
                    newWeightValue = Math.max( 0f, Math.min( 1f, newWeightValue ) );

                    kernelWeights._values[ kernelsOffset ] = newWeightValue;
                } // input z

            } // field x
        } // field y
    }

    public static void integrate(
            SpikingConvolutionalNetworkLayerConfig config,
            Data controllerStatistics,
            Data convControllerErrorIntegral,
            Data kernelControllerErrorIntegral,
//            Data convSpikeErrorHistory,
            //DiscreteTimePIDController convSpikeThresholdController,
            Data kernelWeights,
            Data kernelFrequency,
            Data kernelGains,
            Data inputTrace,
            Data convSums,
            Data convInhibition,
            Data convIntegrated,
            Data convSpikes,
            boolean train ) {
        // Neurons in all convolutional layers are non-leaky integrate-and-fire neurons
        // V_i(t) = V_i(t-1) + sum(j): W_ji * S_j(t-1)
        // S_j = spike of neuron j
        // W_ji = weight j --> i
        HashMap< Integer, Integer > kernelSpikeCount = new HashMap< Integer, Integer >();

        for( int k = 0; k < config._depth; ++k ) {
            kernelSpikeCount.put( k, 0 );
        }

        int controller = 0;
        int controllerOutputOffset = controller * LAYER_STATISTICS_SIZE + LAYER_STATISTICS_CONTROLLER_THRESHOLD;
        float convSpikeThreshold = controllerStatistics._values[ controllerOutputOffset ]; //convSpikeStats._values[ LAYER_STATI STICS_CONTROLLER_THRESHOLD ];

        int spikes = 0;

        for( int cy = 0; cy < config._height; cy++ ) {
            for( int cx = 0; cx < config._width; cx++ ) {

                // inhibition in Z: if any cell fired in this position then inhibit others
                int inhibitionOffset = Data2d.getOffset( config._width, cx, cy );
                float inhibitionValue = convInhibition._values[ inhibitionOffset ];

                // "it is probable that some competitor neurons fire at the same time step. One possible scenario is to pick one randomly and allow
                // it to do STDP. But a better alternative is to pick the one which has the highest potential indicating
                // higher similarity between its learned feature and input pattern (do a max)."
                // foreach( model in the kernel )
                float czMax = 0f;
                int czMaxAt = 0;
                for( int cz = 0; cz < config._depth; ++cz ) {

                    int convolvedOffset = ConvolutionData3d.getOffset( cx, cy, cz, config._width, config._height, config._depth );

                    float c = convSums._values[ convolvedOffset ];
                    float gain = kernelGains._values[ cz ];//getKernelGain( kernelFrequency, cz, config._kernelSpikeFrequencyTarget );
                    c *= gain;
                    float v1 = convIntegrated._values[ convolvedOffset ];
                    float v2 = v1 + c;

                    convIntegrated._values[ convolvedOffset ] = v2;

                    if( v2 >= czMax ) {
                        czMax = v2;
                        czMaxAt = cz;
                    }
                }

//                if( inhibitionValue > 0f ) {
//                    continue; // no spike possible, due to inhibition
//                }
//
                if( czMax <= 0f ) {
                    continue; // no input
                }

                if( czMax >= convSpikeThreshold ) { // over threshold

                    spikes += 1; // TODO include this spike in the value

                    int convolvedOffset = ConvolutionData3d.getOffset( cx, cy, czMaxAt, config._width, config._height, config._depth );
                    convIntegrated._values[ convolvedOffset ] = 0f; // clear integrated activity; actually this makes no difference due to the inhibition at this location if another cell has fired
                    // BUT, we should restore this clearing when we move to realtime modelling.

                    if( inhibitionValue > 0f ) {
                        continue; // no spike possible, due to inhibition
                        // Also no training.
                    }

                    // Here: Only include spikes that weren't inhibited.
//                    spikes += 1; // TODO include spikes with or without inhibition?

                    //spikingKernels.add( czMaxAt );
                    int oldSpikeCount = kernelSpikeCount.get( czMaxAt );
                    int newSpikeCount = oldSpikeCount +1;
                    kernelSpikeCount.put( czMaxAt, newSpikeCount );

                    convSpikes._values[ convolvedOffset ] = 1f;
                    convInhibition._values[ inhibitionOffset ] = 1f; // inhibit here

//                            // inhibit all other cells at this position (x,y)
//                            // This allows ongoing (cumulative) spiking of this cell.
//                            for( int cz2 = 0; cz2 < config._depth; ++cz2 ) {
//                                if( cz2 == cz ) {
//                                    continue;
//                                }
//
//                                inhibitionOffset = ConvolutionData3d.getOffset( cx, cy, cz, config._width, config._height, config._depth );
//                                convInhibition._values[ inhibitionOffset ] = 1f;
//                            }

                    // on output spike, we can train:
                    if( train ) {
                        train( config, kernelWeights, inputTrace, cx, cy, czMaxAt );
                    }

                } // z (max)
            } // x
        } // y

        // The remaining code implements the convolutional layer homeostasis. So if we're not learning, it doesn't get
        // used. We simply stop updating the stats and the threshold controller.
        if( !train ) {
            return;
        }

        // update layer spike frequency - the observed variable
        // normalize for layer area (but not depth)
        int area = config._width * config._height;
        float convSpikeDensity = (float)spikes / (float)area; // normalize for area of the layer

        int controllerIndex = 0;
        float controllerGainP = 1f; // TODO make param
        float controllerGainI = 2;
        Float min = 0f; // must be positive
        Float max = null;
        updateController(
                convControllerErrorIntegral, controllerStatistics, controllerIndex,
                config._convSpikeControllerUpdatePeriod,
                config._convSpikeControllerIntegrationPeriod,
                config._convSpikeControllerTarget,
                convSpikeDensity,
                controllerGainP,
                controllerGainI,
                min, max, true ); // min, max

/*        float convSpikeCount = convSpikeStats._values[ LAYER_STATISTICS_WINDOW_COUNT ];
        float convSpikeSum = convSpikeStats._values[ LAYER_STATISTICS_WINDOW_SUM ];

        convSpikeCount += 1;
        convSpikeSum += convSpikeDensity;

        if( convSpikeCount >= config._convSpikeControllerUpdatePeriod ) {

            float averageDensity = convSpikeSum / (float)config._convSpikeControllerUpdatePeriod;
            float target = config._convSpikeControllerDensityTarget;// / config._convSpikePeriod;
            float error = averageDensity - target;

            int historySize = convSpikeErrorHistory.getSize();
            convSpikeErrorHistory.rotate( 1 ); // i.e. 0 -> 1, last -> 0
            convSpikeErrorHistory._values[ 0 ] = error;
            float integratedError = convSpikeErrorHistory.sum();
            integratedError /= (float)historySize;

            float gainP = 1f; //
            float gainI = 2f;

            // error = observed - target
            // TOO MANY SPIKES :             e =     0.5 - 0.2 =  0.3      ACTION: Increase.
            // TOO FEW  SPIKES :             e =     0.0 - 0.2 = -0.2      ACTION: Decrease.

            float deltaP = gainP * error;
            float deltaI = gainI * integratedError;
            float deltaPI = deltaP + deltaI;

            float convSpikeThresholdNew = convSpikeThreshold + deltaPI;
            convSpikeThresholdNew = Math.max( 0f, convSpikeThresholdNew );

            convSpikeSum = 0;
            convSpikeCount = 0;
            convSpikeStats._values[ LAYER_STATISTICS_CONTROLLER_THRESHOLD ] = convSpikeThresholdNew;
            convSpikeStats._values[ LAYER_STATISTICS_CONTROLLER_MEASURED ] = averageDensity;
            convSpikeStats._values[ LAYER_STATISTICS_CONTROLLER_DELTA ] = deltaPI;
            convSpikeStats._values[ LAYER_STATISTICS_CONTROLLER_ERROR ] = error;
            convSpikeStats._values[ LAYER_STATISTICS_CONTROLLER_ERROR_INTEGRATED ] = integratedError;
        }

        convSpikeStats._values[ LAYER_STATISTICS_SPIKE_DENSITY ] = convSpikeDensity;
        convSpikeStats._values[ LAYER_STATISTICS_WINDOW_COUNT ] = convSpikeCount;
        convSpikeStats._values[ LAYER_STATISTICS_WINDOW_SUM ] = convSpikeSum;*/


        controllerGainP = 100f;
        controllerGainI = 0;
        min = 1f; // ensures that gain can't be < 1 i.e. isn't penalized for being used too often
        max = null;
        for( int cz = 0; cz < config._depth; ++cz ) {
            int kernelSpikes = kernelSpikeCount.get( cz );
            float kernelSpikeDensity = (float)kernelSpikes / (float)area; // make it invariant to area changes

            controllerIndex = 1 + cz;

            int errorIntegralPeriod = config._kernelSpikeControllerIntegrationPeriod;
            Data errorIntegral = new Data( errorIntegralPeriod );
            int offsetThis = 0;
            int offsetThat = cz * errorIntegralPeriod;
            errorIntegral.copyRange( kernelControllerErrorIntegral, offsetThis, offsetThat, errorIntegralPeriod );

            float gain = updateController(
                    errorIntegral, controllerStatistics, controllerIndex,
                    config._kernelSpikeControllerUpdatePeriod,
                    config._kernelSpikeControllerIntegrationPeriod,
                    config._kernelSpikeControllerTarget,
                    kernelSpikeDensity,
                    controllerGainP,
                    controllerGainI,
                    min, max, false ); // min, max

            kernelGains._values[ cz ] = gain; // new gain

            offsetThis = cz * errorIntegralPeriod;
            offsetThat = 0;
            kernelControllerErrorIntegral.copyRange( errorIntegral, offsetThis, offsetThat, errorIntegralPeriod );
        }

        // update kernel frequencies depending whether each kernel z fired or not.
        // (Only includes uninhibited spikes)
        for( int cz = 0; cz < config._depth; ++cz ) {
            // update the frequency for each kernel z
            //boolean spiked = spikingKernels.contains( cz );
            int kernelSpikes = kernelSpikeCount.get( cz );
            float kernelSpikeDensity = (float)kernelSpikes / (float)area;
            float fOld = kernelFrequency._values[ cz ];
            float fNew = Unit.lerp( kernelSpikeDensity, fOld, 0.001f );
            fNew = Math.min( 1f, Math.max( 0f, fNew ) ); // clamped at 0 <= f <= 1
            kernelFrequency._values[ cz ] = fNew;
        }

        // This is only about the distribution of spikes (relative rate) but not the overall rate of spikes?
/*        float sumKernelSpikeDensities = 0f;

        for( int cz = 0; cz < config._depth; ++cz ) {
            // update the frequency for each kernel z
            //boolean spiked = spikingKernels.contains( cz );
            int kernelSpikes = kernelSpikeCount.get( cz );
            float kernelSpikeDensity = (float)kernelSpikes / (float)area;
            //updateKernelFrequency( kernelFrequency, cz, kernelSpikeDensity, config._kernelSpikeFrequencyLearningRate );
            float fOld = kernelFrequency._values[ cz ];
            float fNew = fOld + kernelSpikeDensity;
            kernelFrequency._values[ cz ] = fNew;
            sumKernelSpikeDensities += fNew;
        }

        // Now check if we need to update the gains (periodically):
        float kernelFrequencyTimer = convSpikeStats._values[ LAYER_STATISTICS_KERNEL_GAIN_TIMER ];
        ++kernelFrequencyTimer;
        if( kernelFrequencyTimer >= config._kernelFrequencyUpdatePeriod ) {

            // Update the kernel frequency stats:
            for( int cz = 0; cz < config._depth; ++cz ) {

                float gain = 1f;
                if( sumKernelSpikeDensities > 0f ) {
                    float f = kernelFrequency._values[ cz ];

                    // add 1 spike to make it nonzero
                    float kernelSpikeDensity = 1f / (float)area;
                    f += kernelSpikeDensity;

                    float relativeFrequency = f / sumKernelSpikeDensities;
                    gain = getKernelGain( relativeFrequency, config._kernelSpikeFrequencyTarget );
                }

                kernelGains._values[ cz ] = gain; // new gain
                kernelFrequency._values[ cz ] = 0f; // clear accumulated spike density
            }

            kernelFrequencyTimer = 0; // reset timer
        }
        convSpikeStats._values[ LAYER_STATISTICS_KERNEL_GAIN_TIMER ] = kernelFrequencyTimer;*/

//        Also, there is a lateral inhibition mechanism in
//        all convolutional layers. When a neuron fires, in an
//        specific location, it inhibits other neurons in that
//        location belonging to other neuronal maps and does
//        not allow them to fire in the following time steps.
//        In addition, neurons are not allowed to fire more
//        than once. These together provides an sparse but
//        highly informative coding, because, there can be
//        at most one spike at each location which indicates
//        the existence of a particular visual feature in that
//        location.
    }

    public static float updateController(
        Data errorIntegral,
        Data controllerStatistics,
        int controllerIndex,
        int period,
        int errorIntegralPeriod,
        float target,
        float sample,
        float gainP,
        float gainI,
        Float min,
        Float max,
        boolean inverse
    ) {
        int statisticsOffset = controllerIndex * LAYER_STATISTICS_SIZE;
        int nbrSamples = (int)controllerStatistics._values[ statisticsOffset + LAYER_STATISTICS_WINDOW_COUNT ];
        float sumSamples =    controllerStatistics._values[ statisticsOffset + LAYER_STATISTICS_WINDOW_SUM ];
        float outputOld =     controllerStatistics._values[ statisticsOffset + LAYER_STATISTICS_CONTROLLER_THRESHOLD ];

        nbrSamples += 1;
        sumSamples += sample;

        if( nbrSamples >= period ) {

            if( controllerIndex > 0 ) {
                int g = 0;
                g++;
            }

            float sampleMean = sumSamples / (float)period;
            float error = sampleMean - target;

            if( inverse == false ) {
                error = target - sampleMean;
            }

            //int historySize = controllerErrorIntegral.getSize();
//            Data errorIntegral = new Data( errorIntegralPeriod );
//            int offsetThis = 0;
//            int offsetThat = controllerIndex * errorIntegralPeriod;
//            errorIntegral.copyRange( controllerErrorIntegral, offsetThis, offsetThat, errorIntegralPeriod );
            errorIntegral.rotate( 1 ); // i.e. 0 -> 1, last -> 0
            errorIntegral._values[ 0 ] = error; // append new sample
//            offsetThis = controllerIndex * errorIntegralPeriod;
//            offsetThat = 0;
//            controllerErrorIntegral.copyRange( errorIntegral, offsetThis, offsetThat, errorIntegralPeriod );

            float errorIntegrated = errorIntegral.sum();
            errorIntegrated /= (float)errorIntegralPeriod;

            float deltaP = gainP * error;
            float deltaI = gainI * errorIntegrated;
            float deltaPI = deltaP + deltaI;

            float outputNew = outputOld + deltaPI;
            if( min != null ) {
                outputNew = Math.max( min, outputNew );
            }
            if( max != null ) {
                outputNew = Math.min( max, outputNew );
            }

            // reset average:
            sumSamples = 0;
            nbrSamples = 0;

            controllerStatistics._values[ statisticsOffset + LAYER_STATISTICS_CONTROLLER_THRESHOLD ] = outputNew;
            controllerStatistics._values[ statisticsOffset + LAYER_STATISTICS_CONTROLLER_MEASURED ] = sampleMean;
            controllerStatistics._values[ statisticsOffset + LAYER_STATISTICS_CONTROLLER_DELTA ] = deltaPI;
            controllerStatistics._values[ statisticsOffset + LAYER_STATISTICS_CONTROLLER_ERROR ] = error;
            controllerStatistics._values[ statisticsOffset + LAYER_STATISTICS_CONTROLLER_ERROR_INTEGRATED ] = errorIntegrated;
        }

        controllerStatistics._values[ statisticsOffset + LAYER_STATISTICS_SPIKE_DENSITY ] = sample; // store latest sample
        controllerStatistics._values[ statisticsOffset + LAYER_STATISTICS_WINDOW_COUNT ] = nbrSamples;
        controllerStatistics._values[ statisticsOffset + LAYER_STATISTICS_WINDOW_SUM ] = sumSamples;

        float output = controllerStatistics._values[ statisticsOffset + LAYER_STATISTICS_CONTROLLER_THRESHOLD ];
        return output;
    }

    /**
     *
     * Nominally returns 1.0. However when frequency is too low or too high, the gain is adjusted.
     * The gain can be essentially unbounded as the frequency approaches its limits (0 or 1).
     * We can have an expectation of what a nominal frequency would be.
     *
     * @param kernelFrequency
     * @param cz
     * @return
     */
    public static float getKernelGain( Data kernelFrequency, int cz, float expectedFrequency ) {
        float f = kernelFrequency._values[ cz ];

        // say nominal is 0.05 (1 in 20, which is high)
        // when we get to zero the gain should be very high
        float MIN_FREQUENCY = 0.00001f;
        float fMin = Math.max( MIN_FREQUENCY, f );
        float fraction = fMin / expectedFrequency;
        float gain = 1f / fraction;
        return gain;
    }

    public static float getKernelGain( float relativeFrequency, float expectedFrequency ) {
        // say nominal is 0.05 (1 in 20, which is high)
        // when we get to zero the gain should be very high
        float MIN_FREQUENCY = 0.00001f; // this could also be expressed as max gain?
        float fMin = Math.max( MIN_FREQUENCY, relativeFrequency );
        float fraction = fMin / expectedFrequency;
        float gain = 1f / fraction;
        return gain;
    }

    /**
     * There are essentially 2 ways to ensure all kernel models are fully utilized. One, to adapt the weights of disused
     * cells. Two, to artificially make disused cells fire and adapt their weights normally when they fire. Conceivably
     * we could have a situation where some inputs leave a lot of unintegrated potential, but all cells are firing at
     * times... is this possible? Or would firing cells learn to integrate this?
     *
     * Kernel frequency should adapt linearly, so we add a small amount whenever it fires (or doesn't). It can measure
     * the approximate recent history within a range of values defined by the learningRate.
     *
     * @param kernelFrequency
     * @param cz
     * @param value
     * @param learningRate
     */
    public static void updateKernelFrequency( Data kernelFrequency, int cz, float value, float learningRate ) {
//        float delta = 0f;
//        if( spiked ) {
//            delta = 1f;
//        }
        float fOld = kernelFrequency._values[ cz ];
        float fNew = Unit.lerp( value, fOld, learningRate );
        fNew = Math.min( 1f, Math.max( 0f, fNew ) ); // clamped at 0 <= f <= 1
        kernelFrequency._values[ cz ] = fNew;
    }

    /**
     * Max implements this concept:
     * "Pooling neurons are integrate-and-fire neurons whose input synaptic weights and threshold
     * are all set to one. Hence, the first input spike activates them and leads to an output spike. Regarding
     * to the rank-order coding, each pooling neuron is allowed to fire at most once. It should be noted that
     * no learning occurs in pooling layers."
     *
     * - If we inhibit spikes pre-pooling, we don't need to worry about inhibiting pooling spikes? No, because this is
     *   spatial pooling. Need to additionally implement lateral inhibition.
     *
     * @param config
     * @param convSpikes
     * @param poolSpikes
     * @param poolInhibition
     * @return
     */
    protected static void poolSpike(
            SpikingConvolutionalNetworkLayerConfig config,
            Data convSpikes,
            Data poolSpikes,
            Data poolInhibition ) {
        Int3d i3d = ConvolutionData3d.getSize( convSpikes );
        int iw = i3d.getWidth();
        int ih = i3d.getHeight();
        int id = i3d.getDepth();

        int pw = config._poolingWidth;
        int ph = config._poolingHeight;

        int ow = Useful.DivideRoundUp( iw, pw );
        int oh = Useful.DivideRoundUp( ih, ph );
        int od = id;

        for( int oy = 0; oy < oh; oy++ ) {
            for( int ox = 0; ox < ow; ox++ ) {
                for( int oz = 0; oz < od; ++oz ) {

                    // neighbourhood inhibition
                    // Note this allows other z to still fire. Unclear if this is what was intended.
                    int poolOffset = ConvolutionData3d.getOffset( ox, oy, oz, ow, oh, od );
                    int inhibitionOffset = Data2d.getOffset( ow, ox, oy );
//                    int inhibitionOffset = poolOffset;
                    float inhibitionValue = poolInhibition._values[ inhibitionOffset ];
                    if( inhibitionValue > 0f ) {
                        continue; // no further output from this column
                    }

                    // find max or some other operator within a spatial area
                    float max = 0f;

                    for( int py = 0; py < ph; py++ ) {
                        for( int px = 0; px < pw; px++ ) {

                            int ix = ox * pw + px;
                            int iy = oy * ph + py;

                            if( ( ix >= iw ) || ( iy >= ih ) ) {
                                continue;
                            }

                            int convolvedOffset = ConvolutionData3d.getOffset( ix, iy, oz, iw, ih, id );

//                            if( convolvedOffset >= convSpikes._values.length ) {
//                                int g = 0;
//                                g++;
//                            }
                            float convolvedValue = convSpikes._values[ convolvedOffset ];
                            max = Math.max( convolvedValue, max );

                        } // px
                    } // py

                    if( max > 0f ) { // if( any spike )
                        poolSpikes._values[ poolOffset ] = max;
                        poolInhibition._values[ inhibitionOffset ] = 1f; // prevent further spikes in this col
                    }

                } // out z
            } // out x
        } // out y

    }

    protected static void poolMax(
            SpikingConvolutionalNetworkLayerConfig config,
            Data convIntegrated,
            Data poolIntegrated ) {
        Int3d i3d = ConvolutionData3d.getSize( convIntegrated );
        int iw = i3d.getWidth();
        int ih = i3d.getHeight();
        int id = i3d.getDepth();

        int pw = config._poolingWidth;
        int ph = config._poolingHeight;

        int ow = Useful.DivideRoundUp( iw, pw );
        int oh = Useful.DivideRoundUp( ih, ph );
        int od = id;

        for( int oy = 0; oy < oh; oy++ ) {
            for( int ox = 0; ox < ow; ox++ ) {
                for( int oz = 0; oz < od; ++oz ) {

                    // neighbourhood inhibition
                    // Note this allows other z to still fire. Unclear if this is what was intended.
                    int poolOffset = ConvolutionData3d.getOffset( ox, oy, oz, ow, oh, od );

                    // find max or some other operator within a spatial area
                    float max = 0f;

                    for( int py = 0; py < ph; py++ ) {
                        for( int px = 0; px < pw; px++ ) {

                            int ix = ox * pw + px;
                            int iy = oy * ph + py;

                            if( ( ix >= iw ) || ( iy >= ih ) ) {
                                continue;
                            }

                            int convolvedOffset = ConvolutionData3d.getOffset( ix, iy, oz, iw, ih, id );

                            float convolvedValue = convIntegrated._values[ convolvedOffset ];
                            max = Math.max( convolvedValue, max );

                        } // px
                    } // py

                    poolIntegrated._values[ poolOffset ] += max; // max integrated value for concept z in the pooled input area.

                } // out z
            } // out x
        } // out y

    }

    protected static void convolve( SpikingConvolutionalNetworkLayerConfig config, Data kernelWeights, Data inputSpikes, Data convSums ) {

        Int3d i3d = ConvolutionData3d.getSize( inputSpikes );
        int iw = i3d.getWidth();
        int ih = i3d.getHeight();
        int id = i3d.getDepth();

        // http://cs231n.github.io/convolutional-networks/
        // W2 = (W1−F+2P)/S+1W2 = (W1−F+2P)/S+1
        // H2 = (H1−F+2P)/S+1H2 = (H1−F+2P)/S+1
        //Data output = new Data( config._width, config._height, config._depth );

        assert( id == config._fieldDepth );

        int kernelSize = config._fieldWidth * config._fieldHeight * config._fieldDepth;

//        if( inputSpikes.sum() != 0f ) {
//            int g = 0;
//            g++;
//        }
//float maxInput = 0;
//float minInput = 0;
//float maxWeight = 0;
//float minWeight = 0;

        // for each model position (shifts by stride pixels each time)
        for( int cy = 0; cy < config._height; cy++ ) {
            for( int cx = 0; cx < config._width; cx++ ) {

                // foreach( model in the kernel )
                for( int cz = 0; cz < config._depth; ++cz ) {
                    float sum = 0f;

                    // for each element in the field
                    for( int fy = 0; fy < config._fieldHeight; fy++ ) {
                        for( int fx = 0; fx < config._fieldWidth; fx++ ) {

                            // e.g. padding = 2 stride = 1
                            // image:         0  1  2
                            //                1  1  1
                            // padded:  0  0  1  1  1
                            //          0  1  2  3  4
                            //         -2 -1  0
                            // i = 0 := -2
                            int ix = cx * config._inputStride - config._inputPadding + fx;
                            int iy = cy * config._inputStride - config._inputPadding + fy;

                            if( ( ix < 0 ) || ( iy < 0 ) || ( ix >= iw ) || ( iy >= ih ) ) {
                                continue; // add nothing, because outside image bounds
                            }

                            for( int iz = 0; iz < id; iz++ ) {

                                int fz = iz;
                                int inputOffset = ConvolutionData3d.getOffset( ix, iy, iz, iw, ih, id );
                                int kernelOffset = ConvolutionData3d.getOffset( fx, fy, fz, config._fieldWidth, config._fieldHeight, config._fieldDepth );
                                int kernelsOffset = cz * kernelSize + kernelOffset;

                                float inputValue = inputSpikes._values[ inputOffset ];
                                if( kernelsOffset >= kernelWeights._values.length ) {
                                    int g = 0;
                                    g++;
                                }
                                float weightValue = kernelWeights._values[ kernelsOffset ];

//                                 maxInput = Math.max( inputValue, maxInput );
//                                 minInput = Math.min( inputValue, minInput );
//                                 maxWeight = Math.max( weightValue, maxWeight );
//                                 minWeight = Math.min( weightValue, minWeight );

                                float product = inputValue * weightValue;
                                sum += product;
                            } // input z

                        } // field x
                    } // field y

                    int convOffset = ConvolutionData3d.getOffset( cx, cy, cz, config._width, config._height, config._depth );
                    convSums._values[ convOffset ] = sum;

                } // convolution z (models)

            } // convolution x
        } // convolution y
    }
}
