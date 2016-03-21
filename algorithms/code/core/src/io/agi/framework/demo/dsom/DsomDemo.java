package io.agi.framework.demo.dsom;

import io.agi.core.orm.Keys;
import io.agi.framework.Entity;
import io.agi.framework.Main;
import io.agi.framework.Node;
import io.agi.framework.entities.*;
import io.agi.framework.persistence.Persistence;
import io.agi.framework.serialization.ModelEntity;

/**
 * Code to demonstrate a DSOM Entity on a simple test problem.
 *
 * Created by dave on 12/03/16.
 */
public class DsomDemo {

    public static void main( String[] args ) {

        // Provide classes for entities
        CommonEntityFactory ef = new CommonEntityFactory();

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

        // Programmatic hook to Create entities and references..
        createEntities( m._n );

        // Start the system
        m.run();
    }

    public static void createEntities( Node n ) {

        // Define some entities
        String modelName = "model";
        String classifierName = "classifier";

        ModelEntity model = new ModelEntity( modelName, DiscreteRandomEntity.ENTITY_TYPE, n.getName(), null );
//        ModelEntity classifier = new ModelEntity( classifierName, DynamicSelfOrganizingMapEntity.ENTITY_TYPE, n.getName(), modelName ); // linked, so we only need to update the problem
        ModelEntity classifier = new ModelEntity( classifierName, GrowingNeuralGasEntity.ENTITY_TYPE, n.getName(), modelName ); // linked, so we only need to update the problem

        Persistence p = n.getPersistence();
        p.setEntity( classifier );
        p.setEntity( model );

        // Connect the entities
        Entity.SetDataReference(p, classifierName, DynamicSelfOrganizingMapEntity.INPUT, modelName, RandomVectorEntity.OUTPUT);

        // Set a property:
        int elements = 2; // 2D
        String elementsKey = Keys.concatenate(modelName, RandomVectorEntity.ELEMENTS);
        p.setPropertyInt(elementsKey, elements );

        String ageKey = Keys.concatenate( modelName, Entity.SUFFIX_AGE );
        p.setPropertyInt(ageKey, 0);

        String resetKey = Keys.concatenate( classifierName, Entity.SUFFIX_RESET );
        p.setPropertyBoolean( resetKey, true );
    }
}
