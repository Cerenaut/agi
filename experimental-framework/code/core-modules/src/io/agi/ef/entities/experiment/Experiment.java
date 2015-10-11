package io.agi.ef.entities.experiment;

import io.agi.ef.Entity;
import io.agi.ef.entities.experiment.old_entities.AbstractEntity;
import io.agi.ef.interprocess.coordinator.ControlCommand;
import io.agi.ef.interprocess.coordinator.CoordinatorSlave;

import java.util.Collection;

/**
 * Created by gideon on 30/08/15.
 */
public class Experiment extends Entity {

    public static final String ENTITY_TYPE = "experiment";

    public void configure( String config ) {
    }
}
