/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package io.agi.ef.dave;

/**
 * 
 * @author dave
 */
public interface World extends Asynchronous, Stateful {

    public void reset();
    public String getName();
    public void setName( String name );
    public Experiment getExperiment();
    public void setExperiment( Experiment e );
    
}
