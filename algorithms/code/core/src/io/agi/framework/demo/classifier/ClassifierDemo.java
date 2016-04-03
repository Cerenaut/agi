package io.agi.framework.demo.classifier;

import io.agi.core.orm.Keys;
import io.agi.framework.Entity;
import io.agi.framework.Framework;
import io.agi.framework.Main;
import io.agi.framework.Node;
import io.agi.framework.entities.*;
import io.agi.framework.factories.CommonEntityFactory;
import io.agi.framework.persistence.Persistence;
import io.agi.framework.persistence.models.ModelEntity;

/**
 * Code to demonstrate a DSOM Entity on a simple test problem.
 * <p>
 * Created by dave on 12/03/16.
 */
public class ClassifierDemo {

    public static void main( String[] args ) {

        // Provide classes for entities
        CommonEntityFactory ef = new CommonEntityFactory();

        // Create a Node
        Main m = new Main();
        m.setup( args[ 0 ], null, ef );

        // Create custom entities and references
        if( args.length > 1 ) {
            Framework.LoadEntities( m._n, args[1] );
        }

        if( args.length > 2 ) {
            Framework.LoadDataReferences( m._p, args[2] );
        }

        if( args.length > 3 ) {
            Framework.LoadConfigs( m._p, args[3] );
        }

        // Programmatic hook to create entities and references..
        createEntities( m._n );

        // Start the system
        m.run();
    }

    public static void createEntities( Node n ) {

        // Define some entities
        String modelName = "model";
        String classifierName = "classifier";

        Framework.CreateEntity(n, modelName, DiscreteRandomEntity.ENTITY_TYPE, n.getName(), null);
        Framework.CreateEntity(n, classifierName, GrowingNeuralGasEntity.ENTITY_TYPE, n.getName(), modelName);

        // Connect the entities
        Persistence p = n.getPersistence();

        Framework.SetDataReference( p, classifierName, DynamicSelfOrganizingMapEntity.INPUT, modelName, RandomVectorEntity.OUTPUT );

        // Set a property:
        Framework.SetConfig( p, modelName, "elements", "2" );
        Framework.SetConfig( p, classifierName, Entity.SUFFIX_RESET, "true" );
    }
}
