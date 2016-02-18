package io.agi.ef;

/**
 * Created by dave on 14/02/16.
 */
public interface EntityFactory {

    Entity create( String entityName, String entityType );
}
