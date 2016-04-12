package io.agi.framework.demo.light;

import io.agi.core.orm.ObjectMap;
import io.agi.framework.Entity;
import io.agi.framework.factories.CommonEntityFactory;
import io.agi.framework.persistence.models.ModelEntity;

/**
 * Created by dave on 20/02/16.
 */
public class LightEntityFactory extends CommonEntityFactory {

    public LightEntityFactory() {

    }

    public Entity create( ObjectMap objectMap, ModelEntity modelEntity ) {

        Entity e = super.create( objectMap, modelEntity );

        if ( e != null ) {
            return e;
        }

        String entityType = modelEntity.type;

        if ( entityType.equals( LightSourceEntity.ENTITY_TYPE ) ) {
            return new LightSourceEntity( objectMap, _n, modelEntity );
        }

        if ( entityType.equals( LightControlEntity.ENTITY_TYPE ) ) {
            return new LightControlEntity( objectMap, _n, modelEntity );
        }

        return null;
    }

}
