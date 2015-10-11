/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package io.agi.core.orm;

/**
 * A callback that can be turned on and off.
 * 
 * @author dave
 */
public class ToggleCallback implements Callback {
   
    public boolean _enabled = true;
    public Callback _c;
    
    public ToggleCallback() {
        
    }

    public ToggleCallback( boolean enabled, Callback c ) {
        _enabled = enabled;
        _c = c;
    }
    
    @Override public void call() {
        if( _enabled ) {
            if( _c != null ) {
                _c.call();
            }
        }
    }
}
