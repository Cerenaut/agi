package io.agi.framework.persistence.models;

import io.agi.framework.Entity;

/**
 * Created by dave on 17/02/16.
 */
public class ModelEntity {

    public String name;
    public String type;
    public String node;
    public String parent;
    public String config;

    public ModelEntity( String name, String type, String node, String parent, String config ) {
        this.name = name;
        this.type = type;
        this.node = node;
        this.parent = parent;
        this.config = config;
    }

    public ModelEntity( Entity e ) {
        name = e.getName();
        type = e.getType();
        node = e.getNode().getName();
        parent = e.getParent();
    }

}
