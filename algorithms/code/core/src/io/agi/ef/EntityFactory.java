package io.agi.ef;

import io.agi.core.orm.ObjectMap;

/**
 * Created by dave on 14/02/16.
 */
public interface EntityFactory {

    Entity create( ObjectMap om, String entityName, String entityType );
}
