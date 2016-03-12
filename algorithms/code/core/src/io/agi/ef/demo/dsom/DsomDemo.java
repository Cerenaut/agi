package io.agi.ef.demo.dsom;

import io.agi.ef.Main;
import io.agi.ef.Node;
import io.agi.ef.demo.light.LightEntityFactory;

/**
 * Code to demonstrate a DSOM Entity on a simple test problem.
 *
 * Created by dave on 12/03/16.
 */
public class DsomDemo {

    public static void main( String[] args ) {

        // Provide classes for entities
        LightEntityFactory ef = new LightEntityFactory();

        // Create a Node
        Main m = new Main();
        m.setup( args[0], null, ef );

        // Create custom entities and references
        if( args.length > 1 ) {
            m.loadEntities(args[1]);
        }

        if( args.length > 2 ) {
            m.loadReferences( args[ 2 ] );
        }

        // Programmatic hook to create entities and references..
        createEntities( m._n );

        // Start the system
        m.run();
    }

    public static void createEntities( Node n ) {

//        // Define some entities
//        String lightSourceName = "myLight";
//        String lightControlName = "mySwitch";
//
//        JsonEntity lightControl = new JsonEntity( lightControlName, LightControl.ENTITY_TYPE, n.getName(), null );
//        JsonEntity lightSource = new JsonEntity( lightSourceName, LightSource.ENTITY_TYPE, n.getName(), lightControlName );
//
//        Persistence p = n.getPersistence();
//        p.setEntity( lightSource );
//        p.setEntity( lightControl );
//
//        // Connect the entities
//        Entity.SetInputReference( p, lightSourceName, LightSource.CONTROL_INPUT, lightControlName, LightControl.CONTROL_OUTPUT );
    }
}
