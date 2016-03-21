package io.agi.framework.demo.dsom;

import io.agi.core.orm.Keys;
import io.agi.framework.*;
import io.agi.framework.Persistence.Persistence;
import io.agi.framework.Persistence.PropertyConverter;
import io.agi.framework.Persistence.sql.JdbcPersistence;
import io.agi.framework.entities.CommonEntityFactory;
import io.agi.framework.entities.DynamicSelfOrganizingMapEntity;
import io.agi.framework.entities.RandomVectorEntity;
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

        // Programmatic hook to create entities and references..
        createEntities( m._n );

        // Start the system
        m.run();
    }

    public static void createEntities( Node n ) {

        // Define some entities
        String randomVectorName = "model";
        String classifierName = "classifier";

        ModelEntity model = new ModelEntity( randomVectorName, RandomVectorEntity.ENTITY_TYPE, n.getName(), null );
        ModelEntity classifier = new ModelEntity( classifierName, DynamicSelfOrganizingMapEntity.ENTITY_TYPE, n.getName(), randomVectorName ); // linked, so we only need to update the problem

        Persistence p = n.getPersistence();
        p.setEntity( classifier );
        p.setEntity( model );

        JdbcPersistence jp = (JdbcPersistence)p;
        PropertyConverter propertyConverter = new PropertyConverter( jp );

        // Connect the entities
        Entity.SetDataReference(p, classifierName, DynamicSelfOrganizingMapEntity.INPUT, randomVectorName, RandomVectorEntity.OUTPUT);

        // Set a property:
        int elements = 2; // 2D
        String elementsKey = Keys.concatenate(randomVectorName, RandomVectorEntity.ELEMENTS);
        propertyConverter.setPropertyInt( elementsKey, elements );

        String resetKey = Keys.concatenate( classifierName, Entity.SUFFIX_RESET );
        propertyConverter.setPropertyBoolean( resetKey, true );
    }
}
