package io.agi.framework.demo.light;

import io.agi.framework.Framework;
import io.agi.framework.Main;
import io.agi.framework.Node;

/**
 * A simple interaction between two entities.
 *
 * Created by dave on 18/02/16.
 */
public class LightDemo {

    public static void main( String[] args ) {

        // Provide classes for entities
        LightEntityFactory ef = new LightEntityFactory();

        // Create a Node
        Main m = new Main();
        m.setup( args[ 0 ], null, ef );

        // Create custom entities and references
        if( args.length > 1 ) {
            Framework.LoadEntities(args[1], m._p);
        }

        if( args.length > 2 ) {
            Framework.LoadDataReferences( args[ 2 ], m._p );
        }

        // Start the system
        m.run();
    }

}
