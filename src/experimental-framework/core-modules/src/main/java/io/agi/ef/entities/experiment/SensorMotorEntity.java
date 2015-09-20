package io.agi.ef.entities.experiment;

import io.agi.ef.Entity;
import io.agi.ef.http.node.Node;

import java.util.HashSet;

/**
 * Since depending on the problem the Sensor and Motor entities may be better associated with the Agent OR the World,
 * we easily allow both: This class contains the necessary utilities. Agent and World derive from this class so you DRY.
 *
 * Created by dave on 19/09/15.
 */
public class SensorMotorEntity extends Entity {

    /**
     * Adds a sensor entity to the current node as a child of this entity.
     * @param entityName
     * @param entityConfig
     */
    public void addSensor( String entityName, String entityConfig ) {
        Node n = Node.getInstance();
        String nodeName = n.getName();
        addSensor( nodeName, entityName, entityConfig );
    }

    /**
     * Adds a Sensor entity to the specified Node as a child of this entity.
     * @param nodeName
     * @param entityName
     * @param entityConfig
     */
    public void addSensor( String nodeName, String entityName, String entityConfig ) {
        String entityType = Sensor.ENTITY_TYPE;
        addChildEntity(nodeName, entityName, entityType, entityConfig);
    }

    /**
     * Adds a motor entity to the current node as a child of this entity.
     * @param entityName
     * @param entityConfig
     */
    public void addMotor( String entityName, String entityConfig ) {
        Node n = Node.getInstance();
        String nodeName = n.getName();
        addMotor(nodeName, entityName, entityConfig);
    }

    /**
     * Adds a Motor entity to the specified Node as a child of this entity.
     * @param nodeName
     * @param entityName
     * @param entityConfig
     */
    public void addMotor( String nodeName, String entityName, String entityConfig ) {
        String entityType = Motor.ENTITY_TYPE;
        addChildEntity(nodeName, entityName, entityType, entityConfig);
    }

    public HashSet< String > getSensors() {
        return getChildEntitiesOfType( Sensor.ENTITY_TYPE );
    }

    public HashSet< String > getMotors() {
        return getChildEntitiesOfType( Motor.ENTITY_TYPE );
    }

}
