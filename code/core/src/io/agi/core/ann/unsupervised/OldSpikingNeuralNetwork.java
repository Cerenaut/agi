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
import io.agi.core.data.FloatArray;

import java.util.ArrayList;

/**
 * Created by dave on 20/07/16.
 */
public class OldSpikingNeuralNetwork {

    public Data _input1;
    public Data _input1Spikes;

    public float _t = 0f;

    // http://www.briansimulator.org/docs/units.html
    public static final float mV = 1f / 1000f;
    public static final float ms = 1f / 1000f;
    public static final float nS = 1e-9f; // nano = 10^-9 nS = _units.nsiemens  ///  nsiemens = Unit.create_scaled_unit(siemens, "n")  /// https://en.wikipedia.org/wiki/Siemens_(unit)

    // 1 / 1,000,000,000 nano
    // 0.000000001 1 x 10 ^-9
    float v_rest_e = -65.f * mV;
    float v_rest_i = -60.f * mV;
    float v_reset_e = -65.f * mV;
    float v_reset_i = -45.f * mV;
    float v_thresh_e = -52.f * mV;
    float v_thresh_i = -40.f * mV;
    float refrac_e = 5.f * ms;
    float refrac_i = 2.f * ms;

    float tc_pre_ee = 20f * ms;
    float tc_post_1_ee = 20f * ms;
    float tc_post_2_ee = 40f * ms;
    float nu_ee_pre =  0.0001f;//      # learning rate
    float nu_ee_post = 0.01f;//       # learning rate
    float wmax_ee = 1.0f;
    float exp_ee_pre = 0.2f;
    float exp_ee_post = exp_ee_pre;
    float STDP_offset = 0.4f;

//    boolean _input1Spiked = false;

    public int getSteps( float dt, float totalTime ) {
        // total = 2560
        // dt = 0.13
        //
        int steps = (int)( totalTime / dt );
        if( ( dt * (float)steps ) < totalTime ) {
            ++steps;
        }
        return steps;
    }

    public float update( float t1, float dt, int steps ) {

        float t2 = t1;

        for( int i = 0; i < steps; ++i ) {
            update( t2, dt );
            t2 += dt;
        }

        return t2;
    }

//TODO list:
//- delays on connections (before inputs). although really on axons?
//- input intensity to frequency
//- synapse spike history to weight
//- synapse weight learning.
//- cell sum input weight to voltage
//- cell voltage integration
//- cell threshold spiking
//- cell refractory period
//- cell learning??
//
    public void update( float t1, float dt ) {
//        1. Update every NeuronGroup, this typically performs an integration time step for the differential equations defining the neuron model.
//        2. Check the threshold condition and propagate the spikes to the target neurons.
//        3. Update every Synapses, this may include updating the state of targeted NeuronGroup objects
//        4. Reset all neurons that spiked.
//        5. Call all user-defined operations and state monitors.
    //    updateInputs(); // update magnitude inputs turned into spike trains
    //    updateExcitatoryCells(); // determine activation level of cells (ie. voltages)
        updateSpikes(); // produce spikes where activation exceeds thresholds
        resetCells();
        updateSynapses(); // update the synapses based on inbound spike trains and adapt their weights
    }

    // sparse set of connections:
    // [ s,s,s,    =  spikes (1 or 0)
    // [ t,t,t,    =  0
    // build it into an object:
    // a spike train object.  (spike times)
    // serialize by filtering based on oldest set
    // SpikeTrain =

    protected void updateCells() {
        int cells = 0;
        int inputs = 0;
        SpikeTrain st;
        float t;

        // activate cells
        for( int c = 0; c < cells; ++c ) {
            float sum = 0f;

            // do excitatory and inhibitory
            for( int i = 0; i < inputs; ++i ) {
                int inputIndex = i; // TODO
                float cellInputDelay = getDelay( c, i );
//                ArrayList< Float > inputSpikes = st.getSpikeTimes( inputIndex, cellInputDelay, t );
//                for( Float tSpike : inputSpikes ) {
//                    float weight = getWeight( tSpike, t );
//                    sum += weight;
//                }
            }

            // update the cell itself

            // update output inc spikes

        }

        // train cells
        for( int c = 0; c < cells; ++c ) {
            int cellIndex = c; // TODO
            float cellPostDelay = 0f; // post synaptic
//            ArrayList< Float > postSpikes = st.getSpikeTimes( cellIndex, cellPostDelay, t );

//            for( int i = 0; i < inputs; ++i ) {
//                float delay = getDelay( c, i );
////                ArrayList< Float > preSpikes = st.getSpikeTimes( inputIndex, cellInputDelay, t );
//  //              for( Float tSpike : preSpikes ) {
//                    // train the cell
//  //              }
//            }
        }
    }

    public float getDelay( int cell, int input ) {
        return 0f;
    }

    public int getExcitatoryCellIndex( ) {
        return 0;
    }

    // Paper: Each input is a Poisson spike-train,
    // class brian.PoissonGroup(N, rates=0.0 * hertz, clock=None)
    // A group that generates independent Poisson spike trains.
// " Poisson-distributed spike trains, with firing rates proportional to the intensity of the pixels of the MNIST"
// "   Specifically, the maximum pixel intensity of 255 is divided by 4, resulting in input firing rates between 0 and
// 63.75 Hz. Additionally, if the excitatory neurons in the second layer fire less than five spikes within 350 ms, the
// maximum input firing rate is increased by 32 Hz and the example is presented again for 350 ms. This process is
// repeated until at least five spikes have been fired during the entire time the particular example was presented.

    // all inputs --> all excitatory
    // all inhibitory --> all excitatory
    // each excitatory --> one inhibitory
    // Paper: All synapses from input neurons to excitatory neurons are learned using STDP.

    protected void updateInputs( float t1, float dt ) {
//        for-each( input scalar ) {
//            set the frequency;
//            update any spikes;
//        }

//        1 / 60 = 0.0166
//        1 / 30 = 0.0333
        float tau = 3000f;
        float max = 1.f;
        float reset = 0f;
 //       float dx_dt = max / tau; // i.e. per tau unit of time, it increases by dx_dt

        // eg. dt = 1000
        // tau = 3000
        // dx_dt = 1/3000 = 0.00033
        // z = 1000/3000 = 0.333
        // x1 = 0
        // x2 = x1 + ( dx_dt * dt )
        // x2 = 0 + ( 0.00033 * 1000 )
        // x2 = 0.33
        // x2 = 0.33 + ( 0.00033 * 1000 )
        // x2 = 0.66
        // ...
        // x2 = 1.00

        int inputs = _input1.getSize();

        for( int i = 0; i < inputs; ++i ) {
            float value = _input1._values[ i ];
            float dx_dt = value * (max / tau );
            float x1 = _input1Integrated._values[ i ];
            float x2 = integrate( x1, dx_dt, dt );

            float spike = 0.f;
            if( x2 > max ) {
                spike = 1.f;
                x2 = reset;
            }

            _input1Integrated._values[ i ] = x2;
            _input1Spike     ._values[ i ] = spike;
//            _input1SpikeTraceNext = updateTrace( _input1SpikeTrace, spike, inputs, _input1SpikeTraceNext, _input1SpikeHistory );
        }
    }

//    protected int updateDelayed( FloatArray input, FloatArray delayed, int next, int delay ) {
//        // implement a delayed connection. must react exactly once to each spike.
//        // can be implemented
//        window._values[ next ] = value;
//    }
//
//    // needs to become a matrix
//    protected int updateTrace( FloatArray window, float value, int variables, int variable, int nextIndex, int windowLength ) {
//
//        int offset = variable * variables + nextIndex;
//
//        window._values[ offset ] = value;
//
//        ++nextIndex;
//        if( nextIndex == windowLength ) {
//            nextIndex = 0;
//        }
//
//        return nextIndex;
//    }

Data _input1Integrated; // @ t
Data _input1Spike; // @ t
Data _input1SpikeTrace; // @ t
int _input1SpikeTraceNext;
int _input1SpikeHistory;

    protected void updateSpikes() {

    }

    protected void resetCells() {

    }

    protected void updateSynapses() {

    }

    protected void resetCell( float v, float timer, float theta ) {
//        if test_mode:
//          scr_e = 'v = v_reset_e; timer = 0*ms' // reset equation
//        else:
//          tc_theta = 1e7 * b.ms
//          theta_plus_e = 0.05 * b.mV
//          scr_e = 'v = v_reset_e; theta += theta_plus_e; timer = 0*ms'
//        offset = 20.0*b.mV
//        v_thresh_e = '(v>(theta - offset + ' + str(v_thresh_e) + ')) * (timer>refrac_e)'
        boolean test_mode = false;

        if( test_mode ) {
            v = v_reset_e;
            timer = 0 * ms;
        }
        else {
            float tc_theta = 1e7f * ms;
            float theta_plus_e = 0.05f * mV;
            v = v_reset_e;
            theta += theta_plus_e;
            timer = 0*ms;
        }
    }

    protected void updateCell( float v, float ge, float gi, float timer, float theta, float dt ) {

        float offset = 20.0f * mV;
//        if( ( v > ( theta - offset + v_thresh_e )) * ( timer > refrac_e ) ) {
        if( ( v > ( theta - offset + v_thresh_e )) && ( timer > refrac_e ) ) {
            // spike
        }

//        myconnection=Connection(group1,group2,'ge')
//        defines a connection from group group1 to group2, acting on variable ge. When neurons from group group1 spike, the variable ge of the target neurons in group group2 are incremented. When the connection object is initialised, the

//        Synapses are modeled by conductance changes, i.e., synapses increase their conductance instantaneously by the
//        synaptic weight w when a presynaptic spike arrives at the synapse, otherwise the conductance is decaying exponentially.

//    neuron_eqs_e = '''
        float I_synE = ge * nS *         -v;   //                        : amp
        float I_synI = gi * nS * (-100f * mV -v ); //                         : amp
        float dv_dt = ( ( v_rest_e - v ) + ( I_synE + I_synI ) / nS) / (100 * ms );//  : volt
        float dge_dt = -ge / ( 1.0f * ms );//                                   : 1
        float dgi_dt = -gi / ( 2.0f * ms );//                                   : 1

        float v2 = integrate( v, dv_dt, dt );
        float ge2 = integrate( v, dge_dt, dt );
        float gi2 = integrate( v, dgi_dt, dt );

        // E only
//        if test_mode:
//        neuron_eqs_e += '\n  theta      :volt'
//        else:
//        neuron_eqs_e += '\n  dtheta/dt = -theta / (tc_theta)  : volt'
//        neuron_eqs_e += '\n  dtimer/dt = 100.0  : ms'
        boolean test_mode = false;
        if( test_mode ) {

        }
        else {
            float tc_theta = 1e7f * ms;
            float dtheta_dt = -theta / (tc_theta);//  : volt'
            float dtimer_dt = 100.0f;  //: ms'

            float theta2 = integrate( v, dtheta_dt, dt );
            float timer2 = integrate( v, dtimer_dt, dt );
        }

    }


//    neuron_eqs_e = '''
//    neuron_eqs_i = '''
//    dv/dt = ((v_rest_e - v) + (I_synE+I_synI) / nS) / (100*ms)  : volt
//    dv/dt = ((v_rest_i - v) + (I_synE+I_synI) / nS) / (10*ms)  : volt   <--- different constant

//            I_synE = ge * nS *         -v                           : amp
//            I_synE = ge * nS *         -v                           : amp
//            I_synI = gi * nS * (-100.*mV-v)                          : amp
//            I_synI = gi * nS * (-85.*mV-v)                          : amp      <---- different constant

//    dge/dt = -ge/(1.0*ms)                                   : 1
//    dge/dt = -ge/(1.0*ms)                                   : 1
//    dgi/dt = -gi/(2.0*ms)                                  : 1
//    dgi/dt = -gi/(2.0*ms)                                  : 1

    protected float integrate( float x, float dx_dt, float dt ) {
//        float dx_dt = d.at( x );
        float x2 = x + ( dx_dt * dt );
        return x2;
    }

    // variables per synapse:
    // w           - learned
    // pre         - integrated
    // post1       - integrated   reset on post-spike
    // post2       - integrated   reset on post-spike
    // post2before - seems redundant

    public void onSpikePre( float pre, float w, float post1 ) {
//    eqs_stdp_pre_ee = 'pre = 1.;   w -= nu_ee_pre * post1'
        pre = 1f;
        w = w - nu_ee_pre * post1;
    }

    public void onSpikePost( float post2before, float post2, float w, float pre, float post1 ) {
//    eqs_stdp_post_ee = 'post2before = post2; w += nu_ee_post * pre * post2before; post1 = 1.; post2 = 1.'
        post2before = post2;
        w += nu_ee_post * pre * post2before;
        post1 = 1f;
        post2 = 1f;
    }

    public void onUpdateSynapse( float post1, float post2, float pre, float dt ) {
        float post2before = 0f;//                           : 1.0
        float dpre_dt   =   -pre/(tc_pre_ee);//         : 1.0
        float dpost1_dt  = -post1/(tc_post_1_ee);//     : 1.0
        float dpost2_dt  = -post2/(tc_post_2_ee);//     : 1.0

        float pre2 = integrate( pre, dpre_dt, dt );
        // ditto
    }

// http://briansimulator.org/docs/stdp.html
//    eqs_stdp_ee = '''
//    post2before                            : 1.0
//    dpre/dt   =   -pre/(tc_pre_ee)         : 1.0
//    dpost1/dt  = -post1/(tc_post_1_ee)     : 1.0
//    dpost2/dt  = -post2/(tc_post_2_ee)     : 1.0
//            '''
//    eqs_stdp_pre_ee = 'pre = 1.; w -= nu_ee_pre * post1'
//    eqs_stdp_post_ee = 'post2before = post2; w += nu_ee_post * pre * post2before; post1 = 1.; post2 = 1.'
    // brian:
//    stdp=STDP(myconnection,eqs=eqs_stdp,pre='A_pre+=dA_pre;w+=A_post',
//              post='A_post+=dA_post;w+=A_pre',wmax=gmax)
//    The STDP object acts on the Connection object myconnection.
//    if 'ee' in recurrent_conn_names:
//    stdp_methods[name+'e'+name+'e'] = b.STDP(connections[name+'e'+name+'e'], eqs=eqs_stdp_ee, pre = eqs_stdp_pre_ee,
//    post = eqs_stdp_post_ee, wmin=0., wmax= wmax_ee)

//    Equations of the synaptic variables are given in a string (argument eqs) as for defining neuron models. When a
// presynaptic (postsynaptic) spike is received, the code pre (post) is executed, where the special identifier w stands
// for the synaptic weight (from the specified connection matrix).
// Optionally, an upper limit can be specified for the synaptic weights (wmax).

//    By default, transmission delays are assumed to be axonal, i.e., synapses are located on the soma:
// if the delay of the connection C is d, then presynaptic spikes act after a delay d while postsynaptic
//  spikes act immediately.
// This behaviour can be overriden with the keywords delay_pre and delay_post, in both classes STDP and Exponential STDP.

    // Questions:
    // 1. post2before - does nothing?
    // 2. v_thresh_e equation - is it supposed to be AND of 2 conditions?

}
