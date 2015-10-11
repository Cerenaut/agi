package io.agi.ef;

import io.agi.core.AbstractFactory;
import io.agi.core.ObjectMap;

import java.util.HashMap;

/**
 * Collection of factories for creation of Entities.
 * This convenience structure is used to group all the factories together.
 *
 * Created by dave on 14/09/15.
 */
public class EntityFactory {

    public static final String KEY_ENTITY_FACTORY = "entity-factory";

    HashMap< String, AbstractFactory< Entity >> _factories = new HashMap<>();

    public EntityFactory() {
        ObjectMap.Put( KEY_ENTITY_FACTORY, this );
    }

    /**
     * Apply the given factory to be used for old_entities of a particular type
     * @param entityType
     * @param af
     */
    public static boolean setFactory( String entityType, AbstractFactory af ) {
        EntityFactory ef = getInstance();
        ef._factories.put( entityType, af );

        // Tell the system this type is available.
        boolean b = Persistence.getInstance().addEntityType( entityType );
        return b;
    }

    /**
     * Create an entity of the given type, using the appropriate factory.
     * @param entityName
     * @param entityType
     * @param parentEntityName
     * @param entityConfig
     * @return
     */
    public static Entity create( String entityName, String entityType, String parentEntityName, String entityConfig ) {
        try {
            EntityFactory ef = getInstance();
            AbstractFactory af = ef._factories.get(entityType);
            Entity e = (Entity)af.create();
            e.setup( entityName, entityType, parentEntityName, entityConfig );
            return e;
        }
        catch( Exception e ) {
            e.printStackTrace();
            return null;
        }
    }
    /**
     * Get the reference to the collection of factories
     * @return
     */
    public static EntityFactory getInstance() {
        return (EntityFactory)ObjectMap.Get( KEY_ENTITY_FACTORY );
    }

}
