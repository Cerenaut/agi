/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package io.agi.ef.dave;

import io.agi.core.orm.Callback;

/**
 *
 * @author dave
 */
public abstract class AbstractAgent extends StatefulThread implements Agent, Callback {

    protected String _name;
    protected Experiment _e;
    
    public AbstractAgent() {
    }
    
    @Override public String getName() {
        return _name;
    }

    @Override public void setName( String name ) {
        _name = name;
    }
   
    @Override public Experiment getExperiment() {
        return _e;
    }

    @Override public void setExperiment( Experiment e ) {
        _e = e;
    }
    
    public World getWorld() {
        
        Experiment e = getExperiment();
        
        if( e != null ) {
            return e.getWorld();
        }
        
        return null;
    }
}
