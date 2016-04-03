package io.agi.framework.demo.light;

import io.agi.core.orm.ObjectMap;
import io.agi.framework.Entity;
import io.agi.framework.entities.ExperimentEntity;
import io.agi.framework.factories.CommonEntityFactory;
import io.agi.framework.persistence.models.ModelEntity;

/**
 * Created by dave on 20/02/16.
 */
public class LightEntityFactory extends CommonEntityFactory {

    public LightEntityFactory() {

    }

    public Entity create( ObjectMap om, ModelEntity me ) {

        Entity e = super.create( om, me );

        if ( e != null ) {
            return e;
        }

        String entityType = me.type;

        if ( entityType.equals( LightSourceEntity.ENTITY_TYPE ) ) {
            return new LightSourceEntity( om, _n, me );
        }

        if ( entityType.equals( LightControlEntity.ENTITY_TYPE ) ) {
            return new LightControlEntity( om, _n, me );
        }

        return null;
    }

}
