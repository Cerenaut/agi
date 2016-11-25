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

/**
 * STDP.
 * Created by dave on 17/07/16.
 */
public class SpikeTimingDependentPlasticity {


//    2.5. Input Encoding
//    The input to the network is based on the MNIST dataset which
//    contains 60,000 training examples and 10,000 test examples of
//    28 × 28 pixel images of the digits 0–9 (LeCun et al., 1998). The
//    input is presented to the network for 350 ms in the form of
//    Poisson-distributed spike trains, with firing rates proportional to
//    the intensity of the pixels of the MNIST images. Specifically, the
//    maximum pixel intensity of 255 is divided by 4, resulting in input
//    firing rates between 0 and 63.75 Hz. Additionally, if the excitatory
//    neurons in the second layer fire less than five spikes within 350
//    ms, the maximum input firing rate is increased by 32 Hz and the
//    example is presented again for 350 ms. This process is repeated
//    until at least five spikes have been fired during the entire time the
//    particular example was presented.

//    Before presenting a new image, there is a 150 ms phase without any input to allow all
//    variables of all neurons decay to their resting values (except for
//            the adaptive threshold).

    public void update2() {
        // To simulate our SNNs we used Python and the BRIAN simulator (Goodman and Brette, 2008) 1

        // To model neuron dynamics, we chose the leaky integrate-and-fire
        // model. The membrane voltage V is described by
        float tau = 0.1f; // time constant which is longer for excitatory neurons than for inhibitory neurons
        float V = 0.f; // membrane voltage
        float dV_dt = 0.0f; // derivative of membrane voltage over time
        float E_rest = 0.f; // E_rest is the resting membrane potential
        float E_inh = 0f; // E_inh the equilibrium potential of inhibitory synapses
        float E_exc = 0f; // E_exc the equilibrium potential of excitatory synapses
        float g_e = 0f;// the conductances of excitatory synapses
        float g_i = 0f;// the conductances of inhibitory synapses

        // When the neuron’s membrane potential crosses its
        // membrane threshold v thres , the neuron fires and its membrane
        // potential is reset to v reset

        float v_thres = 0f; // threshold to fire
        float v_reset = 0f; // membrane potential after firing

        float tauDerivative = (E_rest -V) - + g_e * (E_exc - V) + g_i * (E_inh - V);
        tauDerivative = tau * dV_dt;


        // Synapses are modeled by conductance changes, i.e., synapses
        // increase their conductance instantaneously by the synaptic
        // weight w when a presynaptic spike arrives at the synapse,
        // otherwise the conductance is decaying exponentially.

        // If the presynaptic neuron is excitatory, the dynamics of the
        // conductance g_e are:
        //tau_ge * dge_dt = -g_e;
        // where tau_ge = is the time constant of an excitatory postsynaptic potential.

        //Similarly, if the presynaptic neuron is inhibitory, a
        //conductance g)i is updated using the same equation but with the
        //time constant of the inhibitory postsynaptic potential τ_gi .
        //g_i
        float eta = 0f; // nu, learning rate - greek eta
        float w_max = 0f; //is the maximum weight
        float w = 0f; // old weight
        float Mu = 0f;// determines the dependence of the update on the previous weight
        float x_pre = 0f; //
        float x_tar = 0f; // is the target value of the presynaptic trace at the moment
                          // of a postsynaptic spike. The higher the target value, the lower
                          // the synaptic weight will be.

        //delta_w = nu( x_pre − x_tar )( w_max − w )^μ
        float delta_w = eta * ( x_pre - x_tar ) * (float)Math.pow( ( w_max - w ), Mu );
        w = w + delta_w;

//        However, it is desirable that all neurons have
//        approximately equal firing rates to prevent single neurons from
//        dominating the response pattern and to ensure that the receptive
//        fields of the neurons differentiate. To achieve this, we employ
//        an adaptive membrane threshold resembling intrinsic plasticity
//                (Zhang and Linden, 2003). Specifically, each excitatory neuron’s
//        membrane threshold is not only determined by v thresh but by the
//        sum v thresh + θ , where θ is increased every time the neuron fires
//        and is exponentially decaying (Querlioz et al., 2013).

//        Using this mechanism, the firing rate of
//        the neurons is limited because the conductance-based synapse
//        model limits the maximum membrane potential to the excitatory
//        reversal potential E exc , i.e., once the neuron membrane threshold
//        is close to E exc (or higher) it will fire less often (or even stop firing
//                completely) until θ decreases sufficiently.
    }

//    private static float g_e( float r ) {
//
//    }
//
//    private static float g_i( float r ) {
//
//    }

    // Brian update:
    // http://briansimulator.org/docs/simulation.html
    // When a simulation is run, the operations are done in the following order by default:
    //
    // 1. Update every NeuronGroup, this typically performs an integration time step for the differential equations defining the neuron model.
    // 2. Check the threshold condition and propagate the spikes to the target neurons.
    // 3. Update every Synapses, this may include updating the state of targeted NeuronGroup objects
    // 4. Reset all neurons that spiked.
    // 5. Call all user-defined operations and state monitors.

    // http://briansimulator.org/docs/equations.html
    // A Network object holds a collection of objets that can be run, i.e., objects with class NeuronGroup, Connection, SpikeMonitor, StateMonitor
    //Alternatively, the string defining the equations can be evaluated within a given namespace by providing keywords at initialisation time, e.g.:
    // eqs=Equations('dx/dt=-x/tau : volt',tau=10*ms)

    //Numerical integration
    //The currently available integration methods are:
    // Exact integration when the equations are linear.
    // Euler integration (explicit, first order).
    // Runge-Kutta integration (explicit, second order).
    // Exponential Euler integration (implicit, first order).
    // The method is selected when a NeuronGroup is initialized. If the equations are linear, exact integration is automatically selected. Otherwise, Euler integration is selected by default, unless the keyword implicit=True is passed, which selects the exponential Euler method. A second-order method can be selected using the keyword order=2

    // neuron_groups['e'] = b.NeuronGroup(n_e*len(population_names), neuron_eqs_e, threshold= v_thresh_e, refractory= refrac_e, reset= scr_e, compile = True, freeze = True)
    // neuron_groups['i'] = b.NeuronGroup(n_i*len(population_names), neuron_eqs_i, threshold= v_thresh_i, refractory= refrac_i, reset= v_reset_i, compile = True, freeze = True)


    // Stochastic differential equations
    // Noise is introduced in differential equations with the keyword xi, which means normalised gaussian noise (the derivative of the Brownian term). Currently, this is implemented simply by adding a normal random number to the variable at the end of the integration step (independently for each neuron). The unit of white noise is non-trivial, it is second**(-.5). Thus, a typical stochastic equation reads:


//    Increasing the time constant of the excitatory
//    neuron membrane potential to 100 ms (from 10 to 20 ms that are
//            typically observed for biological neurons), greatly increased the
//    classification accuracy. The reason is that rate-coding is used to
//    represent the input, see Section 2.5, and therefore longer neuron
//    membrane constants allow for better estimation of the input
//    spiking rate. For example, if the recognition neuron can only
//    integrate inputs over 20 ms at a maximum input rate of 63.75 Hz,
//    the neuron will only integrate over 1.275 spikes on average, which
//    means that a single noise spike would have a large influence. By
//    increasing the membrane time constant to 100 ms, a neuron can
//    integrate over 6.375 spikes on average,


    // INPUT LAYER:
    // 28 x 28 neurons (one per input value or px)

    // PRocessing layer:
    //  - Excitatory layer
    //  - Inhibitory layer (same size)

    // Excitatory excites 1, inhib. inhib all.
    // All inputs to all excitatory neurons.

    // https://github.com/peter-u-diehl/stdp-mnist/blob/master/Diehl%26Cook_spiking_MNIST.py
    // import brian as b
    float b_second = 0; // inferred or unreferenced
  //  int num_examples = 0;

    //b.set_global_preferences(
    //defaultclock = b.Clock(dt=0.5*b.ms), # The default clock to use if none is provided or defined in any enclosing scope.

    // test
    //int num_examples = 10000 * 1; //
    // training
    int num_examples = 60000 * 3; //

    // copied from source
    float n_input = 784;
    float n_e = 400;
    float n_i = n_e;
    float single_example_time = 0.35f * b_second;
    float resting_time = 0.15f * b_second;
    float runtime = num_examples * ( single_example_time + resting_time);
//    if( num_examples <= 10000 ) {
////        int update_interval = num_examples;
//        int weight_update_interval = 20;
//    }
//    else {
////        int update_interval = 10000
//        int weight_update_interval = 100
//    }
//    update_interval = 10000
   /* B b = new B();

    float v_rest_e = -65.f * b.mV;
    float v_rest_i = -60.f * b.mV;
    float v_reset_e = -65.f * b.mV;
    float v_reset_i = -45.f * b.mV;
    float v_thresh_e = -52.f * b.mV;
    float v_thresh_i = -40.f * b.mV;
    float refrac_e = 5.f * b.ms;
    float refrac_i = 2.f * b.ms;

    //input_population_names = ['X']
    //population_names = ['A']
    //input_connection_names = ['XA']
    //save_conns = ['XeAe']
    // weight['ee_input'] = 78f;
    // delay['ee_input'] = (0*b.ms,10*b.ms);
    // delay['ei_input'] = (0*b.ms,5*b.ms);

    float input_intensity = 2.f;
    float start_input_intensity = input_intensity;
    float tc_pre_ee = 20*b.ms;
    float tc_post_1_ee = 20*b.ms;
    float tc_post_2_ee = 40*b.ms;
    float nu_ee_pre =  0.0001f;//      # learning rate
    float nu_ee_post = 0.01f;//       # learning rate
    float wmax_ee = 1.0f;
    float exp_ee_pre = 0.2f;
    float exp_ee_post = exp_ee_pre;
    float STDP_offset = 0.4f;
*/
//    if test_mode:
//      scr_e = 'v = v_reset_e; timer = 0*ms'
//    else:
//      tc_theta = 1e7 * b.ms
//      theta_plus_e = 0.05 * b.mV
//      scr_e = 'v = v_reset_e; theta += theta_plus_e; timer = 0*ms'
//    offset = 20.0*b.mV
//    v_thresh_e = '(v>(theta - offset + ' + str(v_thresh_e) + ')) * (timer>refrac_e)'


//    neuron_eqs_e = '''
//    dv/dt = ((v_rest_e - v) + (I_synE+I_synI) / nS) / (100*ms)  : volt
//            I_synE = ge * nS *         -v                           : amp
//            I_synI = gi * nS * (-100.*mV-v)                          : amp
//    dge/dt = -ge/(1.0*ms)                                   : 1
//    dgi/dt = -gi/(2.0*ms)                                  : 1
//            '''
//    if test_mode:
//      neuron_eqs_e += '\n  theta      :volt'
//    else:
//      neuron_eqs_e += '\n  dtheta/dt = -theta / (tc_theta)  : volt'
//    neuron_eqs_e += '\n  dtimer/dt = 100.0  : ms'

//    neuron_eqs_i = '''
//    dv/dt = ((v_rest_i - v) + (I_synE+I_synI) / nS) / (10*ms)  : volt
//            I_synE = ge * nS *         -v                           : amp
//            I_synI = gi * nS * (-85.*mV-v)                          : amp
//    dge/dt = -ge/(1.0*ms)                                   : 1
//    dgi/dt = -gi/(2.0*ms)                                  : 1
//            '''
//    eqs_stdp_ee = '''
//    post2before                            : 1.0
//    dpre/dt   =   -pre/(tc_pre_ee)         : 1.0
//    dpost1/dt  = -post1/(tc_post_1_ee)     : 1.0
//    dpost2/dt  = -post2/(tc_post_2_ee)     : 1.0
//            '''
//    eqs_stdp_pre_ee = 'pre = 1.; w -= nu_ee_pre * post1'
//    eqs_stdp_post_ee = 'post2before = post2; w += nu_ee_post * pre * post2before; po

//    neuron_groups[name+'e'].v = v_rest_e - 40. * b.mV
//    neuron_groups[name+'i'].v = v_rest_i - 40. * b.mV

// Querlioz et al. (2013)
//    Querlioz, D., Bichler, O., Dollfus, P., and Gamrat, C. (2013). Immunity to
//    device variations in a spiking neural network with memristive nanodevices.
//    Nanotechnol. IEEE Trans. 12, 288–295. doi: 10.1109/TNANO.2013.2250995

}

// class B {
//     float mV = 0;
//     float ms= 0;
//}