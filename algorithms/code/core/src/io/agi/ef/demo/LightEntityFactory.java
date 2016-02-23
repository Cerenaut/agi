package io.agi.ef.demo;

import io.agi.core.orm.ObjectMap;
import io.agi.ef.Entity;
import io.agi.ef.EntityFactory;
import io.agi.ef.Node;

/**
 * Created by dave on 20/02/16.
 */
public class LightEntityFactory implements EntityFactory {

    protected Node _n;

    public LightEntityFactory() {

    }

    public void setNode( Node n ) {
        _n = n;
    }

    public Entity create( ObjectMap om, String entityName, String entityType ) {
        if( entityType.equals( LightSource.ENTITY_TYPE ) ) {
            return new LightSource( entityName, om, LightSource.ENTITY_TYPE, null, _n );
        }
        if( entityType.equals( LightControl.ENTITY_TYPE ) ) {
            return new LightControl( entityName, om, LightControl.ENTITY_TYPE, null, _n );
        }
        return null;
    }

}
