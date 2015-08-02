/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package io.agi.core.demo;

import io.agi.core.data.Data;
import io.agi.core.ef.AbstractWorld;
import io.agi.core.ef.Experiment;

/**
 * An inert world that has a single scalar state.
 * The state is printed at intervals.
 * The state is manipulated by one or more agents.
 * 
 * @author dave
 */
public class ScalarWorld extends AbstractWorld {
    
    public static final String KEY_STATE = "state";
    
    protected float _initial = 0.f;
    
    public ScalarWorld() { 
        super();
    }

    public void setup( float initial, int interval ) {
        
        _initial = initial;
        
        setInterval( interval );
        
        addStep( this );
        
        reset();
    }

    @Override public void reset() {
        Data state = new Data( 1 );
        state.set( _initial );
        setState( KEY_STATE, state );
    }
    
    @Override public void call() {

        if( !lock( null ) ) {
            return;
        }
        
        Data state = getState( KEY_STATE );
        float value = state._values[ 0 ];
        
        unlock();
        
        System.err.println( "World: Value = " + value );
    }
}
