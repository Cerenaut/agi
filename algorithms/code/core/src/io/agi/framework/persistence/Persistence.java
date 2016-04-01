package io.agi.framework.persistence;

import io.agi.framework.entities.EntityProperties;
import io.agi.framework.persistence.models.ModelData;
import io.agi.framework.persistence.models.ModelEntity;
import io.agi.framework.persistence.models.ModelNode;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dave on 14/02/16.
 */
public interface Persistence {

//    Entity --< Properties
//           --< Data
//    Entity --- name, type, children, node
//               Could be JSON string for entities: { name: xxx, type: yyy } etc


//    Collection<String> getChildren(String name);
//    Collection<String> getNodes();

    // Nodes
    Collection< ModelNode > getNodes(); // list all

    void setNode( ModelNode m ); /// creates if nonexistent (upsert)

    ModelNode getNode( String nodeName ); /// retrieves if exists, or null

    void removeNode( String nodeName ); /// removes if exists

    // Entities
    Collection< ModelEntity > getEntities(); // list all

    Collection< String > getChildEntities( String parent );

    void setEntity( ModelEntity m );

    ModelEntity getEntity( String name );

    void removeEntity( String name );

    // Data
//    Collection< String > getDataKeys();

    void setData( ModelData m );

    ModelData getData( String name );

    void removeData( String name );

    // Properties
//    Map< String, String > getProperties( String filter );
//
//    void getProperties( String key, EntityProperties properties );
//    void setProperties( String key, EntityProperties properties );
}