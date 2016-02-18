package io.agi.ef.serialization;

import io.agi.ef.Entity;
import io.agi.ef.Node;

/**
 * Created by dave on 17/02/16.
 */
public class JsonEntity {

    public String _key;
    public String _type;
    public String _node;
    public String _parent;

    public JsonEntity( String key, String type, String node, String parent ) {
        _key = key;
        _type = type;
        _node = node;
        _parent = parent;
    }

    public JsonEntity( Entity e ) {
        _key = e.getName();
        _type = e.getType();
        _node = e.getNode().getName();
        _parent = e.getParent();
    }

}
