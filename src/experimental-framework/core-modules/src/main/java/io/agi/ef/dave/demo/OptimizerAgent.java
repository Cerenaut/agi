/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package io.agi.ef.dave.demo;

import io.agi.core.data.Data;
import io.agi.core.opt.GradientDescentOptimizer;
import io.agi.core.opt.NewtonOptimizer;
import io.agi.core.opt.Optimizer;
import io.agi.core.opt.OptimizerFn;
import io.agi.ef.dave.Experiment;
import io.agi.ef.dave.World;
import io.agi.ef.dave.demo.AbstractAgent;

/**
 *
 * @author dave
 */
public class OptimizerAgent extends AbstractAgent implements OptimizerFn {
    
    public static final String KEY_SCORE = "score";

    Optimizer _o;
    
    public OptimizerAgent() {
        super();
        
        Data score = new Data( 1 );
        score.set( 0.f );

        setState( KEY_SCORE, score );
    }
    
    public void setup( boolean gradient, float initial, float delta, int interval ) {
        Optimizer o = null;
        if( gradient ) {
            o = new GradientDescentOptimizer( this, initial, delta );
        }
        else {
            o = new NewtonOptimizer( this, initial, delta );
        }
        
        _o = o;
        
        setInterval( interval );
        
        addStep( this );
    }
    
    @Override public void reset() {
        _o.reset();
    }
    
    
    /**
     * Called every step.
     */
    @Override public void call() {
        
        Experiment e = getExperiment();
        World w = e.getWorld();

        Data worldState = w.getStateSafe( ScalarWorld.KEY_STATE );
        float x1 = worldState._values[ 0 ];
        
        if( !lock( null ) ) {
            return;
        }

        _o._x = x1;
        _o.update();
        float x2 = (float)_o._x;
        float fx = (float)f( x2 );
        
        Data score = getState( KEY_SCORE );
        score._values[ 0 ] = fx;
        
        unlock();

        worldState._values[ 0 ] = (float)_o._x;
        w.setStateSafe( ScalarWorld.KEY_STATE, worldState );
        
        System.err.println( "Agent: Score = " + fx );        
    }
    
    /**
     * A simple test problem.
     * y = abs(x)^3
     * http://m.wolframalpha.com/input/?i=y%3D+abs%28x%29%5E3&x=0&y=0
     * @param x
     * @return 
     */
    @Override public double f( double x ) {
        double ax = Math.abs( x );
        return (ax * ax * ax);
    }
    
}
