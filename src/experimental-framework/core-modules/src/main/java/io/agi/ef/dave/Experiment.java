/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package io.agi.ef.dave;

import io.agi.ef.dave.Agent;
import io.agi.ef.dave.World;

import java.util.Collection;

/**
 * An experiment is a World with zero or more Agents.
 * The world can iterate without Agents attached (akin to running a simulation).
 * 
 * @author dave
 */
public interface Experiment {

    public World getWorld();
    public void setWorld( World w );
    
    public Collection<Agent> getAgents();
    public Agent getAgent( String name );
    public void addAgent( Agent a );
    
}
