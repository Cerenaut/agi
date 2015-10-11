package io.agi.ef.entities.experiment;

import io.agi.ef.Entity;
import io.agi.ef.entities.experiment.old_entities.AbstractEntity;
import io.agi.ef.http.node.Node;

import java.util.HashSet;
import java.util.logging.Level;

/**
 *
 *  One of the major modules of the AGIEF. This the base class World.
 *  Inherit it and create your own custom worlds.
 *
 * Created by gideon on 1/08/15.
 */
public class World extends SensorMotorEntity {

    public static final String ENTITY_TYPE = "world";

    public World() {
    }

}
