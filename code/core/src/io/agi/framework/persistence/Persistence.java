package io.agi.framework.persistence;

import io.agi.framework.Node;
import io.agi.framework.persistence.models.ModelData;
import io.agi.framework.persistence.models.ModelEntity;
import io.agi.framework.persistence.models.ModelNode;

import java.util.Collection;

/**
 * Created by dave on 14/02/16.
 */
public interface Persistence {

//    Entity --< Data
//    Entity --- name, type, children, node, config (multiple properties)
//               Could be JSON string for entities: { name: xxx, type: yyy } etc

    // Nodes
    Collection< ModelNode > fetchNodes(); // list all

    void persistNode( ModelNode m ); /// creates if nonexistent (upsert)

    ModelNode fetchNode( String nodeName ); /// retrieves if exists, or null

    void removeNode( String nodeName ); /// removes if exists

    // Entities
    Collection< ModelEntity > getEntities(); // list all

    Collection< String > getChildEntities( String parent );

    void persistEntity( ModelEntity m );

    ModelEntity fetchEntity( String name );

    void removeEntity( String name );

    // Data
    void persistData( ModelData modelData );

    ModelData fetchData( String name );

    void removeData( String name );

}