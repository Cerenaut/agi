package io.agi.framework.persistence;

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

    void setNode( ModelNode e );

    ModelNode getNode( String nodeName );

    void removeNode( String nodeName );

    // Entities
    Collection< ModelEntity > getEntities(); // list all

    Collection< String > getChildEntities( String parent );

    void setEntity( ModelEntity e );

    ModelEntity getEntity( String key );

    void removeEntity( String key );

    // Data
//    Collection< String > getDataKeys();
    void setData( ModelData e );

    ModelData getData( String key );

    void removeData( String key );

    // Properties
    String getPropertyString( String key, String defaultValue );

    void setPropertyString( String key, String value );

    Map< String, String > getProperties( String filter );


    void getPropertyObject( String key, Object propertyObject );
    void setPropertyObject( String key, Object propertyObject );

}