/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package io.agi.ef.dave;

import io.agi.core.orm.AbstractFactory;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Base class for convenience setting up an experiment in which an Agent interacts
 * with a World. Entities are created by externally defined factories, which 
 * include all the parameters needed to instantiate them.
 * 
 * @author dave
 */
public class FactoryExperiment implements Experiment {

    ArrayList<Agent> _agents = new ArrayList<Agent>();
    World _world;

    public FactoryExperiment() {
        
    }
   
    public void createAgent( AbstractFactory<Agent> agentFactory, String name ) {
        Agent a = agentFactory.create();
        a.setExperiment( this );
        a.setName( name );
        _agents.add( a );        
    }

    public void createWorld( AbstractFactory<World> worldFactory ) {
        World w = worldFactory.create();
        setWorld( w );
    }
    
    @Override public World getWorld() {
        return _world;
    }
    @Override public void setWorld( World w ) {
        w.setExperiment( this );
        _world = w;
    }
    
    @Override public Collection<Agent> getAgents() {
        return _agents;
    }

    @Override public Agent getAgent( String name ) {
        for( Agent a : _agents ) {
            if( a.getName().equals( name ) ) {
                return a;
            }
        }
        return null;
    }

    @Override public void addAgent( Agent a ) {
        _agents.add( a );
    }
    
}
