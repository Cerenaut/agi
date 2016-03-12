package io.agi.framework.serialization;

import io.agi.framework.Entity;

/**
 * Created by dave on 17/02/16.
 */
public class ModelEntity {

    public String _key;
    public String _type;
    public String _node;
    public String _parent;

    public ModelEntity( String key, String type, String node, String parent ) {
        _key = key;
        _type = type;
        _node = node;
        _parent = parent;
    }

    public ModelEntity( Entity e ) {
        _key = e.getName();
        _type = e.getType();
        _node = e.getNode().getName();
        _parent = e.getParent();
    }

}
