/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package io.agi.ef.dave.demo;


import io.agi.core.orm.AbstractFactory;
import io.agi.ef.dave.Agent;
import io.agi.ef.dave.FactoryExperiment;
import io.agi.ef.dave.World;

/**
 *
 * @author dave
 */
public class Main {

    
    public static void main( String[] args ) {

        final boolean gradient = Boolean.valueOf( args[ 0 ] );
        final float initial    = Float  .valueOf( args[ 1 ] );
        final float delta      = Float  .valueOf( args[ 2 ] );
        
        final int interval = 100;
        String agentName = "Bond, James Bond";
        
        AbstractFactory< Agent > af = new AbstractFactory< Agent >() {
            @Override public Agent create() {
                OptimizerAgent oa = new OptimizerAgent();
                oa.setup( gradient, initial, delta, interval );
                return oa;
            }
        };

        AbstractFactory< World > wf = new AbstractFactory< World >() {
            @Override public World create() {
                ScalarWorld sw = new ScalarWorld();
                sw.setup( initial, interval );
                return sw;
            }
        };
        
        FactoryExperiment e = new FactoryExperiment();

        e.createWorld( wf );
        e.createAgent( af, agentName );

        e.getWorld().start();
        e.getAgent( agentName ).start();
        
        while( true ) {
            System.out.println( "." );
            try {
                Thread.sleep( 200 );
            }
            catch( InterruptedException ie ) {
                // nothing
            }
        }
    }
    
}
