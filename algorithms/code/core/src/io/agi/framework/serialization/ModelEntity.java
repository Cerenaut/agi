package io.agi.framework.serialization;

import io.agi.framework.Entity;

/**
 * Created by dave on 17/02/16.
 */
public class ModelEntity {

    public String name;
    public String type;
    public String node;
    public String parent;

    public ModelEntity( String name, String type, String node, String parent ) {
        this.name = name;
        this.type = type;
        this.node = node;
        this.parent = parent;
    }

    public ModelEntity( Entity e ) {
        name = e.getName();
        type = e.getType();
        node = e.getNode().getName();
        parent = e.getParent();
    }

}
