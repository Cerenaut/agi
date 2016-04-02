package io.agi.framework.entities;

import io.agi.core.ann.unsupervised.*;
import io.agi.core.orm.ObjectMap;
import io.agi.framework.DataFlags;
import io.agi.framework.Entity;
import io.agi.framework.Node;
import io.agi.framework.persistence.models.ModelEntity;

import java.util.Collection;

/**
 * Features:
 * - Name: Each experiment should have a unique name
 * - Update: updates all child entities.
 * - Objective/Cost function: Defines some function of performance or quality
 * - Termination Condition: Defines some condition on which update should stop
 * - Scheduler: Defines how many updates should be run.
 * - Import/Export: Allow import and export of complete subtree to disk.
 * - Logging: Log variables of interest as they change over time.
 * - Flush: All children can be flushed from the Node Cache on demand, or on export.
 *
 * Created by gideon on 20/03/2016.
 */
public class ExperimentEntity extends Entity {

    public ExperimentEntity( ObjectMap om, Node n, ModelEntity model ) {
        super( om, n, model );
    }

    @Override
    public void getInputKeys( Collection< String > keys ) {

    }

    @Override
    public void getOutputKeys( Collection< String > keys, DataFlags flags ) {

    }

    @Override
    public Class getConfigClass() {
        return ExperimentConfig.class;
    }

    @Override
    public void getPropertyKeys( Collection< String > keys ) {
        keys.add( SUFFIX_AGE );
        keys.add( SUFFIX_SEED );
        keys.add( SUFFIX_RESET );
    }
}
