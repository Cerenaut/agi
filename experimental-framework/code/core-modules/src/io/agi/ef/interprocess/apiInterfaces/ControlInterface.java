package io.agi.ef.interprocess.apiInterfaces;

import io.agi.ef.Entity;

/**
 * Created by gideon on 30/07/15.
 */
public interface ControlInterface {

// renamed to : entity/name,action
//    Response command( String entityName, String command );

// deprecated:
//    Response status( String entityName, String state );

    /**
     * The Node has received an instruction to terminate gracefully.
     */
    public void onStopEvent();

    /**
     * The Node has received an event indicating that an action has happened to a data structure.
     * Broadcast the event to all Entities at this Node.
     * @param data
     * @param action
     */
    public void onDataEvent(String data, String action);

    /**
     * The Node has received an event indicating that an action has happened to an entity.
     * Broadcast the event to all Entities at this Node.
     * @param entity
     * @param action
     */
    public void onEntityEvent(String entity, String action);

    /**
     * This Node has received a Create event, indicating that an Entity should be created at this Node.
     * Unlike other events, Create events are not re-broadcast to all Nodes but must be sent directly
     * to the actioning Node. Therefore, the Entity Will be created locally.
     * @param entityName
     * @param entityType
     * @param parentEntityName
     * @param entityConfig
     */
    public Entity onCreateEvent( String entityName, String entityType, String parentEntityName, String entityConfig );

}
